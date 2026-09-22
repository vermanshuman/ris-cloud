package it.nexera.ris.web.handlers;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import java.io.IOException;
import java.util.Locale;

public class ViewHandlerEx extends ViewHandler {
    private ViewHandler parent;

    public ViewHandlerEx(ViewHandler parent) {
        //System.out.println("CustomViewHandler.CustomViewHandler():Parent View Handler:"+parent.getClass());
        this.parent = parent;
    }

    @Override
    public UIViewRoot restoreView(FacesContext facesContext, String viewId) {
        /**
         * {@link javax.faces.application.ViewExpiredException}. This happens only  when we try to logout from timed out pages.
         */
        UIViewRoot root = null;
        root = parent.restoreView(facesContext, viewId);
        if (root == null) {
            root = createView(facesContext, viewId);
        }
        return root;
    }

    @Override
    public Locale calculateLocale(FacesContext facesContext) {
        return parent.calculateLocale(facesContext);
    }

    @Override
    public String calculateRenderKitId(FacesContext facesContext) {
        String renderKitId = parent.calculateRenderKitId(facesContext);
        //System.out.println("CustomViewHandler.calculateRenderKitId():RenderKitId: "+renderKitId);
        return renderKitId;
    }

    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(
            FacesContext context, String viewId) {
        return parent.getViewDeclarationLanguage(context, viewId);
    }

    @Override
    public UIViewRoot createView(FacesContext facesContext, String viewId) {
        return parent.createView(facesContext, viewId);
    }

    @Override
    public String getActionURL(FacesContext facesContext, String actionId) {
        return parent.getActionURL(facesContext, actionId);
    }

    @Override
    public String getResourceURL(FacesContext facesContext, String resId) {
        return parent.getResourceURL(facesContext, resId);
    }

    @Override
    public void renderView(FacesContext facesContext, UIViewRoot viewId)
            throws IOException, FacesException {
        parent.renderView(facesContext, viewId);
    }

    @Override
    public void writeState(FacesContext facesContext) throws IOException {
        parent.writeState(facesContext);
    }

    public ViewHandler getParent() {
        return parent;
    }

    @Override
    public String getWebsocketURL(FacesContext context, String channel) {
        return parent.getWebsocketURL(context, channel);
    }
}
