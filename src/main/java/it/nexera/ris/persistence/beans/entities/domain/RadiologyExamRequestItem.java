package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestItemBase;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistration;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestItemWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "rad_exam_request_item")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "RAD_EXAM_REQUEST_ITEM_SEQ", allocationSize = 1)
public class RadiologyExamRequestItem extends RadiologyExamRequestItemBase {

    private static final long serialVersionUID = 6218462921014947972L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rad_exam_request_id", foreignKey = @ForeignKey(name = "FK_RAD_EX_REQ_ITEM_RAD_EX_REQ"))
    private RadiologyExamRequest radiologyExamRequest;

    @Column(name = "rad_exam_request_id", insertable = false, updatable = false)
    private Long radiologyExamRequestId;

    @OneToMany(mappedBy = "radiologyExamRequestItem", cascade = CascadeType.REMOVE)
    private List<EventCalendarRegistration> eventCalendarRegistrations;

    @Transient
    private Date latestActionDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_entity_id", foreignKey = @ForeignKey(name = "FK_RAD_EX_REQ_ITEM_FILE_ENTITY"))
    private FileEntity fileEntity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sign_file_entity_id", foreignKey = @ForeignKey(name = "FK_RAD_EX_REQ_ITEM_FE_SIGN"))
    private FileEntity signFileEntity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "not_signed_file_entity_id", foreignKey = @ForeignKey(name = "FK_RAD_EX_REQ_ITEM_FILE_ENTITY"))
    private FileEntity notSignFileEntity;

    @Column(name = "aggregation_request_id")
    private Long aggregationRequestId;

    @Override
    public RadiologyExamRequestItem clone() throws CloneNotSupportedException {
        RadiologyExamRequestItem obj = new RadiologyExamRequestItem();

        obj.setDiagnosticQuestion(getDiagnosticQuestion());
        obj.setAcceptDate(getAcceptDate());
        obj.setCreateDate(getCreateDate());
        obj.setPerformDate(getPerformDate());
        obj.setReserveDate(getReserveDate());
        obj.setUpdateDate(getUpdateDate());
        obj.setRequestDate(getRequestDate());

        obj.setAccessNumber(getAccessNumber());
        obj.setAccessNumberCode(getAccessNumberCode());
        obj.setAccessNumberId(getAccessNumberId());
        obj.setAccessNumberYear(getAccessNumberYear());

        obj.setAsapPlacerOrderNumber(getAsapPlacerOrderNumber());
        obj.setCreateUserId(getCreateUserId());
        obj.setDeleteComment(getDeleteComment());
        obj.setFileEntity(getFileEntity());
        obj.setOperator(getOperator());
        obj.setRadiologyExam(getRadiologyExam());
        obj.setRadiologyExamPackage(getRadiologyExamPackage());
        obj.setRadiologyExamRequest(getRadiologyExamRequest());
        obj.setTrsm(getTrsm());
        obj.setUpdateUserId(getUpdateUserId());
        obj.setUrgentRequest(getUrgentRequest());
        obj.setVersion(getVersion());
        obj.setWaitingListRegistrationState(getWaitingListRegistrationState());
        obj.setRequestingDoctor(getRequestingDoctor());
        obj.setNote(getNote());

        return obj;
    }

    public Long getRadiologyExamRequestId() {
        return radiologyExamRequestId;
    }

    public void setRadiologyExamRequestId(Long radiologyExamRequestId) {
        this.radiologyExamRequestId = radiologyExamRequestId;
    }

    @Override
    public RadiologyExamRequest getRadiologyExamRequest() {
        return radiologyExamRequest;
    }

    public void setRadiologyExamRequest(RadiologyExamRequest radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    @Transient
    public Date getLatestActionDate() {
        if (latestActionDate == null) {
            try {
                List<Object> dates = DaoManager.getFields(RadiologyExamRequest.class, new Criterion[]{
                        Restrictions.eq("id", this.radiologyExamRequestId)
                }, null, true, "latestActionDate");

                if (!ValidationHelper.isNullOrEmpty(dates)) {
                    latestActionDate = (Date) dates.get(0);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return latestActionDate;
    }

    @Transient
    public RadExamRequestItemWrapper getRadExamRequestItemWrapperFromItem()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return new RadExamRequestItemWrapper(this.getId(),
                this.getRadiologyExam(), this.getRequestingDoctor(),
                this.getReserveDate(), this.getRequestDate(), this
                .getRadiologyExamRequest().getId(),
                this.getDiagnosticQuestion(), this.getAccessNumber(), this
                .getRadiologyExamRequest()
                .getRadExamRequestWrapperFromRequest(), this.getAddedManually());
    }

    @Override
    public List<EventCalendarRegistration> getEventCalendarRegistrations() {
        return eventCalendarRegistrations;
    }

    public void setEventCalendarRegistrations(List<EventCalendarRegistration> eventCalendarRegistrations) {
        this.eventCalendarRegistrations = eventCalendarRegistrations;
    }

    public void setLatestActionDate(Date latestActionDate) {
        this.latestActionDate = latestActionDate;
    }

    @Override
    public FileEntity getFileEntity() {
        return fileEntity;
    }

    @Override
    public void setFileEntity(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }

    public FileEntity getSignFileEntity() {
        return signFileEntity;
    }

    public void setSignFileEntity(FileEntity signFileEntity) {
        this.signFileEntity = signFileEntity;
    }

    public Long getAggregationRequestId() {
        return aggregationRequestId;
    }

    public void setAggregationRequestId(Long aggregationRequestId) {
        this.aggregationRequestId = aggregationRequestId;
    }

    @Override
    public String toString() {
        return "\"RadiologyExamRequestItem\":{"
                + super.toString() +
                ",\r\n \"radiologyExamRequestId\":" + radiologyExamRequestId +
                ",\r\n \"latestActionDate\":" + latestActionDate +
                ",\r\n \"aggregationRequestId\":" + aggregationRequestId +
                "} ";
    }

    public FileEntity getNotSignFileEntity() {
        return notSignFileEntity;
    }

    public void setNotSignFileEntity(FileEntity notSignFileEntity) {
        this.notSignFileEntity = notSignFileEntity;
    }
}
