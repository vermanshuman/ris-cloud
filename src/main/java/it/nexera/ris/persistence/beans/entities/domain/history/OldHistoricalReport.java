package it.nexera.ris.persistence.beans.entities.domain.history;

import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "old_historical_report")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "OLD_HIST_REP_SEQ", allocationSize = 1)
public class OldHistoricalReport extends IndexedEntity implements Cloneable {
    private static final long serialVersionUID = 5021737537593419648L;

    @Column(name = "codfis_paziente")
    private String patientFiscalCode;

    @Column(name = "nome_paziente")
    private String patientName;

    @Column(name = "cognome_patiente")
    private String patientSurname;

    @Column(name = "datanascita")
    private Date patientBirthDate;

    @Column(name = "data_esame")
    private Date reserveDate;

    @Column(name = "data_refertazione")
    private Date reportDate;

    @Column(name = "accession_number")
    private String accessNumber;

    @Column(name = "cod_esa")
    private String examCode;

    @Column(name = "descrizione_esame")
    private String examDescription;

    @Column(name = "medico")
    private String doctor;

    @Column(name = "txt_referto")
    private String reportResult;

    @Column(name = "rtf_referto")
    private String rtf;

    @Column(name = "pdf_Path")
    private String pdfPath;

    @Column(name = "hospital_code")
    private String hospitalCode;

    @Transient
    public HistoricalReport getNewHistoricalReport(List<Long> currentMigratedIds)
            throws Exception {
        if (currentMigratedIds.contains(this.getId())) {
            return null;
        }

        HistoricalReport historicalReport = new HistoricalReport();

        historicalReport.setIdInOldDb(this.getId());
        historicalReport.setReportResult(this.getReportResult());
        //Patient
        historicalReport.setPatientFiscalCode(this.getPatientFiscalCode());
        historicalReport.setPatientName(this.getPatientName());
        historicalReport.setPatientSurname(this.getPatientSurname());
        historicalReport.setPatientBirthDate(this.getPatientBirthDate());

        //Date
        historicalReport.setReserveDate(this.getReserveDate());
        historicalReport.setReportDate(this.getReportDate());

        if (!ValidationHelper.isNullOrEmpty(this.getReserveDate())
                && !ValidationHelper.isNullOrEmpty(this.getReportDate())) {
            historicalReport.setLatestActionDate(this.getReserveDate().after(
                    this.getReportDate()) ? this.getReserveDate() : this
                    .getReportDate());
        } else if (ValidationHelper.isNullOrEmpty(this.getReserveDate())
                && !ValidationHelper.isNullOrEmpty(this.getReportDate())) {
            historicalReport.setLatestActionDate(this.getReportDate());
        } else if (!ValidationHelper.isNullOrEmpty(this.getReserveDate())
                && ValidationHelper.isNullOrEmpty(this.getReportDate())) {
            historicalReport.setLatestActionDate(this.getReserveDate());
        }

        //Access number
        historicalReport.setAccessNumber(this.getAccessNumber());

        historicalReport
                .setWaitingListRegistrationState(WaitingListRegistrationStates.REPORTED);

        //File
        if (getPdfPath() != null) {
            FileEntity fileEntity = FileHelper
                    .getFileEntityFromPathFromCatalina(getPdfPath());
            if (fileEntity != null) {
                historicalReport.setFileEntity(fileEntity);
            } else {
                return null;
                /*throw new Exception("Cannot migrate file with path: "
                        + getPdfPath());*/
            }
        }
        //Hospital
        if (!ValidationHelper.isNullOrEmpty(getHospitalCode())) {
            historicalReport.setHospitalCode(getHospitalCode());
        }
        //Radiology Exam
        if (!ValidationHelper.isNullOrEmpty(this.getExamCode())) {
            List<RadiologyExam> radiologyExamsWithThisDescription = DaoManager
                    .load(RadiologyExam.class,
                            new Criterion[]{
                                    Restrictions.eq("description",
                                            this.getExamDescription())
                            });
            if (radiologyExamsWithThisDescription != null
                    && !radiologyExamsWithThisDescription.isEmpty()
                    && radiologyExamsWithThisDescription.get(0) != null) {
                List<RadiologyExam> radiologyExams = new ArrayList<RadiologyExam>();
                radiologyExams.add(radiologyExamsWithThisDescription.get(0));

                historicalReport.setRadiologyExams(radiologyExams);
            } else {
                List<RadiologyExam> radiologyExamsWithThisCode = DaoManager
                        .load(RadiologyExam.class, new Criterion[]{
                                Restrictions.eq("code", this.getExamCode())
                        });
                if (radiologyExamsWithThisCode != null
                        && !radiologyExamsWithThisCode.isEmpty()
                        && radiologyExamsWithThisCode.get(0) != null) {
                    List<RadiologyExam> radiologyExams = new ArrayList<RadiologyExam>();
                    radiologyExams.add(radiologyExamsWithThisCode.get(0));

                    historicalReport.setRadiologyExams(radiologyExams);
                } else {
                    return new HistoricalReport();
                }
            }
        }

        historicalReport.setReferringDoctor(this.getDoctor());

        return historicalReport;
    }

    public String getPatientFiscalCode() {
        return patientFiscalCode;
    }

    public void setPatientFiscalCode(String patientFiscalCode) {
        this.patientFiscalCode = patientFiscalCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public Date getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(Date patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public Date getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(Date reserveDate) {
        this.reserveDate = reserveDate;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getAccessNumber() {
        return accessNumber;
    }

    public void setAccessNumber(String accessNumber) {
        this.accessNumber = accessNumber;
    }

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getReportResult() {
        return reportResult;
    }

    public void setReportResult(String reportResult) {
        this.reportResult = reportResult;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getRtf() {
        return rtf;
    }

    public void setRtf(String rtf) {
        this.rtf = rtf;
    }

    public String getHospitalCode() {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode) {
        this.hospitalCode = hospitalCode;
    }
}
