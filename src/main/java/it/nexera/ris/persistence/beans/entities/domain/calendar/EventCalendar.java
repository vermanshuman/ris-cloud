package it.nexera.ris.persistence.beans.entities.domain.calendar;

import it.nexera.ris.common.enums.EventCalendarStatuses;
import it.nexera.ris.common.enums.SlotSelectionTypes;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Diagnostic;
import it.nexera.ris.web.beans.wrappers.logic.Event;
import org.hibernate.HibernateException;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@javax.persistence.Entity
@Table(name = "event_calendar")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "EVENT_CALENDAR_SEQ", allocationSize = 1)
public class EventCalendar extends IndexedEntity implements
        Comparable<EventCalendar> {
    private static final long serialVersionUID = 8679391016416068248L;

    @OneToMany(mappedBy = "eventCalendar", cascade = CascadeType.REMOVE)
    private List<EventCalendarDiscardDay> discardDays;

    @Transient
    private Date endDate;

    @Column
    private String name;

    @Transient
    private Date startDate;

    @Column(name = "status_id")
    @Enumerated(EnumType.STRING)
    private EventCalendarStatuses status;

    @Column(name = "validation_period_end_date")
    private Date validationPeriodEndDate;

    @Column(name = "validation_period_start_date")
    private Date validationPeriodStartDate;

    @OneToMany(mappedBy = "eventCalendar", cascade = CascadeType.REMOVE)
    private List<EventCalendarWeekDay> weekDays;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_calendar_id", foreignKey = @ForeignKey(name = "FK_EV_CAL_EV_CAL_NEW"))
    private EventCalendar newCalendar;

    @OneToOne(mappedBy = "newCalendar", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "old_calendar_id", foreignKey = @ForeignKey(name = "FK_EV_CAL_EV_CAL_OLD"))
    private EventCalendar oldCalendar;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_selection_type")
    private SlotSelectionTypes slotSelectionType;

    @Column(name = "is_default")
    private Boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "diagnostic_id", foreignKey = @ForeignKey(name = "FK_EVENT_CALENDAR_DIAGNOSTIC"))
    private Diagnostic diagnostic;

    @Column(name = "cup_id")
    private Long cupId;

    public static Event copy(EventCalendar obj) {
        Event event = new Event();

        event.setEcId(obj.getId());
        event.setName(obj.getName());

        return event;
    }

    public EventCalendar createInheritedCopy() {
        EventCalendar ec = new EventCalendar();
        ec.setName(name);
        ec.setStatus(status);
        ec.setValidationPeriodStartDate(validationPeriodStartDate);
        ec.setValidationPeriodEndDate(validationPeriodEndDate);
        ec.setSlotSelectionType(slotSelectionType);
        ec.setIsDefault(isDefault);

        return ec;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(EventCalendar o) {
        return this.getName().compareToIgnoreCase(o.getName());
    }

    public List<EventCalendarDiscardDay> getDiscardDays() {
        return discardDays;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getStatusName() {
        if (!ValidationHelper.isNullOrEmpty(status)) {
            return status.toString();
        }

        return "";
    }

    public Date getValidationPeriodEndDate() {
        return validationPeriodEndDate;
    }

    public Date getValidationPeriodStartDate() {
        return validationPeriodStartDate;
    }

    public List<EventCalendarWeekDay> getWeekDays() {
        return weekDays;
    }

    public void setDiscardDays(List<EventCalendarDiscardDay> discardDays) {
        this.discardDays = discardDays;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setValidationPeriodEndDate(Date validationPeriodEndDate) {
        this.validationPeriodEndDate = validationPeriodEndDate;
    }

    public void setValidationPeriodStartDate(Date validationPeriodStartDate) {
        this.validationPeriodStartDate = validationPeriodStartDate;
    }

    public void setWeekDays(List<EventCalendarWeekDay> weekDays) {
        this.weekDays = weekDays;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean getDeletable() throws HibernateException,
            IllegalAccessException {
        boolean deletable = true;

        //        List<EventCalendarWeekDay> list = DaoManager.load(
        //                EventCalendarWeekDay.class, new Criterion[] {
        //                    Restrictions.eq("eventCalendar.id", getId())
        //                });
        //        
        //        if (list != null)
        //        {
        //            
        //            List<Long> ids = new ArrayList<Long>();
        //            for (EventCalendarWeekDay weekday : this.getWeekDays())
        //            {
        //                ids.add(weekday.getId());
        //            }
        //            deletable = ids.isEmpty() ? true : DaoManager.load(
        //                    EventCalendarRegistration.class,
        //                    new Criterion[] {
        //                        Restrictions.in("eventCalendarWeekday.id",
        //                                ids.toArray(new Long[0]))
        //                    }).isEmpty();
        //        }
        //        
        //        if (this.getOldCalendar() != null)
        //        {
        //            return deletable && this.getOldCalendar().getDeletable();
        //        }

        return deletable;
    }

    public EventCalendar getNewCalendar() {
        return newCalendar;
    }

    public void setNewCalendar(EventCalendar newCalendar) {
        this.newCalendar = newCalendar;
    }

    public EventCalendar getOldCalendar() {
        return oldCalendar;
    }

    public void setOldCalendar(EventCalendar oldCalendar) {
        this.oldCalendar = oldCalendar;
    }

    public SlotSelectionTypes getSlotSelectionType() {
        return slotSelectionType == null ? SlotSelectionTypes.MONO
                : slotSelectionType;
    }

    public void setSlotSelectionType(SlotSelectionTypes slotSelectionType) {
        this.slotSelectionType = slotSelectionType;
    }

    @Transient
    public String getSlotSelectionTypeStr() {
        return this.getSlotSelectionType().toString();
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public EventCalendarStatuses getStatus() {
        return status;
    }

    public void setStatus(EventCalendarStatuses status) {
        this.status = status;
    }

    public Diagnostic getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    public Long getCupId() {
        return cupId;
    }

    public void setCupId(Long cupId) {
        this.cupId = cupId;
    }
}
