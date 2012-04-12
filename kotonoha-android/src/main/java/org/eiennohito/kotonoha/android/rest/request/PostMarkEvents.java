package org.eiennohito.kotonoha.android.rest.request;

import org.apache.http.HttpResponse;
import org.eiennohito.kotonoha.android.db.Values;
import org.eiennohito.kotonoha.android.json.GsonObjectParser;
import org.eiennohito.kotonoha.android.rest.RestPostRequest;
import org.eiennohito.kotonoha.android.services.RestService;
import org.eiennohito.kotonoha.android.util.SuccessCallback;
import org.eiennohito.kotonoha.model.events.MarkEvent;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author eiennohito
 * @since 10.03.12
 */
public class PostMarkEvents extends RestPostRequest<List<MarkEvent>, Values> {
  public PostMarkEvents(RestService client, Callable<List<MarkEvent>> data,
                        SuccessCallback<List<MarkEvent>, Values> listValueCallback) {
    super(client, "events/mark", data, listValueCallback);
  }

  @Override
  protected Values transform(HttpResponse response) throws Exception {
    return new GsonObjectParser<Values>(Values.class).process(response.getEntity());
  }

  @Override
  public boolean singleThreaded() {
    return true;
  }
}
