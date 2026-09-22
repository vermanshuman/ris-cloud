package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Entity;
import org.hibernate.HibernateException;
import org.hibernate.criterion.*;

import javax.faces.model.SelectItem;
import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Used to fill drop down list with database values
 */
public class ComboboxHelper extends BaseHelper {
    public static <T> List<SelectItem> fillList(Class<T> clazz)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return fillList(clazz, true);
    }

    public static <T> List<SelectItem> fillList(Class<T> clazz,
                                                boolean showNotSelected) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return fillList(clazz, showNotSelected, false);
    }

    public static <T> List<SelectItem> fillList(Class<T> clazz,
                                                boolean showNotSelected, boolean showAllElement)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        if (clazz.isEnum()) {
            return fillList(clazz.getEnumConstants(), null, showNotSelected,
                    showAllElement);
        } else {
            List<SelectItem> list = new ArrayList<SelectItem>();
            if (showNotSelected) {
                list.add(SelectItemHelper.getNotSelected());
            }
            if (showAllElement) {
                list.add(SelectItemHelper.getAllElement());
            }
            for (T item : DaoManager.load(clazz)) {
                Entity entity = (Entity) item;
                list.add(new SelectItem(entity.getId(), entity.toString()));
            }

            return list;
        }
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               List<Long> excludeList) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return fillList(clazz, null, excludeList);
    }

    public static <T> List<SelectItem> fillList(Class<T> clazz,
                                                T[] excludeList, boolean showNotSelected, boolean showAllElement)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return fillList(clazz.getEnumConstants(), excludeList, showNotSelected,
                showAllElement);
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               Order order, List<Long> excludeList) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return fillList(clazz, new Criterion[]{}, order, excludeList);
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               Criterion[] ctiretions, Order order, List<Long> excludeList)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        List<SelectItem> list = new ArrayList<SelectItem>();
        list.add(SelectItemHelper.getNotSelected());

        for (T item : DaoManager.load(clazz, ctiretions, order)) {
            if (excludeList == null || !excludeList.contains(item.getId())) {
                list.add(new SelectItem(item.getId(), item.toString()));
            }
        }

        return list;

    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               Order order) throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return fillList(clazz, order, true);
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               Order order, boolean addNotSelected) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return fillList(clazz, order, new Criterion[]{}, addNotSelected);
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               Order order, SimpleExpression expression)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return fillList(clazz, order, new Criterion[]{
                expression
        });
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               Order order, Criterion[] criterions) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return fillList(clazz, order, criterions, true);
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               Order order, Criterion[] criterions, boolean addNotSelected)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return fillList(clazz, order, criterions, addNotSelected, false);
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               Order order, Criterion[] criterions, boolean addNotSelected,
                                                               boolean addAllElements) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        List<SelectItem> list = new ArrayList<SelectItem>();
        if (addNotSelected) {
            list.add(SelectItemHelper.getNotSelected());
        }
        if (addAllElements) {
            list.add(SelectItemHelper.getAllElement());
        }

        for (T item : DaoManager.load(clazz, criterions, order)) {
            list.add(new SelectItem(item.getId(), item.toString()));
        }

        return list;
    }

    public static <T extends Entity> List<SelectItem> fillList(List<T> list,
                                                               boolean showNotSelected) throws HibernateException,
            PersistenceBeanException {
        return fillList(list, showNotSelected, false);
    }

    public static <T extends Entity> List<SelectItem> fillList(List<T> list,
                                                               boolean showNotSelected, boolean showAllElement)
            throws HibernateException, PersistenceBeanException {
        List<SelectItem> list1 = new ArrayList<SelectItem>();
        if (showNotSelected) {
            list1.add(SelectItemHelper.getNotSelected());
        } else if (showAllElement) {
            list1.add(SelectItemHelper.getAllElement());
        }

        for (T item : list) {
            list1.add(new SelectItem(item.getId(), item.toString()));
        }

        return list1;
    }

    public static List<SelectItem> fillList(Object[] array) {
        return fillList(array, null, true, false);
    }

    public static List<SelectItem> fillList(Object[] array,
                                            boolean showNotSelected, boolean showAllElement) {
        return fillList(array, null, showNotSelected, showAllElement);
    }

    public static List<SelectItem> fillList(Object[] array, Object[] exclude,
                                            boolean showNotSelected, boolean showAllElement) {
        List<SelectItem> list = new ArrayList<SelectItem>();
        if (showNotSelected) {
            list.add(SelectItemHelper.getNotSelected());
        }
        if (showAllElement) {
            list.add(SelectItemHelper.getAllElement());
        }
        if (array.length > 0) {
            boolean isEnum = array[0].getClass().isEnum();

            Method getIdMethod = null;

            try {
                getIdMethod = array[0].getClass().getMethod("getId");
            } catch (SecurityException e) {
                LogHelper.log(log, e);
            } catch (NoSuchMethodException e) {
            }

            for (Object item : array) {
                if (exclude != null && contains(exclude, item)) {
                    continue;
                }

                try {
                    list.add(new SelectItem(
                            getIdMethod == null ? (isEnum ? item.getClass()
                                    .getMethod("name").invoke(item) : item
                                    .toString()) : getIdMethod.invoke(item),
                            item.toString()));
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
        }
        return list;
    }

    private static boolean contains(Object[] list, Object obj) {
        for (Object item : list) {
            if (item.equals(obj)) {
                return true;
            }
        }

        return false;
    }

    public static List<SelectItem> fillList(int from, int to, String prefix,
                                            boolean addNumbersPrefix) {
        List<SelectItem> list = new ArrayList<SelectItem>();
        for (int i = from; i <= to; i++) {
            if (i == 1 && !ValidationHelper.isNullOrEmpty(prefix)) {
                list.add(new SelectItem(i, String.format("%s", prefix)));
            } else {
                if (addNumbersPrefix) {
                    list.add(new SelectItem(i, String.format("%d%s %s", i,
                            getNumberPrefix(i), prefix)));
                } else {
                    list.add(new SelectItem(i, String
                            .format("%d %s", i, prefix)));
                }
            }
        }

        return list;
    }

    public static List<SelectItem> fillList(int from, int to, int step) {
        List<SelectItem> list = new ArrayList<SelectItem>();
        for (int i = from; i <= to; i = i + step) {
            list.add(new SelectItem(i, String.valueOf(i)));
        }

        return list;
    }

    public static List<SelectItem> fillWeekDays() {
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.ITALY);
        String weekdays[] = dfs.getWeekdays();
        List<SelectItem> list = new ArrayList<SelectItem>();
        for (int i = 0; i < weekdays.length; i++) {
            if (!ValidationHelper.isNullOrEmpty(weekdays[i])) {
                list.add(new SelectItem(i, weekdays[i]));
            }
        }

        return list;
    }

    public static List<SelectItem> fillMonth() {
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.US);
        String month[] = dfs.getMonths();
        List<SelectItem> list = new ArrayList<SelectItem>();
        for (int i = 0; i < month.length; i++) {
            list.add(new SelectItem(i, month[i]));
        }

        return list;
    }

    private static String getNumberPrefix(Integer number) {
        if (number >= 21) {
            switch (Integer
                    .parseInt(number.toString().substring(
                            number.toString().length() - 1,
                            number.toString().length()))) {
                case 1: {
                    return "st";
                }
                case 2: {
                    return "nd";
                }
                case 3: {
                    return "rd";
                }
                default: {
                    return "th";
                }
            }
        } else {
            switch (number) {
                case 1: {
                    return "st";
                }
                case 2: {
                    return "nd";
                }
                case 3: {
                    return "rd";
                }
                default: {
                    return "th";
                }
            }
        }
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               String orderField, SimpleExpression expression)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return fillList(clazz, orderField, new Criterion[]{
                expression
        });
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               String orderField, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return fillList(clazz, orderField, criterions, true);
    }

    public static <T extends Entity> List<SelectItem> fillList(Class<T> clazz,
                                                               String orderField, Criterion[] criterions, boolean addNotSelected)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        List<SelectItem> list = new ArrayList<SelectItem>();
        if (addNotSelected) {
            list.add(SelectItemHelper.getNotSelected());
        }

        for (T item : DaoManager.load(clazz, criterions, Order.asc(orderField))) {
            list.add(new SelectItem(item.getId(), item.toString()));
        }

        return list;
    }

    public static <T extends Entity> List<SelectItem> newFillList(Class<T> clazz,
                                                               String orderField, Criterion[] criterions, String field)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        List<SelectItem> list = new ArrayList<SelectItem>();

        for (Object item : DaoManager.loadFields(clazz, criterions, Order.asc(orderField),new Projection[]{
                Projections.property("id"), Projections.property(field)})) {
            list.add(new SelectItem(((Object []) item)[0], (String) ((Object []) item)[1]));
        }

        return list;
    }
}
