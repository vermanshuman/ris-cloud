package it.nexera.ris.web.beans;

import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.web.common.EntityLazyListModel;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.primefaces.model.LazyDataModel;

public abstract class EntityLazyListPageBean<T extends IEntity> extends
        EntityListPageBean<T> {

    private static final long serialVersionUID = 2204751150301176665L;

    private LazyDataModel<T> lazyModel;

    protected Boolean isShort = false;

    protected void loadList(Class<T> clazz, Order[] orders) {
        this.setLazyModel(new EntityLazyListModel<T>(clazz, orders));
    }

    protected void loadList(Class<T> clazz, Criterion[] restrictions,
                            Order[] orders) {
        this.setLazyModel(new EntityLazyListModel<T>(clazz, restrictions,
                orders));
    }

    protected void loadList(Class<T> clazz, Criterion[] restrictions,
                            Order[] orders, CriteriaAlias[] criteriaAliases) {
        this.setLazyModel(new EntityLazyListModel<T>(clazz, restrictions,
                orders, criteriaAliases));
    }

    public LazyDataModel<T> getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(LazyDataModel<T> lazyModel) {
        isShort = false;
        this.lazyModel = lazyModel;
    }
}
