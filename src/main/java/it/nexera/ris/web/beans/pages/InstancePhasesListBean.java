package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.InstancePhases;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.List;

@Named("instancePhasesListBean")
@ViewScoped
public class InstancePhasesListBean
        extends EntityLazyListPageBean<InstancePhases> implements Serializable {

    private static final long serialVersionUID = -5890624641085382361L;

    private List<SelectItem> places;

    private String selectedPlace;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.loadList(InstancePhases.class, new Order[]{});
        setPlaces(ComboboxHelper.fillList(DocumentGenerationPlaces.class));
    }

    public void onFilterChange() {
        DocumentGenerationPlaces place = null;

        for (DocumentGenerationPlaces pl : DocumentGenerationPlaces.values()) {
            if (pl.name().equalsIgnoreCase(getSelectedPlace())) {
                place = pl;

                break;
            }
        }

        if (place != null) {
            this.loadList(InstancePhases.class, new Criterion[]{
                    Restrictions.eq("place", place)
            }, new Order[]{});
        } else {
            this.loadList(InstancePhases.class, new Order[]{});
        }
    }

    public List<SelectItem> getPlaces() {
        return places;
    }

    public void setPlaces(List<SelectItem> places) {
        this.places = places;
    }

    public String getSelectedPlace() {
        return selectedPlace;
    }

    public void setSelectedPlace(String selectedPlace) {
        this.selectedPlace = selectedPlace;
    }

}
