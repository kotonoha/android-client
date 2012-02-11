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

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import org.eiennohito.kotonoha.android.db.DatabaseHelper;
import org.eiennohito.kotonoha.android.util.ScheduledWordComparator;
import org.eiennohito.kotonoha.model.learning.WordCard;

import java.sql.SQLException;
import java.util.Collection;
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
    CloseableIterator<WordCard> it = cardDao.iterator();
    TreeSet<WordCard> set = emptySet();
    while (it.hasNext()) {
      set.add(it.next());
    }
    try {
      it.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return set;
  }

  public boolean hasNextCard() {
    synchronized (syncRoot) {
      return !cards.isEmpty();
    }
  }

  public WordCard nextCard() {
    synchronized (syncRoot) {
      return cards.pollFirst();
    }
  }

  public void process(Collection<WordCard> crds) {
    for (WordCard card: crds) {
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
}
