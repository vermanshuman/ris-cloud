package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.base.BaseDBService;

import java.io.Serializable;

public class SettingSynchronizerService extends BaseDBService implements
        Serializable {
    private static final long serialVersionUID = 6594608037317034294L;

    public SettingSynchronizerService() {
        super(SessionNames.SettingsSynchronizer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.services.base.BaseService#routineFuncInternal()
     */
    @Override
    protected void routineFuncInternal() {
        ApplicationSettingsHolder.getInstance().refreshHolder();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.services.base.BaseService#getPollTimeKey()
     */
    @Override
    protected int getPollTimeKey() {
        return 4;
    }

}
