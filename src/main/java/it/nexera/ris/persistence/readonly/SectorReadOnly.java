package it.nexera.ris.persistence.readonly;

import it.nexera.ris.common.annotations.AliasColumn;

import java.io.Serializable;

public class SectorReadOnly implements Serializable {

    private static final long serialVersionUID = 5619411580822916691L;

    @AliasColumn(alias = "id")
    private Long id;

    @AliasColumn(alias = "description")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
