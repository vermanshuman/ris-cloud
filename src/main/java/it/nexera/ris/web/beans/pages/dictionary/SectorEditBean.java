package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;

@Named("sectorEditBean")
@ViewScoped
public class SectorEditBean extends EntityEditPageBean<Sector> implements
        Serializable {

    private static final long serialVersionUID = 7139777237007969955L;

    private String sectorCode;

    private boolean showDicomSettings;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (this.getEntity().isNew()) {
            this.setSectorCode(getNextProgressiveId().toString());
        } else {
            this.setSectorCode(this.getEntity().getCode());
        }
        if (!ValidationHelper.isNullOrEmpty(this.getEntity()
                .getSectorDICOMHost())) {
            this.setShowDicomSettings(true);
        }
    }

    private Integer getNextProgressiveId() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return (Integer) DaoManager.getSession()
                .createQuery("select coalesce(max(TO_NUMBER(code)) + 1, 1) from Sector")
                .uniqueResult();
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCdr())) {
            addRequiredFieldExeption("form:sectorCdr");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getDescription())) {
            addRequiredFieldExeption("form:description");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getAccNumPrefix())) {
            addRequiredFieldExeption("form:accNumbPrefix");
        } else if (this.getEntity().getAccNumPrefix().contains(" ")) {
            addFieldExeption("form:accNumbPrefix", "sectorAccNumPrefixSpaces");
        }

        if (this.isShowDicomSettings() == true) {
            if (ValidationHelper.isNullOrEmpty(this.getEntity()
                    .getSectorDICOMHost())) {
                addRequiredFieldExeption("form:dicomHost");
            }
            if (ValidationHelper.isNullOrEmpty(this.getEntity()
                    .getSectorDICOMPort())) {
                addRequiredFieldExeption("form:dicomPort");
            }
            if (ValidationHelper.isNullOrEmpty(this.getEntity()
                    .getSectorDICOMAETitle())) {
                addRequiredFieldExeption("form:dicomAetitle");
            }
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        this.getEntity().setCode(this.getSectorCode());
        if (!this.isShowDicomSettings()) {
            this.getEntity().setSectorDICOMHost(null);
            this.getEntity().setSectorDICOMPort(null);
            this.getEntity().setSectorDICOMAETitle(null);
        }
        DaoManager.save(this.getEntity());
    }

    public String getSectorCode() {
        return sectorCode;
    }

    public void setSectorCode(String sectorCode) {
        this.sectorCode = sectorCode;
    }

    public boolean isShowDicomSettings() {
        return showDicomSettings;
    }

    public void setShowDicomSettings(boolean showDicomSettings) {
        this.showDicomSettings = showDicomSettings;
    }
}
