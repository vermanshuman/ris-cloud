package it.nexera.ris.web.beans.session;

import it.nexera.ris.common.helpers.LogoutHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.web.beans.PageBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;

@Named("timeoutControlSessionBean")
@SessionScoped
public class TimeoutControlSessionBean extends PageBean implements Serializable {

    public static final int INTERVAL = 30;

    private boolean showWindow;

    @Override
    protected void onConstruct() {
    }


    public long getSessionInactiveInterval() {
        return countHttpSessionTimeInactive() - INTERVAL;
    }

    public long getTimerInterval() {
        return INTERVAL;
    }

    private long countHttpSessionTimeInactive() {
        return getHttpSession().getMaxInactiveInterval() -
                ((System.currentTimeMillis() - getHttpSession().getLastAccessedTime()) / 1000);
    }

    public void handleExpirition() {
        if (UserHolder.getInstance().getCurrentUser() != null) {
            setShowWindow(true);
        }
    }

    public void onTimerComplete() {
        LogoutHelper.showLogoutMessage();
    }

    public void submitSessionExtension() throws IOException {
        setShowWindow(false);
        RedirectHelper.sendRedirect(getRequestUrl());
    }

    public boolean isShowWindow() {
        return showWindow;
    }

    public void setShowWindow(boolean showWindow) {
        this.showWindow = showWindow;
    }
}
