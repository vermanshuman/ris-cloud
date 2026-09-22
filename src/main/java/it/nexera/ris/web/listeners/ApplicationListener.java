package it.nexera.ris.web.listeners;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.IConnectionListner;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.Module;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.integration.hl7.Hl7ReceiveHelper;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.*;
import it.nexera.ris.web.services.base.ServiceHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.sql.JoinType;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class ApplicationListener implements ServletContextListener, IConnectionListner {
    public transient final Logger log = LogManager.getLogger(getClass());

    private final Properties projectProperties = new Properties();

    private static final String PROJECT_PROPERTIES_FILE_NAME = "project.properties";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Locale.setDefault(Locale.ITALIAN);
        System.out.println("Using JSF: "
                + FacesContext.class.getPackage().getImplementationVersion());

        initializeProps();

        FileHelper.setRealPath(servletContextEvent.getServletContext()
                .getRealPath("/"));

        HibernateUtil.addConnectionListener(this);
        HibernateUtil.getSessionFactory(false);
    }

    private void initializeProps() {
        System.out.println("Start reading project properties...");

        try (InputStream is = new FileInputStream(new File("./"
                + PROJECT_PROPERTIES_FILE_NAME))) {
            projectProperties.load(is);
        } catch (FileNotFoundException e) {
            try (InputStream is = ApplicationListener.class.getResourceAsStream("/"
                    + PROJECT_PROPERTIES_FILE_NAME)) {
                projectProperties.load(is);
            } catch (IOException e1) {
                LogHelper.log(log, "Project properties hasn't been read.");
                return;
            }
        } catch (IOException e) {
            try (InputStream is = ApplicationListener.class.getResourceAsStream("/"
                    + PROJECT_PROPERTIES_FILE_NAME)) {
                projectProperties.load(is);
            } catch (IOException e1) {
                LogHelper.log(log, "Project properties hasn't been read.");
                return;
            }
        }

        ApplicationSettingsHolder.setRIS_ADDRESS(projectProperties
                .getProperty("ris_address"));

        System.out.println("Reading project properties ended.");
    }

    @Override
    public void fireConnetionEstablished() {
        tryStopServices();

        startServices();

        startListeners();

        try {
            initData();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void initData() throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            PersistenceSession ps = null;
            Transaction tr = null;
            if (Boolean.TRUE.toString().equals(projectProperties.getProperty("autoUpdateDB"))) {
                try {
                    ps = new PersistenceSession();
                    tr = ps.getSession().beginTransaction();
                    DBFiller.createViews(ps.getSession());
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        tr.rollback();
                    }
                } finally {
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        tr.commit();
                    }
                    if (ps != null) {
                        ps.closeSession();
                    }
                }
            }

            if (Boolean.TRUE.toString().equals(projectProperties.getProperty("autoUpdateDB"))) {
                try {
                    ps = new PersistenceSession();
                    tr = ps.getSession().beginTransaction();
                    DBFiller.createTriggers(ps.getSession());
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        tr.rollback();
                    }
                } finally {
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        tr.commit();
                    }
                    if (ps != null) {
                        ps.closeSession();
                    }
                }
            }

            if (Boolean.TRUE.toString().equals(projectProperties.getProperty("autoUpdateDB"))) {
                try {
                    ps = new PersistenceSession();
                    tr = ps.getSession().beginTransaction();
                    DBFiller.createMViews(ps.getSession());
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        tr.rollback();
                    }
                } finally {
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        tr.commit();
                    }
                    if (ps != null) {
                        ps.closeSession();
                    }
                }
            }

            try {
                ps = new PersistenceSession();
                tr = ps.getSession().beginTransaction();
                DBFiller.fillSequences(ps.getSession());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }

            try {
                ps = new PersistenceSession();
                tr = ps.getSession().beginTransaction();
                DBFiller.createIndexes(ps.getSession());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }

            try {
                ps = new PersistenceSession();
                tr = ps.getSession().beginTransaction();

                DBFiller.updateEmailTypes(ps.getSession());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }

            try {
                ps = new PersistenceSession();
                tr = ps.getSession().beginTransaction();

                if (DBFiller.needFillEntity(ps.getSession(), Role.class)) {
                    for (Role item : DBFiller.fillRoles()) {
                        ConnectionManager.save(item, ps.getSession());
                    }
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }

            try {
                ps = new PersistenceSession();
                tr = ps.getSession().beginTransaction();

                if (DBFiller.needFillUsers(ps.getSession())) {
                    for (User item : DBFiller.fillUsers(ps.getSession())) {
                        ConnectionManager.save(item, ps.getSession());
                    }
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }

            fillFromXml(Asl.class, "fillAsls");
            fillFromXml(AslRegion.class, "fillAslRegions");
            fillFromXml(Province.class, "fillProvinces");

            // need be after asl, aslregion and province
            fillFromXml(City.class, "fillCities");

            // need be after aslregion
            fillFromXml(Nationality.class, "fillNationalities");

            fillFromXml(RadiologyExam.class, "fillRadiologies");

            fillFromXml(Module.class, "fillModuleNonEnums", "getCode", "code");

            fillFromXml(ModulePage.class, "fillModulePages", "getModule_code", "module.code");

            fillFromXml(Permission.class, "fillPermissions", "getModule_code", "module.code");
        } finally {
            HibernateUtil.removeConnectionListener(this);
            HibernateUtil.shutdown();
            HibernateUtil.getSessionFactory(true);
            HibernateUtil.addConnectionListener(this);
        }
    }

    private void fillFromXml(Class<? extends Entity> entity, String methodName) {
        fillFromXml(entity, methodName, null, null);
    }

    private void fillFromXml(Class<? extends Entity> entity, String methodName,
                             String checkedMethodName, String checkedFieldName) {
        PersistenceSession ps = null;
        Transaction tr = null;
        try {
            ps = new PersistenceSession();
            tr = ps.getSession().beginTransaction();
            if (DBFiller.needFillEntity(ps.getSession(), entity)) {
                File file = new File(FileHelper.getLocalDir(), String.format("/WEB-INF/classes/data/%sData.xml",
                        entity.getSimpleName()));
                Method method = null;
                method = ReflectionHelper.getMethod(DBFiller.class, methodName, File.class, Session.class);

                if (method != null) {
                    List<? extends Entity> list = (List<? extends Entity>) (method.invoke(DBFiller.class, file, ps.getSession()));

                    if (!ValidationHelper.isNullOrEmpty(list)) {
                        Method checkedMethod = getCheckedMethod(entity, checkedMethodName);
                        for (Entity item : list) {
                            if (checkedMethod != null) {
                                Object checkedValue = checkedMethod.invoke(item, new Class[0]);
                                Long recordCount = null;
                                if (checkedValue != null) {
                                    recordCount = ConnectionManager.getCount(entity, "id", buildAliases(checkedFieldName, entity), new Criterion[]{
                                            Restrictions.eq(checkedFieldName, checkedValue)
                                    }, ps.getSession());
                                }
                                if (recordCount == null || recordCount > 0) {
                                    continue;
                                }
                            }
                            ConnectionManager.save(item, ps.getSession());
                        }
                    }
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                tr.rollback();
            }
        } finally {
            if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                tr.commit();
            }
            if (ps != null) {
                ps.closeSession();
            }
        }
    }

    private <T extends Entity> CriteriaAlias[] buildAliases(String relation, Class<T> entity) {
        String[] fields = relation.split("\\.");
        if (fields != null) {
            List<CriteriaAlias> criteriaAliases = new ArrayList<>();
            for (int i = 0; i < fields.length - 1; i++) {
                criteriaAliases.add(new CriteriaAlias(fields[i], fields[i], JoinType.INNER_JOIN));
            }
            return criteriaAliases.toArray(new CriteriaAlias[0]);
        }
        return null;
    }

    private Method getCheckedMethod(Class<? extends Entity> entity, String checkedMethodName) {
        Method method = null;
        try {
            if(!ValidationHelper.isNullOrEmpty(checkedMethodName))
                method = entity.getDeclaredMethod(checkedMethodName);
        } catch (NoSuchMethodException e) {
            try {
                method = entity.getSuperclass().getDeclaredMethod(checkedMethodName);
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }
        }
        return method;
    }

    private void startServices() {
        ServiceHolder.getInstance().setKeepAliveService(new KeepAliveService());

        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("keepAliveService"))) {
            System.out.println("Running KeepAliveService...");
            ServiceHolder.getInstance().getKeepAliveService().start();
        }

        ServiceHolder.getInstance().setRadiologyRequestDisableService(
                (new RadiologyRequestDisableService()));

        if (Boolean.TRUE.toString().equals(
                projectProperties.getProperty("disableRequestService"))) {
            System.out.println("Running RadiologyRequestDisableService...");
            ServiceHolder.getInstance().getRadiologyRequestDisableService()
                    .start();
        }

        ServiceHolder.getInstance().setTempRemoverService(
                new TempRemoverService(new String[]{
                        FileHelper.getTempDir(),
                        FileHelper
                                .getCustomFolderDir("/resources/reports/temp/")
                }));

        if (Boolean.TRUE.toString().equals(
                projectProperties.getProperty("tempRemoverService"))) {
            System.out.println("Running TempRemoverService...");
            ServiceHolder.getInstance().getTempRemoverService().start();
        }

        ServiceHolder.getInstance().setHl7RadExamRequestMessagesSenderService(
                new Hl7RadExamRequestMessagesSenderService());

        if (Boolean.TRUE.toString().equals(
                projectProperties.getProperty("hl7MessagesSenderService"))) {
            System.out
                    .println("Running Hl7RadExamRequestMessagesSenderService...");
            ServiceHolder.getInstance()
                    .getHl7RadExamRequestMessagesSenderService().start();
        }

        ServiceHolder.getInstance().setSettingSynchronizerService(
                new SettingSynchronizerService());

        if (Boolean.TRUE.toString().equals(
                projectProperties.getProperty("settingSynchronizerService"))) {
            System.out.println("Running SettingSynchronizerService...");
            ServiceHolder.getInstance().getSettingSynchronizerService().start();
        }

        ServiceHolder.getInstance()
                .setSessionFilterService(new SessionFilterService());

        if (Boolean.TRUE.toString().equals(
                projectProperties.getProperty("sessionFilterService"))) {
            System.out.println("Running SessionFilterService...");
            //ServiceHolder.getInstance().getSessionFilterService().start();
        }

        ServiceHolder.getInstance()
                .setEmptySessionFilterService(new EmptySessionFilterService());

        if (Boolean.TRUE.toString().equals(
                projectProperties.getProperty("emptySessionFilterService"))) {
            System.out.println("Running EmptySessionFilterService...");
            //ServiceHolder.getInstance().getEmptySessionFilterService().start();
        }

        ServiceHolder.getInstance()
                .setCleanShortRequestService(new CleanShortRequestService());

        if (Boolean.TRUE.toString().equals(
                projectProperties.getProperty("cleanShortRequestService"))) {
            System.out.println("Running CleanShortRequestService...");
            ServiceHolder.getInstance().getCleanShortRequestService().start();
        }

        ServiceHolder.getInstance().setHl7MessageForwardingService(new HL7MessageForwardingService());
        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("hl7MessageForwardingService"))) {
            System.out.println("Running HL7MessageForwardingService...");
            ServiceHolder.getInstance().getHl7MessageForwardingService().start();
        }

        ServiceHolder.getInstance().setHl7ReportForwardingService(new HL7ReportForwardingService());
        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("hL7ReportForwardingService"))) {
            System.out.println("Running HL7ReportForwardingService...");
            ServiceHolder.getInstance().getHl7ReportForwardingService().start();
        }

        ServiceHolder.getInstance().setUserDisableService(new UserDisableService());
        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("userDisableService"))) {
            System.out.println("Running UserDisableService...");
            ServiceHolder.getInstance().getUserDisableService().start();
        }
    }

    private void startListeners() {
        if (Boolean.TRUE.toString().equals(
                projectProperties.getProperty("Hl7Listener"))) {
            Integer hl7_listen_port = Integer
                    .parseInt(ApplicationSettingsHolder.getInstance()
                            .getByKey(ApplicationSettingsKeys.HL7_LISTEN_PORT)
                            .getValue());

            if (!ValidationHelper.isNullOrEmpty(hl7_listen_port)) {
                System.out.println("Running Hl7Receive listener on the port = "
                        + hl7_listen_port.intValue() + "...");
                Hl7ReceiveHelper.getInstance().startHl7ReceiveHelper(
                        hl7_listen_port.intValue());
            } else {
                System.out
                        .println("hl7_listen_port is null or 0, Hl7Receive listener not started...");
            }
        }
    }

    private void tryStopServices() {
        try {
            if (ServiceHolder.getInstance().getKeepAliveService() != null) {
                System.out.println("Stopping KeepAliveService...");
                ServiceHolder.getInstance().getKeepAliveService().stop();
                ServiceHolder.getInstance().setKeepAliveService(null);
            }

            if (ServiceHolder.getInstance().getRadiologyRequestDisableService() != null) {
                System.out
                        .println("Stopping RadiologyRequestDisableService...");
                ServiceHolder.getInstance().getRadiologyRequestDisableService()
                        .stop();
                ServiceHolder.getInstance().setRadiologyRequestDisableService(
                        null);
            }

            if (ServiceHolder.getInstance().getTempRemoverService() != null) {
                System.out.println("Stopping TempRemoverService...");
                ServiceHolder.getInstance().getTempRemoverService().stop();
                ServiceHolder.getInstance().setTempRemoverService(null);
            }

            if (ServiceHolder.getInstance()
                    .getHl7RadExamRequestMessagesSenderService() != null) {
                System.out
                        .println("Stopping Hl7RadExamRequestMessagesSenderService...");
                ServiceHolder.getInstance()
                        .getHl7RadExamRequestMessagesSenderService().stop();
                ServiceHolder.getInstance()
                        .setHl7RadExamRequestMessagesSenderService(null);
            }

            if (ServiceHolder.getInstance().getSettingSynchronizerService() != null) {
                System.out.println("Stopping SettingSynchronizerService...");
                ServiceHolder.getInstance().getSettingSynchronizerService()
                        .stop();
                ServiceHolder.getInstance().setSettingSynchronizerService(null);
            }

            if (ServiceHolder.getInstance()
                    .getHl7MessageForwardingService() != null) {
                System.out
                        .println("Stopping HL7MessageForwardingService...");
                ServiceHolder.getInstance()
                        .getHl7MessageForwardingService().stop();
                ServiceHolder.getInstance()
                        .setHl7MessageForwardingService(null);
            }
            if (ServiceHolder.getInstance()
                    .getHl7ReportForwardingService() != null) {
                System.out
                        .println("Stopping HL7ReportForwardingService...");
                ServiceHolder.getInstance()
                        .getHl7ReportForwardingService().stop();
                ServiceHolder.getInstance()
                        .setHl7ReportForwardingService(null);
            }


            Hl7ReceiveHelper.getInstance().stopHl7ReceiveHelper();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void fireConnetionResufed() {
        tryStopServices();
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        tryStopServices();
        HibernateUtil.shutdown();

        for (Object o : C3P0Registry.getPooledDataSources()) {
            try {
                ((PooledDataSource) o).close();
            } catch (Exception e) {
                // oh well, let tomcat do the complaing for us.
            }
        }

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log.info(String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                log.fatal(String.format("Error deregistering driver %s", driver), e);
            }
        }
    }
}
