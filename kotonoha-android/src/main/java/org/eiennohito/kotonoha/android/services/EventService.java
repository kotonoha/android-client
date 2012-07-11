package org.eiennohito.kotonoha.android.services;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import de.akquinet.android.androlog.Log;
import org.eiennohito.kotonoha.android.db.DatabaseHelper;
import org.eiennohito.kotonoha.android.db.Values;
import org.eiennohito.kotonoha.android.rest.request.PostChangeWordStatus;
import org.eiennohito.kotonoha.android.util.SuccessCallback;
import org.eiennohito.kotonoha.model.events.ChangeWordStatusEvent;
import org.joda.time.Duration;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author eiennohito
 * @since 05.07.12
 */
public class EventService {
  private final DataService svc;
  private final DatabaseHelper helper;
  private RuntimeExceptionDao<ChangeWordStatusEvent, Long> dao;

  public EventService(DataService svc) {
    this.svc = svc;
    helper = svc.getHelper();
    dao = helper.getChangeWordDao();
  }


  public void publishChangeStatus(ChangeWordStatusEvent cv) {
    dao.create(cv);
    Scheduler.delayed(new Runnable() {
      @Override
      public void run() {
        if (!hasEvents()) {
          return;
        }
        Scheduler.postRest(new PostChangeWordStatus(
          svc.restSvc,
          new Callable<List<ChangeWordStatusEvent>>() {
            @Override
            public List<ChangeWordStatusEvent> call() throws Exception {
              return loadEvents();
            }
          }, new SuccessCallback<List<ChangeWordStatusEvent>, Values>() {
          @Override
          public void onOk(List<ChangeWordStatusEvent> changeWordStatusEvents, Values values) {
            Log.d(EventService.this, "Change status went successfully");
            //do nothing
          }
        }
        ));
      }
    }, Duration.standardMinutes(1));
  }

  public boolean hasEvents() {
    return dao.countOf() != 0;
  }

  public List<ChangeWordStatusEvent> loadEvents() {
    return dao.queryForAll();
  }
}
