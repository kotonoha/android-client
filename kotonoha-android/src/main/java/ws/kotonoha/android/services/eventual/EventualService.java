package ws.kotonoha.android.services.eventual;

import ws.kotonoha.android.services.DataService;

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
