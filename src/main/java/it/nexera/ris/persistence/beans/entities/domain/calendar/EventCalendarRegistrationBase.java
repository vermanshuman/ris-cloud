package it.nexera.ris.persistence.beans.entities.domain.calendar;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@MappedSuperclass
public class EventCalendarRegistrationBase extends IndexedEntity {
    public transient final Logger log = LogManager.getLogger(EventCalendarRegistrationBase.class);

    private static final long serialVersionUID = -5880160344022933897L;

    @Column(name = "from_date")
    private Date fromDate;

    @OneToMany(mappedBy = "registration", cascade = CascadeType.REMOVE)
    private List<EventCalendarRegistrationSlot> slots;

    @Transient
    private List<EventCalendarRegistrationSlot> slotsToSave;

    @Column(name = "to_date")
    private Date toDate;

    @Column(name = "date_")
    private Date date;

    @Column(name = "package_id")
    private String packageId;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "patient_surname")
    private String patientSurname;

    @Column(name = "patient_birth_day")
    private Date patientBirthDay;

    @Column(name = "sector_name")
    private String sectorName;

    @Column(name = "exams_name", columnDefinition = "CLOB")
    private String examsName;

    @Transient
    private Boolean containsSlot;

    @Transient
    private Integer slotNumber;

    public boolean containsSlot(Integer slotNumber, boolean overbooked) {
        Long count = 0L;

        try {
            count = DaoManager.getCount(EventCalendarRegistrationSlot.class,
                    "id", new Criterion[]{
                            Restrictions.eq("registration.id", this.getId()),
                            Restrictions.eq("slotNumber", slotNumber),
                            Restrictions.eq("overbooked", overbooked)
                    });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return count > 0L;
    }

    public String getPatientFullName() {
        return String.format("%s %s",
                this.getPatientSurname() == null ? ""
                        : this.getPatientSurname(),
                this.getPatientName() == null ? "" : this.getPatientName());
    }

    public String getPatientSurnameName() {
        return String.format("%s %s",
                this.getPatientName() == null ? "" : this.getPatientName(),
                this.getPatientSurname() == null ? ""
                        : this.getPatientSurname());
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public List<EventCalendarRegistrationSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<EventCalendarRegistrationSlot> slots) {
        this.slots = slots;
    }

    public List<EventCalendarRegistrationSlot> getSlotsToSave() {
        return slotsToSave;
    }

    public void setSlotsToSave(List<EventCalendarRegistrationSlot> slotsToSave) {
        this.slotsToSave = slotsToSave;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public Date getPatientBirthDay() {
        return patientBirthDay;
    }

    public void setPatientBirthDay(Date patientBirthDay) {
        this.patientBirthDay = patientBirthDay;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public String getExamsName() {
        return examsName;
    }

    public void setExamsName(String examsName) {
        this.examsName = examsName;
    }

    public Boolean getContainsSlot() {
        return containsSlot;
    }

    public void setContainsSlot(Boolean containsSlot) {
        this.containsSlot = containsSlot;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }
}
