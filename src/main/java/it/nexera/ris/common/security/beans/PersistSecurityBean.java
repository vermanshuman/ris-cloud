package it.nexera.ris.common.security.beans;

import it.nexera.ris.common.exceptions.UserNotFoundException;
import it.nexera.ris.common.security.api.SecurityInfo;
import it.nexera.ris.common.security.api.SecurityInfoFactMethod;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.faces.context.FacesContext;

/**
 * Handles the user security details requests
 */
public class PersistSecurityBean implements SecurityInfoFactMethod {
    public PersistSecurityBean() {
    }

    public SecurityInfo getSecurityInfoByLogin(String userLogin)
            throws Exception {
        User user = null;
        if (FacesContext.getCurrentInstance() != null) {
            user = DaoManager.get(User.class,
                    Restrictions.eq("login", userLogin));
        } else {
            Session session = null;
            try {
                session = PersistenceSession.createSession();

                user = ConnectionManager.get(User.class,
                        Restrictions.eq("login", userLogin), session);
            } catch (Exception e) {
                throw e;
            } finally {
                if (session != null) {
                    session.clear();
                    session.close();
                }
            }
        }

        if (user != null) {
            return new SecurityInfo(user.getId(), user.getLogin(),
                    user.getPassword(), user.getStatus());
        } else {
            throw new UserNotFoundException();
        }
    }
}
