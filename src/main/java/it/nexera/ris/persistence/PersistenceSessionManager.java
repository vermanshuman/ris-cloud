package it.nexera.ris.persistence;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

public final class PersistenceSessionManager {
    public static String HibernateSessionAttribute = "HibernateSessionAttribute";

    private static PersistenceSessionManager instance = new PersistenceSessionManager();

    private PersistenceSession instanceBean = null;

    private PersistenceSessionManager() {
    }

    public static PersistenceSessionManager getInstance() {
        return instance;
    }

    public PersistenceSession bean() {
        if (FacesContext.getCurrentInstance() != null) {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();

            if (request.getAttribute(HibernateSessionAttribute) == null) {
                request.setAttribute(HibernateSessionAttribute,
                        new PersistenceSession());
            }

            this.instanceBean = (PersistenceSession) request
                    .getAttribute(HibernateSessionAttribute);

            return this.instanceBean;
        } else {
            if (instanceBean == null) {
                instanceBean = new PersistenceSession();
            }
            return instanceBean;
        }
    }

    public static PersistenceSession getBean() {
        return instance.bean();
    }
}
