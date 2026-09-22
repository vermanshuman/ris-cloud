package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.beans.entities.domain.PatientActionHistory;
import it.nexera.ris.persistence.beans.entities.domain.readonly.PatientShort;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.common.EntityLazyListModel;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.primefaces.model.LazyDataModel;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("patientListBean")
@ViewScoped
public class PatientListBean extends EntityLazyListPageBean<PatientShort>
        implements Serializable {

    private static final long serialVersionUID = -7950331950944469020L;

    private LazyDataModel<PatientActionHistory> history;

    private List<PatientActionHistory> historyFiltered;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException {
        if (this.getSession().containsKey("patientSaved")) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO, "",
                    ResourcesHelper.getString("patientSavedCorrectly"));
            this.getSession().remove("patientSaved");
        }

        this.loadList(PatientShort.class, new Order[]{
                Order.asc("name")
        });

        history = new EntityLazyListModel<PatientActionHistory>(
                PatientActionHistory.class, new Order[]{
                Order.desc("createDate")
        });
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            RedirectHelper.goTo(PageTypes.getEditPageByClass(getType()
                    .getSimpleName().substring(0,
                            getType().getSimpleName().indexOf("Short"))), null);
        }
    }

    public LazyDataModel<PatientActionHistory> getHistory() {
        return history;
    }

    public void setHistory(LazyDataModel<PatientActionHistory> history) {
        this.history = history;
    }

    public List<PatientActionHistory> getHistoryFiltered() {
        return historyFiltered;
    }

    public void setHistoryFiltered(List<PatientActionHistory> historyFiltered) {
        this.historyFiltered = historyFiltered;
    }

}
