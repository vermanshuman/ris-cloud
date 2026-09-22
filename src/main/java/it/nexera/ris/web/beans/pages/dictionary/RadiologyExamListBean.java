package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.EnableDisableEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.List;

@Named("radiologyExamListBean")
@ViewScoped
public class RadiologyExamListBean extends
        EntityLazyListPageBean<RadiologyExam> implements Serializable {
    private List<SelectItem> examTypes;

    private List<SelectItem> states;

    private static final long serialVersionUID = -2642325745379881448L;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setExamTypes(ComboboxHelper.fillList(ExamType.class, false, true));
        this.setStates(ComboboxHelper.fillList(EnableDisableEnum.class, false,
                true));
        this.loadList(RadiologyExam.class, new Order[]{
                Order.desc("createDate")
        });
    }

    public List<SelectItem> getExamTypes() {
        return examTypes;
    }

    public void setExamTypes(List<SelectItem> examTypes) {
        this.examTypes = examTypes;
    }

    public List<SelectItem> getStates() {
        return states;
    }

    public void setStates(List<SelectItem> states) {
        this.states = states;
    }
}
