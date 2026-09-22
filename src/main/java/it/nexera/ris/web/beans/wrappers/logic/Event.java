package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistration;
import org.hibernate.HibernateException;

import java.util.Date;

public class Event implements IEntity {
    private static final long serialVersionUID = -5934514039354189089L;

    private Long id;

    private String name;

    private Date startDate;

    private Date endDate;

    private boolean overbooked;

    private boolean overbookingAvailable;

    private String patientName;

    private String patientWeek;

    private boolean allDay;

    private boolean full;

    private Integer slotNumber;

    private boolean busy;

    private boolean patientChangeStatus;

    private Long ecId;

    private String styleClass;

    //private RadiologyExamRequestItem radiologyExamRequestItem;

    private String exams;

    private Boolean forwarded;

    private Long radiologyExamRequestItemId;

    private EventCalendarRegistration registration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isOverbooked() {
        return overbooked;
    }

    public void setOverbooked(boolean overbooked) {
        this.overbooked = overbooked;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public boolean isOverbookingAvailable() {
        return overbookingAvailable;
    }

    public void setOverbookingAvailable(boolean overbookingAvailable) {
        this.overbookingAvailable = overbookingAvailable;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getUniqueId() {
        return String.format("%d*%d*%b", id, slotNumber, overbookingAvailable);
    }

    public void setUniqueId(String uniqueId) {

    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public Long getEcId() {
        return ecId;
    }

    public void setEcId(Long ecId) {
        this.ecId = ecId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.persistence.beans.entities.IEntity#isNew()
     */
    @Override
    public boolean isNew() {
        return false;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
    
    /*public RadiologyExamRequestItem getRadiologyExamRequestItem()
    {
        return radiologyExamRequestItem;
    }
    
    public void setRadiologyExamRequestItem(
            RadiologyExamRequestItem radiologyExamRequestItem)
    {
        this.radiologyExamRequestItem = radiologyExamRequestItem;
    }*/

    public String getExams() {
        return exams;
    }

    public void setExams(String exams) {
        this.exams = exams;
    }

    public boolean isPatientChangeStatus() {
        return patientChangeStatus;
    }

    public void setPatientChangeStatus(boolean patientChangeStatus) {
        this.patientChangeStatus = patientChangeStatus;
    }

    @Override
    public boolean isCustomId() {
        return false;
    }

    @Override
    public boolean getDeletable() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return false;
    }

    @Override
    public boolean getEditable() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return false;
    }

    public Boolean getForwarded() {
        return forwarded;
    }

    public void setForwarded(Boolean forwarded) {
        this.forwarded = forwarded;
    }

    public Long getRadiologyExamRequestItemId() {
        return radiologyExamRequestItemId;
    }

    public void setRadiologyExamRequestItemId(Long radiologyExamRequestItemId) {
        this.radiologyExamRequestItemId = radiologyExamRequestItemId;
    }

    public EventCalendarRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(EventCalendarRegistration registration) {
        this.registration = registration;
    }

    public String getPatientWeek() {
        return patientWeek;
    }

    public void setPatientWeek(String patientWeek) {
        this.patientWeek = patientWeek;
    }
}
