package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.AsapSector;
import it.nexera.ris.persistence.beans.entities.domain.relation.ReservationAsapSector;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dic_direct_reservation")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "DIRECT_RESERVATION_SEQ", allocationSize = 1)
public class DirectReservation extends IndexedEntity {

    private static final long serialVersionUID = -7777953477109783986L;

    @ManyToOne
    @JoinColumn(name = "sector_id", foreignKey = @ForeignKey(name = "FK_DIRECT_RESERVATION_SECTOR"))
    private Sector sector;

    @ManyToMany
    @JoinTable(name = "direct_reservation_rad_ex", joinColumns = {
            @JoinColumn(name = "direct_reservation_id", table = "dic_direct_reservation",
                    foreignKey = @ForeignKey(name = "FK_DIRECT_RESERVATION"))
    }, inverseJoinColumns = {
            @JoinColumn(name = "rad_ex_id", table = "dic_radiology_exam",
                    foreignKey = @ForeignKey(name = "FC_DIRECT_RESERVATION_RAD_EX"))
    })
    private List<RadiologyExam> radiologyExams;

    @OneToMany(mappedBy = "directReservation")
    private List<ReservationAsapSector> reservationAsapSectors;

    @Column(name = "activity_line")
    private String activityLine;

    @Transient
    private List<AsapSector> asapSectors;

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public List<RadiologyExam> getRadiologyExams() {
        return radiologyExams;
    }

    public void setRadiologyExams(List<RadiologyExam> radiologyExams) {
        this.radiologyExams = radiologyExams;
    }

    public List<ReservationAsapSector> getReservationAsapSectors() {
        return reservationAsapSectors;
    }

    public void setReservationAsapSectors(List<ReservationAsapSector> reservationAsapSectors) {
        this.reservationAsapSectors = reservationAsapSectors;
    }

    public List<AsapSector> getAsapSectors() {
        if (ValidationHelper.isNullOrEmpty(asapSectors) && !ValidationHelper.isNullOrEmpty(getReservationAsapSectors())) {
            asapSectors = new ArrayList<>();
            for (ReservationAsapSector sector : getReservationAsapSectors()) {
                asapSectors.add(sector.getAsapSector());
            }
        }
        return asapSectors;
    }

    public void setAsapSectors(List<AsapSector> asapSectors) {
        this.asapSectors = asapSectors;
    }

    public String getActivityLine() {
        return activityLine;
    }

    public void setActivityLine(String activityLine) {
        this.activityLine = activityLine;
    }
}
