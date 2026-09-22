package it.nexera.ris.common.xml.wrappers;


import it.nexera.ris.persistence.beans.entities.domain.ModulePage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "module_page")
public class ModulePageList {
    @XmlElement(name = "data_entry", type = ModulePage.class)
    private List<ModulePage> modulePages;

    public ModulePageList() {
        super();
    }

    public List<ModulePage> getModulePages() {
        return modulePages;
    }

    public void setModulePages(List<ModulePage> modulePages) {
        this.modulePages = modulePages;
    }

}