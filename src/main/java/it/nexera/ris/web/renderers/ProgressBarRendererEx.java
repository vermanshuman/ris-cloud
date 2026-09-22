package it.nexera.ris.web.renderers;

import org.primefaces.component.progressbar.ProgressBar;
import org.primefaces.component.progressbar.ProgressBarRenderer;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

public class ProgressBarRendererEx extends ProgressBarRenderer {
    protected void encodeMarkup(FacesContext context, ProgressBar progressBar) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        int value = progressBar.getValue();
        String labelTemplate = progressBar.getLabelTemplate();
        String style = progressBar.getStyle();
        String styleClass = progressBar.getStyleClass();
        styleClass = styleClass == null ? ProgressBar.CONTAINER_CLASS : ProgressBar.CONTAINER_CLASS + " " + styleClass;

        if (progressBar.isDisabled()) {
            styleClass = styleClass + " ui-state-disabled";
        }

        writer.startElement("div", progressBar);
        writer.writeAttribute("id", progressBar.getClientId(context), "id");
        writer.writeAttribute("class", styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute("style", style, "style");
        }

        //value
        writer.startElement("div", progressBar);
        writer.writeAttribute("class", ProgressBar.VALUE_CLASS, null);
        if (value != 0) {
            writer.writeAttribute("style", "display:block;width:" + value + "%", style);
        }
        writer.endElement("div");

        //label
        writer.startElement("div", progressBar);
        writer.writeAttribute("class", ProgressBar.LABEL_CLASS, null);
        if (labelTemplate != null) {
            writer.writeAttribute("style", "display:block", style);
            writer.write(labelTemplate.replaceAll("\\{value\\}", String.valueOf(value)));
        }
        writer.endElement("div");

        writer.endElement("div");
    }
}
