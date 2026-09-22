/**
 *
 */
package it.nexera.ris.persistence;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import org.hibernate.HibernateException;

/**
 * @author Alex Chelombitko
 * 06.02.2013
 */
public interface IAction {
    public void onBeforeExecute();

    public void onException(Exception e) throws Exception;

    public void onExecuted();

    public void execute(Object obj) throws Exception;

    public void onSuccess() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException;
}
