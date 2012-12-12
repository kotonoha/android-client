package ws.kotonoha.android.rest;


import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import ws.kotonoha.android.services.RestService;
import ws.kotonoha.android.util.ValueCallback;

/**
 * @author eiennohito
 * @since 08.03.12
 */
public abstract class RestGetRequest<T> extends RestRequest<T> {
  public RestGetRequest(RestService svc, String servPath, ValueCallback<T> callback) {
    super(svc, servPath, callback);
  }

  @Override
  protected HttpUriRequest createRequest() {
    return new HttpGet(reqUri);
  }
}
