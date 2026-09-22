package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum WorkListOrderColumns {
    SURNAME("surname"),
    NAME("name"),
    BIRTHDATE("birthDate"),
    SECTOR("asapSectorDescription"),
    EXAMTYPE("examTypeDescription"),
    ACTIONDATE("latestActionPerformDate"),
    URGENCY("urgencyId"),
    STATE("waitingListRegistrationState"),
    DOCTOR("referringDoctor");

    private String fieldValue;

    private WorkListOrderColumns(String field) {
        this.fieldValue = field;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public String getFieldValue() {
        return fieldValue;
    }
}
