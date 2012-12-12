package ws.kotonoha.android.util;

/**
 * @author eiennohito
 * @since 12.03.12
 */
public interface ErrorCallback<Req> {
  void onError(Req req);
}
