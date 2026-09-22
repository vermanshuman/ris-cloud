package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TemplateDocumentModel;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;

@Named("templateDocumentModelListBean")
@ViewScoped
public class TemplateDocumentModelListBean extends
        EntityLazyInListEditPageBean<TemplateDocumentModel> implements
        Serializable {
    private static final long serialVersionUID = -4421028341519658946L;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException {
        this.loadList(TemplateDocumentModel.class, new Order[]{
                Order.asc("name")
        });
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityInListEditPageBean#validate()
     */
    protected void validate() {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldExeption("form:name");
        } else if (!ValidationHelper.isUnique(TemplateDocumentModel.class,
                "name", getEntity().getName(), this.getEntity().getId())) {
            addFieldExeption("form:name", "nameAlreadyInUse");
        }
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

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityInListEditPageBean#setEditedValues()
     */
    @Override
    protected void setEditedValues() {
        this.getEditedEntity().setName(this.getEntity().getName());
    }
}
