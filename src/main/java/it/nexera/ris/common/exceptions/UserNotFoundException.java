package it.nexera.ris.common.exceptions;

public class UserNotFoundException extends Exception {

    private static final long serialVersionUID = -3858414010315986661L;

    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
