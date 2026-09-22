package it.nexera.ris.web.beans.wrappers;

import java.io.Serializable;

public class ExceptionWrapper implements Serializable {
    private static final long serialVersionUID = -4834856833092429760L;

    private String message;

    private String stackStace;

    public ExceptionWrapper(String msg, String stackStace) {
        setMessage(msg);
        setStackStace(stackStace);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getMessage();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackStace() {
        return stackStace;
    }

    public void setStackStace(String stackStace) {
        this.stackStace = stackStace;
    }
}
