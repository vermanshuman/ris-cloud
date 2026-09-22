package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ListHelper;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dic_package")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PACKAGE_SEQ", allocationSize = 1)
public class Package extends Dictionary implements Cloneable {
    private static final long serialVersionUID = 2068099338321631412L;

    @ManyToOne
    @JoinColumn(name = "exam_type_id", foreignKey = @ForeignKey(name = "FK_PACKAGE_EXAM_TYPE"))
    private ExamType examType;

    @OneToMany(mappedBy = "dicPackage")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<PackageRadiologyExam> radiologyExams;

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public List<PackageRadiologyExam> getRadiologyExams() {
        return radiologyExams;
    }

    public void setRadiologyExams(List<PackageRadiologyExam> radiologyExams) {
        this.radiologyExams = radiologyExams;
    }

    @Transient
    public String getRadiologyExamsExport() {
        return ListHelper.toString(this.radiologyExams);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Package clone() throws CloneNotSupportedException {
        Package clone = new Package();
        clone.setCode(this.getCode());
        clone.setDescription(this.getDescription());
        clone.setExamType(this.getExamType());
        clone.setRadiologyExams(new ArrayList<PackageRadiologyExam>());
        for (PackageRadiologyExam packageExam : this.getRadiologyExams()) {
            PackageRadiologyExam e = new PackageRadiologyExam();
            e.setRadiologyExam(packageExam.getRadiologyExam());
            e.setDicPackage(clone);
            clone.getRadiologyExams().add(e);
        }
        return clone;
    }
}
