/**
 *
 */
package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.LogHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * Percent converter
 */
@FacesConverter(value = "percentageConverter")
public class PercentageConverter implements Converter {
    protected transient final Logger log = LogManager.getLogger(getClass());

    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        if (value != null && !String.valueOf(value).isEmpty()) {
            try {
                return BaseConverter.convertToPercentageString(value);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
     */
    public Object getAsObject(FacesContext context, UIComponent componnt,
                              String value) throws ConverterException {
        return null;
    }
}
