package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Package;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.PackageRadiologyExam;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.DragDropEvent;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Named("packageEditBean")
@ViewScoped
public class PackageEditBean extends EntityEditPageBean<Package> implements
        Serializable {
    private static final long serialVersionUID = -2816199396828198897L;

    private List<RadiologyExam> sourceSelected;

    private List<RadiologyExam> targetSelected;

    private List<RadiologyExam> sourceFiltered;

    private List<RadiologyExam> targetFiltered;

    private List<RadiologyExam> sourceExams;

    private List<RadiologyExam> targetExams;

    private List<RadiologyExam> baseTarget;

    private Boolean showSourceDroppable;

    private Boolean showTargetDroppable;

    private List<SelectItem> examTypes;

    private Long examTypeId;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.fillLists();
        showSourceDroppable = true;
        showTargetDroppable = true;
        if (!this.isPostback()) {
            if (!ValidationHelper.isNullOrEmpty(this.getEntity().getExamType())) {
                setExamTypeId(this.getEntity().getExamType().getId());
            }

            if (!(ValidationHelper.isNullOrEmpty(this.getExamTypeId()) || this
                    .getExamTypeId().equals(0L))) {
                loadRadiologyExams();
            } else {
                this.setBaseTarget(new ArrayList<RadiologyExam>());
                this.setSourceExams(new ArrayList<RadiologyExam>());
                this.setTargetExams(new ArrayList<RadiologyExam>());
            }
        }
        
        /*        if (!this.getViewState().containsKey("entityPicklistSource"))
                {
                    this.getViewState().put(
                            "entityPicklistSource",
                            DaoManager.load(RadiologyExam.class, new Criterion[] {},
                                    Order.asc("code")));
                }*/
    }

    private void loadRadiologyExams() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        this.setBaseTarget(new ArrayList<RadiologyExam>());

        List<RadiologyExam> source = DaoManager.load(RadiologyExam.class,
                new Criterion[]{
                        Restrictions.isNotNull("examType")
                }, Order.asc("description"));
        List<RadiologyExam> target = null;

        if (source == null) {
            source = new ArrayList<RadiologyExam>();
        }

        target = new ArrayList<RadiologyExam>();

        if (!this.getEntity().isNew()) {
            List<PackageRadiologyExam> pe = DaoManager.load(
                    PackageRadiologyExam.class, new Criterion[]{
                            Restrictions.eq("dicPackage.id", this.getEntity()
                                    .getId())
                    });
            if (pe != null) {
                for (PackageRadiologyExam item : pe) {
                    if (!Boolean.TRUE.equals(item.getRadiologyExam()
                            .getDeleted())) {
                        target.add(item.getRadiologyExam());
                        source.remove(item.getRadiologyExam());
                    }
                }

                this.getBaseTarget().addAll(target);
            }
        }

        this.setSourceExams(source);
        this.setTargetExams(target);
    }

    private void fillLists() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setExamTypes(ComboboxHelper.fillList(ExamType.class, true));
    }

    public void onTargetDrop(DragDropEvent ddEvent) {
        if ((ddEvent.getData() instanceof RadiologyExam)
                && (ddEvent.getDragId().contains("target"))) {
            moveExams(getTargetExams(), getSourceExams(), new RadiologyExam[]{
                    (RadiologyExam) ddEvent.getData()
            }, false);
        }
    }

    public void onSourceDrop(DragDropEvent ddEvent) {
        if ((ddEvent.getData() instanceof RadiologyExam)
                && (ddEvent.getDragId().contains("source"))) {
            moveExams(getSourceExams(), getTargetExams(), new RadiologyExam[]{
                    (RadiologyExam) ddEvent.getData()
            }, false);
        }
    }

    public void add() {
        RadiologyExam temp[] = new RadiologyExam[sourceSelected.size()];
        sourceSelected.toArray(temp);
        moveExams(this.getSourceExams(), this.getTargetExams(), temp, false);
    }

    public void remove() {
        RadiologyExam temp[] = new RadiologyExam[targetSelected.size()];
        targetSelected.toArray(temp);
        moveExams(this.getTargetExams(), this.getSourceExams(), temp, false);
    }

    public void addAll() {
        moveExams(this.getSourceExams(), this.getTargetExams(), null, true);
        this.getSourceExams().clear();
        if (sourceFiltered != null) {
            sourceFiltered.clear();
        }
    }

    public void removeAll() {
        moveExams(this.getTargetExams(), this.getSourceExams(), null, true);
        this.getTargetExams().clear();
    }

    private void moveExams(List<RadiologyExam> source,
                           List<RadiologyExam> target, RadiologyExam[] exams, boolean all) {
        if (source != null && !source.isEmpty()) {
            if (all) {
                target.addAll(source);
                sortList(target);
            } else if (exams != null) {
                for (RadiologyExam radiologyExam : exams) {
                    for (RadiologyExam sourceExam : source) {
                        if (radiologyExam.getId().equals(sourceExam.getId())) {
                            source.remove(sourceExam);
                            break;
                        }
                    }
                    target.add(radiologyExam);
                }
                sortList(target);
            }
        }
    }

    public void sortList(List<RadiologyExam> list) {
        Collections.sort(list, new Comparator<RadiologyExam>() {
            @Override
            public int compare(RadiologyExam o1, RadiologyExam o2) {
                return o1.getCode().compareTo(o2.getCode());
            }
        });
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldExeption("form:code");
        } else if (!ValidationHelper.isUnique(Package.class, "code", getEntity()
                .getCode(), getEntity().getId())) {
            addFieldExeption("form:code", "codeAlreadyInUse");
        }
        if (ValidationHelper.isNullOrEmpty(getEntity().getDescription())) {
            addRequiredFieldExeption("form:description");
        }

        if (ValidationHelper.isNullOrEmpty(this.getExamTypeId())) {
            addRequiredFieldExeption("form:examType");
        }

        if (ValidationHelper.isNullOrEmpty(this.getTargetExams())) {
            addFieldExeption("form:target", "examsIsRequired");
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("validationFailed"),
                    ResourcesHelper.getValidation("examsListIsEmpty"));
        }
    }

    public void handleExamTypeChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!(ValidationHelper.isNullOrEmpty(this.getExamTypeId()) || this
                .getExamTypeId().equals(0L))) {
            List<RadiologyExam> source = new ArrayList<RadiologyExam>();
            if (!ValidationHelper.isNullOrEmpty(this.getExamTypeId())) {
                source = DaoManager.load(
                        RadiologyExam.class,
                        new Criterion[]{
                                Restrictions.isNotNull("examType"),
                                Restrictions.eq("examType.id",
                                        this.getExamTypeId())
                        }, Order.asc("description"));
            } else {
                source = DaoManager.load(RadiologyExam.class, new Criterion[]{
                        Restrictions.isNotNull("examType")
                }, Order.asc("description"));
            }
            this.setSourceExams(source);
        } else {
            this.setSourceExams(new ArrayList<RadiologyExam>());
        }

        this.setTargetExams(new ArrayList<RadiologyExam>());
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        this.getEntity().setExamType(
                DaoManager.get(ExamType.class, this.getExamTypeId()));

        DaoManager.save(this.getEntity());

        List<RadiologyExam> exams = this.getTargetExams();
        List<RadiologyExam> baseTarget = this.getBaseTarget();

        if (exams != null) {
            for (RadiologyExam exam : exams) {
                boolean exist = false;

                if (baseTarget != null) {
                    for (RadiologyExam base : baseTarget) {
                        if (base.getId().equals(exam.getId())) {
                            exist = true;
                            break;
                        }
                    }
                }
                if (!exist) {
                    PackageRadiologyExam pe = new PackageRadiologyExam();
                    pe.setDicPackage(getEntity());
                    pe.setRadiologyExam(exam);
                    DaoManager.save(pe);
                }
            }
        }

        if (baseTarget != null) {
            for (RadiologyExam base : baseTarget) {
                boolean exist = false;
                if (exams != null) {
                    for (RadiologyExam exam : exams) {
                        if (exam.getId().equals(base.getId())) {
                            exist = true;
                            break;
                        }
                    }
                }

                if (!exist) {
                    DaoManager.remove(DaoManager.get(
                            PackageRadiologyExam.class,
                            new Criterion[]{
                                    Restrictions.eq("radiologyExam.id",
                                            base.getId()),
                                    Restrictions.eq("dicPackage.id", this
                                            .getEntity().getId())
                            }));
                }
            }
        }
    }

    public void changeShowSourceDroppable() {
        this.setShowSourceDroppable(true);
        this.setShowTargetDroppable(false);
    }

    public void changeShowTargetDroppable() {
        this.setShowTargetDroppable(true);
        this.setShowSourceDroppable(false);
    }

    public List<SelectItem> getExamTypes() {
        return examTypes;
    }

    public void setExamTypes(List<SelectItem> examTypes) {
        this.examTypes = examTypes;
    }

    public Long getExamTypeId() {
        return examTypeId;
    }

    public void setExamTypeId(Long examTypeId) {
        this.examTypeId = examTypeId;
    }

    public List<RadiologyExam> getTargetExams() {
        return targetExams;
    }

    public void setTargetExams(List<RadiologyExam> targetExams) {
        this.targetExams = targetExams;
    }

    public List<RadiologyExam> getSourceExams() {
        return sourceExams;
    }

    public void setSourceExams(List<RadiologyExam> sourceExams) {
        this.sourceExams = sourceExams;
    }

    public List<RadiologyExam> getSourceSelected() {
        return sourceSelected;
    }

    public void setSourceSelected(List<RadiologyExam> sourceSelected) {
        this.sourceSelected = sourceSelected;
    }

    public List<RadiologyExam> getTargetSelected() {
        return targetSelected;
    }

    public void setTargetSelected(List<RadiologyExam> targetSelected) {
        this.targetSelected = targetSelected;
    }

    public List<RadiologyExam> getBaseTarget() {
        return baseTarget;
    }

    public void setBaseTarget(List<RadiologyExam> baseTarget) {
        this.baseTarget = baseTarget;
    }

    public List<RadiologyExam> getSourceFiltered() {
        return sourceFiltered;
    }

    public void setSourceFiltered(List<RadiologyExam> sourceFiltered) {
        this.sourceFiltered = sourceFiltered;
    }

    public List<RadiologyExam> getTargetFiltered() {
        return targetFiltered;
    }

    public void setTargetFiltered(List<RadiologyExam> targetFiltered) {
        this.targetFiltered = targetFiltered;
    }

    public Boolean getShowSourceDroppable() {
        return showSourceDroppable;
    }

    public void setShowSourceDroppable(Boolean showSourceDroppable) {
        this.showSourceDroppable = showSourceDroppable;
    }

    public Boolean getShowTargetDroppable() {
        return showTargetDroppable;
    }

    public void setShowTargetDroppable(Boolean showTargetDroppable) {
        this.showTargetDroppable = showTargetDroppable;
    }

}
