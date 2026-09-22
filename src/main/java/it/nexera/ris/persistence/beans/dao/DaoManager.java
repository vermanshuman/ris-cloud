package it.nexera.ris.persistence.beans.dao;

import it.nexera.ris.common.annotations.AliasColumn;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.info.StacktraceInfoException;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.transform.Transformers;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.lang.InstantiationException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main class for working with database
 */
public class DaoManager {

    protected static transient final Logger log = LogManager.getLogger(BaseHelper.class);

    private static final Logger traceInfoLog = CustomLibLoggerFactory.getActivityTraceInfoLogger();

    public static int getQueryTimeout() {
        return Integer.parseInt(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.QUERY_TIMEOUT).getValue());
    }

    public static void showQueryTimeoutMessage() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                ResourcesHelper.getValidation("couldNotUploadPage"),
                ResourcesHelper.getValidation("reloadLater"));
    }

    public static Session getSession() throws PersistenceBeanException,
            IllegalAccessException {
        if (FacesContext.getCurrentInstance() == null) {
            throw new IllegalAccessException("");
        }
        return PersistenceSessionManager.getBean().getSession();
    }

    public static Object getMax(Class<? extends IEntity> clazz,
                                String maxField, Criterion[] criterions)
            throws PersistenceBeanException, IllegalAccessException {
        return getMax(clazz, maxField, null, criterions);
    }

    public static Object getMax(Class<? extends IEntity> clazz,
                                String maxField, CriteriaAlias[] aliases, Criterion[] criterions)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.max(maxField));
        criteria.setTimeout(getQueryTimeout());
        if (aliases != null) {
            for (CriteriaAlias ca : aliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        try {
            return criteria.uniqueResult();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return 0;
        }
    }

    public static Long getCountNoDistinct(Class<? extends IEntity> clazz, String field,
                                          CriteriaAlias[] criteriaAlias, Criterion[] criterions) throws PersistenceBeanException,
            IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.count(field));
        criteria.setTimeout(getQueryTimeout());
        if (criterions != null) {
            for (Criterion criterion : criterions) {
                criteria.add(criterion);
            }
        }
        if (criteriaAlias != null) {
            for (CriteriaAlias ca : criteriaAlias) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }
        Object result = null;
        try {
            result = criteria.uniqueResult();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
        }

        if (ValidationHelper.isNullOrEmpty(result)) {
            result = "0";
        }

        return Long.parseLong(String.valueOf(result));
    }


    public static Long getCount(Class<? extends IEntity> clazz, String field,
                                Criterion[] criterions) throws PersistenceBeanException,
            IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.countDistinct(field));
        criteria.setTimeout(getQueryTimeout());
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        Object result = null;
        try {
            result = criteria.uniqueResult();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
        }

        if (ValidationHelper.isNullOrEmpty(result)) {
            result = "0";
        }

        return Long.parseLong(String.valueOf(result));
    }

    public static Long getCount(Class<? extends IEntity> clazz, String field, String tableAlias,
                                Criterion[] criterions) throws PersistenceBeanException,
            IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz, tableAlias);
        criteria.setProjection(Projections.countDistinct(field));
        criteria.setTimeout(getQueryTimeout());
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        Object result = null;
        try {
            result = criteria.uniqueResult();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
        }

        if (ValidationHelper.isNullOrEmpty(result)) {
            result = "0";
        }

        return Long.parseLong(String.valueOf(result));
    }

    public static Long getCount(Class<? extends IEntity> clazz, String field,
                                CriteriaAlias[] criteriaAlias, Criterion[] criterions)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.countDistinct(field));
        criteria.setTimeout(getQueryTimeout());

        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }

        for (CriteriaAlias ca : criteriaAlias) {
            criteria.createAlias(ca.getTable(), ca.getAliasName(),
                    ca.getJoinType());
        }

        Object result = null;
        try {
            result = criteria.uniqueResult();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
        }

        if (ValidationHelper.isNullOrEmpty(result)) {
            result = "0";
        }

        return Long.parseLong(String.valueOf(result));
    }

    public static Long getCount(Class<? extends IEntity> clazz, String field)
            throws PersistenceBeanException, IllegalAccessException {
        return getCount(clazz, field, new Criterion[]{});
    }

    public static List<?> find(String query, Object[] objs)
            throws PersistenceBeanException,
            IllegalAccessException {
        Query queryObject = getSession().createQuery(query);

        queryObject.setTimeout(getQueryTimeout());

        if (objs != null) {
            for (int i = 0; i < objs.length; i++) {
                queryObject.setParameter(i, objs[i]);
            }
        }
        try {
            return queryObject.list();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, Serializable id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (id == null || String.valueOf(id).isEmpty()) {
            return (T) clazz.newInstance();
        }

        return (T) getSession().get(clazz, Long.parseLong(String.valueOf(id)));
    }

    public static <T> T get(Class<T> clazz, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        return get(clazz, new CriteriaAlias[]{}, criterions);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, CriteriaAlias[] criteriaAlias,
                            Criterion[] criterions) throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);

        criteria.setTimeout(getQueryTimeout());

        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        for (CriteriaAlias ca : criteriaAlias) {
            criteria.createAlias(ca.getTable(), ca.getAliasName(),
                    ca.getJoinType());
        }

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        try {
            return (T) criteria.uniqueResult();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return null;
        }
    }

    public static <T> T get(Class<T> clazz, SimpleExpression expression)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        return get(clazz, new Criterion[]{
                expression
        });
    }

    public static <T> List<T> load(Class<T> clazz) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return load(clazz, null, new Order[]{});
    }

    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAlias, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, criteriaAlias, criterions, new Order[]{});
    }

    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAlias, Criterion[] criterions, Order order)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, criteriaAlias, criterions, new Order[]{
                order
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAliases, Criterion[] criterions,
                                                  Order[] orders) throws PersistenceBeanException,
            IllegalAccessException, QueryTimeoutException {
        Criteria criteria = getSession().createCriteria(clazz);

        criteria.setTimeout(getQueryTimeout());

        if (criteriaAliases != null) {
            for (CriteriaAlias ca : criteriaAliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }

            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        }

        if (criterions != null) {
            for (Criterion crit : criterions) {
                if (crit != null) {
                    criteria.add(crit);
                }
            }
        }

        if (orders != null && orders.length > 0) {
            for (Order o : orders) {
                criteria.addOrder(o);
            }
        }
        try {
            return criteria.list();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return null;
        }
    }

    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias criteriaAlias, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, new CriteriaAlias[]{
                criteriaAlias
        }, criterions);
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, criterions, new Order[]{});
    }

    public static <T> List<T> load(Class<T> clazz,
                                   SimpleExpression... restrictions) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        List<Criterion> crits = new ArrayList<Criterion>();
        for (SimpleExpression item : restrictions) {
            crits.add(item);
        }
        return load(clazz, crits.toArray(new Criterion[0]), new Order[]{});
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> load(Class<T> clazz, String sqlQuery)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        try {
            return getSession().createSQLQuery(sqlQuery).setTimeout(getQueryTimeout()).list();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return null;
        }
    }

    public static List<Long> loadIds(Class<?> clazz, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return loadField(clazz, "id", Long.class, null, criterions);
    }

    public static List<Long> loadIds(Class<?> clazz,
                                     CriteriaAlias[] criteriaAlias, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return loadField(clazz, "id", Long.class, criteriaAlias, criterions);
    }

    public static <T> List<T> loadField(Class<?> clazz, String idField,
                                        Class<T> returnType, Criterion[] criterions)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return loadField(clazz, idField, returnType, null, criterions);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> loadField(Class<?> clazz, String idField,
                                        Class<T> returnType, CriteriaAlias[] criteriaAlias,
                                        Criterion[] criterions) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return (List<T>) loadFields(clazz, criteriaAlias, criterions, new Projection[]{Projections.property(idField)});
    }

    public static List<Object> loadFields(Class<?> clazz, Criterion[] criterions, Order order, Projection[] projections)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        ProjectionList projectionList = Projections.projectionList();

        criteria.setTimeout(getQueryTimeout());

        if (projections != null) {
            for (Projection projection : projections) {
                projectionList.add(projection);
            }
        }

        criteria.setProjection(projectionList);

        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        if (order != null) {
            criteria.addOrder(order);
        }

        try {
            return criteria.list();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return null;
        }
    }

    public static List<Object> loadFields(Class<?> clazz, CriteriaAlias[] criteriaAlias, Criterion[] criterions, Projection[] projections)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        ProjectionList projectionList = Projections.projectionList();

        criteria.setTimeout(getQueryTimeout());

        if (projections != null) {
            for (Projection projection : projections) {
                projectionList.add(projection);
            }
        }

        criteria.setProjection(projectionList);

        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        if (criteriaAlias != null) {
            for (CriteriaAlias ca : criteriaAlias) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }
        try {
            return criteria.list();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return null;
        }
    }

    public static <T extends Entity> List<T> load(Class<T> clazz, Order order)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, null, order, null);
    }

    public static <T> List<T> load(Class<T> clazz, Order order,
                                   Integer maxRecords) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return load(clazz, null, order, maxRecords);
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order order) throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, criterions, order, null);
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order order, Integer maxRecords) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (order != null) {
            return load(clazz, criterions, new Order[]{
                    order
            }, maxRecords);
        } else {
            return load(clazz, criterions, new Order[]{}, maxRecords);
        }
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order[] orderList) throws PersistenceBeanException,
            IllegalAccessException {
        return load(clazz, criterions, orderList, null);
    }

    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order[] orderList, Integer maxResult)
            throws PersistenceBeanException, IllegalAccessException {
        return load(clazz, criterions, orderList, maxResult, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions,
                                   Order[] orderList, Integer maxResult, Projection projection)
            throws PersistenceBeanException, IllegalAccessException {
        return load(clazz, criterions, null, orderList, maxResult, projection);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> load(Class<T> clazz, Criterion[] criterions, CriteriaAlias[] criteriaAliases,
                                   Order[] orderList, Integer maxResult, Projection projection)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setTimeout(getQueryTimeout());

        if (maxResult != null) {
            criteria.setMaxResults(maxResult);
        }

        if (criteriaAliases != null) {
            for (CriteriaAlias ca : criteriaAliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }

        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        if (orderList != null) {
            for (Order o : orderList) {
                criteria.addOrder(o);
            }
        }

        if (projection != null) {
            criteria.setProjection(projection);
        }

        try {
            return criteria.list();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> List<T> load(Class<T> clazz,
                                                  CriteriaAlias[] criteriaAliases, Criterion[] criterions, Integer maxresults) throws PersistenceBeanException,
            IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);

        criteria.setTimeout(getQueryTimeout());

        if (criteriaAliases != null) {
            for (CriteriaAlias ca : criteriaAliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }

            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        }

        if (criterions != null) {
            for (Criterion crit : criterions) {
                if (crit != null) {
                    criteria.add(crit);
                }
            }
        }


        if (maxresults != null) {
            criteria.setMaxResults(maxresults);
        }
        try {
            return criteria.list();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return null;
        }
    }

    public static void save(Entity object) throws HibernateException,
            PersistenceBeanException {
        save(object, false);
    }

    public static void save(Entity object, boolean beginTransaction)
            throws HibernateException, PersistenceBeanException {
        Transaction tr = null;
        try {
            if (beginTransaction) {
                tr = DaoManager.getSession().beginTransaction();
            }
            beforeSaveOrUpdate(object);

            if (getSession().contains(object)) {
                getSession().merge(object);
            } else {
                getSession().saveOrUpdate(object);
            }
        } catch (NonUniqueObjectException e) {
            try {
                getSession().merge(object);
            } catch (Exception e1) {
                LogHelper.log(log, e1);
                if (beginTransaction) {
                    if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                        tr.rollback();
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (beginTransaction) {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }
            }
        } finally {
            if (beginTransaction) {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
            }
        }
    }

    public static void beforeSaveOrUpdate(Entity object) {
        if (object.getCreateDate() == null) {
            object.setCreateDate(new Date());
            try {
                if (SessionManager.getInstance().getSessionBean() != null) {
                    object.setCreateUserId(UserHolder.getInstance()
                            .getCurrentUser().getId());
                } else {
                    object.setCreateUserId(0L);
                }
            } catch (Exception e) {
            }
        } else {
            object.setUpdateDate(new Date());
            try {
                if (SessionManager.getInstance().getSessionBean() != null) {
                    object.setUpdateUserId(UserHolder.getInstance()
                            .getCurrentUser().getId());
                } else {
                    object.setUpdateUserId(0L);
                }
            } catch (Exception e) {
            }
        }

        if (object instanceof FileEntity) {
            FileEntity file = ((FileEntity) object);
            if(ValidationHelper.isNullOrEmpty(file.getIncrementVersionOnSave()) || file.getIncrementVersionOnSave()){
                file.setVersionOfSave((file.getVersionOfSave() == null ? 0
                        : file.getVersionOfSave()) + 1);
            }
        } else if (object instanceof RadiologyExamRequest) {
            try {
                LogHelper.log(traceInfoLog, object.toString());
                throw new StacktraceInfoException((RadiologyExamRequest) object);
            } catch (StacktraceInfoException e) {
                LogHelper.log(traceInfoLog, e);
            } catch (Exception e) {
                LogHelper.log(traceInfoLog, e);
            }
        }
    }

    public static void merge(Entity object) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        beforeSaveOrUpdate(object);

        getSession().merge(object);
    }

    public static <T extends IEntity> void remove(T object)
            throws HibernateException, PersistenceBeanException {
        remove(object, false);
    }

    public static <T extends IEntity> void remove(T object,
                                                  boolean beginTransaction) throws HibernateException,
            PersistenceBeanException {
        Transaction tr = null;
        try {
            if (beginTransaction) {
                tr = DaoManager.getSession().beginTransaction();
            }

            getSession().delete(object);

        } catch (Exception e) {
            LogHelper.log(log, e);
            if (beginTransaction) {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.rollback();
                }
            }
        } finally {
            if (beginTransaction) {
                if (tr != null && tr.getStatus() == TransactionStatus.ACTIVE) {
                    tr.commit();
                }
            }
        }
    }

    public static <T extends IEntity> void remove(Class<T> clazz,
                                                  Serializable id, boolean beginTransaction)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        remove(get(clazz, id), beginTransaction);
    }

    public static <T extends IEntity> void remove(Class<T> clazz,
                                                  Serializable id) throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        remove(clazz, id, false);
    }

    public static void addCriteriaIn(String propertyName, List<?> list,
                                     List<Criterion> criterions) {
        criterions.add(addRestrictionIn(propertyName, list));
    }

    public static Criterion addRestrictionIn(String propertyName, List<?> list) {
        Disjunction or = Restrictions.disjunction();
        if (list.size() > 1000) {
            while (list.size() > 1000) {
                List<?> subList = list.subList(0, 1000);
                or.add(Restrictions.in(propertyName, subList));
                list.subList(0, 1000).clear();
            }
        }
        or.add(Restrictions.in(propertyName, list));

        return or;
    }

    @SuppressWarnings("unchecked")
    public static String getField(Class<? extends IEntity> clazz, String field,
                                  Criterion[] criterions, CriteriaAlias[] criteriaAlias)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.setProjection(Projections.property(field));

        criteria.setTimeout(getQueryTimeout());

        if (criterions != null) {
            for (Criterion criterion : criterions) {
                criteria.add(criterion);
            }
        }

        if (criteriaAlias != null) {
            for (CriteriaAlias ca : criteriaAlias) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }

        List<Object> results = null;
        try {
            results = criteria.list();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
        }

        if (!ValidationHelper.isNullOrEmpty(results)) {
            Object result = results.get(0);
            if (result == null) {
                return "";
            }
            return String.valueOf(result);
        } else {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getFields(Class<? extends IEntity> clazz,
                                         Criterion[] criterions, CriteriaAlias[] criteriaAlias,
                                         boolean isDistinct, String... fields)
            throws IllegalAccessException, PersistenceBeanException {
        Criteria criteria = getSession().createCriteria(clazz);
        ProjectionList proList = Projections.projectionList();
        criteria.setTimeout(getQueryTimeout());

        for (String field : fields) {
            proList.add(Projections.property(field));
        }

        if (isDistinct) {
            criteria.setProjection(Projections.distinct(proList));
        } else {
            criteria.setProjection(proList);
        }

        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        if (criteriaAlias != null) {
            for (CriteriaAlias ca : criteriaAlias) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }
        try {
            return criteria.list();
        } catch (QueryTimeoutException e) {
            showQueryTimeoutMessage();
            return null;
        }
    }

    public static <T extends Serializable> List<T> transform(Class<? extends IEntity> clazz, Class<T> transformer,
                                                             Criterion[] criterions, CriteriaAlias[] criteriaAlias, Order[] orders,
                                                             boolean isDistinct, Integer limit)
            throws IllegalAccessException, PersistenceBeanException {
        Criteria criteria = getSession().createCriteria(clazz);
        ProjectionList proList = Projections.projectionList();

        if (transformer != null) {
            Field[] fields = transformer.getDeclaredFields();
            for (Field field : fields) {
                AliasColumn aliasColumn = field.getAnnotation(AliasColumn.class);
                if (aliasColumn != null) {
                    proList.add(Projections.property(aliasColumn.alias()).as(field.getName()));
                }
            }
        }

        if (isDistinct) {
            criteria.setProjection(Projections.distinct(proList));
        } else {
            criteria.setProjection(proList);
        }

        if (criterions != null) {
            for (Criterion crit : criterions) {
                criteria.add(crit);
            }
        }

        if (criteriaAlias != null) {
            for (CriteriaAlias ca : criteriaAlias) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }


        if (orders != null) {
            for (Order order : orders) {
                criteria.addOrder(order);
            }
        }

        if (limit != null) {
            criteria.setMaxResults(limit);
        }

        if (transformer != null) {
            criteria.setResultTransformer(Transformers.aliasToBean(transformer));
        }

        return criteria.list();
    }
}
