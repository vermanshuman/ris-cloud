package it.nexera.ris.persistence;

import it.nexera.ris.web.beans.session.SessionBean;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

public class SessionManager {
    private static SessionManager instance = new SessionManager();

    private final static String STR_BEAN_NAME = "#{sessionBean}";

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return SessionManager.instance;
    }

    public SessionBean getSessionBean() {
        try {
            FacesContext fCtx = FacesContext.getCurrentInstance();
            ELContext elCtx = fCtx.getELContext();
            ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
            ValueExpression ve = ef.createValueExpression(elCtx, STR_BEAN_NAME,
                    SessionBean.class);
            SessionBean session = (SessionBean) ve.getValue(elCtx);
            if (session == null) {
                session = new SessionBean();
            }
            return session;
        } catch (Exception e) {
        }
        return null;
    }

}
