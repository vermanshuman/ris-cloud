package it.nexera.ris.persistence.beans.entities;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import org.hibernate.HibernateException;

import java.io.Serializable;

public interface IEntity extends Serializable {
    public Long getId();

    public void setId(Long id);

    public boolean isNew();

    public boolean isCustomId();

    public boolean getDeletable() throws HibernateException,
            PersistenceBeanException, IllegalAccessException;

    public boolean getEditable() throws HibernateException,
            PersistenceBeanException, IllegalAccessException;
}
