package it.nexera.ris.persistence.integration;

import java.util.Date;

public class PSDPair {
    private String patientId;

    private String psdNumber;

    private String sectorCode;

    private String psdStatus;

    private String recoveryKind;

    private Date recoveryDay;

    private String fiscalCode;

    private String snoNoteDiag;

    private Date snoDdim;

    private String snoTipTipo;

    private String snoMotMoti;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPsdNumber() {
        return psdNumber;
    }

    public void setPsdNumber(String psdNumber) {
        this.psdNumber = psdNumber;
    }

    public String getSectorCode() {
        return sectorCode;
    }

    public void setSectorCode(String sectorCode) {
        this.sectorCode = sectorCode;
    }

    public String getPsdStatus() {
        return psdStatus;
    }

    public void setPsdStatus(String psdStatus) {
        this.psdStatus = psdStatus;
    }

    public String getRecoveryKind() {
        return recoveryKind;
    }

    public void setRecoveryKind(String recoveryKind) {
        this.recoveryKind = recoveryKind;
    }

    public Date getRecoveryDay() {
        return recoveryDay;
    }

    public void setRecoveryDay(Date recoveryDay) {
        this.recoveryDay = recoveryDay;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getSnoNoteDiag() {
        return snoNoteDiag;
    }

    public void setSnoNoteDiag(String snoNoteDiag) {
        this.snoNoteDiag = snoNoteDiag;
    }

    public Date getSnoDdim() {
        return snoDdim;
    }

    public void setSnoDdim(Date snoDdim) {
        this.snoDdim = snoDdim;
    }

    public String getSnoTipTipo() {
        return snoTipTipo;
    }

    public void setSnoTipTipo(String snoTipTipo) {
        this.snoTipTipo = snoTipTipo;
    }

    public String getSnoMotMoti() {
        return snoMotMoti;
    }

    public void setSnoMotMoti(String snoMotMoti) {
        this.snoMotMoti = snoMotMoti;
    }

}
