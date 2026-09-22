package it.nexera.ris.persistence.integration;

import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Hospital;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Urgency;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ORMPair {
    private String diagnosticQuestion;

    private String note;

    private String additionalNote;

    private Long asapSectorId;

    private String asapSectorCode;

    private String asapSectorDescription;

    private Hospital hospital;

    private String placerOrderNumber;

    private DummyPatient dummyPatient;

    private List<RadiologyExamRequestItem> radExamItems;

    private String doctorFiscalCode;

    private String doctorSurname;

    private String doctorName;

    private Urgency urgency;

    private Sector sector;

    private String psdNumber;

    private String electronicRecipeNumber;

    private String asapActivityLine;

    private String requestingDoctor;

    private Long cupId;

    private Boolean forwarded;

    private Date reservationDate;

    private String transportType;

    public ORMPair() {
        super();
        dummyPatient = new DummyPatient();
        radExamItems = new ArrayList<RadiologyExamRequestItem>();
    }

    public String getDiagnosticQuestion() {
        return diagnosticQuestion;
    }

    public void setDiagnosticQuestion(String diagnosticQuestion) {
        this.diagnosticQuestion = diagnosticQuestion;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public DummyPatient getDummyPatient() {
        return dummyPatient;
    }

    public void setDummyPatient(DummyPatient dummyPatient) {
        this.dummyPatient = dummyPatient;
    }

    public List<RadiologyExamRequestItem> getRadExamItems() {
        return radExamItems;
    }

    public void setRadExamItems(List<RadiologyExamRequestItem> radExamItems) {
        this.radExamItems = radExamItems;
    }

    public String getPlacerOrderNumber() {
        return placerOrderNumber;
    }

    public void setPlacerOrderNumber(String placerOrderNumber) {
        this.placerOrderNumber = placerOrderNumber;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public String getDoctorFiscalCode() {
        return doctorFiscalCode;
    }

    public void setDoctorFiscalCode(String doctorFiscalCode) {
        this.doctorFiscalCode = doctorFiscalCode;
    }

    public String getDoctorSurname() {
        return doctorSurname;
    }

    public void setDoctorSurname(String doctorSurname) {
        this.doctorSurname = doctorSurname;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getPsdNumber() {
        return psdNumber;
    }

    public void setPsdNumber(String psdNumber) {
        this.psdNumber = psdNumber;
    }

    public String getAsapActivityLine() {
        return asapActivityLine;
    }

    public void setAsapActivityLine(String asapActivityLine) {
        this.asapActivityLine = asapActivityLine;
    }

    public Long getAsapSectorId() {
        return asapSectorId;
    }

    public void setAsapSectorId(Long asapSectorId) {
        this.asapSectorId = asapSectorId;
    }

    public String getAsapSectorCode() {
        return asapSectorCode;
    }

    public void setAsapSectorCode(String asapSectorCode) {
        this.asapSectorCode = asapSectorCode;
    }

    public String getAsapSectorDescription() {
        return asapSectorDescription;
    }

    public void setAsapSectorDescription(String asapSectorDescription) {
        this.asapSectorDescription = asapSectorDescription;
    }

    public String getRequestingDoctor() {
        return requestingDoctor;
    }

    public void setRequestingDoctor(String requestingDoctor) {
        this.requestingDoctor = requestingDoctor;
    }

    public Long getCupId() {
        return cupId;
    }

    public void setCupId(Long cupId) {
        this.cupId = cupId;
    }

    public Boolean getForwarded() {
        return forwarded;
    }

    public void setForwarded(Boolean forwarded) {
        this.forwarded = forwarded;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getAdditionalNote() {
        return additionalNote;
    }

    public void setAdditionalNote(String additionalNote) {
        this.additionalNote = additionalNote;
    }

    public String getElectronicRecipeNumber() {
        return electronicRecipeNumber;
    }

    public void setElectronicRecipeNumber(String electronicRecipeNumber) {
        this.electronicRecipeNumber = electronicRecipeNumber;
    }
}
