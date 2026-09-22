package it.nexera.ris.persistence;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import org.hibernate.HibernateException;

public abstract class Action implements IAction {
    @Override
    public void onBeforeExecute() {
    }

    @Override
    public void onExecuted() {
    }

    @Override
    public void onSuccess() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
    }

    @Override
    public void onException(Exception e) throws Exception {
        throw e;
    }

    @Override
    public void execute(Object obj) throws Exception {
        this.execute();
    }

    public abstract void execute() throws Exception;
}
