package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.SessionManager;

import java.util.List;
import java.util.Map;

public class SessionHelper {
    public static void put(Object obj, String ID_IN_SESSION) {
        getSession().put(ID_IN_SESSION, obj);
    }

    public static Object get(String ID_IN_SESSION) {
        return getSession().get(ID_IN_SESSION);
    }

    public static void putIds(List<Long> ids, String ID_IN_SESSION) {
        getSession().put(ID_IN_SESSION, ids);
    }

    @SuppressWarnings("unchecked")
    public static List<Long> getIds(String ID_IN_SESSION) {
        return (List<Long>) getSession().get(ID_IN_SESSION);
    }

    public static void removeObject(String ID_IN_SESSION) {
        getSession().remove(ID_IN_SESSION);
    }

    private static Map<String, Object> getSession() {
        return SessionManager.getInstance().getSessionBean().getSession();
    }
}
