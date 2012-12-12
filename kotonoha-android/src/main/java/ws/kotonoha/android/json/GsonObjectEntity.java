package ws.kotonoha.android.json;

import org.apache.http.entity.ByteArrayEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * @author eiennohito
 * @since 08.03.12
 */
public class GsonObjectEntity extends ByteArrayEntity {
  public GsonObjectEntity(Object obj) {
    super(buildArray(obj));
    setContentType("application/json");
    setContentEncoding("utf-8");
  }

  public byte[] array() {
    return this.content;
  }

  private static byte[] buildArray(Object obj) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
      OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
      GsonInstance.instance().toJson(obj, writer);
      writer.flush();
      return os.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
