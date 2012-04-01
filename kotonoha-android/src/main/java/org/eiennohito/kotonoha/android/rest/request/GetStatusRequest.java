package org.eiennohito.kotonoha.android.rest.request;

import org.apache.http.HttpResponse;
import org.eiennohito.kotonoha.android.rest.RestGetRequest;
import org.eiennohito.kotonoha.android.services.RestService;
import org.eiennohito.kotonoha.android.util.ValueCallback;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author eiennohito
 * @since 01.04.12
 */
public class GetStatusRequest extends RestGetRequest<String> {
  public GetStatusRequest(RestService svc, ValueCallback<String> stringValueCallback) {
    super(svc, "status", stringValueCallback);
  }

  @Override
  protected String transform(HttpResponse response) throws Exception {
    InputStream is = response.getEntity().getContent();
    InputStreamReader reader = new InputStreamReader(is, "UTF-8");
    char[] cb = new char[2048];
    reader.read(cb);
    is.close();
    return new String(cb);
  }
}
