package it.nexera.ris.common.helpers;

public class XSSCleaner {
    public static synchronized String cleanXSS(String value) {
        if (value != null) {
            value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
            value = value.replaceAll("'", "&#39;");
            value = value.replaceAll("eval\\((.*)\\)", "");
            value = value.replaceAll(
                    "[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
            value = value.replaceAll("script", "");
        }
        return value;
    }

    public static synchronized String cleanJS(String value) {
        if (value != null) {
            value = value.replaceAll("eval\\((.*)\\)", "");
            value = value.replaceAll(
                    "[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
            value = value.replaceAll("script", "");
        }
        return value;
    }
}
