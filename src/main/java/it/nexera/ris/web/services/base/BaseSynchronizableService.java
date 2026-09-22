package it.nexera.ris.web.services.base;

import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.SessionHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.common.ServiceExecutionTime;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.net.InetAddress;

public abstract class BaseSynchronizableService extends BaseDBService implements
        Serializable {
    //private static final long TIMER_INITIAL_DELAY = 1 * 60 * 1000; // 1

    // minute

    //private static final long TIMER_INTERVAL      = 1 * 60 * 1000; // 1

    private static final long serialVersionUID = -303084472920987437L;

    public BaseSynchronizableService(SessionNames name) {
        super(name);
    }

    protected void runInternal() {
        try {
            this.preRoutineFuncInternal();
            this.preRoutineFunc();
            SessionHolder.getInstance();
            this.routineFunc();
            this.postRoutineFunc();
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            try {
                this.postRoutineFuncInternal();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public String getIPAddress() {
        InetAddress thisIp = null;
        try {
            thisIp = InetAddress.getLocalHost();
        } catch (Exception e) {

        }
        return thisIp == null ? "" : thisIp.getHostAddress();
    }

    public ServiceExecutionTime getSynchEntityForBean()
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return (ServiceExecutionTime) DaoManager.getSession()
                .createCriteria(ServiceExecutionTime.class)
                .add(Restrictions.eq("serviceKey", this.name)).uniqueResult();
    }
}
