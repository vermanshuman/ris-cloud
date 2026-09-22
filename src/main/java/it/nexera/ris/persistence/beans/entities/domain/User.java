package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.UserStatuses;
import it.nexera.ris.common.helpers.ListHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Diagnostic;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.readonly.SectorReadOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@javax.persistence.Entity
@Table(name = "acl_user")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "USER_SEQ", allocationSize = 1)
public class User extends IndexedEntity {
    private static final long serialVersionUID = -8713862973623645229L;

    public transient final Logger log = LogManager.getLogger(User.class);

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "user_code")
    private String userCode;

    @Column(name = "doctor_code")
    private String doctorCode;

    @Column(name = "login", nullable = false, length = 255)
    private String login;

    @Column(name = "not_deletable", columnDefinition = "NUMBER(1) DEFAULT 0")
    private Boolean notDeletable;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "need_to_change_password")
    private Boolean needToChangePassword;

    @ManyToMany
    @JoinTable(name = "acl_user_role", joinColumns = {
            @JoinColumn(name = "user_id", table = "acl_user",
                    foreignKey = @ForeignKey(name = "FK_ROLE_USER"))
    }, inverseJoinColumns = {
            @JoinColumn(name = "role_id", table = "acl_role",
                    foreignKey = @ForeignKey(name = "FK_USER_ROLES"))
    })
    private List<Role> roles = new ArrayList<Role>();

    @ManyToMany
    @JoinTable(name = "user_sector", joinColumns = {
            @JoinColumn(name = "user_id", table = "acl_user",
                    foreignKey = @ForeignKey(name = "FK_SECTOR_USER"))
    }, inverseJoinColumns = {
            @JoinColumn(name = "sector_id", table = "dic_sector",
                    foreignKey = @ForeignKey(name = "FK_USER_SECTORS"))
    })
    private List<Sector> sectors;

    @ManyToMany
    @JoinTable(name = "user_diagnostic", joinColumns = {
            @JoinColumn(name = "user_id", table = "acl_user",
                    foreignKey = @ForeignKey(name = "FK_DIAGNOSTIC_USER"))
    }, inverseJoinColumns = {
            @JoinColumn(name = "diagnostic_id", table = "dic_diagnostic",
                    foreignKey = @ForeignKey(name = "FK_USER_DIAGNOSTICS"))
    })
    private List<Diagnostic> diagnostics;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatuses status;

    @Column(name = "password_change_date")
    private Date passwordChangeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_sector_id", foreignKey = @ForeignKey(name = "FK_USER_PRIMARY_SECTOR"))
    private Sector primarySector;

    @Column(name = "statistic", columnDefinition = "NUMBER(3) DEFAULT 0")
    private Long statistic;

    @Column(name = "reopen_last_report")
    private Boolean reopenLastReport;

    @Column(name = "print_daily_report")
    private Boolean printDailyReport;

    @Column(name = "show_order_date")
    private Boolean showOrderDate;

    @Column(name = "last_login_date")
    private Date lastLoginDate;

    @Column(name = "username_spvoice")
    private String spvoiceUsername;

    @Column(name = "password_spvoice")
    private String spvoicePassword;

    @Transient
    private List<Role> userRoles;

    @Transient
    private List<SectorReadOnly> userSectors;

    @Transient
    private String noEncryptedPassword;

    public List<Role> getUserRoles() {
        if (userRoles == null) {
            try {
                userRoles = DaoManager.load(Role.class, new CriteriaAlias[]{
                        new CriteriaAlias("users", "users", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("users.id", this.getId())
                });
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return userRoles;
    }

    public List<SectorReadOnly> getUserSectors() {
        if (userSectors == null) {
            try {
                userSectors = DaoManager.transform(Sector.class, SectorReadOnly.class,
                        new Criterion[]{
                                Restrictions.eq("users.id", this.getId())
                        },
                        new CriteriaAlias[]{
                                new CriteriaAlias("users", "users",
                                        JoinType.INNER_JOIN)
                        }, null, false, null);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return userSectors;
    }

    @Override
    public boolean getDeletable() {
        if (SessionManager.getInstance() != null
                && SessionManager.getInstance().getSessionBean() != null
                && UserHolder.getInstance().getCurrentUser() != null
                && UserHolder.getInstance().getCurrentUser().getId() != null
                && UserHolder.getInstance().getCurrentUser().getId()
                .equals(this.getId())) {
            return false;
        }
        return getNotDeletable() == null || !getNotDeletable();
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    @Transient
    public String getFullname() {
        return String.format("%s %s",
                this.getLastName() == null ? "" : this.getLastName(),
                this.getFirstName() == null ? "" : this.getFirstName());
    }

    public String getLastName() {
        return lastName;
    }

    public String getLogin() {
        return login;
    }

    public Boolean getNotDeletable() {
        if (getId() == null || notDeletable == null) {
            return null;
        }
        return !this.getId().equals(
                UserHolder.getInstance().getCurrentUser().getId())
                && notDeletable.booleanValue();
    }

    public String getPassword() {
        return password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setNotDeletable(Boolean notDeletable) {
        this.notDeletable = notDeletable;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return this.getFullname();
    }

    public UserStatuses getStatus() {
        return status;
    }

    public void setStatus(UserStatuses status) {
        this.status = status;
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public void setSectors(List<Sector> sectors) {
        this.sectors = sectors;
    }

    public Sector getPrimarySector() {
        return primarySector;
    }

    public void setPrimarySector(Sector primarySector) {
        this.primarySector = primarySector;
    }

    public Date getPasswordChangeDate() {
        return passwordChangeDate;
    }

    public void setPasswordChangeDate(Date passwordChangeDate) {
        this.passwordChangeDate = passwordChangeDate;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getDoctorCode() {
        return doctorCode;
    }

    public void setDoctorCode(String doctorCode) {
        this.doctorCode = doctorCode;
    }

    @Transient
    public String getRolesExport() {
        return ListHelper.toString(this.getUserRoles());
    }

    @Transient
    public String getSectorsExport() {
        return ListHelper.toString(this.getUserSectors());
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Long getStatistic() {
        return statistic;
    }

    public void setStatistic(Long statistic) {
        this.statistic = statistic;
    }

    public Boolean getReopenLastReport() {
        return reopenLastReport;
    }

    public void setReopenLastReport(Boolean reopenLastReport) {
        this.reopenLastReport = reopenLastReport;
    }

    public Boolean getPrintDailyReport() {
        return printDailyReport;
    }

    public void setPrintDailyReport(Boolean printDailyReport) {
        this.printDailyReport = printDailyReport;
    }

    public Boolean getShowOrderDate() {
        return showOrderDate;
    }

    public void setShowOrderDate(Boolean showOrderDate) {
        this.showOrderDate = showOrderDate;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getSpvoiceUsername() {
        return spvoiceUsername;
    }

    public void setSpvoiceUsername(String spvoiceUsername) {
        this.spvoiceUsername = spvoiceUsername;
    }

    public String getSpvoicePassword() {
        return spvoicePassword;
    }

    public void setSpvoicePassword(String spvoicePassword) {
        this.spvoicePassword = spvoicePassword;
    }

    public Boolean getNeedToChangePassword() {
        return needToChangePassword;
    }

    public void setNeedToChangePassword(Boolean needToChangePassword) {
        this.needToChangePassword = needToChangePassword;
    }

    public String getNoEncryptedPassword() {
        return noEncryptedPassword;
    }

    public void setNoEncryptedPassword(String noEncryptedPassword) {
        this.noEncryptedPassword = noEncryptedPassword;
    }
}
