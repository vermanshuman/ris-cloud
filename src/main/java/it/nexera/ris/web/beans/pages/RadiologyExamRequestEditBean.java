package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.comparators.SectorComparator;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.IntegrationConnectionException;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.AsapSectorHelper;
import it.nexera.ris.common.helpers.logic.GeneralFunctionsHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendar;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.integration.ADTIntegrationHelper;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.FilterExamRequestItemWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

@Named("radiologyExamRequestEditBean")
@ViewScoped
public class RadiologyExamRequestEditBean extends
        EntityEditPageBean<RadiologyExam> implements Serializable {

    private static final long serialVersionUID = 4982074599355236222L;

    // selected radiology exams table

    private List<FilterExamRequestItemWrapper> selectedRadExams;

    private List<FilterExamRequestItemWrapper> selectedRadExamsFiltered;

    // single radiology exams table

    private List<FilterExamRequestItemWrapper> listRadExamItemOptions;

    private List<FilterExamRequestItemWrapper> listRadExamItemOptionsFiltered;

    private List<FilterExamRequestItemWrapper> selectedRadExamItemOptions;

    private FilterExamRequestItemWrapper selectedRadExamItemOption;

    private Long selectedDiagnosticCode;

    // other form fields

    private String diagnosticQuestion;

    private List<SelectItem> transportTypes;

    private RadiologyRequestTrasportTypes selectedTransportType;

    private List<SelectItem> urgencies;

    private Long selectedUrgencyId;

    private List<AsapSector> asapSectors;

    private AsapSector selectedAsapSectorId;

    // not a form fields

    private List<RadiologyExam> loadedRadiologyExam;

    private Patient patient;

    private Boolean saveAsList;

    private Boolean redirectToEventCalendar;

    private List<Long> listIds = new ArrayList<Long>();

    private List<SelectItem> userHospitals;

    private List<SelectItem> sectors;

    private Long selectedRisSector;

    private Long selectedUserHospital;

    private Long selectedRadiologyExamType;

    private List<SelectItem> radiologyExamTypes;

    private boolean ableToChooseHospital;

    private FilterExamRequestItemWrapper selectedRadExamToDelete;

    private RadiologyExamRequest radExReqToRedirect;

    Boolean cupRequest;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        initFields();
        fillComboboxes();
    }

    private void initFields() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.setSaveAsList(Boolean.TRUE);

        this.setRadiologyExamTypes(ComboboxHelper.fillList(ExamType.class,
                Order.asc("description"), true));
        setSelectedRadExams(new ArrayList<FilterExamRequestItemWrapper>());
        setListRadExamItemOptions(new ArrayList<FilterExamRequestItemWrapper>());

        Long patientId = (Long) this.getSession().get("patientId");
        this.getSession().remove("patientId");

        if (!ValidationHelper.isNullOrEmpty(patientId)) {
            setPatient(DaoManager.get(Patient.class, new Criterion[]{
                    Restrictions.eq("id", patientId)
            }));
        } else {
            RedirectHelper.goTo(PageTypes.PATIENT_SEARCH);
        }

        loadAllList();

        initListRadExamItemOptions();
    }

    public List<AsapSector> completeText(String query) {
        try {
            this.setAsapSectors(new ArrayList<AsapSector>());
            String hospitalCode = null;
            if (!ValidationHelper.isNullOrEmpty(getSelectedUserHospital())) {
                hospitalCode = DaoManager.getField(Hospital.class, "provenanceHospital", new Criterion[]{
                        Restrictions.eq("id", getSelectedUserHospital())
                }, null);
            }
            this.getAsapSectors().addAll(AsapSectorHelper.getASAPSIOSectorsFromDB(query, hospitalCode));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return this.getAsapSectors();
    }

    private void initListRadExamItemOptions() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getLoadedRadiologyExam())) {
            List<BigDecimal> mostUsedExamIds = MostUsedItemsHelper
                    .getMostUsedRadiologyExamsIds();

            if (!ValidationHelper.isNullOrEmpty(mostUsedExamIds)) {
                for (BigDecimal id : mostUsedExamIds) {
                    for (RadiologyExam e : getLoadedRadiologyExam()) {
                        if (e.getId().longValue() == id.longValue()
                                && !ValidationHelper.isNullOrEmpty(e
                                .getExamType())
                                && !ValidationHelper.isNullOrEmpty(e
                                .getExamType().getDiagnostics())) {
                            List<Diagnostic> ds = e.getExamType()
                                    .getDiagnostics();

                            for (Diagnostic d : ds) {
                                boolean contains = false;

                                if (!ValidationHelper.isNullOrEmpty(d
                                        .getUsers())) {
                                    for (User u : d.getUsers()) {
                                        if (u.getId().longValue() == this
                                                .getCurrentUser().getId()
                                                .longValue()) {
                                            contains = true;
                                            break;
                                        }
                                    }
                                }

                                if (contains) {
                                    getListRadExamItemOptions()
                                            .add(new FilterExamRequestItemWrapper(
                                                    e));
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                for (RadiologyExam exam : getLoadedRadiologyExam()) {
                    getListRadExamItemOptions().add(
                            new FilterExamRequestItemWrapper(exam));
                }
            }
        }
    }

    private void loadAllList() throws PersistenceBeanException,
            IllegalAccessException {
        setLoadedRadiologyExam(DaoManager.load(RadiologyExam.class,
                new Criterion[]{
                        Restrictions.eq("state", EnableDisableEnum.ENABLE)
                }, Order.asc("code")));
    }

    private void fillComboboxes() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (getUrgencies() == null) {
            setUrgencies(ComboboxHelper.fillList(Urgency.class));
        }

        if (getTransportTypes() == null) {
            setTransportTypes(ComboboxHelper
                    .fillList(RadiologyRequestTrasportTypes.class));
        }

        if (getUserHospitals() == null) {
            this.setUserHospitals(ComboboxHelper.fillList(Hospital.class, Order.asc("description"), false));
            setAbleToChooseHospital(true);

            if (!ValidationHelper.isNullOrEmpty(this.getUserHospitals())) {
                this.setSelectedUserHospital((Long) this.getUserHospitals()
                        .get(0).getValue());
            }
        }

        fillSectors();
        if (getSelectedRisSector() != null) {
            List<Long> hospitalIds = DaoManager.loadIds(Hospital.class, new CriteriaAlias[]{
                    new CriteriaAlias("sectors", "sectors", JoinType.LEFT_OUTER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("sectors.id", getSelectedRisSector())
            });
            if (!ValidationHelper.isNullOrEmpty(hospitalIds)) {
                setSelectedUserHospital(hospitalIds.get(0));
            }
        }
    }

    private boolean ifHospitalExist(List<SelectItem> items, Long hospitalId) {
        for (SelectItem item : items) {
            if (item.getValue().equals(hospitalId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getRedirectToEventCalendar())
                || !this.getRedirectToEventCalendar().booleanValue()
                || this.eventCalendarExist()) {
            if ((Boolean.TRUE.equals(this.getRedirectToEventCalendar()) && checkReservedPossibility()
                    .booleanValue())
                    || Boolean.FALSE.equals(this.getRedirectToEventCalendar())) {
                if (saveFunction()) {
                    FacesContext.getCurrentInstance().getExternalContext()
                            .getSessionMap()
                            .put("listIdsRequestItems", this.getListIds());
                    WaitingListStatusHelper wlsh = new WaitingListStatusHelper();
                    if (!ValidationHelper.isNullOrEmpty(getRadExReqToRedirect().getSector())) {
                        getRadExReqToRedirect().setSectorId(getRadExReqToRedirect().getSector().getId());
                    }
                    wlsh.sendOrmMsg(getRadExReqToRedirect());
                    if (Boolean.TRUE.equals(this.getRedirectToEventCalendar())) {
                        try {
                            if (agendaExist()) {
                                assign();
                            }
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }
                    } else {
                        if (Boolean.TRUE.equals(this.getSaveAsList())) {
                            RedirectHelper
                                    .goTo(PageTypes.WAITINGLIST_REGISTRATION_LIST);
                        } else {
                            RedirectHelper.goTo(PageTypes.WORKLIST);
                        }
                    }
                }
            } else {
                MessageHelper
                        .addGlobalMessage(
                                FacesMessage.SEVERITY_ERROR,
                                ResourcesHelper
                                        .getValidation("selectMoreThanOneExamType"),
                                "");
            }
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("assignFailed"),
                    ResourcesHelper.getValidation("assignCreateEventCalendar"));
        }
    }

    private boolean eventCalendarExist() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        List<EventCalendar> eventCalendars = DaoManager
                .load(EventCalendar.class);
        if (!ValidationHelper.isNullOrEmpty(eventCalendars)) {
            List<Long> toCheckRadiologyExamIds = new ArrayList<Long>();
            if (!ValidationHelper.isNullOrEmpty(this.getSelectedRadExams())) {
                for (FilterExamRequestItemWrapper feriw : this
                        .getSelectedRadExams()) {
                    if (!ValidationHelper.isNullOrEmpty(feriw
                            .getRadiologyExam())) {
                        toCheckRadiologyExamIds.add(feriw.getRadiologyExam()
                                .getId());
                    }
                }
                if (!ValidationHelper.isNullOrEmpty(toCheckRadiologyExamIds)) {
                    for (EventCalendar ec : eventCalendars) {
                        List<Long> fullRadiologyExamIds = GeneralFunctionsHelper
                                .exportRadiologyExamIdsFromCalendar(ec);
                        if (!ValidationHelper
                                .isNullOrEmpty(toCheckRadiologyExamIds)
                                && fullRadiologyExamIds
                                .containsAll(toCheckRadiologyExamIds)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void assign() throws SecurityException, NoSuchFieldException,
            HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        WaitingListSessionHelper.backupToSession(this.getRadExReqToRedirect());
        this.getSession().put("returnPage", this.getCurrentPage());
        RedirectHelper.goTo(PageTypes.DATE_CONFIRMATION);
    }

    public boolean agendaExist() {
        if (this.getRadExReqToRedirect() != null) {
            if (ValidationHelper.isNullOrEmpty(getRadExReqToRedirect()
                    .getSector().getDiagnostic())) {
                showAssignErrorMsgForAgenda();

                return false;
            }

            List<Diagnostic> diagnostics = getRadExReqToRedirect().getSector()
                    .getDiagnostic();
            for (Diagnostic d : diagnostics) {
                if (!ValidationHelper.isNullOrEmpty(d.getEventCalendar())) {
                    return true;
                }
            }
        }
        showAssignErrorMsg();

        return false;
    }

    private void showAssignErrorMsg() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                ResourcesHelper.getValidation("assignFailed"),
                ResourcesHelper.getValidation("assignCreateEventCalendar"));
    }

    private void showAssignErrorMsgForAgenda() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                ResourcesHelper.getValidation("assignFailed"), ResourcesHelper
                        .getValidation("assignCreateEventCalendarForSector"));
    }

    private boolean saveFunction() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        Map<Long, List<FilterExamRequestItemWrapper>> map = new HashMap<Long, List<FilterExamRequestItemWrapper>>();

        for (FilterExamRequestItemWrapper item : this.getSelectedRadExams()) {
            if (!ValidationHelper.isNullOrEmpty(item.getRadiologyExam()
                    .getExamType())) {
                if (!ValidationHelper.isNullOrEmpty(item.getRadiologyExam()
                        .getExamType())) {
                    ExamType et = item.getRadiologyExam().getExamType();
                    Long keyMap = et.getId();

                    List<FilterExamRequestItemWrapper> valueMap = null;
                    if (!map.containsKey(keyMap)) {
                        valueMap = new ArrayList<FilterExamRequestItemWrapper>();
                        valueMap.add(item);
                        map.put(keyMap, valueMap);
                    } else {
                        valueMap = map.get(keyMap);
                        valueMap.add(item);
                    }
                }
            }
        }

        Urgency selectedUrgency = null;

        if (!ValidationHelper.isNullOrEmpty(getSelectedUrgencyId())) {
            selectedUrgency = DaoManager.get(Urgency.class,
                    getSelectedUrgencyId());
        } else {
            String defaultUrgencyDescription = "Codice Bianco";
            String defaultUrgencyCode = "Bianco";
            List<Urgency> urgencies = DaoManager.load(Urgency.class, new Criterion[]{
                    Restrictions.or(
                            Restrictions.eq("description", defaultUrgencyDescription),
                            Restrictions.eq("code", defaultUrgencyCode)
                    )
            });
            if (urgencies != null) {
                selectedUrgency = urgencies.get(0);
            }
        }

        Sector selectedSector = null;

        if (!ValidationHelper.isNullOrEmpty(getSelectedRisSector())) {
            selectedSector = DaoManager.get(Sector.class,
                    getSelectedRisSector());
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedAsapSectorId())) {
            for (Entry<Long, List<FilterExamRequestItemWrapper>> element : map
                    .entrySet()) {
                saveFromMap(element, selectedUrgency, selectedSector,
                        getSelectedAsapSectorId().getId());
            }
        } else {
            return false;
        }
        return true;
    }

    private void saveFromMap(
            Entry<Long, List<FilterExamRequestItemWrapper>> element,
            Urgency selectedUrgency, Sector selectedSector,
            Long selectedSectorId) throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        RadiologyExamRequest radExamReq = new RadiologyExamRequest();

        List<RadiologyExamRequestItem> list = new ArrayList<RadiologyExamRequestItem>();
        for (FilterExamRequestItemWrapper item : element.getValue()) {
            createRadExamRequestsFromFilterWrapper(list, item, radExamReq);
        }

        radExamReq.setUrgency(selectedUrgency);
        radExamReq.setSector(selectedSector);
        if(!ValidationHelper.isNullOrEmpty(getCupRequest()) && getCupRequest())
            radExamReq.setForwarded(Boolean.TRUE);
        else
            radExamReq.setForwarded(null);

        if (getSaveAsList().booleanValue()) {
            radExamReq.setRequestDate(new Date());
            radExamReq
                    .setWaitingListRegistrationState(WaitingListRegistrationStates.REQUIRED);
            radExamReq
                    .setSendingStatus(Hl7RequestSendingStatus.SENDING_NOT_NEED);
        } else {
            radExamReq.setRequestDate(new Date());
            radExamReq.setReserveDate(new Date());
            radExamReq
                    .setWaitingListRegistrationState(WaitingListRegistrationStates.RESERVED);
            radExamReq.setLatestActionDate(DateTimeHelper.getNow());

            if (!WaitingListRegistrationStates.DRAFT
                    .equals(radExamReq.getWaitingListRegistrationState())
                    && !WaitingListRegistrationStates.REPORTED.equals(
                    radExamReq.getWaitingListRegistrationState())) {
                radExamReq.setLatestActionPerformDate(DateTimeHelper.getNow());
            }
        }

        try {
            radExamReq = ADTIntegrationHelper.getInstance()
                    .getRequestFieldsFromSectorId(selectedSectorId, radExamReq);
        } catch (IntegrationConnectionException e) {
            LogHelper.log(log, e);
        }

        radExamReq
                .setExamType((DaoManager.get(ExamType.class, element.getKey())));

        radExamReq.setPatient(getPatient());
        radExamReq.setTrasportType(getSelectedTransportType());
        radExamReq.setRadiologyExamRequestItems(list);
        radExamReq.setHospital(DaoManager.get(Hospital.class,
                getSelectedUserHospital()));

        radExamReq.udateItemsDescription(list);//FIXME: DELETE AFTER CREATE TRIGGER

        DaoManager.save(radExamReq);

        for (RadiologyExamRequestItem item : list) {
            item.setRadiologyExamRequest(radExamReq);
            item.setDiagnosticQuestion(getDiagnosticQuestion());
            item.setRequestingDoctor("CUP");
            DaoManager.save(item);
        }

        for (RadiologyExamRequestItem item : list) {
            this.getListIds().add(item.getRadiologyExamRequest().getId());
        }

        this.setRadExReqToRedirect(radExamReq);
    }

    private void createRadExamRequestsFromFilterWrapper(
            List<RadiologyExamRequestItem> list,
            FilterExamRequestItemWrapper item, RadiologyExamRequest request)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (item == null) {
            return;
        }

        if (list == null) {
            list = new ArrayList<RadiologyExamRequestItem>();
        }

        if (item.getRadiologyExam() != null) {
            list.add(createRadiologyExamRequestItemFromFilterWrapper(item,
                    request));
        }
    }

    private RadiologyExamRequestItem createRadiologyExamRequestItemFromFilterWrapper(
            FilterExamRequestItemWrapper wrapper, RadiologyExamRequest request)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (wrapper == null) {
            return null;
        }

        RadiologyExamRequestItem item = new RadiologyExamRequestItem();

        item.setRequestDate(new Date());
        item.setRadiologyExam(wrapper.getRadiologyExam());

        if (getSaveAsList().booleanValue()) {
            item.setWaitingListRegistrationState(WaitingListRegistrationStates.REQUIRED);
        } else {
            item.setWaitingListRegistrationState(WaitingListRegistrationStates.RESERVED);
            item.setReserveDate(new Date());
        }

        item.setUrgentRequest(wrapper.getUrgentRequest() != null ? wrapper
                .getUrgentRequest() : Boolean.FALSE);

        return item;
    }

    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(getDiagnosticQuestion())) {
            addRequiredFieldExeption("diagnosticQuestion");
        }
        if (ValidationHelper.isNullOrEmpty(getSelectedRadExams())) {
            addFieldExeption("selectedRadExamsTable",
                    "necessitaDiScegliereInUnoExam");
        }
        if (ValidationHelper.isNullOrEmpty(getSelectedAsapSectorId())) {
            addRequiredFieldExeption("asapSector");
        }
    }

    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.PATIENT_SEARCH);
    }

    @Override
    public void afterSave() {

    }

    public void filteredByExamType() {
        try {
            getListRadExamItemOptions().clear();
            initListRadExamItemOptions();
            List<RadiologyExam> reList = new ArrayList<RadiologyExam>();
            if (!ValidationHelper.isNullOrEmpty(getSelectedRadiologyExamType())) {
                for (FilterExamRequestItemWrapper feriw : getListRadExamItemOptions()) {
                    if (!ValidationHelper.isNullOrEmpty(feriw
                            .getRadiologyExam())
                            && !ValidationHelper.isNullOrEmpty(feriw
                            .getRadiologyExam().getExamType())
                            && feriw.getRadiologyExam().getExamType().getId()
                            .equals(getSelectedRadiologyExamType())) {
                        reList.add(feriw.getRadiologyExam());
                    }
                }
                getListRadExamItemOptions().clear();
                if (!ValidationHelper.isNullOrEmpty(reList)) {
                    fillListRadExamItemOptions(reList);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void fillListRadExamItemOptions(List<RadiologyExam> list) {
        for (RadiologyExam radiologyExam : list) {
            getListRadExamItemOptions().add(
                    new FilterExamRequestItemWrapper(radiologyExam));
        }
    }

    public void addToSelectedRadExam() {
        try {
            for (FilterExamRequestItemWrapper wrapper : getListRadExamItemOptions()) {
                if (wrapper.getRadiologyExam().getId()
                        .equals(getSelectedRadExamItemOption().getId())) {
                    if (!checkListContains(getSelectedRadExams(), wrapper)
                            || Boolean.TRUE.equals(wrapper.getRadiologyExam()
                            .getMultiple())) {
                        getSelectedRadExams().add(wrapper);
                    } else {
                        MessageHelper.addGlobalMessage(
                                FacesMessage.SEVERITY_WARN, "", ResourcesHelper
                                        .getValidation("examAlreadyExist"));
                    }
                    break;
                }
            }
            this.setSelectedRadExamItemOption(null);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void hospitalChange() {
        setSelectedAsapSectorId(null);
    }

    private void fillSectors() {
        try {
            List<Sector> sectorsForCurrentUser = DaoManager.load(
                    Sector.class, new CriteriaAlias[]{
                            new CriteriaAlias("users", "u",
                                    JoinType.LEFT_OUTER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("u.id", this
                                    .getCurrentUser().getId())
                    });

            Collections.sort(sectorsForCurrentUser,
                    new SectorComparator());

            this.setSectors(new ArrayList<SelectItem>());

            for (Sector sector : sectorsForCurrentUser) {
                this.getSectors().add(
                        new SelectItem(sector.getId(), sector
                                .getDescription()));
            }
            if (!ValidationHelper.isNullOrEmpty(sectorsForCurrentUser)) {
                setSelectedRisSector(sectorsForCurrentUser.get(0).getId());
            }
            if (getCurrentUser().getPrimarySectorId() != null) {
                setSelectedRisSector(getCurrentUser().getPrimarySectorId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private boolean checkListContains(
            List<FilterExamRequestItemWrapper> wrappers,
            FilterExamRequestItemWrapper elementToCheck) {
        for (FilterExamRequestItemWrapper wrapper : wrappers) {
            if (wrapper.getId().equals(elementToCheck.getId())) {
                return true;
            }
        }
        return false;
    }

    public void removeSelectedRadExam() {
        try {
            for (FilterExamRequestItemWrapper wrapper : getSelectedRadExams()) {
                if (wrapper.getRadiologyExam() != null
                        && wrapper.getRadiologyExam().equals(
                        this.getSelectedRadExamToDelete()
                                .getRadiologyExam())) {
                    if (!getListRadExamItemOptions().contains(
                            getSelectedRadExamToDelete())) {
                        getListRadExamItemOptions().add(
                                getSelectedRadExamToDelete());
                    }
                    getSelectedRadExams().remove(wrapper);
                    break;
                }
            }
            this.setSelectedRadExamToDelete(null);
            this.filteredByExamType();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private Boolean checkReservedPossibility() {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedRadExams())) {
            Long examTypeId = this.getSelectedRadExams().get(0)
                    .getRadiologyExam().getExamType().getId();
            for (FilterExamRequestItemWrapper feriw : this
                    .getSelectedRadExams()) {
                if (!feriw.getRadiologyExam().getExamType().getId()
                        .equals(examTypeId)) {
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    public void handleCupRequest(){
        if(!ValidationHelper.isNullOrEmpty(getCupRequest()) && getCupRequest())
            setDiagnosticQuestion("Richiesta dal CUP");
        else {
            setDiagnosticQuestion(null);
        }
    }

    public List<FilterExamRequestItemWrapper> getListRadExamItemOptions() {
        return listRadExamItemOptions;
    }

    public void setListRadExamItemOptions(
            List<FilterExamRequestItemWrapper> listRadExamItemOptions) {
        this.listRadExamItemOptions = listRadExamItemOptions;
    }

    public List<FilterExamRequestItemWrapper> getListRadExamItemOptionsFiltered() {
        return listRadExamItemOptionsFiltered;
    }

    public void setListRadExamItemOptionsFiltered(
            List<FilterExamRequestItemWrapper> listRadExamItemOptionsFiltered) {
        this.listRadExamItemOptionsFiltered = listRadExamItemOptionsFiltered;
    }

    public List<FilterExamRequestItemWrapper> getSelectedRadExams() {
        return selectedRadExams;
    }

    public void setSelectedRadExams(
            List<FilterExamRequestItemWrapper> selectedRadExams) {
        this.selectedRadExams = selectedRadExams;
    }

    public List<FilterExamRequestItemWrapper> getSelectedRadExamsFiltered() {
        return selectedRadExamsFiltered;
    }

    public void setSelectedRadExamsFiltered(
            List<FilterExamRequestItemWrapper> selectedRadExamsFiltered) {
        this.selectedRadExamsFiltered = selectedRadExamsFiltered;
    }

    public RadiologyRequestTrasportTypes getSelectedTransportType() {
        return selectedTransportType;
    }

    public void setSelectedTransportType(
            RadiologyRequestTrasportTypes selectedTransportType) {
        this.selectedTransportType = selectedTransportType;
    }

    public List<SelectItem> getTransportTypes() {
        return transportTypes;
    }

    public void setTransportTypes(List<SelectItem> transportTypes) {
        this.transportTypes = transportTypes;
    }

    public String getDiagnosticQuestion() {
        return diagnosticQuestion;
    }

    public void setDiagnosticQuestion(String diagnosticQuestion) {
        this.diagnosticQuestion = diagnosticQuestion;
    }

    public List<FilterExamRequestItemWrapper> getSelectedRadExamItemOptions() {
        return selectedRadExamItemOptions;
    }

    public void setSelectedRadExamItemOptions(
            List<FilterExamRequestItemWrapper> selectedRadExamItemOptions) {
        this.selectedRadExamItemOptions = selectedRadExamItemOptions;
    }

    public Long getSelectedDiagnosticCode() {
        return selectedDiagnosticCode;
    }

    public void setSelectedDiagnosticCode(Long selectedDiagnosticCode) {
        this.selectedDiagnosticCode = selectedDiagnosticCode;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<RadiologyExam> getLoadedRadiologyExam() {
        return loadedRadiologyExam;
    }

    public void setLoadedRadiologyExam(List<RadiologyExam> loadedRadiologyExam) {
        this.loadedRadiologyExam = loadedRadiologyExam;
    }

    public Boolean getSaveAsList() {
        return saveAsList;
    }

    public void setSaveAsList(Boolean saveAsList) {
        this.saveAsList = saveAsList;
    }

    public List<SelectItem> getUrgencies() {
        return urgencies;
    }

    public void setUrgencies(List<SelectItem> urgencies) {
        this.urgencies = urgencies;
    }

    public Long getSelectedUrgencyId() {
        return selectedUrgencyId;
    }

    public void setSelectedUrgencyId(Long selectedUrgencyId) {
        this.selectedUrgencyId = selectedUrgencyId;
    }

    public AsapSector getSelectedAsapSectorId() {
        return selectedAsapSectorId;
    }

    public void setSelectedAsapSectorId(AsapSector selectedAsapSectorId) {
        this.selectedAsapSectorId = selectedAsapSectorId;
    }

    public Long getSelectedRisSector() {
        return selectedRisSector;
    }

    public void setSelectedRisSector(Long selectedRisSector) {
        this.selectedRisSector = selectedRisSector;
    }

    public Boolean getRedirectToEventCalendar() {
        return redirectToEventCalendar == null ? Boolean.FALSE
                : redirectToEventCalendar;
    }

    public void setRedirectToEventCalendar(Boolean redirectToEventCalendar) {
        this.redirectToEventCalendar = redirectToEventCalendar;
    }

    public List<Long> getListIds() {
        return listIds;
    }

    public void setListIds(List<Long> listIds) {
        this.listIds = listIds;
    }

    public List<SelectItem> getUserHospitals() {
        return userHospitals;
    }

    public void setUserHospitals(List<SelectItem> userHospitals) {
        this.userHospitals = userHospitals;
    }

    public Long getSelectedUserHospital() {
        return selectedUserHospital;
    }

    public void setSelectedUserHospital(Long selectedUserHospital) {
        this.selectedUserHospital = selectedUserHospital;
    }

    public Long getSelectedRadiologyExamType() {
        return selectedRadiologyExamType;
    }

    public void setSelectedRadiologyExamType(Long radiologyExamType) {
        this.selectedRadiologyExamType = radiologyExamType;
    }

    public List<SelectItem> getRadiologyExamTypes() {
        return radiologyExamTypes;
    }

    public void setRadiologyExamTypes(List<SelectItem> radiologyExamTypes) {
        this.radiologyExamTypes = radiologyExamTypes;
    }

    public FilterExamRequestItemWrapper getSelectedRadExamItemOption() {
        return selectedRadExamItemOption;
    }

    public void setSelectedRadExamItemOption(
            FilterExamRequestItemWrapper selectedRadExamItemOption) {
        this.selectedRadExamItemOption = selectedRadExamItemOption;
    }

    public boolean getAbleToChooseHospital() {
        return ableToChooseHospital;
    }

    public void setAbleToChooseHospital(boolean ableToChooseHospital) {
        this.ableToChooseHospital = ableToChooseHospital;
    }

    public FilterExamRequestItemWrapper getSelectedRadExamToDelete() {
        return selectedRadExamToDelete;
    }

    public void setSelectedRadExamToDelete(
            FilterExamRequestItemWrapper selectedRadExamToDelete) {
        this.selectedRadExamToDelete = selectedRadExamToDelete;
    }

    public RadiologyExamRequest getRadExReqToRedirect() {
        return radExReqToRedirect;
    }

    public void setRadExReqToRedirect(RadiologyExamRequest radExReqToRedirect) {
        this.radExReqToRedirect = radExReqToRedirect;
    }

    public List<AsapSector> getAsapSectors() {
        return asapSectors;
    }

    public void setAsapSectors(List<AsapSector> asapSectors) {
        this.asapSectors = asapSectors;
    }

    public List<SelectItem> getSectors() {
        return sectors;
    }

    public void setSectors(List<SelectItem> sectors) {
        this.sectors = sectors;
    }

    public Boolean getCupRequest() {
        return cupRequest;
    }

    public void setCupRequest(Boolean cupRequest) {
        this.cupRequest = cupRequest;
    }
}
