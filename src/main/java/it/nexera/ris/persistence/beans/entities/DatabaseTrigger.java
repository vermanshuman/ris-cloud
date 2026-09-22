package it.nexera.ris.persistence.beans.entities;

import java.util.ArrayList;
import java.util.List;

public class DatabaseTrigger {

    private static final String INSERT_UPDATE_SHORT_REQUEST = "create or replace TRIGGER update_short_request " +
            "AFTER INSERT OR UPDATE " +
            "ON RADIOLOGY_EXAM_REQUEST " +
            "FOR EACH ROW " +

            "declare " +
            "  any_rows_found number; " +

            "BEGIN " +

            "select count(*) " +
            "  into   any_rows_found " +
            "  from RADIOLOGY_EXAM_REQUEST_SHORT " +
            "  where ID=:new.ID;  " +

            "  if any_rows_found = 1 then " +

            "update RADIOLOGY_EXAM_REQUEST_SHORT  " +
            "set CREATE_DATE= :new.CREATE_DATE, " +
            "CREATE_USER_ID= :new.CREATE_USER_ID, " +
            "UPDATE_DATE= :new.UPDATE_DATE, " +
            "UPDATE_USER_ID= :new.UPDATE_USER_ID, " +
            "VERSION= :new.VERSION, " +
            "ASAP_ACTIVITY_LINE= :new.ASAP_ACTIVITY_LINE, " +
            "ASAP_SECTOR_CODE= :new.ASAP_SECTOR_CODE, " +
            "ASAP_SECTOR_DESCTIPTION= :new.ASAP_SECTOR_DESCTIPTION, " +
            "ASAP_SECTOR_ID= :new.ASAP_SECTOR_ID, " +
            "ASSIGN_DATE= :new.ASSIGN_DATE, " +
            "ASSIGN_USER_ID= :new.ASSIGN_USER_ID, " +
            "BLOCKING_DATE= :new.BLOCKING_DATE, " +
            "DELETE_MOTIVATION= :new.DELETE_MOTIVATION, " +
            "DISABLED_REQUEST= :new.DISABLED_REQUEST, " +
            "DISPLAY_NAME= :new.DISPLAY_NAME, " +
            "FORWARDED= :new.FORWARDED, " +
            "ITEMS_DESCR= :new.ITEMS_DESCR, " +
            "LAST_HL7_ORM_SENDER_USER_ID= :new.LAST_HL7_ORM_SENDER_USER_ID, " +
            "LAST_PDF_URL= :new.LAST_PDF_URL, " +
            "LATEST_ACTION_DATE= :new.LATEST_ACTION_DATE, " +
            "LATEST_ACTION_PERFORM_DATE= :new.LATEST_ACTION_PERFORM_DATE, " +
            "MODIFIED_EXAM_TYPES_TEXT= :new.MODIFIED_EXAM_TYPES_TEXT, " +
            "MODIFIED_EXAMS_TEXT= :new.MODIFIED_EXAMS_TEXT, " +
            "PERFORM_DATE= :new.PERFORM_DATE, " +
            "PREDEFINED_SECTOR= :new.PREDEFINED_SECTOR, " +
            "REFERRING_DOCTOR= :new.REFERRING_DOCTOR, " +
            "REQUEST_DATE= :new.REQUEST_DATE, " +
            "RESERVE_DATE= :new.RESERVE_DATE, " +
            "SENDING_STATUS= :new.SENDING_STATUS, " +
            "SORT_NAME= :new.SORT_NAME, " +
            "TRANSPORT_TYPE= :new.TRANSPORT_TYPE, " +
            "UNDO_PERFORMED= :new.UNDO_PERFORMED, " +
            "USER_CLOSING_REPORT_ID= :new.USER_CLOSING_REPORT_ID, " +
            "WL_REGISTRATION_STATE= :new.WL_REGISTRATION_STATE, " +
            "EXAM_TYPE_ID= :new.EXAM_TYPE_ID, " +
            "HL7_FIELDS_FORM_ASAP_ID= :new.HL7_FIELDS_FORM_ASAP_ID, " +
            "HOSPITAL_ID= :new.HOSPITAL_ID, " +
            "PATIENT_ID= :new.PATIENT_ID, " +
            "SECTOR_ID= :new.SECTOR_ID, " +
            "URGENCY_ID= :new.URGENCY_ID " +
            "where ID=:new.ID; " +

