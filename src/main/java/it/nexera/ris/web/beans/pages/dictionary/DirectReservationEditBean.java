package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.IntegrationConnectionException;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.AsapSectorHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.AsapSector;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DirectReservation;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.relation.ReservationAsapSector;
import it.nexera.ris.persistence.integration.ADTIntegrationHelper;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.SelectManyMenuWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named ("directReservationEditBean")
@ViewScoped
public class DirectReservationEditBean extends
        EntityEditPageBean<DirectReservation> implements Serializable {

    private static final long serialVersionUID = 6881223295727992603L;

    private List<SelectItem> sectors;

    private Long sectorId;

    private SelectManyMenuWrapper<RadiologyExam> examSelectManyMenuWrapper;

    private SelectManyMenuWrapper<AsapSector> asapSectorSelectManyMenuWrapper;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {

        this.setSectors(ComboboxHelper.fillList(Sector.class, true));
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getSector())) {
            setSectorId(this.getEntity().getSector().getId());
        }

        setExamSelectManyMenuWrapper(new SelectManyMenuWrapper<>(RadiologyExam.class, getEntity().getRadiologyExams()));
        loadAsapSectors();

    }

    private void loadAsapSectors() {
        try {
            if (!ValidationHelper.isNullOrEmpty(getEntity().getAsapSectors())) {
                setAsapSectorSelectManyMenuWrapper(new SelectManyMenuWrapper<>(AsapSector.class, getEntity().getAsapSectors(),
                        ADTIntegrationHelper.getInstance().getASAPSIOSectors(Restrictions.not(
                                Restrictions.in("id", AsapSectorHelper.getAsapIds(getEntity().getAsapSectors()))).toString())));
            } else {
                setAsapSectorSelectManyMenuWrapper(new SelectManyMenuWrapper<AsapSector>(AsapSector.class, new ArrayList<AsapSector>(),
                        ADTIntegrationHelper.getInstance().getASAPSIOSectors(null)));
            }
        } catch (IntegrationConnectionException e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getSectorId())) {
            addRequiredFieldExeption("form:sector");
        } else {
            if (this.getEntity().isNew()
                    || !(this.getEntity().getSector() != null && !this
                    .getEntity().getSector().equals(this.getSectorId()))) {
                String hql = "from DirectReservation where sector.id = :id";
                int result = 0;
                try {
                    result = DaoManager.getSession().createQuery(hql)
                            .setLong("id", this.getSectorId().longValue())
                            .list().size();
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                if (result > 0) {
                    addFieldExeption("form:sector", "SectorAlreadyInUse");
                }
            }
        }
        if (ValidationHelper.isNullOrEmpty(this.getAsapSectorSelectManyMenuWrapper().getTarget())) {
            addFieldExeption("form:target", "examsIsRequired");
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("validationFailed"),
                    ResourcesHelper.getValidation("examsListIsEmpty"));
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        this.getEntity().setSector(
                DaoManager.get(Sector.class, this.getSectorId()));

        if (!ValidationHelper.isNullOrEmpty(this.getEntity()
                .getRadiologyExams())) {
            processDuplicateAndRemovingExams(getEntity().getRadiologyExams(), getExamSelectManyMenuWrapper().getTarget());
        } else {
            this.getEntity().setRadiologyExams(getExamSelectManyMenuWrapper().getTarget());
        }

        DaoManager.save(this.getEntity());

        saveAsapSectors();
    }

    private void saveAsapSectors() throws PersistenceBeanException, IllegalAccessException {
        AsapSectorHelper.replaceDetachedSectors(getAsapSectorSelectManyMenuWrapper().getTarget());
        for (AsapSector asapSector : getAsapSectorSelectManyMenuWrapper().getTarget()) {
            DaoManager.save(asapSector);
            if (asapSector.isNew()) {
                DaoManager.save(new ReservationAsapSector(getEntity(), asapSector));
            }
        }
    }

    private void processDuplicateAndRemovingExams(List<RadiologyExam> exams,
                                                  List<RadiologyExam> examsToAdd) {
        for (RadiologyExam examToCheckForDuplication : examsToAdd) {
            boolean contains = true;
            for (RadiologyExam exam : exams) {
                if (exam.getId().equals(examToCheckForDuplication.getId())) {
                    contains = false;
                }
            }
            if (contains) {
                exams.add(examToCheckForDuplication);
            }
        }
        List<Long> idsToRemove = new ArrayList<>();
        for (RadiologyExam exam : exams) {
            boolean needToRemove = true;
            for (RadiologyExam examToAdd : examsToAdd) {
                if (examToAdd.getId().equals(exam.getId())) {
                    needToRemove = false;
                }
            }
            if (needToRemove) {
                idsToRemove.add(exam.getId());
            }
        }
        List<RadiologyExam> copied = new ArrayList<>();
        copied.addAll(exams);
        for (RadiologyExam exam : copied) {
            if (idsToRemove.contains(exam.getId())) {
                exams.remove(exam);
            }
        }
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

    public SelectManyMenuWrapper<RadiologyExam> getExamSelectManyMenuWrapper() {
        return examSelectManyMenuWrapper;
    }

    public void setExamSelectManyMenuWrapper(SelectManyMenuWrapper<RadiologyExam> examSelectManyMenuWrapper) {
        this.examSelectManyMenuWrapper = examSelectManyMenuWrapper;
    }

    public SelectManyMenuWrapper<AsapSector> getAsapSectorSelectManyMenuWrapper() {
        return asapSectorSelectManyMenuWrapper;
    }

    public void setAsapSectorSelectManyMenuWrapper(SelectManyMenuWrapper<AsapSector> asapSectorSelectManyMenuWrapper) {
        this.asapSectorSelectManyMenuWrapper = asapSectorSelectManyMenuWrapper;
    }
}
