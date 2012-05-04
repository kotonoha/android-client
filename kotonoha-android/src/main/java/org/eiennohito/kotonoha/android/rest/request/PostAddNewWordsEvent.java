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
 * @since 24.04.12
 */
public class PostAddNewWordsEvent extends RestPostRequest<List<MarkEvent>, Values> {
  public PostAddNewWordsEvent(RestService svc, Callable<List<MarkEvent>> data, final SuccessCallback<List<MarkEvent>, Values> listValuesSuccessCallback) {
    super(svc, "events/add_words", data, listValuesSuccessCallback);
  }

  @Override
  protected Values transform(HttpResponse response) throws Exception {
    return new GsonObjectParser<Values>(Values.class).process(response.getEntity());
  }
}
