package it.nexera.ris.common.helpers.logic.hl7;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.datatype.XAD;
import ca.uhn.hl7v2.model.v26.segment.*;
import ca.uhn.hl7v2.parser.PipeParser;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.common.FileValidation;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class BaseHl7MessageHelper {

    protected static final Logger hl7ErrorLog = CustomLibLoggerFactory.getHl7ErrorLogger();

    protected static final Logger hl7InfoLog = CustomLibLoggerFactory.getHl7InfoLogger();

    protected static final Logger hl7MessageInfoLog = CustomLibLoggerFactory.getHl7MessageInfoLoger();

    public static final String OK_RESPONSE = "AA";

    // Generate sectors block

    protected static boolean fillMSH(MSH msh, String messageCode,
                                     String messageType, String versionId, boolean isRepository) throws DataTypeException {
        return fillMSH(msh, messageCode, messageType, versionId, isRepository, "RIS");
    }

    protected static boolean fillMSH(MSH msh, String messageCode,
                                     String messageType, String versionId, boolean isRepository, String sendingApplication) throws DataTypeException {
        return fillMSH(msh, messageCode, messageType, versionId, isRepository, false,sendingApplication, null);
    }

    protected static boolean fillMSH(MSH msh, String messageCode,
                                     String messageType, String versionId, boolean isRepository, boolean isReport,
                                     String sendingApplication, String hospitalName) throws DataTypeException {
        if (msh != null && !ValidationHelper.isNullOrEmpty(messageCode)
                && !ValidationHelper.isNullOrEmpty(messageType)
                && !ValidationHelper.isNullOrEmpty(versionId)) {
            msh.getMsh1_FieldSeparator().setValue("|");
            msh.getMsh2_EncodingCharacters().setValue("^~\\&");
            msh.getMsh3_SendingApplication().getHd1_NamespaceID()
                    .setValue(sendingApplication);
            msh.getMsh4_SendingFacility().getHd1_NamespaceID()
                    .setValue("NEXERA");
            if(isReport){
                msh.getMsh5_ReceivingApplication().getHd1_NamespaceID()
                        .setValue("ASAP_arc");
                msh.getMsh6_ReceivingFacility().getHd1_NamespaceID()
                        .setValue("NEXERA");
            } else if(isRepository) {
                msh.getMsh5_ReceivingApplication().getHd1_NamespaceID()
                        .setValue("CDR");
                msh.getMsh6_ReceivingFacility().getHd1_NamespaceID()
                        .setValue("UNIDOC");
            }else {
                msh.getMsh5_ReceivingApplication().getHd1_NamespaceID()
                        .setValue("Asap_sio");
                msh.getMsh6_ReceivingFacility().getHd1_NamespaceID()
                        .setValue("NEXERA");
            }

            if(isReport)
                msh.getMsh7_DateTimeOfMessage().setValue(DateTimeHelper.toFormatedString(DateTimeHelper.getNow(),
                        DateTimeHelper.getDatePatternFoHL7()));
            else
                msh.getMsh7_DateTimeOfMessage().setValue(DateTimeHelper.getNow());
            msh.getMsh9_MessageType().getMsg1_MessageCode()
                    .setValue(messageCode);
            msh.getMsh9_MessageType().getMsg2_TriggerEvent()
                    .setValue(messageType);
            msh.getMsh10_MessageControlID().setValue(
                    UUID.randomUUID().toString().substring(0, 20));
            msh.getMsh11_ProcessingID().getPt1_ProcessingID().setValue("P");
            msh.getMsh12_VersionID().getVid1_VersionID().setValue(versionId);
            if(isReport && StringUtils.isNotBlank(hospitalName))
                msh.getMsh22_SendingResponsibleOrganization().getXon1_OrganizationName().setValue(hospitalName);
            return true;
        } else {
            return false;
        }
    }

    protected static boolean fillMSH(ca.uhn.hl7v2.model.v25.segment.MSH msh,
                                     String messageCode, String messageType, String versionId)
            throws DataTypeException {
        if (msh != null && !ValidationHelper.isNullOrEmpty(messageCode)
                && !ValidationHelper.isNullOrEmpty(messageType)
                && !ValidationHelper.isNullOrEmpty(versionId)) {
            msh.getMsh1_FieldSeparator().setValue("|");
            msh.getMsh2_EncodingCharacters().setValue("^~\\&");
            msh.getMsh3_SendingApplication().getHd1_NamespaceID()
                    .setValue("RIS");
            msh.getMsh4_SendingFacility().getHd1_NamespaceID()
                    .setValue("NEXERA");
            msh.getMsh5_ReceivingApplication().getHd1_NamespaceID()
                    .setValue("Asap_sio");
            msh.getMsh6_ReceivingFacility().getHd1_NamespaceID()
                    .setValue("NEXERA");
            msh.getMsh7_DateTimeOfMessage().getTime()
                    .setValue(DateTimeHelper.getNow());
            msh.getMsh9_MessageType().getMsg1_MessageCode()
                    .setValue(messageCode);
            msh.getMsh9_MessageType().getMsg2_TriggerEvent()
                    .setValue(messageType);
            msh.getMsh10_MessageControlID().setValue(
                    UUID.randomUUID().toString().substring(0, 20));
            msh.getMsh11_ProcessingID().getPt1_ProcessingID().setValue("P");
            msh.getMsh12_VersionID().getVid1_VersionID().setValue(versionId);

            return true;
        } else {
            return false;
        }
    }

    protected static boolean fillPID(PID pid, Patient patient, boolean isRepository)
            throws HL7Exception {
        return fillPID(pid, patient, isRepository, false);
    }
    protected static boolean fillPID(PID pid, Patient patient, boolean isRepository, boolean isReport)
            throws HL7Exception {
        if (pid != null && !ValidationHelper.isNullOrEmpty(patient)) {
            if(isReport){
                pid.getPid1_SetIDPID().setValue("1");
                String externalPatientId = null;
                if(StringUtils.isNotBlank(patient.getExternalPatientId()))
                    externalPatientId = patient.getExternalPatientId();
                else if(StringUtils.isNotBlank(patient.getFiscalCode()))
                    externalPatientId = patient.getFiscalCode();
                if(StringUtils.isNotBlank(externalPatientId))
                    pid.getPid2_PatientID().getCx1_IDNumber().setValue(externalPatientId);
            }
            pid.getPid3_PatientIdentifierList(0).getCx1_IDNumber()
                    .setValue(patient.getFiscalCode());
            if(isRepository) {
                pid.getPid3_PatientIdentifierList(0).getCx4_AssigningAuthority().getHd1_NamespaceID()
                        .setValue("RIS");
                pid.getPid3_PatientIdentifierList(0).getCx5_IdentifierTypeCode()
                        .setValue("RIS");
                if (!ValidationHelper.isNullOrEmpty(patient.getExternalPatientId())) {
                    pid.getPid3_PatientIdentifierList(1).getCx1_IDNumber()
                            .setValue(patient.getExternalPatientId());
                }
                pid.getPid3_PatientIdentifierList(1).getCx4_AssigningAuthority().getHd1_NamespaceID()
                        .setValue("XMPI");
                pid.getPid3_PatientIdentifierList(1).getCx5_IdentifierTypeCode()
                        .setValue("XMPI");
                pid.getPid3_PatientIdentifierList(2).getCx1_IDNumber()
                        .setValue(patient.getFiscalCode());
                pid.getPid3_PatientIdentifierList(2).getCx4_AssigningAuthority().getHd1_NamespaceID()
                        .setValue("CF");
                pid.getPid3_PatientIdentifierList(2).getCx5_IdentifierTypeCode()
                        .setValue("NNITA");
                XAD xad =  pid.insertPid11_PatientAddress(0);
                xad.getXad1_StreetAddress().getSad1_StreetOrMailingAddress().setValue(patient.getAddress());
                if (!ValidationHelper.isNullOrEmpty(patient.getCity())) {
                    xad.getCity().setValue(patient.getCity().getDescription());
                }
                if (!ValidationHelper.isNullOrEmpty(patient.getCap())) {
                    xad.getCity().setValue(patient.getCap());
                }
                pid.getPid18_PatientAccountNumber().getCx1_IDNumber().setValue(patient.getFiscalCode());
                pid.getPid30_PatientDeathIndicator().setValue("N");
                pid.getPid31_IdentityUnknownIndicator().setValue("N");
            }
            pid.getPid5_PatientName(0).getXpn1_FamilyName().getFn1_Surname()
                    .setValue(patient.getSurname());
            pid.getPid5_PatientName(0).getXpn2_GivenName()
                    .setValue(patient.getName());
            if(isReport){
                pid.getPid7_DateTimeOfBirth().setValue(
                        DateTimeHelper.toFormatedString(patient.getBirthDate(), DateTimeHelper.XML_SHORT_DATE_PATTERN));
            }else
                pid.getPid7_DateTimeOfBirth().setValue(patient.getBirthDate());

            // This type of setting use because of the absence of a contest in
            // some cases
            String sexType = null;
            if (patient.getSexType() != null) {
                switch (patient.getSexType()) {
                    case MALE:
                        if(isRepository || isReport)
                            sexType = "M";
                        else
                            sexType = "MALE";
                        break;
                    case FEMALE:
                        if(isRepository || isReport)
                            sexType = "F";
                        else
                            sexType = "FEMALE";
                        break;

                    case NOT_IDENTIFY:
                        sexType = "NOT_IDENTIFY";
                        break;

                    default:
                        break;
                }
            }
            pid.getPid8_AdministrativeSex().setValue(sexType);

            return true;
        } else {
            return false;
        }
    }

    protected static boolean fillPID(ca.uhn.hl7v2.model.v25.segment.PID pid,
                                     Patient patient) throws DataTypeException {
        if (pid != null && !ValidationHelper.isNullOrEmpty(patient)) {
            pid.getPid3_PatientIdentifierList(0).getCx1_IDNumber()
                    .setValue(patient.getFiscalCode());
            pid.getPid5_PatientName(0).getXpn1_FamilyName().getFn1_Surname()
                    .setValue(patient.getSurname());
            pid.getPid5_PatientName(0).getXpn2_GivenName()
                    .setValue(patient.getName());
            pid.getPid7_DateTimeOfBirth().getTime()
                    .setValue(patient.getBirthDate());

            // This type of setting use because of the absence of a contest in
            // some cases
            String sexType = null;
            if (patient.getSexType() != null) {
                switch (patient.getSexType()) {
                    case MALE:
                        sexType = "MALE";
                        break;
                    case FEMALE:
                        sexType = "FEMALE";
                        break;

                    case NOT_IDENTIFY:
                        sexType = "NOT_IDENTIFY";
                        break;

                    default:
                        break;
                }
            }
            pid.getPid8_AdministrativeSex().setValue(sexType);

            return true;
        } else {
            return false;
        }
    }

    protected static boolean fillPV1(PV1 pv1, RadiologyExamRequest request,
                                     Patient patient, boolean isRepository, boolean isORM) throws DataTypeException {
        return fillPV1(pv1, request, patient, isRepository, false, null, isORM);
    }
    protected static boolean fillPV1(PV1 pv1, RadiologyExamRequest request,
                                     Patient patient, boolean isRepository, boolean isReport, String consultingDoctor, boolean isORM) throws DataTypeException {
        return fillPV1(pv1, request, patient, isRepository, isReport, false, null, consultingDoctor, null, isORM);
    }
    protected static boolean fillPV1(PV1 pv1, RadiologyExamRequest request,
                                     Patient patient, boolean isRepository, boolean isReport, boolean isAPi,
                                     FileEntity fileEntity, String consultingDoctor, Session session, boolean isORM) throws DataTypeException {
        if(pv1 != null && isRepository){
            pv1.getPv12_PatientClass().setValue("O");
            pv1.getPv13_AssignedPatientLocation().getPl1_PointOfCare()
                    .setValue("OSPEDALE");

            if(!ValidationHelper.isNullOrEmpty(request.getHospital())){
                pv1.getPv13_AssignedPatientLocation().getPl4_Facility()
                        .getHd1_NamespaceID()
                        .setValue(request.getHospital().getCode());
            }
            pv1.getPv13_AssignedPatientLocation().getPl9_LocationDescription()
                    .setValue(request.getAsapSectorDescription());
            if (isORM) {
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getSector())
                        && !ValidationHelper.isNullOrEmpty(request.getSector().getCodeAffinityDomain())){
                    pv1.getPv110_HospitalService().setValue(request.getSector().getCodeAffinityDomain());
                }else {
                    pv1.getPv110_HospitalService().setValue("AD_PSC069");
                }
            } else {
                if(!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request.getExamType())
                        && !ValidationHelper.isNullOrEmpty(request.getExamType().getCodeAffinityDomain())){
                    pv1.getPv110_HospitalService().setValue(request.getExamType().getCodeAffinityDomain());
                }else {
                    pv1.getPv110_HospitalService().setValue("AD_PSC069");
                }
            }
            pv1.getPv151_VisitIndicator().setValue("V");
            if((isAPi || isRepository) && ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                    .getValue() != null && Boolean.parseBoolean(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                    .getValue()) && !ValidationHelper.isNullOrEmpty(fileEntity)){
                List<FileValidation> fileValidation = ConnectionManager.load(FileValidation.class, new CriteriaAlias[]{
                }, new Criterion[]{
                        Restrictions.eq("fileEntityId", fileEntity.getId())
                }, session);
                if(!ValidationHelper.isNullOrEmpty(fileValidation)){
                    String workflowInstanceId = fileValidation.get(0).getWorkflowInstanceId();
                    if(StringUtils.isNotBlank(workflowInstanceId))
                        pv1.getPv150_AlternateVisitID().getCx1_IDNumber().setValue(workflowInstanceId);
                }
            }
            return true;
        }else if (pv1 != null && isReport){
            pv1.getPv11_SetIDPV1().setValue("1");
            if(!ValidationHelper.isNullOrEmpty(consultingDoctor)){
                pv1.getPv19_ConsultingDoctor(0).getXcn1_IDNumber().setValue(consultingDoctor);
                pv1.getPv119_VisitNumber().getCx1_IDNumber().setValue(consultingDoctor);
            }

            return true;
        }else if (pv1 != null
                && !ValidationHelper.isNullOrEmptyMultiple(
                request.getHospital(), request.getAsapSectorCode(),
                request.getAsapSectorDescription(),
                request.getHl7FieldsFromAsap(), patient)
                && !ValidationHelper.isNullOrEmptyMultiple(request
                .getHospital().getCode(), request
                .getHl7FieldsFromAsap().getDoctorFiscalCode(), request
                .getHl7FieldsFromAsap().getDoctorName(), request
                .getHl7FieldsFromAsap().getDoctorSurname())) {
            pv1.getPv12_PatientClass().setValue("E");
            pv1.getPv13_AssignedPatientLocation().getPl1_PointOfCare()
                    .setValue(request.getAsapSectorCode());
            pv1.getPv13_AssignedPatientLocation().getPl9_LocationDescription()
                    .setValue(request.getAsapSectorDescription());
            pv1.getPv13_AssignedPatientLocation().getPl4_Facility()
                    .getHd1_NamespaceID()
                    .setValue(request.getHospital().getCode());
            pv1.getPv18_ReferringDoctor(0)
                    .getXcn1_IDNumber()
                    .setValue(
                            request.getHl7FieldsFromAsap()
                                    .getDoctorFiscalCode());
            pv1.getPv18_ReferringDoctor(0)
                    .getXcn2_FamilyName()
                    .getFn1_Surname()
                    .setValue(request.getHl7FieldsFromAsap().getDoctorSurname());
            pv1.getPv18_ReferringDoctor(0).getXcn3_GivenName()
                    .setValue(request.getHl7FieldsFromAsap().getDoctorName());
            // pv11
            pv1.getPv151_VisitIndicator().setValue("V");

            return true;
        } else if (pv1 != null && !ValidationHelper.isNullOrEmptyMultiple(
                request.getHospital(), request.getAsapSectorCode(),
                request.getAsapSectorDescription(),
                request.getHospital().getCode(), patient)) {
            pv1.getPv12_PatientClass().setValue("E");
            pv1.getPv13_AssignedPatientLocation().getPl1_PointOfCare()
                    .setValue(request.getAsapSectorCode());
            pv1.getPv13_AssignedPatientLocation().getPl9_LocationDescription()
                    .setValue(request.getAsapSectorDescription());
            pv1.getPv13_AssignedPatientLocation().getPl4_Facility()
                    .getHd1_NamespaceID()
                    .setValue(request.getHospital().getCode());
            pv1.getPv151_VisitIndicator().setValue("V");
            if(!ValidationHelper.isNullOrEmpty(request.getSectorId())) {
                pv1.getPv110_HospitalService().setValue(String.valueOf(request.getSector().getCode()));
            }
            return true;
        } else {
            return false;
        }
    }

    protected static boolean fillPV1(ca.uhn.hl7v2.model.v25.segment.PV1 pv1,
                                     RadiologyExamRequest request, Patient patient)
            throws DataTypeException {
        if (pv1 != null
                && !ValidationHelper.isNullOrEmpty(request.getHospital())
                && !ValidationHelper.isNullOrEmpty(request.getHospital()
                .getCode())
                && !ValidationHelper.isNullOrEmpty(request.getAsapSectorCode())
                && !ValidationHelper.isNullOrEmpty(request
                .getAsapSectorDescription())
                && !ValidationHelper.isNullOrEmpty(patient)
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap())
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap().getDoctorFiscalCode())
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap().getDoctorName())
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap().getDoctorSurname())) {
            pv1.getPv12_PatientClass().setValue("E");
            pv1.getPv13_AssignedPatientLocation().getPl1_PointOfCare()
                    .setValue(request.getAsapSectorCode());
            pv1.getPv13_AssignedPatientLocation().getPl9_LocationDescription()
                    .setValue(request.getAsapSectorDescription());
            pv1.getPv13_AssignedPatientLocation().getPl4_Facility()
                    .getHd1_NamespaceID()
                    .setValue(request.getHospital().getCode());
            pv1.getPv18_ReferringDoctor(0)
                    .getXcn1_IDNumber()
                    .setValue(
                            request.getHl7FieldsFromAsap()
                                    .getDoctorFiscalCode());
            pv1.getPv18_ReferringDoctor(0)
                    .getXcn2_FamilyName()
                    .getFn1_Surname()
                    .setValue(request.getHl7FieldsFromAsap().getDoctorSurname());
            pv1.getPv18_ReferringDoctor(0).getXcn3_GivenName()
                    .setValue(request.getHl7FieldsFromAsap().getDoctorName());
            // pv11
            pv1.getPv151_VisitIndicator().setValue("V");

            return true;
        } else {
            return false;
        }
    }


    protected static boolean fillSFT(SFT sft, Properties jwtProperty) throws DataTypeException {
        if(sft != null){
            if(jwtProperty != null){
                String appVendor = jwtProperty.getProperty("APPLICATION_VENDOR");
                if(StringUtils.isNotBlank(appVendor)){
                    sft.getSft1_SoftwareVendorOrganization().getXon1_OrganizationName().setValue(appVendor);
                }
                String appVersion = jwtProperty.getProperty("APPLICATION_VERSION");
                if(StringUtils.isNotBlank(appVersion)){
                    sft.getSft2_SoftwareCertifiedVersionOrReleaseNumber().setValue(appVersion);
                }
                String appId = jwtProperty.getProperty("APPLICATION_ID");
                if(StringUtils.isNotBlank(appId)){
                    sft.getSft4_SoftwareBinaryID().setValue(appId);
                }
            }
            return true;
        }
        return false;
    }

    protected static boolean fillNTE(NTE nte, RequestNote requestNote, int count, User user, Date noteDate) throws HL7Exception {
        if(nte != null){
            if(!ValidationHelper.isNullOrEmpty(requestNote)){
                nte.getNte1_SetIDNTE().setValue(String.valueOf(count));
                nte.getNte2_SourceOfComment().setValue(requestNote.getStrId());
                nte.insertNte3_Comment(0);
                nte.getNte3_Comment(0).setValue(requestNote.getNote());
                if(!ValidationHelper.isNullOrEmpty(user)){
                    nte.getNte5_EnteredBy().getXcn2_FamilyName().getFn1_Surname().setValue(user.getLastName());
                    nte.getNte5_EnteredBy().getXcn3_GivenName().setValue(user.getFirstName());
                }
                nte.getNte6_EnteredDateTime().setValue(DateTimeHelper.toFormatedString(noteDate, DateTimeHelper.getDatePatternFoHL7()));
            }
            return true;
        }
        return false;
    }
    // End Generate sectors block

    // Send message block

    public static Message sendMessage(Message msg, String host, Integer port,
                                      Integer timeout) throws HL7Exception {
        Connection connection = null;
        try {
            Message response = null;
            try {
                connection = openOutboundConnection(host, port);

                if (connection != null) {
                    try {
                        LogHelper
                                .log(hl7InfoLog,
                                        String.format(
                                                "Connection established to %s:%d. Trying to send message...",
                                                host, port));

                        LogHelper.log(
                                hl7MessageInfoLog,
                                String.format("Sending message: %s",
                                        msg.encode()));

                        response = sendMessage(msg, connection, timeout);

                        if (response != null) {
                            try {
                                LogHelper.log(hl7MessageInfoLog, String.format(
                                        "Message sent. Response: %s",
                                        response.encode()));
                            } catch (Exception e) {
                                LogHelper.log(hl7MessageInfoLog,
                                        "Message sent. No responce");
                            }
                        } else {
                            LogHelper.log(hl7MessageInfoLog,
                                    "Message sent. No responce");
                        }
                    } catch (Exception e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                }
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            }

            return response;
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        } finally {
            closeOutboundConnection(connection);
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public static Connection openOutboundConnection(String host, Integer port)
            throws HL7Exception {
        ConnectionHub connectionHub = ConnectionHub.getInstance();

        if (connectionHub != null) {
            try {
                return connectionHub.attach(host, port.intValue(),
                        new PipeParser(), MinLowerLayerProtocol.class);
            } catch (ca.uhn.hl7v2.HL7Exception e) {
                LogHelper.log(hl7ErrorLog, String.format(
                        "Can't open connection to %s:%d. Message not sent.",
                        host, port));
            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public static void closeOutboundConnection(Connection outboundConnection) {
        if (outboundConnection != null) {
            ConnectionHub connectionHub = ConnectionHub.getInstance();

            if (connectionHub != null) {
                connectionHub.discard(outboundConnection);

            }
            try {
                outboundConnection.close();
            } catch (IOException e) {
                LogHelper.log(hl7ErrorLog, e);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static Message sendMessage(Message msg,
                                      Connection outboundConnection, Integer timeout)
            throws HL7Exception, LLPException, IOException {
        if (msg != null && outboundConnection != null
                && outboundConnection.isOpen()) {
            outboundConnection.getInitiator().setTimeoutMillis(
                    timeout.intValue());

            return outboundConnection.getInitiator().sendAndReceive(msg);
        }
        return null;
    }

    // End send message block
}
