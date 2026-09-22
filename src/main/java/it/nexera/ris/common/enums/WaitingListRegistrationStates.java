package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum WaitingListRegistrationStates implements DbEnum {

    //waitinglist
    REQUIRED("#FF77D8"),
    DELETED("#FF3737"),

    //worklist and waitinglist
    ANNULLED("#9B9B9B"),
    RESERVED("#C5C5C5"),

    //worklist
    ACCEPTED("#17CDE6"),
    PARTIALLY_ACCEPTED("#11E68D"),
    PERFORMED("#0DBA13"),
    REPORTED("#727EFF"),
    DRAFT("#db9aff"),
    SUSPENDED("#1B80FC"),
    SIGNED("#FF0A84"),
    IN_READING("#FFBC41");

    private String stateColor;

    private WaitingListRegistrationStates(String stateColor) {
        this.stateColor = stateColor;
    }

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    @Override
    public String getRealName() {
        return this.name();
    }

    @Override
    public Object getRealObject() {
        return this;
    }

    public String getStateColor() {
        return stateColor;
    }

    public void setStateColor(String stateColor) {
        this.stateColor = stateColor;
    }

}
