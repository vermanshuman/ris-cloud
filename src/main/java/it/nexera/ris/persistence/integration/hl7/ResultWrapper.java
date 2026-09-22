package it.nexera.ris.persistence.integration.hl7;

public class ResultWrapper {
    private boolean result;

    private String description;

    public ResultWrapper() {
        super();
    }

    public ResultWrapper(boolean result, String description) {
        super();
        this.result = result;
        this.description = description;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
