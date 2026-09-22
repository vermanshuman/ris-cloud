package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.EnableDisableEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.logic.DiagnosticHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Diagnostic;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.relation.DiagnosticRadiologyExam;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.DragDropEvent;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Named("diagnosticEditBean")
@ViewScoped
public class DiagnosticEditBean extends EntityEditPageBean<Diagnostic>
        implements Serializable {

    private static final long serialVersionUID = 6881223295727992603L;

    private List<SelectItem> examTypes;

    private List<String> selectedExamTypes;

    private List<SelectItem> sectors;

    private Long sectorId;

    private List<RadiologyExam> sourceSelected;

    private List<RadiologyExam> targetSelected;

    private List<RadiologyExam> sourceFiltered;

    private List<RadiologyExam> targetFiltered;

    private List<RadiologyExam> sourceExams;

    private List<RadiologyExam> targetExams;

    private List<RadiologyExam> baseTarget;

    private Boolean showSourceDroppable;

    private Boolean showTargetDroppable;

    private List<SelectItem> states;

    private EnableDisableEnum state;

    private String studyUID;

    private boolean showStudyUID;

    private String diagnosticCode;

    private Boolean useCardNumber;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        showSourceDroppable = Boolean.TRUE;
        showTargetDroppable = Boolean.TRUE;

        if (this.getEntity().isNew()) {
            this.setDiagnosticCode(getNextProgressiveId().toString());
        } else {
            this.setDiagnosticCode(this.getEntity().getCode());
        }
        this.setExamTypes(ComboboxHelper.fillList(ExamType.class, false));

        this.setSelectedExamTypes(new ArrayList<String>());

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getStudyUID())) {
            this.setStudyUID(this.getEntity().getStudyUID());
            this.showStudyUID = true;
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getExamTypes())) {
            for (ExamType et : this.getEntity().getExamTypes()) {
                this.getSelectedExamTypes().add(String.valueOf(et.getId()));
            }
        }
        this.setSectors(ComboboxHelper.fillList(Sector.class, true));
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getSector())) {
            setSectorId(this.getEntity().getSector().getId());
        }

        if (!ValidationHelper.isNullOrEmpty(this.getExamTypes())) {
            loadRadiologyExams();
        } else {
            this.setBaseTarget(new ArrayList<RadiologyExam>());
            this.setSourceExams(new ArrayList<RadiologyExam>());
            this.setTargetExams(new ArrayList<RadiologyExam>());
        }

        this.setStates(ComboboxHelper.fillList(EnableDisableEnum.class));
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getState())) {
            setState(this.getEntity().getState());
        }

        if (!ValidationHelper
                .isNullOrEmpty(this.getEntity().getUseCardNumber())) {
            this.setUseCardNumber(this.getEntity().getUseCardNumber());
        }
    }

    private void loadRadiologyExams() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        this.setBaseTarget(new ArrayList<RadiologyExam>());

        List<RadiologyExam> source = null;
        List<RadiologyExam> target = null;

        if (source == null) {
            source = new ArrayList<RadiologyExam>();
        }

        target = new ArrayList<RadiologyExam>();

        if (!this.getEntity().isNew()) {
            List<Long> examTypeIds = new ArrayList<Long>();
            examTypeIds.add(0L);
            if (!ValidationHelper.isNullOrEmpty(this.getSelectedExamTypes())) {
                for (String str : this.getSelectedExamTypes()) {
                    examTypeIds.add(Long.valueOf(Long.parseLong(str)));
                }
            }
            source = DaoManager.load(RadiologyExam.class, new Criterion[]{
                    Restrictions.in("examType.id", examTypeIds)
            });
            for (DiagnosticRadiologyExam re : this.getEntity().getRadiologyExams()) {
                target.add(re.getRadiologyExam());
            }
            for (RadiologyExam re : target) {
                source.remove(re);
            }
        }

        this.setSourceExams(source);
        this.setTargetExams(target);
    }

    public void changeShowSourceDroppable() {
        this.setShowSourceDroppable(Boolean.TRUE);
        this.setShowTargetDroppable(Boolean.FALSE);
    }

    public void changeShowTargetDroppable() {
        this.setShowTargetDroppable(Boolean.TRUE);
        this.setShowSourceDroppable(Boolean.FALSE);
    }

    public void handleExamTypeChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(this.getSelectedExamTypes())) {
            List<RadiologyExam> source = new ArrayList<RadiologyExam>();

            List<Long> examTypeIds = new ArrayList<Long>();
            if (!ValidationHelper.isNullOrEmpty(this.getSelectedExamTypes())) {
                for (String str : this.getSelectedExamTypes()) {
                    examTypeIds.add(Long.valueOf(Long.parseLong(str)));
                }
            }
            source = DaoManager.load(RadiologyExam.class, new Criterion[]{
                    Restrictions.in("examType.id", examTypeIds)
            });

            this.setSourceExams(source);
        } else {
            this.setSourceExams(new ArrayList<RadiologyExam>());
        }

        this.setTargetExams(new ArrayList<RadiologyExam>());
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
        changeShowTargetDroppable();
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
        if (getTargetFiltered() != null) {
            getTargetFiltered().clear();
        }
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
        if (ValidationHelper.isNullOrEmpty(getEntity().getDescription())) {
            addRequiredFieldExeption("form:description");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getAeTitle())) {
            addRequiredFieldExeption("form:aeTitle");
        }

        if (ValidationHelper.isNullOrEmpty(this.getSelectedExamTypes())) {
            this.addRequiredFieldExeption("form:examTypes");
        }

        if (ValidationHelper.isNullOrEmpty(this.getSectorId())) {
            addRequiredFieldExeption("form:sector");
        }

        if (ValidationHelper
                .isNullOrEmpty(getEntity().getExamTypeDiagnostica())) {
            addRequiredFieldExeption("form:examTypeDiagnostica");
        }

        if (ValidationHelper.isNullOrEmpty(this.getTargetExams())) {
            addFieldExeption("form:target", "examsIsRequired");
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("validationFailed"),
                    ResourcesHelper.getValidation("examsListIsEmpty"));
        }

        if (ValidationHelper.isNullOrEmpty(this.getState())) {
            addRequiredFieldExeption("form:state");
        }

        if (isShowStudyUID()
                && ValidationHelper.isNullOrEmpty(this.getStudyUID())) {
            addRequiredFieldExeption("form:studyUID");
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        this.getEntity().setCode(this.getDiagnosticCode());

        List<Long> idsExamTypes = new ArrayList<Long>();
        for (String str : this.getSelectedExamTypes()) {
            idsExamTypes.add(Long.valueOf(Long.parseLong(str)));
        }

        this.getEntity().setExamTypes(
                DaoManager.load(ExamType.class, new Criterion[]{
                        Restrictions.in("id", idsExamTypes.toArray(new Long[0]))
                }));

        this.getEntity().setSector(
                DaoManager.get(Sector.class, this.getSectorId()));

        this.getEntity().setState(this.getState());

        if (this.isShowStudyUID()
                && !ValidationHelper.isNullOrEmpty(this.getStudyUID())) {
            this.getEntity().setStudyUID(this.getStudyUID());
        } else {
            this.getEntity().setStudyUID(null);
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDicomPort())) {
            this.getEntity().setDicomPort(null);
        }

        if (!ValidationHelper.isNullOrEmpty(getUseCardNumber())
                && getUseCardNumber().booleanValue()) {
            this.getEntity().setUseCardNumber(getUseCardNumber());

            String progressiveNumber = DiagnosticHelper
                    .getNextProgressiveNumber();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            Long progressiveNumberYear = Long.valueOf(calendar
                    .get(Calendar.YEAR));

            this.getEntity().setCardNumber(progressiveNumber);
            this.getEntity().setCardNumberYear(progressiveNumberYear);
        } else {
            this.getEntity().setUseCardNumber(Boolean.FALSE);

            this.getEntity().setCardNumber(null);
            this.getEntity().setCardNumberYear(null);
        }

        DaoManager.save(this.getEntity());

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getRadiologyExams())) {
            processDuplicateAndRemovingExams(this.getEntity().getRadiologyExams(), this.getTargetExams());
        } else {
            getEntity().setRadiologyExams(new ArrayList<DiagnosticRadiologyExam>());
            for (RadiologyExam radiologyExam : getTargetExams()) {
                DiagnosticRadiologyExam relation = new DiagnosticRadiologyExam(getEntity(), radiologyExam);
                getEntity().getRadiologyExams().add(relation);
                DaoManager.save(relation);
            }
        }
    }

    private void processDuplicateAndRemovingExams(List<DiagnosticRadiologyExam> entityExams,
                                                  List<RadiologyExam> examsToAdd) throws PersistenceBeanException {
        for (RadiologyExam examToCheckForDuplication : examsToAdd) {
            boolean contains = true;
            for (DiagnosticRadiologyExam entityExam : entityExams) {
                if (entityExam.getRadiologyExam().getId().equals(examToCheckForDuplication.getId())) {
                    contains = false;
                }
            }
            if (contains) {
                DiagnosticRadiologyExam diagnosticRadiologyExamNew = new DiagnosticRadiologyExam(getEntity(), examToCheckForDuplication);
                DaoManager.save(diagnosticRadiologyExamNew);
                entityExams.add(diagnosticRadiologyExamNew);
            }
        }
        List<Long> idsToRemove = new ArrayList<>();
        for (DiagnosticRadiologyExam exam : entityExams) {
            boolean needToRemove = true;
            for (RadiologyExam examToAdd : examsToAdd) {
                if (examToAdd.getId().equals(exam.getRadiologyExam().getId())) {
                    needToRemove = false;
                }
            }
            if (needToRemove) {
                idsToRemove.add(exam.getRadiologyExam().getId());
            }
        }
        List<DiagnosticRadiologyExam> copied = new ArrayList<>();
        copied.addAll(entityExams);
        for (DiagnosticRadiologyExam exam : copied) {
            if (idsToRemove.contains(exam.getRadiologyExam().getId())) {
                DaoManager.remove(exam);
            }
        }
    }

    private Integer getNextProgressiveId() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return (Integer) DaoManager
                .getSession()
                .createQuery(
                        "select coalesce(max(to_number(code))+1,1) from Diagnostic")
                .uniqueResult();
    }

    public List<SelectItem> getExamTypes() {
        return examTypes;
    }

    public void setExamTypes(List<SelectItem> examTypes) {
        this.examTypes = examTypes;
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

    public List<String> getSelectedExamTypes() {
        return selectedExamTypes;
    }

    public void setSelectedExamTypes(List<String> selectedExamTypes) {
        this.selectedExamTypes = selectedExamTypes;
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

    public List<RadiologyExam> getSourceExams() {
        return sourceExams;
    }

    public void setSourceExams(List<RadiologyExam> sourceExams) {
        this.sourceExams = sourceExams;
    }

    public List<RadiologyExam> getTargetExams() {
        return targetExams;
    }

    public void setTargetExams(List<RadiologyExam> targetExams) {
        this.targetExams = targetExams;
    }

    public List<RadiologyExam> getBaseTarget() {
        return baseTarget;
    }

    public void setBaseTarget(List<RadiologyExam> baseTarget) {
        this.baseTarget = baseTarget;
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

    public List<SelectItem> getStates() {
        return states;
    }

    public void setStates(List<SelectItem> states) {
        this.states = states;
    }

    public EnableDisableEnum getState() {
        return state;
    }

    public void setState(EnableDisableEnum state) {
        this.state = state;
    }

    public boolean isShowStudyUID() {
        return showStudyUID;
    }

    public void setShowStudyUID(boolean showStudyUID) {
        this.showStudyUID = showStudyUID;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public String getDiagnosticCode() {
        return diagnosticCode;
    }

    public void setDiagnosticCode(String diagnosticCode) {
        this.diagnosticCode = diagnosticCode;
    }

    public Boolean getUseCardNumber() {
        return useCardNumber;
    }

    public void setUseCardNumber(Boolean useCardNumber) {
        this.useCardNumber = useCardNumber;
    }
}
