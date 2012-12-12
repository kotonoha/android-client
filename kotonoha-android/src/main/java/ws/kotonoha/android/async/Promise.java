package ws.kotonoha.android.async;

import de.akquinet.android.androlog.Log;
import ws.kotonoha.android.services.Scheduler;
import ws.kotonoha.android.util.ValueCallback;
import ws.kotonoha.android.util.ValueProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eiennohito
 * @since 12.12.12
 */
public class Promise<T> {
  List<ValueCallback<T>> list = new ArrayList<ValueCallback<T>>();
  volatile T data;

  public void then(final ValueCallback<T> f) {
    synchronized (this) {
      if (data == null) {
        list.add(f);
      } else {
        Scheduler.schedule(new Runnable() {
          @Override
          public void run() {
            f.process(data);
          }
        });
      }
    }
  }

  public boolean tryResolve(T val) {
    synchronized (this) {
      if (data != null) {
        return false;
      }
      data = val;
      for (final ValueCallback<T> f : list) {
        Scheduler.schedule(new Runnable() {
          @Override
          public void run() {
            f.process(data);
          }
        });
      }
      list = null;
    }
    return true;
  }

  public void resolve(T val) {
    if (!tryResolve(val)) {
      throw new AlreadyFilledException();
    }
  }

  public <U> Promise<U> transform(final ValueProcessor<T, U> f) {
    final Promise<U> p1 = new Promise<U>();
    this.then(new ValueCallback<T>() {
      @Override
      public void process(final T val) {
        try {
          U u = f.process(val);
          p1.resolve(u);
        } catch (Exception e) {
          Log.e(this, "error in function", e);
        }
      }
    });
    return p1;
  }


}
