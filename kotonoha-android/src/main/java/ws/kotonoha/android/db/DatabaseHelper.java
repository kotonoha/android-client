/*
 * Copyright 2012 eiennohito
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.kotonoha.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import de.akquinet.android.androlog.Log;
import ws.kotonoha.android.db.migration.Migration;
import ws.kotonoha.android.db.migration.Migrations;
import ws.kotonoha.server.model.events.AddWordEvent;
import ws.kotonoha.server.model.events.ChangeWordStatusEvent;
import ws.kotonoha.server.model.events.MarkEvent;
import ws.kotonoha.server.model.learning.Example;
import ws.kotonoha.server.model.learning.ItemLearning;
import ws.kotonoha.server.model.learning.Word;
import ws.kotonoha.server.model.learning.WordCard;

import java.sql.SQLException;

/**
 * @author eiennohito
 * @since 11.02.12
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

  private static String dbname = "kotonoha.db";
  private static int version = Migrations.MIGRATION_ARRAY.length;

  public DatabaseHelper(Context c) {
    super(c, dbname, null, version);
  }


  @Override
  public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
    onUpgrade(database, connectionSource, 0, version);
  }

  @Override
  public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    if (newVersion > oldVersion) {
      Log.i(String.format("Performing migration from %d to %d", oldVersion, newVersion));
      for (int ver = oldVersion; ver < newVersion; ++ver) {
        try {
          Migration migration = Migrations.MIGRATION_ARRAY[ver];
          migration.migrate(database, connectionSource, this);
        } catch (SQLException ex) {
          Log.e(this, String.format("Error in migrating from %d to %d", oldVersion, newVersion), ex);
        }
      }
    }
  }

  public RuntimeExceptionDao<Word, String> getWordDao() {
    return createDaoForClass(Word.class);
  }

  public RuntimeExceptionDao<WordCard, String> getWordCardDao() {
    return createDaoForClass(WordCard.class);
  }

  public RuntimeExceptionDao<MarkEvent, String> getMarkEventDao() {
    return createDaoForClass(MarkEvent.class);
  }

  public RuntimeExceptionDao<Example, Long> getExampleDao() {
    return createDaoForClass(Example.class);
  }

  public RuntimeExceptionDao<ItemLearning, Long> getLearningDao() {
    return createDaoForClass(ItemLearning.class);
  }

  public RuntimeExceptionDao<AddWordEvent, String> getAddWordEventDao() {
    return createDaoForClass(AddWordEvent.class);
  }

  public RuntimeExceptionDao<ChangeWordStatusEvent, String> getChangeWordDao() {
    return createDaoForClass(ChangeWordStatusEvent.class);
  }

  public <T, X> RuntimeExceptionDao<T, X> createDaoForClass(Class<T> clazz) {
    try {
      return RuntimeExceptionDao.createDao(connectionSource, clazz);
    } catch (SQLException e) {
      Log.e("K/DH", "Error in creating table", e);
      throw new RuntimeException(e);
    }
  }


}
