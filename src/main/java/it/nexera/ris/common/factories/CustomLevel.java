package it.nexera.ris.common.factories;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.spi.StandardLevel;

public class CustomLevel {

    private static final long serialVersionUID = 4641115434060788463L;

    public static final int LIB_ERROR_INT = StandardLevel.ERROR.intLevel() - 1;

    public static final Level LIB_ERROR = Level.forName("LIB_ERROR", LIB_ERROR_INT);

    public static final int LIB_INFO_INT = StandardLevel.INFO.intLevel() + 1;

    public static final Level LIB_INFO = Level.forName("LIB_INFO", LIB_INFO_INT);

    public static final int LIB_WARN_INT = StandardLevel.WARN.intLevel() - 1;

    public static final Level LIB_WARN = Level.forName("LIB_WARN", LIB_WARN_INT);

    public static final int HL7_INFO_INT = StandardLevel.INFO.intLevel() + 2;

    public static final Level HL7_INFO = Level.forName("HL7_INFO", HL7_INFO_INT);

    public static final int ACTIVITY_INFO_INT = StandardLevel.INFO.intLevel() + 2;

    public static final Level ACTIVITY_INFO = Level.forName("ACTIVITY_INFO", ACTIVITY_INFO_INT);

    public static final int SERVICE_INFO_INT = StandardLevel.INFO.intLevel() + 3;

    public static final Level SERVICE_INFO = Level.forName("SERVICE_INFO", SERVICE_INFO_INT);

}
