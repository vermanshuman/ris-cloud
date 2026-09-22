package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * This converter cuts all html tags from component text value
 */
@FacesConverter(value = "cutHtmlConverter")
public class CutHtmlConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        return ((String) value).replaceAll("\\<.*?>", "")
                .replace("&nbsp;", " ");
    }
}
