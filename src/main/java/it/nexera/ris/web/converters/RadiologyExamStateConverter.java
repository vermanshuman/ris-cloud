package it.nexera.ris.web.converters;

import it.nexera.ris.common.enums.WaitingListRegistrationStates;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "radiologyExamStateConverter")
public class RadiologyExamStateConverter implements Converter {
    /* (non-Javadoc)
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        try {
            for (WaitingListRegistrationStates state : WaitingListRegistrationStates
                    .values()) {
                if (state.name().equals(value)) {
                    return state;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return value;
    }

    /* (non-Javadoc)
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        return value.toString();
    }
}
