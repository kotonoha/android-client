package org.eiennohito.kotonoha.android.util;

/**
* @author eiennohito
* @since 08.03.12
*/
public interface ValueCallback<T> {
  void process(final T val);
}
