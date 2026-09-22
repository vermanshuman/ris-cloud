package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Patient;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityViewPageBean;
import org.hibernate.HibernateException;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("patientViewBean")
@ViewScoped
public class PatientViewBean extends EntityViewPageBean<Patient> implements
        Serializable {

    private static final long serialVersionUID = -8116521301834937617L;

    private String sex;

    private String maritalStatus;

    private String nationality;

    private String birthProvince;

    private String birthCity;

    private String province;

    private String city;

    private String province1;

    private String city1;

    private String asl;

    private String aslRegion;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityViewPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getSexType())) {
            this.setSex(this.getEntity().getSexType().toString());
        }
        if (!ValidationHelper
                .isNullOrEmpty(this.getEntity().getMaritalStatus())) {
            this.setMaritalStatus(this.getEntity().getMaritalStatus()
                    .toString());
        }
        if (this.getEntity().getNationality() != null) {
            this.setNationality(this.getEntity().getNationality()
                    .getDescription());
        }
        if (this.getEntity().getAsl() != null) {
            this.setAsl(this.getEntity().getAsl().getDescription());
        }
        if (this.getEntity().getRegion() != null) {
            this.setAslRegion(this.getEntity().getRegion().getDescription());
        }

        try {
            if (this.getEntity().getBirthProvince() != null) {
                this.setBirthProvince(DaoManager.get(Province.class,
                        this.getEntity().getBirthProvince().getId()).toString());
            }
            if (this.getEntity().getBirthCity() != null) {
                this.setBirthCity(DaoManager.get(City.class,
                        this.getEntity().getBirthCity().getId()).toString());
            }

            if (this.getEntity().getProvince() != null) {
                this.setProvince(DaoManager.get(Province.class,
                        this.getEntity().getProvince().getId()).toString());
            }
            if (this.getEntity().getCity() != null) {
                this.setCity(DaoManager.get(City.class,
                        this.getEntity().getCity().getId()).toString());
            }

            if (this.getEntity().getProvince1() != null) {
                this.setProvince1(DaoManager.get(Province.class,
                        this.getEntity().getProvince1().getId()).toString());
            }
            if (this.getEntity().getCity1() != null) {
                this.setCity1(DaoManager.get(City.class,
                        this.getEntity().getCity1().getId()).toString());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBirthProvince() {
        return birthProvince;
    }

    public void setBirthProvince(String birthProvince) {
        this.birthProvince = birthProvince;
    }

    public String getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(String birthCity) {
        this.birthCity = birthCity;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince1() {
        return province1;
    }

    public void setProvince1(String province1) {
        this.province1 = province1;
    }

    public String getCity1() {
        return city1;
    }

    public void setCity1(String city1) {
        this.city1 = city1;
    }

    public String getAsl() {
        return asl;
    }

    public void setAsl(String asl) {
        this.asl = asl;
    }

    public String getAslRegion() {
        return aslRegion;
    }

    public void setAslRegion(String alsRegion) {
        this.aslRegion = alsRegion;
    }

}
