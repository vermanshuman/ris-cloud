package it.nexera.ris.web.handlers;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.web.beans.wrappers.ExceptionWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExceptionHandlerEx extends ExceptionHandlerWrapper {

    private static final Logger log = LogManager.getLogger(ExceptionHandlerEx.class);

    public ExceptionHandlerEx(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();
        if (i.hasNext()) {
            ExceptionQueuedEvent exceptionQueuedEvent = i.next();

            ExceptionQueuedEventContext exceptionQueuedEventContext =
                    (ExceptionQueuedEventContext) exceptionQueuedEvent.getSource();

            Throwable throwable = exceptionQueuedEventContext.getException();
            PageTypes redirectPage = null;

            try {
                if (throwable != null) {
                    Throwable t = throwable;

                    LogHelper.log(log, t);

                    FacesContext facesContext = FacesContext
                            .getCurrentInstance();

                    List<ExceptionWrapper> list = new ArrayList<>();

                    while (t.getCause() != t) {
                        list.add(new ExceptionWrapper(t.getMessage(), LogHelper.readStackTrace(t)));
                        if (t.getCause() == null) {
                            break;
                        }
                        t = t.getCause();
                    }
                    facesContext.getExternalContext().getSessionMap().put("exceptionList", list);

                    redirectPage = PageTypes.ERROR;
                }
            } finally {
                i.remove();
            }

            RedirectHelper.goTo(redirectPage);
        }
        getWrapped().handle();
    }

}
