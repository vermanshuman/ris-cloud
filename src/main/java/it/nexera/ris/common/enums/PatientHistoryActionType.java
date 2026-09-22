package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum PatientHistoryActionType {
    CREATE,
    MODIFY,
    MODIFY_WL,
    ASSIGN_WL,
    ASSIGN_SERVICE,
    UNDO,
    EXECUTE,
    REFUSE,
    ACCEPT,
    RECOVER,
    CLOSE,
    WAIT,
    START,
    SUSPEND;

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

}
