package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.persistence.beans.entities.domain.Role;

import java.io.Serializable;

public class RoleWrapper implements Serializable {

    private static final long serialVersionUID = 5419226437759173082L;

    private Long id;

    private RoleTypes type;

    private String name;

    public RoleWrapper(Role r) {
        this.setType(r.getType());
        this.setId(r.getId());
        this.setName(r.getName());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleTypes getType() {
        return type;
    }

    public void setType(RoleTypes type) {
        this.type = type;
    }

}
