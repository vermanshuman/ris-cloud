package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.enums.UserStatuses;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.base.BaseSynchronizableService;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UserDisableService extends BaseSynchronizableService {

    private static final int POLL_INTERVAL_MIN = 60 * 24;

    public UserDisableService() {
        super(SessionNames.UserDisableService);
    }

    @Override
    protected void routineFuncInternal() {
        Transaction tr = null;
        try {
            tr = getSession().beginTransaction();
            List<User> users = ConnectionManager.load(User.class,
                    new Criterion[]{
                            Restrictions.isNull("lastLoginDate")
                    }, getSession());
            if (!ValidationHelper.isNullOrEmpty(users)) {
                for (User user : users) {
                    if (user.getLastLoginDate() == null) {
                        user.setLastLoginDate(new Date());
                        ConnectionManager.save(user, getSession());
                    }
                }
            }
            Calendar expiredDay = Calendar.getInstance();
            expiredDay.setTime(new Date());
            expiredDay.set(Calendar.HOUR_OF_DAY, 0);
            expiredDay.set(Calendar.MINUTE, 0);
            expiredDay.set(Calendar.SECOND, 0);
            expiredDay.add(Calendar.DAY_OF_MONTH, -1 * new Integer(getWaitingDays()));
            List<User> expiredUsers = ConnectionManager.load(User.class, new Criterion[]{
                    Restrictions.le("lastLoginDate", expiredDay.getTime()),
                    Restrictions.eq("status", UserStatuses.ACTIVE)}, getSession());
            if (!ValidationHelper.isNullOrEmpty(expiredUsers)) {
                for (User user : expiredUsers) {
                    user.setStatus(UserStatuses.INACTIVE);
                    ConnectionManager.save(user, getSession());
                }
            }
        } catch (Exception e) {
            if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                tr.rollback();
            }
            LogHelper.log(log, e);
        } finally {
            if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                try {
                    tr.commit();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    @Override
    protected int getPollTimeKey() {
        String value = ApplicationSettingsHolder
                .getInstance()
                .getByKey(ApplicationSettingsKeys.USER_DISABLE_POLLING_TIME)
                .getValue();

        if (value != null && !value.isEmpty()) {
            return Integer.parseInt(value) * 60;
        } else {
            value = ApplicationSettingsHolder
                    .getInstance()
                    .getDefaultValueByKey(
                            ApplicationSettingsKeys.USER_DISABLE_POLLING_TIME);
            if (!ValidationHelper.isNullOrEmpty(value)) {
                return Integer.parseInt(value) * 60;
            } else {
                return POLL_INTERVAL_MIN;
            }
        }
    }

    private String getWaitingDays() {
        String value = ApplicationSettingsHolder
                .getInstance()
                .getByKey(ApplicationSettingsKeys.USER_DISABLE_PERIOD)
                .getValue();

        if (value != null && !value.isEmpty()) {
            return value;
        } else {
            value = ApplicationSettingsHolder
                    .getInstance()
                    .getDefaultValueByKey(
                            ApplicationSettingsKeys.USER_DISABLE_PERIOD);
            if (!ValidationHelper.isNullOrEmpty(value)) {
                return value;
            } else {
                return "30";
            }
        }
    }
}
