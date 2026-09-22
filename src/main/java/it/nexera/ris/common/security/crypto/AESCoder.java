package it.nexera.ris.common.security.crypto;

import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESCoder extends BaseHelper {
    // private static String    secret = "j1sajnm50jvi98h4"; // secret key length must be 16

    private static SecretKey key;

    private static Cipher cipher;

    private static Base64 coder = new Base64();

    static {
        try {
            key = new SecretKeySpec(ResourcesHelper.getString("statisticSecret").getBytes(), "AES");
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static synchronized String encrypt(String plainText)
            throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherText = cipher.doFinal(plainText.getBytes());

        return new String(coder.encode(cipherText));
    }

    public static synchronized String encrypt(byte[] bytes) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherText = cipher.doFinal(bytes);

        return new String(coder.encode(cipherText));
    }

    public static synchronized String decrypt(String codedText)
            throws Exception {
        byte[] encypted = coder.decode(codedText.getBytes());

        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decrypted = cipher.doFinal(encypted);

        return new String(decrypted);
    }

}
