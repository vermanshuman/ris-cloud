package it.nexera.ris.persistence;

import com.google.common.base.Joiner;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.exceptions.info.StacktraceInfoException;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.web.beans.wrappers.Pair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.*;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionHolder {

    private static final Logger traceInfoLog = CustomLibLoggerFactory.getActivityTraceInfoLogger();

    static final Logger log;

    static {
        log = LogManager.getLogger(SessionHolder.class);
    }

    private Map<SessionNames, Pair<Session, Integer>> sessions;

    private List<SessionNames> lockedList;

    private static SessionHolder instance;

    private Object monitor;

    public synchronized static SessionHolder getInstance() {
        if (instance == null) {
            instance = new SessionHolder();
        }

        return instance;
    }

    private SessionHolder() {
        monitor = new Object();
        sessions = new HashMap<SessionNames, Pair<Session, Integer>>();
        lockedList = new ArrayList<SessionNames>();
    }

    public void openSession(SessionNames serviceName) {
        synchronized (SessionHolder.class) {
            Session session = null;
            if (sessions.get(serviceName) != null) {
                session = sessions.get(serviceName).getFirst();
            }
            if (session == null) {
                try {
                    session = createSession(serviceName);
                    SessionTracker.getInstance().sessionOpening(
                            "SessionHolder:" + serviceName);
                    sessions.put(serviceName, new Pair<Session, Integer>(
                            session, 1));
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            } else {
                sessions.get(serviceName).setSecond(
                        sessions.get(serviceName).getSecond() + 1);
            }

        }
    }

    public Session createSession(SessionNames serviceName) {
        Session session;
        session = HibernateUtil.getSessionFactory(true)
                .openSession();
        session.setFlushMode(FlushMode.COMMIT);
        session.setCacheMode(CacheMode.IGNORE);
        session.flush();
        session.clear();
        try {
            session.beginTransaction().commit();
        } catch (Exception e) {
            LogHelper.log(log,
                    "Error while opening session for service: "
                            + serviceName);
            LogHelper.log(log, e);
        }
        return session;
    }

    public void closeSession(SessionNames serviceName) {
        synchronized (SessionHolder.class) {
            Session session = null;
            if (sessions.get(serviceName) != null) {
                session = sessions.get(serviceName).getFirst();
            }
            if (session != null && sessions.get(serviceName).getSecond() == 1) {
                sessions.remove(serviceName);
                session.close();
                session = null;
                SessionTracker.getInstance().sessionClosing(
                        "SessionHolder:" + serviceName);

            } else if (sessions.get(serviceName) != null
                    && sessions.get(serviceName).getSecond() != null) {
                sessions.get(serviceName).setSecond(
                        sessions.get(serviceName).getSecond() - 1);
            } else {
                log.error("Session was not closed for service: " + serviceName);
                try {
                    throw new Exception("Session was not closed for service: " + serviceName);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
        }
    }

    public synchronized void openTransaction(SessionNames serviceName)
            throws Exception {
        synchronized (SessionHolder.class) {
            if (getSession(serviceName).getTransaction().getStatus() != TransactionStatus.ACTIVE) {
                getSession(serviceName).beginTransaction();
            }
        }
    }

    public synchronized void commitTransaction(SessionNames serviceName)
            throws Exception {
        synchronized (SessionHolder.class) {
            Transaction tr = getSession(serviceName).getTransaction();
            if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                getSession(serviceName).getTransaction().commit();
            }
            tr = null;
        }
    }

    public synchronized void saveInTransaction(IndexedEntity ent,
                                               SessionNames serviceName) throws Exception {
        synchronized (SessionHolder.class) {
            Transaction tr = null;
            boolean openedTransaction = false;
            try {
                if (getSession(serviceName).getTransaction().getStatus() == TransactionStatus.ACTIVE) {
                    tr = getSession(serviceName).getTransaction();
                } else {
                    tr = getSession(serviceName).beginTransaction();
                    openedTransaction = true;
                }

                if (ent instanceof RadiologyExamRequest) {
                    try {
                        LogHelper.log(traceInfoLog, ent.toString());
                        throw new StacktraceInfoException((RadiologyExamRequest) ent);
                    } catch (StacktraceInfoException e) {
                        LogHelper.log(traceInfoLog, e);
                    } catch (Exception e) {
                        LogHelper.log(traceInfoLog, e);
                    }
                }
                getSession(serviceName).saveOrUpdate(ent);
            } catch (Exception e) {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }

                throw e;
            } finally {
                if (openedTransaction && tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
                tr = null;
            }
        }
    }

    public synchronized void save(IndexedEntity ent, SessionNames serviceName)
            throws Exception {
        synchronized (SessionHolder.class) {
            Transaction tr = null;
            try {
                if (getSession(serviceName).getTransaction().getStatus() == TransactionStatus.ACTIVE) {
                    tr = getSession(serviceName).getTransaction();
                } else {
                    tr = getSession(serviceName).beginTransaction();
                }

                if (ent instanceof RadiologyExamRequest) {
                    try {
                        LogHelper.log(traceInfoLog, ent.toString());
                        throw new StacktraceInfoException((RadiologyExamRequest) ent);
                    } catch (StacktraceInfoException e) {
                        LogHelper.log(traceInfoLog, e);
                    } catch (Exception e) {
                        LogHelper.log(traceInfoLog, e);
                    }
                }
                getSession(serviceName).saveOrUpdate(ent);
            } catch (Exception e) {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }

                throw e;
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
                tr = null;
            }
        }
    }

    public synchronized void remove(IndexedEntity ent, SessionNames serviceName)
            throws Exception {
        synchronized (SessionHolder.class) {
            Transaction tr = null;
            try {
                if (getSession(serviceName).getTransaction().getStatus() == TransactionStatus.ACTIVE) {
                    tr = getSession(serviceName).getTransaction();
                } else {
                    tr = getSession(serviceName).beginTransaction();
                }

                getSession(serviceName).delete(ent);
            } catch (Exception e) {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }

                throw e;
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
                tr = null;
            }
        }
    }

    public synchronized void remove(Class<? extends IEntity> clazz,
                                    Serializable id, SessionNames serviceName) throws Exception {
        synchronized (SessionHolder.class) {
            Transaction tr = null;
            try {
                if (getSession(serviceName).getTransaction().getStatus() == TransactionStatus.ACTIVE) {
                    tr = getSession(serviceName).getTransaction();
                } else {
                    tr = getSession(serviceName).beginTransaction();
                }

                getSession(serviceName).delete(
                        getSession(serviceName).get(clazz,
                                Long.parseLong(String.valueOf(id))));
            } catch (Exception e) {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }

                throw e;
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    try {
                        tr.commit();
                    } catch (Exception e) {
                        tr.rollback();
                        throw e;
                    }
                }
                tr = null;
            }
        }
    }

    public synchronized void remove(Class<? extends IEntity> clazz,
                                    List<Long> ids, SessionNames serviceName) throws Exception {
        if (ValidationHelper.isNullOrEmpty(ids)) {
            return;
        }
        synchronized (SessionHolder.class) {
            Transaction tr = null;
            try {
                if (getSession(serviceName).getTransaction().getStatus() == TransactionStatus.ACTIVE) {
                    tr = getSession(serviceName).getTransaction();
                } else {
                    tr = getSession(serviceName).beginTransaction();
                }

                Query q = getSession(serviceName).createQuery(
                        String.format("delete from %s where id in (:idList) ",
                                clazz.getSimpleName()));
                q.setString("idList", Joiner.on(',').join(ids));
                q.executeUpdate();
            } catch (Exception e) {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }

                throw e;
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    try {
                        tr.commit();
                    } catch (Exception e) {
                        tr.rollback();
                        throw e;
                    }
                }
                tr = null;
            }
        }
    }

    public Session getSession(SessionNames serviceName) throws Exception {
        Session session = null;
        if (sessions.get(serviceName) != null) {
            session = sessions.get(serviceName).getFirst();
        }
        if (session != null) {
            synchronized (monitor) {
                return session;
            }
        } else {
            throw new Exception("Session was not opened");
        }
    }
}
