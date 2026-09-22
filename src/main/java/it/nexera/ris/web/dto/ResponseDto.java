package it.nexera.ris.web.dto;

import java.io.Serializable;

public class ResponseDto implements Serializable {
    private String resultCode;
    private String resultDescription;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }
}
