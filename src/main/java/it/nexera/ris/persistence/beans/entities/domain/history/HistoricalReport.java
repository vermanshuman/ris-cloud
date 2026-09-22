package it.nexera.ris.persistence.beans.entities.domain.history;

import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.web.beans.wrappers.logic.DocumentGenerationHistoryWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "historical_report")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "HIST_REP_SEQ", allocationSize = 1)
public class HistoricalReport extends IndexedEntity implements Cloneable {
    private static final long serialVersionUID = 5021737537593419648L;

    public transient final Logger log = LogManager.getLogger(getClass());

    @Column(name = "id_in_old_db")
    private Long idInOldDb;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rad_ex_request_id", foreignKey = @ForeignKey(name = "FK_HISTORY_RAD_EXAM_REQUEST"))
    private RadiologyExamRequest radiologyExamRequest;

    @Column(name = "pat_fiscal_code")
    private String patientFiscalCode;

    @Column(name = "pat_name")
    private String patientName;

    @Column(name = "pat_surname")
    private String patientSurname;

    @Column(name = "pat_birth_date")
    private Date patientBirthDate;

    @Column(name = "reserve_date")
    private Date reserveDate;

    @Column(name = "report_date")
    private Date reportDate;

    @Column(name = "request_date")
    private Date requestDate;

    @Column(name = "accept_date")
    private Date acceptDate;

    @Column(name = "perform_date")
    private Date performDate;

    @Column(name = "latest_action_date")
    private Date latestActionDate;

    @Column(name = "access_number")
    private String accessNumber;

    @Column(name = "referring_doctor")
    private String referringDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_type_id", foreignKey = @ForeignKey(name = "FK_HISTORY_EXAM_TYPE"))
    private ExamType examType;

    @ManyToMany
    @JoinTable(name = "history_radiology_exam", joinColumns = {
            @JoinColumn(name = "historical_report_id", table = "historical_reports",
                    foreignKey = @ForeignKey(name = "FK_HISTORY_REPORT_ID"))
    }, inverseJoinColumns = {
            @JoinColumn(name = "radiology_exame_id", table = "dic_radiology_exam",
                    foreignKey = @ForeignKey(name = "FK_HISTORY_RAD_EXAM"))
    })
    private List<RadiologyExam> radiologyExams;

    @Column(name = "hospital_code")
    private String hospitalCode;

    @Column(name = "report_result", columnDefinition = "CLOB")
    private String reportResult;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_entity_id", foreignKey = @ForeignKey(name = "FK_HISTORY_FILE_ENTITY"))
    private FileEntity fileEntity;

    @Column(name = "tsrm")
    private String tsrm;

    @Enumerated(EnumType.STRING)
    @Column(name = "wl_registration_state")
    private WaitingListRegistrationStates waitingListRegistrationState;

    @Column(name = "operator")
    private String operator;

    @Column(name = "requesting_doctor")
    private String requestingDoctor;

    @Column(name = "sector_id")
    private Long sectorId;

    @Column(name = "report_result_without_tags", columnDefinition = "CLOB")
    private String reportResultWithoutTags;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cda2_file_entity_id", foreignKey = @ForeignKey(name = "FK_HIS_REP_CDA2_FILE_ENTITY"))
    private FileEntity cda2FileEntity;

    @Transient
    private String asapSectorDescription;

    @Transient
    public Boolean getCanShowProduceCDs() {
        return WaitingListRegistrationStates.REPORTED.equals(getWaitingListRegistrationState())
                || WaitingListRegistrationStates.PERFORMED.equals(getWaitingListRegistrationState())
                || WaitingListRegistrationStates.IN_READING.equals(getWaitingListRegistrationState())
                || WaitingListRegistrationStates.DRAFT.equals(getWaitingListRegistrationState());
    }

