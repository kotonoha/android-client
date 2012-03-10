package org.eiennohito.kotonoha.android.rest;


import android.net.http.AndroidHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.eiennohito.kotonoha.android.util.ValueCallback;

/**
 * @author eiennohito
 * @since 08.03.12
 */
public abstract class RestGetRequest<T> extends Request<T> {
  public RestGetRequest(AndroidHttpClient client, String servPath, ValueCallback<T> callback) {
    super(client, servPath, callback);
  }

  @Override
  protected HttpUriRequest createRequest() {
    return new HttpGet(reqUri);
  }
}
