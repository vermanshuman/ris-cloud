package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ListHelper;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "dic_hospital")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "HOSPITAL_SEQ", allocationSize = 1)
public class Hospital extends Dictionary {
    private static final long serialVersionUID = -5324643936176312041L;

    @OneToMany(mappedBy = "hospital")
    private List<Sector> sectors;

    @OneToMany(mappedBy = "hospital")
    private List<RadiologyExamRequest> radiologyRequests;

    @OneToMany(mappedBy = "hospital")
    private List<Pacs> pacs;

    @Column(name = "weasis_url")
    private String weasisUrl;

    @Column(name = "provenance_hospital")
    private String provenanceHospital;

    @Column(name = "codice_unidoc")
    private String hospitalProducerCode;

    public List<Sector> getSectors() {
        return sectors;
    }

    public void setSectors(List<Sector> sectors) {
        this.sectors = sectors;
    }

    @Override
    public String toString() {
        return this.getDescription();
    }

    @Transient
    public String getSectorsExport() {
        return ListHelper.toString(this.sectors);
    }

    public List<RadiologyExamRequest> getRadiologyRequests() {
        return radiologyRequests;
    }

    public void setRadiologyRequests(
            List<RadiologyExamRequest> radiologyRequests) {
        this.radiologyRequests = radiologyRequests;
    }

    public List<Pacs> getPacs() {
        return pacs;
    }

    public void setPacs(List<Pacs> pacs) {
        this.pacs = pacs;
    }

    public String getWeasisUrl() {
        return weasisUrl;
    }

    public void setWeasisUrl(String weasisUrl) {
        this.weasisUrl = weasisUrl;
    }

    public String getProvenanceHospital() {
        return provenanceHospital;
    }

    public void setProvenanceHospital(String provenanceHospital) {
        this.provenanceHospital = provenanceHospital;
    }

    public String getHospitalProducerCode() {
        return hospitalProducerCode;
    }

    public void setHospitalProducerCode(String hospitalProducerCode) {
        this.hospitalProducerCode = hospitalProducerCode;
    }
}
