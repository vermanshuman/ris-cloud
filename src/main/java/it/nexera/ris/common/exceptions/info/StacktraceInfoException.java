package it.nexera.ris.common.exceptions.info;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import java.util.Date;

public class StacktraceInfoException extends Exception {

    public <T extends IndexedEntity> StacktraceInfoException(T entity) {
        super(entity.getClass().getSimpleName() + " id:" + entity.getId() + "; Date:" + new Date());
    }
}
