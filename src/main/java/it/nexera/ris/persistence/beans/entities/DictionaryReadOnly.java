package it.nexera.ris.persistence.beans.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class DictionaryReadOnly extends IndexedEntity {
    private static final long serialVersionUID = -8367152490629077991L;

    @Column(insertable = false, updatable = false)
    private String code;

    @Column(insertable = false, updatable = false)
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    @Override
    public String toString() {
        return description;
    }
}
