package it.nexera.ris.web.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IEntity;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import javax.faces.application.FacesMessage;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.List;

public abstract class EntityListPageBean<T extends IEntity> extends
        BaseEntityPageBean implements Serializable {
    private static final long serialVersionUID = 3241298683943990428L;

    private List<T> list;

    protected T[] selectedList;

    protected List<T> filteredList;

    private Class<T> type;

    private T selectedItem;

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

    protected void onConstruct() {
        if (!this.isPostback()) {
            clearSession();
        }

        if (!this.isPostback()) {
            try {
                this.preLoad();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            this.setEntityDeleteId(null);
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
            InstantiationException, IllegalAccessException, IOException;

    public void addEntity() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            RedirectHelper.goTo(
                    PageTypes.getEditPageByClass(getType().getSimpleName()),
                    null);
        }
    }

    public String getEditURL() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (this.getCanEdit()) {
            return String.format("%s?%s=",
                    PageTypes.getEditPageByClass(getType().getSimpleName())
                            .getPagesContext(), RedirectHelper.ID_PARAMETER);

        }
        return null;
    }

    public String getCreateURL() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (this.getCanCreate()) {
            return String.format("%s?%s=",
                    PageTypes.getEditPageByClass(getType().getSimpleName())
                            .getPagesContext(), RedirectHelper.ID_PARAMETER);
        }
        return null;
    }

    public void editEntity() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (this.getCanEdit()) {
            RedirectHelper.goTo(
                    PageTypes.getEditPageByClass(getType().getSimpleName()),
                    this.getEntityEditId());
        }
    }

    public void deleteEntity() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, NumberFormatException, IOException {
        if (this.getEntityDeleteId() != null) {
            Transaction tr = null;
            try {
                tr = PersistenceSessionManager.getBean().getSession()
                        .beginTransaction();

                this.deleteEntityInternal(this.getEntityDeleteId());
            } catch (Exception e) {
                if (tr != null) {
                    tr.rollback();
                }
                if (e instanceof ConstraintViolationException) {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper.getValidation("deleteFail"), "");
                } else {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            e.getMessage(), e.getCause().getMessage());
                }

                LogHelper.log(log, e);
            } finally {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    try {
                        tr.commit();
                    } catch (Exception e) {
                        if (e instanceof ConstraintViolationException) {
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_ERROR,
                                            ResourcesHelper
                                                    .getValidation("deleteFail"),
                                            "");
                        } else {
                            MessageHelper.addGlobalMessage(
                                    FacesMessage.SEVERITY_ERROR,
                                    e.getMessage(), e.getCause().getMessage());
                        }

                        tr.rollback();

                        LogHelper.log(log, e);
                    }
                    if (tr != null && tr.getStatus() != TransactionStatus.ROLLED_BACK) {
                        afterEntityRemoved();
                    }
                }
            }
            this.onLoad();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityListPageBean#afterEntityRemoved()
     */
    public void afterEntityRemoved() {
        if (this.getEntityDeleteId() != null && this.getFilteredList() != null) {
            Iterator<T> iterator = this.getFilteredList().iterator();
            while (iterator.hasNext()) {
                if (this.getEntityDeleteId().equals(iterator.next().getId())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    // This method can be overrided and reused in child class
    // to extend functionality
    protected void deleteEntityInternal(Long id) throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (id == null || id == 0l) {
            return;
        }

        DaoManager.remove(getType(), id);
    }

    @SuppressWarnings("unchecked")
    public void deleteSelectedEntities() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        try {
            if (selectedList != null && selectedList.length > 0) {
                Transaction tr = null;
                try {
                    tr = PersistenceSessionManager.getBean().getSession()
                            .beginTransaction();
                    for (IEntity item : selectedList) {
                        if (!((T) item).getDeletable()) {
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_ERROR,
                                            ResourcesHelper
                                                    .getValidation("deteleFail"),
                                            ResourcesHelper
                                                    .getValidation("deleteFailMessage"));
                            continue;
                        }

                        this.deleteEntityInternal(item.getId());
                    }
                } catch (Exception e) {
                    if (tr != null) {
                        tr.rollback();
                    }
                    LogHelper.log(log, e);
                } finally {
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        tr.commit();
                    }
                }
                this.onLoad();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void viewEntity() {
        RedirectHelper.goTo(
                PageTypes.getViewPageByClass(getType().getSimpleName()),
                this.getEntityEditId());
    }

    public void setEntityDeleteId(Long entityDeleteId) {
        this.getViewState().put("entityDeleteId", entityDeleteId);
    }

    public Long getEntityDeleteId() {
        return (Long) this.getViewState().get("entityDeleteId");
    }

    public void setEntityEditId(Long entityEditId) {
        this.getViewState().put("entityEditId", entityEditId);
    }

    public Long getEntityEditId() {
        return (Long) this.getViewState().get("entityEditId");
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public List<T> getList() {
        return this.list;
    }

    public T[] getSelectedList() {
        return (T[]) selectedList;
    }

    public void setSelectedList(T[] selectedList) {
        this.selectedList = selectedList;
    }

    public List<T> getFilteredList() {
        return filteredList;
    }

    public void setFilteredList(List<T> filteredList) {
        this.filteredList = filteredList;
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(T selectedItem) {
        this.selectedItem = selectedItem;
    }
}
