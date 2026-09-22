package it.nexera.ris.web.filters;

import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.PersistenceSessionManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SessionFilter extends BaseFilter {

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getRequestURI().contains(".css.jsf")
                || httpRequest.getRequestURI().contains(".js.jsf")
                || httpRequest.getRequestURI().contains(".png.jsf")
                || httpRequest.getRequestURI().contains(".gif.jsf")
                || httpRequest.getRequestURI().contains(".ico.jsf")
                || httpRequest.getRequestURI().endsWith(".css")
                || httpRequest.getRequestURI().endsWith(".js")
                || httpRequest.getRequestURI().endsWith(".png")
                || httpRequest.getRequestURI().endsWith(".gif")
                || httpRequest.getRequestURI().endsWith(".ico")) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }
        try {
            openSession(request);

            chain.doFilter(request, response);
        } finally {
            closeSession(request);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    private void closeSession(ServletRequest request) {
        PersistenceSession ps = (PersistenceSession) request
                .getAttribute(PersistenceSessionManager.HibernateSessionAttribute);
        if (ps != null) {
            //            System.out.println("closing session... " + ps.toString());
            ps.closeSession();
            ps = null;
            request.setAttribute(
                    PersistenceSessionManager.HibernateSessionAttribute, ps);
        }
    }

    private void openSession(ServletRequest request) {
        PersistenceSession ps = (PersistenceSession) request
                .getAttribute(PersistenceSessionManager.HibernateSessionAttribute);
        if (ps != null) {
            //            System.out.println("closing session... " + ps.toString());
            ps.closeSession();
            ps = null;
        }
        ps = new PersistenceSession();
        //        System.out.println("opening session... " + ps.toString());
        request.setAttribute(
                PersistenceSessionManager.HibernateSessionAttribute, ps);
    }
}
