package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "glossary_result")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "GLOSSARY_RESULT_SEQ", allocationSize = 1)
public class GlossaryResult extends IndexedEntity {
    private static final long serialVersionUID = -910888589358628169L;

    @Column
    private String value;

    @ManyToOne
    @JoinColumn(name = "glossary_exam_id", foreignKey = @ForeignKey(name = "FK_GLOSSARY_EXAM_RESULT"))
    private GlossaryExam glossaryExam;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public GlossaryExam getGlossaryExam() {
        return glossaryExam;
    }

    public void setGlossaryExam(GlossaryExam glossaryExam) {
        this.glossaryExam = glossaryExam;
    }
}
