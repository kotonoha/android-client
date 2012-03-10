package org.eiennohito.kotonoha.android.rest.request;

import android.net.http.AndroidHttpClient;
import org.apache.http.HttpResponse;
import org.eiennohito.kotonoha.android.db.Values;
import org.eiennohito.kotonoha.android.json.GsonObjectParser;
import org.eiennohito.kotonoha.android.rest.RestPostRequest;
import org.eiennohito.kotonoha.android.util.ValueCallback;
import org.eiennohito.kotonoha.model.events.MarkEvent;

import java.util.List;

/**
 * @author eiennohito
 * @since 10.03.12
 */
public class PostMarkEvents extends RestPostRequest<Values, List<MarkEvent>> {
  public PostMarkEvents(AndroidHttpClient client, List<MarkEvent> data, ValueCallback<Values> listValueCallback) {
    super(client, "events/mark", data, listValueCallback);
  }

  @Override
  protected Values transform(HttpResponse response) throws Exception {
    return new GsonObjectParser<Values>(Values.class).process(response.getEntity());
  }
}
