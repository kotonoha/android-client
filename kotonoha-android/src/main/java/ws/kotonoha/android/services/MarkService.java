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

import android.util.Log;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.*;
import org.joda.time.DateTime;
import ws.kotonoha.server.model.events.MarkEvent;
import ws.kotonoha.server.model.learning.WordCard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author eiennohito
 * @since 11.02.12
 */
public class MarkService implements Purgeable {
  private DataService service;
  private final RuntimeExceptionDao<MarkEvent, String> markDao;

  public MarkService(DataService service) {
    this.service = service;
    markDao = this.service.getHelper().getMarkEventDao();
    this.clear();
  }

  public void addMark(WordCard card, double mark, double time) {
    MarkEvent ev = new MarkEvent();
    ev.setDatetime(new DateTime());
    ev.setCard(card.getId());
    ev.setMark(mark);
    ev.setTime(time);
    ev.setMode(card.getCardMode());
    ev.setOperation(0);

    markDao.create(ev);
  }

  public void submit() {
    try {
      UpdateBuilder<MarkEvent, String> ub = markDao.updateBuilder();
      ub.where().eq("operation", 0);
      ub.updateColumnValue("operation", 1);
      PreparedUpdate<MarkEvent> pu = ub.prepare();
      markDao.update(pu);
    } catch (SQLException e) {
      Log.e("Kotonoha", "Error updating marks for submit", e);
    }

    Scheduler.schedule(new Runnable() {
      public void run() {
        Callable<List<MarkEvent>> markC = new Callable<List<MarkEvent>>() {
          public List<MarkEvent> call() throws Exception {
            return loadReadyMarks();
          }
        };
        service.sendMarks(markC);
      }
    });
  }

  private List<MarkEvent> loadReadyMarks() {
    List<MarkEvent> marks = markDao.queryForEq("operation", 1);
    List<String> ids = new ArrayList<String>();
    for (MarkEvent m : marks) {
      ids.add(m.getId());
    }
    setOperation(ids, 2);
    return marks;
  }

  private void setOperation(List<String> ids, int value) {
    try {
      UpdateBuilder<MarkEvent, String> ub = markDao.updateBuilder();
      ub.where().in("id", ids);
      ub.updateColumnValue("operation", value);
      PreparedUpdate<MarkEvent> pu = ub.prepare();
      markDao.update(pu);
    } catch (SQLException e) {
      Log.e("Kotonoha", "Error while marking updating marks", e);
    }
  }

  public void removeMarks(List<MarkEvent> marks) {
    markDao.delete(marks);
  }

  public void clear() {
    try {
      DeleteBuilder<MarkEvent, String> db = markDao.deleteBuilder();
      db.where().ne("operation", 0);
      PreparedDelete<MarkEvent> pq = db.prepare();
      markDao.delete(pq);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void markForResend(List<MarkEvent> marks) {
    List<String> ids = new ArrayList<String>();
    for (MarkEvent m : marks) {
      ids.add(m.getId());
    }
    setOperation(ids, 0);
  }

  public void purge() {
    markDao.updateRaw("delete from markevent");
  }

  public long countMarks() {
    try {
      QueryBuilder<MarkEvent, String> q = markDao.queryBuilder();
      q.where().eq("operation", 0);
      q.setCountOf(true);
      return markDao.countOf(q.prepare());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
