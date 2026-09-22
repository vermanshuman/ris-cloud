package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum DoseClass {
    ZEROTH(1L),
    FIRST(2L),
    SECOND(3L),
    THIRD(4L),
    FOURTH(5L);

    private final Long id;

    DoseClass(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public static DoseClass findById(Long id) {
        for (DoseClass doseClass : DoseClass.values()) {
            if (doseClass.getId().equals(id)) {
                return doseClass;
            }
        }

        return null;
    }

    public Long getId() {
        return id;
    }
}
