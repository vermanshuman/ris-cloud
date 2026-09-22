package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.List;

@Entity
@Table(name = "module")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "MODULE_SEQ", allocationSize = 1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
public class Module extends IndexedEntity {
    private static final long serialVersionUID = -8042530717985841991L;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "module", cascade = CascadeType.REMOVE)
    @Column(name = "pages")
    private List<ModulePage> pages;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_MODULE_MODULE_PARENT"))
    @XmlIDREF
    private Module parent;

    @Column(name = "position")
    private Integer position;

    @Column(name = "is_template")
    private String isTemplate;

    @Column(name = "code")
    @XmlID
    private String code;

    public String getName() {
        return name;
    }

    public List<ModulePage> getPages() {
        return pages;
    }

    public Module getParent() {
        return parent;
    }

    public Integer getPosition() {
        return position;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPages(List<ModulePage> pages) {
        this.pages = pages;
    }

    public void setParent(Module parent) {
        this.parent = parent;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String s = ResourcesHelper.getString(this.getName());
        if (s.equals(ResourcesHelper.getString("noData"))) {
            return this.getName();
        }
        return s;
    }

    @Transient
    public String getString() {
        return this.toString();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(String isTemplate) {
        this.isTemplate = isTemplate;
    }

}
