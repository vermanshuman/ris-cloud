package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "dateTimeConverterHHmm")
public class DateConverterOnlyHHmm implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        if (!ValidationHelper.isNullOrEmpty(value) && (value.length() > 2)) {
            return DateTimeHelper.fromString(value);
        } else {
            return null;
        }
    }

    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        if (value != null) {
            return BaseConverter.convertToTimeString(value);
        } else {
            return null;
        }
    }
}
