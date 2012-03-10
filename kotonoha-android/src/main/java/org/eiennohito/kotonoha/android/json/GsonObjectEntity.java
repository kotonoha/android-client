package org.eiennohito.kotonoha.android.json;

import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author eiennohito
 * @since 08.03.12
 */
public class GsonObjectEntity extends EntityTemplate {
  public GsonObjectEntity(Object obj) {
    super(new MyProducer(obj));
    setContentType("application/json");
  }

  private static class MyProducer implements ContentProducer {
    private final Object obj;

    public MyProducer(Object obj) {
      this.obj = obj;
    }

    public void writeTo(OutputStream out) throws IOException {
      GsonInstance.instance().toJson(obj, new PrintStream(out, false, "UTF-8"));
    }
  }
}
