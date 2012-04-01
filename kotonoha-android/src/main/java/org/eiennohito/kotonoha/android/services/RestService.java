package org.eiennohito.kotonoha.android.services;

import org.apache.http.client.HttpClient;
import org.eiennohito.kotonoha.android.util.ApiCodes;
import org.eiennohito.kotonoha.rest.KotonohaApi;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

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
