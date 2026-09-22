package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestSessionWrapper;
import org.hibernate.HibernateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaitingListSessionHelper {
    private static final String ID_IN_SESSION = "backupedWaitingList";

    public static void backupToSession(RadiologyExamRequest wlru) {
        List<Long> list = new ArrayList<Long>();
        for (RadiologyExamRequestItem radiologyExamRequestItem : wlru
                .getRadiologyExamRequestItems()) {
            if (!Boolean.FALSE.equals(radiologyExamRequestItem.getSelected())) {
                list.add(radiologyExamRequestItem.getId());
            }
        }
        getSession().put(ID_IN_SESSION,
                new RadExamRequestSessionWrapper(wlru.getId(), list));
    }

    public static void cleanBackupFromSession() {
        getSession().remove(ID_IN_SESSION);
    }

    public static RadiologyExamRequest restoreFromSession()
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        RadiologyExamRequest radExamRequest = null;
        RadExamRequestSessionWrapper wr = (RadExamRequestSessionWrapper) getSession()
                .get(ID_IN_SESSION);

        if (wr != null) {
            radExamRequest = DaoManager.get(RadiologyExamRequest.class,
                    wr.getRadExamRequestId());

            if (radExamRequest != null) {
                radExamRequest
                        .setSelectedRadItemsList(new ArrayList<RadiologyExamRequestItem>());

                for (RadiologyExamRequestItem item : radExamRequest
                        .getRadiologyExamRequestItems()) {
                    if (!wr.getSelectedRadExamRequestItemList().contains(
                            item.getId())) {
                        item.setSelected(Boolean.FALSE);
                    } else {
                        radExamRequest.getSelectedRadItemsList().add(item);
                    }
                }
            }
        }

        cleanBackupFromSession();

        return radExamRequest;
    }

    private static Map<String, Object> getSession() {
        return SessionManager.getInstance().getSessionBean().getSession();
    }
}
