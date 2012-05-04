package org.eiennohito.kotonoha.android.rest.request;

import org.apache.http.HttpResponse;
import org.eiennohito.kotonoha.android.db.Values;
import org.eiennohito.kotonoha.android.json.GsonObjectParser;
import org.eiennohito.kotonoha.android.rest.RestPostRequest;
import org.eiennohito.kotonoha.android.services.RestService;
import org.eiennohito.kotonoha.android.util.SuccessCallback;
import org.eiennohito.kotonoha.model.events.ChangeWordStatusEvent;

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
