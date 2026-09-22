package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.ApplicationSettingsValue;
import it.nexera.ris.persistence.integration.ADTIntegrationHelper;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.ApplicationSettingsValueWrapper;
import org.hibernate.HibernateException;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

@Named("appSettingsBean")
@ViewScoped
public class ApplicationSettingsEditBean extends
        EntityEditPageBean<ApplicationSettingsValue> implements Serializable {
    private static final long serialVersionUID = 2780565527350538548L;

    // Common settings
    private ApplicationSettingsValueWrapper settingPasswordPeriodExpiration;

    private ApplicationSettingsValueWrapper asapSioServerAddress;

    private ApplicationSettingsValueWrapper settingASAPSIOIntegrationUrl;

    private ApplicationSettingsValueWrapper settingASAPSIOIntegrationUsername;

    private ApplicationSettingsValueWrapper settingASAPSIOIntegrationPassword;

    private ApplicationSettingsValueWrapper asapSioUserForPsd;

    private ApplicationSettingsValueWrapper asapSioPasswordForPsd;

    private ApplicationSettingsValueWrapper settingRequestExpiration;

    private ApplicationSettingsValueWrapper settingStatisticServerIp;

    private ApplicationSettingsValueWrapper settingsSignatureDaysLimit;

    // private ApplicationSettingsValueWrapper settingInsertPdf;

    private String ASAPSIOPassword;

    private String ASAPSIOPasswordConfirmation;

    private Boolean ASAPSIOCheckConnectionResult;

    // HL7 settings

    private ApplicationSettingsValueWrapper settingHL7ListenPort;

    private ApplicationSettingsValueWrapper settingHL7SendIp;

    private ApplicationSettingsValueWrapper settingHL7SendPort;

    private ApplicationSettingsValueWrapper settingHL7MDMSendRetryTime;

    private ApplicationSettingsValueWrapper settingHL7ORMSendRetryTime;

    private ApplicationSettingsValueWrapper settingRisRequestSendPollingTime;

    private ApplicationSettingsValueWrapper settingsHL7DaysLimit;

    private ApplicationSettingsValueWrapper settingsHL7MaxCountLimit;

    // SMTP settings

//    private ApplicationSettingsValueWrapper settingSMTPServer;
//
//    private ApplicationSettingsValueWrapper settingSMTPPort;
//
//    private ApplicationSettingsValueWrapper settingSMTPUsername;
//
//    private ApplicationSettingsValueWrapper settingSMTPPassword;

    private ApplicationSettingsValueWrapper settingSMTPMessageSubject;

    private ApplicationSettingsValueWrapper settingSMTPMessage;

    // FILE ENTITY

    private ApplicationSettingsValueWrapper settingFileEntityPath;

    //SESSION TIMEOUT

    private ApplicationSettingsValueWrapper settingSessionTimeout;

    private ApplicationSettingsValueWrapper settingSessionCheckTimeout;

    private ApplicationSettingsValueWrapper settingSessionTimeoutWithoutUser;

    private ApplicationSettingsValueWrapper settingSessionTimeoutIntervalWithoutUser;

    private ApplicationSettingsValueWrapper settingCleanShortRequestInterval;

    private ApplicationSettingsValueWrapper settingQueryTimeout;

    private ApplicationSettingsValueWrapper settingTokenExpiration;

    private ApplicationSettingsValueWrapper settingFSEGatewayEndpoint;

    // IMAGE

    private ApplicationSettingsValueWrapper settingImagePath;

    // Disable Users

    private ApplicationSettingsValueWrapper settingUserDisablePeriod;

    private ApplicationSettingsValueWrapper settingUserDisablePollingTime;

    // HL7 Repo Settings
    private ApplicationSettingsValueWrapper settingHL7RepoSendIp;

    private ApplicationSettingsValueWrapper settingHL7SendRepoPort;

    private ApplicationSettingsValueWrapper settingHl7RequestSendPollingTime;

    // HL7 Report Forwarding Settings
    private ApplicationSettingsValueWrapper settingHL7ReportForwardingSendIp;

    private ApplicationSettingsValueWrapper settingHL7ReportForwardingPort;

    private ApplicationSettingsValueWrapper settingHL7ReportForwardingPollingTime;

    private ApplicationSettingsValueWrapper settingHl7ReportForwardingStartDate;

    private ApplicationSettingsValueWrapper settingHl7ReportForwardingHours;

    private Date hl7ReportForwardingStartDate;

    private ApplicationSettingsValueWrapper settingsIncludeCDA2InPDF;

    private Boolean includeCDA2InPDF;

    private ApplicationSettingsValueWrapper settingsEnableValidationCDA;

    private Boolean enableValidationCDA;
    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        fillSettingsValues();
    }

    private void fillSettingsValues() {
        setSettingPasswordPeriodExpiration(ApplicationSettingsHolder
                .getInstance().getByKey(
                        ApplicationSettingsKeys.PASSWORD_EXPIRATION_PERIOD));

        setSettingRequestExpiration(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.REQUEST_EXPIRATION_PERIOD));

        setSettingsSignatureDaysLimit(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SIGNATURE_DAYS_LIMIT));

        if (ValidationHelper
                .isNullOrEmpty(getSettingsSignatureDaysLimit())) {
            getSettingsSignatureDaysLimit().setValue("0");
        }

        setAsapSioServerAddress(ApplicationSettingsHolder.getInstance()

                .getByKey(ApplicationSettingsKeys.ASAP_SIO_ADDRESS));

        setSettingStatisticServerIp(ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.STATISTIC_SERVER_IP));

        setSettingASAPSIOIntegrationUrl(ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_URL));

        setSettingASAPSIOIntegrationUsername(ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_USERNAME));

        setSettingASAPSIOIntegrationPassword(ApplicationSettingsHolder
                .getInstance()
                .getByKey(
                        ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_PASSWORD));

        setAsapSioUserForPsd(ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.ASAP_SIO_USER_FOR_PSD));

        setAsapSioPasswordForPsd(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.ASAP_SIO_PASSWORD_FOR_PSD));

        setSettingHL7ListenPort(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_LISTEN_PORT));

        setSettingHL7SendIp(ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.HL7_SEND_IP));
        setSettingHL7SendPort(ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.HL7_SEND_PORT));
        setSettingHL7MDMSendRetryTime(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_MDM_SEND_RETRY_TIME));
        setSettingHL7ORMSendRetryTime(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_ORM_SEND_RETRY_TIME));

        setSettingHL7RepoSendIp(ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.HL7_SEND_REPO_IP));
        setSettingHL7SendRepoPort(ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.HL7_SEND_REPO_PORT));
        setSettingHl7RequestSendPollingTime(ApplicationSettingsHolder
                .getInstance().getByKey(
                        ApplicationSettingsKeys.HL7_REQUEST_SEND_POLLING_TIME));
        setSettingsHL7DaysLimit(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_DAYS_LIMIT));
        setSettingsHL7MaxCountLimit(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_MAX_SEND_COUNT_LIMIT));


        setSettingHL7ReportForwardingSendIp(ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.HL7_REPORT_FORWARDING_IP));
        setSettingHL7ReportForwardingPort(ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.HL7_REPORT_FORWARDING_PORT));
        setSettingHL7ReportForwardingPollingTime(ApplicationSettingsHolder
                .getInstance().getByKey(
                        ApplicationSettingsKeys.HL7_REPORT_FORWARDING_POLLING_TIME));

        ApplicationSettingsValueWrapper hl7ReportForwardingStartDate = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_REPORT_FORWARDING_START_DATE);
        setSettingHl7ReportForwardingStartDate(hl7ReportForwardingStartDate);
        if(!ValidationHelper.isNullOrEmpty(hl7ReportForwardingStartDate)
                && !ValidationHelper.isNullOrEmpty(hl7ReportForwardingStartDate.getValue())){
            setHl7ReportForwardingStartDate(DateTimeHelper.fromString(hl7ReportForwardingStartDate.getValue(),
                    DateTimeHelper.getDatePattern()));
        }
        setSettingHl7ReportForwardingHours(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_REPORT_FORWARDING_HOURS));

