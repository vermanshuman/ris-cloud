package it.nexera.ris.persistence.view;

import it.nexera.ris.common.enums.RadiologyRequestTrasportTypes;
import it.nexera.ris.common.enums.SpecialPermissionTypes;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedView;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestItemBase;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Urgency;
import it.nexera.ris.web.beans.PageBean;
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
import java.util.Iterator;
import java.util.List;

@MappedSuperclass
public class BaseWorklistView extends IndexedView {
    private static final long serialVersionUID = 6251820593344152845L;

    protected static transient final Logger log = LogManager.getLogger(BaseHelper.class);

    public static final String SELECT_PART = "AS SELECT     "

            + "p.birth_date BIRTH_DATE,  "
            + "p.name NAME,  "
            + "p.SURNAME SURNAME,  "
            + "p.cell cell,  "
            + "p.fiscal_code fiscal_code,  "
            + "p.second_phone second_phone,  "
            + "p.phone phone,  "
            + "p.province_id province_id,  "
            + "p.mail mail,  "
            + "p.citi_id citi_id,  "
            + "p.address_number address_number,  "
            + "p.birth_city_id birth_city_id,  "
            + "p.ADDRESS ADDRESS,  "
            + "p.id patient_id,  "
            + "(select count(patient_allergies.id) from patient_allergies where patient_id = p.id ) allergy_alert,  "

            + "request.ID ID,  "
            + "request.CREATE_DATE CREATE_DATE, "
            + "request.CREATE_USER_ID CREATE_USER_ID, "
            + "request.UPDATE_DATE UPDATE_DATE, "
            + "request.UPDATE_USER_ID UPDATE_USER_ID, "
            + "request.wl_registration_state wl_registration_state,  "
            + "request.exam_type_id exam_type_id,  "
            + "request.urgency_id urgency_id,  "
            + "request.asap_sector_id asap_sector_id, "
            + "request.asap_sector_desctiption asap_sector_desctiption, "
            + "request.asap_activity_line asap_activity_line, "
            + "request.forwarded forwarded,"
            + "request.hl7_fields_form_asap_id hl7_fields_form_asap_id,"

            + "request.sector_id sector_id, "
            + "request.referring_doctor referring_doctor, "
            + "request.items_descr items_descr, "
            + "request.latest_action_perform_date latest_action_perform_date, "
            + "request.assign_user_id assign_user_id, "
            + "request.assign_date assign_date, "
            + "request.reserve_date reserve_date, "
            + "request.transport_type transport_type, "

            + "dic_ex_type.description exam_type_description, "
            + "dic_ex_type.color exam_type_color, "
            + "dic_ex_type.has_color exam_type_has_color, "
            + "access_number ";

    public static final String FROM_MAIN_PART = "FROM (SELECT DISTINCT request.ID, "
            + "                      CASE "
            + "                          WHEN request_item.ACCESS_NUMBER_CODE IS NOT NULL AND "
            + "                               request_item.ACCESS_NUMBER_YEAR IS NOT NULL AND request_item.ACCESS_NUMBER_ID IS NOT NULL "
            + "                              THEN "
            + "                                  request_item.ACCESS_NUMBER_CODE || request_item.ACCESS_NUMBER_YEAR || "
            + "                                  request_item.ACCESS_NUMBER_ID "
            + "                          WHEN request_item_short.ACCESS_NUMBER_CODE IS NOT NULL AND "
            + "                               request_item_short.ACCESS_NUMBER_YEAR IS NOT NULL AND "
            + "                               request_item_short.ACCESS_NUMBER_ID IS NOT NULL THEN "
            + "                                  request_item_short.ACCESS_NUMBER_CODE || request_item_short.ACCESS_NUMBER_YEAR || "
            + "                                  request_item_short.ACCESS_NUMBER_ID "
            + "                          ELSE "
            + "                              '' "
            + "                          END access_number "
            + "      FROM RADIOLOGY_EXAM_REQUEST request "
            + "               LEFT JOIN rad_exam_request_item request_item ON request_item.RAD_EXAM_REQUEST_ID = request.id "
            + "               LEFT JOIN rad_exam_request_item_short request_item_short "
            + "                         ON request_item_short.RAD_EXAM_REQUEST_ID = request.id "
            + "      WHERE request.id IS NOT NULL) unique_requests "
            + "         JOIN RADIOLOGY_EXAM_REQUEST request ON request.ID = unique_requests.ID "
            + "         LEFT JOIN PATIENT p ON p.ID = request.patient_id "
            + "         LEFT JOIN dic_exam_type dic_ex_type ON dic_ex_type.ID = request.exam_type_id ";

