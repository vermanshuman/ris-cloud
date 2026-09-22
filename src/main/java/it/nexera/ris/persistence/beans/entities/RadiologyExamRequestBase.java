package it.nexera.ris.persistence.beans.entities;

import it.nexera.ris.common.enums.Hl7RequestSendingStatus;
import it.nexera.ris.common.enums.RadiologyRequestTrasportTypes;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Hospital;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Urgency;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@MappedSuperclass
public abstract class RadiologyExamRequestBase extends IndexedEntity implements Cloneable {

    protected static transient final Logger log = LogManager.getLogger(RadiologyExamRequest.class);

    @Transient
    protected final String ID_IN_SESSION = "RadiologyExamRequestsItemsIdsForTads";

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "sort_name")
    private String sortName;

    @Enumerated(EnumType.STRING)
    @Column(name = "wl_registration_state")
    private WaitingListRegistrationStates waitingListRegistrationState;

    @Column(name = "undo_performed")
    private Boolean undoPerformed;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    private RadiologyRequestTrasportTypes trasportType;


    @Column(name = "delete_motivation")
    private String deleteMotivation;

    @Column(name = "predefined_sector")
    private Boolean predefinedSector;

    @ManyToOne
    @JoinColumn(name = "urgency_id")
    private Urgency urgency;

    @ManyToOne
    @JoinColumn(name = "exam_type_id")
    private ExamType examType;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "latest_action_date")
    private Date latestActionDate;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @Column(name = "sending_status")
    @Enumerated(EnumType.STRING)
    private Hl7RequestSendingStatus sendingStatus;

    @Column(name = "user_closing_report_id")
    private Long userClosingReportId;

    @Column(name = "last_hl7_orm_sender_user_id")
    private Long lastHl7OrmSenderUserId;

    @Column(name = "last_pdf_url")
    private String lastPdfUrl;

    // ASAP SIDE
    @Column(name = "asap_sector_id")
    private Long asapSectorId;

    @Column(name = "asap_sector_code")
    private String asapSectorCode;

    @Column(name = "asap_sector_desctiption")
    private String asapSectorDescription;

    @Column(name = "asap_activity_line")
    private String asapActivityLine;

    @ManyToOne
    @JoinColumn(name = "hl7_fields_form_asap_id")
    private Hl7FieldsFromAsap hl7FieldsFromAsap;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @Column(name = "sector_id", insertable = false, updatable = false)
    private Long sectorId;

    @Column(name = "blocking_date")
    private Date blockingDate;

    @Transient
    private boolean expanded;

    @Transient
    private Boolean selected;

    @Column(name = "modified_exam_types_text")
    private String modifiedExamTypesText;

    @Column(name = "disabled_request")
    private Boolean disabledRequest;

    @Column(name = "modified_exams_text", columnDefinition = "NUMBER(1) DEFAULT 0")
    private Boolean modifiedExamsText;

    // field from radiology exam request items

    @Column(name = "request_date")
    private Date requestDate;

    @Column(name = "reserve_date")
    private Date reserveDate;

    @Column(name = "referring_doctor")
    private String referringDoctor;

    @Column(name = "perform_date")
    private Date performDate;

    private Boolean forwarded;

    @Column(name = "items_descr", length = 4000)
    private String itemsDescription;

    @Column(name = "latest_action_perform_date")
    private Date latestActionPerformDate;

    @Column(name = "assign_user_id")
    private Long assignUserId;

    @Column(name = "assign_date")
    private Date assignDate;

    @Column(name = "rep_hl7_state")
    @Enumerated(EnumType.STRING)
    private Hl7RequestSendingStatus repoSendingStatus;

    @Column(name = "rep_send_date")
    private Date repoSendingDate;

    @Column(name = "rep_error")
    private String repoError;

    public String getExamTypeDescriptionForTag() {
        if (getExamType() != null) {
            return getExamType().getDescription();
        } else {
            return "";
        }
    }

    public abstract List<RequestNote> getRequestNotes();

    public abstract List<? extends RadiologyExamRequestItemBase> getRadiologyExamRequestItems();

    @Transient
    public String getCurrentTime() {
        return DateTimeHelper.ToStringTimeWithSeconds(new Date());
    }

    protected String generateFormatString(List<String> strs) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < strs.size(); ++i) {
            if (strs.get(i) != null) {
                if (i != 0 && sb.length() > 0) {
                    sb.append(" - ");
                }

                sb.append(strs.get(i));
            }
        }

        return sb.toString();
    }

    @Transient
    public String getAsapSectorWithActivity() {
        if (!ValidationHelper.isNullOrEmpty(this.getAsapActivityLine())) {
            return this.getAsapActivityLine() + " - " + this.getAsapSectorDescription();
        } else {
            return this.getAsapSectorDescription();
        }
    }

    @Transient
    public boolean getShowDateWithHour() {
        return checkState(true, WaitingListRegistrationStates.REQUIRED,
                WaitingListRegistrationStates.RESERVED);
    }

    private boolean checkState(boolean isState, WaitingListRegistrationStates... states) {
        if (states != null) {
            if (this.getWaitingListRegistrationState() != null) {
                boolean contains = ArrayUtils.contains(states, this.getWaitingListRegistrationState());
                return isState == contains;
            }
        }
        return false;
    }

    public boolean getRenderAssign() {
        return checkState(false, WaitingListRegistrationStates.DELETED);
    }

    public boolean getRenderInstant() {
        return checkState(false, WaitingListRegistrationStates.DELETED);
    }

    public boolean getRenderCancel() {
        return checkState(false, WaitingListRegistrationStates.DELETED);
    }

    public boolean getRenderUndo() {
        return checkState(true, WaitingListRegistrationStates.RESERVED);
    }

    public boolean getRenderRestore() {
        return checkState(true, WaitingListRegistrationStates.DELETED);
    }

    public boolean getRenderPdf() {
        return checkState(true, WaitingListRegistrationStates.DELETED,
                WaitingListRegistrationStates.ANNULLED);
    }

    public boolean getRenderAnnulateBtnOnWorklist() {
        return WaitingListRegistrationStates.PERFORMED.equals(this.getWaitingListRegistrationState())
                || WaitingListRegistrationStates.PERFORMED.equals(this.getWaitingListRegistrationState());
    }

    public boolean getDisableCancelAndModifiBtnOnWorklist() {
        return !WaitingListRegistrationStates.RESERVED.equals(this.getWaitingListRegistrationState())
                && !WaitingListRegistrationStates.ACCEPTED.equals(this.getWaitingListRegistrationState());
    }

    public boolean getNewDate() {
        return WaitingListRegistrationStates.REQUIRED.equals(this.getWaitingListRegistrationState());
    }

    @Transient
    public String getPatientFullname() {
        return String.format("%s %s", this.getPatient().getName() == null ? "" : this.getPatient().getName(),
                this.getPatient().getSurname() == null ? "" : this.getPatient().getSurname());
    }

    public String getPatientSurnameName() {
        return String.format("%s %s", this.getPatient().getSurname() == null ? "" : this.getPatient().getSurname(),
                this.getPatient().getName() == null ? "" : this.getPatient().getName());
    }

    @Transient
    public String getStateColor() {
        String color = null;

        if (waitingListRegistrationState != null) {
            switch (waitingListRegistrationState) {
                case RESERVED:
                    color = "#C5C5C5";
                    break;
                case ACCEPTED:
                    color = "#17CDE6";
                    break;
                case PERFORMED:
                    color = "#0DBA13";
                    break;
                case SUSPENDED:
                    color = "#1B80FC";
                    break;
                case ANNULLED:
                    color = "#9B9B9B";
                    break;
                case DRAFT:
                    color = "#db9aff";
                    break;
                case DELETED:
                    color = "#FF3737";
                    break;
                case REPORTED:
                    color = "#727EFF";
                    break;
                case REQUIRED:
                    color = "#FF77D8";
                    break;
                case SIGNED:
                    color = "#FF0A84";
                    break;
                case PARTIALLY_ACCEPTED:
                    color = "#11E68D";
                    break;
                case IN_READING:
                    color = "#2E63C7";
                    break;
                default:
                    break;
            }
        }

        return color;
    }

    @Transient
    public String getActionBtnTitle() {
        String actionName = null;

        if (waitingListRegistrationState != null) {
            switch (waitingListRegistrationState) {
                case RESERVED:
                    actionName = ResourcesHelper.getString("workListActionAccept");
                    break;
                case ACCEPTED:
                    actionName = ResourcesHelper.getString("workListActionPerform");
                    break;
                case IN_READING:
                case PERFORMED:
                    actionName = ResourcesHelper.getString("workListActionReport");
                    break;
                case DELETED:
                case REPORTED:
                    actionName = ResourcesHelper.getString("workListActionView");
                    break;
                case DRAFT:
                    actionName = ResourcesHelper.getString("workListActionDraft");
                    break;
                case SIGNED:
                    actionName = ResourcesHelper.getString("workListActionReport");
                    break;
                /*
                 * this cases will described later case SUSPENDED: actionName =
                 * ResourcesHelper .getString("workListActionAccept"); break; case
                 * SIGNED: actionName = ResourcesHelper
                 * .getString("workListActionAccept"); break;
                 */
                default:
                    break;
            }
        }

        return actionName;
    }

    @Transient
    public String getActionBtnWaitingListTitle() {
        String actionName = null;

        if (waitingListRegistrationState != null) {
            switch (waitingListRegistrationState) {
                case REQUIRED:
                    actionName = ResourcesHelper.getString("waitingListRegistrationPrenota");
                    break;
                case RESERVED:
                case ANNULLED:
                    actionName = ResourcesHelper.getString("waitingListRegistrationCambia");
                    break;
                default:
                    break;
            }
        }

        return actionName;
    }

    public String getAssignUsername() {
        if (this.getAssignUserId() != null) {
            try {
                List<Object> usernames = DaoManager.getFields(User.class, new Criterion[]{
                        Restrictions.eq("id", this.getAssignUserId())
                }, null, false, "lastName", "firstName");

                if (!ValidationHelper.isNullOrEmpty(usernames)
                        && !ValidationHelper.isNullOrEmpty(((Object[]) usernames.get(0))[0])
                        && !ValidationHelper.isNullOrEmpty(((Object[]) usernames.get(0))[1])) {
                    return String.format("%s %s", ((Object[]) usernames.get(0))[0], ((Object[]) usernames.get(0))[1]);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return "";
    }

    public Boolean getSelected() {
        return selected != null ? selected : Boolean.TRUE;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Hl7RequestSendingStatus getSendingStatus() {
        return sendingStatus == null ? Hl7RequestSendingStatus.NOT_SENT : sendingStatus;
    }

    public void setSendingStatus(Hl7RequestSendingStatus sendingStatus) {
        this.sendingStatus = sendingStatus;
    }

    public String getLastPdfUrl() {
        return lastPdfUrl == null ? "" : lastPdfUrl;
    }

    public void setLastPdfUrl(String lastPdfUrl) {
        this.lastPdfUrl = lastPdfUrl;
    }

    @Transient
    public Boolean getHavePSDNumber() {
        if (!ValidationHelper.isNullOrEmpty(getHl7FieldsFromAsap())
                && !ValidationHelper.isNullOrEmpty(getHl7FieldsFromAsap().getPsdNumber())) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Transient
    public String getPsdNumberForTag() {
        if (this.getHl7FieldsFromAsap() != null) {
            return this.getHl7FieldsFromAsap().getPsdNumber();
        }
        return "";
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public WaitingListRegistrationStates getWaitingListRegistrationState() {
        return waitingListRegistrationState;
    }

    public void setWaitingListRegistrationState(WaitingListRegistrationStates waitingListRegistrationState) {
        this.waitingListRegistrationState = waitingListRegistrationState;
    }

    public Boolean getUndoPerformed() {
        return undoPerformed;
    }

    public void setUndoPerformed(Boolean undoPerformed) {
        this.undoPerformed = undoPerformed;
    }

    public RadiologyRequestTrasportTypes getTrasportType() {
        return trasportType;
    }

    public void setTrasportType(RadiologyRequestTrasportTypes trasportType) {
        this.trasportType = trasportType;
    }

    public String getDeleteMotivation() {
        return deleteMotivation;
    }

    public void setDeleteMotivation(String deleteMotivation) {
        this.deleteMotivation = deleteMotivation;
    }

    public Boolean getPredefinedSector() {
        return predefinedSector;
    }

    public void setPredefinedSector(Boolean predefinedSector) {
        this.predefinedSector = predefinedSector;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Date getLatestActionDate() {
        return latestActionDate;
    }

    public void setLatestActionDate(Date latestActionDate) {
        this.latestActionDate = latestActionDate;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public Long getUserClosingReportId() {
        return userClosingReportId;
    }

    public void setUserClosingReportId(Long userClosingReportId) {
        this.userClosingReportId = userClosingReportId;
    }

    public Long getLastHl7OrmSenderUserId() {
        return lastHl7OrmSenderUserId;
    }

    public void setLastHl7OrmSenderUserId(Long lastHl7OrmSenderUserId) {
        this.lastHl7OrmSenderUserId = lastHl7OrmSenderUserId;
    }

    public Long getAsapSectorId() {
        return asapSectorId;
    }

    public void setAsapSectorId(Long asapSectorId) {
        this.asapSectorId = asapSectorId;
    }

    public String getAsapSectorCode() {
        return asapSectorCode;
    }

    public void setAsapSectorCode(String asapSectorCode) {
        this.asapSectorCode = asapSectorCode;
    }

    public String getAsapSectorDescription() {
        return asapSectorDescription;
    }

    public void setAsapSectorDescription(String asapSectorDescription) {
        this.asapSectorDescription = asapSectorDescription;
    }

    public String getAsapActivityLine() {
        return asapActivityLine;
    }

    public void setAsapActivityLine(String asapActivityLine) {
        this.asapActivityLine = asapActivityLine;
    }

    public Hl7FieldsFromAsap getHl7FieldsFromAsap() {
        return hl7FieldsFromAsap;
    }

    public void setHl7FieldsFromAsap(Hl7FieldsFromAsap hl7FieldsFromAsap) {
        this.hl7FieldsFromAsap = hl7FieldsFromAsap;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public Date getBlockingDate() {
        return blockingDate;
    }

    public void setBlockingDate(Date blockingDate) {
        this.blockingDate = blockingDate;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getModifiedExamTypesText() {
        return modifiedExamTypesText;
    }

    public void setModifiedExamTypesText(String modifiedExamTypesText) {
        this.modifiedExamTypesText = modifiedExamTypesText;
    }

    public Boolean getDisabledRequest() {
        return disabledRequest;
    }

    public void setDisabledRequest(Boolean disabledRequest) {
        this.disabledRequest = disabledRequest;
    }

    public Boolean getModifiedExamsText() {
        return modifiedExamsText;
    }

    public void setModifiedExamsText(Boolean modifiedExamsText) {
        this.modifiedExamsText = modifiedExamsText;
    }

    public Date getRequestDate() {
        return requestDate;
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

    public String getReferringDoctor() {
        return referringDoctor;
    }

    public void setReferringDoctor(String referringDoctor) {
        this.referringDoctor = referringDoctor;
    }

    public Date getPerformDate() {
        return performDate;
    }

    public void setPerformDate(Date performDate) {
        this.performDate = performDate;
    }

    public Boolean getForwarded() {
        return forwarded;
    }

    public void setForwarded(Boolean forwarded) {
        this.forwarded = forwarded;
    }

    public String getItemsDescription() {
        return itemsDescription;
    }

    public void setItemsDescription(String itemsDescription) {
        this.itemsDescription = itemsDescription;
    }

    public Date getLatestActionPerformDate() {
        return latestActionPerformDate;
    }

    public void setLatestActionPerformDate(Date latestActionPerformDate) {
        this.latestActionPerformDate = latestActionPerformDate;
    }

    public Long getAssignUserId() {
        return assignUserId;
    }

    public void setAssignUserId(Long assignUserId) {
        this.assignUserId = assignUserId;
    }

    public Date getAssignDate() {
        return assignDate;
    }

    public void setAssignDate(Date assignDate) {
        this.assignDate = assignDate;
    }

    public Hl7RequestSendingStatus getRepoSendingStatus() {
        return repoSendingStatus;
    }

    public void setRepoSendingStatus(Hl7RequestSendingStatus repoSendingStatus) {
        this.repoSendingStatus = repoSendingStatus;
    }

    public Date getRepoSendingDate() {
        return repoSendingDate;
    }

    public void setRepoSendingDate(Date repoSendingDate) {
        this.repoSendingDate = repoSendingDate;
    }

    public String getRepoError() {
        return repoError;
    }

    public void setRepoError(String repoError) {
        this.repoError = repoError;
    }

    @Override
    public String toString() {
        return  "\"RadiologyExamRequestBase\":{" +
                super.toString() +
                ",\r\n \"sortName\":'" + sortName + '\'' +
                ",\r\n \"waitingListRegistrationState\":" + waitingListRegistrationState +
                ",\r\n \"undoPerformed\":" + undoPerformed +
                ",\r\n \"trasportType\":" + trasportType +
                ",\r\n \"deleteMotivation\":'" + deleteMotivation + '\'' +
                ",\r\n \"predefinedSector\":" + predefinedSector +
                ",\r\n \"urgency\":" + urgency +
                ",\r\n \"examType\":" + examType +
                ",\r\n \"patient\":" + patient +
                ",\r\n \"latestActionDate\":" + latestActionDate +
                ",\r\n \"hospital\":" + hospital +
                ",\r\n \"sendingStatus\":" + sendingStatus +
                ",\r\n \"userClosingReportId\":" + userClosingReportId +
                ",\r\n \"lastHl7OrmSenderUserId\":" + lastHl7OrmSenderUserId +
                ",\r\n \"lastPdfUrl\":'" + lastPdfUrl + '\'' +
                ",\r\n \"asapSectorId\":" + asapSectorId +
                ",\r\n \"asapSectorCode\":'" + asapSectorCode + '\'' +
                ",\r\n \"asapSectorDescription\":'" + asapSectorDescription + '\'' +
                ",\r\n \"asapActivityLine\":'" + asapActivityLine + '\'' +
                ",\r\n \"hl7FieldsFromAsap\":" + hl7FieldsFromAsap +
                ",\r\n \"sector\":" + sector +
                ",\r\n \"sectorId\":" + sectorId +
                ",\r\n \"blockingDate\":" + blockingDate +
                ",\r\n \"expanded\":" + expanded +
                ",\r\n \"selected\":" + selected +
                ",\r\n \"modifiedExamTypesText\":'" + modifiedExamTypesText + '\'' +
                ",\r\n \"disabledRequest\":" + disabledRequest +
                ",\r\n \"modifiedExamsText\":" + modifiedExamsText +
                ",\r\n \"requestDate\":" + requestDate +
                ",\r\n \"reserveDate\":" + reserveDate +
                ",\r\n \"referringDoctor\":'" + referringDoctor + '\'' +
                ",\r\n \"performDate\":" + performDate +
                ",\r\n \"forwarded\":" + forwarded +
                ",\r\n \"itemsDescription\":'" + itemsDescription + '\'' +
                ",\r\n \"latestActionPerformDate\":" + latestActionPerformDate +
                ",\r\n \"assignUserId\":" + assignUserId +
                ",\r\n \"assignDate\":" + assignDate +
                ",\r\n \"repoSendingStatus\":" + repoSendingStatus +
                ",\r\n \"repoError\":" + repoError +
                ",\r\n \"repoSendingDate\":" + repoSendingDate +
                ",\r\n} ";
    }
}
