package it.nexera.ris.common.exceptions;

public class IntegrationConnectionException extends Exception {
    private static final long serialVersionUID = 3647446896712640456L;

    public IntegrationConnectionException(String message) {
        super(message);
    }

    public IntegrationConnectionException() {
        super();
    }

}
