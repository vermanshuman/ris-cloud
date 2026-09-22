package it.nexera.ris.web.functions;

public final class StringFunctions {
    private StringFunctions() {

    }

    public static String concat(String str1, String str2) {
        return str1.concat(str2);
    }

    public static String upper(String str) {
        return str.toUpperCase();
    }

    public static String lower(String str) {
        return str.toLowerCase();
    }

    public static String format(String str1, String str2) {
        return String.format(str1, str2);
    }

    public static String format(String str1, String str2, String str3) {
        return String.format(str1, str2, str3);
    }
}
