package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ListHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.ExamTypeAction;
import it.nexera.ris.persistence.beans.entities.domain.Glossary;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "dic_exam_type")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "EXAM_TYPE_SEQ", allocationSize = 1)
public class ExamType extends IndexedEntity {

    private static final long serialVersionUID = 6216833678130840943L;

    @Column
    private String color;

    @Column(name = "has_color")
    private Boolean hasColor;

    @Column(name = "code_affinity_domain ")
    private String codeAffinityDomain;

    @Column(name = "app_inviante_unidoc")
    private String sendingApp ;

    @Column(name = "codice_tipologia_documento")
    private String documentTypeCode ;

    @Column(name = "document_format_code")
    private String documentFormatCode;

    @OneToMany(mappedBy = "examType")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Package> packages;

    @Column
    private String description;

    @OneToMany(mappedBy = "examType")
    private List<RadiologyExam> radiologyExams;

    @ManyToMany(mappedBy = "examTypes")
    private List<Diagnostic> diagnostics;

    @OneToMany(mappedBy = "examType")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Glossary> glossaries;

    @OneToMany(mappedBy = "examType", cascade = CascadeType.REMOVE)
    private List<ExamTypeAction> examTypeAction;

    @Transient
    public String getRadiologyExamsExport() {
        return ListHelper.toString(this.radiologyExams);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public List<RadiologyExam> getRadiologyExams() {
        return radiologyExams;
    }

    public void setRadiologyExams(List<RadiologyExam> radiologyExams) {
        this.radiologyExams = radiologyExams;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setCodeAffinityDomain(String codeAffinityDomain) {
        this.codeAffinityDomain = codeAffinityDomain;
    }

    public String getCodeAffinityDomain() {
        return this.codeAffinityDomain;
    }

    public void setSendingApp(String sendingApp) {
        this.sendingApp = sendingApp;
    }

    public String getSendingApp()
    {
        return this.sendingApp;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public String getDocumentTypeCode() {
        return this.documentTypeCode;
    }

    public void setDocumentFormatCode(String documentFormatCode) {
        this.documentFormatCode = documentFormatCode;
    }

    public String getDocumentFormatCode() {
        return this.documentFormatCode;
    }

    public Boolean getHasColor() {
        return hasColor;
    }

    public void setHasColor(Boolean hasColor) {
        this.hasColor = hasColor;
    }

    public String getBackgroundStyle() {
        if (this.color != null && !this.color.isEmpty()
                && this.hasColor != null && this.hasColor.booleanValue()) {
            StringBuilder sb = new StringBuilder();
            sb.append("background-color: #");
            sb.append(this.color);
            return sb.toString();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return description;
    }

    public List<Glossary> getGlossaries() {
        return glossaries;
    }

    public void setGlossaries(List<Glossary> glossaries) {
        this.glossaries = glossaries;
    }

    public List<ExamTypeAction> getExamTypeAction() {
        return examTypeAction;
    }

    public void setExamTypeAction(List<ExamTypeAction> examTypeAction) {
        this.examTypeAction = examTypeAction;
    }

}
