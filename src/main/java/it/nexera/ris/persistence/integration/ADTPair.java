package it.nexera.ris.persistence.integration;

import java.util.Date;

public class ADTPair extends PSDPair {

    private Date lastUpdateDate;

    private DummyPatient dummyPatient;

    private Date transferDate;

    private String transferSectorCode;

    private Date dismissDate;

    private String dismissSectorCode;

    public ADTPair() {
        super();
        dummyPatient = new DummyPatient();
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public DummyPatient getDummyPatient() {
        return dummyPatient;
    }

    public void setDummyPatient(DummyPatient dummyPatient) {
        this.dummyPatient = dummyPatient;
    }

    public Date getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(Date transferDate) {
        this.transferDate = transferDate;
    }

    public String getTransferSectorCode() {
        return transferSectorCode;
    }

    public void setTransferSectorCode(String transferSectorCode) {
        this.transferSectorCode = transferSectorCode;
    }

    public Date getDismissDate() {
        return dismissDate;
    }

    public void setDismissDate(Date dismissDate) {
        this.dismissDate = dismissDate;
    }

    public String getDismissSectorCode() {
        return dismissSectorCode;
    }

    public void setDismissSectorCode(String dismissSectorCode) {
        this.dismissSectorCode = dismissSectorCode;
    }

}
