package it.nexera.ris.persistence.beans.entities.domain.calendar;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Tabella per memorizzare i revocato giorni di calendario entita
 */
@javax.persistence.Entity
@Table(name = "event_calendar_discard_day")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "EVENT_CALENDAR_DD_SEQ", allocationSize = 1)
public class EventCalendarDiscardDay extends IndexedEntity {
    private static final long serialVersionUID = -6044619362727111059L;

    @Column(name = "discard_day")
    private Date discardDay;

    @Column(name = "each_year")
    private Boolean eachYear;

    @ManyToOne
    @JoinColumn(name = "event_calendar_id", foreignKey = @ForeignKey(name = "FK_EV_CAL_DISCARD_DAY_EV_CAL"))
    private EventCalendar eventCalendar;

    @Transient
    private String tempId;

    public EventCalendarDiscardDay() {

    }

    public EventCalendarDiscardDay(EventCalendarDiscardDay obj) {
        this.setDiscardDay(obj.getDiscardDay());
        this.setEachYear(obj.getEachYear());
        this.setEventCalendar(obj.getEventCalendar());
        this.setTempId(UUID.randomUUID().toString());
    }

    public Date getDiscardDay() {
        return discardDay;
    }

    public Boolean getEachYear() {
        return eachYear;
    }

    public EventCalendar getEventCalendar() {
        return eventCalendar;
    }

    public String getTempId() {
        return tempId;
    }

    public void setDiscardDay(Date discardDay) {
        this.discardDay = discardDay;
    }

    public void setEachYear(Boolean eachYear) {
        this.eachYear = eachYear;
    }

    public void setEventCalendar(EventCalendar eventCalendar) {
        this.eventCalendar = eventCalendar;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }
}
