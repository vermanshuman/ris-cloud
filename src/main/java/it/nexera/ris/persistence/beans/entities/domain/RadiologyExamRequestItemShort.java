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
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "rad_exam_request_item_short")
@SequenceGenerator(name = "ID_SEQ_GEN", sequenceName = "RAD_EXAM_REQ_ITEM_SHORT_SEQ", allocationSize = 1)
public class RadiologyExamRequestItemShort extends RadiologyExamRequestItemBase {

    private static final long serialVersionUID = -118515637581798552L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rad_exam_request_id", foreignKey = @ForeignKey(name = "FK_RAD_EX_REQ_IT_S_RAD_EX_REQ"))
    private RadiologyExamRequestShort radiologyExamRequest;

    @Column(name = "rad_exam_request_id", insertable = false, updatable = false)
    private Long radiologyExamRequestId;

    @Transient
    private Date latestActionDate;

    @Transient
    private FileEntity fileEntity;

    @Override
    public RadiologyExamRequestItemShort clone() throws CloneNotSupportedException {
        RadiologyExamRequestItemShort obj = new RadiologyExamRequestItemShort();

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

    @Override
    public RadiologyExamRequestShort getRadiologyExamRequest() {
        return radiologyExamRequest;
    }

    public void setRadiologyExamRequest(RadiologyExamRequestShort radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    public Date getLatestActionDate() {
        if (latestActionDate == null) {
            try {
                List<Object> dates = DaoManager.getFields(RadiologyExamRequestShort.class, new Criterion[]{
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
        try {
            return DaoManager.load(EventCalendarRegistration.class, new Criterion[]{
                    Restrictions.eq("radiologyExamRequestItem", getId())
            });
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return new LinkedList<>();
    }

    @Override
    public FileEntity getFileEntity() {
        if (fileEntity == null) {
            loadFileEntity();
        }
        return fileEntity;
    }

    @Override
    public void setFileEntity(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }
}
