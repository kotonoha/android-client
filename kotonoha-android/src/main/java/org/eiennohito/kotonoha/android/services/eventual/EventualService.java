package org.eiennohito.kotonoha.android.services.eventual;

import org.eiennohito.kotonoha.android.services.DataService;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public abstract class EventualService implements Runnable {
  protected DataService svc;

  protected EventualService(DataService svc) {
    this.svc = svc;
  }

  abstract boolean hasWork();
}
