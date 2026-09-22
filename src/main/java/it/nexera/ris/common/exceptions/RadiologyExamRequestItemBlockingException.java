package it.nexera.ris.common.exceptions;

public class RadiologyExamRequestItemBlockingException extends Exception {

    private static final long serialVersionUID = 518692599576575552L;

    public RadiologyExamRequestItemBlockingException(String message) {
        super(message);
    }

    public RadiologyExamRequestItemBlockingException() {
        super();
    }
}
