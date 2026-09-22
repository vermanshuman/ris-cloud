package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.PageTypes;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

public class RedirectHelper extends BaseHelper {
    public static final String ID_PARAMETER = "id";

    public static final String PARENT_ID_PARAMETER = "refferentId";

    public static final String FROM_PARAMETER = "from";

    public static void goTo(PageTypes type) {
        try {
            if (type != null) {
                sendRedirect(type.getPagesContext());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goTo(PageTypes type, HttpServletRequest request,
                            HttpServletResponse response) {
        try {
            if (type != null) {
                sendRedirect(type.getPagesContext(), request, response);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goTo(PageTypes type, Serializable id) {
        try {
            sendRedirect(type.getPagesContext() + "?" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goTo(PageTypes type, Serializable id, boolean newTab) {
        try {
            sendRedirect(type.getPagesContext() + "?" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()), newTab);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goTo(PageTypes type, Serializable parentId,
                            Serializable id) {
        try {
            sendRedirect(type.getPagesContext() + "?" + PARENT_ID_PARAMETER
                    + "=" + parentId + "&" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    /**
     * Sends the redirect to another page.
     *
     * @param url
     * @throws IOException
     */
    public static void sendRedirect(String url, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        String createdUrl = createUrl(request, url, true);
        response.sendRedirect(createdUrl);
    }

    /**
     * Sends the redirect to another page.
     * newTab only for POST requests
     *
     * @param url
     * @param newTab
     * @throws IOException
     */
    public static void sendRedirect(String url, boolean newTab)
            throws IOException {
        String createdUrl = createUrl(null, url, true);

        if (newTab) {
            PFRequestContextHelper.executeJS(String.format(
                    "window.open('%s', '_newtab')", createdUrl));
        } else {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(createdUrl);
        }
    }

    public static void sendRedirectForMultipleTabs(PageTypes type, Serializable id) {
        String idStr = id == null ? "" : id.toString();
        String url = type.getPagesContext() + "?" + ID_PARAMETER + "=" + idStr;
        String createdUrl = createUrl(null, url, true);
        PFRequestContextHelper.executeJS(String.format(
                "window.open('%s', '%s')", createdUrl, idStr));
    }

    public static void sendRedirect(String url) throws IOException {
        String createdUrl = createUrl(null, url, true);

        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext.getResponseComplete()) {
            return;
        }

        facesContext.getExternalContext().redirect(createdUrl);
    }

    public static void sendRedirectWithoutAppContextname(String url)
            throws IOException {
        String createdUrl = createUrl(null, url, false);
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect(createdUrl);
    }

    public static String createUrl(HttpServletRequest request, String url,
                                   boolean withApplicationContextName) {
        if (request == null) {
            request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
        }
        StringBuilder sb = new StringBuilder("");

        sb.append(request.getRequestURL().substring(0,
                request.getRequestURL().indexOf(request.getContextPath())));

        if (withApplicationContextName) {
            sb.append(request.getContextPath());
        }

        if (!url.startsWith("/")) {
            sb.append("/");
        }

        sb.append(url);
        return sb.toString();
    }

    public static void addRequestError(boolean isTms) {
        if (FacesContext.getCurrentInstance() != null) {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();
            request.setAttribute(isTms ? "tmsError" : "dbError",
                    ResourcesHelper.getString(isTms ? "loginCouldNotConnect"
                            : "dbCouldNotConnect"));
        }
    }
}
