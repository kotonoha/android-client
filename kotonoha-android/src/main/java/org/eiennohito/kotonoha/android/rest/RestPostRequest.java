package org.eiennohito.kotonoha.android.rest;

import android.net.http.AndroidHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.eiennohito.kotonoha.android.json.GsonObjectEntity;
import org.eiennohito.kotonoha.android.util.ErrorCallback;
import org.eiennohito.kotonoha.android.util.SuccessCallback;
import org.eiennohito.kotonoha.android.util.ValueCallback;

import java.util.concurrent.Callable;

/**
 * @author eiennohito
 * @since 10.03.12
 */
public abstract class RestPostRequest<Req, Resp> extends Request<Resp> {
  protected Callable<Req> data;

  public RestPostRequest(AndroidHttpClient client, String servPath, Callable<Req> data, final SuccessCallback<Req, Resp> callback) {
    super(client, servPath, new RespValueCallback<Req, Resp>(callback));
    this.data = data;
  }

  @Override
  protected HttpUriRequest createRequest() throws Exception {
    HttpPost post = new HttpPost(reqUri);
    post.setEntity(createEntity());
    return post;
  }

  private Req obj;

  protected GsonObjectEntity createEntity() throws Exception {
    obj = data.call();
    ((RespValueCallback<Req, Resp>)callback).setObj(obj);
    return new GsonObjectEntity(obj);
  }

  public void onFailure(final ErrorCallback<Req> cb) {
    super.onFailure(new Runnable() {
      public void run() {
        cb.onError(obj);
      }
    });
  }

  private static class RespValueCallback<Req, Resp> implements ValueCallback<Resp> {
    private final SuccessCallback<Req, Resp> callback;
    private Req obj;

    public RespValueCallback(SuccessCallback<Req, Resp> callback) {
      this.callback = callback;
    }

    public void process(Resp val) {
      callback.onOk(obj, val);
    }

    public void setObj(Req obj) {
      this.obj = obj;
    }
  }

}
