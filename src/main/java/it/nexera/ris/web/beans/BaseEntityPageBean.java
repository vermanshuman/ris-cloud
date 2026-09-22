package it.nexera.ris.web.beans;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.SpecialPermissionTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.PermissionsHelper;
import it.nexera.ris.common.helpers.UsersRolesHelper;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.web.beans.base.AccessBean;
import org.hibernate.HibernateException;
import org.primefaces.PrimeFaces;

import java.util.List;

public abstract class BaseEntityPageBean extends BaseValidationPageBean {

    private List<Role> userRoles;

    public boolean getCanView() {
        try {
            return AccessBean.canViewPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean getCanCreate() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        try {
            return AccessBean.canCreateInPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean getCanEdit() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        try {
            return AccessBean.canEditInPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean getCanReserve() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.RESERVE);
    }

    public boolean getCanCancel() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.SEARCH_CANCEL);
    }

    public boolean getCanAnnullate() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.SEARCH_ANNULATE);
    }

    public boolean getCanRestore() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.RESTORE);
    }

    public boolean getCanAccept() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.ACCEPT);
    }

    public boolean getCanCancelWorklist() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.CANCEL);
    }

    public boolean getCanModifyWorklist() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.MODIFY);
    }

    public boolean getCanPerformWorklist() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.PERFORM);
    }

    public boolean getCanShow() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.SHOW);
    }

    public boolean getCanAnnulateExecutionPerformed()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.ANNULATE_EXECUTION_PERFORMED);
    }

    public boolean getCanAcceptanceOfFutureBooking()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.ACCEPTANCE_OF_FUTURE_BOOKING);
    }

    public boolean getCanProduceCD()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.PRODUCE_CD);
    }

    public boolean getCanGenerateXmlCda()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.GENERATE_XML_CDA);
    }

    public boolean getCanReportWorklist() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.REPORT);
    }

    public boolean getCanSuperCancelAcceptance()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.SUPER_CANCEL_ACCEPTANCE);
    }

    public boolean getCanSuperCancelExecution()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.SUPER_CANCEL_EXECUTION);
    }

    public boolean getCanDelete() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        try {
            return AccessBean.canDeleteInPage(this.getCurrentPage());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean getCanCloseReport() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.CLOSE_PDF_DOCUMENT);
    }

    public boolean getCanUnlockPDF() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.CAN_UNLOCK_PDF);
    }

    public boolean getCanDigitalSign() throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        return PermissionsHelper
                .getPermission(SpecialPermissionTypes.DIGITAL_SIGNATURE);
    }

    public List<Role> getUserRoles() {
        if (this.userRoles == null) {
            this.userRoles = UsersRolesHelper.getUserRoles();
        }

        return userRoles;
    }

    public boolean getUserEntityPage() {
        return PageTypes.USER_LIST.equals(this.getCurrentPage());
    }

    public void closeDialog() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }
}
