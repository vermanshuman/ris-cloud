package it.nexera.ris.web.common;

import it.nexera.ris.common.helpers.DateTimeHelper;

import java.util.Date;

public class DateCalendar {

    private Date date;

    private Date endDate;

    private Long eventCalendarId;

    public DateCalendar(Date date, Date endDate, Long eventCalendarId) {
        this.date = date;
        this.endDate = endDate;
        this.eventCalendarId = eventCalendarId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateCalendar that = (DateCalendar) o;

        if (date != null && endDate != null ? !DateTimeHelper.between(that.date, date, endDate) :
                that.date != null && that.endDate != null) return false;
        if (date != null && endDate != null ? !DateTimeHelper.between(that.endDate, date, endDate) :
                that.date != null && that.endDate != null) return false;
        return eventCalendarId != null ? eventCalendarId.equals(that.eventCalendarId) : that.eventCalendarId == null;
    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (eventCalendarId != null ? eventCalendarId.hashCode() : 0);
        return result;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getEventCalendarId() {
        return eventCalendarId;
    }

    public void setEventCalendarId(Long eventCalendarId) {
        this.eventCalendarId = eventCalendarId;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
