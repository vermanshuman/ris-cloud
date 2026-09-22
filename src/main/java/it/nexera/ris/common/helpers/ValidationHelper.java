package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validation helper class Used for server-side validation
 */
public class ValidationHelper {

    public transient final static Logger log = LogManager.getLogger(ValidationHelper.class);

    public static String EMPTY = "";

    public static boolean isNullOrEmptyMultiple(Object... args) {
        for (Object obj : args) {
            if (obj == null) {
                return true;
            } else {
                if (obj instanceof String) {
                    String str = (String) obj;
                    if (str.trim().isEmpty()) {
                        return true;
                    }
                }
                if (obj instanceof List<?>) {
                    List<?> list = (List<?>) obj;
                    if (list.isEmpty()) {
                        return true;
                    }
                }
                if (obj instanceof IndexedEntity) {
                    IndexedEntity indexedEntity = (IndexedEntity) obj;
                    if (isNullOrEmpty(indexedEntity.getId())) {
                        return true;
                    }
                }
                if (obj instanceof Long) {
                    Long lStr = (Long) obj;
                    if (lStr.longValue() == 0l) {
                        return true;
                    }
                }
                if (obj instanceof Integer) {
                    Integer integer = (Integer) obj;
                    if (integer.intValue() == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isNullOrEmpty(String str) {
        if (str == null || str.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(Object str) {
        if (str == null) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(Boolean str) {
        return str == null;
    }

    public static boolean isNullOrEmpty(Date str) {
        if (str == null) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(List<?> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(Set<?> set) {
        if (set == null || set.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmptyEditor(String str) {
        if (str.equals("<br>")) {
            str = null;
        }

        if (str == null || str.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(Long str) {
        if (str == null || str == 0l) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(Integer str) {
        if (str == null || str == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(Double str) {
        if (str == null) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(IndexedEntity obj) {
        if (obj == null || ValidationHelper.isNullOrEmpty(obj.getId())) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmpty(Object[] obj) {
        return obj == null || obj.length == 0;
    }

    public static boolean isAlphanumeric(String value) {
        for (Character c : value.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean checkCriterions(Class<T> clazz,
                                              Criterion criterions[]) {
        try {
            return (DaoManager.load(clazz, criterions).size() != 0);
        } catch (HibernateException e) {

        } catch (PersistenceBeanException e) {

        } catch (IllegalAccessException e) {

        }

        return false;
    }

    public static <T extends Entity> boolean isUnique(Class<T> clazz,
                                                      String fieldname, String fieldvalue, Long id) {
        return isUnique(clazz, fieldname, fieldvalue, null, id);
    }

    public static <T extends Entity> boolean isUnique(Class<T> clazz,
                                                      String fieldname, String fieldvalue, SimpleExpression ex, Long id) {
        return isUnique(clazz, fieldname, fieldvalue, ex, null, id);
    }

    public static <T extends Entity> boolean isUnique(Class<T> clazz,
                                                      String fieldname, String fieldvalue, SimpleExpression ex,
                                                      CriteriaAlias[] aliases, Long id) {
        try {
            return (DaoManager.load(
                    clazz,
                    aliases,
                    new Criterion[]
                            {Restrictions.eq(fieldname, fieldvalue).ignoreCase(),
                                    Restrictions.ne("id", id == null ? 0 : id), ex})
                    .size() == 0);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return false;
    }

    public static <T extends Entity> boolean isUnique(Class<T> clazz,
                                                      String fieldname, String fieldvalue, Criterion[] criterions,
                                                      CriteriaAlias[] aliases, Long id) {
        int size = 0;
        if (criterions != null) {
            size = criterions.length;
        }

        Criterion[] restrictions = new Criterion[size + 2];

        restrictions[0] = Restrictions.eq(fieldname, fieldvalue).ignoreCase();
        restrictions[1] = Restrictions.ne("id", id == null ? 0 : id);

        if (criterions != null) {
            for (int i = 0; i < criterions.length; i++) {
                restrictions[i + 2] = criterions[i];
            }
        }

        try {
            return (DaoManager.load(clazz, aliases, restrictions).size() == 0);
        } catch (HibernateException e) {
            LogHelper.log(log, e);
        } catch (PersistenceBeanException e) {
            LogHelper.log(log, e);
        } catch (IllegalAccessException e) {
            LogHelper.log(log, e);
        }

        return false;
    }

    public static boolean checkMailCorrectFormat(String value) {
        return checkCorrectFormatByExpression(
                "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}\\s{0,10}|[0-9]{1,3}\\s{0,10})(\\]?)$",
                value);
    }

    public static boolean checkCorrectFormatByExpression(String expression,
                                                         String value) {
        return Pattern.matches(expression, value);
    }

    public static boolean validPassword(String value) {
        if (value != null && (value.length() != 0) && (value.length() < 8)) {
            return false;
        }

        boolean digit = false;
        boolean upperCase = false;
        boolean lowerCase = false;

        if (value != null) {
            for (char ch : value.toCharArray()) {
                if (Character.isUpperCase(ch)) {
                    upperCase = true;
                }
                if (Character.isLowerCase(ch)) {
                    lowerCase = true;
                }
                if (Character.isDigit(ch)) {
                    digit = true;
                }
            }
        }
        if (!digit || !lowerCase || !upperCase) {
            return false;
        }
        return true;
    }

    public static boolean checkUserNameFormat(String value) {
        if (value != null && (value.length() < 3 || value.length() > 20)) {
            return false;
        }

        return true;
    }

    public static boolean checkFieldLengthFrom(String value, int from) {
        if (value != null && value.length() >= from) {
            return true;
        }

        return false;
    }

    public static boolean checkFieldLengthTo(String value, int to) {
        if (value != null && value.length() <= to) {
            return true;
        }

        return false;
    }

    public static boolean checkFieldLength(String value, int from, int to) {
        if (checkFieldLengthFrom(value, from) && checkFieldLengthTo(value, to)) {
            return true;
        }

        return false;
    }

    public static boolean isNumber(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        char[] valueMas = value.toCharArray();
        for (Character c : valueMas) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkTimeFormat(String value) {
        String[] strs = value.split(":");
        if (Integer.parseInt(strs[0]) > 23) {
            return false;
        }

        if (Integer.parseInt(strs[1]) > 59) {
            return false;
        }

        return true;
    }

    public static boolean checkURLFormat(String value) {
        try {
            @SuppressWarnings("unused")
            URL url = new URL(value);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
