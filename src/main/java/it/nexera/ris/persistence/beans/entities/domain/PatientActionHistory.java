package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.ActionHistory;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "patient_action_history")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PATEINT_ACT_HISTORY_SEQ", allocationSize = 1)
public class PatientActionHistory extends ActionHistory implements Serializable {

    private static final long serialVersionUID = 1212791609765432631L;

    @ManyToOne
    @JoinColumn(name = "patient_id", foreignKey = @ForeignKey(name = "FK_PAT_ACTION_HISTORY_PATIENT"))
    private Patient patient;

    @Column(name = "waiting_list_name")
    private String waitingListName;

    public Patient getPatient() {
        return patient;
    }

    public String getWaitingListName() {
        return waitingListName;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void setWaitingListName(String waitingListName) {
        this.waitingListName = waitingListName;
    }
}
