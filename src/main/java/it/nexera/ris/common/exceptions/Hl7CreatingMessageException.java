package it.nexera.ris.common.exceptions;

public class Hl7CreatingMessageException extends Exception {
    private static final long serialVersionUID = -3218202098599605151L;

    public Hl7CreatingMessageException(Throwable e) {
        super(e);
    }

    public Hl7CreatingMessageException() {
        super();
    }
}
