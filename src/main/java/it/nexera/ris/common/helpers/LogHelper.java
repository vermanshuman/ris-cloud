package it.nexera.ris.common.helpers;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogHelper {

    public static String readStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static void log(Logger logger, Exception e) {
        logger.log(Level.ERROR, readStackTrace(e));
    }

    public static void log(Logger logger, Throwable e) {
        logger.log(Level.ERROR, readStackTrace(e));
    }

    public static void log(Logger logger, String msg) {
        logger.log(logger.getLevel(), msg.concat("\r\n"));
    }

    public static void debugInfo(Logger logger, String msg) {
        logger.info(msg.concat("\r\n"));
        System.out.println(msg);
    }
}
