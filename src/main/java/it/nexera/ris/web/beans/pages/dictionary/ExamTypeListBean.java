package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.UserPreferenceType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.HttpSessionHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.UserPreference;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("examTypeListBean")
@ViewScoped
public class ExamTypeListBean extends EntityLazyListPageBean<ExamType>
        implements Serializable {

    private static final long serialVersionUID = -2091167306391364768L;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.loadList(ExamType.class, new Order[]{
                Order.desc("createDate")
        });
    }

    @Override
    protected void deleteEntityInternal(Long id) throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        removeFromListInSession("WorkListExamTypes", id);
        removeFromListInHttpSession("WorkListExamTypes", id);
        removeFromListInSession("AgendaExamTypes", id);

        List<UserPreference> prefs = new ArrayList<UserPreference>();

        try {
            prefs = DaoManager.load(
                    UserPreference.class,
                    new Criterion[]{
                            Restrictions.eq("type",
                                    UserPreferenceType.WORKLIST_EXAM_TYPE),
                            Restrictions.eq("examType.id", id)
                    });
        } catch (Exception e) {
            log.error("Cant load preferences for examType", e);
        }
        for (UserPreference pref : prefs) {
            DaoManager.remove(pref);
        }
        super.deleteEntityInternal(id);
    }

    private void removeFromListInSession(String nameInSession, Long id) {
        if (SessionHelper.get(nameInSession) != null && id != null) {
            @SuppressWarnings("unchecked")
            List<Long> list = (List<Long>) SessionHelper
                    .get(nameInSession);

            for (Long examTypeWrapper : list) {
                if (id.equals(examTypeWrapper)) {
                    list.remove(examTypeWrapper);
                    break;
                }
            }

            SessionHelper.put(list, nameInSession);
        }
    }

    private void removeFromListInHttpSession(String nameInSession, Long id) {
        if (HttpSessionHelper.get(nameInSession) != null && id != null) {
            @SuppressWarnings("unchecked")
            List<Long> list = (List<Long>) HttpSessionHelper
                    .get(nameInSession);

            for (Long examTypeWrapper : list) {
                if (id.equals(examTypeWrapper)) {
                    list.remove(examTypeWrapper);
                    break;
                }
            }

            HttpSessionHelper.put(list, nameInSession);
        }
    }
}
