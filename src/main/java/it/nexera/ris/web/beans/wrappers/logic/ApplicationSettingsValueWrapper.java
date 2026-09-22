package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;

import java.io.Serializable;

public class ApplicationSettingsValueWrapper implements Serializable {
    private static final long serialVersionUID = 4707788359610491881L;

    private ApplicationSettingsKeys key;

    private String value;

    public ApplicationSettingsValueWrapper(ApplicationSettingsKeys k, String val) {
        this.setKey(k);
        this.setValue(val);
    }

    public ApplicationSettingsKeys getKey() {
        return key;
    }

    public void setKey(ApplicationSettingsKeys key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
