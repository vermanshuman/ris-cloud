package it.nexera.ris.web.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.web.beans.menu.MenuBean;
import it.nexera.ris.web.beans.session.SessionBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.cfg.NotYetImplementedException;
import org.primefaces.component.tabview.TabView;

import javax.annotation.PostConstruct;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlForm;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public abstract class PageBean {

    @PostConstruct
    private void init() {
        if (!this.isLoggedIn()) {
            return;
        }
        if (!this.isPostback()) {
            if (!(this instanceof MenuBean)) {
                this.getViewState().clear();
            }

            this.saveParameters();
        }

        onConstruct();
    }

    protected void clearSession() {
        try {
            DaoManager.getSession().flush();
            DaoManager.getSession().clear();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    protected abstract void onConstruct();

    private HashMap<String, Integer> tabs;

    protected transient final Logger log = LogManager.getLogger(getClass());

    public boolean isLoggedIn() {
        return org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication() != null;
    }

    /**
     * Gets the Session map to srote object over session of the application.
     *
     * @return
     */
    public Map<String, Object> getSession() {
        SessionManager sm = SessionManager.getInstance();

        if (sm != null) {
            SessionBean sessionBean = sm.getSessionBean();

            if (sessionBean != null) {
                if (sessionBean.getSession() != null) {
                    return sessionBean.getSession();
                } else {
                    RedirectHelper.goTo(PageTypes.LOGIN);
                    return null;
                }
            } else {
                RedirectHelper.goTo(PageTypes.LOGIN);
                return null;
            }
        } else {
            RedirectHelper.goTo(PageTypes.LOGIN);
            return null;
        }
    }

    public Map<String, Object> getApplicationMap() {
        if (FacesContext.getCurrentInstance() != null
                && FacesContext.getCurrentInstance().getExternalContext() != null
                && FacesContext.getCurrentInstance().getExternalContext()
                .getApplicationMap() != null) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getApplicationMap();
        } else {
            RedirectHelper.goTo(PageTypes.LOGIN);
            return null;
        }
    }

    private void saveParameters() {
        Enumeration<String> enums = this.getRequest().getParameterNames();
        String item = null;
        while (enums.hasMoreElements()) {
            item = enums.nextElement();
            this.getViewState().put(item, this.getRequestParameter(item));
        }
    }

    /**
     * Gets the Session map to srote object over page of the application.
     * This object will be killer when page changes.
     *
     * @return
     */
    public Map<String, Object> getViewState() {
        return SessionManager.getInstance().getSessionBean().getViewState();
    }

    public UserWrapper getCurrentUser() {
        return UserHolder.getInstance().getCurrentUser();
    }

    /**
     * Gets the sessions class that is present through the application.
     *
     * @return
     */
    public SessionBean getSessionBean() {
        return SessionManager.getInstance().getSessionBean();
    }

    /**
     * Gets the current http sessions.
     *
     * @return
     */
    public HttpSession getHttpSession() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);

        return session;
    }

    /**
     * Gets the current request url.
     *
     * @return
     */
    public String getRequestUrl() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) ctx
                .getExternalContext().getRequest();

        String path = request.getServletPath();
        return path;
    }

    public String getEditingEntityId() {
        if (!isPostback()) {
            return this.getRequestParameter(RedirectHelper.ID_PARAMETER);
        } else {
            if (ValidationHelper.isNullOrEmpty(this
                    .getRequestParameter(RedirectHelper.ID_PARAMETER))) {
                return this.getViewState().get(RedirectHelper.ID_PARAMETER) == null ? null
                        : (String) this.getViewState().get(
                        RedirectHelper.ID_PARAMETER);
            }

            return this.getRequestParameter(RedirectHelper.ID_PARAMETER);
        }
    }

    public void setEditingEntityId(String editingEntityId) {

    }

    public String getEditingEntityParentId() {
        if (!isPostback()) {
            return this.getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        } else {
            if (ValidationHelper.isNullOrEmpty(this
                    .getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER))) {
                return this.getViewState().get(
                        RedirectHelper.PARENT_ID_PARAMETER) == null ? null
                        : (String) this.getViewState().get(
                        RedirectHelper.PARENT_ID_PARAMETER);
            }

            return this.getRequestParameter(RedirectHelper.PARENT_ID_PARAMETER);
        }
    }

    public String getFromParam() {
        if (!isPostback()) {
            return this.getRequestParameter(RedirectHelper.FROM_PARAMETER);
        } else {
            if (ValidationHelper.isNullOrEmpty(this
                    .getRequestParameter(RedirectHelper.FROM_PARAMETER))) {
                return this.getViewState().get(RedirectHelper.FROM_PARAMETER) == null ? null
                        : (String) this.getViewState().get(
                        RedirectHelper.FROM_PARAMETER);
            }

            return this.getRequestParameter(RedirectHelper.FROM_PARAMETER);
        }
    }

    public String getRequestParameter(String name) {
        return this.getRequest().getParameter(name);
    }

    public HttpServletRequest getRequest() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return (HttpServletRequest) ctx.getExternalContext().getRequest();
    }

    public FacesContext getContext() {
        return FacesContext.getCurrentInstance();
    }

    public boolean isPostback() {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        return facesContext.isPostback();
    }

    public UIComponent getComponentById(String id) {
        UIComponent component = this.getContext().getViewRoot()
                .findComponent(id);

        return component;
    }

    public void addFieldExeption(String id, String message) {
        id = fixComponentId(id);
        this.addFieldExeption(this.getComponentById(id), message);
    }

    public void addException(String message) {
        this.setValidationFailed(true);
        this.getExceptions().add(ResourcesHelper.getValidation(message));
    }

    public void markInvalid(String id, String message) {
        id = fixComponentId(id);
        UIComponent component = this.getComponentById(id);
        markInvalid(component, message);
    }

    public void markInvalid(UIComponent component, String message) {
        this.getMarkedIvalidFields().add(completeId(component));
        ValidatorHelper.markNotValid(component,
                ResourcesHelper.getValidation(message), this.getContext(),
                this.getTabs());
    }

    public void addFieldExeption(UIComponent component, String message) {
        this.getMarkedIvalidFields().add(completeId(component));
        this.setValidationFailed(true);
        ValidatorHelper.markNotValid(component,
                ResourcesHelper.getValidation(message), this.getContext(),
                this.getTabs());
        this.getExceptions().add(ResourcesHelper.getValidation(message));
    }

    public void addRequiredFieldExeption(String id) {
        id = fixComponentId(id);
        this.addRequiredFieldExeption(this.getComponentById(id));
    }

    public void addRequiredFieldExeption(String id, String labelId) {
        id = fixComponentId(id);
        this.addRequiredFieldExeption(this.getComponentById(id));
        this.getContext().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(
                        "%s %s", ResourcesHelper.getValidation(labelId),
                        " is required field!"), null));
    }

    public String fixComponentId(String id) {
        if (!id.startsWith("form:")) {
            id = "form:" + id;
        }
        return id;
    }

    private String completeId(UIComponent component) {
        StringBuilder sb = new StringBuilder();
        UIComponent parent = component.getParent();
        while (parent != null) {
            if (parent.getClass().equals(TabView.class)
                    || parent.getClass().equals(HtmlForm.class)
                    || UIComponent.isCompositeComponent(parent)) {
                sb.insert(0, String.format("%s:", parent.getId()));
            }

            parent = parent.getParent();
        }
        sb.append(component.getId());
        return sb.toString();
    }

    public void addRequiredFieldExeption(UIComponent component) {
        try {
            Method method = null;

            try {
                method = component.getClass().getDeclaredMethod("getLabel",
                        new Class[0]);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            if (method == null) {
                try {
                    method = component.getClass().getMethod("getLabel",
                            new Class[0]);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }

            if (method != null) {
                String label = (String) method.invoke(component, new Object[0]);
                if (!ValidationHelper.isNullOrEmpty(label)) {
                    this.getExceptions()
                            .add(String.format("%s %s", label,
                                    "is required field!"));
                    this.setValidationFailed(true);
                    this.markInvalid(component, "requiredField");
                    return;
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        this.addFieldExeption(component, "requiredField");
    }

    public void cleanFieldExeption(String id) {
        ValidatorHelper.markValid(this.getComponentById(id), this.getContext(),
                this.getTabs());
    }

    public boolean getValidationFailed() {
        return this.getViewState().get("validateFail") == null ? false
                : (Boolean) this.getViewState().get("validateFail");
    }

    public void setValidationFailed(boolean value) {
        if ((this.getViewState().get("validateFail") == null || this
                .getViewState().get("validateFail") == Boolean.FALSE)
                && value == true) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("validationFailed"),
                    ResourcesHelper.getValidation("checkData"));
        }

        this.getViewState().put("validateFail", value);
    }

    public void setMarkedIvalidFields(List<String> list) {
        this.getViewState().put("notValidFields", list);
    }

    @SuppressWarnings("unchecked")
    public List<String> getMarkedIvalidFields() {
        if (this.getViewState().get("notValidFields") == null) {
            this.getViewState().put("notValidFields", new ArrayList<String>());
        }

        return (List<String>) this.getViewState().get("notValidFields");
    }

    public void cleanValidation() {
        this.setTabs(null);
        for (String id : getMarkedIvalidFields()) {
            try {
                ValidatorHelper.markValid(getComponentById(fixComponentId(id)),
                        this.getContext(), this.getTabs());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        this.setValidationFailed(false);
        this.setMarkedIvalidFields(new ArrayList<String>());
        this.setExceptions(new ArrayList<String>());
    }

    public void setTabs(HashMap<String, Integer> tabs) {
        this.tabs = tabs;
    }

    public HashMap<String, Integer> getTabs() {
        if (tabs == null) {
            tabs = new HashMap<String, Integer>();
        }
        return tabs;
    }

    public PageTypes getCurrentPage() {
        return PageTypes.getPageTypeByPath(this.getRequestUrl());
    }

    public String getCurrentPageCode() {
        throw new NotYetImplementedException();
    }

    public String getCurrentMenuItem() {
        return this.getCurrentPage().getMenuItem().getMenuItemName();
    }

    @SuppressWarnings("unchecked")
    protected <T> T getManagedBean(String beanName, Class<T> calzz) {
        ELContext elCtx = this.getContext().getELContext();
        ExpressionFactory ef = this.getContext().getApplication()
                .getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elCtx, beanName, calzz);
        return (T) ve.getValue(elCtx);
    }

    @SuppressWarnings("unchecked")
    protected <T> T executeEl(String el, Class<T> calzz) {
        ELContext elCtx = this.getContext().getELContext();
        ExpressionFactory ef = this.getContext().getApplication()
                .getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elCtx, el, calzz);
        return (T) ve.getValue(elCtx);
    }

    public String getUniqueId() {
        return this.getContext().getViewRoot().createUniqueId();
    }

    public void executeJS(String str) {
        PFRequestContextHelper.executeJS(str);
    }

    public String getCookie(String name) {
        for (Cookie cookie : this.getRequest().getCookies()) {
            if (cookie.getName().equalsIgnoreCase(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getExceptions() {
        if (this.getViewState().get("exceptions") == null) {
            this.getViewState().put("exceptions", new ArrayList<String>());
        }
        return (List<String>) this.getViewState().get("exceptions");
    }

    public void setExceptions(List<String> exceptions) {
        this.getViewState().put("exceptions", exceptions);
    }

    public String getCurrentIP() {
        InetAddress ip = null;

        try {
            StringBuffer sb = new StringBuffer();

            sb.append("Current server IP: ");
            ip = InetAddress.getLocalHost();

            if (ip != null) {
                sb.append(ip.getHostAddress());
            } else {
                sb.append("unknown");
            }

            return sb.toString();
        } catch (UnknownHostException e) {
            LogHelper.log(log, e);
        }

        return null;
    }

}
