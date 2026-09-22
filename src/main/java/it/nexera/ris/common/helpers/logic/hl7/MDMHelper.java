package it.nexera.ris.common.helpers.logic.hl7;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v26.datatype.ED;
import ca.uhn.hl7v2.model.v26.datatype.NA;
import ca.uhn.hl7v2.model.v26.datatype.ST;
import ca.uhn.hl7v2.model.v26.message.MDM_T02;
import ca.uhn.hl7v2.model.v26.segment.EVN;
import ca.uhn.hl7v2.model.v26.segment.MSH;
import ca.uhn.hl7v2.model.v26.segment.OBX;
import ca.uhn.hl7v2.model.v26.segment.TXA;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.Hl7CreatingMessageException;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MDMHelper extends BaseHl7MessageHelper {

    public static MDM_T02 getFilledMessage(MDM_T02 msg,
                                           List<RadiologyExamRequestItem> requestItems, String type,
                                           boolean isService, Session session, FileEntity signFile, boolean isRepository,
                                           boolean isReport) {
        return getFilledMessage(msg, requestItems, type, isService, session, signFile, isRepository, isReport, false);
    }
    public static MDM_T02 getFilledMessage(MDM_T02 msg,
                                           List<RadiologyExamRequestItem> requestItems, String type,
                                           boolean isService, Session session, FileEntity signFile, boolean isRepository,
                                           boolean isReport, boolean isApi) {
        try {
            if(!isRepository && !isReport)
                type = "T10"; //Temporaly fix, power treads for Carmine
            if (msg != null
                    && requestItems != null
                    && requestItems.get(0).getRadiologyExamRequest() != null
                    && requestItems.get(0).getFileEntity() != null
                    && requestItems.get(0).getRadiologyExamRequest()
                    .getPatient() != null) {
                RadiologyExamRequest request = requestItems.get(0)
                        .getRadiologyExamRequest();
                FileEntity fileEntity = requestItems.get(0).getFileEntity();
                FileEntity notSignedFileEntity = requestItems.get(0).getNotSignFileEntity();
                if (signFile != null) {
                    fileEntity = signFile;
                }
                Patient patient = request.getPatient();

                String sendingApplication = "RIS";
                if(!isReport && !ValidationHelper.isNullOrEmpty(request.getExamType())
                        && !ValidationHelper.isNullOrEmpty(request.getExamType().getSendingApp()))
                    sendingApplication = request.getExamType().getSendingApp();

                String hospitalName = null;
                if(!ValidationHelper.isNullOrEmpty(request.getHospital()))
                    hospitalName = request.getHospital().getDescription();

                String consultingDoctor;
                if(StringUtils.isNotBlank(requestItems.get(0).getAsapPlacerOrderNumber()))
                    consultingDoctor = requestItems.get(0).getAsapPlacerOrderNumber();
                else
                    consultingDoctor = request.getStrId();

                if (!fillMSH(msg.getMSH(), "MDM", type, "2.6", isRepository, isReport,
                        sendingApplication,hospitalName )) {
                    throw new Hl7CreatingMessageException();
                }
                if (!fillEVN(msg.getEVN(), type, requestItems.get(0)
                        .getReportDate(), isRepository, isReport, isApi)) {
                    throw new Hl7CreatingMessageException();
                }
                if (!fillPID(msg.getPID(), patient, isRepository, isReport)) {
                    throw new Hl7CreatingMessageException();
                }
                if (!fillPV1(msg.getPV1(), request, patient, isRepository, isReport, isApi, notSignedFileEntity, consultingDoctor,session, false)) {
                    throw new Hl7CreatingMessageException();
                }
                if (!fillTXA(msg.getTXA(), requestItems.get(0), msg.getMSH(),
                        request, type, isRepository, isReport, isApi, session)) {
                    throw new Hl7CreatingMessageException();
                }
                if(!isRepository && !isReport){
                    if (!fillFirstOBX(msg.getOBSERVATION(0).getOBX(), msg)) {
                        throw new Hl7CreatingMessageException();
                    }
                    if (!fillSecondOBX(msg.getOBSERVATION(1).getOBX(), msg,
                            fileEntity, request, isService, session)) {
                        throw new Hl7CreatingMessageException();
                    }
                    if (!ValidationHelper.isNullOrEmpty(requestItems)) {
                        for (RadiologyExamRequestItem requestItem : requestItems) {

                            if (!fillAdditionalOBX(
                                    msg.getOBSERVATION(
                                                    requestItems.indexOf(requestItem) + 2)
                                            .getOBX(), msg, getObx3Value(requestItem.getRadiologyExamRequest(), fileEntity),
                                    requestItem.getRadiologyExam())) {
                                throw new Hl7CreatingMessageException();
                            }
                        }
                    }
                }else if(isRepository){
                    String producerCode = null;
                    if(!ValidationHelper.isNullOrEmpty(request.getHospital()) &&
                            StringUtils.isNotBlank(request.getHospital().getHospitalProducerCode()))
                        producerCode = request.getHospital().getHospitalProducerCode();
                    if (!fillRepoOBX(msg.getOBSERVATION(0).getOBX(), msg, fileEntity, getObx3Value(request, fileEntity), producerCode)) {
                        throw new Hl7CreatingMessageException();
                    }
                }else if(isReport){
                    if (!fillReportOBX(msg, fileEntity)) {
                        throw new Hl7CreatingMessageException();
                    }
                }
                if((isApi || isRepository) && ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                        .getValue() != null && Boolean.parseBoolean(ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                        .getValue())){
                    Properties jwtProperty = loadJWTProperty();
                    if(jwtProperty != null){
                        if (!fillSFT(msg.getSFT(), jwtProperty)) {
                            throw new Hl7CreatingMessageException();
                        }
                    }
                }
                List<RequestNote> notes = ConnectionManager.load(RequestNote.class, new CriteriaAlias[]{
                        new CriteriaAlias("radiologyExamRequest", "r", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("r.id", request.getId()),
                        Restrictions.isNull("fromSio")
                }, session);
                if(!ValidationHelper.isNullOrEmpty(notes)){
                    for(int n=0 ; n < notes.size(); n++){
                        Long userId = !ValidationHelper.isNullOrEmpty(notes.get(n).getUpdateUserId()) ? notes.get(n).getUpdateUserId() :
                                notes.get(n).getCreateUserId();
                        Date noteDate = !ValidationHelper.isNullOrEmpty(notes.get(n).getUpdateDate()) ? notes.get(n).getUpdateDate() :
                                notes.get(n).getCreateDate();
                        User user = null;
                        if(userId != null && userId > 0)
                            user = ConnectionManager.get(User.class, userId, session);
                        if (!fillNTE(msg.getOBSERVATION(0).getNTE(n), notes.get(n), n, user, noteDate)) {
                            throw new Hl7CreatingMessageException();
                        }
                    }
                }

            } else {
                throw new Hl7CreatingMessageException();
            }
        } catch (Exception e) {
            msg = null;
            LogHelper.log(hl7ErrorLog, e);
        }
        return msg;
    }

    private static Properties loadJWTProperty() throws Exception {
        String resourceName = "JWT_config.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
            hl7ErrorLog.error(e);
        }
        return properties;
    }

    private static String getObx3Value(RadiologyExamRequest request, FileEntity fileEntity){
        String obx3Value = null;
        if(!ValidationHelper.isNullOrEmpty(request) &&
                !ValidationHelper.isNullOrEmpty(request.getExamType()) &&
                !ValidationHelper.isNullOrEmpty(request.getExamType().getDocumentTypeCode())){
            obx3Value = request.getExamType().getDocumentTypeCode();
        }else if(!ValidationHelper.isNullOrEmpty(fileEntity))
            obx3Value = fileEntity.getId().toString();

        return obx3Value;
    }
    public static MDM_T02 getFilledMessageFromIds(MDM_T02 msg,
                                                  List<Long> requestItemsIds, String type, boolean isSerivce, Session session,
                                                  FileEntity signFile, boolean isRepository) {
      return getFilledMessageFromIds(msg, requestItemsIds, type, isSerivce, session, signFile, isRepository, false);
    }

    public static MDM_T02 getFilledMessageFromIds(MDM_T02 msg,
                                                  List<Long> requestItemsIds, String type, boolean isSerivce, Session session,
                                                  FileEntity signFile, boolean isRepository, boolean isReport) {
        return getFilledMessageFromIds(msg, requestItemsIds, type, isSerivce, session, signFile, isRepository, isReport,
                false);
    }
    public static MDM_T02 getFilledMessageFromIds(MDM_T02 msg,
                                                  List<Long> requestItemsIds, String type, boolean isSerivce, Session session,
                                                  FileEntity signFile, boolean isRepository, boolean isReport, boolean isApi) {
        try {
            List<RadiologyExamRequestItem> requestItems = ConnectionManager.load(
                    RadiologyExamRequestItem.class, new Criterion[]{
                            Restrictions.in("id", requestItemsIds)
                    }, session);
            return getFilledMessage(msg, requestItems, type, isSerivce, session, signFile, isRepository, isReport, isApi);
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
        return null;
    }


    public static MDM_T02 getFilledMessageFromRequest(MDM_T02 msg, RadiologyExamRequest request, String type,
                                                      boolean isService) {
        try {
            return getFilledMessageFromRequest(msg, request, type, isService, DaoManager.getSession());
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
        return null;
    }
    public static MDM_T02 getFilledMessageFromRequest(MDM_T02 msg, RadiologyExamRequest request, String type,
                                                      boolean isService, Session session) {
        try {
            if (!ValidationHelper.isNullOrEmpty(request) && !ValidationHelper.isNullOrEmpty(request
                    .getRadiologyExamRequestItems())) {
                return getFilledMessage(msg,
                        request.getRadiologyExamRequestItems(), type, isService, session, null,
                        false, false);
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
        return null;
    }

    private static boolean fillEVN(EVN evn, String type, Date dateOfDocument, boolean isRepository, boolean isReport)
            throws DataTypeException {
        return fillEVN(evn, type, dateOfDocument, isRepository, isReport, false);
    }
    private static boolean fillEVN(EVN evn, String type, Date dateOfDocument, boolean isRepository, boolean isReport,
                                   boolean isAPi)
            throws DataTypeException {
        if (evn != null && !ValidationHelper.isNullOrEmpty(type)
                && !ValidationHelper.isNullOrEmpty(dateOfDocument)) {
            evn.getEvn1_EventTypeCode().setValue(type);
            if (isRepository) {
                evn.getEvn7_EventFacility().getNamespaceID().setValue("SSN");
                evn.getEvn4_EventReasonCode().setValue("ASD_EST");
            }else if(isReport){
                evn.getEvn2_RecordedDateTime().setValue(DateTimeHelper.toFormatedString(dateOfDocument,
                        DateTimeHelper.getDatePatternFoHL7()));
            }else
                evn.getEvn2_RecordedDateTime().setValue(dateOfDocument);

            if((isAPi || isRepository) && ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                    .getValue() != null && Boolean.parseBoolean(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                    .getValue())){
                evn.getEvn2_RecordedDateTime().setValue(DateTimeHelper.toFormatedString(dateOfDocument,
                        DateTimeHelper.getDatePatternFoHL7()));
            }
            if(isReport)
                evn.getEvn4_EventReasonCode().setValue("O");
            return true;
        } else {
            return false;
        }
    }

    private static boolean fillTXA(TXA txa,
                                   RadiologyExamRequestItem requestItem, MSH msh,
                                   RadiologyExamRequest request, String type, boolean isRepository, boolean isReport,
                                   Session session) throws DataTypeException,
            InstantiationException, IllegalAccessException {
        return  fillTXA(txa,requestItem, msh,request, type, isRepository, isReport,false, session);
    }
    private static boolean fillTXA(TXA txa,
                                   RadiologyExamRequestItem requestItem, MSH msh,
                                   RadiologyExamRequest request, String type, boolean isRepository, boolean isReport,
                                   boolean isApi, Session session) throws DataTypeException,
            InstantiationException, IllegalAccessException {
        if(txa == null)
            return false;

        if(isReport || isRepository){
            User closingUser = null;
            if(!ValidationHelper.isNullOrEmpty(request.getUserClosingReportId())){
                closingUser = ConnectionManager.get(User.class, request.getUserClosingReportId(), session);
            }
            if((isApi || isRepository) && ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                    .getValue() != null && Boolean.parseBoolean(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                    .getValue())){
                if(!ValidationHelper.isNullOrEmpty(request.getExamType())
                        && StringUtils.isNotBlank(request.getExamType().getDocumentFormatCode())){
                    txa.getTxa3_DocumentContentPresentation().setValue(request.getExamType().getDocumentFormatCode());
                }else
                    txa.getTxa3_DocumentContentPresentation().setValue("2.16.840.1.113883.2.9.10.1.7.1");
                if(!ValidationHelper.isNullOrEmpty(closingUser)){
                    txa.getTxa10_AssignedDocumentAuthenticator(0).getXcn1_IDNumber().setValue(closingUser.getFiscalCode());
                    txa.getTxa10_AssignedDocumentAuthenticator(0).getXcn2_FamilyName().getFn1_Surname().setValue(closingUser.getLastName());
                    txa.getTxa10_AssignedDocumentAuthenticator(0).getXcn3_GivenName().setValue(closingUser.getFirstName());
                }
                txa.getTxa10_AssignedDocumentAuthenticator(0).getXcn4_SecondAndFurtherGivenNamesOrInitialsThereof().setValue("CF");
                txa.getTxa10_AssignedDocumentAuthenticator(0).getXcn5_SuffixEgJRorIII().setValue("NNITA");
                txa.getTxa17_DocumentCompletionStatus().setValue("LA");
            }else
                txa.getTxa3_DocumentContentPresentation().setValue("PDF");
            if(!ValidationHelper.isNullOrEmpty(closingUser)){
                txa.getTxa9_OriginatorCodeName(0).getXcn1_IDNumber().setValue(closingUser.getFiscalCode());
                txa.getTxa9_OriginatorCodeName(0).getXcn2_FamilyName().getFn1_Surname().setValue(closingUser.getLastName());
                txa.getTxa9_OriginatorCodeName(0).getXcn3_GivenName().setValue(closingUser.getFirstName());
            }
            txa.getTxa12_UniqueDocumentNumber().getEi1_EntityIdentifier()
                    .setValue(requestItem.getFileEntity().getId().toString());
            txa.getTxa16_UniqueDocumentFileName().setValue(
                    requestItem.getFileEntity().getName());
        }
        if(isReport){
            txa.getTxa1_SetIDTXA().setValue("1");
            txa.getTxa2_DocumentType().setValue("Referti Radiologia");
            txa.getTxa4_ActivityDateTime()
                    .setValue(DateTimeHelper.toFormatedString(requestItem.getReportDate(), DateTimeHelper.getDatePatternFoHL7()));

            StringBuilder sb = new StringBuilder();
            if(StringUtils.isNotBlank(requestItem.getAccessNumberCode()))
                sb.append(requestItem.getAccessNumberCode());
            if(requestItem.getAccessNumberYear() != null)
                sb.append(requestItem.getAccessNumberYear());
            if(StringUtils.isNotBlank(requestItem.getAccessNumberId()))
                sb.append(requestItem.getAccessNumberId());
            if(StringUtils.isNotBlank(type) && type.equalsIgnoreCase("T10")){
                if(!ValidationHelper.isNullOrEmpty(request.getConsFileEntity()))
                    txa.getTxa13_ParentDocumentNumber().getEi1_EntityIdentifier().setValue(
                            request.getConsFileEntity().getStrId());
                Long version = 1l;
                if(request.getConsVersion() != null)
                    version = request.getConsVersion() + 1;
                txa.getTxa15_FillerOrderNumber().getEi1_EntityIdentifier().setValue(String.valueOf(version));
            }
            txa.getTxa14_PlacerOrderNumber(0)
                    .getEi1_EntityIdentifier()
                    .setValue(sb.toString());
            return true;
        }else if(isRepository){
            txa.getTxa2_DocumentType().setValue("REF");
            txa.getTxa4_ActivityDateTime()
                    .setValue(DateTimeHelper.getNow());
            txa.getTxa6_OriginationDateTime().setValue(DateTimeHelper.getNow());
            txa.getTxa8_EditDateTime(0)
                    .setValue(DateTimeHelper.getNow());

            txa.getTxa9_OriginatorCodeName(0).getXcn4_SecondAndFurtherGivenNamesOrInitialsThereof().setValue("CF");
            txa.getTxa9_OriginatorCodeName(0).getXcn5_SuffixEgJRorIII().setValue("NNITA");
            if(!ValidationHelper.isNullOrEmpty(request.getHl7FieldsFromAsap())){
                txa.getTxa14_PlacerOrderNumber(0)
                        .getEi1_EntityIdentifier()
                        .setValue(
                                request.getHl7FieldsFromAsap()
                                        .getPlacerOrderNumber());
            }
            txa.getTxa15_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(msh.getMsh10_MessageControlID().getValue());
            if (Boolean.TRUE.equals(request.getDocumentSecure())) {
                txa.getTxa18_DocumentConfidentialityStatus().setValue("V");
            } else {
                txa.getTxa18_DocumentConfidentialityStatus().setValue("N");
            }

            if (Boolean.TRUE.equals(request.getDocumentHidden()) || Boolean.TRUE.equals(request.getDocumentSecure())) {
                txa.getTxa21_DocumentChangeReason().setValue("P99");
            } else {
                txa.getTxa21_DocumentChangeReason().setValue("");
            }
            return true;
        }else if (!ValidationHelper.isNullOrEmpty(requestItem)
                && msh != null
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap())
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap().getPlacerOrderNumber())) {
            txa.getTxa2_DocumentType().setValue("DI");
            txa.getTxa3_DocumentContentPresentation().setValue("text/html");
            txa.getTxa4_ActivityDateTime()
                    .setValue(
                            requestItem.getRadiologyExamRequest()
                                    .getLatestActionDate());
            txa.getTxa6_OriginationDateTime().setValue(
                    requestItem.getReportDate());
            txa.getTxa9_OriginatorCodeName(0).getXcn1_IDNumber()
                    .setValue(request.getReferringDoctor());
            txa.getTxa12_UniqueDocumentNumber().getEi1_EntityIdentifier()
                    .setValue(requestItem.getFileEntity().getId().toString());
            txa.getTxa15_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(msh.getMsh10_MessageControlID().getValue());
            txa.getTxa14_PlacerOrderNumber(0)
                    .getEi1_EntityIdentifier()
                    .setValue(
                            request.getHl7FieldsFromAsap()
                                    .getPlacerOrderNumber().toString());
            txa.getTxa16_UniqueDocumentFileName().setValue(
                    requestItem.getFileEntity().getName());

            return true;
        } else {
            return false;
        }
    }

    private static void fillBaseFirstOBX(OBX obx) throws DataTypeException {
        obx.getObx2_ValueType().setValue("TX");
        obx.getObx3_ObservationIdentifier().getCwe1_Identifier()
                .setValue("DOCUMENTO");
    }

    private static boolean fillFirstOBX(OBX obx, MDM_T02 msg)
            throws DataTypeException {
        if (obx != null && msg != null) {
            fillBaseFirstOBX(obx);
            ST obx5Data = new ST(msg);
            obx5Data.setValue("TESTO");
            obx.getObx5_ObservationValue(0).setData(obx5Data);

            return true;
        } else {
            return false;
        }
    }

    private static void fillBaseSecondOBX(OBX obx) throws DataTypeException {
        obx.getObx2_ValueType().setValue("RP");
        obx.getObx3_ObservationIdentifier().getCwe1_Identifier()
                .setValue("URL");
    }

    private static boolean fillSecondOBX(OBX obx, MDM_T02 msg,
                                         FileEntity fileEntity, RadiologyExamRequest request,
                                         boolean isService, Session session) throws DataTypeException, HibernateException,
            PersistenceBeanException {
        LogHelper.log(hl7InfoLog, "Fill second OBX");
        LogHelper.log(hl7InfoLog, String.format("OBX is%spresent and fileEntity is%spresent", obx == null ? " not " : " ",
                fileEntity == null ? " not " : " "));
        if (obx != null && msg != null
                && !ValidationHelper.isNullOrEmpty(fileEntity)
                && !ValidationHelper.isNullOrEmpty(request)) {
            fillBaseSecondOBX(obx);
            ST obx5Data = new ST(msg);
            String URL = getFileUrl(fileEntity, request, isService, session);
            LogHelper.log(hl7InfoLog, "URL is: " + URL);
            if (ValidationHelper.isNullOrEmpty(URL)) {
                return false;
            }

            obx5Data.setValue(URL);
            obx.getObx5_ObservationValue(0).setData(obx5Data);

            return true;
        }
        return false;
    }

    private static void fillBaseAdditionalOBX(OBX obx, String obx3Value)
            throws DataTypeException {
        obx.getObx2_ValueType().setValue("TX");
        obx.getObx3_ObservationIdentifier().getCwe1_Identifier()
                .setValue(obx3Value);
    }

    private static boolean fillAdditionalOBX(OBX obx, MDM_T02 msg,
                                             String obx3Value, RadiologyExam radiologyExam)
            throws DataTypeException {
        if (obx != null && msg != null
                && StringUtils.isNotBlank(obx3Value)
                && !ValidationHelper.isNullOrEmpty(radiologyExam)) {
            fillBaseAdditionalOBX(obx, obx3Value);
            NA obx5DataArray = new NA(msg);
            obx5DataArray.getNa1_Value1().setValue(radiologyExam.getCode());
            obx5DataArray.getNa2_Value2().setValue(
                    radiologyExam.getDescription());
            obx5DataArray.getNa3_Value3().setValue("RIS");
            obx.getObx5_ObservationValue(0).setData(obx5DataArray);

            return true;
        } else {
            return false;
        }
    }

    private static boolean fillRepoOBX(OBX obx, MDM_T02 msg, FileEntity fileEntity)
            throws DataTypeException, IOException {
        return fillRepoOBX(obx, msg, fileEntity, fileEntity.getStrId(), null);
    }
    private static boolean fillRepoOBX(OBX obx, MDM_T02 msg, FileEntity fileEntity, String obx3Value, String producerCode)
            throws DataTypeException, IOException {
        if (obx != null && msg != null) {
            obx.getObx2_ValueType().setValue("ED");
            obx.getObx2_ValueType().setValue("ED");
            if(!ValidationHelper.isNullOrEmpty(fileEntity) && fileEntity.getId() != null)
                obx.getObx3_ObservationIdentifier().getCwe1_Identifier().setValue(obx3Value);
            ED ed=new ED(msg);
            if (ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                    .getValue() != null && Boolean.parseBoolean(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF)
                    .getValue())) {
                ed.getEd3_DataSubtype().setValue("application/pdf+text/x-cda-r2+xml");
            }else
                ed.getEd3_DataSubtype().setValue("application/pdf");
            ed.getEd4_Encoding().setValue("BASE64");
            String base64String;
            if (fileEntity.getPath() != null) {
                Path path = Paths.get(fileEntity.getPath());
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    byte [] fileContent = Files.readAllBytes(Paths.get(fileEntity
                            .getPath()));
                    byte[] encoded = Base64.encodeBase64(fileContent);
                    base64String = new String(encoded, StandardCharsets.US_ASCII);
                } else {
                    throw new RuntimeException(
                            "File not exist or path not readble.");
                }

            } else {
                throw new RuntimeException(
                        "No path and binary data inside fileEntity.");
            }
            ed.getData().setValue(base64String);
            obx.getObx5_ObservationValue(0).setData(ed);
            if(StringUtils.isNotBlank(producerCode))
                obx.getObx15_ProducerSID().getCwe1_Identifier().setValue(producerCode);
            return true;
        } else {
            return false;
        }
    }
    private static boolean fillReportOBX(MDM_T02 msg, FileEntity fileEntity)
            throws DataTypeException, IOException, NoSuchAlgorithmException {
        if (msg != null) {
            OBX obxFirst =  msg.getOBSERVATION(0).getOBX();
            if(obxFirst != null) {
                obxFirst.getObx1_SetIDOBX().setValue("1");
                obxFirst.getObx2_ValueType().setValue("ST");
                ED ed=new ED(msg);

                String base64String;
                if (fileEntity.getPath() != null) {
                    Path path = Paths.get(fileEntity.getPath());
                    if (Files.exists(path) && !Files.isDirectory(path)) {
                        byte [] fileContent = Files.readAllBytes(path);
                        byte[] encoded = Base64.encodeBase64(fileContent);
                        base64String = new String(encoded, StandardCharsets.US_ASCII);
                    } else {
                        throw new RuntimeException(
                                "File not exist or path not readble.");
                    }

                } else {
                    throw new RuntimeException(
                            "No path and binary data inside fileEntity.");
                }
                ed.getEd1_SourceApplication().getHd1_NamespaceID().setValue(base64String);
                obxFirst.getObx5_ObservationValue(0).setData(ed);}

            OBX obxSecond =  msg.getOBSERVATION(1).getOBX();
            if(obxSecond != null) {
                obxSecond.getObx1_SetIDOBX().setValue("2");
                obxSecond.getObx2_ValueType().setValue("ED");
                ED ed=new ED(msg);
                ed.getEd1_SourceApplication().getHd1_NamespaceID().setValue("SHA-256");
                String sha256Hash;
                if (fileEntity.getPath() != null) {
                    Path path = Paths.get(fileEntity.getPath());
                    if (Files.exists(path) && !Files.isDirectory(path)) {
                        byte[] buffer= new byte[8192];
                        int count;
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path.toAbsolutePath().toFile()));
                        while ((count = bis.read(buffer)) > 0) {
                            digest.update(buffer, 0, count);
                        }
                        bis.close();
                        byte[] hash = digest.digest();
                        sha256Hash = Hex.encodeHexString(hash);
                    } else {
                        throw new RuntimeException(
                                "File not exist or path not readble.");
                    }

                } else {
                    throw new RuntimeException(
                            "No path and binary data inside fileEntity.");
                }
                ed.getEd2_TypeOfData().setValue(sha256Hash);
                obxSecond.getObx5_ObservationValue(0).setData(ed);
            }
            return true;
        } else {
            return false;
        }
    }
    private static String getFileUrl(FileEntity fileEntity,
                                     RadiologyExamRequest request, boolean isService, Session session)
            throws HibernateException, PersistenceBeanException {
        String URL = null;
        if (!isService) {
            try {
                if (!FileHelper.exists(FileHelper.getLocalFileDir(),
                        fileEntity.getName())) {

                    if (fileEntity.getContent() != null) {
                        FileHelper.writeFileToFolder(fileEntity.getName(),
                                new File(FileHelper.getLocalFileDir()),
                                fileEntity.getContent());
                    }
                    // New logic for entities stored on hard drive
                    else if (fileEntity.getPath() != null) {
                        Path path = Paths.get(fileEntity.getPath());

                        if (Files.exists(path) && !Files.isDirectory(path)) {
                            FileHelper.writeFileToFolder(fileEntity.getName(),
                                    new File(FileHelper.getLocalFileDir()),
                                    Files.readAllBytes(Paths.get(fileEntity
                                            .getPath())));
                        } else {
                            throw new RuntimeException(
                                    "File not exist or path not redable.");
                        }

                    } else {
                        throw new RuntimeException(
                                "No path and binary data inside fileEntity.");
                    }

                }

                String risUrl = ApplicationSettingsHolder.getRIS_ADDRESS();

                if (!ValidationHelper.isNullOrEmpty(risUrl)) {
                    StringBuilder sb = new StringBuilder(risUrl);
                    sb.append("File/");
                    sb.append(fileEntity.getName());
                    sb.append("?pfdrid_c=true");

                    URL = sb.toString();
                }
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            }

            request.setLastPdfUrl(URL);

            ConnectionManager.save(request, isService, session);
        } else {
            URL = request.getLastPdfUrl();
        }

        return URL;
    }
}
