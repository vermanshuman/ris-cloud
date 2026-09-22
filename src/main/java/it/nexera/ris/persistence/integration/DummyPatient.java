package it.nexera.ris.persistence.integration;

import it.nexera.ris.common.enums.MaritalStatuses;
import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.Patient;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;

import java.sql.ResultSet;
import java.util.Date;

public class DummyPatient {
    private String family_name;

    private String given_name;

    private Date dob;

    private String birth_Info_Code;

    private String birth_place;

    private String cap_birth;

    private String birth_provincia;

    private String sex;

    private String nation_Code;

    private String nazionalita;

    private String stato_civile;

    private String codice_fiscale;

    private String e_mail;

    private String telephone;

    private String cellulare;

    private String telephone3;

    private String id_patient;

    private String tessera_sanitaria;

    private String regione;

    private String asl;

    private String medico;

    private String indirizzo_residenza;

    private String residence_Info_Code;

    private String citta_residenza;

    private String provincia_residenza;

    private String cap_residenza;

    private String quartiere_residenza;

    private String indirizzo_domicilio;

    private String addressInfo_Code;

    private String citta_domicilio;

    private String provincia_domicilio;

    private String cap_domicilio;

    private String tsan;

    private boolean isShort = false;

    protected final Logger log = LogManager.getLogger(this.getClass());

    private boolean bothAddresses;

    private String health_card_number;

    private String istat_residenza;

    private String istat_domicilio;

    private String istat_code_nationality;

    private String num_indirizzo_residenza;

    private String num_indirizzo_domicilio;

    private String external_patient_id;

    public DummyPatient() {
        super();
    }

    public DummyPatient(ResultSet set) {
        completePatient(set);
    }

