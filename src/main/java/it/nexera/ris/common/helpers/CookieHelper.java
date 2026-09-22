package it.nexera.ris.common.helpers;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieHelper {
    public static Cookie getCookie(String name) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();

        if (httpServletRequest.getCookies() != null) {
            for (Cookie c : httpServletRequest.getCookies()) {
                if (c.getName().equals(name)) {
                    return c;
                }
            }
        }
        return null;
    }

    public static void setCookie(String name, String value) {
        createCookie(name, value, getCookiePath(
                (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()));
    }

    public static void setGlobalCookie(String name, String value) {
        createCookie(name, value, "/");
    }

    private static void createCookie(String name, String value, String path) {
        HttpServletResponse response = (HttpServletResponse) FacesContext
                .getCurrentInstance().getExternalContext().getResponse();

        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(999999999);
        cookie.setPath(path);
        response.addCookie(cookie);
    }

    public static String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : "/";
    }
}
