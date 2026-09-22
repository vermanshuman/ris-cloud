package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("hexColorConverter")
public class HexColorConverter implements Converter<String> {

    @Override
    public String getAsString(FacesContext context, UIComponent component, String modelValue) {
        if (modelValue == null || modelValue.isEmpty()) {
            return "";
        }

        return modelValue.startsWith("#") ? modelValue : "#" + modelValue;
    }

    @Override
    public String getAsObject(FacesContext context, UIComponent component, String submittedValue) {
        if (submittedValue == null || submittedValue.isEmpty()) {
            return null;
        }

        return submittedValue.substring(1);
    }
}
