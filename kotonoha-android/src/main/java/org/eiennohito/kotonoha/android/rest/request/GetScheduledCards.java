package org.eiennohito.kotonoha.android.rest.request;

import android.net.http.AndroidHttpClient;
import org.apache.http.HttpResponse;
import org.eiennohito.kotonoha.android.json.GsonObjectParser;
import org.eiennohito.kotonoha.android.rest.RestGetRequest;
import org.eiennohito.kotonoha.android.util.ValueCallback;
import org.eiennohito.kotonoha.model.learning.Container;

/**
 * @author eiennohito
 * @since 10.03.12
 */
public class GetScheduledCards extends RestGetRequest<Container> {
  public GetScheduledCards(AndroidHttpClient client, int howMany, ValueCallback<Container> callback) {
    super(client, "words/scheduled/" + howMany, callback);
  }

  private GsonObjectParser<Container> parser = new GsonObjectParser<Container>(Container.class);

  @Override
  protected Container transform(HttpResponse response) throws Exception {
    return parser.process(response.getEntity());
  }

  @Override
  public long spacingInterval() {
    return 7500L;
  }
}
