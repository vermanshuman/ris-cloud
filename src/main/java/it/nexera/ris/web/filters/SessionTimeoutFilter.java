package it.nexera.ris.web.filters;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.web.beans.session.SessionBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import it.nexera.ris.web.common.AtmosphereUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionTimeoutFilter extends BaseFilter implements Filter {

    public static final String NEED_LOGOUT_SESSION_ATTRIBUTE = "needLogout";
    public static final String SHOW_LOGOUT_WINDOW_SESSION_ATTRIBUTE = "can_show_logout_dlg";
    public static final String SHOW_LOGOUT_ON_LOGIN_WINDOW_SESSION_ATTRIBUTE = "show_logout_on_login";

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {
        if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;

            HttpSession session = httpServletRequest.getSession(false);
            if (session != null) {

                SessionBean sb = null;
                try {
                    sb = getSessionBean(httpServletRequest);
                } catch (Exception e) {

                }
                UserWrapper user = (UserWrapper) session.getAttribute(UserHolder.USER_HOLDER_ATTRIBUTE);
                PageTypes page = PageTypes.getPageTypeByPath(httpServletRequest.getRequestURI());

                if (sb != null) {
                    if (PageTypes.DOCUMENT_GENERATION.equals(page)) {
                        session.setMaxInactiveInterval(-1);
                        session.setAttribute("ON_DOCUMENT_GENERATION", Boolean.TRUE);
                    } else if (session.getAttribute(UserHolder.SESSION_TIME_ATTRIBUTE) != null) {
                        session.setMaxInactiveInterval((int) httpServletRequest.getSession().getAttribute(
                                UserHolder.SESSION_TIME_ATTRIBUTE));
                        session.setAttribute("ON_DOCUMENT_GENERATION", Boolean.FALSE);
                    }

                    if ((httpServletRequest.getParameter("reason") != null && httpServletRequest
                            .getParameter("reason").equalsIgnoreCase("expired"))) {
                        UserHolder.getInstance().setCurrentUser(null, request);
                        if (sb.getSession() != null && user != null) {
                            sb.getSession().clear();
                        }
                        session.invalidate();
                    }
                }

                // is session invalid?
                if (this.isSessionInvalid(httpServletRequest)
                        && !httpServletRequest.getRequestURI().contains(".css.jsf")
                        && !httpServletRequest.getRequestURI().contains(".js.jsf")
                        && !httpServletRequest.getRequestURI().contains(".png.jsf")
                        && httpServletRequest.getRequestURI().contains(".jsf")) {
                    RedirectHelper.goTo(PageTypes.LOGIN, httpServletRequest,
                            httpServletResponse);
                    return;
                }

                filterChain.doFilter(request, response);

                if (user != null && PageTypes.LOGIN.equals(page) && httpServletRequest.isRequestedSessionIdValid()) {
                    if (session.getAttribute(NEED_LOGOUT_SESSION_ATTRIBUTE) != null
                            && ((Boolean) session.getAttribute(NEED_LOGOUT_SESSION_ATTRIBUTE))) {
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        SecurityContextLogoutHandler ctxLogOut = new SecurityContextLogoutHandler();
                        ctxLogOut.logout(httpServletRequest, httpServletResponse, auth);
                    } else if (session.getAttribute(SHOW_LOGOUT_WINDOW_SESSION_ATTRIBUTE) == null
                            || ((Boolean) session.getAttribute(SHOW_LOGOUT_WINDOW_SESSION_ATTRIBUTE))) {
                        session.setAttribute(SHOW_LOGOUT_WINDOW_SESSION_ATTRIBUTE, Boolean.FALSE);
                        StringBuffer strb = new StringBuffer();

                        strb.append(user.getId());
                        strb.append("__separator__");
                        strb.append("" + SHOW_LOGOUT_WINDOW_SESSION_ATTRIBUTE);

                        AtmosphereUtil.broadcastTo("/notify", strb.toString());
                    }
                }
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
        boolean sessionInValid = (httpServletRequest.getRequestedSessionId() != null)
                && !httpServletRequest.isRequestedSessionIdValid();
        return sessionInValid;
    }

    private SessionBean getSessionBean(HttpServletRequest httpServletRequest) {
        return ((SessionBean) httpServletRequest.getSession(false).getAttribute("sessionBean"));
    }

    public void destroy() {
    }

}
