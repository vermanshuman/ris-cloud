package it.nexera.ris.persistence;

import org.hibernate.Session;

public interface ISessionAction extends IAction {

    Session getSession();

}
