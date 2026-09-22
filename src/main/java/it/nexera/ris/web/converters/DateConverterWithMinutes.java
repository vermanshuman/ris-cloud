package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.DateTimeHelper;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.Date;

/**
 * DateTime converter, with minutes
 */
@FacesConverter(value = "dateTimeConverterWithMinutes")
public class DateConverterWithMinutes implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        if ((value != null) && (!value.isEmpty()) && (value.length() > 2)) {
            return DateTimeHelper.fromAMPMString(value);
        } else {
            return null;
        }
    }

    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        if (value != null) {
            return DateTimeHelper.toStringWithMinutes((Date) value);
        } else {
            return null;
        }
    }
}
