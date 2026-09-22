package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * This converter cuts all html tags from component text value
 */
@FacesConverter(value = "minutesToTimeConverter")
public class MinutesToTimeConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        long toMinutes = ((Number) value).longValue();
        long toHours = MINUTES.toHours(toMinutes);
        long hours = toHours;
        long minute = toMinutes - (toHours * 60);
        long second = 0;

        return String.format("%02d:%02d:%02d", hours, minute, second);
    }
}
