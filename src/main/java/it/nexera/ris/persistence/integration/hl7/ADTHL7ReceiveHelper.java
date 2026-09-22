package it.nexera.ris.persistence.integration.hl7;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.datatype.XAD;
import ca.uhn.hl7v2.model.v26.message.ACK;
import ca.uhn.hl7v2.model.v26.segment.MSH;
import ca.uhn.hl7v2.model.v26.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.SessionHolder;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.Patient;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.integration.ADTPair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.io.IOException;
import java.util.List;

public class ADTHL7ReceiveHelper {
    //private static final String                 U                    = "U";

    //private static final String                 N                    = "N";

    //private static final String                 I                    = "I";

    //private static final String                 E                    = "E";

    private static final String D = "D";

    private static final String A08 = "A08";

    private final Logger log = LogManager.getLogger(ADTHL7ReceiveHelper.class);

    private static volatile ADTHL7ReceiveHelper instance;

    private SimpleServer serverADTListener;

    private int listenPort;

    private int currentPort;

    private final String residenceAddressCode = "R";

    private final String domicileAddressCode = D;

    private ADTHL7ReceiveHelper() {
        super();
    }

    public static ADTHL7ReceiveHelper getInstance() {
        if (instance == null) {
            synchronized (ADTHL7ReceiveHelper.class) {
                instance = new ADTHL7ReceiveHelper();
            }
        }
        return instance;
    }

    public ListenerStatus getADTReceiveListenerStatus() {
        ListenerStatus ls = new ListenerStatus();
        if (this.serverADTListener != null) {
            ls.setRunning(this.serverADTListener.isRunning());
        } else {
            ls.setRunning(false);
        }
        ls.setPort(this.currentPort);
        return ls;
    }

