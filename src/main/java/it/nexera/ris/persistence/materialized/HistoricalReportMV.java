package it.nexera.ris.persistence.materialized;

import it.nexera.ris.common.annotations.CdaTag;
import it.nexera.ris.common.enums.CdaTags;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedView;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.annotations.Immutable;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Entity
@Immutable
@Table(name = "historical_report_mview")
public class HistoricalReportMV extends IndexedView {

    private static final long serialVersionUID = 3791001647347340174L;

    public transient final Logger log = LogManager.getLogger(HistoricalReportMV.class);

    private static final String[] FISCAL_CODE_PREFIXES = new String[]{"ENI", "STP"};

    private static final Base64 CODER = new Base64();

    @Column(name = "id_in_old_db")
    private Long idInOldDb;

    @Column(name = "rad_ex_request_id")
    private Long radiologyExamRequestId;

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
    @JoinColumn(name = "exam_type_id")
    private ExamType examType;

    @Column(name = "hospital_code")
    private String hospitalCode;

    @Column(name = "report_result", columnDefinition = "CLOB")
    private String reportResult;

    @Column(name = "file_entity_id")
    private Long fileEntityId;

    @Column(name = "tsrm")
    private String tsrm;

    @Enumerated(EnumType.STRING)
    @Column(name = "wl_registration_state")
    private WaitingListRegistrationStates waitingListRegistrationState;

    @Column(name = "operator")
    private String operator;

    @Column(name = "requesting_doctor")
    private String requestingDoctor;

    @Column(name = "sec_description")
    private String sectorDescription;

    @Column(name = "sector_id")
    private Long sectorId;

    @Column(name = "report_result_without_tags", columnDefinition = "CLOB")
    private String reportResultWithoutTags;

    @Column(name = "rad_ex_descr")
    private String radiologyExamDescriptionIds;

    @Transient
    private String asapSectorDescription;

    @Transient
    private FileEntity fileEntity;

    @Transient
    private String patientId;

    @Transient
    private String sexType;

    @Transient
    private RadiologyExamRequest radiologyExamRequest;

    @Transient
    private List<RadiologyExamRequestItem> radiologyExamRequestItems;

    @Transient
    private User closingUser;

    @Transient
    private Sector sector;

    @Transient
    private String userFiscalCode;

    @Transient
    private String userFirstName;

    @Transient
    private String userLastName;

    @Transient
    private String accessNumberRequest;

    @Transient
    private String sectorDescriptionRequest;

    @Transient
    private String itemDescription;

    @Transient
    private String insertPDF;

    @Transient
    private String patientCounty;

    @Transient
    private String patientCity;

    @Transient
    private String patientCap;

    @Transient
    private String patientAddress;

    @Transient
    private String patientBirthCity;

    @Transient
    private String patientBirthCityCode;

    @Transient
    private String examCode;

    @Transient
    private String examDescription;

