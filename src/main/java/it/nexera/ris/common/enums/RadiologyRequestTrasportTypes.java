package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum RadiologyRequestTrasportTypes {
    DEAMBULANTE, A_LETTO, BARELLA, IN_REPARTO, SEDIA_A_ROTELLE;

    public static RadiologyRequestTrasportTypes getByName(String name) {
        for(RadiologyRequestTrasportTypes item : RadiologyRequestTrasportTypes.values()){
            if(item.name().equals(name))
                return item;
        }
        return null;
    }

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }
}
