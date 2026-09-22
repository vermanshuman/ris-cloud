package it.nexera.ris.web.converters;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import java.util.List;

public class OrderListSelectItemConverter implements Converter {
    private List<SelectItem> items;

    public void setItems(List<SelectItem> items) {
        this.items = items;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        if (!CollectionUtils.isEmpty(items)) {
            for (SelectItem si : items) {
                if (value.equals(String.valueOf(si.getValue()))) {
                    return si;
                }
            }
        }

        throw new IllegalArgumentException("Collection doesn't contain value");
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        if (value == null) {
            return null;
        }

        if (!CollectionUtils.isEmpty(items)) {
            for (SelectItem si : items) {
                if (value.equals(String.valueOf(si.getValue()))) {
                    return String.valueOf(si.getValue());
                }
            }
        }

        throw new IllegalArgumentException("Collection doesn't contain value");
    }
}
