package it.nexera.ris.common.enums;

public enum PageTypes {
    HOME("/Pages/Home.jsf", MenuItem.SETTINGS),
    ERROR("Common/Error.jsf", MenuItem.SETTINGS),

    LOGIN("/Login.jsf", MenuItem.SETTINGS),
    LOGOUT("j_spring_security_logout", MenuItem.SETTINGS),

    // Configuration

    APPLICATION_SETTINGS("/Pages/Configuration/ApplicationSettings.jsf", MenuItem.SETTINGS),

    REPORT_SEARCH("/Pages/ManagementGroup/ReportSearchPage.jsf", MenuItem.SEARCH),

    STATISTIC("/Statistic/?t=", MenuItem.STATISTIC),

    MONITORING_VIEW("/Pages/Configuration/Monitoring.jsf", MenuItem.SETTINGS),

    USER_LIST("/Pages/Configuration/UserList.jsf", MenuItem.SETTINGS),
    USER_EDIT("/Pages/Configuration/UserEdit.jsf", MenuItem.SETTINGS),
    USER_PROFILE_VIEW("/Pages/Configuration/UserProfileView.jsf", MenuItem.SETTINGS),

    ROLE_LIST("/Pages/Configuration/RoleList.jsf", MenuItem.SETTINGS),
    ROLE_EDIT("/Pages/Configuration/RoleEdit.jsf", MenuItem.SETTINGS),

    EVENT_CALENDAR_LIST("/Pages/Configuration/EventCalendarList.jsf", MenuItem.SETTINGS),
    EVENT_CALENDAR_EDIT("/Pages/Configuration/EventCalendarEdit.jsf", MenuItem.SETTINGS),

    DOCUMENT_TEMPLATE_LIST("/Pages/Configuration/DocumentTemplateList.jsf", MenuItem.SETTINGS),
    DOCUMENT_TEMPLATE_EDIT("/Pages/Configuration/DocumentTemplateEdit.jsf", MenuItem.SETTINGS),

    INSTANCE_PHASES_EDIT("/Pages/Configuration/InstancePhasesEdit.jsf", MenuItem.SETTINGS),
    INSTANCE_PHASES_LIST("/Pages/Configuration/InstancePhasesList.jsf", MenuItem.SETTINGS),

    // Dictionary

    SECTOR_LIST("/Pages/Dictionaries/SectorList.jsf", true, MenuItem.SETTINGS),
    SECTOR_EDIT("/Pages/Dictionaries/SectorEdit.jsf", true, MenuItem.SETTINGS),

    HOSPITAL_LIST("/Pages/Dictionaries/HospitalList.jsf", true, MenuItem.SETTINGS),
    HOSPITAL_EDIT("/Pages/Dictionaries/HospitalEdit.jsf", true, MenuItem.SETTINGS),

    PRIORITY_LIST("/Pages/Dictionaries/PriorityList.jsf", true, MenuItem.SETTINGS),

    PACKAGE_LIST("/Pages/Dictionaries/PackageList.jsf", true, MenuItem.SETTINGS),
    PACKAGE_EDIT("/Pages/Dictionaries/PackageEdit.jsf", true, MenuItem.SETTINGS),

    EXAM_TYPE_LIST("/Pages/Dictionaries/ExamTypeList.jsf", true, MenuItem.SETTINGS),
    EXAM_TYPE_EDIT("/Pages/Dictionaries/ExamTypeEdit.jsf", true, MenuItem.SETTINGS),

    OSIRIX_LIST("/Pages/Dictionaries/OsirixList.jsf", true, MenuItem.SETTINGS),
    OSIRIX_EDIT("/Pages/Dictionaries/OsirixEdit.jsf", true, MenuItem.SETTINGS),

    DVD_PRODUCER_LIST("/Pages/Dictionaries/DVDProducerList.jsf", true, MenuItem.SETTINGS),
    DVD_PRODUCER_EDIT("/Pages/Dictionaries/DVDProducerEdit.jsf", true, MenuItem.SETTINGS),

    PACS_LIST("/Pages/Dictionaries/PacsList.jsf", true, MenuItem.SETTINGS),
    PACS_EDIT("/Pages/Dictionaries/PacsEdit.jsf", true, MenuItem.SETTINGS),

    DIRECT_RESERVATION_LIST("/Pages/Dictionaries/DirectReservationList.jsf", true, MenuItem.SETTINGS),
    DIRECT_RESERVATION_EDIT("/Pages/Dictionaries/DirectReservationEdit.jsf", true, MenuItem.SETTINGS),

    TEMPLATE_DOCUMENT_MODEL_LIST("/Pages/Dictionaries/TemplateDocumentModelList.jsf", true, MenuItem.SETTINGS),

    RADIOLOGY_EXAM_LIST("/Pages/Dictionaries/RadiologyExamList.jsf", true, MenuItem.SETTINGS),
    RADIOLOGY_EXAM_EDIT("/Pages/Dictionaries/RadiologyExamEdit.jsf", true, MenuItem.SETTINGS),

    DIAGNOSTIC_LIST("/Pages/Dictionaries/DiagnosticList.jsf", true, MenuItem.SETTINGS),
    DIAGNOSTIC_EDIT("/Pages/Dictionaries/DiagnosticEdit.jsf", true, MenuItem.SETTINGS),

