package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "hl7_fields_from_asap")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "HL7_FIELDS_SEQ", allocationSize = 1)
public class Hl7FieldsFromAsap extends IndexedEntity {
    private static final long serialVersionUID = 8027250923326336352L;

    @Column(name = "placer_order_number")
    private String placerOrderNumber;

    @Column(name = "doctor_fiscal_code")
    private String doctorFiscalCode;

    @Column(name = "doctor_surname")
    private String doctorSurname;

    @Column(name = "doctor_name")
    private String doctorName;

    @Column(name = "psd_number")
    private String psdNumber;

    public String getPlacerOrderNumber() {
        return placerOrderNumber;
    }

    public void setPlacerOrderNumber(String placerOrderNumber) {
        this.placerOrderNumber = placerOrderNumber;
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

    public String getPsdNumber() {
        return psdNumber;
    }

    public void setPsdNumber(String psdNumber) {
        this.psdNumber = psdNumber;
    }
}