            "else " +
            "insert into RADIOLOGY_EXAM_REQUEST_SHORT " +
            "(ID, CREATE_DATE, CREATE_USER_ID, UPDATE_DATE, UPDATE_USER_ID, VERSION, ASAP_ACTIVITY_LINE, " +
            "ASAP_SECTOR_CODE, ASAP_SECTOR_DESCTIPTION, ASAP_SECTOR_ID, ASSIGN_DATE, ASSIGN_USER_ID, BLOCKING_DATE, " +
            "DELETE_MOTIVATION, DISABLED_REQUEST, DISPLAY_NAME, FORWARDED, ITEMS_DESCR, LAST_HL7_ORM_SENDER_USER_ID, " +
            "LAST_PDF_URL, LATEST_ACTION_DATE, LATEST_ACTION_PERFORM_DATE, MODIFIED_EXAM_TYPES_TEXT, MODIFIED_EXAMS_TEXT, " +
            "PERFORM_DATE, PREDEFINED_SECTOR, REFERRING_DOCTOR, REQUEST_DATE, RESERVE_DATE, SENDING_STATUS, SORT_NAME, " +
            "TRANSPORT_TYPE, UNDO_PERFORMED, USER_CLOSING_REPORT_ID, WL_REGISTRATION_STATE, EXAM_TYPE_ID, " +
            "HL7_FIELDS_FORM_ASAP_ID, HOSPITAL_ID, PATIENT_ID, SECTOR_ID, URGENCY_ID)  " +

            "VALUES( :new.ID, :new.CREATE_DATE, :new.CREATE_USER_ID, :new.UPDATE_DATE, :new.UPDATE_USER_ID, " +
            ":new.VERSION, :new.ASAP_ACTIVITY_LINE, :new.ASAP_SECTOR_CODE, :new.ASAP_SECTOR_DESCTIPTION, " +
            ":new.ASAP_SECTOR_ID, :new.ASSIGN_DATE, :new.ASSIGN_USER_ID, :new.BLOCKING_DATE, :new.DELETE_MOTIVATION, " +
            ":new.DISABLED_REQUEST, :new.DISPLAY_NAME, :new.FORWARDED, :new.ITEMS_DESCR, :new.LAST_HL7_ORM_SENDER_USER_ID, " +
            ":new.LAST_PDF_URL, :new.LATEST_ACTION_DATE, :new.LATEST_ACTION_PERFORM_DATE, :new.MODIFIED_EXAM_TYPES_TEXT, " +
            ":new.MODIFIED_EXAMS_TEXT, :new.PERFORM_DATE, :new.PREDEFINED_SECTOR, :new.REFERRING_DOCTOR, :new.REQUEST_DATE, " +
            ":new.RESERVE_DATE, :new.SENDING_STATUS, :new.SORT_NAME, :new.TRANSPORT_TYPE, :new.UNDO_PERFORMED, " +
            ":new.USER_CLOSING_REPORT_ID, :new.WL_REGISTRATION_STATE, :new.EXAM_TYPE_ID, :new.HL7_FIELDS_FORM_ASAP_ID, " +
            ":new.HOSPITAL_ID, :new.PATIENT_ID, :new.SECTOR_ID, :new.URGENCY_ID ); " +

            " end if; " +

            "END;";

    private static final String DELETE_SHORT_REQUEST = "CREATE OR REPLACE TRIGGER delete_short_request " +
            "AFTER DELETE " +
            "ON RADIOLOGY_EXAM_REQUEST " +
            "FOR EACH ROW " +

            "BEGIN " +

            "delete from RADIOLOGY_EXAM_REQUEST_SHORT where id = :old.ID; " +

            "END;";

    private static final String INSERT_SHORT_REQUEST_ITEM = "create or replace TRIGGER insert_short_request_item " +
            "AFTER INSERT " +
            "ON rad_exam_request_item " +
            "FOR EACH ROW " +

            "BEGIN " +

            "insert into rad_exam_request_item_short " +

            "(ID, CREATE_DATE, CREATE_USER_ID, UPDATE_DATE, UPDATE_USER_ID, VERSION, ACCEPT_DATE, ACCESS_NUMBER_CODE, " +
            "ACCESS_NUMBER_ID, ACCESS_NUMBER_YEAR, ADDED_MANUALLY, ASAP_PL_ORD_NUM, AUTO_SAVE_DATE, CARD_NUMBER, " +
            "DELETE_COMMENT, DELETE_USER, DIAGNOSTIC_QUESTION, FILE_ENTITY_ID, FORWARDED, NOTE, OPERATOR, PERFORM_DATE, " +
            "RAD_EXAM_REQUEST_ID, REPORT_DATE, REPORT_RESULT, REPORT_RESULT_AUTOSAVED, REPORT_SAVE_DATE, REQUEST_DATE, " +
            "DATE_CHANGED, REQUESTING_DOCTOR, RESERVE_DATE, TRSM, URGENT_REQUEST, WL_REGISTRATION_STATE, RAD_EXAM_ID, " +
            "RADILOGY_EXAM_PACKAGE_ID ) " +

