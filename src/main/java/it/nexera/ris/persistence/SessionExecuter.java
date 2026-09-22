package it.nexera.ris.persistence;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

/**
 * @author Alex Chelombitko
 * 23.01.2013
 */
public class SessionExecuter {
    private static Object monitor = new Object();

    public static synchronized void execute(IAction action) throws Exception {
        synchronized (monitor) {
            Session session = HibernateUtil.getSessionFactory(true)
                    .openSession();
            SessionTracker.getInstance().sessionOpening(
                    SessionExecuter.class.getSimpleName());
            if (session != null) {
                session.setFlushMode(FlushMode.COMMIT);
                session.setCacheMode(CacheMode.IGNORE);
                session.flush();
                session.clear();

                Transaction tr = null;
                try {
                    tr = session.beginTransaction();

                    action.execute(session);
                } catch (Exception e) {
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        tr.rollback();
                    }

                    action.onException(e);
                } finally {
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        try {
                            tr.commit();
                        } catch (Exception e) {
                            action.onException(e);
                        }
                    }

                    if (session != null) {
                        session.close();
                        session = null;
                        SessionTracker.getInstance().sessionClosing(
                                "SessionHolder");
                    }
                    action.onExecuted();
                }
            }
        }
    }
}
