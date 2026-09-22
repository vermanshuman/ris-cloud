package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "glossary_exam")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "GLOSSARY_EXAM_SEQ", allocationSize = 1)
public class GlossaryExam extends IndexedEntity {
    private static final long serialVersionUID = -4026746477603513487L;

    @Column
    private Long medicId;

    @Column
    private String exam;

    @OneToMany(mappedBy = "glossaryExam")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<GlossaryResult> results;

    public String getExam() {
        return exam;
    }

    public void setExam(String exam) {
        this.exam = exam;
    }

    public List<GlossaryResult> getResults() {
        return results;
    }

    public void setResults(List<GlossaryResult> results) {
        this.results = results;
    }

    public Long getMedicId() {
        return medicId;
    }

    public void setMedicId(Long medicId) {
        this.medicId = medicId;
    }
}
