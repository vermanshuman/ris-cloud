package it.nexera.ris.web.beans.pages;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.message.ACK;
import ca.uhn.hl7v2.model.v26.message.MDM_T02;
import com.hrdo.osirix.OsirixHandler;
import com.hrdo.pacs.DicomHandler;
import com.hrdo.pacs.commons.AET;
import it.nexera.ris.common.annotations.ReattachIgnore;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.RadiologyExamRequestItemBlockingException;
import it.nexera.ris.common.exceptions.validation.AssignValidationException;
import it.nexera.ris.common.exceptions.validation.NotSelectedValidationException;
import it.nexera.ris.common.executors.ThreadExecutor;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.GeneralFunctionsHelper;
import it.nexera.ris.common.helpers.logic.OsirixHelper;
import it.nexera.ris.common.helpers.logic.hl7.BaseHl7MessageHelper;
import it.nexera.ris.common.helpers.logic.hl7.MDMHelper;
import it.nexera.ris.common.xml.wrappers.HistoricalReportXMLWrapper;
import it.nexera.ris.persistence.*;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.persistence.beans.entities.domain.relation.DiagnosticRadiologyExam;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityListPageBean;
import it.nexera.ris.web.beans.wrappers.DocumentTemplateWrapper;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.logic.ApplicationSettingsValueWrapper;
import it.nexera.ris.web.beans.wrappers.logic.DocumentGenerationHistoryWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestItemWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import it.nexera.ris.web.services.FSEService;
import it.nexera.ris.web.services.servlets.GetUnsignedFileServlet;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.*;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.extensions.event.RotateEvent;
import org.primefaces.extensions.model.layout.LayoutOptions;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
@Named("documentGenerationBean")
@ViewScoped
public class DocumentGeneration extends EntityListPageBean<IndexedEntity>
        implements IAggregated, Serializable {

    private static final long serialVersionUID = 1400780384025864163L;

    private static Logger hl7ErrorLog = CustomLibLoggerFactory.getHl7ErrorLogger();

    private static Logger hl7InfoLog = CustomLibLoggerFactory.getHl7InfoLogger();

    private static Logger osirixErrorLog = CustomLibLoggerFactory.getOsirixErrorLogger();

    private static Logger osirixInfoLog = CustomLibLoggerFactory.getOsirixInfoLogger();

    private static Logger activityErrorLog = CustomLibLoggerFactory.getActivityErrorLogger();

    private final String ID_IN_SESSION = "RadiologyExamRequestsItemsIds";

    private final String ID_IN_SESSION_FOR_TAGS = "RadiologyExamsIdsForPrint";

    private final String ID_IN_SESSION_FOR_PRINT = "RadiologyExamRequestsItemsIdsForTads";

    private String editorValue;

    private String vocalContent;

    private Date currentDate;

    @ReattachIgnore
    private RadiologyExamRequest radiologyExamRequest;

    private List<SelectItem> users;

    private Long selectedExamTypeId;

    private List<SelectItem> examTypes;

    private List<SelectItem> exams;

    private String selectedExam;

    private List<SelectItem> results;

    private String selectedResult;

    @ReattachIgnore
    private List<Glossary> glossaries;

    @ReattachIgnore
    private List<Glossary> allGlossaries;

    private Glossary currentGlossary;

    private List<DocumentGenerationHistoryWrapper> historyDocuments;

    private List<SelectItem> templates;

    private Long selectedTemplateId;

    private DocumentTemplateWrapper filledTemplate;

    private Long selectedHistoryId;

    private String currentStateToGeneratePdf;

    private String codeOnDialog;

    private boolean disableDICOMButton;

    private boolean unblockItems = true;

    private Boolean consistInOsirixList;

    private String osirixConnectionSettings;

    private Osirix currentOsirix;

    private Boolean enableHtmlEditorButton;

    private Boolean editorDisabled;

    private String linkToRedirect;

    private String tempLinkToRedirect;

    private Integer progressbarValue;

    private RadExamRequestWrapper selectedRequestWrapper;

    private List<RadExamRequestWrapper> forAggregationRequestsWrappers;

    private List<RadExamRequestItemWrapper> forAggregationRequestsItemsWrappers;

    private List<RadExamRequestWrapper> allRequestsWrappers;

    private List<RadExamRequestItemWrapper> allRequestsItemsWrappers;

    private Boolean disableButtonsAndFieldsInReportedCase;

    private Boolean disableButtonsAndFieldsInSignedCase;

    private String weasisUrlArray;

    private List<RadiologyExam> radiologyExams;

    private List<RadiologyExam> filteredRadiologyExams;

    private List<RadiologyExam> selectedRadiologyExams;

    private List<RadiologyExam> removeRadiologyExams;

    private List<SelectItem> accessNumbers;

    private Long selectedAccessNumber;

    private Boolean requestedToLeave = Boolean.FALSE;

    private Boolean releaseReport = Boolean.FALSE;

    private Boolean disableAddExamAN;

    private Boolean showRedBar;

    private Boolean showGreenBar;

    private Boolean descriptionCheckbox;

    private Boolean isDocumentHidden;

    private Boolean isDocumentSecure;

    private String examTypesText;

    private Boolean pdfGenButtonDisabled;

    private Boolean saveText = Boolean.TRUE;

    private LayoutOptions layoutOptions;

    private Boolean moveToPerformedState = Boolean.FALSE;

    //NUMBER OF THE ZOOM DROPDOWN INSIDE THE CKEDITOR TOOLBAR
    private static final long PDF_ZOOM_NUMBER = 9L;

    private List<RequestNote> notes;

    private String userLockChangeRedirectUserName;

    private Boolean showApplet;

    private String printFile;

    private Boolean autoSavePresent;

    private Boolean preferenceAutoSave;

    private String pdfFileId;

    private String pdfFileName;

    private String userId;

    private Boolean fromSigned;

    private FileEntity signFileEntity;

    private String uuid;

    private Date reportDate;

    private String tagFormString;

    private String initialTags;

    private String userFiscalCode;

    @ReattachIgnore
    private List<RequestAttachedFile> requestAttachedFiles;

    @ReattachIgnore
    private List<RequestAttachedFile> requestImagesForDelete;

    private AtomicLong tempId;

    private Long editImageId;

    private String editImagePath;

    private Boolean disableTagsButton;

    private Boolean renderHistoricalReports;

    private Boolean showAllergyWarning;

    private Boolean renderVoiceRecognitionBlock;

    private String buttonsColumnClass;

    private Boolean isShort;

    private List<Allergy> allergies;

    private SpVoiceService spVoiceService;

    @SuppressWarnings("unchecked")
    @Override
    public void onLoad() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        setSpVoiceService(new SpVoiceService());
        try {
            log.debug("DocumentGeneration onLoad");
            Object isShortTable = SessionHelper.get(WorkListBean.IS_SHORT_TABLE);
            setShort((isShortTable != null) && ((Boolean) isShortTable));
            setTempId(new AtomicLong(0));
            String sessionKey = UUID.randomUUID().toString();
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(sessionKey, this);
            if (!ValidationHelper.isNullOrEmpty(this.getEditingEntityId())) {
                log.debug("ID from url is: ".concat(getEditingEntityId()));
                setRadiologyExamRequest(DaoManager.get(
                        RadiologyExamRequest.class,
                        Long.parseLong(this.getEditingEntityId())));
                if (getRadiologyExamRequest() != null) {
                    log.debug("Entity from db is: ".concat(
                            String.valueOf(Objects.requireNonNullElse(
                                            getRadiologyExamRequest().getId(), 0L))));
                } else {
                    log.debug("Entity from db is null");
                }

                if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequest().getPatient())
                        && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequest().getPatient().getAllergies())) {
                    setShowAllergyWarning(true);
                    setAllergies(getRadiologyExamRequest().getPatient().getAllergies());
                } else {
                    setShowAllergyWarning(false);
                }

                if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequest().getRequestTags())) {
                    for (RequestTag tag : getRadiologyExamRequest().getRequestTags()) {
                        executeJS(String.format("createHtmlTag('%s')", tag.getTag()));
                    }
                }

                if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequest().getRequestAttachedFiles())) {
                    setRequestAttachedFiles(getRadiologyExamRequest().getRequestAttachedFiles());
                } else {
                    setRequestAttachedFiles(new ArrayList<RequestAttachedFile>());
                }

                for (RequestAttachedFile requestAttachedFile : getRequestAttachedFiles()) {
                    requestAttachedFile.setTempId(getTempId().incrementAndGet());
                }

                if (!ValidationHelper.isNullOrEmpty(this
                        .getRadiologyExamRequest())
                        && Boolean.TRUE.equals(this.getRadiologyExamRequest()
                        .getModifiedExamsText())) {
                    this.setDescriptionCheckbox(Boolean.TRUE);
                    this.setExamTypesText(this.getRadiologyExamRequest()
                            .getModifiedExamTypesText());
                } else {
                    this.setDescriptionCheckbox(Boolean.FALSE);
                }

                setDocumentHidden(getRadiologyExamRequest().getDocumentHidden());
                setDocumentSecure(getRadiologyExamRequest().getDocumentSecure());

                fillUserPreferences();

                try {
                    if ((getRadiologyExamRequest().getRadiologyExamRequestItems().get(0).getWaitingListRegistrationState()
                            .equals(WaitingListRegistrationStates.REPORTED)
                            || getRadiologyExamRequest().getRadiologyExamRequestItems().get(0).getWaitingListRegistrationState()
                            .equals(WaitingListRegistrationStates.SIGNED))
                            && this.getCurrentUser() != null
                            && getRadiologyExamRequest().getReferringDoctor().equals(this.getCurrentUser().getFullname())) {
                        this.setPdfGenButtonDisabled(Boolean.TRUE);
                    } else {
                        this.setPdfGenButtonDisabled(Boolean.FALSE);
                    }
                } catch (Exception e) {
                    // In case of some of
                    // "getRadiologyExamRequest().getRadiologyExamRequestItems().get(0).getReferringDoctor()"
                    // not initialized
                    LogHelper.log(log, e);
                    this.setPdfGenButtonDisabled(Boolean.FALSE);
                }

                this.setConsistInOsirixList(this.isIpConsistInOsirixList(null));

                this.setShowRedBar(Boolean.FALSE);
                this.setShowGreenBar(Boolean.FALSE);

                this.setProgressbarValue(0);

                if (!ValidationHelper
                        .isNullOrEmpty(this.getRadiologyExamRequest()
                                .getSector().getWeasisUrl())) {
                    setDisableDICOMButton(false);
                } else {
                    setDisableDICOMButton(true);
                    cleanValidation();
                    MessageHelper
                            .addGlobalMessage(
                                    FacesMessage.SEVERITY_INFO,
                                    ResourcesHelper
                                            .getValidation("documentGenerationAttention"),
                                    ResourcesHelper
                                            .getValidation("documentGenerationURLWEASISEmpty"));
                }

                fillRadiologyExamRequestsItems();
                loadNotes();
                fillColumnClass();
                if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequest())
                        && !ValidationHelper
                        .isNullOrEmpty(getRadiologyExamRequest()
                                .getRadiologyExamRequestItems())) {

                    RadiologyExamRequestItem item = getRadiologyExamRequest()
                            .getRadiologyExamRequestItems().get(0);

                    if (preferenceAutoSave.equals(Boolean.TRUE) && item.getAutoSaveDate() != null
                            && item.getReportResultAutoSaved() != null
                            && !item.getReportResultAutoSaved().equals(
                            item.getReportResult())
                            && !item.getReportResultAutoSaved().equals(
                            this.getEditorValue())
                            && (item.getReportSaveDate() == null || item
                            .getAutoSaveDate().after(
                                    item.getReportSaveDate()))) {

                        this.setAutoSavePresent(Boolean.TRUE);

                    } else {
                        this.setAutoSavePresent(Boolean.FALSE);
                    }

                    setEditorValue(item.getReportResult());

                    setCurrentDate(new Date());

                    if (!ValidationHelper.isNullOrEmpty(getAllRequestsItemsWrappers())) {
                        performItemsBlocking();
                    } else {
                        WaitingListStatusHelper.logActivityBlockRequestCanNot();
                    }

                    if (this.getConsistInOsirixList()
                            || WaitingListRegistrationStates.SIGNED
                            .equals(this.getRadiologyExamRequest()
                                    .getWaitingListRegistrationState())
                            || WaitingListRegistrationStates.REPORTED
                            .equals(this.getRadiologyExamRequest()
                                    .getWaitingListRegistrationState())
                            || !getUserEqualToClosingDoc()) {
                        this.editorDisabled = Boolean.TRUE;
                        setEnableHtmlEditorButton(Boolean.TRUE);
                    } else {
                        this.editorDisabled = Boolean.FALSE;
                        setEnableHtmlEditorButton(Boolean.FALSE);
                    }

                    setDisableButtonsAndFieldsInReportedCase(WaitingListRegistrationStates.REPORTED
                            .equals(this.getRadiologyExamRequest()
                                    .getWaitingListRegistrationState()) || WaitingListRegistrationStates.SIGNED
                            .equals(this.getRadiologyExamRequest()
                                    .getWaitingListRegistrationState()));
                    setDisableButtonsAndFieldsInSignedCase(WaitingListRegistrationStates.SIGNED
                            .equals(this.getRadiologyExamRequest()
                                    .getWaitingListRegistrationState()));
                    if (getDisableButtonsAndFieldsInReportedCase() || getDisableButtonsAndFieldsInSignedCase()) {
                        setDisableTagsButton(!getCurrentUser().getId().equals(getRadiologyExamRequest().getUserClosingReportId()));
                    } else {
                        setDisableTagsButton(false);
                    }
                } else {
                    RedirectHelper.goTo(PageTypes.WORKLIST);
                }
            } else {
                //RedirectHelper.goTo(PageTypes.LOGOUT);

                return;
            }

            initializeLayoutOptions();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        if (this.getAutoSavePresent() != null
                && this.getAutoSavePresent() && !WaitingListRegistrationStates.SIGNED
                .equals(this.getRadiologyExamRequest().getWaitingListRegistrationState())) {
            executeJS("PF('restoreDialog').show()");
        }

        Long fileEntityId = getRadiologyExamRequest().getRadiologyExamRequestItems().get(0).getFileEntityId();
        if (fileEntityId != null) {
            try {
                FileEntity fileEntity = DaoManager.get(FileEntity.class, fileEntityId);

                setPdfFileId(fileEntity.getId().toString());
                setPdfFileName(fileEntity.getName());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        setUserId(getCurrentUser().getId().toString());

        if (!isPostback()) {
            createNoteMessage();
        }
    }

    private void fillColumnClass() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            builder.append(" full_width_button,");
        }
        if (!ValidationHelper.isNullOrEmpty(getNotes())) {
            builder.append(" full_width_button,");
        }
        if (getShowAllergyWarning()) {
            builder.append(" full_width_button,");
        }
        builder.append(" full_width_input");
        setButtonsColumnClass(builder.toString());
    }

    private void fillUserPreferences() {
        try {

            UserPreference useDescriptionPref = DaoManager.get(
                    UserPreference.class,
                    new Criterion[]{
                            Restrictions.eq("user.id", this.getCurrentUser()
                                    .getId()),
                            Restrictions.eq("type",
                                    UserPreferenceType.PDF_USE_DESCRIPTION)
                    });

            if (useDescriptionPref != null
                    && useDescriptionPref.getUseDescription().equals(
                    Boolean.TRUE)) {
                this.setDescriptionCheckbox(Boolean.TRUE);
            }
        } catch (HibernateException | InstantiationException
                 | IllegalAccessException | PersistenceBeanException e) {
            log.error(
                    "Exception while getting user usedescription preferences.",
                    e);
            LogHelper.log(log, e);
        }

        try {

            UserPreference autoSavePref = DaoManager.get(
                    UserPreference.class,
                    new Criterion[]{
                            Restrictions.eq("user.id", this.getCurrentUser()
                                    .getId()),
                            Restrictions.eq("type",
                                    UserPreferenceType.AUTO_SAVE_DOCG)
                    });

            if (autoSavePref != null
                    && autoSavePref.getAutoSaveDocg().equals(
                    Boolean.TRUE)) {
                this.preferenceAutoSave = Boolean.TRUE;
            } else {
                this.preferenceAutoSave = Boolean.FALSE;
            }
        } catch (HibernateException | InstantiationException
                 | IllegalAccessException | PersistenceBeanException e) {
            log.error("Exception while getting user usedescription preferences.", e);
            LogHelper.log(log, e);
        }

        try {
            UserPreference autoSavePref = DaoManager.get(
                    UserPreference.class,
                    new Criterion[]{
                            Restrictions.eq("user.id", this.getCurrentUser().getId()),
                            Restrictions.eq("type",UserPreferenceType.VOCAL_RECOGNITION)
                    });
            if (!ValidationHelper.isNullOrEmpty(autoSavePref)
                    && Boolean.TRUE.equals(autoSavePref.getVocalRecognition())) {
                setRenderVoiceRecognitionBlock(Boolean.TRUE);
            }
        } catch (HibernateException | InstantiationException
                | IllegalAccessException | PersistenceBeanException e) {
            log.error("Exception while getting user usedescription preferences.", e);
            LogHelper.log(log, e);
        }
    }

    private void loadNotes() throws HibernateException, IllegalAccessException,
            PersistenceBeanException {

        List<Long> ids = new ArrayList<Long>();

        List<RadExamRequestItemWrapper> wrappers = this.getAllRequestsItemsWrappers();
        if (wrappers != null) {
            for (RadExamRequestItemWrapper wrap :wrappers) {
                if (!ids.contains(wrap.getRequestId())) {
                    ids.add(wrap.getRequestId());
                }
            }
        }

        if (!ids.isEmpty()) {
            this.setNotes(DaoManager.load(RequestNote.class, new Criterion[]{
                    Restrictions.in("radiologyExamRequest.id", ids)
            }));
        } else {
            this.setNotes(new ArrayList<RequestNote>());
        }
    }

    public void hideApplet() {
        this.setShowApplet(Boolean.FALSE);
    }

    public static String getIpAddr(Logger log, HttpServletRequest request,
                                   RadiologyExamRequest radiologyExamRequest2) {
        String ip = request.getHeader("X-Real-IP");

        if (null != ip && !"".equals(ip.trim())
                && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("X-Forwarded-For");

        if (null != ip && !"".equals(ip.trim())
                && !"unknown".equalsIgnoreCase(ip)) {
            // get first ip from proxy ip
            int index = ip.indexOf(',');

            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    public static String getIpAddr() {
        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();

        StringBuffer sb = new StringBuffer();
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

    private static String getOnlyIp() {
        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();

        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private void initializeLayoutOptions() {
        LayoutOptions layoutOptionsOne = new LayoutOptions();
        LayoutOptions panes = new LayoutOptions();

        panes.addOption("slidable", Boolean.TRUE);
        panes.addOption("resizable", Boolean.FALSE);

        layoutOptionsOne.setPanesOptions(panes);

        LayoutOptions west = new LayoutOptions();

        west.addOption("size", 300);
        west.addOption("initClosed", Boolean.TRUE);
        west.addOption("spacing_closed", 18);
        west.addOption("spacing_open", 18);
        west.addOption("collapsible", Boolean.TRUE);

        layoutOptionsOne.setWestOptions(west);

        LayoutOptions east = new LayoutOptions();

        east.addOption("size", 300);
        east.addOption("minSize", 300);
        east.addOption("maxSize", "50%");
        east.addOption("resizable", Boolean.TRUE);
        east.addOption("resizeWhileDragging", Boolean.TRUE);
        east.addOption("closable", Boolean.FALSE);
        east.addOption("spacing_open", 4);

        layoutOptionsOne.setEastOptions(east);

        LayoutOptions south = new LayoutOptions();

        south.addOption("size", 30);
        south.addOption("closable", Boolean.FALSE);
        south.addOption("spacing_open", 0);

        layoutOptionsOne.setSouthOptions(south);

        LayoutOptions north = new LayoutOptions();
        north.addOption("size", 95);
        north.addOption("closable", Boolean.FALSE);
        north.addOption("spacing_open", 0);

        layoutOptionsOne.setNorthOptions(north);

        LayoutOptions childCenterOptions = new LayoutOptions();
        LayoutOptions center = new LayoutOptions();
        center.addOption("maskContents", Boolean.TRUE);
        center.setChildOptions(childCenterOptions);

        LayoutOptions westNorth = new LayoutOptions();
        westNorth.addOption("slidable", Boolean.FALSE);
        westNorth.addOption("resizable", Boolean.FALSE);
        westNorth.addOption("closable", Boolean.FALSE);
        westNorth.addOption("size", "93%");
        westNorth.addOption("spacing_open", 0);

        childCenterOptions.setNorthOptions(westNorth);

        LayoutOptions westCenter = new LayoutOptions();
        westCenter.addOption("slidable", Boolean.FALSE);
        westCenter.addOption("resizable", Boolean.FALSE);
        westCenter.addOption("closable", Boolean.FALSE);
        westCenter.addOption("size", "7%");

        childCenterOptions.setCenterOptions(westCenter);

        layoutOptionsOne.setCenterOptions(center);

        setLayoutOptions(layoutOptionsOne);
    }

    public void fillRadiologyExams() {
        try {
            List<RadiologyExam> radiologyExams = new ArrayList<RadiologyExam>();
            if (this.getCurrentUser().getId() != null) {
                User user = DaoManager.get(User.class, this.getCurrentUser()
                        .getId());
                if (!ValidationHelper.isNullOrEmpty(user)) {
                    List<Diagnostic> diagnostics = user.getDiagnostics();

                    if (!ValidationHelper.isNullOrEmpty(diagnostics)) {
                        for (Diagnostic diagnostic : diagnostics) {
                            if (diagnostic != null
                                    && !ValidationHelper
                                    .isNullOrEmpty(diagnostic
                                            .getRadiologyExams())) {
                                for (DiagnosticRadiologyExam diagnosticRadiologyExam : diagnostic.getRadiologyExams()) {
                                    if (!radiologyExams.contains(diagnosticRadiologyExam.getRadiologyExam())) {
                                        radiologyExams.add(diagnosticRadiologyExam.getRadiologyExam());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ExamType examType = this.getRadiologyExamRequest().getExamType();

            List<RadiologyExam> exams = new ArrayList<RadiologyExam>();

            if (examType != null
                    && !ValidationHelper.isNullOrEmpty(radiologyExams)) {
                for (RadiologyExam rExam : radiologyExams) {
                    if (rExam.getExamType() != null
                            && examType.getId().equals(
                            rExam.getExamType().getId())) {
                        exams.add(rExam);
                    }
                }
            }

            if (!exams.isEmpty()) {
                setRadiologyExams(exams);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void unblockItemsAfterLeaving() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (this.getUnblockItems()) {
            unblockItems();
        }
    }

    public String getUnblockingHeader() {
        RadiologyExamRequest request = null;
        User user = null;
        String result = null;
        try {
            if (this.getRadiologyExamRequest() != null) {
                request = DaoManager.get(RadiologyExamRequest.class, this
                        .getRadiologyExamRequest().getId());

                if (request != null) {
                    if (!this.getUserEqualToClosingDoc()
                            && WaitingListRegistrationStates.REPORTED
                            .equals(request
                                    .getWaitingListRegistrationState())) {
                        return ResourcesHelper
                                .getString("pdfGenerationUnblockDocumentBlocked");
                    }
                    user = DaoManager.get(User.class,
                            request.getUserClosingReportId());
                    if (!ValidationHelper.isNullOrEmpty(user)) {
                        StringBuilder sb = new StringBuilder(
                                ResourcesHelper
                                        .getString("worklistReportingToTheDoctorOpen"));
                        sb.append(" ");
                        sb.append(user.getFirstName());
                        sb.append(" ");
                        sb.append(user.getLastName());
                        sb.append(" in data ");
                        sb.append(DateTimeHelper.toFormatedString(
                                request.getBlockingDate(),
                                "YYYY-MM-dd kk:mm:ss"));
                        sb.append("?");
                        result = sb.toString();
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (!this.getUserEqualToClosingDoc() && result != null) {
            return result;
        } else {
            return ResourcesHelper.getString("pdfGenerationUnblockDocument");
        }
    }

    private void fillRadiologyExamRequestsItems() {
        List<Long> radiologyExamRequestsItemsIds = SessionHelper.getIds(ID_IN_SESSION);
        if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestsItemsIds)) {
            Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair = null;
            if (getShort()) {
                pair = WaitingListStatusHelper
                        .getPairRequestsItemsShort(radiologyExamRequestsItemsIds);
            } else {
                pair = WaitingListStatusHelper
                        .getPairRequestsItems(radiologyExamRequestsItemsIds);
            }
            if (!ValidationHelper.isNullOrEmpty(pair.getFirst())
                    && !ValidationHelper.isNullOrEmpty(pair.getSecond())) {
                setAllRequestsWrappers(pair.getFirst());
                setAllRequestsItemsWrappers(pair.getSecond());
            }
        }
    }

    private void performItemsBlocking() throws HibernateException {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    if (getUserEqualToClosingDoc()) {
                        for (RadExamRequestWrapper wrapper : getAllRequestsWrappers()) {
                            RadiologyExamRequest request = DaoManager.get(
                                    RadiologyExamRequest.class,
                                    new Criterion[]{
                                            Restrictions.eq("id",
                                                    wrapper.getId())
                                    });
                            WaitingListStatusHelper.logActivityBlockRequest(request.getId());
                            request.setUserClosingReportId(UserHolder
                                    .getInstance().getCurrentUser().getId());
                            request.setBlockingDate(DateTimeHelper.getNow());
                            if (WaitingListRegistrationStates.PERFORMED.equals(request.getWaitingListRegistrationState())
                                    && request.getId().equals(getRadiologyExamRequest().getId())) {
                                request.setWaitingListRegistrationState(WaitingListRegistrationStates.IN_READING);
                            }
                            DaoManager.save(request);
                        }
                    } else {
                        throw new RadiologyExamRequestItemBlockingException();
                    }
                }

                @Override
                public void onSuccess() {
                    WaitingListStatusHelper.logActivityBlockRequestSuccess();
                }

                @Override
                public void onException(Exception e) throws Exception {
                    if (e instanceof RadiologyExamRequestItemBlockingException) {
                        try {
                            User closingUser = DaoManager.get(
                                    User.class,
                                    new Criterion[]{
                                            Restrictions
                                                    .eq("id",
                                                    radiologyExamRequest
                                                            .getUserClosingReportId())
                                    });
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_INFO,
                                            ResourcesHelper
                                                    .getString(
                                                            "worklistReportingToTheDoctorOpen")
                                                    .concat(" ")
                                                    .concat(closingUser
                                                            .getFullname())
                                                    .concat(" in data ")
                                                    .concat(DateTimeHelper
                                                            .ToDatePatternWithMinutesAndSuffix(radiologyExamRequest
                                                                    .getBlockingDate())),
                                            "");

                        } catch (Exception ex) {
                            LogHelper.log(log, ex);
                        }
                    } else {
                        LogHelper.log(log, e);
                        LogHelper.log(activityErrorLog, "User <" + getCurrentUser().getId()
                                + "> failure preform blocking of requests");
                        LogHelper.log(activityErrorLog, e);
                        WaitingListStatusHelper.logActivityBlockRequestFailure();
                    }
                }
            });

            DaoManager.getSession().refresh(getRadiologyExamRequest());

            clearSession();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void unblockItems() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        RadiologyExamRequest request = DaoManager.get(
                RadiologyExamRequest.class, this.getRadiologyExamRequest()
                        .getId());

        if (!WaitingListRegistrationStates.REPORTED.equals(request
                .getWaitingListRegistrationState())
                && !WaitingListRegistrationStates.SIGNED.equals(request
                .getWaitingListRegistrationState())) {
            try {
                TransactionExecuter.execute(new Action() {
                    @Override
                    public void execute() throws Exception {

                        List<RadiologyExamRequest> requestList = new ArrayList<RadiologyExamRequest>();
                        for (RadExamRequestWrapper wrapper : getAllRequestsWrappers()) {
                            RadiologyExamRequest request = DaoManager.get(
                                    RadiologyExamRequest.class,
                                    new Criterion[]{
                                            Restrictions.eq("id", wrapper.getId())
                                    });

                            if (request != null) {
                                if (Boolean.TRUE.equals(requestedToLeave)
                                        && Boolean.TRUE.equals(saveText)
                                        && (!(getEditorValue() != null && getEditorValue()
                                        .equals(getRadiologyExamRequest()
                                                .getRadiologyExamRequestItems()
                                                .get(0)
                                                .getReportResult())) && (!(getEditorValue()
                                        .equals("") && getRadiologyExamRequest()
                                        .getRadiologyExamRequestItems()
                                        .get(0).getReportResult() == null)))) {
                                    throw new RadiologyExamRequestItemBlockingException();
                                }
                                requestList.add(request);
                                if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())) {
                                    WaitingListStatusHelper.logActivityUnblockRequest(request.getId());
                                    request.setUserClosingReportId(null);
                                }
                                if (moveToPerformedState
                                        || WaitingListRegistrationStates.IN_READING.equals(request.getWaitingListRegistrationState())) {
                                    request.setWaitingListRegistrationState(WaitingListRegistrationStates.PERFORMED);
                                }
                                DaoManager.save(request);
                            }
                        }

                        FileEntity fileEntity = null;

                        for (RadExamRequestItemWrapper wrapper : getAllRequestsItemsWrappers()) {
                            RadiologyExamRequestItem item = DaoManager.get(
                                    RadiologyExamRequestItem.class,
                                    new Criterion[]{
                                            Restrictions.eq("id", wrapper.getId())
                                    });
                            if (item != null) {
                                item.setReportDate(null);
                                if (Boolean.FALSE.equals(saveText)) {
                                    if (getRadiologyExamRequest()
                                            .getRadiologyExamRequestItems()
                                            .get(0).getReportResult() != null
                                            && Boolean.FALSE
                                            .equals(getReleaseReport())) {
                                        item.setReportResult(getRadiologyExamRequest()
                                                .getRadiologyExamRequestItems()
                                                .get(0).getReportResult());
                                    } else {
                                        item.setReportResult(null);
                                    }
                                } else {
                                    item.setReportResult(getEditorValue());
                                }

                                RadiologyExamRequest request = null;
                                if (item.getRadiologyExamRequest() != null) {
                                    request = requestListContain(requestList, item.getRadiologyExamRequest().getId());
                                }
                                if (request == null) {
                                    request = item.getRadiologyExamRequest();
                                }

                                if (moveToPerformedState) {
                                    item.setWaitingListRegistrationState(WaitingListRegistrationStates.PERFORMED);

                                    request
                                            .setReferringDoctor(null);

                                    if (item.getFileEntity() != null) {
                                        fileEntity = item.getFileEntity();
                                        item.setFileEntity(null);
                                    }
                                }

                                item.setReportSaveDate(new Date());
                                DaoManager.save(item);
                                if (request != null && !WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())) {
                                    WaitingListStatusHelper.logActivityUnblockRequest(request.getId());
                                    request.setUserClosingReportId(null);
                                    request.setBlockingDate(null);
                                }

                                DaoManager.save(request);
                            }
                        }

                        if (fileEntity != null) {
                            List<HistoricalReport> histReport = DaoManager
                                    .load(HistoricalReport.class,
                                            new Criterion[]{
                                                    Restrictions.eq(
                                                            "fileEntity.id",
                                                            fileEntity.getId())
                                            });

                            if (!ValidationHelper.isNullOrEmpty(histReport)) {
                                for (HistoricalReport historicalReport : histReport) {
                                    historicalReport.setFileEntity(null);
                                    DaoManager.save(historicalReport);
                                }
                            }

                            if (!ValidationHelper.isNullOrEmpty(fileEntity.getPath())) {
                                FileHelper.delete(fileEntity.getPath());
                            }

                            DaoManager.remove(fileEntity);
                        }

                        if (getReleaseReport()) {
                            RadiologyExamRequest request = DaoManager.get(
                                    RadiologyExamRequest.class, getRadiologyExamRequest()
                                            .getId());
                            for (RequestTag tag : request.getRequestTags()) {
                                DaoManager.remove(tag);
                            }

                            for (RequestAttachedFile requestAttachedFile : request.getRequestAttachedFiles()) {
                                DaoManager.remove(requestAttachedFile);
                            }
                        }
                    }

                    @Override
                    public void onSuccess() {
                        WaitingListStatusHelper.logActivityUnblockRequestSuccess();
                        MessageHelper.addGlobalMessage(
                                FacesMessage.SEVERITY_INFO,
                                ResourcesHelper
                                        .getString("pdfGenerationItemsBlocked"),
                                "");
                    }

                    @Override
                    public void onException(Exception e) throws Exception {
                        MessageHelper.addGlobalMessage(
                                FacesMessage.SEVERITY_INFO,
                                ResourcesHelper
                                        .getString("pdfGenerationTransactionWasNotSuccessfull"),
                                "");

                        if (e instanceof RadiologyExamRequestItemBlockingException) {
                            throw new RadiologyExamRequestItemBlockingException();
                        } else {
                            WaitingListStatusHelper.logActivityUnblockRequestFailure();
                        }
                    }
                });

                clearSession();
            } catch (Exception e) {
                if (!(e instanceof RadiologyExamRequestItemBlockingException))
                    LogHelper.log(log, e);
            }
        }
        customCloseOsirix();
    }

    private RadiologyExamRequest requestListContain(List<RadiologyExamRequest> list, Long id) {
        for (RadiologyExamRequest request : list) {
            if (request.getId().equals(id)) {
                return request;
            }
        }
        return null;
    }

    public void fillGlossaryModule() throws HibernateException,
            IllegalAccessException, PersistenceBeanException,
            InstantiationException {
        setCurrentGlossary(new Glossary());
        setExamTypes(ComboboxHelper.fillList(ExamType.class, true));

        setResults(new ArrayList<SelectItem>());
        getResults().add(SelectItemHelper.getNotSelected());
        fillExams();
        fillGlossaries();
        examChange();
    }

    public void fillHistoryDocuments() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        log.debug("DocumentGeneration: fillHistoryDocuments");
        setRenderHistoricalReports(Boolean.TRUE);
        setHistoryDocuments(new ArrayList<DocumentGenerationHistoryWrapper>());
        if (getRadiologyExamRequest() == null) {
            log.debug("RadiologyExamRequest is null");
        }
        if (!ValidationHelper.isNullOrEmpty(this.getRadiologyExamRequest().getPatient())) {
            List<HistoricalReport> historicalReports = DaoManager.load(
                    HistoricalReport.class,
                    new Criterion[]{
                            Restrictions.eq("patientFiscalCode", this
                                    .getRadiologyExamRequest().getPatient()
                                    .getFiscalCode()),
                            Restrictions.or(Restrictions.eq("waitingListRegistrationState",
                                            WaitingListRegistrationStates.REPORTED),
                                    Restrictions.eq("waitingListRegistrationState",
                                            WaitingListRegistrationStates.SIGNED)),
                            Restrictions.isNotNull("fileEntity")
                    }, new Order[]{
                            Order.desc("performDate")
                    });

            if (!ValidationHelper.isNullOrEmpty(historicalReports)) {
                for (HistoricalReport historicalReport : historicalReports) {
                    getHistoryDocuments().add(
                            historicalReport.getDGWrapperFromEntity());
                }
            }
        }
    }

    public void fillTemplates() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(this.getRadiologyExamRequest())) {
            List<SelectItem> templates = GeneralFunctionsHelper.fillTemplates(
                    DocumentGenerationPlaces.REPORTS, this
                            .getRadiologyExamRequest().getSector().getId(), null);
            setTemplates(templates);

            if (!ValidationHelper.isNullOrEmpty(templates)) {
                setSelectedTemplateId(Long.valueOf(templates.get(0).getValue().toString()));
            }
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
        executeJS("updateAggregationsDatatables();");
    }

    @Override
    public void aggregateAction() {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    aggregationActionFromTE();
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onException(Exception e) throws Exception {
                    LogHelper.log(log, e);
                }
            });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void aggregationActionFromTE() {
        try {
            List<Long> radiologyExamRequestsItemsIds = AggregationHelper
                    .getRequestItemsIdsForDocumentGeneration(
                            this.getSelectedRequestWrapper(),
                            this.getForAggregationRequestsItemsWrappers());
            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestsItemsIds)) {
                Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair = WaitingListStatusHelper
                        .getPairRequestsItems(radiologyExamRequestsItemsIds);

                if (!ValidationHelper.isNullOrEmpty(pair.getFirst())
                        && !ValidationHelper.isNullOrEmpty(pair.getSecond())) {
                    List<Long> itemIds = new ArrayList<>();
                    for (RadExamRequestItemWrapper radExamRequestItemWrapper : pair.getSecond()) {
                        itemIds.add(radExamRequestItemWrapper.getId());
                    }
                    for (RadExamRequestItemWrapper radExamRequestItemWrapper : getAllRequestsItemsWrappers()) {
                        if (!itemIds.contains(radExamRequestItemWrapper.getId())) {
                            RadiologyExamRequestItem item = DaoManager.get(RadiologyExamRequestItem.class, radExamRequestItemWrapper.getId());
                            item.setWaitingListRegistrationState(WaitingListRegistrationStates.PERFORMED);
                            DaoManager.save(item);
                            item.getRadiologyExamRequest().setWaitingListRegistrationState(WaitingListRegistrationStates.PERFORMED);
                            DaoManager.save(item.getRadiologyExamRequest());
                        }
                    }
                    setAllRequestsWrappers(pair.getFirst());
                    setAllRequestsItemsWrappers(pair.getSecond());

                    for (RadExamRequestWrapper rerw : pair.getFirst()) {
                        RadiologyExamRequest request = DaoManager.get(
                                RadiologyExamRequest.class, rerw.getId());
                        if (ValidationHelper.isNullOrEmpty(request
                                .getUserClosingReportId())) {
                            request.setUserClosingReportId(this
                                    .getCurrentUser().getId());
                            DaoManager.save(request);
                        }
                    }

                    List<Long> requestItemIds = new ArrayList<>();

                    for (RadExamRequestItemWrapper itemWrapper : pair.getSecond()) {
                        requestItemIds.add(itemWrapper.getId());
                    }
                    SessionHelper.putIds(requestItemIds, ID_IN_SESSION);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void tryToCallAggregationDlg() {
        if (checkUserClosed()) {
            try {
                Pair<RadExamRequestWrapper, List<RadExamRequestWrapper>> pair = AggregationHelper
                        .getPairForAggregation(this.getRadiologyExamRequest(),
                                this.getCurrentUser().getId());

                if (!ValidationHelper.isNullOrEmpty(pair)) {
                    this.setForAggregationRequestsWrappers(pair.getSecond());
                    this.setSelectedRequestWrapper(pair.getFirst());
                    this.setForAggregationRequestsItemsWrappers(AggregationHelper
                            .getItemsWrappersFromRequestsWrappers(pair
                                    .getSecond()));
                } else {
                    setSelectedRequestWrapper(getRadiologyExamRequest().getRadExamRequestWrapperFromRequest());
                }

                executeJS("PF('aggregationDlgWV').show();");
                executeJS("updateAggregationsDatatables();");
                executeJS("selectAllOnAggregation();");
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void glossaryManadgment() {
        if (checkUserClosed()) {
            Map<String, Object> options = new HashMap<String, Object>();
            options.put("draggable", Boolean.FALSE);
            options.put("modal", Boolean.TRUE);
            options.put("resizable", Boolean.FALSE);
            options.put("width", 967);
            options.put("contentHeight", 450);
            options.put("contentWidth", 940);

            PrimeFaces.current().dialog().openDynamic("glossaryDialog",
                    options, null);
        }
    }

    public void fillExams() throws HibernateException, IllegalAccessException,
            PersistenceBeanException {
        setExams(new ArrayList<SelectItem>());
        getExams().add(SelectItemHelper.getNotSelected());
        List<GlossaryExam> glossaryExams = DaoManager.load(GlossaryExam.class,
                new Criterion[]{
                        Restrictions.eqOrIsNull("medicId", this.getCurrentUser().getId())
                });
        if (!ValidationHelper.isNullOrEmpty(glossaryExams)) {
            for (GlossaryExam ge : glossaryExams) {
                getExams().add(new SelectItem(ge.getExam(), ge.getExam()));
            }
        }
    }

    public void examChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        getResults().clear();
        getResults().add(SelectItemHelper.getNotSelected());
        if (!ValidationHelper.isNullOrEmpty(getSelectedExam())) {
            GlossaryExam glossaryExam = DaoManager.get(
                    GlossaryExam.class,
                    new Criterion[]{
                            Restrictions.eq("exam", this.getSelectedExam()),
                            Restrictions.eq("medicId", this.getCurrentUser()
                                    .getId())
                    });
            if (!ValidationHelper.isNullOrEmpty(glossaryExam)) {
                List<GlossaryResult> glossaryResults = glossaryExam
                        .getResults();
                if (!ValidationHelper.isNullOrEmpty(glossaryResults)) {
                    for (GlossaryResult gr : glossaryResults) {
                        getResults().add(
                                new SelectItem(gr.getValue(), gr.getValue()));
                    }
                }
            }
        }

        setSelectedResult("");
        sortGlossaries();
    }

    public void fillGlossaries() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        setAllGlossaries(DaoManager.load(Glossary.class, new Criterion[]{
                Restrictions.eq("medicId", this.getCurrentUser().getId())
        }, new Order[]{
                Order.asc("code")
        }));
        if (ValidationHelper.isNullOrEmpty(getAllGlossaries())) {
            setAllGlossaries(new ArrayList<Glossary>());
        }

        sortGlossaries();
    }

    public void sortGlossaries() {
        setGlossaries(new ArrayList<Glossary>());
        getGlossaries().addAll(getAllGlossaries());

        List<Long> idsToRemove = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(this.getSelectedExamTypeId())) {
            for (Glossary g : getGlossaries()) {
                if (!ValidationHelper.isNullOrEmpty(g.getExamType())) {
                    if (!g.getExamType().getId()
                            .equals(this.getSelectedExamTypeId())) {
                        idsToRemove.add(g.getId());
                    }
                } else {
                    idsToRemove.add(g.getId());
                }
            }
        }

        if (!ValidationHelper.isNullOrEmpty(this.getSelectedExam())) {
            for (Glossary g : getGlossaries()) {
                if (!ValidationHelper.isNullOrEmpty(g.getExam())) {
                    if (!g.getExam().equals(this.getSelectedExam())) {
                        idsToRemove.add(g.getId());
                    }
                } else {
                    idsToRemove.add(g.getId());
                }
            }
        }

        if (!ValidationHelper.isNullOrEmpty(this.getSelectedResult())) {
            for (Glossary g : getGlossaries()) {
                if (!ValidationHelper.isNullOrEmpty(g.getResult())) {
                    if (!g.getResult().equals(this.getSelectedResult())) {
                        idsToRemove.add(g.getId());
                    }
                } else {
                    idsToRemove.add(g.getId());
                }
            }
        }

        List<Glossary> tempGlossaryList = new ArrayList<>(getGlossaries());
        for (Glossary g : tempGlossaryList) {
            if (idsToRemove.contains(g.getId())) {
                getGlossaries().remove(g);
            }
        }
    }

    public void onGlossarySelect() {
        if (!ValidationHelper.isNullOrEmpty(this.getCurrentGlossary())) {
            setEditorValue(this.getEditorValue() + this.getCurrentGlossary().getText());
        }
    }

    public void openPSD() {
        if (checkUserClosed()) {
            if (!ValidationHelper.isNullOrEmpty(getRadiologyExamRequest())
                    && !ValidationHelper
                    .isNullOrEmpty(getRadiologyExamRequest()
                            .getHl7FieldsFromAsap())) {
                WaitingListStatusHelper.openPSD(getRadiologyExamRequest()
                        .getHl7FieldsFromAsap().getPsdNumber());
            }
        }
    }

    public void actionOnSaveText() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (Boolean.TRUE.equals(this.requestedToLeave)) {
            this.chooseTemplate();
        } else {
            detectPageAndToGo();
            clearSession();
        }
    }

    //TODO this function will be used on checking is this request already blocked or not
    public boolean checkForIfBlocked() {
        Criteria criteria = null;
        try {
            criteria = DaoManager.getSession().createCriteria(
                    RadiologyExamRequest.class);
        } catch (IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }

        criteria.add(Restrictions.eq("id", this.getRadiologyExamRequest()
                .getId()));

        ProjectionList proList = Projections.projectionList();

        proList.add(Projections.property("userClosingReportId"));
        proList.add(Projections.property("blockingDate"));

        criteria.setProjection(proList);

        Object requestFields = (Object) criteria.list();

        if (requestFields != null) {
            Object[] obj = (Object[]) requestFields;

            Long userClosingReportId = (Long) obj[0];
            Date blockingDate = (Date) obj[1];

            return blockingDate != null
                    || userClosingReportId != null
                    && userClosingReportId.longValue() != UserHolder.getInstance().getCurrentUser().getId().longValue();
        }

        return false;
    }

    public void actionOnUnsaveText() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        saveText = Boolean.FALSE;

        detectPageAndToGo();
    }

    private void closeReport() {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    internalButtonAction(WaitingListRegistrationStates.REPORTED);
                }

                @Override
                public void onSuccess() {

                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                            ResourcesHelper
                                    .getString("dateOperationSuccessReport"),
                            "");
                    sendHL7InNewThread();
                }

                @Override
                public void onException(Exception e) throws Exception {
                    innerOnException(e);
                }
            });

            if (isAggregation() != null) {
                clearSession();
                RedirectHelper.goTo(PageTypes.WORKLIST);
            } else {
                MessageHelper
                        .addGlobalMessage(
                                FacesMessage.SEVERITY_ERROR,
                                ResourcesHelper
                                        .getValidation("radExamItemWrongSelect"),
                                "");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        customCloseOsirix();
    }

    public void waitForResponse() {
        try {
            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;
            int minutes = Integer.parseInt(ResourcesHelper.getProperty("timeoutWaitForSignedFile"));
            LogHelper.debugInfo(log, "waitForResponse from third-part application with uuid <" + uuid + ">");
            while (ApplicationSettingsHolder.getInstance().getLocalValue(uuid) == null && elapsedTime < minutes * 60 * 1000) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    LogHelper.log(log, e);
                }
                elapsedTime = (new Date()).getTime() - startTime;
            }
            Object arg = ApplicationSettingsHolder.getInstance().getLocalValue(uuid);
            ApplicationSettingsHolder.getInstance().addToRemove(uuid);
            if (arg != null) {
                if (arg instanceof String) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper
                                    .getString("worklistWaitingSignDialog"),
                            arg.toString());
                } else if (((FileEntity) arg).getId() != null) {
                    setFromSigned(true);
                    setSignFileEntity((FileEntity) arg);
                    LogHelper.debugInfo(log, "FileEntity came from third-part application <" + ((FileEntity) arg).getName() + ">");
                    setRadiologyExamRequest(DaoManager.get(RadiologyExamRequest.class, getRadiologyExamRequest().getId()));
                    TransactionExecuter.execute(new Action() {
                        @Override
                        public void execute() throws Exception {
                            internalButtonAction(WaitingListRegistrationStates.SIGNED, false, getSignFileEntity(),
                                    getReportDate());
                        }

                        @Override
                        public void onSuccess() {
                            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                                    ResourcesHelper
                                            .getValidation("dateOperationSuccessSign"),
                                    "");

                            sendHL7InNewThread();
                            sendHL7RepoInNewThread();
                        }

                        @Override
                        public void onException(Exception e) throws Exception {
                            DaoManager.remove(FileEntity.class, getPdfFileId());
                            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                                    ResourcesHelper
                                            .getString("digitalSignError"),
                                    "");
                        }
                    });

                    if (isAggregation() != null) {
                        clearSession();
                        List<UserPreference> userPreferences = DaoManager.load(UserPreference.class, new Criterion[]{
                                Restrictions.eq("user.id", getCurrentUser().getId())
                        });
                        Boolean openFile = false;
                        for (UserPreference userPreference : userPreferences) {
                            if (UserPreferenceType.OPEN_PDF_DOCUMENT.equals(userPreference.getType())) {
                                openFile = userPreference.getOpenPdfDocument();
                            }
                        }
                        if (openFile != null && openFile) {
                            GeneralFunctionsHelper.openPdfInNewTab(getSignFileEntity());
                        }
                        RedirectHelper.goTo(PageTypes.WORKLIST);
                    } else {
                        MessageHelper
                                .addGlobalMessage(
                                        FacesMessage.SEVERITY_ERROR,
                                        ResourcesHelper
                                                .getValidation("radExamItemWrongSelect"),
                                        "");
                    }
                } else {
                    DaoManager.remove(FileEntity.class, getPdfFileId());
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper
                                    .getValidation("digitalSignCancel"),
                            "");
                }
            } else {
                DaoManager.remove(FileEntity.class, getPdfFileId());
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper
                                .getValidation("digitalSignError"),
                        "");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void signReport() {
        try {
            setUuid(UUID.randomUUID().toString());
            setReportDate(new Date());
            setUserFiscalCode(DaoManager.get(User.class, getUserId()).getFiscalCode());

            if (getUserFiscalCode() == null) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                        ResourcesHelper
                                .getString("fiscalCodeIsNull"),
                        "");
                return;
            }

            if (!WaitingListRegistrationStates.REPORTED
                    .equals(this.getRadiologyExamRequest()
                            .getWaitingListRegistrationState())) {

                List<Long> radExamsIds = new ArrayList<>();
                if (!ValidationHelper.isNullOrEmpty(this.getAllRequestsItemsWrappers())) {
                    for (RadExamRequestItemWrapper reriw : this
                            .getAllRequestsItemsWrappers()) {
                        Long id = reriw.getRadiologyExam().getId();
                        radExamsIds.add(id);
                    }
                    SessionHelper.putIds(radExamsIds, ID_IN_SESSION_FOR_TAGS);
                }

                FileEntity printEntity = GeneralFunctionsHelper.generatePdf(
                        getRadiologyExamRequest(),
                        getSelectedTemplateId(),
                        getCurrentUser(), null,
                        getEditorValue(),
                        this.getExamTypesText(), false, getReportDate());
                printEntity.setIncrementVersionOnSave(Boolean.FALSE);
                DaoManager.save(printEntity, true);
                setPdfFileId(printEntity.getId().toString());
                setPdfFileName(printEntity.getName());
                SessionHelper.removeObject(ID_IN_SESSION_FOR_TAGS);
            }
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();

            String url = RedirectHelper.createUrl(null, PageTypes.SIGN_FILE_SEND.getPagesContext().concat("?fileId=").concat(getPdfFileId())
                            .concat("&viewId=").concat(getUuid())
                            .concat("&radiologyExamRequestId=").concat(getRadiologyExamRequest().getId().toString())
                            .concat("&userId=").concat(getCurrentUser().getId().toString())
                            .concat("&customPath=1").concat(request.getContextPath().replace("/", "")).concat("&token=").concat(GetUnsignedFileServlet.TOKEN), true)
                    .concat("&userFiscalCode=").concat(getUserFiscalCode());

            executeJS("sendSignDocument('" + url + "')");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void printReport() {
        putIdsForTag();
        List<Long> radExamsIds = new ArrayList<Long>();
        if (!ValidationHelper.isNullOrEmpty(this.getAllRequestsItemsWrappers())) {
            for (RadExamRequestItemWrapper reriw : this
                    .getAllRequestsItemsWrappers()) {
                Long id = reriw.getRadiologyExam().getId();
                radExamsIds.add(id);
            }

            SessionHelper.putIds(radExamsIds, ID_IN_SESSION_FOR_TAGS);

            boolean isFromReported = WaitingListRegistrationStates.REPORTED.equals(getRadiologyExamRequest()
                    .getWaitingListRegistrationState());
            if (isFromReported) {
                GeneralFunctionsHelper.showReport(getRadiologyExamRequest(),
                        getSelectedTemplateId(), getCurrentUser(), null,
                        getEditorValue(), false, this.getExamTypesText());
            } else {
                GeneralFunctionsHelper.showReport(getRadiologyExamRequest(),
                        getSelectedTemplateId(), getCurrentUser(), null,
                        getEditorValue(), true, this
                                .getExamTypesText(), true);
            }

            SessionHelper.removeObject(ID_IN_SESSION_FOR_TAGS);
            removeIdsForTag();
        }
    }

    private void saveReport() {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    internalButtonAction(WaitingListRegistrationStates.DRAFT);
                }

                @Override
                public void onSuccess() {
                    try {
                        WaitingListStatusHelper.logActivityBlockRequest(getRadiologyExamRequest().getId());
                        getRadiologyExamRequest().setUserClosingReportId(UserHolder
                                .getInstance().getCurrentUser().getId());
                        DaoManager.save(getRadiologyExamRequest(), true);
                        WaitingListStatusHelper.logActivityBlockRequestSuccess();
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                        WaitingListStatusHelper.logActivityBlockRequestFailure();
                    }
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                            ResourcesHelper
                                    .getString("dateOperationSuccessReport"),
                            "");
                }

                @Override
                public void onException(Exception e) throws Exception {
                    innerOnException(e);
                }
            });

            if (isAggregation() != null) {
                clearSession();
            } else {
                MessageHelper
                        .addGlobalMessage(
                                FacesMessage.SEVERITY_ERROR,
                                ResourcesHelper
                                        .getValidation("radExamItemWrongSelect"),
                                "");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    // For state move
    private void internalButtonAction(WaitingListRegistrationStates state, boolean isReopen)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, NotSelectedValidationException,
            AssignValidationException {
        internalButtonAction(state, isReopen, null, null);
    }

    private void internalButtonAction(WaitingListRegistrationStates state)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, NotSelectedValidationException,
            AssignValidationException {
        internalButtonAction(state, false, null, null);
    }

    private void internalButtonAction(WaitingListRegistrationStates state, boolean isReopen, FileEntity printEntitySign, Date
            reportDateSign)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException,
            AssignValidationException, CloneNotSupportedException, NotSelectedValidationException {
        if (validateSelection()) {
            WaitingListStatusHelper wlsh = new WaitingListStatusHelper();

            List<Long> radExItemIds = getRequestsItemsIds();

            setRadiologyExamRequest(DaoManager.get(RadiologyExamRequest.class,
                    getRadiologyExamRequest().getId()));

            this.getRadiologyExamRequest().setModifiedExamTypesText(
                    this.getExamTypesText());
            this.getRadiologyExamRequest().setModifiedExamsText(
                    getDescriptionCheckbox());
            this.getRadiologyExamRequest().setDocumentHidden(getDocumentHidden());
            this.getRadiologyExamRequest().setDocumentSecure(getDocumentSecure());

            switch (state) {
                case REPORTED:
                    if (this.isAggregation() != null) {
                        List<Long> radExamsIds = new ArrayList<>();
                        if (!ValidationHelper.isNullOrEmpty(this
                                .getAllRequestsItemsWrappers())) {
                            for (RadExamRequestItemWrapper reriw : this
                                    .getAllRequestsItemsWrappers()) {
                                Long id = reriw.getRadiologyExam().getId();
                                radExamsIds.add(id);
                            }
                        }

                        SessionHelper.putIds(radExamsIds, ID_IN_SESSION_FOR_TAGS);

                        Date reportDate = new Date();

                        List<RequestAttachedFile> requestAttachedFiles = DaoManager.load(RequestAttachedFile.class, new Criterion[]{
                                Restrictions.eq("radiologyExamRequest.id", radiologyExamRequest.getId())
                        });

                        FileEntity printEntity = GeneralFunctionsHelper.generatePdf(
                                getRadiologyExamRequest(),
                                getSelectedTemplateId(),
                                getCurrentUser(), null,
                                getEditorValue(),
                                this.getExamTypesText(), false, reportDate, requestAttachedFiles);
                        DaoManager.save(printEntity);

                        for (RadiologyExamRequestItem item : getRadiologyExamRequest().getRadiologyExamRequestItems()) {
                            item.setSignFileEntity(null);
                            DaoManager.save(item);
                        }

                        this.getSession().put("appletPrintOnStart", printEntity.getId());

                        wlsh.toReportedState(this.getRadiologyExamRequest(),
                                radExItemIds, getEditorValue(),
                                printEntity, this
                                        .isAggregation(), reportDate);

                /*        List<HistoricalReport>  hreports = ConnectionManager.load(HistoricalReport.class,
                                new Criterion[]{
                                        Restrictions.eq("radiologyExamRequest", getRadiologyExamRequest())
                                }, DaoManager.getSession());
*/

                        HistoricalReport historicalReport = new HistoricalReport();
                        historicalReport =
                                new HistoricalReportHelper().getSingleHistoricalReportFromRequest(getRadiologyExamRequest(),
                                        historicalReport, getRadiologyExamRequest().getRadiologyExamRequestItems());
                        try {
                            if (ApplicationSettingsHolder.getInstance()
                                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                                    .getValue() != null && Boolean.parseBoolean(ApplicationSettingsHolder.getInstance()
                                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                                    .getValue())) {
                                log.info("Embedding CDA2 XML : " + printEntity.getPath() + "("
                                        + getRadiologyExamRequest().getId() + ")");
                                Path pdfPath = Paths.get(printEntity.getPath());
                                byte[] data = Files.readAllBytes(pdfPath);
                                String fileNameWithoutExt = FilenameUtils.removeExtension(printEntity.getName());
                                String xmlFileName = FileHelper.getRandomFileName("1.xml");
                                if (historicalReport != null) {
                                    HistoricalReportXMLWrapper historicalReportWrapper = new HistoricalReportXMLWrapper();
                                    historicalReportWrapper.setRadiologyExamRequestId(getRadiologyExamRequest().getId());
                                    historicalReportWrapper.lazyLoadEntities();
                                    historicalReportWrapper.setExamCode(historicalReport.getPatientFiscalCode());
                                    historicalReportWrapper.setPatientFiscalCode(historicalReport.getPatientFiscalCode());
                                    historicalReportWrapper.setPatientName(historicalReport.getPatientName());
                                    historicalReportWrapper.setPatientSurname(historicalReport.getPatientSurname());
                                    historicalReportWrapper.setPatientBirthDate(historicalReport.getPatientBirthDate());
                                    historicalReportWrapper.setFileEntity(printEntity);
                                    historicalReportWrapper.setDocumentHidden(getRadiologyExamRequest().getDocumentHidden());
                                    historicalReportWrapper.setDocumentSecure(getRadiologyExamRequest().getDocumentSecure());
                                    historicalReportWrapper.setElectronicRecipeNumber(getRadiologyExamRequest().getElectronicRecipeNumber());
                                    if (historicalReportWrapper.getFileEntity() != null && printEntity.getVersion() != null)
                                        historicalReportWrapper.getFileEntity().setVersion(printEntity.getVersion() + 1);
                                    if (!ValidationHelper.isNullOrEmpty(historicalReport.getFileEntity()))
                                        historicalReportWrapper.setFileEntityId(historicalReport.getFileEntity().getId());
                                    historicalReportWrapper.setReportResultWithoutTags(historicalReport.getReportResultWithoutTags());
                                    String xml = XmlHelper.createCdaXml(historicalReportWrapper);
                                    log.info("XML: " + xml);
                                    xml = XmlHelper.escapeHtmlEntities(xml, DaoManager.getSession());
                                    log.info("XML Replaced: " + xml);
                                    byte[] xmlData = xml.getBytes(StandardCharsets.ISO_8859_1);
                                    byte[] output = inject(data, xmlData, xmlFileName);
                                    Path embeddedPdfPath = Paths.get(pdfPath.getParent().toString(),
                                            fileNameWithoutExt.concat("_tmp.pdf"));
                                    log.info("embeddedPdfPath : " + embeddedPdfPath);
                                    FileHelper.saveToFile(output, embeddedPdfPath);
                                    try {
                                        Files.delete(pdfPath);
                                    } catch (Exception e) {
                                        log.error("Error in deleting pdf");
                                    }
                                    Files.move(embeddedPdfPath, embeddedPdfPath.getParent().resolve(pdfPath.getFileName()),
                                            StandardCopyOption.REPLACE_EXISTING);

                                    if (isValidatePdfFile()) {
                                        FSEService service = new FSEService();
                                        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                                                ResourcesHelper.getString("FSEValidationInProgress"), "");
                                        service.createValidationRequest(printEntity.getPath(),
                                                historicalReport.getPatientFiscalCode(),
                                                getRadiologyExamRequest().getId(),
                                                printEntity.getId());
                                    }

                                  /*  ByteArrayInputStream fakeFile = new ByteArrayInputStream(xmlData);

                                    PDEmbeddedFile ef = new PDEmbeddedFile(doc, fakeFile);
                                    ef.setSubtype("application/xml");
                                    ef.setSize(data.length);
                                    ef.setCreationDate(Calendar.getInstance());
                                    fs.setEmbeddedFile(ef);
                                    PDEmbeddedFilesNameTreeNode treeNode = new PDEmbeddedFilesNameTreeNode();
                                    treeNode.setNames(Collections.singletonMap("CDA2 attachment", fs));

                                    List<PDEmbeddedFilesNameTreeNode> kids = new ArrayList<>();
                                    kids.add(treeNode);
                                    efTree.setKids(kids);

                                    PDDocumentNameDictionary names = new PDDocumentNameDictionary(doc.getDocumentCatalog());
                                    names.setEmbeddedFiles(efTree);
                                    doc.getDocumentCatalog().setNames(names);
                                    Path embeddedPdfPath = Paths.get(pdfPath.getParent().toString(),
                                            fileNameWithoutExt.concat("_tmp.pdf"));
                                    log.info("embeddedPdfPath : " + embeddedPdfPath);
                                    doc.save(embeddedPdfPath.toString());
                                    doc.close();
                                    try {
                                        Files.delete(pdfPath);
                                    } catch (Exception e) {
                                        log.error("Error in deleting pdf");
                                    }
                                    Files.move(embeddedPdfPath, embeddedPdfPath.getParent().resolve(pdfPath.getFileName()),
                                            StandardCopyOption.REPLACE_EXISTING);*/

                                } else {
                                    log.error("No historical report find");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("Error in embedding cda2 xml");
                            LogHelper.log(log, e);
                        }

                        SessionHelper.removeObject(ID_IN_SESSION_FOR_TAGS);
                    }
                    break;
                case SIGNED:
                    if (this.isAggregation() != null) {
                        for (RadiologyExamRequestItem item : getRadiologyExamRequest().getRadiologyExamRequestItems()) {
                            item.setSignFileEntity(null);
                            DaoManager.save(item);
                        }
                        wlsh.toSignState(this.getRadiologyExamRequest(),
                                radExItemIds, getEditorValue(),
                                printEntitySign, this
                                        .isAggregation(), reportDateSign);
                    }
                    break;
                case DRAFT:
                    if (this.isAggregation() != null) {
                        if (!isReopen) {
                            List<RadiologyExamRequestItem> list = DaoManager.load(
                                    RadiologyExamRequestItem.class, new Criterion[]{
                                            Restrictions.in("id", radExItemIds)
                                    });
                            for (RadiologyExamRequestItem item : list) {
                                if (!item.getRadiologyExamRequest().getId().equals(getRadiologyExamRequest().getId())) {
                                    item.setAggregationRequestId(getRadiologyExamRequest().getId());
                                    DaoManager.save(item);
                                }
                            }
                            wlsh.toDraftState(this.getRadiologyExamRequest(),
                                    radExItemIds, getEditorValue(),
                                    null, this
                                            .isAggregation());
                        } else {
                            wlsh.performReopening(this.getRadiologyExamRequest(),
                                    radExItemIds, getEditorValue(),
                                    null, this
                                            .isAggregation());
                        }
                    }
                default:
                    break;
            }
        } else {
            throw new NotSelectedValidationException();
        }
    }

    public boolean isValidatePdfFile() {
        return (ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.ENABLE_VALIDATION_CDA2)
                .getValue() != null && Boolean.parseBoolean(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.ENABLE_VALIDATION_CDA2)
                .getValue()) && Boolean.TRUE.equals(getRadiologyExamRequest().getForwarded()));
    }

    public byte[] inject(byte[] bytePDF, byte[] byteCDA, String fileName) throws IOException {
        final String mimeType = "application/xml";

        final PDDocument document = PDDocument.load(new ByteArrayInputStream(bytePDF));

        final Map<String, PDComplexFileSpecification> embeddedFileMap = new HashMap<>();

        final PDEmbeddedFile embeddedFile = new PDEmbeddedFile(document, new ByteArrayInputStream(byteCDA));
        embeddedFile.setSubtype(mimeType);
        embeddedFile.setSize(byteCDA.length);

        final PDComplexFileSpecification fileSpecification = new PDComplexFileSpecification();
        fileSpecification.setFile(fileName);
        fileSpecification.setEmbeddedFile(embeddedFile);

        embeddedFileMap.put(fileName, fileSpecification);

        final PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();
        efTree.setNames(embeddedFileMap);

        final PDDocumentNameDictionary names = new PDDocumentNameDictionary(document.getDocumentCatalog());
        names.setEmbeddedFiles(efTree);
        document.getDocumentCatalog().setNames(names);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();
        return baos.toByteArray();
    }

    private void sendHL7InNewThread() {
        ThreadExecutor.execute(new Action() {
            @Override
            public void execute() throws Exception {
                TransactionExecuter.execute(
                        new SinglePersistenceSessionAction() {
                            @Override
                            public void execute() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
                                sendMsgsForDocument(getSession());
                            }
                        });
            }
        });
    }

    private void sendHL7RepoInNewThread() {
        ThreadExecutor.execute(new Action() {
            @Override
            public void execute() throws Exception {
                TransactionExecuter.execute(
                        new SinglePersistenceSessionAction() {
                            @Override
                            public void execute() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
                                sendRepoMsgsForDocument(getSession());
                            }
                        });
            }
        });
    }

    public void reOpenReport() {
        log.debug("DocumentGeneration: reOpenReport");
        if (checkUserClosed()) {

            WaitingListStatusHelper.logActivity(
                    UserActivityLogStates.RIAPRI_REFERTO,
                    getRadiologyExamRequest().getId());
            try {
                TransactionExecuter.execute(new Action() {
                    @Override
                    public void execute() throws Exception {
                        reOpenReportAction();
                    }

                    @Override
                    public void onSuccess() {
                        MessageHelper.addGlobalMessage(
                                FacesMessage.SEVERITY_INFO,
                                ResourcesHelper
                                        .getString("dateOperationSuccessReport"),
                                "");
                    }

                    @Override
                    public void onException(Exception e) throws Exception {
                        innerOnException(e);
                        log.debug("Exception during reopen action");
                    }
                });

                if (isAggregation() != null) {
                    clearSession();
                } else {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper
                                    .getValidation("radExamItemWrongSelect"),
                            "");
                }

                List<Long> itemIds = AggregationHelper
                        .getRadExamItemIdsWithSameFileEntity(this
                                .getRadiologyExamRequest());

                log.debug("itemIds size: ".concat(String.valueOf(itemIds.size())));
                redirectToDocumentGeneration(itemIds);
                this.setPdfGenButtonDisabled(Boolean.FALSE);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            log.debug("userClosingReportId changed, do nothing");
        }
    }

    private void redirectToDocumentGeneration(List<Long> itemIds) {
        try {
            if (!ValidationHelper.isNullOrEmpty(itemIds)) {
                SessionHelper.putIds(itemIds, ID_IN_SESSION);

                if (getRadiologyExamRequest() != null) {
                    log.debug("Redirecting to the page with id: ".concat(String.valueOf(
                            Objects.requireNonNullElse(getRadiologyExamRequest().getId(), 0L)
                    )));
                } else {
                    log.debug("Redirecting to the page, but RadiologyExamRequest is null");
                }
                RedirectHelper.goTo(PageTypes.DOCUMENT_GENERATION, this
                        .getRadiologyExamRequest().getId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void reOpenReportAction() {
        try {
            internalButtonAction(WaitingListRegistrationStates.DRAFT, true);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private List<Long> getRequestsItemsIds() {
        List<Long> result = null;

        if (!ValidationHelper.isNullOrEmpty(this.getAllRequestsItemsWrappers())) {
            result = new ArrayList<Long>();

            for (RadExamRequestItemWrapper requestItemWrapper : this
                    .getAllRequestsItemsWrappers()) {
                result.add(requestItemWrapper.getId());
            }
        }

        return result;
    }

    private List<Long> getRequestsIds() {
        List<Long> result = null;

        if (!ValidationHelper.isNullOrEmpty(this.getAllRequestsItemsWrappers())) {
            result = new ArrayList<Long>();

            for (RadExamRequestItemWrapper requestItemWrapper : this
                    .getAllRequestsItemsWrappers()) {
                Long requestId = requestItemWrapper.getRequestId();
                if (!result.contains(requestId)) {
                    result.add(requestId);
                }
            }
        }

        return result;
    }

    private Boolean isAggregation() {
        List<Long> selReqIds = this.getRequestsIds();
        if (!ValidationHelper.isNullOrEmpty(selReqIds)) {
            if (selReqIds.contains(this.getRadiologyExamRequest().getId())) {
                if (selReqIds.size() == 1) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            }
        }

        return null;
    }

    private boolean validateSelection() {
        if (this.getRadiologyExamRequest() != null
                && !ValidationHelper.isNullOrEmpty(this
                .getRadiologyExamRequest()
                .getRadiologyExamRequestItems())) {
            if (this.getRadiologyExamRequest().isExpanded()) {
                for (RadiologyExamRequestItem wlr : this
                        .getRadiologyExamRequest()
                        .getRadiologyExamRequestItems()) {
                    if (wlr.getSelected()) {
                        return true;
                    }
                }
            } else {
                for (RadiologyExamRequestItem wlr : this
                        .getRadiologyExamRequest()
                        .getRadiologyExamRequestItems()) {
                    wlr.setSelected(Boolean.TRUE);
                }
                return true;
            }
        }

        return false;
    }

    private void innerOnException(Exception e) {
        if (e instanceof NotSelectedValidationException) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("selectionEmpty"),
                    ResourcesHelper.getValidation("selectAtLeastOneItem"));
        } else if (e instanceof AssignValidationException) {
            showAssignErrorMsg();
        } else {
            LogHelper.log(log, e);
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("dateOperationFailed"), "");
        }
    }

    private void showAssignErrorMsg() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                ResourcesHelper.getValidation("assignFailed"),
                ResourcesHelper.getValidation("assignCreateEventCalendar"));
    }

    // End state move module
    // For pdf generation

    public void leavePage() {
        if (checkUserClosed()) {

            this.setTempLinkToRedirect(this.getLinkToRedirect());

            try {
                if (this.getUserEqualToClosingDoc()) {
                    if (WaitingListRegistrationStates.REPORTED.equals(this
                            .getRadiologyExamRequest()
                            .getWaitingListRegistrationState())) {
                        this.detectPageAndToGo();
                    } else {
                        if (ValidationHelper.isNullOrEmpty(this.templates)) {
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_WARN,
                                            ResourcesHelper
                                                    .getValidation("warning"),
                                            ResourcesHelper
                                                    .getValidation("noDocumentTemplates"));
                        } else {
                            if (getEditorValue() != null
                                    && getEditorValue()
                                    .equals(getRadiologyExamRequest()
                                            .getRadiologyExamRequestItems()
                                            .get(0).getReportResult())
                                    || ("".equals(getEditorValue()) && getRadiologyExamRequest()
                                    .getRadiologyExamRequestItems()
                                    .get(0).getReportResult() == null)) {
                                actionOnUnsaveText();
                            } else {
                                executeJS("PF('savingReportTextConfirmation').show();");
                            }
                        }
                    }
                } else {
                    this.detectPageAndToGo();
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void chooseTemplate() {
        boolean checkUser = checkUserClosed();
        if ("REPORTED".equals(this.getCurrentStateToGeneratePdf())) {
            try {
                String userClosingReportId = DaoManager.getField(getShort() ? RadiologyExamRequestShort.class : RadiologyExamRequest.class,
                        "userClosingReportId", new Criterion[]{
                                Restrictions.eq("id", getRadiologyExamRequest().getId())
                        }, null);
                if (ValidationHelper.isNullOrEmpty(userClosingReportId)
                        || "null".equalsIgnoreCase(userClosingReportId)) {
                    LogHelper.log(activityErrorLog, "Request <" + getRadiologyExamRequest().getId()
                            + "> has user_closing_report_id is null; action performed by User <" + getCurrentUser().getId() + ">");
                    Session session = DaoManager.getSession();
                    Transaction tx = session.beginTransaction();

                    String hqlUpdate = "update RadiologyExamRequest r set r.userClosingReportId = :newUserClosingReportId where r.id = :oldId";
                    session.createQuery(hqlUpdate)
                            .setLong("newUserClosingReportId", getCurrentUser().getId())
                            .setLong("oldId", getRadiologyExamRequest().getId())
                            .executeUpdate();
                    tx.commit();
                }
                if (!checkUser) {
                    checkUser = true;
                    String userClosingReportDbId = DaoManager.getField(getShort() ? RadiologyExamRequestShort.class : RadiologyExamRequest.class,
                            "userClosingReportId", new Criterion[]{
                                    Restrictions.eq("id", getRadiologyExamRequest().getId())
                            }, null);
                    LogHelper.log(activityErrorLog, "Request <" + getRadiologyExamRequest().getId()
                            + "> has user_closing_report_id is <" + userClosingReportDbId + ">; action performed by User <" + getCurrentUser().getId() + ">");
                    getRadiologyExamRequest().setUserClosingReportId(getCurrentUser().getId());
                    DaoManager.save(getRadiologyExamRequest(), true);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        if (checkUser) {
            try {
                if (WaitingListRegistrationStates.SIGNED.equals(getRadiologyExamRequest().getWaitingListRegistrationState())
                        && !ValidationHelper.isNullOrEmpty(getRadiologyExamRequest().getRadiologyExamRequestItems())) {
                    Session session = DaoManager.getSession();
                    session.update(getRadiologyExamRequest().getRadiologyExamRequestItems().get(0));

                    FileEntity signedFile = getRadiologyExamRequest().getRadiologyExamRequestItems().get(0).getFileEntity();
                    preparePrintFile(signedFile);

                    RedirectHelper.sendRedirect("/File/" + signedFile.getName() + "?pfdrid_c=true", true);
                } else if (this.getUserEqualToClosingDoc()
                        && !WaitingListRegistrationStates.REPORTED.equals(this
                        .getRadiologyExamRequest()
                        .getWaitingListRegistrationState())
                        || "SIGN".equals(this.getCurrentStateToGeneratePdf())) {
                    if (!ValidationHelper.isNullOrEmpty(this.getEditorValue())) {
                        if (!ValidationHelper
                                .isNullOrEmpty(this.getTemplates())) {
                            if (this.getTemplates().size() == 1) {
                                this.dialogTemplateActionGeneral();
                            } else {
                                executeJS("PF('templates').show();");
                            }
                        } else {
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_WARN,
                                            ResourcesHelper
                                                    .getValidation("warning"),
                                            ResourcesHelper
                                                    .getValidation("noDocumentTemplates"));
                        }
                    } else {
                        this.cleanValidation();
                        MessageHelper
                                .addGlobalMessage(
                                        FacesMessage.SEVERITY_ERROR,
                                        ResourcesHelper
                                                .getValidation("validationFailed"),
                                        ResourcesHelper
                                                .getValidation("documentGenerationReportEditor"));
                    }
                } else {
                    if (WaitingListRegistrationStates.REPORTED.equals(this
                            .getRadiologyExamRequest()
                            .getWaitingListRegistrationState())) {
                        if (this.getRadiologyExamRequest() != null
                                && !ValidationHelper.isNullOrEmpty(this
                                .getRadiologyExamRequest()
                                .getRadiologyExamRequestItems())
                                && this.getRadiologyExamRequest()
                                .getRadiologyExamRequestItems().get(0)
                                .getFileEntity() != null
                                && Boolean.TRUE
                                .equals(this
                                        .getDisableButtonsAndFieldsInReportedCase())) {

                            Long fileEntityId = this
                                    .getRadiologyExamRequest()
                                    .getRadiologyExamRequestItems().get(0)
                                    .getSignFileEntityId();
                            if (fileEntityId == null) {
                                fileEntityId = this
                                        .getRadiologyExamRequest()
                                        .getRadiologyExamRequestItems().get(0)
                                        .getFileEntityId();
                            }

                            if (fileEntityId != null) {
                                FileEntity fileEntity = DaoManager.get(FileEntity.class, fileEntityId);
                                preparePrintFile(fileEntity);

                                RedirectHelper.sendRedirect(
                                        "/File/" + fileEntity.getName()
                                                + "?pfdrid_c=true", true);
                            }
                        } else {
                            printReport();
                        }
                    } else {
                        MessageHelper
                                .addGlobalMessage(
                                        FacesMessage.SEVERITY_ERROR,
                                        ResourcesHelper
                                                .getValidation("validationFailed"),
                                        ResourcesHelper
                                                .getValidation("documentIsUnderBlockByAnotherUser"));
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    private void preparePrintFile(FileEntity signedFile) throws IOException {
        if (signedFile.getContent() != null) {
            FileHelper.writeFileToFolder(signedFile
                            .getName(),
                    new File(FileHelper.getLocalFileDir()),
                    signedFile.getContent());
        }
        // New logic for entities stored on hard drive
        else if (signedFile.getPath() != null) {
            Path path = Paths.get(signedFile.getPath());

            if (Files.exists(path)
                    && !Files.isDirectory(path)) {
                FileHelper.writeFileToFolder(signedFile.getName(), new File(FileHelper.getLocalFileDir()),
                        Files.readAllBytes(Paths.get(signedFile.getPath())));
            }
        }
    }

    public boolean getUserEqualToClosingDoc() {
        if (this.getRadiologyExamRequest() != null
                && ValidationHelper.isNullOrEmpty(this
                .getRadiologyExamRequest().getUserClosingReportId())
                || this.getCurrentUser() != null
                && this.getRadiologyExamRequest() != null
                && this.getCurrentUser()
                .getId()
                .equals(this.getRadiologyExamRequest()
                        .getUserClosingReportId())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean needToSaveText() {
        try {
            RadiologyExamRequest request = DaoManager.get(
                    RadiologyExamRequest.class, this.getRadiologyExamRequest()
                            .getId());
            if (ValidationHelper.isNullOrEmpty(request
                    .getRadiologyExamRequestItems().get(0).getReportResult())
                    || request.getRadiologyExamRequestItems().get(0)
                    .getReportResult().equals(this.getEditorValue())
                    || !WaitingListRegistrationStates.REPORTED.equals(request
                    .getWaitingListRegistrationState())) {
                return true;
            }
            return false;
        } catch (HibernateException | InstantiationException
                 | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
            return true;
        }
    }

    public void detectPageAndToGo() {
        try {
            unblockItems();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (this.getTempLinkToRedirect() == null) {
            RedirectHelper.goTo(PageTypes.WORKLIST);
            return;
        }

        for (PageTypes type : PageTypes.values()) {
            if (type.getCode().equals(this.getTempLinkToRedirect())) {
                if (PageTypes.STATISTIC.equals(type)) {

                    executeJS(String.format(
                            "window.open('%s', '_newtab')", UserHolder.getInstance()
                                    .getCurrentUser()
                                    .getStatisticUrl()));


                } else {
                    RedirectHelper.goTo(type);
                }
                break;
            }
        }
    }

    public void unblock() {
        WaitingListStatusHelper.logActivity(UserActivityLogStates.RILASCIA,
                this.getRadiologyExamRequest().getId());

        try {
            unblockItems();
        } catch (HibernateException | InstantiationException
                 | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        RedirectHelper.goTo(PageTypes.WORKLIST);
    }

    public void chooseDocumentUnblocking() throws HibernateException,
            PersistenceBeanException, IOException, InstantiationException,
            IllegalAccessException {
        if (checkUserClosed()) {

            moveToPerformedState = Boolean.TRUE;
            if (!this.getUserEqualToClosingDoc()) {
                executeJS("PF('unblockingDocument').show();");
            } else {
                if (this.needToSaveText()) {
                    if (!ValidationHelper.isNullOrEmpty(this.getEditorValue())) {
                        executeJS("PF('savingReportTextConfirmation').show();");
                    } else {
                        unblockItems();
                        RedirectHelper.goTo(PageTypes.WORKLIST);
                    }
                }
            }
        }
    }

    public void dialogTemplateActionGeneral() {
        putIdsForTag();
        this.setUnblockItems(false);
        if (this.getRequestedToLeave()) {
            saveReport();
            this.detectPageAndToGo();
        } else {
            try {
                this.switchState();
            } catch (HibernateException | PersistenceBeanException | IOException e) {
                LogHelper.log(log, e);
            }
        }
        removeIdsForTag();
    }

    private void removeIdsForTag() {
        SessionHelper.removeObject(ID_IN_SESSION_FOR_PRINT);
    }

    private void putIdsForTag() {
        List<Long> ids = SessionHelper.getIds(ID_IN_SESSION);
        SessionHelper.putIds(ids, ID_IN_SESSION_FOR_PRINT);
    }

    private void switchState() throws HibernateException,
            PersistenceBeanException, IOException {
        if (!ValidationHelper.isNullOrEmpty(getCurrentStateToGeneratePdf())) {
            switch (getCurrentStateToGeneratePdf()) {
                case "REPORTED":
                    writeSessionAttr();
                    WaitingListStatusHelper.logActivity(
                            UserActivityLogStates.CHIUDI,
                            getRadiologyExamRequest().getId());
                    closeReport();
                    break;
                case "SIGN":
                    WaitingListStatusHelper.logActivity(
                            UserActivityLogStates.FIRMA,
                            getRadiologyExamRequest().getId());
                    signReport();
                    break;
                case "DRAFT":
                    WaitingListStatusHelper.logActivity(
                            UserActivityLogStates.SALVA,
                            getRadiologyExamRequest().getId());
                    saveReport();
                    break;
                case "PRINT":
                    printReport();
                    break;
                default:
                    break;
            }
        }
    }

    // End pdf generation module

    private void sendRepoMsgsForDocument(Session session) throws HibernateException {
        if (!ValidationHelper.isNullOrEmpty(this.getAllRequestsWrappers())) {
            for (RadExamRequestWrapper requestWrapper : this.getAllRequestsWrappers()) {
                if (!ValidationHelper.isNullOrEmpty(requestWrapper.getForwarded())
                        && requestWrapper.getForwarded()) {
                    new HL7RepoHelper().sendRepoMsgsForDocument(requestWrapper, getSignFileEntity(), session);
                }
            }
        }
    }

    private void sendMsgsForDocument(Session session) throws HibernateException,
            IllegalAccessException, PersistenceBeanException,
            InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(this.getAllRequestsWrappers())) {
            for (RadExamRequestWrapper requestWrapper : this.getAllRequestsWrappers()) {
                if (!ValidationHelper.isNullOrEmpty(requestWrapper.getAsapPlaceOrderNumber())) {
                    //sendMsgForDocument(requestWrapper, session);
                    new HL7RepoHelper().sendMsgForDocument(requestWrapper, getSignFileEntity(), session);
                }
            }
        }
    }

    private void sendMsgForDocument(RadExamRequestWrapper requestWrapper, Session session)
            throws HibernateException, IllegalAccessException,
            PersistenceBeanException, InstantiationException {
        List<Long> requestItemsIds = getSelectedRequestsItemsIdsFromRequestWrapper(requestWrapper);
        if (!ValidationHelper.isNullOrEmpty(requestItemsIds)) {
            MDM_T02 msg = new MDM_T02();
            msg.getParser().getParserConfiguration().setValidating(false);
            if (requestWrapper.getState().equals(
                    WaitingListRegistrationStates.DRAFT)) {
                msg = MDMHelper.getFilledMessageFromIds(msg, requestItemsIds,
                        "T10", false, session, null, false);
            } else {
                msg = MDMHelper.getFilledMessageFromIds(msg, requestItemsIds,
                        "T02", false, session, getSignFileEntity(), false);
            }
            if (msg != null) {
                Message response = null;
                try {
                    ApplicationSettingsValueWrapper ipW = ApplicationSettingsHolder
                            .getInstance().getByKey(
                                    ApplicationSettingsKeys.HL7_SEND_IP);
                    ApplicationSettingsValueWrapper portW = ApplicationSettingsHolder
                            .getInstance().getByKey(
                                    ApplicationSettingsKeys.HL7_SEND_PORT);
                    ApplicationSettingsValueWrapper timeoutW = ApplicationSettingsHolder
                            .getInstance()
                            .getByKey(
                                    ApplicationSettingsKeys.HL7_MDM_SEND_RETRY_TIME);
                    if (!ValidationHelper.isNullOrEmpty(ipW.getValue())
                            && !ValidationHelper
                            .isNullOrEmpty(portW.getValue())
                            && !ValidationHelper.isNullOrEmpty(timeoutW
                            .getValue())) {
                        String ip = ipW.getValue();
                        Integer port = Integer.valueOf(portW.getValue());
                        Integer timeout = Integer.valueOf(timeoutW.getValue());

                        response = MDMHelper
                                .sendMessage(msg, ip, port, timeout);
                    }
                    if (response != null
                            && BaseHl7MessageHelper.OK_RESPONSE
                            .equals(((ACK) response).getMSA()
                                    .getAcknowledgmentCode().getValue())) {
                        RadiologyExamRequest request = ConnectionManager.get(
                                RadiologyExamRequest.class,
                                requestWrapper.getId(), session);
                        request.setSendingStatus(Hl7RequestSendingStatus.SENT);
                        ConnectionManager.save(request, session);
                    }
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                }
            }
        }
    }

    private void writeSessionAttr() {
        this.getSession().put("fromDocumentGeneration", Boolean.TRUE);
        this.getSession().put("radExamReqId",
                this.getRadiologyExamRequest().getId());
    }

    public void viewExitingReport() {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedHistoryId())) {
            try {
                if (!ValidationHelper.isNullOrEmpty(this.getHistoryDocuments())) {
                    DocumentGenerationHistoryWrapper dHistoryWrapper = null;
                    for (DocumentGenerationHistoryWrapper dghw : this
                            .getHistoryDocuments()) {
                        if (dghw.getId().equals(this.getSelectedHistoryId())) {
                            dHistoryWrapper = dghw;
                            break;
                        }
                    }

                    if (dHistoryWrapper != null
                            && dHistoryWrapper.getFile() != null) {

                        FileEntity fileEntity = dHistoryWrapper.getFile();

                        preparePrintFile(fileEntity);

                        RedirectHelper.sendRedirect("/File/"
                                + dHistoryWrapper.getFile().getName()
                                + "?pfdrid_c=true", true);
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
                GeneralFunctionsHelper.showPdfNotGeneratedWarnMsg();
            }
        }
    }

    public void insertGlossaryFromDialog() {
        this.cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(this.getCodeOnDialog())) {
            Glossary glossary = null;
            try {
                glossary = DaoManager
                        .get(Glossary.class,
                                new Criterion[]{
                                        Restrictions.eq("code",
                                                this.getCodeOnDialog()),
                                        Restrictions.eq("medicId", this
                                                .getCurrentUser().getId())
                                });
            } catch (Exception e) {
                LogHelper.log(log, e);
                this.addFieldExeption("form:glossaryCodeOnDialog",
                        "glossaryFromDBIsAnavailable");
            }
            if (!ValidationHelper.isNullOrEmpty(glossary)) {
                this.setCurrentGlossary(glossary);
                executeJS("PF('insertGlossary').hide()");
            } else {
                this.addFieldExeption("form:glossaryCodeOnDialog",
                        "noSuchGlossaryCode");
            }
            this.setCodeOnDialog(null);
        } else {
            this.addRequiredFieldExeption("form:glossaryCodeOnDialog");
        }
    }

    public void goToWorklist() {
        RedirectHelper.goTo(PageTypes.WORKLIST);
    }

    private List<Long> getSelectedRequestsItemsIdsFromRequestWrapper(
            RadExamRequestWrapper radExamRequestWrapper) {
        List<Long> requestsItemsIds = null;

        if (!ValidationHelper.isNullOrEmpty(radExamRequestWrapper)
                && !ValidationHelper.isNullOrEmpty(radExamRequestWrapper
                .getRadExamRequestItemWrappers())) {
            requestsItemsIds = new ArrayList<Long>();
            for (RadExamRequestItemWrapper reriw : radExamRequestWrapper
                    .getRadExamRequestItemWrappers()) {
                if (reriw.getSelected()) {
                    requestsItemsIds.add(reriw.getId());
                }
            }
        }

        return requestsItemsIds;
    }

    private Boolean isIpConsistInOsirixList(
            HistoricalReport selectedHistoricalReport)
            throws HibernateException, IllegalAccessException,
            PersistenceBeanException {
        List<Osirix> osirixList = null;

        if (selectedHistoricalReport == null) {
            osirixList = DaoManager.load(Osirix.class);
        } else {
            osirixList = OsirixHelper
                    .loadOsiricListBySector(selectedHistoricalReport);
        }

        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();

        String userIp = getIpAddr(log, request, this.getRadiologyExamRequest());

        if ("0:0:0:0:0:0:0:1".equals(userIp)) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                    ResourcesHelper
                            .getValidation("documentGenerationAttention"),
                    ResourcesHelper
                            .getValidation("documentGenerationRunOnLocalhost"));
        }

        if (!ValidationHelper.isNullOrEmpty(osirixList)
                && !ValidationHelper.isNullOrEmpty(userIp)) {
            for (Osirix osirix : osirixList) {
                if (!ValidationHelper.isNullOrEmpty(osirix.getIpAddress())) {
                    if (osirix.getIpAddress().equals(userIp)) {
                        StringBuilder sb = new StringBuilder("");
                        sb.append("http://");
                        sb.append(osirix.getIpAddress());
                        sb.append(":");
                        sb.append(osirix.getPort());

                        this.setOsirixConnectionSettings(sb.toString());

                        this.setCurrentOsirix(osirix);

                        return Boolean.TRUE;
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    public void showWarningDicom() {
        MessageHelper
                .addGlobalMessage(
                        FacesMessage.SEVERITY_WARN,
                        ResourcesHelper
                                .getValidation("documentGenerationAttention"),
                        ResourcesHelper
                                .getValidation("documentGenerationNotPresentImages"));
    }

    public void openDicom() {
        if (this.getConsistInOsirixList()) {
            customOpenOsirix(this.getAllRequestsWrappers(), PacsType.CACHE);
        } else {
            if (!ValidationHelper.isNullOrEmpty(this.getRadiologyExamRequest()
                    .getSector().getWeasisUrl())
                    && this.getAllRequestsWrappers() != null) {
                String weasisUrl = this.getRadiologyExamRequest().getSector()
                        .getWeasisUrl();

                weasisUrlArray = OsirixHelper.getWeasisUrlArray(weasisUrl, this.getAllRequestsWrappers());

                executeJS("openWeasis(" + "'" + weasisUrlArray + "'" + ");");
            } else {
                cleanValidation();
                MessageHelper
                        .addGlobalMessage(
                                FacesMessage.SEVERITY_INFO,
                                ResourcesHelper
                                        .getValidation("documentGenerationAttention"),
                                ResourcesHelper
                                        .getValidation("documentGenerationURLWEASISEmpty"));
            }
        }
    }

    public void enableEditor() {
        if (this.getConsistInOsirixList() != null
                && this.getConsistInOsirixList()) {
            LogHelper.log(osirixInfoLog, "pbAjax start");
            executeJS("PF('pbAjax').start();");

            if (!ValidationHelper.isNullOrEmpty(this.getAllRequestsWrappers())) {
                Pacs pacs = null;
                try {
                    pacs = DaoManager.get(
                            Pacs.class,
                            new Criterion[]{
                                    Restrictions.eq("hospital.id", this
                                            .getAllRequestsWrappers().get(0)
                                            .getHospitalId()),
                                    Restrictions.eq("sector.id", this
                                            .getAllRequestsWrappers().get(0)
                                            .getSectorId()),
                                    Restrictions.eq("pacsType", PacsType.CACHE)
                            });
                } catch (HibernateException | InstantiationException
                         | IllegalAccessException | PersistenceBeanException e) {
                    LogHelper.log(osirixErrorLog, e);
                }

                if (pacs != null) {
                    for (RadExamRequestWrapper rerw : this
                            .getAllRequestsWrappers()) {
                        try {
                            AET sourceAET = new AET(pacs.getName(),
                                    pacs.getiPHostPacs(), pacs
                                    .getPortHostPacs().toString());

                            DicomHandler handlerDicom = new DicomHandler(
                                    sourceAET);

                            LogHelper
                                    .log(osirixInfoLog,
                                            "_________openOsirixAccessionNumbers in enable editor function_________");

                            OsirixHelper.openOsirixAccessionNumbers(
                                    rerw.getAccessNumber(),
                                    this.getOsirixConnectionSettings());

                            int countDicomSourceImages = OsirixHelper
                                    .countDicomSourceImages(
                                            rerw.getAccessNumber(),
                                            handlerDicom);

                            if (countDicomSourceImages > 0) {
                                LogHelper
                                        .log(osirixInfoLog,
                                                "countDicomSourceImages (images from pacs) > 0 ");
                                break;
                            } else {
                                LogHelper
                                        .log(osirixInfoLog,
                                                "ShowRedBar (countDicomSourceImages (images from pacs) <= 0 ) = true\nnow editor still disabled");

                                showRedBarAndMsg("pdfGenerationOsirixImagesInPacsNotFound");
                            }
                        } catch (Exception e) {
                            LogHelper
                                    .log(osirixInfoLog,
                                            "<< -- some EXCEPTION occurred please check in error log file -- >>");
                            LogHelper.log(osirixErrorLog, e);
                        }
                    }
                } else {
                    LogHelper.log(osirixInfoLog,
                            "ShowRedBar (pacs for hospital not found) = true");

                    showRedBarAndMsg("pdfGenerationOsirixHospitalNotFound");
                }
            }
            executeJS("PF('pbAjax').cancel();");
        }
    }

    private void showRedBarAndMsg(String msg) {
        this.setShowRedBar(Boolean.TRUE);
        this.setShowGreenBar(Boolean.FALSE);

        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                ResourcesHelper.getValidation("warning"),
                ResourcesHelper.getValidation(msg));

    }

    public void openOsirix() {
        customOpenOsirix(this.getAllRequestsWrappers(), PacsType.CACHE);
    }

    public void customOpenOsirix(
            List<RadExamRequestWrapper> radiologyExamRequestsWrappers,
            PacsType pacsType) {
        if (this.getConsistInOsirixList() != null
                && this.getConsistInOsirixList()) {
            LogHelper
                    .log(osirixInfoLog,
                            "_______________________________________START______________________________________________");
            LogHelper
                    .log(osirixInfoLog,
                            "your current IP and port exists in osirix list dictionary");
            LogHelper.log(osirixInfoLog,
                    "start check of validation radiology exam requests");

            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestsWrappers)) {
                LogHelper.log(osirixInfoLog,
                        "radiology exam requests are exists");

                Pacs pacs = null;
                try {
                    pacs = DaoManager.get(
                            Pacs.class,
                            new Criterion[]{
                                    Restrictions.eq("hospital.id",
                                            radiologyExamRequestsWrappers
                                                    .get(0).getHospitalId()),
                                    Restrictions.eq("sector.id",
                                            radiologyExamRequestsWrappers
                                                    .get(0).getSectorId()),
                                    Restrictions.eq("pacsType", pacsType)
                            });
                } catch (HibernateException | InstantiationException
                         | IllegalAccessException | PersistenceBeanException e) {
                    LogHelper.log(osirixErrorLog, e);
                }

                if (pacs != null) {
                    for (RadExamRequestWrapper rerw : radiologyExamRequestsWrappers) {
                        LogHelper.log(osirixInfoLog,
                                "call open osirix action for radiology exam request with id = <<"
                                        + rerw.getId() + ">>");

                        LogHelper.log(osirixInfoLog, "checkNumber = 0");

                        openOsirixAction(rerw.getAccessNumber(), pacs);
                    }
                } else {
                    LogHelper.log(osirixInfoLog,
                            "pacs for hospital not found\n ShowRedBar = true");

                    showRedBarAndMsg("pdfGenerationOsirixHospitalNotFound");
                }
            } else {
                LogHelper
                        .log(osirixInfoLog,
                                "radiology exam requests is null or empty\n ShowRedBar = true");
                showRedBarAndMsg("pdfGenerationOsirixImagesInPacsNotFound");
            }

            LogHelper
                    .log(osirixInfoLog,
                            "_______________________________________END________________________________________________");
        }
    }

    public void customCloseOsirix() {
        if (this.getConsistInOsirixList() != null
                && this.getConsistInOsirixList()) {
            LogHelper
                    .log(osirixInfoLog,
                            "_______________________________________your current IP and port exists in osirix list dictionary______________________________________________");

            OsirixHandler handler;

            LogHelper.log(osirixInfoLog,
                    "start creating osirix handler for close flow");

            try {
                LogHelper.log(osirixInfoLog, "osirix connection settings = <<"
                        + this.getOsirixConnectionSettings() + ">>");

                handler = new OsirixHandler(this.getOsirixConnectionSettings());
                LogHelper.log(osirixInfoLog, "osirix handler created");

                handler.callCloseAllWindows();
                LogHelper.log(osirixInfoLog,
                        "callCloseAllWindows function executed");
            } catch (Exception e) {
                LogHelper
                        .log(osirixInfoLog,
                                "<< -- some EXCEPTION occurred please check in error log file -- >>");
                LogHelper.log(osirixErrorLog, e);
            }

            LogHelper
                    .log(osirixInfoLog,
                            "_______________________________________for this access number work of closing flow finished________________________________________________");
        }

    }

    private void openOsirixAction(String accessNumber, Pacs pacs) {
        OsirixHandler handler;

        LogHelper.log(osirixInfoLog, "start creating osirix handler");
        if (getCurrentOsirix() == null || getCurrentOsirix().getDicomPort() == null || this.getCurrentOsirix().getAet() == null || this.getCurrentOsirix().getIpAddress() == null) {
            LogHelper.log(osirixInfoLog, "current osirix is null");
        } else {
            try {
                LogHelper.log(osirixInfoLog, "osirix connection settings = <<"
                        + this.getOsirixConnectionSettings() + ">>");

                handler = new OsirixHandler(this.getOsirixConnectionSettings());

                LogHelper.log(osirixInfoLog, "osirix handler created");

                LogHelper.log(
                        osirixInfoLog,
                        "For creating sourceAET used this parameters << name = "
                                + pacs.getAetPacs() + "; IP = "
                                + pacs.getiPHostPacs() + "; port = "
                                + pacs.getPortHostPacs().toString() + ">>");

                AET sourceAET = new AET(pacs.getAetPacs(), pacs.getiPHostPacs(),
                        pacs.getPortHostPacs().toString());

                DicomHandler handlerDicom = new DicomHandler(sourceAET);

                LogHelper.log(osirixInfoLog,
                        "For creating destAET used this parameters << name = "
                                + this.getCurrentOsirix().getAet() + "; IP = "
                                + this.getCurrentOsirix().getIpAddress()
                                + "; port = "
                                + this.getCurrentOsirix().getDicomPort().toString()
                                + ">>");

                AET destAET = new AET(this.getCurrentOsirix().getAet(), this
                        .getCurrentOsirix().getIpAddress(), this.getCurrentOsirix()
                        .getDicomPort().toString());

                LogHelper
                        .log(osirixInfoLog,
                                "_________openOsirixAccessionNumbers in openOsirixAction function_________");

                OsirixHelper.openOsirixAccessionNumbers(accessNumber,
                        this.getOsirixConnectionSettings());

                int countDicomSourceImages = OsirixHelper.countDicomSourceImages(
                        accessNumber, handlerDicom);

                if (handler.isErrors()) {
                    LogHelper.log(osirixInfoLog,
                            "[A] handler.isErrors() return true");

                    LogHelper.log(osirixErrorLog,
                            "Call error-result : {}  handler.getRemoteErrorCode()"
                                    + handler.getRemoteErrorCode());

                    LogHelper.log(osirixInfoLog,
                            "call checkForStudyToAction function for destAET");

                    this.setProgressbarValue(Integer.valueOf(0));

                    checkForStudyToAction(accessNumber, pacs, sourceAET, handler,
                            destAET);
                } else {
                    LogHelper.log(osirixInfoLog, "TUTTO OK!");
                    LogHelper.log(osirixInfoLog,
                            "[A] handlerOsirix.isErrors() return false");

                    if (countDicomSourceImages > 0) {
                        LogHelper.log(osirixInfoLog, "countDicomSourceImages > 0 ");

                        int countDicomDestImages = OsirixHelper
                                .countDicomDestImages(accessNumber, handlerDicom,
                                        destAET);

                        LogHelper.log(osirixInfoLog, "countDicomDestImages = <<"
                                + countDicomDestImages + ">>");

                        if (countDicomSourceImages == countDicomDestImages) {
                            successOsirixFlowAndUndisableEditor();

                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_INFO,
                                            ResourcesHelper.getString("succesfull"),
                                            ResourcesHelper
                                                    .getString("pdfGenerationOsirixSuccess"));
                        } else {
                            int current = OsirixHelper
                                    .calculateNewProgressBarValue(
                                            countDicomSourceImages,
                                            countDicomDestImages);

                            this.setProgressbarValue(Integer.valueOf(current));

                            LogHelper.log(osirixInfoLog, "progress bar status "
                                    + String.valueOf(current) + "%");

                            LogHelper.log(
                                    osirixInfoLog,
                                    "Count source - count dest ="
                                            + String.valueOf(countDicomSourceImages
                                            - countDicomDestImages));

                            if (countDicomSourceImages > countDicomDestImages) {
                                LogHelper
                                        .log(osirixInfoLog,
                                                "countDicomSourceImages > countDicomDestImages");

                                LogHelper.log(osirixInfoLog,
                                        "call checkForStudyToAction function");

                                checkForStudyToAction(accessNumber, pacs,
                                        sourceAET, handler, destAET);
                            } else {
                                LogHelper
                                        .log(osirixInfoLog,
                                                "countDicomSourceImages < countDicomDestImages\n ShowRedBar = true");

                                showRedBarAndMsg("pdfGenerationOsirixPacsNotContainAppropriate");
                            }
                        }
                    } else {
                        LogHelper.log(osirixInfoLog,
                                "countDicomSourceImages = 0 and ShowRedBar = true");

                        showRedBarAndMsg("pdfGenerationOsirixImagesInPacsNotFound");
                    }
                }
            } catch (Exception e) {
                LogHelper
                        .log(osirixInfoLog,
                                "<< -- some EXCEPTION occurred please check in error log file -- >>");
                LogHelper.log(osirixErrorLog, e);
            }
        }
        LogHelper.log(osirixInfoLog, "for this access number work finished");
    }

    private void successOsirixFlowAndUndisableEditor() {
        LogHelper
                .log(osirixInfoLog,
                        "countDicomSourceImages == countDicomDestImages\nprogress bar status 100%");

        this.setProgressbarValue(Integer.valueOf(100));

        LogHelper.log(osirixInfoLog, "ShowRedBar = false, ShowGreenBar = true");

        this.setShowRedBar(Boolean.FALSE);
        this.setShowGreenBar(Boolean.TRUE);

        if (!WaitingListRegistrationStates.REPORTED.equals(this
                .getRadiologyExamRequest().getWaitingListRegistrationState())) {
            LogHelper.log(osirixInfoLog, "_________undisableEditor_________");

            this.editorDisabled = Boolean.FALSE;
            setEnableHtmlEditorButton(Boolean.FALSE);

            executeJS("updateEnableEditorButton();");

            LogHelper.log(osirixInfoLog, "Text editor enabled.");
        } else {
            LogHelper.log(osirixInfoLog,
                    "Text editor and button disabled because state = REPORTED");
        }
    }

    private void checkForStudyToAction(final String accessNumber, Pacs pacs,
                                       final AET sourceAET, OsirixHandler handlerOsirix, final AET destAET)
            throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        LogHelper.log(osirixInfoLog, "____executorService first flow started____");
        List<Callable<Boolean>> tasks = new ArrayList<>();

        tasks.add(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LogHelper.log(osirixInfoLog, "____thread 1 first flow (sendStudyTo) started____");
                try {
                    DicomHandler MainHandlerDicom = new DicomHandler(sourceAET);
                    LogHelper.log(osirixInfoLog, "checkForStudyToAction: new MainHandlerDicom created");

                    LogHelper.log(osirixInfoLog, "handlerDicom.sendStudyTo(accessNumber, destAET) return "
                            + MainHandlerDicom.sendStudyTo(accessNumber, destAET));
                } catch (Exception e) {
                    LogHelper.log(osirixInfoLog, "checkForStudyToAction: exception appeared.");

                }
                LogHelper.log(osirixInfoLog, "____thread 1 first flow (sendStudyTo) ended____");
                return Boolean.FALSE;
            }
        });

        tasks.add(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LogHelper.log(osirixInfoLog, "____thread 2 first flow (checkNumberLogic) started____");

                boolean success = checkNumberLogicFirstFlow(accessNumber, sourceAET, destAET);
                LogHelper.log(osirixInfoLog, "____thread 2 first flow (checkNumberLogic) ended____");
                return success;
            }
        });
        List<Future<Boolean>> results = executorService.invokeAll(tasks);

        Boolean successB = null;
        try {
            successB = results.get(1).get();
        } catch (Exception e) {
            LogHelper.log(osirixErrorLog, e);
        }

        LogHelper.log(osirixInfoLog, "____threads first flow ended____");

        executorService.shutdown();
        LogHelper.log(osirixInfoLog, "____threads first flow shutdown ended____");
        try {
            if (!executorService.awaitTermination(2, TimeUnit.MINUTES))
                throw new InterruptedException("Time Limit of 2 Minutes Exceeded");
        } catch (InterruptedException e) {
            LogHelper.log(osirixErrorLog, e);
            executorService.shutdownNow();
        }

        LogHelper.log(osirixInfoLog, "shutdown first flow ended____");

        if (Boolean.FALSE.equals(successB)) {
            LogHelper.log(osirixInfoLog, "Study not transfer (checkNumber = 6)" +
                    "\nShowRedBar (!successs) = true, ShowGreenBar = false");

            showRedBarAndMsg("pdfGenerationOsirixPacsNotContainAppropriate");
        } else {
            successOsirixFlowAndUndisableEditor();

            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                    ResourcesHelper.getString("succesfull"),
                    ResourcesHelper.getString("pdfGenerationOsirixSuccess"));
        }

    }

    private boolean checkNumberLogicFirstFlow(final String accessNumber,
                                              final AET sourceAET, final AET destAET) throws InterruptedException {
        boolean success = false;
        boolean openedImagesOnce = false;
        for (int checkNumber = 0; checkNumber < 6; ++checkNumber) {
            LogHelper.log(osirixInfoLog, "checkNumber = " + checkNumber);

            LogHelper.log(osirixInfoLog, "sleep for 1 second...");
            Thread.sleep(1000); // sleep for 1 sec

            DicomHandler handlerDicom = new DicomHandler(sourceAET);

            LogHelper.log(osirixInfoLog, "handlerDicom for checkNumber = {" + checkNumber + "} created");

            int countDicomSourceImages = OsirixHelper.countDicomSourceImages(
                    accessNumber, handlerDicom);

            int countDicomDestImages = OsirixHelper.countDicomDestImages(
                    accessNumber, handlerDicom, destAET);

            LogHelper.log(osirixInfoLog, "checkForStudyToAction: new handlerDicom created");

            if (countDicomDestImages == countDicomSourceImages) {
                if (countDicomDestImages == 1 || countDicomDestImages == 2) {

                    try {
                        LogHelper.log(osirixInfoLog, "Osirix images number = 1 or 2");

                        OsirixHelper.openOsirixAccessionNumbers(accessNumber, getOsirixConnectionSettings());
                    } catch (Exception e) {
                        LogHelper.log(osirixInfoLog, "<< -- some EXCEPTION occurred please check in error log file -- >>");
                        LogHelper.log(osirixErrorLog, e);
                    }
                }
                successOsirixFlowAndUndisableEditor();

                success = true;
                break;
            } else {
                if (!openedImagesOnce && countDicomDestImages > 0) {
                    callOpenImages(accessNumber);
                    openedImagesOnce = true;
                }

                int current = OsirixHelper.calculateNewProgressBarValue(countDicomSourceImages, countDicomDestImages);

                if (current > this.getProgressbarValue()) {
                    checkNumber = 0;

                    LogHelper.log(osirixInfoLog, "new progressbar value > old progressbar value");
                    LogHelper.log(osirixInfoLog, "checkNumber set to \"0\" ");
                }
                this.setProgressbarValue(current);

                LogHelper.log(osirixInfoLog, "progress bar status " + current + "%");
            }
        }
        callOpenImages(accessNumber);

        return success;
    }

    private void callOpenImages(String accessNumber) {
        try {
            LogHelper.log(osirixInfoLog, "_________openOsirixAccessionNumbers in checkNumber < 6 function_________");
            OsirixHelper.openOsirixAccessionNumbers(accessNumber, getOsirixConnectionSettings());
        } catch (Exception e) {
            LogHelper.log(osirixInfoLog, "<< -- some EXCEPTION occurred please check in error log file -- >>");
            LogHelper.log(osirixErrorLog, e);
        }
    }

    public void enableEditorAction() {
        setEnableHtmlEditorButton(Boolean.FALSE);
        this.editorDisabled = Boolean.FALSE;
    }

    public void deleteRadRequest(final RadiologyExamRequest request, final List<RadExamRequestItemWrapper> list) throws
            Exception {
        if (list != null) {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    for (RadExamRequestItemWrapper requestItemWrapper : list) {
                        DaoManager.remove(RadiologyExamRequestItem.class, requestItemWrapper.getId());
                    }
                    removeEqualsItemFromList(request, list);
                }

                @Override
                public void onSuccess() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
                    for (RadExamRequestItemWrapper requestItemWrapper : list) {
                        Iterator<RadExamRequestItemWrapper> iterator = getAllRequestsItemsWrappers().iterator();
                        while (iterator.hasNext()) {
                            if (requestItemWrapper.getId().equals(iterator.next().getId())) {
                                iterator.remove();
                            }
                        }
                    }
                }

                @Override
                public void onException(Exception e) throws Exception {
                    super.onException(e);
                }
            });
        }
    }

    private void removeEqualsItemFromList(RadiologyExamRequest request, List<RadExamRequestItemWrapper> list) throws PersistenceBeanException {
        for (RadExamRequestItemWrapper requestItemWrapper : list) {
            Iterator<RadiologyExamRequestItem> iterator = request.getRadiologyExamRequestItems().iterator();
            while (iterator.hasNext()) {
                if (requestItemWrapper.getId().equals(iterator.next().getId())) {
                    iterator.remove();
                }
            }
        }
        request.udateItemsDescription(request.getRadiologyExamRequestItems());
        DaoManager.save(request);
    }

    private List<RadExamRequestItemWrapper> getWrapperByExam(List<RadiologyExam> list, List<RadiologyExamRequestItem> requestItems) {
        List<RadExamRequestItemWrapper> finallyList = null;
        if (!ValidationHelper.isNullOrEmpty(list)) {
            finallyList = new ArrayList<>();
            for (RadiologyExam exam : list) {
                for (RadiologyExamRequestItem item : requestItems) {
                    if (item.getRadiologyExam().equals(exam)) {
                        try {
                            finallyList.add(item.getRadExamRequestItemWrapperFromItem());
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }
                    }
                }
            }
        }
        return finallyList;
    }

    private void setSelectedRadRequest() {
        setSelectedRadiologyExams(new ArrayList<RadiologyExam>());
        setRemoveRadiologyExams(new ArrayList<RadiologyExam>());
        for (RadExamRequestItemWrapper wrap : this.getAllRequestsItemsWrappers()) {
            if (Boolean.TRUE.equals(wrap.getAddedManually())) {
                getSelectedRadiologyExams().add(wrap.getRadiologyExam());
                getRemoveRadiologyExams().add(wrap.getRadiologyExam());
            }
        }
    }

    public void beforeOpenAddExamDlgAction() {
        fillRadiologyExams();
        setSelectedRadRequest();
        try {
            List<SelectItem> accessNumbers = new ArrayList<SelectItem>();
            if (!ValidationHelper.isNullOrEmpty(this
                    .getAllRequestsItemsWrappers())) {
                if (!ValidationHelper.isNullOrEmpty(this
                        .getAllRequestsWrappers())
                        && this.getAllRequestsWrappers().size() > 1) {
                    accessNumbers.add(SelectItemHelper.getNotSelected());
                    setDisableAddExamAN(Boolean.FALSE);
                }
                List<Long> requestIds = new ArrayList<Long>();
                for (RadExamRequestItemWrapper radExamReqWrapper : this
                        .getAllRequestsItemsWrappers()) {
                    if (!requestIds.contains(radExamReqWrapper
                            .getRequestWrapper().getId())) {
                        requestIds.add(radExamReqWrapper.getRequestWrapper()
                                .getId());
                        accessNumbers.add(new SelectItem(radExamReqWrapper
                                .getRequestWrapper().getId(), radExamReqWrapper
                                .getRequestWrapper().getAccessNumber()));
                    }
                }

                if (this.getAllRequestsWrappers().size() > 1) {
                    this.setSelectedAccessNumber(Long.valueOf(0));
                } else {
                    this.setSelectedAccessNumber(this.getAllRequestsWrappers()
                            .get(0).getId());
                }
            }

            this.setAccessNumbers(accessNumbers);

            executeJS("PF('addRadiologyExamDialogWV').show();");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private boolean checkDuplicateRadiologyItems(List<RadiologyExamRequestItem> requestItems, RadiologyExam
            selectedRadiologyExam) {
        for (RadiologyExamRequestItem requestItem : requestItems) {
            if (selectedRadiologyExam.getId().equals(
                    requestItem.getRadiologyExam().getId())
                    && !selectedRadiologyExam
                    .getMultiple()) {
                return true;
            }
        }
        return false;
    }


    private boolean checkIsManual(RadiologyExam exam) {
        for (RadExamRequestItemWrapper wrapper : getAllRequestsItemsWrappers()) {
            if (wrapper.getRadiologyExam().getId().equals(exam.getId())) {
                return Boolean.TRUE.equals(wrapper.getAddedManually());
            }
        }
        return false;
    }

    private boolean containsRadiologyList(List<RadiologyExam> list, RadiologyExam exam) {
        for (RadiologyExam radiologyExam : list) {
            if (radiologyExam.getId().equals(exam.getId())) {
                return true;
            }
        }
        return false;
    }

    private List<RadiologyExam> getDiffSave(List<RadiologyExam> first, List<RadiologyExam> second) {
        List<RadiologyExam> finallyList = null;
        if (!ValidationHelper.isNullOrEmpty(first) && !ValidationHelper.isNullOrEmpty(second)) {
            finallyList = new ArrayList<>();
            finallyList.addAll(second);
            Iterator<RadiologyExam> iterator = finallyList.iterator();
            while (iterator.hasNext()) {
                RadiologyExam exam = iterator.next();
                if (containsRadiologyList(first, exam)) {
                    iterator.remove();
                }
            }
        } else if (ValidationHelper.isNullOrEmpty(first) || !ValidationHelper.isNullOrEmpty(second)) {
            finallyList = second;
        }
        return finallyList;
    }

    public void addOrRemoveRadiologyExam() {
        try {
            cleanValidation();
            if (!ValidationHelper.isNullOrEmpty(getSelectedRadiologyExams())
                    || !ValidationHelper.isNullOrEmpty(getRemoveRadiologyExams())) {
                if (!ValidationHelper.isNullOrEmpty(getSelectedAccessNumber())) {
                    // loading by getSelectedAccessNumber is ok, because
                    // getSelectedAccessNumber returns id of request, for
                    // detailed info
                    // look at beforeOpenAddExamDlgAction
                    if (getSelectedRadiologyExams().equals(getRemoveRadiologyExams())) {
                        addFieldExeption("form:radExamsTable", "documentGenerationSelectRadiologyExam");
                        return;
                    }
                    final RadiologyExamRequest request = DaoManager.get(RadiologyExamRequest.class,
                            getSelectedAccessNumber());
                    final List<RadiologyExamRequestItem> requestItems = request.getRadiologyExamRequestItems();

                    addNewRadiologyRequestItems(
                            request, getDiffSave(getRemoveRadiologyExams(), getSelectedRadiologyExams())
                    );
                    deleteRadRequest(request,
                            getWrapperByExam(
                                    getDiffSave(getSelectedRadiologyExams(), getRemoveRadiologyExams()), requestItems
                            )
                    );
                    RadiologyExamRequest updatedRequest = DaoManager.get(RadiologyExamRequest.class,
                            getSelectedAccessNumber());
                    List<Long> requestItemIds = new ArrayList<>();
                    for (RadiologyExamRequestItem itemWrapper : updatedRequest.getRadiologyExamRequestItems()) {
                        requestItemIds.add(itemWrapper.getId());
                    }
                    SessionHelper.putIds(requestItemIds, ID_IN_SESSION);
                } else {
                    addRequiredFieldExeption("form:accessNumbers");
                }
            } else {
                addFieldExeption("form:radExamsTable", "documentGenerationSelectRadiologyExam");
            }
            executeJS("PF('addRadiologyExamDialogWV').hide();");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void addNewRadiologyRequestItems(final RadiologyExamRequest request, final List<RadiologyExam>
            selectedRadiologyExams) throws Exception {
        final List<RadiologyExamRequestItem> requestItems = request.getRadiologyExamRequestItems();
        if (!ValidationHelper.isNullOrEmpty(requestItems) && !ValidationHelper.isNullOrEmpty(selectedRadiologyExams)) {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    for (RadiologyExam selectedRadiologyExam : selectedRadiologyExams) {
                        if (!checkDuplicateRadiologyItems(requestItems, selectedRadiologyExam)) {
                            RadiologyExamRequestItem simpleRequestItem = requestItems
                                    .get(0);

                            RadiologyExamRequestItem item = simpleRequestItem
                                    .clone();

                            item.setRadiologyExam(selectedRadiologyExam);
                            item.setAddedManually(Boolean.TRUE);
                            DaoManager.save(item);
                            requestItems.add(item);//FIXME: DELETE AFTER CREATE TRIGGER
                            request.udateItemsDescription(requestItems);
                            DaoManager.save(request);

                            RadExamRequestItemWrapper requestItemWrapper = item
                                    .getRadExamRequestItemWrapperFromItem();

                            RadExamRequestWrapper selectedRequestWrapper = null;
                            if (!ValidationHelper.isNullOrEmpty(getAllRequestsWrappers())) {
                                for (RadExamRequestWrapper requestWrapper : getAllRequestsWrappers()) {
                                    if (request.getId().equals(
                                            requestWrapper.getId())) {
                                        selectedRequestWrapper = requestWrapper;
                                        break;
                                    }
                                }
                            }

                            requestItemWrapper
                                    .setRequestWrapper(selectedRequestWrapper);

                            if (selectedRequestWrapper != null) {
                                selectedRequestWrapper
                                        .getRadExamRequestItemWrappers().add(
                                                requestItemWrapper);
                            }

                            getAllRequestsItemsWrappers().add(
                                    requestItemWrapper);
                        }
                    }
                }

                @Override
                public void onException(Exception e) throws Exception {
                    super.onException(e);
                }
            });
        }
    }

    public void openDicomForHistory() {
        try {
            List<HistoricalReport> selectedHistoricalReport = DaoManager.load(
                    HistoricalReport.class,
                    new Criterion[]{
                            Restrictions.eq("fileEntity.id",
                                    this.getSelectedHistoryId())
                    });
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();
            LogHelper.log(log, String.format("The user with id <%s> logged from the IP <%s>", getUserId(),
                    request.getRemoteAddr()));
            boolean isPresent = this.isIpConsistInOsirixList(null);
            LogHelper.log(log, String.format("IP <%s> is %s in the Osirix dictionary", request.getRemoteAddr(),
                    isPresent ? "present" : "not present"));
            if (!ValidationHelper.isNullOrEmpty(selectedHistoricalReport)
                    && isPresent) {
                List<RadExamRequestWrapper> requestsWrappers = OsirixHelper
                        .getRequestWrappersFromFileEntityId(this
                                .getSelectedHistoryId());

                if (!ValidationHelper.isNullOrEmpty(requestsWrappers)) {
                    List<String> list = new ArrayList<>();
                    for (RadExamRequestWrapper radwrap : requestsWrappers) {
                        list.add(radwrap.getId().toString());
                    }
                    LogHelper.log(log, String.format("Search for the requests <%s>", list.toString()));

                    OsirixHelper.secondOsirixFlow(requestsWrappers, null,
                            PacsType.LTA, this.getOsirixConnectionSettings(),
                            this.getCurrentOsirix());
                } else {
                    OsirixHelper.secondOsirixFlow(null,
                            selectedHistoricalReport.get(0), PacsType.LTA,
                            this.getOsirixConnectionSettings(),
                            this.getCurrentOsirix());
                }
            } else {
                List<RadExamRequestWrapper> requestsWrappers = OsirixHelper
                        .getRequestWrappersFromFileEntityId(this
                                .getSelectedHistoryId());

                if (!ValidationHelper.isNullOrEmpty(requestsWrappers)) {
                    Sector sector = null;

                    if (requestsWrappers.get(0) != null
                            && requestsWrappers.get(0).getSectorId() != null) {
                        sector = DaoManager.get(Sector.class,
                                requestsWrappers.get(0).getSectorId());
                    }

                    if (sector != null) {
                        String weasisUrl = sector.getWeasisUrl();

                        weasisUrlArray = OsirixHelper.getWeasisUrlArray(
                                weasisUrl, requestsWrappers);

                        executeJS("openWeasis(" + "'" + weasisUrlArray + "'" + ");");
                    } else {
                        MessageHelper
                                .addGlobalMessage(
                                        FacesMessage.SEVERITY_INFO,
                                        ResourcesHelper
                                                .getValidation("documentGenerationAttention"),
                                        ResourcesHelper
                                                .getValidation("documentGenerationURLWEASISEmpty"));
                    }
                } else {
                    MessageHelper
                            .addGlobalMessage(
                                    FacesMessage.SEVERITY_INFO,
                                    ResourcesHelper
                                            .getValidation("documentGenerationAttention"),
                                    ResourcesHelper
                                            .getValidation("documentGenerationURLWEASISEmpty"));
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void handleOpen() {
        try {
            fillGlossaryModule();
            executeJS("PF('bui').hide()");
        } catch (HibernateException | IllegalAccessException
                 | InstantiationException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
    }

    public void userLockChangeRedirect() {
        //Do nothing stay on page
        //this.goToWorklist();
    }

    public void userLockChangeGet() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        String currentValue = this.getEditorValue();
        try {
            this.setRadiologyExamRequest(DaoManager.get(
                    RadiologyExamRequest.class, this.getRadiologyExamRequest()
                            .getId()));
            this.onLoad();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        this.setEditorValue(currentValue);
    }

    private boolean checkUserClosed() {
        try {
            Long lastId = this.getRadiologyExamRequest().getUserClosingReportId();
            String userClosingReportId = DaoManager.getField(getShort() ? RadiologyExamRequestShort.class : RadiologyExamRequest.class,
                    "userClosingReportId", new Criterion[]{
                            Restrictions.eq("id", getRadiologyExamRequest().getId())
                    }, null);
            if (ValidationHelper.isNullOrEmpty(userClosingReportId)
                    || "null".equalsIgnoreCase(userClosingReportId)) {
                executeJS("PF('userLockChangeGet').show()");

                return false;
            } else {
                Long userId = Long.parseLong(userClosingReportId);
                if (!userId.equals(lastId)) {
                    this.setUserLockChangeRedirectUserName(DaoManager.get(User.class, userId).getFullname());
                    executeJS("PF('userLockChangeRedirect').show()");

                    return false;
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("warning"),
                    ResourcesHelper.getValidation("warning"));

            return false;
        }

        return true;
    }

    public void autoSave() throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        RadiologyExamRequestItem itemToSave = DaoManager.load(RadiologyExamRequestItem.class,
                new Criterion[]{
                        Restrictions.eq("radiologyExamRequestId", getRadiologyExamRequest().getId())
                }).get(0);
        itemToSave.setReportResultAutoSaved(this.getEditorValue());
        itemToSave.setAutoSaveDate(new Date());
        DaoManager.save(itemToSave, true);
    }

    public void handleImageUpload(FileUploadEvent event) {
        LogHelper.debugInfo(log, "Start upload file");
        LogHelper.debugInfo(log, "Event:" + event);
        if (event != null) {
            LogHelper.debugInfo(log, "Contents:" + event.getFile().getContent());
            LogHelper.debugInfo(log, "FileName:" + event.getFile().getFileName());
            String fileName = event.getFile().getFileName();
            String randomFileName = FileHelper.getRandomFileName(fileName);
            boolean bSuccessful = false;
            String imagePath = null;

            try (InputStream inputstream = event.getFile().getInputStream()) {
                LogHelper.debugInfo(log, "Inputstream:" + inputstream);
                File path = FileEntityHelper.locateOrCreateSavingImageDir();
                imagePath = FileHelper.writeFileToFolder(randomFileName, path,
                        IOUtils.toByteArray(inputstream));
                bSuccessful = true;
            } catch (IOException e) {
                LogHelper.log(log, e);
            }

            if (bSuccessful) {
                RequestAttachedFile requestAttachedFile = new RequestAttachedFile();
                requestAttachedFile.setTempId(getTempId().incrementAndGet());
                requestAttachedFile.setFileName(fileName);
                requestAttachedFile.setFilePath(imagePath);
                requestAttachedFile.setRadiologyExamRequest(getRadiologyExamRequest());
                getRequestAttachedFiles().add(requestAttachedFile);
            }
        }
        LogHelper.debugInfo(log, "End upload file");
    }

    public void previewPdf() {
        try {
            if (!ValidationHelper.isNullOrEmpty(getEditImageId())) {
                for (RequestAttachedFile requestAttachedFile : getRequestAttachedFiles()) {
                    if (getEditImageId().equals(requestAttachedFile.getTempId())) {
                        String fileName = new File(requestAttachedFile.getFilePath()).getName();
                        FileHelper.writeFileToFolder(fileName,
                                new File(FileHelper.getLocalFileDir()),
                                IOUtils.toByteArray(new FileInputStream(new File(requestAttachedFile.getFilePath()))));
                        setEditImagePath("/File/" + new File(requestAttachedFile.getFilePath()).getName());
                        executeJS("PF('previewPdfWV').show();PF('previewPdfWV').toggleMaximize();");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void previewImage() {
        try {
            if (!ValidationHelper.isNullOrEmpty(getEditImageId())) {
                for (RequestAttachedFile requestAttachedFile : getRequestAttachedFiles()) {
                    if (getEditImageId().equals(requestAttachedFile.getTempId())) {
                        String fileName = new File(requestAttachedFile.getFilePath()).getName();
                        FileHelper.writeFileToFolder(fileName,
                                new File(FileHelper.getLocalFileDir()),
                                IOUtils.toByteArray(new FileInputStream(new File(requestAttachedFile.getFilePath()))));
                        setEditImagePath("/File/" + new File(requestAttachedFile.getFilePath()).getName());
                        executeJS("PF('previewImageWV').show();PF('previewImageWV').toggleMaximize();");
                        if (!ValidationHelper.isNullOrEmpty(requestAttachedFile.getRotateDegree())) {
                            executeJS(String.format("setTimeout(function() {PF('rotateAndResizeWidget').rotateRight(%s);}, 100);",
                                    requestAttachedFile.getRotateDegree().toString()));
                        }

                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void rotateListener(final RotateEvent e) {
        if (!ValidationHelper.isNullOrEmpty(getEditImageId())) {
            for (RequestAttachedFile requestAttachedFile : getRequestAttachedFiles()) {
                if (getEditImageId().equals(requestAttachedFile.getTempId())) {
                    requestAttachedFile.setRotateDegree((long) e.getDegree());

                    break;
                }
            }
        }
    }

    public void removeImage() {
        if (!ValidationHelper.isNullOrEmpty(getEditImageId())) {
            for (RequestAttachedFile requestAttachedFile : getRequestAttachedFiles()) {
                if (getEditImageId().equals(requestAttachedFile.getTempId())) {
                    if (getRequestImagesForDelete() == null) {
                        setRequestImagesForDelete(new ArrayList<RequestAttachedFile>());
                    }
                    getRequestImagesForDelete().add(requestAttachedFile);
                    getRequestAttachedFiles().remove(requestAttachedFile);

                    break;
                }
            }
        }
    }

    public void saveImages() {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    for (RequestAttachedFile requestAttachedFile : getRequestAttachedFiles()) {
                        DaoManager.save(requestAttachedFile);
                    }
                    if (!ValidationHelper.isNullOrEmpty(getRequestImagesForDelete())) {
                        for (RequestAttachedFile requestAttachedFile : getRequestImagesForDelete()) {
                            if (!requestAttachedFile.isNew()) {
                                DaoManager.remove(requestAttachedFile);
                            }
                        }
                        getRequestImagesForDelete().clear();
                    }
                }
            });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void restoreImages() {
        try {
            setRequestImagesForDelete(new ArrayList<RequestAttachedFile>());
            setRequestAttachedFiles(DaoManager.load(RequestAttachedFile.class, new Criterion[]{
                    Restrictions.eq("radiologyExamRequest.id", getRadiologyExamRequest().getId())
            }));

            for (RequestAttachedFile requestAttachedFile : getRequestAttachedFiles()) {
                requestAttachedFile.setTempId(getTempId().incrementAndGet());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void saveTags() {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    List<RequestTag> requestTags = DaoManager.load(RequestTag.class, new Criterion[]{
                            Restrictions.eq("radiologyExamRequest.id", getRadiologyExamRequest().getId())
                    });
                    if (!ValidationHelper.isNullOrEmpty(requestTags)) {
                        for (RequestTag tag : requestTags) {
                            DaoManager.remove(tag);
                        }
                    }
                    if (!ValidationHelper.isNullOrEmpty(getTagFormString())) {
                        String tagStr = getTagFormString()
                                .replaceAll("<.*?>", "\n");
                        String[] tags = tagStr.split("\n");
                        for (String str : tags) {
                            if (!ValidationHelper.isNullOrEmpty(str)) {
                                RequestTag tag = new RequestTag();
                                tag.setRadiologyExamRequest(getRadiologyExamRequest());
                                tag.setTag(str);
                                DaoManager.save(tag);
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void loadAvtoSavedValue() {
        this.setEditorValue(getRadiologyExamRequest()
                .getRadiologyExamRequestItems().get(0).getReportResultAutoSaved());
        this.setAutoSavePresent(Boolean.FALSE);
    }

    public void createNoteMessage() {
        StringBuilder sb = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(getNotes())) {
            for (RequestNote rn : getNotes()) {
                sb.append(rn.getNote());
                sb.append("<br/>");
            }
        }
        String msg = sb.toString();
        if (!ValidationHelper.isNullOrEmpty(msg)) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                    ResourcesHelper.getString("documentGenerationNoteMessage"), msg);
        }
    }

    public List<SelectItem> getTemplates() {
        if (ValidationHelper.isNullOrEmpty(templates)) {
            try {
                fillTemplates();
            } catch (HibernateException | IllegalAccessException
                     | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }
        return templates;
    }

    public String getTempText() {
        if (getCurrentGlossary() != null) {
            return getCurrentGlossary().getText();
        }
        return "";
    }

    public void setTempText(String tempText) {
        // It's ok, this tempText is need to ckEditor js insertGlossaryToEditor
        // function work
    }

    public void setEditorDisabled(Boolean editorDisabled) {
        // empty is ok
    }

    public void changeDescription() {
        if (this.getDescriptionCheckbox() != null
                && this.getDescriptionCheckbox()) {
            try {
                setExamTypesText(radiologyExamRequest.getExamsDescription());
            } catch (Exception e) {
                LogHelper.log(log, e);
                setExamTypesText("");
            }
        } else {
            setExamTypesText(null);
        }
    }

    public boolean getUnblockItems() {
        return unblockItems;
    }

    public void setUnblockItems(boolean unblockItems) {
        this.unblockItems = unblockItems;
    }

    public Boolean getShort() {
        return isShort;
    }

    public void setShort(Boolean aShort) {
        isShort = aShort;
    }

    public String getUserFiscalCode() {
        return userFiscalCode;
    }

    public void setUserFiscalCode(String userFiscalCode) {
        this.userFiscalCode = userFiscalCode;
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
}
