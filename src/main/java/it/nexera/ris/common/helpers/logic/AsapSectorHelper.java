package it.nexera.ris.common.helpers.logic;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.IntegrationConnectionException;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.AsapSector;
import it.nexera.ris.persistence.integration.ADTIntegrationHelper;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.ApplicationSettingsValueWrapper;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

public class AsapSectorHelper extends BaseHelper {

    public static List<AsapSector> getASAPSIOSectorsFromDB(String query) {
        return getASAPSIOSectorsFromDB(query, null);
    }

    public static List<AsapSector> getASAPSIOSectorsFromDB(String query, String hospitalCode) {
        try {
            ApplicationSettingsValueWrapper asapSioIntUrl = ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_URL);

            ApplicationSettingsValueWrapper asapSioIntUser = ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_USERNAME);

            ApplicationSettingsValueWrapper asapSioIntPwd = ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.SETTING_ASAP_SIO_INTEGRATION_PASSWORD);

            ADTIntegrationHelper helper = ADTIntegrationHelper.getInstance();
            if (asapSioIntUrl == null || asapSioIntUser == null
                    || asapSioIntPwd == null) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("applicationSettings"),
                        ResourcesHelper.getValidation("emptyASAPSettings"));
            } else {
                if (helper.checkConnection(asapSioIntUrl.getValue(), asapSioIntUser.getValue(), asapSioIntPwd.getValue())) {
                    String sqlQuery = "";
                    if (!ValidationHelper.isNullOrEmpty(query)) {
                        sqlQuery = "lower(s.DESCRIPTION) like lower(\'" + query + "%\')";
                    }
                    return helper.getASAPSIOSectors(sqlQuery, hospitalCode);
                } else {
                    return new ArrayList<>();
                }
            }
        } catch (IntegrationConnectionException e) {
            LogHelper.log(log, e);
        }
        return new ArrayList<>();
    }

    public static void replaceDetachedSectors(List<AsapSector> sectors) throws PersistenceBeanException, IllegalAccessException {
        List<AsapSector> asapSectors = DaoManager.load(AsapSector.class, new Criterion[]{Restrictions.in("asapId", getAsapIds(sectors))});
        for (AsapSector asapSector : asapSectors) {
            int index = sectors.indexOf(asapSector);
            AsapSector sector = sectors.get(index);
            sector.setDescription(asapSector.getDescription());
            sector.setCode(asapSector.getCode());
            sectors.set(index, sector);
        }
    }

    public static List<Long> getAsapIds(List<AsapSector> asapSectors) {
        List<Long> ids = new ArrayList<>();
        for (AsapSector sector : asapSectors) {
            ids.add(sector.getAsapId());
        }
        return ids;
    }
}
