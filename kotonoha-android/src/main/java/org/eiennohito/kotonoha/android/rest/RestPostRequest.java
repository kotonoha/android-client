package org.eiennohito.kotonoha.android.rest;

import android.net.http.AndroidHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.eiennohito.kotonoha.android.json.GsonObjectEntity;
import org.eiennohito.kotonoha.android.util.ValueCallback;

/**
 * @author eiennohito
 * @since 10.03.12
 */
public abstract class RestPostRequest<T, D> extends Request<T> {
  protected D data;

  public RestPostRequest(AndroidHttpClient client, String servPath, D data, ValueCallback<T> callback) {
    super(client, servPath, callback);
    this.data = data;
  }

  @Override
  protected HttpUriRequest createRequest() {
    HttpPost post = new HttpPost(reqUri);
    post.setEntity(createEntity());
    return post;
  }

  protected GsonObjectEntity createEntity() {
    return new GsonObjectEntity(data);
  }
}
