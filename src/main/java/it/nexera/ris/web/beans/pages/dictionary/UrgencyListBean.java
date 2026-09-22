package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.HttpSessionHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Urgency;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("urgencyListBean")
@ViewScoped
public class UrgencyListBean extends EntityLazyListPageBean<Urgency> implements
        Serializable {
    private static final long serialVersionUID = -3539610943212052773L;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.loadList(Urgency.class, new Order[]{
                Order.desc("createDate")
        });
    }

    @Override
    protected void deleteEntityInternal(Long id) throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        removeFromListInSession("WorkListUrgencies", id);
        removeFromListInHttpSession("WorkListUrgencies", id);
        removeFromListInSession("AgendaUrgencies", id);
        super.deleteEntityInternal(id);
    }

    private void removeFromListInSession(String nameInSession, Long id) {
        if (SessionHelper.get(nameInSession) != null && id != null) {
            @SuppressWarnings("unchecked")
            List<Long> list = (List<Long>) SessionHelper
                    .get(nameInSession);

            for (Long urgencyWrapper : list) {
                if (id.equals(urgencyWrapper)) {
                    list.remove(urgencyWrapper);
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

            for (Long urgencyWrapper : list) {
                if (id.equals(urgencyWrapper)) {
                    list.remove(urgencyWrapper);
                    break;
                }
            }

            HttpSessionHelper.put(list, nameInSession);
        }
    }
}
