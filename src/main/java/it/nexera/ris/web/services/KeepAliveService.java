package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.web.services.base.BaseDBService;

import java.io.Serializable;

public class KeepAliveService extends BaseDBService implements Serializable {

    private static final long serialVersionUID = -8763703352786846974L;

    public KeepAliveService() {
        super(SessionNames.KeepAliveService);
    }

    @Override
    protected void routineFuncInternal() {
        getSession().createSQLQuery("SELECT * FROM DUAL").list();
    }

    @Override
    protected int getPollTimeKey() {
        return 10;
    }
}
