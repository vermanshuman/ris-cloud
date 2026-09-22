package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.WorkingListTags;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestItemBase;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItemShort;
import it.nexera.ris.persistence.beans.entities.domain.RequestForPdfImport;
import it.nexera.ris.web.beans.wrappers.logic.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zefer.pd4ml.PD4ML;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.List;

/**
 * Classe per la conversione da HTML a PDF
 */
public class WorkingListHelper {
    private final static Logger log = LogManager.getLogger(WorkingListHelper.class);

    private static final String DIAGNOSTIC_QUESTION_TITLE = "<strong>Quesito diagnostico: </strong>";

    private static final String RADIOLOGY_NOTE_TITLE = "<strong>Note: </strong>";

    private static final String TD_WITH_STYLE_11 = "<td style=\"padding:2px 5px; border:1px solid #000;font-size: 0.7em;\">";

    private static final String TD_WITH_STYLE_14 = "<td style=\"padding:2px 5px; border:1px solid #000;font-size: 0.85em;\">";

    private static final String TD_WITH_STYLE_14_ROWSPAN_2 = "<td rowspan=\"2\" style=\"padding:2px 5px; border:1px solid #000;font-size: 0.85em;\">";

    private static final String TD_WITH_STYLE_9_COLSPAN_5 = "<td colspan=\"5\" style=\"width: 100%;word-break: break-word;word-wrap: break-word;padding:2px 5px; border:1px solid #000;font-size: 0.6em;\">";

    private WorkingListHelper() {
    }

