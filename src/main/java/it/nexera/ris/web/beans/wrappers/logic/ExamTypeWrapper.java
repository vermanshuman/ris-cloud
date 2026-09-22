package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;

import java.io.Serializable;

public class ExamTypeWrapper implements Serializable {

    private static final long serialVersionUID = -2420934805565584593L;

    private Long id;

    private ExamType realExamType;

    private String value;

    private Boolean selected;

    public ExamTypeWrapper() {
    }

    public ExamTypeWrapper(ExamType state) {
        this.setId(state.getId());
        this.setValue(state.getDescription());
        this.setRealExamType(state);
    }

    public ExamTypeWrapper(ExamType state, Boolean selected) {
        this.setId(state.getId());
        this.setValue(state.getDescription());
        this.setRealExamType(state);
        this.setSelected(selected);
    }

    public ExamType getRealExamType() {
        return realExamType;
    }

    public void setRealExamType(ExamType realExamType) {
        this.realExamType = realExamType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getSelected() {
        return selected == null ? Boolean.FALSE : selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public boolean customEquals(ExamTypeWrapper etw) {
        if (!ValidationHelper.isNullOrEmptyMultiple(etw, this.getValue())
                && !ValidationHelper.isNullOrEmpty(etw.getValue())) {
            return this.getValue().equals(etw.getValue());
        } else {
            return false;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
