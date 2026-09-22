package it.nexera.ris.common.helpers;

import java.io.UnsupportedEncodingException;

public class EncodingHelper extends BaseHelper {

    public static long ConvertToLong(String str) {
        long item = 0;
        try {
            item = Long.parseLong(str);
        } catch (NumberFormatException e) {
            log.warn("EncodingHelper.ConvertToLong : " + e);
            try {
                byte[] arr = str.getBytes("ASCII");
                String strAux = new String(arr);
                char[] strAux2 = strAux.toCharArray();
                item = Long.parseLong(Character.toString(strAux2[1]));
            } catch (NumberFormatException e1) {
                log.warn("EncodingHelper.ConvertToLong : " + e1);
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                log.warn("EncodingHelper.ConvertToLong : " + e1);
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
            log.warn("EncodingHelper.ConvertToInteger : " + e);
            try {
                byte[] arr = str.getBytes("ASCII");
                String strAux = new String(arr);
                char[] strAux2 = strAux.toCharArray();
                item = Integer.parseInt(Character.toString(strAux2[1]));
            } catch (NumberFormatException e1) {
                log.warn("EncodingHelper.ConvertToInteger : " + e1);
                e1.printStackTrace();
            } catch (UnsupportedEncodingException e1) {
                log.warn("EncodingHelper.ConvertToInteger : " + e1);
                e1.printStackTrace();
            }
        }

        return item;
    }

    public static String ConvertToUTF8String(String str) {
        String UTF8Str = null;
        try {
            UTF8Str = new String(str.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("EncodingHelper.ConvertToUTF8String : " + e);
            LogHelper.log(log, e);
        }

        return UTF8Str;
    }
}
