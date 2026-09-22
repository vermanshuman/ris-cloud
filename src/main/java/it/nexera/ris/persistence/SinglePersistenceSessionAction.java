package it.nexera.ris.persistence;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public abstract class SinglePersistenceSessionAction implements ISessionAction {

    private PersistenceSession persistenceSession;
    private Session session;

    public SinglePersistenceSessionAction() {
        try {
            this.persistenceSession = new PersistenceSession();
            this.session = this.persistenceSession.getSession();
        } catch (Exception e) {
            onException(e);
        }
    }

    @Override
    public void onBeforeExecute() {
    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public final void onExecuted() {
        persistenceSession.closeSession();
    }

    @Override
    public void execute(Object obj) throws Exception {
        execute();
    }


    public abstract void execute() throws IllegalAccessException, PersistenceBeanException, InstantiationException;

    @Override
    public void onSuccess() throws HibernateException {

    }

    public Session getSession() {
        return session;
    }
}
