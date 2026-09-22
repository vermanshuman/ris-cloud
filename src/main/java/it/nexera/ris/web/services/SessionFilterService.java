package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.HttpSessionCollector;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.base.BaseService;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class SessionFilterService extends BaseService {

    private static final Logger serviceActivityLog = CustomLibLoggerFactory.getServiceInfoLogger();

    public SessionFilterService() {
        super("SessionFilterService");
    }

    @Override
    protected void routineFuncInternal() {
        String serviceName = ResourcesHelper.getString("sessionTimeout");
        LogHelper.log(serviceActivityLog, String.format("Service <%s> start work at <%s>", serviceName, DateTimeHelper.ToStringWithSeconds(new Date())));
        HttpSessionCollector.checkEmptySession(serviceName);
        LogHelper.log(serviceActivityLog, String.format("Service <%s> end work at <%s>", serviceName, DateTimeHelper.ToStringWithSeconds(new Date())));
    }

    @Override
    protected int getPollTimeKey() {
        if (!ValidationHelper
                .isNullOrEmpty(ApplicationSettingsHolder.getInstance().getByKey(
                        ApplicationSettingsKeys.SESSION_CHECK_TIMEOUT))
                && !ValidationHelper
                .isNullOrEmpty(ApplicationSettingsHolder.getInstance()
                        .getByKey(
                                ApplicationSettingsKeys.SESSION_CHECK_TIMEOUT)
                        .getValue())) {
            return Integer.parseInt(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.SESSION_CHECK_TIMEOUT)
                    .getValue());
        }

        return 120;
    }

}