//        setSettingSMTPServer(ApplicationSettingsHolder.getInstance().getByKey(
//                ApplicationSettingsKeys.SMTP_SERVER));
//        setSettingSMTPPort(ApplicationSettingsHolder.getInstance().getByKey(
//                ApplicationSettingsKeys.SMTP_PORT));
//        setSettingSMTPUsername(ApplicationSettingsHolder.getInstance().getByKey(
//                ApplicationSettingsKeys.SMTP_USERNAME));
//        setSettingSMTPPassword(ApplicationSettingsHolder.getInstance().getByKey(
//                ApplicationSettingsKeys.SMTP_PASSWORD));

        setSettingRisRequestSendPollingTime(ApplicationSettingsHolder
                .getInstance().getByKey(
                        ApplicationSettingsKeys.RIS_REQUEST_SEND_POLLING_TIME));

        setSettingFileEntityPath(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.FILE_ENTITY_PATH));
        setSettingImagePath(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.IMAGE_PATH));

        setSettingUserDisablePeriod(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.USER_DISABLE_PERIOD));
        setSettingUserDisablePollingTime(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.USER_DISABLE_POLLING_TIME));

        setSettingSessionCheckTimeout(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SESSION_CHECK_TIMEOUT));
        setSettingSessionTimeout(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT));
        setSettingSessionTimeoutWithoutUser(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT_WITHOUT_USER));
        setSettingSessionTimeoutIntervalWithoutUser(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT_INTERVAL_WITHOUT_USER));
        setSettingCleanShortRequestInterval(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.CLEAN_SHORT_REQUEST_INTERVAL));
        setSettingQueryTimeout(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.QUERY_TIMEOUT));
        setSettingTokenExpiration(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.TOKEN_EXPIRATION));


