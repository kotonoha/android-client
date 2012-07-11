package org.eiennohito.kotonoha.android.db.migration;

import org.eiennohito.kotonoha.android.db.migration.m.M00;
import org.eiennohito.kotonoha.android.db.migration.m.M01;
import org.eiennohito.kotonoha.android.db.migration.m.M02;

/**
 * @author eiennohito
 * @since 24.04.12
 */
public class Migrations {
  public static final Migration[] MIGRATION_ARRAY = new Migration[]{
    new M00(),
    new M01(),
    new M02()
  };
}
