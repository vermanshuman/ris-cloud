package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.SpecialPermissionTypes;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.validation.AssignValidationException;
import it.nexera.ris.common.exceptions.validation.NotSelectedValidationException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.GeneralFunctionsHelper;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.RequestNote;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendar;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Diagnostic;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Urgency;
import it.nexera.ris.persistence.view.BaseWaitinglistView;
import it.nexera.ris.persistence.view.ShortWaitinglistView;
import it.nexera.ris.persistence.view.WaitinglistView;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.DocumentTemplateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.ExamTypeWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UrgencyWrapper;
import it.nexera.ris.web.beans.wrappers.logic.WaitingListRegistrationListBeanWrapper;
import it.nexera.ris.web.beans.wrappers.logic.WaitingListRegistrationStateWrapper;
import it.nexera.ris.web.common.EntityLazyListModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@Named("waitingListRegistrationListBean")
@ViewScoped
public class WaitingListRegistrationListBean extends
        EntityLazyListPageBean<WaitinglistView> implements Serializable {

    private static final String WAITING_LIST_CURRENT_TABLE_PAGE = "waiting_list_current_table_page";

    private static final Long NOT_FOUND_IN_DB = 0L;

    private static final long serialVersionUID = -7335945327218133347L;

    private final String ID_IN_SESSION_FOR_TAGS = "RadiologyExamsIdsForPrint";

    private List<SelectItem> templates;

    private Long modelTemplateIdCancel;

    private Long modelTemplateIdAnnulate;

    private Date modifiedReserveDate;

    private String commentCancel;

    private String commentAnnulate;

    private DocumentTemplateWrapper filledTemplate;

    private boolean disableTemplates;

    private BaseWaitinglistView selectedViewToCreatePdf;

    private boolean availableRegistrationInstantDate;

    private List<WaitingListRegistrationStateWrapper> allStates;

    private List<ExamTypeWrapper> allExamTypes;

    private List<WaitingListRegistrationStates> selectedStates;

    private List<Long> selectedExamTypes;

    private WaitingListRegistrationStateWrapper selectedStateForFilter;

    private ExamTypeWrapper selectedTypeForFilter;

    private List<UrgencyWrapper> allUrgencies;

    private List<Long> selectedUrgencies;

    private UrgencyWrapper selectedUrgencyForFilter;

    private Long selectedTemplateId;

    private Long requestIdToAddNote;

    private Long noteId;

    private String currentTablePage;

    private WaitingListRegistrationListBeanWrapper beanWrapper;

    private Boolean showFullView;

    private LazyDataModel<ShortWaitinglistView> shortLazyModel;

    private List<ShortWaitinglistView> shortLazyModelFiltered;

    private boolean showCancel;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        setBeanWrapper(new WaitingListRegistrationListBeanWrapper());
        setShowFullView(false);
        if (!this.isPostback()) {
            fillComboboxes();
            Boolean showError = (Boolean) this.getSession().get("showErrorMes");
            if (showError != null) {
                if (showError) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper
                                    .getValidation("dateOperationFailed"), "");
                }

                this.getSession().remove("showErrorMes");
            }

            getBeanWrapper().setNote(new RequestNote());
            getBeanWrapper().getNote().setNote("");
        }

        setAvailableRegistrationInstantDate(PermissionsHelper
                .getPermission(SpecialPermissionTypes.RESERVE));

        setShowCancel(PermissionsHelper
                .getPermission(SpecialPermissionTypes.CUPREQUESTS));

        filterTableFromPanel();

        if (!ValidationHelper.isNullOrEmpty(
                SessionHelper.get(WAITING_LIST_CURRENT_TABLE_PAGE))) {
            this.setCurrentTablePage((String) SessionHelper
                    .get(WAITING_LIST_CURRENT_TABLE_PAGE));
        } else if (!ValidationHelper.isNullOrEmpty(
                HttpSessionHelper.get(WAITING_LIST_CURRENT_TABLE_PAGE))) {
            this.setCurrentTablePage((String) HttpSessionHelper
                    .get(WAITING_LIST_CURRENT_TABLE_PAGE));
        } else {
            this.setCurrentTablePage("0");
        }

        executeJS(String.format("PF('tableWV').paginator.setPage( %s )",
                this.getCurrentTablePage()));
    }

    public void showAllRecords() {
        setShowFullView(true);
        filterTableFromPanel();
    }

    public void showNewRecords() {
        setShowFullView(false);
        filterTableFromPanel();
    }

    public void filterTableFromPanel() {
        this.cleanValidation();

        putToSession();

        try {
            this.fillLazyList();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void putToSession() {
        if (!ValidationHelper.isNullOrEmpty((this.getAllStates()))) {
            setSelectedStates(new ArrayList<WaitingListRegistrationStates>());

            for (WaitingListRegistrationStateWrapper wlrsw : this
                    .getAllStates()) {
                if (wlrsw.getSelected()) {
                    getSelectedStates().add(wlrsw.getRealState());
                }
            }
            SessionHelper.put(this.getSelectedStates(), "AgendaStates");
            HttpSessionHelper.put(this.getSelectedStates(), "AgendaStates");
        }

        if (!ValidationHelper.isNullOrEmpty((this.getAllExamTypes()))) {
            setSelectedExamTypes(new ArrayList<Long>());

            for (ExamTypeWrapper etw : this.getAllExamTypes()) {
                if (etw.getSelected()) {
                    getSelectedExamTypes().add(etw.getRealExamType().getId());
                }
            }
            SessionHelper.put(this.getSelectedExamTypes(), "AgendaExamTypes");
            HttpSessionHelper.put(this.getSelectedExamTypes(), "AgendaExamTypes");
        }

        if (!ValidationHelper.isNullOrEmpty((this.getAllUrgencies()))) {
            setSelectedUrgencies(new ArrayList<Long>());

            for (UrgencyWrapper uw : this.getAllUrgencies()) {
                if (uw.getSelected()) {
                    getSelectedUrgencies()
                            .add(uw.getId() == null ? 0L : uw.getId());
                }
            }
            SessionHelper.put(this.getSelectedUrgencies(), "AgendaUrgencies");
            HttpSessionHelper.put(this.getSelectedUrgencies(), "AgendaUrgencies");
        }
    }

    private void fillLazyList() throws PersistenceBeanException,
            IllegalAccessException {
        List<Criterion> criterias = new ArrayList<>();

        criterias.add(Restrictions.or(
                Restrictions.eq("disabledRequest", Boolean.FALSE),
                Restrictions.isNull("disabledRequest")));

        if (!ValidationHelper.isNullOrEmpty(getSelectedStates())) {
            criterias.add(Restrictions.in("waitingListRegistrationState",
                    getSelectedStates()));
        } else {
            criterias.add(Restrictions.not(Restrictions.in(
                    "waitingListRegistrationState",
                    new ArrayList<>(Arrays.asList(WaitingListRegistrationStates.values())))));
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedExamTypes())) {
            criterias
                    .add(Restrictions.in("examTypeId", getSelectedExamTypes()));
        } else {
            List<Long> ids = new ArrayList<>();

            if (!ValidationHelper.isNullOrEmpty(getAllExamTypes())) {
                for (ExamTypeWrapper ex : getAllExamTypes()) {
                    ids.add(ex.getRealExamType().getId());
                }
            }

            if (ids.isEmpty()) {
                ids.add(0L);
            }

            criterias.add(Restrictions.not(Restrictions.in("examTypeId", ids)));
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedUrgencies())) {
            if (getSelectedUrgencies().contains(0L)) {
                getSelectedUrgencies().remove(0L);

                if (getSelectedUrgencies().size() > 0) {
                    criterias.add(Restrictions.or(Restrictions
                            .isNull("urgencyId"), Restrictions.in("urgencyId",
                            getSelectedUrgencies())));
                } else {
                    criterias.add(Restrictions.isNull("urgencyId"));
                }

                getSelectedUrgencies().add(0L);
            } else {
                criterias.add(Restrictions.in("urgencyId",
                        getSelectedUrgencies()));
            }
        } else {
            List<Long> ids = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(getAllUrgencies())) {
                for (UrgencyWrapper uw : getAllUrgencies()) {
                    ids.add(uw.getId());
                }
                if (!ids.isEmpty()) {
                    criterias.add(Restrictions.not(Restrictions.in("urgencyId",
                            ids)));
                }
            }
        }
        criterias.add(Subqueries.propertyIn("id", GeneralFunctionsHelper
                .getRequestIdsWithDiagnostic(getShowFullView())));

        if (getShowFullView()) {
            this.loadList(WaitinglistView.class,
                    criterias.toArray(new Criterion[]{}), new Order[]{
                            Order.desc("requestDate"),
                    });
        } else {
            this.setShortLazyModel(new EntityLazyListModel<ShortWaitinglistView>(ShortWaitinglistView.class,
                    criterias.toArray(new Criterion[]{}), new Order[]{
                    Order.desc("requestDate"),
            }));
        }
    }

    public void selectStateForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getAllStates())) {
            for (WaitingListRegistrationStateWrapper wkrsw : this
                    .getAllStates()) {
                if (wkrsw != null
                        && wkrsw.customEquals(this.getSelectedStateForFilter())) {
                    wkrsw.setSelected(!wkrsw.getSelected());
                }
            }
        }
    }

    public boolean getSelectedAllStatesOnPanel() {
        if (this.getAllStates() != null) {
            for (WaitingListRegistrationStateWrapper wlrsw : this
                    .getAllStates()) {
                if (!wlrsw.getSelected()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllStatesOnPanel(boolean selectedAllStatesOnPanel) {
        if (this.getAllStates() != null) {
            for (WaitingListRegistrationStateWrapper wlrsw : this
                    .getAllStates()) {
                wlrsw.setSelected(selectedAllStatesOnPanel);
            }
        }
    }

    public void removeSessionAttributeAfterRender(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().remove("listIdsRequestItems");
        }
    }

    public void onRowToggle(ToggleEvent event) throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (event != null) {
            BaseWaitinglistView radExReq = (BaseWaitinglistView) event.getData();

            if (!ValidationHelper.isNullOrEmpty(radExReq)) {
                radExReq.loadLazyValues();

                radExReq.setExpanded(!radExReq.isExpanded());
                radExReq.setSelectedAll(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<SortMeta> getDefaultSortBy() {
        return (Collection<SortMeta>) SessionHelper.get(
                EntityLazyListModel.SORTFIELDS_WAITING_PAGE_IN_SESSION);
    }

    private void fillComboboxes() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        fillStates();
        fillExamTypes();
        fillUrgencies();
    }

    @SuppressWarnings("unchecked")
    private void fillUrgencies() {
        fillUrgenciesWrappers();

        if (SessionHelper.get("AgendaUrgencies") != null) {
            List<Long> selected = (List<Long>) SessionHelper
                    .get("AgendaUrgencies");

            for (UrgencyWrapper wrapp : getAllUrgencies()) {
                if (wrapp.getId() != null && selected.contains(wrapp.getId())) {
                    wrapp.setSelected(Boolean.TRUE);
                } else if (wrapp.getId() == null && selected.contains(0L)) {
                    wrapp.setSelected(Boolean.TRUE);
                } else {
                    wrapp.setSelected(Boolean.FALSE);
                }
            }
        } else if (HttpSessionHelper.get("AgendaUrgencies") != null) {
            List<Long> selected = (List<Long>) HttpSessionHelper
                    .get("AgendaUrgencies");

            for (UrgencyWrapper wrapp : getAllUrgencies()) {
                if (wrapp.getId() != null && selected.contains(wrapp.getId())) {
                    wrapp.setSelected(Boolean.TRUE);
                } else if (wrapp.getId() == null && selected.contains(0L)) {
                    wrapp.setSelected(Boolean.TRUE);
                } else {
                    wrapp.setSelected(Boolean.FALSE);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void fillExamTypes() {
        fillExamTypesWrappers();

        if (SessionHelper.get("AgendaExamTypes") != null) {
            List<Long> selected = (List<Long>) SessionHelper
                    .get("AgendaExamTypes");

            for (ExamTypeWrapper wrap : getAllExamTypes()) {
                if (selected.contains(wrap.getRealExamType().getId())) {
                    wrap.setSelected(Boolean.TRUE);
                } else {
                    wrap.setSelected(Boolean.FALSE);
                }
            }
        } else if (HttpSessionHelper.get("AgendaExamTypes") != null) {
            List<Long> selected = (List<Long>) HttpSessionHelper
                    .get("AgendaExamTypes");

            for (ExamTypeWrapper wrap : getAllExamTypes()) {
                if (selected.contains(wrap.getRealExamType().getId())) {
                    wrap.setSelected(Boolean.TRUE);
                } else {
                    wrap.setSelected(Boolean.FALSE);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void fillStates() {
        this.setAllStates(
                WaitingListStatusHelper.getWaitinglistStateWrappers());

        if (SessionHelper.get("AgendaStates") != null) {
            List<WaitingListRegistrationStates> selected = (List<WaitingListRegistrationStates>) SessionHelper
                    .get("AgendaStates");
            for (WaitingListRegistrationStateWrapper sw : getAllStates()) {
                if (selected.contains(sw.getRealState())) {
                    sw.setSelected(Boolean.TRUE);
                }
            }
        } else if (HttpSessionHelper.get("AgendaStates") != null) {
            List<WaitingListRegistrationStates> selected = (List<WaitingListRegistrationStates>) HttpSessionHelper
                    .get("AgendaStates");
            for (WaitingListRegistrationStateWrapper sw : getAllStates()) {
                if (selected.contains(sw.getRealState())) {
                    sw.setSelected(Boolean.TRUE);
                }
            }
        } else {
            for (WaitingListRegistrationStateWrapper sw : getAllStates()) {
                if (!sw.getRealState()
                        .equals(WaitingListRegistrationStates.DELETED)) {
                    sw.setSelected(Boolean.TRUE);
                }
            }
        }
    }

    public void fillTemplates() throws PersistenceBeanException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedViewToCreatePdf())) {
            setTemplates(GeneralFunctionsHelper.fillTemplates(
                    DocumentGenerationPlaces.WAITINGLIST, null, null));

            if (!ValidationHelper.isNullOrEmpty(getTemplates())) {
                setDisableTemplates(this.getTemplates().size() <= 1);
                setModelTemplateIdAnnulate(Long.valueOf(getTemplates().get(0)
                        .getValue().toString()));
                setModelTemplateIdCancel(Long.valueOf(getTemplates().get(0)
                        .getValue().toString()));
            } else {
                setDisableTemplates(true);
            }
        }
    }

    public void loadTemplate() {
        try {
            setTemplates(GeneralFunctionsHelper.fillTemplates(
                    DocumentGenerationPlaces.RESERVATION, getBeanWrapper().getSelectedRequest().getSector().getId(), null));
            chooseTemplate();
        } catch (HibernateException | IllegalAccessException
                | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
    }

    public void chooseTemplate() {
        try {
            if (!ValidationHelper.isNullOrEmpty(this.getTemplates())) {
                if (this.getTemplates().size() == 1) {
                    setSelectedTemplateId(Long.valueOf(getTemplates().get(0)
                            .getValue().toString()));
                    this.printReport();
                } else {
                    executeJS("PF('templates').show();");
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void actionOnPaging() {
        SessionHelper.put(this.getCurrentTablePage(),
                WAITING_LIST_CURRENT_TABLE_PAGE);
        HttpSessionHelper.put(this.getCurrentTablePage(),
                WAITING_LIST_CURRENT_TABLE_PAGE);
    }

    public void printReport() {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest())) {
            List<Long> radExamsIds = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest()
                    .getRadiologyExamRequestItems())) {
                for (RadiologyExamRequestItem reri : getBeanWrapper().getSelectedRequest()
                        .getRadiologyExamRequestItems()) {
                    Long id = reri.getRadiologyExam().getId();
                    radExamsIds.add(id);
                }
            }

            SessionHelper.putIds(radExamsIds, ID_IN_SESSION_FOR_TAGS);

            GeneralFunctionsHelper.showReport(getBeanWrapper().getSelectedRequest(),
                    getSelectedTemplateId(), getCurrentUser(), null, null,
                    false, null);

            SessionHelper.removeObject(ID_IN_SESSION_FOR_TAGS);
        }
    }

    public void showCancelReport() {
        if (this.getModelTemplateIdCancel() != null) {
            GeneralFunctionsHelper.showReport(getBeanWrapper().getSelectedRequest(),
                    getModelTemplateIdCancel(), getCurrentUser(),
                    getCommentCancel(), null, false, null);
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("templateNotSelected"), "");
        }
    }

    public void showAnnulateReport() {
        if (this.getModelTemplateIdAnnulate() != null) {
            GeneralFunctionsHelper.showReport(getBeanWrapper().getSelectedRequest(),
                    getModelTemplateIdAnnulate(), getCurrentUser(),
                    getCommentAnnulate(), null, false, null);
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("templateNotSelected"), "");
        }
    }

    public void viewCalendars() {
        WaitingListSessionHelper.cleanBackupFromSession();
        this.getSession().put("returnPage", this.getCurrentPage());
        RedirectHelper.goTo(PageTypes.DATE_CONFIRMATION);
    }

    public void viewReservationPage() {
        WaitingListSessionHelper.cleanBackupFromSession();
        RedirectHelper.goTo(PageTypes.RESERVATION_PAGE);
    }

    private boolean eventCalendarExist() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (getBeanWrapper().getSelectedRequest() != null) {
            List<Long> eventCalendars = DaoManager
                    .loadIds(EventCalendar.class, null);
            if (!ValidationHelper.isNullOrEmpty(eventCalendars)) {
                List<Long> toCheckRadiologyExamIds = null;
                try {
                    List<Object> radiologyExamIds = DaoManager.getFields(
                            RadiologyExamRequestItem.class, new Criterion[]{
                                    Restrictions.eq("radiologyExamRequest.id",
                                            getBeanWrapper().getSelectedRequest().getId())
                            }, new CriteriaAlias[]{
                                    new CriteriaAlias("radiologyExam",
                                            "radiologyExam",
                                            JoinType.INNER_JOIN)
                            }, true, "radiologyExam.id");

                    if (!ValidationHelper.isNullOrEmpty(radiologyExamIds)) {
                        toCheckRadiologyExamIds = new ArrayList<>();

                        for (Object obj : radiologyExamIds) {
                            toCheckRadiologyExamIds
                                    .add(Long.valueOf(obj.toString()));
                        }
                    }
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                if (!ValidationHelper.isNullOrEmpty(toCheckRadiologyExamIds)) {
                    for (Long ec : eventCalendars) {

                        List<Object> radExamIds = DaoManager.getFields(RadiologyExam.class,
                                new Criterion[]{
                                        Restrictions.eq("eventCalendar.id", ec)
                                }, new CriteriaAlias[]{
                                        new CriteriaAlias("diagnostics", "diagRelation", JoinType.INNER_JOIN),
                                        new CriteriaAlias("diagRelation.diagnostic", "diagnostics", JoinType.INNER_JOIN),
                                        new CriteriaAlias("diagnostics.eventCalendar", "eventCalendar", JoinType.INNER_JOIN)
                                }, true, "id");

                        List<Long> fullRadiologyExamIds = null;

                        if (!ValidationHelper.isNullOrEmpty(radExamIds)) {
                            fullRadiologyExamIds = new ArrayList<>();

                            for (Object obj : radExamIds) {
                                fullRadiologyExamIds
                                        .add(Long.valueOf(obj.toString()));
                            }
                        }

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

    public boolean agendaExist() {
        if (getBeanWrapper().getSelectedRequest() != null) {
            if (ValidationHelper
                    .isNullOrEmpty(getBeanWrapper().getSelectedRequest().getSector())) {

                return false;
            }

            try {
                Long countCalendar = DaoManager.getCount(EventCalendar.class,
                        "id", new CriteriaAlias[]{
                                new CriteriaAlias("diagnostic", "diagnostic",
                                        JoinType.INNER_JOIN),
                                new CriteriaAlias("diagnostic.sector", "sector",
                                        JoinType.INNER_JOIN)
                        }, new Criterion[]{
                                Restrictions.eq("sector.id",
                                        getBeanWrapper().getSelectedRequest().getSector()
                                                .getId())
                        });

                if (!NOT_FOUND_IN_DB.equals(countCalendar)) {
                    return true;
                } else {
                    Long countDiagnostic = DaoManager.getCount(Diagnostic.class,
                            "id", new CriteriaAlias[]{
                                    new CriteriaAlias("sector", "sector",
                                            JoinType.INNER_JOIN)
                            }, new Criterion[]{
                                    Restrictions.eq("sector.id",
                                            getBeanWrapper().getSelectedRequest().getSector()
                                                    .getId())
                            });

                    if (NOT_FOUND_IN_DB.equals(countDiagnostic)) {
                        showAssignErrorMsgForAgenda();

                        return false;
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        showAssignErrorMsg();

        return false;
    }

    // buttons

    public void assign() throws SecurityException, NoSuchFieldException,
            HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (agendaExist() && !checkIfRequestChangeStatus()) {
            if (eventCalendarExist()) {
                if (validateSelection()) {
                    WaitingListSessionHelper.backupToSession(getBeanWrapper().getSelectedRequest());
                    this.getSession().put("returnPage", this.getCurrentPage());
                    RedirectHelper.goTo(PageTypes.DATE_CONFIRMATION);
                } else {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper.getValidation("selectionEmpty"),
                            ResourcesHelper
                                    .getValidation("selectAtLeastOneItem"));
                }
            } else {
                showAssignErrorMsg();
            }
        }
    }

    public void cancel() {
        if (!ValidationHelper.isNullOrEmpty(this.getModelTemplateIdCancel())) {
            try {
                TransactionExecuter.execute(new Action() {
                    @Override
                    public void execute() throws Exception {
                        if (!ValidationHelper
                                .isNullOrEmpty(getBeanWrapper().getRadExamRequestItemsForCancel())) {
                            getSelectedViewToCreatePdf()
                                    .setRadiologyExamRequestItems(
                                            getBeanWrapper().getRadExamRequestItemsForCancel());
                            internalButtonAction(WaitingListRegistrationStates.DELETED);
                        }
                    }

                    @Override
                    public void onSuccess() {
                        MessageHelper.addGlobalMessage(
                                FacesMessage.SEVERITY_INFO,
                                ResourcesHelper
                                        .getString("dateOperationSuccessCancel"),
                                "");
                        executeJS("PF('cancelDlgWV').hide();PF('tableWV').filter();PF('tableWV').paginator.setPage(0); filterTableFromPanel();");
                    }

                    @Override
                    public void onException(Exception e) throws Exception {
                        innerOnException(e);
                    }

                });

                clearSession();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            MessageHelper
                    .addGlobalMessage(
                            FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper
                                    .getValidation("templateNotSelectedOperationAborted"),
                            "");
        }
    }

    public void restore() {
        try {
            if (this.getCanRestore()) {
                TransactionExecuter.execute(new Action() {
                    @Override
                    public void execute() throws Exception {
                        internalButtonAction(WaitingListRegistrationStates.REQUIRED);
                    }

                    @Override
                    public void onSuccess() {
                        MessageHelper.addGlobalMessage(
                                FacesMessage.SEVERITY_INFO,
                                ResourcesHelper
                                        .getString("dateOperationSuccessRestored"),
                                "");
                    }

                    @Override
                    public void onException(Exception e) throws Exception {
                        innerOnException(e);
                    }

                });
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void annulate() {
        if (this.getModelTemplateIdAnnulate() != null) {
            try {
                TransactionExecuter.execute(new Action() {
                    @Override
                    public void execute() throws Exception {
                        if (!ValidationHelper
                                .isNullOrEmpty(getBeanWrapper().getRadExamRequestItemsForAnnula())) {
                            getSelectedViewToCreatePdf()
                                    .setRadiologyExamRequestItems(
                                            getBeanWrapper().getRadExamRequestItemsForAnnula());
                            internalButtonAction(WaitingListRegistrationStates.ANNULLED);
                        }
                    }

                    @Override
                    public void onSuccess() {
                        MessageHelper.addGlobalMessage(
                                FacesMessage.SEVERITY_INFO,
                                ResourcesHelper
                                        .getString("dateOperationSuccessAnnulled"),
                                "");
                        executeJS("PF('annulateDlgWV').hide();PF('tableWV').filter();PF('tableWV').paginator.setPage(0); filterTableFromPanel();");
                    }

                    @Override
                    public void onException(Exception e) throws Exception {
                        innerOnException(e);
                    }

                });

                clearSession();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            MessageHelper
                    .addGlobalMessage(
                            FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper
                                    .getValidation("templateNotSelectedOperationAborted"),
                            "");
        }
    }

    public void instant() {
        try {
            if (this.getCanReserve() && !checkIfRequestChangeStatus()) {
                TransactionExecuter.execute(new Action() {
                    @Override
                    public void execute() throws Exception {
                        if (eventCalendarExist()) {
                            internalButtonAction(WaitingListRegistrationStates.RESERVED);
                        } else {
                            throw new AssignValidationException();
                        }
                    }

                    @Override
                    public void onSuccess() {
                        RedirectHelper.goTo(PageTypes.WORKLIST);
                    }

                    @Override
                    public void onException(Exception e) throws Exception {
                        innerOnException(e);
                    }

                });
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private boolean checkIfRequestChangeStatus() {
        WaitingListRegistrationStates state = getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState();
        if (WaitingListRegistrationStates.PARTIALLY_ACCEPTED.equals(state)
                || WaitingListRegistrationStates.IN_READING.equals(state)
                || WaitingListRegistrationStates.REPORTED.equals(state)
                || WaitingListRegistrationStates.DRAFT.equals(state)
                || WaitingListRegistrationStates.PERFORMED.equals(state)
                || WaitingListRegistrationStates.ACCEPTED.equals(state)
                || WaitingListRegistrationStates.SIGNED.equals(state)) {
            MessageHelper
                    .addGlobalMessage(
                            FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper
                                    .getValidation("requestChangeStatus"),
                            "");

            return true;
        }

        return false;
    }

    public void addNote() {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getNote().getNote())) {
            try {
                RequestNote note = getBeanWrapper().getNote();
                note.setNote(getBeanWrapper().getNote().getNote());
                if (!ValidationHelper.isNullOrEmpty(getRequestIdToAddNote())) {
                    RadiologyExamRequest req = DaoManager
                            .get(RadiologyExamRequest.class,
                                    getRequestIdToAddNote());

                    note.setRadiologyExamRequest(req);
                }
                DaoManager.save(note, true);
                executeJS("PF('addNoteWV').hide(); PF('tableWV').collapseAllRows()");
                PrimeFaces.current().ajax().update("table");
                getBeanWrapper().setNote(new RequestNote());
            } catch (HibernateException | InstantiationException
                    | IllegalAccessException | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        } else {
            this.addFieldExeption("form:inputNote", "workListNoAddNote");
        }
    }

    public void showNoteDlg() {
        if (!ValidationHelper.isNullOrEmpty(getNoteId())) {
            try {
                RequestNote note = DaoManager.get(RequestNote.class,
                        getNoteId());

                if (note != null) {
                    getBeanWrapper().setNote(note);
                    executeJS("PF('addNoteWV').show();");
                    this.setNoteId(null);
                }
            } catch (HibernateException | InstantiationException
                    | IllegalAccessException | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void deleteNote() {
        if (getNoteId() != null) {
            try {
                DaoManager.remove(RequestNote.class, getNoteId(), true);
                this.setNoteId(null);
            } catch (HibernateException | InstantiationException
                    | IllegalAccessException | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void onHideNoteDlg() {
        this.cleanValidation();
        getBeanWrapper().setNote(new RequestNote());
        getBeanWrapper().getNote().setNote("");
        PrimeFaces.current().ajax().update("inputNote");
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

    private void innerOnException(Exception e) {
        if (e instanceof NotSelectedValidationException) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("selectionEmpty"),
                    ResourcesHelper.getValidation("selectAtLeastOneItem"));
        } else if (e instanceof AssignValidationException) {
            showAssignErrorMsg();
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("dateOperationFailed"), "");
            LogHelper.log(log, e);
        }
    }

    public void openPSD() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedViewToCreatePdf())
                && !ValidationHelper.isNullOrEmpty(getSelectedViewToCreatePdf()
                .getPsdNumber())) {
            WaitingListStatusHelper.openPSD(getSelectedViewToCreatePdf()
                    .getPsdNumber());
        }
    }

    private void internalButtonAction(WaitingListRegistrationStates state)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, NotSelectedValidationException,
            AssignValidationException {
        if (validateSelection()) {
            WaitingListStatusHelper wlsh = new WaitingListStatusHelper();

            if (this.getSelectedViewToCreatePdf() != null) {
                this.getSelectedViewToCreatePdf().loadLazyValues();
            }

            if (this.getSelectedViewToCreatePdf() != null
                    && !ValidationHelper.isNullOrEmpty(this
                    .getSelectedViewToCreatePdf()
                    .getRadiologyExamRequestItems())) {

                getBeanWrapper().getSelectedRequest().setRadiologyExamRequestItems(
                        this.getSelectedViewToCreatePdf()
                                .getRadiologyExamRequestItems());

                List<Long> radExItemIds = wlsh.getSelectedIds(getBeanWrapper().getSelectedRequest());

                switch (state) {
                    case DELETED:
                        wlsh.toCancelState(getBeanWrapper().getSelectedRequest(),
                                radExItemIds, this.getCommentCancel(),
                                GeneralFunctionsHelper.generatePdf(
                                        getBeanWrapper().getSelectedRequest(),
                                        getModelTemplateIdCancel(),
                                        getCurrentUser(), getCommentCancel(),
                                        null, null, false, null));
                        break;
                    case REQUIRED:
                        wlsh.toRequiredState(getBeanWrapper().getSelectedRequest(),
                                radExItemIds);
                        break;
                    case ANNULLED:
                        wlsh.toAnnuledState(getBeanWrapper().getSelectedRequest(),
                                radExItemIds, this.getCommentAnnulate(),
                                GeneralFunctionsHelper.generatePdf(
                                        getBeanWrapper().getSelectedRequest(),
                                        getModelTemplateIdAnnulate(),
                                        getCurrentUser(), getCommentAnnulate(),
                                        null, null, false, null));
                        break;
                    case RESERVED:
                        wlsh.toReservedState(getBeanWrapper().getSelectedRequest(),
                                radExItemIds, Boolean.TRUE);
                        break;
                    default:
                        break;
                }
            }
        } else {
            throw new NotSelectedValidationException();
        }
    }

    private boolean validateSelection() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (getBeanWrapper().getSelectedRequest() != null) {
            if (this.getSelectedViewToCreatePdf() != null) {
                this.getSelectedViewToCreatePdf().loadLazyValues();
            }

            if (!ValidationHelper.isNullOrEmpty(getSelectedViewToCreatePdf()
                    .getRadiologyExamRequestItems())) {
                getBeanWrapper().getSelectedRequest().setRadiologyExamRequestItems(
                        getSelectedViewToCreatePdf()
                                .getRadiologyExamRequestItems());
                getBeanWrapper().getSelectedRequest().setExpanded(
                        getSelectedViewToCreatePdf().isExpanded());

                if (getBeanWrapper().getSelectedRequest().isExpanded()) {
                    for (RadiologyExamRequestItem wlr : getBeanWrapper().getSelectedRequest()
                            .getRadiologyExamRequestItems()) {
                        if (wlr.getSelected()) {
                            return true;
                        }
                    }
                } else {
                    for (RadiologyExamRequestItem wlr : getBeanWrapper().getSelectedRequest()
                            .getRadiologyExamRequestItems()) {
                        wlr.setSelected(Boolean.TRUE);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public void deselectToCancel() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (getBeanWrapper().getSelectedRequest() != null) {
            this.getSelectedViewToCreatePdf().loadLazyValues();

            for (RadiologyExamRequestItem item : this
                    .getSelectedViewToCreatePdf()
                    .getRadiologyExamRequestItems()) {
                item.setSelected(Boolean.FALSE);
            }
        }
    }

    public RadiologyExamRequest[] getSelectedToTable() {
        if (getBeanWrapper().getSelectedRequest() != null) {
            return new RadiologyExamRequest[]{
                    getBeanWrapper().getSelectedRequest()
            };
        }
        return null;
    }

    public boolean isDisableTemplates() {
        return disableTemplates;
    }

    public void setDisableTemplates(boolean disableTemplates) {
        this.disableTemplates = disableTemplates;
    }

    public void setSelectedViewToCreatePdf(
            BaseWaitinglistView selectedViewToCreatePdf) {
        this.selectedViewToCreatePdf = selectedViewToCreatePdf;

        if (selectedViewToCreatePdf != null) {
            try {
                getBeanWrapper().setSelectedRequest(DaoManager.get(RadiologyExamRequest.class,
                        selectedViewToCreatePdf.getId()));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public boolean isAvailableRegistrationInstantDate() {
        return availableRegistrationInstantDate;
    }

    public void setAvailableRegistrationInstantDate(
            boolean availableRegistrationInstantDate) {
        this.availableRegistrationInstantDate = availableRegistrationInstantDate;
    }

    public boolean getSelectedAllExamTypesOnPanel() {
        if (this.getAllExamTypes() != null) {
            for (ExamTypeWrapper etw : this.getAllExamTypes()) {
                if (!etw.getSelected()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllExamTypesOnPanel(
            boolean selectedAllExamTypesOnPanel) {
        if (this.getAllExamTypes() != null) {
            for (ExamTypeWrapper etw : this.getAllExamTypes()) {
                etw.setSelected(selectedAllExamTypesOnPanel);
            }
        }
    }

    public void selectExamTypeForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getAllExamTypes())) {
            for (ExamTypeWrapper ekrsw : this.getAllExamTypes()) {
                if (ekrsw != null
                        && ekrsw.customEquals(this.getSelectedTypeForFilter())) {
                    ekrsw.setSelected(!ekrsw.getSelected());
                }
            }
        }
    }

    private void fillExamTypesWrappers() {
        List<ExamTypeWrapper> examTypes = new ArrayList<>();
        try {
            List<ExamType> types = DaoManager.load(ExamType.class);
            if (!ValidationHelper.isNullOrEmpty(types)) {
                for (ExamType examType : types) {
                    ExamTypeWrapper etw = new ExamTypeWrapper(examType);
                    etw.setSelected(true);
                    examTypes.add(etw);
                }
            }
            this.setAllExamTypes(examTypes);
            this.setSelectedAllExamTypesOnPanel(true);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public boolean getSelectedAllUrgenciesOnPanel() {
        if (this.getAllUrgencies() != null) {
            for (UrgencyWrapper uw : this.getAllUrgencies()) {
                if (!uw.getSelected()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllUrgenciesOnPanel(
            boolean selectedAllUrgenciesOnPanel) {
        if (this.getAllUrgencies() != null) {
            for (UrgencyWrapper uw : this.getAllUrgencies()) {
                uw.setSelected(selectedAllUrgenciesOnPanel);
            }
        }
    }

    public void selectUrgencyForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getAllUrgencies())) {
            for (UrgencyWrapper ukrsw : this.getAllUrgencies()) {
                if (ukrsw != null
                        && ukrsw.customEquals(this
                        .getSelectedUrgencyForFilter())) {
                    ukrsw.setSelected(!ukrsw.getSelected());
                }
            }
        }
    }

    private void fillUrgenciesWrappers() {
        List<UrgencyWrapper> list = new ArrayList<>();
        try {
            List<Urgency> urgencies = DaoManager.load(Urgency.class);
            if (!ValidationHelper.isNullOrEmpty(urgencies)) {
                for (Urgency urg : urgencies) {
                    UrgencyWrapper uw = new UrgencyWrapper(urg);
                    uw.setSelected(true);
                    list.add(uw);
                }
            }
            setSelectedAllUrgenciesOnPanel(true);
            this.setAllUrgencies(list);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public Boolean getShowFullView() {
        return showFullView;
    }

    public void setShowFullView(Boolean showFullView) {
        this.showFullView = showFullView;
    }

    public boolean isShowCancel() {
        return showCancel;
    }

    public void setShowCancel(boolean showCancel) {
        this.showCancel = showCancel;
    }
}
