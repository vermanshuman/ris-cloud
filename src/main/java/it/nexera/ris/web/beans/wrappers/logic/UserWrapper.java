package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.DocumentGenerationTags;
import it.nexera.ris.common.enums.UserPreferenceType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Permission;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.persistence.beans.entities.domain.SpecialPermission;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.UserPreference;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Diagnostic;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserWrapper implements Serializable {

    private static final long serialVersionUID = 7770139502939393406L;

    public transient final Logger log = LogManager.getLogger(UserWrapper.class);

    private String email;

    private String firstName;

    private String lastName;

    private String login;

    private String password;

    private Date passwordChangeDate;

    private Long id;

    private transient Map<String, PermissionWrapper> permissions;

    private List<RoleWrapper> roles;

    private List<SpecialPermissionWrapper> specialPermissions;

    private List<Long> sectors;

    private List<Long> diagnostics;

    private Long primarySectorId;

    private Long statistic;

    private String sectorsDescriptions;

    private Boolean reopenLastReport;

    private Boolean printDailyReport;

    private Boolean showOrderDate;

    private Boolean needToChangePassword;

    public UserWrapper(User u, Session session) {
        this.setEmail(u.getEmail());
        this.setFirstName(u.getFirstName());
        this.setId(u.getId());
        this.setLastName(u.getLastName());
        this.setLogin(u.getLogin());
        this.setPassword(u.getPassword());
        this.setPasswordChangeDate(u.getPasswordChangeDate());
        this.setPermissions(this.loadPermissions());
        this.setRoles(this.loadRoles(session));
        this.setSpecialPermissions(this.loadSpecialPermisions());
        this.setStatistic(u.getStatistic());
        this.setNeedToChangePassword(u.getNeedToChangePassword());

        this.setSectors(this.loadSectorsIds(session));
        if (u.getPrimarySector() != null) {
            setPrimarySectorId(u.getPrimarySector().getId());
        }
        this.setDiagnostics(this.loadDiagnosticsIds(session));

        setReopenLastReport(u.getReopenLastReport());
        setPrintDailyReport(u.getPrintDailyReport());
        setShowOrderDate(u.getShowOrderDate());
    }

    private Map<String, PermissionWrapper> loadPermissions() {
        Map<String, PermissionWrapper> listPermissions = new HashMap<String, PermissionWrapper>();

        List<Role> roles = null;
        List<Permission> permissions = null;
        try {
            if (FacesContext.getCurrentInstance() != null) {
                roles = DaoManager.load(Role.class, new CriteriaAlias[]{
                        new CriteriaAlias("users", "u", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("u.id", this.getId())
                });

                if (roles == null) {
                    return listPermissions;
                }

                permissions = DaoManager.load(Permission.class,
                        new Criterion[]{
                                Restrictions.in("role", roles)
                        });
            } else {
                Session session = null;
                try {
                    session = PersistenceSession.createSession();

                    roles = ConnectionManager.load(Role.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("users", "u",
                                            JoinType.INNER_JOIN)
                            }, new Criterion[]{
                                    Restrictions.eq("u.id", this.getId())
                            }, session);

                    if (roles == null) {
                        return listPermissions;
                    }

                    permissions = ConnectionManager.load(Permission.class,
                            new Criterion[]{
                                    Restrictions.in("role", roles)
                            }, session);
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (session != null) {
                        session.clear();
                        session.close();
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            if (!ValidationHelper.isNullOrEmpty(permissions)) {
                for (Permission p : permissions) {
                    PermissionWrapper per = listPermissions.get(p.getModule()
                            .getCode());
                    if (per == null) {
                        per = new PermissionWrapper();
                    }

                    if (p.getCanCreate() != null
                            && p.getCanCreate().booleanValue()) {
                        per.setCanCreate(true);
                    }
                    if (p.getCanDelete() != null
                            && p.getCanDelete().booleanValue()) {
                        per.setCanDelete(true);
                    }
                    if (p.getCanEdit() != null && p.getCanEdit().booleanValue()) {
                        per.setCanEdit(true);
                    }
                    if (p.getCanView() != null && p.getCanView().booleanValue()) {
                        per.setCanView(true);
                    }
                    if (p.getCanList() != null && p.getCanList().booleanValue()) {
                        per.setCanList(true);
                    }
                    if (p.getModule() != null) {
                        per.setIdModule(p.getModule().getId());
                        per.setParent(p.getModule().getParent() == null);
                        listPermissions.put(p.getModule().getCode(), per);
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return listPermissions;
    }

    private List<RoleWrapper> loadRoles(Session session) {
        List<RoleWrapper> rolesWrapper = new ArrayList<RoleWrapper>();

        List<Role> roles = null;
        try {
            roles = ConnectionManager.load(Role.class, new CriteriaAlias[]{
                    new CriteriaAlias("users", "u", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("u.id", this.getId())
            }, session);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (roles == null) {
            return rolesWrapper;
        }

        try {
            for (Role r : roles) {
                rolesWrapper.add(new RoleWrapper(r));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return rolesWrapper;
    }

    private List<Long> loadSectorsIds(Session session) {
        List<Long> sectorsIds = new ArrayList<Long>();

        List<Sector> sectors = null;
        try {
            sectors = ConnectionManager.load(Sector.class, new CriteriaAlias[]{
                    new CriteriaAlias("users", "u", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("u.id", this.getId())
            }, session);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (sectors == null) {
            return sectorsIds;
        }

        try {
            StringBuilder strBuilder = new StringBuilder();

            for (Sector s : sectors) {
                sectorsIds.add(s.getId());
                strBuilder.append(s.getDescription());
                strBuilder.append(",");
            }
            this.setSectorsDescriptions(strBuilder.substring(0,
                    strBuilder.length() - 1));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return sectorsIds;
    }

    private List<Long> loadDiagnosticsIds(Session session) {
        List<Long> diagnosticsIds = new ArrayList<Long>();

        List<Diagnostic> diagnostics = null;
        try {
            diagnostics = ConnectionManager.load(Diagnostic.class,
                    new CriteriaAlias[]{
                            new CriteriaAlias("users", "u", JoinType.INNER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("u.id", this.getId())
                    }, session);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (diagnostics == null) {
            return diagnosticsIds;
        }

        try {
            for (Diagnostic d : diagnostics) {
                diagnosticsIds.add(d.getId());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return diagnosticsIds;
    }

    private List<SpecialPermissionWrapper> loadSpecialPermisions() {
        List<SpecialPermissionWrapper> listPermissions = new ArrayList<SpecialPermissionWrapper>();

        List<Long> rolesIdList = null;
        try {
            rolesIdList = DaoManager.loadIds(Role.class, new CriteriaAlias[]{
                    new CriteriaAlias("users", "u", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("u.id", this.getId())
            });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (rolesIdList == null) {
            return listPermissions;
        }

        try {
            for (SpecialPermission p : DaoManager.load(SpecialPermission.class,
                    new CriteriaAlias[]{
                            new CriteriaAlias("roles", "r", JoinType.INNER_JOIN)
                    }, new Criterion[]{
                            Restrictions.in("r.id", rolesIdList)
                    })) {
                listPermissions.add(new SpecialPermissionWrapper(p));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return listPermissions;
    }

    public static UserWrapper wrap(User u, Session session) {
        return new UserWrapper(u, session);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

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

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getFullname();
    }

    public Map<String, PermissionWrapper> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, PermissionWrapper> listPermissions) {
        this.permissions = listPermissions;
    }

    public List<RoleWrapper> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleWrapper> roles) {
        this.roles = roles;
    }

    public Date getPasswordChangeDate() {
        return passwordChangeDate;
    }

    public void setPasswordChangeDate(Date passwordChangeDate) {
        this.passwordChangeDate = passwordChangeDate;
    }

    public List<SpecialPermissionWrapper> getSpecialPermissions() {
        return specialPermissions;
    }

    public void setSpecialPermissions(
            List<SpecialPermissionWrapper> specialPermissions) {
        this.specialPermissions = specialPermissions;
    }

    public List<Long> getSectors() {
        return sectors;
    }

    public void setSectors(List<Long> sectors) {
        this.sectors = sectors;
    }

    public List<Long> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<Long> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Long getStatistic() {
        return statistic;
    }

    public void setStatistic(Long statistic) {
        this.statistic = statistic;
    }

    public String getSectorsDescriptions() {
        return sectorsDescriptions;
    }

    public void setSectorsDescriptions(String sectorsDescriptions) {
        this.sectorsDescriptions = sectorsDescriptions;
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

    public Boolean getShowStatisticMenu() {
        if (this.getStatistic() == null) {
            return Boolean.FALSE;
        }

        return this.getStatistic().longValue() > 0 ? Boolean.TRUE
                : Boolean.FALSE;
    }

    public String getStatisticUrl() {
        StringBuilder firstPart = new StringBuilder().append(
                ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.STATISTIC_SERVER_IP)
                        .getValue()).append("?r=");
        StringBuilder str = new StringBuilder("RIS");
        str.append("&u=");
        str.append(this.login);
        str.append("&l=");
        str.append(this.getSectorsDescriptions().replaceAll(",", "|")
                .replaceAll(" ", "_"));
        str.append("&p=");
        str.append(this.getStatistic());
        str.append("&x=");
        str.append(this.getDateString());
        log.info("Statistic URL before encoding:" + firstPart + str);
        firstPart.append(str);
        return firstPart.toString();
    }

    public String getDateString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        long smallValue = Long.parseLong(formatter.format(new Date())) - 15328593282712l;

        String stringValue = String.valueOf(smallValue);

        StringBuilder builder = new StringBuilder();
        for (char c : stringValue.toCharArray()) {

            switch (c) {
                case '0':
                    builder.append('a');
                    break;
                case '1':
                    builder.append('c');
                    break;
                case '2':
                    builder.append('e');
                    break;
                case '3':
                    builder.append('g');
                    break;
                case '4':
                    builder.append('i');
                    break;
                case '5':
                    builder.append('k');
                    break;
                case '6':
                    builder.append('m');
                    break;
                case '7':
                    builder.append('o');
                    break;
                case '8':
                    builder.append('q');
                    break;
                case '9':
                    builder.append('s');
                    break;
                default:
                    break;
            }
        }
        return builder.toString();
    }

    public Boolean getShowOrderDate() {
        return showOrderDate == null ? false : showOrderDate;
    }

    public void setShowOrderDate(Boolean showOrderDate) {
        this.showOrderDate = showOrderDate;
    }

    public Long getPrimarySectorId() {
        return primarySectorId;
    }

    public void setPrimarySectorId(Long primarySectorId) {
        this.primarySectorId = primarySectorId;
    }

    public UserPreference getUserPreference(UserPreferenceType type, Session session) {
        List<UserPreference> userPreferences = null;
        if (getId() != null) {
            userPreferences = ConnectionManager.load(UserPreference.class, new Criterion[]{
                    Restrictions.eq("user.id", getId()),
                    Restrictions.eq("type", type)
            }, session);
        }
        return ValidationHelper.isNullOrEmpty(userPreferences) ? null : userPreferences.get(0);
    }

    public Boolean getNeedToChangePassword() {
        return needToChangePassword == null ? false : needToChangePassword;
    }

    public void setNeedToChangePassword(Boolean needToChangePassword) {
        this.needToChangePassword = needToChangePassword;
    }
}
