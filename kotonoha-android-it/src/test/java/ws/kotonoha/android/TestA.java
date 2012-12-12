package ws.kotonoha.android;

import android.test.AndroidTestCase;
import android.util.Base64;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * @author eiennohito
 * @since 11.12.12
 */

public class TestA extends AndroidTestCase {

  private static final String testURI = "http://kotonoha.ws/intent/auth?data=Sf3GiPW3bS55GwOEkNRxxG00ZekFCAwd9ntDsA_PPlTt2w_fSuZjheG_Y6Lg6J074znqIBHmBfzCkGZRfqWhKJcieQlmp4U2nbUQKDGetvNGFPxFQiVT7YIiI1SjfJstcmhJTdnQYEmnvJ1Sc-02fajyRwXlrEL5Qb4Foe0AUX59-aJR";
  private static final String key = "82e52a2bc0d78d206388788e897f7383";

  public void testA() throws Exception {
    int i = testURI.indexOf("data=");
    String s = testURI.substring(i + 5);
    byte[] keyBytes = Hex.decodeHex(key.toCharArray());
    byte[] in = Base64.decode(s, Base64.URL_SAFE);
    PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CFBBlockCipher(new AESFastEngine(), 8));
    KeyParameter kp = new KeyParameter(keyBytes);
    aes.init(false, kp);
    int outl = aes.getOutputSize(in.length);
    byte[] out = new byte[outl];
    int r1 = aes.processBytes(in, 0, in.length, out, 0);
    r1 += aes.doFinal(out, r1);
    String outs = new String(out, 0, r1, "UTF-8");
    System.out.println(outs);
  }
}
