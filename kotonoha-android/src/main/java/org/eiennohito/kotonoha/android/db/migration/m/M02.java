package org.eiennohito.kotonoha.android.db.migration.m;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.eiennohito.kotonoha.android.db.DatabaseHelper;
import org.eiennohito.kotonoha.android.db.migration.Migration;
import org.eiennohito.kotonoha.model.events.AddWordEvent;
import org.eiennohito.kotonoha.model.events.ChangeWordStatusEvent;
import org.eiennohito.kotonoha.model.learning.WordCard;

import java.sql.SQLException;

/**
 * @author eiennohito
 * @since 29.06.12
 */
public class M02 extends Migration {
  @Override
  public void migrate(SQLiteDatabase db, ConnectionSource src, DatabaseHelper helper) throws SQLException {
    TableUtils.dropTable(src, WordCard.class, true);
    TableUtils.createTable(src, WordCard.class);
    TableUtils.createTable(src, AddWordEvent.class);
    TableUtils.createTable(src, ChangeWordStatusEvent.class);
  }
}
