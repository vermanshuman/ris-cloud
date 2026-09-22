package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Pacs;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("pascListBean")
@ViewScoped
public class PacsListBean extends EntityLazyListPageBean<Pacs> implements
        Serializable {
    private static final long serialVersionUID = 8131310041862325127L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.loadList(Pacs.class, new Order[]{
                Order.desc("createDate")
        });
    }
}
