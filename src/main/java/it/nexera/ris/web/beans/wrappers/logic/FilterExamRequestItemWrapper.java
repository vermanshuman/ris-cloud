package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;

import java.io.Serializable;

public class FilterExamRequestItemWrapper implements Serializable {
    private static final long serialVersionUID = -1249236875823918905L;

    private Boolean selected;

    private RadiologyExam radiologyExam;

    private Boolean urgentRequest;

    private Boolean hasDuplicate;

    public FilterExamRequestItemWrapper(RadiologyExam radiologyExam) {
        this.setRadiologyExam(radiologyExam);
    }

    public Long getId() {
        if (this.getRadiologyExam() != null) {
            return this.getRadiologyExam().getId();
        }

        return null;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Boolean getUrgentRequest() {
        return urgentRequest;
    }

    public void setUrgentRequest(Boolean urgentRequest) {
        this.urgentRequest = urgentRequest;
    }

    public Boolean getHasDuplicate() {
        return hasDuplicate;
    }

    public void setHasDuplicate(Boolean hasDuplicate) {
        this.hasDuplicate = hasDuplicate;
    }

    public RadiologyExam getRadiologyExam() {
        return radiologyExam;
    }

    public void setRadiologyExam(RadiologyExam radiologyExam) {
        this.radiologyExam = radiologyExam;
    }
}
