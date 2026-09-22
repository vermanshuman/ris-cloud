package it.nexera.ris.persistence.beans.entities.domain.relation;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.AsapSector;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DirectReservation;

import javax.persistence.*;

@Entity
@Table(name = "reservation_asap_sector")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "RES_ASAP_SECTOR_SEQ", allocationSize = 1)
public class ReservationAsapSector extends IndexedEntity {

    private static final long serialVersionUID = -8329990059572798290L;

    @ManyToOne
    @JoinColumn(name = "direct_reservation_id")
    private DirectReservation directReservation;

    @ManyToOne
    @JoinColumn(name = "asap_sector_info_id")
    private AsapSector asapSector;

    public ReservationAsapSector() {
    }

    public ReservationAsapSector(DirectReservation directReservation, AsapSector asapSector) {
        this.directReservation = directReservation;
        this.asapSector = asapSector;
    }

    public DirectReservation getDirectReservation() {
        return directReservation;
    }

    public void setDirectReservation(DirectReservation directReservation) {
        this.directReservation = directReservation;
    }

    public AsapSector getAsapSector() {
        return asapSector;
    }

    public void setAsapSector(AsapSector asapSector) {
        this.asapSector = asapSector;
    }
}
