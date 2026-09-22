package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class DoubleConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String value) {
        if (value != null && !value.isEmpty()) {
            value = value.replaceAll(",", ".");

            try {
                Double doubleValue = Double.parseDouble(value);
                return doubleValue;
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component,
                              Object value) {
        if (value != null && value instanceof Double) {
            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
            df.applyPattern("##.#####");
            return df.format(value);
        }

        return null;
    }
}
