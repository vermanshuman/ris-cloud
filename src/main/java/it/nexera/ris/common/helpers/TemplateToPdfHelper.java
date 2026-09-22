package it.nexera.ris.common.helpers;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import it.nexera.ris.common.enums.DocumentGenerationTags;
import it.nexera.ris.common.enums.UserPreferenceType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.domain.DocumentTemplate;
import it.nexera.ris.persistence.beans.entities.domain.Patient;
import it.nexera.ris.persistence.beans.entities.domain.RequestAttachedFile;
import it.nexera.ris.persistence.beans.entities.domain.UserPreference;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.web.beans.wrappers.DocumentTemplateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.TemplateEntity;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.zefer.pd4ml.PD4ML;
import org.zefer.pd4ml.PD4PageMark;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

/**
 * Classe per la conversione da HTML a PDF
 */
public class TemplateToPdfHelper {
    private final static Logger log = LogManager.getLogger(TemplateToPdfHelper.class);

    private static final String ID_IN_SESSION_FOR_TAGS = "RadiologyExamsIdsForPrint";

    private TemplateToPdfHelper() {
    }

    public static byte[] convert(DocumentTemplate template, Entity entity,
                                 UserWrapper currentUser, String deleteComment, String reportResult) {
        if (template != null && entity != null) {
            try {

                DocumentTemplateWrapper documentTemplateWrapper = fillTemplate(
                        template, entity, currentUser, deleteComment,
                        reportResult, null);
                return convertToPDF(template, documentTemplateWrapper);

            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return null;

    }

    public static DocumentTemplateWrapper fillTemplate(
            DocumentTemplate template, Entity entity, UserWrapper currentUser,
            String deleteComment, String reportResult,
            String modyfiedExamRequest) throws PersistenceBeanException, IllegalAccessException {
        DocumentTemplateWrapper documentTemplateWrapper = new DocumentTemplateWrapper(
                template);
        TemplateEntity wrappedEntity = new TemplateEntity(entity, currentUser);

        if (template.getFooter()) {
            documentTemplateWrapper.setFooterContent(replaceTags(
                    template.getFooterContent(), wrappedEntity, deleteComment,
                    reportResult, currentUser, modyfiedExamRequest));
        }
        if (template.getHeader()) {
            documentTemplateWrapper.setHeaderContent(replaceTags(
                    template.getHeaderContent(), wrappedEntity, deleteComment,
                    reportResult, currentUser, modyfiedExamRequest));
        }

        documentTemplateWrapper.setBodyContent(replaceTags(
                template.getBodyContent(), wrappedEntity, deleteComment,
                reportResult, currentUser, modyfiedExamRequest));

        return documentTemplateWrapper;
    }

    public static DocumentTemplateWrapper fillTemplate(
            DocumentTemplate template, List<? extends Entity> entities,
            UserWrapper currentUser, String deleteComment) throws PersistenceBeanException, IllegalAccessException {
        DocumentTemplateWrapper documentTemplateWrapper = new DocumentTemplateWrapper(
                template);
        List<TemplateEntity> wrappedEntities = new ArrayList<TemplateEntity>();
        for (Entity entity : entities) {
            wrappedEntities.add(new TemplateEntity(entity, currentUser));
        }

        if (template.getFooter().booleanValue()) {
            documentTemplateWrapper
                    .setFooterContent(replaceTags(template.getFooterContent(),
                            wrappedEntities, deleteComment));
        }
        if (template.getHeader().booleanValue()) {
            documentTemplateWrapper
                    .setHeaderContent(replaceTags(template.getHeaderContent(),
                            wrappedEntities, deleteComment));
        }

        documentTemplateWrapper.setBodyContent(replaceTags(
                template.getBodyContent(), wrappedEntities, deleteComment));

        return documentTemplateWrapper;
    }

    public static DocumentTemplateWrapper fillTemplate(
            DocumentTemplate template, Map<String, String> map) {
        DocumentTemplateWrapper documentTemplateWrapper = new DocumentTemplateWrapper(
                template);

        if (template.getFooter().booleanValue()) {
            documentTemplateWrapper.setFooterContent(replaceTags(
                    template.getFooterContent(), map));
        }
        if (template.getHeader().booleanValue()) {
            documentTemplateWrapper.setHeaderContent(replaceTags(
                    template.getHeaderContent(), map));
        }

        documentTemplateWrapper.setBodyContent(replaceTags(
                template.getBodyContent(), map));

        return documentTemplateWrapper;
    }

    public static Map<String, String> fillStaticTags(Patient patient,
                                                     UserWrapper userWrapper, Map<String, String> map)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (map == null) {
            map = new HashMap<String, String>();
        }

        map = fillPatientInfoTags(patient, map);
        // map = fillOperatorInfoTags(userWrapper, map);
        map = addCurrentDateTag(map);
        map = addPageBreakTag(map);

        return map;
    }

    public static Map<String, String> fillPatientInfoTags(Patient patient,
                                                          Map<String, String> map) {
        if (map == null) {
            map = new HashMap<String, String>();
        }

        for (DocumentGenerationTags tag : DocumentGenerationTags.values()) {
            if (tag.name().toLowerCase().startsWith("patient")) {
                try {
                    Object result = patient.getClass()
                            .getMethod(tag.getGetMethod()).invoke(patient);
                    String resultString = "";
                    if (result != null) {
                        if (result instanceof Date) {
                            resultString = DateTimeHelper
                                    .toString((Date) result);
                        } else {
                            resultString = result.toString();
                        }
                    }
                    map.put(tag.getTag(), resultString);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
        }

        return map;
    }

    public static Map<String, String> fillOperatorInfoTags(
            UserWrapper userWrapper, Map<String, String> map)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (map == null) {
            map = new HashMap<String, String>();
        }

        if (userWrapper.getFirstName() != null) {
            map.put(DocumentGenerationTags.OPERATOR_NAME.getTag(),
                    userWrapper.getFirstName());
        } else {
            map.put(DocumentGenerationTags.OPERATOR_NAME.getTag(), "");
        }

        if (userWrapper.getLastName() != null) {
            map.put(DocumentGenerationTags.OPERATOR_SURNAME.getTag(),
                    userWrapper.getLastName());
        } else {
            map.put(DocumentGenerationTags.OPERATOR_SURNAME.getTag(), "");
        }
        return map;
    }

    public static Map<String, String> addCurrentDateTag(Map<String, String> map) {
        if (map == null) {
            map = new HashMap<String, String>();
        }

        map.put(DocumentGenerationTags.CURRENT_DATE.getTag(),
                DateTimeHelper.toString(new Date()));

        return map;
    }

    public static Map<String, String> addPageBreakTag(Map<String, String> map) {
        if (map == null) {
            map = new HashMap<String, String>();
        }

        map.put(DocumentGenerationTags.PAGE_BREAK.getTag(),
                "<pd4ml:page.break/>");

        return map;
    }

    public static byte[] convertToPDF(DocumentTemplate document,
                                      DocumentTemplateWrapper wrapper) throws InvalidParameterException,
            IOException {
        return convertToPDF(document, wrapper, null);
    }

    public static byte[] convertToPDF(DocumentTemplate document,
                                      DocumentTemplateWrapper wrapper, String watermark)
            throws InvalidParameterException, IOException {
        return convertToPDF(document, wrapper, watermark, null);
    }

    public static byte[] convertToPDF(DocumentTemplate document, DocumentTemplateWrapper wrapper,
                                      String watermark, List<RequestAttachedFile> requestAttachedFiles)
            throws InvalidParameterException, IOException {
        if (wrapper == null || document == null) {
            return null;
        }

        String header = null, footer = null;

        if (document.getHeader() != null && document.getHeader().booleanValue()
                && document.getHeaderContent() != null
                && !document.getHeaderContent().isEmpty()) {
            header = fixTableStyle(wrapper.getHeaderContent());
        }

        if (document.getFooter() != null && document.getFooter().booleanValue()
                && document.getFooterContent() != null
                && !document.getFooterContent().isEmpty()) {
            footer = fixTableStyle(wrapper.getFooterContent());
        }

        Long width = document.getWidth();
        Long height = document.getHeight();

        Long marginTop = document.getMarginTop();
        Long marginBottom = document.getMarginBottom();
        Long marginLeft = document.getMarginLeft();
        Long marginRight = document.getMarginRight();

        if (width <= 0l || height <= 0l) {
            return null;
        }

        if (marginTop < 0l) {
            marginTop = 0l;
        }

        if (marginBottom < 0l) {
            marginBottom = 0l;
        }

        if (marginLeft < 0l) {
            marginLeft = 0l;
        }

        if (marginRight < 0l) {
            marginRight = 0l;
        }

        StringBuilder bodyContent = new StringBuilder(fixTableStyle(wrapper.getBodyContent()));

        if (!ValidationHelper.isNullOrEmpty(requestAttachedFiles)) {
            for (RequestAttachedFile requestAttachedFile : requestAttachedFiles) {
                if (requestAttachedFile.getFileName().endsWith(".pdf")) {
                    continue;
                }
                bodyContent.append("<pd4ml:page.break /> ");

                String imagePath = requestAttachedFile.getFilePath();
                String fileName = new File(imagePath).getName();
                if (!ValidationHelper.isNullOrEmpty(requestAttachedFile.getRotateDegree())) {
                    fileName = rotateImage(requestAttachedFile);
                } else {
                    FileHelper.writeFileToFolder(fileName,
                            new File(FileHelper.getLocalFileDir()),
                            IOUtils.toByteArray(new FileInputStream(new File(imagePath))));
                }

                bodyContent.append("<img src=\"");
                bodyContent.append(RedirectHelper.createUrl(null, "/File/" + fileName, true));
                bodyContent.append("\" ");

                Long footerSize = document.getFooterHeight() == null ? 0 : document.getFooterHeight();
                Long headerSize = document.getHeaderHeight() == null ? 0 : document.getHeaderHeight();
                Double bodyWidth = Double.valueOf(width) - marginLeft - marginRight;
                Double bodyHeight = Double.valueOf(height) - marginTop - marginBottom - footerSize - headerSize;
                double coefficient = 1.8346;

                BufferedImage bufferedImage = ImageIO.read(new File(FileHelper.getLocalFileDir() + File.separator + fileName));
                double imageHeight = bufferedImage.getHeight();
                double imageWidth = bufferedImage.getWidth();
                if (imageWidth > bodyWidth * coefficient || imageHeight > bodyHeight * coefficient) {
                    bodyContent.append(" style=\"");
                    if((imageWidth - (bodyWidth * coefficient)) > (imageHeight - (bodyHeight * coefficient))){
                        bodyContent.append("width: 100%");
                    } else {
                        bodyContent.append("height:");
                        bodyContent.append(bodyHeight * coefficient);
                        bodyContent.append("pt; ");
                    }
                    bodyContent.append("\"");
                }
                bodyContent.append(" >");
            }
        }

        StringReader isr = new StringReader(bodyContent.toString());

        PD4ML html = new PD4ML();

        html.useTTF("java:fonts", true);

        Dimension truePageSize = new Dimension(MMtoDots(width),
                MMtoDots(height));
        html.setPageSize(truePageSize);

        html.setPageInsets(new Insets(MMtoDots(marginTop),
                MMtoDots(marginLeft), MMtoDots(marginBottom),
                MMtoDots(marginRight)));

        html.enableImgSplit(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (header != null) {
            Long headerHeight = document.getHeaderHeight();
            if (headerHeight > 0l) {
                PD4PageMark headerPDF = new PD4PageMark();
                headerPDF.setHtmlTemplate(header);
                headerPDF.setAreaHeight(MMtoDots(headerHeight));
                html.setPageHeader(headerPDF);
            }
        }

        if (footer != null) {
            Long footerHeight = document.getFooterHeight();
            if (footerHeight > 0l) {
                PD4PageMark footerPDF = new PD4PageMark();
                footerPDF.setHtmlTemplate(footer);
                footerPDF.setAreaHeight(MMtoDots(footerHeight));
                if (watermark != null) {
                    footerPDF.setWatermark(
                            // watermark image location URL.
                            // For local images use "file:" protocol i.e.
                            // "file:images/logo.png"
                            watermark,
                            // watermark image position
                            new Rectangle(10, 10,
                                    (int) truePageSize.getWidth(),
                                    (int) truePageSize.getHeight()
                                            - (int) MMtoDots(footerHeight)),
                            // watermark opacity in percents
                            50);
                    html.setPageFooter(footerPDF);
                }
                html.setPageFooter(footerPDF);
            }
        }
        if (watermark != null) {
            PD4PageMark footerPDF = new PD4PageMark();
            footerPDF.setWatermark(
                    // watermark image location URL.
                    // For local images use "file:" protocol i.e. "file:images/logo.png"
                    watermark,
                    // watermark image position
                    new Rectangle(10, 10, (int) truePageSize.getWidth(),
                            (int) truePageSize.getHeight()),
                    // watermark opacity in percents
                    50);
            html.setPageFooter(footerPDF);
        }

        html.render(isr, baos);

        try {
            List<byte[]> list = new ArrayList<>();
            list.add(baos.toByteArray());
            addPdfToByteList(requestAttachedFiles, list);
            return mergePDF(list);
        } catch (Exception e) {
            return null;
        }
    }

    private static void addPdfToByteList(List<RequestAttachedFile> requestAttachedFileList, List<byte[]> list) throws
            IOException {
        if (!ValidationHelper.isNullOrEmpty(requestAttachedFileList)) {
            for (RequestAttachedFile requestAttached : requestAttachedFileList) {
                if (!requestAttached.getFileName().endsWith(".pdf")) {
                    continue;
                }
                File file = new File(requestAttached.getFilePath());
                list.add(Files.readAllBytes(file.toPath()));
            }
        }
    }

    private static byte[] mergePDF(List<byte[]> pdfFilesAsByteArray) throws DocumentException, IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        Document document = null;
        PdfCopy writer = null;

        for (byte[] pdfByteArray : pdfFilesAsByteArray) {

            try {
                PdfReader.unethicalreading = true;
                PdfReader reader = new PdfReader(pdfByteArray);
                int numberOfPages = reader.getNumberOfPages();

                if (document == null) {
                    document = new Document(reader.getPageSizeWithRotation(1));
                    writer = new PdfCopy(document, outStream); // new
                    document.open();
                }
                PdfImportedPage page;
                for (int i = 0; i < numberOfPages; ) {
                    ++i;
                    page = writer.getImportedPage(reader, i);
                    writer.addPage(page);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        document.close();
        outStream.close();
        return outStream.toByteArray();

    }

    private static String rotateImage(RequestAttachedFile requestAttachedFile) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(requestAttachedFile.getFilePath()));
        int degree = requestAttachedFile.getRotateDegree().intValue();
        AffineTransform affineTransform = new AffineTransform();

        double rotateDegree = 0;

        switch (degree) {
            case 90:
                rotateDegree = Math.PI / 2;
                break;
            case 180:
                rotateDegree = Math.PI;
                break;
            case 270:
                rotateDegree = 3 * Math.PI / 2;
                break;
        }

        if (degree == 90 || degree == 270) {
            affineTransform.translate(bufferedImage.getHeight() >> 1, bufferedImage.getWidth() >> 1);
            affineTransform.rotate(rotateDegree);
            affineTransform.translate(-bufferedImage.getWidth() >> 1, -bufferedImage.getHeight() >> 1);

        } else if (degree == 180) {
            affineTransform.translate(bufferedImage.getWidth() >> 1, bufferedImage.getHeight() >> 1);
            affineTransform.rotate(rotateDegree);
            affineTransform.translate(-bufferedImage.getWidth() >> 1, -bufferedImage.getHeight() >> 1);

        } else {
            affineTransform.rotate(rotateDegree);
        }

        AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage result;

        if (degree == 90 || degree == 270) {
            result = new BufferedImage(bufferedImage.getHeight(), bufferedImage.getWidth(), bufferedImage.getType());

        } else {
            result = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
        }

        affineTransformOp.filter(bufferedImage, result);

        String fileName = new File(requestAttachedFile.getFilePath()).getName();

        String randomFileName = FileHelper.getRandomFileName(fileName);
        ImageIO.write(result, fileName.substring(fileName.lastIndexOf(".") + 1), new File(FileHelper.getLocalFileDir() + File.separator + randomFileName));

        return randomFileName;
    }

    public static String fixTableStyle(String html) {
        if (html != null) {
            String firstStyle = "";
            boolean deliveryTableFound = false;
            int startIndex = 0;
            StringBuffer newHtml = new StringBuffer(html);

            while (true) {
                int spanNum = newHtml.indexOf("<span", startIndex);
                int tableNum = newHtml.indexOf("<table ", startIndex);

                startIndex = tableNum + 7;//7 is length of "<table" + 'space'

                if (tableNum == -1) {
                    break;
                }

                StringBuffer style = new StringBuffer("style=\"");

                if (spanNum != -1 && tableNum != -1) {
                    while (true) {
                        int styleNum = newHtml.substring(0, tableNum).indexOf(
                                "style=\"", spanNum);
                        if (!deliveryTableFound && styleNum == -1){
                            int theadIndex = newHtml.substring(tableNum).indexOf(
                                    "><thead>");
                            if(theadIndex > -1){
                                String tableString = newHtml.substring(tableNum, tableNum + theadIndex);
                                if(tableString.contains("class=\"deliveryDose\"")){
                                    deliveryTableFound = true;
                                    newHtml.insert(tableNum + 7, firstStyle);

                                }
                            }
                        }
                        if (styleNum == -1) {
                            break;
                        }

                        int closeStyleNum = newHtml.indexOf("\">", styleNum);
                        spanNum = closeStyleNum;
                        style.append(newHtml.substring(styleNum + 7,//7 is length of "style=""
                                closeStyleNum));
                        style.append("; ");
                    }

                    style.append("\" ");
                    newHtml.insert(tableNum + 7, style);
                    if(StringUtils.isNotBlank(style) && !style.toString().trim().equals("style=\"\"")){
                        firstStyle = style.toString();
                    }
                }
            }

            return newHtml.toString();
        } else {
            return html;
        }
    }

    private static int MMtoDots(Long mm) {
        return (int) (mm * 72f / 25.4f);
    }

    public static boolean convertAndSend(DocumentTemplate template,
                                         Entity entity, UserWrapper currentUser, String deleteComment,
                                         String reportResult) throws IOException {
        if (template != null && entity != null) {
            String fileName = null;
            if (!ValidationHelper.isNullOrEmpty(template.getName())) {
                fileName = template.getName() + ".pdf";
            } else {
                fileName = ResourcesHelper.getString("pdfTemplateDefaultName")
                        + ".pdf";
            }

            byte[] data = convert(template, entity, currentUser, deleteComment,
                    reportResult);

            if (data != null) {
                FileHelper.sendFile(fileName, data);
                return true;
            }

            return false;
        }

        return false;
    }

    public static boolean convertAndSend(DocumentTemplate template,
                                         DocumentTemplateWrapper wrapper, String watermark)
            throws IOException {
        if (template != null && wrapper != null) {
            String fileName = null;
            if (!ValidationHelper.isNullOrEmpty(template.getName())) {
                fileName = template.getName() + ".pdf";
            } else {
                fileName = ResourcesHelper.getString("pdfTemplateDefaultName")
                        + ".pdf";
            }

            byte[] data = convertToPDF(template, wrapper, watermark);

            if (data != null) {
                FileHelper.sendFile(fileName, data);
                return true;
            }

            return false;
        }

        return false;
    }

    public static byte[] convertAndReturnData(DocumentTemplate template,
                                              DocumentTemplateWrapper wrapper, String watermark)
            throws IOException {
        return convertAndReturnData(template, wrapper, watermark, null);
    }

    public static byte[] convertAndReturnData(DocumentTemplate template,
                                              DocumentTemplateWrapper wrapper, String watermark, List<RequestAttachedFile> requestAttachedFiles)
            throws IOException {
        if (template != null && wrapper != null) {
            byte[] data = convertToPDF(template, wrapper, watermark, requestAttachedFiles);

            if (data != null) {
                return data;
            }

            return null;
        }

        return null;
    }

    private static String replaceTags(String source, TemplateEntity entity,
                                      String deleteComment, String reportResult, UserWrapper currentUser,
                                      String modyfiedExamRequest) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(source)) {
            String result = source;

            for (DocumentGenerationTags tag : DocumentGenerationTags.values()) {
                if (tag.equals(DocumentGenerationTags.PAGE_BREAK)) {
                    result = result.replace(tag.getTag(),
                            "<pd4ml:page.break/>");
                }

                if (tag.equals(DocumentGenerationTags.CANCEL_COMMENT)) {
                    if (deleteComment != null) {
                        result = result.replace(tag.getTag(), deleteComment);
                    } else {
                        result = result.replace(tag.getTag(), "");
                    }
                }

                if (tag.equals(DocumentGenerationTags.USER_PERFORM_DELETE)) {
                    if (currentUser != null) {
                        result = result.replace(tag.getTag(),
                                currentUser.getFullname());
                    } else {
                        result = result.replace(tag.getTag(), "");
                    }
                }

                if (tag.equals(DocumentGenerationTags.REPORT_RESULT)) {
                    if (reportResult != null) {
                        result = result.replace(tag.getTag(), reportResult);
                    } else {
                        result = result.replace(tag.getTag(), "");
                    }
                }

                if (tag.equals(DocumentGenerationTags.OPERATOR_REFERRING_DICTION)) {
                    result = result.replace(tag.getTag(), getReferringDiction(currentUser));
                }

                if (tag.equals(DocumentGenerationTags.EXAMS_LIST_DESCRIPTION)) {
                    if (modyfiedExamRequest != null) {
                        result = result.replace(tag.getTag(), modyfiedExamRequest);
                    } else {
                        if (!ValidationHelper.isNullOrEmpty(SessionHelper
                                .getIds(ID_IN_SESSION_FOR_TAGS))) {
                            List<Long> ids = SessionHelper
                                    .getIds(ID_IN_SESSION_FOR_TAGS);

                            try {
                                List<RadiologyExam> radiologyExams = DaoManager
                                        .load(RadiologyExam.class,
                                                new Criterion[]{
                                                        Restrictions.in("id", ids)
                                                });
                                if (!ValidationHelper
                                        .isNullOrEmpty(radiologyExams)) {
                                    StringBuilder sb = new StringBuilder();
                                    int restSize = radiologyExams.size();
                                    for (RadiologyExam re : radiologyExams) {
                                        sb.append(re.getDescription());
                                        if (restSize != 1) {
                                            sb.append(", ");
                                            --restSize;
                                        }
                                    }
                                    result = result.replace(tag.getTag(),
                                            sb.toString());
                                }
                            } catch (Exception e) {
                                LogHelper.log(log, e);
                            }
                        } else {
                            result = result.replace(tag.getTag(), "");
                        }
                    }
                } else {
                    try {
                        if (result.contains(tag.getTag())) {
                            String replacement = entity.invokeGetMethod(tag
                                    .getGetMethod());
                            if (ValidationHelper.isNullOrEmpty(replacement)) {
                                replacement = "";
                            }
                            result = result.replace(tag.getTag(),
                                    replacement);
                        }
                    } catch (Throwable e) {
                    }
                }
            }

            return result;
        }

        return "";
    }

    private static String getReferringDiction(UserWrapper user) throws PersistenceBeanException, IllegalAccessException {
        String referringDiction = "";
        if (user != null) {
            UserPreference userPreference = user.getUserPreference(
                    UserPreferenceType.REFERRING_DICTION, DaoManager.getSession());
            if (userPreference != null && userPreference.getReferringDiction() != null) {
                referringDiction = userPreference.getReferringDiction();
            }
        }
        return referringDiction;
    }

    private static String replaceTags(String source, Map<String, String> map) {
        if (!ValidationHelper.isNullOrEmpty(source)
                && !ValidationHelper.isNullOrEmpty(map)) {
            String result = source;

            for (Entry<String, String> entry : map.entrySet()) {
                if (result.contains(entry.getKey())) {
                    if (entry.getKey() != null) {
                        String screenedTag = entry.getKey().replaceAll("\\(",
                                "\\\\(");
                        screenedTag = screenedTag.replaceAll("\\)", "\\\\)");
                        result = result.replaceAll(screenedTag,
                                entry.getValue());
                    }
                }
            }

            return result;
        }

        return "";
    }

    public static String replaceMailTags(String source, TemplateEntity entity) {
        if (!ValidationHelper.isNullOrEmpty(source)) {
            String result = source;

            for (MailHelper.EmailTag tag : MailHelper.EmailTag.values()) {
                if (tag.equals(MailHelper.EmailTag.PAGE_BREAK)) {
                    result = result.replace(tag.getTag(),
                            "<pd4ml:page.break/>");
                } else {
                    try {
                        if (result.contains(tag.getTag())) {
                            String replacement = entity.invokeGetMethod(tag.getMethod());

                            if (ValidationHelper.isNullOrEmpty(replacement)) {
                                replacement = "";
                            }

                            result = result.replace(tag.getTag(), replacement);
                        }
                    } catch (Throwable e) {
                        result = result.replace(tag.getTag(), "");
                    }
                }
            }

            return result;
        }

        return "";
    }

    private static String replaceTags(String source,
                                      List<TemplateEntity> entities, String deleteComment)
            throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(source)) {
            String result = source;

            for (DocumentGenerationTags tag : DocumentGenerationTags.values()) {
                if (tag.equals(DocumentGenerationTags.PAGE_BREAK)) {
                    result = result.replaceAll(tag.getTag(),
                            "<pd4ml:page.break/>");
                }
                if (tag.equals(DocumentGenerationTags.CANCEL_COMMENT)) {
                    if (deleteComment != null) {
                        result = result.replaceAll(tag.getTag(), deleteComment);
                    }
                } else if (tag.equals(DocumentGenerationTags.OPERATOR_REFERRING_DICTION)) {
                    UserWrapper currentUser = UserHolder.getInstance().getCurrentUser();
                    result = result.replaceAll(tag.getTag(), getReferringDiction(currentUser));
                } else {
                    try {
                        if (result.contains(tag.getTag())) {
                            StringBuilder replacement = new StringBuilder("");
                            for (TemplateEntity entity : entities) {
                                replacement.append(entity.invokeGetMethod(tag
                                        .getGetMethod()));
                                break;
                            }
                            if (ValidationHelper.isNullOrEmpty(replacement)) {
                                replacement = new StringBuilder("");
                            }
                            result = result.replaceAll(tag.getTag(),
                                    replacement.toString());
                        }
                    } catch (Throwable e) {
                    }
                }
            }

            return result;
        }

        return "";
    }
}
