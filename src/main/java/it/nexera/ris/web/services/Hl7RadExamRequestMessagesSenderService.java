package it.nexera.ris.web.services;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.message.ACK;
import ca.uhn.hl7v2.model.v26.message.MDM_T02;
import ca.uhn.hl7v2.model.v26.message.ORM_O01;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.Hl7RequestSendingStatus;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.logic.hl7.BaseHl7MessageHelper;
import it.nexera.ris.common.helpers.logic.hl7.MDMHelper;
import it.nexera.ris.common.helpers.logic.hl7.ORMHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.base.BaseSynchronizableService;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Hl7RadExamRequestMessagesSenderService extends
        BaseSynchronizableService implements Serializable {

    private static final long serialVersionUID = 1461762998722361788L;

    private static final Logger hl7ErrorLog = CustomLibLoggerFactory.getHl7ErrorLogger();

    private static final Logger hl7InfoLog = CustomLibLoggerFactory.getHl7InfoLogger();

    public Hl7RadExamRequestMessagesSenderService() {
        super(SessionNames.Hl7MessagesSenderService);
    }

    @Override
    protected void routineFuncInternal() {
        String hl7HostValue = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_SEND_IP).getValue();

        String hl7PortValue = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_SEND_PORT).getValue();

        String hl7OrmSendRetryTimeValue = ApplicationSettingsHolder
                .getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_ORM_SEND_RETRY_TIME)
                .getValue();

        String hl7MdmSendRetryTimeValue = ApplicationSettingsHolder
                .getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_MDM_SEND_RETRY_TIME)
                .getValue();

        if (hl7HostValue != null && hl7PortValue != null
                && hl7OrmSendRetryTimeValue != null
                && hl7MdmSendRetryTimeValue != null) {
            Query selectTasksIdsQuery = getSession()
                    .createSQLQuery(
                            "select id from radiology_exam_request " +
                                    "where hl7_fields_form_asap_id is not null and (forwarded is null or forwarded = 0) " +
                                    "and sending_status = :sentValue order by id asc");
            selectTasksIdsQuery.setString("sentValue",
                    Hl7RequestSendingStatus.NOT_SENT.name());

            @SuppressWarnings("unchecked")
            List<BigDecimal> requestsIdsDecimal = selectTasksIdsQuery.list();

            List<Long> requestsIds = new ArrayList<Long>();
            for (BigDecimal bd : requestsIdsDecimal) {
                requestsIds.add(Long.valueOf(bd.longValue()));
            }

            if (!ValidationHelper.isNullOrEmpty(requestsIds)) {
                LogHelper
                        .log(hl7InfoLog,
                                "Request list to send from RIS to SIO as ORM messages not empty");
                for (Long id : requestsIds) {
                    try {
                        RadiologyExamRequest request = ConnectionManager.get(
                                RadiologyExamRequest.class, id, getSession());

                        Message response = null;
                        try {
                            Message msg = createMsgFromRequest(request, getSession());

                            if (msg != null) {
                                String host = hl7HostValue;
                                Integer port = Integer.valueOf(hl7PortValue);
                                Integer timeout = Integer.valueOf(10000);

                                switch (request
                                        .getWaitingListRegistrationState()) {
                                    case DRAFT:
                                    case REPORTED:
                                        timeout = Integer
                                                .valueOf(hl7MdmSendRetryTimeValue);
                                        break;
                                    case REQUIRED:
                                    case RESERVED:
                                    case PERFORMED:
                                    case IN_READING:
                                    case ANNULLED:
                                    case DELETED:
                                        timeout = Integer
                                                .valueOf(hl7OrmSendRetryTimeValue);
                                        break;
                                    default:
                                        break;
                                }

                                LogHelper
                                        .log(hl7InfoLog, "Msg forHl7RadExamRequestMessagesSenderService: \n" + msg.encode());

                                response = BaseHl7MessageHelper.sendMessage(
                                        msg, host, port, timeout);

                                LogHelper
                                        .log(hl7InfoLog, "Msg for Hl7RadExamRequestMessagesSenderService response: \n" + response.encode());


                                if (response != null
                                        && BaseHl7MessageHelper.OK_RESPONSE
                                        .equals(((ACK) response)
                                                .getMSA()
                                                .getAcknowledgmentCode()
                                                .getValue())) {
                                    request.setSendingStatus(Hl7RequestSendingStatus.SENT);

                                    Transaction tr = null;
                                    try {
                                        tr = getSession().beginTransaction();
                                        ConnectionManager.save(request,
                                                getSession());
                                    } catch (Exception e) {
                                        if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                                            tr.rollback();
                                        }
                                        LogHelper.log(hl7ErrorLog, e);
                                    } finally {
                                        if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                                            try {
                                                tr.commit();
                                            } catch (Exception e) {
                                                LogHelper.log(hl7ErrorLog, e);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LogHelper.log(hl7ErrorLog, e);
                        }
                    } catch (Exception e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }
                }
            }
        }
    }

    @Override
    protected int getPollTimeKey() {
        Integer val = parseIntegerValue(ApplicationSettingsHolder
                .getInstance()
                .getByKey(ApplicationSettingsKeys.RIS_REQUEST_SEND_POLLING_TIME)
                .getValue());

        if (val != null) {
            return val.intValue();
        } else {
            val = parseIntegerValue(ApplicationSettingsHolder
                    .getInstance()
                    .getDefaultValueByKey(
                            ApplicationSettingsKeys.RIS_REQUEST_SEND_POLLING_TIME));
            if (val != null) {
                return val.intValue();
            }
        }
        return 1;
    }

    protected Integer parseIntegerValue(String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            Integer val = null;

            try {
                val = Integer.parseInt(value);
            } catch (NumberFormatException e) {

            }

            return val;
        }

        return null;
    }

    private Message createMsgFromRequest(RadiologyExamRequest request)
            throws HibernateException, PersistenceException,
            InstantiationException, IllegalAccessException {
        return createMsgFromRequest(request, null);
    }
    private Message createMsgFromRequest(RadiologyExamRequest request, Session session)
            throws HibernateException, PersistenceException,
            InstantiationException, IllegalAccessException {
        switch (request.getWaitingListRegistrationState()) {
            case DRAFT:
                MDM_T02 mdm10 = new MDM_T02();
                mdm10.getParser().getParserConfiguration().setValidating(false);
                mdm10 = MDMHelper.getFilledMessageFromRequest(mdm10, request,
                        "T10", true);

                if (mdm10 == null) {
                    LogHelper.log(hl7ErrorLog,
                            "mdm T10 msg from request not created.");
                }

                return mdm10;
            case REPORTED:
                MDM_T02 mdm02 = new MDM_T02();
                mdm02.getParser().getParserConfiguration().setValidating(false);
                mdm02 = MDMHelper.getFilledMessageFromRequest(mdm02, request,
                        "T02", true, session);

                if (mdm02 == null) {
                    LogHelper.log(hl7ErrorLog,
                            "mdm T02 msg from request not created.");
                }
                return mdm02;
            case REQUIRED:
            case RESERVED:
            case PERFORMED:
            case ANNULLED:
            case DELETED:
                ORM_O01 orm = new ORM_O01();
                orm.getParser().getParserConfiguration().setValidating(false);
                orm = ORMHelper.getFilledMessage(orm, request, true, session);

                if (orm == null) {
                    LogHelper.log(hl7ErrorLog, "orm msg from request not created.");
                }
                return orm;
            default:
                return null;
        }
    }
}
