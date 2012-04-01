package org.eiennohito.kotonoha.android.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import de.akquinet.android.androlog.Log;
import org.eiennohito.kotonoha.android.R;
import org.eiennohito.kotonoha.android.services.DataService;
import org.eiennohito.kotonoha.android.util.WordsLoadedCallback;
import org.eiennohito.kotonoha.android.util.zxing.IntentIntegrator;
import org.eiennohito.kotonoha.android.util.zxing.IntentResult;
import org.eiennohito.kotonoha.android.voice.VoiceRecognition;

import static org.eiennohito.kotonoha.android.util.ActivityUtil.setAllClickable;

public class KotonohaMain extends Activity {

  enum State {
    Initial,
    Configured,
    AsyncLoading,
    Ready
  }

  private State state = State.Initial;

  private static final int[] availableOnLogin = new int[] {
    R.id.preloadWordsBtn,
    R.id.record,
    R.id.showWordBtn
  };

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.init(this, "kotonoha-log.properties");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    findViewById(R.id.preloadWordsBtn).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        if (toConfigured()) {
          aloadWords();
        }
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

    findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        IntentIntegrator ii = new IntentIntegrator(KotonohaMain.this);
        ii.initiateScan();
      }
    });

    connectToService();
  }

  private boolean toInitial() {
    state = State.Initial;
    setAllClickable(availableOnLogin, this, false);
    return true;
  }

  private boolean toConfigured() {
    if (!service.isAuthOk()) {
      return false;
    }
    state = State.Configured;
    Runnable runnable = new Runnable() {
      public void run() {
        findViewById(R.id.preloadWordsBtn).setClickable(false);
        findViewById(R.id.showWordBtn).setClickable(false);
      }
    };
    runOnUiThread(runnable);
    return true;
  }

  private boolean toAsyncLoading() {
    if (state != State.Configured) {
      return false;
    }
    state = State.AsyncLoading;
    Runnable runnable = new Runnable() {
      public void run() {
        findViewById(R.id.showWordBtn).setClickable(service.hasNextCard());
        findViewById(R.id.preloadWordsBtn).setClickable(false);
      }
    };
    runOnUiThread(runnable);
    return true;
  }

  private boolean toReady() {
    state = State.Ready;
    setAllClickable(availableOnLogin, this, true);
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    IntentResult res = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    if (res != null) {
      String contents = res.getContents();
      boolean result = service.parseAuthInfo(contents);
      if (result) {
        toConfigured();
        setStatusText("Auth ok, loading words");
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private WordsLoadedCallback wordsLoadedCallback = new WordsLoadedCallback() {
    public void wordsLoaded(final boolean success) {
      runOnUiThread(new Runnable() {
        public void run() {
          handleLoadWords(success);
        }
      });
    }
  };

  private void setStatusText(String text) {
    TextView tv = (TextView)findViewById(R.id.statusText);
    tv.setText(text);
  }


  private void handleLoadWords(boolean success) {
    setStatusText(success ? "Words have loaded successfully" : "Error when loading words");
    if (success) {
      toReady();
    } else {
      toConfigured();
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
      if (!toConfigured()) {
        setStatusText("Please login in kotonoha!");
      } else {
        if (service.hasNextCard()) {
          service.loadWordsAsync(WordsLoadedCallback.EMPTY); //update words
          toReady();
        } else {
          aloadWords();
        }
      }
    }
  };

  private void aloadWords() {
    if (toAsyncLoading()) {
      setStatusText("Loading words asynchronously");
      service.loadWordsAsync(wordsLoadedCallback);
    }
  }

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
