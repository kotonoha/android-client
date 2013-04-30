package ws.kotonoha.android.services.eventual;

import de.akquinet.android.androlog.Log;
import org.joda.time.Duration;
import ws.kotonoha.android.services.DataService;
import ws.kotonoha.android.services.Scheduler;

import java.util.*;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public class EventualSvcRegistry implements Runnable {

  public EventualSvcRegistry(DataService svc) {
    Collections.addAll(svcs,
      new SendChangeWordStatusES(svc),
      new SendMarksES(svc)
    );
  }

  private Queue<EventualService> svcs = new LinkedList<EventualService>();

  private Collection<EventualService> current = new ArrayList<EventualService>(10);

  @Override
  public synchronized void run() {
    Runnable r = null;
    Log.d(this, "Executing eventual service registry");
    try {
      while (!svcs.isEmpty()) {
        final EventualService svc = svcs.poll();
        current.add(svc);
        if (svc != null && svc.hasWork()) {
          r = svc;
          break;
        }
      }
    } catch (Exception e) {
      Log.w(this, "Caught an exception trying to find work", e);
    }

    svcs.addAll(current);
    current.clear();

    if (r == null) {
      Scheduler.delayed(this, Duration.standardSeconds(60));
    } else {
      Scheduler.schedule(r).doAfter(new Runnable() {
        @Override
        public void run() {
          Scheduler.delayed(EventualSvcRegistry.this, Duration.standardSeconds(30));
        }
      });
    }
  }

}
