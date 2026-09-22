package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.enums.DoseClass;
import it.nexera.ris.common.enums.EnableDisableEnum;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.persistence.beans.entities.domain.relation.DiagnosticRadiologyExam;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "dic_radiology_exam")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "RAD_EXAM_SEQ", allocationSize = 1)
public class RadiologyExam extends Dictionary {
    private static final long serialVersionUID = -2932787012558550066L;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column
    private String regionalCode;

    @Column
    private Double amount;

    @Column
    private Double kWh;

    @Column
    private Double mAS;

    @Column
    private EnableDisableEnum state;

    @ManyToOne
    @JoinColumn(name = "exam_type_id", foreignKey = @ForeignKey(name = "FK_RADIOLOGY_EXAM_EXAM_TYPE"))
    private ExamType examType;

    @OneToMany(mappedBy = "radiologyExam", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<DiagnosticRadiologyExam> diagnostics;

    @Column
    private Boolean multiple;

    @Enumerated(EnumType.STRING)
    @Column(name = "dose_class")
    private DoseClass doseClass;

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public String getRegionalCode() {
        return regionalCode;
    }

    public void setRegionalCode(String regionalCode) {
        this.regionalCode = regionalCode;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getkWh() {
        return kWh;
    }

    public void setkWh(Double kWh) {
        this.kWh = kWh;
    }

    public Double getmAS() {
        return mAS;
    }

    public void setmAS(Double mAS) {
        this.mAS = mAS;
    }

    public EnableDisableEnum getState() {
        return state;
    }

    public void setState(EnableDisableEnum state) {
        this.state = state;
    }

    public Boolean getMultiple() {
        return multiple == null ? Boolean.FALSE : multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public List<DiagnosticRadiologyExam> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<DiagnosticRadiologyExam> diagnostics) {
        this.diagnostics = diagnostics;
    }

    @Transient
    public Boolean getCheckDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    public DoseClass getDoseClass() {
        return doseClass;
    }

    public void setDoseClass(DoseClass doseClass) {
        this.doseClass = doseClass;
    }
}
