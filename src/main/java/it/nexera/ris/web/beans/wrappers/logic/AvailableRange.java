package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.web.converters.BaseConverter;

import java.util.Calendar;
import java.util.Date;

public class AvailableRange implements Comparable<AvailableRange> {
    private Date startDate;

    private Date endDate;

    private int minutesRange;

    public AvailableRange(Date start, Date end, int range) {
        this.setEndDate(end);
        this.setStartDate(start);
        this.setMinutesRange(range);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getMinutesRange() {
        return minutesRange;
    }

    public void setMinutesRange(int minutesRange) {
        this.minutesRange = minutesRange;
    }

    public String getFromTime() {
        Calendar c3 = Calendar.getInstance();
        c3.setTime(this.getStartDate());

        return String
                .format("%s:%s", BaseConverter.convertToHourString(c3
                        .get(Calendar.HOUR_OF_DAY)), BaseConverter
                        .convertToHourString(c3.get(Calendar.MINUTE)));
    }

    public String getToTime() {
        Calendar c4 = Calendar.getInstance();
        c4.setTime(this.getEndDate());

        return String
                .format("%s:%s", BaseConverter.convertToHourString(c4
                        .get(Calendar.HOUR_OF_DAY)), BaseConverter
                        .convertToHourString(c4.get(Calendar.MINUTE)));
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(AvailableRange o) {
        return ((Integer) o.getMinutesRange())
                .compareTo(this.getMinutesRange());
    }
}
