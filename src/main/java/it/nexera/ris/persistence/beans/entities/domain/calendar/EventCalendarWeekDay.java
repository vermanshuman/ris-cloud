package it.nexera.ris.persistence.beans.entities.domain.calendar;

import it.nexera.ris.common.enums.EventCalendarWeekdayAvailabilityTypes;
import it.nexera.ris.common.enums.Weekdays;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@javax.persistence.Entity
@Table(name = "event_calendar_week_day")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "EVENT_CALENDAR_WD_SEQ", allocationSize = 1)
public class EventCalendarWeekDay extends IndexedEntity {

    private static final long serialVersionUID = 1795254420659849416L;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_type")
    private EventCalendarWeekdayAvailabilityTypes availabilityType;

    @Column(name = "average_duration")
    private Long averageDuration;

    @Column(name = "end_time_str")
    private String endTimeStr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_calendar_id", foreignKey = @ForeignKey(name = "FK_EV_CAL_WEEKDAY_EV_CAL"))
    private EventCalendar eventCalendar;

    @OneToMany(mappedBy = "eventCalendarWeekday")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<EventCalendarRegistration> eventCalendarRegistrations;

    @Column(name = "maximum_overbooking")
    private Integer maximumOverbooking;

    @Column
    private Boolean overbooking;

    @Column(name = "slot_number")
    private Integer slotNumber;

    @Column(name = "start_time_str")
    private String startTimeStr;

    @Transient
    private String tempId;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_day")
    private Weekdays weekDay;

    @Transient
    private boolean canBeMerged = false;

    public EventCalendarWeekDay() {
    }

    public EventCalendarWeekDay(EventCalendarWeekDay obj) {
        this.setAvailabilityType(obj.getAvailabilityType());
        this.setEndTimeStr(obj.getEndTimeStr());
        this.setEventCalendar(obj.getEventCalendar());
        this.setMaximumOverbooking(obj.getMaximumOverbooking());
        this.setOverbooking(obj.getOverbooking());
        this.setSlotNumber(obj.getSlotNumber());
        this.setStartTimeStr(obj.getStartTimeStr());
        this.setWeekDay(obj.getWeekDay());
        this.setTempId(UUID.randomUUID().toString());
        this.setAverageDuration(obj.getAverageDuration());
    }

    public EventCalendarWeekdayAvailabilityTypes getAvailabilityType() {
        return availabilityType;
    }

    public void setAvailabilityType(
            EventCalendarWeekdayAvailabilityTypes availabilityType) {
        this.availabilityType = availabilityType;
    }

    public String getAvailabilityKindName() {
        return (this.getAvailabilityType() == null) ? "" : this
                .getAvailabilityType().toString();
    }

    public Long getAverageDuration() {
        return averageDuration;
    }

    public String getEndTimeStr() {
        return endTimeStr;
    }

    public EventCalendar getEventCalendar() {
        return eventCalendar;
    }

    public Integer getMaximumOverbooking() {
        return maximumOverbooking;
    }

    public Boolean getOverbooking() {
        return overbooking;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public String getTempId() {
        return tempId;
    }

    public Weekdays getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(Weekdays weekDay) {
        this.weekDay = weekDay;
    }

    public String getWeekdayName() {
        return this.getWeekDay().toString();
    }

    public void setAverageDuration(Long averageDuration) {
        this.averageDuration = averageDuration;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
    }

    public void setEventCalendar(EventCalendar eventCalendar) {
        this.eventCalendar = eventCalendar;
    }

    public void setMaximumOverbooking(Integer maximumOverbooking) {
        this.maximumOverbooking = maximumOverbooking;
    }

    public void setOverbooking(Boolean overbooking) {
        this.overbooking = overbooking;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public void setStartTimeStr(String startTimeStr) {
        this.startTimeStr = startTimeStr;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    public boolean isCanBeMerged() {
        return canBeMerged;
    }

    public void setCanBeMerged(boolean canBeMerged) {
        this.canBeMerged = canBeMerged;
    }

    public List<EventCalendarRegistration> getEventCalendarRegistrations() {
        return eventCalendarRegistrations;
    }

    public void setEventCalendarRegistrations(
            List<EventCalendarRegistration> eventCalendarRegistrations) {
        this.eventCalendarRegistrations = eventCalendarRegistrations;
    }
}
