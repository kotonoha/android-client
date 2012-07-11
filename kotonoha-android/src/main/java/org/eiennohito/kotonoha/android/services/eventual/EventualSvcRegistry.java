package org.eiennohito.kotonoha.android.services.eventual;

import org.eiennohito.kotonoha.android.services.DataService;
import org.eiennohito.kotonoha.android.services.Scheduler;
import org.joda.time.Duration;

import java.util.*;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public class EventualSvcRegistry implements Runnable {

  public EventualSvcRegistry(DataService svc) {
    Collections.addAll(svcs,
      new SendMarksES(svc),
      new SendChangeWordStatusES(svc)
    );
  }

  private Queue<EventualService> svcs = new LinkedList<EventualService>();

  private Collection<EventualService> current = new ArrayList<EventualService>(10);

  @Override
  public synchronized void run() {
    Runnable r = null;

    while (!svcs.isEmpty()) {
      final EventualService svc = svcs.poll();
      current.add(svc);
      if (svc != null && svc.hasWork()) {
        r = svc;
        break;
      }
    }

    svcs.addAll(current);
    current.clear();

    if (r == null) {
      Scheduler.delayed(this, Duration.standardSeconds(20));
    } else {
      Scheduler.schedule(r).doAfter(new Runnable() {
        @Override
        public void run() {
          Scheduler.delayed(this, Duration.standardSeconds(20));
        }
      });
    }
  }

}
