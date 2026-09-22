package it.nexera.ris.web.beans.base;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.ModulesHolder;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Module;
import it.nexera.ris.web.beans.wrappers.logic.PermissionWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class AccessBean {
    public static boolean canViewPage(PageTypes pageType)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        return canViewPage(pageType, UserHolder.getInstance().getCurrentUser());
    }

    public static boolean canAccessPage(PageTypes pageType)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (pageType.getPagesContext().contains("List.jsf")) {
            return canListPage(pageType, UserHolder.getInstance()
                    .getCurrentUser());
        } else if (pageType.getPagesContext().contains("Edit.jsf")) {
            return canEditInPage(pageType, UserHolder.getInstance()
                    .getCurrentUser());
        } else {
            return true;
        }
    }

    public static boolean canAccessPage(PageTypes pageType, UserWrapper user)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (pageType.getPagesContext().contains("List.jsf")) {
            return canListPage(pageType, user);
        } else if (pageType.getPagesContext().contains("Edit.jsf")) {
            return canEditInPage(pageType, user);
        } else {
            return true;
        }
    }

    public static boolean canCreateInPage(PageTypes pageType)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        return canCreateInPage(pageType, UserHolder.getInstance()
                .getCurrentUser());
    }

    public static boolean canEditInPage(PageTypes pageType)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        return canEditInPage(pageType, UserHolder.getInstance()
                .getCurrentUser());
    }

    public static boolean canDeleteInPage(PageTypes pageType)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        return canDeleteInPage(pageType, UserHolder.getInstance()
                .getCurrentUser());
    }

    public static boolean canViewPage(PageTypes pageType, UserWrapper user)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (pageType == null) {
            return false;
        }

        if (pageType.getCode() == null) {
            return true;
        }

        List<PermissionWrapper> list = getRightsForPage(pageType, user);

        if (list.isEmpty()
                && ValidationHelper.isNullOrEmpty(pageType.getCode())) {
            return true;
        }

        for (PermissionWrapper permission : list) {
            if (permission.isCanView()) {
                return true;
            }
        }
        return false;
    }

    public static boolean canListPage(PageTypes pageType, UserWrapper user)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (pageType == null) {
            return false;
        }

        if (pageType.getCode() == null) {
            return true;
        }

        List<PermissionWrapper> list = getRightsForPage(pageType, user);

        if (list.isEmpty()
                && ValidationHelper.isNullOrEmpty(pageType.getCode())) {
            return true;
        }

        for (PermissionWrapper permission : list) {
            if (permission.isCanList() || (permission.isParent() == true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canCreateInPage(PageTypes pageType, UserWrapper user)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (pageType == null) {
            return false;
        }

        if (pageType.getCode() == null) {
            return true;
        }

        for (PermissionWrapper permission : getRightsForPage(pageType, user)) {
            if (permission.isCanCreate()) {
                return true;
            }
        }
        return false;
    }

    public static boolean canEditInPage(PageTypes pageType, UserWrapper user)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (pageType == null) {
            return false;
        }

        if (pageType.getCode() == null) {
            return true;
        }

        for (PermissionWrapper permission : getRightsForPage(pageType, user)) {
            if (permission.isCanEdit()) {
                return true;
            }
        }
        return false;
    }

    public static boolean canDeleteInPage(PageTypes pageType, UserWrapper user)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (pageType == null) {
            return false;
        }

        if (pageType.getCode() == null) {
            return true;
        }

        for (PermissionWrapper permission : getRightsForPage(pageType, user)) {
            if (permission.isCanDelete()) {
                return true;
            }
        }
        return false;
    }

    public static List<PermissionWrapper> getRightsForPage(PageTypes pageType)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        return getRightsForPage(pageType, UserHolder.getInstance()
                .getCurrentUser());
    }

    public static List<PermissionWrapper> getRightsForPage(PageTypes pageType,
                                                           UserWrapper user) throws HibernateException,
            PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        if (user == null || pageType == null) {
            return new ArrayList<PermissionWrapper>();
        }

        List<Long> modules = getModuleByPage(pageType);

        if (modules == null || modules.isEmpty()) {
            return new ArrayList<PermissionWrapper>();
        }

        List<PermissionWrapper> list = new ArrayList<PermissionWrapper>();
        List<Module> m = null;
        for (Long module : modules) {
            if (user.getPermissions() != null) {
                m = DaoManager.load(Module.class, new Criterion[]{
                        Restrictions.eq("id", module)
                });

                if (m != null) {
                    PermissionWrapper tem = user.getPermissions().get(
                            m.get(0).getCode());
                    if (tem != null) {
                        list.add(tem);
                    }
                }
            }
        }
        return list;
    }

    public static List<Long> getModuleByPage(PageTypes page) {
        if (page.getCode().contains("EDIT")) {
            String p = page.getCode().replace("EDIT", "LIST");
            return ModulesHolder.getInstance().getModules().get(p);
        }
        return ModulesHolder.getInstance().getModules().get(page.getCode());
    }
}
