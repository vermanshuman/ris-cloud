package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This converter cuts all html tags from component text value
 */
@FacesConverter(value = "secondsToTimeConverter")
public class SecondsToTimeConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        long seconds = ((Number) value).longValue();
        long toMinutes = SECONDS.toMinutes(seconds);
        long toHours = SECONDS.toHours(seconds);
        long hours = toHours;
        long minute = toMinutes - (toHours * 60);
        long second = SECONDS.toSeconds(seconds) - (toMinutes * 60);

        return String.format("%02d:%02d:%02d", hours, minute, second);
    }
}
