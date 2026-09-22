package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DVDProducer;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("dvdProducerEditBean")
@ViewScoped
public class DVDProducerEditBean extends EntityEditPageBean<DVDProducer> implements Serializable {

    private List<SelectItem> sectors;

    private Long selectedSectorId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        setSectors(ComboboxHelper.fillList(Sector.class));
        if (getEntity().getSector() != null) {
            setSelectedSectorId(getEntity().getSector().getId());
        }
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getIpAddress())) {
            addRequiredFieldExeption("form:ipAddress");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getPort())) {
            addRequiredFieldExeption("form:port");
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
        if (ValidationHelper.isNullOrEmpty(getSelectedSectorId())) {
            addRequiredFieldExeption("sector");
        }
        try {
            DVDProducer osirixWithSameIp = DaoManager.get(
                    DVDProducer.class,
                    new Criterion[]{
                            Restrictions.eq("ipAddress", getEntity().getIpAddress().trim()),
                            Restrictions.eq("sector.id", getSelectedSectorId())
                    });
            if (osirixWithSameIp != null && !osirixWithSameIp.getId().equals(this.getEntity().getId())) {
                addFieldExeption("form:ipAddress", "osirixWithTheSamePort");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, InstantiationException, IllegalAccessException {
        if (getSelectedSectorId() != null) {
            Sector sector = DaoManager.get(Sector.class, getSelectedSectorId());
            getEntity().setSector(sector);
        }
        DaoManager.save(getEntity());
    }

    public List<SelectItem> getSectors() {
        return sectors;
    }

    public void setSectors(List<SelectItem> sectors) {
        this.sectors = sectors;
    }

    public Long getSelectedSectorId() {
        return selectedSectorId;
    }

    public void setSelectedSectorId(Long selectedSectorId) {
        this.selectedSectorId = selectedSectorId;
    }
}
