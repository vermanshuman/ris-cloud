package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectManyMenuWrapper<T extends Dictionary> implements Serializable {

    private static final long serialVersionUID = -6286861539818222218L;

    private List<T> source;

    private List<T> sourceFiltered;

    private List<T> sourceSelected;

    private List<T> target;

    private List<T> targetFiltered;

    private List<T> targetSelected;

    private Boolean showSourceDroppable;

    private Boolean showTargetDroppable;

    private Class<T> bean;

    public SelectManyMenuWrapper(Class<T> bean) {
        this.bean = bean;
        defaultInititalize();
        setShowSourceDroppable(true);
        setShowTargetDroppable(true);
    }

    public SelectManyMenuWrapper(Class<T> bean, List<T> target) throws PersistenceBeanException, IllegalAccessException {
        this.bean = bean;
        if (!ValidationHelper.isNullOrEmpty(target)) {
            this.target = target;
            List<Long> ids = BaseHelper.getIds(target);
            setSource(DaoManager.load(bean, new Criterion[]{
                    Restrictions.not(Restrictions.in("id", ids))
            }));
        } else {
            defaultInititalize();
        }
    }

    public SelectManyMenuWrapper(Class<T> bean, List<T> target, List<T> source) {
        this.bean = bean;
        if (target == null) {
            target = new ArrayList<>();
        }
        if (source == null) {
            this.source = new ArrayList<>();
        }
        this.target = target;
        this.source = source;
    }

    private void defaultInititalize() {
        try {
            setSource(DaoManager.load(bean));
            setTarget(new ArrayList<T>());
        } catch (Exception e) {
        }
    }

    public void onDrop() {
        String strId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        String intoTargetStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("intoTarget");
        boolean intoTarget = Boolean.valueOf(intoTargetStr);
        List<T> from = intoTarget ? getSource() : getTarget();
        List<T> to = intoTarget ? getTarget() : getSource();
        if (!ValidationHelper.isNullOrEmpty(strId)) {
            migrate(from, to, Long.parseLong(strId));
        }
    }

    public void changeShowSourceDroppable() {
        this.setShowSourceDroppable(Boolean.TRUE);
        this.setShowTargetDroppable(Boolean.FALSE);
    }

    public void changeShowTargetDroppable() {
        this.setShowTargetDroppable(Boolean.TRUE);
        this.setShowSourceDroppable(Boolean.FALSE);
    }

    private void migrate(List<T> from, List<T> to, Long migrationElementId) {
        T instance = null;
        for (int i = 0; i < from.size(); i++) {
            if (migrationElementId.equals(from.get(i).getId())) {
                instance = from.remove(i);
                break;
            }
        }
        if (instance != null) {
            to.add(instance);
        }
    }

    private void move(List<T> from, List<T> to, List<T> data, boolean all) {
        if (all) {
            to.addAll(from);
            Collections.sort(to);
        } else if (data != null) {
            for (T instance : data) {
                migrate(from, to, instance.getId());
            }
            Collections.sort(to);
        }
    }

    public void add() {
        move(getSource(), getTarget(), getSourceSelected(), false);
    }

    public void remove() {
        move(getTarget(), getSource(), getTargetSelected(), false);
    }

    public void addAll() {
        move(getSource(), getTarget(), null, true);
        this.getSource().clear();
        if (getSourceFiltered() != null) {
            getSourceFiltered().clear();
        }
    }

    public void removeAll() {
        move(getTarget(), getSource(), null, true);
        getTarget().clear();
        if (getTargetFiltered() != null) {
            getTargetFiltered().clear();
        }
    }

    public List<T> getSource() {
        return source;
    }

    public void setSource(List<T> source) {
        this.source = source;
    }

    public List<T> getSourceFiltered() {
        return sourceFiltered;
    }

    public void setSourceFiltered(List<T> sourceFiltered) {
        this.sourceFiltered = sourceFiltered;
    }

    public List<T> getSourceSelected() {
        return sourceSelected;
    }

    public void setSourceSelected(List<T> sourceSelected) {
        this.sourceSelected = sourceSelected;
    }

    public List<T> getTarget() {
        return target;
    }

    public void setTarget(List<T> target) {
        this.target = target;
    }

    public List<T> getTargetFiltered() {
        return targetFiltered;
    }

    public void setTargetFiltered(List<T> targetFiltered) {
        this.targetFiltered = targetFiltered;
    }

    public List<T> getTargetSelected() {
        return targetSelected;
    }

    public void setTargetSelected(List<T> targetSelected) {
        this.targetSelected = targetSelected;
    }

    public Class<T> getBean() {
        return bean;
    }

    public Boolean getShowSourceDroppable() {
        return showSourceDroppable;
    }

    public void setShowSourceDroppable(Boolean showSourceDroppable) {
        this.showSourceDroppable = showSourceDroppable;
    }

    public Boolean getShowTargetDroppable() {
        return showTargetDroppable;
    }

    public void setShowTargetDroppable(Boolean showTargetDroppable) {
        this.showTargetDroppable = showTargetDroppable;
    }
}
