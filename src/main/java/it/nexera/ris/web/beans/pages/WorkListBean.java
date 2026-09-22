package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.IntegrationConnectionException;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.validation.AssignValidationException;
import it.nexera.ris.common.exceptions.validation.NotSelectedValidationException;
import it.nexera.ris.common.executors.ThreadExecutor;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.AsapSectorHelper;
import it.nexera.ris.common.helpers.logic.Dcm4cheeHelper;
import it.nexera.ris.common.helpers.logic.GeneralFunctionsHelper;
import it.nexera.ris.common.helpers.logic.RobotHelper;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.IAggregated;
import it.nexera.ris.persistence.SinglePersistenceSessionAction;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestItemBase;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalProduceCD;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.persistence.integration.ADTIntegrationHelper;
import it.nexera.ris.persistence.view.BaseWorklistView;
import it.nexera.ris.persistence.view.ShortWorklistView;
import it.nexera.ris.persistence.view.WorklistView;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.DocumentTemplateWrapper;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.logic.*;
import it.nexera.ris.web.common.EntityLazyListModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

@Getter
@Setter
@Named("workListBean")
@ViewScoped
public class WorkListBean extends EntityLazyListPageBean<WorklistView> implements IAggregated, Serializable {

    private static final long serialVersionUID = -374040054287046055L;

    private static final int NUMBER_OF_DAYS = 30;

    public static final String IS_SHORT_TABLE = "is_short_table";

    private static Logger dicomErrorLog = CustomLibLoggerFactory.getDicomErrorLogger();

    private static Logger dicomInfoLog = CustomLibLoggerFactory.getDicomInfoLogger();

    private static Logger activityInfoLog = CustomLibLoggerFactory.getActivityInfoLoger();

    private final String ID_IN_SESSION = "RadiologyExamRequestsItemsIds";

    private final String ID_IN_SESSION_FOR_PRINT = "RadiologyExamRequestsItemsIdsForTads";

    private final String PREVENT_RELOAD = "prevent_reload";

    private Date dateFrom;

    private Date dateTo;

    private List<WaitingListRegistrationStateWrapper> allStates;

    private List<WaitingListRegistrationStates> selectedStates;

    private WaitingListRegistrationStateWrapper selectedStateForFilter;

    private List<ExamTypeWrapper> allExamTypes;

    private List<ExamTypeWrapper> allDialogExamTypes;

    private List<ExamTypeWrapper> selectedExamTypesForDialogFilter;

    private List<Long> selectedExamTypes;

    private ExamTypeWrapper selectedTypeForFilter;

    private List<Long> selectedExamTypesTop;

    private ExamTypeWrapper selectedTypeForFilterTop;

    private BaseWorklistView selectedRequestView;

    private String comment;

    private DocumentTemplateWrapper filledTemplate;

    private Long selectedTemplateId;

    private Long sectorId;

    private Long examId;

    private List<SelectItem> templates;

    private List<SelectItem> etichTemplates;

    private Long selectedEtichTemplateId;

    private List<SelectItem> sectors;

    private List<SelectItem> exams;

    private boolean disableTemplates;

    private RadExamRequestItemWrapper selectedModifyExamWrapper;

    private List<RadExamRequestItemWrapper> tempItemsList;

    private List<Long> selectedUrgencies;

    private List<UrgencyWrapper> allUrgencies;

    private UrgencyWrapper selectedUrgencyForFilter;

    private List<Long> selectedDiagnostics;

    private List<DiagnosticWrapper> allDiagnostics;

    private DiagnosticWrapper selectedDiagnosticForFilter;

    private List<DiagnosticWrapper> allDialogDiagnostics;

    private List<DiagnosticWrapper> selectedDiagnosticForDialogFilter;

    private List<Long> radExamItemsIdsForDicom;

    private Boolean toNextStateFromDialog;

    private Long requestIdToPartAcceptActions;

    private RadExamRequestWrapper selectedRequestWrapper;

    private List<RadExamRequestWrapper> forAggregationRequestsWrappers;

    private List<RadExamRequestItemWrapper> forAggregationRequestsItemsWrappers;

    private Boolean showPopUp;

    private List<Pair<Long, Boolean>> copyOfSelectionItemsWaitinglistView;

    private final String ID_IN_SESSION_FOR_TAGS = "RadiologyExamsIdsForPrint";

    private String selectedRequestState;

    private Long rows;

    private String orderField;

    private Boolean tepmlatesForReservedState = Boolean.FALSE;

    private OrderType orderType;

    private Long requestIdToAddNote;

    private Long noteId;

    private String printFile;

    private Boolean showApplet;

    private String selectedStatesForResume;

    private String selectedExamTypesForResume;

    private String selectedUrgencyForResume;

    private List<String> selectedStatesForResumeList;

    private List<String> selectedExamTypesForResumeList;

    private List<String> selectedUrgencyForResumeList;

    private BaseWorklistView selectedRow;

    private Integer printCount;

    private String printerName;

    private WorkListBeanWrapper beanWrapper;

    private Boolean showShortView;

    private LazyDataModel<ShortWorklistView> shortLazyModel;

    private List<ShortWorklistView> shortLazyModelFiltered;

    private List<RadExamRequestWrapper> radExamRequestWrappers;

    private Date printRequestDate;

    private Boolean selectAllPrintDialog;

    private Date postponedExecutionDate;

    private Date postponedExecutionTime;

    private String postponedExecutionReason;

    private List<Allergy> allergies;

    private BaseWorklistView editItem;

    private Patient selectedPatient;

    private String accessNumber;

    private Long selectedSectorId;

    private List<SelectItem> docSectors;

    private String referringDoctor;

    private Date referringDate;

    private Long selectedDocExamTypeId;

    private List<SelectItem> docExamTypes;

    private List<String> selectedDocExamIds;

    private List<SelectItem> docExams;

    private String docFileName;

    private String docFilePath;

    private Pacs selectedPacs;

    private List<DVDProducer> robots;

    private Long selectRobotId;

