package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;

import javax.persistence.*;

@Entity
@Table(name = "glossary")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "GLOSSARY_SEQ", allocationSize = 1)
public class Glossary extends IndexedEntity {
    private static final long serialVersionUID = 6269397050445680964L;

    @Column
    private String title;

    @Column
    private String code;

    @Column(columnDefinition = "CLOB")
    private String text;

    @Column
    private Long medicId;

    @ManyToOne
    @JoinColumn(name = "exam_type_id", foreignKey = @ForeignKey(name = "FK_GLOSSARY_EXAM_TYPE"))
    private ExamType examType;

    @Column
    private String exam;

    @Column
    private String result;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getMedicId() {
        return medicId;
    }

    public void setMedicId(Long medicId) {
        this.medicId = medicId;
    }

    public String getExam() {
        return exam;
    }

    public void setExam(String exam) {
        this.exam = exam;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

}
