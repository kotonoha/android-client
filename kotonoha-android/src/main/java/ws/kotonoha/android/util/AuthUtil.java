package ws.kotonoha.android.util;

import android.util.Base64;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

/**
 * @author eiennohito
 * @since 11.12.12
 */
public class AuthUtil {

  static private final byte[] key = Hex.decode(ApiCodes.privateKey);

  public static String compileAuth(String as) {
    try {
      PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CFBBlockCipher(new AESFastEngine(), 8));
      KeyParameter kp = new KeyParameter(key);
      aes.init(false, kp);
      byte[] in = Base64.decode(as, Base64.URL_SAFE);
      int outl = aes.getOutputSize(in.length);
      byte[] out = new byte[outl];
      int r1 = aes.processBytes(in, 0, in.length, out, 0);
      r1 += aes.doFinal(out, r1);
      String outs = new String(out, 0, r1, "UTF-8");
      return outs;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
