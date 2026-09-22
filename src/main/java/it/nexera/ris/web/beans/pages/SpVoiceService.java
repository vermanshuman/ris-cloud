package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;

public class SpVoiceService {

    protected static final Logger log = LogManager.getLogger(SpVoiceService.class);

    public String getSpVoiceApiUrl() {
        return ResourcesHelper.getProperty("spvoiceApiUrl");
    }

    public String getSpVoiceUsername() {
        String username = null;
        try {
            User user = DaoManager.get(User.class, UserHolder.getInstance().getCurrentUser().getId());
            username = user.getSpvoiceUsername();

            if (StringUtils.isEmpty(username)) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        ResourcesHelper.getString("spVoiceUsernameNotConfigured"),
                        "");
            }

        } catch (PersistenceBeanException | InstantiationException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return username;
    }
}
