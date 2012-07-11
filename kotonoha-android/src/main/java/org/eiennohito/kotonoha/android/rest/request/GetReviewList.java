package org.eiennohito.kotonoha.android.rest.request;

import org.apache.http.HttpResponse;
import org.eiennohito.kotonoha.android.json.GsonObjectParser;
import org.eiennohito.kotonoha.android.rest.RestGetRequest;
import org.eiennohito.kotonoha.android.services.RestService;
import org.eiennohito.kotonoha.android.util.ValueCallback;
import org.eiennohito.kotonoha.model.learning.Container;

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
