package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import org.hibernate.HibernateException;

import java.math.BigDecimal;
import java.util.List;

public class MostUsedItemsHelper {

    @SuppressWarnings("unchecked")
    public static List<BigDecimal> getMostUsedRadiologyExamsIds()
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return DaoManager
                .getSession()
                .createSQLQuery(
                        "SELECT ID FROM most_used_exams ORDER BY COUNT DESC, CODE ASC")
                .list();
    }
}
