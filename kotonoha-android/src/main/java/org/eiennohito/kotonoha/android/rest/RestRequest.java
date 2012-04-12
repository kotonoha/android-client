package org.eiennohito.kotonoha.android.rest;

import de.akquinet.android.androlog.Log;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.eiennohito.kotonoha.android.services.RestService;
import org.eiennohito.kotonoha.android.services.Scheduler;
import org.eiennohito.kotonoha.android.util.ValueCallback;
import org.joda.time.Duration;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Verb;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eiennohito
 * @since 08.03.12
 */
abstract public class RestRequest<Resp> {
  private static final String AUTHORIZATION = "Authorization";
  private final RestService svc;
  protected final ValueCallback<Resp> callback;
  private List<Runnable> failureCallbacks = new ArrayList<Runnable>();
  protected final URI reqUri;

  private int times = 5;
  private int current = 0;

  public RestRequest(RestService svc, String servPath, ValueCallback<Resp> callback) {
    this.svc = svc;
    this.callback = callback;
    reqUri = URI.create(svc.baseUri()).resolve(servPath);
  }

  public void launchRequest() {

    HttpUriRequest req = null;
    try {
      req = createRequest();
      sign(req);
    } catch (Exception e) {
      Log.e(this, "Couldn't create request");
      returnAsync(null);
      return;
    }

    HttpResponse response;
    try {
      response = svc.getClient().execute(req);
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

  private void sign(HttpUriRequest req) {
    OAuthRequest oar = new OAuthRequest(Verb.valueOf(req.getMethod()), req.getURI().toString());
/*    if (req instanceof HttpEntityEnclosingRequestBase) {
      HttpEntityEnclosingRequestBase erb = (HttpEntityEnclosingRequestBase) req;
      GsonObjectEntity entity = (GsonObjectEntity) erb.getEntity();
      oar.addPayload(entity.array());
    }*/
    for (Header h : req.getAllHeaders()) {
      oar.addHeader(h.getName(), h.getValue());
    }
    svc.sign(oar);
    req.addHeader(AUTHORIZATION, oar.getHeaders().get(AUTHORIZATION));
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
        Scheduler.postRest(RestRequest.this);
      }
    }, Duration.standardSeconds(5));
  }

  protected abstract Resp transform(HttpResponse response) throws Exception;

  protected abstract HttpUriRequest createRequest() throws Exception;

  public long identify() {
    return getClass().hashCode();
  }

  public boolean singleThreaded() {
    return false;
  }

  protected void success(final Resp resp) {
    Scheduler.schedule("Successful response", new Runnable() {
      public void run() {
        callback.process(resp);
      }
    });
  }

  protected void failure() {
    for (final Runnable r : failureCallbacks) {
      Scheduler.schedule(new Runnable() {
        public void run() {
          try {
            r.run();
          } catch (Exception e) {
            Log.e(RestRequest.this, "Error while running failure callback", e);
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
   *
   * @return
   */
  public long spacingInterval() {
    return 0L;
  }
}
