package it.nexera.ris.persistence.beans.entities.domain.relation;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Diagnostic;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "diagnostic_rad_ex")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "DIAGNOSTIC_RAD_EXAM_SEQ", allocationSize = 1)
public class DiagnosticRadiologyExam extends IndexedEntity {
    private static final long serialVersionUID = -4702553096098146657L;

    @ManyToOne
    @JoinColumn(name = "diagnostic_id", foreignKey = @ForeignKey(name = "FC_DIAGNOSTIC_RAD_EX"))
    private Diagnostic diagnostic;

    @ManyToOne
    @JoinColumn(name = "rad_ex_id", foreignKey = @ForeignKey(name = "FK_RAD_EX_DIAGNOSTIC"))
    private RadiologyExam radiologyExam;

    public DiagnosticRadiologyExam() {
    }

    public DiagnosticRadiologyExam(Diagnostic diagnostic, RadiologyExam radiologyExam) {
        this.diagnostic = diagnostic;
        this.radiologyExam = radiologyExam;
    }

    public Diagnostic getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    public RadiologyExam getRadiologyExam() {
        return radiologyExam;
    }

    public void setRadiologyExam(RadiologyExam radiologyExam) {
        this.radiologyExam = radiologyExam;
    }

    @Override
    public String toString() {
        return radiologyExam.toString();
    }
}
