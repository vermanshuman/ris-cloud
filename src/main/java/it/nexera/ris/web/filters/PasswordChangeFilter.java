package it.nexera.ris.web.filters;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.ApplicationSettingsValueWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class PasswordChangeFilter extends BaseFilter {
    private static final long MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;

    private static final String PASSWORD_EXPIRED_PARAM = "password_expired";

    /* (non-Javadoc)
     * @see it.nexera.web.filters.BaseFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)
                && !(response instanceof HttpServletResponse)) {
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getHeader("faces-request") != null
                && httpRequest.getHeader("faces-request").equalsIgnoreCase(
                "partial/ajax")) {
            super.doFilter(httpRequest, httpResponse, chain);
            return;
        }

        if (httpRequest.getRequestURI().contains(".css.jsf")
                || httpRequest.getRequestURI().contains(".js.jsf")
                || httpRequest.getRequestURI().contains(".png.jsf")
                || httpRequest.getRequestURI().contains(".gif.jsf")
                || httpRequest.getRequestURI().contains(".ico.jsf")
                || httpRequest.getRequestURI().endsWith(".css")
                || httpRequest.getRequestURI().endsWith(".js")
                || httpRequest.getRequestURI().endsWith(".png")
                || httpRequest.getRequestURI().endsWith(".gif")
                || httpRequest.getRequestURI().endsWith(".ico")
                || this.getFacesContext(httpRequest, httpResponse).isPostback()) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        PageTypes pageType = getPageTypeByRequest(httpRequest);
        if (pageType != null && (
                (pageType.equals(PageTypes.USER_PROFILE_VIEW)
                        && httpRequest.getParameterMap().containsKey(PASSWORD_EXPIRED_PARAM))
                || pageType.equals(PageTypes.HOME))) {
            super.doFilter(httpRequest, httpResponse, chain);
            return;
        }

        UserWrapper currentUser = this.getCurrentUser(httpRequest, httpResponse);

        if (currentUser != null) {
            if(currentUser.getNeedToChangePassword()){
                redirectUserForPasswordChange(httpRequest, httpResponse);
                return;
            }

            ApplicationSettingsValueWrapper passwordExpirationSettings = ApplicationSettingsHolder
                    .getInstance().getByKey(
                            ApplicationSettingsKeys.PASSWORD_EXPIRATION_PERIOD);

            if (passwordExpirationSettings != null) {
                Integer passwordActivePeriodDays = null;
                try {
                    passwordActivePeriodDays = Integer
                            .parseInt(passwordExpirationSettings.getValue());
                } catch (NumberFormatException e) {
                }

                if (passwordActivePeriodDays != null
                        && passwordActivePeriodDays > 0) {
                    if (currentUser.getPasswordChangeDate() != null) {
                        Date lastPasswordChangeDate = currentUser
                                .getPasswordChangeDate();
                        long diffMs = new Date().getTime()
                                - lastPasswordChangeDate.getTime();
                        float days = diffMs / MILLISECONDS_IN_DAY;

                        if (days >= passwordActivePeriodDays) {
                            redirectUserForPasswordChange(httpRequest,
                                    httpResponse);
                            return;
                        }
                    } else {
                        redirectUserForPasswordChange(httpRequest, httpResponse);
                        return;
                    }

                    super.doFilter(httpRequest, httpResponse, chain);
                } else {
                    super.doFilter(httpRequest, httpResponse, chain);
                }
            } else {
                super.doFilter(httpRequest, httpResponse, chain);
            }
        } else {
            super.doFilter(httpRequest, httpResponse, chain);
        }
    }

    private void redirectUserForPasswordChange(HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        this.doRedirectToPage(httpRequest, httpResponse, PageTypes.USER_PROFILE_VIEW,
                PASSWORD_EXPIRED_PARAM);
    }
}
