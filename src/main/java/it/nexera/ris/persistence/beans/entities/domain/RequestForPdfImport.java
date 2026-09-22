package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.WaitingListRegistrationStates;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class RequestForPdfImport implements Serializable {

    private static final long serialVersionUID = 3769948708303231293L;

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "asap_sector_description")
    private String asapSectorDescription;

    @Column(name = "asap_activity_line")
    private String asapActivityLine;

    @Column(name = "diagnostic_question")
    private String diagnosticQuestion;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "wl_registration_state")
    private WaitingListRegistrationStates waitingListRegistrationState;

    @Transient
    public String getPatientSurnameName() {
        return String.format("%s %s", getSurname(), getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAsapSectorDescription() {
        return asapSectorDescription;
    }

    public void setAsapSectorDescription(String asapSectorDescription) {
        this.asapSectorDescription = asapSectorDescription;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public WaitingListRegistrationStates getWaitingListRegistrationState() {
        return waitingListRegistrationState;
    }

    public void setWaitingListRegistrationState(WaitingListRegistrationStates waitingListRegistrationState) {
        this.waitingListRegistrationState = waitingListRegistrationState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAsapActivityLine() {
        return asapActivityLine;
    }

    public void setAsapActivityLine(String asapActivityLine) {
        this.asapActivityLine = asapActivityLine;
    }

    public String getDiagnosticQuestion() {
        return diagnosticQuestion;
    }

    public void setDiagnosticQuestion(String diagnosticQuestion) {
        this.diagnosticQuestion = diagnosticQuestion;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String note) {
        this.notes = note;
    }
}