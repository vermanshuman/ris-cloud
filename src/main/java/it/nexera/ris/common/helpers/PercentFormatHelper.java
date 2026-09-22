package it.nexera.ris.common.helpers;

import java.util.IllegalFormatException;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;

/**
 * Formats string like "User: %user%, password: %password%".
 *
 * @author Andrey Kunitsin
 * @see #PercentFormatHelper.format(String, Map)
 */
public class PercentFormatHelper {

    private static final char PERCENT_SIGN = '%';

    /**
     * Returns a formatted string using the specified format and map of key-value parameters.
     * For example, "User: %user%, password: %password%" with map {"user": "testuser",
     * "password": "k8zuu2_H"} will be formatted as "User: testuser, password: k8zuu2_H".
     * Unpaired "%" characters and unknown parameters in the format string are not permitted.
     * "%%" in format string will give "%" in result.
     *
     * @param formatString Format to be parsed.
     * @param params       Map of key-value parameters.
     * @return A foramtted string.
     * @throws IllegalFormatException if the format string contains illegal syntax.
     */
    public static String format(String formatString, Map<String, String> params) {
        int l = formatString.length();
        if (l == 0) {
            return "";
        }
        StringBuilder ret = new StringBuilder();
        int seek = 0;
        while (true) {
            int start = formatString.indexOf(PERCENT_SIGN, seek);
            if (start == -1) {
                // that's all
                ret.append(formatString.substring(seek));
                break;
            }
            int finish = formatString.indexOf(PERCENT_SIGN, start + 1);
            if (finish == -1) {
                throw new UnknownFormatConversionException("Unpaired \'"
                        + PERCENT_SIGN + "\' at the position " + start);
            }
            String key = formatString.substring(start + 1, finish);
            if (key.length() == 0) {
                // it's "%%" combination
                ret.append(formatString.substring(seek, finish));
            } else {
                if (!params.containsKey(key)) {
                    throw new MissingFormatArgumentException("Key \"" + key
                            + "\" at position " + (start + 1) + " not found");
                }
                ret.append(formatString.substring(seek, start));
                ret.append(params.get(key));
            }
            if (finish == l - 1) {
                break;
            } else {
                seek = finish + 1;
            }
        }
        return ret.toString();
    }
}
