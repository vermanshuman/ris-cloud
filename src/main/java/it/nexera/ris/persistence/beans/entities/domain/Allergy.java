package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "patient_allergies")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "ALLERGIES_SEQ", allocationSize = 1)
public class Allergy extends IndexedEntity {
    private static final long serialVersionUID = 6269397050445680964L;

    @Column(columnDefinition = "VARCHAR2(2048)")
    private String allergy;

    @Column(name = "date_edit")
    private Date date;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Transient
    public String getCreateUserName() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        return DaoManager.get(User.class, getCreateUserId()).getFullname();
    }

    @Transient
    public boolean getCanEditAndDelete() {
        if (getCreateUserId() != null
                && UserHolder.getInstance().getCurrentUser() != null) {
            return getCreateUserId().equals(
                    UserHolder.getInstance().getCurrentUser().getId());
        } else {
            return false;
        }
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getAllergy() {
        return allergy;
    }

    public void setAllergy(String allergy) {
        this.allergy = allergy;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
