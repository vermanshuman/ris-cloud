package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.InstancePhases;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TemplateDocumentModel;
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

@Named("instancePhasesEditBean")
@ViewScoped
public class InstancePhasesEditBean extends EntityEditPageBean<InstancePhases>
        implements Serializable {
    private static final long serialVersionUID = 9152346992792318061L;

    private List<SelectItem> places;

    private List<SelectItem> models;

    private List<SelectItem> sectors;

    private Long modelId;

    private String placeId;

    private Long sectorId;

    private Boolean showSectors;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        fillDropDown();
        fillValues();
    }

    private void fillValues() {
        if (this.getEntity().getModel() != null) {
            this.setModelId(this.getEntity().getModel().getId());
        }
        if (this.getEntity().getPlace() != null) {
            this.setPlaceId(this.getEntity().getPlace().name());
        }
        if (this.getEntity().getSector() != null) {
            this.setShowSectors(Boolean.TRUE);
            this.setSectorId(this.getEntity().getSector().getId());
        }
    }

    private void fillDropDown() {
        try {
            this.setPlaces(ComboboxHelper
                    .fillList(DocumentGenerationPlaces.class));

            this.setModels(ComboboxHelper.fillList(TemplateDocumentModel.class));

            this.setSectors(ComboboxHelper.fillList(Sector.class));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getModelId())) {
            addRequiredFieldExeption("model");
        }
        if (ValidationHelper.isNullOrEmpty(getPlaceId())) {
            addRequiredFieldExeption("place");
        }
        if (getShowSectors().booleanValue()
                && ValidationHelper.isNullOrEmpty(getSectorId())) {
            addRequiredFieldExeption("sectors");
        }
        if (!ValidationHelper.isNullOrEmpty(this.getModelId())
                && !ValidationHelper.isNullOrEmpty(this.getPlaceId())
                && (!ValidationHelper.isNullOrEmpty(this.getSectorId()) || !getShowSectors()
                .booleanValue())) {
            DocumentGenerationPlaces place = DocumentGenerationPlaces
                    .valueOf(this.getPlaceId());
            if (checkInstancePhasesExistanse(getModelId(), getSectorId(), place)) {
                addException("instancePhaseAlreadyExist");
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
        DocumentGenerationPlaces place = DocumentGenerationPlaces.valueOf(this
                .getPlaceId());
        TemplateDocumentModel model = DaoManager.get(
                TemplateDocumentModel.class, this.getModelId());
        Sector sector = null;

        if (this.getShowSectors().booleanValue()) {
            sector = DaoManager.get(Sector.class, this.getSectorId());
        }

        if (!ValidationHelper.isNullOrEmpty(place)
                && !ValidationHelper.isNullOrEmpty(model)) {
            this.getEntity().setModel(model);
            this.getEntity().setPlace(place);
            this.getEntity().setSector(sector);

            DaoManager.save(this.getEntity());
        }
    }

    private boolean checkInstancePhasesExistanse(Long modelId, Long sectorId,
                                                 DocumentGenerationPlaces place) {
        try {
            InstancePhases instancePhases = null;

            if (getShowSectors().booleanValue()) {
                instancePhases = DaoManager.get(
                        InstancePhases.class,
                        new Criterion[]{
                                Restrictions.eq("place", place),
                                Restrictions.eq("model.id", modelId),
                                Restrictions.eq("sector.id", sectorId)
                        });
            } else {
                instancePhases = DaoManager.get(
                        InstancePhases.class,
                        new Criterion[]{
                                Restrictions.eq("place", place),
                                Restrictions.eq("model.id", modelId)
                        });
            }

            if (instancePhases != null
                    && !instancePhases.getId().equals(this.getEntity().getId())) {
                return true;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public void plaseChange() {
        if (!ValidationHelper.isNullOrEmpty(this.getPlaceId())) {
            DocumentGenerationPlaces place = DocumentGenerationPlaces
                    .valueOf(this.getPlaceId());
            if (DocumentGenerationPlaces.REPORTS.equals(place)
                    || DocumentGenerationPlaces.RESERVATION.equals(place)
                    || DocumentGenerationPlaces.ETICH.equals(place)) {
                this.setShowSectors(Boolean.TRUE);
            } else {
                this.setShowSectors(Boolean.FALSE);
            }
        }
    }

    public List<SelectItem> getPlaces() {
        return places;
    }

    public void setPlaces(List<SelectItem> places) {
        this.places = places;
    }

    public List<SelectItem> getModels() {
        return models;
    }

    public void setModels(List<SelectItem> models) {
        this.models = models;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
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

    public Boolean getShowSectors() {
        return showSectors == null ? Boolean.FALSE : showSectors;
    }

    public void setShowSectors(Boolean showSectors) {
        this.showSectors = showSectors;
    }
}
