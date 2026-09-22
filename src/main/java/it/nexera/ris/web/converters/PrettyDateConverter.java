package it.nexera.ris.web.converters;

import it.nexera.ris.common.enums.LocaleType;
import it.nexera.ris.common.helpers.DateTimeHelper;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.Date;
import java.util.Locale;

/**
 * Pretty date converter
 */
@FacesConverter(value = "prettyDateTimeConverter")
public class PrettyDateConverter implements Converter {

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
            return DateTimeHelper.toFormatedString((Date) value,
                    "EEEE, d MMM yyyy", new Locale(LocaleType.IT.getValue()));
        } else {
            return null;
        }
    }
}
