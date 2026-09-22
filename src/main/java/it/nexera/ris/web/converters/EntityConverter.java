package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.beans.entities.Entity;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.util.List;

@FacesConverter(value = "entityConverter")
public class EntityConverter implements Converter {
    protected transient final Logger log = LogManager.getLogger(getClass());

    @SuppressWarnings("unchecked")
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
        if (submittedValue.trim().equals("")) {
            return null;
        } else {
            try {
                long id = Long.parseLong(submittedValue);

                if (SessionManager.getInstance().getSessionBean()
                        .getViewState().get("entityPicklistSource") != null) {
                    for (Entity entity : (List<Entity>) SessionManager
                            .getInstance().getSessionBean().getViewState()
                            .get("entityPicklistSource")) {
                        if (entity.getId().equals(id)) {
                            return entity;
                        }
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);

                throw new ConverterException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Conversion Error",
                        "Not a valid entity"));
            }
        }

        return null;
    }

    public String getAsString(FacesContext facesContext, UIComponent component,
                              Object value) {
        if (value == null) {
            return null;
        } else {
            return String.valueOf(((Entity) value).getId());
        }
    }
}
