package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.Hl7RequestSendingStatus;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HL7MessageForwardingService extends BaseSynchronizableService {

    private static final int POLL_INTERVAL_MIN = 5;

    public HL7MessageForwardingService() {
        super(SessionNames.HL7MessageForwardingService);
    }

    @Override
    protected void routineFuncInternal() {
        log.info("Entering HL7MessageForwardingService");
        int maxLimitDays = 1;
        int maxNumberForwards = 1;
        if (ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_DAYS_LIMIT)
                .getValue() != null) {
            maxLimitDays = Integer
                    .parseInt(ApplicationSettingsHolder
                            .getInstance()
                            .getByKey(
                                    ApplicationSettingsKeys.HL7_DAYS_LIMIT)
                            .getValue());
        }

        if (ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_MAX_SEND_COUNT_LIMIT)
                .getValue() != null) {
            maxNumberForwards = Integer
                    .parseInt(ApplicationSettingsHolder
                            .getInstance()
                            .getByKey(
                                    ApplicationSettingsKeys.HL7_MAX_SEND_COUNT_LIMIT)
                            .getValue());
        }
        Calendar difference = Calendar.getInstance();
        difference.set(Calendar.HOUR_OF_DAY, 0);
        difference.set(Calendar.MINUTE, 0);
        difference.set(Calendar.SECOND, 0);
        difference.set(Calendar.MILLISECOND, 0);
        difference.add(Calendar.DAY_OF_MONTH, -maxLimitDays);
        Date dateFrom = difference.getTime();

        try {
            List<RadiologyExamRequest> radiologyExamRequests = ConnectionManager
                    .load(RadiologyExamRequest.class,
                            new CriteriaAlias[]{new CriteriaAlias("radiologyExamRequestItems", "radiologyExamRequestItems", JoinType.INNER_JOIN)},
                            new Criterion[]{
                                    Restrictions.eq("waitingListRegistrationState",
                                            WaitingListRegistrationStates.SIGNED),
                                    Restrictions.ge("radiologyExamRequestItems.reportDate", dateFrom),
                                    Restrictions.or(Restrictions.eq(
                                                    "repoSendingStatus", Hl7RequestSendingStatus.NOT_SENT),
                                            Restrictions.isNull("repoSendingStatus")),
                                    Restrictions.or(Restrictions.lt(
                                                    "countRepoHL7", maxNumberForwards),
                                            Restrictions.isNull("countRepoHL7")),
                                    Restrictions.eq("forwarded", Boolean.TRUE),
                            }, getSession());

            log.info("HL7MessageForwardingService(radiologyExamRequests) : " + radiologyExamRequests.size());
            for(RadiologyExamRequest radiologyExamRequest : radiologyExamRequests) {
                List<RadiologyExamRequestItem> items = radiologyExamRequest.getRadiologyExamRequestItems();

                List<Long> radExItemIds = new ArrayList<>();
                for (RadiologyExamRequestItem item : items) {
                    radExItemIds.add(item.getId());
                }
                if (!ValidationHelper.isNullOrEmpty(radExItemIds)) {
                    HL7RepoHelper hl7RepoHelper =  new HL7RepoHelper();
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
                                if (!ValidationHelper.isNullOrEmpty(requestWrapper.getForwarded())
                                        && requestWrapper.getForwarded()) {
                                    hl7RepoHelper.sendRepoMsgsForDocument(requestWrapper, fileEntity, getSession());
                                }
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
        sleepTimeMs = getPollTimeKey() *  1000; //minutes in ms
    }

    @Override
    protected int getPollTimeKey() {
        String value = ApplicationSettingsHolder
                .getInstance()
                .getByKey(ApplicationSettingsKeys.HL7_REQUEST_SEND_POLLING_TIME)
                .getValue();


        if (value != null && !value.isEmpty()) {
            return Integer.parseInt(value);
        } else {
            value = ApplicationSettingsHolder
                    .getInstance()
                    .getDefaultValueByKey(
                            ApplicationSettingsKeys.HL7_REQUEST_SEND_POLLING_TIME);
            if (!ValidationHelper.isNullOrEmpty(value)) {
                return Integer.parseInt(value);
            } else {
                return POLL_INTERVAL_MIN;
            }
        }
    }

}
