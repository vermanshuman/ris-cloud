package it.nexera.ris.web.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Entity;
import org.hibernate.HibernateException;

import java.lang.reflect.ParameterizedType;

public abstract class EntityViewPageBean<T extends Entity> extends
        BaseEntityPageBean {
    private T entity;

    private Long entityId;

    private Class<T> type;

    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        if (type == null) {
            ParameterizedType superclass = (ParameterizedType) getClass()
                    .getGenericSuperclass();
            type = (Class<T>) ((ParameterizedType) superclass)
                    .getActualTypeArguments()[0];
        }
        return type;
    }

    private void loadEntity() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.setEntityId((this.getEditingEntityId() == null || this
                .getEditingEntityId().isEmpty()) ? null : Long.parseLong(this
                .getEditingEntityId()));
        if (this.getEntityId() != null) {
            this.entity = DaoManager.get(getType(), this.getEntityId());
        } else {
            this.entity = getType().newInstance();
        }
    }

    protected final void onConstruct() {
        this.clearSession();

        if (!this.isPostback()) {
            try {
                this.preLoad();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        try {
            this.loadEntity();

            this.onLoad();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    protected void preLoad() throws PersistenceBeanException {
    }

    public abstract void onLoad() throws NumberFormatException,
            HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException;

    public void goBack() {
        RedirectHelper.goTo(PageTypes.getListPageByClass(getType()
                .getSimpleName()));
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public T getEntity() {
        return this.entity;
    }

    public Long getEntityId() {
        if (this.getEntity() != null && this.getEntity().getId() != null) {
            return this.getEntity().getId();
        } else {
            if (this.entityId != null) {
                return entityId;
            }

            return null;
        }
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
}