            "VALUES( :new.ID, :new.CREATE_DATE, :new.CREATE_USER_ID, :new.UPDATE_DATE, " +
            ":new.UPDATE_USER_ID, :new.VERSION, :new.ACCEPT_DATE, :new.ACCESS_NUMBER_CODE, :new.ACCESS_NUMBER_ID, " +
            ":new.ACCESS_NUMBER_YEAR, :new.ADDED_MANUALLY, :new.ASAP_PL_ORD_NUM, :new.AUTO_SAVE_DATE, :new.CARD_NUMBER, " +
            ":new.DELETE_COMMENT, :new.DELETE_USER, :new.DIAGNOSTIC_QUESTION, :new.FILE_ENTITY_ID, :new.FORWARDED, " +
            ":new.NOTE, :new.OPERATOR, :new.PERFORM_DATE, :new.RAD_EXAM_REQUEST_ID, :new.REPORT_DATE, :new.REPORT_RESULT, " +
            ":new.REPORT_RESULT_AUTOSAVED, :new.REPORT_SAVE_DATE, :new.REQUEST_DATE, :new.DATE_CHANGED, " +
            ":new.REQUESTING_DOCTOR, :new.RESERVE_DATE, :new.TRSM, :new.URGENT_REQUEST, :new.WL_REGISTRATION_STATE, " +
            ":new.RAD_EXAM_ID, :new.RADILOGY_EXAM_PACKAGE_ID ); " +

            "END;";

    private static final String UPDATE_SHORT_REQUEST_ITEM = "create or replace TRIGGER update_short_request_item " +
            "AFTER UPDATE " +
            "ON rad_exam_request_item " +
            "FOR EACH ROW " +
            "declare   any_rows_found number; " +
            "any_requests_found number; " +
            "BEGIN " +

            "select count(*)   " +
            "into   any_rows_found   " +
            "from rad_exam_request_item_short   " +
            "where ID=:new.ID; " +

            "if any_rows_found = 1 then " +

            "update rad_exam_request_item_short  " +
            "set CREATE_DATE=:new.CREATE_DATE, CREATE_USER_ID=:new.CREATE_USER_ID, UPDATE_DATE=:new.UPDATE_DATE, " +
            "UPDATE_USER_ID=:new.UPDATE_USER_ID, VERSION=:new.VERSION, ACCEPT_DATE=:new.ACCEPT_DATE, " +
            "ACCESS_NUMBER_CODE=:new.ACCESS_NUMBER_CODE, ACCESS_NUMBER_ID=:new.ACCESS_NUMBER_ID, " +
            "ACCESS_NUMBER_YEAR=:new.ACCESS_NUMBER_YEAR, ADDED_MANUALLY=:new.ADDED_MANUALLY, " +
            "ASAP_PL_ORD_NUM=:new.ASAP_PL_ORD_NUM, AUTO_SAVE_DATE=:new.AUTO_SAVE_DATE, CARD_NUMBER=:new.CARD_NUMBER, " +
            "DELETE_COMMENT=:new.DELETE_COMMENT, DELETE_USER=:new.DELETE_USER, DIAGNOSTIC_QUESTION=:new.DIAGNOSTIC_QUESTION, " +
            "FILE_ENTITY_ID=:new.FILE_ENTITY_ID, FORWARDED=:new.FORWARDED, NOTE=:new.NOTE, OPERATOR=:new.OPERATOR, " +
            "PERFORM_DATE=:new.PERFORM_DATE, RAD_EXAM_REQUEST_ID=:new.RAD_EXAM_REQUEST_ID, REPORT_DATE=:new.REPORT_DATE, " +
            "REPORT_RESULT=:new.REPORT_RESULT, REPORT_RESULT_AUTOSAVED=:new.REPORT_RESULT_AUTOSAVED, " +
            "REPORT_SAVE_DATE=:new.REPORT_SAVE_DATE, REQUEST_DATE=:new.REQUEST_DATE, DATE_CHANGED=:new.DATE_CHANGED, " +
            "REQUESTING_DOCTOR=:new.REQUESTING_DOCTOR, RESERVE_DATE=:new.RESERVE_DATE, TRSM=:new.TRSM, " +
            "URGENT_REQUEST=:new.URGENT_REQUEST, WL_REGISTRATION_STATE=:new.WL_REGISTRATION_STATE, RAD_EXAM_ID=:new.RAD_EXAM_ID, " +
            "RADILOGY_EXAM_PACKAGE_ID=:new.RADILOGY_EXAM_PACKAGE_ID " +
            "where ID=:new.ID; " +

