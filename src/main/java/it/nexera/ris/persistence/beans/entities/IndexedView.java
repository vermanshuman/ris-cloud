package it.nexera.ris.persistence.beans.entities;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class IndexedView extends Entity {
    private static final long serialVersionUID = 136815106630765597L;

    @Id
    @Column(name = "id")
    private Long id;

    @Transient
    private boolean customId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        this.customId = true;
    }

    public boolean isCustomId() {
        return customId;
    }

    public void setCustomId(boolean customId) {
        this.customId = customId;
    }

}
