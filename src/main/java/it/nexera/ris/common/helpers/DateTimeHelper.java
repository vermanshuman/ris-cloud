package it.nexera.ris.common.helpers;

import it.nexera.ris.web.converters.BaseConverter;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeHelper extends BaseHelper {

    public static final String XML_DATE_PATTERN = "yyyyMMddHHmmssZZZZ";

    public static final String XML_SHORT_DATE_PATTERN = "yyyyMMdd";

    private static String datePattern = "dd/MM/yyyy";

    private static String timePattern = "HH:mm";

    private static String timePatternWithSeconds = "HH:mm:ss";

    private static String mySQLDatePattern = "yyyy-MM-dd";

    private static String mySQLDateTimePattern = "yyyy-MM-dd HH:mm:ss";

    private static String pathDatePattern = "dd-MM-yyyy";

    private static String pathDateTimePattern = "dd-MM-yyyy HH:mm";

    private static String dotsDatePattern = "dd.MM.yyyy";

    private static String datePatternWithMinutes = "dd/MM/yyyy HH:mm";

    private static String datePatternWithSeconds = "dd/MM/yyyy HH:mm:ss";

    private static String datePatternWithMinutesAndSuffix = "dd/MM/yyyy HH:mm a";

    private static String datePatternWithTimezone = "MMM dd yyyy HH:mm Z";

    private static String datePatternForSessionId = "yyyy_MM_dd_HH_mm_ss_SSS";

    private static String datePatternForFileEntity = "_ddMMMyy_HHmmss_SSSSSSSSS_";

    private static String datePatternFoHL7 = "yyyyMMddHHmmss";

    public static TimeZone defaultTimeZone;

    static {
        defaultTimeZone = TimeZone.getTimeZone("Europe/Rome").inDaylightTime(new Date())
                ? TimeZone.getTimeZone("GMT+2") : TimeZone.getTimeZone("Europe/Rome");
    }

    private static DateFormat fromCalendar = new SimpleDateFormat(
            "EEE dd MMM HH:mm:ss z yyyy",
            Locale.ITALY);

    private static DateFormat dfm = new SimpleDateFormat(
            DateTimeHelper
                    .getDatePattern());

    private static DateFormat dotsFormatter = new SimpleDateFormat(
            dotsDatePattern,
            Locale.ITALY);

    public static Date getNow() {
        Date date = new Date();

        return date;
    }

    public static Date getDayStart(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    public static Date getDayEnd(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);

        return c.getTime();
    }

    public static Calendar getDayStart(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;
    }

    public static Calendar getDayEnd(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);

        return c;
    }

    public static Date getDate(Date time) {
        long millisInDay = 60 * 60 * 24 * 1000;
        long currentTime = time.getTime();
        long dateOnly = (currentTime / millisInDay) * millisInDay;
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date clearDate = new Date(dateOnly);
        return clearDate;
    }

    @SuppressWarnings("deprecation")
    public static boolean DateLessThenMaxDate(Date date, Date maxDate) {
        Date today = maxDate;
        if (date.getYear() < today.getYear()) {
            return true;
        } else if (date.getYear() == today.getYear()
                && date.getMonth() < today.getMonth()) {
            return true;
        } else if (date.getYear() == today.getYear()
                && date.getMonth() == today.getMonth()
                && date.getDay() <= today.getDay()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean between(Date compareDate, Date startDate, Date endDate) {
        if ((compareDate.after(startDate) || compareDate.getTime() == startDate
                .getTime())
                && (compareDate.before(endDate) || compareDate.getTime() == endDate
                .getTime())) {
            return true;
        }

        return false;
    }

    public static boolean between(Calendar compareDate, Calendar startDate,
                                  Calendar endDate) {
        if ((compareDate.after(startDate) || compareDate.getTime().equals(startDate
                .getTime()))
                && (compareDate.before(endDate) || compareDate.getTime().equals(endDate
                .getTime()))) {
            return true;
        }

        return false;
    }

    public static Date fromString(String str, String format) {
        return fromString(str, format, null);
    }

    public static Date fromString(String str, String format, Locale locale) {
        if (!ValidationHelper.isNullOrEmpty(str)) {
            DateFormat dateFormat = null;
            if (locale != null) {
                dateFormat = new SimpleDateFormat(format, locale);
            } else {
                dateFormat = new SimpleDateFormat(format);
            }
            try {
                return dateFormat.parse(str);
            } catch (ParseException e2) {

            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public static Date fromAMPMString(String str) {
        return new Date(str);
    }

    public static synchronized Date fromString(String str) {
        if (!ValidationHelper.isNullOrEmpty(str)) {
            try {
                return dfm.parse(str);
            } catch (ParseException e2) {
                //                log.warn("DateTime.FromString(dfm1.parse(str)) : " + e2);
                try {
                    return dotsFormatter.parse(str);
                } catch (ParseException e) {
                    //log.warn("DateTime.FromString(dfm1.parse(str)) : " + e2);
                }
            }
            try {
                return fromCalendar.parse(str);
            } catch (ParseException e1) {
                //log.warn("DateTime.FromString(fromCalendar.parse(str)) : "+ e1);
                return null;
            }
        } else {
            return null;
        }
    }

    public static String toStringTime(Date value) {
        return toFormatedString(value, getTimePattern());
    }

    public static String toFileNameString(Date value) {
        return toFormatedString(value, getPathDateTimePattern());
    }

    public static String toString(Date value) {
        return toFormatedString(value, getDatePattern());
    }

    public static String toStringDateWithDots(Date value) {
        return toFormatedString(value, getDotsDatePattern());
    }

    public static String toStringWithMinutes(Date value) {
        return toFormatedString(value, getDatePatternWithMinutes());
    }

    public static String ToStringWithSeconds(Date value) {
        return toFormatedString(value, getDatePatternWithSeconds());
    }

    public static String ToPathString(Date value) {
        return toFormatedString(value, getPathDatePattern());
    }

    public static String toFormatedString(Date value, String format) {
        return toFormatedString(value, format, null);
    }

    public static String ToStringTimeWithSeconds(Date value) {
        return toFormatedString(value, getTimePatternWithSeconds());
    }

    public static String ToDatePatternWithMinutesAndSuffix(Date value) {
        return toFormatedString(value, getDatePatternWithMinutesAndSuffix());
    }

    public static String toSessionTime(Date value) {
        return toFormatedString(value, getDatePatternForSessionId());
    }

    public static String toFileEntityDate(Date value) {
        return toFormatedString(value, getDatePatternForFileEntity());
    }

    public static String toFormatedString(Date value, String format,
                                          Locale local) {
        SimpleDateFormat df = null;
        String returnValue = "";

        if (value == null) {
            log.error("aDate is null!");
        } else {
            if (local == null) {
                df = new SimpleDateFormat(format);
            } else {
                df = new SimpleDateFormat(format, local);
            }
            returnValue = df.format(value);
        }

        return returnValue;
    }

    public static String ToMySqlString(Date value) {
        return toFormatedString(value, getMySQLDatePattern());
    }

    public static String ToMySqlStringWithSeconds(Date value) {
        return toFormatedString(value, getMySQLDateTimePattern());
    }

    public static void setDatePattern(String datePattern) {
        DateTimeHelper.datePattern = datePattern;
    }

    public static String getDatePattern() {
        return datePattern;
    }

    public static void setMySQLDatePattern(String mySQLDatePattern) {
        DateTimeHelper.mySQLDatePattern = mySQLDatePattern;
    }

    public static String getMySQLDatePattern() {
        return mySQLDatePattern;
    }

    public static void setPathDatePattern(String pathDatePattern) {
        DateTimeHelper.pathDatePattern = pathDatePattern;
    }

    public static String getPathDatePattern() {
        return pathDatePattern;
    }

    public static void setDatePatternWithMinutes(String datePatternWithMinutes) {
        DateTimeHelper.datePatternWithMinutes = datePatternWithMinutes;
    }

    public static String getDatePatternWithMinutes() {
        return datePatternWithMinutes;
    }

    public static void setDatePatternWithSeconds(String datePatternWithSeconds) {
        DateTimeHelper.datePatternWithSeconds = datePatternWithSeconds;
    }

    public static String getDatePatternWithSeconds() {
        return datePatternWithSeconds;
    }

    public static void setPathDateTimePattern(String pathDateTimePattern) {
        DateTimeHelper.pathDateTimePattern = pathDateTimePattern;
    }

    public static String getPathDateTimePattern() {
        return pathDateTimePattern;
    }

    public static void setMySQLDateTimePattern(String mySQLDateTimePattern) {
        DateTimeHelper.mySQLDateTimePattern = mySQLDateTimePattern;
    }

    public static String getMySQLDateTimePattern() {
        return mySQLDateTimePattern;
    }

    public static String getTimePattern() {
        return timePattern;
    }

    public static void setTimePattern(String timePattern) {
        DateTimeHelper.timePattern = timePattern;
    }

    public static String getTimeString(Calendar c) {
        return String.format("%s:%s",
                BaseConverter.convertToHourString(c.get(Calendar.HOUR_OF_DAY)),
                BaseConverter.convertToHourString(c.get(Calendar.MINUTE)));
    }

    public static String getTimePatternWithSeconds() {
        return timePatternWithSeconds;
    }

    public static void setTimePatternWithSeconds(String timePatternWithSeconds) {
        DateTimeHelper.timePatternWithSeconds = timePatternWithSeconds;
    }

    public static String getDotsDatePattern() {
        return dotsDatePattern;
    }

    public static void setDotsDatePattern(String dotsDatePattern) {
        DateTimeHelper.dotsDatePattern = dotsDatePattern;
    }

    public static String getDatePatternWithMinutesAndSuffix() {
        return datePatternWithMinutesAndSuffix;
    }

    public static void setDatePatternWithMinutesAndSuffix(
            String datePatternWithMinutesAndSuffix) {
        DateTimeHelper.datePatternWithMinutesAndSuffix = datePatternWithMinutesAndSuffix;
    }

    public static String getDatePatternWithTimezone() {
        return datePatternWithTimezone;
    }

    public static String getDatePatternForSessionId() {
        return datePatternForSessionId;
    }

    public static void setDatePatternForSessionId(String datePatternForSessionId) {
        DateTimeHelper.datePatternForSessionId = datePatternForSessionId;
    }

    public static String getDatePatternForFileEntity() {
        return datePatternForFileEntity;
    }

    public static void setDatePatternForFileEntity(String datePatternForFileEntity) {
        DateTimeHelper.datePatternForFileEntity = datePatternForFileEntity;
    }

    public static String getDatePatternWithTimezone(Locale locale) {
        StringBuilder sb = new StringBuilder();
        sb.append(((SimpleDateFormat) DateFormat.getDateInstance(
                DateFormat.MEDIUM, locale)).toPattern());
        sb.append(" ");
        sb.append(((SimpleDateFormat) DateFormat.getTimeInstance(
                DateFormat.SHORT, locale)).toPattern());
        sb.append(" Z");
        return sb.toString();
    }

    public static void setDatePatternWithTimezone(String datePatternWithTimezone) {
        DateTimeHelper.datePatternWithTimezone = datePatternWithTimezone;
    }

    public static Date convertTimeZones(Date date, TimeZone from, TimeZone to) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        TimeZone fromTimeZone = from;
        TimeZone toTimeZone = to;

        calendar.setTimeZone(fromTimeZone);
        calendar.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);
        if (fromTimeZone.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, calendar.getTimeZone()
                    .getDSTSavings() * -1);
        }

        calendar.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
        if (toTimeZone.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
        }

        calendar.setTimeZone(toTimeZone);

        return calendar.getTime();
    }

    public static String normalizeTimezone(String str) {
        StringBuilder sb = new StringBuilder(str.substring(0, str.length() - 5));
        sb.append("UTC");

        if (Integer.parseInt(str.substring(str.length() - 4, str.length())) != 0) {
            sb.append(str.substring(str.length() - 5, str.length() - 2));
            sb.append(":");
            sb.append(str.substring(str.length() - 2, str.length()));
        }
        return sb.toString();
    }

    public static TimeZone getTimeZone() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        return c.getTimeZone();
    }

    public static int getHour(String time) {
        return Integer.parseInt(time.substring(0, 2));
    }

    public static int getMinute(String time) {
        return Integer.parseInt(time.substring(3, 5));
    }

    public static int getDateDiffInMin(Date date1, Date date2) {
        return (int) ((date1.getTime() - date2.getTime()) / (1000 * 60));
    }

    public static long getDateDiffInDay(Date date1, Date date2) {
        return (date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24);
    }

    public static int getTimeDiff(String time1, String time2) {
        int h1 = getHour(time1);
        int h2 = getHour(time2);

        int m1 = getMinute(time1);
        int m2 = getMinute(time2);

        return ((h2 - h1) * 60 + (m2 - m1));
    }

    public static void setTimeToCalendar(Date date, Calendar calendar) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
    }

    public static void setTimeToCalendar(String time, Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, getHour(time));
        calendar.set(Calendar.MINUTE, getMinute(time));
    }

    public static boolean isTheSameDay(Date dateAssignment, Date currentDate) {
        Date dateAssig = getDayStart(dateAssignment);
        Date dateCurr = getDayStart(currentDate);
        /*This method need for compare two dates without time.
        For example (Fri Mar 20 09:00:00 EET 2015).equals(Fri Mar 20 13:00:00 EET 2015) return false, 
        but (Fri Mar 20 00:00:00 EET 2015).equals(Fri Mar 20 00:00:00 EET 2015) return true */
        return dateAssig.equals(dateCurr);
    }

    public static boolean timeBetweenTwoDate(Date dateFrom, Date dateTo,
                                             String timeFrom, String timeTo, Date dateAssignment) {
        if (isTheSameDay(dateAssignment, dateFrom)) {
            Calendar dateFromTime = Calendar.getInstance();
            Calendar dateToTime = Calendar.getInstance();
            Calendar timeFromTime = Calendar.getInstance();
            Calendar timeToTime = Calendar.getInstance();

            setTimeToCalendar(dateFrom, dateFromTime);
            setTimeToCalendar(dateTo, dateToTime);
            setTimeToCalendar(timeFrom, timeFromTime);
            setTimeToCalendar(timeTo, timeToTime);

            if (between(timeFromTime, dateFromTime, dateToTime)) {
                return true;
            } else if (between(timeToTime, dateFromTime, dateToTime)) {
                return true;
            } else if (between(dateFromTime, timeFromTime, timeToTime)) {
                return true;
            } else if (between(dateToTime, timeFromTime, timeToTime)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean timeLessThanAvailableTime(String startTime,
                                                    String endTime, Long maxTime, Long minTime, Long averageDuration) {
        long availableTime = (maxTime - minTime) * 60;
        long selectedTime = (getHour(endTime) * 60 + getMinute(endTime))
                - (getHour(startTime) * 60 + getMinute(startTime))
                + averageDuration;

        return selectedTime <= availableTime;
    }

    public static long getAvailableAverageDuration(String startTime,
                                                   String endTime, Long maxTime, Long minTime) {
        long availableTime = (maxTime - minTime) * 60;
        long selectedTime = (getHour(endTime) * 60 + getMinute(endTime))
                - (getHour(startTime) * 60 + getMinute(startTime));

        return availableTime - selectedTime;
    }

    public static Integer differenceInMinutes(Date date1, Date date2) {
        Integer difference = null;

        if (date1 != null && date2 != null) {
            if (datesEqual(date1, date2)) {
                return new Integer(0);
            } else {
                long time1 = date1.getTime();
                long time2 = date2.getTime();

                return new Integer((int) (time2 - time1) / 1000 / 60);
            }
        }

        return difference;
    }

    public static boolean datesEqual(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return true;
        }

        if (date1 == null || date2 == null) {
            return false;
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        if (cal1.getTime().equals(cal2.getTime())) {
            return true;
        }

        return false;
    }

    public static Date getMergeDates(Date date, Date time) {
        Calendar aDate = Calendar.getInstance();
        aDate.setTime(date);

        Calendar aTime = Calendar.getInstance();
        aTime.setTime(time);

        Calendar aDateTime = Calendar.getInstance();
        aDateTime.set(Calendar.DAY_OF_MONTH, aDate.get(Calendar.DAY_OF_MONTH));
        aDateTime.set(Calendar.MONTH, aDate.get(Calendar.MONTH));
        aDateTime.set(Calendar.YEAR, aDate.get(Calendar.YEAR));
        aDateTime.set(Calendar.HOUR_OF_DAY, aTime.get(Calendar.HOUR_OF_DAY));
        aDateTime.set(Calendar.MINUTE, aTime.get(Calendar.MINUTE));
        aDateTime.set(Calendar.SECOND, aTime.get(Calendar.SECOND));

        return aDateTime.getTime();
    }

    public static boolean isValidFormat(String format, String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
        }
        return date != null;
    }

    public static Date addMinutes(Date date, int minutes) {
        Instant instant = date.toInstant().plus(minutes, ChronoUnit.MINUTES);
        return Date.from(instant);
    }

    public static Date addHours(Date date, int hours) {
        Instant instant = date.toInstant().plus(hours, ChronoUnit.HOURS);
        return Date.from(instant);
    }

    public static String getDatePatternFoHL7() {
        return datePatternFoHL7;
    }

    public static Date getTodayDateFromTime(String time){
        if(StringUtils.isBlank(time))
            return null;
        Date date = DateTimeHelper.fromString(time,
                DateTimeHelper.getTimePattern());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        todayCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        todayCalendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
        return todayCalendar.getTime();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static Date fromLocalDateTime(LocalDateTime dateTime) {
        return Date.from(dateTime
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
