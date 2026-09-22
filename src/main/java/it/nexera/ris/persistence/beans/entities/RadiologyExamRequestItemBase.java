package it.nexera.ris.persistence.beans.entities;

import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistration;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Package;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.annotations.Formula;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@MappedSuperclass
public abstract class RadiologyExamRequestItemBase extends IndexedEntity implements Cloneable {

    public transient final Logger log = LogManager.getLogger(getClass());

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rad_exam_id")
    private RadiologyExam radiologyExam;

    @Column(name = "rad_exam_id", insertable = false, updatable = false)
    private Long radiologyExamId;

    @Column(name = "request_date")
    private Date requestDate;

    @Column(name = "reserve_date")
    private Date reserveDate;

    @Column(name = "accept_date")
    private Date acceptDate;

    @Column(name = "perform_date")
    private Date performDate;

    @Column(name = "report_date")
    private Date reportDate;

    @Column(name = "note", length = 1024)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "wl_registration_state")
    private WaitingListRegistrationStates waitingListRegistrationState;

    @Column(name = "date_changed")
    private Boolean requestDateChanged;

    @Column(name = "access_number_code")
    private String accessNumberCode;

    @Column(name = "access_number_year")
    private Long accessNumberYear;

    @Column(name = "access_number_Id")
    private String accessNumberId;

    @Formula("access_number_code || access_number_year || access_number_Id")
    private String accessNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "radilogy_exam_package_id")
    private Package radiologyExamPackage;

    @Column(name = "urgent_request")
    private Boolean urgentRequest;

    @Column(name = "file_entity_id", insertable = false, updatable = false)
    private Long fileEntityId;

    @Column(name = "sign_file_entity_id", insertable = false, updatable = false)
    private Long signFileEntityId;

    @Column(name = "delete_comment", length = 4000)
    private String deleteComment;

    @Column(name = "report_result", columnDefinition = "CLOB")
    private String reportResult;

    @Column(name = "trsm")
    private String trsm;

    @Column(name = "operator")
    private String operator;

    @Column(name = "delete_user")
    private String deleteUser;

    @Column(name = "requesting_doctor")
    private String requestingDoctor;

    @Column(name = "asap_pl_ord_num")
    private String asapPlacerOrderNumber;

    @Column(name = "diagnostic_question", length = 1024)
    private String diagnosticQuestion;

    @Column(name = "card_number")
    private Long cardNumber;

    @Column(name = "report_result_autosaved", columnDefinition = "CLOB")
    private String reportResultAutoSaved;

    @Column(name = "auto_save_date")
    private Date autoSaveDate;

    @Column(name = "report_save_date")
    private Date reportSaveDate;

    @Column(name = "added_manually")
    private Boolean addedManually;

    @Column(name = "forwarded")
    private Boolean forwarded;

    @Column(name = "delivery_dose")
    private String deliverydose;

    @Transient
    private RadiologyExamRequest[] selectedItems;

    @Transient
    private Boolean selected;

    @Transient
    private EventCalendarRegistration eventCalendarRegistration;

    public abstract List<EventCalendarRegistration> getEventCalendarRegistrations();

    public abstract FileEntity getFileEntity();

    public abstract void setFileEntity(FileEntity fileEntity);

    public abstract <T extends RadiologyExamRequestBase> T getRadiologyExamRequest();

    public Long getRadiologyExamId() {
        return radiologyExamId;
    }

    public void setRadiologyExamId(Long radiologyExamId) {
        this.radiologyExamId = radiologyExamId;
    }

    public void loadFileEntity() {
        try {
            setFileEntity(DaoManager.get(FileEntity.class, new Criterion[]{
                    Restrictions.eq("id", fileEntityId)
            }));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public Date getRequestDate() {
        if (this.requestDate != null) {
            this.requestDateChanged = Boolean.TRUE;
        }
        return requestDate;
    }

    @Transient
    public String getAgenda() {
        try {
            if (!ValidationHelper.isNullOrEmpty(this.getEventCalendarRegistrations())) {
                return this.getEventCalendarRegistrations().get(0).toString();
            }
        } catch (Exception e) {

        }
        return null;
    }

    public Boolean getRequestDateChanged() {
        return requestDateChanged == null ? Boolean.FALSE : requestDateChanged;
    }

    public void setRequestDateChanged(Boolean requestDateChanged) {
        this.requestDateChanged = requestDateChanged;
    }

    public Boolean getSelected() {
        return selected == null ? Boolean.TRUE : selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public void downloadPdf() throws IOException {
        try {
            FileEntity file = DaoManager.get(FileEntity.class, fileEntityId);

            if (file != null && file.getName() != null) {
                byte[] data = null;

                if (file.getContent() != null) {
                    data = file.getContent();
                }

                // New logic for entities stored on hard drive
                else if (file.getPath() != null) {
                    Path path = Paths.get(file.getPath());

                    if (Files.exists(path) && !Files.isDirectory(path)) {
                        data = Files.readAllBytes(path);
                    }
                }

                if (data != null) {
                    FileHelper.sendFile(file.getName(), data);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Transient
    public Date getDateForModify() {
        if (WaitingListRegistrationStates.ACCEPTED.equals(this.getWaitingListRegistrationState())) {
            return this.getAcceptDate();
        } else if (WaitingListRegistrationStates.RESERVED.equals(this.getWaitingListRegistrationState())) {
            return this.getReserveDate();
        }
        return null;
    }

    public String getAccessNumber() {
        if (WaitingListRegistrationStates.RESERVED.equals(this.getWaitingListRegistrationState())) {
            return "";
        }
        return accessNumber;
    }

    public void setAccessNumber(String accessNumber) {
        this.accessNumber = accessNumber;
    }

    public RadiologyExam getRadiologyExam() {
        if (radiologyExam instanceof HibernateProxy) {
            Session session = null;
            try {
                session = PersistenceSession.createSession();
                radiologyExam = ConnectionManager.get(RadiologyExam.class, getRadiologyExamId(), session);
            } catch (Exception e) {
                LogHelper.log(log, e);
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }

        return radiologyExam;
    }

    public void setRadiologyExam(RadiologyExam radiologyExam) {
        this.radiologyExam = radiologyExam;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(Date reserveDate) {
        this.reserveDate = reserveDate;
    }

    public Date getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(Date acceptDate) {
        this.acceptDate = acceptDate;
    }

    public Date getPerformDate() {
        return performDate;
    }

    public void setPerformDate(Date performDate) {
        this.performDate = performDate;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public WaitingListRegistrationStates getWaitingListRegistrationState() {
        return waitingListRegistrationState;
    }

    public void setWaitingListRegistrationState(WaitingListRegistrationStates waitingListRegistrationState) {
        this.waitingListRegistrationState = waitingListRegistrationState;
    }

    public String getAccessNumberCode() {
        return accessNumberCode;
    }

    public void setAccessNumberCode(String accessNumberCode) {
        this.accessNumberCode = accessNumberCode;
    }

    public Long getAccessNumberYear() {
        return accessNumberYear;
    }

    public void setAccessNumberYear(Long accessNumberYear) {
        this.accessNumberYear = accessNumberYear;
    }

    public String getAccessNumberId() {
        return accessNumberId;
    }

    public void setAccessNumberId(String accessNumberId) {
        this.accessNumberId = accessNumberId;
    }

    public Package getRadiologyExamPackage() {
        return radiologyExamPackage;
    }

    public void setRadiologyExamPackage(Package radiologyExamPackage) {
        this.radiologyExamPackage = radiologyExamPackage;
    }

    public Boolean getUrgentRequest() {
        return urgentRequest;
    }

    public void setUrgentRequest(Boolean urgentRequest) {
        this.urgentRequest = urgentRequest;
    }

    public Long getFileEntityId() {
        return fileEntityId;
    }

    public void setFileEntityId(Long fileEntityId) {
        this.fileEntityId = fileEntityId;
    }

    public Long getSignFileEntityId() {
        return signFileEntityId;
    }

    public void setSignFileEntityId(Long signFileEntityId) {
        this.signFileEntityId = signFileEntityId;
    }

    public String getDeleteComment() {
        return deleteComment;
    }

    public void setDeleteComment(String deleteComment) {
        this.deleteComment = deleteComment;
    }

    public String getReportResult() {
        return reportResult;
    }

    public void setReportResult(String reportResult) {
        this.reportResult = reportResult;
    }

    public String getTrsm() {
        return trsm;
    }

    public void setTrsm(String trsm) {
        this.trsm = trsm;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDeleteUser() {
        return deleteUser;
    }

    public void setDeleteUser(String deleteUser) {
        this.deleteUser = deleteUser;
    }

    public String getRequestingDoctor() {
        return requestingDoctor;
    }

    public void setRequestingDoctor(String requestingDoctor) {
        this.requestingDoctor = requestingDoctor;
    }

    public String getAsapPlacerOrderNumber() {
        return asapPlacerOrderNumber;
    }

    public void setAsapPlacerOrderNumber(String asapPlacerOrderNumber) {
        this.asapPlacerOrderNumber = asapPlacerOrderNumber;
    }

    public String getDiagnosticQuestion() {
        return diagnosticQuestion;
    }

    public void setDiagnosticQuestion(String diagnosticQuestion) {
        this.diagnosticQuestion = diagnosticQuestion;
    }

    public Long getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(Long cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getReportResultAutoSaved() {
        return reportResultAutoSaved;
    }

    public void setReportResultAutoSaved(String reportResultAutoSaved) {
        this.reportResultAutoSaved = reportResultAutoSaved;
    }

    public Date getAutoSaveDate() {
        return autoSaveDate;
    }

    public void setAutoSaveDate(Date autoSaveDate) {
        this.autoSaveDate = autoSaveDate;
    }

    public Date getReportSaveDate() {
        return reportSaveDate;
    }

    public void setReportSaveDate(Date reportSaveDate) {
        this.reportSaveDate = reportSaveDate;
    }

    public Boolean getAddedManually() {
        return addedManually;
    }

    public void setAddedManually(Boolean addedManually) {
        this.addedManually = addedManually;
    }

    public Boolean getForwarded() {
        return forwarded;
    }

    public void setForwarded(Boolean forwarded) {
        this.forwarded = forwarded;
    }

    public RadiologyExamRequest[] getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(RadiologyExamRequest[] selectedItems) {
        this.selectedItems = selectedItems;
    }

    public EventCalendarRegistration getEventCalendarRegistration() {
        return eventCalendarRegistration;
    }

    public void setEventCalendarRegistration(EventCalendarRegistration eventCalendarRegistration) {
        this.eventCalendarRegistration = eventCalendarRegistration;
    }

    public String getDeliverydose() {
        return deliverydose;
    }

    public void setDeliverydose(String deliverydose) {
        this.deliverydose = deliverydose;
    }

    @Override
    public String toString() {
        return "RadiologyExamRequestItemBase{"
                + super.toString() +
                ",\r\n \"radiologyExamId\":" + radiologyExamId +
                ",\r\n \"requestDate\":" + requestDate +
                ",\r\n \"reserveDate\":" + reserveDate +
                ",\r\n \"acceptDate\":" + acceptDate +
                ",\r\n \"performDate\":" + performDate +
                ",\r\n \"reportDate\":" + reportDate +
                ",\r\n \"note\":'" + note + '\'' +
                ",\r\n \"waitingListRegistrationState\":" + waitingListRegistrationState +
                ",\r\n \"requestDateChanged\":" + requestDateChanged +
                ",\r\n \"accessNumberCode\":'" + accessNumberCode + '\'' +
                ",\r\n \"accessNumberYear\":" + accessNumberYear +
                ",\r\n \"accessNumberId\":'" + accessNumberId + '\'' +
                ",\r\n \"accessNumber\":'" + accessNumber + '\'' +
                ",\r\n \"urgentRequest\":" + urgentRequest +
                ",\r\n \"fileEntityId\":" + fileEntityId +
                ",\r\n \"signFileEntityId\":" + signFileEntityId +
                ",\r\n \"deleteComment\":'" + deleteComment + '\'' +
                ",\r\n \"reportResult\":'" + reportResult + '\'' +
                ",\r\n \"trsm\":'" + trsm + '\'' +
                ",\r\n \"operator\":'" + operator + '\'' +
                ",\r\n \"deleteUser\":'" + deleteUser + '\'' +
                ",\r\n \"requestingDoctor\":'" + requestingDoctor + '\'' +
                ",\r\n \"asapPlacerOrderNumber\":'" + asapPlacerOrderNumber + '\'' +
                ",\r\n \"diagnosticQuestion\":'" + diagnosticQuestion + '\'' +
                ",\r\n \"cardNumber\":" + cardNumber +
                ",\r\n \"reportResultAutoSaved\":'" + reportResultAutoSaved + '\'' +
                ",\r\n \"autoSaveDate\":" + autoSaveDate +
                ",\r\n \"reportSaveDate\":" + reportSaveDate +
                ",\r\n \"addedManually\":" + addedManually +
                ",\r\n \"forwarded\":" + forwarded +
                ",\r\n \"deliverydose\":" + deliverydose +
                ",\r\n} ";
    }

}
