package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "package_exam")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PACKAGE_EXAM_SEQ", allocationSize = 1)
public class PackageRadiologyExam extends IndexedEntity {
    private static final long serialVersionUID = -7370097425294988222L;

    @ManyToOne
    @JoinColumn(name = "package_id", foreignKey = @ForeignKey(name = "FK_PACKAGE_RAD_EXAM_PACKAGE"))
    private Package dicPackage;

    @ManyToOne
    @JoinColumn(name = "radiology_exam_id", foreignKey = @ForeignKey(name = "FK_PACKAGE_RAD_EXAM_RAD_EXAM"))
    private RadiologyExam radiologyExam;

    public Package getDicPackage() {
        return dicPackage;
    }

    public void setDicPackage(Package dicPackage) {
        this.dicPackage = dicPackage;
    }

    public RadiologyExam getRadiologyExam() {
        return radiologyExam;
    }

    public void setRadiologyExam(RadiologyExam radiologyExam) {
        this.radiologyExam = radiologyExam;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return radiologyExam.toString();
    }

}
