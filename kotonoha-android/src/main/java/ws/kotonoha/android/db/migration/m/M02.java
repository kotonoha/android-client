package ws.kotonoha.android.db.migration.m;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ws.kotonoha.android.db.DatabaseHelper;
import ws.kotonoha.android.db.migration.Migration;
import ws.kotonoha.server.model.learning.ReviewCard;

import java.sql.SQLException;

/**
 * @author eiennohito
 * @since 02.03.13
 */
public class M02 extends Migration {
  @Override
  public void migrate(SQLiteDatabase db, ConnectionSource src, DatabaseHelper helper) throws SQLException {
    TableUtils.createTable(src, ReviewCard.class);
  }
}
