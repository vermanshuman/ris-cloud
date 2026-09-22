package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.UserPreference;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Printer;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import it.nexera.ris.web.beans.wrappers.logic.WaitingListRegistrationStateWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.PrimeFaces;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Named("userProfileViewBean")
@ViewScoped
public class UserProfileViewBean extends EntityEditPageBean<User> implements
        Serializable {
    private static final long serialVersionUID = 7377804605535599777L;

    private static final String PASSWORD_EXPIRED_PARAM = "password_expired";

    public static final String ZOOM_COOKIE_PREFERENCE = "profile_zoom_value";

    @Inject
    private PasswordEncoder passwordEncoder;

    private String oldPwd;

    private String pwd;

    private String confirmPwd;

    private Integer defaultTabIndex;

    private Boolean disableFunctionality;

    private List<SelectItem> allStates;

    private List<String> selectedStates;

    private List<SelectItem> allExamTypes;

    private List<String> selectedExams;

    private List<SelectItem> itemsPPList;

    private List<SelectItem> useDescriptionList;

    private UserPreference itemsPerPagePreference;

    private UserPreference orderTypePreference;

    private List<UserPreference> wlStatesPreferences;

    private List<UserPreference> examTypesPreferences;

    private UserPreference useDescriptionPreference;

    private UserPreference labelPrintActivePreference;

    private UserPreference labelPrintPrinterNamePreference;

    private UserPreference labelPrintCountPreference;

    private UserPreference labelPrintActivePreferenceDocg;

    private UserPreference labelPrintPrinterNamePreferenceDocg;

    private UserPreference labelPrintCountPreferenceDocg;

    private UserPreference autoSaveDocgPreference;

    private UserPreference openPDFDocument;

    private UserPreference referringDiction;

    private UserPreference vocalRecognition;

    private List<SelectItem> ordersList;

    private UserPreference orderPreference;

    private UserPreference pdfZoomPreference;

    private UserPreference priorityAlertPreference;

    private List<SelectItem> pdfZoomList;

    private List<SelectItem> orderTypesList;

    private List<SelectItem> priorityAlertTypesList;

    private List<SelectItem> printers;

    private List<SelectItem> printersDocg;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.setDefaultTabIndex(Integer.valueOf(0));

        if (!this.isPostback()) {
            this.setDisableFunctionality(Boolean.FALSE);
        }

        try {
            this.setEntity(DaoManager.get(User.class, this.getCurrentUser()
                    .getId()));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (getRequestParameter(PASSWORD_EXPIRED_PARAM) != null) {
            this.setDefaultTabIndex(Integer.valueOf(1));
            this.setDisableFunctionality(Boolean.TRUE);
        }

        if (!this.isPostback() && this.getDisableFunctionality().booleanValue()) {
            addPasswordExpiredMessage();
        }

        fillComboBoxes();
        fillSelectedPrefs();
    }

    private void fillComboBoxes() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        fillItemsPP();
        fillStates();
        fillExamTypes();
        fillOrders();
        fillOrderTypes();
        fillPDFZooms();
        fillUseDescriptions();
        fillPrinters();
        fillPrintersDocg();
        fillPriorityAlertTypes();
    }

    private void fillSelectedPrefs() {
        fillAllpreferences();
        fillPrefsPP();
        fillPrefsWlStates();
        fillPrefsExamTypes();
        fillPrefsOrder();
        fillPrefsOrderType();
        fillPrefsPdfZoom();
        fillPrefsUseDescription();
        fillPrefsLabelPrint();
        fillAutoSavePref();
        fillOpenPdfDocument();
        fillPrefsPriorityAlert();
        fillReferringDictionPref();
        fillVocalRecognition();
    }

    private void fillAutoSavePref() {

        if (this.getAutoSaveDocgPreference() == null) {
            UserPreference autoSaveDocgPref = new UserPreference();
            autoSaveDocgPref.setUser(this.getEntity());
            autoSaveDocgPref.setType(UserPreferenceType.AUTO_SAVE_DOCG);
            autoSaveDocgPref.setAutoSaveDocg(Boolean.FALSE);
            this.setAutoSaveDocgPreference(autoSaveDocgPref);
        }
    }

    private void fillReferringDictionPref() {

        if (this.getReferringDiction() == null) {
            UserPreference referringDiction = new UserPreference();
            referringDiction.setUser(this.getEntity());
            referringDiction.setType(UserPreferenceType.REFERRING_DICTION);
            this.setReferringDiction(referringDiction);
        }
    }

    private void fillVocalRecognition() {
        if (this.getVocalRecognition() == null) {
            UserPreference vocalRecognition = new UserPreference();
            vocalRecognition.setUser(this.getEntity());
            vocalRecognition.setType(UserPreferenceType.VOCAL_RECOGNITION);
            vocalRecognition.setVocalRecognition(Boolean.FALSE);
            this.setVocalRecognition(vocalRecognition);
        }
    }

    private void fillOpenPdfDocument() {

        if (this.getOpenPDFDocument() == null) {
            UserPreference openPDFDocPref = new UserPreference();
            openPDFDocPref.setUser(this.getEntity());
            openPDFDocPref.setType(UserPreferenceType.OPEN_PDF_DOCUMENT);
            openPDFDocPref.setOpenPdfDocument(Boolean.FALSE);
            this.setOpenPDFDocument(openPDFDocPref);
        }
    }

    private void fillUseDescriptions() {
        this.setUseDescriptionList(new ArrayList<SelectItem>());
        this.getUseDescriptionList().add(
                new SelectItem(Boolean.FALSE, ResourcesHelper
                        .getString("userPrefUseDescriptionStandart")));
        this.getUseDescriptionList().add(
                new SelectItem(Boolean.TRUE, ResourcesHelper
                        .getString("userPrefUseDescriptionBlank")));
    }

    private void fillPDFZooms() {
        this.setPdfZoomList(new ArrayList<SelectItem>());
        this.getPdfZoomList().add(new SelectItem(100l, "100"));
        this.getPdfZoomList().add(new SelectItem(125l, "125"));
        this.getPdfZoomList().add(new SelectItem(150l, "150"));
        this.getPdfZoomList().add(new SelectItem(175l, "175"));
        this.getPdfZoomList().add(new SelectItem(200l, "200"));
        this.getPdfZoomList().add(new SelectItem(250l, "250"));
        this.getPdfZoomList().add(new SelectItem(300l, "300"));
        this.getPdfZoomList().add(new SelectItem(350l, "350"));
    }

    private void fillPrinters() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {

        this.setPrinters(new ArrayList<SelectItem>());
        this.getPrinters().add(new SelectItem(null, "Default"));
        for (Printer printer : DaoManager.load(Printer.class)) {
            this.getPrinters().add(new SelectItem(printer.getName()));
        }

    }

    private void fillPrintersDocg() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {

        this.setPrintersDocg(new ArrayList<SelectItem>());
        this.getPrintersDocg().add(new SelectItem(null, "Default"));
        for (Printer printer : DaoManager.load(Printer.class)) {
            this.getPrintersDocg().add(new SelectItem(printer.getName()));
        }

    }

    private void fillAllpreferences() {
        List<UserPreference> prefs = null;

        try {
            prefs = DaoManager.load(UserPreference.class, new Criterion[]{
                    Restrictions.eq("user.id", this.getEntityId())
            });
        } catch (HibernateException | IllegalAccessException
                | PersistenceBeanException e) {
            log.error("Error while loading preferences", e);
            LogHelper.log(log, e);
        }

        this.setWlStatesPreferences(new ArrayList<UserPreference>());
        this.setExamTypesPreferences(new ArrayList<UserPreference>());

        if (!ValidationHelper.isNullOrEmpty(prefs)) {
            for (UserPreference pref : prefs) {
                if (pref.getType() != null) {

                    switch (pref.getType()) {
                        case WORKLIST_STATE:
                            getWlStatesPreferences().add(pref);
                            break;
                        case WORKLIST_EXAM_TYPE:
                            getExamTypesPreferences().add(pref);
                            break;
                        case PDF_USE_DESCRIPTION:
                            setUseDescriptionPreference(pref);
                            break;
                        case PDF_ZOOM:
                            setPdfZoomPreference(pref);
                            break;
                        case WORKLIST_ITEMS_PER_PAGE:
                            setItemsPerPagePreference(pref);
                            break;
                        case WORKLIST_ORDER:
                            setOrderPreference(pref);
                            break;
                        case WORKLIST_ORDER_TYPE:
                            setOrderTypePreference(pref);
                            break;
                        case LABEL_PRINT_ACTIVE:
                            setLabelPrintActivePreference(pref);
                            break;
                        case LABEL_PRINT_COUNT:
                            setLabelPrintCountPreference(pref);
                            break;
                        case LABEL_PRINT_PRINTER_NAME:
                            setLabelPrintPrinterNamePreference(pref);
                            break;
                        case LABEL_PRINT_ACTIVE_DOCG:
                            setLabelPrintActivePreferenceDocg(pref);
                            break;
                        case LABEL_PRINT_COUNT_DOCG:
                            setLabelPrintCountPreferenceDocg(pref);
                            break;
                        case LABEL_PRINT_PRINTER_NAME_DOCG:
                            setLabelPrintPrinterNamePreferenceDocg(pref);
                            break;
                        case AUTO_SAVE_DOCG:
                            setAutoSaveDocgPreference(pref);
                            break;
                        case OPEN_PDF_DOCUMENT:
                            setOpenPDFDocument(pref);
                            break;
                        case PRIORITY_ALERT:
                            setPriorityAlertPreference(pref);
                            break;
                        case REFERRING_DICTION:
                            setReferringDiction(pref);
                            break;
                        case VOCAL_RECOGNITION:
                            setVocalRecognition(pref);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void fillPrefsPdfZoom() {
        if (this.getPdfZoomPreference() == null) {
            UserPreference pdfZoomPref = new UserPreference();
            pdfZoomPref.setUser(this.getEntity());
            pdfZoomPref.setType(UserPreferenceType.PDF_ZOOM);
            pdfZoomPref.setPdfZoom(100l);
            this.setPdfZoomPreference(pdfZoomPref);
        }
    }

    private void fillPrefsPriorityAlert() {
        if (this.getPriorityAlertPreference() == null) {
            UserPreference pdfZoomPref = new UserPreference();
            pdfZoomPref.setUser(this.getEntity());
            pdfZoomPref.setType(UserPreferenceType.PRIORITY_ALERT);
            pdfZoomPref.setPriorityAlertType(PriorityAlertTypes.NOBODY);
            this.setPriorityAlertPreference(pdfZoomPref);
        }
    }

    private void fillPrefsLabelPrint() {

        if (this.getLabelPrintActivePreference() == null) {
            UserPreference lablePrintActivePref = new UserPreference();
            lablePrintActivePref.setLabelPrintActive(Boolean.FALSE);
            lablePrintActivePref.setType(UserPreferenceType.LABEL_PRINT_ACTIVE);
            lablePrintActivePref.setUser(this.getEntity());
            this.setLabelPrintActivePreference(lablePrintActivePref);
        }

        if (this.getLabelPrintPrinterNamePreference() == null) {
            UserPreference lablePrintPrinterNamePref = new UserPreference();
            lablePrintPrinterNamePref.setUser(this.getEntity());
            lablePrintPrinterNamePref
                    .setType(UserPreferenceType.LABEL_PRINT_PRINTER_NAME);
            this.setLabelPrintPrinterNamePreference(lablePrintPrinterNamePref);
        }

        if (this.getLabelPrintCountPreference() == null) {
            UserPreference lablePrintCountPref = new UserPreference();
            lablePrintCountPref.setUser(this.getEntity());
            lablePrintCountPref.setType(UserPreferenceType.LABEL_PRINT_COUNT);
            this.setLabelPrintCountPreference(lablePrintCountPref);
        }

        if (this.getLabelPrintActivePreferenceDocg() == null) {
            UserPreference lablePrintActivePrefDocg = new UserPreference();
            lablePrintActivePrefDocg.setLabelPrintActive(Boolean.FALSE);
            lablePrintActivePrefDocg
                    .setType(UserPreferenceType.LABEL_PRINT_ACTIVE_DOCG);
            lablePrintActivePrefDocg.setUser(this.getEntity());
            this.setLabelPrintActivePreferenceDocg(lablePrintActivePrefDocg);
        }

        if (this.getLabelPrintPrinterNamePreferenceDocg() == null) {
            UserPreference lablePrintPrinterNamePrefDocg = new UserPreference();
            lablePrintPrinterNamePrefDocg.setUser(this.getEntity());
            lablePrintPrinterNamePrefDocg
                    .setType(UserPreferenceType.LABEL_PRINT_PRINTER_NAME_DOCG);
            this.setLabelPrintPrinterNamePreferenceDocg(lablePrintPrinterNamePrefDocg);
        }

        if (this.getLabelPrintCountPreferenceDocg() == null) {
            UserPreference lablePrintCountPrefDocg = new UserPreference();
            lablePrintCountPrefDocg.setUser(this.getEntity());
            lablePrintCountPrefDocg
                    .setType(UserPreferenceType.LABEL_PRINT_COUNT_DOCG);
            this.setLabelPrintCountPreferenceDocg(lablePrintCountPrefDocg);
        }

    }

    private void fillPrefsExamTypes() {
        this.setSelectedExams(new ArrayList<String>());

        if (!ValidationHelper.isNullOrEmpty(this.getExamTypesPreferences())) {
            for (SelectItem item : this.getAllExamTypes()) {
                for (UserPreference pref : this.getExamTypesPreferences()) {
                    if (pref.getExamType().getId()
                            .equals(Long.parseLong((String) item.getValue()))) {
                        this.getSelectedExams().add((String) item.getValue());
                        break;
                    }
                }
            }
        }
    }

    private void fillPrefsWlStates() {
        this.setSelectedStates(new ArrayList<String>());
        if (!ValidationHelper.isNullOrEmpty(this.getWlStatesPreferences())) {
            for (UserPreference pref : this.getWlStatesPreferences()) {
                for (SelectItem state : this.getAllStates()) {

                    if (pref.getState().name().equals(state.getValue())) {
                        this.getSelectedStates().add((String) state.getValue());
                        break;
                    }
                }
            }
        }
    }

    private void fillPrefsUseDescription() {
        if (this.getUseDescriptionPreference() == null) {
            UserPreference useDescrPref = new UserPreference();
            useDescrPref.setUser(this.getEntity());
            useDescrPref.setType(UserPreferenceType.PDF_USE_DESCRIPTION);
            useDescrPref.setUseDescription(Boolean.FALSE);
            this.setUseDescriptionPreference(useDescrPref);
        }
    }

    private void fillPrefsPP() {
        if (this.getItemsPerPagePreference() == null) {
            UserPreference itemsPPPref = new UserPreference();
            itemsPPPref.setUser(this.getEntity());
            itemsPPPref.setType(UserPreferenceType.WORKLIST_ITEMS_PER_PAGE);
            itemsPPPref.setItemsPerPage(10l);
            this.setItemsPerPagePreference(itemsPPPref);
        }
    }

    private void fillPrefsOrder() {
        if (this.getOrderPreference() == null) {
            UserPreference orderPref = new UserPreference();
            orderPref.setUser(this.getEntity());
            orderPref.setType(UserPreferenceType.WORKLIST_ORDER);
            orderPref.setOrderColumn(WorkListOrderColumns.NAME);
            this.setOrderPreference(orderPref);
        }
    }

    private void fillPrefsOrderType() {
        if (this.getOrderTypePreference() == null) {
            UserPreference orderTypePref = new UserPreference();
            orderTypePref.setUser(this.getEntity());
            orderTypePref.setType(UserPreferenceType.WORKLIST_ORDER_TYPE);
            orderTypePref.setOrderColumnType(OrderType.DESCENDING);
            this.setOrderTypePreference(orderTypePref);
        }
    }

    private void fillStates() {
        this.setAllStates(new ArrayList<SelectItem>());
        for (WaitingListRegistrationStateWrapper wrap : WaitingListStatusHelper
                .getWorklistStateWrappers()) {
            this.getAllStates().add(
                    new SelectItem(wrap.getRealState().name(), wrap
                            .getRealState().toString()));
        }
    }

    private void fillOrders() {
        ArrayList<SelectItem> orders = new ArrayList<SelectItem>();

        for (WorkListOrderColumns order : WorkListOrderColumns.values()) {
            orders.add(new SelectItem(order, order.toString()));
        }
        this.setOrdersList(orders);
    }

    private void fillOrderTypes() {
        ArrayList<SelectItem> orderTypes = new ArrayList<SelectItem>();

        for (OrderType order : OrderType.values()) {
            orderTypes.add(new SelectItem(order, order.toString()));
        }
        this.setOrderTypesList(orderTypes);
    }

    private void fillPriorityAlertTypes() {
        List<SelectItem> priorityTypes = new ArrayList<>();

        for (PriorityAlertTypes priority : PriorityAlertTypes.values()) {
            priorityTypes.add(new SelectItem(priority, priority.toString()));
        }
        this.setPriorityAlertTypesList(priorityTypes);
    }

    @Override
    public void goBack() {
        if (!this.getDisableFunctionality().booleanValue()) {
            RedirectHelper.goTo(PageTypes.WORKLIST);
            this.getViewState().clear();
        } else {
            addPasswordExpiredMessage();
        }
    }

    private void addPasswordExpiredMessage() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                ResourcesHelper
                        .getValidation("profilePasswordExpiredMessageTitle"),
                ResourcesHelper
                        .getValidation("profilePasswordExpiredMessageBody"));
    }

    @Override
    public void onValidate() throws PersistenceBeanException {
        boolean bValid = true;
        if (!ValidationHelper.isNullOrEmpty(this.getOldPwd())
                || !ValidationHelper.isNullOrEmpty(this.getPwd())
                || !ValidationHelper.isNullOrEmpty(this.getConfirmPwd())) {
            if (ValidationHelper.isNullOrEmpty(this.getOldPwd())) {
                this.addFieldExeption("tabs:oldPassword", "requiredOldPassword");
                bValid = false;

            } else if (!passwordEncoder.matches(getOldPwd(), getEntity().getPassword())) {
                addFieldExeption("tabs:oldPassword", "wrongPassword");
                bValid = false;
            }

            if (ValidationHelper.isNullOrEmpty(this.getPwd())) {
                this.addFieldExeption("tabs:newPassword", "requiredNewPassword");
                bValid = false;
            }

            if (ValidationHelper.isNullOrEmpty(this.getConfirmPwd())) {
                this.addFieldExeption("tabs:confirmationPassword",
                        "requiredConfirmPassword");
                bValid = false;
            }

            if (!ValidationHelper.isNullOrEmpty(this.getConfirmPwd())
                    && !ValidationHelper.isNullOrEmpty(this.getPwd())
                    && !this.getConfirmPwd().equals(this.getPwd())) {
                this.addFieldExeption("tabs:newPassword", "passwordMissmatch");
                this.addFieldExeption("tabs:confirmationPassword",
                        "passwordMissmatch", Boolean.FALSE);
                bValid = false;
            }

//            if (!ValidationHelper.isNullOrEmpty(this.getConfirmPwd())
//                    && !ValidationHelper.isNullOrEmpty(this.getPwd())
//                    && this.getConfirmPwd().equals(this.getPwd())) {
//                if (!ValidationHelper.checkFieldLength(this.getPwd(), 8, 40)
//                        && !ValidationHelper.checkFieldLength(this.getConfirmPwd(), 8, 40)) {
//                    this.addFieldExeption("tabs:newPassword",
//                            "passwordFormatError");
//                    this.addFieldExeption("tabs:confirmationPassword",
//                            "passwordFormatError", Boolean.FALSE);
//                    bValid = false;
//                } else if (!ValidationHelper.checkCorrectFormatByExpression(
//                        UserEditBean.PASSWORD_PATTERN, this.getPwd().trim())) {
//                    this.addFieldExeption("tabs:newPassword",
//                            "passwordFormatError");
//                    this.addFieldExeption("tabs:confirmationPassword",
//                            "passwordFormatError", Boolean.FALSE);
//                    bValid = false;
//                }
//            }

            if (bValid) {
                if (!ValidationHelper.isNullOrEmpty(this.getOldPwd())
                        && !ValidationHelper.isNullOrEmpty(this.getPwd())) {
                    if (this.getOldPwd().equals(this.getPwd())) {
                        this.addFieldExeption("tabs:newPassword",
                                "passwordChangePasswordShouldBeDifferent");
                        this.addFieldExeption("tabs:confirmationPassword",
                                "passwordChangePasswordShouldBeDifferent",
                                Boolean.FALSE);
                        bValid = false;
                    }
                }
            }
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getPwd())) {
            this.getEntity().setPassword(passwordEncoder.encode(getPwd()));
            this.getContext().getExternalContext().getApplicationMap()
                    .put("reload_users", Boolean.TRUE);
            this.getEntity().setPasswordChangeDate(new Date());
            this.getEntity().setNeedToChangePassword(false);
        }

        DaoManager.save(this.getEntity());

        this.setDisableFunctionality(Boolean.FALSE);
        if (this.getEntity().getId().equals(this.getCurrentUser().getId())) {
            UserHolder.getInstance().setCurrentUser(
                    UserWrapper.wrap(getEntity(), DaoManager.getSession()));
        }

        DaoManager.save(this.getItemsPerPagePreference());

        DaoManager.save(this.getOrderPreference());
        DaoManager.save(this.getOrderTypePreference());
        DaoManager.save(this.getPdfZoomPreference());
        DaoManager.save(this.getUseDescriptionPreference());
        DaoManager.save(this.getLabelPrintActivePreference());
        DaoManager.save(this.getLabelPrintCountPreference());
        DaoManager.save(this.getLabelPrintPrinterNamePreference());
        DaoManager.save(this.getLabelPrintActivePreferenceDocg());
        DaoManager.save(this.getLabelPrintCountPreferenceDocg());
        DaoManager.save(this.getLabelPrintPrinterNamePreferenceDocg());
        DaoManager.save(this.getAutoSaveDocgPreference());
        DaoManager.save(this.getOpenPDFDocument());
        DaoManager.save(this.getPriorityAlertPreference());
        DaoManager.save(this.getReferringDiction());
        DaoManager.save(this.getVocalRecognition());

        saveStates();
        saveExamTypes();

        CookieHelper.setCookie(ZOOM_COOKIE_PREFERENCE, getPdfZoomPreference().getPdfZoom().toString());
    }

    private void saveExamTypes() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        outer:
        for (String id : this.getSelectedExams()) {
            for (UserPreference pref : this.getExamTypesPreferences()) {
                if (pref.getExamType().getId().equals(Long.parseLong(id))) {
                    continue outer;
                }
            }
            UserPreference newPref = new UserPreference();

            newPref.setUser(this.getEntity());
            newPref.setType(UserPreferenceType.WORKLIST_EXAM_TYPE);
            newPref.setExamType(DaoManager.get(ExamType.class, id));

            DaoManager.save(newPref);
        }

        outer:
        for (UserPreference pref : this.getExamTypesPreferences()) {
            for (String id : this.getSelectedExams()) {
                if (new Long(Long.parseLong(id)).equals(pref.getExamType()
                        .getId())) {
                    continue outer;
                }
            }
            DaoManager.remove(pref);
        }
    }

    private void saveStates() throws HibernateException,
            PersistenceBeanException {
        outer:
        for (String state : this.getSelectedStates()) {
            for (UserPreference pref : this.getWlStatesPreferences()) {

                if (pref.getState().equals(
                        WaitingListRegistrationStates.valueOf(state))) {
                    continue outer;
                }
            }

            UserPreference newPref = new UserPreference();

            newPref.setUser(this.getEntity());
            newPref.setType(UserPreferenceType.WORKLIST_STATE);
            newPref.setState(WaitingListRegistrationStates.valueOf(state));

            DaoManager.save(newPref);
        }

        outer:
        for (UserPreference pref : this.getWlStatesPreferences()) {
            for (String state : this.getSelectedStates()) {
                if (state.equals(pref.getState().name())) {
                    continue outer;
                }
            }
            DaoManager.remove(pref);
        }
    }

    private void fillItemsPP() {
        this.setItemsPPList(new ArrayList<SelectItem>());
        this.getItemsPPList().add(new SelectItem(10l, "10"));
        this.getItemsPPList().add(new SelectItem(20l, "20"));
        this.getItemsPPList().add(new SelectItem(50l, "50"));
    }

    private void fillExamTypes() {
        List<SelectItem> examTypes = new ArrayList<>();
        try {
            List<ExamType> types = DaoManager.load(ExamType.class);
            if (!ValidationHelper.isNullOrEmpty(types)) {
                for (ExamType examType : types) {

                    examTypes.add(new SelectItem(String.valueOf(examType
                            .getId()), examType.toString()));
                }
            }
            this.setAllExamTypes(examTypes);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void glossaryManagement() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("draggable", Boolean.FALSE);
        options.put("modal", Boolean.TRUE);
        options.put("resizable", Boolean.FALSE);
        options.put("width", 967);
        options.put("contentHeight", 450);
        options.put("contentWidth", 940);

        PrimeFaces.current().dialog()
                .openDynamic("/Pages/ManagementGroup/glossaryDialog.xhtml", options, null);
    }

    public String getOldPwd() {
        return oldPwd;
    }

    public void setOldPwd(String oldPwd) {
        this.oldPwd = oldPwd;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getConfirmPwd() {
        return confirmPwd;
    }

    public void setConfirmPwd(String confirmPwd) {
        this.confirmPwd = confirmPwd;
    }

    public Integer getDefaultTabIndex() {
        return defaultTabIndex;
    }

    public void setDefaultTabIndex(Integer defaultTabIndex) {
        this.defaultTabIndex = defaultTabIndex;
    }

    public Boolean getDisableFunctionality() {
        return disableFunctionality;
    }

    public void setDisableFunctionality(Boolean disableFunctionality) {
        this.disableFunctionality = disableFunctionality;
    }

    public List<SelectItem> getItemsPPList() {
        return itemsPPList;
    }

    public void setItemsPPList(List<SelectItem> itemsPPList) {
        this.itemsPPList = itemsPPList;
    }

    public UserPreference getItemsPerPagePreference() {
        return itemsPerPagePreference;
    }

    public void setItemsPerPagePreference(UserPreference itemsPerPagePreference) {
        this.itemsPerPagePreference = itemsPerPagePreference;
    }

    public List<SelectItem> getAllStates() {
        return allStates;
    }

    public void setAllStates(List<SelectItem> allStates) {
        this.allStates = allStates;
    }

    public List<UserPreference> getWlStatesPreferences() {
        return wlStatesPreferences;
    }

    public void setWlStatesPreferences(List<UserPreference> wlStatesPreferences) {
        this.wlStatesPreferences = wlStatesPreferences;
    }

    public List<String> getSelectedStates() {
        return selectedStates;
    }

    public void setSelectedStates(List<String> selectedStates) {
        this.selectedStates = selectedStates;
    }

    public List<SelectItem> getAllExamTypes() {
        return allExamTypes;
    }

    public void setAllExamTypes(List<SelectItem> allExamTypes) {
        this.allExamTypes = allExamTypes;
    }

    public List<String> getSelectedExams() {
        return selectedExams;
    }

    public void setSelectedExams(List<String> selectedExams) {
        this.selectedExams = selectedExams;
    }

    public List<UserPreference> getExamTypesPreferences() {
        return examTypesPreferences;
    }

    public void setExamTypesPreferences(
            List<UserPreference> examTypesPreferences) {
        this.examTypesPreferences = examTypesPreferences;
    }

    public List<SelectItem> getOrdersList() {
        return ordersList;
    }

    public void setOrdersList(List<SelectItem> ordersList) {
        this.ordersList = ordersList;
    }

    public UserPreference getOrderPreference() {
        return orderPreference;
    }

    public void setOrderPreference(UserPreference orderPreference) {
        this.orderPreference = orderPreference;
    }

    public UserPreference getPdfZoomPreference() {
        return pdfZoomPreference;
    }

    public void setPdfZoomPreference(UserPreference pdfZoomPreference) {
        this.pdfZoomPreference = pdfZoomPreference;
    }

    public List<SelectItem> getPdfZoomList() {
        return pdfZoomList;
    }

    public void setPdfZoomList(List<SelectItem> pdfZoomList) {
        this.pdfZoomList = pdfZoomList;
    }

    public List<SelectItem> getUseDescriptionList() {
        return useDescriptionList;
    }

    public void setUseDescriptionList(List<SelectItem> useDescriptionList) {
        this.useDescriptionList = useDescriptionList;
    }

    public UserPreference getUseDescriptionPreference() {
        return useDescriptionPreference;
    }

    public void setUseDescriptionPreference(
            UserPreference useDescriptionPreference) {
        this.useDescriptionPreference = useDescriptionPreference;
    }

    public UserPreference getOrderTypePreference() {
        return orderTypePreference;
    }

    public void setOrderTypePreference(UserPreference orderTypePreference) {
        this.orderTypePreference = orderTypePreference;
    }

    public List<SelectItem> getOrderTypesList() {
        return orderTypesList;
    }

    public void setOrderTypesList(List<SelectItem> orderTypesList) {
        this.orderTypesList = orderTypesList;
    }

    public UserPreference getLabelPrintActivePreference() {
        return labelPrintActivePreference;
    }

    public void setLabelPrintActivePreference(
            UserPreference labelPrintActivePreference) {
        this.labelPrintActivePreference = labelPrintActivePreference;
    }

    public UserPreference getLabelPrintPrinterNamePreference() {
        return labelPrintPrinterNamePreference;
    }

    public void setLabelPrintPrinterNamePreference(
            UserPreference labelPrintPrinterNamePreference) {
        this.labelPrintPrinterNamePreference = labelPrintPrinterNamePreference;
    }

    public UserPreference getLabelPrintCountPreference() {
        return labelPrintCountPreference;
    }

    public void setLabelPrintCountPreference(
            UserPreference labelPrintCountPreference) {
        this.labelPrintCountPreference = labelPrintCountPreference;
    }

    public UserPreference getLabelPrintActivePreferenceDocg() {
        return labelPrintActivePreferenceDocg;
    }

    public void setLabelPrintActivePreferenceDocg(
            UserPreference labelPrintActivePreferenceDocg) {
        this.labelPrintActivePreferenceDocg = labelPrintActivePreferenceDocg;
    }

    public UserPreference getLabelPrintPrinterNamePreferenceDocg() {
        return labelPrintPrinterNamePreferenceDocg;
    }

    public void setLabelPrintPrinterNamePreferenceDocg(
            UserPreference labelPrintPrinterNamePreferenceDocg) {
        this.labelPrintPrinterNamePreferenceDocg = labelPrintPrinterNamePreferenceDocg;
    }

    public UserPreference getLabelPrintCountPreferenceDocg() {
        return labelPrintCountPreferenceDocg;
    }

    public void setLabelPrintCountPreferenceDocg(
            UserPreference labelPrintCountPreferenceDocg) {
        this.labelPrintCountPreferenceDocg = labelPrintCountPreferenceDocg;
    }

    public List<SelectItem> getPrinters() {
        return printers;
    }

    public void setPrinters(List<SelectItem> printers) {
        this.printers = printers;
    }

    public List<SelectItem> getPrintersDocg() {
        return printersDocg;
    }

    public void setPrintersDocg(List<SelectItem> printersDocg) {
        this.printersDocg = printersDocg;
    }

    public UserPreference getAutoSaveDocgPreference() {
        return autoSaveDocgPreference;
    }

    public void setAutoSaveDocgPreference(UserPreference autoSaveDocgPreference) {
        this.autoSaveDocgPreference = autoSaveDocgPreference;
    }

    public UserPreference getOpenPDFDocument() {
        return openPDFDocument;
    }

    public void setOpenPDFDocument(UserPreference openPDFDocument) {
        this.openPDFDocument = openPDFDocument;
    }

    public UserPreference getPriorityAlertPreference() {
        return priorityAlertPreference;
    }

    public void setPriorityAlertPreference(UserPreference priorityAlertPreference) {
        this.priorityAlertPreference = priorityAlertPreference;
    }

    public List<SelectItem> getPriorityAlertTypesList() {
        return priorityAlertTypesList;
    }

    public void setPriorityAlertTypesList(List<SelectItem> priorityAlertTypesList) {
        this.priorityAlertTypesList = priorityAlertTypesList;
    }

    public UserPreference getReferringDiction() {
        return referringDiction;
    }

    public void setReferringDiction(UserPreference referringDiction) {
        this.referringDiction = referringDiction;
    }

    public UserPreference getVocalRecognition() {
        return vocalRecognition;
    }

    public void setVocalRecognition(UserPreference vocalRecognition) {
        this.vocalRecognition = vocalRecognition;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
