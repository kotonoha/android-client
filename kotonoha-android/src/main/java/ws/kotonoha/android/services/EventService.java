package ws.kotonoha.android.services;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import ws.kotonoha.android.db.DatabaseHelper;
import ws.kotonoha.server.model.events.ChangeWordStatusEvent;

import java.sql.SQLException;
import java.util.List;

/**
 * @author eiennohito
 * @since 05.07.12
 */
public class EventService implements Purgeable {
  private final DataService svc;
  private final DatabaseHelper helper;
  private RuntimeExceptionDao<ChangeWordStatusEvent, String> dao;

  public EventService(DataService svc) {
    this.svc = svc;
    helper = svc.getHelper();
    dao = helper.getChangeWordDao();
  }


  public void publishChangeStatus(ChangeWordStatusEvent cv) {
    dao.create(cv);
  }

  public boolean hasEvents() {
    return dao.countOf() != 0;
  }

  public List<ChangeWordStatusEvent> loadEvents() {
    return dao.queryForAll();
  }

  @Override
  public void purge() {
    DeleteBuilder<ChangeWordStatusEvent, String> db = dao.deleteBuilder();
    try {
      dao.delete(db.prepare());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void delete(List<ChangeWordStatusEvent> evnts) {
    dao.delete(evnts);
  }
}
