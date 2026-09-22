package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DVDProducer;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;

@Named("dvdProducerListBean")
@ViewScoped
public class DVDProducerListBean extends EntityLazyListPageBean<DVDProducer> {

    private static final long serialVersionUID = -1929020686227595301L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        this.loadList(DVDProducer.class, new Order[]{Order.desc("createDate")});
    }
}
