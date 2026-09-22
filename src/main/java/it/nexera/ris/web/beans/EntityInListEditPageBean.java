package it.nexera.ris.web.beans;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Entity;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

public abstract class EntityInListEditPageBean<T extends Entity> extends
        EntityListPageBean<T> {
    private static final long serialVersionUID = 4287780150062097662L;

    private Transaction tr;

    private T editedEntity;

    private Class<T> type;

    @SuppressWarnings("unchecked")
    protected Class<T> getType() {
        if (type == null) {
            ParameterizedType superclass = (ParameterizedType) getClass()
                    .getGenericSuperclass();
            type = (Class<T>) ((ParameterizedType) superclass)
                    .getActualTypeArguments()[0];
        }
        return type;
    }

    protected void formConstruct() {
        super.onConstruct();
        if (this.getEntity() == null) {
            this.initNewEntity();
        }
    }

    private void initNewEntity() {
        try {
            this.setEntity(getType().newInstance());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.BaseEntityPageBean#getCanCreate()
     */
    @Override
    public boolean getCanCreate() {
        return false;
    }

    public boolean getCanCreateInBean() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        return super.getCanCreate();
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityListPageBean#editEntity()
     */
    public void editEntity() {
        this.cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
            try {
                this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                DaoManager.getSession().evict(this.getEntity());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void pageSave() {
        this.cleanValidation();
        this.setValidationFailed(false);

        try {
            this.validate();
        } catch (PersistenceBeanException e) {
            LogHelper.log(log, e);
        }

        if (this.getValidationFailed()) {
            return;
        }

        try {
            this.tr = PersistenceSessionManager.getBean().getSession()
                    .beginTransaction();

            if (!this.getEntity().isNew()) {
                this.editedEntity = DaoManager.get(getType(), this.getEntity()
                        .getId());
                this.setEditedValues();
                this.setEntity(this.editedEntity);
            }
            this.save();
        } catch (Exception e) {
            if (this.tr != null) {
                this.tr.rollback();
            }
            LogHelper.log(log, e);
        } finally {
            if (this.tr != null && this.tr.getStatus() == TransactionStatus.ACTIVE) {
                this.tr.commit();
            }
        }

        this.resetFields();

        try {
            this.onLoad();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    protected abstract void setEditedValues();

    public void resetFields() {
        this.initNewEntity();
        this.cleanValidation();
    }

    protected abstract void validate() throws PersistenceBeanException;

    public abstract void save() throws HibernateException,
            PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException;

    @SuppressWarnings("unchecked")
    public T getEntity() {
        if (this.getViewState().get("entityList") == null) {
            this.initNewEntity();
        }
        return (T) this.getViewState().get("entityList");
    }

    public void setEntity(T entity) {
        this.getViewState().put("entityList", entity);
    }

    public String getPanelTitle() {
        if (this.getEntity() == null
                || ValidationHelper.isNullOrEmpty(this.getEntity().getId())) {
            return ResourcesHelper.getString("newItem");
        }

        return ResourcesHelper.getString("edit");
    }

    public T getEditedEntity() {
        return editedEntity;
    }

    public void setEditedEntity(T editedEntity) {
        this.editedEntity = editedEntity;
    }
}
