package it.nexera.ris.web.services.servlets;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.XmlHelper;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.materialized.HistoricalReportMV;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named( "fixXml")
@ViewScoped
public class FixXmlService implements Serializable {

    private static final long serialVersionUID = -6321372828109657557L;

    private final Logger log = LogManager.getLogger(FixXmlService.class);

    private String idsString;

    private String historicalReportIdsString;

    public void fixXmlFile() throws PersistenceBeanException {
        LogHelper.debugInfo(log, "Start xml fixing service.");

        List<Long> fileIds = parseIds(getIdsString());
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Processed IDs: " + fileIds, ""));

        PersistenceSession persistenceSession = null;
        try {
            if (!ValidationHelper.isNullOrEmpty(fileIds)) {
                persistenceSession = new PersistenceSession();
                Session session = persistenceSession.getSession();

                List<FileEntity> fileEntities = ConnectionManager.load(FileEntity.class,
                        new Criterion[]{
                                Restrictions.in("id", fileIds)
                        }, session);
                LogHelper.debugInfo(log, "Loaded files: " + fileEntities.size());

                for (FileEntity fileEntity : fileEntities) {
                    processFile(fileEntity, session);
                }
            }

        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            setIdsString("");
            if (persistenceSession != null) {
                persistenceSession.getSession().close();
            }
        }
    }

    public void generateXmlFile() {
        LogHelper.debugInfo(log, "Start XML generation service");

        try {
            List<Long> historicalReportsId = parseIds(getHistoricalReportIdsString());

            if (!ValidationHelper.isNullOrEmpty(historicalReportsId)) {
                List<HistoricalReportMV> historicalReports = DaoManager.load(
                        HistoricalReportMV.class,
                        new Criterion[]{Restrictions.in("id", historicalReportsId)}
                );

                LogHelper.debugInfo(log, "Loaded Historical reports: " + historicalReports.size());

                for (HistoricalReportMV report : historicalReports) {
                    processHistoricalReport(report);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            setHistoricalReportIdsString("");
        }
        LogHelper.debugInfo(log, "XML generation service finished");
    }

    private void processHistoricalReport(HistoricalReportMV report) throws PersistenceBeanException {
        LogHelper.debugInfo(log, "Processing historical report with ID: " + report.getId());

        PersistenceSession persistenceSession = null;
        try {
            persistenceSession = new PersistenceSession();
            Session session = persistenceSession.getSession();

            String xml = XmlHelper.createCdaXml(report, session);

            FileEntity fileEntity = ConnectionManager.get(FileEntity.class, report.getFileEntityId(), session);
            Path pdfPath = Paths.get(fileEntity.getPath());

            byte[] pdfData = Files.readAllBytes(pdfPath);
            byte[] xmlData = xml.getBytes(StandardCharsets.ISO_8859_1);

            replacePdfWithEmbeddedXml(pdfPath, pdfData, xmlData, fileEntity.getName());

        } catch (Exception e) {
            LogHelper.log(log, e);
            LogHelper.debugInfo(log, "Error processing report with ID: " + report.getId());
        } finally {
            if (persistenceSession != null) {
                persistenceSession.getSession().close();
            }
        }
    }

    private void processFile(FileEntity fileEntity, Session session) {
        LogHelper.debugInfo(log, "Processing file ID: " + fileEntity.getId());

        if (ValidationHelper.isNullOrEmpty(fileEntity.getPath())) {
            LogHelper.debugInfo(log, "Skipping file due to empty path.");
            return;
        }

        try {
            Path pdfPath = Paths.get(fileEntity.getPath());
            byte[] pdfData = Files.readAllBytes(pdfPath);
            byte[] xmlData = extractAndFixXml(pdfPath, session);
            replacePdfWithEmbeddedXml(pdfPath, pdfData, xmlData, fileEntity.getName());
            LogHelper.debugInfo(log, "Successfully processed file: " + fileEntity.getName());

        } catch (Exception e) {
            LogHelper.log(log, e);
            LogHelper.debugInfo(log, "Error processing file ID: " + fileEntity.getId());
        }
    }

    private byte[] extractAndFixXml(Path pdfPath, Session session) throws IOException {
        byte[] rawXmlData = getXmlFileFromPdf(pdfPath.toFile());
        String fixedXml = XmlHelper.escapeHtmlEntities(new String(rawXmlData, StandardCharsets.UTF_8), session);
        return fixedXml.getBytes(StandardCharsets.ISO_8859_1);
    }

    private void replacePdfWithEmbeddedXml(Path pdfPath, byte[] pdfData, byte[] xmlData, String fileName) throws IOException {
        String xmlFileName = FileHelper.getRandomFileName("1.xml");
        byte[] output = inject(pdfData, xmlData, xmlFileName);

        Path tempPdfPath = Paths.get(pdfPath.getParent().toString(), FilenameUtils.removeExtension(fileName) + "_tmp.pdf");
        FileHelper.saveToFile(output, tempPdfPath);

        try {
            Files.delete(pdfPath);
        } catch (Exception e) {
            log.error("Error deleting original PDF: " + pdfPath);
        }

        Files.move(tempPdfPath, pdfPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private byte[] inject(byte[] bytePDF, byte[] byteCDA, String fileName) throws IOException {
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

    private List<Long> parseIds(String idsString) {
        List<Long> idsList = new ArrayList<>();
        if (idsString != null && !idsString.trim().isEmpty()) {
            String[] idsArray = idsString.split(",");
            for (String idStr : idsArray) {
                try {
                    idsList.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException e) {
                    LogHelper.log(log, e);
                }
            }
        }
        return idsList;
    }

    private static byte[] getXmlFileFromPdf(File pdfFile) throws IOException {
        try (PDDocument doc = PDDocument.load(pdfFile)) {
            PDDocumentNameDictionary names = new PDDocumentNameDictionary(doc.getDocumentCatalog());
            PDEmbeddedFilesNameTreeNode efTree = names.getEmbeddedFiles();
            if (efTree != null) {
                Map<String, PDComplexFileSpecification> namesMap = efTree.getNames();
                if (namesMap != null) {
                    for (Map.Entry<String, PDComplexFileSpecification> entry : namesMap.entrySet()) {
                        PDComplexFileSpecification fs = entry.getValue();
                        PDEmbeddedFile embeddedFile = fs.getEmbeddedFile();
                        if (embeddedFile != null) {
                            try (InputStream is = embeddedFile.createInputStream()) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = is.read(buffer)) != -1) {
                                    baos.write(buffer, 0, len);
                                }
                                return baos.toByteArray();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getIdsString() {
        return idsString;
    }

    public void setIdsString(String idsString) {
        this.idsString = idsString;
    }

    public String getHistoricalReportIdsString() {
        return historicalReportIdsString;
    }

    public void setHistoricalReportIdsString(String historicalReportIdsString) {
        this.historicalReportIdsString = historicalReportIdsString;
    }
}