//        setSettingInsertPdf(ApplicationSettingsHolder.getInstance()
//                .getByKey(ApplicationSettingsKeys.INSERT_PDF_INTO_CDA2));
        ApplicationSettingsValueWrapper includeCDA2InPDF = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.INCLUDE_CDA2_IN_PDF);
        setSettingsIncludeCDA2InPDF(includeCDA2InPDF);
        if(!ValidationHelper.isNullOrEmpty(includeCDA2InPDF)
                && !ValidationHelper.isNullOrEmpty(includeCDA2InPDF.getValue())){
            setIncludeCDA2InPDF(Boolean.valueOf(includeCDA2InPDF.getValue()));
        }
        ApplicationSettingsValueWrapper enableValidationCDA = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.ENABLE_VALIDATION_CDA2);
        setSettingsEnableValidationCDA(enableValidationCDA);
        if(!ValidationHelper.isNullOrEmpty(enableValidationCDA)
                && !ValidationHelper.isNullOrEmpty(enableValidationCDA.getValue())){
            setEnableValidationCDA(Boolean.valueOf(enableValidationCDA.getValue()));
        }
        setSettingFSEGatewayEndpoint(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.FSE_GATEWAY_ENDPOINT));
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        validateASAPSIOSettings();
    }

    public void checkGeneralConnection() {
        try {
            this.ASAPSIOCheckConnectionResult = Boolean.FALSE;
            ASAPSIOCheckConnectionResult = ADTIntegrationHelper.getInstance()
                    .checkConnection(
                            this.getSettingASAPSIOIntegrationUrl().getValue(),
                            this.getSettingASAPSIOIntegrationUsername()
                                    .getValue(),
                            ValidationHelper.isNullOrEmpty(this
                                    .getASAPSIOPassword()) ? this
                                    .getSettingASAPSIOIntegrationPassword()
                                    .getValue() : this.getASAPSIOPassword());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void validateASAPSIOSettings() {
        if (ValidationHelper.isNullOrEmpty(this
                .getSettingASAPSIOIntegrationUrl().getValue())) {
            this.addRequiredFieldExeption("tab:generalUrl");
        }

        if (ValidationHelper.isNullOrEmpty(this
                .getSettingASAPSIOIntegrationUsername().getValue())) {
            this.addRequiredFieldExeption("tab:generalUsername");
        }

        boolean passwordAlreadySet = !ValidationHelper
                .isNullOrEmpty(getSettingASAPSIOIntegrationPassword()
                        .getValue());

        if (!passwordAlreadySet
                && ValidationHelper.isNullOrEmpty(this.getASAPSIOPassword()
                .trim())) {
            this.addRequiredFieldExeption("tab:generalPassword");
        }

        if (!passwordAlreadySet
                && ValidationHelper.isNullOrEmpty(this
                .getASAPSIOPasswordConfirmation().trim())) {
            this.addRequiredFieldExeption("tab:generalPasswordConfirmation");
        }

        if (ValidationHelper.isNullOrEmpty(this.getSettingStatisticServerIp()
                .getValue()) || (!this.getSettingStatisticServerIp()
                .getValue().startsWith("http://") && !this.getSettingStatisticServerIp()
                .getValue().startsWith("https://"))) {
            this.addRequiredFieldExeption("tab:statisticServerIp");
        }

        if (!passwordAlreadySet) {
            if (!ValidationHelper.isNullOrEmpty(this
                    .getASAPSIOPasswordConfirmation().trim())
                    && !ValidationHelper.isNullOrEmpty(this
                    .getASAPSIOPassword().trim())
                    && !this.getASAPSIOPasswordConfirmation().trim()
                    .equals(this.getASAPSIOPassword().trim())) {
                this.addFieldExeption("tab:generalPassword",
                        "passwordMissmatch");
                this.addFieldExeption("tab:generalPasswordConfirmation",
                        "passwordMissmatch", Boolean.FALSE);
            }
        } else {
            if (ValidationHelper.isNullOrEmpty(this.getASAPSIOPassword())
                    && ValidationHelper.isNullOrEmpty(this
                    .getASAPSIOPasswordConfirmation())) {
                return;
            }

            if (!ValidationHelper.isNullOrEmpty(this.getASAPSIOPassword())
                    && ValidationHelper.isNullOrEmpty(this
                    .getASAPSIOPasswordConfirmation())) {
                this.addFieldExeption("tab:generalPassword",
                        "passwordMissmatch");
                this.addFieldExeption("tab:generalPasswordConfirmation",
                        "passwordMissmatch", Boolean.FALSE);
            } else if (ValidationHelper.isNullOrEmpty(this.getASAPSIOPassword())
                    && !ValidationHelper.isNullOrEmpty(this
                    .getASAPSIOPasswordConfirmation())) {
                this.addFieldExeption("tab:generalPassword",
                        "passwordMissmatch");
                this.addFieldExeption("tab:generalPassword",
                        "passwordMissmatch", Boolean.FALSE);
            } else if (!this.getASAPSIOPasswordConfirmation().trim()
                    .equals(this.getASAPSIOPassword().trim())) {
                this.addFieldExeption("tab:generalPassword",
                        "passwordMissmatch");
                this.addFieldExeption("tab:generalPassword",
                        "passwordMissmatch", Boolean.FALSE);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (ValidationHelper
                .isNullOrEmpty(getSettingPasswordPeriodExpiration())) {
            getSettingPasswordPeriodExpiration().setValue("0");
        }

        Integer value = Integer.parseInt(getSettingPasswordPeriodExpiration()
                .getValue());
        getSettingPasswordPeriodExpiration().setValue(String.valueOf(value));

        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingPasswordPeriodExpiration());

        if (ValidationHelper.isNullOrEmpty(getSettingRequestExpiration().getValue())) {
            getSettingRequestExpiration().setValue("0");
        }

        Integer settingRequestExpirationValue = Integer
                .parseInt(getSettingRequestExpiration().getValue());
        getSettingRequestExpiration().setValue(
                String.valueOf(settingRequestExpirationValue));

        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingRequestExpiration());


        if (ValidationHelper.isNullOrEmpty(getSettingsSignatureDaysLimit().getValue())) {
            getSettingsSignatureDaysLimit().setValue("0");
        }
        Integer signatureDaysLimitValue = Integer
                .parseInt(getSettingsSignatureDaysLimit().getValue());
        getSettingsSignatureDaysLimit().setValue(
                String.valueOf(signatureDaysLimitValue));
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingsSignatureDaysLimit());

        if (!ValidationHelper.isNullOrEmpty(this.getASAPSIOPassword())) {
            getSettingASAPSIOIntegrationPassword().setValue(
                    this.getASAPSIOPassword());
        }
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingASAPSIOIntegrationUrl());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingASAPSIOIntegrationUsername());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingASAPSIOIntegrationPassword());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingStatisticServerIp());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getAsapSioServerAddress());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getAsapSioUserForPsd());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getAsapSioPasswordForPsd());

        //FILE ENTITY

        if (getSettingFileEntityPath() != null && (getSettingFileEntityPath().getValue().endsWith("\\") || getSettingFileEntityPath().getValue().endsWith("/"))) {
            getSettingFileEntityPath().setValue(getSettingFileEntityPath().getValue().substring(0, getSettingFileEntityPath().getValue().length() - 1));
        }

        ApplicationSettingsHolder.getInstance().applyNewValue(getSettingFileEntityPath());

        //IMAGE

        if (getSettingImagePath() != null && (getSettingImagePath().getValue().endsWith("\\") || getSettingImagePath().getValue().endsWith("/"))) {
            getSettingImagePath().setValue(getSettingImagePath().getValue().substring(0, getSettingImagePath().getValue().length() - 1));
        }

        ApplicationSettingsHolder.getInstance().applyNewValue(getSettingImagePath());

        //Disable Users

        ApplicationSettingsHolder.getInstance().applyNewValue(getSettingUserDisablePeriod());
        ApplicationSettingsHolder.getInstance().applyNewValue(getSettingUserDisablePollingTime());

        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7ListenPort());

        // HL7
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7SendIp());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7SendPort());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7MDMSendRetryTime());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7ORMSendRetryTime());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7RepoSendIp());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7SendRepoPort());

        if (ValidationHelper.isNullOrEmpty(getSettingHl7RequestSendPollingTime().getValue())) {
            getSettingHl7RequestSendPollingTime().setValue("5");
        }
        Integer hl7RequestSendPollingTime = Integer
                .parseInt(getSettingHl7RequestSendPollingTime().getValue());
        getSettingHl7RequestSendPollingTime().setValue(
                String.valueOf(hl7RequestSendPollingTime));
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHl7RequestSendPollingTime());

        if (ValidationHelper.isNullOrEmpty(getSettingsHL7DaysLimit().getValue())) {
            getSettingsHL7DaysLimit().setValue("1");
        }
        Integer hl7DaysLimitValue = Integer
                .parseInt(getSettingsHL7DaysLimit().getValue());
        getSettingsHL7DaysLimit().setValue(
                String.valueOf(hl7DaysLimitValue));
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingsHL7DaysLimit());

        if (ValidationHelper.isNullOrEmpty(getSettingsHL7MaxCountLimit().getValue())) {
            getSettingsHL7MaxCountLimit().setValue("1");
        }
        Integer hl7MaxCountLimitValue = Integer
                .parseInt(getSettingsHL7MaxCountLimit().getValue());
        getSettingsHL7MaxCountLimit().setValue(
                String.valueOf(hl7MaxCountLimitValue));
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingsHL7MaxCountLimit());

        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7ReportForwardingSendIp());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7ReportForwardingPort());

        if (ValidationHelper.isNullOrEmpty(getSettingHL7ReportForwardingPollingTime().getValue())) {
            getSettingHL7ReportForwardingPollingTime().setValue("12:00");
        }
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHL7ReportForwardingPollingTime());
        String hl7ReportForwardingStartDate = DateTimeHelper.toFormatedString(
                getHl7ReportForwardingStartDate(), DateTimeHelper.getDatePattern());
        try {
            getSettingHl7ReportForwardingStartDate().setValue(hl7ReportForwardingStartDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHl7ReportForwardingStartDate());
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHl7ReportForwardingHours());
        Integer hl7ReportForwardingHours = Integer
                .parseInt(getSettingHl7ReportForwardingHours().getValue());
        getSettingHl7ReportForwardingHours().setValue(
                String.valueOf(hl7ReportForwardingHours));
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingHl7ReportForwardingHours());

        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingRisRequestSendPollingTime());

        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingSessionCheckTimeout());
        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingSessionTimeout());
        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingSessionTimeoutWithoutUser());
        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingSessionTimeoutIntervalWithoutUser());
        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingCleanShortRequestInterval());
        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingQueryTimeout());
        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingTokenExpiration());
        String includeCDA2InPDF = Boolean.toString(getIncludeCDA2InPDF());
        try {
            getSettingsIncludeCDA2InPDF().setValue(includeCDA2InPDF);
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
        }
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingsIncludeCDA2InPDF());
        String enableValidationCDA = Boolean.toString(getEnableValidationCDA());
        try {
            getSettingsEnableValidationCDA().setValue(enableValidationCDA);
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
        }
        ApplicationSettingsHolder.getInstance().applyNewValue(
                getSettingsEnableValidationCDA());
        ApplicationSettingsHolder.getInstance()
                .applyNewValue(getSettingFSEGatewayEndpoint());
    }

    public ApplicationSettingsValueWrapper getSettingPasswordPeriodExpiration() {
        return settingPasswordPeriodExpiration;
    }

    public void setSettingPasswordPeriodExpiration(
            ApplicationSettingsValueWrapper settingPasswordPeriodExpiration) {
        this.settingPasswordPeriodExpiration = settingPasswordPeriodExpiration;
    }

    public ApplicationSettingsValueWrapper getSettingASAPSIOIntegrationUrl() {
        return settingASAPSIOIntegrationUrl;
    }

    public void setSettingASAPSIOIntegrationUrl(
            ApplicationSettingsValueWrapper settingGeneralIntegrationUrl) {
        this.settingASAPSIOIntegrationUrl = settingGeneralIntegrationUrl;
    }

    public ApplicationSettingsValueWrapper getSettingASAPSIOIntegrationUsername() {
        return settingASAPSIOIntegrationUsername;
    }

    public void setSettingASAPSIOIntegrationUsername(
            ApplicationSettingsValueWrapper settingGeneralIntegrationUsername) {
        this.settingASAPSIOIntegrationUsername = settingGeneralIntegrationUsername;
    }

    public ApplicationSettingsValueWrapper getSettingASAPSIOIntegrationPassword() {
        return settingASAPSIOIntegrationPassword;
    }

    public void setSettingASAPSIOIntegrationPassword(
            ApplicationSettingsValueWrapper settingGeneralIntegrationPassword) {
        this.settingASAPSIOIntegrationPassword = settingGeneralIntegrationPassword;
    }

    public String getASAPSIOPassword() {
        return ASAPSIOPassword;
    }

    public void setASAPSIOPassword(String generalPassword) {
        this.ASAPSIOPassword = generalPassword;
    }

    public String getASAPSIOPasswordConfirmation() {
        return ASAPSIOPasswordConfirmation;
    }

    public void setASAPSIOPasswordConfirmation(
            String generalPasswordConfirmation) {
        this.ASAPSIOPasswordConfirmation = generalPasswordConfirmation;
    }

    public Boolean getASAPSIOCheckConnectionResult() {
        return ASAPSIOCheckConnectionResult;
    }

    public void setASAPSIOCheckConnectionResult(
            Boolean generalCheckConnectionResult) {
        this.ASAPSIOCheckConnectionResult = generalCheckConnectionResult;
    }

    public ApplicationSettingsValueWrapper getSettingHL7ListenPort() {
        return settingHL7ListenPort;
    }

    public void setSettingHL7ListenPort(
            ApplicationSettingsValueWrapper settingHL7ListenPort) {
        this.settingHL7ListenPort = settingHL7ListenPort;
    }

    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.HOME);
    }

    public ApplicationSettingsValueWrapper getSettingHL7SendIp() {
        return settingHL7SendIp;
    }

    public void setSettingHL7SendIp(
            ApplicationSettingsValueWrapper settingHL7SendIp) {
        this.settingHL7SendIp = settingHL7SendIp;
    }

    public ApplicationSettingsValueWrapper getSettingHL7SendPort() {
        return settingHL7SendPort;
    }

    public void setSettingHL7SendPort(
            ApplicationSettingsValueWrapper settingHL7SendPort) {
        this.settingHL7SendPort = settingHL7SendPort;
    }

    public ApplicationSettingsValueWrapper getSettingHL7MDMSendRetryTime() {
        return settingHL7MDMSendRetryTime;
    }

    public void setSettingHL7MDMSendRetryTime(
            ApplicationSettingsValueWrapper settingHL7MDMSendRetryTime) {
        this.settingHL7MDMSendRetryTime = settingHL7MDMSendRetryTime;
    }

    public ApplicationSettingsValueWrapper getSettingHL7ORMSendRetryTime() {
        return settingHL7ORMSendRetryTime;
    }

    public void setSettingHL7ORMSendRetryTime(
            ApplicationSettingsValueWrapper settingHL7ORMSendRetryTime) {
        this.settingHL7ORMSendRetryTime = settingHL7ORMSendRetryTime;
    }

    public ApplicationSettingsValueWrapper getAsapSioServerAddress() {
        return asapSioServerAddress;
    }

    public void setAsapSioServerAddress(
            ApplicationSettingsValueWrapper asapSioServerAddress) {
        this.asapSioServerAddress = asapSioServerAddress;
    }

    public ApplicationSettingsValueWrapper getAsapSioUserForPsd() {
        return asapSioUserForPsd;
    }

    public void setAsapSioUserForPsd(
            ApplicationSettingsValueWrapper asapSioUserForPsd) {
        this.asapSioUserForPsd = asapSioUserForPsd;
    }

    public ApplicationSettingsValueWrapper getAsapSioPasswordForPsd() {
        return asapSioPasswordForPsd;
    }

    public void setAsapSioPasswordForPsd(
            ApplicationSettingsValueWrapper asapSioPasswordForPsd) {
        this.asapSioPasswordForPsd = asapSioPasswordForPsd;
    }

    public ApplicationSettingsValueWrapper getSettingRisRequestSendPollingTime() {
        return settingRisRequestSendPollingTime;
    }

    public void setSettingRisRequestSendPollingTime(
            ApplicationSettingsValueWrapper settingRisRequestSendPollingTime) {
        this.settingRisRequestSendPollingTime = settingRisRequestSendPollingTime;
    }

    public ApplicationSettingsValueWrapper getSettingRequestExpiration() {
        return settingRequestExpiration;
    }

    public void setSettingRequestExpiration(
            ApplicationSettingsValueWrapper settingRequestExpiration) {
        this.settingRequestExpiration = settingRequestExpiration;
    }

    public ApplicationSettingsValueWrapper getSettingStatisticServerIp() {
        return settingStatisticServerIp;
    }

    public void setSettingStatisticServerIp(
            ApplicationSettingsValueWrapper settingStatisticServerIp) {
        this.settingStatisticServerIp = settingStatisticServerIp;
    }

    public ApplicationSettingsValueWrapper getSettingFileEntityPath() {
        return settingFileEntityPath;
    }

    public void setSettingFileEntityPath(
            ApplicationSettingsValueWrapper settingFileEntityPath) {
        this.settingFileEntityPath = settingFileEntityPath;
    }

    public ApplicationSettingsValueWrapper getSettingSessionTimeout() {
        return settingSessionTimeout;
    }

    public void setSettingSessionTimeout(
            ApplicationSettingsValueWrapper settingSessionTimeout) {
        this.settingSessionTimeout = settingSessionTimeout;
    }

    public ApplicationSettingsValueWrapper getSettingSessionCheckTimeout() {
        return settingSessionCheckTimeout;
    }

    public void setSettingSessionCheckTimeout(
            ApplicationSettingsValueWrapper settingSessionCheckTimeout) {
        this.settingSessionCheckTimeout = settingSessionCheckTimeout;
    }

    public ApplicationSettingsValueWrapper getSettingSessionTimeoutWithoutUser() {
        return settingSessionTimeoutWithoutUser;
    }

    public void setSettingSessionTimeoutWithoutUser(ApplicationSettingsValueWrapper settingSessionTimeoutWithoutUser) {
        this.settingSessionTimeoutWithoutUser = settingSessionTimeoutWithoutUser;
    }

    public ApplicationSettingsValueWrapper getSettingSessionTimeoutIntervalWithoutUser() {
        return settingSessionTimeoutIntervalWithoutUser;
    }

    public void setSettingSessionTimeoutIntervalWithoutUser(ApplicationSettingsValueWrapper settingSessionTimeoutIntervalWithoutUser) {
        this.settingSessionTimeoutIntervalWithoutUser = settingSessionTimeoutIntervalWithoutUser;
    }

    public ApplicationSettingsValueWrapper getSettingCleanShortRequestInterval() {
        return settingCleanShortRequestInterval;
    }

    public void setSettingCleanShortRequestInterval(ApplicationSettingsValueWrapper settingCleanShortRequestInterval) {
        this.settingCleanShortRequestInterval = settingCleanShortRequestInterval;
    }

    public ApplicationSettingsValueWrapper getSettingQueryTimeout() {
        return settingQueryTimeout;
    }

    public void setSettingQueryTimeout(ApplicationSettingsValueWrapper settingQueryTimeout) {
        this.settingQueryTimeout = settingQueryTimeout;
    }

    public ApplicationSettingsValueWrapper getSettingImagePath() {
        return settingImagePath;
    }

    public void setSettingImagePath(ApplicationSettingsValueWrapper settingImagePath) {
        this.settingImagePath = settingImagePath;
    }

    public ApplicationSettingsValueWrapper getSettingUserDisablePeriod() {
        return settingUserDisablePeriod;
    }

    public void setSettingUserDisablePeriod(ApplicationSettingsValueWrapper settingUserDisablePeriod) {
        this.settingUserDisablePeriod = settingUserDisablePeriod;
    }

    public ApplicationSettingsValueWrapper getSettingUserDisablePollingTime() {
        return settingUserDisablePollingTime;
    }

    public void setSettingUserDisablePollingTime(ApplicationSettingsValueWrapper settingUserDisablePollingTime) {
        this.settingUserDisablePollingTime = settingUserDisablePollingTime;
    }

