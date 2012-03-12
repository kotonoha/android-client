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
import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import com.j256.ormlite.misc.TransactionManager;
import de.akquinet.android.androlog.Log;
import org.apache.http.params.HttpConnectionParams;
import org.eiennohito.kotonoha.android.db.DatabaseHelper;
import org.eiennohito.kotonoha.android.db.Values;
import org.eiennohito.kotonoha.android.rest.request.GetScheduledCards;
import org.eiennohito.kotonoha.android.rest.request.PostMarkEvents;
import org.eiennohito.kotonoha.android.transfer.WordWithCard;
import org.eiennohito.kotonoha.android.util.*;
import org.eiennohito.kotonoha.model.events.MarkEvent;
import org.eiennohito.kotonoha.model.learning.Container;
import org.eiennohito.kotonoha.model.learning.Word;
import org.eiennohito.kotonoha.model.learning.WordCard;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

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
  AndroidHttpClient httpClient;

  @Override
  public void onCreate() {
    super.onCreate();
    httpClient = AndroidHttpClient.newInstance("Kotonoha/1.0");
    
    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
    HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10000);

    markSvc = new MarkService(this);
    cardSvc = new CardService(this);
    wordSvc = new WordService(this);
  }

  public void markWord(WordCard card, double mark, double time) {
    markSvc.addMark(card, mark, time);
    wordSvc.prune(card.getWord());
    Scheduler.schedule(new Runnable() {
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
    Log.d(this, String.format("it's %d cards now", cnt));
    if (cnt < 25) {
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
    if (card == null) {
      return null;
    }
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

  
  public void loadWordsAsync(final WordsLoadedCallback callback) {
    //int count = Math.min(49, 25 + cardSvc.countPresent());
    int count = 49;
    GetScheduledCards gsc = new GetScheduledCards(httpClient, count, new ValueCallback<Container>() {
      public void process(Container val) {
        callback.wordsLoaded(loadContainer(val));
      }
    });

    gsc.onFailure(new Runnable() {
      public void run() {
        callback.wordsLoaded(false);
      }
    });

    Scheduler.postRest(gsc);
  }

  private boolean loadContainer(final Container val) {
    try {
      TransactionManager.callInTransaction(getConnectionSource(), new Callable<Object>() {
        public Object call() throws Exception {
          wordSvc.process(val.getWords());
          cardSvc.process(val.getCards());
          return null;
        }
      });
    } catch (Exception e) {
      Log.e(this, "Couldn't load values from container", e);
      return false;
    }
    return true;
  }


  public void sendMarks(Callable<List<MarkEvent>> marks) {
    PostMarkEvents pme = new PostMarkEvents(httpClient, marks, new SuccessCallback<List<MarkEvent>, Values>() {
    public void onOk(List<MarkEvent> marks, Values values) {
        markSvc.removeMarks(marks);
        cardSvc.removeCardsFor(marks);
      }
    });

    pme.onFailure(new ErrorCallback<List<MarkEvent>>() {
      public void onError(List<MarkEvent> marks) {
        markSvc.markForResend(marks);
      }
    });

    Scheduler.postRest(pme);
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
    //Scheduler.destroy();
    httpClient.close();
    super.onDestroy();
  }
}
