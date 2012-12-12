package ws.kotonoha.android.rest.request;

import org.apache.http.HttpResponse;
import ws.kotonoha.android.db.Values;
import ws.kotonoha.android.json.GsonObjectParser;
import ws.kotonoha.android.rest.RestPostRequest;
import ws.kotonoha.android.services.RestService;
import ws.kotonoha.android.util.SuccessCallback;
import ws.kotonoha.server.model.events.MarkEvent;

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
