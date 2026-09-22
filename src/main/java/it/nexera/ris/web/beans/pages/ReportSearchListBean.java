package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.PacsType;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.GeneralFunctionsHelper;
import it.nexera.ris.common.helpers.logic.OsirixHelper;
import it.nexera.ris.common.helpers.logic.RobotHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalProduceCD;
import it.nexera.ris.persistence.materialized.HistoricalReportMV;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.HistoricalReportWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import it.nexera.ris.web.beans.wrappers.logic.WaitingListRegistrationStateWrapper;
import it.nexera.ris.web.common.WrapperLazyModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Named("reportSearchListBean")
@ViewScoped
public class ReportSearchListBean extends
        EntityLazyListPageBean<HistoricalReportMV> implements Serializable {

    private static final long serialVersionUID = 3752739635564073419L;

    private String patientSurname;

    private String patientName;

    private String reportResultText;

    private Date patientBirthDate;

    private String patientFiscalCode;

    private String reportAccessNumber;

    private Date dateFrom;

    private Date dateTo;

    private Date datePerform;

    private List<SelectItem> doctorPossibleNames;

    private String selectedDoctorName;

    private List<SelectItem> possibleTSRM;

    private String selectedTSRM;

    private List<SelectItem> possibleExamTypes;

    private Long selectedExamType;

    private Boolean searchOnlyReported;

    private HistoricalReportMV selectedHistoricalReport;

    private String weasisUrlArray;

    private List<WaitingListRegistrationStateWrapper> allStates;

    private List<WaitingListRegistrationStates> selectedStates;

    private WaitingListRegistrationStateWrapper selectedStateForFilter;

    private Boolean consistInOsirixList;

    private String osirixConnectionSettings;

    private Osirix currentOsirix;

    private List<SelectItem> sectors;

    private Long selectedSector;

    // Values for State filter - needed to save search values for filter after
    // search button was clicked

    private String patientSurnameForFilter;

    private String patientNameForFilter;

    private Date patientBirthDateForFilter;

    private String patientFiscalCodeForFilter;

    private String reportAccessNumberForFilter;

    private Date dateFromForFilter;

    private Date dateToForFilter;

    private String selectedDoctorNameForFilter;

    private String selectedTSRMForFilter;

    private Long selectedExamTypeForFilter;

    private Long selectedSectorForFilter;

    private String tagFormString;

    private WrapperLazyModel lazyModelList;

    private Pacs selectedPacs;

    private List<DVDProducer> robots;

    private Long selectRobotId;

    private String dicomInfoMsg;

    private RadiologyExamRequest radiologyExamRequest;

    private StreamedContent xmlContentFile;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        if (!this.isPostback()) {
            fillDropdownLists();
        }
    }

    public static String getIpAddr(Logger log, HttpServletRequest request) {
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

    private Boolean isIpConsistInOsirixList() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {

        List<Osirix> osirixList = DaoManager.load(Osirix.class);

        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();
        String userIp = getIpAddr(log, request);

        if ("0:0:0:0:0:0:0:1".equals(userIp)) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                    ResourcesHelper.getValidation("reportSearchAttention"),
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

    public void checkHistoryProduceCD() {
        try {
            setRadiologyExamRequest(DaoManager.get(RadiologyExamRequest.class, getSelectedHistoricalReport().getRadiologyExamRequestId()));
            Long count = DaoManager.getCount(HistoricalProduceCD.class, "id", new Criterion[]{
                    Restrictions.eq("radiologyExamRequest.id", getRadiologyExamRequest().getId())
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
            if (getRadiologyExamRequest() != null) {
                Pacs pacs = DaoManager.get(
                        Pacs.class,
                        new Criterion[]{
                                Restrictions.eq("sector.id",
                                        getRadiologyExamRequest().getSectorId()),
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
                                getRadiologyExamRequest().getSectorId())
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
            if (getRadiologyExamRequest() != null) {
                DVDProducer robot = null;
                for (DVDProducer producer : getRobots()) {
                    if (producer.getId().equals(getSelectRobotId())) {
                        robot = producer;
                        break;
                    }
                }
                String msg;
                boolean result = RobotHelper.produceCD(robot, getSelectedPacs(),
                        getRadiologyExamRequest().getId(), getRadiologyExamRequest().getAccessNumber());
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

    public void createXml(HistoricalReportMV selectedHistoricalReport) {
        if (selectedHistoricalReport != null) {
            try {
                String xml = XmlHelper.createCdaXml(selectedHistoricalReport);
                setXmlContentFile(DefaultStreamedContent.builder()
                        .name(FileHelper.getRandomFileName("1.xml"))
                        .contentType("text/xml")
                        .stream(() -> new ByteArrayInputStream(xml.getBytes()))
                        .build());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void searchReport() {
        this.cleanValidation();
        if (areAllFieldsEmpty()) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "",
                    ResourcesHelper.getValidation("allSearchFieldsEmpty"));
            return;
        }
        if (!ValidationHelper.isNullOrEmpty(this.getDateFrom())
                && !ValidationHelper.isNullOrEmpty(this.getDateTo())) {
            if (this.getDateFrom().after(this.getDateTo())) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "",
                        ResourcesHelper.getValidation("dateFromGreaterDateTo"));
                return;
            }
        }

        fillValsForFilter();
        loadReports();
    }

    public void fillValsForFilter() {
        this.patientSurnameForFilter = this.getPatientSurname();
        this.patientNameForFilter = this.getPatientName();
        this.patientBirthDateForFilter = this.getPatientBirthDate();
        this.patientFiscalCodeForFilter = this.getPatientFiscalCode();
        this.reportAccessNumberForFilter = this.getReportAccessNumber();
        this.dateFromForFilter = this.getDateFrom();
        this.dateToForFilter = this.getDateTo();
        this.selectedDoctorNameForFilter = this.getSelectedDoctorName();
        this.selectedTSRMForFilter = this.getSelectedTSRM();
        this.selectedExamTypeForFilter = this.getSelectedExamType();
        this.selectedSectorForFilter = this.getSelectedSector();
    }

    public boolean checkValsForFilter() {
        if (!ValidationHelper.isNullOrEmpty(patientSurnameForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(selectedSectorForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(patientNameForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(patientBirthDateForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(patientFiscalCodeForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(reportAccessNumberForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(dateFromForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(dateToForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(selectedDoctorNameForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(selectedTSRMForFilter)) {
            return true;
        }
        if (!ValidationHelper.isNullOrEmpty(selectedExamTypeForFilter)) {
            return true;
        }
        return !ValidationHelper.isNullOrEmpty(tagFormString);
    }

    public void openReport() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(this
                .getSelectedHistoricalReport())
                && !ValidationHelper.isNullOrEmpty(this
                .getSelectedHistoricalReport().getFileEntityLazy())) {

            GeneralFunctionsHelper.openPdfInNewTab(this
                    .getSelectedHistoricalReport().getFileEntityLazy());
        }
    }

    public void filterTableFromPanel() {
        if (!ValidationHelper.isNullOrEmpty((this.getAllStates()))) {
            setSelectedStates(new ArrayList<WaitingListRegistrationStates>());

            for (WaitingListRegistrationStateWrapper wlrsw : this
                    .getAllStates()) {
                if (wlrsw.getSelected()) {
                    getSelectedStates().add(wlrsw.getRealState());
                }
            }
        }

        loadReports();
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

    private void loadReports() {
        try {
            List<Criterion> searchCriterion = new ArrayList<>();
            List<CriteriaAlias> aliases = new ArrayList<>();

            if (!ValidationHelper.isNullOrEmpty(this.getPatientSurname())) {
                searchCriterion.add(Restrictions.ilike("patientSurname",
                        this.getPatientSurname(), MatchMode.START));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getPatientName())) {
                searchCriterion.add(Restrictions.ilike("patientName",
                        this.getPatientName(), MatchMode.START));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getPatientBirthDate())) {
                searchCriterion.add(Restrictions.eq("patientBirthDate",
                        this.getPatientBirthDate()));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getReportResultText())) {
                searchCriterion.add(Restrictions.sqlRestriction(
                        "CONTAINS({alias}.report_result, ?, 1) > 0",
                        this.getReportResultText(),
                        StringType.INSTANCE));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getPatientFiscalCode())) {
                searchCriterion.add(Restrictions.ilike("patientFiscalCode",
                        this.getPatientFiscalCode(), MatchMode.START));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getReportAccessNumber())) {
                searchCriterion.add(Restrictions.ilike("accessNumber",
                        this.getReportAccessNumber(), MatchMode.START));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getSelectedDoctorName())) {
                searchCriterion.add(Restrictions.eq("referringDoctor",
                        this.getSelectedDoctorName()));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getSelectedTSRM())) {
                searchCriterion.add(Restrictions.eq("tsrm",
                        this.getSelectedTSRM()));
            }

            if (!ValidationHelper.isNullOrEmpty(this.getSelectedExamType())) {
                searchCriterion.add(Restrictions.eq("examType.id",
                        this.getSelectedExamType()));
            }

            if (Boolean.FALSE.equals(this.getSearchOnlyReported())) {

                searchCriterion.add(Restrictions.or(Restrictions.eq(
                        "waitingListRegistrationState",
                        WaitingListRegistrationStates.REPORTED),
                        Restrictions.eq("waitingListRegistrationState",
                                WaitingListRegistrationStates.SIGNED)));

                if (!ValidationHelper.isNullOrEmpty(this.getSelectedStates())
                        && this.checkValsForFilter()) {
                    searchCriterion.add(Restrictions
                            .in("waitingListRegistrationState",
                                    getSelectedStates()));
                } else {
                    searchCriterion.add(Restrictions.not(Restrictions.in(
                            "waitingListRegistrationState",
                            WaitingListRegistrationStates.values())));
                }

                if (!ValidationHelper.isNullOrEmpty(this.getDateFrom())) {
                    searchCriterion.add(Restrictions.ge("performDate",
                            DateTimeHelper.getDayStart(this.getDateFrom())));
                }

                if (!ValidationHelper.isNullOrEmpty(this.getDateTo())) {
                    searchCriterion.add(Restrictions.le("performDate",
                            DateTimeHelper.getDayEnd(this.getDateTo())));
                }

            } else {

                if (!ValidationHelper.isNullOrEmpty(this.getSelectedStates())
                        && this.checkValsForFilter()) {
                    searchCriterion.add(Restrictions
                            .in("waitingListRegistrationState",
                                    getSelectedStates()));
                } else {
                    searchCriterion.add(Restrictions.not(Restrictions.in(
                            "waitingListRegistrationState",
                            WaitingListRegistrationStates.values())));
                }

                if (!ValidationHelper.isNullOrEmpty(this.getDateFrom())) {
                    searchCriterion.add(Restrictions.ge("latestActionDate",
                            DateTimeHelper.getDayStart(this.getDateFrom())));
                }

                if (!ValidationHelper.isNullOrEmpty(this.getDateTo())) {
                    searchCriterion.add(Restrictions.le("latestActionDate",
                            DateTimeHelper.getDayEnd(this.getDateTo())));
                }

            }

            if (!ValidationHelper.isNullOrEmpty(this.getDatePerform())) {
                searchCriterion.add(Restrictions.ge("performDate",
                        DateTimeHelper.getDayStart(this.getDatePerform())));

                searchCriterion.add(Restrictions.le("performDate",
                        DateTimeHelper.getDayEnd(this.getDatePerform())));
            }

            if (!ValidationHelper.isNullOrEmpty(getTagFormString())) {
                String tagStr = getTagFormString()
                        .replaceAll("<.*?>", "\n");
                String[] tags = tagStr.split("\n");
                List<String> searchTags = new ArrayList<>();
                for (String str : tags) {
                    if (!ValidationHelper.isNullOrEmpty(str)) {
                        searchTags.add(str);
                        executeJS(String.format("createHtmlTag('%s')", str));
                    }
                }
                if (!searchTags.isEmpty()) {
                    searchCriterion.add(Subqueries.propertyIn("radiologyExamRequestId",
                            GeneralFunctionsHelper.getRequestIdsWithTags(searchTags)));
                }
            }

            setLazyModelList(new WrapperLazyModel<HistoricalReportWrapper, HistoricalReportMV>(HistoricalReportMV.class,
                    searchCriterion.toArray(new Criterion[0]), new Order[]{
                    Order.desc("latestActionDate")
            }, aliases.toArray(new CriteriaAlias[0])));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void fillDropdownLists() {
        try {
            this.setPossibleExamTypes(ComboboxHelper.fillList(ExamType.class,
                    true));

            this.loadAllDoctorsAndTSRM();

            if (ValidationHelper.isNullOrEmpty(getSectors())) {
                setSectors(ComboboxHelper.fillList(Sector.class,
                        Order.asc("description"), true));
            }

            if (this.getSearchOnlyReported() == null
                    || !this.getSearchOnlyReported()) {
                if (this.getAllStates() != null
                        && this.getAllStates().size() < 2
                        && this.getSelectedStates() != null
                        && this.getSelectedStates().contains(
                        WaitingListRegistrationStates.REPORTED)) {
                    this.setAllStates(WaitingListStatusHelper
                            .getWorklistStateWrappers());
                    for (WaitingListRegistrationStateWrapper stw : this
                            .getAllStates()) {
                        if (stw.getValue().equals(
                                WaitingListRegistrationStates.REPORTED
                                        .toString())) {
                            stw.setSelected(Boolean.TRUE);
                        }
                    }
                } else {
                    this.setAllStates(WaitingListStatusHelper
                            .getWorklistStateWrappers());
                    for (WaitingListRegistrationStateWrapper ws : this
                            .getAllStates()) {
                        ws.setSelected(Boolean.TRUE);
                    }
                    this.setSelectedStates(Arrays
                            .asList(WaitingListRegistrationStates.values()));
                }
            } else {
                List<WaitingListRegistrationStateWrapper> states = new ArrayList<>();
                WaitingListRegistrationStateWrapper reported = new WaitingListRegistrationStateWrapper(
                        WaitingListRegistrationStates.REPORTED);

                states.add(reported);
                this.setAllStates(states);

                ArrayList<WaitingListRegistrationStates> selStates = new ArrayList<>();
                if (this.getSelectedStates().contains(
                        WaitingListRegistrationStates.REPORTED)) {
                    selStates.add(WaitingListRegistrationStates.REPORTED);
                    reported.setSelected(Boolean.TRUE);
                }
                this.setSelectedStates(selStates);
            }
        } catch (HibernateException | IllegalAccessException
                | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAllDoctorsAndTSRM() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        String refDoctorQueryStr = "select referring_doctor from unique_referring_doctor";
        Query query = DaoManager.getSession().createSQLQuery(refDoctorQueryStr);

        List<String> referringDoctors = query.list();

        String tsrmQueryStr = "select tsrm from unique_tsrm";
        query = DaoManager.getSession().createSQLQuery(tsrmQueryStr);

        List<String> allTSRM = query.list();

        this.setDoctorPossibleNames(new ArrayList<SelectItem>());
        this.getDoctorPossibleNames().add(SelectItemHelper.getNotSelected());
        for (String referringDoctor : referringDoctors) {
            this.getDoctorPossibleNames().add(
                    new SelectItem(referringDoctor));
        }

        this.setPossibleTSRM(new ArrayList<SelectItem>());
        this.getPossibleTSRM().add(SelectItemHelper.getNotSelected());
        for (String s : allTSRM) {
            this.getPossibleTSRM().add(new SelectItem(s));
        }
    }

    public void openDicomForHistory() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        if (this.getSelectedHistoricalReport() != null
                && !WaitingListRegistrationStates.DRAFT.equals(this
                .getSelectedHistoricalReport()
                .getWaitingListRegistrationState())) {
            if (this.getSelectedHistoricalReport().getFileEntityId() != null) {
                this.setConsistInOsirixList(this.isIpConsistInOsirixList());

                try {
                    if (!ValidationHelper
                            .isNullOrEmpty(getSelectedHistoricalReport())) {
                        HistoricalReportMV historicalReport = DaoManager.get(
                                HistoricalReportMV.class,
                                getSelectedHistoricalReport().getId());
                        if (this.getConsistInOsirixList() != null
                                && this.getConsistInOsirixList()) {
                            List<RadExamRequestWrapper> requestsWrappers = OsirixHelper
                                    .getRequestWrappersFromFileEntityId(historicalReport
                                            .getFileEntityId());

                            if (!ValidationHelper
                                    .isNullOrEmpty(requestsWrappers)) {
                                OsirixHelper.secondOsirixFlow(requestsWrappers,
                                        null, PacsType.LTA,
                                        this.getOsirixConnectionSettings(),
                                        this.getCurrentOsirix());
                            } else {
                                OsirixHelper.secondOsirixFlow(null,
                                        historicalReport.toHistoricalReportForOsirix(), PacsType.LTA,
                                        this.getOsirixConnectionSettings(),
                                        this.getCurrentOsirix());
                            }
                        } else {
                            List<RadExamRequestWrapper> requestsWrappers = OsirixHelper
                                    .getRequestWrappersFromFileEntityId(historicalReport
                                            .getFileEntityId());

                            if (!ValidationHelper
                                    .isNullOrEmpty(requestsWrappers)) {
                                Sector sector = null;

                                if (requestsWrappers.get(0) != null
                                        && requestsWrappers.get(0)
                                        .getSectorId() != null) {
                                    sector = DaoManager.get(Sector.class,
                                            requestsWrappers.get(0)
                                                    .getSectorId());
                                }

                                if (sector != null) {
                                    String weasisUrl = sector.getWeasisUrl();

                                    weasisUrlArray = OsirixHelper
                                            .getWeasisUrlArray(weasisUrl,
                                                    requestsWrappers);

                                    executeJS("openWeasisAction();");
                                } else {
                                    MessageHelper
                                            .addGlobalMessage(
                                                    FacesMessage.SEVERITY_INFO,
                                                    ResourcesHelper
                                                            .getValidation("reportSearchAttention"),
                                                    ResourcesHelper
                                                            .getValidation("weasisUrlForThisSectorIsEmpty"));
                                }
                            } else {
                                MessageHelper
                                        .addGlobalMessage(
                                                FacesMessage.SEVERITY_INFO,
                                                ResourcesHelper
                                                        .getValidation("reportSearchAttention"),
                                                ResourcesHelper
                                                        .getValidation("reportSearchURLWeasisEmpty"));
                            }
                        }
                    }
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            } else {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        ResourcesHelper.getValidation("reportSearchAttention"),
                        ResourcesHelper
                                .getValidation("reportSearchFileEntityEmpty"));
            }
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                    ResourcesHelper.getValidation("reportSearchAttention"),
                    ResourcesHelper
                            .getValidation("reportSearchNotInReportedState"));
        }
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

    private boolean areAllFieldsEmpty() {
        return ValidationHelper.isNullOrEmpty(this.getPatientSurname())
                && ValidationHelper.isNullOrEmpty(this.getPatientName())
                && ValidationHelper.isNullOrEmpty(this.getPatientBirthDate())
                && ValidationHelper.isNullOrEmpty(this.getPatientFiscalCode())
                && ValidationHelper.isNullOrEmpty(this.getReportAccessNumber())
                && ValidationHelper.isNullOrEmpty(this.getDateFrom())
                && ValidationHelper.isNullOrEmpty(this.getDateTo())
                && ValidationHelper.isNullOrEmpty(this.getSelectedDoctorName())
                && ValidationHelper.isNullOrEmpty(this.getSelectedTSRM())
                && ValidationHelper.isNullOrEmpty(this.getSelectedExamType())
                && ValidationHelper.isNullOrEmpty(this.getSelectedSector())
                && ValidationHelper.isNullOrEmpty(this.getTagFormString());
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

    public Boolean getSearchOnlyReported() {
        return searchOnlyReported;
    }

    public void setSearchOnlyReported(Boolean searchOnlyReported) {
        this.searchOnlyReported = searchOnlyReported;
    }

    public Boolean getConsistInOsirixList() {
        return consistInOsirixList;
    }

    public void setConsistInOsirixList(Boolean consistInOsirixList) {
        this.consistInOsirixList = consistInOsirixList;
    }

}
