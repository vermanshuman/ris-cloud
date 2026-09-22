package it.nexera.ris.web.services.base;

import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.SessionHolder;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.hibernate.Criteria;
import org.hibernate.Session;

import java.util.Date;

public abstract class BaseDBService extends BaseService {

    private SessionNames serviceName;

    public BaseDBService(SessionNames name) {
        super(name.toString());
        this.serviceName = name;
    }

    /* (non-Javadoc)
     * @see it.nexera.web.services.BaseService#onDestroy()
     */
    @Override
    protected void onDestroy() {
        this.closeSession();
    }

    public SessionNames getServiceName() {
        return serviceName;
    }

    protected void runInternal() {
        try {
            if (!stopFlag) {
                this.preRoutineFuncInternal();
                this.preRoutineFunc();
                this.routineFunc();
                this.postRoutineFunc();
            }
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

    protected Session getSession() {
        try {
            return SessionHolder.getInstance().getSession(serviceName);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    protected void preRoutineFuncInternal() {
        if (stopFlag) {
            return;
        }

        this.updateSleepTime();

        this.checkSession();
    }

    protected final void postRoutineFuncInternal() {
        this.closeSession();
    }

    private void initSession() {
        SessionHolder.getInstance().openSession(serviceName);
    }

    protected final void checkSession() {
        initSession();
    }

    protected final void closeSession() {
        SessionHolder.getInstance().closeSession(serviceName);
    }

    protected final void beforeSaveOrUpdate(IndexedEntity object) {
        if (object.getCreateDate() == null) {
            object.setCreateDate(new Date());
            object.setCreateUserId(0L);
        } else {
            object.setUpdateDate(new Date());
            object.setUpdateUserId(0L);
        }
    }

    protected final void save(IndexedEntity ent) throws Exception {
        beforeSaveOrUpdate(ent);
        SessionHolder.getInstance().save(ent, serviceName);
    }

    protected final void remove(IndexedEntity ent) throws Exception {
        SessionHolder.getInstance().remove(ent, serviceName);
    }

    protected final void openTransaction() throws Exception {
        SessionHolder.getInstance().openTransaction(serviceName);
    }

    protected final void commitTransaction() throws Exception {
        SessionHolder.getInstance().commitTransaction(serviceName);
    }

    protected Criteria createCriteria(Class<?> clazz) {
        return getSession().createCriteria(clazz);
    }

    protected final void saveInTransaction(IndexedEntity ent) throws Exception {
        beforeSaveOrUpdate(ent);
        SessionHolder.getInstance().saveInTransaction(ent, serviceName);
    }
    //    protected final void saveInTransaction(IndexedEntity ent)
    //    {
    //        beforeSaveOrUpdate(ent);
    //        saveInTransaction(ent);
    //    }
}
