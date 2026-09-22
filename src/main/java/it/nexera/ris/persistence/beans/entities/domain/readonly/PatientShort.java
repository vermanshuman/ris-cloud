package it.nexera.ris.persistence.beans.entities.domain.readonly;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Patient;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "patient")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PATIENT_SEQ", allocationSize = 1)
public class PatientShort extends IndexedEntity implements Comparable<Patient>,
        Serializable {

    private static final long serialVersionUID = 6171602283864285979L;

    @Column(insertable = false, updatable = false)
    private String name;

    @Column(insertable = false, updatable = false)
    private String surname;

    @Column(name = "birth_date", insertable = false, updatable = false)
    private Date birthDate;

    @Column(name = "fiscal_code", insertable = false, updatable = false)
    private String fiscalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birth_city_id", foreignKey = @ForeignKey(name = "FK_PATIENT_SHORT_BIRTH_CITY"))
    private City birthCity;

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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public City getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(City birthCity) {
        this.birthCity = birthCity;
    }

    @Override
    public int compareTo(Patient o) {
        if (!ValidationHelper.isNullOrEmpty(this.getName())
                && !ValidationHelper.isNullOrEmpty(o.getName())) {
            return this.getName().compareTo(o.getName());
        }

        return 0;
    }

}
