package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "dic_osirix")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "OSIRIX_SEQ", allocationSize = 1)
public class Osirix extends IndexedEntity {

    private static final long serialVersionUID = -4375182010578571138L;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "port")
    private Long port;

    @Column(name = "type")
    private Long type;

    @Column(name = "description")
    private String description;

    @Column(name = "aet")
    private String aet;

    @Column(name = "dicom_port")
    private Long dicomPort;

    @Column(name = "ip_routing")
    private String ipRouting;

    @ManyToOne
    @JoinColumn(name = "sector_id", foreignKey = @ForeignKey(name = "FK_OSIRIX_SECTOR"))
    private Sector sector;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipaddress) {
        this.ipAddress = ipaddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAet() {
        return aet;
    }

    public void setAet(String aet) {
        this.aet = aet;
    }

    public String getIpRouting() {
        return ipRouting;
    }

    public void setIpRouting(String ipRouting) {
        this.ipRouting = ipRouting;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Long getDicomPort() {
        return dicomPort;
    }

    public void setDicomPort(Long dicomPort) {
        this.dicomPort = dicomPort;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }
}
