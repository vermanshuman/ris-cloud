package it.nexera.ris.persistence.beans.entities.domain.calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CountSlot {

    @Id
    @Column(name = "event_calendar_id")
    private Long eventCalendarId;

    @Column(name = "slot_number")
    private Integer slotNumber;

    @Column(name = "id_count")
    private Long countSlot;

    @Column(name = "overbooked")
    private Boolean overboking;

    public CountSlot() {
    }

    public CountSlot(Long eventCalendarId, Integer slotNumber, Boolean overboking) {
        this.eventCalendarId = eventCalendarId;
        this.slotNumber = slotNumber;
        this.overboking = overboking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CountSlot countSlot = (CountSlot) o;

        if (eventCalendarId != null ? !eventCalendarId.equals(countSlot.eventCalendarId) : countSlot.eventCalendarId != null)
            return false;
        if (slotNumber != null ? !slotNumber.equals(countSlot.slotNumber) : countSlot.slotNumber != null) return false;
        return overboking != null ? overboking.equals(countSlot.overboking) : countSlot.overboking == null;
    }

    @Override
    public int hashCode() {
        int result = eventCalendarId != null ? eventCalendarId.hashCode() : 0;
        result = 31 * result + (slotNumber != null ? slotNumber.hashCode() : 0);
        result = 31 * result + (overboking != null ? overboking.hashCode() : 0);
        return result;
    }

    public Long getEventCalendarId() {
        return eventCalendarId;
    }

    public void setEventCalendarId(Long eventCalendarId) {
        this.eventCalendarId = eventCalendarId;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public Long getCountSlot() {
        return countSlot;
    }

    public void setCountSlot(Long countSlot) {
        this.countSlot = countSlot;
    }

    public Boolean getOverboking() {
        return overboking;
    }

    public void setOverboking(Boolean overboking) {
        this.overboking = overboking;
    }
}
