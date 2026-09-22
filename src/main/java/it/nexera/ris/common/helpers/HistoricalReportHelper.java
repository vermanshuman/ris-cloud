package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HistoricalReportHelper extends BaseHelper implements Serializable {
    private static final long serialVersionUID = 3234440859872148187L;

    public static final Logger log = LogManager.getLogger(HistoricalReportHelper.class);

    private static class HistoricalReportHelperHolder {
        public static final HistoricalReportHelper instance = new HistoricalReportHelper();
    }

    public static HistoricalReportHelper getInstance() {
        return HistoricalReportHelperHolder.instance;
    }

    public List<HistoricalReport> getHistoryFromRequestWrappers(
            List<RadExamRequestWrapper> requestWrappers) {
        try {
            List<Long> requestsIds = new ArrayList<Long>();

            for (RadExamRequestWrapper requestWrapper : requestWrappers) {
                requestsIds.add(requestWrapper.getId());
            }

            List<RadiologyExamRequest> radiologyExamRequests = DaoManager.load(
                    RadiologyExamRequest.class, new Criterion[]{
                            Restrictions.in("id", requestsIds)
                    });

            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequests)) {
                return getHistoryFromRequests(radiologyExamRequests);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    public List<HistoricalReport> getHistoryFromRequests(
            List<RadiologyExamRequest> requests) {
        List<HistoricalReport> historicalReports = null;

        try {
            historicalReports = new ArrayList<HistoricalReport>();

            for (RadiologyExamRequest radiologyExamRequest : requests) {
                if (radiologyExamRequest != null
                        && !ValidationHelper.isNullOrEmpty(radiologyExamRequest
                        .getRadiologyExamRequestItems())) {
                    HistoricalReport historicalReport = getSingleHistoricalReportFromRequest(
                            radiologyExamRequest, new HistoricalReport(), radiologyExamRequest
                                    .getRadiologyExamRequestItems());

                    historicalReports.add(historicalReport);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return historicalReports;
    }

    public HistoricalReport getSingleHistoricalReportFromRequest(RadiologyExamRequest radiologyExamRequest,
                                                                 HistoricalReport historicalReport,
                                                                 List<RadiologyExamRequestItem> items) {
        if (historicalReport != null) {

            RadiologyExamRequestItem simpleItem = items.get(0);

            historicalReport.setAccessNumber(this.getAccessNumber(simpleItem));
            historicalReport.setExamType(radiologyExamRequest.getExamType());
            if (simpleItem.getSignFileEntity() != null) {
                historicalReport.setFileEntity(simpleItem.getSignFileEntity());
            } else {
                historicalReport.setFileEntity(simpleItem.getFileEntity());
            }
            historicalReport.setHospitalCode(radiologyExamRequest.getHospital().getCode());
            historicalReport.setPatientBirthDate(radiologyExamRequest.getPatient().getBirthDate());
            historicalReport.setPatientFiscalCode(radiologyExamRequest.getPatient().getFiscalCode());
            historicalReport.setPatientName(radiologyExamRequest.getPatient().getName());
            historicalReport.setPatientSurname(radiologyExamRequest.getPatient().getSurname());
            historicalReport.setRadiologyExamRequest(radiologyExamRequest);
            historicalReport.setReferringDoctor(radiologyExamRequest.getReferringDoctor());
            historicalReport.setReportDate(simpleItem.getReportDate());
            historicalReport.setReportResult(simpleItem.getReportResult());
            historicalReport.setReserveDate(simpleItem.getReserveDate());
            historicalReport.setLatestActionDate(simpleItem.getRadiologyExamRequest().getLatestActionDate());
            historicalReport.setTsrm(simpleItem.getTrsm());
            historicalReport.setWaitingListRegistrationState(radiologyExamRequest.getWaitingListRegistrationState());
            historicalReport.setSectorId(radiologyExamRequest.getSector() == null
                    ? null : radiologyExamRequest.getSector().getId());
            historicalReport.setOperator(simpleItem.getOperator());
            historicalReport.setAcceptDate(simpleItem.getAcceptDate());
            historicalReport.setPerformDate(simpleItem.getPerformDate());
            historicalReport.setRequestDate(simpleItem.getRequestDate());
            historicalReport.setRequestingDoctor(simpleItem.getRequestingDoctor());

            List<RadiologyExam> radiologyExams = new ArrayList<RadiologyExam>();

            for (RadiologyExamRequestItem requestItem : items) {
                if (requestItem != null
                        && requestItem.getRadiologyExam() != null) {
                    radiologyExams.add(requestItem.getRadiologyExam());
                }
            }

            if (!ValidationHelper.isNullOrEmpty(radiologyExams)) {
                historicalReport.setRadiologyExams(radiologyExams);
            }
        }

        return historicalReport;
    }

    public String getAccessNumber(RadiologyExamRequestItem item) {
        if (!ValidationHelper.isNullOrEmpty(item)
                && !ValidationHelper.isNullOrEmpty(item.getAccessNumber())) {
            return item.getAccessNumber();
        }

        return "";
    }

    public void saveHistoricalReportForRadiologyExamRequest(RadiologyExamRequest request,
                                                            List<RadiologyExamRequestItem> items,
                                                            Session session,
                                                            boolean openTransaction) {
        try {
            if (request == null) {
                LogHelper.log(log, "request is null or empty, it's not ok");
                return;
            }
            if (session == null) {
                session = DaoManager.getSession();
            }
            List<Object> historicalReportIds = ConnectionManager.getFields(
                    HistoricalReport.class, new Criterion[]{
                            Restrictions.eq("radiologyExamRequest.id", request.getId())
                    }, new CriteriaAlias[]{}, session, "id", "idInOldDb");
            HistoricalReport historicalReport = new HistoricalReport();

            if (!ValidationHelper.isNullOrEmpty(historicalReportIds)) {
                historicalReport.setId((Long) ((Object[]) historicalReportIds.get(0))[0]);
                historicalReport.setIdInOldDb((Long) ((Object[]) historicalReportIds.get(0))[1]);
                historicalReport.setCustomId(false);
                if (historicalReportIds.size() > 1) {
                    LogHelper.log(log,
                            "For radiology exam request with id = <<"
                                    + request.getId()
                                    + ">> exist a several historical reports!\nThis is a list of historical report ids: ");

                    for (Object hr : historicalReportIds) {
                        LogHelper.log(log, ((Object[]) hr)[0] + ", ");
                    }
                    LogHelper.log(log, "Please check this situation in DB");
                }
            }

            if (items == null) {
                items = request.getRadiologyExamRequestItems();
            }

            if (!ValidationHelper.isNullOrEmpty(items)) {
                historicalReport = getSingleHistoricalReportFromRequest(request, historicalReport, items);
                if (historicalReport.getId() != null) {
                    ConnectionManager.removeById(HistoricalReport.class, historicalReport.getId(), session);
                }
                ConnectionManager.save(historicalReport, openTransaction, session);
            }

        } catch (IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
    }
}
