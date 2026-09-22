package it.nexera.ris.common.security.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.web.beans.BaseValidationPageBean;
import it.nexera.ris.web.filters.SessionTimeoutFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Date;

@Named("loginBean")
@RequestScoped
public class LoginBean extends BaseValidationPageBean {
    protected transient final Logger log = LogManager.getLogger(LoginBean.class);

    @Inject
    private PasswordEncoder passwordEncoder;

    private String password;

    private String username;

    @Override
    protected void onConstruct() {
        if (getRequestParameter("showLogoutDlg") != null) {
            executeJS("PF('invalidateSessionDlgWV').show();");
        } else if (getHttpSession().getAttribute(SessionTimeoutFilter.SHOW_LOGOUT_ON_LOGIN_WINDOW_SESSION_ATTRIBUTE) != null
                && ((Boolean) getHttpSession().getAttribute(SessionTimeoutFilter.SHOW_LOGOUT_ON_LOGIN_WINDOW_SESSION_ATTRIBUTE))) {
            RedirectHelper.goTo(PageTypes.LOGOUT);
        } else if (getCurrentUser() != null) {
            getHttpSession().setAttribute(SessionTimeoutFilter.SHOW_LOGOUT_WINDOW_SESSION_ATTRIBUTE, Boolean.FALSE);
            RedirectHelper.goTo(PageTypes.WORKLIST);
        } else if (SessionHelper.get("authFail") != null) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("wrongLoginOrPasswordInfo"), "");
        }

        if (getHttpSession() != null && getHttpSession()
                .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) != null) {
            handleAuthenticationException((AuthenticationException) getHttpSession()
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION));
        }
    }

    public void doLogin() throws IOException, ServletException {
        ExternalContext context = FacesContext.getCurrentInstance()
                .getExternalContext();
        RequestDispatcher dispatcher = ((ServletRequest) context.getRequest())
                .getRequestDispatcher("/j_spring_security_check");

        dispatcher.forward((ServletRequest) context.getRequest(),
                (ServletResponse) context.getResponse());

        String oldSessionId = (String) SessionHelper.get("my_session_id");
        if (ValidationHelper.isNullOrEmpty(getCurrentUser()) || ValidationHelper.isNullOrEmpty(oldSessionId)) {
            SessionHelper.put(true, "authFail");
        } else {
            StringBuffer sb = new StringBuffer(oldSessionId);
            sb.append("__");
            sb.append(getCurrentUser().getId());
            SessionHelper.removeObject("my_session_id");
            SessionHelper.put(sb.toString(), "my_session_id");
            FacesContext.getCurrentInstance().responseComplete();
            saveLoginDate();
        }
    }

    private void handleAuthenticationException(AuthenticationException exception) {
        if (exception instanceof DisabledException) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("validationFailed"),
                    ResourcesHelper.getValidation("wrongLoginStatus"));
        }
    }

    private void saveLoginDate() {
        try {
            User currentUser = DaoManager.get(User.class, getCurrentUser().getId());
            if (currentUser != null) {
                currentUser.setLastLoginDate(new Date());
                DaoManager.save(currentUser, true);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