//    public ApplicationSettingsValueWrapper getSettingSMTPServer() {
//        return settingSMTPServer;
//    }
//
//    public void setSettingSMTPServer(ApplicationSettingsValueWrapper settingSMTPServer) {
//        this.settingSMTPServer = settingSMTPServer;
//    }
//
//    public ApplicationSettingsValueWrapper getSettingSMTPPort() {
//        return settingSMTPPort;
//    }
//
//    public void setSettingSMTPPort(ApplicationSettingsValueWrapper settingSMTPPort) {
//        this.settingSMTPPort = settingSMTPPort;
//    }
//
//    public ApplicationSettingsValueWrapper getSettingSMTPUsername() {
//        return settingSMTPUsername;
//    }
//
//    public void setSettingSMTPUsername(ApplicationSettingsValueWrapper settingSMTPUsername) {
//        this.settingSMTPUsername = settingSMTPUsername;
//    }
//
//    public ApplicationSettingsValueWrapper getSettingSMTPPassword() {
//        return settingSMTPPassword;
//    }
//
//    public void setSettingSMTPPassword(ApplicationSettingsValueWrapper settingSMTPPassword) {
//        this.settingSMTPPassword = settingSMTPPassword;
//    }

//    public ApplicationSettingsValueWrapper getSettingInsertPdf() {
//        return settingInsertPdf;
//    }
//
//    public void setSettingInsertPdf(ApplicationSettingsValueWrapper settingInsertPdf) {
//        this.settingInsertPdf = settingInsertPdf;
//    }

    public ApplicationSettingsValueWrapper getSettingHL7RepoSendIp() {
        return settingHL7RepoSendIp;
    }

    public void setSettingHL7RepoSendIp(ApplicationSettingsValueWrapper settingHL7RepoSendIp) {
        this.settingHL7RepoSendIp = settingHL7RepoSendIp;
    }
