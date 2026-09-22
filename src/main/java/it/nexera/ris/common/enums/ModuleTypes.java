package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ModuleTypes {
    USER_MANAGEMENT(
            PageTypes.USER_EDIT,
            PageTypes.USER_LIST,
            PageTypes.USER_PROFILE_VIEW),
    ROLE_MANAGEMENT(PageTypes.ROLE_EDIT, PageTypes.ROLE_LIST),

    APPLICATION_SETTINGS_MANAGEMENT(PageTypes.APPLICATION_SETTINGS),

    MONITORING_MANAGEMENT(PageTypes.MONITORING_VIEW),

    EVENT_CALENDAR_MANAGMENT(
            PageTypes.EVENT_CALENDAR_LIST,
            PageTypes.EVENT_CALENDAR_EDIT),

    DOCUMENT_TEMPLATE_MANAGMENT(
            PageTypes.DOCUMENT_TEMPLATE_LIST,
            PageTypes.DOCUMENT_TEMPLATE_EDIT),

    INSTANCE_PHASES(
            PageTypes.INSTANCE_PHASES_LIST,
            PageTypes.INSTANCE_PHASES_EDIT),

    // Dictionaries
    DICTIONARY_LIST(PageTypes.DICTIONARY_LIST),

    PATIENT_MANAGEMENT(
            PageTypes.PATIENT_EDIT,
            PageTypes.PATIENT_LIST,
            PageTypes.PATIENT_VIEW),

    HOSPITAL_MANAGEMENT(PageTypes.HOSPITAL_EDIT, PageTypes.HOSPITAL_LIST),
    PACKAGE_MANAGEMENT(PageTypes.PACKAGE_LIST, PageTypes.PACKAGE_EDIT),

    RADIOLOGY_EXAM_MANAGEMENT(
            PageTypes.RADIOLOGY_EXAM_LIST,
            PageTypes.RADIOLOGY_EXAM_EDIT,
            PageTypes.RADIOLOGY_EXAM_REQUEST),

    DIAGNOSTIC_MANAGEMENT(PageTypes.DIAGNOSTIC_LIST, PageTypes.DIAGNOSTIC_EDIT),

    EXAM_TYPE_MANAGEMENT(PageTypes.EXAM_TYPE_LIST, PageTypes.EXAM_TYPE_EDIT),

    SECTOR_MANAGEMENT(PageTypes.SECTOR_LIST, PageTypes.SECTOR_EDIT),
    PRIORITY_LIST(PageTypes.PRIORITY_LIST),
    TEMPLATE_DOCUMENT_MODEL_LIST(PageTypes.TEMPLATE_DOCUMENT_MODEL_LIST),

    URGENCY_MANAGEMENT(PageTypes.URGENCY_LIST, PageTypes.URGENCY_EDIT),

    // ManagementGroup

    PATIENT_SEARCH(PageTypes.PATIENT_SEARCH),
    WAITINGLIST(PageTypes.WAITINGLIST_REGISTRATION_LIST),
    WORKLIST(PageTypes.WORKLIST),
    DATE_CONFIRMATION_VIEW(PageTypes.DATE_CONFIRMATION);

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    private List<PageTypes> pages = new ArrayList<PageTypes>();

    private ModuleTypes(PageTypes... pages) {
        this.setPages(Arrays.asList(pages));
    }

    public static List<ModuleTypes> LoadByPage(PageTypes page) {
        List<ModuleTypes> list = new ArrayList<ModuleTypes>();

        for (ModuleTypes type : ModuleTypes.values()) {
            for (PageTypes item : type.getPages()) {
                if (item.equals(page)) {
                    list.add(type);
                }
            }
        }

        return list;
    }

    public List<PageTypes> getPages() {
        return pages;
    }

    public void setPages(List<PageTypes> pages) {
        this.pages = pages;
    }

}
