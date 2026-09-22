package it.nexera.ris.common.helpers;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.message.ACK;
import ca.uhn.hl7v2.model.v26.message.MDM_T02;
import ca.uhn.hl7v2.model.v26.segment.MSA;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.ConservationStates;
import it.nexera.ris.common.enums.Hl7RequestSendingStatus;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.logic.hl7.BaseHl7MessageHelper;
import it.nexera.ris.common.helpers.logic.hl7.MDMHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.logic.ApplicationSettingsValueWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestItemWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HL7RepoHelper {

    private static final Logger hl7ErrorLog = CustomLibLoggerFactory.getHl7ErrorLogger();

    private static final Logger hl7InfoLog = CustomLibLoggerFactory.getHl7InfoLogger();

    public void sendRepoMsgsForDocument(RadExamRequestWrapper requestWrapper, FileEntity signFileEntity, Session session)
            throws HibernateException {
        sendRepoMsgsForDocument(requestWrapper, signFileEntity, false, session);
    }
    public void sendRepoMsgsForDocument(RadExamRequestWrapper requestWrapper, FileEntity signFileEntity, boolean isApi, Session session)
            throws HibernateException {
        List<Long> requestItemsIds = getSelectedRequestsItemsIdsFromRequestWrapper(requestWrapper);
        if (!ValidationHelper.isNullOrEmpty(requestItemsIds)) {
            MDM_T02 msg = new MDM_T02();
            msg.getParser().getParserConfiguration().setValidating(false);
            if (ValidationHelper.isNullOrEmpty(signFileEntity.getVersionOfSave())
                    || signFileEntity.getVersionOfSave().equals(1l)) {
                msg = MDMHelper.getFilledMessageFromIds(msg, requestItemsIds,
                        "T02", false, session, signFileEntity, true, false, isApi);
            } else {
                msg = MDMHelper.getFilledMessageFromIds(msg, requestItemsIds,
                        "T10", false, session, null, true, false, isApi);
            }
            if (msg != null) {
                Message response;
                try {
                    ApplicationSettingsValueWrapper ipR = ApplicationSettingsHolder
                            .getInstance().getByKey(
                                    ApplicationSettingsKeys.HL7_SEND_REPO_IP);
                    ApplicationSettingsValueWrapper portR = ApplicationSettingsHolder
                            .getInstance().getByKey(
                                    ApplicationSettingsKeys.HL7_SEND_REPO_PORT);
                    ApplicationSettingsValueWrapper timeoutR = ApplicationSettingsHolder
                            .getInstance()
                            .getByKey(
                                    ApplicationSettingsKeys.HL7_MDM_SEND_RETRY_TIME);

                    if (!ValidationHelper.isNullOrEmpty(ipR.getValue())
                            && !ValidationHelper
                            .isNullOrEmpty(portR.getValue())
                            && !ValidationHelper.isNullOrEmpty(timeoutR
                            .getValue())) {
                        String ip = ipR.getValue();
                        Integer port = Integer.valueOf(portR.getValue());
                        Integer timeout = Integer.valueOf(timeoutR.getValue());
                        LogHelper
                                .log(hl7InfoLog, "HL7 Repo message request: \n" + msg.encode());
                        response = MDMHelper
                                .sendMessage(msg, ip, port, timeout);
                        LogHelper
                                .log(hl7InfoLog, "HL7 Repo message response: \n" + response.encode());
                        RadiologyExamRequest request = ConnectionManager.get(
                                RadiologyExamRequest.class,
                                requestWrapper.getId(), session);
                        int countRepoHL7 = request.getCountRepoHL7();
                        request.setRepoSendingDate(new Date());
                        request.setCountRepoHL7(countRepoHL7 + 1);

                        if (response != null) {

                            if(BaseHl7MessageHelper.OK_RESPONSE
                                    .equals(((ACK) response).getMSA()
                                            .getAcknowledgmentCode().getValue())){
                                request.setRepoSendingStatus(Hl7RequestSendingStatus.SENT);
                            }else {
                                ACK ack = (ACK) response;
                                if(ack != null)
                                    LogHelper.log(hl7InfoLog, "HL7 Repo acknowledge : \n" + ack.encode());

                                if(ack != null && ack.getERR() != null
                                        && ack.getERR().getErr3_HL7ErrorCode() != null){
                                    request.setRepoSendingStatus(Hl7RequestSendingStatus.NOT_SENT);
                                    if(ack.getERR().getErr3_HL7ErrorCode().getCwe1_Identifier() != null &&
                                            StringUtils.isNotBlank(ack.getERR().getErr3_HL7ErrorCode().getCwe1_Identifier().getValue())){
                                        request.setRepoError(ack.getERR().getErr3_HL7ErrorCode().encode());
                                    }else if(ack.getERR().getErr3_HL7ErrorCode().getCwe9_OriginalText() != null){
                                        request.setRepoError(ack.getERR().getErr3_HL7ErrorCode().getCwe9_OriginalText().getValue());
                                    }
                                }

                            }
                        }
                        ConnectionManager.save(request, true, session);
                    }
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                }
            }
        }
    }

    public void sendReportConservation(RadExamRequestWrapper requestWrapper, FileEntity signFileEntity, Session session)
            throws HibernateException {
        List<Long> requestItemsIds = getSelectedRequestsItemsIdsFromRequestWrapper(requestWrapper);
        if (!ValidationHelper.isNullOrEmpty(requestItemsIds)) {
            MDM_T02 msg = new MDM_T02();
            msg.getParser().getParserConfiguration().setValidating(false);
            if (ValidationHelper.isNullOrEmpty(requestWrapper.getConservationState())
                    || requestWrapper.getConservationState().equals(ConservationStates.NOT_SENT)){
                msg = MDMHelper.getFilledMessageFromIds(msg, requestItemsIds,
                        "T02", false, session, signFileEntity, false, true);
            } else if(requestWrapper.getConservationState().equals(ConservationStates.RESEND)){
                msg = MDMHelper.getFilledMessageFromIds(msg, requestItemsIds,
                        "T10", false, session, null, false, true);
            }
            if (msg != null) {
                Message response;
                try {
                    ApplicationSettingsValueWrapper ipR = ApplicationSettingsHolder
                            .getInstance().getByKey(
                                    ApplicationSettingsKeys.HL7_REPORT_FORWARDING_IP);
                    ApplicationSettingsValueWrapper portR = ApplicationSettingsHolder
                            .getInstance().getByKey(
                                    ApplicationSettingsKeys.HL7_REPORT_FORWARDING_PORT);
                    ApplicationSettingsValueWrapper timeoutR = ApplicationSettingsHolder
                            .getInstance()
                            .getByKey(
                                    ApplicationSettingsKeys.HL7_MDM_SEND_RETRY_TIME);

                    if (!ValidationHelper.isNullOrEmpty(ipR.getValue())
                            && !ValidationHelper
                            .isNullOrEmpty(portR.getValue())
                            && !ValidationHelper.isNullOrEmpty(timeoutR
                            .getValue())) {
                        String ip = ipR.getValue();
                        Integer port = Integer.valueOf(portR.getValue());
                        Integer timeout = Integer.valueOf(timeoutR.getValue());
                        LogHelper
                                .log(hl7InfoLog, "HL7 Report Forwarding message request: \n" + msg.encode());
                        response = MDMHelper
                                .sendMessage(msg, ip, port, timeout);
                        LogHelper
                                .log(hl7InfoLog, "HL7  Report Forwarding response: \n" + response.encode());
                        RadiologyExamRequest request = ConnectionManager.get(RadiologyExamRequest.class,
                                requestWrapper.getId(), session);
                        if (response != null) {
                            MSA msaResponse = ((ACK) response).getMSA();
                            request.setConservationSendDate(DateTimeHelper.getNow());
                            String msa3Response = msaResponse.getMsa3_TextMessage().getValue();
                            request.setConsResponse(msa3Response);
                            if(BaseHl7MessageHelper.OK_RESPONSE
                                    .equals(msaResponse.getAcknowledgmentCode().getValue())){
                                LogHelper.log(hl7InfoLog, "Repost MSA3 response ACK : \n" + msa3Response);
                                if(StringUtils.isNotBlank(msa3Response)
                                        && msa3Response.contains(ResourcesHelper.getValidation("hl7SuccessMessage"))){
                                    request.setConservationState(ConservationStates.SENT);
                                    request.setConsFileEntity(signFileEntity);
                                    Long version = 1l;
                                    if(request.getConsVersion() != null)
                                        version = request.getConsVersion() + 1;
                                    request.setConsVersion(version);

                                }
                            }else {
                                ACK ack = (ACK) response;
                                if(request.getConservationState() == null
                                        || !request.getConservationState().equals(ConservationStates.RESEND))
                                request.setConservationState(ConservationStates.NOT_SENT);
                                if(StringUtils.isBlank(request.getConsResponse())){
                                    if(ack != null && ack.getERR() != null
                                            && ack.getERR().getErr3_HL7ErrorCode() != null
                                            && ack.getERR().getErr3_HL7ErrorCode().getCwe9_OriginalText() != null){
                                        request.setConsResponse(ack.getERR().getErr3_HL7ErrorCode().getCwe9_OriginalText().getValue());
                                    }
                                }
                                if(ack != null){
                                    LogHelper.log(hl7InfoLog, "HL7 Report Forward NACK : \n" + ack.encode());
                                }
                            }
                        }
                        ConnectionManager.save(request, true, session);
                    }
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                }
            }
        }
    }

    private List<Long> getSelectedRequestsItemsIdsFromRequestWrapper(
            RadExamRequestWrapper radExamRequestWrapper) {
        List<Long> requestsItemsIds = null;

        if (!ValidationHelper.isNullOrEmpty(radExamRequestWrapper)
                && !ValidationHelper.isNullOrEmpty(radExamRequestWrapper
                .getRadExamRequestItemWrappers())) {
            requestsItemsIds = new ArrayList<Long>();
            for (RadExamRequestItemWrapper reriw : radExamRequestWrapper
                    .getRadExamRequestItemWrappers()) {
                if (reriw.getSelected()) {
                    requestsItemsIds.add(reriw.getId());
                }
            }
        }
        return requestsItemsIds;
    }

    public Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> getPairRequestsItems(
            List<Long> radExamReqIds, Session session) {
        Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair = null;
        if (!ValidationHelper.isNullOrEmpty(radExamReqIds)) {
            List<RadExamRequestItemWrapper> radExamRequestItemsWrappers = new ArrayList<>();
            List<RadExamRequestWrapper> radExamRequestsWrappers = new ArrayList<>();
            List<RadiologyExamRequestItem> requestItems = ConnectionManager.load(
                    RadiologyExamRequestItem.class, new Criterion[]{
                            Restrictions.in("id", radExamReqIds)
                    }, session);
            List<Long> reqIds = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(requestItems)) {
                for (RadiologyExamRequestItem reri : requestItems) {
                    if (!reqIds.contains(reri.getRadiologyExamRequest()
                            .getId())) {
                        reqIds.add(reri.getRadiologyExamRequest().getId());
                    }
                }
                if (!ValidationHelper.isNullOrEmpty(reqIds)) {
                    List<RadiologyExamRequest> radExamRequests = ConnectionManager
                            .load(RadiologyExamRequest.class,
                                    new Criterion[]{
                                            Restrictions.in("id", reqIds)
                                    }, session);
                    if (!ValidationHelper.isNullOrEmpty(radExamRequests)) {
                        List<RadExamRequestWrapper> radiologyExamRequestsWrappers = new ArrayList<>();
                        for (RadiologyExamRequest rer : radExamRequests) {
                            RadExamRequestWrapper rerw = rer
                                    .getRadExamRequestWrapperFromRequest();
                            if (!ValidationHelper.isNullOrEmpty(rerw
                                    .getRadExamRequestItemWrappers())) {
                                List<RadExamRequestItemWrapper> reriws = new ArrayList<>();
                                for (RadExamRequestItemWrapper reriw : rerw
                                        .getRadExamRequestItemWrappers()) {
                                    if (radExamReqIds.contains(reriw
                                            .getId())) {
                                        reriws.add(reriw);
                                    }
                                }
                                radExamRequestItemsWrappers.addAll(reriws);
                                rerw.setRadExamRequestItemWrappers(reriws);
                            }
                            radExamRequestsWrappers.add(rerw);
                        }
                        radExamRequestsWrappers
                                .addAll(radiologyExamRequestsWrappers);
                    }
                }
            }
            pair = new Pair<>(radExamRequestsWrappers, radExamRequestItemsWrappers);
        }
        return pair;
    }

    public void sendMsgForDocument(RadExamRequestWrapper requestWrapper, FileEntity signFileEntity, Session session)
            throws HibernateException {
        List<Long> requestItemsIds = getSelectedRequestsItemsIdsFromRequestWrapper(requestWrapper);
        if (!ValidationHelper.isNullOrEmpty(requestItemsIds)) {
            MDM_T02 msg = new MDM_T02();
            msg.getParser().getParserConfiguration().setValidating(false);
            if (requestWrapper.getState().equals(
                    WaitingListRegistrationStates.DRAFT)) {
                msg = MDMHelper.getFilledMessageFromIds(msg, requestItemsIds,
                        "T10", false, session, null, false);
            } else {
                msg = MDMHelper.getFilledMessageFromIds(msg, requestItemsIds,
                        "T02", false, session, signFileEntity, false);
            }
            if (msg != null) {
                Message response = null;
                try {
                    ApplicationSettingsValueWrapper ipW = ApplicationSettingsHolder
                            .getInstance().getByKey(
                                    ApplicationSettingsKeys.HL7_SEND_IP);
                    ApplicationSettingsValueWrapper portW = ApplicationSettingsHolder
                            .getInstance().getByKey(
                                    ApplicationSettingsKeys.HL7_SEND_PORT);
                    ApplicationSettingsValueWrapper timeoutW = ApplicationSettingsHolder
                            .getInstance()
                            .getByKey(
                                    ApplicationSettingsKeys.HL7_MDM_SEND_RETRY_TIME);
                    if (!ValidationHelper.isNullOrEmpty(ipW.getValue())
                            && !ValidationHelper
                            .isNullOrEmpty(portW.getValue())
                            && !ValidationHelper.isNullOrEmpty(timeoutW
                            .getValue())) {
                        String ip = ipW.getValue();
                        Integer port = Integer.valueOf(portW.getValue());
                        Integer timeout = Integer.valueOf(timeoutW.getValue());

                        response = MDMHelper
                                .sendMessage(msg, ip, port, timeout);
                    }
                    if (response != null
                            && BaseHl7MessageHelper.OK_RESPONSE
                            .equals(((ACK) response).getMSA()
                                    .getAcknowledgmentCode().getValue())) {
                        RadiologyExamRequest request = ConnectionManager.get(
                                RadiologyExamRequest.class,
                                requestWrapper.getId(), session);
                        request.setSendingStatus(Hl7RequestSendingStatus.SENT);
                        ConnectionManager.save(request, session);
                    }
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                }
            }
        }
    }
}
