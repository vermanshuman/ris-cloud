package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Urgency;

import java.io.Serializable;

public class UrgencyWrapper implements Serializable {
    private static final long serialVersionUID = 3329754245968716314L;

    private Long id;

    private String value;

    private Boolean selected;

    public UrgencyWrapper() {

    }

    public UrgencyWrapper(Urgency urgency) {
        this.setId(urgency.getId());
        this.setValue(urgency.getDescription());
    }

    public UrgencyWrapper(Urgency urgency, Boolean selected) {
        this.setId(urgency.getId());
        this.setValue(urgency.getDescription());
        this.setSelected(selected);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getSelected() {
        return selected == null ? Boolean.FALSE : selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean customEquals(UrgencyWrapper uw) {
        if (!ValidationHelper.isNullOrEmptyMultiple(uw, this.getValue())
                && !ValidationHelper.isNullOrEmpty(uw.getValue())) {
            return this.getValue().equals(uw.getValue());
        } else {
            return false;
        }
    }
}
