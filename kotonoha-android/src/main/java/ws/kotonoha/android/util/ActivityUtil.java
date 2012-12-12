package ws.kotonoha.android.util;

import android.app.Activity;

/**
 * @author eiennohito
 * @since 01.04.12
 */
public class ActivityUtil {
  public static void setAllClickable(final int[] btns, final Activity act, final boolean clickable) {
    Runnable runnable = new Runnable() {
      public void run() {
        for (int val : btns) {
          act.findViewById(val).setClickable(clickable);
        }
      }
    };
    act.runOnUiThread(runnable);
  }
}
