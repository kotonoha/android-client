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
package org.eiennohito.kotonoha.android.services;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import org.eiennohito.kotonoha.android.db.DatabaseHelper;
import org.eiennohito.kotonoha.android.util.ScheduledWordComparator;
import org.eiennohito.kotonoha.model.events.MarkEvent;
import org.eiennohito.kotonoha.model.learning.WordCard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * @author eiennohito
 * @since 11.02.12
 */
public class CardService {
  private final Object syncRoot = new Object();
  private final DataService dataService;
  private final RuntimeExceptionDao<WordCard,Long> cardDao;
  private TreeSet<WordCard> cards;


  private static TreeSet<WordCard> emptySet() {
    return new TreeSet<WordCard>(new ScheduledWordComparator());
  }

  public CardService(DataService dataService) {
    this.dataService = dataService;
    DatabaseHelper helper = dataService.getHelper();
    cardDao = helper.getWordCardDao();
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
      card.setStatus(1);
      dataService.defaultScheduler.schedule(new Runnable() {
        public void run() {
          cardDao.update(card);
        }
      });
      return card;
    }
  }

  public void process(Collection<WordCard> crds) {
   
    for (WordCard card: crds) {
      card.setStatus(0);
      cardDao.createIfNotExists(card);
    }
    reloadCards();
  }

  public void reloadCards() {
    dataService.defaultScheduler.schedule(new Runnable() {
      public void run() {
        TreeSet<WordCard> crds = loadCards();
        synchronized (syncRoot) {
          cards = crds;
        }
      }
    });
  }

  public int countPresent() {
    synchronized (syncRoot) {
      return cards.size();
    }
  }

  public void removeCardsFor(List<MarkEvent> marks) {
    List<Long> ids = new ArrayList<Long>(marks.size());
    for (MarkEvent e: marks) {
      ids.add(e.getCard());
    }
    cardDao.deleteIds(ids);
  }

  public void clear() {
    cardDao.updateRaw("delete from wordcard where id not in (select e.id from markevent e where e.operation <> 0) and status <> 0");
  }
}
