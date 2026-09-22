package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.DateTimeHelper;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Date converter
 */
@FacesConverter(value = "dateTimeConverter")
public class DateConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        if ((value != null) && (!value.isEmpty()) && (value.length() > 2)) {
            return DateTimeHelper.fromString(value);
        } else {
            return null;
        }
    }

    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        if (value != null) {
            return BaseConverter.convertToDateString(value);
        } else {
            return null;
        }
    }
}
