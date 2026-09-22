package it.nexera.ris.web.renderers;

import org.primefaces.component.selectonebutton.SelectOneButton;
import org.primefaces.component.selectonebutton.SelectOneButtonRenderer;
import org.primefaces.util.HTML;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import java.io.IOException;

public class SelectOneButtonRendererEx extends SelectOneButtonRenderer {
    @Override
    protected void encodeOption(FacesContext context, SelectOneButton button, SelectItem option,
                                String id, String name, Converter converter, boolean selected, boolean disabled,
                                int idx, int size) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String itemValueAsString = getOptionAsString(context, button, converter, option.getValue());

        String buttonStyle = HTML.BUTTON_TEXT_ONLY_BUTTON_FLAT_CLASS;
        if (idx == 0)
            buttonStyle = buttonStyle + " ui-corner-left";
        else if (idx == (size - 1))
            buttonStyle = buttonStyle + " ui-corner-right";

        buttonStyle = selected ? buttonStyle + " ui-state-active" : buttonStyle;
        buttonStyle = disabled ? buttonStyle + " ui-state-disabled" : buttonStyle;


        //button
        writer.startElement("div", null);
        writer.writeAttribute("class", buttonStyle, null);
        if (option.getDescription() != null) writer.writeAttribute("title", option.getDescription(), null);

        //input
        writer.startElement("input", null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("name", name, null);
        writer.writeAttribute("type", "radio", null);
        writer.writeAttribute("value", itemValueAsString, null);
        writer.writeAttribute("class", "ui-helper-hidden", null);

        if (selected) writer.writeAttribute("checked", "checked", null);
        if (disabled) writer.writeAttribute("disabled", "disabled", null);
        if (button.getOnchange() != null) writer.writeAttribute("onchange", button.getOnchange(), null);

        writer.endElement("input");

        //item label
        writer.startElement("span", null);
        writer.writeAttribute("class", HTML.BUTTON_TEXT_CLASS, null);
        //originally:
        //writer.writeText(option.getLabel(), "itemLabel");
        writer.write(option.getLabel());
        writer.endElement("span");

        writer.endElement("div");
    }

}
