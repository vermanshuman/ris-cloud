package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.FormattingUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * This converter cuts all html tags from component text value
 */
@FacesConverter(value = "htmlToStrConverter")
public class HtmlToStrConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        return FormattingUtils.htmlToStr((String) value);
    }
}
