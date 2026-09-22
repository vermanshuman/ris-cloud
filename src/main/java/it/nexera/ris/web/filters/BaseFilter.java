package it.nexera.ris.web.filters;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

public class BaseFilter implements Filter {
    protected transient final Logger log = LogManager.getLogger(getClass());

    FilterConfig config;

    public void setFilterConfig(FilterConfig config) {
        this.config = config;
    }

    public FilterConfig getFilterConfig() {
        return config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {

        this.setFilterConfig(config);
    }

    public PageTypes getPageTypeByRequest(HttpServletRequest httpRequest) {
        String path = httpRequest.getRequestURI();
        String context = httpRequest.getContextPath();
        if (path.contains(context)) {
            int index = path.indexOf(context);
            path = path.substring(index + context.length(), path.length());

            PageTypes pageType = PageTypes.getPageTypeByPath(path);
            return pageType;
        }

        return null;
    }

    protected void doRedirectToPage(HttpServletRequest httpRequest,
                                    HttpServletResponse response, PageTypes page) {
        doRedirectToPage(httpRequest, response, page, null);
    }

    protected void doRedirectToPage(HttpServletRequest httpRequest,
                                    HttpServletResponse response, PageTypes page, String params) {
        if (response == null) {
            return;
        }

        try {
            StringBuilder sb = new StringBuilder("");
            int index = httpRequest.getRequestURL().indexOf(
                    httpRequest.getServletPath());

            if (index != -1) {
                sb.append(httpRequest.getRequestURL().substring(0, index));
            } else {
                sb.append(httpRequest.getContextPath());
            }

            if (!page.getPagesContext().startsWith("/")) {
                sb.append("/");
            }

            sb.append(page.getPagesContext());

            if (params != null) {
                sb.append("?");
                sb.append(params);
            }

            response.sendRedirect(sb.toString());
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
    }

    protected UserWrapper getCurrentUser(HttpServletRequest request,
                                         HttpServletResponse response) {
        return UserHolder.getInstance().getCurrentUser(request);
    }

    protected FacesContext getFacesContext(HttpServletRequest request,
                                           HttpServletResponse response) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {

            FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder
                    .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
                    .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lifecycleFactory
                    .getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

            facesContext = contextFactory.getFacesContext(request.getSession()
                    .getServletContext(), request, response, lifecycle);

            // Set using our inner class
            InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);

            // set a new viewRoot, otherwise context.getViewRoot returns null
            UIViewRoot view = facesContext.getApplication().getViewHandler()
                    .createView(facesContext, "");
            facesContext.setViewRoot(view);
        }
        return facesContext;
    }

    protected Application getApplication(FacesContext facesContext) {
        return facesContext.getApplication();
    }

    private abstract static class InnerFacesContext extends FacesContext {
        protected static void setFacesContextAsCurrentInstance(
                FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }

    protected void doRedirectToPage(HttpServletResponse response, PageTypes page) {
        if (response == null) {
            return;
        }

        try {
            response.sendRedirect(".." + page.getPagesContext());
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
    }

    public void resendRequest(HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> enums = httpRequest.getParameterNames();
        String item = null;
        while (enums.hasMoreElements()) {
            item = enums.nextElement();
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s", item,
                    httpRequest.getParameter(item)));
        }

        try {
            httpResponse.sendRedirect(httpRequest.getRequestURL().toString()
                    + (sb.length() > 0 ? String.format("?%s", sb.toString())
                    : ""));
        } catch (Exception e1) {
        }
    }
}
