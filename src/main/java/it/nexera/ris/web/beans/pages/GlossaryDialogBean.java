package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.SelectItemHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Glossary;
import it.nexera.ris.persistence.beans.entities.domain.GlossaryExam;
import it.nexera.ris.persistence.beans.entities.domain.GlossaryResult;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.web.beans.BaseEntityPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("glossaryDialogBean")
@ViewScoped
public class GlossaryDialogBean extends BaseEntityPageBean implements
        Serializable {
    private static final long serialVersionUID = -3526229077157007807L;

    private List<SelectItem> examTypes;

    private Long selectedExamTypeId;

    private List<SelectItem> exams;

    private String selectedExam;

    private String newExamValue;

    private String currentExamValue;

    private List<SelectItem> results;

    private String selectedResult;

    private String newResultValue;

    private String currentResultValue;

    private String title;

    private String code;

    private String editorValue;

    private List<Glossary> glossaries;

    private List<Glossary> allGlossaries;

    private Long currentGlossaryToDelete;

    private Long currentGlossaryToUpdate;

    private Boolean isNew;

    private Boolean isExamEdit;

    private Boolean isResultEdit;

    @Override
    protected void onConstruct() {
        try {
            setAllGlossaries(new ArrayList<Glossary>());

            setExamTypes(new ArrayList<SelectItem>());
            getExamTypes().add(SelectItemHelper.getNotSelected());

            setResults(new ArrayList<SelectItem>());
            getResults().add(SelectItemHelper.getNotSelected());

            List<ExamType> examTypes = DaoManager.load(ExamType.class);
            if (!ValidationHelper.isNullOrEmpty(examTypes)) {
                for (ExamType d : examTypes) {
                    getExamTypes().add(
                            new SelectItem(d.getId(), d.getDescription()));
                }
            }

            fillGlossaries();
            fillExams();
            setIsNew(Boolean.TRUE);
            setIsExamEdit(Boolean.FALSE);
            setIsResultEdit(Boolean.FALSE);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void newGlossary() throws HibernateException,
            PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        this.cleanValidation();
        if (ValidationHelper.isNullOrEmpty(this.getCode())) {
            this.addRequiredFieldExeption("form:code");
        }
        if (ValidationHelper.isNullOrEmpty(this.getTitle())) {
            this.addRequiredFieldExeption("form:title");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEditorValue())) {
            this.addRequiredFieldExeption("form:reportEditor");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getCode())
                && !ValidationHelper.isNullOrEmpty(this.getTitle())
                && !ValidationHelper.isNullOrEmpty(this.getEditorValue())) {
            if (ValidationHelper.isNullOrEmpty(DaoManager.get(
                    Glossary.class,
                    new Criterion[]{
                            Restrictions.eq("medicId", this.getCurrentUser()
                                    .getId()),
                            Restrictions.eq("code", this.getCode())
                    }))
                    || getIsEdit().booleanValue()) {
                Glossary glossary = null;
                if (getIsEdit().booleanValue()) {
                    glossary = DaoManager.get(
                            Glossary.class,
                            new Criterion[]{
                                    Restrictions.eq("medicId", this
                                            .getCurrentUser().getId()),
                                    Restrictions.eq("id",
                                            getCurrentGlossaryToUpdate())
                            });
                    if (ValidationHelper.isNullOrEmpty(glossary)) {
                        glossary = new Glossary();
                    }
                    setIsNew(Boolean.TRUE);
                } else {
                    glossary = new Glossary();
                }
                glossary.setCode(this.getCode());
                glossary.setTitle(this.getTitle());
                glossary.setMedicId(this.getCurrentUser().getId());
                glossary.setExamType(DaoManager.get(ExamType.class,
                        new Criterion[]{
                                Restrictions.eq("id", this.getSelectedExamTypeId())
                        }));
                if (!ValidationHelper.isNullOrEmpty(this.getSelectedExam())) {
                    glossary.setExam(this.getSelectedExam());
                }
                if (!ValidationHelper.isNullOrEmpty(this.getSelectedResult())) {
                    glossary.setResult(this.getSelectedResult());
                }
                glossary.setText(this.getEditorValue());

                DaoManager.save(glossary, true);

                fillGlossaries();
                setTitle("");
                setCode("");
                setEditorValue("");
                //close();
            } else {
                addFieldExeption("form:code", "codeAlreadyInUse");
            }
        }
    }

    public void examChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        getResults().clear();
        getResults().add(SelectItemHelper.getNotSelected());
        if (!ValidationHelper.isNullOrEmpty(getSelectedExam())) {
            GlossaryExam glossaryExam = DaoManager.get(
                    GlossaryExam.class,
                    new Criterion[]{
                            Restrictions.eq("exam", this.getSelectedExam()),
                            Restrictions.eq("medicId", this.getCurrentUser()
                                    .getId())
                    });
            if (!ValidationHelper.isNullOrEmpty(glossaryExam)) {
                for (GlossaryResult gr : glossaryExam.getResults()) {
                    getResults().add(
                            new SelectItem(gr.getValue(), gr.getValue()));
                }
            }
        }
        setSelectedResult("");
        sortGlossaries();
    }

    public void glossaryLoad() throws HibernateException,
            PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        setIsNew(Boolean.FALSE);
        Glossary glossary = DaoManager.get(Glossary.class, new Criterion[]{
                Restrictions.eq("medicId", this.getCurrentUser().getId()),
                Restrictions.eq("id", this.getCurrentGlossaryToUpdate())
        });

        if (!ValidationHelper.isNullOrEmpty(glossary.getExamType())) {
            setSelectedExamTypeId(glossary.getExamType().getId());
        } else {
            setSelectedExamTypeId(Long.valueOf(0));
        }

        if (!ValidationHelper.isNullOrEmpty(glossary.getExam())) {
            setSelectedExam(glossary.getExam());
        } else {
            setSelectedExam("");
        }

        examChange();

        if (!ValidationHelper.isNullOrEmpty(glossary.getResult())) {
            setSelectedResult(glossary.getResult());
        } else {
            setSelectedResult("");
        }

        setTitle(glossary.getTitle());
        setCode(glossary.getCode());
        setEditorValue(glossary.getText());
    }

    public void glossaryDelete() throws HibernateException,
            PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getCurrentGlossaryToDelete())) {
            DaoManager.remove(DaoManager.get(Glossary.class, new Criterion[]{
                    Restrictions.eq("medicId", this.getCurrentUser().getId()),
                    Restrictions.eq("id", this.getCurrentGlossaryToDelete())
            }), true);
            fillGlossaries();
        }
    }

    public void fillGlossaries() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        setAllGlossaries(DaoManager.load(Glossary.class, new Criterion[]{
                Restrictions.eq("medicId", this.getCurrentUser().getId())
        }));
        if (ValidationHelper.isNullOrEmpty(getAllGlossaries())) {
            setAllGlossaries(new ArrayList<Glossary>());
        }

        sortGlossaries();
    }

    public void sortGlossaries() {
        setGlossaries(new ArrayList<Glossary>());
        getGlossaries().addAll(getAllGlossaries());

        List<Long> idsToRemove = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(this.getSelectedExamTypeId())) {
            for (Glossary g : getGlossaries()) {
                if (!ValidationHelper.isNullOrEmpty(g.getExamType())) {
                    if (!g.getExamType().getId()
                            .equals(this.getSelectedExamTypeId())) {
                        idsToRemove.add(g.getId());
                    }
                } else {
                    idsToRemove.add(g.getId());
                }
            }
        }

        if (!ValidationHelper.isNullOrEmpty(this.getSelectedExam())) {
            for (Glossary g : getGlossaries()) {
                if (!ValidationHelper.isNullOrEmpty(g.getExam())) {
                    if (!g.getExam().equals(this.getSelectedExam())) {
                        idsToRemove.add(g.getId());
                    }
                } else {
                    idsToRemove.add(g.getId());
                }
            }
        }

        if (!ValidationHelper.isNullOrEmpty(this.getSelectedResult())) {
            for (Glossary g : getGlossaries()) {
                if (!ValidationHelper.isNullOrEmpty(g.getResult())) {
                    if (!g.getResult().equals(this.getSelectedResult())) {
                        idsToRemove.add(g.getId());
                    }
                } else {
                    idsToRemove.add(g.getId());
                }
            }
        }

        List<Glossary> tempGlossaryList = new ArrayList<Glossary>();
        tempGlossaryList.addAll(getGlossaries());
        for (Glossary g : tempGlossaryList) {
            if (idsToRemove.contains(g.getId())) {
                getGlossaries().remove(g);
            }
        }
    }

    public void fillExams() throws HibernateException, IllegalAccessException,
            PersistenceBeanException {
        setExams(new ArrayList<SelectItem>());
        getExams().add(SelectItemHelper.getNotSelected());

        List<GlossaryExam> glossaryExams = DaoManager.load(GlossaryExam.class,
                new Criterion[]{
                        Restrictions.eq("medicId", this.getCurrentUser().getId())
                });
        if (!ValidationHelper.isNullOrEmpty(glossaryExams)) {
            for (GlossaryExam ge : glossaryExams) {
                getExams().add(new SelectItem(ge.getExam(), ge.getExam()));
            }
        }
    }

    public void addExam() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(this.getNewExamValue())) {
            GlossaryExam glossaryExam = null;
            List<GlossaryExam> glossaryExams = DaoManager.load(
                    GlossaryExam.class, new Criterion[]{
                            Restrictions.eq("medicId", this.getCurrentUser()
                                    .getId())
                    });
            if (!ValidationHelper.isNullOrEmpty(glossaryExams)) {
                for (GlossaryExam ge : glossaryExams) {
                    if (ge.getExam().equals(this.getNewExamValue())) {
                        glossaryExam = ge;
                        break;
                    }
                }
                if (ValidationHelper.isNullOrEmpty(glossaryExam)) {
                    for (GlossaryExam ge : glossaryExams) {
                        if (ge.getExam().equals(this.getCurrentExamValue())) {
                            glossaryExam = ge;
                            break;
                        }
                    }
                }
            }
            if (ValidationHelper.isNullOrEmpty(glossaryExam)) {
                glossaryExam = new GlossaryExam();
                glossaryExam.setMedicId(this.getCurrentUser().getId());
            }
            glossaryExam.setExam(getNewExamValue());

            DaoManager.save(glossaryExam, true);
            fillExams();
            examChange();
            exitExamEdit();
        }
    }

    public void addResult() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(this.getNewResultValue())) {
            GlossaryResult glossaryResult = null;
            List<GlossaryResult> glossaryResults = DaoManager.load(
                    GlossaryResult.class, new Criterion[]{
                            Restrictions.eq("glossaryExam", DaoManager.get(
                                    GlossaryExam.class,
                                    new Criterion[]{
                                            Restrictions.eq("exam",
                                                    this.getSelectedExam()),
                                            Restrictions.eq("medicId", this
                                                    .getCurrentUser().getId())
                                    }))
                    });
            if (!ValidationHelper.isNullOrEmpty(glossaryResults)) {
                for (GlossaryResult gr : glossaryResults) {
                    if (gr.getValue().equals(this.getNewResultValue())) {
                        glossaryResult = gr;
                        break;
                    }
                }
                if (ValidationHelper.isNullOrEmpty(glossaryResult)) {
                    for (GlossaryResult gr : glossaryResults) {
                        if (gr.getValue().equals(this.getCurrentResultValue())) {
                            glossaryResult = gr;
                            break;
                        }
                    }
                }
            }
            if (ValidationHelper.isNullOrEmpty(glossaryResult)) {
                glossaryResult = new GlossaryResult();
                glossaryResult
                        .setGlossaryExam(DaoManager.get(
                                GlossaryExam.class,
                                new Criterion[]{
                                        Restrictions.eq("exam",
                                                this.getSelectedExam()),
                                        Restrictions.eq("medicId", this
                                                .getCurrentUser().getId())
                                }));
            }

            glossaryResult.setValue(this.getNewResultValue());
            DaoManager.save(glossaryResult, true);
            examChange();
            exitResultEdit();
        }
    }

    public void removeExam() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(this.getCurrentExamValue())) {
            DaoManager.remove(DaoManager
                    .get(GlossaryExam.class,
                            new Criterion[]{
                                    Restrictions.eq("exam",
                                            this.getCurrentExamValue()),
                                    Restrictions.eq("medicId", this
                                            .getCurrentUser().getId())
                            }), true);
            fillExams();
            setSelectedExam(null);
            examChange();
            setCurrentExamValue(null);
        }
    }

    public void removeResult() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(this.getCurrentResultValue())) {
            DaoManager.remove(DaoManager.get(
                    GlossaryResult.class,
                    new Criterion[]{
                            Restrictions.eq("value",
                                    this.getCurrentResultValue()),
                            Restrictions.eq("glossaryExam", DaoManager.get(
                                    GlossaryExam.class,
                                    new Criterion[]{
                                            Restrictions.eq("exam",
                                                    this.getSelectedExam()),
                                            Restrictions.eq("medicId", this
                                                    .getCurrentUser().getId())
                                    }))
                    }), true);
            examChange();
            setCurrentResultValue(null);
        }
    }

    public void showExamAdd() {
        setIsExamEdit(Boolean.TRUE);
    }

    public void showExamEdit() {
        setNewExamValue(getSelectedExam());
        showExamAdd();
    }

    public void exitExamEdit() {
        setNewExamValue(null);
        setCurrentExamValue(null);
        setIsExamEdit(Boolean.FALSE);
    }

    public void showResultAdd() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedExam())) {
            setIsResultEdit(Boolean.TRUE);
        }
    }

    public void showResultEdit() {
        setNewResultValue(getSelectedResult());
        showResultAdd();
    }

    public void exitResultEdit() {
        setNewResultValue(null);
        setCurrentResultValue(null);
        setIsResultEdit(Boolean.FALSE);
    }

    public List<SelectItem> getExamTypes() {
        return examTypes;
    }

    public void setExamTypes(List<SelectItem> examTypes) {
        this.examTypes = examTypes;
    }

    public Long getSelectedExamTypeId() {
        return selectedExamTypeId;
    }

    public void setSelectedExamTypeId(Long selectedExamTypeId) {
        this.selectedExamTypeId = selectedExamTypeId;
    }

    public List<SelectItem> getExams() {
        return exams;
    }

    public void setExams(List<SelectItem> exams) {
        this.exams = exams;
    }

    public String getSelectedExam() {
        return selectedExam;
    }

    public void setSelectedExam(String selectedExam) {
        this.selectedExam = selectedExam;
    }

    public List<SelectItem> getResults() {
        return results;
    }

    public void setResults(List<SelectItem> results) {
        this.results = results;
    }

    public String getSelectedResult() {
        return selectedResult;
    }

    public void setSelectedResult(String selectedResult) {
        this.selectedResult = selectedResult;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEditorValue() {
        return editorValue;
    }

    public void setEditorValue(String editorValue) {
        this.editorValue = editorValue;
    }

    public List<Glossary> getGlossaries() {
        return glossaries;
    }

    public void setGlossaries(List<Glossary> glossaries) {
        this.glossaries = glossaries;
    }

    public Long getCurrentGlossaryToDelete() {
        return currentGlossaryToDelete;
    }

    public void setCurrentGlossaryToDelete(Long currentGlossaryToDelete) {
        this.currentGlossaryToDelete = currentGlossaryToDelete;
    }

    public Long getCurrentGlossaryToUpdate() {
        return currentGlossaryToUpdate;
    }

    public void setCurrentGlossaryToUpdate(Long currentGlossaryToUpdate) {
        this.currentGlossaryToUpdate = currentGlossaryToUpdate;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    public String getNewExamValue() {
        return newExamValue;
    }

    public void setNewExamValue(String newExamValue) {
        this.newExamValue = newExamValue;
    }

    public String getNewResultValue() {
        return newResultValue;
    }

    public void setNewResultValue(String newResultValue) {
        this.newResultValue = newResultValue;
    }

    public Boolean getIsExamEdit() {
        return isExamEdit;
    }

    public void setIsExamEdit(Boolean isExamEdit) {
        this.isExamEdit = isExamEdit;
    }

    public Boolean getIsResultEdit() {
        return isResultEdit;
    }

    public void setIsResultEdit(Boolean isResultEdit) {
        this.isResultEdit = isResultEdit;
    }

    public String getCurrentExamValue() {
        return currentExamValue;
    }

    public void setCurrentExamValue(String currentExamValue) {
        this.currentExamValue = currentExamValue;
    }

    public String getCurrentResultValue() {
        return currentResultValue;
    }

    public void setCurrentResultValue(String currentResultValue) {
        this.currentResultValue = currentResultValue;
    }

    public List<Glossary> getAllGlossaries() {
        return allGlossaries;
    }

    public void setAllGlossaries(List<Glossary> allGlossaries) {
        this.allGlossaries = allGlossaries;
    }

    public Boolean getIsSelectExam() {
        return ValidationHelper.isNullOrEmpty(this.getSelectedExam()) ? Boolean.TRUE
                : Boolean.FALSE;
    }

    public Boolean getIsSelectResult() {
        return ValidationHelper.isNullOrEmpty(this.getSelectedResult()) ? Boolean.TRUE
                : Boolean.FALSE;
    }

    public Boolean getIsEdit() {
        return isNew.booleanValue() ? Boolean.FALSE : Boolean.TRUE;
    }

}
