package it.nexera.ris.web.dto;

import com.google.gson.annotations.SerializedName;

public class MetaDataRequestDTO {
    @SerializedName("user_id")
    private String userId;
    private String token;
    @SerializedName("date_from")
    private String dateFrom;

    @SerializedName("cda2")
    private Boolean cda2;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Boolean getCda2() {
        return cda2;
    }

    public void setCda2(Boolean cda2) {
        this.cda2 = cda2;
    }
}
