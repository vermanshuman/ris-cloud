package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "dic_nationality")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "NATIONALITY_SEQ", allocationSize = 1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
public class Nationality extends Dictionary {

    private static final long serialVersionUID = 5928827257619755028L;

    public static final String ITALY = "ITALIA";

    @ManyToOne
    @JoinColumn(name = "asl_region_id", foreignKey = @ForeignKey(name = "FK_NATIONALITY_ASL_REGION"))
    private AslRegion aslRegion;

    @Column
    private String cfis;

    @Transient
    @XmlElement(name = "asl_region")
    private String asl_region_code;

    public AslRegion getAslRegion() {
        return aslRegion;
    }

    public void setAslRegion(AslRegion aslRegion) {
        this.aslRegion = aslRegion;
    }

    public String getCfis() {
        return cfis;
    }

    public void setCfis(String cfis) {
        this.cfis = cfis;
    }

    public String getAsl_region_code() {
        return asl_region_code;
    }

    public void setAsl_region_code(String asl_region_code) {
        this.asl_region_code = asl_region_code;
    }

}