//
    public ApplicationSettingsValueWrapper getSettingHL7SendRepoPort() {
        return settingHL7SendRepoPort;
    }

    public void setSettingHL7SendRepoPort(ApplicationSettingsValueWrapper settingHL7SendRepoPort) {
        this.settingHL7SendRepoPort = settingHL7SendRepoPort;
    }

    public ApplicationSettingsValueWrapper getSettingsSignatureDaysLimit() {
        return settingsSignatureDaysLimit;
    }

    public void setSettingsSignatureDaysLimit(ApplicationSettingsValueWrapper settingsSignatureDaysLimit) {
        this.settingsSignatureDaysLimit = settingsSignatureDaysLimit;
    }

    public ApplicationSettingsValueWrapper getSettingHl7RequestSendPollingTime() {
        return settingHl7RequestSendPollingTime;
    }

    public void setSettingHl7RequestSendPollingTime(ApplicationSettingsValueWrapper settingHl7RequestSendPollingTime) {
        this.settingHl7RequestSendPollingTime = settingHl7RequestSendPollingTime;
    }

    public ApplicationSettingsValueWrapper getSettingsHL7DaysLimit() {
        return settingsHL7DaysLimit;
    }

    public void setSettingsHL7DaysLimit(ApplicationSettingsValueWrapper settingsHL7DaysLimit) {
        this.settingsHL7DaysLimit = settingsHL7DaysLimit;
    }

    public ApplicationSettingsValueWrapper getSettingsHL7MaxCountLimit() {
        return settingsHL7MaxCountLimit;
    }

    public void setSettingsHL7MaxCountLimit(ApplicationSettingsValueWrapper settingsHL7MaxCountLimit) {
        this.settingsHL7MaxCountLimit = settingsHL7MaxCountLimit;
    }

    public ApplicationSettingsValueWrapper getSettingTokenExpiration() {
        return settingTokenExpiration;
    }

    public void setSettingTokenExpiration(ApplicationSettingsValueWrapper settingTokenExpiration) {
        this.settingTokenExpiration = settingTokenExpiration;
    }

    public ApplicationSettingsValueWrapper getSettingHL7ReportForwardingSendIp() {
        return settingHL7ReportForwardingSendIp;
    }

    public void setSettingHL7ReportForwardingSendIp(ApplicationSettingsValueWrapper settingHL7ReportForwardingSendIp) {
        this.settingHL7ReportForwardingSendIp = settingHL7ReportForwardingSendIp;
    }

    public ApplicationSettingsValueWrapper getSettingHL7ReportForwardingPort() {
        return settingHL7ReportForwardingPort;
    }

    public void setSettingHL7ReportForwardingPort(ApplicationSettingsValueWrapper settingHL7ReportForwardingPort) {
        this.settingHL7ReportForwardingPort = settingHL7ReportForwardingPort;
    }

    public ApplicationSettingsValueWrapper getSettingHL7ReportForwardingPollingTime() {
        return settingHL7ReportForwardingPollingTime;
    }

    public void setSettingHL7ReportForwardingPollingTime(ApplicationSettingsValueWrapper settingHL7ReportForwardingPollingTime) {
        this.settingHL7ReportForwardingPollingTime = settingHL7ReportForwardingPollingTime;
    }

    public ApplicationSettingsValueWrapper getSettingHl7ReportForwardingStartDate() {
        return settingHl7ReportForwardingStartDate;
    }

    public void setSettingHl7ReportForwardingStartDate(ApplicationSettingsValueWrapper settingHl7ReportForwardingStartDate) {
        this.settingHl7ReportForwardingStartDate = settingHl7ReportForwardingStartDate;
    }

    public ApplicationSettingsValueWrapper getSettingHl7ReportForwardingHours() {
        return settingHl7ReportForwardingHours;
    }

    public void setSettingHl7ReportForwardingHours(ApplicationSettingsValueWrapper settingHl7ReportForwardingHours) {
        this.settingHl7ReportForwardingHours = settingHl7ReportForwardingHours;
    }

    public Date getHl7ReportForwardingStartDate() {
        return hl7ReportForwardingStartDate;
    }

    public void setHl7ReportForwardingStartDate(Date hl7ReportForwardingStartDate) {
        this.hl7ReportForwardingStartDate = hl7ReportForwardingStartDate;
    }

    public ApplicationSettingsValueWrapper getSettingsIncludeCDA2InPDF() {
        return settingsIncludeCDA2InPDF;
    }

    public void setSettingsIncludeCDA2InPDF(ApplicationSettingsValueWrapper settingsIncludeCDA2InPDF) {
        this.settingsIncludeCDA2InPDF = settingsIncludeCDA2InPDF;
    }

    public Boolean getIncludeCDA2InPDF() {
        return includeCDA2InPDF;
    }

    public void setIncludeCDA2InPDF(Boolean includeCDA2InPDF) {
        this.includeCDA2InPDF = includeCDA2InPDF;
    }

    public ApplicationSettingsValueWrapper getSettingFSEGatewayEndpoint() {
        return settingFSEGatewayEndpoint;
    }

    public void setSettingFSEGatewayEndpoint(ApplicationSettingsValueWrapper settingFSEGatewayEndpoint) {
        this.settingFSEGatewayEndpoint = settingFSEGatewayEndpoint;
    }

    public ApplicationSettingsValueWrapper getSettingsEnableValidationCDA() {
        return settingsEnableValidationCDA;
    }

    public void setSettingsEnableValidationCDA(ApplicationSettingsValueWrapper settingsEnableValidationCDA) {
        this.settingsEnableValidationCDA = settingsEnableValidationCDA;
    }

    public Boolean getEnableValidationCDA() {
        return enableValidationCDA;
    }

    public void setEnableValidationCDA(Boolean enableValidationCDA) {
        this.enableValidationCDA = enableValidationCDA;
    }
}
