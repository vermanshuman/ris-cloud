package it.nexera.ris.web.renderers;

import it.nexera.ris.common.helpers.XSSCleaner;
import org.primefaces.component.selectonelistbox.SelectOneListbox;
import org.primefaces.component.selectonelistbox.SelectOneListboxRenderer;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import java.io.IOException;

public class SelectOneListboxRendererEx extends SelectOneListboxRenderer {
    @Override
    protected void encodeOption(FacesContext context, SelectOneListbox listbox,
                                SelectItem option, Object values, Object submittedValues,
                                Converter converter) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String itemValueAsString = getOptionAsString(context, listbox,
                converter, option.getValue());
        boolean disabled = option.isDisabled() || listbox.isDisabled();

        Object valuesArray;
        Object itemValue;
        if (submittedValues != null) {
            valuesArray = submittedValues;
            itemValue = itemValueAsString;
        } else {
            valuesArray = values;
            itemValue = option.getValue();
        }

        boolean selected = isSelected(context, listbox, itemValue, valuesArray,
                converter);
        if (option.isNoSelectionOption() && values != null && !selected) {
            return;
        }

        writer.startElement("option", null);

        writer.writeAttribute("value", XSSCleaner.cleanXSS(itemValueAsString),
                null);
        if (disabled) {
            writer.writeAttribute("disabled", "disabled", null);
        }
        if (selected) {
            writer.writeAttribute("selected", "selected", null);
        }

        if (option.isEscape()) {
            writer.writeText(option.getLabel(), "value");
        } else {
            writer.write(option.getLabel());
        }

        writer.endElement("option");
    }
}
