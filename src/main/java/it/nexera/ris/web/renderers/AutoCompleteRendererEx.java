package it.nexera.ris.web.renderers;

import org.primefaces.component.autocomplete.AutoComplete;
import org.primefaces.component.autocomplete.AutoCompleteRenderer;
import org.primefaces.util.ComponentUtils;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AutoCompleteRendererEx extends AutoCompleteRenderer {
    protected void encodeMultipleMarkup(FacesContext context, AutoComplete ac)
            throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = ac.getClientId(context);
        String inputId = clientId + "_input";

        @SuppressWarnings("unchecked")
        List<Object> values = (List<Object>) ac.getValue();
        List<String> stringValues = new ArrayList<String>();
        Converter converter = ComponentUtils.getConverter(context, ac);
        String var = ac.getVar();
        boolean pojo = var != null;
        boolean disabled = ac.isDisabled();

        String styleClass = ac.getStyleClass();
        styleClass = styleClass == null ? AutoComplete.MULTIPLE_STYLE_CLASS
                : AutoComplete.MULTIPLE_STYLE_CLASS + " " + styleClass;
        String listClass = disabled ? AutoComplete.MULTIPLE_CONTAINER_CLASS
                + " ui-state-disabled" : AutoComplete.MULTIPLE_CONTAINER_CLASS;

        writer.startElement("div", null);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute("title", ac.getTitle(), null);
        writer.writeAttribute("class", styleClass, null);
        if (ac.getStyle() != null) {
            writer.writeAttribute("style", ac.getStyle(), null);
        }

        writer.startElement("ul", null);
        writer.writeAttribute("class", listClass, null);

        if (values != null && !values.isEmpty()) {
            for (Iterator<Object> it = values.iterator(); it.hasNext(); ) {
                Object value = it.next();
                Object itemValue = null;
                String itemLabel = null;

                if (pojo) {
                    context.getExternalContext().getRequestMap()
                            .put(var, value);
                    itemValue = ac.getItemValue();
                    itemLabel = ac.getItemLabel();
                } else {
                    itemValue = value;
                    itemLabel = String.valueOf(value);
                }

                String tokenValue = converter != null ? converter.getAsString(
                        context, ac, itemValue) : String.valueOf(itemValue);

                writer.startElement("li", null);
                writer.writeAttribute("data-token-value", tokenValue, null);
                writer.writeAttribute("class",
                        AutoComplete.TOKEN_DISPLAY_CLASS, null);

                writer.startElement("span", null);
                writer.writeAttribute("class", AutoComplete.TOKEN_LABEL_CLASS,
                        null);
                writer.writeText(itemLabel, null);
                writer.endElement("span");

                writer.startElement("span", null);
                writer.writeAttribute("class", AutoComplete.TOKEN_ICON_CLASS,
                        null);
                writer.endElement("span");

                writer.endElement("li");

                stringValues.add(tokenValue);
            }
        }

        writer.startElement("li", null);
        writer.writeAttribute("class", AutoComplete.TOKEN_INPUT_CLASS, null);
        writer.startElement("input", null);
        writer.writeAttribute("type", "text", null);
        writer.writeAttribute("id", inputId, null);
        writer.writeAttribute("name", inputId, null);
        writer.writeAttribute("autocomplete", "off", null);
        if (disabled) {
            writer.writeAttribute("disabled", "disabled", "disabled");
        }
        writer.endElement("input");
        writer.endElement("li");

        writer.endElement("ul");

        encodePanel(context, ac);

        encodeHiddenSelect(context, ac, clientId, stringValues);

        writer.endElement("div");
    }
}
