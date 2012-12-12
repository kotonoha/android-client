package ws.kotonoha.android.services.eventual;

import ws.kotonoha.android.services.DataService;
import ws.kotonoha.android.services.MarkService;

/**
 * @author eiennohito
 * @since 11.07.12
 */
public class SendMarksES extends EventualService {

  private final MarkService markSvc;

  public SendMarksES(DataService svc) {
    super(svc);
    markSvc = svc.getMarkSvc();
  }

  @Override
  boolean hasWork() {
    return markSvc.countMarks() != 0;
  }

  @Override
  public void run() {
    markSvc.submit();
  }
}
