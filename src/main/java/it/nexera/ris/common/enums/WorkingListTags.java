package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum WorkingListTags {
    SELECTED_DATE("%selected_date%"),
    PATIENTS("%patients%"),
    CURRENT_DATE("%current_date%"),
    SELECTED_CALENDAR("%selected_calendar%"),

    WORKING_PROGRAM_TITLE("%working_program_title%"),
    WORKING_PROGRAM_PATIENT("%working_program_patient%"),
    WORKING_PROGRAM_TIME("%working_program_time%"),
    WORKING_PROGRAM_BIRTH_DATE("%working_program_birth_date%"),
    WORKING_PROGRAM_SECTOR("%working_program_sector%"),
    WORKING_PROGRAM_STATE("%working_program_state%"),
    WORKING_PROGRAM_EXAMS("%working_program_exams%"),
    WORKING_PROGRAM_PRINT_DATE("%working_program_print_date%");

    private String value;

    private WorkingListTags(String value) {
        this.setValue(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

}
