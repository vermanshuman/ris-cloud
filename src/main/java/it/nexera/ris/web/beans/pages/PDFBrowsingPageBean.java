package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.PacsType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.GeneralFunctionsHelper;
import it.nexera.ris.common.helpers.logic.OsirixHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Osirix;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.web.beans.EntityListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.DocumentGenerationHistoryWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Named("pdfBrowsingPageBean")
@ViewScoped
public class PDFBrowsingPageBean extends EntityListPageBean<IndexedEntity>
        implements Serializable {

    private static final long serialVersionUID = -8203824951317680887L;

    private List<DocumentGenerationHistoryWrapper> historyDocuments;

    private Long selectedHistoryId;

    private RadiologyExamRequest radiologyExamRequest;

    private String pathToFile;

    private boolean disableDICOMButton;

    private String osirixConnectionSettings;

    private Osirix currentOsirix;

    private String weasisUrlArray;

    @Override
    public void onLoad() throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getEditingEntityId())) {
            try {
                RadiologyExamRequest downLoadedRequest = DaoManager.get(
                        RadiologyExamRequest.class,
                        Long.parseLong(this.getEditingEntityId()));

                if (!ValidationHelper.isNullOrEmpty(downLoadedRequest)
                        && !ValidationHelper.isNullOrEmpty(downLoadedRequest
                        .getRadiologyExamRequestItems())) {
                    setRadiologyExamRequest(downLoadedRequest);

                    fillHistoryDocuments();
                    this.setPathToFile(GeneralFunctionsHelper
                            .getPdfPathToFile(this.getRadiologyExamRequest()));
                }

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
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            RedirectHelper.goTo(PageTypes.WORKLIST);
        }
    }

    public void fillHistoryDocuments() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        setHistoryDocuments(new ArrayList<DocumentGenerationHistoryWrapper>());
        if (!ValidationHelper.isNullOrEmpty(this.getRadiologyExamRequest()
                .getPatient())) {
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
                            Order.desc("reportDate")
                    });

            if (!ValidationHelper.isNullOrEmpty(historicalReports)) {
                for (HistoricalReport historicalReport : historicalReports) {
                    getHistoryDocuments().add(
                            historicalReport.getDGWrapperFromEntity());
                }
            }
        }
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

                        if (fileEntity.getContent() != null) {
                            FileHelper.writeFileToFolder(fileEntity.getName(),
                                    new File(FileHelper.getLocalFileDir()),
                                    fileEntity.getContent());
                        }
                        // New logic for entities stored on hard drive
                        else if (fileEntity.getPath() != null) {
                            Path path = Paths.get(fileEntity.getPath());

                            if (Files.exists(path) && !Files.isDirectory(path)) {
                                FileHelper.writeFileToFolder(fileEntity
                                                .getName(),
                                        new File(FileHelper.getLocalFileDir()),
                                        Files.readAllBytes(Paths.get(fileEntity
                                                .getPath())));
                            }
                        }

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
        String userIp = DocumentGeneration.getIpAddr(log, request, this.getRadiologyExamRequest());

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

            LogHelper.debugInfo(log, String.format("The user with id <%s> logged from the IP <%s>", getCurrentUser().getId().toString(),
                    DocumentGeneration.getIpAddr(log, request, this.getRadiologyExamRequest())));
            boolean isPresent = this.isIpConsistInOsirixList(null);
            LogHelper.debugInfo(log, String.format("IP <%s> is %s in the Osirix dictionary", request.getRemoteAddr(),
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
                    LogHelper.debugInfo(log, String.format("Search for the requests <%s>", list.toString()));
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

                        weasisUrlArray = OsirixHelper.getWeasisUrlArray(weasisUrl, requestsWrappers);

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

    public void showWarningDicom(){
        MessageHelper
                .addGlobalMessage(
                        FacesMessage.SEVERITY_WARN,
                        ResourcesHelper
                                .getValidation("documentGenerationAttention"),
                        ResourcesHelper
                                .getValidation("documentGenerationNotPresentImages"));
    }

    public List<DocumentGenerationHistoryWrapper> getHistoryDocuments() {
        return historyDocuments;
    }

    public void setHistoryDocuments(
            List<DocumentGenerationHistoryWrapper> historyDocuments) {
        this.historyDocuments = historyDocuments;
    }

    public Long getSelectedHistoryId() {
        return selectedHistoryId;
    }

    public void setSelectedHistoryId(Long selectedHistoryId) {
        this.selectedHistoryId = selectedHistoryId;
    }

    public RadiologyExamRequest getRadiologyExamRequest() {
        return radiologyExamRequest;
    }

    public void setRadiologyExamRequest(
            RadiologyExamRequest radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public boolean getDisableDICOMButton() {
        return disableDICOMButton;
    }

    public void setDisableDICOMButton(boolean disableDICOMButton) {
        this.disableDICOMButton = disableDICOMButton;
    }

    public String getOsirixConnectionSettings() {
        return osirixConnectionSettings;
    }

    public void setOsirixConnectionSettings(String osirixConnectionSettings) {
        this.osirixConnectionSettings = osirixConnectionSettings;
    }

    public Osirix getCurrentOsirix() {
        return currentOsirix;
    }

    public void setCurrentOsirix(Osirix currentOsirix) {
        this.currentOsirix = currentOsirix;
    }

    public String getWeasisUrlArray() {
        return weasisUrlArray;
    }

    public void setWeasisUrlArray(String weasisUrlArray) {
    }
}
