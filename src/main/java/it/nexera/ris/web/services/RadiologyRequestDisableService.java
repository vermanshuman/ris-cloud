package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.base.BaseDBService;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public class RadiologyRequestDisableService extends BaseDBService implements
        Serializable {

    private static final long serialVersionUID = -8763703352786846974L;

    public RadiologyRequestDisableService() {
        super(SessionNames.RadiologyRequestDisableService);
    }

    @Override
    protected void routineFuncInternal() {
        if (ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.REQUEST_EXPIRATION_PERIOD)
                .getValue() != null) {
            int expirationDaysCount = Integer
                    .parseInt(ApplicationSettingsHolder
                            .getInstance()
                            .getByKey(
                                    ApplicationSettingsKeys.REQUEST_EXPIRATION_PERIOD)
                            .getValue());

            if (expirationDaysCount != 0) {
                List<RadiologyExamRequest> loadedList = ConnectionManager
                        .load(RadiologyExamRequest.class,
                                new Criterion[]{
                                        Restrictions
                                                .eq("waitingListRegistrationState",
                                                WaitingListRegistrationStates.REQUIRED),
                                        Restrictions.or(Restrictions.eq(
                                                "disabledRequest",
                                                Boolean.FALSE), Restrictions
                                                .isNull("disabledRequest"))
                                }, getSession());

                if (!ValidationHelper.isNullOrEmpty(loadedList)) {
                    Calendar difference = Calendar.getInstance();
                    difference.add(Calendar.DAY_OF_MONTH, -expirationDaysCount);

                    for (RadiologyExamRequest radiologyExamRequest : loadedList) {
                        if (radiologyExamRequest.getCreateDate().before(
                                difference.getTime())) {
                            radiologyExamRequest
                                    .setDisabledRequest(Boolean.TRUE);
                            ConnectionManager.save(radiologyExamRequest, true,
                                    getSession());
                        }
                    }
                }
            }
        }
    }

    @Override
    protected int getPollTimeKey() {
        return 2880; // once per day
    }
}
