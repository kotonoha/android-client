package ws.kotonoha.android.rest.request;

import org.apache.http.HttpResponse;
import ws.kotonoha.android.json.GsonObjectParser;
import ws.kotonoha.android.rest.RestGetRequest;
import ws.kotonoha.android.services.RestService;
import ws.kotonoha.android.util.ValueCallback;
import ws.kotonoha.server.model.learning.Container;

/**
 * @author eiennohito
 * @since 10.03.12
 */
public class GetScheduledCards extends RestGetRequest<Container> {
  public GetScheduledCards(RestService client, int howMany, ValueCallback<Container> callback) {
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

  @Override
  public boolean singleThreaded() {
    return true;
  }
}
