package ws.kotonoha.android.db.migration.m;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ws.kotonoha.android.db.DatabaseHelper;
import ws.kotonoha.android.db.migration.Migration;
import ws.kotonoha.server.model.events.MarkEvent;
import ws.kotonoha.server.model.learning.Example;
import ws.kotonoha.server.model.learning.ItemLearning;
import ws.kotonoha.server.model.learning.Word;
import ws.kotonoha.server.model.learning.WordCard;

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
