package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.helpers.ValidationHelper;

import java.io.Serializable;

public class DiagnosticWrapper implements Serializable {

    private static final long serialVersionUID = 291304127770689107L;

    private String value;

    private Long diagnosticId;

    private boolean selected;

    public DiagnosticWrapper() {
    }

    public DiagnosticWrapper(Long id, String description) {
        setValue(description);
        setDiagnosticId(id);
    }

    public DiagnosticWrapper(Long id, String description, boolean selected) {
        setValue(description);
        setDiagnosticId(id);
        setSelected(selected);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getDiagnosticId() {
        return diagnosticId;
    }

    public void setDiagnosticId(Long diagnosticId) {
        this.diagnosticId = diagnosticId;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }


    public boolean customEquals(DiagnosticWrapper state) {
        if (!ValidationHelper.isNullOrEmptyMultiple(state, this.getValue())
                && !ValidationHelper.isNullOrEmpty(state.getValue())) {
            return this.getValue().equals(state.getValue());
        } else {
            return false;
        }
    }
}
