package it.nexera.ris.common.helpers.logic;

import com.hrdo.osirix.OsirixHandler;
import com.hrdo.pacs.commons.AET;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DVDProducer;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Pacs;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalProduceCD;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class RobotHelper extends BaseHelper {

    private static final Logger robotErrorLog = CustomLibLoggerFactory.getRobotErrorLogger();

    private static final Logger robotInfoLog = CustomLibLoggerFactory.getRobotInfoLogger();
    
    private static String prepareUrl(DVDProducer dicomEntity) {
        return "http://" + dicomEntity.getIpAddress() + ":" + dicomEntity.getPort();
    }

    public static boolean produceCD(DVDProducer robot, Pacs pacs, Long requestId, String accessNumber) {
        try {
            if (requestId != null) {
                String url = prepareUrl(robot);
                LogHelper.log(robotInfoLog, "osirix connection settings = <<" + url + ">>");
                OsirixHandler handler = new OsirixHandler(url);
                LogHelper.log(robotInfoLog, "osirix handler created ");
                LogHelper.log(robotInfoLog, "For creating sourceAET used this parameters << name = "
                        + pacs.getAetPacs() + "; IP = "
                        + pacs.getiPHostPacs() + "; port = "
                        + pacs.getPortHostPacs().toString() + ">>");
                AET sourceAET = new AET(pacs.getAetPacs(), pacs.getiPHostPacs(), pacs.getPortHostPacs().toString());

                LogHelper.log(robotInfoLog, "For creating destAET used this parameters << name = "
                        + robot.getAet() + "; IP = "
                        + robot.getIpAddress()
                        + "; port = "
                        + robot.getDicomPort().toString()
                        + ">>");
                AET destAET = new AET(robot.getAet(), robot.getIpAddress(),
                        robot.getDicomPort().toString());
                LogHelper.log(robotInfoLog, "_________openOsirixAccessionNumbers in openOsirixAction function_________");
                OsirixHelper.sendStudyTo(accessNumber, sourceAET, destAET, robotInfoLog);
                if (handler.isErrors()) {
                    LogHelper.log(robotInfoLog, "[A] handler.isErrors() return true");
                    LogHelper.log(robotErrorLog, "Call error-result : {}  handler.getRemoteErrorCode()"
                            + handler.getRemoteErrorCode());
                    LogHelper.log(robotInfoLog, "call checkForStudyToAction function for destAET");
                } else {
                    LogHelper.log(robotInfoLog, "TUTTO OK!");
                    LogHelper.log(robotInfoLog, "[A] handlerOsirix.isErrors() return false");
                    createDicomHistory(robot, pacs, requestId);
                    return true;
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return false;
    }

    private static void createDicomHistory(DVDProducer robot, Pacs pacs, Long requestId) {
        try {
            HistoricalProduceCD historicalProduceCD = new HistoricalProduceCD();
            historicalProduceCD.setRadiologyExamRequest(DaoManager.get(RadiologyExamRequest.class, requestId));
            historicalProduceCD.setPacs(pacs);
            historicalProduceCD.setRobot(robot);
            historicalProduceCD.setUser(DaoManager.get(User.class,  UserHolder.getInstance().getCurrentUser().getId()));
            historicalProduceCD.setActionDate(new Date());
            DaoManager.save(historicalProduceCD, true);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }
}
