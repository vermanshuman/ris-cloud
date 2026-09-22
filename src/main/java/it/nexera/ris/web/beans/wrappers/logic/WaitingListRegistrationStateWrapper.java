package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.helpers.ValidationHelper;

import java.io.Serializable;

public class WaitingListRegistrationStateWrapper implements Serializable {
    private static final long serialVersionUID = -5358097470545147512L;

    private String value;

    private WaitingListRegistrationStates realState;

    private Boolean selected;

    public WaitingListRegistrationStateWrapper() {

    }

    public WaitingListRegistrationStateWrapper(
            WaitingListRegistrationStates state) {
        this.setValue(state.toString());
        this.setRealState(state);
    }

    public WaitingListRegistrationStateWrapper(
            WaitingListRegistrationStates state, Boolean selected) {
        this.setValue(state.toString());
        this.setRealState(state);
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

    public WaitingListRegistrationStates getRealState() {
        return realState;
    }

    public void setRealState(WaitingListRegistrationStates realState) {
        this.realState = realState;
    }

    public boolean customEquals(WaitingListRegistrationStateWrapper state) {
        if (!ValidationHelper.isNullOrEmptyMultiple(state, this.getValue())
                && !ValidationHelper.isNullOrEmpty(state.getValue())) {
            return this.getValue().equals(state.getValue());
        } else {
            return false;
        }
    }
}
