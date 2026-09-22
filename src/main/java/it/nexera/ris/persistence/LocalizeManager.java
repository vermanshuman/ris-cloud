package it.nexera.ris.persistence;

import it.nexera.ris.web.beans.base.LocalizeBean;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

public class LocalizeManager {
    private static LocalizeManager instance = new LocalizeManager();

    private final static String STR_BEAN_NAME = "#{localizeBean}";

    public static LocalizeManager getInstance() {
        return LocalizeManager.instance;
    }

    public LocalizeBean getLocalizeBean() {
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ELContext elCtx = fCtx.getELContext();
        ExpressionFactory ef = fCtx.getApplication().getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elCtx, STR_BEAN_NAME,
                LocalizeBean.class);
        LocalizeBean session = (LocalizeBean) ve.getValue(elCtx);
        return session;
    }
}
