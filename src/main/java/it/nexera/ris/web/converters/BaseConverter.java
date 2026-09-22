package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

public class BaseConverter {
    public static String convertToMoneyString(Object value) {
        DecimalFormat dec = new DecimalFormat("\u0024 ###,###,##0.00",
                DecimalFormatSymbols.getInstance(Locale.US));

        return String.valueOf(
                dec.format(Double.parseDouble(String.valueOf(value)))).replace(
                " ", "\u00a0");
    }

    public static String convertToDoubleString(Object value) {
        DecimalFormat dec = new DecimalFormat("###.##");

        return String.valueOf(dec.format(Double.parseDouble(String
                .valueOf(value))));
    }

    public static String convertToDecimalString(Object value) {
        DecimalFormat dec = new DecimalFormat("###.#");

        return String.valueOf(dec.format(Double.parseDouble(String
                .valueOf(value))));
    }

    public static String convertToDateString(Object value) {
        return DateTimeHelper.toString((Date) value);
    }

    public static String convertToDateWithMinutesString(Object value) {
        return DateTimeHelper.toStringWithMinutes((Date) value);
    }

    public static String convertToDateWithTimezone(Object value) {
        return DateTimeHelper.toFormatedString((Date) value,
                DateTimeHelper.getDatePatternWithTimezone(), Locale.US);
    }

    public static String convertToPercentageString(Object value) {
        if (value instanceof Double) {
            return convertToDoubleString(value) + " \u0025";
        }
        return value.toString() + " \u0025";
    }

    public static String convertToNumberString(Object value) {
        return value.toString();
    }

    public static String convertToBooleanString(Object value) {
        if ((Boolean) value) {
            return ResourcesHelper.getString("yes");
        }

        return ResourcesHelper.getString("no");
    }

    public static String convertDateToFileName(Object value) {
        return DateTimeHelper.toFileNameString((Date) value);
    }

    public static String convertToRecordLocatorString(Object value) {
        if (value == null) {
            return null;
        }
        DecimalFormat dec = new DecimalFormat("00000");

        return String
                .valueOf(dec.format(Integer.parseInt(String.valueOf(value))));
    }

    public static String convertToHourString(Object value) {
        DecimalFormat dec = new DecimalFormat("00");

        return String
                .valueOf(dec.format(Integer.parseInt(String.valueOf(value))));
    }

    public static String convertToTimeString(Object value) {
        return DateTimeHelper.toFormatedString((Date) value,
                DateTimeHelper.getTimePattern());
    }
}
