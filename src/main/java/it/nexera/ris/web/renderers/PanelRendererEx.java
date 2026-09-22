package it.nexera.ris.web.renderers;

import it.nexera.ris.common.helpers.XSSCleaner;
import org.primefaces.component.menu.Menu;
import org.primefaces.component.panel.Panel;
import org.primefaces.component.panel.PanelRenderer;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

public class PanelRendererEx extends PanelRenderer {
    @Override
    protected void encodeHeader(FacesContext context, Panel panel, Menu optionsMenu)
            throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIComponent header = panel.getFacet("header");
        String headerText = panel.getHeader();
        String clientId = panel.getClientId(context);

        if (headerText == null && header == null) {
            return;
        }

        writer.startElement("div", null);
        writer.writeAttribute("id", panel.getClientId(context) + "_header",
                null);
        writer.writeAttribute("class", Panel.PANEL_TITLEBAR_CLASS, null);

        //Title
        writer.startElement("span", null);
        writer.writeAttribute("class", Panel.PANEL_TITLE_CLASS, null);

        if (header != null) {
            renderChild(context, header);
        } else if (headerText != null) {
            writer.write(XSSCleaner.cleanXSS(headerText));
        }

        writer.endElement("span");

        //Options
        if (panel.isClosable()) {
            encodeIcon(context, panel, "ui-icon-closethick", clientId
                    + "_closer", panel.getCloseTitle(),panel.getCloseTitle());
        }

        if (panel.isToggleable()) {
            String icon = panel.isCollapsed() ? "ui-icon-plusthick"
                    : "ui-icon-minusthick";
            encodeIcon(context, panel, icon, clientId + "_toggler",
                    panel.getToggleTitle(), panel.getToggleTitle());
        }

        if (optionsMenu != null) {
            encodeIcon(context, panel, "ui-icon-gear", clientId + "_menu", panel.getMenuTitle(), panel.getMenuTitle());
        }

        //Actions
        UIComponent actionsFacet = panel.getFacet("actions");
        if (actionsFacet != null) {
            actionsFacet.encodeAll(context);
        }

        writer.endElement("div");
    }
}
