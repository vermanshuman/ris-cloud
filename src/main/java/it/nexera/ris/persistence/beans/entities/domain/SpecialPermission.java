package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.SpecialPermissionTypes;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@javax.persistence.Entity
@Table(name = "spec_permission")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "SPEC_PERMISSION_SEQ", allocationSize = 1)
public class SpecialPermission extends IndexedEntity {

    private static final long serialVersionUID = 887466697457473579L;

    @Column(name = "special_permission_type")
    @Enumerated(EnumType.STRING)
    private SpecialPermissionTypes specialPermissionType;

    @ManyToMany(mappedBy = "specPermissions")
    private List<Role> roles = new ArrayList<Role>();

    @Transient
    private Boolean selected;

    @Override
    public String toString() {
        return specialPermissionType.toString();
    }

    public SpecialPermissionTypes getSpecialPermissionType() {
        return specialPermissionType;
    }

    public void setSpecialPermissionType(
            SpecialPermissionTypes specialPermissionType) {
        this.specialPermissionType = specialPermissionType;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public Boolean getSelected() {
        return selected == null ? Boolean.FALSE : selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