    public static byte[] convert(List<Event> list, Date date) {
        try {
            String body = FileHelper.readLayoutFile("body.html", "WorkingList");

            if (!ValidationHelper.isNullOrEmpty(body)) {
                body = replaceTags(body, list, date);

                return convertToPDF(body);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    private static byte[] convertToPDF(String body)
            throws InvalidParameterException, IOException {
        if (body == null) {
            return null;
        }

        Long width = 210l;
        Long height = 297l;

        Long marginTop = 3l;
        Long marginBottom = 3l;
        Long marginLeft = 5l;
        Long marginRight = 5l;

        StringReader isr = new StringReader(body);

        PD4ML html = new PD4ML();

        html.useTTF("java:fonts", true);

        html.setPageSize(new Dimension(MMtoDots(height), MMtoDots(width))); // horizontal orientation

        html.setPageInsets(new Insets(MMtoDots(marginTop),
                MMtoDots(marginLeft), MMtoDots(marginBottom),
                MMtoDots(marginRight)));

        html.enableImgSplit(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        html.render(isr, baos);

        try {
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private static int MMtoDots(Long mm) {
        return (int) (mm * 72f / 25.4f);
    }

    public static boolean convertAndViewInTab(List<Event> list, Date date)
            throws IOException {
        byte[] data = convert(list, date);

        if (data != null) {
            String randomFileName = FileHelper
                    .getRandomFileName("1.pdf");

            FileHelper.writeFileToFolder(randomFileName, new File(
                    FileHelper.getLocalFileDir()), data);

            RedirectHelper.sendRedirect("/File/" + randomFileName
                    + "?pfdrid_c=true", true);

            return true;
        }

        return false;
    }

    private static String replaceTags(String source, List<Event> list, Date date) {
        if (!ValidationHelper.isNullOrEmpty(source)) {
            StringBuilder patients = new StringBuilder();
            if (ValidationHelper.isNullOrEmpty(list)) {
                patients.append("<tr><td>");
                patients.append(ResourcesHelper.getValidation("noRecordsFound"));
                patients.append("</td></tr>");
            } else {
                for (Event event : list) {
                    RequestForPdfImport radiologyExamRequest = null;

                    if (event.getRadiologyExamRequestItemId() != null) {
                        List<RequestForPdfImport> requests = null;

                        try {
                            requests =
                                    DaoManager.getSession().createSQLQuery(
                                            "select rad_ex.id                      as id,\n" +
                                                    "       p.NAME                         as name,\n" +
                                                    "       p.surname                      as surname,\n" +
                                                    "       p.birth_date                   as birth_date,\n" +
                                                    "       rad_ex.asap_activity_line      as asap_activity_line,\n" +
                                                    "       rad_ex.asap_sector_desctiption as asap_sector_description,\n" +
                                                    "       rad_it.wl_registration_state   as wl_registration_state,\n" +
                                                    "       rad_it.DIAGNOSTIC_QUESTION     as diagnostic_question,\n" +
                                                    "       LISTAGG(request_note.NOTE, '. ') WITHIN GROUP (ORDER BY request_note.NOTE) AS notes \n" +
                                                    "from RADIOLOGY_EXAM_REQUEST rad_ex\n" +
                                                    "         inner join rad_exam_request_item rad_it on rad_ex.ID = rad_it.RAD_EXAM_REQUEST_ID\n" +
                                                    "         inner join patient p on rad_ex.PATIENT_ID = p.ID " +
                                                    "         left join REQUEST_NOTE request_note on request_note.RAD_EXAM_REQUEST_ID = rad_ex.ID" +
                                                    " where rad_it.id = " + event.getRadiologyExamRequestItemId() +
                                                    " GROUP BY rad_ex.id, p.NAME, p.surname, p.birth_date, rad_ex.asap_activity_line, rad_ex.asap_sector_desctiption, rad_it.wl_registration_state, rad_it.DIAGNOSTIC_QUESTION"
                                    ).addEntity(RequestForPdfImport.class).list();
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }

                        if (!ValidationHelper.isNullOrEmpty(requests)) {
                            radiologyExamRequest = requests.get(0);
                        }
                    }

                    patients.append("<tr>");

                    if (radiologyExamRequest != null
                            && !ValidationHelper.isNullOrEmpty(radiologyExamRequest.getDiagnosticQuestion())) {
                        patients.append(TD_WITH_STYLE_14_ROWSPAN_2);
                    } else {
                        patients.append(TD_WITH_STYLE_14);
                    }

                    patients.append(event.getStartDate() == null ? ""
                            : DateTimeHelper.toStringTime(event.getStartDate()));

                    patients.append("</td>");

                    patients.append(TD_WITH_STYLE_14);

                    patients.append(radiologyExamRequest == null ? ""
                            : radiologyExamRequest.getPatientSurnameName());
                    patients.append("</td>");

                    patients.append(TD_WITH_STYLE_11);

                    patients.append(radiologyExamRequest == null ? ""
                            : DateTimeHelper.toString(radiologyExamRequest.getBirthDate()));

                    patients.append("</td>");

                    patients.append(TD_WITH_STYLE_11);
                    if (radiologyExamRequest != null && radiologyExamRequest.getAsapActivityLine() != null) {
                        patients.append("<strong>").append(radiologyExamRequest.getAsapActivityLine()).append("</strong>");
                        if (radiologyExamRequest.getAsapSectorDescription() != null) {
                            patients.append(" - ");
                        }
                    }

                    patients.append(radiologyExamRequest == null ? ""
                            : radiologyExamRequest.getAsapSectorDescription());
                    patients.append("</td>");

                    patients.append(TD_WITH_STYLE_11);

                    patients.append(radiologyExamRequest == null ? ""
                            : radiologyExamRequest.getWaitingListRegistrationState());

                    patients.append("</td>");

                    patients.append(TD_WITH_STYLE_14);
                    patients.append(event.getExams() == null ? "" : event
                            .getExams());
                    patients.append("</td></tr>");

                    if (radiologyExamRequest != null
                            && !ValidationHelper.isNullOrEmpty(radiologyExamRequest.getDiagnosticQuestion())) {
                        patients.append("<tr>");
                        patients.append(TD_WITH_STYLE_9_COLSPAN_5);
                        patients.append(DIAGNOSTIC_QUESTION_TITLE);
                        patients.append(radiologyExamRequest.getDiagnosticQuestion());
                        if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest.getNotes())) {
                            patients.append("<div>");
                            patients.append(RADIOLOGY_NOTE_TITLE);
                            patients.append(radiologyExamRequest.getNotes());
                            patients.append("</div>");
                        }
                        patients.append("</td></tr>");
                    }
                }
            }

            return source
                    .replaceAll(
                            WorkingListTags.CURRENT_DATE.getValue(),
                            DateTimeHelper.toFormatedString(new Date(),
                                    DateTimeHelper.getDatePattern()))
                    .replaceAll(
                            WorkingListTags.SELECTED_DATE.getValue(),
                            DateTimeHelper.toFormatedString(date,
                                    DateTimeHelper.getDatePattern()))
                    .replaceAll(WorkingListTags.PATIENTS.getValue(),
                            patients.toString())
                    .replaceAll(
                            WorkingListTags.WORKING_PROGRAM_TITLE.getValue(),
                            WorkingListTags.WORKING_PROGRAM_TITLE.toString())
                    .replaceAll(
                            WorkingListTags.WORKING_PROGRAM_PATIENT.getValue(),
                            WorkingListTags.WORKING_PROGRAM_PATIENT.toString())
                    .replaceAll(
                            WorkingListTags.WORKING_PROGRAM_TIME.getValue(),
                            WorkingListTags.WORKING_PROGRAM_TIME.toString())
                    .replaceAll(
                            WorkingListTags.WORKING_PROGRAM_BIRTH_DATE
                                    .getValue(),
                            WorkingListTags.WORKING_PROGRAM_BIRTH_DATE
                                    .toString())
                    .replaceAll(
                            WorkingListTags.WORKING_PROGRAM_SECTOR.getValue(),
                            WorkingListTags.WORKING_PROGRAM_SECTOR.toString())
                    .replaceAll(
                            WorkingListTags.WORKING_PROGRAM_STATE.getValue(),
                            WorkingListTags.WORKING_PROGRAM_STATE.toString())
                    .replaceAll(
                            WorkingListTags.WORKING_PROGRAM_EXAMS.getValue(),
                            WorkingListTags.WORKING_PROGRAM_EXAMS.toString())
                    .replaceAll(
                            WorkingListTags.WORKING_PROGRAM_PRINT_DATE
                                    .getValue(),
                            WorkingListTags.WORKING_PROGRAM_PRINT_DATE
                                    .toString());
        }

        return "";
    }

    public static Class<? extends RadiologyExamRequestItemBase> getActualBean(Boolean isShort) {
        return isShort == null || !isShort ? RadiologyExamRequestItem.class : RadiologyExamRequestItemShort.class;
    }
}