    public static final String FROM_SHORT_PART = "FROM (SELECT DISTINCT request.ID, "
            + "                      CASE "
            + "                          WHEN request_item.ACCESS_NUMBER_CODE IS NOT NULL AND "
            + "                               request_item.ACCESS_NUMBER_YEAR IS NOT NULL AND request_item.ACCESS_NUMBER_ID IS NOT NULL "
            + "                              THEN "
            + "                                  request_item.ACCESS_NUMBER_CODE || request_item.ACCESS_NUMBER_YEAR || "
            + "                                  request_item.ACCESS_NUMBER_ID "
            + "                          WHEN request_item_short.ACCESS_NUMBER_CODE IS NOT NULL AND "
            + "                               request_item_short.ACCESS_NUMBER_YEAR IS NOT NULL AND "
            + "                               request_item_short.ACCESS_NUMBER_ID IS NOT NULL THEN "
            + "                                  request_item_short.ACCESS_NUMBER_CODE || request_item_short.ACCESS_NUMBER_YEAR || "
            + "                                  request_item_short.ACCESS_NUMBER_ID "
            + "                          ELSE "
            + "                              '' "
            + "                          END access_number "
            + "      FROM RADIOLOGY_EXAM_REQUEST_SHORT request "
            + "               LEFT JOIN rad_exam_request_item request_item ON request_item.RAD_EXAM_REQUEST_ID = request.id "
            + "               LEFT JOIN rad_exam_request_item_short request_item_short "
            + "                         ON request_item_short.RAD_EXAM_REQUEST_ID = request.id "
            + "      WHERE request.id IS NOT NULL) unique_requests "
            + "         JOIN RADIOLOGY_EXAM_REQUEST request ON request.ID = unique_requests.ID "
            + "         LEFT JOIN PATIENT p ON p.ID = request.patient_id "
            + "         LEFT JOIN dic_exam_type dic_ex_type ON dic_ex_type.ID = request.exam_type_id ";

    @Transient
    private String psdNumber;

