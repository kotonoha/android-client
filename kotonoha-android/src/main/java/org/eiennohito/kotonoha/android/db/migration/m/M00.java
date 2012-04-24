package org.eiennohito.kotonoha.android.db.migration.m;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.eiennohito.kotonoha.android.db.DatabaseHelper;
import org.eiennohito.kotonoha.android.db.migration.Migration;
import org.eiennohito.kotonoha.model.events.MarkEvent;
import org.eiennohito.kotonoha.model.learning.Example;
import org.eiennohito.kotonoha.model.learning.ItemLearning;
import org.eiennohito.kotonoha.model.learning.Word;
import org.eiennohito.kotonoha.model.learning.WordCard;

import java.sql.SQLException;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public class M00 extends Migration {
  public void migrate(SQLiteDatabase db, ConnectionSource src, DatabaseHelper helper) throws SQLException {
    TableUtils.createTable(src, Word.class);
    TableUtils.createTable(src, WordCard.class);
    TableUtils.createTable(src, ItemLearning.class);
    TableUtils.createTable(src, Example.class);
    TableUtils.createTable(src, MarkEvent.class);
  }
}
