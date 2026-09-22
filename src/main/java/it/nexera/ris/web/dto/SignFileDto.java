package it.nexera.ris.web.dto;

public class SignFileDto extends BaseDto {

    private String fileName;

    private String mimeType;

    private String data;

    private Long radiologyExamRequestId;

    private Long userId;

    private String viewId;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getRadiologyExamRequestId() {
        return radiologyExamRequestId;
    }

    public void setRadiologyExamRequestId(Long radiologyExamRequestId) {
        this.radiologyExamRequestId = radiologyExamRequestId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }
}
