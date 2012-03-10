package org.eiennohito.kotonoha.android.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eiennohito.kotonoha.model.converters.DateTimeTypeConverter;
import org.joda.time.DateTime;

/**
 * @author eiennohito
 * @since 08.03.12
 */
public class GsonInstance {
  private Gson gson;

  private GsonInstance() {
    GsonBuilder gb = new GsonBuilder();
    gb.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter());
    gson = gb.create();
  }

  static GsonInstance instance_;

  public static synchronized Gson instance() {
    if (instance_ == null) {
      instance_ = new GsonInstance();
    }
    return instance_.gson;
  }
}
