package it.nexera.ris.persistence;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.PercentFormatHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersistenceSession {

    public static final transient Logger log = LogManager.getLogger(PersistenceSession.class);

    private Session session = null;

    private String id;

    public PersistenceSession() {
        id = UUID.randomUUID().toString();
    }

    public Session getSession() throws PersistenceBeanException {
        if (this.session == null || !this.session.isConnected()
                || !this.session.isOpen()) {
            try {
                //                System.out.print("Opening a session.." + new Date().toString());
                this.session = createSession();
            } catch (FileNotFoundException e) {
                log.warn("PersistenceSession.getSession : " + e);
                LogHelper.log(log, e);
            }
        }
        return this.session;
    }

    public Session getSession(String host, String database)
            throws PersistenceBeanException {
        if (this.session == null) {
            try {
                this.session = createSession(host, null, database, null, null,
                        null);
            } catch (FileNotFoundException e) {
                LogHelper.log(log, e);
            }
        }
        return this.session;
    }

    public Session getSession(String host, String port, String database,
                              String username, String password, String hibernateMode)
            throws PersistenceBeanException {
        if (this.session == null) {
            try {
                this.session = createSession(host, port, database, username,
                        password, hibernateMode);
            } catch (FileNotFoundException e) {
                LogHelper.log(log, e);
            }
        }
        return this.session;
    }

    public Session getSession(String host, String port, String database,
                              String username, String password) throws PersistenceBeanException {
        return getSession(host, port, database, username, password, null);
    }

    public static PersistenceSession getInstance() {
        return new PersistenceSession();
    }

    @Override
    public String toString() {
        return this.id;
    }

    public static Session createSession() throws PersistenceBeanException,
            FileNotFoundException {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory(true).openSession();
            SessionTracker.getInstance().sessionOpening("PersistenceSession");
            session.setFlushMode(FlushMode.COMMIT);
            session.setCacheMode(CacheMode.IGNORE);
        } catch (Throwable ex) {
            throw new PersistenceBeanException(
                    "Error of Hibernate Session creating...", ex);
        }
        return session;
    }

    @SuppressWarnings("deprecation")
    private static Session createSession(String host, String port,
                                         String database, String username, String password,
                                         String hibernateMode) throws PersistenceBeanException,
            FileNotFoundException {
        log.info("Session.createSession()...");
        Session session = null;
        //  Configuration cfg = null;

        try {
            //  cfg = getConfiguration(log);
            //  SessionFactory sessionFactory = cfg.buildSessionFactory();
            Configuration config = new Configuration();
            HibernateUtil.addAnnotatedClasses(config, true);
            config.configure();

            Map<String, String> params = new HashMap<String, String>();

            if (ValidationHelper.isNullOrEmpty(host)) {
                params.put(
                        "host",
                        String.valueOf(config.getProperties().get(
                                "hibernate.connection.host")));
            } else {
                params.put("host", host);
            }

            if (ValidationHelper.isNullOrEmpty(database)) {
                params.put(
                        "database",
                        String.valueOf(config.getProperties().get(
                                "hibernate.connection.database")));
            } else {
                params.put("database", database);
            }

            config.getProperties().put(
                    "hibernate.connection.url",
                    PercentFormatHelper.format(
                            String.valueOf(config.getProperties().get(
                                    "hibernate.connection.url.template")),
                            params));

            if (!ValidationHelper.isNullOrEmpty(username)) {
                config.getProperties().put("hibernate.connection.username",
                        username);
            }
            if (!ValidationHelper.isNullOrEmpty(password)) {
                config.getProperties().put("hibernate.connection.password",
                        password);
            }

            SessionFactory sessionFactory = config.buildSessionFactory();
            session = sessionFactory.openSession();
            session.setFlushMode(FlushMode.COMMIT);
        } catch (Throwable ex) {
            log.info("createSession(). exception : " + ex);
            throw new PersistenceBeanException(
                    "Error of Hibernate Session creating...", ex);
        }
        log.info("Session.createSession()...");
        return session;
    }

    public static Configuration getConfiguration(Logger outLog) {
        Configuration cfg = null;
        InputStream is = null;
        try {
            outLog.info("Trying to read Hibernate cfg from current dir...");
            is = new FileInputStream(new File("./hibernate.cfg.xml"));
            cfg = new Configuration().addInputStream(is).configure();

            outLog.info("Hibernate cfg has been read from current dir...");
        } catch (FileNotFoundException e) {
            outLog.info("Trying to read Hibernate cfg by current ClassLoader...");
            is = PersistenceSession.class
                    .getResourceAsStream("/hibernate.cfg.xml");
            if (is != null) {
                cfg = new Configuration().addInputStream(is).configure();

                outLog.info("Hibernate cfg has been read by current ClassLoader...");
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogHelper.log(log, e);
                }
            }
        }
        if (cfg == null) {
            cfg = new Configuration().configure();
        }
        return cfg;
    }

    public void closeSession() {
        if (this.session != null) {
            try {
                //                this.session.flush();
                this.session.clear();
                this.session.close();
            } catch (Exception e) {
                this.session.cancelQuery();
                this.session.clear();
                this.session.close();
                LogHelper.log(log, e);
            }
            SessionTracker.getInstance().sessionClosing("PersistenceSession");
            this.session = null;
        }
    }

    @Override
    protected void finalize() {
        try {
            this.closeSession();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }
}
