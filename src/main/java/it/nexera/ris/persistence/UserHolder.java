package it.nexera.ris.persistence;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.security.api.UserDetailsImpl;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class UserHolder implements Serializable {
    private static final long serialVersionUID = 2665946832720090301L;

    public static final String USER_HOLDER_ATTRIBUTE = "USER_HOLDER_ATTRIBUTE";

    public static final String SESSION_TIME_ATTRIBUTE = "SESSION_TIME_ATTRIBUTE";

    public transient final Logger log = LogManager.getLogger(getClass());

    private static UserHolder instance;

    public static synchronized UserHolder getInstance() {
        if (instance == null) {
            instance = new UserHolder();
        }

        return instance;
    }

    public UserWrapper getCurrentUser() {
        if (FacesContext.getCurrentInstance() != null) {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();
            UserWrapper user = (UserWrapper) request.getSession().getAttribute(
                    USER_HOLDER_ATTRIBUTE);
            if (user == null
                    && SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();

                if (principal instanceof UserDetailsImpl) {
                    UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                    try {
                        user = UserWrapper.wrap(
                                this.loadUser(userDetails.getUsername()),
                                DaoManager.getSession());

                        setCurrentUser(user);
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                }
            }

            return user;
        } else {
            throw new IllegalStateException();
        }
    }

    public UserWrapper getCurrentUser(ServletRequest request) {
        if (request != null) {
            UserWrapper user = (UserWrapper) ((HttpServletRequest) request)
                    .getSession().getAttribute(USER_HOLDER_ATTRIBUTE);

            if (user == null
                    && SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();

                if (principal instanceof UserDetailsImpl) {
                    UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                    try {
                        user = UserWrapper.wrap(
                                this.loadUser(userDetails.getUsername()),
                                DaoManager.getSession());

                        setCurrentUser(user, request);
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                }
            }
            return user;
        } else {
            throw new IllegalStateException();
        }
    }

    public void setCurrentUser(UserWrapper user) {
        if (FacesContext.getCurrentInstance() != null) {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();

            request.getSession().setAttribute(USER_HOLDER_ATTRIBUTE, user);

            if (request.getSession()
                    .getAttribute(SESSION_TIME_ATTRIBUTE) == null) {
                request.getSession().setAttribute(SESSION_TIME_ATTRIBUTE,
                        request.getSession().getMaxInactiveInterval());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public void setCurrentUser(UserWrapper user, ServletRequest request) {
        if (request != null) {
            ((HttpServletRequest) request).getSession().setAttribute(
                    USER_HOLDER_ATTRIBUTE, user);

            if (((HttpServletRequest) request).getSession()
                    .getAttribute(SESSION_TIME_ATTRIBUTE) == null) {
                ((HttpServletRequest) request).getSession().setAttribute(
                        SESSION_TIME_ATTRIBUTE, ((HttpServletRequest) request)
                                .getSession().getMaxInactiveInterval());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public User loadUser(String username) throws Exception {
        User user = null;
        if (FacesContext.getCurrentInstance() != null) {
            user = DaoManager.get(User.class,
                    Restrictions.eq("login", username));
        } else {
            Session session = null;
            try {
                session = PersistenceSession.createSession();

                user = ConnectionManager.get(User.class,
                        Restrictions.eq("login", username), session);
            } catch (Exception e) {
                throw e;
            } finally {
                if (session != null) {
                    session.clear();
                    session.close();
                }
            }
        }

        return user;
    }
}
