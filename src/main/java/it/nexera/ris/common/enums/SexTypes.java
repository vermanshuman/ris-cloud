package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum SexTypes {

    MALE, FEMALE, NOT_IDENTIFY;

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public String getShortValue() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatterShort(this));
    }
}
