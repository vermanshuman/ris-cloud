package it.nexera.ris.common.helpers.logic;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestBase;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestItemBase;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendar;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.InstancePhases;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TemplateDocumentModel;
import it.nexera.ris.persistence.beans.entities.domain.relation.DiagnosticRadiologyExam;
import it.nexera.ris.web.beans.wrappers.DocumentTemplateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.hibernate.HibernateException;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GeneralFunctionsHelper extends BaseHelper {

    private static final String ID_IN_SESSION_FOR_TAGS = "RadiologyExamsIdsForPrint";

    public static List<SelectItem> fillTemplates(
            DocumentGenerationPlaces place, Long sectorId, List<Long> sectorIds)
            throws HibernateException, IllegalAccessException,
            PersistenceBeanException {
        List<SelectItem> templates = new ArrayList<SelectItem>();
        List<Criterion> criteria = new ArrayList<>();
        criteria.add(Restrictions.eq("place", place));
        criteria.add(Restrictions.isNotNull("model"));
        if (!ValidationHelper.isNullOrEmpty(sectorId)) {
            criteria.add(Restrictions.eq("sector.id", sectorId));
        } else if (!ValidationHelper.isNullOrEmpty(sectorIds)) {
            criteria.add(Restrictions.in("sector.id", sectorIds));
        }

        List<InstancePhases> instancePhases = DaoManager.load(
                InstancePhases.class,
                criteria.toArray(new Criterion[0]));

        List<Long> modelIds = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(instancePhases)) {
            for (InstancePhases ip : instancePhases) {
                modelIds.add(ip.getModel().getId());
            }
        }

        if (!ValidationHelper.isNullOrEmpty(modelIds)) {
            List<Long> templateIds = new ArrayList<Long>();

            List<TemplateDocumentModel> templateDocumentModels = DaoManager
                    .load(TemplateDocumentModel.class, new Criterion[]{
                            Restrictions.in("id", modelIds.toArray())
                    });

            if (!ValidationHelper.isNullOrEmpty(templateDocumentModels)) {
                for (TemplateDocumentModel tdm : templateDocumentModels) {
                    if (!ValidationHelper.isNullOrEmpty(tdm
                            .getDocumentTemplates())) {
                        for (DocumentTemplate dt : tdm.getDocumentTemplates()) {
                            if (!templateIds.contains(dt.getId())) {
                                templates.add(new SelectItem(dt.getId(), dt
                                        .getName()));
                                templateIds.add(dt.getId());
                            }
                        }
                    }
                }
            }
        }

        return templates;
    }

    public static void showPdfNotGeneratedWarnMsg() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                ResourcesHelper.getValidation("warning"),
                ResourcesHelper.getValidation("pdfDocumentWasNotGenerated"));
    }

    //New method to save file on disc instead creating fileentity
    public static String generatePdfNew(
            RadiologyExamRequest radiologyExamRequest, Long selectedTemplateId,
            UserWrapper currentUser, String comment, String reportResult,
            String modyfiedExamRequest, boolean openInNewTab) {

        DocumentTemplateWrapper filledTemplate;
        boolean showErrMsg = false;
        String filePath = null;
        try {
            if (!ValidationHelper.isNullOrEmpty(selectedTemplateId)) {
                DocumentTemplate documentTemplate = DaoManager.get(
                        DocumentTemplate.class, new Criterion[]{
                                Restrictions.eq("id", selectedTemplateId)
                        });

                if (!ValidationHelper.isNullOrEmpty(documentTemplate)) {
                    filledTemplate = TemplateToPdfHelper.fillTemplate(
                            documentTemplate, radiologyExamRequest,
                            currentUser, comment, reportResult,
                            modyfiedExamRequest);

                    byte[] data = TemplateToPdfHelper
                            .convertAndReturnData(documentTemplate,
                                    filledTemplate, null);


                    String randomFileName;

                    if (Boolean.TRUE.equals(Boolean.valueOf(
                            ResourcesHelper.getString("saveOnHard")))
                            && !ValidationHelper
                            .isNullOrEmpty(radiologyExamRequest
                                    .getRadiologyExamRequestItems())) {
                        randomFileName = FileHelper
                                .getFileEntityName(radiologyExamRequest
                                        .getRadiologyExamRequestItems()
                                        .get(0), null);
                    } else {
                        randomFileName = FileHelper
                                .getRandomFileName("1.pdf");
                    }

                    File path = FileEntityHelper.locateOrCreateSavingDir();
                    FileHelper.writeFileToFolder(randomFileName,
                            path,
                            data);
                    filePath = path.getAbsolutePath() + File.separator + randomFileName;
                    if (openInNewTab) {
                        FileHelper.writeFileToFolder(randomFileName,
                                new File(FileHelper.getLocalFileDir()),
                                data);
                        RedirectHelper.sendRedirect("/File/"
                                        + randomFileName + "?pfdrid_c=true",
                                true);
                    }
                } else {
                    showErrMsg = true;
                }
            } else {
                showErrMsg = true;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            showErrMsg = true;
        }

        if (showErrMsg) {
            showPdfNotGeneratedWarnMsg();
        }

        return filePath;
    }

    public static FileEntity generatePdf(
            RadiologyExamRequest radiologyExamRequest, Long selectedTemplateId,
            UserWrapper currentUser, String comment, String reportResult,
            String modyfiedExamRequest, boolean openInNewTab, Date reportDate) {
        return generatePdf(radiologyExamRequest, selectedTemplateId, currentUser, comment,
                reportResult, modyfiedExamRequest, openInNewTab, reportDate, null);
    }
    public static FileEntity generatePdf(
            RadiologyExamRequest radiologyExamRequest, Long selectedTemplateId,
            UserWrapper currentUser, String comment, String reportResult,
            String modyfiedExamRequest, boolean openInNewTab, Date reportDate, List<RequestAttachedFile> requestAttachedFiles) {

        FileEntity file = new FileEntity();
        DocumentTemplateWrapper filledTemplate = null;
        boolean showErrMsg = false;
        try {
            if (!ValidationHelper.isNullOrEmpty(selectedTemplateId)) {
                DocumentTemplate documentTemplate = DaoManager.get(
                        DocumentTemplate.class, new Criterion[]{
                                Restrictions.eq("id", selectedTemplateId)
                        });

                if (!ValidationHelper.isNullOrEmpty(documentTemplate)) {
                    filledTemplate = TemplateToPdfHelper.fillTemplate(
                            documentTemplate, radiologyExamRequest,
                            currentUser, comment, reportResult,
                            modyfiedExamRequest);

                    byte[] data = TemplateToPdfHelper.convertAndReturnData(
                            documentTemplate, filledTemplate, null, requestAttachedFiles);
                    String randomFileName = null;

                    if (Boolean.TRUE.equals(Boolean.valueOf(
                            ResourcesHelper.getString("saveOnHard")))
                            && !ValidationHelper
                            .isNullOrEmpty(radiologyExamRequest
                                    .getRadiologyExamRequestItems())) {
                        randomFileName = FileHelper
                                .getFileEntityName(radiologyExamRequest
                                        .getRadiologyExamRequestItems()
                                        .get(0), reportDate);
                    } else {
                        randomFileName = FileHelper
                                .getRandomFileName("1.pdf");
                    }
                    if (Boolean.TRUE.equals(Boolean.valueOf(ResourcesHelper
                            .getString("saveOnHard")))) {

                        File path = FileEntityHelper
                                .locateOrCreateSavingDir();

                        file.setPath(path.getAbsolutePath()
                                + File.separator + randomFileName);

                        FileHelper.writeFileToFolder(randomFileName, path,
                                data);

                    } else {
                        file.setContent(data);
                    }

                    if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest
                            .getRadiologyExamRequestItems())
                            && radiologyExamRequest
                            .getRadiologyExamRequestItems().get(0)
                            .getFileEntity() != null) {
                        long version = 0L;
                        try {
                            String versionStr = DaoManager.getField(FileEntity.class, "versionOfSave", new Criterion[]{
                                    Restrictions.eq("id", radiologyExamRequest
                                            .getRadiologyExamRequestItems().get(0).getFileEntityId())
                            }, null);
                            if (!ValidationHelper.isNullOrEmpty(versionStr)) {
                                version = Long.parseLong(versionStr);
                            }
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }

                        file.setVersionOfSave(version);
                    }

                    file.setName(randomFileName);
                    file.setIncrementVersionOnSave(Boolean.TRUE);
                    DaoManager.save(file);
                    FileHelper.writeFileToFolder(randomFileName, new File(
                            FileHelper.getLocalFileDir()), data);

                    if (openInNewTab) {
                        RedirectHelper.sendRedirect("/File/"
                                + randomFileName + "?pfdrid_c=true", true);
                    }
                } else {
                    showErrMsg = true;
                }
            } else {
                showErrMsg = true;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            showErrMsg = true;
        }

        if (showErrMsg) {
            showPdfNotGeneratedWarnMsg();
        }

        return file;

    }

    public static String showReport(RadiologyExamRequest radiologyExamRequest,
                                    Long selectedTemplateId, UserWrapper currentUser, String comment,
                                    String reportResult, boolean printWithBozza,
                                    String modyfiedExamRequest, boolean needImages) {
        String randomFileName = null;
        DocumentTemplateWrapper filledTemplate;
        boolean showErrMsg = false;
        try {
            if (!ValidationHelper.isNullOrEmpty(selectedTemplateId)) {
                DocumentTemplate documentTemplate = DaoManager.get(
                        DocumentTemplate.class, new Criterion[]{
                                Restrictions.eq("id", selectedTemplateId)
                        });

                if (!ValidationHelper.isNullOrEmpty(documentTemplate)) {
                    filledTemplate = TemplateToPdfHelper.fillTemplate(
                            documentTemplate, radiologyExamRequest,
                            currentUser, comment, reportResult,
                            modyfiedExamRequest);

                    byte[] data;
                    String watermark = null;
                    if (WaitingListRegistrationStates.DRAFT
                            .equals(radiologyExamRequest
                                    .getWaitingListRegistrationState())
                            || printWithBozza) {
                        watermark = String.format(
                                "file:/%s/resources/images/watermark.png",
                                FileHelper.getLocalDir()).replaceAll("//",
                                "/");
                    }

                    List<RequestAttachedFile> requestAttachedFiles = null;

                    if (needImages) {
                        requestAttachedFiles = DaoManager.load(RequestAttachedFile.class, new Criterion[]{
                                Restrictions.eq("radiologyExamRequest.id", radiologyExamRequest.getId())
                        });
                    }

                    data = TemplateToPdfHelper.convertAndReturnData(documentTemplate, filledTemplate, watermark, requestAttachedFiles);
                    randomFileName = FileHelper
                            .getRandomFileName("1.pdf");

                    FileHelper.writeFileToFolder(randomFileName, new File(
                            FileHelper.getLocalFileDir()), data);

                    RedirectHelper.sendRedirect("/File/" + randomFileName
                            + "?pfdrid_c=true", true);
                } else {
                    showErrMsg = true;
                }
            } else {
                showErrMsg = true;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            showErrMsg = true;
        }

        if (showErrMsg) {
            showPdfNotGeneratedWarnMsg();
        }

        return randomFileName;
    }

    public static String showReport(RadiologyExamRequest radiologyExamRequest,
                                    Long selectedTemplateId, UserWrapper currentUser, String comment,
                                    String reportResult, boolean printWithBozza,
                                    String modyfiedExamRequest) {
        return showReport(radiologyExamRequest, selectedTemplateId, currentUser, comment,
                reportResult, printWithBozza, modyfiedExamRequest, false);
    }

    public static <T extends RadiologyExamRequestBase> String showReport(List<T> radiologyExamRequests, UserWrapper currentUser) {
        String randomFileName = null;
        boolean showErrMsg = false;
        try {
            PDFMergerUtility ut = new PDFMergerUtility();
            for (T radiologyExamRequest : radiologyExamRequests) {
                List<SelectItem> templates = fillTemplates(DocumentGenerationPlaces.RESERVATION,
                        radiologyExamRequest.getSectorId(), null);
                if (!ValidationHelper.isNullOrEmpty(templates)) {
                    DocumentTemplate documentTemplate = DaoManager.get(
                            DocumentTemplate.class, new Criterion[]{
                                    Restrictions.eq("id", templates.get(0).getValue())
                            });
                    List<Long> radExamsIds = new ArrayList<>();

                    if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest
                            .getRadiologyExamRequestItems())) {
                        for (RadiologyExamRequestItemBase reri : radiologyExamRequest
                                .getRadiologyExamRequestItems()) {
                            Long id = reri.getRadiologyExam().getId();
                            radExamsIds.add(id);
                        }
                    }

                    SessionHelper.putIds(radExamsIds, ID_IN_SESSION_FOR_TAGS);
                    DocumentTemplateWrapper filledTemplate = TemplateToPdfHelper.fillTemplate(
                            documentTemplate, radiologyExamRequest, currentUser, null, null, null);

                    byte[] data = TemplateToPdfHelper.convertAndReturnData(
                            documentTemplate, filledTemplate, null);

                    String fileName = FileHelper.writeFileToFolder(FileHelper.getRandomFileName("1.pdf"),
                            new File(FileHelper.getLocalFileDir()), data);
                    ut.addSource(new File(fileName));
                    SessionHelper.removeObject(ID_IN_SESSION_FOR_TAGS);
                }
            }
            randomFileName = FileHelper.getRandomFileName("1.pdf");
            ut.setDestinationFileName(FileHelper.getLocalFileDir() + File.separator + randomFileName);
            ut.mergeDocuments(null);
            RedirectHelper.sendRedirect("/File/" + randomFileName + "?pfdrid_c=true", true);
        } catch (Exception e) {
            LogHelper.log(log, e);
            showErrMsg = true;
        }

        if (showErrMsg) {
            showPdfNotGeneratedWarnMsg();
        }

        return randomFileName;
    }

    public static List<Long> exportRadiologyExamIdsFromCalendar(EventCalendar eventCalendar) {
        List<Long> radiologyExamIds = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(eventCalendar.getDiagnostic())) {
            for (DiagnosticRadiologyExam re : eventCalendar.getDiagnostic().getRadiologyExams()) {
                if (!radiologyExamIds.contains(re.getRadiologyExam().getId())) {
                    radiologyExamIds.add(re.getRadiologyExam().getId());
                }
            }
        }

        return radiologyExamIds;
    }

    public static List<Long> exportRadiologyExamIdsFromRadiologyExamRequest(
            RadiologyExamRequest radiologyExamRequest) {
        List<Long> radiologyExamIds = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest
                .getRadiologyExamRequestItems())) {
            for (RadiologyExamRequestItem reri : radiologyExamRequest
                    .getRadiologyExamRequestItems()) {
                if (!ValidationHelper.isNullOrEmpty(reri.getRadiologyExam())
                        && !radiologyExamIds.contains(reri.getRadiologyExam()
                        .getId())) {
                    radiologyExamIds.add(reri.getRadiologyExam().getId());
                }
            }
        }

        return radiologyExamIds;
    }

    public static void openPdfInNewTab(RadiologyExamRequest radiologyExamRequest) {
        if (validateRequest(radiologyExamRequest)
                && radiologyExamRequest.getRadiologyExamRequestItems().get(0)
                .getFileEntity() != null) {
            try {
                FileEntity fileEntity = radiologyExamRequest
                        .getRadiologyExamRequestItems().get(0).getFileEntity();
                if (!ValidationHelper.isNullOrEmpty(fileEntity)) {
                    createFileInLocalDirIfNeeded(fileEntity);

                    RedirectHelper.sendRedirect("/File/" + fileEntity.getName() + "?pfdrid_c=true", true);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
                GeneralFunctionsHelper.showPdfNotGeneratedWarnMsg();
            }
        }
    }

    public static String getPdfPathToFile(
            RadiologyExamRequest radiologyExamRequest) {
        if (validateRequest(radiologyExamRequest)
                && radiologyExamRequest.getRadiologyExamRequestItems().get(0)
                .getFileEntity() != null) {
            try {
                FileEntity fileEntity = radiologyExamRequest
                        .getRadiologyExamRequestItems().get(0).getFileEntity();
                FileEntity signFileEntity = radiologyExamRequest
                        .getRadiologyExamRequestItems().get(0).getSignFileEntity();
                if (signFileEntity != null) {
                    fileEntity = signFileEntity;
                }
                if (!ValidationHelper.isNullOrEmpty(fileEntity)) {
                    createFileInLocalDirIfNeeded(fileEntity);
                    String url = "/File/" + fileEntity.getName() + "?pfdrid_c=true";

                    return RedirectHelper.createUrl(null, url, true);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
                GeneralFunctionsHelper.showPdfNotGeneratedWarnMsg();
            }
        }
        return null;
    }

    public static void openPdfInNewTab(FileEntity fileEntity) {
        if (!ValidationHelper.isNullOrEmpty(fileEntity)) {
            try {
                if (!ValidationHelper.isNullOrEmpty(fileEntity)) {
                    createFileInLocalDirIfNeeded(fileEntity);

                    RedirectHelper.sendRedirect("/File/" + fileEntity.getName() + "?pfdrid_c=true", true);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
                GeneralFunctionsHelper.showPdfNotGeneratedWarnMsg();
            }
        }
    }

    private static void createFileInLocalDirIfNeeded(FileEntity fileEntity) throws IOException {
        if (FileHelper.exists(FileHelper.getLocalFileDir(),
                fileEntity.getName())) {
            return;
        }
        if (fileEntity.getContent() != null) {
            FileHelper.writeFileToFolder(fileEntity.getName(),
                    new File(FileHelper.getLocalFileDir()),
                    fileEntity.getContent());
        }
        // New logic for entities stored on hard drive
        else if (fileEntity.getPath() != null) {
            Path path = Paths.get(fileEntity.getPath());

            if (Files.exists(path) && !Files.isDirectory(path)) {
                byte[] fileContent = Files.readAllBytes(Paths.get(fileEntity.getPath()));
                FileHelper.writeFileToFolder(fileEntity.getName(),
                        new File(FileHelper.getLocalFileDir()), fileContent);
            } else {
                throw new RuntimeException("File not exist or path not redable.");
            }

        } else {
            throw new RuntimeException("No path and binary data inside fileEntity.");
        }
    }

    public static boolean validateRequest(
            RadiologyExamRequest radiologyExamRequest) {
        return radiologyExamRequest != null
                && !ValidationHelper.isNullOrEmpty(radiologyExamRequest
                .getRadiologyExamRequestItems());
    }

    public static DetachedCriteria getRequestIdsWithDiagnostic(Boolean showFullView) {
        ProjectionList offProviderProjList = Projections.projectionList();
        offProviderProjList.add(Projections.property("rer.id"));

        Class<? extends Entity> clazz = showFullView ? RadiologyExamRequest.class : RadiologyExamRequestShort.class;
        DetachedCriteria dc = DetachedCriteria
                .forClass(clazz, "rer")
                .createAlias("rer.radiologyExamRequestItems", "reri",
                        JoinType.INNER_JOIN)
                .createAlias("reri.radiologyExam", "re", JoinType.INNER_JOIN)
                .createAlias("re.diagnostics", "d", JoinType.INNER_JOIN);

        dc.add(Restrictions.in("d.diagnostic.id", UserHolder.getInstance().getCurrentUser().getDiagnostics()));
        dc.add(Restrictions.in("rer.sector.id", UserHolder.getInstance().getCurrentUser().getSectors()));
        dc.setProjection(offProviderProjList);
        return dc;
    }

    public static DetachedCriteria getRequestIdsWithTags(List<String> tags) {
        ProjectionList offProviderProjList = Projections.projectionList();
        offProviderProjList.add(Projections.property("rer.id"));

        DetachedCriteria dc = DetachedCriteria
                .forClass(RadiologyExamRequest.class, "rer")
                .createAlias("rer.requestTags", "tags",
                        JoinType.INNER_JOIN);

        dc.add(Restrictions.in("tags.tag", tags));
        dc.setProjection(offProviderProjList);
        return dc;
    }
}
