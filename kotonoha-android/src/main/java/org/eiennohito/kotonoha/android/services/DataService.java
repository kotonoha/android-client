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
package org.eiennohito.kotonoha.android.services;

import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpConnectionParams;
import org.eiennohito.kotonoha.android.db.DatabaseHelper;
import org.eiennohito.kotonoha.android.db.Values;
import org.eiennohito.kotonoha.android.transfer.WordWithCard;
import org.eiennohito.kotonoha.android.util.AddressUtil;
import org.eiennohito.kotonoha.android.util.WordsLoadedCallback;
import org.eiennohito.kotonoha.model.converters.DateTimeTypeConverter;
import org.eiennohito.kotonoha.model.events.MarkEvent;
import org.eiennohito.kotonoha.model.learning.Container;
import org.eiennohito.kotonoha.model.learning.Word;
import org.eiennohito.kotonoha.model.learning.WordCard;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author eiennohito
 * @since 10.02.12
 */
public class DataService extends OrmLiteBaseService<DatabaseHelper> {
  public final static URI serviceUri = URI.create(AddressUtil.baseUri);

  MarkService markSvc;
  CardService cardSvc;
  WordService wordSvc;

  /**
   * All web service calls should be done through this scheduler.
   */
  public final Scheduler webScheduler = new Scheduler(1, 1);
  public final Scheduler defaultScheduler = new Scheduler(1, 5);
  public final ScheduledThreadPoolExecutor timerSc = new ScheduledThreadPoolExecutor(2);
  AndroidHttpClient httpClient;

  @Override
  public void onCreate() {
    super.onCreate();
    httpClient = AndroidHttpClient.newInstance("Kotonoha/1.0");
    
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 5000);
    HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10000);

    
    markSvc = new MarkService(this);
    cardSvc = new CardService(this);
    wordSvc = new WordService(this);

    markSvc.clear();
    cardSvc.clear();
    wordSvc.clear();
  }

  public void markWord(WordCard card, double mark, double time) {
    markSvc.addMark(card, mark, time);
    wordSvc.prune(card.getWord());
    defaultScheduler.schedule(new Runnable() {
      public void run() {
        checkGetNewWords();
      }
    });
  }

  private int callCount = 0;
  private void checkGetNewWords() {
    if (++callCount % 4 == 0) {
      pushSubmitMarks();
    }
    int cnt = cardSvc.countPresent();
    Log.d("Kotonoha", String.format("it's %d cards now", cnt));
    if (cnt < 9) {
      loadWordsAsync(WordsLoadedCallback.EMPTY);
    }
  }

  public boolean hasNextCard() {
    return cardSvc.hasNextCard();
  }

  public boolean preloadWords() {
    loadWordsAsync(WordsLoadedCallback.EMPTY);
    return cardSvc.hasNextCard();
  }

  public WordWithCard getNextWord() {
    WordCard card = cardSvc.nextCard();
    Word word = wordSvc.wordForCard(card.getWord());
    return checkWnC(new WordWithCard(word, card));
  }

  private WordWithCard checkWnC(WordWithCard wc) {
    if (wc.word != null) {
      return wc;
    }
    cardSvc.drop(wc.card);
    return getNextWord();
  }

  public void pushSubmitMarks() {
    markSvc.submit();
  }

  
  private AtomicBoolean areWordsLoading = new AtomicBoolean(false);
  
  public void loadWordsAsync(final WordsLoadedCallback callback) {
    if (areWordsLoading.get()) {
      return;
    }
    areWordsLoading.set(true);
    URI uri = serviceUri.resolve("words/scheduled/20");
    HttpUriRequest req = new HttpGet(uri);
    executeHttpQuery(req, new GsonObjectParser<Container>(Container.class), 
      new ValueCallback<Container>() {
        public void process(Container val) {
          areWordsLoading.set(false);
          if (val == null) {
            callback.wordsLoaded(false);
          } else {
            callback.wordsLoaded(loadContainer(val));
          }
        }
      });
    
  }

  private boolean loadContainer(Container val) {
    try {
      wordSvc.process(val.getWords());
      cardSvc.process(val.getCards());
    } catch (Exception e) {
      Log.e("Kotonoha", "Couldn't load values from container", e);
      return false;
    }
    return true;
  }
  
  public <F> void executeHttpQuery(final HttpUriRequest req, final ValueProcessor<HttpEntity, F> proc, final ValueCallback<F> cb) {
    Runnable r = new Runnable() {      
      public void run() {
        HttpResponse resp;
        try {
          resp = httpClient.execute(req);
        } catch (IOException e) {
          Log.e("Kotonoha", "Couldn't execute http query ", e);
          scheduleAnswer(cb, null);
          return;
        }
        int code = resp.getStatusLine().getStatusCode();        
        if (code != 200) {
          scheduleAnswer(cb, null);
          Log.e("Kotonoha", "Server error: got code" + code);
          Log.e("Kotonoha", "Reason is:" + resp.getStatusLine().getReasonPhrase());
          return;
        }
        HttpEntity entity = resp.getEntity();
        F val;
        try {
          val = proc.process(entity);
        } catch (Exception e) {
          Log.e("Kotonoha", "Couldn't process http entity", e);
          scheduleAnswer(cb, null);
          return;
        }
        scheduleAnswer(cb, val);
      }
    };
    webScheduler.schedule(r);
  }
  
  public <F> void scheduleAnswer(final ValueCallback<F> cb, final F val) {
    if (cb == null) { return; }
    defaultScheduler.schedule(new Runnable() {
      public void run() {        
        cb.process(val); 
      }
    });
  }

  public void sendMarks(final List<MarkEvent> marks) {
    URI uri = serviceUri.resolve("events/mark");
    GsonBuilder gb = new GsonBuilder();
    gb.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter());
    Gson gson = gb.create();
    String json = gson.toJson(marks);
    
    HttpPost req = new HttpPost(uri);
    StringEntity se;
    try {
      se = new StringEntity(json, "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    se.setContentType("application/json");
    req.setEntity(se);

    executeHttpQuery(req, new GsonObjectParser<Values>(Values.class),
    new ValueCallback<Values>() {
      public void process(Values val) {
        if (val != null) {
          markSvc.removeMarks(marks);
          cardSvc.removeCardsFor(marks);
        }
      }
    });
  }

  interface ValueProcessor<T, F> {
    F process(final T val) throws Exception;
  } 
  
  interface ValueCallback<T> {
    void process(final T val);
  }
  
  
  public static class GsonObjectParser<T> implements ValueProcessor<HttpEntity, T> {
    private Gson gson;
    private Class<T> clazz;

    public GsonObjectParser(Class<T> clazz) {
      this.clazz = clazz;
      GsonBuilder gb = new GsonBuilder();
      gb.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter());
      gson = gb.create();
    }

    public T process(HttpEntity val) throws Exception {
      InputStream content = val.getContent();
      InputStreamReader reader = new InputStreamReader(content, Charset.forName("UTF-8"));
      return gson.fromJson(reader, clazz);
    }
  }


  public class DataServiceBinder extends Binder {

    public DataService getService() {
      return DataService.this;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return new DataServiceBinder();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    timerSc.shutdown();
    webScheduler.shutdown();
    defaultScheduler.shutdown();
    httpClient.close();
  }
}
