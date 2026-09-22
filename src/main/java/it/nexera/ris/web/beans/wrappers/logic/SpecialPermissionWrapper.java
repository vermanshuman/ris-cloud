package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.SpecialPermissionTypes;
import it.nexera.ris.persistence.beans.entities.domain.SpecialPermission;

import java.io.Serializable;

public class SpecialPermissionWrapper implements Serializable {

    private static final long serialVersionUID = 1280697436468965074L;

    private Long id;

    private SpecialPermissionTypes specialPermissionType;

    public SpecialPermissionWrapper(SpecialPermission sp) {
        this.setId(sp.getId());
        this.setSpecialPermissionType(sp.getSpecialPermissionType());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SpecialPermissionTypes getSpecialPermissionType() {
        return specialPermissionType;
    }

    public void setSpecialPermissionType(
            SpecialPermissionTypes specialPermissionType) {
        this.specialPermissionType = specialPermissionType;
    }

}
