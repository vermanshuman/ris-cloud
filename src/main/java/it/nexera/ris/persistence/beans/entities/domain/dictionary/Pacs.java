package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.enums.PacsType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "dic_pacs")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PACS_SEQ", allocationSize = 1)
public class Pacs extends IndexedEntity {

    private static final long serialVersionUID = -6003641019216945368L;

    @Column(name = "name")
    private String name;

    @Column(name = "ip_host_pacs")
    private String iPHostPacs;

    @Column(name = "port_host_pacs")
    private Long portHostPacs;

    @Column(name = "aet_pacs")
    private String aetPacs;

    @Column(name = "dicom_port")
    private Long dicomPort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", foreignKey = @ForeignKey(name = "FK_PACS_HOSPITAL"))
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(name = "pacs_type")
    private PacsType pacsType;

    @ManyToOne
    @JoinColumn(name = "sector_id", foreignKey = @ForeignKey(name = "FK_PACS_SECTOR"))
    private Sector sector;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDicomPort() {
        return dicomPort;
    }

    public void setDicomPort(Long dicomPort) {
        this.dicomPort = dicomPort;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public PacsType getPacsType() {
        return pacsType;
    }

    public void setPacsType(PacsType pacsType) {
        this.pacsType = pacsType;
    }

    public String getiPHostPacs() {
        return iPHostPacs;
    }

    public void setiPHostPacs(String iPHostPacs) {
        this.iPHostPacs = iPHostPacs;
    }

    public Long getPortHostPacs() {
        return portHostPacs;
    }

    public void setPortHostPacs(Long portHostPacs) {
        this.portHostPacs = portHostPacs;
    }

    public String getAetPacs() {
        return aetPacs;
    }

    public void setAetPacs(String aetPacs) {
        this.aetPacs = aetPacs;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }
}
