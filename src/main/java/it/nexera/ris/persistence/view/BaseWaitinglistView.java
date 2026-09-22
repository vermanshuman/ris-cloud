package it.nexera.ris.persistence.view;

import it.nexera.ris.common.enums.RadiologyRequestTrasportTypes;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedView;
import it.nexera.ris.persistence.beans.entities.domain.Hl7FieldsFromAsap;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.RequestNote;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Urgency;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.context.FacesContext;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MappedSuperclass
public class BaseWaitinglistView extends IndexedView {
    protected static transient final Logger log = LogManager.getLogger(BaseHelper.class);
    @Transient
    private String psdNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "wl_registration_state")
    private WaitingListRegistrationStates waitingListRegistrationState;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "FISCAL_CODE")
    private String fiscalCode;

    @Column(name = "exam_type_id")
    private Long examTypeId;

    @Column(name = "exam_type_description")
    private String examTypeDescription;

    @Column(name = "asap_sector_desctiption")
    private String asapSectorDescription;

    @Column(name = "asap_activity_line")
    private String asapActivityLine;

    @Column(name = "urgency_id")
    private Long urgencyId;

    @Column(name = "disabled_request")
    private Boolean disabledRequest;

    @Column(name = "request_date")
    private Date requestDate;

    @Column(name = "reserve_date")
    private Date reserveDate;

    @Column(name = "forwarded")
    private Boolean forwarded;

    @Column(name = "items_descr", length = 4000)
    private String itemsDescription;

    @Column(name = "hl7_fields_form_asap_id")
    private Long hl7FieldsFormAsapId;

    @Column(name = "transport_type")
    private String transportType;

    @Transient
    private boolean expanded;

    @Transient
    private List<RadiologyExamRequestItem> radiologyExamRequestItems;

    @Transient
    private List<RequestNote> reqNot;

    @Transient
    public List<RadiologyExamRequestItem> getRadiologyExamRequestItems()
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        return radiologyExamRequestItems;
    }

    public void loadLazyValues()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (getRadiologyExamRequestItems() == null) {
            List<RadiologyExamRequestItem> loadedRadiologyExamRequestItems = DaoManager
                    .load(RadiologyExamRequestItem.class, new Criterion[]{
                            Restrictions.eq("radiologyExamRequest.id",
                                    this.getId())
                    });

            radiologyExamRequestItems = new ArrayList<RadiologyExamRequestItem>();

            if (!ValidationHelper
                    .isNullOrEmpty(loadedRadiologyExamRequestItems)) {
                radiologyExamRequestItems
                        .addAll(loadedRadiologyExamRequestItems);
            }
        }

        if (reqNot == null) {
            try {
                this.reqNot = DaoManager.load(RequestNote.class,
                        new Criterion[]{
                                Restrictions.eq("radiologyExamRequest.id",
                                        this.getId())
                        });

                if (reqNot == null) {
                    reqNot = new ArrayList<RequestNote>();
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    @Transient
    public List<RequestNote> getRequestNotes() {
        return reqNot;
    }

    @Transient
    public boolean getIsReported() {
        return WaitingListRegistrationStates.REPORTED
                .equals(getWaitingListRegistrationState());
    }

    public WaitingListRegistrationStates getWaitingListRegistrationState() {
        return waitingListRegistrationState;
    }

    public void setWaitingListRegistrationState(
            WaitingListRegistrationStates waitingListRegistrationState) {
        this.waitingListRegistrationState = waitingListRegistrationState;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setRadiologyExamRequestItems(
            List<RadiologyExamRequestItem> radiologyExamRequestItems) {
        this.radiologyExamRequestItems = radiologyExamRequestItems;
    }

    @Transient
    public String getExamsDescription() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        StringBuilder sb = new StringBuilder();
        this.loadLazyValues();

        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())) {
            for (int i = 0; i < getRadiologyExamRequestItems().size() - 1; i++) {
                if (!ValidationHelper
                        .isNullOrEmpty(getRadiologyExamRequestItems().get(i))
                        && !ValidationHelper
                        .isNullOrEmpty(getRadiologyExamRequestItems()
                                .get(i).getRadiologyExam())
                        && !ValidationHelper
                        .isNullOrEmpty(getRadiologyExamRequestItems()
                                .get(i).getRadiologyExam()
                                .getDescription())) {
                    sb.append(getRadiologyExamRequestItems().get(i)
                            .getRadiologyExam().getDescription());
                    sb.append(", ");
                }
            }
            if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems()
                    .get(getRadiologyExamRequestItems().size() - 1))
                    && !ValidationHelper
                    .isNullOrEmpty(getRadiologyExamRequestItems().get(
                            getRadiologyExamRequestItems().size() - 1))
                    && !ValidationHelper
                    .isNullOrEmpty(getRadiologyExamRequestItems().get(
                            getRadiologyExamRequestItems().size() - 1)
                            .getRadiologyExam())
                    && !ValidationHelper
                    .isNullOrEmpty(getRadiologyExamRequestItems()
                            .get(getRadiologyExamRequestItems().size() - 1)

                            .getRadiologyExam().getDescription())) {
                sb.append(getRadiologyExamRequestItems()
                        .get(getRadiologyExamRequestItems().size() - 1)
                        .getRadiologyExam().getDescription());
            }
        }
        return sb.toString();
    }

    @Transient
    public String getActionBtnWaitingListTitle() {
        String actionName = null;

        if (waitingListRegistrationState != null) {
            switch (waitingListRegistrationState) {
                case REQUIRED:
                    actionName = ResourcesHelper
                            .getString("waitingListRegistrationPrenota");
                    break;
                case RESERVED:
                case ANNULLED:
                    actionName = ResourcesHelper
                            .getString("waitingListRegistrationCambia");
                    break;
                default:
                    break;
            }
        }

        return actionName;
    }

    @Transient
    public String getRadiologyExamRequestItemsString()
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        return getItemsDescription();
    }

    public String[] getItems() {
        return ValidationHelper.isNullOrEmpty(getItemsDescription())
                ? new String[0] : getItemsDescription().split(", ");
    }

    @Transient
    public Boolean getHavePSDNumber() {
        if (ValidationHelper.isNullOrEmpty(getPsdNumber())
                && getHl7FieldsFormAsapId() != null) {
            try {
                setPsdNumber(
                        DaoManager.getField(Hl7FieldsFromAsap.class,
                                "psdNumber", new Criterion[]{
                                        Restrictions.eq("id",
                                                this.getHl7FieldsFormAsapId())
                                }, null));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return !ValidationHelper.isNullOrEmpty(getPsdNumber());
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

    private boolean checkState(boolean isState,
                               WaitingListRegistrationStates... states) {
        if (states != null && states.length >= 0) {
            if (this.getWaitingListRegistrationState() != null) {
                boolean contains = ArrayUtils.contains(states,
                        this.getWaitingListRegistrationState());
                return isState ? contains : !contains;
            }
        }
        return false;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean getSelectedAll() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (this.getRadiologyExamRequestItems() != null) {
            for (RadiologyExamRequestItem radiologyExamRequestItem : this
                    .getRadiologyExamRequestItems()) {
                if (!Boolean.TRUE
                        .equals(radiologyExamRequestItem.getSelected())) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAll(boolean selectedAll) throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (this.getRadiologyExamRequestItems() != null) {
            for (RadiologyExamRequestItem radiologyExamRequestItem : this
                    .getRadiologyExamRequestItems()) {
                radiologyExamRequestItem.setSelected(selectedAll);
            }
        }
    }

    public Long getUrgencyId() {
        return urgencyId;
    }

    public void setUrgencyId(Long urgencyId) {
        this.urgencyId = urgencyId;
    }

    @Transient
    public String getAsapSectorWithActivity() {
        if (!ValidationHelper.isNullOrEmpty(this.getAsapActivityLine())) {
            return this.getAsapActivityLine() + " - "
                    + this.getAsapSectorDescription();
        } else {
            return this.getAsapSectorDescription();
        }
    }

    public String getAsapSectorDescription() {
        return asapSectorDescription;
    }

    public void setAsapSectorDescription(String asapSectorDescription) {
        if (!ValidationHelper.isNullOrEmpty(asapSectorDescription)) {
            this.asapSectorDescription = asapSectorDescription;
        }
    }

    public String getExamTypeDescription() {
        return examTypeDescription;
    }

    public void setExamTypeDescription(String examTypeDescription) {
        this.examTypeDescription = examTypeDescription;
    }

    @Transient
    public Urgency getUrgency() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getUrgencyId())) {
            Urgency urgency = DaoManager
                    .get(Urgency.class, this.getUrgencyId());
            return urgency;
        }
        return null;
    }

    @Transient
    public String getNewRowClass() {
        @SuppressWarnings("unchecked")
        List<Long> listIdsRequestItems = (List<Long>) (FacesContext
                .getCurrentInstance().getExternalContext().getSessionMap()
                .get("listIdsRequestItems"));

        if (!ValidationHelper.isNullOrEmpty(listIdsRequestItems)) {
            if (listIdsRequestItems.contains(this.getId())) {
                return "ui-state-highlight";
            }
        } else {
            return "";
        }
        return "";
    }

    public String getAsapActivityLine() {
        return asapActivityLine;
    }

    public void setAsapActivityLine(String asapActivityLine) {
        this.asapActivityLine = asapActivityLine;
    }

    public String getPsdNumber() {
        return psdNumber;
    }

    public void setPsdNumber(String psdNumber) {
        this.psdNumber = psdNumber;
    }

    public Long getExamTypeId() {
        return examTypeId;
    }

    public void setExamTypeId(Long examTypeId) {
        this.examTypeId = examTypeId;
    }

    public Boolean getDisabledRequest() {
        return disabledRequest;
    }

    public void setDisabledRequest(Boolean disabledRequest) {
        this.disabledRequest = disabledRequest;
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

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
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

    public Long getHl7FieldsFormAsapId() {
        return hl7FieldsFormAsapId;
    }

    public void setHl7FieldsFormAsapId(Long hl7FieldsFormAsapId) {
        this.hl7FieldsFormAsapId = hl7FieldsFormAsapId;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    @Transient
    public String getTransport() {
        if (StringUtils.isNotBlank(getTransportType())) {
            if(getTransportType().equals(RadiologyRequestTrasportTypes.DEAMBULANTE.name()))
                return "Deambulante";
            else if(getTransportType().equals(RadiologyRequestTrasportTypes.A_LETTO.name()))
                return "A letto";
            else if(getTransportType().equals(RadiologyRequestTrasportTypes.BARELLA.name()))
                return  "Barella";
            else if(getTransportType().equals(RadiologyRequestTrasportTypes.IN_REPARTO.name()))
                return "In reparto";
            else if(getTransportType().equals(RadiologyRequestTrasportTypes.SEDIA_A_ROTELLE.name()))
                return "Sedia a rotelle";
        }
        return null;
    }
}
