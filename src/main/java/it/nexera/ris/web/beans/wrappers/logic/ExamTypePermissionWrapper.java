package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;

import java.io.Serializable;

public class ExamTypePermissionWrapper implements Serializable {

    private static final long serialVersionUID = -1850401443659451624L;

    private ExamType examType;

    private Boolean state;

    public ExamTypePermissionWrapper(ExamType examType, Boolean state) {
        super();
        this.examType = examType;
        this.state = state;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Boolean getState() {
        return state;
    }

}
