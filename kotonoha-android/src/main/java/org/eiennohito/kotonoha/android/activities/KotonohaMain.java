package org.eiennohito.kotonoha.android.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import org.eiennohito.kotonoha.android.R;
import org.eiennohito.kotonoha.android.services.DataService;
import org.eiennohito.kotonoha.android.util.WordsLoadedCallback;
import org.eiennohito.kotonoha.android.voice.VoiceRecognition;

public class KotonohaMain extends Activity {

  private static final String TAG = Activity.class.getSimpleName();

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    findViewById(R.id.preloadWordsBtn).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        findViewById(R.id.preloadWordsBtn).setClickable(false);
        findViewById(R.id.showWordBtn).setClickable(false);
        service.loadWordsAsync(wordsLoadedCallback);
      }
    });

    findViewById(R.id.showWordBtn).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        startActivity(new Intent(KotonohaMain.this, WordFormActivity.class));
      }
    });

    findViewById(R.id.record).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        startActivity(new Intent(KotonohaMain.this, VoiceRecognition.class));
      }
    });

    connectToService();
  }


  private WordsLoadedCallback wordsLoadedCallback = new WordsLoadedCallback() {
    public void wordsLoaded(boolean success) {
      handleLoadWords(success);
    }
  };


  private void handleLoadWords(boolean success) {
    TextView tv = (TextView)findViewById(R.id.statusText);
    tv.setText(success ? "Ok" : "Not Ok");

    findViewById(R.id.preloadWordsBtn).setClickable(true);
    if (success) {
      findViewById(R.id.showWordBtn).setClickable(true);
    }
  }


  private DataService service;

  private ServiceConnection connection = new ServiceConnection() {
    public void onServiceDisconnected(ComponentName name) {
      service = null;
      //should not get here
    }

    public void onServiceConnected(ComponentName name, IBinder binder) {
      service = ((DataService.DataServiceBinder)binder).getService();
      service.loadWordsAsync(wordsLoadedCallback);
    }
  };

  private void connectToService() {
    bindService(new Intent(this, DataService.class), connection, BIND_AUTO_CREATE);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (service != null) {
      unbindService(connection);
    }
  }
}