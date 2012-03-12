package org.eiennohito.kotonoha.android.util;

/**
 * @author eiennohito
 * @since 12.03.12
 */
public interface SuccessCallback<Req, Resp> {
  void onOk(Req req, Resp resp);
}
