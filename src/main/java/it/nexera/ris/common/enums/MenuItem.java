package it.nexera.ris.common.enums;

public enum MenuItem {
    PATIENT_SEARCH("patientSearch"),
    WORKLIST("worklist"),
    WAITING_LIST("waitingList"),
    SEARCH("search"),
    SETTINGS("settings"),
    STATISTIC("statistic");

    private String menuItemName;

    private MenuItem(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

}
