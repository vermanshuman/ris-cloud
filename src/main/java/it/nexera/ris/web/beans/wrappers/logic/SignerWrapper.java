package it.nexera.ris.web.beans.wrappers.logic;

public class SignerWrapper {

    private String pdfUrl;

    private String pdfFilename;

    private String userId;

    private String requestId;

    private String callbackUrl;


    public SignerWrapper() {
    }

    public SignerWrapper(String pdfUrl, String pdfFilename, String userId, String requestId, String callbackUrl) {
        this.pdfUrl = pdfUrl;
        this.pdfFilename = pdfFilename;
        this.userId = userId;
        this.requestId = requestId;
        this.callbackUrl = callbackUrl;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getPdfFilename() {
        return pdfFilename;
    }

    public void setPdfFilename(String pdfFilename) {
        this.pdfFilename = pdfFilename;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
