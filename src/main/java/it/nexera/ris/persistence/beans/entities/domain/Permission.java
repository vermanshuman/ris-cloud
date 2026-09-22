package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "permission")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PERMISSION_SEQ", allocationSize = 1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
public class Permission extends IndexedEntity {
    private static final long serialVersionUID = -6477750452633701341L;

    @Column(name = "can_create")
    @XmlElement(name = "can_create")
    private Boolean canCreate;

    @Column(name = "can_delete")
    @XmlElement(name = "can_delete")
    private Boolean canDelete;

    @Column(name = "can_edit")
    @XmlElement(name = "can_edit")
    private Boolean canEdit;

    @Column(name = "can_view")
    @XmlElement(name = "can_view")
    private Boolean canView;

    @Column(name = "can_list")
    @XmlElement(name = "can_list")
    private Boolean canList;

    @ManyToOne
    @JoinColumn
    private Module module;

    @ManyToOne
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_PERMISSION_ROLE"))
    private Role role;

    @Transient
    @XmlElement(name = "module_code")
    private String module_code;

    @Transient
    @XmlElement(name = "role_id")
    private Long role_id;

    public Boolean getCanCreate() {
        return canCreate;
    }

    public void setCanCreate(Boolean canCreate) {
        this.canCreate = canCreate;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    public Boolean getCanView() {
        return canView;
    }

    public void setCanView(Boolean canView) {
        this.canView = canView;
    }

    public Boolean getCanList() {
        return canList;
    }

    public void setCanList(Boolean canList) {
        this.canList = canList;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getModule_code() {
        return module_code;
    }

    public void setModule_code(String module_code) {
        this.module_code = module_code;
    }

    public Long getRole_id() {
        return role_id;
    }

    public void setRole_id(Long role_id) {
        this.role_id = role_id;
    }

}
