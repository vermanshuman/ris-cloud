package it.nexera.ris.common.helpers;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

public class MessageHelper {

    public static void addGlobalMessage(Severity severity, String title,
                                        String message) {
        FacesMessage msg = new FacesMessage(title, message);
        msg.setSeverity(severity);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

}
