package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum ThreeStateCheckbox {

    ACCEPTED("1"), DENIED("2"), NOT_SELECTED("0");

    private String value;

    private ThreeStateCheckbox(String value) {
        this.setValue(value);
    }

    public String getValue() {
        return value;
    }

    public static ThreeStateCheckbox fromBoolean(Boolean state) {
        if (state == null) {
            return NOT_SELECTED;
        }
        return state ? ACCEPTED : DENIED; // true -> "1", false -> "2"
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static ThreeStateCheckbox getByValue(String value) {
        for (ThreeStateCheckbox item : ThreeStateCheckbox.values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

}
