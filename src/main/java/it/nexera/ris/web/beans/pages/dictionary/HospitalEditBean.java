package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Hospital;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.DualListModel;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("hospitalEditBean")
@ViewScoped
public class HospitalEditBean extends EntityEditPageBean<Hospital> implements
        Serializable {
    private static final long serialVersionUID = 448807886696646867L;

    private List<Sector> source;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        List<Sector> target = null;
        source = DaoManager.load(Sector.class, new Criterion[]{
                Restrictions.or(Restrictions.isNull("hospital.id"),
                        Restrictions.eq("hospital.id", 0l))
        });

        if (source == null) {
            source = new ArrayList<Sector>();
        }

        if (this.getEntity().isNew()) {
            target = new ArrayList<Sector>();
        } else {
            target = this.getEntity().getSectors();
        }

        this.setSectors(new DualListModel<Sector>(source, target));
        this.getViewState().put(
                "entityPicklistSource",
                DaoManager.load(Sector.class, new Criterion[]{
                        Restrictions.or(Restrictions.isNull("hospital.id"),
                                Restrictions.eq(
                                        "hospital.id",
                                        this.getEntityId() == null ? 0l : this
                                                .getEntityId()))
                }));
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldExeption("form:code");
        } else if (!ValidationHelper.isUnique(Hospital.class, "code", getEntity()
                .getCode(), getEntity().getId())) {
            addFieldExeption("form:code", "codeAlreadyInUse");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getDescription())) {
            addRequiredFieldExeption("form:description");
        }

        if (ValidationHelper.isNullOrEmpty(this.getSectors().getTarget())) {
            addRequiredFieldExeption("form:sectors");
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("validationFailed"),
                    ResourcesHelper.getValidation("sectorsListIsEmpty"));
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(this.getEntity());

        List<Sector> sectors = this.getSectors().getTarget();
        if (sectors != null) {
            for (Sector sector : sectors) {
                if (sector.getHospital() == null) {
                    sector.setHospital(this.getEntity());
                    DaoManager.save(sector);
                }
            }
        }
        sectors = this.getSectors().getSource();
        if (sectors != null) {
            for (Sector sector : sectors) {
                if (sector.getHospital() != null) {
                    sector.setHospital(null);
                    DaoManager.save(sector);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public DualListModel<Sector> getSectors() {
        return (DualListModel<Sector>) this.getViewState().get("entitiesDL");
    }

    public void setSectors(DualListModel<Sector> sectors) {
        this.getViewState().put("entitiesDL", sectors);
    }

}
