package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class BaseHelper {

    protected static final transient Logger log = LogManager.getLogger(BaseHelper.class);

    public static <T extends IndexedEntity> List<Long> getIds(List<T> list) {
        List<Long> ids = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(list)) {
            for (T item : list) {
                ids.add(item.getId());
            }
        }

        return ids;
    }
}