    private String dicomInfoMsg;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        if (!isPostback()) {
            setShowShortView(true);
            setPrintRequestDate(new Date());
        }
        setAllStates(new ArrayList<>());
        setAllUrgencies(new ArrayList<>());
        setAllExamTypes(new ArrayList<>());
        setTempItemsList(new ArrayList<>());
        setDocSectors(ComboboxHelper.fillList(Sector.class));
        setDocExamTypes(ComboboxHelper.fillList(ExamType.class));
        onExamTypeSelect();
        setEditItem(null);
        setPostponedExecutionDate(new Date());
        setPostponedExecutionTime(new Date());
        setBeanWrapper(new WorkListBeanWrapper());
        fillDiagnosticsWrappers();
        setSelectedAllDiagnosticsOnPanel(true);
        loadUserPreferences();

        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get("WorkListDateFrom"))) {
            this.setDateFrom((Date) SessionHelper.get("WorkListDateFrom"));
        } else if (!ValidationHelper.isNullOrEmpty(HttpSessionHelper.get("WorkListDateFrom"))) {
            this.setDateFrom((Date) HttpSessionHelper.get("WorkListDateFrom"));
        } else {
            this.setDateFrom(new Date());
        }

        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get("WorkListDateTo"))) {
            this.setDateTo((Date) SessionHelper.get("WorkListDateTo"));
        } else if (!ValidationHelper.isNullOrEmpty(HttpSessionHelper.get("WorkListDateTo"))) {
            this.setDateTo((Date) HttpSessionHelper.get("WorkListDateTo"));
        } else {
            this.setDateTo(new Date());
        }
        if (getCurrentUser() != null) {
            fillExamTypesDialogWrappers();
            if (ValidationHelper.isNullOrEmpty(getAllStates())) {
                fillStatesFromComboboxes();
            }
            if (ValidationHelper.isNullOrEmpty(getAllExamTypes())) {
                fillExamTypesComboboxes();
            }
            if (ValidationHelper.isNullOrEmpty(getAllUrgencies())) {
                fillUrgenciesFromComboboxes();
            }

            fillResume();
            filterTableFromPanel();

            openReportIfNeeded();

            setShowPopUp(Boolean.FALSE);

            getBeanWrapper().setNote(new RequestNote());
            getBeanWrapper().getNote().setNote("");

            checkPrintNeeds();
        }
    }

    public void reopenLastReport() {
        try {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.HOUR_OF_DAY, -2);
            List<RadiologyExamRequest> requests = DaoManager.load(RadiologyExamRequest.class, new Criterion[]
                            {Restrictions.eq("userClosingReportId", getCurrentUser().getId()),
                                    Restrictions.eq("waitingListRegistrationState", WaitingListRegistrationStates.REPORTED),
                                    Restrictions.ge("radiologyExamRequestItems.reportDate", c.getTime())},
                    new CriteriaAlias[]{new CriteriaAlias("radiologyExamRequestItems", "radiologyExamRequestItems", JoinType.INNER_JOIN)},
                    new Order[]{Order.desc("radiologyExamRequestItems.reportDate")}, 1, null);
            if (ValidationHelper.isNullOrEmpty(requests)) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        ResourcesHelper.getValidation("warning"),
                        ResourcesHelper.getValidation("noReportFound"));
            } else {
                List<Long> itemsIds = new ArrayList<>();
                for (RadiologyExamRequestItem reri : requests.get(0).getRadiologyExamRequestItems()) {
                    itemsIds.add(reri.getId());
                }
                redirectToDocumentGeneration(itemsIds, requests.get(0).getId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void checkBeforeCancel() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (getBeanWrapper().getSelectedRequest() != null && !ValidationHelper.isNullOrEmpty(
                getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems())) {
            if (WaitingListRegistrationStates.ACCEPTED
                    .equals(getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems()
                            .get(0).getWaitingListRegistrationState())) {
                if (getCanSuperCancelAcceptance()
                        || getCurrentUser().getFullname()
                        .equals(getBeanWrapper().getSelectedRequest()
                                .getRadiologyExamRequestItems().get(0)
                                .getOperator())) {
                    executeJS("PF('actionOnAnnulateExecution').show();");
                } else {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                            ResourcesHelper.getValidation("warning"),
                            ResourcesHelper
                                    .getValidation("workListIncorrectUser"));
                }

                return;
            } else if (WaitingListRegistrationStates.PERFORMED
                    .equals(getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems()
                            .get(0).getWaitingListRegistrationState())
                    || WaitingListRegistrationStates.IN_READING
                    .equals(getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems()
                            .get(0).getWaitingListRegistrationState())) {
                if (!getUserEqualToClosingDoc()) {
                    try {
                        User closingUser = DaoManager.get(User.class,
                                new Criterion[]{
                                        Restrictions.eq("id",
                                                getBeanWrapper().getSelectedRequest()
                                                        .getUserClosingReportId())
                                });
                        MessageHelper
                                .addGlobalMessage(FacesMessage.SEVERITY_WARN,
                                        ResourcesHelper
                                                .getString(
                                                        "worklistReportingToTheDoctorOpen")
                                                .concat(" ")
                                                .concat(closingUser
                                                        .getFullname())
                                                .concat(" in data ")
                                                .concat(DateTimeHelper
                                                        .ToDatePatternWithMinutesAndSuffix(
                                                                getBeanWrapper().getSelectedRequest()
                                                                        .getBlockingDate())),
                                        "");
                        return;

                    } catch (Exception ex) {
                        LogHelper.log(log, ex);
                    }

                }

                if (getCanSuperCancelExecution()
                        || getCurrentUser().getFullname()
                        .equals(getBeanWrapper().getSelectedRequest()
                                .getRadiologyExamRequestItems().get(0)
                                .getTrsm())) {
                    executeJS("PF('actionOnAnnulateExecution').show();");
                } else {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                            ResourcesHelper.getValidation("warning"),
                            ResourcesHelper.getValidation(
                                    "workListIncorrectUserPerformed"));
                }

                return;
            }
        }

        executeJS("PF('actionOnAnnulateExecution').show();");
    }

    private boolean getUserEqualToClosingDoc() {
        return getBeanWrapper().getSelectedRequest() != null
                && ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest().getUserClosingReportId())
                || this.getCurrentUser() != null
                && this.getCurrentUser()
                .getId()
                .equals(getBeanWrapper().getSelectedRequest()
                        .getUserClosingReportId());
    }

    public static String getIpAddr() {
        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();

        StringBuilder sb = new StringBuilder();
        String ip = request.getHeader("X-Real-IP");

        if (null != ip && !"".equals(ip.trim())
                && !"unknown".equalsIgnoreCase(ip)) {
            sb.append("X-Real-IP: ");
            sb.append(ip);
            sb.append(";");
        }

        ip = request.getHeader("X-Forwarded-For");

        if (null != ip && !"".equals(ip.trim())
                && !"unknown".equalsIgnoreCase(ip)) {
            // get first ip from proxy ip
            int index = ip.indexOf(',');

            if (index != -1) {
                sb.append("X-Forwarded-For: ");
                sb.append(ip.substring(0, index));
                sb.append(";");
            } else {
                sb.append("X-Forwarded-For: ");
                sb.append(ip);
                sb.append(";");
            }
        }

        sb.append("application use ip: ");
        sb.append(request.getRemoteAddr());

        return sb.toString();
    }

    public void onExamTypeSelect() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedDocExamTypeId())) {
            try {
                setDocExams(ComboboxHelper.fillList(RadiologyExam.class, Order.asc("description"), new Criterion[]{
                        Restrictions.eq("examType.id", getSelectedDocExamTypeId())
                }, false));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            setDocExams(null);
        }
    }

    @SuppressWarnings("unchecked")
    public void fillExamTypesComboboxes() throws HibernateException {
        if (!ValidationHelper.isNullOrEmpty((List<Long>) SessionHelper.get("WorkListExamTypes"))) {
            fillExamTypesFromSession((List<Long>) SessionHelper.get("WorkListExamTypes"));
        } else if (!ValidationHelper.isNullOrEmpty((List<Long>) HttpSessionHelper.get("WorkListExamTypes"))) {
            fillExamTypesFromSession((List<Long>) HttpSessionHelper.get("WorkListExamTypes"));
        } else {
            fillExamTypesWrappers();
        }
    }

    public void handleDocUpload(FileUploadEvent event) {
        setDocFileName(event.getFile().getFileName());
        try (InputStream inputstream = event.getFile().getInputStream()) {
            File path = new File(FileHelper.getTempDir());
            String fileName = event.getFile().getFileName();
            String randomFileName = FileHelper.getRandomFileName(fileName);
            String FilePath = FileHelper.writeFileToFolder(randomFileName, path,
                    IOUtils.toByteArray(inputstream));
            setDocFilePath(FilePath);
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
    }

    public void saveNewHistoricalReport() {
        cleanValidation();
        boolean bValid = true;
        if (ValidationHelper.isNullOrEmpty(getReferringDoctor())) {
            this.addFieldExeption("referringDoctor", "workListReferringDoctorEmpty");
            bValid = false;
        }
        if (ValidationHelper.isNullOrEmpty(getReferringDate())) {
            this.addFieldExeption("referringDate", "workListReferringDateEmpty");
            bValid = false;
        }
        if (ValidationHelper.isNullOrEmpty(getSelectedDocExamTypeId())) {
            this.addFieldExeption("selectedDocExamTypeId", "workListSelectedDocExamTypeIdEmpty");
            bValid = false;
        }
        if (ValidationHelper.isNullOrEmpty(getSelectedDocExamIds())) {
            this.addFieldExeption("selectedDocExamId", "workListSelectedDocExamIdsEmpty");
            bValid = false;
        }
        if (ValidationHelper.isNullOrEmpty(getDocFileName())) {
            this.addFieldExeption("filePdfUploader", "workListFilePdfUploaderEmpty");
            bValid = false;
        }

        if (!bValid) {
            return;
        }
        Transaction tr = null;
        try {
            File pathHtml = new File(FileHelper.getTempDir(), FileHelper.getRandomFileName("1.html"));
            PDDocument pdf = PDDocument.load(new File(getDocFilePath()));
            Writer output = new PrintWriter(pathHtml.getAbsolutePath(), "iso-8859-1");
            new PDFDomTree().writeText(pdf, output);

            output.close();
            HistoricalReport historicalReport = new HistoricalReport();
            historicalReport.setAccessNumber(getAccessNumber());
            if (!ValidationHelper.isNullOrEmpty(getSelectedSectorId())) {
                historicalReport.setSectorId(getSelectedSectorId());
                Hospital hospital = DaoManager.get(Hospital.class, new CriteriaAlias[]{
                        new CriteriaAlias("sectors", "sectors", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("sectors.id", getSelectedSectorId())
                });
                historicalReport.setHospitalCode(hospital.getCode());
            }
            historicalReport.setPatientBirthDate(getSelectedRequestView().getPatient().getBirthDate());
            historicalReport.setPatientFiscalCode(getSelectedRequestView().getPatient().getFiscalCode());
            historicalReport.setPatientName(getSelectedRequestView().getPatient().getName());
            historicalReport.setPatientSurname(getSelectedRequestView().getPatient().getSurname());
            historicalReport.setPerformDate(getReferringDate());
            historicalReport.setReferringDoctor(getReferringDoctor());
            historicalReport.setReportDate(getReferringDate());

            String contents = new String(Files.readAllBytes(Paths.get(pathHtml.getAbsolutePath())));
            pathHtml.delete();
            Pattern p2 = Pattern.compile("(<head).*</head>", Pattern.DOTALL);

            historicalReport.setReportResult(p2.matcher(contents).replaceFirst(""));
            historicalReport.setWaitingListRegistrationState(WaitingListRegistrationStates.REPORTED);
            if (!ValidationHelper.isNullOrEmpty(getSelectedDocExamTypeId())) {
                historicalReport.setExamType(DaoManager.get(ExamType.class, getSelectedDocExamTypeId()));
            }
            historicalReport.setLatestActionDate(getReferringDate());
            if (!ValidationHelper.isNullOrEmpty(getSelectedDocExamIds())) {
                List<Long> ids = new ArrayList<>();
                for (String id : getSelectedDocExamIds()) {
                    ids.add(Long.valueOf(id));
                }
                List<RadiologyExam> radiologyExams = DaoManager.load(RadiologyExam.class, new Criterion[]{
                        Restrictions.in("id", ids)
                });
                historicalReport.setRadiologyExams(radiologyExams);
            }

            String filePath = null;
            byte[] content = null;

            File file = new File(getDocFilePath());
            try (InputStream inputstream = new FileInputStream(file)) {
                content = IOUtils.toByteArray(inputstream);
                if (Boolean.TRUE.equals(Boolean.valueOf(ResourcesHelper.getString("saveOnHard")))) {
                    File path = FileEntityHelper.locateOrCreateSavingDir();
                    filePath = FileHelper.writeFileToFolder(file.getName(), path, content);
                }
            }
            tr = DaoManager.getSession().beginTransaction();
            FileEntity fileEntity = new FileEntity();
            fileEntity.setName(file.getName());
            if (filePath != null) {
                fileEntity.setPath(filePath);
            } else {
                fileEntity.setContent(content);
            }
            DaoManager.save(fileEntity);
            historicalReport.setFileEntity(fileEntity);
            DaoManager.save(historicalReport);
            tr.commit();
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO, "",
                    ResourcesHelper.getString("workListAddedNewReport"));
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (tr != null) {
                tr.rollback();
            }
        } finally {
            cleanHistoricalReportFields();
        }
        executeJS("PF('addReportDialogWV').hide();");
    }

    public void cleanHistoricalReportFields() {
        cleanValidation();
        setAccessNumber(null);
        setSelectedSectorId(null);
        setReferringDoctor(null);
        setReferringDate(null);
        setSelectedDocExamTypeId(null);
        setSelectedDocExamIds(null);
        setDocFileName(null);
        setDocFilePath(null);
        onExamTypeSelect();
    }

    @SuppressWarnings("unchecked")
    public void fillStatesFromComboboxes() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(SessionHelper.get("WorkListStates"))) {
            fillStatesFromSession((List<WaitingListRegistrationStates>) SessionHelper.get("WorkListStates"));
        } else if (!ValidationHelper.isNullOrEmpty(HttpSessionHelper.get("WorkListStates"))) {
            fillStatesFromSession((List<WaitingListRegistrationStates>) HttpSessionHelper.get("WorkListStates"));
        } else {
            this.setAllStates(WaitingListStatusHelper.getWorklistStateWrappers());

            for (WaitingListRegistrationStateWrapper sw : getAllStates()) {
                if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getWlStatesUserPreferences())) {
                    for (UserPreference pref : getBeanWrapper().getWlStatesUserPreferences()) {
                        if (sw.getRealState().equals(pref.getState())) {
                            sw.setSelected(Boolean.TRUE);
                            break;
                        }

                    }
                } else {
                    if (!sw.getRealState().equals(
                            WaitingListRegistrationStates.DELETED)) {
                        sw.setSelected(Boolean.TRUE);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void fillUrgenciesFromComboboxes() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty((List<Long>) SessionHelper.get("WorkListUrgencies"))) {
            fillUrgenciesFromSession((List<Long>) SessionHelper.get("WorkListUrgencies"));
        } else if (!ValidationHelper.isNullOrEmpty((List<Long>) HttpSessionHelper.get("WorkListUrgencies"))) {
            fillUrgenciesFromSession((List<Long>) HttpSessionHelper.get("WorkListUrgencies"));
        } else {
            fillUrgenciesForWrapper();
        }
    }

    private void fillExamTypesFromSession(List<Long> ids) {
        setSelectedExamTypes(ids);

        List<ExamTypeWrapper> examTypes = new ArrayList<>();

        try {
            List<ExamType> types = DaoManager.load(ExamType.class, new CriteriaAlias[]{
                    new CriteriaAlias("diagnostics", "diags", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.in("diags.id", getCurrentUser().getDiagnostics())
            });

            if (!ValidationHelper.isNullOrEmpty(types)) {
                for (ExamType examType : types) {
                    ExamTypeWrapper etw = new ExamTypeWrapper(examType);

                    if (ids.contains(examType.getId())) {
                        etw.setSelected(Boolean.TRUE);
                    } else {
                        etw.setSelected(Boolean.FALSE);
                    }

                    examTypes.add(etw);
                }
            }

            this.setAllExamTypes(examTypes);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void fillUrgenciesFromSession(List<Long> ids) {
        setSelectedUrgencies(ids);

        List<UrgencyWrapper> list = new ArrayList<>();

        try {
            List<Urgency> urgencies = DaoManager.load(Urgency.class);

            if (!ValidationHelper.isNullOrEmpty(urgencies)
                    && !ValidationHelper.isNullOrEmpty(ids)) {
                for (Urgency urg : urgencies) {
                    UrgencyWrapper uw = new UrgencyWrapper(urg);

                    if (ids.contains(urg.getId())) {
                        uw.setSelected(Boolean.TRUE);
                    } else {
                        uw.setSelected(Boolean.FALSE);
                    }

                    list.add(uw);
                }
            }

            this.setAllUrgencies(list);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void fillStatesFromSession(List<WaitingListRegistrationStates> states) {
        this.setSelectedStates(states);
        this.setAllStates(WaitingListStatusHelper.getWorklistStateWrappers());

        if (!ValidationHelper.isNullOrEmpty(states)
                && !ValidationHelper.isNullOrEmpty(getAllStates())) {
            for (WaitingListRegistrationStateWrapper sw : getAllStates()) {
                if (states.contains(sw.getRealState())) {
                    sw.setSelected(Boolean.TRUE);
                } else {
                    sw.setSelected(Boolean.FALSE);
                }
            }
        }
    }

    private void checkPrintNeeds() {
        if (this.getSession().get("appletPrintOnStart") != null) {
            try {
                FileEntity entity = DaoManager.get(FileEntity.class,
                        (Long) this.getSession().get("appletPrintOnStart"));

                if (entity != null) {
                    tryPrintWithAppletDG(entity);
                }
            } catch (Exception e) {
                log.error("Exception during getting fileentity", e);
            } finally {
                this.getSession().remove("appletPrintOnStart");
            }
        }
    }

    private void fillResume() {
        this.selectedStatesForResumeList = new ArrayList<>();

        for (WaitingListRegistrationStateWrapper wrap : this.getAllStates()) {
            if (wrap.getSelected()) {
                selectedStatesForResumeList.add(wrap.getValue());
            }
        }

        this.setSelectedStatesForResume(formatResume(selectedStatesForResumeList));

        this.selectedExamTypesForResumeList = new ArrayList<>();

        for (ExamTypeWrapper wrap : this.getAllExamTypes()) {
            if (wrap.getSelected()) {
                selectedExamTypesForResumeList.add(wrap.getValue());
            }
        }

        this.setSelectedExamTypesForResume(formatResume(selectedExamTypesForResumeList));

        this.selectedUrgencyForResumeList = new ArrayList<>();

        for (UrgencyWrapper wrap : this.getAllUrgencies()) {
            if (wrap.getSelected()) {
                selectedUrgencyForResumeList.add(wrap.getValue());
            }
        }

        this.setSelectedUrgencyForResume(formatResume(selectedUrgencyForResumeList));
    }

    private String formatResume(List<String> values) {
        StringBuilder sb = new StringBuilder();

        for (String s : values) {
            sb.append(s);
            sb.append(", ");
        }

        return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 2);
    }

    @Override
    public void tryToCallAggregationDlg() {
        try {
            Pair<RadExamRequestWrapper, List<RadExamRequestWrapper>> pair = AggregationHelper
                    .getPairForAggregationWL(getBeanWrapper().getSelectedRequest().getId(), this
                            .getCurrentUser().getId());

            if (!ValidationHelper.isNullOrEmpty(pair)) {
                this.setForAggregationRequestsWrappers(pair.getSecond());
                this.setSelectedRequestWrapper(pair.getFirst());
                this.setForAggregationRequestsItemsWrappers(AggregationHelper
                        .getItemsWrappersFromRequestsWrappers(pair.getSecond()));

                setToNextStateFromDialog(Boolean.TRUE);
                executeJS("PF('aggregationDlgWV').show();");
                executeJS("updateAggregationsDatatables();");
                executeJS("selectAllOnAggregation();");
            } else {
                setToNextStateFromDialog(Boolean.FALSE);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void aggregateAction() {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    switch (getBeanWrapper().getSelectedRequest()
                            .getWaitingListRegistrationState()) {
                        case RESERVED:
                            LogHelper.log(activityInfoLog,
                                    "start\tAggregate action\tfor requestID " + getBeanWrapper().getSelectedRequest().getId());
                            aggregateActionAccepted();
                            LogHelper.log(activityInfoLog,
                                    "end\tAggregate action\tfor requestID " + getBeanWrapper().getSelectedRequest().getId());
                            break;
                        case DRAFT:
                        case PERFORMED:
                        case IN_READING:
                            aggregateActionPerfomed();
                        default:
                            break;
                    }
                }

                @Override
                public void onSuccess() throws HibernateException,
                        InstantiationException, IllegalAccessException,
                        PersistenceBeanException {
                    switch (getBeanWrapper().getSplitedRequest() == null
                            ? getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState()
                            : getBeanWrapper().getSplitedRequest().getWaitingListRegistrationState()) {
                        case ACCEPTED:

                            if (getBeanWrapper().getSelectedRequest() != null) {

                                LogHelper.log(activityInfoLog,
                                        "start\tDicom flow\tfor requestID " + getBeanWrapper().getSelectedRequest().getId());
                                callDicom(
                                        getBeanWrapper().getSplitedRequest() == null ? getBeanWrapper().getSelectedRequest()
                                                : getBeanWrapper().getSplitedRequest(),
                                        getRadExamItemsIdsForDicom(), true);
                                LogHelper.log(activityInfoLog,
                                        "end\tDicom flow\tfor requestID " + getBeanWrapper().getSelectedRequest().getId());
                            }
                            break;
                        default:
                            break;
                    }

                    getBeanWrapper().setSplitedRequest(null);
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
    }

    @Override
    public void selectAllItemsForAggregation() {
        setSelectionToAggregationDatatables(Boolean.TRUE);
    }

    @Override
    public void unselectItemsForAggregation() {
        if (!ValidationHelper.isNullOrEmpty(this
                .getForAggregationRequestsItemsWrappers())) {
            for (RadExamRequestItemWrapper reriw : getForAggregationRequestsItemsWrappers()) {
                reriw.setSelected(Boolean.FALSE);
            }
        }
    }

    private void setSelectionToAggregationDatatables(Boolean value) {
        if (!ValidationHelper.isNullOrEmpty(this
                .getForAggregationRequestsItemsWrappers())) {
            for (RadExamRequestItemWrapper reriw : getForAggregationRequestsItemsWrappers()) {
                reriw.setSelected(value);
            }

            if (!ValidationHelper.isNullOrEmpty(this
                    .getSelectedRequestWrapper()
                    .getRadExamRequestItemWrappers())) {
                for (RadExamRequestItemWrapper reriw : this
                        .getSelectedRequestWrapper()
                        .getRadExamRequestItemWrappers()) {
                    reriw.setSelected(value);
                }
            }
        }

        executeJS("updateAggregationsDatatables();");
    }

    private void fillUrgenciesForWrapper() {
        List<UrgencyWrapper> list = new ArrayList<>();

        try {
            List<Urgency> urgencies = DaoManager.load(Urgency.class);

            if (!ValidationHelper.isNullOrEmpty(urgencies)) {
                for (Urgency urg : urgencies) {
                    UrgencyWrapper uw = new UrgencyWrapper(urg);
                    uw.setSelected(Boolean.TRUE);
                    list.add(uw);
                }
            }

            setSelectedAllUrgenciesOnPanel(true);
            this.setAllUrgencies(list);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void fillExamTypesWrappers() {
        try {
            List<ExamTypeWrapper> examTypes = loadExamTypes();

            if (getBeanWrapper().getExTypesUserPreferences().isEmpty()) {
                this.setSelectedAllExamTypesOnPanel(true);
            }

            this.setAllExamTypes(examTypes);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void fillExamTypesDialogWrappers() {
        try {
            List<ExamTypeWrapper> examTypes = loadExamTypes();

            setAllDialogExamTypes(new ArrayList<>(examTypes));
            setSelectedExamTypesForDialogFilter(new ArrayList<>(examTypes));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private List<ExamTypeWrapper> loadExamTypes() throws PersistenceBeanException, IllegalAccessException {
        List<ExamTypeWrapper> examTypes = new ArrayList<>();
        List<ExamType> types = DaoManager.load(ExamType.class, new CriteriaAlias[]{
                new CriteriaAlias("diagnostics", "diags", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.in("diags.id", getCurrentUser().getDiagnostics())
        });

        if (!ValidationHelper.isNullOrEmpty(types)) {
            for (ExamType examType : types) {
                ExamTypeWrapper etw = new ExamTypeWrapper(examType);

                if (!getBeanWrapper().getExTypesUserPreferences().isEmpty()) {
                    for (UserPreference up : getBeanWrapper().getExTypesUserPreferences()) {
                        if (up.getExamType().getId()
                                .equals(examType.getId())) {
                            etw.setSelected(Boolean.TRUE);
                        }
                    }

                    examTypes.add(etw);
                } else {
                    etw.setSelected(Boolean.TRUE);
                    examTypes.add(etw);
                }
            }
        }
        return examTypes;
    }

    private void loadUserPreferences() {
        if (ValidationHelper.isNullOrEmpty(getCurrentUser())) {
            return;
        }
        List<UserPreference> userPreferences = null;

        try {
            userPreferences = DaoManager.load(UserPreference.class, new Criterion[]{
                    Restrictions.eq("user.id", getCurrentUser().getId())
            });
        } catch (HibernateException | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }

        if (getBeanWrapper().getWlStatesUserPreferences() == null) {
            getBeanWrapper().setWlStatesUserPreferences(new ArrayList<UserPreference>());
        }

        if (getBeanWrapper().getExTypesUserPreferences() == null) {
            getBeanWrapper().setExTypesUserPreferences(new ArrayList<UserPreference>());
        }

        this.setRows(10L);

        this.orderField = "name";

        if (!ValidationHelper.isNullOrEmpty(userPreferences)) {
            for (UserPreference up : userPreferences) {
                if (up.getType() != null) {
                    switch (up.getType()) {
                        case WORKLIST_ITEMS_PER_PAGE: {
                            if (up.getItemsPerPage() != null)
                                this.setRows(up.getItemsPerPage());
                        }
                        case WORKLIST_STATE: {
                            getBeanWrapper().getWlStatesUserPreferences().add(up);
                            break;
                        }
                        case WORKLIST_EXAM_TYPE: {
                            getBeanWrapper().getExTypesUserPreferences().add(up);
                            break;
                        }
                        case WORKLIST_ORDER:
                            if (up.getOrderColumn() != null) {
                                this.orderField = up.getOrderColumn().getFieldValue();
                            }
                        case WORKLIST_ORDER_TYPE:
                            if (up.getOrderColumnType() != null) {
                                this.orderType = up.getOrderColumnType();
                            }
                        case LABEL_PRINT_ACTIVE:
                            getBeanWrapper().setLabelPrintActivePreference(up);
                            break;
                        case LABEL_PRINT_COUNT:
                            getBeanWrapper().setLabelPrintCountPreference(up);
                            break;
                        case LABEL_PRINT_PRINTER_NAME:
                            getBeanWrapper().setLabelPrintPrinterNamePreference(up);
                            break;
                        case LABEL_PRINT_ACTIVE_DOCG:
                            getBeanWrapper().setLabelPrintActivePreferenceDocg(up);
                            break;
                        case LABEL_PRINT_COUNT_DOCG:
                            getBeanWrapper().setLabelPrintCountPreferenceDocg(up);
                            break;
                        case LABEL_PRINT_PRINTER_NAME_DOCG:
                            getBeanWrapper().setLabelPrintPrinterNamePreferenceDocg(up);
                            break;
                        case OPEN_PDF_DOCUMENT:
                            getBeanWrapper().setOpenPdfDocPref(up);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void fillDiagnosticsWrappers() {
        try {
            List<DiagnosticWrapper> diagnostics = new ArrayList<>();
            List<DiagnosticWrapper> dialogDiagnostics = new ArrayList<>();
            setSelectedDiagnosticForDialogFilter(new ArrayList<DiagnosticWrapper>());
            setAllDiagnostics(new ArrayList<DiagnosticWrapper>());
            if (!ValidationHelper.isNullOrEmpty(getCurrentUser())) {
                List<Diagnostic> allDiagnostics = DaoManager.load(Diagnostic.class, new Criterion[]{
                        Restrictions.in("id", getCurrentUser().getDiagnostics())
                });
                if (!ValidationHelper.isNullOrEmpty(allDiagnostics)) {
                    for (Diagnostic diagnostic : allDiagnostics) {
                        diagnostics.add(new DiagnosticWrapper(diagnostic.getId(), diagnostic.getDescription()));
                        DiagnosticWrapper dialogDiagnosticWrapper = new DiagnosticWrapper(diagnostic.getId(), diagnostic.getDescription(), true);
                        dialogDiagnostics.add(dialogDiagnosticWrapper);
                        getSelectedDiagnosticForDialogFilter().add(dialogDiagnosticWrapper);
                    }
                }
            }
            setAllDiagnostics(diagnostics);
            setAllDialogDiagnostics(dialogDiagnostics);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void fillTemplates() throws PersistenceBeanException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedRequestView())) {
            setTemplates(GeneralFunctionsHelper.fillTemplates(
                    DocumentGenerationPlaces.WORKLIST, null, null));

            setDisableTemplates(this.getTemplates().size() <= 1);

            if (!ValidationHelper.isNullOrEmpty(getTemplates())) {
                setDisableTemplates(this.getTemplates().size() <= 1);
                setSelectedTemplateId(Long.valueOf(getTemplates().get(0)
                        .getValue().toString()));
            } else {
                setDisableTemplates(true);
            }
        }
    }

    private void fillEtichTemplates() throws PersistenceBeanException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedRequestView())) {
            setEtichTemplates(GeneralFunctionsHelper
                    .fillTemplates(
                            Boolean.FALSE.equals(tepmlatesForReservedState) ? DocumentGenerationPlaces.ETICH
                                    : DocumentGenerationPlaces.RESERVATION,
                            this.getSelectedRequestView().getSectorId(), null));
            this.setTepmlatesForReservedState(Boolean.FALSE);
        }
    }

    private void openReportIfNeeded() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException, IOException {
        if (getBeanWrapper().getOpenPdfDocPref() != null
                && Boolean.TRUE.equals(getBeanWrapper().getOpenPdfDocPref()
                .getOpenPdfDocument())
                && !ValidationHelper.isNullOrEmpty(this.getSession().get(
                "fromDocumentGeneration"))
                && !ValidationHelper.isNullOrEmpty(this.getSession().get(
                "fromDocumentGeneration"))) {
            Long radExamReqId = (Long) this.getSession().get("radExamReqId");

            if (!ValidationHelper.isNullOrEmpty(radExamReqId)) {
                RadiologyExamRequest request = DaoManager.get(
                        RadiologyExamRequest.class, new Criterion[]{
                                Restrictions.eq("id", radExamReqId)
                        });

                if (!ValidationHelper.isNullOrEmpty(request
                        .getRadiologyExamRequestItems())
                        && !ValidationHelper.isNullOrEmpty(request
                        .getRadiologyExamRequestItems().get(0)
                        .getFileEntity())
                        && (!ValidationHelper.isNullOrEmpty(request
                        .getRadiologyExamRequestItems().get(0)
                        .getFileEntity().getContent()) || !ValidationHelper
                        .isNullOrEmpty(request
                                .getRadiologyExamRequestItems().get(0)
                                .getFileEntity().getPath()))
                        && !ValidationHelper.isNullOrEmpty(request
                        .getRadiologyExamRequestItems().get(0)
                        .getFileEntity().getName())) {
                    GeneralFunctionsHelper.openPdfInNewTab(request);
                }
            }
        }

        clearSessionAttr();
    }

    private void clearSessionAttr() {
        this.getSession().remove("fromDocumentGeneration");
        this.getSession().remove("radExamReqId");
    }

    public void callFilterTable() {
        Boolean firstFilterOnWorklist = (Boolean) SessionHelper
                .get(PREVENT_RELOAD);

        if (!Boolean.TRUE.equals(firstFilterOnWorklist)) {
            executeJS("filterTableFromPanel();PF('blockTable').hide();showPopUp();");
        } else {
            executeJS("PF('blockTable').hide();showPopUp();");
        }

        SessionHelper.removeObject(PREVENT_RELOAD);
    }

    public void filterTableFromPanel() {
        if (getBeanWrapper().getSelectedRequest() != null) {
            LogHelper.log(activityInfoLog,
                    "start\tRefresh worklist\tfor requestID " + getBeanWrapper().getSelectedRequest().getId());
        }
        this.cleanValidation();

        SessionHelper.put(this.getDateFrom(), "WorkListDateFrom");
        SessionHelper.put(this.getDateTo(), "WorkListDateTo");
        HttpSessionHelper.put(this.getDateFrom(), "WorkListDateFrom");
        HttpSessionHelper.put(this.getDateTo(), "WorkListDateTo");

        if (!ValidationHelper.isNullOrEmpty((this.getAllStates()))) {
            setSelectedStates(new ArrayList<WaitingListRegistrationStates>());

            for (WaitingListRegistrationStateWrapper wlrsw : this
                    .getAllStates()) {
                if (wlrsw.getSelected()) {
                    getSelectedStates().add(wlrsw.getRealState());

                    if (!this.selectedStatesForResumeList.contains(wlrsw
                            .getValue())) {
                        this.selectedStatesForResumeList.add(wlrsw.getValue());
                    }

                } else {
                    if (this.selectedStatesForResumeList.contains(wlrsw
                            .getValue())) {
                        this.selectedStatesForResumeList.remove(wlrsw
                                .getValue());
                    }
                }
            }

            this.setSelectedStatesForResume(formatResume(this.selectedStatesForResumeList));

            SessionHelper.put(this.getSelectedStates(), "WorkListStates");//getSelectedStates()
            HttpSessionHelper.put(this.getSelectedStates(), "WorkListStates");
        }

        if (!ValidationHelper.isNullOrEmpty((this.getAllUrgencies()))) {
            setSelectedUrgencies(new ArrayList<Long>());

            for (UrgencyWrapper uw : this.getAllUrgencies()) {
                if (uw.getSelected()) {
                    getSelectedUrgencies()
                            .add(uw.getId() == null ? 0L : uw.getId());

                    if (!this.selectedUrgencyForResumeList.contains(uw
                            .getValue())) {
                        this.selectedUrgencyForResumeList.add(uw.getValue());
                    }

                } else {
                    if (this.selectedUrgencyForResumeList.contains(uw
                            .getValue())) {
                        this.selectedUrgencyForResumeList.remove(uw.getValue());
                    }
                }
            }

            this.setSelectedUrgencyForResume(formatResume(this.selectedUrgencyForResumeList));

            SessionHelper.put(this.getSelectedUrgencies(), "WorkListUrgencies");//getSelectedUrgencies()
            HttpSessionHelper.put(this.getSelectedUrgencies(), "WorkListUrgencies");
        }

        if (!ValidationHelper.isNullOrEmpty((this.getAllExamTypes()))) {
            setSelectedExamTypes(new ArrayList<Long>());

            for (ExamTypeWrapper etw : this.getAllExamTypes()) {
                if (etw.getSelected()) {
                    getSelectedExamTypes().add(etw.getRealExamType().getId());

                    if (!this.selectedExamTypesForResumeList.contains(etw
                            .getValue())) {
                        this.selectedExamTypesForResumeList.add(etw.getValue());
                    }
                } else {
                    if (this.selectedExamTypesForResumeList.contains(etw
                            .getValue())) {
                        this.selectedExamTypesForResumeList.remove(etw
                                .getValue());
                    }
                }
            }

            this.setSelectedExamTypesForResume(formatResume(this.selectedExamTypesForResumeList));

            SessionHelper.put(this.getSelectedExamTypes(), "WorkListExamTypes");//getSelectedExamTypes()
            HttpSessionHelper.put(this.getSelectedExamTypes(), "WorkListExamTypes");
        }

        if (!ValidationHelper.isNullOrEmpty((this.getAllDiagnostics()))) {
            setSelectedDiagnostics(new ArrayList<Long>());

            for (DiagnosticWrapper dw : this.getAllDiagnostics()) {
                if (dw.getSelected()) {
                    getSelectedDiagnostics().add(dw.getDiagnosticId());
                }
            }

            if (getSelectedDiagnostics().isEmpty()) {
                for (DiagnosticWrapper dw : this.getAllDiagnostics()) {
                    getSelectedDiagnostics().add(dw.getDiagnosticId());
                }
            }
        }

        try {
            this.fillLazyList();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        if (getBeanWrapper().getSelectedRequest() != null) {
            LogHelper.log(activityInfoLog,
                    "end\tRefresh worklist\tfor requestID " + getBeanWrapper().getSelectedRequest().getId());
    }
    }

    private void fillLazyList() throws PersistenceBeanException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getDateFrom())
                && !ValidationHelper.isNullOrEmpty(this.getDateTo())
                && this.getDateFrom().after(this.getDateTo())) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("workListSelectCorrectDate"));

            return;
        }

        List<Criterion> criterias = new ArrayList<>();
        List<CriteriaAlias> aliases = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getSelectedExamTypes())) {
            criterias
                    .add(Restrictions.in("examTypeId", getSelectedExamTypes()));
        } else {
            if (!ValidationHelper.isNullOrEmpty(getAllExamTypes())) {
                List<Long> ids = new ArrayList<>();

                for (ExamTypeWrapper ex : getAllExamTypes()) {
                    ids.add(ex.getRealExamType().getId());
                }

                if (ids.isEmpty()) {
                    ids.add(Long.valueOf(0));
                }

                criterias.add(Restrictions.not(Restrictions.in("examTypeId",
                        ids)));
            }
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
            if (!ValidationHelper.isNullOrEmpty(getAllUrgencies())) {
                List<Long> ids = new ArrayList<>();

                for (UrgencyWrapper uw : getAllUrgencies()) {
                    ids.add(uw.getId());
                }

                criterias.add(Restrictions.not(Restrictions
                        .in("urgencyId", ids)));
            }
        }

        if (!ValidationHelper.isNullOrEmpty(this.getDateFrom())) {
            criterias.add(Restrictions.ge("latestActionPerformDate",
                    DateTimeHelper.getDayStart(this.getDateFrom())));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getDateTo())) {
            criterias.add(Restrictions.le("latestActionPerformDate",
                    DateTimeHelper.getDayEnd(this.getDateTo())));
        }

        criterias.add(Restrictions.in("sectorId", getCurrentUser().getSectors()));
        /*aliases.add(new CriteriaAlias("radiologyExamRequestLazyItems", "items",
                JoinType.INNER_JOIN));
        aliases.add(new CriteriaAlias("items.radiologyExam", "radiologyExam",
                JoinType.INNER_JOIN));
        aliases.add(new CriteriaAlias("radiologyExam.diagnostics",
                "diagnostics", JoinType.INNER_JOIN));
        criterias.add(Restrictions.in("diagnostics.id",
                this.getSelectedDiagnostics()));*/

        if (!ValidationHelper.isNullOrEmpty(getSelectedStates())) {
            if (getSelectedStates().size() == 1) {
                criterias.add(Restrictions.eq("waitingListRegistrationState",
                        getSelectedStates().get(0)));
            } else {
                criterias
                        .add(Restrictions.in("waitingListRegistrationState",
                                getSelectedStates()));
            }
        }

        Order order = Order.desc(orderField);

        if (OrderType.ASCENDING.equals(this.orderType)) {
            order = Order.asc(orderField);
        }
        boolean showShortView = false;

        if (getDateFrom() == null || getDateTo() == null) {
            showShortView = false;
        } else {

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar = DateTimeHelper.getDayEnd(currentCalendar);

            Calendar lessCalendar = Calendar.getInstance();
            lessCalendar = DateTimeHelper.getDayStart(lessCalendar);
            lessCalendar.add(Calendar.DAY_OF_MONTH, -NUMBER_OF_DAYS);

            Calendar dateFromCalendar = Calendar.getInstance();
            dateFromCalendar.setTime(getDateFrom());

            Calendar dateToCalendar = Calendar.getInstance();
            dateToCalendar.setTime(getDateTo());

            showShortView = DateTimeHelper.between(dateFromCalendar, lessCalendar,
                    currentCalendar)
                    && DateTimeHelper.between(dateToCalendar, lessCalendar,
                    currentCalendar);
        }

        setShowShortView(showShortView);

        if (showShortView) {
            this.loadShortList(criterias.toArray(new Criterion[]{}),
                    new Order[]{
                            order
                    }, aliases.toArray(new CriteriaAlias[]{}));
        } else {
            this.loadList(WorklistView.class,
                    criterias.toArray(new Criterion[]{}), new Order[]{
                            order
                    }, aliases.toArray(new CriteriaAlias[]{}));
        }
    }

    protected void loadShortList(Criterion[] restrictions,
                                 Order[] orders, CriteriaAlias[] aliases) {
        this.setShortLazyModel(new EntityLazyListModel<ShortWorklistView>(ShortWorklistView.class, restrictions,
                orders, aliases));
    }

    public void annulateExecution() {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest())
                && getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState() != null) {
            switch (getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState()) {
                case PERFORMED:
                case IN_READING:
                    WaitingListStatusHelper.logActivity(
                            UserActivityLogStates.ANNULLA_ESEGUI,
                            getBeanWrapper().getSelectedRequest().getId());
                    annulateAction(WaitingListRegistrationStates.ACCEPTED);
                    break;
                case ACCEPTED:
                    WaitingListStatusHelper.logActivity(
                            UserActivityLogStates.ANNULLA_ACCETTA,
                            getBeanWrapper().getSelectedRequest().getId());
                    annulateAction(WaitingListRegistrationStates.RESERVED);
                    break;
                default:
                    break;
            }
        }
    }

    public void annulateAction(WaitingListRegistrationStates state) {
        try {
            final WaitingListRegistrationStates fState = state;

            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    returnRequestToState(fState);
                }

                @Override
                public void onSuccess() {
                    List<Long> radExItemIds = new ArrayList<>();
                    List<RadiologyExamRequestItem> items = null;

                    try {
                        if (getBeanWrapper().getSelectedRequest() != null) {
                            items = DaoManager.load(RadiologyExamRequestItem.class,
                                    Restrictions.eq("radiologyExamRequest.id",
                                            getBeanWrapper().getSelectedRequest().getId()));
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }

                    if (!ValidationHelper.isNullOrEmpty(items)) {
                        for (RadiologyExamRequestItem item : items) {
                            radExItemIds.add(item.getId());
                        }

                        switch (fState) {
                            case RESERVED:
                                callDicom(getBeanWrapper().getSelectedRequest(), radExItemIds,
                                        false);
                                break;
                            case ACCEPTED:
                                callDicom(getBeanWrapper().getSelectedRequest(), radExItemIds,
                                        true);
                                break;
                            default:
                                break;
                        }
                    }
                }

                @Override
                public void onException(Exception e) throws Exception {
                    LogHelper.log(log, e);
                }
            });

        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        filterTableFromPanel();
    }

    private void returnRequestToState(WaitingListRegistrationStates state)
            throws HibernateException, PersistenceBeanException, IllegalAccessException {
        List<RadiologyExamRequestItem> items = null;

        try {
            if (getBeanWrapper().getSelectedRequest() != null) {
                items = DaoManager.load(RadiologyExamRequestItem.class,
                        Restrictions.eq("radiologyExamRequest.id",
                                getBeanWrapper().getSelectedRequest().getId()));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (!ValidationHelper.isNullOrEmpty(items)) {
            List<Long> radExItemIds = new ArrayList<>();

            for (RadiologyExamRequestItem item : items) {
                item.setWaitingListRegistrationState(state);

                switch (state) {
                    case RESERVED:
                        item.setAcceptDate(null);
                        item.setOperator(null);
                        break;
                    case ACCEPTED:
                        item.setPerformDate(null);
                        item.getRadiologyExamRequest().setPerformDate(null);

                        if (WaitingListRegistrationStates.DRAFT
                                .equals(item.getRadiologyExamRequest()
                                        .getWaitingListRegistrationState())
                                || WaitingListRegistrationStates.REPORTED
                                .equals(item.getRadiologyExamRequest()
                                        .getWaitingListRegistrationState())) {
                            item.getRadiologyExamRequest()
                                    .setLatestActionPerformDate(null);
                        }

                        item.setTrsm(null);
                        break;
                    default:
                        break;
                }

                radExItemIds.add(item.getId());

                DaoManager.save(item);
            }
            getBeanWrapper().getSelectedRequest().setWaitingListRegistrationState(state);

            DaoManager.merge(getBeanWrapper().getSelectedRequest());

            HistoricalReportHelper.getInstance()
                    .saveHistoricalReportForRadiologyExamRequest(
                            getBeanWrapper().getSelectedRequest(), items, null, false);
        }
    }

    public void moveToNextState() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (GeneralFunctionsHelper.validateRequest(getBeanWrapper().getSelectedRequest())
                && getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems().get(0)
                .getWaitingListRegistrationState() != null) {

            if (!getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems().get(0)
                    .getWaitingListRegistrationState().name()
                    .equals(this.getSelectedRequestState())
                    && !WaitingListRegistrationStates.IN_READING.name().equals(getSelectedRequestState())) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        ResourcesHelper.getValidation("warning"),
                        ResourcesHelper
                                .getValidation("workListRequestWasChanged"));

                return;
            }

            switch (getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems().get(0)
                    .getWaitingListRegistrationState()) {
                case RESERVED:
                    if (this.getCanAccept()) {
                        Date currentDate = new Date();
                        Date reserveDate = getBeanWrapper().getSelectedRequest().getReserveDate();
                        if (reserveDate == null
                                || currentDate.after(reserveDate)
                                || currentDate.equals(reserveDate)
                                || getCanAcceptanceOfFutureBooking()) {
                            tryToCallAggregationDlg();
                            if (!getToNextStateFromDialog()) {
                                WaitingListStatusHelper.logActivity(
                                        UserActivityLogStates.ACCETTA,
                                        getBeanWrapper().getSelectedRequest().getId());
                                accept();
                            }
                        } else {
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_WARN,
                                            ResourcesHelper
                                                    .getValidation("warning"),
                                            ResourcesHelper
                                                    .getValidation("workListReservationDateAfterCurrentDate"));
                        }
                    }
                    break;
                case ACCEPTED:
                case PARTIALLY_ACCEPTED:
                    if (this.getCanPerformWorklist()) {
                        List<Object> types = DaoManager.getFields(ExamTypeAction.class,
                                new Criterion[]{
                                        Restrictions.eq("role.id", this
                                                .getCurrentUser().getRoles()
                                                .get(0).getId()),
                                        Restrictions.eq("examType.id",
                                                getBeanWrapper().getSelectedRequest()
                                                        .getExamType().getId())
                                }, null, false, "state");

                        if (!ValidationHelper.isNullOrEmpty(types)) {
                            switch ((ThreeStateCheckbox) types.get(0)) {
                                case ACCEPTED:
                                    WaitingListStatusHelper.logActivity(
                                            UserActivityLogStates.ESEGUI,
                                            getBeanWrapper().getSelectedRequest().getId());
                                    perform(null);
                                    break;
                                case DENIED:
                                    MessageHelper
                                            .addGlobalMessage(
                                                    FacesMessage.SEVERITY_WARN,
                                                    ResourcesHelper
                                                            .getValidation("warning"),
                                                    ResourcesHelper
                                                            .getValidation("workListUserNotAllowPerform"));
                                    break;
                                case NOT_SELECTED:

                                    this.setCopyOfSelectionItemsWaitinglistView(new ArrayList<Pair<Long, Boolean>>());

                                    loadLazyItems(getSelectedRequestView());

                                    for (RadiologyExamRequestItemBase item : getSelectedRequestView().getRadiologyExamRequestItems()) {
                                        this.getCopyOfSelectionItemsWaitinglistView().add(new Pair<>(item.getId(), item.getSelected()));
                                    }

                                    executeJS("PF('actionOnPerformWV').show();");
                                    break;
                            }
                        } else {
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_WARN,
                                            ResourcesHelper
                                                    .getValidation("warning"),
                                            ResourcesHelper
                                                    .getValidation("workListPermissionNotSet"));
                        }
                    }
                    break;
                case PERFORMED:
                    WaitingListStatusHelper.logActivity(
                            UserActivityLogStates.REFERTA, getBeanWrapper().getSelectedRequest()
                                    .getId());
                    moveToReportedState();
                    break;
                case DRAFT:
                    User userClosing = checkDocumentGenerationPossibility();

                    if (userClosing == null
                            || userClosing.getId().equals(
                            this.getCurrentUser().getId())
                            || this.getCanUnlockPDF()) {
                        toDocumentGenerationFromDraft();
                    } else {
                        setShowPopUp(Boolean.TRUE);
                    }
                    break;
                case DELETED:
                    if (this.getCanShow()) {
                        viewExitingReport();
                    }
                    break;
                case SIGNED:
                    if (this.getCanShow()) {
                        viewReportedSign();
                    }
                    break;
                case REPORTED:
                    if (this.getCanShow()) {
                        viewReportedReport();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void checkHistoryProduceCD() {
        try {
            Long count = DaoManager.getCount(HistoricalProduceCD.class, "id", new Criterion[]{
                    Restrictions.eq("radiologyExamRequest.id", getSelectedRequestView().getId())
            });
            if (count > 0) {
                executeJS("PF('confirmDicomHistoryDlgWv').show();");
            } else {
                checkProduceCD();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void checkProduceCD() {
        try {
            if (getSelectedRequestView() != null) {
                Pacs pacs = DaoManager.get(
                        Pacs.class,
                        new Criterion[]{
                                Restrictions.eq("sector.id",
                                        getSelectedRequestView().getSectorId()),
                                Restrictions.eq("pacsType", PacsType.LTA)
                        });
                if (pacs == null) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                            ResourcesHelper.getValidation("warning"), ResourcesHelper
                                    .getValidation("workListPacsNotPresent"));

                    return;
                } else {
                    setSelectedPacs(pacs);
                }
                List<DVDProducer> robots = DaoManager.load(DVDProducer.class, new Criterion[]{
                        Restrictions.eq("sector.id",
                                getSelectedRequestView().getSectorId())
                });
                if (ValidationHelper.isNullOrEmpty(robots)) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                            ResourcesHelper.getValidation("warning"), ResourcesHelper
                                    .getValidation("workListRobotNotPresent"));

                    return;
                }
                setRobots(robots);
                if (robots.size() > 1) {
                    executeJS("PF('selectRobotDlgWv').show();");

                    return;
                } else {
                    setSelectRobotId(robots.get(0).getId());
                }
                executeJS("startProduceCD();");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void produceCD() {
        try {
            if (getSelectedRequestView() != null) {
                DVDProducer robot = null;
                for (DVDProducer producer : getRobots()) {
                    if (producer.getId().equals(getSelectRobotId())) {
                        robot = producer;
                        break;
                    }
                }
                String msg;
                boolean result = RobotHelper.produceCD(robot, getSelectedPacs(),
                        getSelectedRequestView().getId(), getSelectedRequestView().getAccessNumber());
                if (result) {
                    msg = "workListDicomOk";
                } else {
                    msg = "workListDicomError";
                }
                setDicomInfoMsg(ResourcesHelper.getValidation(msg));
                executeJS("PF('dicomInfoDlgWv').show();");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void confirmedPerform() {
        try {
            if (this.getCopyOfSelectionItemsWaitinglistView() != null) {
                loadLazyItems(getSelectedRequestView());

                for (RadiologyExamRequestItemBase item : getSelectedRequestView().getRadiologyExamRequestItems()) {
                    for (Pair<Long, Boolean> pair : getCopyOfSelectionItemsWaitinglistView()) {
                        if (pair.getFirst().equals(item.getId())) {
                            item.setSelected(pair.getSecond());
                            break;
                        }
                    }
                }

                this.setCopyOfSelectionItemsWaitinglistView(null);
            }

            perform(null);

            filterTableFromPanel();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void moveToReportedState() throws InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        User userClosingReport = checkDocumentGenerationPossibility();

        if (userClosingReport == null
                || userClosingReport.getId().equals(
                this.getCurrentUser().getId())
                || this.getCanUnlockPDF()) {
            tryToCallAggregationDlg();

            if (!this.getToNextStateFromDialog()) {
                toDocumentGeneration();
            }
        } else {
            setShowPopUp(Boolean.TRUE);
        }
    }

    public String getDialogNameToAnnulate() {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest())) {
            if (WaitingListRegistrationStates.PERFORMED.equals(getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState())
                    || WaitingListRegistrationStates.IN_READING.equals(getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState())) {
                return this.getPatientNameSurname("worklistSureWantToDelete");
            } else if (WaitingListRegistrationStates.ACCEPTED.equals(getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState())) {
                return this.getPatientNameSurname("worklistSureWantToAnnulate");
            }
        }

        return "";
    }

    public String getAggregationButtonName() {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest())) {
            if (WaitingListRegistrationStates.RESERVED.equals(getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState())) {
                return ResourcesHelper
                        .getString("workListAggregationButtonCheckIn");
            } else if (WaitingListRegistrationStates.PERFORMED.equals(getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState())
                    || WaitingListRegistrationStates.IN_READING.equals(getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState())) {
                return ResourcesHelper
                        .getString("workListAggregationButtonReferta");
            }
        }

        return "";
    }

    public String getPatientNameSurname(String msg) {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest())) {
            String patientFullName = getBeanWrapper().getSelectedRequest()
                    .getPatientFullname();
            StringBuilder sb = new StringBuilder();
            sb.append(ResourcesHelper.getString(msg)).append(" ").append("<b>")
                    .append(patientFullName).append("</b>").append("?");
            return sb.toString();
        }

        return "";
    }

    public void addNote() {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getNote().getNote())) {
            try {
                RequestNote note = getBeanWrapper().getNote();
                note.setNote(getBeanWrapper().getNote().getNote());
                note.setPerformDate(new Date());
                if (!ValidationHelper.isNullOrEmpty(getRequestIdToAddNote())) {
                    RadiologyExamRequest req = DaoManager
                            .get(RadiologyExamRequest.class,
                                    getRequestIdToAddNote());

                    note.setRadiologyExamRequest(req);
                }

                DaoManager.save(note, true);
                executeJS("PF('addNoteWV').hide(); PF('tableWV').collapseAllRows()");
                PrimeFaces.current().ajax().update("table");
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
                    this.cleanValidation();
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

    private void viewReportedReport() {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest()
                .getUserClosingReportId())) {
            if (getBeanWrapper().getSelectedRequest().getUserClosingReportId()
                    .equals(this.getCurrentUser().getId())) {
                toDocumentGenerationFromReported();
            } else {
                RedirectHelper.goTo(PageTypes.PDF_VIEW, getBeanWrapper().getSelectedRequest().getId(), true);
            }
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"), ResourcesHelper
                            .getValidation("workListUserClosingReportIsNull"));
        }
    }

    private void viewReportedSign() {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest()
                .getUserClosingReportId())) {
            if (getBeanWrapper().getSelectedRequest().getUserClosingReportId()
                    .equals(this.getCurrentUser().getId())) {
                toDocumentGenerationFromSign();
            } else {
                RedirectHelper.goTo(PageTypes.PDF_VIEW, getBeanWrapper().getSelectedRequest().getId(), true);
            }
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"), ResourcesHelper
                            .getValidation("workListUserClosingReportIsNull"));
        }
    }

    private void callDicom(RadiologyExamRequest request,
                           List<Long> radExamItemsIdsForDicom, boolean isPublish) {
        callDicom(request, radExamItemsIdsForDicom, isPublish, null);
    }

    private void callDicom(RadiologyExamRequest request,
                           List<Long> radExamItemsIdsForDicom, boolean isPublish, Session session) {
        Pacs pacs = null;

        try {
            if (session == null) {
                session = DaoManager.getSession();
            }
            pacs = ConnectionManager.get(
                    Pacs.class,
                    new Criterion[]{
                            Restrictions
                                    .eq("hospital.id", getBeanWrapper().getSelectedRequest().getHospital()
                                    .getId()),
                            Restrictions
                                    .eq("pacsType", PacsType.WORKLIST_DICOM),
                            Restrictions.eq("sector.id", getBeanWrapper().getSelectedRequest().getSector().getId())
                    }, session);
        } catch (HibernateException | InstantiationException
                | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(dicomErrorLog, e);
        }

        if (pacs != null && pacs.getiPHostPacs() != null
                && pacs.getPortHostPacs() != null) {
            LogHelper.log(
                    dicomInfoLog,
                    "pacs.getiPHostPacs() = <<" + pacs.getiPHostPacs()
                            + ">> \tpacs.getPortHostPacs() = <<"
                            + pacs.getPortHostPacs() + ">>");

            Dcm4cheeHelper dcm4 = new Dcm4cheeHelper();
            dcm4.doActionWithExam(radExamItemsIdsForDicom, pacs, true,
                    isPublish, session);
        } else {
            LogHelper.log(dicomInfoLog, "pacs is null for this hospital ID = "
                    + getBeanWrapper().getSelectedRequest().getHospital().getId());
        }
    }

    private User checkDocumentGenerationPossibility()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (getBeanWrapper().getSelectedRequest() != null
                && getBeanWrapper().getSelectedRequest().getUserClosingReportId() != null) {
            return DaoManager.get(User.class, getBeanWrapper().getSelectedRequest().getUserClosingReportId());
        } else {
            return null;
        }
    }

    public void toDocumentGeneration() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        List<Long> itemsIds = this.getSeletedItemsIdsFromSelectedView(this
                .getSelectedRequestView());

        if (!ValidationHelper.isNullOrEmpty(itemsIds)) {
            redirectToDocumentGeneration(itemsIds, getBeanWrapper().getSelectedRequest()
                    .getId());
        } else {
            MessageHelper
                    .addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper
                                    .getValidation("radExamItemWrongSelect"),
                            "");
        }
    }

    public void redirectToDocumentGeneration(List<Long> itemsIds, Long id) {
        SessionHelper.putIds(itemsIds, ID_IN_SESSION);
        SessionHelper.put(Boolean.TRUE, PREVENT_RELOAD);
        SessionHelper.put(getShowShortView(), IS_SHORT_TABLE);
        RedirectHelper.goTo(PageTypes.DOCUMENT_GENERATION, id);
    }

    public void toDocumentGenerationFromDraft() {
        try {
            List<Long> itemIds = AggregationHelper
                    .getRadExamItemIdsWithSameFileEntityDraft(getBeanWrapper().getSelectedRequest());
            if (!ValidationHelper.isNullOrEmpty(itemIds)) {
                redirectToDocumentGeneration(itemIds, getBeanWrapper().getSelectedRequest()
                        .getId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void toDocumentGenerationFromReported() {
        try {
            List<Long> itemIds = AggregationHelper
                    .getRadExamItemIdsWithSameFileEntity(getBeanWrapper().getSelectedRequest());
            if (!ValidationHelper.isNullOrEmpty(itemIds)) {
                redirectToDocumentGeneration(itemIds, getBeanWrapper().getSelectedRequest()
                        .getId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void toDocumentGenerationFromSign() {
        try {
            List<Long> itemIds = AggregationHelper
                    .getRadExamItemIdsWithSignFileEntity(getBeanWrapper().getSelectedRequest());
            if (!ValidationHelper.isNullOrEmpty(itemIds)) {
                redirectToDocumentGeneration(itemIds, getBeanWrapper().getSelectedRequest()
                        .getId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public boolean getActionButtonDisablability(BaseWorklistView request)
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (request != null) {
            switch (request.getWaitingListRegistrationState()) {
                case RESERVED:
                    return this.getCanAccept();
                case ACCEPTED:
                case PARTIALLY_ACCEPTED:
                    return this.getCanPerformWorklist();
                case IN_READING:
                case PERFORMED:
                case DRAFT:
                    return this.getCanReportWorklist();
                case SIGNED:
                case DELETED:
                case REPORTED:
                    return this.getCanShow();
                default:
                    break;
            }
        }

        return false;
    }

    public void changeRequestItem() {
        if (this.getSelectedModifyExamWrapper() != null
                && !ValidationHelper.isNullOrEmpty(this.getTempItemsList())) {
            for (RadExamRequestItemWrapper wrap : this.getTempItemsList()) {
                if (wrap.getId().equals(
                        this.getSelectedModifyExamWrapper().getId())) {
                    try {
                        wrap.setRadiologyExam(DaoManager.get(
                                RadiologyExam.class, this.getExamId()));
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                    break;
                }
            }
        }
    }

    public void saveModifiedReqest() {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    saveModifiedReqestAction();
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onException(Exception e) throws Exception {
                    LogHelper.log(log, e);
                }
            });

            clearSession();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void saveModifiedReqestAction() {
        try {
            if (GeneralFunctionsHelper.validateRequest(getBeanWrapper().getSelectedRequest())) {
                for (RadiologyExamRequestItem item : getBeanWrapper().getSelectedRequest()
                        .getRadiologyExamRequestItems()) {
                    for (RadExamRequestItemWrapper wrap : this
                            .getTempItemsList()) {
                        if (wrap.getId().equals(item.getId())) {
                            item.setRadiologyExam(wrap.getRadiologyExam());
                            DaoManager.save(item);
                        }
                    }
                }

                getBeanWrapper().getSelectedRequest().udateItemsDescription(getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems());//FIXME: DELETE AFTER CREATE TRIGGER

                try {
                    getBeanWrapper().setSelectedRequest(ADTIntegrationHelper.getInstance()
                            .getRequestFieldsFromSectorId(this.getSectorId(),
                                    getBeanWrapper().getSelectedRequest()));
                } catch (IntegrationConnectionException e) {
                    LogHelper.log(log, e);
                }

                DaoManager.save(getBeanWrapper().getSelectedRequest());
                getBeanWrapper().getSelectedRequest().setSelectedAll(true);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void beforeLoadModifyDlg() {
        this.setExamId(null);
        this.setSelectedModifyExamWrapper(null);

        if (GeneralFunctionsHelper.validateRequest(getBeanWrapper().getSelectedRequest())) {
            try {
                this.setSectors(new ArrayList<SelectItem>());

                List<AsapSector> sectorAutocompleteHelperList = AsapSectorHelper.getASAPSIOSectorsFromDB(null);

                if (sectorAutocompleteHelperList != null) {
                    for (AsapSector saw : sectorAutocompleteHelperList) {
                        getSectors().add(
                                new SelectItem(saw.getId(), saw.getDescription()));
                    }
                }

                this.setExams(ComboboxHelper.fillList(
                        RadiologyExam.class,
                        Order.desc("description"),
                        new Criterion[]{
                                Restrictions.eq("examType.id", getBeanWrapper().getSelectedRequest().getExamType()
                                        .getId()),
                                Restrictions.eq("state",
                                        EnableDisableEnum.ENABLE)
                        }, false));

                if (getBeanWrapper().getSelectedRequest().getAsapSectorId() != null) {
                    this.setSectorId(getBeanWrapper().getSelectedRequest()
                            .getAsapSectorId());
                }

                this.setTempItemsList(new ArrayList<RadExamRequestItemWrapper>());

                for (RadiologyExamRequestItem item : getBeanWrapper().getSelectedRequest()
                        .getRadiologyExamRequestItems()) {
                    this.getTempItemsList().add(
                            new RadExamRequestItemWrapper(item.getId(), item
                                    .getRadiologyExam()));
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void cancel() {
        WaitingListStatusHelper.logActivity(UserActivityLogStates.CANCELLA,
                getBeanWrapper().getSelectedRequest().getId());

        if (!ValidationHelper.isNullOrEmpty(this.getSelectedTemplateId())) {
            try {
                TransactionExecuter.execute(new Action() {
                    @Override
                    public void execute() throws Exception {
                        internalButtonAction(WaitingListRegistrationStates.DELETED, null);
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

    public void accept() {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    internalButtonAction(WaitingListRegistrationStates.ACCEPTED, null);
                }

                @Override
                public void onSuccess() {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                            ResourcesHelper.getString("dateOperationSuccessAccept"), "");
                    ThreadExecutor.execute(new Action() {
                        @Override
                        public void execute() throws Exception {
                            TransactionExecuter.execute(
                                    new SinglePersistenceSessionAction() {
                                        @Override
                                        public void execute() {
                                            callDicom(getBeanWrapper().getSelectedRequest(), getRadExamItemsIdsForDicom(), true, getSession());
                                        }
                                    }
                            );
                        }
                    });
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
    }

    public void perform(final Date performDate) {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    internalButtonAction(WaitingListRegistrationStates.PERFORMED, performDate);
                }

                @Override
                public void onSuccess() {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                            ResourcesHelper
                                    .getString("dateOperationSuccessPerform"),
                            "");
                    callDicom(getBeanWrapper().getSelectedRequest(),
                            getRadExamItemsIdsForDicom(), false);
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
    }

    private void internalButtonAction(WaitingListRegistrationStates state, Date performDate)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, NotSelectedValidationException,
            AssignValidationException {
        if (validateSelection()) {
            WaitingListStatusHelper wlsh = new WaitingListStatusHelper();

            loadLazyItems(getSelectedRequestView());

            if (this.getSelectedRequestView() != null
                    && !ValidationHelper.isNullOrEmpty(this
                    .getSelectedRequestView()
                    .getRadiologyExamRequestItems())) {
                resetItems();

                List<Long> radExItemIds = wlsh.getSelectedIds(getBeanWrapper().getSelectedRequest());

                this.setRadExamItemsIdsForDicom(radExItemIds);

                switch (state) {
                    case DELETED:
                        wlsh.toCancelState(getBeanWrapper().getSelectedRequest(),
                                radExItemIds, this.getComment(),
                                GeneralFunctionsHelper.generatePdf(
                                        getBeanWrapper().getSelectedRequest(),
                                        getSelectedTemplateId(),
                                        getCurrentUser(), getComment(), null,
                                        null, false, null));
                        break;
                    case ACCEPTED:
                        wlsh.toAcceptedState(getBeanWrapper().getSelectedRequest(),
                                radExItemIds);
                        break;
                    case ANNULLED:
                        wlsh.toAnnuledState(getBeanWrapper().getSelectedRequest(),
                                radExItemIds, this.getComment(),
                                GeneralFunctionsHelper.generatePdf(
                                        getBeanWrapper().getSelectedRequest(),
                                        getSelectedTemplateId(),
                                        getCurrentUser(), getComment(), null,
                                        null, false, null));
                        break;
                    case PERFORMED:
                        wlsh.toPerformedState(getBeanWrapper().getSelectedRequest(), radExItemIds, performDate);
                        break;
                    case REPORTED:
                        wlsh.toReportedState(getBeanWrapper().getSelectedRequest(),
                                radExItemIds, null, null, Boolean.FALSE,
                                new Date());
                        break;
                    default:
                        break;
                }
            }
        } else {
            throw new NotSelectedValidationException();
        }
    }

    private void resetItems() throws PersistenceBeanException, IllegalAccessException {
        if (getSelectedRequestView().getRadiologyExamRequestItems().get(0) instanceof RadiologyExamRequestItem) {
            getBeanWrapper().getSelectedRequest()
                    .setRadiologyExamRequestItems((List<RadiologyExamRequestItem>) getSelectedRequestView().getRadiologyExamRequestItems());
        } else {
            List<Long> ids = new ArrayList<>();
            for (RadiologyExamRequestItemBase itemBase : getSelectedRequestView().getRadiologyExamRequestItems()) {
                ids.add(itemBase.getId());
            }
            List<RadiologyExamRequestItem> items = DaoManager.load(RadiologyExamRequestItem.class,
                    new Criterion[]{
                            Restrictions.in("id", ids)
                    });
            for (RadiologyExamRequestItem item : items) {
                for (RadiologyExamRequestItemBase requestItem : getSelectedRequestView().getRadiologyExamRequestItems()) {
                    if (item.getId().equals(requestItem.getId())) {
                        item.setSelected(requestItem.getSelected());
                        break;
                    }
                }
            }
            getBeanWrapper().getSelectedRequest()
                    .setRadiologyExamRequestItems(items);
        }
    }

    public void recheckCheckBoxes() {
        List<RadExamRequestItemWrapper> wrappers = this
                .getSelectedRequestWrapper().getRadExamRequestItemWrappers();
        Boolean disable = Boolean.TRUE;
        Long id = null;
        for (RadExamRequestItemWrapper wrapper : wrappers) {
            if (wrapper.getSelected() && id != null) {
                disable = Boolean.FALSE;
                break;
            }
            if (wrapper.getSelected()) {
                id = wrapper.getId();
            }
        }
        if (Boolean.TRUE.equals(disable) && id != null) {
            for (RadExamRequestItemWrapper wrapper : wrappers) {
                if (wrapper.getId().equals(id)) {
                    wrapper.setDisabled(Boolean.TRUE);
                }
            }
        } else {
            for (RadExamRequestItemWrapper wrapper : wrappers) {

                wrapper.setDisabled(Boolean.FALSE);

            }
        }

    }

    private boolean validateSelection() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (this.getSelectedRequestView() != null) {
            loadLazyItems(getSelectedRequestView());

            if (!ValidationHelper.isNullOrEmpty(this.getSelectedRequestView()
                    .getRadiologyExamRequestItems())) {
                resetItems();
                getBeanWrapper().getSelectedRequest().setExpanded(
                        getSelectedRequestView().isExpanded());

                if (getBeanWrapper().getSelectedRequest().isExpanded()) {
                    for (RadiologyExamRequestItemBase wlr : getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems()) {
                        if (wlr.getSelected()) {
                            return true;
                        }
                    }
                } else {
                    for (RadiologyExamRequestItemBase wlr : getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems()) {
                        wlr.setSelected(Boolean.TRUE);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private void aggregateActionAccepted() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, NotSelectedValidationException,
            CloneNotSupportedException, AssignValidationException {
        List<Long> radExItemIds = AggregationHelper
                .getSeletedItemsIdsFromRequestWrapper(this
                        .getSelectedRequestWrapper());
        if (!ValidationHelper.isNullOrEmpty(radExItemIds)) {
            List<Long> radExItemsForDicom = new ArrayList<>(radExItemIds);

            for (RadExamRequestItemWrapper reriw : this
                    .getForAggregationRequestsItemsWrappers()) {
                if (reriw.getSelected()) {
                    radExItemsForDicom.add(reriw.getId());
                }
            }

            this.setRadExamItemsIdsForDicom(radExItemsForDicom);

            WaitingListStatusHelper wlsh = new WaitingListStatusHelper();

            LogHelper.log(activityInfoLog,
                    "start\taction update request \tfor requestID " + getBeanWrapper().getSelectedRequest().getId());
            wlsh.toAcceptedState(getBeanWrapper().getSelectedRequest(), radExItemIds);
            LogHelper.log(activityInfoLog,
                    "end\taction update request \tfor requestID " + getBeanWrapper().getSelectedRequest().getId());

            RadiologyExamRequestItem requestItem = DaoManager.get(
                    RadiologyExamRequestItem.class, radExItemIds.get(0));
            RadiologyExamRequest request = requestItem
                    .getRadiologyExamRequest();

            if (!ValidationHelper.isNullOrEmpty(request)) {
                AggregationHelper.aggregateActionAcceta(request,
                        this.getSelectedRequestWrapper(),
                        this.getForAggregationRequestsWrappers());

                if (!request.equals(getBeanWrapper().getSelectedRequest())) {
                    getBeanWrapper().setSplitedRequest(request);
                }
            }
        }
    }

    private void aggregateActionPerfomed() {
        if (!ValidationHelper.isNullOrEmpty(this
                .getForAggregationRequestsWrappers())
                && !ValidationHelper.isNullOrEmpty(this
                .getSelectedRequestWrapper())
                && !ValidationHelper.isNullOrEmpty(this
                .getSelectedRequestWrapper()
                .getRadExamRequestItemWrappers())) {
            List<Long> radiologyExamRequestItemIds = AggregationHelper
                    .getRequestItemsIdsForDocumentGeneration(
                            this.getSelectedRequestWrapper(),
                            this.getForAggregationRequestsItemsWrappers());

            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestItemIds)) {
                redirectToDocumentGeneration(radiologyExamRequestItemIds, this
                        .getSelectedRequestWrapper().getId());
            } else {
                MessageHelper
                        .addGlobalMessage(
                                FacesMessage.SEVERITY_ERROR,
                                ResourcesHelper
                                        .getValidation("radExamItemWrongSelect"),
                                "");
            }
        }
    }

    private List<Long> getSeletedItemsIdsFromSelectedView(
            BaseWorklistView waitinglistView) throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        List<Long> selectedItemsIds = null;

        loadLazyItems(waitinglistView);

        if (!ValidationHelper.isNullOrEmpty(waitinglistView
                .getRadiologyExamRequestItems())) {
            selectedItemsIds = new ArrayList<>();

            for (RadiologyExamRequestItemBase reri : waitinglistView.getRadiologyExamRequestItems()) {
                if (!waitinglistView.isExpanded() || reri.getSelected()) {
                    selectedItemsIds.add(reri.getId());
                }
            }
        }

        return selectedItemsIds;
    }

    private void showAssignErrorMsg(String s1, String s2) {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                ResourcesHelper.getValidation(s1),
                ResourcesHelper.getValidation(s2));
    }

    private void innerOnException(Exception e) {
        if (e instanceof NotSelectedValidationException) {
            showAssignErrorMsg("selectionEmpty", "selectAtLeastOneItem");
        } else if (e instanceof AssignValidationException) {
            showAssignErrorMsg("assignFailed", "assignCreateEventCalendar");
        } else if (e instanceof StaleObjectStateException) {
            showAssignErrorMsg("assignFailed",
                    "assignModifiedInAnotherTransaction");
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("dateOperationFailed"), "");
            LogHelper.log(log, e);
        }
    }

    public void viewExitingReport() {
        GeneralFunctionsHelper.openPdfInNewTab(getBeanWrapper().getSelectedRequest());
    }

    public void showReport() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedTemplateId())) {
            GeneralFunctionsHelper.showReport(getBeanWrapper().getSelectedRequest(),
                    getSelectedTemplateId(), getCurrentUser(), getComment(),
                    null, false, null);
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("noDocumentTemplates"));
        }
    }

    protected void clearSession() {
        try {
            DaoManager.getSession().flush();
            DaoManager.getSession().clear();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void deselectToCancel() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (GeneralFunctionsHelper.validateRequest(getBeanWrapper().getSelectedRequest())) {
            loadLazyItems(getSelectedRequestView());

            for (RadiologyExamRequestItemBase item : this.getSelectedRequestView()
                    .getRadiologyExamRequestItems()) {
                item.setSelected(Boolean.FALSE);
            }
        }
    }

    private void loadLazyItems(BaseWorklistView request) {
        if (request != null && ValidationHelper.isNullOrEmpty(request.getRadiologyExamRequestItems())) {
            try {
                request.loadLazyValues(WorkingListHelper.getActualBean(getShowShortView()));
            } catch (Exception e) {
                LogHelper.log(log, e);
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

    public void onSelectAllPrintDialog() {
        for (RadExamRequestWrapper wrapper : getRadExamRequestWrappers()) {
            wrapper.setSelected(getSelectAllPrintDialog());
        }
    }

    public void chooseTemplateForDailyReport() {
        try {
            LogHelper.debugInfo(log, "Start select sectors in ids <" + Arrays.toString(getCurrentUser().getSectors().toArray()) + ">");
            List<Long> sectorIds = DaoManager.loadField(InstancePhases.class, "sector.id", Long.class,
                    new Criterion[]{Restrictions.eq("place", DocumentGenerationPlaces.RESERVATION),
                            Restrictions.in("sector.id", getCurrentUser().getSectors())});
            LogHelper.debugInfo(log, "Select sector result <" + Arrays.toString(sectorIds.toArray()) + ">");

            List<Long> examTypeIds = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(getSelectedExamTypesForDialogFilter())) {
                for (ExamTypeWrapper diagnosticWrapper : getSelectedExamTypesForDialogFilter()) {
                    LogHelper.debugInfo(log, "Selected Exam Type <" + diagnosticWrapper.getRealExamType().getId()
                            + "#" + diagnosticWrapper.getRealExamType().getDescription() + ">");
                    examTypeIds.add(diagnosticWrapper.getRealExamType().getId());
                }
            } else {
                LogHelper.debugInfo(log, "Selected Exam Types is empty");
            }
            if (ValidationHelper.isNullOrEmpty(getPrintRequestDate())) {
                setPrintRequestDate(new Date());
            }
            LogHelper.debugInfo(log, "Print Request Date " + getPrintRequestDate());

            Calendar lessCalendar = Calendar.getInstance();
            lessCalendar = DateTimeHelper.getDayStart(lessCalendar);
            lessCalendar.add(Calendar.DAY_OF_MONTH, -NUMBER_OF_DAYS);

            Calendar dateCurrentCalendar = Calendar.getInstance();
            dateCurrentCalendar.setTime(getPrintRequestDate());

            List<CriteriaAlias> aliases = new ArrayList<>();
            aliases.add(new CriteriaAlias("patient", "patient", JoinType.INNER_JOIN));

            List<Criterion> criterions = new ArrayList<>();
            criterions.add(Restrictions.between("reserveDate", DateTimeHelper.getDayStart(getPrintRequestDate()),
                    DateTimeHelper.getDayEnd(getPrintRequestDate())));
            if (!ValidationHelper.isNullOrEmpty(sectorIds)) {
                criterions.add(Restrictions.in("sector.id", sectorIds));
            }
            if (!ValidationHelper.isNullOrEmpty(examTypeIds)) {
                aliases.add(new CriteriaAlias("radiologyExamRequestItems", "items", JoinType.INNER_JOIN));
                aliases.add(new CriteriaAlias("items.radiologyExam", "radiologyExam", JoinType.INNER_JOIN));
                aliases.add(new CriteriaAlias("radiologyExam.examType", "examType", JoinType.INNER_JOIN));
                criterions.add(Restrictions.in("examType.id", examTypeIds));
            }

            if (dateCurrentCalendar.after(lessCalendar)) {
                LogHelper.debugInfo(log, "Search in short request table");
                List<RadiologyExamRequestShort> requests = DaoManager.load(RadiologyExamRequestShort.class,
                        aliases.toArray(new CriteriaAlias[0]), criterions.toArray(new Criterion[0]),
                        new Order[]{Order.asc("patient.surname"), Order.asc("patient.name")});
                LogHelper.debugInfo(log, "Result size " + requests.size());
                setRadExamRequestWrappers(new ArrayList<RadExamRequestWrapper>());
                if (!ValidationHelper.isNullOrEmpty(requests)) {
                    setSelectAllPrintDialog(true);
                    for (RadiologyExamRequestShort request : requests) {
                        RadExamRequestWrapper wrapper = request.getRadExamRequestWrapperFromRequest();
                        wrapper.setSelected(true);
                        getRadExamRequestWrappers().add(wrapper);
                    }
                }
            } else {
                LogHelper.debugInfo(log, "Search in request table");
                List<RadiologyExamRequest> requests = DaoManager.load(RadiologyExamRequest.class,
                        aliases.toArray(new CriteriaAlias[0]), criterions.toArray(new Criterion[0]),
                        new Order[]{Order.asc("patient.surname"), Order.asc("patient.name")});
                setRadExamRequestWrappers(new ArrayList<RadExamRequestWrapper>());
                LogHelper.debugInfo(log, "Result size " + requests.size());
                if (!ValidationHelper.isNullOrEmpty(requests)) {
                    setSelectAllPrintDialog(true);
                    for (RadiologyExamRequest request : requests) {
                        RadExamRequestWrapper wrapper = request.getRadExamRequestWrapperFromRequest();
                        wrapper.setSelected(true);
                        getRadExamRequestWrappers().add(wrapper);
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void printRequests() {
        try {
            List<Long> selectedIds = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(getRadExamRequestWrappers())) {
                for (RadExamRequestWrapper wrapper : getRadExamRequestWrappers()) {
                    if (wrapper.getSelected()) {
                        selectedIds.add(wrapper.getId());
                    }
                }

                Calendar lessCalendar = DateTimeHelper.getDayStart(Calendar.getInstance());
                lessCalendar.add(Calendar.DAY_OF_MONTH, -NUMBER_OF_DAYS);

                Calendar dateCurrentCalendar = Calendar.getInstance();
                dateCurrentCalendar.setTime(getPrintRequestDate());

                if (dateCurrentCalendar.after(lessCalendar)) {
                    List<RadiologyExamRequestShort> requests = DaoManager.load(RadiologyExamRequestShort.class,
                            new Criterion[]{Restrictions.in("id", selectedIds)});
                    String fileName = GeneralFunctionsHelper.showReport(requests, getCurrentUser());
                    printFile(fileName);
                } else {
                    List<RadiologyExamRequest> requests = DaoManager.load(RadiologyExamRequest.class,
                            new Criterion[]{Restrictions.in("id", selectedIds)});
                    String fileName = GeneralFunctionsHelper.showReport(requests, getCurrentUser());
                    printFile(fileName);
                }
            }
            closeDialog();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }


    public void postponedExecution() {
        this.cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getPostponedExecutionDate())) {
            this.addFieldExeption("form:postponedExecutionDate", "workListNoAddPostponedExecutionDate");
        }
        if (ValidationHelper.isNullOrEmpty(getPostponedExecutionReason())) {
            this.addFieldExeption("form:postponedExecutionReason", "workListNoAddPostponedExecutionReason");
        }
        if (getValidationFailed()) {
            return;
        }
        try {
            getBeanWrapper().setSelectedRequest(DaoManager.get(RadiologyExamRequest.class,
                    getSelectedRequestView().getId()));
            if (GeneralFunctionsHelper.validateRequest(getBeanWrapper().getSelectedRequest())
                    && getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems().get(0)
                    .getWaitingListRegistrationState() != null) {
                if (!getBeanWrapper().getSelectedRequest().getRadiologyExamRequestItems().get(0)
                        .getWaitingListRegistrationState().name()
                        .equals(this.getSelectedRequestState())
                        && !WaitingListRegistrationStates.IN_READING.name().equals(getSelectedRequestState())) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                            ResourcesHelper.getValidation("warning"),
                            ResourcesHelper
                                    .getValidation("workListRequestWasChanged"));

                    return;
                }
                getSelectedRequestView().setSelectedAll(true);
                Date performDate = DateTimeHelper.getMergeDates(getPostponedExecutionDate(), getPostponedExecutionTime());
                perform(performDate);

                RequestNote note = new RequestNote();
                note.setPerformDate(performDate);
                note.setNote(String.format("%s: %s", ResourcesHelper.getString("workListNotePrefix"), getPostponedExecutionReason()));
                note.setFromSio(Boolean.FALSE);
                note.setRadiologyExamRequest(getBeanWrapper().getSelectedRequest());

                DaoManager.save(note, true);
                resetPostponedExecutionDlg();
                executeJS("callFilterTableFromPanel();");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void resetPostponedExecutionDlg() {
        setPostponedExecutionDate(new Date());
        setPostponedExecutionReason(null);
        executeJS("PF('postponedExecutionWV').hide();");
    }

    private void printFile(String fileName) {
        if (getBeanWrapper().getLabelPrintActivePreference() != null
                && Boolean.TRUE.equals(getBeanWrapper().getLabelPrintActivePreference()
                .getLabelPrintActive())) {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();

            request.getRequestURL().substring(
                    0,
                    request.getRequestURL().indexOf(
                            request.getContextPath()));
            this.setPrintFile(request.getRequestURL().substring(
                    0,
                    request.getRequestURL().indexOf(
                            request.getContextPath()))
                    + request.getContextPath() + "/" + "/File/" + fileName);
            this.setShowApplet(Boolean.TRUE);
            executeJS("disableApplet();");
        }

        tryPrintWithApplet(fileName);
    }

    public void chooseTemplate() throws HibernateException, IOException,
            PersistenceBeanException {
        try {
            this.fillEtichTemplates();
        } catch (IllegalAccessException e) {
            LogHelper.log(log, e);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEtichTemplates())) {
            if (this.getEtichTemplates().size() == 1) {
                setSelectedEtichTemplateId((Long) this.getEtichTemplates()
                        .get(0).getValue());
                generate();
            } else {
                executeJS("PF('templates').show();");
            }
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("noDocumentTemplates"));
        }
    }

    public void generate() throws IOException, HibernateException,
            PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest())) {
            List<Long> radExamsIds = new ArrayList<>();
            List<Long> examsIds = new ArrayList<>();

            if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest()
                    .getRadiologyExamRequestItems())) {
                for (RadiologyExamRequestItem reri : getBeanWrapper().getSelectedRequest()
                        .getRadiologyExamRequestItems()) {
                    Long id = reri.getRadiologyExam().getId();
                    radExamsIds.add(id);
                    examsIds.add(reri.getId());
                }
            }

            SessionHelper.putIds(radExamsIds, ID_IN_SESSION_FOR_TAGS);
            SessionHelper.putIds(examsIds, ID_IN_SESSION_FOR_PRINT);

            String fileName = GeneralFunctionsHelper.showReport(
                    getBeanWrapper().getSelectedRequest(), getSelectedEtichTemplateId(),
                    getCurrentUser(), null, null, false, null);

            printFile(fileName);

            SessionHelper.removeObject(ID_IN_SESSION_FOR_TAGS);
        }
    }

    private void tryPrintWithApplet(String fileName) {
        if (getBeanWrapper().getLabelPrintActivePreference() != null
                && Boolean.TRUE.equals(getBeanWrapper().getLabelPrintActivePreference()
                .getLabelPrintActive())) {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();

            request.getRequestURL().substring(0,
                    request.getRequestURL().indexOf(request.getContextPath()));

            this.setPrintCount(getBeanWrapper().getLabelPrintCountPreference()
                    .getLabelPrintCount());
            this.setPrinterName(getBeanWrapper().getLabelPrintPrinterNamePreference()
                    .getLabelPrintPrinterName());
            this.setPrintFile(request.getRequestURL().substring(0,
                    request.getRequestURL().indexOf(request.getContextPath()))
                    + request.getContextPath() + "/" + "/File/" + fileName);
            this.setShowApplet(Boolean.TRUE);
            executeJS("disableApplet();");
        }
    }

    private void tryPrintWithAppletDG(FileEntity fileEntity) {
        if (getBeanWrapper().getLabelPrintActivePreferenceDocg() != null
                && Boolean.TRUE.equals(getBeanWrapper().getLabelPrintActivePreferenceDocg()
                .getLabelPrintActiveDocg())) {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();

            request.getRequestURL().substring(0,
                    request.getRequestURL().indexOf(request.getContextPath()));
            this.setPrintCount(getBeanWrapper().getLabelPrintCountPreferenceDocg()
                    .getLabelPrintCountDocg());
            this.setPrinterName(getBeanWrapper().getLabelPrintPrinterNamePreferenceDocg()
                    .getLabelPrintPrinterNameDocg());
            this.setPrintFile(request.getRequestURL().substring(0,
                    request.getRequestURL().indexOf(request.getContextPath()))
                    + request.getContextPath()
                    + "/"
                    + "/File/"
                    + fileEntity.getName());
            this.setShowApplet(Boolean.TRUE);
            executeJS("disableApplet();");
        }
    }

    public void resendDicomAgain() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    resendDicomAgainAction();
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onException(Exception e) throws Exception {
                    LogHelper.log(dicomErrorLog, e);
                }
            });
        } catch (Exception e) {
            LogHelper.log(dicomErrorLog, e);
        }
    }

    public void addNewAllergy(BaseWorklistView item) {
        cleanValidation();
        setEditItem(item);
        Allergy allergy = new Allergy();
        allergy.setDate(new Date());
        allergy.setPatient(item.getPatient());
        item.setEditAllergy(allergy);
    }

    public void saveAllergy() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(getEditItem().getEditAllergy().getAllergy())) {
            addRequiredFieldExeption("allergyStrId");
            return;
        }
        DaoManager.save(getEditItem().getEditAllergy(), true);
        getEditItem().updateAllergies();
        cleanValidation();
        executeJS("PF('addAllergyNoteWV').hide(); PF('tableWV').collapseAllRows()");
        PrimeFaces.current().ajax().update("table");
    }

    public void editAllergyDlg(BaseWorklistView item, Long id) throws PersistenceBeanException, IllegalAccessException {
        cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(item.getAllergies()) &&
                !ValidationHelper.isNullOrEmpty(id)) {
            setEditItem(item);
            Iterator<Allergy> iterator = item.getAllergies().iterator();
            while (iterator.hasNext()) {
                Allergy allergy = iterator.next();
                if (id.equals(allergy.getId())) {
                    DaoManager.getSession().refresh(allergy);
                    if (allergy.getDate() == null) {
                        allergy.setDate(new Date());
                    }
                    item.setEditAllergy(allergy);
                    return;
                }
            }
        }
    }

    private void resendDicomAgainAction() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(this
                .getRequestIdToPartAcceptActions())) {
            RadiologyExamRequest request = DaoManager.get(
                    RadiologyExamRequest.class,
                    this.getRequestIdToPartAcceptActions());

            List<Long> radExamItemsIdsList = new ArrayList<>();

            if (!ValidationHelper.isNullOrEmpty(request)
                    && !ValidationHelper.isNullOrEmpty(request
                    .getRadiologyExamRequestItems())) {
                for (RadiologyExamRequestItem reri : request
                        .getRadiologyExamRequestItems()) {
                    radExamItemsIdsList.add(reri.getId());
                }
            }

            callDicom(request, radExamItemsIdsList, true);
        }
    }

    public void onRowToggle(ToggleEvent event) throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (event != null) {
            BaseWorklistView radExReq = (BaseWorklistView) event.getData();

            if (!ValidationHelper.isNullOrEmpty(radExReq)) {
                radExReq.setExpanded(!radExReq.isExpanded());
                radExReq.setSelectedAll(true);
                radExReq.loadLazyValues(WorkingListHelper.getActualBean(getShowShortView()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<SortMeta> getDefaultSortBy() {
        return (Collection<SortMeta>) SessionHelper.get(
                EntityLazyListModel.SORTFIELDS_IN_SESSION);
    }

    public Boolean getShowAccetionNumberOrNot() {
        if (!ValidationHelper.isNullOrEmpty(getBeanWrapper().getSelectedRequest())) {
            return !WaitingListRegistrationStates.RESERVED.equals(getBeanWrapper().getSelectedRequest().getWaitingListRegistrationState());
        }

        return Boolean.FALSE;
    }

    public void selectStateForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getAllStates())) {
            for (WaitingListRegistrationStateWrapper wkrsw : this
                    .getAllStates()) {
                if (wkrsw.customEquals(this.getSelectedStateForFilter())) {
                    wkrsw.setSelected(!wkrsw.getSelected());
                    break;
                }
            }
        }
    }

    public void selectUrgencyForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getAllUrgencies())) {
            for (UrgencyWrapper uw : this.getAllUrgencies()) {
                if (uw != null
                        && uw.customEquals(this.getSelectedUrgencyForFilter())) {
                    uw.setSelected(!uw.getSelected());
                    break;
                }
            }
        }
    }

    public void selectExamTypeForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getAllExamTypes())) {
            for (ExamTypeWrapper etw : this.getAllExamTypes()) {
                if (etw.customEquals(this.getSelectedTypeForFilter())) {
                    etw.setSelected(!etw.getSelected());
                    break;
                }
            }
        }
    }

    public void selectExamTypeTopForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getAllExamTypes())) {
            for (ExamTypeWrapper etw : this.getAllExamTypes()) {
                if (etw.customEquals(this.getSelectedTypeForFilterTop())) {
                    etw.setSelected(!etw.getSelected());
                }
            }
        }
    }

    public void selectDiagnosticForFilter() {
        if (!ValidationHelper.isNullOrEmpty(this.getAllDiagnostics())) {
            for (DiagnosticWrapper dw : this.getAllDiagnostics()) {
                if (dw != null && dw.customEquals(this.getSelectedDiagnosticForFilter())) {
                    dw.setSelected(!dw.getSelected());
                }
            }
        }
    }

    public void selectDiagnosticForDialogFilter(SelectEvent<DiagnosticWrapper> rowSelectEvent) {
        DiagnosticWrapper wrapper = rowSelectEvent.getObject();
        if (wrapper.getSelected()) {
            getSelectedDiagnosticForDialogFilter().remove(wrapper);
        }
        wrapper.setSelected(!wrapper.getSelected());
    }

    public void selectExamTypeForDialogFilter(SelectEvent<ExamTypeWrapper> rowSelectEvent) {
        ExamTypeWrapper wrapper = rowSelectEvent.getObject();
        if (wrapper.getSelected()) {
            getSelectedExamTypesForDialogFilter().remove(wrapper);
        }
        wrapper.setSelected(!wrapper.getSelected());
    }

    public void openPrintRequestsDialog() {
        Map<String, Object> dialogProperties = new HashMap<>();
        dialogProperties.put("modal", true);
        dialogProperties.put("resizable", false);
        dialogProperties.put("contentHeight", 450);
        PrimeFaces.current().dialog().openDynamic("/Pages/ManagementGroup/PrintRequestsDialog.xhtml", dialogProperties, null);
    }

    public void openPSD() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedRequestView())
                && !ValidationHelper.isNullOrEmpty(getSelectedRequestView()
                .getPsdNumber())) {
            WaitingListStatusHelper.openPSD(getSelectedRequestView()
                    .getPsdNumber());
        }
    }

    public void hideApplet() {
        this.setShowApplet(Boolean.FALSE);
    }

    public boolean isDisableTemplates() {
        return disableTemplates;
    }

    public void setDisableTemplates(boolean disableTemplates) {
        this.disableTemplates = disableTemplates;
    }

    public void setSelectedRequestView(BaseWorklistView selectedRequestView)
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        this.selectedRequestView = selectedRequestView;

        if (!ValidationHelper.isNullOrEmpty(selectedRequestView)) {
            RadiologyExamRequest rer = DaoManager.get(
                    RadiologyExamRequest.class, selectedRequestView.getId());
            getBeanWrapper().setSelectedRequest(rer);
        } else {
            getBeanWrapper().setSelectedRequest(null);
        }
    }

    public Boolean getToNextStateFromDialog() {
        return toNextStateFromDialog == null ? Boolean.FALSE
                : toNextStateFromDialog;
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

    public void setSelectedAllUrgenciesOnPanel(boolean selectedAllStatesOnPanel) {
        if (this.getAllUrgencies() != null) {
            for (UrgencyWrapper uw : this.getAllUrgencies()) {
                uw.setSelected(selectedAllStatesOnPanel);
            }
        }
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

    public void setSelectedAllExamTypesOnPanel(boolean selectedAllStatesOnPanel) {
        if (this.getAllExamTypes() != null) {
            for (ExamTypeWrapper etw : this.getAllExamTypes()) {
                etw.setSelected(selectedAllStatesOnPanel);
            }
        }
    }

    public boolean getSelectedAllExamTypesTopOnPanel() {
        if (this.getAllExamTypes() != null) {
            for (ExamTypeWrapper etw : this.getAllExamTypes()) {
                if (!etw.getSelected()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void setSelectedAllExamTypesTopOnPanel(
            boolean selectedAllStatesOnPanel) {
        if (this.getAllExamTypes() != null) {
            for (ExamTypeWrapper etw : this.getAllExamTypes()) {
                etw.setSelected(selectedAllStatesOnPanel);
            }
        }
    }

    public boolean getSelectedAllDiagnosticsOnPanel() {
        return getSelectedDiagnostics(getAllDiagnostics());
    }

    public void setSelectedAllDiagnosticsOnPanel(boolean selectedAllStatesOnPanel) {
        setSelectedDiagnostics(getAllDiagnostics(), selectedAllStatesOnPanel);
    }

    public boolean getSelectedDiagnostics(List<DiagnosticWrapper> allDiagnostics) {
        if (!ValidationHelper.isNullOrEmpty(allDiagnostics)) {
            for (DiagnosticWrapper dw : allDiagnostics) {
                if (!dw.getSelected()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setSelectedDiagnostics(List<DiagnosticWrapper> diagnostics, boolean selectedAllStatesOnPanel) {
        if (this.getAllDiagnostics() != null) {
            for (DiagnosticWrapper dw : this.getAllDiagnostics()) {
                dw.setSelected(selectedAllStatesOnPanel);
            }
        }
    }

    public int getListSize() {
        try {
            if (getShowShortView()) {
                return this.getShortLazyModel().getRowCount();
            }
            return this.getLazyModel().getRowCount();
        } catch (NullPointerException e) {
            return 0;
        } catch (Exception e) {
            LogHelper.log(log, e);
            return 0;
        }
    }

    public void setSelectedExamTypes(List<Long> selectedExamTypes) {
        this.selectedExamTypes = selectedExamTypes;
        this.selectedExamTypesTop = selectedExamTypes;
    }

    public void setSelectedTypeForFilterTop(
            ExamTypeWrapper selectedTypeForFilterTop) {
        this.selectedTypeForFilterTop = selectedTypeForFilterTop;
        this.selectedTypeForFilter = selectedTypeForFilterTop;
    }

    public void showPopUp() {
        if (Boolean.TRUE.equals(getShowPopUp())) {
            User userClosing;
            try {
                userClosing = checkDocumentGenerationPossibility();
                MessageHelper.addGlobalMessage(
                        FacesMessage.SEVERITY_INFO,
                        ResourcesHelper
                                .getString("worklistReportingToTheDoctorOpen")
                                .concat(" ")
                                .concat(userClosing.getFullname())
                                .concat(" in data ")
                                .concat(DateTimeHelper
                                        .ToDatePatternWithMinutesAndSuffix(getBeanWrapper().getSelectedRequest()
                                                .getBlockingDate())), "");
            } catch (Exception e) {
                LogHelper.log(log, e);
            } finally {
                setShowPopUp(Boolean.FALSE);
            }
        }
    }

    public Boolean getShowPopUp() {
        return showPopUp;
    }

    public void setShowPopUp(Boolean showPopUp) {
        this.showPopUp = showPopUp;
    }

    public Boolean getTepmlatesForReservedState() {
        return tepmlatesForReservedState;
    }

    public void setTepmlatesForReservedState(Boolean tepmlatesForReservedState) {
        this.tepmlatesForReservedState = tepmlatesForReservedState;
    }

    public Boolean getShowApplet() {
        return showApplet;
    }

    public void setShowApplet(Boolean showApplet) {
        this.showApplet = showApplet;
    }

    public Boolean getShowShortView() {
        return showShortView;
    }

    public void setShowShortView(Boolean showShortView) {
        this.showShortView = showShortView;
    }

    public Boolean getSelectAllPrintDialog() {
        return selectAllPrintDialog;
    }

    public void setSelectAllPrintDialog(Boolean selectAllPrintDialog) {
        this.selectAllPrintDialog = selectAllPrintDialog;
    }
}
