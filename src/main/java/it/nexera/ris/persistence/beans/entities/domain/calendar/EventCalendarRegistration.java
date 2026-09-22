package it.nexera.ris.persistence.beans.entities.domain.calendar;

import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "event_calendar_registration")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "EVENT_CALENDAR_REG_SEQ", allocationSize = 1)
public class EventCalendarRegistration extends EventCalendarRegistrationBase {
    private static final long serialVersionUID = -8469159016544471258L;

    @ManyToOne
    @JoinColumn(name = "event_calendar_weekday_id", foreignKey = @ForeignKey(name = "FK_EV_CAL_REG_EV_CAL_WEEKDAY"))
    private EventCalendarWeekDay eventCalendarWeekday;

    @ManyToOne
    @JoinColumn(name = "request_item_id", foreignKey = @ForeignKey(name = "FK_EV_CAL_REG_RAD_EX_ITEM"))
    private RadiologyExamRequestItem radiologyExamRequestItem;

    public EventCalendarWeekDay getEventCalendarWeekday() {
        return eventCalendarWeekday;
    }

    public void setEventCalendarWeekday(
            EventCalendarWeekDay eventCalendarWeekday) {
        this.eventCalendarWeekday = eventCalendarWeekday;
    }

    @Override
    public String toString() {
        return this.eventCalendarWeekday.getEventCalendar().getName();
    }

    public RadiologyExamRequestItem getRadiologyExamRequestItem() {
        return radiologyExamRequestItem;
    }

    public void setRadiologyExamRequestItem(
            RadiologyExamRequestItem radiologyExamRequestItem) {
        this.radiologyExamRequestItem = radiologyExamRequestItem;
    }

}
