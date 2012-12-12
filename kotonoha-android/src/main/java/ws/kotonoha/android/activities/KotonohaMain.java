package ws.kotonoha.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import de.akquinet.android.androlog.Log;
import ws.kotonoha.android.R;
import ws.kotonoha.android.async.Promise;
import ws.kotonoha.android.services.DataService;
import ws.kotonoha.android.util.AuthUtil;
import ws.kotonoha.android.util.ValueCallback;
import ws.kotonoha.android.util.WordsLoadedCallback;
import ws.kotonoha.android.util.zxing.IntentIntegrator;
import ws.kotonoha.android.util.zxing.IntentResult;
import ws.kotonoha.android.voice.VoiceRecognition;

import static ws.kotonoha.android.util.ActivityUtil.setAllClickable;

public class KotonohaMain extends Activity {

  enum State {
    Initial,
    Configured,
    AsyncLoading,
    Ready
  }

  private State state = State.Initial;

  private static final int[] availableOnLogin = new int[]{
    R.id.preloadWordsBtn,
    R.id.record,
    R.id.showWordBtn
  };

  void callLoginDialog() {
    AlertDialog ad = new AlertDialog.Builder(this)
      .setTitle(R.string.login_method_info)
      .setMessage(R.string.login_form_message)
      .setPositiveButton(R.string.login_qr, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          handleQrLogin();
        }
      }).setNegativeButton(R.string.login_browser, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          handleBrowserLogin();
        }
      }).create();
    ad.show();
  }

  private void handleBrowserLogin() {
    Intent i = new Intent(this, BrowserAuthActivity.class);
    startActivityForResult(i, 520);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_frm_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_main_options:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      default:
        return false;
    }
  }

  private void handleQrLogin() {
    IntentIntegrator ii = new IntentIntegrator(this);
    ii.initiateScan();
  }


  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.init(this, "kotonoha-log.properties");

    super.onCreate(savedInstanceState);

    if (service == null) {
      performInitialization();
    }
  }

  private void performInitialization() {
    service = new Promise<DataService>();
    setContentView(R.layout.main);
    connectToService();
    toInitial();


    findViewById(R.id.preloadWordsBtn).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        service.then(new ValueCallback<DataService>() {
          @Override
          public void process(DataService val) {
            if (toConfigured(val)) {
              aloadWords();
            }
          }
        });
      }
    });

    findViewById(R.id.status_btn).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        service.then(new ValueCallback<DataService>() {
          @Override
          public void process(DataService val) {
            val.postStatus(new ValueCallback<String>() {
              public void process(final String val) {
                runOnUiThread(new Runnable() {
                  public void run() {
                    setStatusText(val);
                  }
                });
              }
            });
          }
        });
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
        callLoginDialog();
      }
    });
  }

  private void handleIntents(final Intent intent) {
    if (intent != null && intent.getAction() != null && intent.getAction().equals("ws.kotonoha.AUTH")) {
      service.then(new ValueCallback<DataService>() {
        @Override
        public void process(DataService val) {
          String data = intent.getData().getQueryParameter("data");
          if (data != null) {
            String decoded = AuthUtil.compileAuth(data);
            eatAuth(decoded);
          }
        }
      });
    }
  }

  private boolean toInitial() {
    state = State.Initial;
    setAllClickable(availableOnLogin, this, false);
    return true;
  }

  private boolean toConfigured(final DataService service) {
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

  private boolean toAsyncLoading(final DataService service) {
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
    setStatusText("Are you ready to learn? I am!");
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case 520: {
        if (resultCode == 0) {
          handleIntents(data);
        }
      }
      break;
      default: {
        IntentResult res = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (res != null) {
          String contents = res.getContents();
          eatAuth(contents);
        }
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }


  private void eatAuth(final String contents) {
    service.then(new ValueCallback<DataService>() {
      @Override
      public void process(DataService val) {
        boolean result = val.parseAuthInfo(contents);
        if (result) {
          if (toConfigured(val)) {
            setStatusText("Auth ok, loading words");
            aloadWords();
          }
        }
      }
    });
  }

  private WordsLoadedCallback wordsLoadedCallback(final DataService svc) {
    return new WordsLoadedCallback() {
      public void wordsLoaded(final boolean success) {
        runOnUiThread(new Runnable() {
          public void run() {
            handleLoadWords(svc, success);
          }
        });
      }
    };
  }

  private void setStatusText(final String text) {
    final TextView tv = (TextView) findViewById(R.id.statusText);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        tv.setText(text);
      }
    });
  }


  private void handleLoadWords(DataService svc, boolean success) {
    setStatusText(success ? "Words have loaded successfully" : "Error when loading words");
    if (success) {
      toReady();
    } else {
      toConfigured(svc);
    }
  }


  private Promise<DataService> service;

  private ServiceConnection connection = new ServiceConnection() {
    public void onServiceDisconnected(ComponentName name) {
      service = null;
    }

    public void onServiceConnected(ComponentName name, IBinder binder) {
      final DataService svc = ((DataService.DataServiceBinder) binder).getService();
      service.tryResolve(svc);
      if (!toConfigured(svc)) {
        setStatusText("Please login in kotonoha!");
      } else {
        if (svc.hasNextCard()) {
          svc.loadWordsAsync(WordsLoadedCallback.EMPTY); //update words
          toReady();
        } else {
          aloadWords();
        }
      }
    }
  };

  private void aloadWords() {
    service.then(new ValueCallback<DataService>() {
      @Override
      public void process(DataService val) {
        if (toAsyncLoading(val)) {
          setStatusText("Loading words asynchronously");
        }
        val.loadWordsAsync(wordsLoadedCallback(val));
      }
    });
  }

  private void connectToService() {
    bindService(new Intent(this, DataService.class), connection, BIND_AUTO_CREATE);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (service != null) {
      unbindService(connection);
      service = null;
    }
  }
}
