package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Osirix;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("osirixListBean")
@ViewScoped
public class OsirixListBean extends EntityLazyListPageBean<Osirix> implements
        Serializable {
    private static final long serialVersionUID = -8312761238850424124L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.loadList(Osirix.class, new Order[]{
                Order.desc("createDate")
        });
    }
}
