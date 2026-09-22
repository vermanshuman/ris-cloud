package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItemShort;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestShort;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.base.BaseDBService;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.Calendar;
import java.util.List;

public class CleanShortRequestService extends BaseDBService {
    public CleanShortRequestService() {
        super(SessionNames.CleanShortRequestService);
    }

    @Override
    protected void routineFuncInternal() {
        Calendar lessCalendar = Calendar.getInstance();
        lessCalendar = DateTimeHelper.getDayStart(lessCalendar);
        lessCalendar.add(Calendar.DAY_OF_MONTH, -30);
        List<RadiologyExamRequestShort> requests = ConnectionManager.load(RadiologyExamRequestShort.class, new Criterion[]{
                Restrictions.lt("createDate", lessCalendar.getTime()),
                Restrictions.or(Restrictions.lt("latestActionPerformDate", lessCalendar.getTime()),
                        Restrictions.isNull("latestActionPerformDate"))
        }, getSession());

        for (RadiologyExamRequestShort request : requests) {
            Transaction tr = null;
            try {
                tr = getSession().beginTransaction();
                for (RadiologyExamRequestItemShort item : request.getRadiologyExamRequestItems()) {
                    ConnectionManager.remove(item, getSession());
                }
                ConnectionManager.remove(request, getSession());
            } catch (Exception e) {
                LogHelper.log(log, e);
                if (tr != null) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
            }
        }
    }

    @Override
    protected void updateSleepTime() {
        sleepTimeMs = getPollTimeKey() * 60 * 1000; //minutes in ms
    }

    @Override
    protected int getPollTimeKey() {
        if (!ValidationHelper
                .isNullOrEmpty(ApplicationSettingsHolder.getInstance().getByKey(
                        ApplicationSettingsKeys.CLEAN_SHORT_REQUEST_INTERVAL))
                && !ValidationHelper
                .isNullOrEmpty(ApplicationSettingsHolder.getInstance()
                        .getByKey(
                                ApplicationSettingsKeys.CLEAN_SHORT_REQUEST_INTERVAL)
                        .getValue())) {
            return Integer.parseInt(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.CLEAN_SHORT_REQUEST_INTERVAL)
                    .getValue()) * 24 * 60; // day to minutes
        }

        return 24 * 60; // one day
    }
}
