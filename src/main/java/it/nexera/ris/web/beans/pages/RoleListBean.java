package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("roleListBean")
@ViewScoped
public class RoleListBean extends EntityLazyListPageBean<Role> implements
        Serializable {

    private static final long serialVersionUID = 2934758631283242272L;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        this.loadList(Role.class, new Order[]{
                Order.asc("name")
        });
    }

}
