package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.MaritalStatuses;
import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.web.converters.BaseConverter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@javax.persistence.Entity
@Table(name = "patient")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "PATIENT_SEQ", allocationSize = 1)
public class Patient extends IndexedEntity implements Comparable<Patient>,
        Serializable {

    private static final long serialVersionUID = -6304034073590084586L;

    private static transient final Logger log = LogManager.getLogger(Patient.class);

    @Column(name = "show_additional_address")
    private Boolean showAdditionalAddress = false;

    @Column
    private String address;

    @Column(name = "address_1")
    private String address1;

    @Column(name = "address_number")
    private String addressNumber;

    @Column(name = "address_number_1")
    private String addressNumber1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asl_id", foreignKey = @ForeignKey(name = "FK_PATIENT_ASL"))
    private Asl asl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", foreignKey = @ForeignKey(name = "FK_PATIENT_REGION"))
    private AslRegion region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birth_city_id", foreignKey = @ForeignKey(name = "FK_PATIENT_BIRTH_CITY"))
    private City birthCity;

    @Column(name = "birth_city_id", insertable = false, updatable = false)
    private Long birthCityId;

    @Column(name = "birth_date")
    private Date birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birth_province_id", foreignKey = @ForeignKey(name = "FK_PATIENT_BIRTH_PROVINCE"))
    private Province birthProvince;

    @Column
    private String cap;

    @Column(name = "cap_1")
    private String cap1;

    @Column(name = "cap_birth")
    private String capBirth;

    @Column
    private String cell;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citi_id", foreignKey = @ForeignKey(name = "FK_PATIENT_CITY"))
    private City city;

    @Column(name = "citi_id", insertable = false, updatable = false)
    private Long cityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_1_id", foreignKey = @ForeignKey(name = "FK_PATIENT_CITY_1"))
    private City city1;

    @Column
    private String district;

    @Column(name = "district_1")
    private String district1;

    @Column
    private String doctor;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    @Column(name = "health_card_number")
    private String healthCardNumber;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<PatientActionHistory> history;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<RadiologyExamRequest> radiologyExamRequests;

    @Column(name = "istat_code")
    private String istatCode;

    @Column(name = "istat_code_1")
    private String istatCode1;

    @Column(name = "istat_code_birth")
    private String istatCodeBirth;

    @Column(name = "istat_code_nationality")
    private String istatCodeNationality;

    @Column
    private String mail;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatuses maritalStatus;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_id", foreignKey = @ForeignKey(name = "FK_PATIENT_NATIONALITY"))
    private Nationality nationality;

    @Column
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", foreignKey = @ForeignKey(name = "FK_PATIENT_PROVINCE"))
    private Province province;

    @Column(name = "province_id", insertable = false, updatable = false)
    private Long provinceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_1_id", foreignKey = @ForeignKey(name = "FK_PATIENT_PROVINCE_1"))
    private Province province1;

    @Column(name = "second_phone")
    private String secondPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex_type")
    private SexTypes sexType;

    @Column
    private String surname;

    @Column(name = "fiscal_code_type")
    private String fiscalCodeType;

    @OneToMany(mappedBy = "patient")
    private List<Allergy> allergies;

    @Column(name = "external_patient_id")
    private String externalPatientId;

    @Transient
    private String birthCityDescription;

    @Transient
    private String cityDescription;

    @Transient
    private String provinceDescription;


    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Patient o) {
        if (!ValidationHelper.isNullOrEmpty(this.getName())
                && !ValidationHelper.isNullOrEmpty(o.getName())) {
            return this.getName().compareTo(o.getName());
        }

        return 0;
    }

    public void fillMissingInfo(Patient p) {
        if (ValidationHelper.isNullOrEmpty(this.getAddress())) {
            this.setAddress(p.getAddress());
        }
        if (ValidationHelper.isNullOrEmpty(this.getAddress1())) {
            this.setAddress1(p.getAddress1());
        }
        if (ValidationHelper.isNullOrEmpty(this.getAddressNumber())) {
            this.setAddressNumber(p.getAddressNumber());
        }
        if (ValidationHelper.isNullOrEmpty(this.getAddressNumber1())) {
            this.setAddressNumber1(p.getAddressNumber1());
        }
        if (ValidationHelper.isNullOrEmpty(this.getAsl())) {
            this.setAsl(p.getAsl());
        }
        if (ValidationHelper.isNullOrEmpty(this.getBirthCity())) {
            this.setBirthCity(p.getBirthCity());
        }
        if (ValidationHelper.isNullOrEmpty(this.getBirthProvince())) {
            this.setBirthProvince(p.getBirthProvince());
        }
        if (ValidationHelper.isNullOrEmpty(this.getCap())) {
            this.setCap(p.getCap());
        }
        if (ValidationHelper.isNullOrEmpty(this.getCap1())) {
            this.setCap1(p.getCap1());
        }
        if (ValidationHelper.isNullOrEmpty(this.getCapBirth())) {
            this.setCapBirth(p.getCapBirth());
        }
        if (ValidationHelper.isNullOrEmpty(this.getCell())) {
            this.setCell(p.getCell());
        }
        if (ValidationHelper.isNullOrEmpty(this.getCity())) {
            this.setCity(p.getCity());
        }
        if (ValidationHelper.isNullOrEmpty(this.getCity1())) {
            this.setCity1(p.getCity1());
        }
        if (ValidationHelper.isNullOrEmpty(this.getDistrict())) {
            this.setDistrict(p.getDistrict());
        }
        if (ValidationHelper.isNullOrEmpty(this.getDistrict1())) {
            this.setDistrict1(p.getDistrict1());
        }
        if (ValidationHelper.isNullOrEmpty(this.getDoctor())) {
            this.setDoctor(p.getDoctor());
        }
        if (ValidationHelper.isNullOrEmpty(this.getFiscalCode())) {
            this.setFiscalCode(p.getFiscalCode());
        }
        if (ValidationHelper.isNullOrEmpty(this.getHealthCardNumber())) {
            this.setHealthCardNumber(p.getHealthCardNumber());
        }
        if (ValidationHelper.isNullOrEmpty(this.getIstatCode())) {
            this.setIstatCode(p.getIstatCode());
        }
        if (ValidationHelper.isNullOrEmpty(this.getIstatCode1())) {
            this.setIstatCode1(p.getIstatCode1());
        }
        if (ValidationHelper.isNullOrEmpty(this.getIstatCodeBirth())) {
            this.setIstatCodeBirth(p.getIstatCodeBirth());
        }
        if (ValidationHelper.isNullOrEmpty(this.getIstatCodeNationality())) {
            this.setIstatCodeNationality(p.getIstatCodeNationality());
        }
        if (ValidationHelper.isNullOrEmpty(this.getExternalPatientId())) {
            this.setExternalPatientId(p.getExternalPatientId());
        }
        if (ValidationHelper.isNullOrEmpty(this.getMail())) {
            this.setMail(p.getMail());
        }
        if (ValidationHelper.isNullOrEmpty(this.getMaritalStatus())) {
            this.setMaritalStatus(p.getMaritalStatus());
        }
        if (ValidationHelper.isNullOrEmpty(this.getName())) {
            this.setName(p.getName());
        }
        if (ValidationHelper.isNullOrEmpty(this.getNationality())) {
            this.setNationality(p.getNationality());
        }
        if (ValidationHelper.isNullOrEmpty(this.getPhone())) {
            this.setPhone(p.getPhone());
        }
        if (ValidationHelper.isNullOrEmpty(this.getProvince())) {
            this.setProvince(p.getProvince());
        }
        if (ValidationHelper.isNullOrEmpty(this.getProvince1())) {
            this.setProvince1(p.getProvince1());
        }
        if (ValidationHelper.isNullOrEmpty(this.getRegion())) {
            this.setRegion(p.getRegion());
        }
        if (ValidationHelper.isNullOrEmpty(this.getSecondPhone())) {
            this.setSecondPhone(p.getSecondPhone());
        }
        if (ValidationHelper.isNullOrEmpty(this.getSexType())) {
            this.setSexType(p.getSexType());
        }
        if (ValidationHelper.isNullOrEmpty(this.getShowAdditionalAddress())) {
            this.setShowAdditionalAddress(p.getShowAdditionalAddress());
        }
        if (ValidationHelper.isNullOrEmpty(this.getSurname())) {
            this.setSurname(p.getSurname());
        }
    }

    public String getBirthCityDescription() {
        if (birthCityDescription == null) {
            try {
                birthCityDescription = DaoManager.getField(City.class, "description", new Criterion[]{
                        Restrictions.eq("id", getBirthCityId())
                }, null);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return birthCityDescription;
    }

    public void setBirthCityDescription(String birthCityDescription) {
        this.birthCityDescription = birthCityDescription;
    }

    public String getCityDescription() {
        if (cityDescription == null) {
            try {
                cityDescription = DaoManager.getField(City.class, "description", new Criterion[]{
                        Restrictions.eq("id", getCityId())
                }, null);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return cityDescription;
    }

    public void setCityDescription(String cityDescription) {
        this.cityDescription = cityDescription;
    }

    public String getProvinceDescription() {
        if (provinceDescription == null) {
            try {
                provinceDescription = DaoManager.getField(Province.class, "description", new Criterion[]{
                        Restrictions.eq("id", getProvinceId())
                }, null);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return provinceDescription;
    }

    public void setProvinceDescription(String provinceDescription) {
        this.provinceDescription = provinceDescription;
    }

    public Long getBirthCityId() {
        return birthCityId;
    }

    public void setBirthCityId(Long birthCityId) {
        this.birthCityId = birthCityId;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public String getAddress() {
        return address;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public String getAddressNumber1() {
        return addressNumber1;
    }

    public City getBirthCity() {
        return birthCity;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getBirthDateText() {
        return BaseConverter.convertToDateString(birthDate);
    }

    public Province getBirthProvince() {
        return birthProvince;
    }

    public String getCap() {
        return cap;
    }

    public String getCap1() {
        return cap1;
    }

    public String getCapBirth() {
        return capBirth;
    }

    public String getCell() {
        return cell;
    }

    public City getCity() {
        return city;
    }

    public City getCity1() {
        return city1;
    }

    public String getDistrict() {
        return district;
    }

    public String getDistrict1() {
        return district1;
    }

    public String getDoctor() {
        return doctor;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    @Transient
    public String getFullName() {
        return String.format("%s %s",
                this.getSurname() == null ? "" : this.getSurname(),
                this.getName() == null ? "" : this.getName());
    }

    @Transient
    public Integer getAge() {
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTime(this.birthDate);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate
                .get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    public String getHealthCardNumber() {
        return healthCardNumber;
    }

    public List<PatientActionHistory> getHistory() {
        return history;
    }

    public String getIstatCode() {
        return istatCode;
    }

    public String getIstatCode1() {
        return istatCode1;
    }

    public String getIstatCodeBirth() {
        return istatCodeBirth;
    }

    public String getIstatCodeNationality() {
        return istatCodeNationality;
    }

    public String getMail() {
        return mail;
    }

    public String getName() {
        return name;
    }

    public Nationality getNationality() {
        return nationality;
    }

    public String getPhone() {
        return phone;
    }

    public Province getProvince() {
        return province;
    }

    public Province getProvince1() {
        return province1;
    }

    public String getSecondPhone() {
        return secondPhone;
    }

    public String getSurname() {
        return surname;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public void setAddressNumber1(String addressNumber1) {
        this.addressNumber1 = addressNumber1;
    }

    public void setBirthCity(City birthCity) {
        this.birthCity = birthCity;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public void setBirthProvince(Province birthProvince) {
        this.birthProvince = birthProvince;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public void setCap1(String cap1) {
        this.cap1 = cap1;
    }

    public void setCapBirth(String capBirth) {
        this.capBirth = capBirth;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setCity1(City city1) {
        this.city1 = city1;
    }

    public void setDistrict(String discrict) {
        this.district = discrict;
    }

    public void setDistrict1(String discrict1) {
        this.district1 = discrict1;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public void setHealthCardNumber(String healthCardNumber) {
        this.healthCardNumber = healthCardNumber;
    }

    public void setHistory(List<PatientActionHistory> history) {
        this.history = history;
    }

    public void setIstatCode(String istatCode) {
        this.istatCode = istatCode;
    }

    public void setIstatCode1(String istatCode1) {
        this.istatCode1 = istatCode1;
    }

    public void setIstatCodeBirth(String istatCodeBirth) {
        this.istatCodeBirth = istatCodeBirth;
    }

    public void setIstatCodeNationality(String istatCodeNationality) {
        this.istatCodeNationality = istatCodeNationality;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNationality(Nationality nationality) {
        this.nationality = nationality;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public void setProvince1(Province province1) {
        this.province1 = province1;
    }

    public void setSecondPhone(String secondPhone) {
        this.secondPhone = secondPhone;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getFullName();
    }

    public Boolean getShowAdditionalAddress() {
        return showAdditionalAddress;
    }

    public void setShowAdditionalAddress(Boolean showAdditionalAddress) {
        this.showAdditionalAddress = showAdditionalAddress;
    }

    public Asl getAsl() {
        return asl;
    }

    public void setAsl(Asl asl) {
        this.asl = asl;
    }

    public AslRegion getRegion() {
        return region;
    }

    public void setRegion(AslRegion region) {
        this.region = region;
    }

    public String getFiscalCodeType() {
        return fiscalCodeType;
    }

    public void setFiscalCodeType(String fiscalCodeType) {
        this.fiscalCodeType = fiscalCodeType;
    }

    public String getBloodGroup() {
        return " ";
    }

    public SexTypes getSexType() {
        return sexType;
    }

    @Transient
    public String getSexTypeShortValue() {
        return sexType.getShortValue();
    }

    public void setSexType(SexTypes sexType) {
        this.sexType = sexType;
    }

    public MaritalStatuses getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatuses maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public List<RadiologyExamRequest> getRadiologyExamRequests() {
        return radiologyExamRequests;
    }

    public void setRadiologyExamRequests(
            List<RadiologyExamRequest> radiologyExamRequests) {
        this.radiologyExamRequests = radiologyExamRequests;
    }

    public List<Allergy> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<Allergy> allergies) {
        this.allergies = allergies;
    }

    public String getExternalPatientId() {
        return externalPatientId;
    }

    public void setExternalPatientId(String externalPatientId) {
        this.externalPatientId = externalPatientId;
    }
}