            "else " +
            "select count(*)   into   any_requests_found   from radiology_exam_request_short   where ID=:new.rad_exam_request_id; " +
            "if any_requests_found = 0 then " +
            "insert into radiology_exam_request_short (id) values (:new.rad_exam_request_id); " +
            "end if; " +

            "insert into rad_exam_request_item_short " +

            "(ID, CREATE_DATE, CREATE_USER_ID, UPDATE_DATE, UPDATE_USER_ID, VERSION, ACCEPT_DATE, ACCESS_NUMBER_CODE, " +
            "ACCESS_NUMBER_ID, ACCESS_NUMBER_YEAR, ADDED_MANUALLY, ASAP_PL_ORD_NUM, AUTO_SAVE_DATE, CARD_NUMBER, DELETE_COMMENT, " +
            "DELETE_USER, DIAGNOSTIC_QUESTION, FILE_ENTITY_ID, FORWARDED, NOTE, OPERATOR, PERFORM_DATE, RAD_EXAM_REQUEST_ID, " +
            "REPORT_DATE, REPORT_RESULT, REPORT_RESULT_AUTOSAVED, REPORT_SAVE_DATE, REQUEST_DATE, DATE_CHANGED, " +
            "REQUESTING_DOCTOR, RESERVE_DATE, TRSM, URGENT_REQUEST, WL_REGISTRATION_STATE, RAD_EXAM_ID, RADILOGY_EXAM_PACKAGE_ID ) " +

            "VALUES( :new.ID, :new.CREATE_DATE, :new.CREATE_USER_ID, :new.UPDATE_DATE, :new.UPDATE_USER_ID, :new.VERSION, " +
            ":new.ACCEPT_DATE, :new.ACCESS_NUMBER_CODE, :new.ACCESS_NUMBER_ID, :new.ACCESS_NUMBER_YEAR, :new.ADDED_MANUALLY, " +
            ":new.ASAP_PL_ORD_NUM, :new.AUTO_SAVE_DATE, :new.CARD_NUMBER, :new.DELETE_COMMENT, :new.DELETE_USER, " +
            ":new.DIAGNOSTIC_QUESTION, :new.FILE_ENTITY_ID, :new.FORWARDED, :new.NOTE, :new.OPERATOR, :new.PERFORM_DATE, " +
            ":new.RAD_EXAM_REQUEST_ID, :new.REPORT_DATE, :new.REPORT_RESULT, :new.REPORT_RESULT_AUTOSAVED, :new.REPORT_SAVE_DATE, " +
            ":new.REQUEST_DATE, :new.DATE_CHANGED, :new.REQUESTING_DOCTOR, :new.RESERVE_DATE, :new.TRSM, :new.URGENT_REQUEST, " +
            ":new.WL_REGISTRATION_STATE, :new.RAD_EXAM_ID, :new.RADILOGY_EXAM_PACKAGE_ID ); " +

            "end if; " +

            "END;";

    private static final String DELETE_SHORT_REQUEST_ITEM = "create or replace TRIGGER delete_short_request_item " +
            "AFTER DELETE " +
            "ON rad_exam_request_item " +
            "FOR EACH ROW " +
            "BEGIN " +

            "delete from rad_exam_request_item_short " +
            "where id = :old.ID; " +

            "END;";

    private static final String DIC_DIAGNOSTIC_BEFORE_UPDATE = "CREATE OR REPLACE TRIGGER DIC_DIAGNOSTIC_BEFORE_UPDATE " +
            "BEFORE UPDATE " +
            "  ON DIC_DIAGNOSTIC " +
            "FOR EACH ROW " +
            "  BEGIN " +
            "    IF :new.SECTOR_ID IS NULL THEN " +
            "      :new.SECTOR_ID := :OLD.SECTOR_ID; " +
            "    END IF; " +
            "  END; ";

    public static List<String> getTriggers() {
        List<String> triggers = new ArrayList<>();
        triggers.add(INSERT_UPDATE_SHORT_REQUEST);
        triggers.add(DELETE_SHORT_REQUEST);
        triggers.add(INSERT_SHORT_REQUEST_ITEM);
        triggers.add(UPDATE_SHORT_REQUEST_ITEM);
        triggers.add(DELETE_SHORT_REQUEST_ITEM);
        triggers.add(DIC_DIAGNOSTIC_BEFORE_UPDATE);

        return triggers;
    }
}
