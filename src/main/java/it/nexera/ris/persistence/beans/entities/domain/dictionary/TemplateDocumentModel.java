package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.DocumentTemplate;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "dic_model")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "MODEL_SEQ", allocationSize = 1)
public class TemplateDocumentModel extends IndexedEntity {
    private static final long serialVersionUID = -1585382014917551349L;

    @Column
    private String name;

    @OneToMany(mappedBy = "model")
    private List<DocumentTemplate> documentTemplates;

    @OneToMany(mappedBy = "model")
    private List<InstancePhases> instansePhases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public List<DocumentTemplate> getDocumentTemplates() {
        return documentTemplates;
    }

    public void setDocumentTemplates(List<DocumentTemplate> documentTemplates) {
        this.documentTemplates = documentTemplates;
    }

    public List<InstancePhases> getInstansePhases() {
        return instansePhases;
    }

    public void setInstansePhases(List<InstancePhases> instansePhases) {
        this.instansePhases = instansePhases;
    }
}
