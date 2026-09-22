package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;

import java.io.Serializable;
import java.util.Date;

public class RadExamRequestItemWrapper implements Serializable {
    private static final long serialVersionUID = -6879758953639272803L;

    private Long id;

    private RadiologyExam radiologyExam;

    private String requestingDoctor;

    private Date reserveDate;

    private Date requestDate;

    private Boolean selected;

    private Long requestId;

    private String requestDiagnosticQuestion;

    private String accessNumber;

    private Boolean disabled;

    private Boolean addedManually;

    private RadExamRequestWrapper requestWrapper;

    public RadExamRequestItemWrapper() {
        super();
    }

    public RadExamRequestItemWrapper(Long id, RadiologyExam radiologyExam) {
        super();
        this.id = id;
        this.radiologyExam = radiologyExam;
        this.setDisabled(Boolean.FALSE);
    }

    public RadExamRequestItemWrapper(Long id, RadiologyExam radiologyExam,
                                     String requestingDoctor, Date reserveDate, Date requestDate,
                                     Long requestId, String requestDiagnosticQuestion,
                                     String accessNumber, RadExamRequestWrapper requestWrapper, Boolean manually) {
        super();
        this.id = id;
        this.radiologyExam = radiologyExam;
        this.requestingDoctor = requestingDoctor;
        this.reserveDate = reserveDate;
        this.requestDate = requestDate;
        this.requestId = requestId;
        this.requestDiagnosticQuestion = requestDiagnosticQuestion;
        this.accessNumber = accessNumber;
        this.requestWrapper = requestWrapper;
        this.setDisabled(Boolean.FALSE);
        this.setAddedManually(manually);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RadiologyExam getRadiologyExam() {
        return radiologyExam;
    }

    public void setRadiologyExam(RadiologyExam radiologyExam) {
        this.radiologyExam = radiologyExam;
    }

    public String getRequestingDoctor() {
        return requestingDoctor;
    }

    public void setRequestingDoctor(String requestingDoctor) {
        this.requestingDoctor = requestingDoctor;
    }

    public Date getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(Date reserveDate) {
        this.reserveDate = reserveDate;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Boolean getSelected() {
        return selected == null ? Boolean.TRUE : selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getRequestDiagnosticQuestion() {
        return requestDiagnosticQuestion;
    }

    public void setRequestDiagnosticQuestion(String requestDiagnosticQuestion) {
        this.requestDiagnosticQuestion = requestDiagnosticQuestion;
    }

    public String getAccessNumber() {
        return accessNumber;
    }

    public void setAccessNumber(String accessNumber) {
        this.accessNumber = accessNumber;
    }

    public RadExamRequestWrapper getRequestWrapper() {
        return requestWrapper;
    }

    public void setRequestWrapper(RadExamRequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Boolean getAddedManually() {
        return addedManually;
    }

    public void setAddedManually(Boolean addedManually) {
        this.addedManually = addedManually;
    }


}
