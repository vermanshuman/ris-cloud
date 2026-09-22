/**
 *
 */
package it.nexera.ris.common.security.crypto;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 encrypter
 */
public class MD5 {
    /**
     * The jce MD5 message digest generator.
     */
    private static MessageDigest md5;

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length >= 1) {
            String strEncodongValue = encodeString(args[0], null);
            System.out.println("Source Value : " + args[0]);
            System.out.println("Result Value : " + strEncodongValue);
        }
    }

    /**
     * Retrieves a hexidecimal character sequence representing the MD5 digest of
     * the specified character sequence, using the specified encoding to first
     * convert the character sequence into a byte sequence. If the specified
     * encoding is null, then ISO-8859-1 is assumed
     *
     * @param string   the string to encode.
     * @param encoding the encoding used to convert the string into the byte sequence
     *                 to submit for MD5 digest
     * @return a hexidecimal character sequence representing the MD5 digest of
     * the specified string
     * @throws HsqlUnsupportedOperationException if an MD5 digest algorithm is not available through the
     *                                           java.security.MessageDigest spi or the requested encoding is
     *                                           not available
     */
    public static final String encodeString(String string, String encoding)
            throws RuntimeException {
        return StringConverter.byteToHex(digestString(string, encoding));
    }

    /**
     * Retrieves a byte sequence representing the MD5 digest of the specified
     * character sequence, using the specified encoding to first convert the
     * character sequence into a byte sequence. If the specified encoding is
     * null, then ISO-8859-1 is assumed.
     *
     * @param string   the string to digest.
     * @param encoding the character encoding.
     * @return the digest as an array of 16 bytes.
     * @throws HsqlUnsupportedOperationException if an MD5 digest algorithm is not available through the
     *                                           java.security.MessageDigest spi or the requested encoding is
     *                                           not available
     */
    public static byte[] digestString(String string, String encoding)
            throws RuntimeException {

        byte[] data;

        if (encoding == null) {
            encoding = "ISO-8859-1";
        }

        try {
            data = string.getBytes(encoding);
        } catch (UnsupportedEncodingException x) {
            throw new RuntimeException(x.toString());
        }

        return digestBytes(data);
    }

    /**
     * Retrieves a byte sequence representing the MD5 digest of the specified
     * byte sequence.
     *
     * @param data the data to digest.
     * @return the MD5 digest as an array of 16 bytes.
     * @throws HsqlUnsupportedOperationException if an MD5 digest algorithm is not available through the
     *                                           java.security.MessageDigest spi
     */
    public static final byte[] digestBytes(byte[] data) throws RuntimeException {

        synchronized (MD5.class) {
            if (md5 == null) {
                try {
                    md5 = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e.toString());
                }
            }

            return md5.digest(data);
        }
    }

    public static byte[] createChecksum(String filename) throws Exception {
        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        try (InputStream fis = new FileInputStream(filename)) {
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            }
            while (numRead != -1);
        }

        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}
