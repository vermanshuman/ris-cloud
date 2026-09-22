package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "dic_city")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "CITY_SEQ", allocationSize = 1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
public class City extends Dictionary {
    private static final long serialVersionUID = -9206231689506545191L;

    @ManyToOne
    @JoinColumn(name = "province_id", foreignKey = @ForeignKey(name = "FK_CITY_PROVINCE"))
    private Province province;

    @ManyToOne
    @JoinColumn(name = "asl_id", foreignKey = @ForeignKey(name = "FK_CITY_ASL"))
    private Asl asl;

    @Column(name = "patient_instance")
    private Boolean patientInstance;

    @ManyToOne
    @JoinColumn(name = "asl_region_id", foreignKey = @ForeignKey(name = "FK_CITY_ASL_REGION"))
    private AslRegion aslRegion;

    @Column
    private String cap;

    @Column
    private String cfis;

    @Column
    private Boolean obsolete;

    @Transient
    @XmlElement(name = "asl_region_code")
    private String asl_region_code;

    @Transient
    @XmlElement(name = "asl_code")
    private String asl_code;

    @Transient
    @XmlElement(name = "province_code")
    private String province_code;

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public Boolean getPatientInstance() {
        return patientInstance;
    }

    public void setPatientInstance(Boolean patientInstance) {
        this.patientInstance = patientInstance;
    }

    public Asl getAsl() {
        return asl;
    }

    public void setAsl(Asl asl) {
        this.asl = asl;
    }

    public AslRegion getAslRegion() {
        return aslRegion;
    }

    public void setAslRegion(AslRegion aslRegion) {
        this.aslRegion = aslRegion;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public String getCfis() {
        return cfis;
    }

    public void setCfis(String cfis) {
        this.cfis = cfis;
    }

    public Boolean getObsolete() {
        return obsolete;
    }

    public void setObsolete(Boolean obsolete) {
        this.obsolete = obsolete;
    }

    public String getAsl_region_code() {
        return asl_region_code;
    }

    public void setAsl_region_code(String asl_region_code) {
        this.asl_region_code = asl_region_code;
    }

    public String getAsl_code() {
        return asl_code;
    }

    public void setAsl_code(String asl_code) {
        this.asl_code = asl_code;
    }

    public String getProvince_code() {
        return province_code;
    }

    public void setProvince_code(String province_code) {
        this.province_code = province_code;
    }

}
