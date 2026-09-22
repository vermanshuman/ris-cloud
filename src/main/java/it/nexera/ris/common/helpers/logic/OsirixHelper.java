package it.nexera.ris.common.helpers.logic;

import com.hrdo.osirix.OsirixHandler;
import com.hrdo.pacs.DicomHandler;
import com.hrdo.pacs.commons.AET;
import it.nexera.ris.common.enums.PacsType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Hospital;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Osirix;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Pacs;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OsirixHelper extends BaseHelper {

    private static final Logger osirixInfoLog = CustomLibLoggerFactory.getOsirixInfoLogger();

    private static final Logger osirixErrorLog = CustomLibLoggerFactory.getOsirixErrorLogger();

    public static void openOsirixAccessionNumbers(String accessNumber,
                                                  String settingsOsirix) throws Exception {
        openOsirixAccessionNumbers(accessNumber, settingsOsirix, osirixInfoLog, osirixErrorLog);
    }

    public static void openOsirixAccessionNumbers(String accessNumber,
                                                  String settingsOsirix, Logger info, Logger error) throws Exception {
        LogHelper.log(info, "start execute callDBWindowFind action for access number = <<"
                + accessNumber + ">>");

        OsirixHandler handler = new OsirixHandler(settingsOsirix);
        LogHelper.log(info, "new OsirixHandler created");
        handler.callDBWindowFind(accessNumber); // _____________callDBWindowFind

        if (handler.isErrors()) {
            LogHelper.log(info, "Errors after callDBWindowFind");

            LogHelper.log(error, "Call error-result : {}  handler.getRemoteErrorCode()"
                    + handler.getRemoteErrorCode());
        }

        LogHelper.log(info, "callDBWindowFind action for access number = <<" + accessNumber + ">> was executed");
    }

    public static int countDicomSourceImages(String accessNumber,
                                             DicomHandler handlerDicom) {
        return countDicomSourceImages(accessNumber, handlerDicom, osirixInfoLog);
    }

    public static int countDicomSourceImages(String accessNumber,
                                             DicomHandler handlerDicom, Logger info) {
        LogHelper.log(info, "start to count countDicomSourceImages...");
        int countDicomSourceImages = handlerDicom.countStudyImages(accessNumber);
        LogHelper.log(info, "countDicomSourceImages = <<" + countDicomSourceImages + ">>");
        return countDicomSourceImages;
    }

    public static int countDicomDestImages(String accessNumber,
                                           DicomHandler handlerDicom, AET destAET) {
        return countDicomDestImages(accessNumber, handlerDicom, destAET, osirixInfoLog);
    }

    public static int countDicomDestImages(String accessNumber,
                                           DicomHandler handlerDicom, AET destAET, Logger info) {
        LogHelper.log(info, "start to count countDicomDestImages...");

        int countDicomDestImages = handlerDicom.countStudyImages(accessNumber,
                destAET);

        LogHelper.log(info, "countDicomDestImages = <<" + countDicomDestImages + ">>");
        return countDicomDestImages;
    }

    public static int calculateNewProgressBarValue(int countDicomSourceImages,
                                                   int countDicomDestImages) {
        if (countDicomSourceImages != 0) {
            double onePercent = countDicomSourceImages / 100.0;
            double current = countDicomDestImages / onePercent;

            return (int) current;
        }
        return 0;
    }

    public static String getWeasisUrlArray(String weasisUrl,
                                           List<RadExamRequestWrapper> radExamRequestWrappers) {
        StringBuilder finalUrlArray = new StringBuilder();

        boolean isFirst = true;
        for (RadExamRequestWrapper requestWrapper : radExamRequestWrappers) {
            if (!isFirst) {
                finalUrlArray.append("%_del_%");
            }
            finalUrlArray.append(weasisUrl);
            finalUrlArray.append(requestWrapper.getAccessNumber());

            isFirst = false;
        }

        return finalUrlArray.toString();
    }

    public static List<RadExamRequestWrapper> getRequestWrappersFromFileEntityId(
            Long fileEntityId) {
        List<RadExamRequestWrapper> requestsWrappers = null;

        try {
            List<RadiologyExamRequestItem> requestsItems = DaoManager.load(
                    RadiologyExamRequestItem.class, new Criterion[]{
                            Restrictions.eq("fileEntity.id", fileEntityId)
                    });

            List<Long> requestsIds = new ArrayList<Long>();

            if (!ValidationHelper.isNullOrEmpty(requestsItems)) {
                for (RadiologyExamRequestItem reri : requestsItems) {
                    if (!requestsIds.contains(reri.getRadiologyExamRequest()
                            .getId())) {
                        requestsIds.add(reri.getRadiologyExamRequest().getId());
                    }
                }

                if (!requestsIds.isEmpty()) {
                    List<RadiologyExamRequest> requests = DaoManager.load(
                            RadiologyExamRequest.class, new Criterion[]{
                                    Restrictions.in("id", requestsIds)
                            });

                    if (!ValidationHelper.isNullOrEmpty(requests)) {
                        requestsWrappers = new ArrayList<RadExamRequestWrapper>();

                        for (RadiologyExamRequest request : requests) {
                            requestsWrappers.add(request
                                    .getRadExamRequestWrapperFromRequest());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return requestsWrappers;
    }

    public static void secondOsirixFlow(
            List<RadExamRequestWrapper> radiologyExamRequestsWrappers,
            HistoricalReport selectedHistoricalReport, PacsType pacsType,
            String osirixConnectionSettings, Osirix currentOsirix) {
        Long hospitalId = null;
        Long sectorId = null;

        if (radiologyExamRequestsWrappers != null) {
            hospitalId = radiologyExamRequestsWrappers.get(0).getHospitalId();
            sectorId = radiologyExamRequestsWrappers.get(0).getSectorId();
        } else if (selectedHistoricalReport != null) {
            Hospital hospitalFromHistory = null;
            try {
                hospitalFromHistory = DaoManager.get(Hospital.class, new Criterion[]{
                                Restrictions.eq("code", selectedHistoricalReport.getHospitalCode())
                        });
            } catch (HibernateException | InstantiationException
                    | IllegalAccessException | PersistenceBeanException e) {
                LogHelper.log(osirixErrorLog, e);
            }

            if (hospitalFromHistory != null) {
                hospitalId = hospitalFromHistory.getId();
            }

            sectorId = selectedHistoricalReport.getSectorId();
        } else {
            LogHelper.log(osirixErrorLog,
                    "radiologyExamRequestsWrappers or selectedHistoricalReport is null");
        }

        LogHelper.log(osirixInfoLog,
                "_______________________________________START secondOsirixFlow______________________________________________");

        if (hospitalId != null && sectorId != null) {

            Pacs pacs = null;
            try {
                pacs = DaoManager.get(Pacs.class,
                        new Criterion[]{
                                Restrictions.eq("hospital.id", hospitalId),
                                Restrictions.eq("sector.id", sectorId),
                                Restrictions.eq("pacsType", pacsType)
                        });
            } catch (HibernateException | InstantiationException
                    | IllegalAccessException | PersistenceBeanException e) {
                LogHelper.log(osirixErrorLog, e);
            }

            if (pacs != null) {
                if (radiologyExamRequestsWrappers != null) {
                    for (RadExamRequestWrapper rerw : radiologyExamRequestsWrappers) {
                        LogHelper.log(osirixInfoLog, "call open osirix action for radiology exam request with id = <<"
                                + rerw.getId() + ">>");

                        LogHelper.log(osirixInfoLog, "checkNumber = 0");

                        secondOsirixFlowAction(rerw.getAccessNumber(), pacs,
                                osirixConnectionSettings, currentOsirix);
                    }
                } else {
                    LogHelper.log(osirixInfoLog, "call open osirix action for imported historical report with id = <<"
                            + selectedHistoricalReport.getId() + ">>");

                    LogHelper.log(osirixInfoLog, "checkNumber = 0");

                    secondOsirixFlowAction(
                            selectedHistoricalReport.getAccessNumber(), pacs,
                            osirixConnectionSettings, currentOsirix);
                }
            } else {
                LogHelper.log(osirixInfoLog, "pacs for hospital not found");
                MessageHelper.addGlobalMessage(
                        FacesMessage.SEVERITY_WARN,
                        ResourcesHelper.getValidation("warning"),
                        ResourcesHelper.getValidation("pdfGenerationOsirixHospitalNotFound"));
            }

            LogHelper.log(osirixInfoLog,
                    "_______________________________________END________________________________________________");
        } else {
            LogHelper.log(osirixErrorLog, "hospitalId or sectorId is null");
        }
    }

    public static void secondFlowCheckForStudyToAction(final String accessNumber, Pacs pacs, final AET sourceAET,
            OsirixHandler handlerOsirix, final AET destAET,
            final String osirixConnectionSettings) throws InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        LogHelper.log(osirixInfoLog, "____executorService second flow started____");

        List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();

        tasks.add(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LogHelper.log(osirixInfoLog, "____thread 1 second flow (sendStudyTo) started____");

                sendStudyTo(accessNumber, sourceAET, destAET, osirixInfoLog);
                LogHelper.log(osirixInfoLog, "____thread 1 second flow (sendStudyTo) ended____");
                return null;
            }
        });

        tasks.add(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LogHelper.log(osirixInfoLog, "____thread 2 second flow (checkNumberLogic) started____");

                checkNumberLogic(accessNumber, sourceAET, destAET, osirixConnectionSettings);

                LogHelper.log(osirixInfoLog, "____thread 2 second flow (checkNumberLogic) ended____");
                return null;
            }
        });

        executorService.invokeAll(tasks);
        LogHelper.log(osirixInfoLog, "____invokeAll second flow ended____");

        executorService.shutdown();
        LogHelper.log(osirixInfoLog, "shutdown second flow ended____");
    }

    public static void sendStudyTo(String accessNumber, AET sourceAET, AET destAET, Logger info) {
        try {
            DicomHandler mainHandlerDicom = new DicomHandler(sourceAET);
            LogHelper.log(info, "checkForStudyToAction: new mainHandlerDicom created");
            LogHelper.log(info, "handlerDicom.sendStudyTo(accessNumber, destAET) return "
                                    + mainHandlerDicom.sendStudyTo(accessNumber, destAET));
        } catch (Exception e) {
            LogHelper.log(info, "checkForStudyToAction: exception appeared.");
        }
    }

    private static void checkNumberLogic(final String accessNumber,
                                         final AET sourceAET, final AET destAET,
                                         String osirixConnectionSettings) throws InterruptedException {
        boolean success = false;
        boolean openedImagesOnce = false;
        for (int checkNumber = 0; checkNumber < 6; ++checkNumber) {
            LogHelper.log(osirixInfoLog, "checkNumber = " + checkNumber);

            LogHelper.log(osirixInfoLog, "sleep for 1 second...");
            Thread.sleep(1000); // sleep for 1 sec

            DicomHandler handlerDicom = new DicomHandler(sourceAET);

            LogHelper.log(osirixInfoLog, "handlerDicom for checkNumber = {" + checkNumber + "} created");

            int countDicomSourceImages = OsirixHelper.countDicomSourceImages(
                    accessNumber, handlerDicom);

            int countDicomDestImages = OsirixHelper.countDicomDestImages(
                    accessNumber, handlerDicom, destAET);

            LogHelper.log(osirixInfoLog, "checkForStudyToAction: new handlerDicom created");
            if (countDicomDestImages == countDicomSourceImages) {
                success = true;
                break;
            } else {
                if (!openedImagesOnce && countDicomDestImages > 0) {
                    callOpenImages(accessNumber, osirixConnectionSettings);
                    openedImagesOnce = true;
                }

                LogHelper.log(osirixInfoLog, "countDicomDestImages != countDicomSourceImages (checkNumber = "
                        + checkNumber + " )");
            }
        }

        callOpenImages(accessNumber, osirixConnectionSettings);
        if (!success) {
            LogHelper.log(osirixInfoLog, "Study not transfer (checkNumber = 6)");
        }
    }

    private static void callOpenImages(String accessNumber, String connectionSettings) {
        try {
            LogHelper.log(osirixInfoLog, "_________openOsirixAccessionNumbers in checkNumber < 6 function_________");

            OsirixHelper.openOsirixAccessionNumbers(accessNumber, connectionSettings);
        } catch (Exception e) {
            LogHelper.log(osirixInfoLog, "<< -- some EXCEPTION occurred please check in error log file -- >>");
            LogHelper.log(osirixErrorLog, e);
        }
    }

    private static void secondOsirixFlowAction(String accessNumber, Pacs pacs,
                                               String osirixConnectionSettings, Osirix currentOsirix) {
        LogHelper.log(osirixInfoLog, "__start secondOsirixFlowAction__");

        OsirixHandler handler;

        LogHelper.log(osirixInfoLog, "start creating osirix handler");
        if (currentOsirix == null || currentOsirix.getAet() == null || currentOsirix.getIpAddress() == null || currentOsirix.getDicomPort() == null) {
            LogHelper.log(osirixInfoLog, "current osirix is null");
        } else {
            try {
                LogHelper.log(osirixInfoLog, "osirix connection settings = <<" + osirixConnectionSettings + ">>");
                handler = new OsirixHandler(osirixConnectionSettings);
                LogHelper.log(osirixInfoLog, "osirix handler created");

                LogHelper.log(osirixInfoLog,
                        "For creating sourceAET used this parameters << name = "
                                + pacs.getAetPacs() + "; IP = "
                                + pacs.getiPHostPacs() + "; port = "
                                + pacs.getPortHostPacs().toString() + ">>");
                AET sourceAET = new AET(pacs.getAetPacs(), pacs.getiPHostPacs(),
                        pacs.getPortHostPacs().toString());

                DicomHandler handlerDicom = new DicomHandler(sourceAET);

                LogHelper.log(osirixInfoLog,
                        "For creating destAET used this parameters << name = "
                                + currentOsirix.getAet() + "; IP = "
                                + currentOsirix.getIpAddress() + "; port = "
                                + currentOsirix.getDicomPort().toString() + ">>");
                AET destAET = new AET(currentOsirix.getAet(),
                        currentOsirix.getIpAddress(), currentOsirix.getDicomPort()
                        .toString());

                LogHelper.log(osirixInfoLog, "_________openOsirixAccessionNumbers in openOsirixAction function_________");

                OsirixHelper.openOsirixAccessionNumbers(accessNumber,
                        osirixConnectionSettings);

                int countDicomSourceImages = OsirixHelper.countDicomSourceImages(
                        accessNumber, handlerDicom);

                if (handler.isErrors()) {
                    LogHelper.log(osirixInfoLog, "[A] handler.isErrors() return true");

                    LogHelper.log(osirixErrorLog, "Call error-result : {}  handler.getRemoteErrorCode()"
                            + handler.getRemoteErrorCode());

                    LogHelper.log(osirixInfoLog, "call checkForStudyToAction function for destAET");

                    secondFlowCheckForStudyToAction(accessNumber, pacs, sourceAET,
                            handler, destAET, osirixConnectionSettings);
                } else {
                    LogHelper.log(osirixInfoLog, "TUTTO OK!");
                    LogHelper.log(osirixInfoLog, "[A] handlerOsirix.isErrors() return false");

                    if (countDicomSourceImages > 0) {
                        LogHelper.log(osirixInfoLog, "countDicomSourceImages > 0 ");

                        int countDicomDestImages = OsirixHelper
                                .countDicomDestImages(accessNumber, handlerDicom, destAET);

                        LogHelper.log(osirixInfoLog, "countDicomDestImages = <<" + countDicomDestImages + ">>");

                        if (countDicomSourceImages == countDicomDestImages) {
                            LogHelper.log(osirixInfoLog, "countDicomSourceImages == countDicomDestImages");

                            MessageHelper.addGlobalMessage(
                                    FacesMessage.SEVERITY_INFO,
                                    ResourcesHelper.getString("succesfull"),
                                    ResourcesHelper.getString("pdfGenerationOsirixSuccess"));
                        } else {
                            LogHelper.log(osirixInfoLog, "Count source - count dest ="
                                    + (countDicomSourceImages - countDicomDestImages));

                            if (countDicomSourceImages > countDicomDestImages) {
                                LogHelper.log(osirixInfoLog, "countDicomSourceImages > countDicomDestImages");

                                LogHelper.log(osirixInfoLog, "call checkForStudyToAction function");

                                secondFlowCheckForStudyToAction(accessNumber, pacs,
                                        sourceAET, handler, destAET,
                                        osirixConnectionSettings);
                            } else {
                                LogHelper.log(osirixInfoLog, "countDicomSourceImages < countDicomDestImages");

                                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                                                ResourcesHelper.getValidation("warning"),
                                                ResourcesHelper.getValidation("pdfGenerationOsirixPacsNotContainAppropriate"));
                            }
                        }
                    } else {
                        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                                ResourcesHelper.getValidation("warning"),
                                ResourcesHelper.getValidation("pdfGenerationOsirixImagesInPacsNotFound"));
                    }
                }
            } catch (Exception e) {
                LogHelper.log(osirixInfoLog, "<< -- some EXCEPTION occurred please check in error log file -- >>");
                LogHelper.log(osirixErrorLog, e);
            }
            LogHelper.log(osirixInfoLog, "for this access number work finished");
        }
    }

    public static List<Osirix> loadOsiricListBySector(HistoricalReport historicalReport)
            throws PersistenceBeanException, IllegalAccessException {
        List<Osirix> osirixList = null;
        if (historicalReport.getRadiologyExamRequest() != null) {
            if (historicalReport.getRadiologyExamRequest().getSector() != null) {
                osirixList = DaoManager.load(Osirix.class,
                        new Criterion[]{
                                Restrictions.eq("sector.id", historicalReport
                                        .getRadiologyExamRequest().getSector().getId())});
            } else {
                LogHelper.log(log, "Sector is null for historical report with id = "
                        + historicalReport.getId());
            }
        } else {
            if (historicalReport.getSectorId() != null) {
                osirixList = DaoManager.load(Osirix.class,
                        new Criterion[]{
                                Restrictions.eq("sector.id", historicalReport.getSectorId())
                        });
            } else {
                LogHelper.log(log, "Sector id is null for imported historical report with id = "
                        + historicalReport.getId());
            }
        }
        return osirixList;
    }
}
