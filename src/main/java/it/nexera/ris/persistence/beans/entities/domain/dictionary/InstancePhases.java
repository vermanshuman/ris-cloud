package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "instance_phase")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "INSTANCE_PHASE_SEQ", allocationSize = 1)
public class InstancePhases extends IndexedEntity {
    private static final long serialVersionUID = -5252179405556255388L;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_place")
    private DocumentGenerationPlaces place;

    @ManyToOne
    @JoinColumn(name = "model_id", foreignKey = @ForeignKey(name = "FK_INSTANCE_PHASES_MODEL"))
    private TemplateDocumentModel model;

    @ManyToOne
    @JoinColumn(name = "sector_id", foreignKey = @ForeignKey(name = "FK_INSTANCE_PHASES_SECTOR"))
    private Sector sector;

    public TemplateDocumentModel getModel() {
        return model;
    }

    public void setModel(TemplateDocumentModel model) {
        this.model = model;
    }

    public DocumentGenerationPlaces getPlace() {
        return place;
    }

    public void setPlace(DocumentGenerationPlaces place) {
        this.place = place;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }
}
