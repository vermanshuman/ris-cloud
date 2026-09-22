package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.UserStatuses;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Diagnostic;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import jakarta.mail.MessagingException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named("userEditBean")
@ViewScoped
public class UserEditBean extends EntityEditPageBean<User> implements
        Serializable {
    private static final long serialVersionUID = -2909406163733552007L;

    public static final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[.,<>?^&*()=/@#$%!]).{8,40})";

    @Inject
    private PasswordEncoder passwordEncoder;

    private List<SelectItem> roles;

    private List<SelectItem> statuses;

    private UserStatuses status;

    private String pwd;

    private String confirmPwd;

    private List<String> selectedRoles;

    private List<SelectItem> sectors;

    private List<SelectItem> avSectors;

    private List<SelectItem> diagnostics;

    private List<String> selectedSectors;

    private List<String> selectedDiagnostics;

    private Long prSector;

    private String spvoicePassword;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!this.getEntity().isNew() && !this.isPostback()) {
            this.setSelectedRoles(new ArrayList<String>());
            for (Role role : this.getEntity().getRoles()) {
                this.getSelectedRoles().add(role.getStrId());
            }

            this.setStatus(this.getEntity().getStatus());

            this.setSelectedSectors(new ArrayList<String>());
            for (Sector s : this.getEntity().getSectors()) {
                this.getSelectedSectors().add(String.valueOf(s.getId()));
            }

            this.setSelectedDiagnostics(new ArrayList<String>());
            for (Diagnostic d : this.getEntity().getDiagnostics()) {
                this.getSelectedDiagnostics().add(String.valueOf(d.getId()));
            }

            if (this.getEntity().getPrimarySector() != null) {
                this.setPrSector(this.getEntity().getPrimarySector().getId());
            }
        }

        selectedSectorChange();

        this.setRoles(ComboboxHelper.fillList(Role.class, Order.asc("name"),
                false));
        this.setStatuses(ComboboxHelper.fillList(UserStatuses.values()));
        this.setSectors(ComboboxHelper.fillList(Sector.class,
                Order.asc("description"), false));
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException, MessagingException {
        if (this.getEntity().getNotDeletable() == null) {
            this.getEntity().setNotDeletable(Boolean.FALSE);
        }

        this.getEntity().setStatus(this.getStatus());

        List<Long> ids = new ArrayList<Long>();
        for (String str : this.getSelectedRoles()) {
            ids.add(Long.parseLong(str));
        }

        this.getEntity().setRoles(DaoManager.load(Role.class, new Criterion[]{
                Restrictions.in("id", ids.toArray(new Long[0]))
        }));

        List<Long> idsSectors = new ArrayList<Long>();
        for (String str : this.getSelectedSectors()) {
            idsSectors.add(Long.parseLong(str));
        }

        this.getEntity().setSectors(
                DaoManager.load(Sector.class, new Criterion[]{
                        Restrictions.in("id", idsSectors.toArray(new Long[0]))
                }));

        if (this.getPrSector() != null) {
            this.getEntity().setPrimarySector(
                    DaoManager.get(Sector.class, this.getPrSector()));
        } else {
            this.getEntity().setPrimarySector(null);
        }

        List<Long> idsDiagnostics = new ArrayList<Long>();
        for (String str : this.getSelectedDiagnostics()) {
            idsDiagnostics.add(Long.parseLong(str));
        }

        this.getEntity().setDiagnostics(
                DaoManager.load(Diagnostic.class, new Criterion[]{
                        Restrictions.in("id", idsDiagnostics.toArray(new Long[0]))
                }));

        if(!ValidationHelper.isNullOrEmpty(this.getPwd())){
            this.getEntity().setPasswordChangeDate(new Date());
            this.getEntity().setPassword(passwordEncoder.encode(getPwd()));
            this.getContext().getExternalContext().getApplicationMap()
                    .put("reload_users", Boolean.TRUE);
        }

        if (StringUtils.isNotEmpty(getSpvoicePassword())) {
            getEntity().setSpvoicePassword(getSpvoicePassword());
        }

        DaoManager.save(this.getEntity());

        if (this.getEntity().getId().equals(this.getCurrentUser().getId())) {
            UserHolder.getInstance().setCurrentUser(
                    UserWrapper.wrap(getEntity(), DaoManager.getSession()));
        }
    }

    public void selectedSectorChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (this.getSelectedSectors() == null
                || this.getSelectedSectors().isEmpty()) {
            this.setAvSectors(ComboboxHelper.fillList(new ArrayList<Sector>(),
                    true));
            return;
        }
        List<Long> ids = new ArrayList<Long>();
        for (String str : this.getSelectedSectors()) {
            ids.add(Long.parseLong(str));
        }

        if (ids.size() != 0) {
            this.setAvSectors(ComboboxHelper.fillList(
                    DaoManager.load(Sector.class, new Criterion[]{
                            Restrictions.in("id", ids)
                    }), true));
        }

        fillDiagnostics();
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getStatus())) {
            this.addRequiredFieldExeption("form:status");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getEmail().trim())
                && !ValidationHelper.checkMailCorrectFormat(this.getEntity()
                .getEmail().trim())) {
            this.addFieldExeption("form:email", "emailWrongFormat");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getEmail().trim())
                && !ValidationHelper.isUnique(User.class, "email", this
                .getEntity().getEmail().trim(), this.getEntityId())) {
            this.addFieldExeption("form:email", "emailAlreadyInUse");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getLastName()
                .trim())) {
            this.addRequiredFieldExeption("form:lastname");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getFirstName()
                .trim())) {
            this.addRequiredFieldExeption("form:firstname");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getLogin().trim())) {
            this.addRequiredFieldExeption("form:login");
        } else if (!ValidationHelper.checkUserNameFormat(this.getEntity()
                .getLogin().trim())) {
            this.addFieldExeption("form:login", "loginFormatError");
        }

        if (!ValidationHelper.isUnique(User.class, "login", this.getEntity()
                .getLogin().trim(), this.getEntityId())) {
            this.addFieldExeption("form:login", "loginAlreadyInUse");
        }

        if (ValidationHelper.isNullOrEmpty(this.getSelectedRoles())) {
            this.addRequiredFieldExeption("form:role");
        }

        if (ValidationHelper.isNullOrEmpty(this.getSelectedSectors())) {
            this.addRequiredFieldExeption("form:sectors");
        }

        if (ValidationHelper.isNullOrEmpty(this.getSelectedDiagnostics())) {
            this.addRequiredFieldExeption("form:diagnostics");
        }

        if (getEntity().isNew() && ValidationHelper.isNullOrEmpty(this.getPwd())) {
            this.addRequiredFieldExeption("form:password");
        }

        if (getEntity().isNew() && ValidationHelper.isNullOrEmpty(this.getConfirmPwd())) {
            this.addRequiredFieldExeption("form:c_password");
        }

       // if (!getEntity().isNew()) {
            if (!ValidationHelper.isNullOrEmpty(this.getPwd().trim())) {
                if (ValidationHelper.isNullOrEmpty(this.getConfirmPwd().trim())) {
                    this.addFieldExeption("form:password", "passwordMissmatch");
                    this.addFieldExeption("form:c_password", "passwordMissmatch",
                            Boolean.FALSE);
                }else if (!ValidationHelper.isNullOrEmpty(this.getConfirmPwd().trim())
                        && !ValidationHelper.isNullOrEmpty(this.getPwd().trim())
                        && !this.getConfirmPwd().trim().equals(this.getPwd().trim())) {
                    this.addFieldExeption("form:password", "passwordMissmatch");
                    this.addFieldExeption("form:c_password", "passwordMissmatch",
                            Boolean.FALSE);
                } else {
                    Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
                    Matcher matcher = pattern.matcher(getPwd().trim());
                    if (!matcher.matches()) {
                        this.addFieldExeption("form:password", "passwordFormatError");
                    }
                    matcher = pattern.matcher(getConfirmPwd().trim());
                    if (!matcher.matches()) {
                        this.addFieldExeption("form:c_password", "passwordFormatError", false);
                    }
                }
            }
        // }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getFiscalCode())
                && !((this.getEntity().getFiscalCode().length() == 11 && ValidationHelper
                .checkCorrectFormatByExpression("^[0-9]+$", this
                        .getEntity().getFiscalCode()))
                || (this.getEntity().getFiscalCode().length() == 16 && ValidationHelper
                .checkCorrectFormatByExpression(
                        "^[a-zA-Z]{6}[0-9]{2}[a-zA-Z]{1}[0-9]{2}[a-zA-Z]{1}[0-9]{3}[a-zA-Z]{1}$",
                        this.getEntity().getFiscalCode())) || (this
                .getEntity().getFiscalCode().length() == 16 && ValidationHelper
                .checkCorrectFormatByExpression("^(STP|ENI)[0-9]{13}$",
                        this.getEntity().getFiscalCode())))) {
            addFieldExeption("fiscalCode", "fiscalCodeWrongFormat");
        } else if (!ValidationHelper.isUnique(User.class, "fiscalCode", this
                .getEntity().getFiscalCode(), this.getEntity().getId())) {
            addFieldExeption("fiscalCode", "fiscalCodeAlreadyInUse");
        }

        if (StringUtils.isNotEmpty(getEntity().getSpvoiceUsername())
                && StringUtils.isEmpty(getSpvoicePassword())
                && StringUtils.isEmpty(getEntity().getSpvoicePassword())) {
            addRequiredFieldExeption("form:spvoicePassword");

        } else if ((StringUtils.isNotEmpty(getSpvoicePassword())
                || StringUtils.isNotEmpty(getEntity().getSpvoicePassword()))
                && StringUtils.isEmpty(getEntity().getSpvoiceUsername())) {
            addRequiredFieldExeption("form:spvoiceUsername");
        }
    }

    @Override
    public void afterSave() {
        try {
            UserHolder.getInstance().setCurrentUser(
                    UserWrapper.wrap(
                            DaoManager.get(User.class, UserHolder.getInstance()
                                    .getCurrentUser().getId()),
                            DaoManager.getSession()));
        } catch (HibernateException | InstantiationException
                | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        super.afterSave();
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPwd() {
        return pwd;
    }

    public void setConfirmPwd(String confirmPwd) {
        this.confirmPwd = confirmPwd;
    }

    public String getConfirmPwd() {
        return confirmPwd;
    }

    public List<SelectItem> getRoles() {
        return roles;
    }

    public void setRoles(List<SelectItem> roles) {
        this.roles = roles;
    }

    public List<SelectItem> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<SelectItem> statuses) {
        this.statuses = statuses;
    }

    public List<String> getSelectedRoles() {
        return selectedRoles;
    }

    public void setSelectedRoles(List<String> selectedRoles) {
        this.selectedRoles = selectedRoles;
    }

    public List<String> getSelectedSectors() {
        return selectedSectors;
    }

    public void setSelectedSectors(List<String> selectedSectors) {
        this.selectedSectors = selectedSectors;
    }

    public List<SelectItem> getSectors() {
        return sectors;
    }

    public void setSectors(List<SelectItem> sectors) {
        this.sectors = sectors;
    }

    public List<SelectItem> getAvSectors() {
        return avSectors;
    }

    public void setAvSectors(List<SelectItem> avSectors) {
        this.avSectors = avSectors;
    }

    public Long getPrSector() {
        return prSector;
    }

    public void setPrSector(Long prSector) {
        this.prSector = prSector;
    }

    public String getSpvoicePassword() {
        return spvoicePassword;
    }

    public void setSpvoicePassword(String spvoicePassword) {
        this.spvoicePassword = spvoicePassword;
    }

    public UserStatuses getStatus() {
        return status;
    }

    public void setStatus(UserStatuses status) {
        this.status = status;
    }

    public List<String> getSelectedDiagnostics() {
        return selectedDiagnostics;
    }

    public void setSelectedDiagnostics(List<String> selectedDiagnostics) {
        this.selectedDiagnostics = selectedDiagnostics;
    }

    public List<SelectItem> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<SelectItem> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public void fillDiagnostics() throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        List<SelectItem> temp = new ArrayList<SelectItem>();
        if (!ValidationHelper.isNullOrEmpty(getSelectedSectors())) {
            for (String selectedSector : getSelectedSectors()) {
                List<Diagnostic> listDiagnostics = DaoManager.load(
                        Diagnostic.class, new CriteriaAlias("sector", "s",
                                JoinType.LEFT_OUTER_JOIN), new Criterion[]{
                                Restrictions.eq("s.id",
                                        Long.parseLong(selectedSector))
                        });
                for (Diagnostic d : listDiagnostics) {
                    temp.add(new SelectItem(d.getId(), d.getDescription()));
                }
            }
        }
        setDiagnostics(temp);
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
