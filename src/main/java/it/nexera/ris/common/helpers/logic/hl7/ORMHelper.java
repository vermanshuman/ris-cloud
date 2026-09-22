package it.nexera.ris.common.helpers.logic.hl7;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v26.message.ORM_O01;
import ca.uhn.hl7v2.model.v26.segment.MSH;
import ca.uhn.hl7v2.model.v26.segment.OBR;
import ca.uhn.hl7v2.model.v26.segment.ORC;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.Hl7CreatingMessageException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.persistence.PersistenceException;
import java.util.Date;
import java.util.List;

public class ORMHelper extends BaseHl7MessageHelper {

    public static ORM_O01 getFilledMessage(ORM_O01 msg,
                                           RadiologyExamRequest request, boolean isService) {
        return getFilledMessage(msg, request, isService, null);
    }
    public static ORM_O01 getFilledMessage(ORM_O01 msg,
                                           RadiologyExamRequest request, boolean isService, Session session) {
        try {
            if (msg != null && request != null) {

                Patient patient = request.getPatient();


                String accessNumber = null;

                if(!ValidationHelper.isNullOrEmpty(request.getRadiologyExamRequestItems()))
                    accessNumber = request.getRadiologyExamRequestItems().get(0).getAccessNumber();

                String orc1 = "";
                String orc5 = "";
                switch (request.getWaitingListRegistrationState()) {
                    case RESERVED:
                    case ANNULLED:
                    case REQUIRED:
                        orc1 = "SC";
                        orc5 = "IP";
                        break;
                    case DRAFT:
                    case IN_READING:
                    case PERFORMED:
                        orc1 = "SC";
                        orc5 = "CM";
                        break;
                    case DELETED:
                        orc1 = "CA";
                        orc5 = "CA";
                        break;

                    default:
                        break;
                }

                if (!fillMSH(msg.getMSH(), "ORM", "O01", "2.6", false)) {
                    throw new Hl7CreatingMessageException();
                } else {
                    msg.getMSH().getMsh9_MessageType()
                            .getMsg3_MessageStructure().setValue("ORM_O01");
                }

                if (!fillPID(msg.getPATIENT().getPID(), patient, false)) {
                    throw new Hl7CreatingMessageException();
                }

                if (!fillPV1(msg.getPATIENT().getPATIENT_VISIT().getPV1(),
                        request, patient, false, true)) {
                    throw new Hl7CreatingMessageException();
                }

                try {
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
                            if (!fillNTE(msg.getPATIENT().getNTE(n), notes.get(n), n, user, noteDate)) {
                                throw new Hl7CreatingMessageException();
                            }
                        }
                    }
                } catch (PersistenceException e) {
                    e.printStackTrace();
                    try {
                        LogHelper.log(hl7ErrorLog, e);
                    } catch (Exception ex) {
                    }
                    hl7ErrorLog.error("Error in getting notes " + e.getMessage());
                }

                if (!ValidationHelper.isNullOrEmpty(request
                        .getRadiologyExamRequestItems())) {
                    int i = 0;
                    for (RadiologyExamRequestItem requestItem : request
                            .getRadiologyExamRequestItems()) {
                        ++i;
                        if (!fillORC(msg.getORDER(i - 1).getORC(),
                                msg.getMSH(), orc1, orc5, request, requestItem)) {
                            throw new Hl7CreatingMessageException();
                        }
                        if (!fillOBR(msg.getORDER(i - 1).getORDER_DETAIL()
                                        .getOBR(), msg.getMSH(), Integer.toString(i),
                                requestItem.getRadiologyExam(), request,
                                accessNumber)) {
                            throw new Hl7CreatingMessageException();
                        }
                    }
                }
            } else {
                throw new Hl7CreatingMessageException();
            }
        } catch (Exception e) {
            msg = null;
            e.printStackTrace();
            LogHelper.log(hl7ErrorLog, e);
        }
        return msg;
    }

    public static ca.uhn.hl7v2.model.v25.message.ORM_O01 getFilledMessageV25(ca.uhn.hl7v2.model.v25.message.ORM_O01 msg,
                                                                             RadiologyExamRequest request, boolean isService) {
        try {
            if (msg != null && request != null) {

                Patient patient = request.getPatient();

                String accessNumber = request.getRadiologyExamRequestItems()
                        .get(0).getAccessNumber();

                String orc1 = "";
                String orc5 = "";
                switch (request.getWaitingListRegistrationState()) {
                    case RESERVED:
                    case ANNULLED:
                    case REQUIRED:
                        orc1 = "SC";
                        orc5 = "IP";
                        break;
                    case IN_READING:
                    case PERFORMED:
                        orc1 = "SC";
                        orc5 = "CM";
                        break;
                    case DELETED:
                        orc1 = "CA";
                        orc5 = "CA";
                        break;

                    default:
                        break;
                }

                if (!fillMSH(msg.getMSH(), "ORM", "O01", "2.6")) {
                    throw new Hl7CreatingMessageException();
                } else {
                    msg.getMSH().getMsh9_MessageType()
                            .getMsg3_MessageStructure().setValue("ORM_O01");
                }

                if (!fillPID(msg.getPATIENT().getPID(), patient)) {
                    throw new Hl7CreatingMessageException();
                }

                if (!fillPV1(msg.getPATIENT().getPATIENT_VISIT().getPV1(),
                        request, patient)) {
                    throw new Hl7CreatingMessageException();
                }

                if (!ValidationHelper.isNullOrEmpty(request
                        .getRadiologyExamRequestItems())) {
                    int i = 0;
                    for (RadiologyExamRequestItem requestItem : request
                            .getRadiologyExamRequestItems()) {
                        ++i;
                        if (!fillORC(msg.getORDER(i - 1).getORC(),
                                msg.getMSH(), orc1, orc5, request, requestItem)) {
                            throw new Hl7CreatingMessageException();
                        }
                        if (!fillOBR(msg.getORDER(i - 1).getORDER_DETAIL()
                                        .getOBR(), msg.getMSH(), Integer.toString(i),
                                requestItem.getRadiologyExam(), request,
                                accessNumber)) {
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

    private static boolean fillORC(ORC orc, MSH msh, String orc1, String orc5,
                                   RadiologyExamRequest request, RadiologyExamRequestItem requestItem)
            throws DataTypeException {
        if (orc != null
                && msh != null
                && !ValidationHelper.isNullOrEmpty(orc5)
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap())
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap().getPlacerOrderNumber())) {
            orc.getOrc1_OrderControl().setValue(orc1);
            orc.getOrc2_PlacerOrderNumber()
                    .getEi1_EntityIdentifier()
                    .setValue(
                            request.getHl7FieldsFromAsap()
                                    .getPlacerOrderNumber());
            orc.getOrc3_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(msh.getMsh10_MessageControlID().getValue());

            orc.getOrc4_PlacerGroupNumber().getEi1_EntityIdentifier()
                    .setValue(requestItem.getAsapPlacerOrderNumber());

            orc.getOrc5_OrderStatus().setValue(orc5);
            orc.getOrc9_DateTimeOfTransaction().setValue(
                    DateTimeHelper.getNow());
            //orc.getOrc12_OrderingProvider(0).get

            return true;
        } else if (orc != null
                && msh != null
                && !ValidationHelper.isNullOrEmpty(orc5)) {
            orc.getOrc1_OrderControl().setValue(orc1);
            orc.getOrc3_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(msh.getMsh10_MessageControlID().getValue());
            orc.getOrc5_OrderStatus().setValue(orc5);
            orc.getOrc9_DateTimeOfTransaction().setValue(
                    DateTimeHelper.getNow());
            return true;
        } else {
            return false;
        }
    }

    private static boolean fillORC(ca.uhn.hl7v2.model.v25.segment.ORC orc, ca.uhn.hl7v2.model.v25.segment.MSH msh, String orc1, String orc5,
                                   RadiologyExamRequest request, RadiologyExamRequestItem requestItem)
            throws DataTypeException {
        if (orc != null
                && msh != null
                && !ValidationHelper.isNullOrEmpty(orc5)
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap())
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap().getPlacerOrderNumber())) {
            orc.getOrc1_OrderControl().setValue(orc1);
            orc.getOrc2_PlacerOrderNumber()
                    .getEi1_EntityIdentifier()
                    .setValue(
                            request.getHl7FieldsFromAsap()
                                    .getPlacerOrderNumber());
            orc.getOrc3_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(msh.getMsh10_MessageControlID().getValue());

            orc.getOrc4_PlacerGroupNumber().getEi1_EntityIdentifier()
                    .setValue(requestItem.getAsapPlacerOrderNumber());

            orc.getOrc5_OrderStatus().setValue(orc5);
            orc.getOrc9_DateTimeOfTransaction().getTime().setValue(
                    DateTimeHelper.getNow());
            //orc.getOrc12_OrderingProvider(0).get

            return true;
        } else {
            return false;
        }
    }

    private static boolean fillOBR(OBR obr, MSH msh, String id,
                                   RadiologyExam radExam, RadiologyExamRequest request,
                                   String accessNumber) throws DataTypeException {
        if (obr != null
                && msh != null
                && !ValidationHelper.isNullOrEmpty(id)
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap())
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap().getPlacerOrderNumber())) {
            obr.getObr1_SetIDOBR().setValue(id);
            obr.getObr2_PlacerOrderNumber()
                    .getEi1_EntityIdentifier()
                    .setValue(
                            request.getHl7FieldsFromAsap()
                                    .getPlacerOrderNumber());
            obr.getObr3_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(msh.getMsh10_MessageControlID().getValue());

            obr.getObr4_UniversalServiceIdentifier().getCwe1_Identifier()
                    .setValue(radExam.getCode());
            obr.getObr4_UniversalServiceIdentifier().getCwe2_Text()
                    .setValue(radExam.getDescription());

            if (!WaitingListRegistrationStates.ANNULLED.equals(request
                    .getWaitingListRegistrationState())) {
                obr.getObr36_ScheduledDateTime().setValueToMinute(
                        request.getRadiologyExamRequestItems().get(0)
                                .getReserveDate());
            }

            if (!ValidationHelper.isNullOrEmpty(accessNumber)) {
                obr.getObr18_PlacerField1().setValue(accessNumber);
            }
            insertDeletedData(request, obr);
            return true;
        } else if (obr != null
                && msh != null
                && !ValidationHelper.isNullOrEmpty(id)) {
            obr.getObr1_SetIDOBR().setValue(id);
            obr.getObr3_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(msh.getMsh10_MessageControlID().getValue());
            obr.getObr4_UniversalServiceIdentifier().getCwe1_Identifier()
                    .setValue(radExam.getCode());
            obr.getObr4_UniversalServiceIdentifier().getCwe2_Text()
                    .setValue(radExam.getDescription());
            if (!WaitingListRegistrationStates.ANNULLED.equals(request
                    .getWaitingListRegistrationState())) {
                obr.getObr36_ScheduledDateTime().setValueToMinute(
                        request.getRadiologyExamRequestItems().get(0)
                                .getReserveDate());
            }
            if (!ValidationHelper.isNullOrEmpty(accessNumber)) {
                obr.getObr18_PlacerField1().setValue(accessNumber);
            }
            insertDeletedData(request, obr);
            return true;
        } else {
            return false;
        }
    }

    private static void insertDeletedData(RadiologyExamRequest request, OBR obr){
        if(!ValidationHelper.isNullOrEmpty(request.getRadiologyExamRequestItems())){
            String deletedUser = request.getRadiologyExamRequestItems()
                    .get(0).getDeleteUser();
            if(StringUtils.isNotBlank(deletedUser)){
                try {
                    obr.insertObr46_PlacerSupplementalServiceInformation(0);
                    obr.getObr46_PlacerSupplementalServiceInformation()[0].getCwe1_Identifier().setValue(deletedUser);;
                } catch (HL7Exception e) {
                    e.printStackTrace();
                    LogHelper.log(hl7ErrorLog, e);
                }
            }
            String deletedComment = request.getRadiologyExamRequestItems()
                    .get(0).getDeleteComment();
            if(StringUtils.isNotBlank(deletedComment)){
                try {
                    obr.insertObr47_FillerSupplementalServiceInformation(0);
                    obr.getObr47_FillerSupplementalServiceInformation()[0].getCwe1_Identifier().setValue(deletedComment);;
                } catch (HL7Exception e) {
                    e.printStackTrace();
                    LogHelper.log(hl7ErrorLog, e);
                }
            }
        }
    }
    private static boolean fillOBR(ca.uhn.hl7v2.model.v25.segment.OBR obr, ca.uhn.hl7v2.model.v25.segment.MSH msh, String id,
                                   RadiologyExam radExam, RadiologyExamRequest request,
                                   String accessNumber) throws DataTypeException {
        if (obr != null
                && msh != null
                && !ValidationHelper.isNullOrEmpty(id)
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap())
                && !ValidationHelper.isNullOrEmpty(request
                .getHl7FieldsFromAsap().getPlacerOrderNumber())) {
            obr.getObr1_SetIDOBR().setValue(id);
            obr.getObr2_PlacerOrderNumber()
                    .getEi1_EntityIdentifier()
                    .setValue(
                            request.getHl7FieldsFromAsap()
                                    .getPlacerOrderNumber());
            obr.getObr3_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(msh.getMsh10_MessageControlID().getValue());

            obr.getObr4_UniversalServiceIdentifier().getCe1_Identifier()
                    .setValue(radExam.getCode());
            obr.getObr4_UniversalServiceIdentifier().getCe2_Text()
                    .setValue(radExam.getDescription());

            if (!WaitingListRegistrationStates.ANNULLED.equals(request
                    .getWaitingListRegistrationState())) {
                obr.getObr36_ScheduledDateTime().getTime().setValueToMinute(
                        request.getRadiologyExamRequestItems().get(0)
                                .getReserveDate());
            }

            if (!ValidationHelper.isNullOrEmpty(accessNumber)) {
                obr.getObr18_PlacerField1().setValue(accessNumber);
            }

            return true;
        } else {
            return false;
        }
    }
}
