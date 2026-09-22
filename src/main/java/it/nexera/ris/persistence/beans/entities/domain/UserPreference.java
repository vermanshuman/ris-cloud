package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;

import javax.persistence.*;

@Entity
@Table(name = "user_preferences")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "USER_PREF_SEQ", allocationSize = 1)
public class UserPreference extends IndexedEntity {

    private static final long serialVersionUID = 7759244420439581575L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_USER_PREFS_USER"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private UserPreferenceType type;

    @Column(name = "items_pp")
    private Long itemsPerPage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_type_id", foreignKey = @ForeignKey(name = "FK_USER_PREFS_EXAM_TYPE"))
    private ExamType examType;

    @Enumerated(EnumType.STRING)
    @Column(name = "wl_state")
    private WaitingListRegistrationStates state;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_column")
    private WorkListOrderColumns orderColumn;

    @Column(name = "pdf_zoom")
    private Long pdfZoom;

    @Column(name = "use_description")
    private Boolean useDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_column_type")
    private OrderType orderColumnType;

    @Column(name = "label_print_active")
    private Boolean labelPrintActive;

    @Column(name = "label_print_name")
    private String labelPrintPrinterName;

    @Column(name = "label_print_count")
    private Integer labelPrintCount;


    @Column(name = "label_print_active_docg")
    private Boolean labelPrintActiveDocg;

    @Column(name = "label_print_name_docg")
    private String labelPrintPrinterNameDocg;

    @Column(name = "label_print_count_docg")
    private Integer labelPrintCountDocg;

    @Column(name = "auto_save_docg")
    private Boolean autoSaveDocg;

    @Column(name = "open_pdf_document")
    private Boolean openPdfDocument;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_alert_type")
    private PriorityAlertTypes priorityAlertType;

    @Column(name = "referring_diction")
    private String referringDiction;

    @Column(name = "vocal_recognition")
    private Boolean vocalRecognition;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserPreferenceType getType() {
        return type;
    }

    public void setType(UserPreferenceType type) {
        this.type = type;
    }

    public Long getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Long itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public WaitingListRegistrationStates getState() {
        return state;
    }

    public void setState(WaitingListRegistrationStates state) {
        this.state = state;
    }

    public WorkListOrderColumns getOrderColumn() {
        return orderColumn;
    }

    public void setOrderColumn(WorkListOrderColumns orderColumn) {
        this.orderColumn = orderColumn;
    }

    public Long getPdfZoom() {
        return pdfZoom;
    }

    public void setPdfZoom(Long pdfZoom) {
        this.pdfZoom = pdfZoom;
    }

    public Boolean getUseDescription() {
        return useDescription;
    }

    public void setUseDescription(Boolean useDescription) {
        this.useDescription = useDescription;
    }

    public OrderType getOrderColumnType() {
        return orderColumnType;
    }

    public void setOrderColumnType(OrderType orderColumnType) {
        this.orderColumnType = orderColumnType;
    }

    public Boolean getLabelPrintActive() {
        return labelPrintActive;
    }

    public void setLabelPrintActive(Boolean labelPrintActive) {
        this.labelPrintActive = labelPrintActive;
    }

    public String getLabelPrintPrinterName() {
        return labelPrintPrinterName;
    }

    public void setLabelPrintPrinterName(String labelPrintPrinterName) {
        this.labelPrintPrinterName = labelPrintPrinterName;
    }

    public Integer getLabelPrintCount() {
        return labelPrintCount;
    }

    public void setLabelPrintCount(Integer labelPrintCount) {
        this.labelPrintCount = labelPrintCount;
    }

    public Boolean getLabelPrintActiveDocg() {
        return labelPrintActiveDocg;
    }

    public void setLabelPrintActiveDocg(Boolean labelPrintActiveDocg) {
        this.labelPrintActiveDocg = labelPrintActiveDocg;
    }

    public String getLabelPrintPrinterNameDocg() {
        return labelPrintPrinterNameDocg;
    }

    public void setLabelPrintPrinterNameDocg(String labelPrintPrinterNameDocg) {
        this.labelPrintPrinterNameDocg = labelPrintPrinterNameDocg;
    }

    public Integer getLabelPrintCountDocg() {
        return labelPrintCountDocg;
    }

    public void setLabelPrintCountDocg(Integer labelPrintCountDocg) {
        this.labelPrintCountDocg = labelPrintCountDocg;
    }

    public Boolean getAutoSaveDocg() {
        return autoSaveDocg;
    }

    public void setAutoSaveDocg(Boolean autoSaveDocg) {
        this.autoSaveDocg = autoSaveDocg;
    }

    public Boolean getOpenPdfDocument() {
        return openPdfDocument;
    }

    public void setOpenPdfDocument(Boolean openPdfDocument) {
        this.openPdfDocument = openPdfDocument;
    }

    public PriorityAlertTypes getPriorityAlertType() {
        return priorityAlertType;
    }

    public void setPriorityAlertType(PriorityAlertTypes priorityAlertType) {
        this.priorityAlertType = priorityAlertType;
    }

    public String getReferringDiction() {
        return referringDiction;
    }

    public void setReferringDiction(String referringDiction) {
        this.referringDiction = referringDiction;
    }

    public Boolean getVocalRecognition() {
        return vocalRecognition;
    }

    public void setVocalRecognition(Boolean vocalRecognition) {
        this.vocalRecognition = vocalRecognition;
    }
}
