package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Priority;
import it.nexera.ris.web.beans.EntityInListEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;

@Named("priorityListBean")
@ViewScoped
public class PriorityListBean extends EntityInListEditPageBean<Priority>
        implements Serializable {
    private static final long serialVersionUID = 8092831564519881994L;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setList(DaoManager.load(Priority.class, Order.asc("code")));
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityInListEditPageBean#validate()
     */
    @Override
    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldExeption("form:code");
        } else if (!ValidationHelper.isUnique(Priority.class, "code", this
                .getEntity().getCode(), this.getEntity().getId())) {
            addFieldExeption("form:code", "codeAlreadyInUse");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDescription())) {
            addRequiredFieldExeption("form:description");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDays())) {
            addRequiredFieldExeption("form:days");
        } else if (this.getEntity().getDays() < 0) {
            addFieldExeption("form:days", "negativeField");
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityInListEditPageBean#setEditedValues()
     */
    @Override
    protected void setEditedValues() {
        this.getEditedEntity().setCode(this.getEntity().getCode());
        this.getEditedEntity()
                .setDescription(this.getEntity().getDescription());
        this.getEditedEntity().setDays(this.getEntity().getDays());
        this.getEditedEntity().setColor(this.getEntity().getColor());
        this.getEditedEntity().setHasColor(this.getEntity().getHasColor());
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityInListEditPageBean#save()
     */
    @Override
    public void save() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(this.getEntity());
    }
}
