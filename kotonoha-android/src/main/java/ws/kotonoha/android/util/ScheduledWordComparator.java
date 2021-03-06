package ws.kotonoha.android.util;

import ws.kotonoha.server.model.learning.WordCard;

import java.util.Comparator;


/**
 * @author eiennohito
 * @since 26.09.2010
 */
public class ScheduledWordComparator implements Comparator<WordCard> {
  public int compare(WordCard w1, WordCard w2) {
    if (w1.getLearning() == null && w2.getLearning() == null) {
      int i = w1.getCreatedOn().compareTo(w2.getCreatedOn());
      return i == 0 ? w1.getId().compareTo(w2.getId()) : i;
    }
    if (w1.getLearning() == null) {
      return 1;
    }
    if (w2.getLearning() == null) {
      return -1;
    }
    return w1.getLearning().getIntervalEnd().compareTo(w2.getLearning().getIntervalEnd());
  }
}
