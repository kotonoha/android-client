package ws.kotonoha.android.db.migration.m;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import ws.kotonoha.android.db.DatabaseHelper;
import ws.kotonoha.android.db.migration.Migration;

import java.sql.SQLException;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public class M01 extends Migration {
  @Override
  public void migrate(SQLiteDatabase db, ConnectionSource src, DatabaseHelper helper) throws SQLException {
  }
}
