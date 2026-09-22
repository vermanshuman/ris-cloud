package it.nexera.ris.web.converters;

import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.AsapSector;
import it.nexera.ris.persistence.integration.ADTIntegrationHelper;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("sectorAutocompleteWrapperConverter")
public class SectorAutocompleteWrapperConverter extends BaseHelper implements Converter {

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        Long id = Long.parseLong(arg2);
        try {
            Object object = DaoManager.get(AsapSector.class, id);
            if (object == null) {
                object = ADTIntegrationHelper.getInstance().getASAPSIOSectorWrapperById(id);
            }
            return object;
        } catch (Exception e) {
            LogHelper.log(log, e);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        if (arg2 != null) {
            return String.valueOf(((AsapSector) arg2).getId());
        }
        return null;
    }

}
