package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.ConservationStates;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Patient;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RadExamRequestWrapper implements Serializable {
    private static final long serialVersionUID = -1799692820073950460L;

    private Long id;

    private ExamType examType;

    private Date latestActionDate;

    private List<RadExamRequestItemWrapper> radExamRequestItemWrappers;

    private Patient patient;

    private String asapSectorCode;

    private String asapSectorDescription;

    private String asapActivityLine;

    private String accessNumber;

    private Long hospitalId;

    private WaitingListRegistrationStates state;

    private Long userClosingReport;

    private String asapPlaceOrderNumber;

    private Long sectorId;

    private Boolean isSelected;

    private Boolean forwarded;

    private ConservationStates conservationState;

    public RadExamRequestWrapper() {
        super();
    }

    public RadExamRequestWrapper(Long id, ExamType examType,
                                 Date latestActionDate,
                                 List<RadExamRequestItemWrapper> radExamRequestItemWrappers,
                                 Patient patient, String accessNumber,
                                 WaitingListRegistrationStates state, Long userClosingReport,
                                 String asapPlaceOrderNumber, Long hospitalId,
                                 String asapSectorCode, String asapSectorDescription,
                                 String asapActivityLine, Long sectorId) {
        super();
        this.id = id;
        this.examType = examType;
        this.latestActionDate = latestActionDate;
        this.radExamRequestItemWrappers = radExamRequestItemWrappers;
        this.patient = patient;
        this.accessNumber = accessNumber;
        this.asapPlaceOrderNumber = asapPlaceOrderNumber;
        this.hospitalId = hospitalId;
        this.state = state;
        this.userClosingReport = userClosingReport;
        this.hospitalId = hospitalId;
        this.asapSectorCode = asapSectorCode;
        this.asapSectorDescription = asapSectorDescription;
        this.asapActivityLine = asapActivityLine;
        this.sectorId = sectorId;
    }

    public RadExamRequestWrapper(Long id, ExamType examType,
                                 Date latestActionDate,
                                 List<RadExamRequestItemWrapper> radExamRequestItemWrappers,
                                 Patient patient, String accessNumber,
                                 WaitingListRegistrationStates state, Long userClosingReport,
                                 String asapPlaceOrderNumber, Long hospitalId,
                                 String asapSectorCode, String asapSectorDescription,
                                 String asapActivityLine, Long sectorId,  Boolean forwarded) {
       this(id, examType, latestActionDate, radExamRequestItemWrappers, patient, accessNumber, state, userClosingReport,
               asapPlaceOrderNumber, hospitalId, asapSectorCode, asapSectorDescription,asapActivityLine, sectorId);
       this.forwarded = forwarded;
    }


    public RadExamRequestWrapper(Long id, ExamType examType,
                                 Date latestActionDate,
                                 List<RadExamRequestItemWrapper> radExamRequestItemWrappers,
                                 Patient patient, String accessNumber,
                                 WaitingListRegistrationStates state, Long userClosingReport,
                                 String asapPlaceOrderNumber, Long hospitalId,
                                 String asapSectorCode, String asapSectorDescription,
                                 String asapActivityLine, Long sectorId,  Boolean forwarded, ConservationStates conservationState) {
        this(id, examType, latestActionDate, radExamRequestItemWrappers, patient, accessNumber, state, userClosingReport,
                asapPlaceOrderNumber, hospitalId, asapSectorCode, asapSectorDescription,asapActivityLine, sectorId, forwarded);
        this.conservationState = conservationState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public Date getLatestActionDate() {
        return latestActionDate;
    }

    public void setLatestActionDate(Date latestActionDate) {
        this.latestActionDate = latestActionDate;
    }

    public List<RadExamRequestItemWrapper> getRadExamRequestItemWrappers() {
        return radExamRequestItemWrappers;
    }

    public void setRadExamRequestItemWrappers(
            List<RadExamRequestItemWrapper> radExamRequestItemWrappers) {
        this.radExamRequestItemWrappers = radExamRequestItemWrappers;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getAccessNumber() {
        return accessNumber;
    }

    public void setAccessNumber(String accessNumber) {
        this.accessNumber = accessNumber;
    }

    public WaitingListRegistrationStates getState() {
        return state;
    }

    public void setState(WaitingListRegistrationStates state) {
        this.state = state;
    }

    public boolean getSelectedAll() {
        if (!ValidationHelper.isNullOrEmpty(this
                .getRadExamRequestItemWrappers())) {
            for (RadExamRequestItemWrapper reriw : this
                    .getRadExamRequestItemWrappers()) {
                if (!reriw.getSelected().booleanValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    public List<Long> getItemsIds() {
        List<Long> itemsIds = null;

        if (!ValidationHelper.isNullOrEmpty(this
                .getRadExamRequestItemWrappers())) {
            itemsIds = new ArrayList<Long>();
            for (RadExamRequestItemWrapper reriw : this
                    .getRadExamRequestItemWrappers()) {
                itemsIds.add(reriw.getId());
            }
        }

        return itemsIds;
    }

    public Long getUserClosingReport() {
        return userClosingReport;
    }

    public void setUserClosingReport(Long userClosingReport) {
        this.userClosingReport = userClosingReport;
    }

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getAsapPlaceOrderNumber() {
        return asapPlaceOrderNumber;
    }

    public void setAsapPlaceOrderNumber(String asapPlaceOrderNumber) {
        this.asapPlaceOrderNumber = asapPlaceOrderNumber;
    }

    public String getAsapSectorCode() {
        return asapSectorCode;
    }

    public void setAsapSectorCode(String asapSectorCode) {
        this.asapSectorCode = asapSectorCode;
    }

    public String getAsapSectorDescription() {
        return asapSectorDescription;
    }

    public void setAsapSectorDescription(String asapSectorDescription) {
        this.asapSectorDescription = asapSectorDescription;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public String getAsapActivityLine() {
        return asapActivityLine;
    }

    public void setAsapActivityLine(String asapActivityLine) {
        this.asapActivityLine = asapActivityLine;
    }

    public Boolean getSelected() {
        return isSelected == null ? false : isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public String getAsapSectorWithActivity() {
        if (!ValidationHelper.isNullOrEmpty(this.getAsapActivityLine())) {
            return this.getAsapActivityLine() + " - "
                    + this.getAsapSectorDescription();
        } else {
            return this.getAsapSectorDescription();
        }
    }

    public Boolean getForwarded() {
        return forwarded;
    }

    public void setForwarded(Boolean forwarded) {
        this.forwarded = forwarded;
    }

    public ConservationStates getConservationState() {
        return conservationState;
    }

    public void setConservationState(ConservationStates conservationState) {
        this.conservationState = conservationState;
    }
}
