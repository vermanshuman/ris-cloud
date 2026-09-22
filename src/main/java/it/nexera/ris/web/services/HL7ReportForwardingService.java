package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.ConservationStates;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.HL7RepoHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestItemWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import it.nexera.ris.web.services.base.BaseSynchronizableService;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HL7ReportForwardingService extends BaseSynchronizableService {

    private static final int POLL_INTERVAL_MIN = 5;

    private static final String DEFAULT_TIME = "12:00";

    public HL7ReportForwardingService() {
        super(SessionNames.HL7ReportForwardingService);
    }

    @Override
    protected void routineFuncInternal() {
        log.info("Entering HL7ReportForwardingService");
        try {
            Date dateFrom = DateTimeHelper.getNow();
            if (ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.HL7_REPORT_FORWARDING_START_DATE)
                    .getValue() != null) {
                dateFrom = DateTimeHelper.fromString(ApplicationSettingsHolder
                        .getInstance()
                        .getByKey(
                                ApplicationSettingsKeys.HL7_REPORT_FORWARDING_START_DATE)
                        .getValue(), DateTimeHelper.getDatePattern());
            }
            int hours = 0;
            if (ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.HL7_REPORT_FORWARDING_HOURS)
                    .getValue() != null) {
                hours = Integer
                        .parseInt(ApplicationSettingsHolder
                                .getInstance()
                                .getByKey(
                                        ApplicationSettingsKeys.HL7_REPORT_FORWARDING_HOURS)
                                .getValue());
            }
            Date dateTo = DateTimeHelper.addHours(DateTimeHelper.getNow(), hours * -1);

            log.info("HL7ReportForwardingService dateFrom - dateTo" + dateFrom + "-" + dateTo);
            List<RadiologyExamRequest> radiologyExamRequests = ConnectionManager
                    .load(RadiologyExamRequest.class,
                            new CriteriaAlias[]{new CriteriaAlias("radiologyExamRequestItems", "radiologyExamRequestItems", JoinType.INNER_JOIN)},
                            new Criterion[]{
                                    Restrictions.eq("waitingListRegistrationState",
                                            WaitingListRegistrationStates.SIGNED),
                                    Restrictions.ge("radiologyExamRequestItems.reportDate", dateFrom),
                                    Restrictions.le("radiologyExamRequestItems.reportDate", dateTo),
                                    Restrictions.or(
                                            Restrictions.isNull("conservationState"),
                                            Restrictions.eq(
                                                    "conservationState", ConservationStates.NOT_SENT),
                                            Restrictions.eq(
                                                    "conservationState", ConservationStates.RESEND)
                                    )
                            }, getSession());

            log.info("HL7ReportForwardingService(radiologyExamRequests) : " + radiologyExamRequests.size());
            HL7RepoHelper hl7RepoHelper =  new HL7RepoHelper();
            for (RadiologyExamRequest radiologyExamRequest : radiologyExamRequests) {
                List<RadiologyExamRequestItem> items = radiologyExamRequest.getRadiologyExamRequestItems();
                List<Long> radExItemIds = new ArrayList<>();
                for (RadiologyExamRequestItem item : items) {
                    radExItemIds.add(item.getId());
                }
                if (!ValidationHelper.isNullOrEmpty(radExItemIds)) {
                    FileEntity fileEntity = items.get(0).getFileEntity();
                    Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair =
                            hl7RepoHelper.getPairRequestsItems(radExItemIds, getSession());
                    if (!ValidationHelper.isNullOrEmpty(pair.getFirst())
                            && !ValidationHelper.isNullOrEmpty(pair.getSecond())) {
                        List<Long> itemIds = new ArrayList<>();
                        for (RadExamRequestItemWrapper radExamRequestItemWrapper : pair.getSecond()) {
                            itemIds.add(radExamRequestItemWrapper.getId());
                        }
                        List<RadExamRequestWrapper> allRequestsWrappers = pair.getFirst();
                        if (!ValidationHelper.isNullOrEmpty(allRequestsWrappers)) {
                            for (RadExamRequestWrapper requestWrapper : allRequestsWrappers) {
                                hl7RepoHelper.sendReportConservation(requestWrapper, fileEntity, getSession());
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
        }
    }

    @Override
    protected void updateSleepTime() {
        sleepTimeMs = getPollTimeKey() * 60 * 1000; //minutes in ms
    }

    @Override
    protected int getPollTimeKey() {
        /*String value = ApplicationSettingsHolder
                .getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_REPORT_FORWARDING_POLLING_TIME)
                .getValue();

        if (value != null && !value.isEmpty()) {
            return Integer.parseInt(value);
        } else {
            value = ApplicationSettingsHolder
                    .getInstance()
                    .getDefaultValueByKey(
                            ApplicationSettingsKeys.HL7_REPORT_FORWARDING_POLLING_TIME);
            if (!ValidationHelper.isNullOrEmpty(value)) {
                return Integer.parseInt(value);
            } else {
                return POLL_INTERVAL_MIN;
            }
        }*/
        try {
            Date now = DateTimeHelper.getNow();
            Date startDate = DateTimeHelper.getTodayDateFromTime(getStartTime());
            log.info("start : " + startDate + ", now : " + now);
            if(now.compareTo(startDate) == 0 || now.after(startDate)){
                startDate = DateTimeHelper.addHours(startDate, 24);
            }
            Long diffInMinutes = ((startDate.getTime() - now.getTime()))/60000;
            log.info("Polling time(Forwarding) " + diffInMinutes);
            if(diffInMinutes.equals(0l))
                diffInMinutes = 1440l;
            return diffInMinutes.intValue();
        } catch (Exception e) {
            log.error("Error in getting HL7ReportForwardingService start time");
            LogHelper.log(log,e);
        }
        return POLL_INTERVAL_MIN;
    }

    protected String getStartTime() {
        String value = ApplicationSettingsHolder
                .getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_REPORT_FORWARDING_POLLING_TIME)
                .getValue();

        if (value != null && !value.isEmpty()) {
            return value;
        } else {
            value = ApplicationSettingsHolder
                    .getInstance()
                    .getDefaultValueByKey(
                            ApplicationSettingsKeys.HL7_REPORT_FORWARDING_POLLING_TIME);
            if (!ValidationHelper.isNullOrEmpty(value)) {
                return value;
            } else {
                return DEFAULT_TIME;
            }
        }
    }
}
