package ws.kotonoha.android.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import ws.kotonoha.android.R;

/**
 * @author eiennohito
 * @since 12.12.12
 */
public class SettingsActivity extends PreferenceActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.options);
  }
}
