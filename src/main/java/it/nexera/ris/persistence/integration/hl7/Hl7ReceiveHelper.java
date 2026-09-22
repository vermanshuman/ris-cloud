package it.nexera.ris.persistence.integration.hl7;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.datatype.EI;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_RESPONSE;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.model.v23.segment.ORC;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.message.ORM_O01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.UrgencyHelper;
import it.nexera.ris.persistence.SessionHolder;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendar;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistration;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistrationSlot;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarWeekDay;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.persistence.integration.ADTIntegrationHelper;
import it.nexera.ris.persistence.integration.DummyPatient;
import it.nexera.ris.persistence.integration.ORMPair;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.sql.JoinType;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Hl7ReceiveHelper {

    private final Logger hl7ErrorLog = CustomLibLoggerFactory.getHl7ErrorLogger();

    private final Logger hl7InfoLog = CustomLibLoggerFactory.getHl7InfoLogger();

    private static volatile Hl7ReceiveHelper instance;

    private SimpleServer serverRadExamsListener;

    private int listenPort;

    private int currentPort;

    private SessionNames serviceName;

    private Hl7ReceiveHelper() {
        super();
        serviceName = SessionNames.Hl7ReceiveHelper;
    }

    public static Hl7ReceiveHelper getInstance() {
        if (instance == null) {
            synchronized (Hl7ReceiveHelper.class) {
                instance = new Hl7ReceiveHelper();
            }
        }
        return instance;
    }

    public ListenerStatus getHl7ReceiveHelperStatus() {
        ListenerStatus ls = new ListenerStatus();
        if (this.serverRadExamsListener != null) {
            ls.setRunning(this.serverRadExamsListener.isRunning());
        } else {
            ls.setRunning(false);
        }
        ls.setPort(this.currentPort);
        return ls;
    }

    @SuppressWarnings("deprecation")
    public boolean startHl7ReceiveHelper(int port) {
        this.listenPort = port;

        boolean serversStarted = false;

        if (!ValidationHelper.isNullOrEmpty(listenPort)) {
            try {
                if (this.serverRadExamsListener != null) {
                    synchServers(this.serverRadExamsListener);
                }

                serversStarted = false;

                this.serverRadExamsListener = new SimpleServer(listenPort,
                        LowerLayerProtocol.makeLLP(), new PipeParser());

                Application handler = new Hl7Listener();

                this.serverRadExamsListener.registerApplication("ORM", "O01",
                        handler);

                Application oruHandler = new ObservationalHL7Listener();

                this.serverRadExamsListener.registerApplication("ORU", "R01",
                        oruHandler);

                this.serverRadExamsListener.start();

                serversStarted = this.serverRadExamsListener.isRunning();
                if (serversStarted) {
                    currentPort = listenPort;
                }
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            } finally {
                LogHelper.debugInfo(hl7InfoLog, "HL7 Receive server start:"
                        + listenPort + ' ' + serversStarted);
            }
        }

        return serversStarted;
    }

    public void stopHl7ReceiveHelper() {
        if (this.serverRadExamsListener != null) {
            if (this.serverRadExamsListener.isRunning()) {
                this.serverRadExamsListener.stop();

                synchServers(this.serverRadExamsListener);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LogHelper.log(hl7ErrorLog, e);
                }

                System.out.println(">>>>>this.serverRadExams.stop()");
            }
        }
    }

    public synchronized Message handleMessage(Message msg,
                                              SessionNames serviceName) throws HL7Exception, IOException {
        LogHelper.log(hl7InfoLog, "\n Starting handling HL7 message:\n");

        ORMPair ormPair = null;
        ResultWrapper result = new ResultWrapper(true, "");

        boolean unknownMessage = false;
        boolean is25 = false;
        boolean is26 = false;
        boolean is23 = false;

        Transaction tr = null;
        Session session = null;

        try {
            LogHelper
                    .log(hl7InfoLog, "Handling HL7 message: \n" + msg.encode());
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
        List<Long> requestIds = null;
        try {
            session = SessionHolder.getInstance().createSession(this.serviceName);
            tr = session.beginTransaction();

            if (msg instanceof ca.uhn.hl7v2.model.v23.message.ORU_R01) {
                is23 = true;
                ORU_R01 message = ((ca.uhn.hl7v2.model.v23.message.ORU_R01) msg);
                handleORUR01(message, session);
            }else if (msg instanceof ca.uhn.hl7v2.model.v25.message.ORM_O01) {
                is25 = true;

                String type = ((ca.uhn.hl7v2.model.v25.message.ORM_O01) msg)
                        .getORDER().getORC().getOrderControl().getValue();

                String typeORC5 = ((ca.uhn.hl7v2.model.v25.message.ORM_O01) msg)
                        .getORDER().getORC().getOrderStatus().getValue();

                if ("NW".equals(type)) {
                    ormPair = getInfoFromORM_O01(
                            (ca.uhn.hl7v2.model.v25.message.ORM_O01) msg,
                            session);
                    requestIds = handleOrm01NWPair(ormPair, session);
                } else if ("CA".equals(type) && "CA".equals(typeORC5)) {
                    handleOrm01CA((ca.uhn.hl7v2.model.v25.message.ORM_O01) msg,
                            session);
                }
            } else if (msg instanceof ca.uhn.hl7v2.model.v26.message.ORM_O01) {
                is26 = true;

                /*
                 * HandlerORM_O01 handler = new HandlerORM_O01(); reject =
                 * !handler.handleMessage(
                 * (ca.uhn.hl7v2.model.v26.message.ORM_O01) msg, curSession);
                 */

                String type = ((ca.uhn.hl7v2.model.v26.message.ORM_O01) msg)
                        .getORDER().getORC().getOrderControl().getValue();

                if ("NW".equals(type)) {
                    ormPair = getInfoFromORM_O01(
                            (ca.uhn.hl7v2.model.v26.message.ORM_O01) msg,
                            session);
                    requestIds = handleOrm01NWPair(ormPair, session);
                } else if ("CA".equals(type)) {
                    handleOrm01CA((ca.uhn.hl7v2.model.v26.message.ORM_O01) msg,
                            session);
                }
            } else {
                unknownMessage = true;
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
            if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                try {
                    tr.rollback();
                } catch (Exception ex) {
                    LogHelper.log(hl7ErrorLog, ex);
                }
            }
            result = new ResultWrapper(false, e.toString());
        } finally {
            if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                try {
                    tr.commit();
                    if(requestIds != null){
                        sendNotification(requestIds);
                    }
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                    result = new ResultWrapper(false, e.toString());
                }
            }
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                }
            }
        }

        LogHelper.log(hl7InfoLog, "\n Ending handling HL7 message:\n");
        if (is23) {
            LogHelper.log(hl7InfoLog, "generateAck23:\n");
            return generateAck23(msg, result, unknownMessage);
        } else if (is25) {
            LogHelper.log(hl7InfoLog, "generateAck25:\n");
            return generateAck25(msg, result, unknownMessage);
        } else if (is26) {
            LogHelper.log(hl7InfoLog, "generateAck26:\n");
            return generateAck26(msg, result, unknownMessage);
        } else {
            LogHelper.log(hl7InfoLog,
                    "generateAck25 but message not 2.5 version:\n");
            return generateAck25(msg, result, unknownMessage);
        }
    }

    private ca.uhn.hl7v2.model.v26.message.ACK generateAck26(Message msg,
                                                             ResultWrapper result, boolean uncknownMessage) {
        try {
            ca.uhn.hl7v2.model.v26.segment.MSH msh = (ca.uhn.hl7v2.model.v26.segment.MSH) msg
                    .get("MSH");
            @SuppressWarnings("deprecation")
            ca.uhn.hl7v2.model.v26.message.ACK ack = (ca.uhn.hl7v2.model.v26.message.ACK) DefaultApplication
                    .makeACK(msh);

            if (uncknownMessage) {
                ack.getMSA().getAcknowledgmentCode().setValue("AR");
            } else if (result.isResult()) {
                ack.getMSA().getAcknowledgmentCode().setValue("AA");
            } else if (!result.isResult()) {
                ack.getMSA().getAcknowledgmentCode().setValue("AE");
                ack.getMSA().getTextMessage().setValue(result.getDescription());
            }

            if (uncknownMessage) {
                ack.getERR().getHL7ErrorCode().getIdentifier().setValue("200");
                ack.getERR().getErr4_Severity().setValue("E");
            }
            return ack;
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }

        return null;
    }

    private List<Long> handleOrm01NWPair(ORMPair ormPair, Session session) {
        try {
            if (ormPair == null) {
                throw new Exception("ormPair is null");
            }

            List<Patient> listPatients = null;

            Patient patient = null;

            if (!ValidationHelper.isNullOrEmpty(ormPair.getDummyPatient()
                    .getCodice_fiscale())) {
                listPatients = ConnectionManager.load(Patient.class,
                        new Criterion[]{
                                Restrictions.eq("fiscalCode", ormPair
                                        .getDummyPatient().getCodice_fiscale())
                        }, session);

                if (!ValidationHelper.isNullOrEmpty(listPatients)) {
                    patient = listPatients.get(0);
                }
            } else {
                throw new Exception("Fiscal code is empty!");
            }

            if (patient == null) {
                patient = new Patient();
            }

            ormPair.getDummyPatient().toPatient(-1l, patient, session);
            ConnectionManager.save(patient, session);

            if (ormPair.getAsapSectorCode() == null) {
                throw new Exception("Sector code is Null");
            }
            /*
             * if (ormPair.getAsapSectorDescription() == null) { throw new
             * Exception("Sector description is Null"); } if
             * (ormPair.getAsapSectorId() == null) { LogHelper.log(hl7InfoLog,
             * "Asap Sector id is Null"); }
             */
            if (ormPair.getHospital() == null) {
                throw new Exception("Hospital is null");
            }
            if (ormPair.getSector() == null) {
                throw new Exception("RIS Sector is null");
            }

            if (ormPair.getDoctorFiscalCode() == null
                    || ormPair.getDoctorName() == null
                    || ormPair.getDoctorSurname() == null) {
                // throw new Exception("Doctor is null");
                LogHelper.log(hl7InfoLog, "Doctor is null");
            }

            if (ormPair.getUrgency() == null) {
                LogHelper
                        .log(hl7InfoLog,
                                "Urgency is null, there are no urgency with such code in RIS or no urgency in ASAP msg");
            }

            String placerOrderNumber = null;
            if (!ValidationHelper.isNullOrEmpty(ormPair.getPlacerOrderNumber())) {
                placerOrderNumber = ormPair.getPlacerOrderNumber();
            } else {
                throw new Exception(String.format(
                        "Placer order number is empty %s",
                        ormPair.getPlacerOrderNumber()));
            }

            if (ormPair != null
                    && !ValidationHelper.isNullOrEmpty(ormPair
                    .getRadExamItems())) {
                List<DirectReservation> drList = ConnectionManager.load(
                        DirectReservation.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("radiologyExams", "re", JoinType.INNER_JOIN),
                                new CriteriaAlias("reservationAsapSectors", "ras", JoinType.INNER_JOIN),
                                new CriteriaAlias("ras.asapSector", "as", JoinType.INNER_JOIN)
                        },
                        new Criterion[]{
                                Restrictions.eq("as.code", ormPair.getAsapSectorCode()),
                                Restrictions.eq("sector.id", ormPair
                                        .getSector().getId()),
                                Restrictions.eq("re.id", ormPair
                                        .getRadExamItems().get(0)
                                        .getRadiologyExam().getId()),
                                Restrictions.eq("re.state", EnableDisableEnum.ENABLE),
                                Restrictions.or(Restrictions.isNull("activityLine"),
                                        Restrictions.eq("activityLine", ormPair.getAsapActivityLine()))
                        }, session);

                List<RadiologyExamRequestItem> itemsToReserve = new ArrayList<RadiologyExamRequestItem>();

                if (!ValidationHelper.isNullOrEmpty(drList)) {
                    for (DirectReservation itemDrList : drList) {
                        for (RadiologyExamRequestItem item : ormPair
                                .getRadExamItems()) {
                            if (itemDrList.getRadiologyExams().contains(
                                    item.getRadiologyExam())) {
                                item.setReserveDate(new Date());
                                item.setWaitingListRegistrationState(WaitingListRegistrationStates.RESERVED);
                                itemsToReserve.add(item);
                            } else {
                                item.setWaitingListRegistrationState(WaitingListRegistrationStates.REQUIRED);
                            }
                        }
                        ormPair.getRadExamItems().removeAll(itemsToReserve);
                    }
                }

                if (!ValidationHelper.isNullOrEmpty(ormPair.getRadExamItems())) {
                    return createRequest(ormPair, session, ormPair.getRadExamItems(),
                            patient, placerOrderNumber, false);
                }

                if (!ValidationHelper.isNullOrEmpty(itemsToReserve)) {
                    return createRequest(ormPair, session, itemsToReserve, patient,
                            placerOrderNumber, true);
                }
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
        return null;
    }

    private void handleOrm01CA(ca.uhn.hl7v2.model.v26.message.ORM_O01 msg, Session session) {
        try {
            if (msg == null) {
                throw new Exception("msg is null");
            }

            List<RadiologyExamRequestItem> items = new ArrayList<RadiologyExamRequestItem>();
            List<RadiologyExamRequest> requests = new ArrayList<RadiologyExamRequest>();

            for (int i = 0; i < msg.getORDERReps(); i++) {
                String asapPlacerOrderNumber = msg.getORDER(i).getORDER_DETAIL()
                        .getOBR().getObr2_PlacerOrderNumber()
                        .getEi1_EntityIdentifier().getValue();

                List<RadiologyExamRequestItem> requestItems = ConnectionManager.load(RadiologyExamRequestItem.class,
                        new Criterion[]{Restrictions.eq("asapPlacerOrderNumber", asapPlacerOrderNumber)},
                        session);

                if (requestItems != null) {
                    for (RadiologyExamRequestItem item : requestItems) {
                        item.setWaitingListRegistrationState(WaitingListRegistrationStates.DELETED);
                        item.setReserveDate(null);

                        deleteRegistrationForRequestItem(item, session);

                        items.add(item);
                        if (!requests.contains(item
                                .getRadiologyExamRequest())) {
                            requests.add(item.getRadiologyExamRequest());
                        }
                    }
                }
            }

            splitItems(session, items, requests);
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private void splitItems(Session session, List<RadiologyExamRequestItem> items, List<RadiologyExamRequest> requests) throws CloneNotSupportedException {
        if (!requests.isEmpty() && !items.isEmpty()) {
            for (RadiologyExamRequest request : requests) {
                if (items.containsAll(request
                        .getRadiologyExamRequestItems())) {
                    removeItems(request, null, null, session);
                } else {
                    List<RadiologyExamRequestItem> reqItems = new ArrayList<>();
                    List<RadiologyExamRequestItem> splitItems = new ArrayList<>();

                    for (RadiologyExamRequestItem item : request
                            .getRadiologyExamRequestItems()) {
                        if (!items.contains(item)) {
                            reqItems.add(item);
                        } else {
                            splitItems.add(item);
                        }
                    }

                    removeItems(request, reqItems, splitItems, session);
                }

                items.removeAll(request.getRadiologyExamRequestItems());
            }
        }
    }

    private void handleOrm01CA(ca.uhn.hl7v2.model.v25.message.ORM_O01 msg,
                               Session session) {
        try {
            if (msg == null) {
                throw new Exception("msg is null");
            }

            List<RadiologyExamRequestItem> items = new ArrayList<RadiologyExamRequestItem>();
            List<RadiologyExamRequest> requests = new ArrayList<RadiologyExamRequest>();

            for (int i = 0; i < msg.getORDERReps(); i++) {
                String radExamItemCode = msg.getORDER(i).getORDER_DETAIL()
                        .getOBR().getUniversalServiceIdentifier()
                        .getIdentifier().getValue();
                String orderNumber = msg.getORDER(i).getORC()
                        .getOrc4_PlacerGroupNumber().getEi1_EntityIdentifier()
                        .getValue();

                if (radExamItemCode != null && orderNumber != null) {
                    List<Long> examsIds = null;

                    try {
                        examsIds = ConnectionManager.loadIds(
                                RadiologyExam.class,
                                new Criterion[]{
                                        Restrictions.eq("code", radExamItemCode),
                                        Restrictions.eq("state", EnableDisableEnum.ENABLE),
                                        Restrictions.isNotNull("examType")
                                }, session);
                    } catch (Exception e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }

                    if (ValidationHelper.isNullOrEmpty(examsIds)) {
                        throw new Exception(String.format(
                                "RadiologyExam %s not found!", radExamItemCode));
                    } else {
                        RadiologyExamRequestItem item = ConnectionManager
                                .get(RadiologyExamRequestItem.class,
                                        new Criterion[]{
                                                Restrictions.eq("asapPlacerOrderNumber", orderNumber),
                                                Restrictions.eq("radiologyExam.id", examsIds.get(0)),
                                                Restrictions.ne("waitingListRegistrationState",
                                                        WaitingListRegistrationStates.PERFORMED),
                                                Restrictions.ne("waitingListRegistrationState",
                                                        WaitingListRegistrationStates.IN_READING),
                                                Restrictions.ne("waitingListRegistrationState",
                                                        WaitingListRegistrationStates.DELETED)
                                        }, session);

                        if (item != null) {
                            item.setWaitingListRegistrationState(WaitingListRegistrationStates.DELETED);
                            item.setDeleteComment(msg.getORDER(i)
                                    .getORDER_DETAIL().getOBR()
                                    .getRelevantClinicalInformation()
                                    .getValue());
                            item.setReserveDate(null);
                            item.setDeleteUser(String.format("%s %s", msg
                                            .getORDER(i).getORC()
                                            .getOrc12_OrderingProvider(0)
                                            .getXcn2_FamilyName().getFn1_Surname()
                                            .getValue(),

                                    msg.getORDER(i).getORC()
                                            .getOrc12_OrderingProvider(0)
                                            .getXcn3_GivenName().getValue()));

                            deleteRegistrationForRequestItem(item, session);

                            items.add(item);
                            if (!requests.contains(item
                                    .getRadiologyExamRequest())) {
                                requests.add(item.getRadiologyExamRequest());
                            }
                        }

                    }
                } else {
                    LogHelper.log(hl7ErrorLog,
                            "RadiologyExam not found (code missing)!");
                }
            }

            splitItems(session, items, requests);
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private void handleORUR01(ca.uhn.hl7v2.model.v23.message.ORU_R01 oruMessage,
                               Session session) {
        try {
            if (oruMessage == null) {
                throw new Exception("msg is null for ORU R01");
            }
            // Loop through all repetitions of the RESPONSE group
            for (int i = 0; i < oruMessage.getRESPONSEReps(); i++) {
                ORU_R01_RESPONSE responseGroup = oruMessage.getRESPONSE(i);
                // Loop through all ORDER_OBSERVATION groups within RESPONSE
                for (int j = 0; j < responseGroup.getORDER_OBSERVATIONReps(); j++) {
                    ORU_R01_ORDER_OBSERVATION orderObservationGroup = responseGroup.getORDER_OBSERVATION(j);
                    // Access the ORC segment
                    ORC orcSegment = orderObservationGroup.getORC();
                    String accessionNumber = null;
                    String radioNuclideTotalDose = null;
                    EI[] placerOrderNumbers = orcSegment.getPlacerOrderNumber();
                    if (placerOrderNumbers.length > 0) {
                        accessionNumber = placerOrderNumbers[0].getEntityIdentifier().getValue();
                        hl7InfoLog.info("accessionNumber found : " + accessionNumber);
                    }
                    for (int k = 0; k < orderObservationGroup.getOBSERVATIONReps(); k++) {
                        ORU_R01_OBSERVATION observationGroup = orderObservationGroup.getOBSERVATION(k);
                        // Access the OBX segment within OBSERVATION
                        OBX obxSegment = observationGroup.getOBX();
                        if ("7".equals(obxSegment.getSetIDOBX().getValue())) {
                            radioNuclideTotalDose = obxSegment.getObx5_ObservationValue(0).getData().toString();
                            hl7InfoLog.info("radioNuclideTotalDose found : " + radioNuclideTotalDose);
                        }
                    }
                    if(StringUtils.isNotBlank(accessionNumber) && StringUtils.isNotBlank(radioNuclideTotalDose)){
                        List<Long> itemIds = searchItemsForAccessionNumber(accessionNumber, session);
                        for (Long id : itemIds) {
                            RadiologyExamRequestItem item = ConnectionManager.get(RadiologyExamRequestItem.class, new Criterion[]{
                                    Restrictions.eq("id", id)
                            }, session);
                            item.setDeliverydose(radioNuclideTotalDose);
                            ConnectionManager.save(item, session);
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private List<Long> searchItemsForAccessionNumber(String accessionNumber, Session session) throws InstantiationException, IllegalAccessException {
        SQLQuery sqlQuery = session.createSQLQuery("SELECT * FROM RAD_EXAM_REQUEST_ITEM WHERE ACCESS_NUMBER_CODE || ACCESS_NUMBER_YEAR || ACCESS_NUMBER_ID = :accessionNumber");
        sqlQuery.setParameter("accessionNumber", accessionNumber);
        sqlQuery.addScalar("id", org.hibernate.type.LongType.INSTANCE);
        return sqlQuery.list();
    }

    private void deleteRegistrationForRequestItem(RadiologyExamRequestItem requestItem, Session session) {
        try {
            if (!ValidationHelper.isNullOrEmpty(requestItem.getEventCalendarRegistrations())) {
                for (EventCalendarRegistration ecr : requestItem.getEventCalendarRegistrations()) {
                    ConnectionManager.remove(ecr, session);
                }
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private void removeItems(RadiologyExamRequest request,
                             List<RadiologyExamRequestItem> reqItems,
                             List<RadiologyExamRequestItem> splitItems, Session session)
            throws CloneNotSupportedException {
        if (reqItems != null && splitItems != null) {
            RadiologyExamRequest splittedRequest = request.clone();
            request.setUserClosingReportId(null);
            splittedRequest
                    .setWaitingListRegistrationState(WaitingListRegistrationStates.DELETED);
            splittedRequest.setLatestActionDate(new Date());

            if (!WaitingListRegistrationStates.DRAFT
                    .equals(splittedRequest.getWaitingListRegistrationState())
                    && !WaitingListRegistrationStates.REPORTED.equals(
                    splittedRequest.getWaitingListRegistrationState())) {
                splittedRequest.setLatestActionPerformDate(new Date());
            }

            splittedRequest.setReserveDate(null);

            try {
                request.udateItemsDescription(reqItems);//FIXME: DELETE AFTER CREATE TRIGGER
                splittedRequest.udateItemsDescription(splitItems);//FIXME: DELETE AFTER CREATE TRIGGER

                ConnectionManager.save(splittedRequest, session);
                ConnectionManager.save(request, session);
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            }

            request.setRadiologyExamRequestItems(reqItems);
            splittedRequest.setRadiologyExamRequestItems(splitItems);

            for (RadiologyExamRequestItem item : reqItems) {
                item.setRadiologyExamRequest(request);
                ConnectionManager.save(item, session);
            }
            for (RadiologyExamRequestItem item : splitItems) {
                item.setRadiologyExamRequest(splittedRequest);
                ConnectionManager.save(item, session);
            }
            saveHistoryForRequest(splittedRequest, session);
            saveHistoryForRequest(request, session);
        } else {
            request.setWaitingListRegistrationState(WaitingListRegistrationStates.DELETED);
            saveHistoryForRequest(request, session);
        }
    }

    private void saveHistoryForRequest(RadiologyExamRequest request,
                                       Session session) {
        try {
            switch (request.getWaitingListRegistrationState()) {
                case RESERVED:
                case ACCEPTED:
                case PERFORMED:
                case IN_READING:
                case DRAFT:
                case REPORTED:
                    HistoricalReportHelper.getInstance()
                            .saveHistoricalReportForRadiologyExamRequest(
                                    request, null, session, false);
                    break;

                case DELETED:
                case REQUIRED:
                case ANNULLED:
                    String historicalReportId = ConnectionManager.getField(HistoricalReport.class, "id",
                            new Criterion[]{
                                    Restrictions
                                            .eq("radiologyExamRequest", request)
                            }, new CriteriaAlias[]{}, session);
                    if (!ValidationHelper.isNullOrEmpty(historicalReportId)) {
                        ConnectionManager.removeById(HistoricalReport.class, NumberHelper.longValueFromString(historicalReportId), session);
                    }
                default:
                    break;
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private List<Long> createRequest(ORMPair ormPair, Session session,
                               List<RadiologyExamRequestItem> requestItems, Patient patient,
                               String placerOrderNumber, boolean isReserved)
            throws CloneNotSupportedException {
        RadiologyExamRequest request = new RadiologyExamRequest();
        request.setPatient(patient);
        request.setForwarded(ormPair.getForwarded());

        if (!ValidationHelper.isNullOrEmpty(requestItems)) {
            for (RadiologyExamRequestItem item : requestItems) {
                item.setDiagnosticQuestion(ormPair.getDiagnosticQuestion());
            }
        }

        request.setAsapSectorId(ormPair.getAsapSectorId());
        request.setAsapSectorCode(ormPair.getAsapSectorCode());
        request.setAsapSectorDescription(ormPair.getAsapSectorDescription());
        request.setHospital(ormPair.getHospital());

        request.setUrgency(ormPair.getUrgency());
        request.setSector(ormPair.getSector());

        request.setRadiologyExamRequestItems(requestItems);

        if (!ValidationHelper.isNullOrEmpty(ormPair.getElectronicRecipeNumber())) {
            request.setElectronicRecipeNumber(ormPair.getElectronicRecipeNumber());
        }

        if (request.getRadiologyExamRequestItems().get(0).getReserveDate() != null) {
            isReserved = true;
        }

        if (isReserved) {
            request.setRequestDate(requestItems.get(0).getRequestDate());
            request.setReserveDate(request.getRadiologyExamRequestItems()
                    .get(0).getReserveDate());
            request.setWaitingListRegistrationState(WaitingListRegistrationStates.RESERVED);
            request.setSendingStatus(Hl7RequestSendingStatus.NOT_SENT);
            request.setLatestActionDate(request.getRadiologyExamRequestItems()
                    .get(0).getReserveDate());

            if (!WaitingListRegistrationStates.DRAFT
                    .equals(request.getWaitingListRegistrationState())
                    && !WaitingListRegistrationStates.REPORTED
                    .equals(request.getWaitingListRegistrationState())) {
                request.setLatestActionPerformDate(
                        request.getRadiologyExamRequestItems().get(0)
                                .getReserveDate());
            }
        } else {
            request.setSendingStatus(Hl7RequestSendingStatus.SENDING_NOT_NEED);
            request.setRequestDate(requestItems.get(0).getRequestDate());
            request.setWaitingListRegistrationState(WaitingListRegistrationStates.REQUIRED);
        }

        if (ormPair.getReservationDate() != null) {
            request.setReserveDate(ormPair.getReservationDate());
            request.setWaitingListRegistrationState(WaitingListRegistrationStates.RESERVED);
            for (RadiologyExamRequestItem radiologyExamRequestItem : request.getRadiologyExamRequestItems()) {
                radiologyExamRequestItem.setWaitingListRegistrationState(WaitingListRegistrationStates.RESERVED);
                radiologyExamRequestItem.setReserveDate(ormPair.getReservationDate());
            }
        }

        request.setAsapActivityLine(ormPair.getAsapActivityLine());

        if(StringUtils.isNotBlank(ormPair.getTransportType()))
            request.setTrasportType(RadiologyRequestTrasportTypes.getByName(ormPair.getTransportType()));

        Hl7FieldsFromAsap hl7FieldsFromAsap = new Hl7FieldsFromAsap();
        hl7FieldsFromAsap.setPlacerOrderNumber(placerOrderNumber);
        hl7FieldsFromAsap.setDoctorFiscalCode(ormPair.getDoctorFiscalCode());
        hl7FieldsFromAsap.setDoctorName(ormPair.getDoctorName());
        hl7FieldsFromAsap.setDoctorSurname(ormPair.getDoctorSurname());
        hl7FieldsFromAsap.setPsdNumber(ormPair.getPsdNumber());

        ConnectionManager.save(hl7FieldsFromAsap, session);
        request.setHl7FieldsFromAsap(hl7FieldsFromAsap);

        List<Long> requestIds = new ArrayList<>();

        for (RadiologyExamRequest reqItem : WaitingListStatusHelper
                .getRadiologyExamRequestsAfterSplit(request)) {
            reqItem.udateItemsDescription(reqItem
                    .getRadiologyExamRequestItems());//FIXME: DELETE AFTER CREATE TRIGGER

            ConnectionManager.save(reqItem, session);
            requestIds.add(reqItem.getId());

            for (RadiologyExamRequestItem item : reqItem
                    .getRadiologyExamRequestItems()) {
                item.setRadiologyExamRequest(reqItem);

                if (ormPair.getDoctorSurname() != null
                        && ormPair.getDoctorName() != null) {
                    item.setRequestingDoctor(ormPair.getDoctorSurname() + " "
                            + ormPair.getDoctorName());
                }

                if (ormPair.getRequestingDoctor() != null) {
                    item.setRequestingDoctor(ormPair.getRequestingDoctor());
                }

                ConnectionManager.save(item, session);

            }
            if (!ValidationHelper.isNullOrEmpty(ormPair.getNote())) {
                createNote(ormPair.getNote(), reqItem, session);
            }
            if (!ValidationHelper.isNullOrEmpty(ormPair.getAdditionalNote())) {
                createNote(ormPair.getAdditionalNote(), reqItem, session);
            }
            assignRequestToCalendar(ormPair, reqItem, session);
        }
        if (ormPair.getUrgency() != null && ormPair.getUrgency().getShowAlert()) {
            return requestIds;
        }
        return null;
    }

    private void sendNotification(List<Long> requestIds) {
        List<String> ids = new ArrayList<>();
        for (Long id : requestIds) {
            ids.add(id.toString());
        }
        UrgencyHelper.processIds(ids.toArray(new String[0]));
        String externalURLs = ResourcesHelper.getProperty("serverURLs");
        if(!ValidationHelper.isNullOrEmpty(externalURLs)){
            String[] urls = externalURLs.split(";");
            if(!ValidationHelper.isNullOrEmpty(urls)){
                for(String url : urls){
                    if(!ValidationHelper.isNullOrEmpty(url)){
                        callShowMsg(url, requestIds);
                    }
                }
            }
        }
    }

    private void callShowMsg(String url, List<Long> requestIds) {
        try {
            StringBuilder idsStr = new StringBuilder();
            for (int i = 0; i < requestIds.size(); ++i) {
                idsStr.append(requestIds.get(i));
                if ((i + 1) < requestIds.size()) {
                    idsStr.append("_");
                }
            }
            URL serverUrl = new URL(url + "/rest/request/msg?ids=" + idsStr.toString());
            HttpURLConnection con = (HttpURLConnection) serverUrl.openConnection();
            con.setRequestMethod("GET");
            con.getResponseCode();
            con.disconnect();
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    public void filterRegistrationsByDate(
            List<EventCalendarRegistration> registrations) {
        List<Date> dates = new ArrayList<Date>();

        Iterator<EventCalendarRegistration> regIter = registrations.iterator();
        while (regIter.hasNext()) {
            EventCalendarRegistration reg = regIter.next();
            if (dates.contains(reg.getFromDate())) {
                regIter.remove();
            } else {
                dates.add(reg.getFromDate());
            }
        }

    }

    public void assignRequestToCalendar(ORMPair pair,
                                        RadiologyExamRequest request, Session session) {
        // Assigning forwarded requests to calendar

        if (pair.getCupId() != null && request != null) {

            EventCalendar calendarByCup = null;
            EventCalendarRegistrationSlot slot = null;
            Date startDate = null;
            try {
                // Loading calendar by CUP_ID from ORM message
                calendarByCup = ConnectionManager.get(EventCalendar.class,
                        new Criterion[]{
                                Restrictions.eq("cupId", pair.getCupId())
                        }, session);
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            }

            if (calendarByCup != null) {
                if (!ValidationHelper.isNullOrEmpty(request
                        .getRadiologyExamRequestItems())) {
                    try {

                        Date date;
                        if (pair.getReservationDate() != null) {
                            date = pair.getReservationDate();
                        } else {
                            date = request.getReserveDate();
                        }

                        //Loading weekday by reservation date 
                        EventCalendarWeekDay weekday = ConnectionManager.get(
                                EventCalendarWeekDay.class,
                                new Criterion[]{
                                        Restrictions.eq("weekDay",
                                                Weekdays.getByDate(date)),
                                        Restrictions.eq("eventCalendar.id",
                                                calendarByCup.getId())
                                }, session);

                        if (weekday != null) {

                            for (RadiologyExamRequestItem radExamRequestItem : request
                                    .getRadiologyExamRequestItems()) {
                                EventCalendarRegistration ecwlr = new EventCalendarRegistration();

                                ecwlr.setDate(date);
                                ecwlr.setPatientName(radExamRequestItem
                                        .getRadiologyExamRequest().getPatient()
                                        .getName());
                                ecwlr.setPatientSurname(radExamRequestItem
                                        .getRadiologyExamRequest().getPatient()
                                        .getSurname());

                                if (radExamRequestItem
                                        .getRadiologyExamRequest()
                                        .getAsapSectorCode() != null) {
                                    ecwlr.setSectorName(radExamRequestItem
                                            .getRadiologyExamRequest()
                                            .getAsapSectorCode());
                                }
                                ecwlr.setPatientBirthDay(radExamRequestItem
                                        .getRadiologyExamRequest().getPatient()
                                        .getBirthDate());

                                if (ecwlr.getExamsName() == null) {
                                    ecwlr.setExamsName("");
                                }
                                for (RadiologyExamRequestItem radExanReqItem : request
                                        .getRadiologyExamRequestItems()) {
                                    ecwlr.setExamsName((ecwlr.getExamsName()
                                            .isEmpty() ? ecwlr.getExamsName()
                                            : (ecwlr.getExamsName() + ", "))
                                            + radExanReqItem.getRadiologyExam()
                                            .getDescription());
                                }
                                if (EventCalendarWeekdayAvailabilityTypes.SLOT
                                        .equals(weekday.getAvailabilityType())) {

                                    //Searching for vacant slot
                                    if (slot == null) {
                                        slot = createOrGetSlot(weekday, ecwlr,
                                                date, session);
                                    }

                                    if (slot != null) {
                                        if (!slot.isNew()) {
                                            slot = new EventCalendarRegistrationSlot(
                                                    ecwlr,
                                                    slot.getSlotNumber(),
                                                    slot.getOverbooked());
                                        }

                                        ecwlr.setRadiologyExamRequestItem(radExamRequestItem);
                                        ecwlr.setEventCalendarWeekday(weekday);
                                        ecwlr.setPackageId(UUID.randomUUID()
                                                .toString());
                                        ConnectionManager.save(ecwlr, session);
                                        ConnectionManager.save(slot, session);
                                        radExamRequestItem
                                                .setEventCalendarRegistration(ecwlr);
                                        ConnectionManager.save(
                                                radExamRequestItem, session);

                                    } else {
                                        return;
                                    }

                                } else if (EventCalendarWeekdayAvailabilityTypes.DYNAMIC
                                        .equals(weekday.getAvailabilityType())) {

                                    if (startDate == null) {
                                        startDate = searchForVacantDate(
                                                weekday, date, session);
                                    }

                                    if (startDate != null) {
                                        ecwlr.setRadiologyExamRequestItem(radExamRequestItem);
                                        ecwlr.setEventCalendarWeekday(weekday);
                                        ecwlr.setPackageId(UUID.randomUUID()
                                                .toString());
                                        ecwlr.setFromDate(startDate);
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(startDate);
                                        cal.add(Calendar.MINUTE, weekday
                                                .getAverageDuration()
                                                .intValue());
                                        ecwlr.setToDate(cal.getTime());
                                        ConnectionManager.save(ecwlr, session);
                                        radExamRequestItem
                                                .setEventCalendarRegistration(ecwlr);
                                        ConnectionManager.save(
                                                radExamRequestItem, session);
                                    } else {
                                        return;
                                    }

                                }

                            }
                        }
                    } catch (Exception e) {
                        LogHelper.log(hl7ErrorLog, e);
                    }

                }
            }

        }
    }

    private EventCalendarRegistrationSlot createOrGetSlot(
            EventCalendarWeekDay weekday,
            EventCalendarRegistration registration, Date date, Session session)
            throws IllegalAccessException, PersistenceBeanException {

        if (!AgendaHelper.checkIsInDay(weekday, date)) {
            return null;
        }

        Integer slot = null;
        Boolean overbooking = Boolean.FALSE;
        for (int i = 1; i <= weekday.getSlotNumber(); i++) {
            if (AgendaHelper.checkIsInNormalSlot(i, weekday, date)) {
                slot = new Integer(i);
                break;
            }
        }
        if (slot == null) {
            if (weekday.getOverbooking()) {
                for (int i = 1; i <= weekday.getMaximumOverbooking(); i++) {
                    if (AgendaHelper.checkIsOverbooking(i, weekday, date)) {
                        slot = new Integer(i);
                        overbooking = Boolean.TRUE;
                        break;
                    }
                }
            }
        }
        if (slot != null) {
            Long count = ConnectionManager.getCount(
                    EventCalendarRegistrationSlot.class,
                    "request.id",
                    new CriteriaAlias[]{
                            new CriteriaAlias("registration", "reg",
                                    JoinType.LEFT_OUTER_JOIN),
                            new CriteriaAlias("reg.eventCalendarWeekday",
                                    "weekDay", JoinType.LEFT_OUTER_JOIN),
                            new CriteriaAlias("reg.radiologyExamRequestItem",
                                    "item", JoinType.LEFT_OUTER_JOIN),
                            new CriteriaAlias("item.radiologyExamRequest",
                                    "request", JoinType.LEFT_OUTER_JOIN)

                    },
                    new Criterion[]{
                            Restrictions.eq("weekDay.id", weekday.getId()),
                            Restrictions.le("reg.date",
                                    DateTimeHelper.getDayEnd(date)),
                            Restrictions.ge("reg.date",
                                    DateTimeHelper.getDayStart(date)),
                            Restrictions.eq("slotNumber", slot),
                            Restrictions.eq("overbooked", overbooking),
                    }, session);

            if (count < 2) {
                return new EventCalendarRegistrationSlot(registration, slot,
                        overbooking);
            }
        }

        return null;

    }

    private Date searchForVacantDate(EventCalendarWeekDay weekday, Date date,
                                     Session session) {

        List<EventCalendarRegistration> registrations = ConnectionManager
                .load(EventCalendarRegistration.class,
                        new Criterion[]{
                                Restrictions.eq("eventCalendarWeekday.id",
                                        weekday.getId()),
                                Restrictions.le("date",
                                        DateTimeHelper.getDayEnd(date)),
                                Restrictions.ge("date",
                                        DateTimeHelper.getDayStart(date))

                        }, new Order[]{
                                Order.asc("fromDate")
                        }, session);

        filterRegistrationsByDate(registrations);

        //Define start weekday date

        Calendar startNormalCalendar = Calendar.getInstance();
        if (!ValidationHelper.isNullOrEmpty(registrations)) {
            startNormalCalendar.setTime(registrations.get(0).getToDate());
        } else {
            startNormalCalendar.setTime(DateTimeHelper.getDayStart(date));
        }
        String[] hoursminutes = weekday.getStartTimeStr().split(":");
        startNormalCalendar.set(Calendar.HOUR_OF_DAY,
                Integer.parseInt(hoursminutes[0]));
        startNormalCalendar.set(Calendar.MINUTE,
                Integer.parseInt(hoursminutes[1]));

        Date weekdayStart = startNormalCalendar.getTime();

        //Define end weekday date

        Calendar endNormalCalendar = Calendar.getInstance();
        if (!ValidationHelper.isNullOrEmpty(registrations)) {
            endNormalCalendar.setTime(registrations.get(registrations.size() - 1)
                    .getToDate());
        } else {
            startNormalCalendar.setTime(DateTimeHelper.getDayStart(date));
        }
        endNormalCalendar.set(Calendar.HOUR_OF_DAY,
                Integer.parseInt(hoursminutes[0]));
        endNormalCalendar.set(Calendar.MINUTE,
                Integer.parseInt(hoursminutes[1]));
        Date weekdayEnd = endNormalCalendar.getTime();

        //Check if there are any registrations for that day or if startTime => empty => firstregistration
        if (registrations.size() == 0
                || DateTimeHelper.getDateDiffInMin(registrations.get(0)
                .getFromDate(), weekdayStart) > weekday
                .getAverageDuration()) {

            return weekdayStart;

        } else {

            //Check if there is empty time between registrations where one more registration could be placed
            for (int i = 0; i < registrations.size(); i++) {

                if (i + 1 < registrations.size()) {

                    if (DateTimeHelper.getDateDiffInMin(registrations
                            .get(i + 1).getFromDate(), registrations.get(i)
                            .getToDate()) >= weekday.getAverageDuration()) {
                        return registrations.get(i).getToDate();
                    }

                }

            }

            //Check if there is empty time after all regs where one more registration could be placed

            if (DateTimeHelper.getDateDiffInMin(endNormalCalendar.getTime(),
                    registrations.get(registrations.size() - 1).getToDate()) > weekday
                    .getAverageDuration()) {
                return registrations.get(registrations.size() - 1).getToDate();
            }
            //Check if there is empty time overbooking

            else if (weekday.getOverbooking()
                    && (registrations.get(registrations.size() - 1).getToDate()
                    .before(weekdayEnd) || !(registrations
                    .get(registrations.size() - 1).getToDate()
                    .before(weekdayEnd) || registrations
                    .get(registrations.size() - 1).getToDate()
                    .after(weekdayEnd)))) {
                return weekdayEnd;
            }
        }
        return null;

    }

    private void createNote(String note, RadiologyExamRequest request,
                            Session session) {
        RequestNote reqNote = new RequestNote();
        reqNote.setNote(note);
        reqNote.setFromSio(Boolean.TRUE);
        reqNote.setRadiologyExamRequest(request);

        ConnectionManager.save(reqNote, session);
    }

    private ca.uhn.hl7v2.model.v23.message.ACK generateAck23(Message msg, ResultWrapper result, boolean unknownMessage) {
        try {
            ca.uhn.hl7v2.model.v23.segment.MSH msh = (ca.uhn.hl7v2.model.v23.segment.MSH) msg.get("MSH");

            @SuppressWarnings("deprecation")
            ca.uhn.hl7v2.model.v23.message.ACK ack = (ca.uhn.hl7v2.model.v23.message.ACK) DefaultApplication.makeACK(msh);
            if (unknownMessage) {
                ack.getMSA().getAcknowledgementCode().setValue("AR");
            } else if (result.isResult()) {
                ack.getMSA().getAcknowledgementCode().setValue("AA");
            } else if (!result.isResult()) {
                ack.getMSA().getAcknowledgementCode().setValue("AE");
                ack.getMSA().getTextMessage().setValue(result.getDescription());
            }
            return ack;
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
        return null;
    }
    private ACK generateAck25(Message msg, ResultWrapper result,
                              boolean uncknownMessage) {
        try {
            MSH msh = (MSH) msg.get("MSH");

            @SuppressWarnings("deprecation")
            ACK ack = (ACK) DefaultApplication.makeACK(msh);

            if (uncknownMessage) {
                ack.getMSA().getAcknowledgmentCode().setValue("AR");
            } else if (result.isResult()) {
                ack.getMSA().getAcknowledgmentCode().setValue("AA");
            } else if (!result.isResult()) {
                ack.getMSA().getAcknowledgmentCode().setValue("AE");
                ack.getMSA().getTextMessage().setValue(result.getDescription());
            }
            return ack;
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }

        return null;
    }

    private void checkForwarded(ca.uhn.hl7v2.model.v25.message.ORM_O01 msg,
                                ORMPair pair) {

        try {
            if (msg.getORDERReps() > 0) {
                if (!ValidationHelper.isNullOrEmpty(msg.getORDER(0)
                        .getORDER_DETAIL().getOBR()
                        .getObr36_ScheduledDateTime().getTs1_Time().getValue())
                        && !ValidationHelper
                        .isNullOrEmpty(msg
                                .getORDER(0)
                                .getORDER_DETAIL()
                                .getOBR()
                                .getObr46_PlacerSupplementalServiceInformation()[0]
                                .getCe1_Identifier().getValue())
                        && !ValidationHelper.isNullOrEmpty(msg.getORDER(0)
                        .getORC().getOrc12_OrderingProvider()[0]
                        .getIDNumber().getValue())
                        && msg.getORDER(0).getORC().getOrc12_OrderingProvider()[0]
                        .getIDNumber().getValue().equals("CUP")) {
                    pair.setCupId(new Long(Long.parseLong(msg.getORDER(0)
                            .getORDER_DETAIL().getOBR()
                            .getObr46_PlacerSupplementalServiceInformation()[0]
                            .getCe1_Identifier().getValue())));
                    pair.setForwarded(Boolean.TRUE);
                    for (RadiologyExamRequestItem item : pair.getRadExamItems()) {
                        item.setForwarded(Boolean.TRUE);
                    }
                    return;

                }
            }
        } catch (Exception e) {
            LogHelper.log(hl7InfoLog, e);
        }
        pair.setForwarded(Boolean.FALSE);
    }

    private void checkForwarded(ca.uhn.hl7v2.model.v26.message.ORM_O01 msg,
                                ORMPair pair) {

        try {
            if (msg.getORDERReps() > 0) {
                if (!ValidationHelper.isNullOrEmpty(msg.getORDER(0)
                        .getORDER_DETAIL().getOBR()
                        .getObr36_ScheduledDateTime().getValue())
                        && !ValidationHelper
                        .isNullOrEmpty(msg
                                .getORDER(0)
                                .getORDER_DETAIL()
                                .getOBR()
                                .getObr46_PlacerSupplementalServiceInformation()[0]
                                .getCwe1_Identifier().getValue())
                        && !ValidationHelper.isNullOrEmpty(msg.getORDER(0)
                        .getORC().getOrc12_OrderingProvider()[0]
                        .getIDNumber().getValue())
                        && msg.getORDER(0).getORC().getOrc12_OrderingProvider()[0]
                        .getIDNumber().getValue().equals("CUP")) {
                    pair.setCupId(new Long(Long.parseLong(msg.getORDER(0)
                            .getORDER_DETAIL().getOBR()
                            .getObr46_PlacerSupplementalServiceInformation()[0]
                            .getCwe1_Identifier().getValue())));
                    pair.setForwarded(Boolean.TRUE);
                    for (RadiologyExamRequestItem item : pair.getRadExamItems()) {
                        item.setForwarded(Boolean.TRUE);
                    }
                    return;

                }
            }
        } catch (Exception e) {
            LogHelper.log(hl7InfoLog, e);
        }
        pair.setForwarded(Boolean.FALSE);
    }

    private ORMPair getInfoFromORM_O01(
            ca.uhn.hl7v2.model.v25.message.ORM_O01 msg, Session session)
            throws Exception {
        ORMPair pair = new ORMPair();
        extractExmaxItems(pair.getRadExamItems(), msg, session);
        checkForwarded(msg, pair);
        pair.setDiagnosticQuestion(msg.getORDER(0).getORDER_DETAIL().getOBR()
                .getRelevantClinicalInformation().getValue());
        pair.setPlacerOrderNumber(msg.getORDER(0).getORC()
                .getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier()
                .getValue());
        pair.setNote(msg.getNTE().getComment(0).getValue());
        pair.setAdditionalNote(msg.getNTE().getComment(1).getValue());
        extractPatientInfo(pair, msg.getPATIENT().getPID());

        extractSector(pair, msg.getPATIENT().getPATIENT_VISIT().getPV1(),
                session);
        getInfoFromSectorAndHospital(msg, pair, session);

        extractDoctorInfo(pair, msg.getPATIENT().getPATIENT_VISIT().getPV1());
        extractUrgency(pair, msg.getORDER(0).getORDER_DETAIL().getOBR(),
                session);
        pair.setPsdNumber(msg.getPATIENT().getPATIENT_VISIT().getPV1()
                .getPv119_VisitNumber().getCx1_IDNumber().getValue());

        String cupId = msg.getORDER(0)
                .getORDER_DETAIL().getOBR()
                .getObr46_PlacerSupplementalServiceInformation(0)
                .getCe1_Identifier().getValue();
        if (!ValidationHelper.isNullOrEmpty(cupId)) {
            pair.setCupId(Long.parseLong(cupId));
        }
        String reserveDateString = msg.getORDER(0)
                .getORDER_DETAIL().getOBR().getObr36_ScheduledDateTime().getTs1_Time().getValue();

        if (reserveDateString != null && reserveDateString.length() == 12) {
            try {
                Calendar c = extractReservationDate(reserveDateString);
                pair.setReservationDate(c.getTime());
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            }
        }
        pair.setTransportType(msg.getORDER(0).getORDER_DETAIL().getOBR().getObr19_PlacerField2().getValue());
        return pair;
    }

    private Calendar extractReservationDate(String reserveDateString) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR,
                Integer.parseInt(reserveDateString.substring(0, 4)));
        c.set(Calendar.MONTH,
                Integer.parseInt(reserveDateString.substring(4, 6)) - 1);
        c.set(Calendar.DAY_OF_MONTH,
                Integer.parseInt(reserveDateString.substring(6, 8)));
        c.set(Calendar.HOUR_OF_DAY, Integer
                .parseInt(reserveDateString.substring(8, 10)));
        c.set(Calendar.MINUTE, Integer.parseInt(reserveDateString
                .substring(10, 12)));
        return c;
    }

    private ORMPair getInfoFromORM_O01(
            ca.uhn.hl7v2.model.v26.message.ORM_O01 msg, Session session)
            throws Exception {
        ORMPair pair = new ORMPair();

        extractExmaxItems(pair.getRadExamItems(), msg, session);
        checkForwarded(msg, pair);
        pair.setDiagnosticQuestion(msg.getORDER(0).getORDER_DETAIL().getOBR()
                .getRelevantClinicalInformation().getValue());

        pair.setNote(msg.getNTE().getComment(0).getValue());
        pair.setAdditionalNote(msg.getNTE().getComment(1).getValue());
        pair.setPlacerOrderNumber(msg.getORDER(0).getORC()
                .getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier()
                .getValue());

        extractPatientInfo(pair, msg.getPATIENT().getPID());

        extractSector(pair, msg.getPATIENT().getPATIENT_VISIT().getPV1(),
                session);

        getInfoFromSectorAndHospital(msg, pair, session);

        extractDoctorInfo26(pair, msg.getPATIENT().getPATIENT_VISIT().getPV1(),
                msg.getORDER(0).getORC());

        extractUrgency(pair, msg.getORDER(0).getORDER_DETAIL().getOBR(),
                session);

        pair.setPsdNumber(msg.getPATIENT().getPATIENT_VISIT().getPV1()
                .getPv119_VisitNumber().getCx1_IDNumber().getValue());

        String cupId = msg.getORDER(0)
                .getORDER_DETAIL().getOBR()
                .getObr46_PlacerSupplementalServiceInformation(0)
                .getCwe1_Identifier().getValue();
        if (!ValidationHelper.isNullOrEmpty(cupId)) {
            pair.setCupId(Long.parseLong(cupId));
        }
        String electronicRecipeNumber = msg.getORDER(0)
                .getORDER_DETAIL().getOBR()
                .getObr46_PlacerSupplementalServiceInformation(0).getAlternateIdentifier().getValue();
        if (!ValidationHelper.isNullOrEmpty(electronicRecipeNumber)) {
            pair.setElectronicRecipeNumber(electronicRecipeNumber);
        }
        String reserveDateString = msg.getORDER(0)
                .getORDER_DETAIL().getOBR().getObr36_ScheduledDateTime().getValue();

        if (reserveDateString != null && reserveDateString.length() == 12) {
            try {
                Calendar c = extractReservationDate(reserveDateString);
                pair.setReservationDate(c.getTime());
            } catch (Exception e) {
                LogHelper.log(hl7ErrorLog, e);
            }
        }
        pair.setTransportType(msg.getORDER(0).getORDER_DETAIL().getOBR().getObr19_PlacerField2().getValue());
        return pair;
    }

    private void extractSector(ORMPair pair,
                               ca.uhn.hl7v2.model.v25.segment.PV1 pv1, Session session) {
        try {
            String sectorCode = pv1.getPv110_HospitalService().getValue();

            if (!ValidationHelper.isNullOrEmpty(sectorCode)) {
                Sector sector = ConnectionManager.get(Sector.class,
                        new Criterion[]{
                                Restrictions.eq("code", sectorCode)
                        }, session);
                if (sector != null) {
                    pair.setSector(sector);
                }
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private void extractSector(ORMPair pair,
                               ca.uhn.hl7v2.model.v26.segment.PV1 pv1, Session session) {
        try {
            String sectorCode = pv1.getPv110_HospitalService().getValue();

            if (!ValidationHelper.isNullOrEmpty(sectorCode)) {
                Sector sector = ConnectionManager.get(Sector.class,
                        new Criterion[]{
                                Restrictions.eq("code", sectorCode)
                        }, session);
                if (sector != null) {
                    pair.setSector(sector);
                }
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private void extractUrgency(ORMPair pair,
                                ca.uhn.hl7v2.model.v25.segment.OBR obr, Session session) {
        try {
            String urgencyCode = obr.getObr5_PriorityOBR().getValue();

            Urgency urgency = ConnectionManager.get(Urgency.class,
                    new Criterion[]{
                            Restrictions.eq("code", urgencyCode)
                    }, session);

            if (urgency != null) {
                pair.setUrgency(urgency);
            }

        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private void extractUrgency(ORMPair pair,
                                ca.uhn.hl7v2.model.v26.segment.OBR obr, Session session) {
        try {
            String urgencyCode = obr.getObr5_Priority().getValue();

            if (!ValidationHelper.isNullOrEmpty(urgencyCode)) {
                Urgency urgency = ConnectionManager.get(Urgency.class,
                        new Criterion[]{
                                Restrictions.eq("code", urgencyCode)
                        }, session);

                if (urgency != null) {
                    pair.setUrgency(urgency);
                }
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private void extractDoctorInfo(ORMPair pair,
                                   ca.uhn.hl7v2.model.v25.segment.PV1 pv1) {
        pair.setDoctorFiscalCode(pv1.getPv18_ReferringDoctor(0)
                .getXcn1_IDNumber().getValue());
        pair.setDoctorSurname(pv1.getPv18_ReferringDoctor(0)
                .getXcn2_FamilyName().getFn1_Surname().getValue());
        pair.setDoctorName(pv1.getPv18_ReferringDoctor(0).getXcn3_GivenName()
                .getValue());
    }

    private void extractDoctorInfo26(ORMPair pair,
                                     ca.uhn.hl7v2.model.v26.segment.PV1 pv1,
                                     ca.uhn.hl7v2.model.v26.segment.ORC orc) {
        pair.setDoctorFiscalCode(pv1.getPv18_ReferringDoctor(0)
                .getXcn1_IDNumber().getValue());
        pair.setRequestingDoctor(orc.getOrc12_OrderingProvider()[0]
                .getXcn1_IDNumber().getValue());
    }

    private void getInfoFromSectorAndHospital(
            ca.uhn.hl7v2.model.v25.message.ORM_O01 msg, ORMPair pair,
            Session session) {
        pair.setAsapSectorCode(msg.getPATIENT().getPATIENT_VISIT().getPV1()
                .getPv13_AssignedPatientLocation().getPointOfCare().getValue());

        pair.setAsapSectorDescription(msg.getPATIENT().getPATIENT_VISIT()
                .getPV1().getPv13_AssignedPatientLocation()
                .getPl9_LocationDescription().getValue());
        try {
            if (pair.getAsapSectorCode() != null) {
                Long localAsapSector = ADTIntegrationHelper.getInstance()
                        .getASAPSIOSectorIdFromCode(pair.getAsapSectorCode());
                if (!ValidationHelper.isNullOrEmpty(localAsapSector)) {
                    pair.setAsapSectorId(localAsapSector);
                } else {
                    String asapSectorCode = pair.getAsapSectorCode();
                    if (asapSectorCode.length() > 2) {
                        String localCode = asapSectorCode.substring(0,
                                asapSectorCode.length() - 2);
                        localAsapSector = ADTIntegrationHelper.getInstance()
                                .getASAPSIOSectorIdFromCode(localCode);
                        if (!ValidationHelper.isNullOrEmpty(localAsapSector)) {
                            String activityLine = asapSectorCode
                                    .substring(localCode.length());

                            pair.setAsapActivityLine(activityLine);
                            pair.setAsapSectorId(localAsapSector);
                            pair.setAsapSectorCode(localCode);
                        }
                    }
                }

            }
            
            /* unused for 30806 */
            /*
             * String hospitalCode =
             * msg.getPATIENT().getPATIENT_VISIT().getPV1()
             * .getPv13_AssignedPatientLocation().getPl4_Facility()
             * .getHd1_NamespaceID().getValue();
             * 
             * Hospital hospital = null; if (hospitalCode != null) { hospital =
             * ConnectionManager.get(Hospital.class, new Criterion[] {
             * Restrictions.eq("code", hospitalCode) }, session); }
             */

            List<Hospital> hospitals = null;
            if (pair.getSector() != null) {
                hospitals = ConnectionManager.load(Hospital.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("sectors", "s",
                                        JoinType.INNER_JOIN)
                        }, new Criterion[]{
                                Restrictions.eq("s.code", pair.getSector()
                                        .getCode())
                        }, session);
            }

            if (!ValidationHelper.isNullOrEmpty(hospitals)) {
                pair.setHospital(hospitals.get(0));
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private void getInfoFromSectorAndHospital(
            ca.uhn.hl7v2.model.v26.message.ORM_O01 msg, ORMPair pair,
            Session session) {
        String asapSectorInfo = msg.getPATIENT().getPATIENT_VISIT().getPV1()
                .getPv13_AssignedPatientLocation().getPointOfCare().getValue();

        String[] forSplit = asapSectorInfo.split(" ");

        if (forSplit != null) {
            pair.setAsapSectorCode(forSplit[0]);
            pair.setAsapSectorDescription(asapSectorInfo.substring(forSplit[0]
                    .length() + 1));
        }
        
        /*
         * pair.setAsapSectorCode(msg.getPATIENT().getPATIENT_VISIT().getPV1()
         * .getPv13_AssignedPatientLocation().getPointOfCare().getValue());
         * 
         * pair.setAsapSectorDescription(msg.getPATIENT().getPATIENT_VISIT()
         * .getPV1().getPv13_AssignedPatientLocation()
         * .getPl9_LocationDescription().getValue());
         */
        try {
            if (pair.getAsapSectorCode() != null) {
                Long localAsapSector = ADTIntegrationHelper.getInstance()
                        .getASAPSIOSectorIdFromCode(pair.getAsapSectorCode());
                if (!ValidationHelper.isNullOrEmpty(localAsapSector)) {
                    pair.setAsapSectorId(localAsapSector);
                } else {
                    String asapSectorCode = pair.getAsapSectorCode();
                    if (asapSectorCode.length() > 2) {
                        String localCode = asapSectorCode.substring(0,
                                asapSectorCode.length() - 2);
                        localAsapSector = ADTIntegrationHelper.getInstance()
                                .getASAPSIOSectorIdFromCode(localCode);
                        if (!ValidationHelper.isNullOrEmpty(localAsapSector)) {
                            String activityLine = asapSectorCode
                                    .substring(localCode.length());

                            pair.setAsapActivityLine(activityLine);
                            pair.setAsapSectorId(localAsapSector);
                            pair.setAsapSectorCode(localCode);
                        }
                    }
                }

            }
            
            /* unused for 30806 */
            /*
             * String hospitalCode =
             * msg.getPATIENT().getPATIENT_VISIT().getPV1()
             * .getPv13_AssignedPatientLocation().getPl4_Facility()
             * .getHd1_NamespaceID().getValue();
             * 
             * Hospital hospital = null; if (hospitalCode != null) { hospital =
             * ConnectionManager.get(Hospital.class, new Criterion[] {
             * Restrictions.eq("code", hospitalCode) }, session); }
             */

            List<Hospital> hospitals = null;
            if (pair.getSector() != null) {
                hospitals = ConnectionManager.load(Hospital.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("sectors", "s",
                                        JoinType.INNER_JOIN)
                        }, new Criterion[]{
                                Restrictions.eq("s.code", pair.getSector()
                                        .getCode())
                        }, session);
            }

            if (!ValidationHelper.isNullOrEmpty(hospitals)) {
                pair.setHospital(hospitals.get(0));
            }
        } catch (Exception e) {
            LogHelper.log(hl7ErrorLog, e);
        }
    }

    private void extractExmaxItems(List<RadiologyExamRequestItem> list,
                                   ORM_O01 msg, Session session) throws Exception {
        for (int i = 0; i < msg.getORDERReps(); i++) {
            String radExamItemCode = msg.getORDER(i).getORDER_DETAIL().getOBR()
                    .getUniversalServiceIdentifier().getIdentifier().getValue();
            String orderNumber = msg.getORDER(i).getORC()
                    .getOrc4_PlacerGroupNumber().getEi1_EntityIdentifier()
                    .getValue();

            if (radExamItemCode != null && orderNumber != null) {
                List<RadiologyExam> exams = null;
                try {
                    exams = ConnectionManager.load(RadiologyExam.class,
                            new Criterion[]{
                                    Restrictions.eq("code", radExamItemCode),
                                    Restrictions.eq("state", EnableDisableEnum.ENABLE),
                                    Restrictions.isNotNull("examType")
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                }

                if (ValidationHelper.isNullOrEmpty(exams)) {
                    throw new Exception(String.format(
                            "RadiologyExam %s not found!", radExamItemCode));
                } else {
                    RadiologyExamRequestItem item = new RadiologyExamRequestItem();
                    item.setRequestDate(new Date());
                    item.setRadiologyExam(exams.get(0));
                    item.setWaitingListRegistrationState(WaitingListRegistrationStates.REQUIRED);
                    item.setAsapPlacerOrderNumber(orderNumber);
                    item.setNote(msg.getNTE().getComment(0).getValue());
                    item.setForwarded(Boolean.FALSE);
                    list.add(item);
                }
            } else {
                LogHelper.log(hl7ErrorLog,
                        "RadiologyExam not found (code missing)!");
                // throw new
                // Exception("RadiologyExam not found (code missing)!");
            }
        }
    }

    private void extractExmaxItems(List<RadiologyExamRequestItem> list,
                                   ca.uhn.hl7v2.model.v26.message.ORM_O01 msg, Session session)
            throws Exception {
        for (int i = 0; i < msg.getORDERReps(); i++) {
            String radExamItemCode = msg.getORDER(i).getORDER_DETAIL().getOBR()
                    .getUniversalServiceIdentifier().getIdentifier().getValue();
            String orderNumber = msg.getORDER(i).getORC()
                    .getOrc4_PlacerGroupNumber().getEi1_EntityIdentifier()
                    .getValue();

            String reserveDateString = msg.getORDER(i).getORDER_DETAIL()
                    .getOBR().getObr36_ScheduledDateTime().getValue();

            Date reserveDate = null;

            if (reserveDateString != null && reserveDateString.length() == 12) {
                try {
                    Calendar c = extractReservationDate(reserveDateString);

                    reserveDate = c.getTime();
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                }
            } else {
                LogHelper.log(hl7ErrorLog,
                        "reserve date is empty or not correct format");
            }

            if (radExamItemCode != null && orderNumber != null) {
                List<RadiologyExam> exams = null;
                try {
                    exams = ConnectionManager.load(RadiologyExam.class,
                            new Criterion[]{
                                    Restrictions.eq("code", radExamItemCode),
                                    Restrictions.isNotNull("examType")
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                }

                if (ValidationHelper.isNullOrEmpty(exams)) {
                    throw new Exception(String.format(
                            "RadiologyExam %s not found!", radExamItemCode));
                } else {
                    RadiologyExamRequestItem item = new RadiologyExamRequestItem();
                    item.setRequestDate(new Date());
                    item.setRadiologyExam(exams.get(0));

                    if (reserveDate == null) {
                        item.setWaitingListRegistrationState(WaitingListRegistrationStates.REQUIRED);
                    } else {
                        item.setReserveDate(reserveDate);
                        item.setWaitingListRegistrationState(WaitingListRegistrationStates.RESERVED);
                    }
                    item.setAsapPlacerOrderNumber(orderNumber);

                    list.add(item);
                }
            } else {
                LogHelper.log(hl7ErrorLog,
                        "RadiologyExam not found (code missing)!");
                // throw new
                // Exception("RadiologyExam not found (code missing)!");
            }
        }
    }

    private void extractPatientInfo(ORMPair co,
                                    ca.uhn.hl7v2.model.v26.segment.PID patientInfo)
            throws DataTypeException {
        DummyPatient dPat = co.getDummyPatient();
        dPat.setCodice_fiscale(patientInfo.getPatientIdentifierList(1)
                .getIDNumber().getValue());
        if (ValidationHelper.isNullOrEmpty(dPat.getCodice_fiscale())) {
            dPat.setCodice_fiscale(patientInfo.getPatientIdentifierList(2)
                    .getIDNumber().getValue());
        }
        dPat.setHealth_card_number(patientInfo.getPatientIdentifierList(2)
                .getIDNumber().getValue());
        dPat.setGiven_name(patientInfo.getPatientName(0).getGivenName()
                .getValue());
        dPat.setFamily_name(patientInfo.getPatientName(0).getFamilyName()
                .getSurname().getValue());
        dPat.setDob(patientInfo.getDateTimeOfBirth().getValueAsDate());
        dPat.setSex(patientInfo.getAdministrativeSex().getValue());
        dPat.setIndirizzo_residenza(patientInfo.getPatientAddress(0)
                .getStreetAddress().getStreetName().getValue());
        dPat.setNum_indirizzo_residenza(patientInfo.getPatientAddress(0)
                .getStreetAddress().getDwellingNumber().getValue());
        dPat.setResidence_Info_Code(patientInfo.getPatientAddress(0)
                .getCensusTract().getValue());
        dPat.setCitta_residenza(patientInfo.getPatientAddress(0).getCity()
                .getValue());
        dPat.setProvincia_residenza(patientInfo.getPatientAddress(0)
                .getStateOrProvince().getValue());
        dPat.setCap_residenza(patientInfo.getPatientAddress(0)
                .getZipOrPostalCode().getValue());
        dPat.setIstat_residenza(patientInfo.getPatientAddress(0)
                .getCountyParishCode().getValue());
        /*
         * dPat.setE_mail(patientInfo.getPhoneNumberHome(0).getEmailAddress()
         * .getValue());
         */
        dPat.setTelephone(patientInfo.getPhoneNumberHome(0)
                .getUnformattedTelephoneNumber().getValue());
        dPat.setIstat_code_nationality(patientInfo.getNationality()
                .getAlternateIdentifier().getValue());
        dPat.setNazionalita(patientInfo.getNationality().getAlternateText()
                .getValue());
        dPat.setExternal_patient_id(patientInfo.getPid3_PatientIdentifierList(0)
                .getIDNumber().getValue());
    }

    private void extractPatientInfo(ORMPair co, PID patientInfo)
            throws DataTypeException {
        DummyPatient dPat = co.getDummyPatient();
        dPat.setCodice_fiscale(patientInfo.getPatientIdentifierList(1)
                .getIDNumber().getValue());
        dPat.setHealth_card_number(patientInfo.getPatientIdentifierList(2)
                .getIDNumber().getValue());
        dPat.setGiven_name(patientInfo.getPatientName(0).getGivenName()
                .getValue());
        dPat.setFamily_name(patientInfo.getPatientName(0).getFamilyName()
                .getSurname().getValue());
        dPat.setDob(patientInfo.getDateTimeOfBirth().getTime().getValueAsDate());
        dPat.setSex(patientInfo.getAdministrativeSex().getValue());
        dPat.setIndirizzo_residenza(patientInfo.getPatientAddress(0)
                .getStreetAddress().getStreetName().getValue());
        dPat.setNum_indirizzo_residenza(patientInfo.getPatientAddress(0)
                .getStreetAddress().getDwellingNumber().getValue());
        dPat.setResidence_Info_Code(patientInfo.getPatientAddress(0)
                .getCensusTract().getValue());
        dPat.setCitta_residenza(patientInfo.getPatientAddress(0).getCity()
                .getValue());
        dPat.setProvincia_residenza(patientInfo.getPatientAddress(0)
                .getStateOrProvince().getValue());
        dPat.setCap_residenza(patientInfo.getPatientAddress(0)
                .getZipOrPostalCode().getValue());
        dPat.setIstat_residenza(patientInfo.getPatientAddress(0)
                .getCountyParishCode().getValue());
        dPat.setE_mail(patientInfo.getPhoneNumberHome(0).getEmailAddress()
                .getValue());
        dPat.setTelephone(patientInfo.getPhoneNumberHome(0)
                .getUnformattedTelephoneNumber().getValue());
        dPat.setIstat_code_nationality(patientInfo.getNationality()
                .getAlternateIdentifier().getValue());
        dPat.setNazionalita(patientInfo.getNationality().getAlternateText()
                .getValue());
        dPat.setExternal_patient_id(patientInfo.getPid3_PatientIdentifierList(0)
                .getIDNumber().getValue());
    }

    private void synchServers(SimpleServer server) {
        if (server != null && server.isRunning()) {
            server.stop();
        } else {
            return;
        }

        int j = 0;
        while (j < 10) {
            if (server != null && server.isRunning()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LogHelper.log(hl7ErrorLog, e);
                }
            } else {
                // Go out of the cycle
                break;
            }

            j++;
            if (j > 5) {
                j = 0;
            }
        }
    }

    public SessionNames getServiceName() {
        return serviceName;
    }

    public void setServiceName(SessionNames serviceName) {
        this.serviceName = serviceName;
    }

}
