package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.entities.domain.readonly.PatientShort;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("patientSearchListBean")
@ViewScoped
public class PatientSearchListBean extends EntityLazyListPageBean<PatientShort>
        implements Serializable {

    private static final long serialVersionUID = 8629852936269154527L;

    private String patientSurname;

    private String patientName;

    private Date patientBirthDate;

    private String patientFiscalCode;

    private Boolean makeSearch;

    private PatientShort patient;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {

    }

    public void addPatient() {
        this.getSession().put("fromPatientSearchList", Boolean.TRUE);
        this.getSession().put("patientSurname", patientSurname);
        this.getSession().put("patientName", patientName);
        this.getSession().put("patientDob", patientBirthDate);
        this.getSession().put("patientFiscalCode", patientFiscalCode);

        RedirectHelper.goTo(PageTypes.PATIENT_EDIT);
    }

    public void loadPatients() {
        try {
            List<Criterion> searchCriterion = new ArrayList<Criterion>();
            if (!ValidationHelper.isNullOrEmpty(patientSurname)) {
                searchCriterion.add(Restrictions.ilike("surname",
                        patientSurname, MatchMode.START));
            }

            if (!ValidationHelper.isNullOrEmpty(patientName)) {
                searchCriterion.add(Restrictions.ilike("name", patientName,
                        MatchMode.START));
            }

            if (!ValidationHelper.isNullOrEmpty(patientFiscalCode)) {
                searchCriterion.add(Restrictions.ilike("fiscalCode",
                        patientFiscalCode, MatchMode.START));
            }

            if (!ValidationHelper.isNullOrEmpty(patientBirthDate)) {
                searchCriterion.add(Restrictions.eq("birthDate",
                        patientBirthDate));
            }
            this.loadList(PatientShort.class,
                    searchCriterion.toArray(new Criterion[0]), new Order[]
                            {Order.asc("name")});
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void searchPatients() {
        this.cleanValidation();
        validateParameters();
        if (!getMakeSearch().booleanValue()) {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    ResourcesHelper.getValidation("parameters"),
                    ResourcesHelper.getValidation("allParametersAreEmpty"));
        } else {
            loadPatients();
            executeJS("updatePatientsPanel();");
        }
    }

    public void validateParameters() {
        if (ValidationHelper.isNullOrEmpty(patientSurname)
                && ValidationHelper.isNullOrEmpty(patientName)
                && ValidationHelper.isNullOrEmpty(patientFiscalCode)
                && ValidationHelper.isNullOrEmpty(patientBirthDate)) {
            this.setMakeSearch(Boolean.FALSE);
        } else {
            this.setMakeSearch(Boolean.TRUE);
        }
    }

    public void requestCreation() {
        if (!ValidationHelper.isNullOrEmpty(getPatient())) {
            this.getSession().put("patientId", getPatient().getId());

            RedirectHelper.goTo(PageTypes.RADIOLOGY_EXAM_REQUEST);
        }
    }

    public String getPatientSurname() {
        return patientSurname;
    }

    public void setPatientSurname(String patientSurname) {
        this.patientSurname = patientSurname;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Date getPatientBirthDate() {
        return patientBirthDate;
    }

    public void setPatientBirthDate(Date patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    public String getPatientFiscalCode() {
        return patientFiscalCode;
    }

    public void setPatientFiscalCode(String patientFiscalCode) {
        this.patientFiscalCode = patientFiscalCode;
    }

    public Boolean getMakeSearch() {
        return makeSearch;
    }

    public void setMakeSearch(Boolean makeSearch) {
        this.makeSearch = makeSearch;
    }

    public PatientShort getPatient() {
        return patient;
    }

    public void setPatient(PatientShort patient) {
        this.patient = patient;
    }

}
