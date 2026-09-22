package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.*;

@Entity
@Table(name = "dic_dvd_producer")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "DVD_PROD_SEQ", allocationSize = 1)
public class DVDProducer extends Dictionary {

    private static final long serialVersionUID = -4375182070573571138L;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "port")
    private Long port;

    @Column(name = "aet")
    private String aet;

    @Column(name = "dicom_port")
    private Long dicomPort;

    @Column(name = "ip_routing")
    private String ipRouting;

    @ManyToOne
    @JoinColumn(name = "sector_id", foreignKey = @ForeignKey(name = "FK_DVD_PRODUCER_SECTOR"))
    private Sector sector;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String getAet() {
        return aet;
    }

    public void setAet(String aet) {
        this.aet = aet;
    }

    public Long getDicomPort() {
        return dicomPort;
    }

    public void setDicomPort(Long dicomPort) {
        this.dicomPort = dicomPort;
    }

    public String getIpRouting() {
        return ipRouting;
    }

    public void setIpRouting(String ipRouting) {
        this.ipRouting = ipRouting;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }
}