    public void stopAdtReceiveListener() {
        if (this.serverADTListener != null) {
            if (this.serverADTListener.isRunning()) {
                this.serverADTListener.stop();

                synchServers(this.serverADTListener);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LogHelper.log(log, e);
                }

                System.out.println("this.serverRadExams.stop()");
            }
        }
    }

    @SuppressWarnings("deprecation")
    public boolean startAdtReceiveListener(int port) {
        this.listenPort = port;

        boolean serversStarted = false;

        if (!ValidationHelper.isNullOrEmpty(listenPort)) {
            try {
                if (this.serverADTListener != null) {
                    synchServers(this.serverADTListener);
                }

                serversStarted = false;

                this.serverADTListener = new SimpleServer(listenPort,
                        LowerLayerProtocol.makeLLP(), new PipeParser());

                Application handler = new ADTHL7Listener();

                this.serverADTListener.registerApplication("ADT", "A01",
                        handler);
                this.serverADTListener.registerApplication("ADT", "A02",
                        handler);
                this.serverADTListener.registerApplication("ADT", "A03",
                        handler);
                this.serverADTListener.registerApplication("ADT", A08, handler);
                this.serverADTListener.start();

                serversStarted = this.serverADTListener.isRunning();
                if (serversStarted) {
                    currentPort = listenPort;
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            } finally {
                LogHelper.debugInfo(log, "HL7 Receive ADT server start:"
                        + listenPort + ' ' + serversStarted);
            }
        }

        return serversStarted;
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
                    LogHelper.log(log, e);
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

    public Message handleMessage(Message msg, SessionNames serviceName)
            throws HL7Exception, IOException {
        ADTPair adtPair = null;
        boolean result = true;
        Transaction tr = null;
        boolean unknownMessage = false;
        try {
            SessionHolder.getInstance().openSession(serviceName);
            tr = SessionHolder.getInstance().getSession(serviceName)
                    .beginTransaction();
            if (msg instanceof ca.uhn.hl7v2.model.v26.message.ADT_A01) {
                // Maybe you will think that it strange that ADT_A01 and ADT_A08
                // is the same type, but it depend on 3rd library. They decided
                // to
                // use the same Class.

                if (A08.equals(((ca.uhn.hl7v2.model.v26.message.ADT_A01) msg)
                        .getMSH().getMessageType().getMsg2_TriggerEvent()
                        .getValue())) {
                    adtPair = getPatientInfoFromADT_A08((ca.uhn.hl7v2.model.v26.message.ADT_A01) msg);
                    handleAdt08Pair(adtPair, serviceName);
                    // handle A08 message (update event)
                } else {
                    adtPair = getPatientInfoFromADT_A01((ca.uhn.hl7v2.model.v26.message.ADT_A01) msg);
                    handleAdt01Pair(adtPair, serviceName);
                    // handle A01 message (create event)
                }
            } else {
                unknownMessage = true;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                try {
                    tr.rollback();
                } catch (Exception ex) {
                    LogHelper.log(log, ex);
                }
            }
            result = false;
        } finally {
            if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                try {
                    tr.commit();
                } catch (Exception e) {
                    SessionHolder.getInstance().closeSession(serviceName);
                    LogHelper.log(log, e);
                    result = false;
                }
            }
            SessionHolder.getInstance().closeSession(serviceName);
        }

        return generateAck26(msg, result, unknownMessage);
    }

    private ADTPair getPatientInfoFromADT_A01(
            ca.uhn.hl7v2.model.v26.message.ADT_A01 msg) {
        ADTPair co = null;
        try {
            co = new ADTPair();

            co.setPsdNumber(msg.getPV1().getVisitNumber().getIDNumber()
                    .getValue());
            co.setSectorCode(msg.getPV1().getAssignedPatientLocation()
                    .getPl1_PointOfCare().getValue());

            co.setRecoveryDay(msg.getEVN().getEventOccurred().getValueAsDate());

            PID patientInfo = msg.getPID();
            co.setRecoveryKind(msg.getPV1().getPatientClass().getValue());
            // patient info
            extractPatientInfo(co, patientInfo);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        // patient info

        return co;
    }

    private ACK generateAck26(Message msg, boolean result,
                              boolean uncknownMessage) {
        try {
            MSH msh = (MSH) msg.get("MSH");

            @SuppressWarnings("deprecation")
            ACK ack = (ACK) DefaultApplication.makeACK(msh);
            if (result) {
                ack.getMSA().getAcknowledgmentCode().setValue("AA");
            }
            if (!result) {
                ack.getMSA().getAcknowledgmentCode().setValue("AR");
            }

            if (uncknownMessage) {
                ack.getERR().getHL7ErrorCode().getIdentifier().setValue("200");
                ack.getERR().getErr4_Severity().setValue("E");
            }
            return ack;
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    private void handleAdt01Pair(ADTPair adtPair, SessionNames serviceName)
            throws HibernateException, PersistenceBeanException, Exception {
        if (adtPair == null
                || adtPair.getDummyPatient().getCodice_fiscale() == null) {
            return;
        }

        Sector sector = ConnectionManager.get(Sector.class, new Criterion[]{
                Restrictions.eq("code", adtPair.getSectorCode())
        }, SessionHolder.getInstance().getSession(serviceName));

        if (sector == null) {
            throw new Exception(String.format(
                    "Sector with code %s was not found!",
                    adtPair.getSectorCode()));
        }

        List<Patient> listPatients = null;

        listPatients = ConnectionManager.load(Patient.class, new Criterion[]{
                Restrictions.eq("fiscalCode", adtPair.getDummyPatient()
                        .getCodice_fiscale())
        }, SessionHolder.getInstance().getSession(serviceName));

        Patient patient = null;

        if (!ValidationHelper.isNullOrEmpty(listPatients)) {
            patient = listPatients.get(0);
        }
        if (patient == null) {
            patient = new Patient();
        } else {
            patient = adtPair.getDummyPatient().toPatient(-1l, patient,
                    SessionHolder.getInstance().getSession(serviceName));
        }

        ConnectionManager.save(patient,
                SessionHolder.getInstance().getSession(serviceName));
    }

    private void handleAdt08Pair(ADTPair adtPair, SessionNames serviceName)
            throws HibernateException, PersistenceBeanException, Exception {
        if (adtPair == null
                || adtPair.getDummyPatient().getCodice_fiscale() == null) {
            return;
        }

        Sector sector = ConnectionManager.get(Sector.class, new Criterion[]{
                Restrictions.eq("code", adtPair.getSectorCode())
        }, SessionHolder.getInstance().getSession(serviceName));

        if (sector == null) {
            throw new Exception(String.format(
                    "Sector with code %s was not found!",
                    adtPair.getSectorCode()));
        }

        List<Patient> listPatients = null;

        listPatients = ConnectionManager.load(Patient.class, new Criterion[]{
                Restrictions.eq("fiscalCode", adtPair.getDummyPatient()
                        .getCodice_fiscale())
        }, SessionHolder.getInstance().getSession(serviceName));

        Patient patient = null;

        if (!ValidationHelper.isNullOrEmpty(listPatients)) {
            patient = listPatients.get(0);
        }

        if (patient == null) {
            patient = new Patient();
        }
        if (patient != null) {
            patient = adtPair.getDummyPatient().toPatient(-1l, patient,
                    SessionHolder.getInstance().getSession(serviceName));
        }

        ConnectionManager.save(patient,
                SessionHolder.getInstance().getSession(serviceName));
    }

    private ADTPair getPatientInfoFromADT_A08(
            ca.uhn.hl7v2.model.v26.message.ADT_A01 msg) {
        ADTPair co = null;
        try {
            co = new ADTPair();

            co.setPsdNumber(msg.getPV1().getVisitNumber().getIDNumber()
                    .getValue());
            co.setSectorCode(msg.getPV1().getAssignedPatientLocation()
                    .getPl1_PointOfCare().getValue());
            co.setDismissSectorCode(msg.getPV1().getAssignedPatientLocation()
                    .getPl1_PointOfCare().getValue());

            co.setDismissDate(msg.getEVN().getEventOccurred().getValueAsDate());

            PID patientInfo = msg.getPID();
            co.setRecoveryKind(msg.getPV1().getPatientClass().getValue());
            // patient info
            extractPatientInfo(co, patientInfo);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        // patient info

        return co;
    }

    /**
     * @param co
     * @param patientInfo
     * @throws DataTypeException
     */
    private void extractPatientInfo(ADTPair co, PID patientInfo)
            throws DataTypeException {
        XAD residenceAddress = null;
        XAD domesticAddress = null;

        if (patientInfo.getPatientAddress(0) != null
                && patientInfo.getPatientAddress(0).getXad7_AddressType() != null
                && patientInfo.getPatientAddress(0).getXad7_AddressType()
                .getValue() != null) {
            if (residenceAddressCode.equals(patientInfo.getPatientAddress(0)
                    .getXad7_AddressType().getValue().toUpperCase())) {
                residenceAddress = patientInfo.getPatientAddress(0);
            } else if (domicileAddressCode.equals(patientInfo
                    .getPatientAddress(0).getXad7_AddressType().getValue()
                    .toUpperCase())) {
                domesticAddress = patientInfo.getPatientAddress(0);
            }
        }

        if (patientInfo.getPatientAddress(1) != null
                && patientInfo.getPatientAddress(1).getXad7_AddressType() != null
                && patientInfo.getPatientAddress(1).getXad7_AddressType()
                .getValue() != null) {
            if (residenceAddressCode.equals(patientInfo.getPatientAddress(1)
                    .getXad7_AddressType().getValue().toUpperCase())) {
                residenceAddress = patientInfo.getPatientAddress(1);
            } else if (domicileAddressCode.equals(patientInfo
                    .getPatientAddress(1).getXad7_AddressType().getValue()
                    .toUpperCase())) {
                domesticAddress = patientInfo.getPatientAddress(1);
            }
        }

        co.setFiscalCode(patientInfo.getPid3_PatientIdentifierList(2)
                .getIDNumber().getValue());
        co.getDummyPatient().setCodice_fiscale(
                patientInfo.getPid3_PatientIdentifierList(2).getIDNumber()
                        .getValue());
        co.getDummyPatient().setGiven_name(
                patientInfo.getPatientName(0).getGivenName().getValue());
        co.getDummyPatient().setFamily_name(
                patientInfo.getPatientName(0).getFamilyName().getSurname()
                        .getValue());

        co.getDummyPatient().setDob(
                patientInfo.getDateTimeOfBirth().getValueAsDate());

        co.getDummyPatient().setSex(
                patientInfo.getAdministrativeSex().getValue());

        extractPatientAddress(co, residenceAddress, domesticAddress);
    }

    /**
     * @param co
     * @param residenceAddress
     * @param domesticAddress
     */
    private void extractPatientAddress(ADTPair co, XAD residenceAddress,
                                       XAD domesticAddress) {
        if (residenceAddress != null) {
            co.getDummyPatient().setIndirizzo_residenza(
                    residenceAddress.getXad1_StreetAddress()
                            .getStreetOrMailingAddress().getValue());
            co.getDummyPatient().setCap_residenza(
                    residenceAddress.getXad5_ZipOrPostalCode().getValue());
            co.getDummyPatient().setQuartiere_residenza(
                    residenceAddress.getXad4_StateOrProvince().getValue());
            co.getDummyPatient().setProvincia_residenza(
                    residenceAddress.getStateOrProvince().getValue());
        }

        if (domesticAddress != null) {
            co.getDummyPatient().setIndirizzo_domicilio(
                    domesticAddress.getXad1_StreetAddress()
                            .getStreetOrMailingAddress().getValue());
            co.getDummyPatient().setCap_domicilio(
                    domesticAddress.getXad5_ZipOrPostalCode().getValue());
            // co.getDummyPatient().setQuartiere_domicilio(
            // domesticAddress.getXad4_StateOrProvince().getValue());
            co.getDummyPatient().setProvincia_domicilio(
                    domesticAddress.getStateOrProvince().getValue());
        }
    }
}
