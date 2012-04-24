package org.eiennohito.kotonoha.android.db.migration.m;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.eiennohito.kotonoha.android.db.DatabaseHelper;
import org.eiennohito.kotonoha.android.db.migration.Migration;
import org.eiennohito.kotonoha.model.events.AddWordEvent;
import org.eiennohito.kotonoha.model.events.ChangeWordStatusEvent;

import java.sql.SQLException;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public class M01 extends Migration {
  @Override
  public void migrate(SQLiteDatabase db, ConnectionSource src, DatabaseHelper helper) throws SQLException {
    TableUtils.clearTable(src, AddWordEvent.class);
    TableUtils.clearTable(src, ChangeWordStatusEvent.class);
  }
}