    @Column(name = "referring_doctor")
    private String referringDoctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "wl_registration_state")
    private WaitingListRegistrationStates waitingListRegistrationState;

    // patient fields

    @Column(name = "patient_id")
    private Long patientId;

    @ManyToOne
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column
    private String cell;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    @Column
    private String phone;

    @Column(name = "second_phone")
    private String secondPhone;

    @Column(name = "province_id")
    private Long provinceId;

    @Column
    private String mail;

    @Column(name = "citi_id")
    private Long cityId;

    @Column(name = "address_number")
    private String addressNumber;

    @Column
    private String address;

    @Column(name = "birth_city_id")
    private Long birthCityId;

    @Column(name = "exam_type_id")
    private Long examTypeId;

    @Column(name = "exam_type_description")
    private String examTypeDescription;

    @Column(name = "exam_type_color")
    private String examTypeColor;

    @Column(name = "exam_type_has_color")
    private Boolean examTypeHasColor;

    @Column(name = "asap_sector_id")
    private Long asapSectorId;

    @Column(name = "asap_sector_desctiption")
    private String asapSectorDescription;

    @Column(name = "asap_activity_line")
    private String asapActivityLine;

    @Column(name = "urgency_id")
    private Long urgencyId;

    @Column(name = "sector_id")
    private Long sectorId;

    @Column(name = "hl7_fields_form_asap_id")
    private Long hl7FieldsFormAsapId;

    @Transient
    private Sector sector;

    @Column(name = "forwarded")
    private Boolean forwarded;

    @Column(name = "items_descr", length = 4000)
    private String itemsDescription;

    @Column(name = "latest_action_perform_date")
    private Date latestActionPerformDate;

    @Column(name = "assign_user_id")
    private Long assignUserId;

    @Column(name = "assign_date")
    private Date assignDate;

    @Column(name = "reserve_date")
    private Date reserveDate;

    @Column(name = "allergy_alert")
    private Long allergyAlert;

    @Column(name = "transport_type")
    private String transportType;

    @Column(name = "access_number")
    private String accessNumber;

    @Transient
    private boolean expanded;

    @Transient
    private List<? extends RadiologyExamRequestItemBase> radiologyExamRequestItems;

    @Transient
    private List<? extends RadiologyExamRequestItemBase> itemsForTag;

    @Transient
    private List<RequestNote> reqNot;

    @Transient
    private List<Allergy> allergies;

    @Transient
    public List<? extends RadiologyExamRequestItemBase> getRadiologyExamRequestItems() {
        return radiologyExamRequestItems;
    }

    @Transient
    private Allergy editAllergy;

    public boolean getCanAddReport() {
        if (getWaitingListRegistrationState() == null) {
            return false;
        }
        switch (getWaitingListRegistrationState()) {
            case ACCEPTED:
            case PERFORMED:
            case IN_READING:
            case DRAFT:
            case REPORTED:
            case SIGNED:
                return true;
            default:
                return false;
        }
    }

    public <T extends RadiologyExamRequestItemBase> void loadLazyValues(Class<T> radiologyItemBean)
            throws HibernateException, IllegalAccessException, PersistenceBeanException {
        if (getRadiologyExamRequestItems() == null) {
            List<T> loadedRadiologyExamRequestItems = DaoManager
                    .load(radiologyItemBean, new Criterion[]{
                            Restrictions.eq("radiologyExamRequest.id",
                                    this.getId())
                    });

            setRadiologyExamRequestItems(loadedRadiologyExamRequestItems);
        }

        if (reqNot == null) {
            try {
                reqNot = DaoManager.load(RequestNote.class, new Criterion[]{
                        Restrictions.eq("radiologyExamRequest.id", this.getId())
                });
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        if (allergies == null) {
            updateAllergies();
        }
    }

    public Boolean getShowAsterisk() {
        return UserHolder.getInstance().getCurrentUser().getShowOrderDate()
                && getReserveDate() != null
                && getAcceptDate() != null
                && DateTimeHelper.getDateDiffInMin(getAcceptDate(), getReserveDate()) > 20L;
    }

    public void updateAllergies() {
        try {
            allergies = DaoManager.load(Allergy.class, new Criterion[]{
                    Restrictions.eq("patient.id", this.getPatientId())
            });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void removeAllergy(Long id) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getAllergies()) &&
                !ValidationHelper.isNullOrEmpty(id)) {
            Iterator<Allergy> iterator = getAllergies().iterator();
            while (iterator.hasNext()) {
                if (id.equals(iterator.next().getId())) {
                    DaoManager.remove(Allergy.class, id, true);
                    iterator.remove();
                    return;
                }
            }
        }
    }

    public String getAssignUsername() {
        if (this.getAssignUserId() != null) {
            try {
                List<Object> usernames = DaoManager.getFields(User.class,
                        new Criterion[]{
                                Restrictions.eq("id", this.getAssignUserId())
                        }, null, false, "lastName", "firstName");

                if (!ValidationHelper.isNullOrEmpty(usernames)
                        && !ValidationHelper
                        .isNullOrEmpty(((Object[]) usernames.get(0))[0])
                        && !ValidationHelper.isNullOrEmpty(
                        ((Object[]) usernames.get(0))[1])) {
                    return String.format("%s %s",
                            ((Object[]) usernames.get(0))[0],
                            ((Object[]) usernames.get(0))[1]);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return "";
    }

    @Transient
    public List<RequestNote> getRequestNotes() {
        return reqNot;
    }

    @Transient
    public Boolean getNeedToShowAccessNumber() {
        if (WaitingListRegistrationStates.RESERVED
                .equals(this.getWaitingListRegistrationState())) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public boolean getRenderAnnulateBtnOnWorklist() {
        return WaitingListRegistrationStates.PERFORMED
                .equals(this.getWaitingListRegistrationState())
                || WaitingListRegistrationStates.ACCEPTED
                .equals(this.getWaitingListRegistrationState())
                || WaitingListRegistrationStates.IN_READING
                .equals(this.getWaitingListRegistrationState());
    }

    public boolean getDisableCancelAndModifiBtnOnWorklist() {
        return !WaitingListRegistrationStates.RESERVED
                .equals(this.getWaitingListRegistrationState())
                && !WaitingListRegistrationStates.ACCEPTED
                .equals(this.getWaitingListRegistrationState());
    }

    public boolean getRenderAnnulateExecutionBtnOnWorklist() {
        return WaitingListRegistrationStates.ACCEPTED
                .equals(this.getWaitingListRegistrationState());
    }

    public String getAnnulateButtonName() {
        if (WaitingListRegistrationStates.PERFORMED
                .equals(this.getWaitingListRegistrationState())
                || WaitingListRegistrationStates.IN_READING
                .equals(this.getWaitingListRegistrationState())) {
            return ResourcesHelper.getString("workListCancelExecution");
        } else if (WaitingListRegistrationStates.ACCEPTED
                .equals(this.getWaitingListRegistrationState())) {
            return ResourcesHelper.getString("worklistCancelExecutionAc");
        }
        return "";
    }

    @Transient
    public String getPatientFullname() {
        return String.format("%s %s",
                this.getSurname() == null ? "" : this.getSurname(),
                this.getName() == null ? "" : this.getName());
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

    public void setRadiologyExamRequestItems(List<? extends RadiologyExamRequestItemBase> radiologyExamRequestItems) {
        this.radiologyExamRequestItems = radiologyExamRequestItems;
    }

    // Transient methods for document generation

    @Transient
    public String getExamsDescription()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        StringBuilder sb = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())) {
            for (int i = 0; i < getRadiologyExamRequestItems().size() - 1; i++) {
                if (!ValidationHelper
                        .isNullOrEmpty(getRadiologyExamRequestItems().get(i))
                        && !ValidationHelper
                        .isNullOrEmpty(getRadiologyExamRequestItems()
                                .get(i).getRadiologyExam())
                        && !ValidationHelper.isNullOrEmpty(
                        getRadiologyExamRequestItems().get(i)
                                .getRadiologyExam().getDescription())) {
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
                    .isNullOrEmpty(
                            getRadiologyExamRequestItems()
                                    .get(getRadiologyExamRequestItems()
                                            .size() - 1)
                                    .getRadiologyExam())
                    && !ValidationHelper
                    .isNullOrEmpty(getRadiologyExamRequestItems().get(
                            getRadiologyExamRequestItems().size() - 1)

                            .getRadiologyExam().getDescription())) {
                sb.append(getRadiologyExamRequestItems()
                        .get(getRadiologyExamRequestItems().size() - 1)
                        .getRadiologyExam().getDescription());
            }
        }
        return sb.toString();
    }

    @Transient
    public String getItemRequestDate()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0)
                .getRequestDate() != null) {
            return DateTimeHelper.toString(
                    getRadiologyExamRequestItems().get(0).getRequestDate());
        }
        return null;
    }

    @Transient
    public String getItemAcceptDate()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0)
                .getAcceptDate() != null) {
            return DateTimeHelper.toString(
                    getRadiologyExamRequestItems().get(0).getAcceptDate());
        }
        return null;
    }

    @Transient
    public Date getAcceptDate() {
        try {
            loadLazyValues(RadiologyExamRequestItem.class);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0)
                .getAcceptDate() != null) {
            return getRadiologyExamRequestItems().get(0).getAcceptDate();
        }
        return null;
    }

    @Transient
    public String getItemExecutionDate()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0)
                .getPerformDate() != null) {
            return DateTimeHelper.toString(
                    getRadiologyExamRequestItems().get(0).getPerformDate());
        }
        return null;
    }

    @Transient
    public String getItemReserveDate()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0)
                .getReserveDate() != null) {
            return DateTimeHelper.toString(
                    getRadiologyExamRequestItems().get(0).getReserveDate());
        }
        return null;
    }

    @Transient
    public String getItemRequestTime()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0)
                .getRequestDate() != null) {
            return DateTimeHelper.toStringTime(
                    getRadiologyExamRequestItems().get(0).getRequestDate());
        }
        return null;
    }

    @Transient
    public String getItemAcceptTime()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0)
                .getAcceptDate() != null) {
            return DateTimeHelper.toStringTime(
                    getRadiologyExamRequestItems().get(0).getAcceptDate());
        }
        return null;
    }

    @Transient
    public String getItemExecutionTime()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0)
                .getPerformDate() != null) {
            return DateTimeHelper.toStringTime(
                    getRadiologyExamRequestItems().get(0).getPerformDate());
        }
        return null;
    }

    @Transient
    public String getItemReserveTime()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && getRadiologyExamRequestItems().get(0)
                .getReserveDate() != null) {
            return DateTimeHelper.toStringTime(
                    getRadiologyExamRequestItems().get(0).getReserveDate());
        }
        return null;
    }

    @Transient
    public String getItemUserAccept()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper.isNullOrEmpty(
                getRadiologyExamRequestItems().get(0).getOperator())) {
            return getRadiologyExamRequestItems().get(0).getOperator();
        }
        return "";
    }

    @Transient
    public String getItemUserPerform()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0)
                        .getRequestingDoctor())) {
            return getRadiologyExamRequestItems().get(0).getRequestingDoctor();
        }
        return "";
    }

    @Transient
    public boolean getIsReported() {
        return WaitingListRegistrationStates.REPORTED
                .equals(getWaitingListRegistrationState());
    }

    @Transient
    public String getItemUserDelete()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper
                .isNullOrEmpty(getRadiologyExamRequestItems().get(0))
                && !ValidationHelper.isNullOrEmpty(
                getRadiologyExamRequestItems().get(0).getOperator())) {
            return getRadiologyExamRequestItems().get(0).getDeleteUser();
        }
        return "";
    }

    @Transient
    public String getActionBtnTitle() {
        String actionName = null;

        if (waitingListRegistrationState != null) {
            switch (waitingListRegistrationState) {
                case RESERVED:
                    actionName = ResourcesHelper
                            .getString("workListActionAccept");
                    break;
                case ACCEPTED:
                    actionName = ResourcesHelper
                            .getString("workListActionPerform");
                    break;
                case PERFORMED:
                case IN_READING:
                    actionName = ResourcesHelper
                            .getString("workListActionReport");
                    break;
                case DELETED:
                case REPORTED:
                    actionName = ResourcesHelper
                            .getString("workListActionView");
                    break;
                case DRAFT:
                    actionName = ResourcesHelper
                            .getString("workListActionDraft");
                    break;
                case PARTIALLY_ACCEPTED:
                    actionName = ResourcesHelper
                            .getString("workListActionPartAccepted");
                    break;
                case SIGNED:
                    actionName = ResourcesHelper
                            .getString("workListActionView");
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
    public Boolean getShowCheckBox() {
        switch (this.getWaitingListRegistrationState()) {
            case PARTIALLY_ACCEPTED:
            case DRAFT:
                return Boolean.FALSE;

            default:
                break;
        }
        return Boolean.TRUE;
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
            throws HibernateException {
        return getItemsDescription();
    }

    public String[] getItems() {
        return ValidationHelper.isNullOrEmpty(getItemsDescription())
                ? new String[0] : getItemsDescription().split(", ");
    }

    @Transient
    public String getExpansionRowTitle()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        StringBuilder str = new StringBuilder(
                ResourcesHelper.getString("workListExpansionRowPart1"));
        str.append(" ");
        str.append(ResourcesHelper.getString("workListExpansionRowPart2"));
        str.append(" ");

        if (!ValidationHelper.isNullOrEmpty(this.getRadiologyExamRequestItems())
                && this.getRadiologyExamRequestItems().get(0)
                .getAccessNumber() != null) {
            str.append(this.getRadiologyExamRequestItems().get(0)
                    .getAccessNumber());
        }

        str.append(" ");
        str.append(ResourcesHelper.getString("workListExpansionRowPart3"));
        str.append(" ");
        str.append(getCardNumber());

        return str.toString();
    }

    @Transient
    public String getCardNumber()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getAllItems())) {
            StringBuilder sb = new StringBuilder();
            List<Long> cardNumber = new ArrayList<Long>();
            List<Long> cardYear = new ArrayList<Long>();

            for (RadiologyExamRequestItemBase item : getItemsForTag()) {
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

                    sb.append(this.getExamTypeDescription().substring(0, 2));
                    sb.append(cardYear.get(i));
                    sb.append(String.format("%06d", cardNumber.get(i)));
                }
            }

            return sb.toString();
        }

        return "";
    }

    private List<RadiologyExamRequestItem> getAllItems()
            throws HibernateException {
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems())
                && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequestItems().get(0).getFileEntityId())) {
            try {
                List<RadiologyExamRequestItem> radItemsWithSameFileEntity = DaoManager
                        .load(RadiologyExamRequestItem.class, new Criterion[]{
                                Restrictions.eq("fileEntity.id",
                                        getRadiologyExamRequestItems().get(0)
                                                .getFileEntityId())
                        });

                setItemsForTag(radItemsWithSameFileEntity);
            } catch (HibernateException | IllegalAccessException
                    | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        } else {
            setItemsForTag(this.getRadiologyExamRequestItems());
        }
        return (List<RadiologyExamRequestItem>) getItemsForTag();
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

    @Transient
    public Boolean getIsPartAccepted() {
        return WaitingListRegistrationStates.PARTIALLY_ACCEPTED
                .equals(this.getWaitingListRegistrationState());
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
        if (states != null) {
            if (this.getWaitingListRegistrationState() != null) {
                boolean contains = ArrayUtils.contains(states,
                        this.getWaitingListRegistrationState());
                return isState == contains;
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

    public boolean getSelectedAll() throws HibernateException {
        if (this.getRadiologyExamRequestItems() != null) {
            for (RadiologyExamRequestItemBase radiologyExamRequestItem : getRadiologyExamRequestItems()) {
                if (!Boolean.TRUE.equals(radiologyExamRequestItem.getSelected())) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAll(boolean selectedAll) throws HibernateException {
        if (this.getRadiologyExamRequestItems() != null) {
            for (RadiologyExamRequestItemBase radiologyExamRequestItem : getRadiologyExamRequestItems()) {
                radiologyExamRequestItem.setSelected(selectedAll);
            }
        }
    }

    @Transient
    public Boolean getIsAccepted() {
        return WaitingListRegistrationStates.ACCEPTED
                .equals(this.getWaitingListRegistrationState());
    }

    @Transient
    public Boolean getCanAnnulateAction() {
        try {
            if (WaitingListRegistrationStates.PERFORMED
                    .equals(this.getWaitingListRegistrationState())
                    || WaitingListRegistrationStates.IN_READING
                    .equals(this.getWaitingListRegistrationState())) {
                return PermissionsHelper.getPermission(
                        SpecialPermissionTypes.ANNULATE_EXECUTION_PERFORMED);
            } else if (WaitingListRegistrationStates.ACCEPTED
                    .equals(this.getWaitingListRegistrationState())) {
                return PermissionsHelper.getPermission(
                        SpecialPermissionTypes.ANNULATE_EXECUTION_ACCEPTED);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return Boolean.FALSE;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSecondPhone() {
        return secondPhone;
    }

    public void setSecondPhone(String secondPhone) {
        this.secondPhone = secondPhone;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getBirthCityId() {
        return birthCityId;
    }

    public void setBirthCityId(Long birthCityId) {
        this.birthCityId = birthCityId;
    }

    public Long getAsapSectorId() {
        return asapSectorId;
    }

    public void setAsapSectorId(Long sectorId) {
        this.asapSectorId = sectorId;
    }

    public Long getUrgencyId() {
        return urgencyId;
    }

    public void setUrgencyId(Long urgencyId) {
        this.urgencyId = urgencyId;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
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

    public Long getExamTypeId() {
        return examTypeId;
    }

    public void setExamTypeId(Long examTypeId) {
        this.examTypeId = examTypeId;
    }

    public String getExamTypeDescription() {
        return examTypeDescription;
    }

    public void setExamTypeDescription(String examTypeDescription) {
        this.examTypeDescription = examTypeDescription;
    }

    @Transient
    public Urgency getUrgency()
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getUrgencyId())) {
            Urgency urgency = DaoManager.get(Urgency.class,
                    this.getUrgencyId());
            return urgency;
        }
        return null;
    }

    @Transient
    public City getCity() throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getCityId())) {
            City city = DaoManager.get(City.class, this.getCityId());
            return city;
        }
        return null;
    }

    @Transient
    public City getBirthCity()
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getBirthCityId())) {
            City birthCity = DaoManager.get(City.class, this.getBirthCityId());
            return birthCity;
        }
        return null;
    }

    @Transient
    public Province getProvince()
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getProvinceId())) {
            return DaoManager.get(Province.class,
                    this.getProvinceId());
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

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public String getExamTypeColor() {
        return examTypeColor;
    }

    public void setExamTypeColor(String examTypeColor) {
        this.examTypeColor = examTypeColor;
    }

    public Boolean getExamTypeHasColor() {
        return examTypeHasColor;
    }

    public void setExamTypeHasColor(Boolean examTypeHasColor) {
        this.examTypeHasColor = examTypeHasColor;
    }

    public String getBackgroundStyle() {
        StringBuilder sb = new StringBuilder();

        if (this.examTypeColor != null && !this.examTypeColor.isEmpty()
                && this.examTypeHasColor != null
                && this.examTypeHasColor) {
            sb.append("background-color: #");
            sb.append(this.examTypeColor);
        } else {
            sb.append("color: #");
            sb.append("000000");
            sb.append(";");
        }
        return sb.toString();
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

    public void setReferringDoctor(String referringDoctor) {
        this.referringDoctor = referringDoctor;
    }

    public String getReferringDoctor() {
        return referringDoctor;
    }

    public List<? extends RadiologyExamRequestItemBase> getItemsForTag() {
        return itemsForTag;
    }

    public void setItemsForTag(List<? extends RadiologyExamRequestItemBase> itemsForTag) {
        this.itemsForTag = itemsForTag;
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

    public String getAccessNumber() {
        return accessNumber;
    }

    public void setAccessNumber(String accessNumber) {
        this.accessNumber = accessNumber;
    }

    public Date getLatestActionPerformDate() {
        return latestActionPerformDate;
    }

    public void setLatestActionPerformDate(Date latestActionPerformDate) {
        this.latestActionPerformDate = latestActionPerformDate;
    }

    public Sector getSector() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (sector == null && sectorId != null) {
            sector = DaoManager.get(Sector.class, getSectorId());
        }
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public Long getHl7FieldsFormAsapId() {
        return hl7FieldsFormAsapId;
    }

    public void setHl7FieldsFormAsapId(Long hl7FieldsFormAsapId) {
        this.hl7FieldsFormAsapId = hl7FieldsFormAsapId;
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

    public Date getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(Date reserveDate) {
        this.reserveDate = reserveDate;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getAllergyAlert() {
        return allergyAlert;
    }

    public void setAllergyAlert(Long allergyAlert) {
        this.allergyAlert = allergyAlert;
    }

    public List<Allergy> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<Allergy> allergies) {
        this.allergies = allergies;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Allergy getEditAllergy() {
        return editAllergy;
    }

    public void setEditAllergy(Allergy editAllergy) {
        this.editAllergy = editAllergy;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }
}
