package ws.kotonoha.android.db.migration.m;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ws.kotonoha.android.db.DatabaseHelper;
import ws.kotonoha.android.db.migration.Migration;
import ws.kotonoha.server.model.learning.WordCard;

import java.sql.SQLException;

/**
 * @author eiennohito
 * @since 14.12.12
 */
public class M01 extends Migration {
  @Override
  public void migrate(SQLiteDatabase db, ConnectionSource src, DatabaseHelper helper) throws SQLException {
    TableUtils.dropTable(src, WordCard.class, true);
    TableUtils.createTable(src, WordCard.class);
  }
}
