package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.AslRegion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "asl_region")
public class AslRegionList {
    @XmlElement(name = "data_entry", type = AslRegion.class)
    private List<AslRegion> aslRegions;

    public AslRegionList() {
        super();
    }

    public List<AslRegion> getAslRegions() {
        return aslRegions;
    }

    public void setAslRegions(List<AslRegion> aslRegions) {
        this.aslRegions = aslRegions;
    }

}
