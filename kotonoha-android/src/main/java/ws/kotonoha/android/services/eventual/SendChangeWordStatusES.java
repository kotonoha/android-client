package ws.kotonoha.android.services.eventual;

import de.akquinet.android.androlog.Log;
import ws.kotonoha.android.db.Values;
import ws.kotonoha.android.rest.request.PostChangeWordStatus;
import ws.kotonoha.android.services.DataService;
import ws.kotonoha.android.services.EventService;
import ws.kotonoha.android.services.Scheduler;
import ws.kotonoha.android.util.SuccessCallback;
import ws.kotonoha.server.model.events.ChangeWordStatusEvent;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author eiennohito
 * @since 11.07.12
 */
public class SendChangeWordStatusES extends EventualService {

  private EventService esvc;

  public SendChangeWordStatusES(DataService svc) {
    super(svc);
    esvc = svc.getEventSvc();
  }

  @Override
  boolean hasWork() {
    return svc.getEventSvc().hasEvents();
  }

  @Override
  public void run() {
    if (!esvc.hasEvents()) {
      return;
    }
    Scheduler.postRest(new PostChangeWordStatus(
      svc.getRestSvc(),
      new Callable<List<ChangeWordStatusEvent>>() {
        @Override
        public List<ChangeWordStatusEvent> call() throws Exception {
          return esvc.loadEvents();
        }
      }, new SuccessCallback<List<ChangeWordStatusEvent>, Values>() {
      @Override
      public void onOk(List<ChangeWordStatusEvent> evnts, Values values) {
        esvc.delete(evnts);
        Log.d(SendChangeWordStatusES.this, "Change status went successfully");
      }
    }
    ));
  }
}
