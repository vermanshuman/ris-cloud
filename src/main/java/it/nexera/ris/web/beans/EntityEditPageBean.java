package it.nexera.ris.web.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Entity;
import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

import javax.faces.application.FacesMessage;
import jakarta.mail.MessagingException;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

public abstract class EntityEditPageBean<T extends Entity> extends
        BaseEntityPageBean {
    private T entity;

    private Long entityId;

    private boolean canEdit;

    protected Transaction tr;

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

    protected void loadEntity() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.entityId)
                && !this.entityId.equals("null")) {
            this.entity = DaoManager.get(getType(), this.entityId);
            if (this.entity == null) {
                this.entity = getType().newInstance();
            }
        } else {
            this.entity = getType().newInstance();
        }
    }

    protected void onConstruct() {
        if (!this.isPostback()) {
            this.clearSession();
        }

        if (!this.isLoggedIn()) {
            return;
        }
        if (!this.isPostback()) {
            try {
                this.preLoad();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        this.setEntityId((this.getEditingEntityId() == null || this
                .getEditingEntityId().isEmpty()) ? null : Long.parseLong(this
                .getEditingEntityId()));

        if (!this.isPostback()) {
            try {
                this.pageLoadStatic();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        try {
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

    protected void pageLoadStatic() throws PersistenceBeanException {
    }

    public void pageSave() {
        if (this.getSaveFlag() == 0) {
            try {
                this.cleanValidation();
                this.setValidationFailed(false);
                this.onValidate();
                if (this.getValidationFailed()) {
                    return;
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
                return;
            }

            boolean runAfterSave = true;
            try {
                this.tr = DaoManager.getSession().beginTransaction();

                this.setSaveFlag(1);

                this.onSave();
            } catch (Exception e) {
                if (this.tr != null) {
                    this.tr.rollback();
                }
                LogHelper.log(log, e);
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "",
                        ResourcesHelper.getValidation("objectEditedException"));
            } finally {
                if (this.tr != null && this.tr.getStatus() == TransactionStatus.ACTIVE) {
                    try {
                        this.tr.commit();
                    } catch (StaleObjectStateException e) {
                        MessageHelper
                                .addGlobalMessage(
                                        FacesMessage.SEVERITY_ERROR,
                                        "",
                                        ResourcesHelper
                                                .getValidation("exceptionOccuredWhileSaving"));
                        LogHelper.log(log, e);
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                }
                this.setSaveFlag(0);
            }
            if (runAfterSave) {
                this.afterSave();
            }
        }
    }

    public abstract void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException;

    public abstract void onSave() throws HibernateException,
            PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException, MessagingException;

    public void goBack() {
        try {
            RedirectHelper.goTo(PageTypes.getListPageByClass(getType()
                    .getSimpleName()));
        } finally {
            this.getViewState().clear();
        }
    }

    public void afterSave() {
        this.goBack();
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public T getEntity() {
        return this.entity;
    }

    public Long getEntityId() {
        if (this.entityId == null && this.getEntity() != null
                && this.getEntity().getId() != null) {
            this.entityId = this.getEntity().getId();
        }

        return entityId;
    }

    public void setSaveFlag(int saveFlag) {
        this.getViewState().put("saveFlag", saveFlag);
    }

    public int getSaveFlag() {
        return this.getViewState().get("saveFlag") == null ? 0 : (Integer) this
                .getViewState().get("saveFlag");
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
        try {
            loadEntity();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean getCanEdit() {
        return canEdit;
    }

    protected void refreshPage() {
        try {
            this.onLoad();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }
}
