package ws.kotonoha.android.config;


import ws.kotonoha.server.rest.AuthObject;

/**
 * @author eiennohito
 * @since 01.04.12
 */
public class AppConfig {
  private AuthObject authObject;

  public AuthObject getAuthObject() {
    return authObject;
  }

  public void setAuthObject(AuthObject authObject) {
    this.authObject = authObject;
  }
}
