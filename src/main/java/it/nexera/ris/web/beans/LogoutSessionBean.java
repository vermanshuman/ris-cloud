package it.nexera.ris.web.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.HttpSessionCollector;
import it.nexera.ris.web.filters.SessionTimeoutFilter;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

import static it.nexera.ris.common.helpers.LogoutHelper.showLogoutMessage;

@Named("logoutSessionBean")
@SessionScoped
public class LogoutSessionBean extends PageBean implements Serializable {

    private static final long serialVersionUID = -6558435211153703457L;

    private boolean needRendered;

    private String sessionLogoutId;

    @Override
    protected void onConstruct() {
        setNeedRendered(true);
    }

    public void showLogoutDlg() {
        showLogoutMessage();
    }

    public void closeSession() {
        HttpSessionCollector.closeSessionById(((HttpSession) FacesContext.getCurrentInstance().
                getExternalContext().getSession(false)).getId());
    }

    public void redirectOnLogout() {
        getHttpSession().setAttribute(SessionTimeoutFilter.NEED_LOGOUT_SESSION_ATTRIBUTE, Boolean.FALSE);
        getHttpSession().setAttribute(SessionTimeoutFilter.SHOW_LOGOUT_WINDOW_SESSION_ATTRIBUTE, Boolean.FALSE);
        getHttpSession().setAttribute(SessionTimeoutFilter.SHOW_LOGOUT_ON_LOGIN_WINDOW_SESSION_ATTRIBUTE, Boolean.TRUE);
        RedirectHelper.goTo(PageTypes.LOGIN);
    }

    public void checkNeedLogout() {
        if (!ValidationHelper.isNullOrEmpty(getSessionLogoutId())
                && getCurrentUser() != null && getCurrentUser().getId() != null
                && SessionHelper.get("my_session_id") != null) {
            String[] params = null;

            if (getSessionLogoutId().contains("pfpd")) {
                String sessionId = getSessionLogoutId().substring(0, getSessionLogoutId().lastIndexOf("\""));

                sessionId = sessionId.substring(sessionId.lastIndexOf("\"") + 1);

                params = sessionId.split("__separator__");
            } else {
                params = getSessionLogoutId().split("__separator__");
            }

            if (params != null && params.length == 2) {
                String userId = params[0];
                String sessionId = params[1];

                if (!ValidationHelper.isNullOrEmpty(userId) && !ValidationHelper.isNullOrEmpty(sessionId)
                        && getCurrentUser().getId().equals(Long.parseLong(userId))) {
                    if ("show_logout_dlg".equals(sessionId)
                            || !sessionId.equals((String) SessionHelper.get("my_session_id"))) {
                        showLogoutDlg();
                    }
                }
            }
        }

        setSessionLogoutId(null);
    }

    public boolean isNeedRendered() {
        return needRendered;
    }

    public void setNeedRendered(boolean needRendered) {
        this.needRendered = needRendered;
    }

    public String getSessionLogoutId() {
        return sessionLogoutId;
    }

    public void setSessionLogoutId(String sessionLogoutId) {
        this.sessionLogoutId = sessionLogoutId;
    }

}
