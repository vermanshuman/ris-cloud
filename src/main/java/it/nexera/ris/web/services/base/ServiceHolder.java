package it.nexera.ris.web.services.base;

import it.nexera.ris.web.services.*;

public class ServiceHolder {
    private KeepAliveService keepAliveService;

    private RadiologyRequestDisableService radiologyRequestDisableService;

    private TempRemoverService tempRemoverService;

    private Hl7RadExamRequestMessagesSenderService hl7RadExamRequestMessagesSenderService;

    private SettingSynchronizerService settingSynchronizerService;

    private SessionFilterService sessionFilterService;

    private EmptySessionFilterService emptySessionFilterService;

    private CleanShortRequestService cleanShortRequestService;

    private UserDisableService userDisableService;

    private HL7MessageForwardingService hl7MessageForwardingService;

    private HL7ReportForwardingService hl7ReportForwardingService;

    private static ServiceHolder instance;

    public static synchronized ServiceHolder getInstance() {
        if (instance == null) {
            instance = new ServiceHolder();
        }
        return instance;
    }

    private ServiceHolder() {
    }

    public KeepAliveService getKeepAliveService() {
        return keepAliveService;
    }

    public void setKeepAliveService(KeepAliveService keepAliveService) {
        this.keepAliveService = keepAliveService;
    }

    public TempRemoverService getTempRemoverService() {
        return tempRemoverService;
    }

    public void setTempRemoverService(TempRemoverService tempRemoverService) {
        this.tempRemoverService = tempRemoverService;
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
