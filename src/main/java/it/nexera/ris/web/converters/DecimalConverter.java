package it.nexera.ris.web.converters;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@FacesConverter(value = "decimalConverter")
public class DecimalConverter implements Converter {
    protected transient final Logger log = LogManager.getLogger(getClass());

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String value)
            throws ConverterException {
        if ((value != null) && (!value.isEmpty())) {
            try {
                value = value.replaceAll(",", ".");
                DecimalFormat dec = new DecimalFormat("###.#",
                        DecimalFormatSymbols.getInstance(Locale.US));
                return dec.format(Double.parseDouble(value));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return value;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) throws ConverterException {
        if (value != null && !String.valueOf(value).isEmpty()) {
            try {
                return BaseConverter.convertToDecimalString(value);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

}
