package it.nexera.ris.common.factories;

import it.nexera.ris.common.factories.logic.BaseCustomLoggerFactory;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class CustomLibLoggerFactory extends BaseCustomLoggerFactory {

    private final static String RIS_LIB_PATH = "ris.lib" + File.separator;

    private final static String RIS_ACTIVITY_PATH = "ris.activity" + File.separator;

    private final static String RIS_SERVICE_PATH = "ris.service" + File.separator;

    private final static Long RIS_LIB_MAX_FILE_SIZE = Long.valueOf(1000000);

    public static Logger getDicomErrorLogger() {
        return getCustomLogger("ris.dicom.error.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.LIB_ERROR);
    }

    public static Logger getDicomInfoLogger() {
        return getCustomLogger("ris.dicom.info.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.LIB_INFO);
    }

    public static Logger getHl7ErrorLogger() {
        return getCustomLogger("ris.hl7.error.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.LIB_ERROR);
    }

    public static Logger getHl7InfoLogger() {
        return getCustomLogger("ris.hl7.info.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.LIB_INFO);
    }

    public static Logger getOsirixErrorLogger() {
        return getCustomLogger("ris.osirix.error.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.LIB_ERROR);
    }

    public static Logger getOsirixInfoLogger() {
        return getCustomLogger("ris.osirix.info.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.LIB_INFO);
    }

    public static Logger getRobotErrorLogger() {
        return getCustomLogger("ris.robot.error.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.LIB_ERROR);
    }

    public static Logger getRobotInfoLogger() {
        return getCustomLogger("ris.robot.info.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.LIB_INFO);
    }

    public static Logger getActivityErrorLogger() {
        return getCustomLogger("activity.error.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.LIB_INFO);
    }

    public static Logger getHl7MessageInfoLoger() {
        return getCustomLogger("hl7.message.log", RIS_LIB_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.HL7_INFO);
    }

    public static Logger getActivityInfoLoger() {
        return getCustomLogger("activity.info.log", RIS_ACTIVITY_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.ACTIVITY_INFO);
    }

    public static Logger getActivityTraceInfoLogger() {
        return getCustomLogger("activity.trace.info.log", "ris.trace" + File.separator,
                RIS_LIB_MAX_FILE_SIZE * 10, DEFFAULT_MAX_BACKUP_INDEX * 2,
                CustomLevel.ACTIVITY_INFO);
    }

    public static Logger getServiceInfoLogger() {
        return getCustomLogger("services.info.log", RIS_SERVICE_PATH,
                RIS_LIB_MAX_FILE_SIZE, DEFFAULT_MAX_BACKUP_INDEX,
                CustomLevel.SERVICE_INFO);
    }
}
