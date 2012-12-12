package ws.kotonoha.android.db.migration;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import ws.kotonoha.android.db.DatabaseHelper;

import java.sql.SQLException;

/**
 * @author eiennohito
 * @since 24.04.12
 */
abstract public class Migration {
  abstract public void migrate(SQLiteDatabase db, ConnectionSource src, DatabaseHelper helper) throws SQLException;
}
