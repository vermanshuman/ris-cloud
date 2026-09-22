package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum SpecialPermissionTypes {
    ACCEPT(null, PermissionType.ACTION_AVAILABILITY),
    ANNULATE_EXECUTION_PERFORMED(null, PermissionType.ACTION_AVAILABILITY),
    ANNULATE_EXECUTION_ACCEPTED(null, PermissionType.ACTION_AVAILABILITY),
    CANCEL(null, PermissionType.ACTION_AVAILABILITY),
    CAN_LOOK_APP_SETTINGS(
            PageTypes.APPLICATION_SETTINGS,
            PermissionType.PAGE_ACCESS),
    CAN_LOOK_DICTIONARY_LIST(
            PageTypes.DICTIONARY_LIST,
            PermissionType.PAGE_ACCESS),
    CAN_LOOK_MONITORING(PageTypes.MONITORING_VIEW, PermissionType.PAGE_ACCESS),
    CAN_UNLOCK_PDF(null, PermissionType.ACTION_AVAILABILITY),
    CLOSE_PDF_DOCUMENT(null, PermissionType.ACTION_AVAILABILITY),
    MODIFY(null, PermissionType.ACTION_AVAILABILITY),
    REPORT(null, PermissionType.ACTION_AVAILABILITY),
    RESERVE(null, PermissionType.ACTION_AVAILABILITY),
    RESTORE(null, PermissionType.ACTION_AVAILABILITY),
    PERFORM(null, PermissionType.ACTION_AVAILABILITY),
    SEARCH_CANCEL(null, PermissionType.ACTION_AVAILABILITY),
    SEARCH_ANNULATE(null, PermissionType.ACTION_AVAILABILITY),
    SEE_ALL_SECTORS(null, PermissionType.ACTION_AVAILABILITY),
    SHOW(null, PermissionType.ACTION_AVAILABILITY),
    SUPER_CANCEL_ACCEPTANCE(null, PermissionType.ACTION_AVAILABILITY),
    SUPER_CANCEL_EXECUTION(null, PermissionType.ACTION_AVAILABILITY),
    DIGITAL_SIGNATURE(null, PermissionType.ACTION_AVAILABILITY),
    ACCEPTANCE_OF_FUTURE_BOOKING(null, PermissionType.ACTION_AVAILABILITY),
    PRODUCE_CD(null, PermissionType.ACTION_AVAILABILITY),
    GENERATE_XML_CDA(null, PermissionType.ACTION_AVAILABILITY),
    CUPREQUESTS(null, PermissionType.ACTION_AVAILABILITY),;

    public enum PermissionType {
        PAGE_ACCESS, ACTION_AVAILABILITY;
    }

    private PageTypes pageType;

    private PermissionType permissionType;

    private SpecialPermissionTypes(PageTypes pageType,
                                   PermissionType permissionType) {
        this.pageType = pageType;
        this.permissionType = permissionType;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public PageTypes getPageType() {
        return pageType;
    }

    public void setPageType(PageTypes pageType) {
        this.pageType = pageType;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

}
