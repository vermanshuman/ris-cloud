package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.domain.Patient;

import java.lang.reflect.Method;
import java.util.Date;

public class TemplateEntity {
    private Entity entity;

    private Class<? extends Entity> clazz;

    private UserWrapper currentUser;

    @SuppressWarnings("unchecked")
    public TemplateEntity(Entity entity, UserWrapper currentUser) {
        this.entity = entity;
        this.clazz = (Class<? extends Entity>) this.getClazz();
        this.currentUser = currentUser;
    }

    public String invokeGetMethod(String methodName) throws Throwable {
        if (clazz != null) {
            Method method = null;
            try {
                method = clazz.getMethod(methodName);
                return correctMethodInvoking(method, this.entity);
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
                try {
                    method = clazz.getMethod("getPatient"
                            + methodName.substring(3));
                    return correctMethodInvoking(method, this.entity);
                } catch (SecurityException e1) {
                } catch (NoSuchMethodException e1) {
                    try {
                        method = TemplateEntity.class.getMethod(methodName);
                        return correctMethodInvoking(method, this);
                    } catch (SecurityException e2) {
                    } catch (NoSuchMethodException e2) {
                        try {
                            method = clazz.getMethod("getPatient");
                            Patient p = (Patient) method.invoke(this.entity);
                            method = Patient.class.getMethod(methodName);
                            return correctMethodInvoking(method, p);
                        } catch (Exception e3) {
                        }
                    }
                }
            }
        }

        throw new Throwable("Cannot process such method");
    }

    private String correctMethodInvoking(Method method, Object instance) {
        Object result = null;
        try {
            result = method.invoke(instance);
        } catch (Exception e) {
        }

        if (result != null) {
            if (result instanceof Date) {
                result = DateTimeHelper.toString((Date) result);
            } else if (!(result instanceof String)) {
                result = result.toString();
            }

            return (String) result;
        }
        return "";
    }

    public Date getCurrentDate() {
        return new Date();
    }

    public String getOperatorName() {
        return this.currentUser.getFirstName();
    }

    public String getOperatorSurname() {
        return this.currentUser.getLastName();
    }

    private Object getClazz() {
        if (this.entity instanceof Patient) {
            return Patient.class;
        }

        return this.entity.getClass();
    }
}
