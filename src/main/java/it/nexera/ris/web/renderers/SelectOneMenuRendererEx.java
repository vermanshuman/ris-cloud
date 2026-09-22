package it.nexera.ris.web.renderers;

import it.nexera.ris.common.helpers.XSSCleaner;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.selectonemenu.SelectOneMenuRenderer;
import org.primefaces.util.ComponentUtils;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import java.io.IOException;
import java.util.List;

public class SelectOneMenuRendererEx extends SelectOneMenuRenderer {

    /* (non-Javadoc)
     * @see org.primefaces.component.selectonemenu.SelectOneMenuRenderer#encodeLabel(javax.faces.context.FacesContext, org.primefaces.component.selectonemenu.SelectOneMenu, java.util.List)
     */
    @Override
    protected void encodeLabel(FacesContext context, SelectOneMenu menu,
                               List<SelectItem> selectItems) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String valueToRender = ComponentUtils.getValueToRender(context, menu);

        if (menu.isEditable()) {
            writer.startElement("input", null);
            writer.writeAttribute("type", "text", null);
            writer.writeAttribute("name",
                    menu.getClientId() + "_editableInput", null);
            writer.writeAttribute("class", SelectOneMenu.LABEL_CLASS, null);

            if (menu.getTabindex() != null) {
                writer.writeAttribute("tabindex", menu.getTabindex(), null);
            }

            if (menu.isDisabled()) {
                writer.writeAttribute("disabled", "disabled", null);
            }

            if (valueToRender != null) {
                writer.writeAttribute("value", valueToRender, null);
            }

            if (menu.getMaxlength() != Integer.MAX_VALUE) {
                writer.writeAttribute("maxlength", menu.getMaxlength(), null);
            }

            writer.endElement("input");
        } else {
            writer.startElement("label", null);
            writer.writeAttribute("id", menu.getClientId() + "_label", null);

            writer.writeAttribute("class", SelectOneMenu.LABEL_CLASS, null);

            if (menu.getClientId().contains("dynaForm")) {
                writer.write("#{choice.value}");
            } else {
                writer.write("&nbsp;");
            }

            writer.endElement("label");
        }
    }

    protected void encodeOption(FacesContext context, SelectOneMenu menu,
                                SelectItem option, Object values, Object submittedValues,
                                Converter converter) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (option instanceof SelectItemGroup) {
            SelectItemGroup group = (SelectItemGroup) option;
            for (SelectItem groupItem : group.getSelectItems()) {
                encodeOption(context, menu, groupItem, values, submittedValues,
                        converter);
            }
        } else {
            String itemValueAsString = getOptionAsString(context, menu,
                    converter, option.getValue());
            boolean disabled = option.isDisabled();

            Object valuesArray;
            Object itemValue;
            if (submittedValues != null) {
                valuesArray = submittedValues;
                itemValue = itemValueAsString;
            } else {
                valuesArray = values;
                itemValue = option.getValue();
            }

            boolean selected = isSelected(context, menu, itemValue,
                    valuesArray, converter);

            writer.startElement("option", null);
            writer.writeAttribute("value",
                    XSSCleaner.cleanXSS(itemValueAsString), null);
            if (disabled) {
                writer.writeAttribute("disabled", "disabled", null);
            }
            if (selected) {
                writer.writeAttribute("selected", "selected", null);
            }

            writer.writeText(option.getLabel(), "value");

            writer.endElement("option");
        }
    }

}
