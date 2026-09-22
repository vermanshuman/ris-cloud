package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "application_settings")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "APP_SETTINGS_SEQ", allocationSize = 1)
public class ApplicationSettingsValue extends IndexedEntity {
    private static final long serialVersionUID = -5721332310846891726L;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_key")
    private ApplicationSettingsKeys key;

    @Column
    private String value;

    public ApplicationSettingsKeys getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(ApplicationSettingsKeys key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
