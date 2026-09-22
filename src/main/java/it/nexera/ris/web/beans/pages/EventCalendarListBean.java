package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Module;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendar;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("eventCalendarListBean")
@ViewScoped
public class EventCalendarListBean extends
        EntityLazyListPageBean<EventCalendar> implements Serializable {

    private static final long serialVersionUID = -8390062884419328267L;

    private enum UserAbility {
        canCreate, canDelete, canEdit, canView;
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.loadList(EventCalendar.class, new Criterion[]{
                Restrictions.isNull("newCalendar")
        }, new Order[]{
                Order.asc("name")
        });
    }

    private boolean checkUserPermossion(UserAbility canAction)
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        Module module = DaoManager.get(Module.class, new Criterion[]{
                Restrictions.eq("code", "PGC")
        });
        switch (canAction) {
            case canCreate:
                return getCurrentUser().getPermissions().get(module.getCode())
                        .isCanCreate();
            case canDelete:
                return getCurrentUser().getPermissions().get(module.getCode())
                        .isCanDelete();
            case canEdit:
                return getCurrentUser().getPermissions().get(module.getCode())
                        .isCanEdit();
            default:
                return false;
        }
    }

    public boolean getCanCreate() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return checkUserPermossion(UserAbility.canCreate);
    }

    public boolean getCanDelete() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return checkUserPermossion(UserAbility.canDelete);
    }

    public boolean getCanEdit() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return checkUserPermossion(UserAbility.canEdit);
    }

    public boolean getCanView() {
        return false;
    }

}
