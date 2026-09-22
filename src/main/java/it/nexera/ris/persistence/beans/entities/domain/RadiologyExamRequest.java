package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ConservationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.logic.GenerationTagsHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestBase;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestItemWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import org.hibernate.HibernateException;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "radiology_exam_request")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "RAD_EXAM_REQUEST_SEQ", allocationSize = 1)
public class RadiologyExamRequest extends RadiologyExamRequestBase {

    private static final long serialVersionUID = 5934638741054310728L;

    @OneToMany(mappedBy = "radiologyExamRequest", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    private List<RadiologyExamRequestItem> radiologyExamRequestItems;

    @OneToMany(mappedBy = "radiologyExamRequest", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<RequestTag> requestTags;

    @OneToMany(mappedBy = "radiologyExamRequest", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<RequestAttachedFile> requestAttachedFiles;

    @Column(name = "COUNT_REPO_HL7", nullable = false, columnDefinition = "int default 0")
    private int countRepoHL7;

    @Column(name="CDA2_STATE")
    private String cda2State;

    @Column(name = "CDA2_SIGN_DATE")
    private Date cda2SignDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "conservation_state")
    private ConservationStates conservationState;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cons_doc_id", foreignKey = @ForeignKey(name = "fk_rad_ex_req_cons_doc_file_entity"))
    private FileEntity consFileEntity;

    @Column(name = "cons_version", columnDefinition = "NUMBER(19,0) default 0")
    private Long consVersion;

    @Column(name="cons_response")
    private String consResponse;

    @Column(name = "electronic_recipe_number")
    private String electronicRecipeNumber;

    @Column(name = "is_document_hidden")
    private Boolean isDocumentHidden;

    @Column(name = "is_doc_hidden_tutela")
    private Boolean isDocumentSecure;

    @Column(name="conservation_send_date")
    private Date conservationSendDate;
    @Transient
    private List<RadiologyExamRequestItem> selectedRadItemsList;

    @Transient
    private List<RadiologyExamRequestItem> itemsForTag;

    @OneToMany(mappedBy = "radiologyExamRequest")
    private List<RequestNote> requestNotes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cda2_file_entity_id", foreignKey = @ForeignKey(name = "FK_RAD_EX_REQ_CDA2_FILE_ENTITY"))
    private FileEntity cda2FileEntity;

    @Override
    public List<RequestNote> getRequestNotes() {
        return requestNotes;
    }

    public void setRequestNotes(List<RequestNote> requestNotes) {
        this.requestNotes = requestNotes;
    }

    @Transient
    public String getExamsDescription() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        // this method used for DocumentGenerationTags exams_list_description
        StringBuilder sb = new StringBuilder();
        List<RadiologyExamRequestItem> items = null;
        if (!ValidationHelper.isNullOrEmpty(this.getRadiologyExamRequestItems())) {
            if (!ValidationHelper.isNullOrEmpty(this.getRadiologyExamRequestItems().get(0))
                    && !ValidationHelper.isNullOrEmpty(this.getRadiologyExamRequestItems().get(0).getFileEntity())) {
                FileEntity fileEntity = this.getRadiologyExamRequestItems().get(0).getFileEntity();
                items = DaoManager.load(RadiologyExamRequestItem.class, new Criterion[]{
                        Restrictions.eq("fileEntity", fileEntity)
                });
            } else {
                items = getRadiologyExamRequestItems();
            }

            for (int i = 0; i < items.size() - 1; i++) {
                if (!ValidationHelper.isNullOrEmpty(items.get(i).getRadiologyExam())
                        && !ValidationHelper.isNullOrEmpty(items.get(i).getRadiologyExam().getDescription())) {
                    sb.append(items.get(i).getRadiologyExam().getDescription());
                    sb.append(", ");
                }
            }
            if (!ValidationHelper.isNullOrEmpty(items.get(items.size() - 1).getRadiologyExam())
                    && !ValidationHelper.isNullOrEmpty(items.get(items.size() - 1).getRadiologyExam().getDescription())) {
                sb.append(items.get(items.size() - 1).getRadiologyExam().getDescription());
            }
        }

        return sb.toString();
    }

    public String getItemRequestDate() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0).getRequestDate() != null) {
            return DateTimeHelper.toString(getRadiologyExamRequestItems().get(0).getRequestDate());
        }

        return null;
    }

    @Transient
    public String getItemAcceptDate() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0).getAcceptDate() != null) {
            return DateTimeHelper.toString(getRadiologyExamRequestItems().get(0).getAcceptDate());
        }

        return null;
    }

    @Transient
    public String getItemExecutionDateForTag() {
        if (!ValidationHelper.isNullOrEmpty(getAllItemsForTag())) {
            StringBuffer sb = new StringBuffer();
            List<String> executionDate = new ArrayList<String>();

            for (RadiologyExamRequestItem item : getItemsForTag()) {
                if (!executionDate.contains(DateTimeHelper.toString(item.getPerformDate()))) {
                    executionDate.add(DateTimeHelper.toString(item.getPerformDate()));
                }
            }

            for (int i = 0; i < executionDate.size(); ++i) {
                if (executionDate.get(i) != null) {
                    if (i != 0 && sb.length() > 0) {
                        sb.append(" - ");
                    }

                    sb.append(executionDate.get(i));
                }
            }

            return sb.toString();
        }

        return "";
    }


    public void udateItemsDescription(List<RadiologyExamRequestItem> items)//FIXME: DELETE AFTER CREATE TRIGGER
    {
        if (!ValidationHelper.isNullOrEmpty(items)) {
            StringBuffer sb = new StringBuffer();

            for (RadiologyExamRequestItem item : items) {
                sb.append(", ");
                sb.append(item.getRadiologyExam().getDescription());
            }

            if (sb.length() > 0) {
                this.setItemsDescription(sb.toString().substring(2));
            }
        }
    }

    @Transient
    public String getItemReserveDate() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0).getReserveDate() != null) {
            return DateTimeHelper.toString(getRadiologyExamRequestItems().get(0).getReserveDate());
        }

        return null;
    }

    @Transient
    public String getItemRequestTime() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0).getRequestDate() != null) {
            return DateTimeHelper.toStringTime(getRadiologyExamRequestItems().get(0).getRequestDate());
        }

        return null;
    }

    @Transient
    public String getItemAcceptTime() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0).getAcceptDate() != null) {
            return DateTimeHelper.toStringTime(getRadiologyExamRequestItems().get(0).getAcceptDate());
        }

        return null;
    }

    @Transient
    public String getItemExecutionTime() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0).getPerformDate() != null) {
            return DateTimeHelper.toStringTime(getRadiologyExamRequestItems().get(0).getPerformDate());
        }

        return null;
    }

    @Transient
    public String getItemReserveTime() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0).getReserveDate() != null) {
            return DateTimeHelper.toStringTime(getRadiologyExamRequestItems().get(0).getReserveDate());
        }

        return null;
    }

    @Transient
    public String getItemTsrmForTag() {
        if (!ValidationHelper.isNullOrEmpty(getAllItemsForTag())) {
            List<String> trsm = new ArrayList<String>();

            for (RadiologyExamRequestItem item : getItemsForTag()) {
                if (!trsm.contains(item.getTrsm())) {
                    trsm.add(item.getTrsm());
                }
            }

            return generateFormatString(trsm);
        }

        return "";
    }

    @Transient
    public String getItemUserAccept() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0).getOperator())) {
            return getRadiologyExamRequestItems().get(0).getOperator();
        }

        return "";
    }

    @Transient
    public String getItemUserDelete() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0).getOperator())) {
            return getRadiologyExamRequestItems().get(0).getDeleteUser();
        }

        return "";
    }

    @Transient
    public String getItemUserPerform() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0).getRequestingDoctor())) {
            return getRadiologyExamRequestItems().get(0).getRequestingDoctor();
        }

        return "";
    }

    @Transient
    public String getRequestingDoctor() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0).getAccessNumber())) {
            return getRadiologyExamRequestItems().get(0).getRequestingDoctor();
        }

        return "";
    }

    @Transient
    public String getTrsm() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0).getAccessNumber())) {
            return getRadiologyExamRequestItems().get(0).getTrsm();
        }

        return "";
    }

    @Transient
    public String getAccessNumber() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0).getAccessNumber())) {
            return getRadiologyExamRequestItems().get(0).getAccessNumber();
        }

        return "";
    }

    @Transient
    public String getAccessNumberForTag() {
        if (!ValidationHelper.isNullOrEmpty(getAllItemsForTag())) {
            List<String> accessNumbers = new ArrayList<String>();

            for (RadiologyExamRequestItem item : getItemsForTag()) {
                if (!accessNumbers.contains(item.getAccessNumber())) {
                    accessNumbers.add(item.getAccessNumber());
                }
            }

            return generateFormatString(accessNumbers);
        }

        return "";
    }

    @Transient
    public String getCardNumberForTag() {
        if (!ValidationHelper.isNullOrEmpty(getAllItemsForTag())) {
            StringBuffer sb = new StringBuffer();
            List<Long> cardNumber = new ArrayList<Long>();
            List<Long> cardYear = new ArrayList<Long>();

            for (RadiologyExamRequestItem item : getItemsForTag()) {
                if (!cardNumber.contains(item.getCardNumber())) {
                    cardNumber.add(item.getCardNumber());
                    cardYear.add(item.getAccessNumberYear());
                }
            }

            for (int i = 0; i < cardNumber.size(); ++i) {
                if (cardNumber.get(i) != null) {
                    if (i != 0 && sb.length() > 0) {
                        sb.append(" - ");
                    }

                    sb.append(this.getExamType().getDescription(), 0, 2);
                    sb.append(cardYear.get(i));
                    sb.append(String.format("%06d", cardNumber.get(i)));
                }
            }

            return sb.toString();
        }

        return "";
    }

    private List<RadiologyExamRequestItem> getAllItemsForTag() {
        List<Long> ids = SessionHelper.getIds(ID_IN_SESSION);

        if (ValidationHelper.isNullOrEmpty(ids)) {
            if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                    && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0).getFileEntity())) {
                try {
                    List<RadiologyExamRequestItem> radItemsWithSameFileEntity = DaoManager
                            .load(RadiologyExamRequestItem.class, new Criterion[]{
                                    Restrictions.eq("fileEntity",
                                            getRadiologyExamRequestItems().get(0).getFileEntity())
                            });

                    setItemsForTag(radItemsWithSameFileEntity);
                } catch (HibernateException | IllegalAccessException | PersistenceBeanException e) {
                    LogHelper.log(log, e);
                }
            } else {
                setItemsForTag(this.getRadiologyExamRequestItems());
            }
        } else {
            try {
                setItemsForTag(DaoManager.load(RadiologyExamRequestItem.class, new Criterion[]{
                        Restrictions.in("id", ids)
                }));
            } catch (HibernateException | IllegalAccessException | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }

        return getItemsForTag();
    }

    @Transient
    public String getDeleteComment() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0).getDeleteComment())) {
            return getRadiologyExamRequestItems().get(0).getDeleteComment();
        }

        return "";
    }

    @Transient
    public String getCancelExamItems() {
        return GenerationTagsHelper.generatetCancelExamItemsTable(this.getRadiologyExamRequestItems(), this);
    }

    @Transient
    public String getExamsTable() {
        return GenerationTagsHelper.generateExamsTable(this.getRadiologyExamRequestItems());
    }

    @Transient
    public String getExamsAggregatedTable() {
        return GenerationTagsHelper.generateExamsAggregatedTable();
    }

    @Transient
    public String getCancelPatients() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        return GenerationTagsHelper.generatePatientTable(this.getPatient()
                .getName(), this.getPatient().getSurname(), this.getPatient()
                .getBirthDate(), this.getAsapSectorDescription(), this
                .getExamsDescription(), this.getRadiologyExamRequestItems());
    }

    @Transient
    public RadExamRequestWrapper getRadExamRequestWrapperFromRequest() {
        if (!ValidationHelper.isNullOrEmpty(this.getRadiologyExamRequestItems())) {
            RadExamRequestWrapper requestWrapper = new RadExamRequestWrapper(
                    this.getId(), this.getExamType(), this.getLatestActionDate(), null,
                    this.getPatient(), this.getRadiologyExamRequestItems().get(0).getAccessNumber(),
                    this.getWaitingListRegistrationState(), this.getUserClosingReportId(), null,
                    this.getHospital().getId(), this.getAsapSectorCode(),
                    this.getAsapSectorDescription(), this.getAsapActivityLine(), this.getSector().getId(), this.getForwarded()
            ,this.getConservationState());

            if (this.getHl7FieldsFromAsap() != null) {
                requestWrapper.setAsapPlaceOrderNumber(this.getHl7FieldsFromAsap().getPlacerOrderNumber());
            }

            List<RadExamRequestItemWrapper> radExamRequestItemWrappers = new ArrayList<RadExamRequestItemWrapper>();
            for (RadiologyExamRequestItem reri : this.getRadiologyExamRequestItems()) {
                radExamRequestItemWrappers.add(new RadExamRequestItemWrapper(
                        reri.getId(), reri.getRadiologyExam(), reri
                        .getRequestingDoctor(), reri.getReserveDate(),
                        reri.getRequestDate(), this.getId(), reri
                        .getDiagnosticQuestion(), reri
                        .getAccessNumber(), requestWrapper, reri.getAddedManually()));
            }

            requestWrapper.setRadExamRequestItemWrappers(radExamRequestItemWrappers);

            return requestWrapper;
        }

        return null;
    }

    @Override
    public List<RadiologyExamRequestItem> getRadiologyExamRequestItems() {
        return radiologyExamRequestItems;
    }

    public void setRadiologyExamRequestItems(List<RadiologyExamRequestItem> radiologyExamRequestItems) {
        this.radiologyExamRequestItems = radiologyExamRequestItems;
    }

    @Transient
    public String getRadiologyExamRequestItemsString() {
        StringBuilder sb = new StringBuilder();
        if (this.getRadiologyExamRequestItems() != null) {
            for (RadiologyExamRequestItem item : this.getRadiologyExamRequestItems()) {
                sb.append(item.getRadiologyExam().getDescription());
                sb.append(", ");
            }
            if (sb.length() > 2) {
                sb.delete(sb.length() - 2, sb.length() - 1);
            }
        }
        return sb.toString();
    }

    public boolean getSelectedAll() {
        if (this.getRadiologyExamRequestItems() != null) {
            for (RadiologyExamRequestItem radiologyExamRequestItem : this
                    .getRadiologyExamRequestItems()) {
                if (!radiologyExamRequestItem.getSelected()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAll(boolean selectedAll) {
        if (this.getRadiologyExamRequestItems() != null) {
            for (RadiologyExamRequestItem radiologyExamRequestItem : this.getRadiologyExamRequestItems()) {
                radiologyExamRequestItem.setSelected(selectedAll);
            }
        }
    }

    public List<RadiologyExamRequestItem> getSelectedRadItemsList() {
        return selectedRadItemsList;
    }

    public void setSelectedRadItemsList(List<RadiologyExamRequestItem> selectedRadItemsList) {
        this.selectedRadItemsList = selectedRadItemsList;
    }

    @Transient
    public String getDiagnosticQuestion() {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0))) {
            return getRadiologyExamRequestItems().get(0).getDiagnosticQuestion();
        }

        return "";
    }

    @Transient
    public String getPerformanceList() {
        return GenerationTagsHelper.generatetPerformanceList(this.getRadiologyExamRequestItems(), this);
    }

    @Transient
    public String getDeliveryDoseTable() {
        return GenerationTagsHelper.generateDeliveryDoseTable();
    }

    public List<RadiologyExamRequestItem> getItemsForTag() {
        return itemsForTag;
    }

    public void setItemsForTag(List<RadiologyExamRequestItem> itemsForTag) {
        this.itemsForTag = itemsForTag;
    }

    @Override
    public RadiologyExamRequest clone() throws CloneNotSupportedException {
        RadiologyExamRequest obj = new RadiologyExamRequest();

        obj.setDisplayName(getDisplayName());
        obj.setSortName(getSortName());
        obj.setWaitingListRegistrationState(getWaitingListRegistrationState());
        obj.setUndoPerformed(getUndoPerformed());
        obj.setTrasportType(getTrasportType());
        obj.setDeleteMotivation(getDeleteMotivation());
        obj.setPredefinedSector(getPredefinedSector());
        obj.setUrgency(getUrgency());

        obj.setRequestDate(getRequestDate());
        obj.setReserveDate(getReserveDate());
        obj.setReferringDoctor(getReferringDoctor());
        obj.setPerformDate(getPerformDate());
        obj.setLatestActionPerformDate(getLatestActionPerformDate());

        obj.setExamType(getExamType());
        obj.setPatient(getPatient());

        obj.setSector(getSector());

        obj.setAsapSectorCode(getAsapSectorCode());
        obj.setAsapSectorDescription(getAsapSectorDescription());
        obj.setAsapSectorId(getAsapSectorId());
        obj.setAsapActivityLine(getAsapActivityLine());

        obj.setHl7FieldsFromAsap(getHl7FieldsFromAsap());

        obj.setHospital(getHospital());
        obj.setUserClosingReportId(getUserClosingReportId());

        obj.setSendingStatus(getSendingStatus());
        obj.setForwarded(getForwarded());

        obj.setElectronicRecipeNumber(getElectronicRecipeNumber());

        return obj;
    }

    public List<RequestTag> getRequestTags() {
        return requestTags;
    }

    public void setRequestTags(List<RequestTag> requestTags) {
        this.requestTags = requestTags;
    }

    public List<RequestAttachedFile> getRequestAttachedFiles() {
        return requestAttachedFiles;
    }

    public void setRequestAttachedFiles(List<RequestAttachedFile> requestAttachedFiles) {
        this.requestAttachedFiles = requestAttachedFiles;
    }

    public int getCountRepoHL7() {
        return countRepoHL7;
    }

    public void setCountRepoHL7(int countRepoHL7) {
        this.countRepoHL7 = countRepoHL7;
    }

    public String getCda2State() {
        return cda2State;
    }

    public void setCda2State(String cda2State) {
        this.cda2State = cda2State;
    }

    public FileEntity getCda2FileEntity() {
        return cda2FileEntity;
    }

    public void setCda2FileEntity(FileEntity cda2FileEntity) {
        this.cda2FileEntity = cda2FileEntity;
    }

    public Date getCda2SignDate() {
        return cda2SignDate;
    }

    public void setCda2SignDate(Date cda2SignDate) {
        this.cda2SignDate = cda2SignDate;
    }

    @Override
    public String toString() {
        return "\"RadiologyExamRequest\":{" +
                "\r\n " + super.toString() +
                ",\r\n \"radiologyExamRequestItems\":" + radiologyExamRequestItems +
                "}";
    }

    public ConservationStates getConservationState() {
        return conservationState;
    }

    public void setConservationState(ConservationStates conservationState) {
        this.conservationState = conservationState;
    }

    public FileEntity getConsFileEntity() {
        return consFileEntity;
    }

    public void setConsFileEntity(FileEntity consFileEntity) {
        this.consFileEntity = consFileEntity;
    }

    public Long getConsVersion() {
        return consVersion;
    }

    public void setConsVersion(Long consVersion) {
        this.consVersion = consVersion;
    }

    public String getConsResponse() {
        return consResponse;
    }

    public void setConsResponse(String consResponse) {
        this.consResponse = consResponse;
    }

    public Date getConservationSendDate() {
        return conservationSendDate;
    }

    public void setConservationSendDate(Date conservationSendDate) {
        this.conservationSendDate = conservationSendDate;
    }

    public Boolean getDocumentHidden() {
        return isDocumentHidden;
    }

    public void setDocumentHidden(Boolean documentHidden) {
        isDocumentHidden = documentHidden;
    }

    public Boolean getDocumentSecure() {
        return isDocumentSecure;
    }

    public void setDocumentSecure(Boolean documentSecure) {
        isDocumentSecure = documentSecure;
    }

    public String getElectronicRecipeNumber() {
        return electronicRecipeNumber;
    }

    public void setElectronicRecipeNumber(String electronicRecipeNumber) {
        this.electronicRecipeNumber = electronicRecipeNumber;
    }
}
