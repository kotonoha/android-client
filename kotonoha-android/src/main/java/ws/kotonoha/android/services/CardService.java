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
import ws.kotonoha.android.util.ScheduledWordComparator;
import ws.kotonoha.server.model.events.MarkEvent;
import ws.kotonoha.server.model.learning.ItemLearning;
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
  private TreeSet<WordCard> cards;
  private final RuntimeExceptionDao<ItemLearning, Long> learningDao;


  private static TreeSet<WordCard> emptySet() {
    return new TreeSet<WordCard>(new ScheduledWordComparator());
  }

  public CardService(DataService dataService) {
    this.dataService = dataService;
    DatabaseHelper helper = dataService.getHelper();
    cardDao = helper.getWordCardDao();
    learningDao = helper.getLearningDao();
    clear();
    removeStale();
    cards = loadCards();
  }

  private TreeSet<WordCard> loadCards() {
    List<WordCard> cards = cardDao.queryForEq("status", 0);
    TreeSet<WordCard> set = emptySet();
    set.addAll(cards);
    return set;
  }

  public boolean hasNextCard() {
    synchronized (syncRoot) {
      return !cards.isEmpty();
    }
  }

  public WordCard nextCard() {
    synchronized (syncRoot) {
      final WordCard card = cards.pollFirst();
      if (card == null) {
        return null;
      }
      card.setStatus(1);
      Scheduler.schedule(new Runnable() {
        public void run() {
          cardDao.update(card);
        }
      });
      return card;
    }
  }

  private Random rand = new Random();

  public synchronized void process(final Collection<WordCard> crds) {

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

    reloadCards();
  }

  public void reloadCards() {
    TreeSet<WordCard> crds = loadCards();
    synchronized (syncRoot) {
      cards = crds;
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
    removeLearningForCards(ids);
    cardDao.deleteIds(ids);
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
  }

  public void drop(WordCard card) {
    ArrayList<String> ids = new ArrayList<String>();
    ids.add(card.getId());
    dropCardsByIds(ids);
  }

  public void purge() {
    learningDao.updateRaw("delete from wordcard");
    learningDao.updateRaw("delete from itemlearning");
  }
}
