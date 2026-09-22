package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.FormattingUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;


@FacesConverter(value = "italianCharsConverter")
public class ItalianCharsConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {

        return FormattingUtils.htmlToStringWithTags(arg2);
    }

    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        return FormattingUtils.htmlToStringWithTags((String) arg2);
    }

}
