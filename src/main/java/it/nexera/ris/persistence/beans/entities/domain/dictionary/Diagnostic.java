package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.enums.EnableDisableEnum;
import it.nexera.ris.common.helpers.ListHelper;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendar;
import it.nexera.ris.persistence.beans.entities.domain.relation.DiagnosticRadiologyExam;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "dic_diagnostic")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "DIAGNOSTIC_SEQ", allocationSize = 1)
public class Diagnostic extends Dictionary {

    private static final long serialVersionUID = -7915205798272153294L;

    @Column
    private String aeTitle;

    @Column
    private String ipAddress;

    @Column
    private Long dicomPort;

    @Column
    private EnableDisableEnum state;

    @Column
    private String studyUID;

    @ManyToMany
    @JoinTable(name = "diagnostic_ex_types", joinColumns = {
            @JoinColumn(name = "diagnostic_id", table = "dic_diagnostic",
                    foreignKey = @ForeignKey(name = "FK_EXAM_TYPE_DIAG"))
    }, inverseJoinColumns = {
            @JoinColumn(name = "exam_type_id", table = "dic_exam_type",
                    foreignKey = @ForeignKey(name = "FK_DIAGNOSTIC_EX_TYPE"))
    })
    private List<ExamType> examTypes;

    @ManyToOne
    @JoinColumn(name = "sector_id", foreignKey = @ForeignKey(name = "FK_DIAGNOSTIC_SECTOR"))
    private Sector sector;

    @ManyToMany(mappedBy = "diagnostics")
    private Set<User> users;

    @SuppressWarnings("deprecation")
    @OneToMany(mappedBy = "diagnostic", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<DiagnosticRadiologyExam> radiologyExams;

    @Column(name = "use_card_number")
    private Boolean useCardNumber;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "card_number_year")
    private Long cardNumberYear;

    @Column
    private String examTypeDiagnostica;

    @OneToMany(mappedBy = "diagnostic")
    private List<EventCalendar> eventCalendar;

    @Transient
    public String getExamTypesExport() {
        return ListHelper.toString(this.examTypes);
    }

    public String getAeTitle() {
        return aeTitle;
    }

    public void setAeTitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public List<ExamType> getExamTypes() {
        return examTypes;
    }

    public void setExamTypes(List<ExamType> examTypes) {
        this.examTypes = examTypes;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Long getDicomPort() {
        return dicomPort;
    }

    public void setDicomPort(Long dicomPort) {
        this.dicomPort = dicomPort;
    }

    public EnableDisableEnum getState() {
        return state;
    }

    public void setState(EnableDisableEnum state) {
        this.state = state;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public List<DiagnosticRadiologyExam> getRadiologyExams() {
        return radiologyExams;
    }

    public void setRadiologyExams(List<DiagnosticRadiologyExam> radiologyExams) {
        this.radiologyExams = radiologyExams;
    }

    public Boolean getUseCardNumber() {
        return useCardNumber;
    }

    public void setUseCardNumber(Boolean useCardNumber) {
        this.useCardNumber = useCardNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getCardNumberYear() {
        return cardNumberYear;
    }

    public void setCardNumberYear(Long cardNumberYear) {
        this.cardNumberYear = cardNumberYear;
    }

    public String getExamTypeDiagnostica() {
        return examTypeDiagnostica;
    }

    public void setExamTypeDiagnostica(String examTypeDiagnostica) {
        this.examTypeDiagnostica = examTypeDiagnostica;
    }

    public List<EventCalendar> getEventCalendar() {
        return eventCalendar;
    }

    public void setEventCalendar(List<EventCalendar> eventCalendar) {
        this.eventCalendar = eventCalendar;
    }

}
