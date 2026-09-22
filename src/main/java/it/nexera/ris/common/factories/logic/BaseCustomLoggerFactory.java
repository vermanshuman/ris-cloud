package it.nexera.ris.common.factories.logic;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;

public class BaseCustomLoggerFactory {

    protected static final Long DEFFAULT_MAX_FILE_SIZE = 5000000L;

    protected static final Integer DEFFAULT_MAX_BACKUP_INDEX = 5;

    protected static final Level DEFFAULT_LOG_LEVEL = Level.INFO;

    protected static final Boolean DEFFAUL_USE_CATALINA_LOG_DIRECTORY = Boolean.TRUE;

    protected static final String CATALINA_LOG_DIRECTORY = System.getProperty("catalina.home")
            + File.separator
            + "logs"
            + File.separator;

    protected static final String DEFFAULT_LOG_PATH = "";

    private static void baseInitLogger(Logger logger, String fileName,
                                       String path, boolean useCatalinaLogDirectory, Long maxFileSize,
                                       Integer maxBackupIndex, Level logLevel) {
        try {
            org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) logger;

            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();

            String appenderName = "fileAppender_" + fileName;
            if (config.getAppenders().containsKey(appenderName)) {
                return;
            }

            String filePath = (useCatalinaLogDirectory ? CATALINA_LOG_DIRECTORY : "") + path + fileName;

            RollingFileAppender appender = RollingFileAppender.newBuilder()
                    .setName(appenderName)
                    .withFileName(filePath)
                    .withFilePattern(filePath + ".%i")
                    .withPolicy(SizeBasedTriggeringPolicy.createPolicy(maxFileSize.toString()))
                    .withStrategy(DefaultRolloverStrategy.newBuilder().withMax(maxBackupIndex.toString()).build())
                    .setLayout(PatternLayout.newBuilder().withPattern("%d{ISO8601} [%p] %m%n").build())
                    .build();

            appender.start();

            config.addAppender(appender);
            coreLogger.addAppender(appender);

            Configurator.setLevel(coreLogger.getName(), logLevel);
            context.updateLoggers();
        } catch (Exception e) {
            //It can be only in case of incorrect logger setting
            e.printStackTrace();
        }
    }

    protected static void initLogger(Logger logger, String fileName,
                                     String path, Long maxFileSize, Integer maxBackupIndex,
                                     Boolean useCatalinaLogDirectory, Level logLevel) {
        baseInitLogger(logger, fileName, path, useCatalinaLogDirectory,
                maxFileSize, maxBackupIndex, logLevel);
    }

    protected static void initLogger(Logger logger, String fileName,
                                     String path, Long maxFileSize, Integer maxBackupIndex,
                                     Level logLevel) {
        baseInitLogger(logger, fileName, path,
                DEFFAUL_USE_CATALINA_LOG_DIRECTORY, maxFileSize,
                maxBackupIndex, logLevel);
    }

    protected static void initLogger(Logger logger, String fileName,
                                     Level logLevel) {
        baseInitLogger(logger, fileName, DEFFAULT_LOG_PATH,
                DEFFAUL_USE_CATALINA_LOG_DIRECTORY, DEFFAULT_MAX_FILE_SIZE,
                DEFFAULT_MAX_BACKUP_INDEX, logLevel);
    }

    public static Logger getCustomLogger(String fileName, String path,
                                         Long maxFileSize, Integer maxBackupIndex,
                                         Boolean useCatalinaLogDirectory, Level logLevel) {
        Logger logger = LogManager.getLogger(fileName);

        initLogger(logger, fileName, path, maxFileSize, maxBackupIndex,
                useCatalinaLogDirectory, logLevel);

        return logger;
    }

    public static Logger getCustomLogger(String fileName, String path,
                                         Long maxFileSize, Integer maxBackupIndex, Level logLevel) {
        Logger logger = LogManager.getLogger(fileName);

        initLogger(logger, fileName, path, maxFileSize, maxBackupIndex,
                logLevel);

        return logger;
    }

    public static Logger getCustomLogger(String loggerFileName, Level logLevel) {
        Logger logger = LogManager.getLogger(loggerFileName);

        initLogger(logger, loggerFileName, logLevel);

        return logger;
    }

    public static Logger getCustomLogger(String loggerFileName) {
        Logger logger = LogManager.getLogger(loggerFileName);

        initLogger(logger, loggerFileName, DEFFAULT_LOG_LEVEL);

        return logger;
    }
}
