package org.eiennohito.kotonoha.android.config;

import org.eiennohito.kotonoha.rest.AuthObject;

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
