package org.eiennohito.kotonoha.android.services.eventual;

import org.eiennohito.kotonoha.android.services.Scheduler;
import org.joda.time.Duration;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public class Registry implements Runnable {

  private AtomicReference<Boolean> isRunning = new AtomicReference<Boolean>(false);
  private static final EventualService[] ALL_REGISTERED = new EventualService[]{

  };

  public Registry() {
    Collections.addAll(svcs, ALL_REGISTERED);
  }

  private Queue<EventualService> svcs = new LinkedList<EventualService>();

  @Override
  public void run() {
    if (isRunning.get()) {
      return;
    }
    try {
      isRunning.set(true);
      final EventualService svc = svcs.poll();
      if (svc == null) {
        Scheduler.delayed(this, Duration.standardSeconds(20));
        return;
      }

      Scheduler.schedule(svc).doAfter(new Runnable() {
        @Override
        public void run() {
          Scheduler.delayed(this, Duration.standardSeconds(20));
        }
      });

    } finally {
      isRunning.set(false);
    }
  }

}
