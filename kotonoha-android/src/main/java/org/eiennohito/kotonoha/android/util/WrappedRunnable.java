package org.eiennohito.kotonoha.android.util;

import de.akquinet.android.androlog.Log;
import org.eiennohito.kotonoha.android.services.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public class WrappedRunnable {
  AtomicBoolean finished = new AtomicBoolean(false);
  private final List<Runnable> after = new ArrayList<Runnable>();
  private final Runnable inner;

  public WrappedRunnable(Runnable inner) {
    this.inner = inner;
  }

  public WrappedRunnable doAfter(Runnable r) {
    if (!finished.get()) {
      synchronized (after) {
        after.add(r);
      }
    } else {
      Scheduler.schedule(r);
    }
    return this;
  }

  public Runnable wrapped() {
    return new Runnable() {
      @Override
      public void run() {
        try {
          inner.run();
          finished.set(true);
        } catch (RuntimeException e) {
          Log.e(WrappedRunnable.this, "Error in inner block", e);
          throw e;
        } finally {
          synchronized (after) {
            for (Runnable r : after) {
              Scheduler.schedule(r);
            }
          }
        }
      }
    };
  }
}
