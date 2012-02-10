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
package org.eiennohito.kotonoha.android;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import org.eiennohito.kotonoha.model.learning.Word;

/**
 * @author eiennohito
 * @since 10.02.12
 */
public class DataService extends OrmLiteBaseService{

  public void markWord(Word word, double mark, double time) {
    //To change body of created methods use File | Settings | File Templates.
  }

  public boolean hasNextWord() {
    return false;  //To change body of created methods use File | Settings | File Templates.
  }

  public boolean preloadWords() {
    return false;  //To change body of created methods use File | Settings | File Templates.
  }

  public Word getNextWord() {
    return null;  //To change body of created methods use File | Settings | File Templates.
  }

  public void pushSubmitMarks() {
    //To change body of created methods use File | Settings | File Templates.
  }

  public void loadWordsAsync(WordsLoadedCallback callback) {

  }


  public class DataServiceBinder extends Binder {

    public DataService getService() {
      return DataService.this;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

}
