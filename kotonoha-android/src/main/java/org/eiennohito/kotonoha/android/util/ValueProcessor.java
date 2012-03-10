package org.eiennohito.kotonoha.android.util;

/**
* @author eiennohito
* @since 08.03.12
*/
public interface ValueProcessor<T, F> {
  F process(final T val) throws Exception;
}