    private void lazyLoadEntities() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestId())) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                setRadiologyExamRequest(request);
                List<RadiologyExamRequestItem> items = request.getRadiologyExamRequestItems();
                Hibernate.initialize(items);
                setRadiologyExamRequestItems(items);
                setClosingUser(DaoManager.get(User.class, request.getUserClosingReportId()));
                setSector(request.getSector());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public boolean getCanGenerateXmlCda() {
        return WaitingListRegistrationStates.REPORTED.equals(getWaitingListRegistrationState())
                || WaitingListRegistrationStates.SIGNED.equals(getWaitingListRegistrationState());
    }

    public HistoricalReport toHistoricalReportForOsirix() {
        HistoricalReport historicalReport = new HistoricalReport();

        historicalReport.setId(getId());
        historicalReport.setHospitalCode(getHospitalCode());
        historicalReport.setSectorId(getSectorId());
        historicalReport.setAccessNumber(getAccessNumber());

        return historicalReport;
    }

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
        return ValidationHelper.isNullOrEmpty(this.getIdInOldDb());
    }

    @CdaTag(CdaTags.ITEM_LIST)
    public String getItemListTag() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())) {
            String template = FileHelper.readLayoutFile("item.xml", "ReportSearch");
            StringBuilder sb = new StringBuilder();
            for (int r=0; r < getRadiologyExamRequestItems().size() ; r++) {
                RadiologyExamRequestItem item = getRadiologyExamRequestItems().get(r);
                if(examCode == null){
                    setExamCode(item.getRadiologyExam().getCode());
                }
                if(examDescription == null){
                    setExamDescription(item.getRadiologyExam().getDescription());
                }
                sb.append(template
                        .replaceAll("%EXAM_CODE%", item.getRadiologyExam().getCode())
                        .replaceAll("%EXAM_DESCRIPTION%", item.getRadiologyExam().getDescription())
                        .replaceAll("%ENTRY_ROW_ID%", "Esame" + (r+1))
                        .replaceAll("%REQUEST_PERFORM_DATE%", getPerformDateRequest()));
            }

            return sb.toString();
        }

        return "";
    }

    @CdaTag(CdaTags.REQUEST_PERFORM_DATE)
    public String getPerformDateRequest() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequest())) {
            return DateTimeHelper.toFormatedString(getRadiologyExamRequest().getPerformDate(), DateTimeHelper.XML_DATE_PATTERN);
        }

        return "";
    }

    @CdaTag(CdaTags.FIRST_ROOT_CODE)
    public String getFirstRootCode() {
        if (!ValidationHelper.isNullOrEmpty(getPatientFiscalCode())) {
            if (fiscalCodeCheck(getPatientFiscalCode())) {
                return ResourcesHelper.getString("searchGenerateXmlRootCodeSE");
            } else {
                return ResourcesHelper.getString("searchGenerateXmlRootCodeNSE");
            }
        }

        return "";
    }

    @CdaTag(CdaTags.FIRST_HOSPITAL_NAME)
    public String getFirstHospitalName() {
        if (!ValidationHelper.isNullOrEmpty(getPatientFiscalCode())) {
            if (fiscalCodeCheck(getPatientFiscalCode())) {
                return ResourcesHelper.getString("searchGenerateXmlHospitalNameSE");
            } else {
                return ResourcesHelper.getString("searchGenerateXmlHospitalNameNSE");
            }
        }

        return "";
    }

    private boolean fiscalCodeCheck(String fiscalCode) {
        for (String prefix : FISCAL_CODE_PREFIXES) {
            if (fiscalCode.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    @CdaTag(CdaTags.PATIENT_FISCAL_CODE)
    public String getPatientFiscalCode() {
        return patientFiscalCode;
    }

    public void setPatientFiscalCode(String patientFiscalCode) {
        this.patientFiscalCode = patientFiscalCode;
    }

    @CdaTag(CdaTags.PATIENT_NAME)
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    @CdaTag(CdaTags.PATIENT_SURNAME)
    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public Date getPatientBirthDate() {
        return patientBirthDate;
    }

    @CdaTag(CdaTags.PATIENT_BIRTH_DATE)
    public String getPatientBirthDateTag() {
        return DateTimeHelper.toFormatedString(getPatientBirthDate(), DateTimeHelper.XML_SHORT_DATE_PATTERN);
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

    public String getHospitalCode() {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode) {
        this.hospitalCode = hospitalCode;
    }

    @CdaTag(CdaTags.FILE_ENTITY_ID)
    public Long getFileEntityId() {
        return fileEntityId;
    }

    @CdaTag(CdaTags.FILE_ENTITY_CREATE_DATE)
    public String getFileEntityCreateDate() {
        try {
            if (getFileEntityLazy() != null) {
                return DateTimeHelper.toFormatedString(getFileEntityLazy().getCreateDate(),
                        DateTimeHelper.XML_DATE_PATTERN);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return "";
    }

    @CdaTag(CdaTags.FILE_ENTITY_VERSION)
    public Long getFileEntityVersion() {
        try {
            if (getFileEntityLazy() != null) {
                return getFileEntityLazy().getVersion();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    public void setFileEntityId(Long fileEntityId) {
        this.fileEntityId = fileEntityId;
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

    @SuppressWarnings("unchecked")
    @Transient
    public String getDiagnosticQuestion() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        List<String> diagnQuests = DaoManager
                .getSession()
                .createQuery(
                        "select DISTINCT ri.diagnosticQuestion from RadiologyExamRequestItem ri where ri.radiologyExamRequest.id = :parametr")
                .setParameter("parametr", getRadiologyExamRequestId())
                .list();
        StringBuilder sb = new StringBuilder();
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
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestId())) {
            try {
                setAsapSectorDescription(DaoManager.getField(RadiologyExamRequest.class, "asapSectorDescription",
                        new Criterion[]{Restrictions.eq("id", getRadiologyExamRequestId())}, null));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return asapSectorDescription;
    }

    @Transient
    public FileEntity getFileEntityLazy() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (fileEntity == null) {
            HistoricalReportMV rep = DaoManager.get(HistoricalReportMV.class, this.getId());
            if (!ValidationHelper.isNullOrEmpty(rep.getFileEntityId())) {
                try {
                    fileEntity = DaoManager.get(FileEntity.class, rep.getFileEntityId());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
        }
        return fileEntity;
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

    @CdaTag(CdaTags.HISTORICAL_REPORT_RESULT)
    public String getReportResultWithoutTags() {
        return reportResultWithoutTags;
    }

    public void setReportResultWithoutTags(String reportResultWithoutTags) {
        this.reportResultWithoutTags = reportResultWithoutTags;
    }

    public void setAsapSectorDescription(String asapSectorDescription) {
        this.asapSectorDescription = asapSectorDescription;
    }

    public void setSectorDescription(String sectorDescription) {
        this.sectorDescription = sectorDescription;
    }

    public String getSectorDescription() {
        return sectorDescription;
    }

    public Long getRadiologyExamRequestId() {
        return radiologyExamRequestId;
    }

    public void setRadiologyExamRequestId(Long radiologyExamRequestId) {
        this.radiologyExamRequestId = radiologyExamRequestId;
    }

    @CdaTag(CdaTags.PATIENT_ID)
    public String getPatientId() {
        if (patientId == null) {
            try {
                patientId = DaoManager.getField(Patient.class, "id", new Criterion[]{
                        Restrictions.eq("fiscalCode", getPatientFiscalCode())
                }, null);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return patientId;
    }

    public String getRadiologyExamDescriptionIds() {
        return radiologyExamDescriptionIds;
    }

    public Set<Long> getRadiologyExamDescriptionIdsAsLong() {
        HashSet<Long> ids = new HashSet<>();
        if (getRadiologyExamDescriptionIds() != null) {
            for (String id : getRadiologyExamDescriptionIds().split(";")) {
                ids.add(Long.valueOf(id));
            }
        }
        return ids;
    }

    public void setRadiologyExamDescriptionIds(String radiologyExamDescriptionIds) {
        this.radiologyExamDescriptionIds = radiologyExamDescriptionIds;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    @CdaTag(CdaTags.PATIENT_SEX_TYPE)
    public String getSexType() {
        if (sexType == null) {
            try {
                String value = DaoManager.getField(Patient.class, "sexType", new Criterion[]{
                        Restrictions.eq("fiscalCode", getPatientFiscalCode())
                }, null);
                if (!ValidationHelper.isNullOrEmpty(value)) {
                    sexType = value.substring(0, 1);
                } else {
                    sexType = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return sexType;
    }

    public void setSexType(String sexType) {
        this.sexType = sexType;
    }

    @Transient
    @CdaTag(CdaTags.PATIENT_SEX_DISPLAY_NAME)
    public String getSexTypeDisplayName() {
        String sex = getSexType();
        if ("M".equalsIgnoreCase(sex)) {
            return "MASCHIO";
        } else if ("F".equalsIgnoreCase(sex)) {
            return "FEMMINA";
        }
        return "";
    }

    public RadiologyExamRequest getRadiologyExamRequest() {
        if (radiologyExamRequest == null) {
            lazyLoadEntities();
        }

        return radiologyExamRequest;
    }

    public void setRadiologyExamRequest(RadiologyExamRequest radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    public List<RadiologyExamRequestItem> getRadiologyExamRequestItems() {
        if (radiologyExamRequestItems == null) {
            lazyLoadEntities();
        }

        return radiologyExamRequestItems;
    }

    public void setRadiologyExamRequestItems(List<RadiologyExamRequestItem> radiologyExamRequestItems) {
        this.radiologyExamRequestItems = radiologyExamRequestItems;
    }

    public User getClosingUser() {
        if (closingUser == null) {
            lazyLoadEntities();
        }

        return closingUser;
    }

    public void setClosingUser(User closingUser) {
        this.closingUser = closingUser;
    }

    public Sector getSector() {
        if (sector == null) {
            lazyLoadEntities();
        }

        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    @CdaTag(CdaTags.USER_FISCAL_CODE)
    public String getUserFiscalCode() {
        if (userFiscalCode == null) {
            if (getClosingUser() != null) {
                userFiscalCode = getClosingUser().getFiscalCode();
            } else {
                userFiscalCode = "";
            }
        }

        return userFiscalCode;
    }

    public void setUserFiscalCode(String userFiscalCode) {
        this.userFiscalCode = userFiscalCode;
    }

    @CdaTag(CdaTags.USER_FIRST_NAME)
    public String getUserFirstName() {
        if (userFirstName == null) {
            if (getClosingUser() != null) {
                userFirstName = getClosingUser().getFirstName();
            } else {
                userFirstName = "";
            }
        }

        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    @CdaTag(CdaTags.USER_LAST_NAME)
    public String getUserLastName() {
        if (userLastName == null) {
            if (getClosingUser() != null) {
                userLastName = getClosingUser().getLastName();
            } else {
                userLastName = "";
            }
        }

        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    @CdaTag(CdaTags.REQUEST_ACCESS_NUMBER)
    public String getAccessNumberRequest() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())) {
            List<String> accessNumbers = new ArrayList<>();

            for (RadiologyExamRequestItem item : getRadiologyExamRequestItems()) {
                if (!accessNumbers.contains(item.getAccessNumber())) {
                    accessNumbers.add(item.getAccessNumber());
                }
            }

            accessNumberRequest = generateFormatString(accessNumbers);
        } else {
            accessNumberRequest = "";
        }

        return accessNumberRequest;
    }

    public String generateFormatString(List<String> strs) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = strs.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next();
            sb.append(str);
            if (iterator.hasNext()) {
                sb.append(" - ");
            }
        }

        return sb.toString();
    }

    public void setAccessNumberRequest(String accessNumberRequest) {
        this.accessNumberRequest = accessNumberRequest;
    }

    @CdaTag(CdaTags.REQUEST_SECTOR_DESCRIPTION)
    public String getSectorDescriptionRequest() {
        if (sectorDescriptionRequest == null) {
            if (getSector() != null) {
                sectorDescriptionRequest = getSector().getDescription();
            } else {
                sectorDescriptionRequest = "";
            }
        }

        return sectorDescriptionRequest;
    }

    public void setSectorDescriptionRequest(String sectorDescriptionRequest) {
        this.sectorDescriptionRequest = sectorDescriptionRequest;
    }

    @CdaTag(CdaTags.REQUEST_ITEMS_DESCR)
    public String getItemDescription() {
        if (itemDescription == null) {
            if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())) {
                String template = FileHelper.readLayoutFile("item_table.xml", "ReportSearch");
                StringBuilder sb = new StringBuilder();
                for (int r=0; r < getRadiologyExamRequestItems().size() ; r++) {
                    RadiologyExamRequestItem item = getRadiologyExamRequestItems().get(r);
                    sb.append(template
                            .replaceAll("%ITEM_ROW_ID%", "Esame" + (r+1))
                            .replaceAll("%ITEM_ROW_DESC%", "Esame_descrizione" + (r+1))
                            .replaceAll("%ITEM_ROW_DATE%", "Esame_data_esecuzione" + (r+1))
                            .replaceAll("%EXAM_ITEM_DESC%", item.getRadiologyExam().getDescription())
                            .replaceAll("%REQUEST_PERFORM_DATE%", getPerformDateRequest()));
                }
                itemDescription = sb.toString();
            }else
                itemDescription = "";
        }

        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    @CdaTag(CdaTags.INSERT_PDF)
    public String getInsertPDF() {
        try {
            if (!ValidationHelper.isNullOrEmpty(this.getFileEntityId())) {
                File file = new File(this.getFileEntityLazy().getPath());
                byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
                return CODER.encodeAsString(bytes);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return "";
    }

    public void setInsertPDF(String insertPDF) {
        this.insertPDF = insertPDF;
    }


    @CdaTag(CdaTags.PATIENT_COUNTY)
    public String getPatientCounty() {
        if (patientCounty == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());

                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient) && !ValidationHelper.isNullOrEmpty(patient.getProvince())){
                        patientCounty = patient.getProvince().getCode();
                    }
                }
                if(patientCounty == null){
                    patientCounty = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientCounty;
    }

    public void setPatientCounty(String patientCounty) {
        this.patientCounty = patientCounty;
    }

    @CdaTag(CdaTags.PATIENT_CITY)
    public String getPatientCity() {
        if (patientCity == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)){
                        patientCity = patient.getCityDescription();
                    }
                }
                if(patientCity == null){
                    patientCity = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientCity;
    }

    public void setPatientCity(String patientCity) {
        this.patientCity = patientCity;
    }

    @CdaTag(CdaTags.PATIENT_CAP)
    public String getPatientCap() {
        if (patientCap == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)){
                        patientCap = patient.getCap();
                    }
                }
                if(patientCap == null){
                    patientCap = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientCap;
    }

    public void setPatientCap(String patientCap) {
        this.patientCap = patientCap;
    }

    @CdaTag(CdaTags.PATIENT_ADDRESS)
    public String getPatientAddress() {
        if (patientAddress == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)){
                        patientAddress = patient.getAddress();
                    }
                }
                if(patientAddress == null){
                    patientAddress = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    @CdaTag(CdaTags.PATIENT_BIRTH_CITY)
    public String getPatientBirthCity() {
        if (patientBirthCity == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)){
                        patientBirthCity = patient.getBirthCityDescription();
                    }
                }
                if(patientBirthCity == null){
                    patientBirthCity = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientBirthCity;
    }

    public void setPatientBirthCity(String patientBirthCity) {
        this.patientBirthCity = patientBirthCity;
    }

    @CdaTag(CdaTags.PATIENT_BIRTH_CITY_CODE)
    public String getPatientBirthCityCode() {
        if (patientBirthCityCode == null) {
            try {
                RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequestId());
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getPatient())){
                    Patient patient = request.getPatient();
                    if(!ValidationHelper.isNullOrEmpty(patient)
                            && !ValidationHelper.isNullOrEmpty(patient.getBirthCity())){
                        patientBirthCityCode = patient.getBirthCity().getCode();
                    }
                }
                if(patientBirthCityCode == null){
                    patientBirthCityCode = "";
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return patientBirthCityCode;
    }

    public void setPatientBirthCityCode(String patientBirthCityCode) {
        this.patientBirthCityCode = patientBirthCityCode;
    }

    @CdaTag(CdaTags.EXAM_CODE)
    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    @CdaTag(CdaTags.EXAM_DESCRIPTION)
    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
    }

    public void setFileEntity(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }

    @CdaTag(CdaTags.SECOND_ROOT_CODE)
    public String getSecondRootCode() {
        return ResourcesHelper.getString("searchGenerateXmlSecondCodeSE");
    }

    @CdaTag(CdaTags.EXTERNAL_PATIENT_CODE)
    public String getExternalPatientCode() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getPatientId())){
            Patient patient = DaoManager.get(Patient.class, new Criterion[]{
                    Restrictions.eq("id", patientId)
            });
            if(!ValidationHelper.isNullOrEmpty(patient) && !ValidationHelper.isNullOrEmpty(patient.getExternalPatientId()))
                return patient.getExternalPatientId();
            else
                return getPatientFiscalCode();
        }
        return "";
    }

    @CdaTag(CdaTags.CONFIDENTIALITY_CODE)
    public String getConfidentialityCode() {
        return Boolean.TRUE.equals(getRadiologyExamRequest().getDocumentHidden()) ? ResourcesHelper.getString("searchGenerateXmlHiddenDocument")
                : ResourcesHelper.getString("searchGenerateXmlNotHiddenDocument");
    }

    @Transient
    @CdaTag(CdaTags.IN_FULFILLMENT_OF_ELECTRONIC_RECIPE)
    public String getInFulfillmentOfElectronicRecipe() {
        String recipeNumber = getRadiologyExamRequest() != null
                ? getRadiologyExamRequest().getElectronicRecipeNumber() : null;
        if (ValidationHelper.isNullOrEmpty(recipeNumber)) {
            return "";
        }
        return "    <inFulfillmentOf>\n"
                + "        <order classCode=\"ACT\" moodCode=\"RQO\">\n"
                + "            <id " + String.format(ResourcesHelper.getString("searchGenerateXmlIdTagForElectronicRecipe"), recipeNumber) + " />\n"
                + "            <priorityCode code=\"R\" codeSystem=\"2.16.840.1.113883.5.7\" codeSystemName=\"HL7 ActPriority\" displayName=\"Normale\"/>\n"
                + "        </order>\n"
                + "    </inFulfillmentOf>\n";
    }
}
