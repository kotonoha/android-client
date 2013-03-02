package ws.kotonoha.android.services;

import org.apache.http.client.HttpClient;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import ws.kotonoha.android.util.ApiCodes;
import ws.kotonoha.server.rest.KotonohaApi;

/**
 * @author eiennohito
 * @since 01.04.12
 */
public class RestService {
  private final OAuthService service;
  private final HttpClient client;
  private final String baseUri;
  private final Token token;

  public RestService(HttpClient client, String baseUri, Token token) {
    this.client = client;
    this.baseUri = baseUri;
    this.token = token;
    ServiceBuilder bldr = new ServiceBuilder().provider(new KotonohaApi(baseUri))
      .apiSecret(ApiCodes.privateKey).apiKey(ApiCodes.publicKey);
    service = bldr.build();
  }

  public HttpClient getClient() {
    return client;
  }

  public void sign(OAuthRequest req) {
    service.signRequest(token, req);
  }

  public String baseUri() {
    return (baseUri + "/api/");
  }
}
