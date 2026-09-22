package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.DoseClass;
import it.nexera.ris.common.enums.EnableDisableEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named("radiologyExamEditBean")
@ViewScoped
public class RadiologyExamEditBean extends EntityEditPageBean<RadiologyExam>
        implements Serializable {
    private static final long serialVersionUID = -2974536582981433667L;

    private Long selectedExamTypeId;

    private List<SelectItem> examTypeList;

    private String regionalCode;

    private Double amount;

    private Double kWh;

    private Double mAS;

    private List<SelectItem> states;

    private EnableDisableEnum state;

    private List<SelectItem> doseClassList;

    private Long selectedDoseClass;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setExamTypeList(ComboboxHelper.fillList(ExamType.class, true));
        this.setDoseClassList(ComboboxHelper.fillList(DoseClass.class));
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getExamType())) {
            this.setSelectedExamTypeId(this.getEntity().getExamType().getId());
        }
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getDoseClass())) {
            this.setSelectedDoseClass(this.getEntity().getDoseClass().getId());
        }
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getRegionalCode())) {
            this.setRegionalCode(this.getEntity().getRegionalCode());
        }
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getAmount())) {
            this.setAmount(this.getEntity().getAmount());
        }
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getkWh())) {
            this.setkWh(this.getEntity().getkWh());
        }
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getmAS())) {
            this.setmAS(this.getEntity().getmAS());
        }
        this.setStates(ComboboxHelper.fillList(EnableDisableEnum.class));
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getState())) {
            setState(this.getEntity().getState());
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldExeption("form:code");
        } else if (!ValidationHelper.isUnique(RadiologyExam.class, "code", this
                .getEntity().getCode(), this.getEntityId())
                && this.getEntity().isNew()) {
            addFieldExeption("form:code", "codeAlreadyInUse");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getDescription())) {
            addRequiredFieldExeption("form:description");
        }

        if (ValidationHelper.isNullOrEmpty(getSelectedExamTypeId())) {
            addRequiredFieldExeption("form:examType");
        }

        if (ValidationHelper.isNullOrEmpty(getRegionalCode())) {
            addRequiredFieldExeption("form:regionalCode");
        }
        if (ValidationHelper.isNullOrEmpty(this.getState())) {
            addRequiredFieldExeption("form:state");
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        this.getEntity().setExamType(
                DaoManager.get(ExamType.class, this.getSelectedExamTypeId()));
        this.getEntity().setRegionalCode(this.getRegionalCode());
        this.getEntity().setAmount(this.getAmount());
        this.getEntity().setkWh(this.getkWh());
        this.getEntity().setmAS(this.getmAS());
        this.getEntity().setState(this.getState());
        this.getEntity().setDoseClass(DoseClass.findById(getSelectedDoseClass()));

        DaoManager.save(this.getEntity());
    }

    public Long getSelectedExamTypeId() {
        return selectedExamTypeId;
    }

    public void setSelectedExamTypeId(Long selectedExamTypeId) {
        this.selectedExamTypeId = selectedExamTypeId;
    }

    public List<SelectItem> getExamTypeList() {
        return examTypeList;
    }

    public void setExamTypeList(List<SelectItem> examTypeList) {
        this.examTypeList = examTypeList;
    }

    public String getRegionalCode() {
        return regionalCode;
    }

    public void setRegionalCode(String regionalCode) {
        this.regionalCode = regionalCode;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getkWh() {
        return kWh;
    }

    public void setkWh(Double kWh) {
        this.kWh = kWh;
    }

    public Double getmAS() {
        return mAS;
    }

    public void setmAS(Double mAS) {
        this.mAS = mAS;
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

    public List<SelectItem> getDoseClassList() {
        return doseClassList;
    }

    public void setDoseClassList(List<SelectItem> doseClassList) {
        this.doseClassList = doseClassList;
    }

    public Long getSelectedDoseClass() {
        return selectedDoseClass;
    }

    public void setSelectedDoseClass(Long selectedDoseClass) {
        this.selectedDoseClass = selectedDoseClass;
    }
}
