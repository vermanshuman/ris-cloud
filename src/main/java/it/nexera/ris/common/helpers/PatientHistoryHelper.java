package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.PatientHistoryActionType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Patient;
import it.nexera.ris.persistence.beans.entities.domain.PatientActionHistory;
import org.hibernate.HibernateException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class PatientHistoryHelper extends BaseHelper {
    /**
     * Creates record in patient history with supplied patient, action type
     * and waiting list name.
     *
     * @throws PersistenceBeanException
     * @throws HibernateException
     */
    public static void createRecord(Patient patient,
                                    PatientHistoryActionType action, String waitingListName)
            throws HibernateException, PersistenceBeanException {
        PatientActionHistory record = new PatientActionHistory();
        record.setPatient(patient);
        record.setActionType(action);
        record.setIp(((HttpServletRequest) (FacesContext.getCurrentInstance()
                .getExternalContext().getRequest())).getRemoteAddr());
        record.setActionDate(new Date());
        //        record.setUsername(SessionManager.getInstance().getSessionBean()
        //                .getCurrentUser().getLogin());
        record.setUsername(UserHolder.getInstance().getCurrentUser().getLogin());
        record.setWaitingListName(waitingListName);

        DaoManager.save(record);
    }

    /**
     * Creates record in patient history with supplied patient and action type.
     *
     * @throws PersistenceBeanException
     * @throws HibernateException
     */
    public static void createRecord(Patient patient,
                                    PatientHistoryActionType action) throws HibernateException,
            PersistenceBeanException {
        createRecord(patient, action, null);
    }
}
