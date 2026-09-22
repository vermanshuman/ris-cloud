package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "radiology_exam")
public class RadiologyExamList {
    @XmlElement(name = "data_entry", type = RadiologyExam.class)
    private List<RadiologyExam> radExams;

    public RadiologyExamList() {
        super();
    }

    public List<RadiologyExam> getRadExams() {
        return radExams;
    }

    public void setRadExams(List<RadiologyExam> radExams) {
        this.radExams = radExams;
    }

}