    @Transient
    private String getExamTypeDescription() {
        List<String> str = null;
        try {
            str = DaoManager.loadField(ExamType.class, "description",
                    String.class, new Criterion[]{
                            Restrictions.eq("id", getExamType())
                    });
        } catch (HibernateException | IllegalAccessException
                | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }

        if (!ValidationHelper.isNullOrEmpty(str)) {
            return str.get(0);
        }

        return null;
    }

    @Transient
    public Boolean getNeedToShowPdf() {
        if (WaitingListRegistrationStates.REPORTED.equals(this
                .getWaitingListRegistrationState())
                || WaitingListRegistrationStates.DRAFT.equals(this
                .getWaitingListRegistrationState())
                || WaitingListRegistrationStates.SIGNED.equals(this
                .getWaitingListRegistrationState())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Transient
    public Boolean getNeedToShowWeasis() {
        if (WaitingListRegistrationStates.PERFORMED.equals(this
                .getWaitingListRegistrationState())
                || WaitingListRegistrationStates.DRAFT.equals(this
                .getWaitingListRegistrationState())
                || WaitingListRegistrationStates.REPORTED.equals(this
                .getWaitingListRegistrationState())
                || WaitingListRegistrationStates.IN_READING.equals(this
                .getWaitingListRegistrationState())
                || WaitingListRegistrationStates.SIGNED.equals(this
                .getWaitingListRegistrationState())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Transient
    public DocumentGenerationHistoryWrapper getDGWrapperFromEntity() {
        DocumentGenerationHistoryWrapper dHistoryWrapper = null;

        dHistoryWrapper = new DocumentGenerationHistoryWrapper();
        dHistoryWrapper.setDate(getPerformDate());
        dHistoryWrapper.setDescription(getRadExamsLine());
        dHistoryWrapper.setFile(getFileEntity());
        dHistoryWrapper.setId(getFileEntity().getId());
        dHistoryWrapper.setDoctor(getReferringDoctor());
        dHistoryWrapper.setSectorDescription(getSectorDescription());

        if (getExamType() != null) {
            dHistoryWrapper.setExamTypeDescription(getExamType()
                    .getDescription());
        } else {
            dHistoryWrapper.setExamTypeDescription("");
        }

        dHistoryWrapper.setText(getReportResultWithoutTags());

        return dHistoryWrapper;
    }

    @Transient
    public String getRadExamsLine() {
        StringBuilder sb = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExams())) {
            for (RadiologyExam radiologyExam : getRadiologyExams()) {
                sb.append(radiologyExam.getDescription());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Transient
    public String getSectorDescription() {
        StringBuilder sb = new StringBuilder();

        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequest())) {
            sb.append(getRadiologyExamRequest().getSector().getDescription());
        } else if (getSectorId() != null) {
            try {
                sb.append(DaoManager.loadField(Sector.class, "description",
                        String.class, new Criterion[]{
                                Restrictions.eq("id", getSectorId())
                        }).get(0));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return sb.toString();
    }

    @Transient
    public String getHistoricalReportExpansionRowTitle()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        StringBuffer str = new StringBuffer(
                ResourcesHelper.getString("workListExpansionRowPart1"));
        str.append(" ");
        str.append(ResourcesHelper.getString("workListExpansionRowPart2"));
        str.append(" ");
        str.append(this.getAccessNumber() != null ? this.getAccessNumber() : "");

        return str.toString();
    }

    @Transient
    public Boolean getNeedToRenderRowExpansion() {
        if (ValidationHelper.isNullOrEmpty(this.getIdInOldDb())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public String getPatientFiscalCode() {
        return patientFiscalCode;
    }

    public void setPatientFiscalCode(String patientFiscalCode) {
        this.patientFiscalCode = patientFiscalCode;
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

    public Date getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(Date patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public Date getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(Date reserveDate) {
        this.reserveDate = reserveDate;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getAccessNumber() {
        return accessNumber;
    }

    public void setAccessNumber(String accessNumber) {
        this.accessNumber = accessNumber;
    }

    public String getReferringDoctor() {
        return referringDoctor;
    }

    public void setReferringDoctor(String referringDoctor) {
        this.referringDoctor = referringDoctor;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public List<RadiologyExam> getRadiologyExams() {
        return radiologyExams;
    }

    public void setRadiologyExams(List<RadiologyExam> radiologyExams) {
        this.radiologyExams = radiologyExams;
    }

    public String getHospitalCode() {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode) {
        this.hospitalCode = hospitalCode;
    }

    public FileEntity getFileEntity() {
        return fileEntity;
    }

    public void setFileEntity(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }

    public String getReportResult() {
        return reportResult;
    }

    public void setReportResult(String reportResult) {
        this.reportResult = reportResult;
        if (reportResult != null) {
            setReportResultWithoutTags(reportResult.replaceAll("<[^>]*>", ""));
        } else {
            setReportResultWithoutTags(null);
        }
    }

    public Long getIdInOldDb() {
        return idInOldDb;
    }

    public void setIdInOldDb(Long idInOldDb) {
        this.idInOldDb = idInOldDb;
    }

    public RadiologyExamRequest getRadiologyExamRequest() {
        return radiologyExamRequest;
    }

    @SuppressWarnings("unchecked")
    @Transient
    public String getDiagnosticQuestion() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        HistoricalReport historicalReport = DaoManager.get(this.getClass(), this.getId());
        setRadiologyExamRequest(historicalReport.getRadiologyExamRequest());
        List<String> diagnQuests = DaoManager
                .getSession()
                .createQuery(
                        "select DISTINCT ri.diagnosticQuestion from RadiologyExamRequestItem ri where ri.radiologyExamRequest.id = :parametr")
                .setParameter("parametr", getRadiologyExamRequest().getId())
                .list();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < diagnQuests.size(); ++i) {
            sb.append(diagnQuests.get(i));
            if (i != diagnQuests.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    public String getAsapSectorDescription() {
        if (ValidationHelper.isNullOrEmpty(asapSectorDescription)
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequest().getId())) {
            try {
                setAsapSectorDescription(DaoManager.getField(RadiologyExamRequest.class, "asapSectorDescription",
                        new Criterion[]{Restrictions.eq("id", getRadiologyExamRequest().getId())}, null));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return asapSectorDescription;
    }

    @Transient
    public FileEntity getFileEntityLazy() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        HistoricalReport rep = DaoManager.get(this.getClass(), this.getId());
        if (!ValidationHelper.isNullOrEmpty(rep.getFileEntity())) {
            try {
                return rep.getFileEntity();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return null;
    }

    public void setRadiologyExamRequest(
            RadiologyExamRequest radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    public String getTsrm() {
        return tsrm;
    }

    public void setTsrm(String tsrm) {
        this.tsrm = tsrm;
    }

    public WaitingListRegistrationStates getWaitingListRegistrationState() {
        return waitingListRegistrationState;
    }

    public void setWaitingListRegistrationState(
            WaitingListRegistrationStates waitingListRegistrationState) {
        this.waitingListRegistrationState = waitingListRegistrationState;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRequestingDoctor() {
        return requestingDoctor;
    }

    public void setRequestingDoctor(String requestingDoctor) {
        this.requestingDoctor = requestingDoctor;
    }

    public Date getLatestActionDate() {
        return latestActionDate;
    }

    public void setLatestActionDate(Date latestActionDate) {
        this.latestActionDate = latestActionDate;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public String getReportResultWithoutTags() {
        return reportResultWithoutTags;
    }

    public void setReportResultWithoutTags(String reportResultWithoutTags) {
        this.reportResultWithoutTags = reportResultWithoutTags;
    }

    public void setAsapSectorDescription(String asapSectorDescription) {
        this.asapSectorDescription = asapSectorDescription;
    }

    public FileEntity getCda2FileEntity() {
        return cda2FileEntity;
    }

    public void setCda2FileEntity(FileEntity cda2FileEntity) {
        this.cda2FileEntity = cda2FileEntity;
    }
}
