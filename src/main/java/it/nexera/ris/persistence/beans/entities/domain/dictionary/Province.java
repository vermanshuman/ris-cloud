package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.Dictionary;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "dic_province")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PROVINCE_SEQ", allocationSize = 1)
public class Province extends Dictionary {

    private static final long serialVersionUID = -7915205798272153294L;

    @Column(name = "patient_instance")
    private Boolean patientInstance;

    public Boolean getPatientInstance() {
        return patientInstance;
    }

    public void setPatientInstance(Boolean patientInstance) {
        this.patientInstance = patientInstance;
    }
}