    private void completePatient(ResultSet set) {
        try {
            this.setFamily_name(set.getString("family_name"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setGiven_name(set.getString("given_name"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setDob(set.getDate("dob"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setBirth_Info_Code(set.getString("Birth_Info_Code"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setBirth_provincia(set.getString("birth_provincia"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setBirth_place(set.getString("birth_place"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setCap_birth(set.getString("cap_birth"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setSex(set.getString("sex"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setNazionalita(set.getString("nazionalita"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setStato_civile(set.getString("stato_civile"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setCodice_fiscale(set.getString("codice_fiscale"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setE_mail(set.getString("e_mail"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setTelephone(set.getString("telephone"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setCellulare(set.getString("cellulare"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setTelephone3(set.getString("telephone3"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setId_patient(set.getString("id_patient"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setTessera_sanitaria(set.getString("tessera_sanitaria"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setMedico(set.getString("medico"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setIndirizzo_residenza(set.getString("indirizzo_residenza"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setResidence_Info_Code(set.getString("Residence_Info_Code"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setProvincia_residenza(set.getString("provincia_residenza"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setCitta_residenza(set.getString("citta_residenza"));

        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setCap_residenza(set.getString("cap_residenza"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {

            this.setQuartiere_residenza(set.getString("quartiere_residenza"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setAddressInfo_Code(set.getString("addressInfo_Code"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setProvincia_domicilio(set.getString("provincia_domicilio"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {

            this.setCitta_domicilio(set.getString("citta_domicilio"));

        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setCap_domicilio(set.getString("cap_domicilio"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        try {
            this.setIndirizzo_domicilio(set.getString("indirizzo_domicilio"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setRegione(set.getString("regione"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setAsl(set.getString("asl"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setTsan(set.getString("tsan"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.setExternal_patient_id(set.getString("external_patient_id"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

    }

    public DummyPatient(ResultSet set, boolean isShortMode) {
        if (isShortMode) {
            this.setShort(true);
            try {
                this.setFamily_name(set.getString("family_name"));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            try {
                this.setGiven_name(set.getString("given_name"));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            try {
                this.setDob(set.getDate("dob"));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            try {
                this.setCodice_fiscale(set.getString("codice_fiscale"));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            try {
                this.setId_patient(set.getString("id_patient"));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            completePatient(set);
        }
    }

    public Patient toPatient(Long id, Patient patient, Session session) {
        if (patient != null) {
            if (patient.getIsNew()) {
                patient.setId(id);
            }
            if (this.isShort()) {
                patient.setSurname(this.getFamily_name());
                patient.setName(this.getGiven_name());
                patient.setBirthDate(this.getDob());
                patient.setFiscalCode(this.getCodice_fiscale());
                return patient;
            } else {
                try {
                    patient.setSurname(this.getFamily_name());
                    patient.setName(this.getGiven_name());
                    patient.setBirthDate(this.getDob());
                    patient.setCapBirth(this.getCap_birth());
                    patient.setFiscalCode(this.getCodice_fiscale());
                    patient.setMail(this.getE_mail());
                    patient.setPhone(this.getTelephone());
                    patient.setCell(this.getCellulare());
                    patient.setSecondPhone(this.getTelephone3());
                    patient.setDoctor(this.getMedico());
                    patient.setAddress(this.getIndirizzo_residenza());
                    patient.setAddressNumber(this.getNum_indirizzo_residenza());
                    patient.setCap(this.getCap_residenza());
                    patient.setDistrict(this.getQuartiere_residenza());
                    patient.setCap1(this.getCap_domicilio());
                    patient.setAddress1(this.getIndirizzo_domicilio());
                    patient.setAddressNumber1(this.getNum_indirizzo_domicilio());
                    patient.setFiscalCodeType(this.getTsan());
                    patient.setHealthCardNumber(this.getHealth_card_number());
                    patient.setIstatCode(this.getIstat_residenza());
                    patient.setIstatCode1(this.getIstat_domicilio());
                    patient.setIstatCodeNationality(this.getIstat_code_nationality());
                    patient.setExternalPatientId(this.getExternal_patient_id());

                    if (this.isBothAddresses()) {
                        patient.setShowAdditionalAddress(Boolean.TRUE);
                    }

                    try {
                        String birthCityCode = this.getBirth_Info_Code();
                        String birthCityDesc = this.getCitta_residenza();
                        String birthProvince = this.getProvincia_residenza();
                        if (!ValidationHelper.isNullOrEmpty(birthCityCode)
                                && !birthCityCode.equalsIgnoreCase("null")) {
                            City bCity = ConnectionManager.get(City.class,
                                    Restrictions.eq("code", birthCityCode)
                                            .ignoreCase(), session);
                            if (bCity != null) {
                                patient.setBirthCity(bCity);
                                patient.setBirthProvince(bCity.getProvince());
                            }
                        } else if (!ValidationHelper.isNullOrEmpty(birthCityDesc)
                                && !birthCityDesc.equalsIgnoreCase("null")) {
                            City bCity = ConnectionManager.get(
                                    City.class,
                                    Restrictions.eq("description",
                                            birthCityDesc).ignoreCase(),
                                    session);
                            if (bCity != null) {
                                patient.setBirthCity(bCity);
                                patient.setBirthProvince(bCity.getProvince());
                            }
                        } else if (!ValidationHelper.isNullOrEmpty(birthProvince)
                                && !birthProvince.equalsIgnoreCase("null")) {
                            Province bProvince = ConnectionManager.get(
                                    Province.class,
                                    Restrictions.eq("code", birthProvince)
                                            .ignoreCase(), session);
                            if (bProvince != null) {
                                patient.setBirthProvince(bProvince);
                            }
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }

                    try {
                        String str = this.getSex();
                        if (!ValidationHelper.isNullOrEmpty(str)
                                && str != "null") {
                            if (str.equalsIgnoreCase("F")) {
                                patient.setSexType(SexTypes.FEMALE);
                            } else if (str.equalsIgnoreCase("M")) {
                                patient.setSexType(SexTypes.MALE);
                            } else {
                                patient.setSexType(SexTypes.NOT_IDENTIFY);
                            }
                        } else {
                            patient.setSexType(SexTypes.NOT_IDENTIFY);
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    } finally {
                        if (patient != null
                                && ValidationHelper.isNullOrEmpty(patient
                                .getSexType())) {
                            patient.setSexType(SexTypes.NOT_IDENTIFY);
                        }
                    }

                    try {
                        String str = this.getNazionalita();
                        if (str != null) {
                            Nationality type = ConnectionManager.get(
                                    Nationality.class, new Criterion[]{
                                            Restrictions.eq("description", str)
                                                    .ignoreCase()
                                    }, session);
                            if (type != null) {
                                patient.setNationality(type);
                            }
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }

                    try {
                        String str = this.getStato_civile();
                        MaritalStatuses type = null;
                        if (str != null) {
                            type = MaritalStatuses.valueOf(str);
                        }
                        if (type != null) {
                            patient.setMaritalStatus(type);
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }

                    try {
                        String residenceCityCode = this.getResidence_Info_Code();
                        String residenceCityDesc = this.getCitta_residenza();
                        String residenceProvince = this.getProvincia_residenza();
                        City rCity = null;
                        SimpleExpression cfisExpression = Restrictions.eq("cfis", residenceCityCode);
                        SimpleExpression descriptionExpression = Restrictions.eq("description", residenceCityDesc);
                        if (!ValidationHelper.isNullOrEmpty(residenceCityCode) && !residenceCityCode.equalsIgnoreCase("null")
                                && !ValidationHelper.isNullOrEmpty(residenceCityDesc) && !residenceCityDesc.equalsIgnoreCase("null")) {
                            rCity = ConnectionManager.get(City.class, new Criterion[]{
                                    cfisExpression, descriptionExpression
                            }, session);
                        } else if (!ValidationHelper.isNullOrEmpty(residenceCityCode)
                                && !residenceCityCode.equalsIgnoreCase("null")) {
                            rCity = ConnectionManager.get(City.class, cfisExpression, session);
                        } else if (!ValidationHelper.isNullOrEmpty(residenceCityDesc)
                                && !residenceCityDesc.equalsIgnoreCase("null")) {
                            rCity = ConnectionManager.get(City.class, descriptionExpression, session);
                        } else if (!ValidationHelper.isNullOrEmpty(residenceProvince)
                                && !residenceProvince.equalsIgnoreCase("null")) {
                            Province rProvince = ConnectionManager.get(Province.class,
                                    Restrictions.eq("code", residenceProvince).ignoreCase(), session);
                            if (rProvince != null) {
                                patient.setProvince(rProvince);
                            }
                        }
                        if (rCity != null) {
                            patient.setCity(rCity);
                            patient.setProvince(rCity.getProvince());
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }

                    try {
                        String domicilioCityCode = this.getAddressInfo_Code();
                        String domicilioCityDesc = this.getCitta_domicilio();
                        String domicilioProvince = this
                                .getProvincia_domicilio();
                        if (!ValidationHelper.isNullOrEmpty(domicilioCityCode)
                                && !domicilioCityCode.equalsIgnoreCase("null")) {
                            City dCity = ConnectionManager.get(City.class,
                                    Restrictions.eq("code", domicilioCityCode)
                                            .ignoreCase(), session);
                            if (dCity != null) {
                                patient.setCity1(dCity);
                                patient.setProvince1(dCity.getProvince());
                            }
                        } else if (!ValidationHelper
                                .isNullOrEmpty(domicilioCityDesc)
                                && !domicilioCityDesc.equalsIgnoreCase("null")) {
                            City dCity = ConnectionManager.get(
                                    City.class,
                                    Restrictions.eq("description",
                                            domicilioCityDesc).ignoreCase(),
                                    session);
                            if (dCity != null) {
                                patient.setCity1(dCity);
                                patient.setProvince1(dCity.getProvince());
                            }
                        } else if (!ValidationHelper
                                .isNullOrEmpty(domicilioProvince)
                                && !domicilioProvince.equalsIgnoreCase("null")) {
                            Province dProvince = ConnectionManager.get(
                                    Province.class,
                                    Restrictions.eq("code", domicilioProvince)
                                            .ignoreCase(), session);
                            if (dProvince != null) {
                                patient.setProvince1(dProvince);
                            }
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }

                    try {
                        if (patient.getBirthCity() != null
                                && patient.getBirthCity().getAsl() != null
                                && patient.getBirthCity().getAslRegion() != null) {
                            patient.setAsl(patient.getBirthCity().getAsl());
                            patient.setRegion(patient.getBirthCity()
                                    .getAslRegion());
                        } else {
                            try {

                                String regione = this.getRegione();
                                String als = this.getAsl();
                                if (!ValidationHelper.isNullOrEmpty(regione)
                                        && !regione.equalsIgnoreCase("null")) {
                                    AslRegion region = ConnectionManager.get(
                                            AslRegion.class,
                                            new Criterion[]{
                                                    Restrictions.eq("description",
                                                            regione).ignoreCase()
                                            }, session);

                                    if (region != null) {
                                        patient.setRegion(region);
                                    }

                                }
                                if (!ValidationHelper.isNullOrEmpty(als)
                                        && !als.equalsIgnoreCase("null")) {
                                    Asl asl = ConnectionManager.get(
                                            Asl.class,
                                            new Criterion[]{
                                                    Restrictions.eq("description",
                                                            als).ignoreCase()
                                            }, session);

                                    if (asl != null) {
                                        patient.setAsl(asl);
                                    }

                                }
                            } catch (Exception e) {
                                LogHelper.log(log, e);
                            }
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                return patient;
            }
        }
        return patient;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getBirth_Info_Code() {
        return birth_Info_Code;
    }

    public void setBirth_Info_Code(String birth_Info_Code) {
        this.birth_Info_Code = birth_Info_Code;
    }

    public String getBirth_place() {
        return birth_place;
    }

    public void setBirth_place(String birth_place) {
        this.birth_place = birth_place;
    }

    public String getCap_birth() {
        return cap_birth;
    }

    public void setCap_birth(String cap_birth) {
        this.cap_birth = cap_birth;
    }

    public String getBirth_provincia() {
        return birth_provincia;
    }

    public void setBirth_provincia(String birth_provincia) {
        this.birth_provincia = birth_provincia;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation_Code() {
        return nation_Code;
    }

    public void setNation_Code(String nation_Code) {
        this.nation_Code = nation_Code;
    }

    public String getNazionalita() {
        return nazionalita;
    }

    public void setNazionalita(String nazionalita) {
        this.nazionalita = nazionalita;
    }

    public String getStato_civile() {
        return stato_civile;
    }

    public void setStato_civile(String stato_civile) {
        this.stato_civile = stato_civile;
    }

    public String getCodice_fiscale() {
        return codice_fiscale;
    }

    public void setCodice_fiscale(String codice_fiscale) {
        this.codice_fiscale = codice_fiscale;
    }

    public String getE_mail() {
        return e_mail;
    }

    public void setE_mail(String e_mail) {
        this.e_mail = e_mail;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCellulare() {
        return cellulare;
    }

    public void setCellulare(String cellulare) {
        this.cellulare = cellulare;
    }

    public String getTelephone3() {
        return telephone3;
    }

    public void setTelephone3(String telephone3) {
        this.telephone3 = telephone3;
    }

    public String getId_patient() {
        return id_patient;
    }

    public void setId_patient(String id_patient) {
        this.id_patient = id_patient;
    }

    public String getTessera_sanitaria() {
        return tessera_sanitaria;
    }

    public void setTessera_sanitaria(String tessera_sanitaria) {
        this.tessera_sanitaria = tessera_sanitaria;
    }

    public String getRegione() {
        return regione;
    }

    public void setRegione(String regione) {
        this.regione = regione;
    }

    public String getAsl() {
        return asl;
    }

    public void setAsl(String asl) {
        this.asl = asl;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public String getIndirizzo_residenza() {
        return indirizzo_residenza;
    }

    public void setIndirizzo_residenza(String indirizzo_residenza) {
        this.indirizzo_residenza = indirizzo_residenza;
    }

    public String getResidence_Info_Code() {
        return residence_Info_Code;
    }

    public void setResidence_Info_Code(String residence_Info_Code) {
        this.residence_Info_Code = residence_Info_Code;
    }

    public String getCitta_residenza() {
        return citta_residenza;
    }

    public void setCitta_residenza(String citta_residenza) {
        this.citta_residenza = citta_residenza;
    }

    public String getProvincia_residenza() {
        return provincia_residenza;
    }

    public void setProvincia_residenza(String provincia_residenza) {
        this.provincia_residenza = provincia_residenza;
    }

    public String getCap_residenza() {
        return cap_residenza;
    }

    public void setCap_residenza(String cap_residenza) {
        this.cap_residenza = cap_residenza;
    }

    public String getQuartiere_residenza() {
        return quartiere_residenza;
    }

    public void setQuartiere_residenza(String quartiere_residenza) {
        this.quartiere_residenza = quartiere_residenza;
    }

    public String getIndirizzo_domicilio() {
        return indirizzo_domicilio;
    }

    public void setIndirizzo_domicilio(String indirizzo_domicilio) {
        this.indirizzo_domicilio = indirizzo_domicilio;
    }

    public String getAddressInfo_Code() {
        return addressInfo_Code;
    }

    public void setAddressInfo_Code(String addressInfo_Code) {
        this.addressInfo_Code = addressInfo_Code;
    }

    public String getCitta_domicilio() {
        return citta_domicilio;
    }

    public void setCitta_domicilio(String citta_domicilio) {
        this.citta_domicilio = citta_domicilio;
    }

    public String getProvincia_domicilio() {
        return provincia_domicilio;
    }

    public void setProvincia_domicilio(String provincia_domicilio) {
        this.provincia_domicilio = provincia_domicilio;
    }

    public String getCap_domicilio() {
        return cap_domicilio;
    }

    public void setCap_domicilio(String cap_domicilio) {
        this.cap_domicilio = cap_domicilio;
    }

    public boolean isShort() {
        return isShort;
    }

    public void setShort(boolean isShort) {
        this.isShort = isShort;
    }

    public String getTsan() {
        return tsan;
    }

    public void setTsan(String tsan) {
        this.tsan = tsan;
    }

    public boolean isBothAddresses() {
        return bothAddresses;
    }

    public void setBothAddresses(boolean bothAddresses) {
        this.bothAddresses = bothAddresses;
    }

    public String getHealth_card_number() {
        return health_card_number;
    }

    public void setHealth_card_number(String health_card_number) {
        this.health_card_number = health_card_number;
    }

    public String getIstat_residenza() {
        return istat_residenza;
    }

    public void setIstat_residenza(String istat_residenza) {
        this.istat_residenza = istat_residenza;
    }

    public String getIstat_domicilio() {
        return istat_domicilio;
    }

    public void setIstat_domicilio(String istat_domicilio) {
        this.istat_domicilio = istat_domicilio;
    }

    public String getIstat_code_nationality() {
        return istat_code_nationality;
    }

    public void setIstat_code_nationality(String istat_code_nationality) {
        this.istat_code_nationality = istat_code_nationality;
    }

    public String getNum_indirizzo_residenza() {
        return num_indirizzo_residenza;
    }

    public void setNum_indirizzo_residenza(String num_indirizzo_residenza) {
        this.num_indirizzo_residenza = num_indirizzo_residenza;
    }

    public String getNum_indirizzo_domicilio() {
        return num_indirizzo_domicilio;
    }

    public void setNum_indirizzo_domicilio(String num_indirizzo_domicilio) {
        this.num_indirizzo_domicilio = num_indirizzo_domicilio;
    }

    public String getExternal_patient_id() {
        return external_patient_id;
    }

    public void setExternal_patient_id(String external_patient_id) {
        this.external_patient_id = external_patient_id;
    }
}
