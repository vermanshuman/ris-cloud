package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum MenuItemTypes {
    APPLICATION_SETTINGS("app_settings.png"),
    MONITORING("app_settings.png"),
    USER_LIST("users.png"),
    ROLE_LIST("roles.png"),
    DICTIONARY_LIST("dictionary.png"),
    PATIENT_LIST("patients.png"),
    EVENT_CALENDAR_LIST("events.png"),
    DOCUMENT_TEMPLATE_LIST("document_templates.png"),
    INSTANCE_PHASES_LIST("instance_phases.png"),
    PATIENT_SEARCH("patients.png"),
    WORKLIST("w_list.png"),
    WAITINGLIST("w_list.png"),
    DATE_CONFIRMATION_VIEW("w_list.png");

    private String image;

    private boolean draggable;

    MenuItemTypes(String image) {
        this.image = image;

        this.draggable = true;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return "menu_" + this.name().toLowerCase();
    }

    public boolean isDraggable() {
        return draggable;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

}
