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

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import ws.kotonoha.android.db.DatabaseHelper;
import ws.kotonoha.server.model.learning.Example;
import ws.kotonoha.server.model.learning.Word;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author eiennohito
 * @since 11.02.12
 */
public class WordService implements Purgeable {
  private final DataService dataService;
  private final RuntimeExceptionDao<Word, String> wordDao;
  private HashMap<String, Word> cache = new HashMap<String, Word>();
  private final Object syncRoot = new Object();
  private final RuntimeExceptionDao<Example, Long> exampleDao;

  public WordService(DataService dataService) {
    this.dataService = dataService;
    DatabaseHelper helper = dataService.getHelper();
    wordDao = helper.getWordDao();
    exampleDao = helper.getExampleDao();
    clear();
    loadWordsFromDb();
  }

  private void loadWordsFromDb() {
    CloseableIterator<Word> it = wordDao.iterator();
    while (it.hasNext()) {
      Word next = it.next();
      cache.put(next.getId(), next);
    }
    try {
      it.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Word wordForCard(String id) {
    synchronized (syncRoot) {
      return cache.get(id);
    }
  }

  public void prune(final String id) {
    synchronized (syncRoot) {
      cache.remove(id);
    }

    Scheduler.schedule("delete marks from db", new Runnable() {
      public void run() {
        deleteFromDb(id);
      }
    });
  }

  private void deleteFromDb(String id) {
    exampleDao.updateRaw("delete from example where word_id = ?", id);
    wordDao.deleteById(id);
  }

  public void process(Collection<Word> words) {
    synchronized (syncRoot) {
      for (Word w : words) {
        cache.put(w.getId(), w);
      }
    }

    for (Word w : words) {
      Collection<Example> exs = w.getExamples();
      if (exs != null) {
        for (Example ex : exs) {
          ex.setWord(w);
          exampleDao.create(ex);
        }
      }
      wordDao.createIfNotExists(w);
    }

  }

  public void clear() {
    wordDao.updateRaw("delete from word where id not in (select c.word from wordcard c)");
    exampleDao.updateRaw("delete from example where word_id not in (select w.id from word w)");
  }

  public void purge() {
    wordDao.updateRaw("delete from word");
    exampleDao.updateRaw("delete from example");
  }
}
