package org.eiennohito.kotonoha.android.rest;

import android.net.http.AndroidHttpClient;
import de.akquinet.android.androlog.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.eiennohito.kotonoha.android.services.Scheduler;
import org.eiennohito.kotonoha.android.util.AddressUtil;
import org.eiennohito.kotonoha.android.util.ValueCallback;
import org.joda.time.Period;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eiennohito
 * @since 08.03.12
 */
abstract public class Request<Resp> {
  protected final AndroidHttpClient client;
  protected final ValueCallback<Resp> callback;
  private List<Runnable> failureCallbacks = new ArrayList<Runnable>();
  protected final URI reqUri;

  private int times = 5;
  private int current = 0;

  public Request(AndroidHttpClient client, String servPath, ValueCallback<Resp> callback) {
    this.client = client;
    this.callback = callback;
    reqUri = URI.create(AddressUtil.baseUri).resolve(servPath);
  }

  public void launchRequest() {

    HttpUriRequest req = null;
    try {
      req = createRequest();
    } catch (Exception e) {
      Log.e(this, "Couldn't create request");
      returnAsync(null);
      return;
    }

    HttpResponse response;
    try {
      response = client.execute(req);
    } catch (IOException ex) {
      Log.e(this, "Couldn't execute http request", ex);
      returnAsync(null);
      return;
    }
    int code = response.getStatusLine().getStatusCode();
    if (code != 200) {
      returnAsync(null);
      Log.e(this, "Server error: got code" + code);
      Log.e(this, "Reason is:" + response.getStatusLine().getReasonPhrase());
      return;
    }
    Resp res;
    try {
      res = transform(response);
    } catch (Exception e) {
      Log.e(this, "Couldn't transform response", e);
      returnAsync(null);
      return;
    }
    returnAsync(res);
  }

  private void returnAsync(final Resp resp) {
    Runnable r = new Runnable() {
      public void run() {
        if (resp == null) {
          failure();
        } else {
          success(resp);
        }
      }
    };
    Scheduler.schedule(r);
  }

  private void retry() {
    if (current >= times) {
      return;
    }

    ++current;
    Scheduler.delayed(new Runnable() {
      public void run() {
        Scheduler.postRest(Request.this);
      }
    }, Period.seconds(5));
  }

  protected abstract Resp transform(HttpResponse response) throws Exception;

  protected abstract HttpUriRequest createRequest() throws Exception;

  public long identify() {
    return getClass().hashCode();
  }

  protected void success(final Resp resp) {
    Scheduler.schedule("Successful response", new Runnable() {
      public void run() {
        callback.process(resp);
      }
    });
  }

  protected void failure() {
    for (final Runnable r: failureCallbacks) {
      Scheduler.schedule(new Runnable() {
        public void run() {
          try {
            r.run();
          } catch (Exception e) {
            Log.e(Request.this, "Error while running failure callback", e);
          }
        }
      });
    }
    retry();
  }

  public void onFailure(Runnable runnable) {
    failureCallbacks.add(runnable);
  }


  /**
   * Request should return time in ms that should be between any 2 sequential invocations.
   * @return
   */
  public long spacingInterval() {
    return 0L;
  }
}
