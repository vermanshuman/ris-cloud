package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.LogHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@FacesConverter(value = "floatConverter")
public class FloatConverter implements Converter {
    protected transient final Logger log = LogManager.getLogger(getClass());

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        if ((value != null) && (!value.isEmpty())) {
            try {
                value = value.replaceAll(",", ".");
                DecimalFormat dec = new DecimalFormat("###.##",
                        DecimalFormatSymbols.getInstance(Locale.US));
                return dec.format(Double.parseDouble(value));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            return value;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        if (value != null && !String.valueOf(value).isEmpty()) {
            try {
                return BaseConverter.convertToDoubleString(value);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return null;
    }
}
