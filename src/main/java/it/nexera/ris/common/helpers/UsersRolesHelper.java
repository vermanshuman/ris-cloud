package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.List;

public class UsersRolesHelper extends BaseHelper {

    public static List<Role> getUserRoles() {
        if (SessionManager.getInstance().getSessionBean() != null
                && UserHolder.getInstance().getCurrentUser() != null) {
            UserWrapper current = UserHolder.getInstance().getCurrentUser();

            List<Role> listRoles = null;
            try {
                listRoles = DaoManager.load(Role.class, new CriteriaAlias(
                                "users", "u", JoinType.LEFT_OUTER_JOIN),
                        new Criterion[]{
                                Restrictions.eq("u.id", current.getId())
                        });
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            return listRoles;
        }

        return null;
    }
}
