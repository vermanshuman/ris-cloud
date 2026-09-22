package it.nexera.ris.web.renderers;

import it.nexera.ris.common.helpers.ValidationHelper;
import org.primefaces.component.selectoneradio.SelectOneRadio;
import org.primefaces.component.selectoneradio.SelectOneRadioRenderer;
import org.primefaces.util.HTML;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import java.io.IOException;

public class SelectOneRadioRendererEx extends SelectOneRadioRenderer {
    @Override
    protected void encodeOption(FacesContext context, SelectOneRadio radio,
                                SelectItem option, String id, String name, Converter converter,
                                boolean selected, boolean disabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String itemValueAsString = getOptionAsString(context, radio, converter,
                option.getValue());
        String styleClass = HTML.RADIOBUTTON_CLASS;

        writer.startElement("td", null);

        writer.startElement("div", null);
        writer.writeAttribute("class", styleClass, null);

        encodeOptionInput(context, radio, id, name, selected, disabled,
                itemValueAsString);
        encodeOptionOutput(context, radio, selected, disabled);

        writer.endElement("div");
        writer.endElement("td");

        if (!ValidationHelper.isNullOrEmpty(option.getLabel())) {
            writer.startElement("td", null);
            encodeOptionLabel(context, radio, id, option, disabled);
            writer.endElement("td");
        }
    }
}