    URGENCY_LIST("/Pages/Dictionaries/UrgencyList.jsf", true, MenuItem.SETTINGS),
    URGENCY_EDIT("/Pages/Dictionaries/UrgencyEdit.jsf", true, MenuItem.SETTINGS),

    EMAIL_TEMPLATE_EDIT("/Pages/Dialog/EmailTemplateEdit.xhtml", MenuItem.SETTINGS),

    // ConfigurationArea

    DICTIONARY_LIST("/Pages/ConfigurationArea/DictionaryList.jsf", true, MenuItem.SETTINGS),

    PATIENT_LIST("/Pages/ConfigurationArea/PatientList.jsf", MenuItem.SETTINGS),
    PATIENT_EDIT("/Pages/ConfigurationArea/PatientEdit.jsf", MenuItem.SETTINGS),
    PATIENT_VIEW("/Pages/ConfigurationArea/PatientView.jsf", MenuItem.SETTINGS),

    // ManagementGroup

    PATIENT_SEARCH("/Pages/ManagementGroup/PatientSearchList.jsf", MenuItem.PATIENT_SEARCH),
    DOCUMENT_GENERATION("/Pages/ManagementGroup/DocumentGeneration.jsf", MenuItem.WORKLIST),
    GLOSSARY_DIALOG("/Pages/ManagementGroup/glossaryDialog.jsf", MenuItem.WORKLIST),
    PDF_VIEW("/Pages/ManagementGroup/PDFBrowsingPage.jsf", MenuItem.WORKLIST),
    RADIOLOGY_EXAM_REQUEST("/Pages/ManagementGroup/RadiologyExamRequestEdit.jsf", MenuItem.PATIENT_SEARCH),
    WORKLIST("/Pages/ManagementGroup/WorkList.jsf", MenuItem.WORKLIST),
    WAITINGLIST_REGISTRATION_LIST("/Pages/ManagementGroup/WaitingListRegistrationList.jsf", MenuItem.WAITING_LIST),
    DATE_CONFIRMATION("/Pages/ManagementGroup/DateConfirmation.jsf", MenuItem.WAITING_LIST),
    RESERVATION_PAGE("/Pages/ManagementGroup/ReservationPage.jsf", MenuItem.WAITING_LIST),
    SIGN_FILE_SEND("resources/jnlp/SignFile.jsp", MenuItem.WAITING_LIST);

    private String page;

    private MenuItem menuItem;

    private boolean dictionaryPage;

    private PageTypes(String page) {
        this.page = page;
        dictionaryPage = false;
    }

    private PageTypes(String page, boolean dictionaryPage) {
        this.page = page;
        this.dictionaryPage = dictionaryPage;
    }

    private PageTypes(String page, boolean dictionaryPage, MenuItem menuItem) {
        this.page = page;
        this.dictionaryPage = dictionaryPage;
        this.menuItem = menuItem;
    }

    private PageTypes(String page, MenuItem menuItem) {
        this.page = page;
        this.menuItem = menuItem;
    }

    public static String getPageByPath(String path) {
        for (PageTypes type : PageTypes.values()) {
            if (path.indexOf(type.getPagesContext()) != -1) {
                return type.getPagesContext();
            }
        }

        return "";
    }

    public static PageTypes getPageTypeByPath(String path) {
        if (path == null) {
            return null;
        }

        for (PageTypes type : PageTypes.values()) {
            if (path.indexOf(type.getPagesContext()) != -1) {
                return type;
            }
        }

        return null;
    }

    public static PageTypes getPageTypeByCode(String code) {
        if (code == null) {
            return null;
        }

        for (PageTypes type : PageTypes.values()) {
            if (code.equalsIgnoreCase(type.name())) {
                return type;
            }
        }

        return null;
    }

    public String getPagesContext() {
        return this.page;
    }

    public static PageTypes getEditPageByClass(String className) {
        if (className.contains("Short")) {
            className = className.replace("Short", "");
        }

        for (PageTypes type : PageTypes.values()) {
            if (type.getPagesContext().contains("/" + className + "Edit")) {
                return type;
            }
        }

        if (className.endsWith("VIEW")) {
            className = className.substring(0, className.indexOf("VIEW"));

            for (PageTypes type : PageTypes.values()) {
                if (type.getPagesContext().contains("/" + className + "Edit")) {
                    return type;
                }
            }
        }

        return null;
    }

    public static PageTypes getViewPageByClass(String className) {
        if (className.contains("Short")) {
            className = className.replace("Short", "");
        }

        for (PageTypes type : PageTypes.values()) {
            if (type.getPagesContext().contains("/" + className + "View")) {
                return type;
            }
        }

        return null;
    }

    public static PageTypes getListPageByClass(String className) {
        if (className.contains("Short")) {
            className = className.replace("Short", "");
        }

        for (PageTypes type : PageTypes.values()) {
            if (type.getPagesContext().contains("/" + className + "List")) {
                return type;
            }
        }

        return null;
    }

    public String getCode() {
        return this.name();
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public boolean isDictionaryPage() {
        return dictionaryPage;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

}
