package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.integration.hl7.Hl7ReceiveHelper;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.BaseEntityPageBean;
import it.nexera.ris.web.services.*;
import it.nexera.ris.web.services.base.ServiceHolder;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.net.InetAddress;

@Named("monitoringBean")
@ViewScoped
public class MonitoringBean extends BaseEntityPageBean implements Serializable {

    private static final long serialVersionUID = -9198755620346683397L;

    private TempRemoverService tempRemoverService;

    private KeepAliveService keepAliveService;

    private RadiologyRequestDisableService radiologyRequestDisableService;

    private Hl7RadExamRequestMessagesSenderService hl7RadExamRequestMessagesSenderService;

    private SettingSynchronizerService settingSynchronizerService;

    private SessionFilterService sessionFilterService;

    private EmptySessionFilterService emptySessionFilterService;

    private CleanShortRequestService cleanShortRequestService;

    private UserDisableService userDisableService;

    private HL7MessageForwardingService hl7MessageForwardingService;

    private HL7ReportForwardingService hl7ReportForwardingService;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.PageBean#onConstruct()
     */
    @Override
    protected void onConstruct() {
        tempRemoverService = ServiceHolder.getInstance()
                .getTempRemoverService();

        keepAliveService = ServiceHolder.getInstance().getKeepAliveService();

        radiologyRequestDisableService = ServiceHolder.getInstance()
                .getRadiologyRequestDisableService();

        hl7RadExamRequestMessagesSenderService = ServiceHolder.getInstance()
                .getHl7RadExamRequestMessagesSenderService();

        settingSynchronizerService = ServiceHolder.getInstance()
                .getSettingSynchronizerService();

        sessionFilterService = ServiceHolder.getInstance()
                .getSessionFilterService();

        emptySessionFilterService = ServiceHolder.getInstance()
                .getEmptySessionFilterService();

        cleanShortRequestService = ServiceHolder.getInstance()
                .getCleanShortRequestService();

        userDisableService = ServiceHolder.getInstance()
                .getUserDisableService();

        hl7MessageForwardingService = ServiceHolder.getInstance()
                .getHl7MessageForwardingService();

        hl7ReportForwardingService = ServiceHolder.getInstance()
                .getHl7ReportForwardingService();
    }

    public void startHl7Listener() {
        Hl7ReceiveHelper.getInstance().startHl7ReceiveHelper(
                Integer.parseInt(ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.HL7_LISTEN_PORT)
                        .getValue()));
    }

    public void stopHl7Listener() {
        Hl7ReceiveHelper.getInstance().stopHl7ReceiveHelper();
    }

    public String getIPAddress() {
        InetAddress thisIp = null;
        try {
            thisIp = InetAddress.getLocalHost();
        } catch (Exception e) {

        }
        return thisIp == null ? "" : thisIp.getHostAddress();
    }

    public Boolean getReceiverRunning() {
        boolean isRun = false;
        try {
            if (Hl7ReceiveHelper.getInstance().getHl7ReceiveHelperStatus() != null) {
                isRun = Hl7ReceiveHelper.getInstance()
                        .getHl7ReceiveHelperStatus().isRunning();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return isRun;
    }

    public int getLaboratoryCurrentPort() {
        return Hl7ReceiveHelper.getInstance().getHl7ReceiveHelperStatus()
                .getPort();
    }

    public TempRemoverService getTempRemoverService() {
        return tempRemoverService;
    }

    public void setTempRemoverService(TempRemoverService tempRemoverService) {
        this.tempRemoverService = tempRemoverService;
    }

    public KeepAliveService getKeepAliveService() {
        return keepAliveService;
    }

    public void setKeepAliveService(KeepAliveService keepAliveService) {
        this.keepAliveService = keepAliveService;
    }

    public Hl7RadExamRequestMessagesSenderService getHl7RadExamRequestMessagesSenderService() {
        return hl7RadExamRequestMessagesSenderService;
    }

    public void setHl7RadExamRequestMessagesSenderService(
            Hl7RadExamRequestMessagesSenderService hl7RadExamRequestMessagesSenderService) {
        this.hl7RadExamRequestMessagesSenderService = hl7RadExamRequestMessagesSenderService;
    }

    public RadiologyRequestDisableService getRadiologyRequestDisableService() {
        return radiologyRequestDisableService;
    }

    public void setRadiologyRequestDisableService(
            RadiologyRequestDisableService radiologyRequestDisableService) {
        this.radiologyRequestDisableService = radiologyRequestDisableService;
    }

    public SettingSynchronizerService getSettingSynchronizerService() {
        return settingSynchronizerService;
    }

    public void setSettingSynchronizerService(
            SettingSynchronizerService settingSynchronizerService) {
        this.settingSynchronizerService = settingSynchronizerService;
    }

    public SessionFilterService getSessionFilterService() {
        return sessionFilterService;
    }

    public void setSessionFilterService(SessionFilterService sessionFilterService) {
        this.sessionFilterService = sessionFilterService;
    }

    public EmptySessionFilterService getEmptySessionFilterService() {
        return emptySessionFilterService;
    }

    public void setEmptySessionFilterService(EmptySessionFilterService emptySessionFilterService) {
        this.emptySessionFilterService = emptySessionFilterService;
    }

    public CleanShortRequestService getCleanShortRequestService() {
        return cleanShortRequestService;
    }

    public void setCleanShortRequestService(CleanShortRequestService cleanShortRequestService) {
        this.cleanShortRequestService = cleanShortRequestService;
    }

    public UserDisableService getUserDisableService() {
        return userDisableService;
    }

    public void setUserDisableService(UserDisableService userDisableService) {
        this.userDisableService = userDisableService;
    }

    public HL7MessageForwardingService getHl7MessageForwardingService() {
        return hl7MessageForwardingService;
    }

    public void setHl7MessageForwardingService(HL7MessageForwardingService hl7MessageForwardingService) {
        this.hl7MessageForwardingService = hl7MessageForwardingService;
    }

    public HL7ReportForwardingService getHl7ReportForwardingService() {
        return hl7ReportForwardingService;
    }

    public void setHl7ReportForwardingService(HL7ReportForwardingService hl7ReportForwardingService) {
        this.hl7ReportForwardingService = hl7ReportForwardingService;
    }
}
