package it.nexera.ris.common.security.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.UserPreferenceType;
import it.nexera.ris.common.helpers.CookieHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.security.api.UserDetailsImpl;
import it.nexera.ris.persistence.HttpSessionCollector;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.UserPreference;
import it.nexera.ris.web.beans.pages.UserProfileViewBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationSuccessHandlerEx implements AuthenticationSuccessHandler {

    public transient final Logger log = LogManager.getLogger(getClass());

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            try {
                User user = null;
                if (FacesContext.getCurrentInstance() != null) {
                    user = DaoManager
                            .get(User.class,
                                    Restrictions.eq("login",
                                            userDetails.getUsername()));

                    if (user != null) {
                        HttpSessionCollector.checkUniqueUser(user.getId());
                    }

                    UserHolder.getInstance().setCurrentUser(
                            new UserWrapper(user, DaoManager.getSession()),
                            request);
                } else {
                    Session session = null;
                    try {
                        session = PersistenceSession.createSession();

                        user = ConnectionManager.get(
                                User.class,
                                Restrictions.eq("login",
                                        userDetails.getUsername()), session);

                        if (user != null) {
                            HttpSessionCollector.checkUniqueUser(user.getId());
                        }

                        UserHolder.getInstance().setCurrentUser(
                                new UserWrapper(user, session), request);
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        if (session != null) {
                            session.clear();
                            session.close();
                        }
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        fillPdfZoomInformation();
        PageTypes page = PageTypes.getPageTypeByPath(request.getRequestURI());
        RedirectHelper.goTo(page == null ? PageTypes.WORKLIST : page,
                request, response);
    }

    private void fillPdfZoomInformation() {
        try {
            String pdfZoom = DaoManager.getField(UserPreference.class, "pdfZoom",
                    new Criterion[]{
                            Restrictions.eq("user.id", UserHolder.getInstance().getCurrentUser().getId()),
                            Restrictions.eq("type", UserPreferenceType.PDF_ZOOM)
                    }, null);
            if (ValidationHelper.isNullOrEmpty(pdfZoom)) {
                pdfZoom = "100";
            }
            CookieHelper.setCookie(UserProfileViewBean.ZOOM_COOKIE_PREFERENCE, pdfZoom);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

}
