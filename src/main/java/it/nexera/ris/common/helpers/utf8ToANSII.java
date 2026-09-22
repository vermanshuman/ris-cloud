package it.nexera.ris.common.helpers;

import java.io.UnsupportedEncodingException;

/**
 * Used to transform UTF-8 strings to ANSI encoding
 */
public class utf8ToANSII extends BaseHelper {

    public static long ConvertToLong(String str) {
        long item = 0;
        try {
            item = Long.parseLong(str);
        } catch (NumberFormatException e) {
            log.warn("utf8ToANSII.ConvertToLong : " + e);
            try {
                byte[] arr = str.getBytes("ASCII");
                String strAux = new String(arr);
                char[] strAux2 = strAux.toCharArray();
                item = Long.parseLong(Character.toString(strAux2[3]));
            } catch (NumberFormatException e1) {
                log.warn("utf8ToANSII.ConvertToLong : " + e1);
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                log.warn("utf8ToANSII.ConvertToLong : " + e1);
                e1.printStackTrace();
            }
        }

        return item;
    }

    public static int ConvertToInteger(String str) {
        int item = 0;
        try {
            item = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            log.warn("utf8ToANSII.ConvertToInteger : " + e);
            try {
                byte[] arr = str.getBytes("ASCII");
                String strAux = new String(arr);
                char[] strAux2 = strAux.toCharArray();
                item = Integer.parseInt(Character.toString(strAux2[3]));
            } catch (NumberFormatException e1) {
                log.warn("utf8ToANSII.ConvertToInteger : " + e1);
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                log.warn("utf8ToANSII.ConvertToInteger : " + e1);
                e1.printStackTrace();
            }
        }

        return item;
    }
}
