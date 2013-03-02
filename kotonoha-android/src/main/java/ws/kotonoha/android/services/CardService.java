/*
 * Copyright 2012 eiennohito
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.kotonoha.android.services;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.QueryBuilder;
import de.akquinet.android.androlog.Log;
import org.joda.time.DateTime;
import ws.kotonoha.android.db.DatabaseHelper;
import ws.kotonoha.android.util.ReviewCardComparator;
import ws.kotonoha.server.model.Oid;
import ws.kotonoha.server.model.events.MarkEvent;
import ws.kotonoha.server.model.learning.ItemLearning;
import ws.kotonoha.server.model.learning.ReviewCard;
import ws.kotonoha.server.model.learning.WordCard;

import java.sql.SQLException;
import java.util.*;

/**
 * @author eiennohito
 * @since 11.02.12
 */
public class CardService implements Purgeable {
  private final Object syncRoot = new Object();
  private final DataService dataService;
  private final RuntimeExceptionDao<WordCard, String> cardDao;
  private Map<String, WordCard> cards = new HashMap<String, WordCard>();
  private final RuntimeExceptionDao<ItemLearning, Long> learningDao;
  private final RuntimeExceptionDao<ReviewCard, String> reviewDao;
  private Queue<ReviewCard> reviewCards = makeQueue();

  private static PriorityQueue<ReviewCard> makeQueue() {
    return new PriorityQueue<ReviewCard>(80, new ReviewCardComparator());
  }


  public CardService(DataService dataService) {
    this.dataService = dataService;
    DatabaseHelper helper = dataService.getHelper();
    cardDao = helper.getWordCardDao();
    learningDao = helper.getLearningDao();
    reviewDao = helper.getReviewDao();
    clear();
    removeStale();
    cards = loadCards();
  }

  private Map<String, WordCard> loadCards() {
    List<WordCard> cards = cardDao.queryForEq("status", 0);
    Map<String, WordCard> map = new HashMap<String, WordCard>();
    for (WordCard card : cards) {
      map.put(card.getId(), card);
    }
    return map;
  }

  public boolean hasNextCard() {
    synchronized (syncRoot) {
      while (!reviewCards.isEmpty()) {
        ReviewCard rc = reviewCards.peek();
        if (!cards.containsKey(rc.getCid())) {
          reviewCards.poll();
        } else {
          return true;
        }
      }
      return false;
    }
  }

  public WordCard nextCard() {
    synchronized (syncRoot) {
      while (!reviewCards.isEmpty()) {
        final ReviewCard rc = reviewCards.poll();
        final WordCard card = cards.get(rc.getCid());
        if (card == null) continue;
        cards.remove(rc.getCid());
        card.setStatus(1);
        Scheduler.schedule(new Runnable() {
          @Override
          public void run() {
            cardDao.updateRaw("UPDATE wordcard SET status = 1 WHERE id = ?", rc.getCid());
          }
        });
        Log.d(this, String.format("Selected card %s <- %s", rc.getCid(), rc.getSource()));
        return card;
      }
      return null;
    }
  }

  private Random rand = new Random();

  public synchronized void process(final Collection<WordCard> crds, final Collection<ReviewCard> rcs) {
    long lid = rand.nextLong();
    for (WordCard card : crds) {
      card.setStatus(0);
      card.setGotOn(DateTime.now());
      ItemLearning lrn = card.getLearning();
      if (lrn != null) {
        lrn.setId(lid);
        lid += 1;
        learningDao.create(lrn);
      }
      cardDao.createIfNotExists(card);
    }
    for (ReviewCard rc : rcs) {
      Oid oid = new Oid((int) (rc.getSeq() >> 24), 0, (int) (rc.getSeq() & 0xfff));
      rc.setId(oid.toString());
      reviewDao.createIfNotExists(rc);
    }

    reloadCards();
  }

  public void reloadCards() {
    Map<String, WordCard> crds = loadCards();
    PriorityQueue<ReviewCard> q = makeQueue();
    List<ReviewCard> rcs = reviewDao.queryForAll();
    q.addAll(rcs);

    synchronized (syncRoot) {
      this.cards = crds;
      this.reviewCards = q;
    }
  }

  public int countPresent() {
    synchronized (syncRoot) {
      return cards.size();
    }
  }

  public void removeCardsFor(List<MarkEvent> marks) {
    List<String> ids = new ArrayList<String>(marks.size());
    for (MarkEvent e : marks) {
      ids.add(e.getCard());
    }
    dropCardsByIds(ids);
  }

  private void dropCardsByIds(Collection<String> ids) {
    try {
      removeLearningForCards(ids);
      cardDao.deleteIds(ids);
      DeleteBuilder<ReviewCard, String> bldr = reviewDao.deleteBuilder();
      bldr.where().in("cid", ids);
      PreparedDelete<ReviewCard> stmt = bldr.prepare();
      reviewDao.delete(stmt);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void removeStale() {
    try {
      DeleteBuilder<WordCard, String> db = cardDao.deleteBuilder();
      db.where().le("gotOn", DateTime.now().minusHours(4)).and().eq("status", 0);
      int deleted = cardDao.delete(db.prepare());
      Log.d(this, String.format("deleted %d stale messages", deleted));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void removeLearningForCards(Collection<String> ids) {
    //"delete from itemlearning where id in (select w.learning from wordcard w where w.id in (...))"
    try {
      QueryBuilder<WordCard, String> cb = cardDao.queryBuilder();
      cb.selectColumns("learning_id");
      cb.where().in("id", ids);
      DeleteBuilder<ItemLearning, Long> db = learningDao.deleteBuilder();
      db.where().in("id", cb);
      PreparedDelete<ItemLearning> q = db.prepare();
      learningDao.delete(q);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void clear() {
    cardDao.updateRaw("delete from wordcard where id not in (select e.id from markevent e where e.operation <> 0) and status <> 0");
    learningDao.updateRaw("delete from itemlearning where id not in (select c.id from wordcard c)");
    reviewDao.updateRaw("delete from reviewcard where cid not in (select id from wordcard)");
  }

  public void drop(WordCard card) {
    ArrayList<String> ids = new ArrayList<String>();
    ids.add(card.getId());
    dropCardsByIds(ids);
  }

  public void purge() {
    learningDao.updateRaw("delete from wordcard");
    learningDao.updateRaw("delete from itemlearning");
    reviewDao.updateRaw("delete from reviewcard");
  }
}
