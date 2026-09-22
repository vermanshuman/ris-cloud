package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DirectReservation;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("directReservationListBean")
@ViewScoped
public class DirectReservationListBean extends
        EntityLazyListPageBean<DirectReservation> implements Serializable {

    private static final long serialVersionUID = -8383824153122862698L;

    private List<SelectItem> sectors;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setSectors(ComboboxHelper.fillList(Sector.class, false, true));
        this.loadList(DirectReservation.class, new Order[]{
                Order.desc("createDate")
        });
    }

    public List<SelectItem> getSectors() {
        return sectors;
    }

    public void setSectors(List<SelectItem> sectors) {
        this.sectors = sectors;
    }
}
