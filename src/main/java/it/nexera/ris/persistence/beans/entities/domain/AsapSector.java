package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "asap_dic_sector")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "ASAP_DIC_SECTOR_SEQ", allocationSize = 1)
public class AsapSector extends Dictionary {

    private static final long serialVersionUID = 5936584706491957086L;

    @Column(name = "asap_id")
    private Long asapId;

    public AsapSector() {
    }

    public AsapSector(Long asapId, String code, String description) {
        setId(asapId);
        this.asapId = asapId;
        setCode(code);
        setDescription(description);
    }

    public Long getAsapId() {
        return asapId;
    }

    public void setAsapId(Long asapId) {
        this.asapId = asapId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AsapSector that = (AsapSector) o;

        return asapId.equals(that.asapId);
    }

    @Override
    public int hashCode() {
        return asapId.hashCode();
    }
}
