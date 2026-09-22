package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.Asl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "asl")
public class AslList {
    @XmlElement(name = "data_entry", type = Asl.class)
    private List<Asl> asls;

    public AslList() {
        super();
    }

    public List<Asl> getAsls() {
        return asls;
    }

    public void setAsls(List<Asl> asls) {
        this.asls = asls;
    }

}
