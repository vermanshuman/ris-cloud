package it.nexera.ris.web.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import static it.nexera.ris.common.helpers.ValidationHelper.isNullOrEmpty;

@FacesConverter(value = "stringTrimConverter")
public class StringTrimConverter implements Converter {
    static final int TRUNCATE_LENGTH = 32;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        String result = value.replaceAll("\\<.*?>", "").trim();
        if (isNullOrEmpty(result)) {
            return null;
        } else {
            if (isNullOrEmpty(result.replace("&nbsp;", " ").trim())) {
                return null;
            }
        }
        return value.trim();
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        return value.toString();
    }
}
