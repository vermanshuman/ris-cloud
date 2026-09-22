package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.MaritalStatuses;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.PatientHistoryActionType;
import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Patient;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.SelectEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("patientEditBean")
@ViewScoped
public class PatientEditBean extends EntityEditPageBean<Patient> implements
        Serializable {
    private static final long serialVersionUID = 5266236796429350096L;

    private static final String ITALIA_COUNTRY_DESCRIPTION = "ITALIA";

    private List<SelectItem> sexes;

    private SexTypes sexType;

    private List<SelectItem> maritalStatuses;

    private MaritalStatuses maritalStatus;

    private List<SelectItem> nationalities;

    private Long nationalityId;

    private List<SelectItem> provinces;

    private Long birthProvinceId;

    private List<SelectItem> birthCities;

    private Long birthCityId;

    private Long addressProvinceId;

    private Long address1ProvinceId;

    private List<SelectItem> addressCities;

    private Long addressCityId;

    private List<SelectItem> address1Cities;

    private Long address1CityId;

    private List<List<City>> generalCities;

    private List<Province> generalProvinces;

    private List<SelectItem> asls;

    private List<SelectItem> aslRegions;

    private Long aslId;

    private Long aslRegionId;

    private String dateBirth;

    private Boolean calendarEventFired;

    private boolean renderMenu;

    private Boolean fromPatientSearchList;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (this.getSession().get("fromPatientSearchList") == Boolean.TRUE) {
            setFromPatientSearchList(Boolean.TRUE);
            this.setRenderMenu(false);
        } else {
            this.setRenderMenu(true);
        }

        parseSession();

        if (!this.getEntity().isNew() && !this.isPostback()) {
            if (this.getEntity().getSexType() != null) {
                this.setSexType(this.getEntity().getSexType());
            }

            if (this.getEntity().getMaritalStatus() != null) {
                this.setMaritalStatus(this.getEntity().getMaritalStatus());
            }

            if (this.getEntity().getNationality() != null) {
                this.setNationalityId(this.getEntity().getNationality().getId());
            }

            if (this.getEntity().getBirthProvince() != null) {
                this.setBirthProvinceId(this.getEntity().getBirthProvince()
                        .getId());
            }

            if (this.getEntity().getBirthCity() != null) {
                this.setBirthCityId(this.getEntity().getBirthCity().getId());
            }

            if (this.getEntity().getProvince() != null) {
                this.setAddressProvinceId(this.getEntity().getProvince()
                        .getId());
            }

            if (this.getEntity().getCity() != null) {
                this.setAddressCityId(this.getEntity().getCity().getId());
            }

            if (this.getEntity().getProvince1() != null) {
                this.setAddress1ProvinceId(this.getEntity().getProvince1()
                        .getId());
            }

            if (this.getEntity().getCity1() != null) {
                this.setAddress1CityId(this.getEntity().getCity1().getId());
            }

            if (this.getEntity().getAsl() != null) {
                this.setAslId(this.getEntity().getAsl().getId());
            }

            if (this.getEntity().getRegion() != null) {
                this.setAslRegionId(this.getEntity().getRegion().getId());
            }
        }

        this.fillLists();
    }

    public void removeSessionAttributeAfterRender(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            clearSessionFields();
        }
    }

    @Override
    public void goBack() {
        try {
            if (!Boolean.TRUE.equals(this.getFromPatientSearchList())) {
                RedirectHelper.goTo(PageTypes.PATIENT_LIST);
            } else {
                RedirectHelper.goTo(PageTypes.RADIOLOGY_EXAM_REQUEST);
                this.getSession().put("patientId", this.getEntity().getId());
            }
        } finally {
            clearSessionFields();
        }
    }

    private void clearSessionFields() {
        this.getSession().remove("fromPatientSearchList");
        this.getSession().remove("patientName");
        this.getSession().remove("patientSurname");
        this.getSession().remove("patientDob");
        this.getSession().remove("patientFiscalCode");
    }

    private void parseSession() {
        if (this.getSession().get("fromPatientSearchList") != null) {
            if (!ValidationHelper.isNullOrEmpty((String) this.getSession().get(
                    "patientName"))) {
                this.getEntity().setName(
                        ((String) this.getSession().get("patientName"))
                                .toUpperCase());
            }
            if (!ValidationHelper.isNullOrEmpty((String) this.getSession().get(
                    "patientSurname"))) {
                this.getEntity().setSurname(
                        ((String) this.getSession().get("patientSurname"))
                                .toUpperCase());
            }
            if (!ValidationHelper.isNullOrEmpty((Date) this.getSession().get(
                    "patientDob"))) {
                this.getEntity().setBirthDate(
                        (Date) this.getSession().get("patientDob"));
            }
            if (!ValidationHelper.isNullOrEmpty((String) this.getSession().get(
                    "patientFiscalCode"))) {
                this.getEntity().setFiscalCode(
                        ((String) this.getSession().get("patientFiscalCode"))
                                .toUpperCase());
            }
        }
    }

    private void fillLists() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setSexes(ComboboxHelper.fillList(SexTypes.values()));
        this.setMaritalStatuses(ComboboxHelper.fillList(MaritalStatuses
                .values()));
        this.setNationalities(ComboboxHelper.fillList(Nationality.class,
                Order.asc("description")));
        this.generalProvinces = DaoManager.load(Province.class, new Criterion[]
                {Restrictions.or(Restrictions.eq("patientInstance", false),
                        Restrictions.isNull("patientInstance"))}, Order
                .asc("description"));
        if (this.generalProvinces == null) {
            this.generalProvinces = new ArrayList<Province>();
        }
        this.setProvinces(ComboboxHelper.fillList(this.generalProvinces, true));

        this.generalCities = new ArrayList<List<City>>();

        List<City> cities;
        cities = DaoManager.load(City.class, new Criterion[]
                        {Restrictions.eq("province.id", this.getBirthProvinceId())},
                Order.asc("description"));
        if (cities == null) {
            cities = new ArrayList<City>();
        }
        this.setBirthCities(ComboboxHelper.fillList(cities, true));
        this.generalCities.add(cities);

        cities = DaoManager.load(City.class, new Criterion[]
                        {Restrictions.eq("province.id", this.getAddressProvinceId())},
                Order.asc("description"));
        if (cities == null) {
            cities = new ArrayList<City>();
        }
        this.setAddressCities(ComboboxHelper.fillList(cities, true));
        this.generalCities.add(cities);

        cities = DaoManager.load(City.class, new Criterion[]
                        {Restrictions.eq("province.id", this.getAddress1ProvinceId())},
                Order.asc("description"));
        if (cities == null) {
            cities = new ArrayList<City>();
        }
        this.setAddress1Cities(ComboboxHelper.fillList(cities, true));
        this.generalCities.add(cities);

        this.asls = ComboboxHelper.fillList(Asl.class);

        this.aslRegions = ComboboxHelper.fillList(AslRegion.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getSurname())) {
            addRequiredFieldExeption("form:tabs:surname");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldExeption("form:tabs:name");
        }

        if (ValidationHelper.isNullOrEmpty(this.getSexType())) {
            addRequiredFieldExeption("form:tabs:sex");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getFiscalCode())) {
            addRequiredFieldExeption("form:tabs:fiscalCode");
        }

        if (ValidationHelper.isNullOrEmpty(this.getNationalityId())) {
            addRequiredFieldExeption("form:tabs:nationality");
        }

        boolean bNationalityItalia = false;
        try {
            Nationality n = DaoManager.get(Nationality.class, new Criterion[]
                    {Restrictions.eq("description", "ITALIA")});

            if (n != null) {
                bNationalityItalia = n.getId().equals(this.getNationalityId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (this.getNationalityId() != null
                && this.getNationalityId().intValue() != 0l) {
            Nationality current = null;
            try {
                current = DaoManager.get(Nationality.class,
                        this.getNationalityId());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            if (current != null) {
                if (ITALIA_COUNTRY_DESCRIPTION.equalsIgnoreCase(current
                        .getDescription())) {
                    bNationalityItalia = true;
                }
            }
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getFiscalCode())) {
            if (bNationalityItalia) {
                addRequiredFieldExeption("form:tabs:fiscalCode");
            }
        } else if (!((this.getEntity().getFiscalCode().length() == 11 && ValidationHelper
                .checkCorrectFormatByExpression("^[0-9]+$", this.getEntity()
                        .getFiscalCode()))
                || (this.getEntity().getFiscalCode().length() == 16 && ValidationHelper
                .checkCorrectFormatByExpression(
                        "^[a-zA-Z]{6}[0-9]{2}[a-zA-Z]{1}[0-9]{2}[a-zA-Z]{1}[0-9]{3}[a-zA-Z]{1}$",
                        this.getEntity().getFiscalCode())) || (this
                .getEntity().getFiscalCode().length() == 16 && ValidationHelper
                .checkCorrectFormatByExpression("^(STP|ENI|SCF)[0-9]{13}$", this
                        .getEntity().getFiscalCode())))) {
            addFieldExeption("form:tabs:fiscalCode", "fiscalCodeWrongFormat");
        } else if (!ValidationHelper.isUnique(Patient.class, "fiscalCode", this
                .getEntity().getFiscalCode(), this.getEntity().getId())) {
            addFieldExeption("form:tabs:fiscalCode", "fiscalCodeAlreadyInUse");
        }

        if (this.getEntity().getBirthDate() == null) {
            addRequiredFieldExeption("form:tabs:birthDate");
        } else if (this.getEntity().getBirthDate().compareTo(new Date()) > 0) {
            addFieldExeption("form:tabs:birthDate", "wrongBirthDate");
        }

        if (bNationalityItalia
                && ValidationHelper.isNullOrEmpty(this.getBirthProvinceId())) {
            addRequiredFieldExeption("form:tabs:birthProvince");
        }

        if (bNationalityItalia
                && ValidationHelper.isNullOrEmpty(this.getBirthCityId())) {
            addRequiredFieldExeption("form:tabs:birthCity");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity()
                .getHealthCardNumber())
                && !ValidationHelper.checkCorrectFormatByExpression(
                "^[0-9]{20}$", this.getEntity().getHealthCardNumber())) {
            addFieldExeption("form:tabs:healthCardNumber",
                    "healthCardNumberWrongFormat");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getMail())
                && !ValidationHelper.checkMailCorrectFormat(this.getEntity()
                .getMail())) {
            addFieldExeption("form:tabs:email", "emailWrongFormat");
        }

    }

    public void handleDateSelect(SelectEvent<Date> event) {
        this.getEntity().setBirthDate(event.getObject());
        this.setCalendarEventFired(Boolean.TRUE);
        makeFiscalCode();
        this.setCalendarEventFired(Boolean.FALSE);
    }

    public void handleDateSelect(AjaxBehaviorEvent event) {
        // not worked
        // this.getEntity().setBirthDate(event.getDate());
        /*
         * worked Date d = (Date) ((UIInput) event.getComponent()).getValue();
         * 
         * if (!ValidationHelper.isNullOrEmpty(d)) {
         * this.getEntity().setBirthDate(d); }
         */
        this.setCalendarEventFired(Boolean.TRUE);
        makeFiscalCode();
        this.setCalendarEventFired(Boolean.FALSE);
    }

    public void makeFiscalCode() {
        City city = null;
        Nationality nationality = null;
        try {
            if (!ValidationHelper.isNullOrEmpty(this.getBirthCityId())) {
                city = DaoManager.get(City.class, this.getBirthCityId());
            }
            if (!ValidationHelper.isNullOrEmpty(this.getNationalityId())) {
                nationality = DaoManager.get(Nationality.class,
                        this.getNationalityId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getSurname())) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(nationality)) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(this.getSexType())) {
            return;
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getBirthDate())) {
            return;
        }

        if (ValidationHelper.isNullOrEmpty(city)
                && nationality.getDescription().equalsIgnoreCase(
                Nationality.ITALY)) {
            return;
        }
        try {
            if (nationality.getDescription()
                    .equalsIgnoreCase(Nationality.ITALY)) {
                this.getEntity().setFiscalCode(
                        CalcoloCodiceFiscale.calcola(
                                this.getEntity().getName(), this.getEntity()
                                        .getSurname(), this.getEntity()
                                        .getBirthDate(), city.getCfis(), this
                                        .getSexType()));
            } else {
                this.getEntity().setFiscalCode(
                        CalcoloCodiceFiscale.calcola(
                                this.getEntity().getName(), this.getEntity()
                                        .getSurname(), this.getEntity()
                                        .getBirthDate(), nationality.getCfis(),
                                this.getSexType()));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void handleBirthProvinceChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setBirthCities(ComboboxHelper.fillList(City.class,
                Order.asc("description"),
                Restrictions.eq("province.id", this.getBirthProvinceId())));
    }

    public void handleAddressProvinceChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setAddressCities(ComboboxHelper.fillList(City.class,
                Order.asc("description"),
                Restrictions.eq("province.id", this.getAddressProvinceId())));
    }

    public void handleAddress1ProvinceChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setAddress1Cities(ComboboxHelper.fillList(City.class,
                Order.asc("description"),
                Restrictions.eq("province.id", this.getAddress1ProvinceId())));
    }

    public void handleAddressCityChange() throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        City city = DaoManager.get(City.class, this.getAddressCityId());
        if (city != null) {
            if (city.getAsl() != null) {
                this.setAslId(city.getAsl().getId());
            }

            if (city.getAslRegion() != null) {
                this.setAslRegionId(city.getAslRegion().getId());
            }
        }
    }

    private City getCity(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id) && this.generalCities != null) {
            for (List<City> list : this.generalCities) {
                if (!ValidationHelper.isNullOrEmpty(list)) {
                    for (City city : list) {
                        if (city.getId().equals(id)) {
                            return city;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Province getProvince(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)
                && this.generalProvinces != null)
            for (Province province : this.generalProvinces) {
                if (province.getId().equals(id)) {
                    return province;
                }
            }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        onLoad();

        this.getEntity().setSexType(this.getSexType());

        if (!ValidationHelper.isNullOrEmpty(this.getMaritalStatus())) {
            this.getEntity().setMaritalStatus(this.getMaritalStatus());
        }

        if (!ValidationHelper.isNullOrEmpty(this.getNationalityId())) {
            this.getEntity().setNationality(
                    DaoManager.get(Nationality.class, this.getNationalityId()));
        }

        this.getEntity().setBirthProvince(
                this.getProvince(this.getBirthProvinceId()));

        this.getEntity().setBirthCity(this.getCity(this.getBirthCityId()));

        if (this.getEntity().getBirthProvince() != null
                && this.getEntity().getBirthCity() != null) {
            this.getEntity().setIstatCodeBirth(
                    this.getEntity().getBirthProvince().getCode()
                            + this.getEntity().getBirthCity().getCode());
        } else {
            this.getEntity().setIstatCodeBirth(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
            this.getEntity().setProvince(
                    this.getProvince(this.getAddressProvinceId()));
        }
        if (!ValidationHelper.isNullOrEmpty(this.getAddressCityId())) {
            this.getEntity().setCity(this.getCity(this.getAddressCityId()));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getProvince())
                && !ValidationHelper.isNullOrEmpty(this.getEntity().getCity())) {
            this.getEntity().setIstatCode(
                    this.getEntity().getProvince().getCode()
                            + this.getEntity().getCity().getCode());
        } else {
            this.getEntity().setIstatCode(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getAddress1ProvinceId())) {
            this.getEntity().setProvince1(
                    this.getProvince(this.getAddress1ProvinceId()));
        }
        if (!ValidationHelper.isNullOrEmpty(this.getAddress1CityId())) {
            this.getEntity().setCity1(this.getCity(this.getAddress1CityId()));
        }
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getProvince1())
                && !ValidationHelper.isNullOrEmpty(this.getEntity().getCity1())) {
            this.getEntity().setIstatCode1(
                    this.getEntity().getProvince1().getCode()
                            + this.getEntity().getCity1().getCode());
        } else {
            this.getEntity().setIstatCode1(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getAslId())) {
            this.getEntity().setAsl(DaoManager.get(Asl.class, this.getAslId()));
        }

        if (!ValidationHelper.isNullOrEmpty(this.getAslRegionId())) {
            this.getEntity().setRegion(
                    DaoManager.get(AslRegion.class, this.getAslRegionId()));
        }

        boolean wasNew = this.getEntity().isNew();
        DaoManager.save(this.getEntity());

        if (wasNew) {
            PatientHistoryHelper.createRecord(this.getEntity(),
                    PatientHistoryActionType.CREATE);
        } else {
            PatientHistoryHelper.createRecord(this.getEntity(),
                    PatientHistoryActionType.MODIFY);
        }
    }

    public List<SelectItem> getSexes() {
        return sexes;
    }

    public void setSexes(List<SelectItem> sexes) {
        this.sexes = sexes;
    }

    public List<SelectItem> getMaritalStatuses() {
        return maritalStatuses;
    }

    public void setMaritalStatuses(List<SelectItem> maritalStatuses) {
        this.maritalStatuses = maritalStatuses;
    }

    public MaritalStatuses getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatuses maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public List<SelectItem> getNationalities() {
        return nationalities;
    }

    public void setNationalities(List<SelectItem> nationalityTypes) {
        this.nationalities = nationalityTypes;
    }

    public Long getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(Long nationalityId) {
        this.nationalityId = nationalityId;
    }

    public Date getToday() {
        return new Date();
    }

    public List<SelectItem> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<SelectItem> provinces) {
        this.provinces = provinces;
    }

    public Long getBirthProvinceId() {
        return birthProvinceId;
    }

    public void setBirthProvinceId(Long birthProvinceId) {
        this.birthProvinceId = birthProvinceId;
    }

    public List<SelectItem> getBirthCities() {
        return birthCities;
    }

    public void setBirthCities(List<SelectItem> cities) {
        this.birthCities = cities;
    }

    public Long getBirthCityId() {
        return birthCityId;
    }

    public void setBirthCityId(Long birthCityId) {
        this.birthCityId = birthCityId;
    }

    public List<SelectItem> getAddressCities() {
        return addressCities;
    }

    public void setAddressCities(List<SelectItem> addressCities) {
        this.addressCities = addressCities;
    }

    public Long getAddressCityId() {
        return addressCityId;
    }

    public void setAddressCityId(Long addressCityId) {
        this.addressCityId = addressCityId;
    }

    public Long getAddressProvinceId() {
        return addressProvinceId;
    }

    public void setAddressProvinceId(Long addressProvinceId) {
        this.addressProvinceId = addressProvinceId;
    }

    public Long getAddress1ProvinceId() {
        return address1ProvinceId;
    }

    public void setAddress1ProvinceId(Long address1ProvinceId) {
        this.address1ProvinceId = address1ProvinceId;
    }

    public List<SelectItem> getAddress1Cities() {
        return address1Cities;
    }

    public void setAddress1Cities(List<SelectItem> address1Cities) {
        this.address1Cities = address1Cities;
    }

    public Long getAddress1CityId() {
        return address1CityId;
    }

    public void setAddress1CityId(Long address1CityId) {
        this.address1CityId = address1CityId;
    }

    public List<SelectItem> getAsls() {
        return asls;
    }

    public void setAsls(List<SelectItem> asls) {
        this.asls = asls;
    }

    public List<SelectItem> getAslRegions() {
        return aslRegions;
    }

    public void setAslRegions(List<SelectItem> aslRegions) {
        this.aslRegions = aslRegions;
    }

    public Long getAslId() {
        return aslId;
    }

    public void setAslId(Long aslId) {
        this.aslId = aslId;
    }

    public Long getAslRegionId() {
        return aslRegionId;
    }

    public void setAslRegionId(Long aslRegionId) {
        this.aslRegionId = aslRegionId;
    }

    public String getDateBirth() {
        return dateBirth;
    }

    public void setDateBirth(String dateBirth) {
        this.dateBirth = dateBirth;
    }

    public Boolean getCalendarEventFired() {
        return calendarEventFired;
    }

    public void setCalendarEventFired(Boolean calendarEventFired) {
        this.calendarEventFired = calendarEventFired;
    }

    public SexTypes getSexType() {
        return sexType;
    }

    public void setSexType(SexTypes sexType) {
        this.sexType = sexType;
    }

    public boolean getRenderMenu() {
        return renderMenu;
    }

    public void setRenderMenu(boolean renderMenu) {
        this.renderMenu = renderMenu;
    }

    public Boolean getFromPatientSearchList() {
        return fromPatientSearchList;
    }

    public void setFromPatientSearchList(Boolean fromPatientSearchList) {
        this.fromPatientSearchList = fromPatientSearchList;
    }

}
