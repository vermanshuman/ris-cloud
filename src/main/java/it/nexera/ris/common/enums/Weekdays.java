package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.util.Calendar;
import java.util.Date;

public enum Weekdays {
    MONDAY(2l),
    TUESDAY(3l),
    WEDNESDAY(4l),
    THURTHDAY(5l),
    FRIDAY(6l),
    SATURDAY(7l),
    SUNDAY(1l);

    private Long number;

    private Weekdays(Long number) {
        this.setNumber(number);
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public static Weekdays getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        for (Weekdays day : Weekdays.values()) {
            if (day.getNumber().intValue() == calendar
                    .get(Calendar.DAY_OF_WEEK)) {
                return day;
            }
        }

        return null;
    }

    public static Weekdays getByDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        for (Weekdays day : Weekdays.values()) {
            if (day.getNumber().intValue() == calendar
                    .get(Calendar.DAY_OF_WEEK)) {
                return day;
            }
        }

        return null;
    }

}
