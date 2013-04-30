package ws.kotonoha.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import de.akquinet.android.androlog.Log;
import ws.kotonoha.android.BuildConfig;
import ws.kotonoha.android.R;
import ws.kotonoha.android.util.ApiCodes;

import java.io.ByteArrayInputStream;

/**
 * @author eiennohito
 * @since 12.12.12
 */
public class BrowserAuthActivity extends Activity {

  private static final String url = loginUrl();

  private static String loginUrl() {
    if (BuildConfig.DEBUG) {
      return "http://weaboo.net:9867/oauth/request";
    } else {
      return "http://kotonoha.ws/oauth/request";
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.browser_auth);
    super.onCreate(savedInstanceState);

    WebView view = (WebView) findViewById(R.id.wview);
    view.loadUrl(url + "?key=" + ApiCodes.publicKey);
    view.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.contains("intent/auth")) {
          performAuth(url);
          return true;
        }
        return false;
      }

      @Override
      public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url.contains("intent/auth")) {
          //performAuth(url);
          return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
        } else {
          return null;
        }
      }
    });
  }

  private void performAuth(final String url) {
    final Activity self = this;
    Runnable runnable = new Runnable() {
      public void run() {
        try {
          Uri uri = Uri.parse(url);
          Intent intent = new Intent("ws.kotonoha.AUTH", uri);
          self.setResult(0, intent);
        } catch (RuntimeException e) {
          Log.e(BrowserAuthActivity.this, "Trying to pass auth to main activity", e);
          throw e;
        }
      }
    };
    runOnUiThread(runnable);
    finish();
  }
}
