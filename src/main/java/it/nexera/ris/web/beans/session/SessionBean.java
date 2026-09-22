package it.nexera.ris.web.beans.session;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Named("sessionBean")
@SessionScoped
public class SessionBean implements Serializable {
    private static final long serialVersionUID = -3444727566938800223L;

    protected transient final Logger log = LogManager.getLogger(getClass());

    private transient Map<String, Object> Session = new HashMap<String, Object>();

    public static final boolean useNativeViewState = true;

    public SessionBean() {
        StringBuffer sb = new StringBuffer();

        sb.append(DateTimeHelper.toSessionTime(new Date()));
        Session.put("my_session_id", sb.toString());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getViewState() {
        if (useNativeViewState) {
            //            if (FacesContext.getCurrentInstance().getViewRoot().getViewMap()
            //                    .get("test_key") == null)
            //            {
            //                FacesContext.getCurrentInstance().getViewRoot().getViewMap()
            //                        .put("test_key", UUID.randomUUID().toString());
            //                System.out.println(FacesContext.getCurrentInstance()
            //                        .getViewRoot().getViewMap().get("test_key"));
            //            }
            return FacesContext.getCurrentInstance().getViewRoot().getViewMap();
        } else {
            if (Session == null) {
                Session = new HashMap<String, Object>();
            }
            if (Session.get(getRequestUrl()) == null) {
                Session.put(getRequestUrl(), new HashMap<String, Object>());
            }
            return (Map<String, Object>) Session.get(getRequestUrl());
        }
    }

    private String getRequestUrl() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) ctx
                .getExternalContext().getRequest();

        String path = request.getServletPath();
        return path;
    }

    public Map<String, Object> getSession() {
        try {
            if (Session == null) {
                Session = new HashMap<String, Object>();
            }
            return Session;
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    public UserWrapper getCurrentUser() {
        return UserHolder.getInstance().getCurrentUser();
    }

}
