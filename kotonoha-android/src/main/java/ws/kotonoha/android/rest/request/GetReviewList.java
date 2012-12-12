package ws.kotonoha.android.rest.request;

import org.apache.http.HttpResponse;
import ws.kotonoha.android.json.GsonObjectParser;
import ws.kotonoha.android.rest.RestGetRequest;
import ws.kotonoha.android.services.RestService;
import ws.kotonoha.android.util.ValueCallback;
import ws.kotonoha.server.model.learning.Container;

/**
 * @author eiennohito
 * @since 29.06.12
 */
public class GetReviewList extends RestGetRequest<Container> {

  private final GsonObjectParser<Container> parser = new GsonObjectParser<Container>(Container.class);

  public GetReviewList(RestService svc, int max, ValueCallback<Container> callback) {
    super(svc, "words/review/" + max, callback);
  }

  public GetReviewList(RestService svc, ValueCallback<Container> callback) {
    this(svc, 20, callback);
  }

  @Override
  protected Container transform(HttpResponse response) throws Exception {
    return parser.process(response.getEntity());
  }
}
