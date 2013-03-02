package ws.kotonoha.android.util;

import ws.kotonoha.server.model.learning.ReviewCard;

import java.util.Comparator;

/**
 * @author eiennohito
 * @since 02.03.13
 */
public class ReviewCardComparator implements Comparator<ReviewCard> {
  @Override
  public int compare(ReviewCard lhs, ReviewCard rhs) {
    long l = lhs.getSeq();
    long r = rhs.getSeq();
    if (l == r) return 0;
    if (l > r)
      return 1;
    else
      return -1;
  }
}
