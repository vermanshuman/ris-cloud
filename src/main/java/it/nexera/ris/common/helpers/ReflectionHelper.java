package it.nexera.ris.common.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelper {

    public static Field getField(Class<?> bean, String name) {
        Class<?> superBean = bean.getSuperclass();
        try {
            return bean.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (superBean == Object.class) {
                return null;
            }
        }
        return getField(superBean, name);
    }

    public static Method getMethod(Class<?> bean, String name, Class<?>... parameters) {
        Class<?> superBean = bean.getSuperclass();
        try {
            return bean.getDeclaredMethod(name, parameters);
        } catch (NoSuchMethodException e) {
            if (superBean == Object.class) {
                return null;
            }
        }
        return getMethod(superBean, name, parameters);
    }

}
