package org.eiennohito.kotonoha.android.services;

import de.akquinet.android.androlog.Log;
import org.eiennohito.kotonoha.android.config.AppConfig;
import org.eiennohito.kotonoha.android.json.GsonInstance;

import java.io.*;

/**
 * @author eiennohito
 * @since 01.04.12
 */
public class ConfigService {

  private static final String CONFIG_NAME = "config.json";
  private AppConfig conf;
  private final DataService dataService;

  public ConfigService(DataService dataService) {
    this.dataService = dataService;
  }

  void load() {
    File path = dataService.getFileStreamPath(CONFIG_NAME);
    try {
      FileInputStream inp = new FileInputStream(path);
      InputStreamReader isr = new InputStreamReader(inp, "UTF-8");
      conf = GsonInstance.instance().fromJson(isr, AppConfig.class);
      inp.close();
    } catch (IOException e) {
      Log.e(this, "Error when reading config from memory", e);
    }
  }

  void save() {
    File path = dataService.getFileStreamPath(CONFIG_NAME);
    try {
      PrintWriter pw = new PrintWriter(path, "UTF-8");
      GsonInstance.instance().toJson(conf, pw);
    } catch (IOException ex) {
      Log.e(this, "Can't write config to file");
    }
  }

  void asave() {
    Scheduler.schedule(new Runnable() {
      public void run() {
        save();
      }
    });
  }

  public synchronized AppConfig config() {
    if (conf == null) {
      conf = new AppConfig();
    }
    return conf;
  }
}
