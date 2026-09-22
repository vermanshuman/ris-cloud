package it.nexera.ris.web.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class BaseDto implements Serializable {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}