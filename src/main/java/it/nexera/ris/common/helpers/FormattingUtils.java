package it.nexera.ris.common.helpers;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static it.nexera.ris.common.helpers.ValidationHelper.isNullOrEmpty;

public class FormattingUtils {

    private static final Map<String, String> italianLocalSymbols = new HashMap<>();

    static {

        italianLocalSymbols.put("&Agrave;", "&#192;");
        italianLocalSymbols.put("&Aacute;", "&#193;");
        italianLocalSymbols.put("&Acirc;", "&#194;");
        italianLocalSymbols.put("&Atilde;", "&#195;");
        italianLocalSymbols.put("&Auml;", "&#196;");
        italianLocalSymbols.put("&Aring;", "&#197;");
        italianLocalSymbols.put("&AElig;", "&#198;");
        italianLocalSymbols.put("&Ccedil;", "&#199;");
        italianLocalSymbols.put("&Egrave;", "&#200;");
        italianLocalSymbols.put("&Eacute;", "&#201;");
        italianLocalSymbols.put("&Ecirc;", "&#202;");
        italianLocalSymbols.put("&Euml;", "&#203;");
        italianLocalSymbols.put("&Igrave;", "&#204;");
        italianLocalSymbols.put("&Iacute;", "&#205;");
        italianLocalSymbols.put("&Icirc;", "&#206;");
        italianLocalSymbols.put("&Iuml;", "&#207;");
        italianLocalSymbols.put("&ETH;", "&#208;");
        italianLocalSymbols.put("&Ntilde;", "&#209;");
        italianLocalSymbols.put("&Ograve;", "&#210;");
        italianLocalSymbols.put("&Oacute;", "&#211;");
        italianLocalSymbols.put("&Ocirc;", "&#212;");
        italianLocalSymbols.put("&Otilde;", "&#213;");
        italianLocalSymbols.put("&Ouml;", "&#214;");
        italianLocalSymbols.put("&Oslash;", "&#216;");
        italianLocalSymbols.put("&OElig;", "&#338;");
        italianLocalSymbols.put("&Scaron;", "&#352;");
        italianLocalSymbols.put("&Ugrave;", "&#217;");
        italianLocalSymbols.put("&Uacute;", "&#218;");
        italianLocalSymbols.put("&Ucirc;", "&#219;");
        italianLocalSymbols.put("&Uuml;", "&#220;");
        italianLocalSymbols.put("&Yacute;", "&#221;");
        italianLocalSymbols.put("&szlig;", "&#223;");
        italianLocalSymbols.put("&agrave;", "&#224;");
        italianLocalSymbols.put("&aacute;", "&#225;");
        italianLocalSymbols.put("&acirc;", "&#226;");
        italianLocalSymbols.put("&atilde;", "&#227;");
        italianLocalSymbols.put("&auml;", "&#228;");
        italianLocalSymbols.put("&aring;", "&#229;");
        italianLocalSymbols.put("&aelig;", "&#230;");
        italianLocalSymbols.put("&ccedil;", "&#231;");
        italianLocalSymbols.put("&egrave;", "&#232;");
        italianLocalSymbols.put("&eacute;", "&#233;");
        italianLocalSymbols.put("&ecirc;", "&#234;");
        italianLocalSymbols.put("&euml;", "&#235;");
        italianLocalSymbols.put("&igrave;", "&#236;");
        italianLocalSymbols.put("&iacute;", "&#237;");
        italianLocalSymbols.put("&icirc;", "&#238;");
        italianLocalSymbols.put("&iuml;", "&#239;");
        italianLocalSymbols.put("&eth;", "&#240;");
        italianLocalSymbols.put("&ntilde;", "&#241;");
        italianLocalSymbols.put("&ograve;", "&#242;");
        italianLocalSymbols.put("&oacute;", "&#243;");
        italianLocalSymbols.put("&ocirc;", "&#244;");
        italianLocalSymbols.put("&otilde;", "&#245;");
        italianLocalSymbols.put("&ouml;", "&#246;");
        italianLocalSymbols.put("&oslash;", "&#248;");
        italianLocalSymbols.put("&oelig;", "&#339;");
        italianLocalSymbols.put("&scaron;", "&#353;");
        italianLocalSymbols.put("&ugrave;", "&#249;");
        italianLocalSymbols.put("&uacute;", "&#250;");
        italianLocalSymbols.put("&ucirc;", "&#251;");
        italianLocalSymbols.put("&uuml;", "&#252;");
        italianLocalSymbols.put("&yacute;", "&#253;");
        italianLocalSymbols.put("&yuml;", "&#255;");
        italianLocalSymbols.put("&thorn;", "&#254;");


    }

    public static String htmlToStr(String html) {
        String text = html.replaceAll("\\<.*?>", "").replace("&nbsp;", " ");
        if (isNullOrEmpty(text)) {
            int begIdx = html.indexOf("src=") + "src=".length() + 1;
            int endIdx = html.indexOf("\"", begIdx);
            text = html.substring(begIdx, endIdx);
        }
        return text;
    }

    public static Long strToLong(String str) {
        return isNullOrEmpty(str) ? null : Long.parseLong(str);
    }

    public static Double strToDouble(String str) {
        return isNullOrEmpty(str) ? null : Double.parseDouble(str);
    }

    public static String intToStr(Integer val) {
        return val == null ? "" : val.toString();
    }

    public static Date strToDate(String utcDateString) {
        //remove UTC
        StringBuilder gmtDateString = new StringBuilder(
                utcDateString.substring(0, utcDateString.indexOf("UTC")));
        gmtDateString.append("GMT");

        return DateTimeHelper.fromString(gmtDateString.toString(),
                DateTimeHelper.getDatePatternWithTimezone(), Locale.ITALY);
    }

    public static String prepareSearchStr(String str) {
        return str.trim().replace("%", "\\%");
    }

    public static String htmlToStringWithTags(String html) {
        StringBuilder sb = new StringBuilder();
        sb.append(html);
        for (String key : italianLocalSymbols.keySet()) {
            if (sb.lastIndexOf(key) != -1) {
                replaceString(sb, key, italianLocalSymbols.get(key));
            }

        }
        return sb.toString();
    }

    public static void replaceString(StringBuilder sb, String toReplace,
                                     String replacement) {
        int index = -1;
        while ((index = sb.lastIndexOf(toReplace)) != -1) {
            sb.replace(index, index + toReplace.length(), replacement);
        }
    }
}
