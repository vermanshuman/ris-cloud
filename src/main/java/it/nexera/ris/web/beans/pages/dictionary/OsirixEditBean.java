package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Osirix;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named("osirixEditBean")
@ViewScoped
public class OsirixEditBean extends EntityEditPageBean<Osirix> implements
        Serializable {

    private static final long serialVersionUID = -1221224855070823599L;

    private List<SelectItem> sectors;

    private Long sectorId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.setSectors(ComboboxHelper.fillList(Sector.class));
        if (this.getEntity().getSector() != null) {
            this.setSectorId(this.getEntity().getSector().getId());
        }
    }

    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getIpAddress())) {
            addRequiredFieldExeption("form:ipAddress");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getPort())) {
            addRequiredFieldExeption("form:port");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getType())) {
            addRequiredFieldExeption("form:type");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDescription())) {
            addRequiredFieldExeption("form:description");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getAet())) {
            addRequiredFieldExeption("form:aet");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDicomPort())) {
            addRequiredFieldExeption("form:dicomPort");
        }
        if (ValidationHelper.isNullOrEmpty(getSectorId())) {
            addRequiredFieldExeption("sector");
        }

        try {
            Osirix osirixWithSameIp = DaoManager.get(
                    Osirix.class,
                    new Criterion[]{
                            Restrictions.eq("ipAddress", this.getEntity()
                                    .getIpAddress().trim()),
                            Restrictions.eq("sector.id", this.getSectorId())
                    });
            if (osirixWithSameIp != null
                    && !osirixWithSameIp.getId().equals(
                    this.getEntity().getId())) {
                addFieldExeption("form:ipAddress", "osirixWithTheSamePort");
            }
        } catch (HibernateException | InstantiationException
                | IllegalAccessException e) {
            LogHelper.log(log, e);
        }

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (this.getSectorId() != null) {
            Sector sector = DaoManager.get(Sector.class, this.getSectorId());
            this.getEntity().setSector(sector);
        }
        DaoManager.save(this.getEntity());
    }

    public List<SelectItem> getSectors() {
        return sectors;
    }

    public void setSectors(List<SelectItem> sectors) {
        this.sectors = sectors;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }
}
