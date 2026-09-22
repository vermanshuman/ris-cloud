package it.nexera.ris.persistence.beans.entities.domain.calendar;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "event_calendar_reg_slot")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "EVENT_CALENDAR_REG_SLOT_SEQ", allocationSize = 1)
public class EventCalendarRegistrationSlot extends IndexedEntity {
    private static final long serialVersionUID = 4583288593197933595L;

    @ManyToOne
    @JoinColumn(name = "event_calendar_registration_id", foreignKey = @ForeignKey(name = "FK_EV_CAL_REG_SLOT_EV_CAL_REG"))
    private EventCalendarRegistration registration;

    @Column(name = "slot_number")
    private Integer slotNumber;

    @Column
    private Boolean overbooked;

    public EventCalendarRegistrationSlot() {
        super();
    }

    public EventCalendarRegistrationSlot(
            EventCalendarRegistration registration, Integer slotNumber,
            Boolean overbooked) {
        super();
        this.registration = registration;
        this.slotNumber = slotNumber;
        this.overbooked = overbooked;
    }

    public EventCalendarRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(EventCalendarRegistration registration) {
        this.registration = registration;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public boolean getOverbooked() {
        return overbooked == null ? false : overbooked;
    }

    public void setOverbooked(Boolean overbooked) {
        this.overbooked = overbooked;
    }
}
