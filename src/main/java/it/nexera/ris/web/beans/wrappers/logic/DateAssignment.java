package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;
import java.util.Date;

public class DateAssignment implements Serializable {
    private static final long serialVersionUID = 5211618051696260234L;

    private Date date;

    private String timeFrom;

    private String timeTo;

    private Boolean slots;

    private Long ecId;

    private Long averageDuration;

    private boolean assignedSlot;

    private boolean assignNotAvail;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public Boolean getSlots() {
        return slots;
    }

    public void setSlots(Boolean slots) {
        this.slots = slots;
    }

    public Long getEcId() {
        return ecId;
    }

    public void setEcId(Long ecId) {
        this.ecId = ecId;
    }

    public boolean isAssignedSlot() {
        return assignedSlot;
    }

    public void setAssignedSlot(boolean assignedSlot) {
        this.assignedSlot = assignedSlot;
    }

    public boolean isAssignNotAvail() {
        return assignNotAvail;
    }

    public void setAssignNotAvail(boolean assignNotAvail) {
        this.assignNotAvail = assignNotAvail;
    }

    public Long getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(Long averageDuration) {
        this.averageDuration = averageDuration;
    }
}
