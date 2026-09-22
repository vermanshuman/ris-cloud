package it.nexera.ris.common.helpers.logic;

import com.hrdo.hl7.HL7Placer;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Diagnostic;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Pacs;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Dcm4cheeHelper implements Serializable {

    private static final long serialVersionUID = -6299627350241433831L;

    private static final Logger dicomErrorLog = CustomLibLoggerFactory.getDicomErrorLogger();

    private static final Logger dicomInfoLog = CustomLibLoggerFactory.getDicomInfoLogger();

    private RadiologyExamRequest requestToSend;

    private List<RadiologyExamRequestItem> requestItemsToSend;

    public void doActionWithExam(List<Long> radExItemIds, Pacs pacs,
                                 boolean isFirst, boolean isPublish, Session session) {
        List<RadiologyExamRequestItem> requestItems = null;
        try {
            if (session == null) {
                session = DaoManager.getSession();
            }

            if (isFirst) {
                LogHelper.log(dicomInfoLog, "First try to send dicom");

                requestItems = ConnectionManager.load(RadiologyExamRequestItem.class,
                        new Criterion[]{
                                Restrictions.in("id", radExItemIds)
                        }, session);
            } else {
                LogHelper.log(dicomInfoLog, "Retrying to send dicom");

                requestItems = ConnectionManager.load(
                        RadiologyExamRequestItem.class,
                        new Criterion[]{
                                Restrictions.in("id", radExItemIds),
                                Restrictions.ne("waitingListRegistrationState",
                                        WaitingListRegistrationStates.ACCEPTED)
                        }, session);
            }
        } catch (HibernateException | IllegalAccessException
                | PersistenceBeanException e1) {
            LogHelper.log(dicomErrorLog, e1);
        }

        if (!ValidationHelper.isNullOrEmpty(requestItems)) {
            RadiologyExamRequest request = null;
            try {
                request = ConnectionManager.get(RadiologyExamRequest.class,
                        requestItems.get(0).getRadiologyExamRequest().getId(), session);
            } catch (HibernateException | InstantiationException
                    | IllegalAccessException e) {
                LogHelper.log(dicomErrorLog, e);
            }

            if (!ValidationHelper.isNullOrEmpty(request)) {
                for (RadiologyExamRequestItem requestItem : requestItems) {
                    List<Diagnostic> diagnostics = null;

                    try {
                        if (!ValidationHelper.isNullOrEmpty(requestItem.getRadiologyExam())) {
                            diagnostics = ConnectionManager.load(Diagnostic.class,
                                    new CriteriaAlias[]{
                                            new CriteriaAlias("radiologyExams", "radiologyExams", JoinType.INNER_JOIN),
                                    }, new Criterion[]{
                                            Restrictions.eq("radiologyExams.radiologyExam.id",
                                                    requestItem.getRadiologyExam().getId())
                                    }, session);
                        }
                    } catch (Exception e1) {
                        LogHelper.log(dicomInfoLog, e1);
                    }

                    if (!ValidationHelper.isNullOrEmpty(requestItem
                            .getRadiologyExam())
                            && !ValidationHelper.isNullOrEmpty(diagnostics)
                            && request.getSector() != null) {
                        RadiologyExam radiologyExam = requestItem
                                .getRadiologyExam();

                        for (Diagnostic diagnostic : diagnostics) {
                            if (diagnostic.getSector() != null
                                    && diagnostic
                                    .getSector()
                                    .getId()
                                    .equals(request.getSector().getId())) {
                                int counter = 3;

                                do {
                                    if (counter == 3) {
                                        LogHelper
                                                .log(dicomInfoLog,
                                                        "Try to "
                                                                + (isPublish ? "publish"
                                                                : "remove")
                                                                + "exam for diagnostic with id = "
                                                                + diagnostic
                                                                .getId()
                                                                .toString()
                                                                + "radiologyExam with id = "
                                                                + radiologyExam
                                                                .getId()
                                                                .toString());
                                    } else {
                                        LogHelper
                                                .log(dicomInfoLog,
                                                        "Retry to "
                                                                + (isPublish ? "publish"
                                                                : "remove")
                                                                + "exam for diagnostic with id = "
                                                                + diagnostic
                                                                .getId()
                                                                .toString()
                                                                + "radiologyExam with id = "
                                                                + radiologyExam
                                                                .getId()
                                                                .toString());
                                    }

                                    if (doActionForItem(radiologyExam, request,
                                            diagnostic, pacs, isPublish) == 0) {
                                        LogHelper
                                                .log(dicomInfoLog,
                                                        "PublishExamForDiagnostic return false");

                                        counter--;
                                        if (counter == 0 && !isPublish) {
                                            try {
                                                request.setWaitingListRegistrationState(WaitingListRegistrationStates.PARTIALLY_ACCEPTED);

                                                if (!ValidationHelper
                                                        .isNullOrEmpty(request
                                                                .getRadiologyExamRequestItems())) {
                                                    for (RadiologyExamRequestItem reri : request
                                                            .getRadiologyExamRequestItems()) {
                                                        List<Diagnostic> reriDiagnostics = null;

                                                        try {
                                                            if (!ValidationHelper
                                                                    .isNullOrEmpty(
                                                                            reri.getRadiologyExam())) {
                                                                reriDiagnostics = ConnectionManager
                                                                        .load(Diagnostic.class,
                                                                                new CriteriaAlias[]{
                                                                                        new CriteriaAlias(
                                                                                                "radiologyExams", "radiologyExams", JoinType.INNER_JOIN)
                                                                                }, new Criterion[]{
                                                                                        Restrictions
                                                                                                .eq("radiologyExams.radiologyExam.id",
                                                                                                reri.getRadiologyExam().getId())
                                                                                }, session);
                                                            }
                                                        } catch (Exception e1) {
                                                            LogHelper.log(
                                                                    dicomInfoLog,
                                                                    e1);
                                                        }

                                                        if (!ValidationHelper
                                                                .isNullOrEmpty(reri
                                                                        .getRadiologyExam())
                                                                && !ValidationHelper
                                                                .isNullOrEmpty(reriDiagnostics)) {
                                                            for (Diagnostic d : reriDiagnostics) {
                                                                if (d.getId()
                                                                        .equals(diagnostic
                                                                                .getId())) {
                                                                    reri.setWaitingListRegistrationState(WaitingListRegistrationStates.PARTIALLY_ACCEPTED);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        if (reri.getWaitingListRegistrationState()
                                                                .equals(WaitingListRegistrationStates.PARTIALLY_ACCEPTED)) {
                                                            this.getRequestItemsToSend()
                                                                    .add(reri);
                                                        }
                                                    }
                                                }

                                                this.setRequestToSend(request);

                                                sendRequestsAndItemsToDatabase(session);
                                            } catch (Exception e) {
                                                LogHelper.log(dicomErrorLog, e);
                                            }
                                        }
                                    } else {
                                        LogHelper
                                                .log(dicomInfoLog,
                                                        "DoActionForDiagnostic for "
                                                                + (isPublish ? "publish"
                                                                : "delete")
                                                                + " exam return true");
                                        break;
                                    }
                                }
                                while (counter != 0);
                            }
                        }
                    } else {
                        LogHelper
                                .log(dicomErrorLog,
                                        "requestItem.getRadiologyExam() is null or "
                                                + "requestItem.getRadiologyExam().getDiagnostics() is null or empty or request.getSector() is null");
                    }
                }
            }
        } else {
            LogHelper.log(dicomInfoLog, "have no requestItems to send");
        }
    }

    private int doActionForItem(RadiologyExam radiologyExam,
                                RadiologyExamRequest request, Diagnostic diagnostic, Pacs pacs,
                                boolean isPublish) {
        HL7Placer hL7Placer = new HL7Placer();
        Properties prop = new Properties();

        String accessNumber = "";

        if (!ValidationHelper.isNullOrEmpty(request
                .getRadiologyExamRequestItems().get(0).getAccessNumberId())
                && !ValidationHelper.isNullOrEmpty(request
                .getRadiologyExamRequestItems().get(0)
                .getAccessNumberCode())
                && !ValidationHelper.isNullOrEmpty(request
                .getRadiologyExamRequestItems().get(0)
                .getAccessNumberYear())) {
            accessNumber = request.getRadiologyExamRequestItems().get(0)
                    .getAccessNumberCode()
                    + request.getRadiologyExamRequestItems().get(0)
                    .getAccessNumberYear()
                    + request.getRadiologyExamRequestItems().get(0)
                    .getAccessNumberId();
        } else {
            LogHelper
                    .log(dicomInfoLog,
                            "_____      something wrong accessNumber is empty             _____");
        }

        LogHelper.log(dicomInfoLog,
                "_____creation of hL7Placer and Properties started_____");

        LogHelper.log(dicomInfoLog, "hl7_server_host=" + pacs.getiPHostPacs());
        prop.setProperty("hl7_server_host", pacs.getiPHostPacs());

        LogHelper.log(dicomInfoLog, "hl7_server_port="
                + pacs.getPortHostPacs().toString());
        prop.setProperty("hl7_server_port", pacs.getPortHostPacs().toString());

        LogHelper.log(dicomInfoLog, "patient_surname="
                + request.getPatient().getSurname());
        prop.setProperty("patient_surname", request.getPatient().getSurname());

        LogHelper.log(dicomInfoLog, "patient_name="
                + request.getPatient().getName());
        prop.setProperty("patient_name", request.getPatient().getName());

        LogHelper.log(dicomInfoLog, "patient_sex="
                + request.getPatient().getSexTypeShortValue());
        prop.setProperty("patient_sex", request.getPatient()
                .getSexTypeShortValue());

        LogHelper.log(
                dicomInfoLog,
                "patient_dob="
                        + DateTimeHelper.ToMySqlString(request.getPatient()
                        .getBirthDate()));
        prop.setProperty("patient_dob", DateTimeHelper.ToMySqlString(request
                .getPatient().getBirthDate()));

        String useHL7ExamPatientId = ResourcesHelper.getProperty("useHL7ExamPatientId");
        String patientId = request.getPatient().getFiscalCode();
        if (Boolean.TRUE.toString().equals(useHL7ExamPatientId)) {
            patientId = String.valueOf(request.getPatient().getId());
        }
        LogHelper.log(dicomInfoLog, "patient_id="
                + patientId);
        prop.setProperty("patient_id", patientId);

        LogHelper.log(dicomInfoLog, "scheduled_procedure_step_id="
                + accessNumber + diagnostic.getId());
        prop.setProperty("scheduled_procedure_step_id", accessNumber
                + diagnostic.getId());

        LogHelper.log(dicomInfoLog,
                "requested_procedure_id=" + radiologyExam.getStrId());
        prop.setProperty("requested_procedure_id", radiologyExam.getStrId());

        LogHelper.log(dicomInfoLog, "requested_procedure_description="
                + radiologyExam.getDescription());
        prop.setProperty("requested_procedure_description",
                radiologyExam.getDescription());

        LogHelper.log(dicomInfoLog, "accession_number=" + accessNumber);
        prop.setProperty("accession_number", accessNumber);

        if (!ValidationHelper
                .isNullOrEmpty(diagnostic.getExamTypeDiagnostica())) {
            LogHelper.log(dicomInfoLog,
                    "station_name=" + diagnostic.getExamTypeDiagnostica());
            prop.setProperty("station_name",
                    diagnostic.getExamTypeDiagnostica());
        } else {
            LogHelper
                    .log(dicomInfoLog,
                            "station_name parameter not set, because \"exam type diagnostica\" is null for diagnostic with id = "
                                    + diagnostic.getId());
        }

        if (!ValidationHelper.isNullOrEmpty(diagnostic.getStudyUID())) {
            LogHelper.log(dicomInfoLog, "diagnostic.getStudyUID() is not null");

            LogHelper.log(dicomInfoLog, "addTemplateStudyUID = 1");
            prop.setProperty("addTemplateStudyUID", "1");

            String templateStudyUID = diagnostic.getStudyUID()
                    + "."
                    + request.getRadiologyExamRequestItems().get(0)
                    .getAccessNumberYear()
                    + request.getRadiologyExamRequestItems().get(0)
                    .getAccessNumberId();

            LogHelper.log(dicomInfoLog, "templateStudyUID = "
                    + templateStudyUID);

            prop.setProperty("templateStudyUID", templateStudyUID);
        } else {
            LogHelper.log(dicomInfoLog, "diagnostic.getStudyUID() is null");

            LogHelper.log(dicomInfoLog, "addTemplateStudyUID = 0");
            prop.setProperty("addTemplateStudyUID", "0");

            LogHelper.log(dicomInfoLog, "templateStudyUID set to empty string");
            prop.setProperty("templateStudyUID", "");
        }

        try {
            if (isPublish) {
                LogHelper
                        .log(dicomInfoLog,
                                "_________________call hL7Placer.publishExam_________________");
                return hL7Placer.publishExam(prop);
            } else {
                LogHelper
                        .log(dicomInfoLog,
                                "_________________call hL7Placer.removeExam__________________");
                return hL7Placer.removeExam(prop);
            }
        } catch (Exception e) {
            LogHelper.log(dicomErrorLog, "Error when"
                    + (isPublish ? "publish" : "delete") + "exam");
            LogHelper.log(dicomErrorLog, "Unable to send message to DMW");
            LogHelper.log(dicomErrorLog, e);
        }
        return 0;
    }

    private void sendRequestsAndItemsToDatabase(Session session) {
        try {
            if (!ValidationHelper.isNullOrEmpty(this.getRequestToSend())) {
                ConnectionManager.save(this.getRequestToSend(), session);
            }
            if (!ValidationHelper.isNullOrEmpty(this.getRequestItemsToSend())) {
                for (RadiologyExamRequestItem reri : this
                        .getRequestItemsToSend()) {
                    ConnectionManager.save(reri, session);
                }
            }
        } catch (HibernateException e) {
            LogHelper.log(dicomErrorLog, e);
        }
    }

    public RadiologyExamRequest getRequestToSend() {
        return requestToSend;
    }

    public void setRequestToSend(RadiologyExamRequest requestToSend) {
        this.requestToSend = requestToSend;
    }

    public List<RadiologyExamRequestItem> getRequestItemsToSend() {
        if (requestItemsToSend == null) {
            requestItemsToSend = new ArrayList<RadiologyExamRequestItem>();
        }
        return requestItemsToSend;
    }

    public void setRequestItemsToSend(
            List<RadiologyExamRequestItem> requestItemsToSend) {
        this.requestItemsToSend = requestItemsToSend;
    }
}
