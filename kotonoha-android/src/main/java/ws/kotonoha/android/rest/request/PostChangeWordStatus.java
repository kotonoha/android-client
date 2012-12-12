package ws.kotonoha.android.rest.request;

import org.apache.http.HttpResponse;
import ws.kotonoha.android.db.Values;
import ws.kotonoha.android.json.GsonObjectParser;
import ws.kotonoha.android.rest.RestPostRequest;
import ws.kotonoha.android.services.RestService;
import ws.kotonoha.android.util.SuccessCallback;
import ws.kotonoha.server.model.events.ChangeWordStatusEvent;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public class PostChangeWordStatus extends RestPostRequest<List<ChangeWordStatusEvent>, Values> {
  public PostChangeWordStatus(RestService svc, Callable<List<ChangeWordStatusEvent>> data, final SuccessCallback<List<ChangeWordStatusEvent>, Values> listValuesSuccessCallback) {
    super(svc, "events/change_word_status", data, listValuesSuccessCallback);
  }

  @Override
  protected Values transform(HttpResponse response) throws Exception {
    return new GsonObjectParser<Values>(Values.class).process(response.getEntity());
  }
}
