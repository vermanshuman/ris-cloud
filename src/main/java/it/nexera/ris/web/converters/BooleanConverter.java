package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "booleanConverter")
public class BooleanConverter implements Converter {
    protected transient final Logger log = LogManager.getLogger(getClass());

    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        if (value != null) {
            try {
                if ((Boolean) value) {
                    return ResourcesHelper.getString("yes");
                }

                return ResourcesHelper.getString("no");
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return ResourcesHelper.getString("no");
    }

    /* (non-Javadoc)
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
     */
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2)
            throws ConverterException {
        return null;
    }
}
