package it.nexera.ris.settings;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.SessionTracker;
import it.nexera.ris.persistence.beans.entities.domain.ApplicationSettingsValue;
import it.nexera.ris.web.beans.wrappers.logic.ApplicationSettingsValueWrapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationSettingsHolder {
    private final ConcurrentHashMap<ApplicationSettingsKeys, ApplicationSettingsValue> settings = new ConcurrentHashMap<ApplicationSettingsKeys, ApplicationSettingsValue>();

    private final ConcurrentHashMap<ApplicationSettingsKeys, Object> monitors = new ConcurrentHashMap<ApplicationSettingsKeys, Object>();

    private final static Logger log = LogManager.getLogger(ApplicationSettingsHolder.class);

    private static volatile ApplicationSettingsHolder instance;

    private static volatile Session session;

    // Default settings
    private static final int DEFAULT_PASSWORD_EXPIRATION_PERIOD = 10;

    // Exam request polling time
    private static final int DEFAULT_EXAM_REQUEST_SUBMIT_POLLING_TIME = 5;                                                                         // minutes

    private static final int DEFAULT_EXAM_REQUEST_CHECK_POLLING_TIME = 5;                                                                         // minutes

    private static final int DEFAULT_EXAM_REQUEST_LABEL_READ_POLLING_TIME = 10;                                                                        // minutes

    private static final int DEFAULT_EXAM_REQUEST_RESULT_POLLING_TIME = 30;                                                                        // minutes

    private static final int DEFAULT_WAITING_LIST_STATUS_CHECK_POLLING_TIME = 30;                                                                        // minutes

    private static final int DEFAULT_PSD_CHECK_POLLING_TIME = 30;                                                                        // minutes

    private static final int DEFAULT_PSD_CHECK_NEW_PATIENTS_POLLING_TIME = 30;                                                                        // minutes

    private static final int DEFAULT_MAX_STARTED_DAYS = 60;                                                                        // days

    private static final int DEFAULT_RIS_RESPONSE_TIMEOUT = 15;                                                                        // seconds

    private static final int DEFAULT_RIS_REQUEST_SEND_POLLING_TIME = 5;                                                                         // minutes

    private static final int DEFAULT_RIS_RESEND_COUNT = 15;                                                                        // number

    private static final int DEFAULT_LABORATORY_EXAMS_PORT = 10304;                                                                     // number

    private static final int DEFAULT_RIS_PORT = 10305;                                                                     // number

    private static final int DEFAULT_HL7_PORT = 10306;

    private static final String DEFAULT_ASAP_SIO_URL = "jdbc:oracle:thin:@192.168.30.12:1521:ora10g";

    private static final String DEFAULT_ASAP_SIO_USERNAME = "HSB";

    private static final String DEFAULT_ASAP_SIO_PASSWORD = "HSB_PASSWORD";

    private static final int DEFAULT_QUERY_TIMEOUT = 120;

    private static String RIS_ADDRESS;

    private static Map<String, Object> localStorage;

    private static List<String> listForRemove;

    public ApplicationSettingsHolder() {
        if (instance != null) {
            throw new IllegalStateException();
        }

        doInitialLoading();

        instance = this;
    }

    @SuppressWarnings("unchecked")
    private void doInitialLoading() {
        List<ApplicationSettingsValue> list = null;
        localStorage = new HashMap<>();
        listForRemove = new ArrayList<>();
        try {
            session = HibernateUtil.getSessionFactory(true).openSession();
            SessionTracker.getInstance().sessionOpening(
                    "ApplicationSettingsHolder");
            if (session == null || !session.isConnected() || !session.isOpen()) {
                log.error("Error creating session");
                return;
            }

            try {
                list = session.createCriteria(ApplicationSettingsValue.class)
                        .add(Restrictions.in("key", ApplicationSettingsKeys.values()))
                        .list();
            } catch (HibernateException e) {
                log.error("Failed to load application settings");
            }

            if (list == null) {
                return;
            }

            for (ApplicationSettingsValue val : list) {
                ApplicationSettingsKeys key = ApplicationSettingsKeys
                        .valueOf(val.getKey().name());

                if (key != null) {
                    settings.put(key, val);
                    monitors.put(key, new Object());
                }
            }

            createDefaultSettings();
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            session.close();
            SessionTracker.getInstance().sessionClosing(
                    "ApplicationSettingsHolder");
            session = null;
        }
    }

    private void createDefaultSettings() {
        for (ApplicationSettingsKeys key : ApplicationSettingsKeys.values()) {
            if (!settings.containsKey(key)) {
                ApplicationSettingsValue val = new ApplicationSettingsValue();
                val.setKey(key);
                val.setValue(getDefaultValueByKey(key));

                settings.put(key, val);
                monitors.put(key, new Object());

                applyNewValue(key, val.getValue());
            }
        }
    }

    public String getDefaultValueByKey(ApplicationSettingsKeys key) {
        if (key == null) {
            return null;
        }

        if (key.equals(ApplicationSettingsKeys.PASSWORD_EXPIRATION_PERIOD)) {
            return String.valueOf(DEFAULT_PASSWORD_EXPIRATION_PERIOD);
        } else if (key
                .equals(ApplicationSettingsKeys.EXAM_REQUEST_SUBMIT_POLLING_TIME)) {
            return String.valueOf(DEFAULT_EXAM_REQUEST_SUBMIT_POLLING_TIME);
        } else if (key
                .equals(ApplicationSettingsKeys.EXAM_REQUEST_CHECK_POLLING_TIME)) {
            return String.valueOf(DEFAULT_EXAM_REQUEST_CHECK_POLLING_TIME);
        } else if (key
                .equals(ApplicationSettingsKeys.EXAM_REQUEST_LABEL_READ_POLLING_TIME)) {
            return String.valueOf(DEFAULT_EXAM_REQUEST_LABEL_READ_POLLING_TIME);
        } else if (key
                .equals(ApplicationSettingsKeys.EXAM_REQUEST_RESULT_POLLING_TIME)) {
            return String.valueOf(DEFAULT_EXAM_REQUEST_RESULT_POLLING_TIME);
        } else if (key
                .equals(ApplicationSettingsKeys.WAITING_LIST_STATUS_CHECK_POLLING_TIME)) {
            return String
                    .valueOf(DEFAULT_WAITING_LIST_STATUS_CHECK_POLLING_TIME);
        } else if (key.equals(ApplicationSettingsKeys.PSD_CHECK_POLLING_TIME)) {
            return String.valueOf(DEFAULT_PSD_CHECK_POLLING_TIME);
        } else if (key
                .equals(ApplicationSettingsKeys.PSD_CHECK_NEW_PATIENTS_POLLING_TIME)) {
            return String.valueOf(DEFAULT_PSD_CHECK_NEW_PATIENTS_POLLING_TIME);
        } else if (key.equals(ApplicationSettingsKeys.MAX_STARTED_DAYS)) {
            return String.valueOf(DEFAULT_MAX_STARTED_DAYS);
        } else if (key
                .equals(ApplicationSettingsKeys.RIS_REQUEST_SEND_POLLING_TIME)) {
            return String.valueOf(DEFAULT_RIS_REQUEST_SEND_POLLING_TIME);
        } else if (key.equals(ApplicationSettingsKeys.RIS_RESPONSE_TIMEOUT)) {
            return String.valueOf(DEFAULT_RIS_RESPONSE_TIMEOUT);
        } else if (key.equals(ApplicationSettingsKeys.LABORATORY_EXAMS_PORT)) {
            return String.valueOf(DEFAULT_LABORATORY_EXAMS_PORT);
        } else if (key.equals(ApplicationSettingsKeys.RIS_PORT)) {
            return String.valueOf(DEFAULT_RIS_PORT);
        } else if (key.equals(ApplicationSettingsKeys.RIS_RESEND_COUNT)) {
            return String.valueOf(DEFAULT_RIS_RESEND_COUNT);
        } else if (key.equals(ApplicationSettingsKeys.HL7_LISTEN_PORT)) {
            return String.valueOf(DEFAULT_HL7_PORT);
        } else if (key
                .equals(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_URL)) {
            return DEFAULT_ASAP_SIO_URL;
        } else if (key
                .equals(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_USERNAME)) {
            return DEFAULT_ASAP_SIO_USERNAME;
        } else if (key
                .equals(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_PASSWORD)) {
            return DEFAULT_ASAP_SIO_PASSWORD;
        } else if (key.equals(ApplicationSettingsKeys.QUERY_TIMEOUT)) {
            return String.valueOf(DEFAULT_QUERY_TIMEOUT);
        }

        return null;
    }

    public static ApplicationSettingsHolder getInstance() {
        if (instance == null) {
            synchronized (ApplicationSettingsHolder.class) {
                if (instance == null) {
                    instance = new ApplicationSettingsHolder();
                }
            }
        }

        return instance;
    }

    public ApplicationSettingsValueWrapper getByKey(ApplicationSettingsKeys key) {
        if (key == null) {
            return null;
        }

        if (settings.containsKey(key) && monitors.containsKey(key)) {
            ApplicationSettingsValueWrapper setting = null;
            Object monitor = monitors.get(key);
            synchronized (monitor) {
                setting = new ApplicationSettingsValueWrapper(key,
                        handleReadValue(key, settings.get(key).getValue()));
            }

            return setting;
        }

        return null;
    }

    public boolean applyNewValue(ApplicationSettingsKeys key, String value) {
        return applyNewValue(new ApplicationSettingsValueWrapper(key, value));
    }

    public void createNewLocalValue(String key, Object value) {
        for (String uuid : listForRemove) {
            localStorage.remove(uuid);
        }
        localStorage.put(key, value);
    }

    public Object getLocalValue(String key) {
        return localStorage.get(key);
    }

    public boolean containsKey(String key) {
        return localStorage.containsKey(key);
    }

    public void addToRemove(String key) {
        listForRemove.add(key);
    }

    @SuppressWarnings("unchecked")
    public void refreshHolder() {
        List<ApplicationSettingsValue> list = null;
        try {
            session = HibernateUtil.getSessionFactory(true).openSession();
            SessionTracker.getInstance().sessionOpening(
                    "ApplicationSettingsHolder");
            if (session == null || !session.isConnected() || !session.isOpen()) {
                log.error("Error creating session");
                return;
            }

            try {
                list = session.createCriteria(ApplicationSettingsValue.class)
                        .list();
            } catch (HibernateException e) {
                log.error("Failed to load application settings");
            }

            if (list == null) {
                return;
            }

            for (ApplicationSettingsValue val : list) {
                if (ApplicationSettingsKeys.valueOf(val.getKey().name()) != null
                        && settings.containsKey(ApplicationSettingsKeys
                        .valueOf(val.getKey().name()))
                        && monitors.containsKey(ApplicationSettingsKeys
                        .valueOf(val.getKey().name()))) {
                    Object monitor = monitors.get(ApplicationSettingsKeys
                            .valueOf(val.getKey().name()));
                    synchronized (monitor) {
                        settings.replace(ApplicationSettingsKeys.valueOf(val
                                .getKey().name()), val);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            session.close();
            SessionTracker.getInstance().sessionClosing(
                    "ApplicationSettingsHolder");
            session = null;
        }

    }

    public boolean applyNewValue(ApplicationSettingsValueWrapper setting) {
        if (setting == null) {
            return false;
        }

        if (setting.getKey() == null) {
            return false;
        }

        boolean isOpenedNewSession = false;

        if (settings.containsKey(setting.getKey())
                && monitors.containsKey(setting.getKey())) {
            Object monitor = monitors.get(setting.getKey());
            synchronized (monitor) {
                ApplicationSettingsValue val = settings.get(setting.getKey());
                val.setValue(handleNewValue(setting.getKey(),
                        setting.getValue()));

                try {
                    if (session == null || !session.isOpen()) {
                        session = HibernateUtil.getSessionFactory(true)
                                .openSession();
                        isOpenedNewSession = true;
                        SessionTracker.getInstance().sessionOpening(
                                "ApplicationSettingsHolder");
                    }

                    Transaction tr = null;
                    try {
                        tr = session.beginTransaction();

                        if (session.contains(val)) {
                            session.merge(val);
                        } else {
                            session.saveOrUpdate(val);
                        }
                    } catch (Exception e) {
                        if (tr != null) {
                            tr.rollback();
                        }
                        LogHelper.log(log, e);
                    } finally {
                        if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                            tr.commit();
                        }
                    }
                } catch (HibernateException e) {
                    return false;
                } finally {
                    if (isOpenedNewSession) {
                        session.close();
                        session = null;
                        SessionTracker.getInstance().sessionClosing(
                                "ApplicationSettingsHolder");
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private String handleNewValue(ApplicationSettingsKeys key, String value) {
        if (key == null) {
            return value;
        }

        if (value == null || value.isEmpty()) {
            return value;
        }

        return value;
    }

    private String handleReadValue(ApplicationSettingsKeys key, String value) {
        if (key == null) {
            return value;
        }

        if (value == null || value.isEmpty()) {
            return value;
        }

        return value;
    }

    public static String getRIS_ADDRESS() {
        return RIS_ADDRESS;
    }

    public static void setRIS_ADDRESS(String rIS_ADDRESS) {
        RIS_ADDRESS = rIS_ADDRESS;
    }

}
