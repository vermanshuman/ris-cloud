/**
 *
 */
package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.ResourcesHelper;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * No data converter
 */
@FacesConverter(value = "noDataConverter")
public class NoDataConverter implements Converter {
    /**
     * @param context
     * @param component
     * @param value
     * @return
     */
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        if ((value != null) && !value.isEmpty()) {
            return value;
        } else {
            return ResourcesHelper.getString("converter.nodata");
        }
    }

    /**
     * @param context
     * @param component
     * @param value
     * @return
     */
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        if ((value != null)) {
            return value.toString();
        } else {
            return ResourcesHelper.getString("converter.nodata");
        }
    }
}
