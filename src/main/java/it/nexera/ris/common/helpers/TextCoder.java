package it.nexera.ris.common.helpers;

/**
 * TextCoder. A simple text encrypter/decrypter
 */
public class TextCoder {
    /**
     * @param str original string
     * @return encoded string
     */
    public static String Encode(String str) {
        char[] array = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (Character ch : array) {
            if (ch == '\t' || ch == '\n' || ch == ' ' || ch == '\r') {
                sb.append(ch);
            } else {
                int i = Integer.valueOf(ch);
                i += 3;
                sb.append((char) i);
            }
        }
        return sb.toString();
    }

    /**
     * @param str encoded string
     * @return original string
     */
    public static String Decode(String str) {
        char[] array = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (Character ch : array) {
            if (ch == '\t' || ch == '\n' || ch == ' ' || ch == '\r') {
                sb.append(ch);
            } else {
                int i = Integer.valueOf(ch);
                i -= 3;
                sb.append((char) i);
            }
        }
        return sb.toString();
    }
}
