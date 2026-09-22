package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.readonly.SectorReadOnly;
import it.nexera.ris.web.beans.EntityListPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("userListBean")
@ViewScoped
public class UserListBean extends EntityListPageBean<User> implements
        Serializable {

    private static final long serialVersionUID = -190774165561455798L;

    private List<User> users;

    private List<SelectItem> roles;

    private Long selectedRole;

    private List<SelectItem> sectors;

    private Long selectedSector;

    private Long selectedRoleTemp;

    private Long selectedSectorTemp;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        fillUsers();

        if (!this.isPostback()) {
            if (ValidationHelper.isNullOrEmpty(getSectors())) {
                setSectors(ComboboxHelper.fillList(Sector.class,
                        Order.asc("description"), true));
            }

            if (ValidationHelper.isNullOrEmpty(getRoles())) {
                setRoles(ComboboxHelper.fillList(Role.class, Order.asc("name"),
                        true));
            }
        }
    }

    public void fillUsers() throws HibernateException {
        try {
            List<User> resListStep1 = new ArrayList<User>();

            if (getUsers() == null) {
                setUsers(DaoManager.load(User.class));
            }

            if (!ValidationHelper.isNullOrEmpty(getSelectedSector())
                    && ValidationHelper.isNullOrEmpty(getSelectedRole())) {
                for (User user : getUsers()) {
                    for (SectorReadOnly s : user.getUserSectors()) {
                        if (!ValidationHelper.isNullOrEmpty(getSelectedSector())
                                && !ValidationHelper.isNullOrEmpty(s)
                                && getSelectedSector().equals(s.getId())) {
                            resListStep1.add(user);
                            break;
                        }
                    }
                }

                setList(resListStep1);
            } else if (ValidationHelper.isNullOrEmpty(getSelectedSector())
                    && !ValidationHelper.isNullOrEmpty(getSelectedRole())) {
                for (User user : getUsers()) {
                    for (Role r : user.getUserRoles()) {
                        if (!ValidationHelper.isNullOrEmpty(getSelectedRole())
                                && !ValidationHelper.isNullOrEmpty(r)
                                && getSelectedRole().equals(r.getId())) {
                            resListStep1.add(user);
                            break;
                        }
                    }
                }

                setList(resListStep1);
            } else if (!ValidationHelper.isNullOrEmpty(getSelectedSector())
                    && !ValidationHelper.isNullOrEmpty(getSelectedRole())) {
                List<User> resListStep2 = new ArrayList<User>();

                for (User user : getUsers()) {
                    for (SectorReadOnly s : user.getUserSectors()) {
                        if (s.getId() != null && s.getId().equals(getSelectedSector())) {
                            resListStep1.add(user);
                            break;
                        }
                    }
                }

                if (!ValidationHelper.isNullOrEmpty(resListStep1)) {
                    for (User user : resListStep1) {
                        for (Role r : user.getRoles()) {
                            if (r.getId() != null && r.getId().equals(getSelectedRole())) {
                                resListStep2.add(user);
                                break;
                            }
                        }
                    }
                } else {
                    setList(resListStep1);

                    return;
                }

                setList(resListStep2);
            } else {
                setList(getUsers());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public String sectorsString(List<Sector> sList) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < sList.size() - 1; i++) {
            sb.append(sList.get(i).getDescription());
            sb.append("\n");
        }

        sb.append(sList.get(sList.size()).getDescription());

        return sb.toString();
    }

    public void selectedRoleChanged() {
        try {
            this.selectedRole = this.selectedRoleTemp;
            fillUsers();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void selectedSectorChanged() {
        try {
            this.selectedSector = this.selectedSectorTemp;
            fillUsers();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public List<SelectItem> getRoles() {
        return roles;
    }

    public void setRoles(List<SelectItem> roles) {
        this.roles = roles;
    }

    public List<SelectItem> getSectors() {
        return sectors;
    }

    public void setSectors(List<SelectItem> sectors) {
        this.sectors = sectors;
    }

    public Long getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(Long selectedRole) {
        this.selectedRoleTemp = selectedRole;
    }

    public Long getSelectedSector() {
        return selectedSector;
    }

    public void setSelectedSector(Long selectedSector) {
        this.selectedSectorTemp = selectedSector;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
