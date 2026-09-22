package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.PacsType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.SelectItemHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Hospital;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Pacs;
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
import java.util.ArrayList;
import java.util.List;

@Named("pacsEditBean")
@ViewScoped
public class PacsEditBean extends EntityEditPageBean<Pacs> implements
        Serializable {

    private static final long serialVersionUID = -8573727528668471316L;

    private List<SelectItem> hospitals;

    private Long selectedHospital;

    private List<SelectItem> pacsTypes;

    private PacsType selectedPacsType;

    private List<SelectItem> sectors;

    private Long selectedSector;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.setHospitals(ComboboxHelper.fillList(Hospital.class, true));
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getHospital())) {
            this.setSelectedHospital(this.getEntity().getHospital().getId());
        }
        this.setPacsTypes(ComboboxHelper.fillList(PacsType.values()));
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getSector())) {
            this.setSelectedSector(this.getEntity().getSector().getId());
        }

        hospitalChange();
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldExeption("form:name");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getiPHostPacs())) {
            addRequiredFieldExeption("form:ipAddress");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getPortHostPacs())) {
            addRequiredFieldExeption("form:port");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getAetPacs())) {
            addRequiredFieldExeption("form:aet");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDicomPort())) {
            addRequiredFieldExeption("form:dicomPort");
        }
        if (ValidationHelper.isNullOrEmpty(this.getSelectedHospital())) {
            addRequiredFieldExeption("form:hospitals");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getPacsType())) {
            addRequiredFieldExeption("form:pacsTypes");
        }
        if (ValidationHelper.isNullOrEmpty(this.getSelectedSector())) {
            addRequiredFieldExeption("form:sectors");
        }
        if (this.getSelectedHospital() != null
                && this.getEntity().getPacsType() != null
                && this.getEntity().getSector() != null) {
            try {
                if (hospitalPacsTypeCombinationExist()) {
                    this.addFieldExeption("form:pacsTypes", "pacsNonUnique");
                    this.addFieldExeption("form:hospitals", "pacsNonUnique",
                            Boolean.FALSE);
                    this.addFieldExeption("form:sectors", "pacsNonUnique",
                            Boolean.FALSE);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    private boolean hospitalPacsTypeCombinationExist()
            throws HibernateException, IllegalAccessException,
            PersistenceBeanException, InstantiationException {
        List<Pacs> pacsList = DaoManager.load(Pacs.class, new Criterion[]{
                Restrictions.eq("hospital.id", this.getSelectedHospital()),
                Restrictions.eq("pacsType", this.getEntity().getPacsType()),
                Restrictions.eq("sector.id", this.getSelectedSector())
        });

        if (!ValidationHelper.isNullOrEmpty(pacsList) && pacsList.size() > 1) {
            return true;
        }

        return false;
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        Hospital hospital = DaoManager.get(Hospital.class,
                this.getSelectedHospital());

        Sector sector = DaoManager.get(Sector.class, this.getSelectedSector());

        this.getEntity().setHospital(hospital);
        this.getEntity().setSector(sector);
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDicomPort())) {
            this.getEntity().setDicomPort(null);
        }
        DaoManager.save(this.getEntity());
    }

    public void hospitalChange() {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedHospital())) {
            Hospital hospital = null;

            try {
                hospital = DaoManager.get(Hospital.class,
                        this.getSelectedHospital());

                if (hospital != null
                        && !ValidationHelper.isNullOrEmpty(hospital
                        .getSectors())) {
                    List<Sector> sectorsFromDb = hospital.getSectors();

                    this.setSectors(new ArrayList<SelectItem>());
                    this.getSectors().add(SelectItemHelper.getNotSelected());

                    for (Sector sector : sectorsFromDb) {
                        this.getSectors().add(
                                new SelectItem(sector.getId(), sector
                                        .getDescription()));
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            this.setSectors(new ArrayList<SelectItem>());
            this.getSectors().add(SelectItemHelper.getNotSelected());
        }
    }

    public List<SelectItem> getHospitals() {
        return hospitals;
    }

    public void setHospitals(List<SelectItem> hospitals) {
        this.hospitals = hospitals;
    }

    public Long getSelectedHospital() {
        return selectedHospital;
    }

    public void setSelectedHospital(Long selectedHospital) {
        this.selectedHospital = selectedHospital;
    }

    public List<SelectItem> getPacsTypes() {
        return pacsTypes;
    }

    public void setPacsTypes(List<SelectItem> pacsTypes) {
        this.pacsTypes = pacsTypes;
    }

    public PacsType getSelectedPacsType() {
        return selectedPacsType;
    }

    public void setSelectedPacsType(PacsType selectedPacsType) {
        this.selectedPacsType = selectedPacsType;
    }

    public List<SelectItem> getSectors() {
        return sectors;
    }

    public void setSectors(List<SelectItem> sectors) {
        this.sectors = sectors;
    }

    public Long getSelectedSector() {
        return selectedSector;
    }

    public void setSelectedSector(Long selectedSector) {
        this.selectedSector = selectedSector;
    }
}
