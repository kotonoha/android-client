package ws.kotonoha.android.json;

import org.apache.http.HttpEntity;
import ws.kotonoha.android.util.ValueProcessor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author eiennohito
 * @since 08.03.12
 */
public class GsonObjectParser<T> implements ValueProcessor<HttpEntity, T> {
  private Class<T> clazz;

  public GsonObjectParser(Class<T> clazz) {
    this.clazz = clazz;
  }

  public T process(HttpEntity val) throws Exception {
    InputStream content = val.getContent();
    InputStreamReader reader = new InputStreamReader(content, Charset.forName("UTF-8"));
    return GsonInstance.instance().fromJson(reader, clazz);
  }
}
