package it.nexera.ris.common.helpers;

public class NumberHelper extends BaseHelper {

    public static Long longValueFromString(String stringValue) {
        Long id = null;
        if (stringValue != null && !stringValue.isEmpty()) {
            try {
                id = Long.parseLong(stringValue);
            } catch (NumberFormatException e) {
                LogHelper.log(log, e);
            }
            return id;
        }
        return null;
    }
}