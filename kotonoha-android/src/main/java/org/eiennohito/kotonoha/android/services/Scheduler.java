package org.eiennohito.kotonoha.android.services;

import de.akquinet.android.androlog.Log;
import org.eiennohito.kotonoha.android.rest.RestRequest;
import org.joda.time.Period;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author eiennohito
 * @since 08.03.12
 */
public class Scheduler {
  public final static SchedulerService single = new SchedulerService(1, 1);
  public final static SchedulerService defaultScheduler = new SchedulerService(1, 5);
  public final static ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(2);

  private static Future<?> scheduleSingle(Runnable toRun) {
    return single.schedule(toRun);
  }

  public static Future<?> schedule(Runnable toRun) {
    return defaultScheduler.schedule(toRun);
  }

  public static Future<?> schedule(String name, Runnable toRun) {
    return defaultScheduler.schedule(new TimedRunnable(name, toRun));
  }

  public static ScheduledFuture<?> delayed(Runnable runnable, Period period) {
    return timer.schedule(runnable, period.getMillis(), TimeUnit.MILLISECONDS);
  }

  private static final Map<Long, RestRequest> scheduled = new ConcurrentHashMap<Long, RestRequest>(16, 0.75f, 4);
  private static final Map<Long, Long> scheduledTimes = new ConcurrentHashMap<Long, Long>(16, 0.75f, 4);

  public static synchronized void postRest(final RestRequest<?> req) {
    long id = req.identify();
    if (!checkTimes(req, id)) {
      return;
    }
    Log.d(req, "Trying to schedule request " + req);
    if (scheduled.containsKey(id)) {
      Log.d(req, "Request of same type is already present, skipping");
      return;
    }
    scheduled.put(id, req);
    scheduledTimes.put(id, System.currentTimeMillis());
    scheduleSingle(new RequestWrapper(req));
    Log.d(req, "Request scheduled");
  }

  private static boolean checkTimes(final RestRequest<?> req, long id) {
    long time = System.currentTimeMillis();
    Long prev = scheduledTimes.get(id);
    long passed;
    long interval = req.spacingInterval();
    if (prev == null) {
      passed = interval + 1;
    } else {
      passed = time - prev;
    }
    if (passed < interval) {
      delayed(new Runnable() {
        public void run() {
          postRest(req);
        }
      }, new Period(interval - passed));
      return false;
    }
    return true;
  }

  public static void destroy() {
    single.shutdown();
    defaultScheduler.shutdown();
    timer.shutdown();
  }

  private static class RequestWrapper implements Runnable {
    private RestRequest<?> req;

    private RequestWrapper(RestRequest<?> req) {
      this.req = req;
    }

    public void run() {
      long tm = System.nanoTime();
      try {
        req.launchRequest();
      } catch (Exception e) {
        Log.e(req, "Error when executing request", e);
      } finally {
        long id = req.identify();
        long time = System.nanoTime() - tm;
        scheduled.remove(id);
        Log.d(req, String.format("Request %s finished executing in %.1f ms", req, time / 1e6));
      }
    }
  }

  private static class TimedRunnable implements Runnable {
    private String tag;
    private Runnable inner;

    public TimedRunnable(String tag, Runnable inner) {
      this.tag = tag;
      this.inner = inner;
    }

    public void run() {
      long tm = System.nanoTime();
      try {
        inner.run();
      } finally {
        long time = System.nanoTime() - tm;
        String res = String.format("Executed request %s in %.1f ms", tag, time / 1e6);
        Log.d(Scheduler.class.getName(), res);
      }
    }
  }
}
