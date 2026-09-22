package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "access_number_for_year")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "ACC_NUM_FOR_YEAR_SEQ", allocationSize = 1)
public class AccessNumberForYear extends IndexedEntity {
    private static final long serialVersionUID = 656381080515463605L;

    @Column(name = "dic_access_number_id")
    private Long accessNumberId;

    @Column(name = "dic_access_number_year", unique = true)
    private Long accessNumberYear;

    public Long getAccessNumberId() {
        return accessNumberId;
    }

    public void setAccessNumberId(Long accessNumberId) {
        this.accessNumberId = accessNumberId;
    }

    public Long getAccessNumberYear() {
        return accessNumberYear;
    }

    public void setAccessNumberYear(Long accessNumberYear) {
        this.accessNumberYear = accessNumberYear;
    }

}
