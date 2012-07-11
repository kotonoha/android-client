package org.eiennohito.kotonoha.android.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import org.eiennohito.kotonoha.android.R;
import org.eiennohito.kotonoha.android.services.DataService;
import org.eiennohito.kotonoha.android.transfer.WordWithCard;
import org.eiennohito.kotonoha.model.learning.Example;
import org.eiennohito.kotonoha.model.learning.Word;
import org.eiennohito.kotonoha.model.learning.WordCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author eiennohito
 * @since 27.09.2010
 */
public class WordFormActivity extends Activity {
  private static final String TAG = "Kotonoha.WordFormActivity";

  private enum WordFormState {
    Init,
    Mark,
    View
  }

  private static final int[] MARK_BTNS = new int[]{
    R.id.Mark1,
    R.id.Mark2,
    R.id.Mark3,
    R.id.Mark4,
    R.id.Mark5
  };

  private static final int[] ALL_ELEMS = new int[]{
    R.id.Example,
    //R.id.ExampleArea,
    R.id.Meaning,
    R.id.Reading,
    R.id.Writing,
    R.id.Mark1,
    R.id.Mark2,
    R.id.Mark3,
    R.id.Mark4,
    R.id.Mark5
  };

  private WordFormState state = WordFormState.Init;

  private long lastChange = 0;
  private final static long CHANGE_TIME = 100;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.word_frm_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.badCard:
        markCardAsBad();
        return true;
      default:
        return super.onOptionsItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
    }
  }

  private void markCardAsBad() {
    if (currentCard != null && currentWord != null) {
      service.setWordAsBad(currentWord.getId());
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    connectToService();

    this.setContentView(R.layout.word_form);

    View exbtn = findViewById(R.id.ExampleArea);
    exbtn.setOnTouchListener(new View.OnTouchListener() {
      public boolean onTouch(View view, MotionEvent motionEvent) {

        long last = lastChange;
        lastChange = System.currentTimeMillis();
        long epl = lastChange - last;

        if (epl < CHANGE_TIME) {
          return true;
        }

        if (state == WordFormState.Init) {
          state = WordFormState.Mark;
          massSetVisibility(ALL_ELEMS, View.VISIBLE);
          return true;
        }

        int width = view.getWidth();
        float x = motionEvent.getX();
        changeExample(x < (width / 2));
        return true;
      }
    });


    for (int i = 0; i < MARK_BTNS.length; ++i) {
      int id = MARK_BTNS[i];
      this.findViewById(id).setOnClickListener(new MarkBtnListener(i + 1));
    }

    findViewById(R.id.nextWordBtn).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        view.setVisibility(View.INVISIBLE);
        massSetVisibility(ALL_ELEMS, View.INVISIBLE);
        nextWord();
      }
    });
  }

  private void showNextWordBtn() {
    massSetVisibility(MARK_BTNS, View.INVISIBLE);
    findViewById(R.id.nextWordBtn).setVisibility(View.VISIBLE);
  }

  @Override
  protected void onStart() {
    super.onStart();
    massSetVisibility(ALL_ELEMS, View.INVISIBLE);
    showNextWordBtn();
  }

  @Override
  protected void onPause() {
    super.onPause();
    service.pushSubmitMarks();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (service != null) {
      service.pushSubmitMarks();
      unbindService(connection);
    }
  }

  private Word currentWord;
  private WordCard currentCard;
  private int exampleIndex;
  private List<Example> examples;
  private long wordMapTime;

  /**
   * Changes current example to next or previous.
   *
   * @param prev true if previous
   */
  private void changeExample(boolean prev) {

    if (examples == null || examples.size() == 0) {
      setText(R.id.Example, getString(R.string.no_examples));
      return;
    }

    int nextIdx = exampleIndex + (prev ? -1 : 1);
    if (nextIdx < 0) {
      nextIdx = examples.size() - 1;
    }
    if (nextIdx >= examples.size()) {
      nextIdx = 0;
    }

    exampleIndex = nextIdx;

    Example ex = examples.get(exampleIndex);
    String exString = String.format("[%d/%d]\n%s\n%s",
      exampleIndex + 1, examples.size(), ex.getExample(), ex.getTranslation());
    setText(R.id.Example, exString);
  }

  private void massSetVisibility(int[] views, int visibility) {
    for (int view : views) {
      View v = findViewById(view);
      v.setVisibility(visibility);
    }
  }

  private void setText(int id, String text) {
    TextView tv = (TextView) findViewById(id);
    tv.setText(text);
  }

  private void mapCurrentWord() {
    setText(R.id.Writing, currentWord.getWriting());
    setText(R.id.Reading, currentWord.getReading());
    setText(R.id.Meaning, currentWord.getMeaning());

    exampleIndex = -1;
    wordMapTime = System.currentTimeMillis();
    examples = new ArrayList<Example>(currentWord.getExamples());
    changeExample(false);
  }

  class MarkBtnListener implements View.OnClickListener {
    int mark;

    MarkBtnListener(int mark) {
      this.mark = mark;
    }

    public void onClick(View v) {
      service.markWord(currentCard, mark, calcTime());
      //massSetVisibility(ALL_ELEMS, View.VISIBLE);
      showNextWordBtn();
      state = WordFormState.View;
    }
  }

  /**
   * @return how much time word processing took in seconds
   */
  private double calcTime() {
    long timeNow = System.currentTimeMillis();
    long viewTime = timeNow - wordMapTime;
    return viewTime / 1000.0;
  }

  private Random rand = new Random();

  private void nextWord() {
    if (service.hasNextCard() || service.preloadWords()) {
      WordWithCard wc = service.getNextWord();
      if (wc == null) {
        Log.w("Kotonoha", "Returned nothing, closing an activity");
        finish();
      }
      currentWord = wc.word;
      currentCard = wc.card;
      mapCurrentWord();
      //massSetVisibility(MARK_BTNS, View.VISIBLE);
      int viewId = rand.nextBoolean() ? R.id.Reading : R.id.Writing;
      findViewById(viewId).setVisibility(View.VISIBLE);
      state = WordFormState.Init;
    }
  }

  private DataService service;

  private ServiceConnection connection = new ServiceConnection() {
    public void onServiceDisconnected(ComponentName name) {
      service = null;
      //should not get here
    }

    public void onServiceConnected(ComponentName name, IBinder binder) {
      service = ((DataService.DataServiceBinder) binder).getService();
    }
  };

  private void connectToService() {
    bindService(new Intent(this, DataService.class), connection, BIND_AUTO_CREATE);
  }
}
