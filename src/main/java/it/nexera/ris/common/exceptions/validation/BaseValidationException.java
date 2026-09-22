package it.nexera.ris.common.exceptions.validation;

public class BaseValidationException extends Exception {

    private static final long serialVersionUID = -847425267497401848L;

    public BaseValidationException() {
    }

    public BaseValidationException(String message) {
        super(message);
    }

}
