package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.beans.entities.IEntity;

import java.util.Iterator;
import java.util.List;

public class ListHelper {
    public static <T extends IEntity> boolean contains(List<T> list, T obj) {
        for (T item : list) {
            if (obj.getId().equals(item.getId())) {
                return true;
            }
        }

        return false;
    }

    public static <T extends IEntity> boolean contains(List<T> list, Long id) {
        for (T item : list) {
            if (id.equals(item.getId())) {
                return true;
            }
        }

        return false;
    }

    public static <T extends IEntity> T get(List<T> list, Long id) {
        for (T item : list) {
            if (id.equals(item.getId())) {
                return item;
            }
        }

        return null;
    }

    public static <T extends IEntity> int getIndex(List<T> list, Long id) {
        for (T item : list) {
            if (item.getId().equals(id)) {
                return list.indexOf(item);
            }
        }

        return -1;
    }

    public static <T extends IEntity> void remove(List<T> list, Long id) {
        int index = getIndex(list, id);
        if (index > -1) {
            list.remove(index);
        }
    }

    public static <T extends IEntity> Long getMaxId(List<T> list) {
        Long max = 1l;
        for (T item : list) {
            if (item.getId() > max) {
                max = item.getId();
            }
        }

        return max;
    }

    public static <T> String toString(List<T> list) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(list)) {
            Iterator<T> iterator = list.iterator();
            Object obj = iterator.next();
            stringBuilder.append(obj != null ? obj.toString() : "");
            while (iterator.hasNext()) {
                obj = iterator.next();
                stringBuilder.append("\r\n");
                stringBuilder.append(obj != null ? obj.toString() : "");
            }
        }
        return stringBuilder.toString();
    }
}
