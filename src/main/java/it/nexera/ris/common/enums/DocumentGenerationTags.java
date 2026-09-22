package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum DocumentGenerationTags {
    CURRENT_DATE("current_date", "getCurrentDate"),
    PAGE_BREAK("page_break"),

    OPERATOR_NAME("operator_name", "getOperatorName"),
    OPERATOR_SURNAME("operator_surname", "getOperatorSurname"),
    OPERATOR_REFERRING_DICTION("operator_referring_diction"),

    PATIENT_NAME("patient_name", "getName"),
    PATIENT_SURNAME("patient_surname", "getSurname"),
    PATIENT_FISCAL_CODE("patient_fiscal_code", "getFiscalCode"),
    PATIENT_ADDRESS_NUMBER("patient_address_number", "getAddressNumber"),
    PATIENT_ADDRESS_CITY("patient_city", "getCityDescription"),
    PATIENT_ADDRESS_PROVINCE("patient_province", "getProvinceDescription"),
    PATIENT_PHONE("patient_phone", "getPhone"),
    PATIENT_SECOND_PHONE("patient_second_phone", "getSecondPhone"),
    PATIENT_CELL("patient_cell", "getCell"),
    PATIENT_MAIL("patient_mail", "getMail"),
    PATIENT_ADDRESS_INFO("patient_address", "getAddress"),
    PATIENT_BIRTH_CITY("patient_birth_city", "getBirthCityDescription"),
    PATIENT_BIRTH_DATE("patient_birth_date", "getBirthDate"),
    PATIENT_SEX_TYPE("patient_sex_type", "getSexTypeShortValue"),

    REQUEST_DATE("request_date", "getItemRequestDate"),
    ACCEPT_DATE("accept_date", "getItemAcceptDate"),
    PERFORM_DATE("execution_date", "getItemExecutionDateForTag"),
    RESERVE_DATE("reserve_date", "getItemReserveDate"),

    REQUEST_TIME("request_time", "getItemRequestTime"),
    ACCEPT_TIME("accept_time", "getItemAcceptTime"),
    PERFORM_TIME("execution_time", "getItemExecutionTime"),
    RESERVE_TIME("reserve_time", "getItemReserveTime"),

    DIAGNOSTIC_QUESTION("diagnostic_question", "getDiagnosticQuestion"),
    PROVENIENZA_SECTOR_NAME(
            "provenienza_sector_name",
            "getAsapSectorDescription"),

    ACTIVITY_LINE("activity_line", "getAsapActivityLine"),

    USER_PERFORM_REQUEST("user_perform_request", "getItemUserPerform"),
    USER_PERFORM_ACCEPT("user_perform_accept", "getItemUserAccept"),
    USER_PERFORM_DELETE("user_perform_delete", "getItemUserDelete"),

    EXAMS_LIST_DESCRIPTION("exams_list_description", "getExamsDescription"),
    ACCESS_NUMBER("access_number", "getAccessNumberForTag"),
    CARD_NUMBER("card_number", "getCardNumberForTag"),

    TSRM("trsm", "getItemTsrmForTag"),

    CANCEL_COMMENT("cancel_comment", "getDeleteComment"),
    CANCEL_PATIENTS("cancel_patients", "getCancelPatients"),
    CANCEL_RAD_EXAMS("cancel_exam_items", "getCancelExamItems"),

    REPORT_RESULT("report_result", "getReportResult"),

    DOCTOR("doctor", "getReferringDoctor"),

    PERFORMANCE_LIST("performance_list", "getPerformanceList"),

    PSD_NUMBER("psd_number", "getPsdNumberForTag"),

    EXAM_TYPE_DESCRIPTION("exam_type_description", "getExamTypeDescriptionForTag"),

    CURRENT_TIME("current_time", "getCurrentTime"),
    EXAMS_TABLE("EXAMS_TABLE", "getExamsTable"),
    EXAMS_AGGREGATED_TABLE("EXAMS_AGGREGATED_TABLE", "getExamsAggregatedTable"),
    DELIVERY_DOSE("DELIVERY_DOSE", "getDeliveryDoseTable");


    private String tag;

    private String getMethod;

    private DocumentGenerationTags(String editorValue, String getMethod) {
        this.tag = editorValue;
        this.getMethod = getMethod;
    }

    private DocumentGenerationTags(String editorValue) {
        this.tag = editorValue;
        this.getMethod = null;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public DocumentGenerationTags getByTag(String tag) {
        for (DocumentGenerationTags item : DocumentGenerationTags.values()) {
            if (item.getTag().equals(tag)) {
                return item;
            }
        }

        return null;
    }

    public String getTagReal() {
        return tag;
    }

    public String getTag() {
        return tag.contains("%") ? tag : '%' + tag + '%';
    }

    public void setTag(String editorValue) {
        this.tag = editorValue.contains("%") ? editorValue
                : '%' + editorValue + '%';
    }

    public String getGetMethod() {
        return getMethod;
    }

    public void setGetMethod(String getMethod) {
        this.getMethod = getMethod;
    }

}
