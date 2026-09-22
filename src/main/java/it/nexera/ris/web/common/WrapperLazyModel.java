package it.nexera.ris.web.common;


import it.nexera.ris.common.enums.DbEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ExecutionTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.materialized.HistoricalReportMV;
import it.nexera.ris.web.managers.HistoricalReportManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WrapperLazyModel<T extends IEntity, T2 extends IEntity> extends
        LazyDataModel<T> {
    Class<? extends IEntity> clazz;

    private Order[] orders;

    private Criterion[] restrictions;

    private CriteriaAlias[] criteriaAliases;

    private List<String> aliases;

    private int rowIndex = -1;

    private int totalNumRows;

    private List<T> list;

    protected transient final Logger log = LogManager.getLogger(getClass());

    private boolean calculated;

    private static final long serialVersionUID = 5148271666683822739L;

    public WrapperLazyModel(Class<? extends IEntity> clazz, Order[] orders) {
        this(clazz, null, orders);
    }

    public WrapperLazyModel(Class<? extends IEntity> clazz,
                            Criterion[] restrictions, Order[] orders) {
        this(clazz, restrictions, orders, null);
    }

    public WrapperLazyModel(Class<? extends IEntity> clazz,
                            Criterion[] restrictions, Order[] orders,
                            CriteriaAlias[] criteriaAliases) {
        super();
        this.clazz = clazz;
        this.orders = orders;
        this.restrictions = restrictions;
        this.criteriaAliases = criteriaAliases;
    }

    @Override
    public String getRowKey(T entity) {
        return String.valueOf(entity.getId());
    }

    @Override
    public int count(Map<String, FilterMeta> map) {
        log.info("************ Starting lazy count ************");
        Long rowCount = 0L;
        try (Session session = HibernateUtil.getSessionFactory(false).openSession()) {
            WrapperLazyModel.this.aliases = new ArrayList<>();
            Criteria countCriteria = session.createCriteria(clazz);
            countCriteria.setProjection(Projections.rowCount());

            for (Criterion criterion : getCriterion(map, countCriteria)) {
                countCriteria.add(criterion);
            }
            if (restrictions != null) {
                for (Criterion criterion : restrictions) {
                    countCriteria.add(criterion);
                }
            }

            if (criteriaAliases != null) {
                for (CriteriaAlias ca : criteriaAliases) {
                    countCriteria.createAlias(ca.getTable(), ca.getAliasName(),
                            ca.getJoinType());
                }
            }

            rowCount = (Long) countCriteria.uniqueResult();

            WrapperLazyModel.this.setRowCount(rowCount == null ? 0 : rowCount.intValue());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in Fetching List", e);
        }
        return rowCount == null ? 0 : rowCount.intValue();
    }

    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        list = new ArrayList<T>();
        LogHelper.debugInfo(log, "************ Start lazy load ************");
        log.info("Filters: ".concat(filterBy.toString()));
        long startLazyLoadTime = ExecutionTimeHelper.getStartExecutionTime();
        try {
            if (clazz.equals(HistoricalReportMV.class)) {

                try (Session session = HibernateUtil.getSessionFactory(false).openSession()) {
                    List<HistoricalReportMV> listItems = (List<HistoricalReportMV>) WrapperLazyModel.this.loadList(
                            first, pageSize, sortBy, filterBy, session);

                    if (!ValidationHelper.isNullOrEmpty(listItems)) {
                        HistoricalReportManager manager = new HistoricalReportManager(listItems);

                        final List resultList = manager.getResultList(session);
                        if (resultList != null) {
                            list.addAll(resultList);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Error in Fetching List", e);
                }
            }

        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        long lazyLoadExecutionTime = ExecutionTimeHelper.getExecutionTime(startLazyLoadTime);
        log.info("************ End lazy load ************"
                .concat(" Elapsed time (ns): ".concat(String.valueOf(lazyLoadExecutionTime))));
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<T2> loadList(Integer first, Integer pageSize,
                              Map<String, SortMeta> sortBy, Map<String, FilterMeta> filters, Session session)
            throws PersistenceBeanException, IllegalAccessException {
        if (session == null)
            session = DaoManager.getSession();
        Criteria criteria = session.createCriteria(clazz);
        LogHelper.debugInfo(log, "Entity <" + clazz.getSimpleName() + ">");
        LogHelper.debugInfo(log, "first <" + first + ">");
        LogHelper.debugInfo(log, "pageSize <" + pageSize + ">");
        if (pageSize != null) {
            criteria.setMaxResults(pageSize);
        }
        if (first != null) {
            criteria.setFirstResult(first);
        }
        if (criteriaAliases != null) {
            LogHelper.debugInfo(log, "criteriaAliases -> ");
            for (CriteriaAlias ca : criteriaAliases) {
                LogHelper.debugInfo(log, "Table <" + ca.getTable() + ">, AliasName <" + ca.getAliasName() + ">, JoinType <" + ca.getJoinType() + ">");
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
            LogHelper.debugInfo(log, "criteriaAliases end");
        }

        this.aliases = new ArrayList<String>();

        for (Criterion criterion : getCriterion(filters, criteria)) {
            criteria.add(criterion);
        }

        if (restrictions != null) {
            LogHelper.debugInfo(log, "restrictions -> ");
            for (Criterion criterion : restrictions) {
                LogHelper.debugInfo(log, "criterion <" + criterion + ">");
                criteria.add(criterion);
            }
            LogHelper.debugInfo(log, "restrictions end ");
        }

        LogHelper.debugInfo(log, "orders -> ");
        if (sortBy.isEmpty()) {
            for (Order order : this.orders) {
                LogHelper.debugInfo(log, "order <" + order + ">");
                criteria.addOrder(order);
            }
        } else {
            for (SortMeta sort : sortBy.values()) {
                if (sort.getOrder().equals(SortOrder.ASCENDING)) {
                    criteria.addOrder(Order.asc(getCorrectFieldName(
                            sort.getField(), criteria)));
                } else {
                    criteria.addOrder(Order.desc(getCorrectFieldName(
                            sort.getField(), criteria)));
                }
            }
        }
        LogHelper.debugInfo(log, "orders end ");

        criteria.setFetchMode("examRequestItems", FetchMode.SELECT);

        return criteria.list();
    }

    /* (non-Javadoc)
     * @see org.primefaces.model.LazyDataModel#setRowCount(int)
     */
    @Override
    public void setRowCount(int rowCount) {
        this.totalNumRows = rowCount;
        super.setRowCount(rowCount);
    }

    private String getCorrectFieldName(String oldFieldName, Criteria criteria)
            throws HibernateException, PersistenceBeanException {
        if (!oldFieldName.contains(".")) {
            return oldFieldName;
        }
        String fieldName = oldFieldName;
        if (oldFieldName.replace(".strId", ".id").endsWith(".id")) {
            fieldName = oldFieldName.replace(".strId", ".id");
        }

        String tmp = fieldName;
        int aliasCtr = 0;
        while (tmp.contains(".")
                && !tmp.substring(tmp.indexOf('.')).equals(".id")) {
            aliasCtr++;
            tmp = tmp.substring(tmp.indexOf('.') + 1);
        }

        if (aliasCtr > 0) {
            String aliasField = '.' + tmp;
            String alias = "";
            String table = "";
            String field = "";
            for (int i = 0; i < aliasCtr; i++) {
                if (field.length() > 0) {
                    field = fieldName.substring(
                            field.length() + 1,
                            field.length()
                                    + fieldName.substring(field.length() + 1)
                                    .indexOf('.') + 1);
                    table = alias + '.' + field;
                } else {
                    table = field = fieldName.substring(0,
                            fieldName.indexOf('.'));
                }
                if (alias.length() > 0) {
                    alias += "_alias_" + field;
                } else {
                    alias = "alias_" + field;
                }

                if (!this.aliases.contains(alias)) {
                    criteria.createAlias(table, alias, JoinType.INNER_JOIN);
                    this.aliases.add(alias);
                }
            }
            fieldName = alias + aliasField;
        }

        return fieldName;

    }

    private Criterion[] getCriterion(Map<String, FilterMeta> filters,
                                     Criteria criteria) throws HibernateException,
            PersistenceBeanException {
        List<Criterion> cr = new ArrayList<Criterion>();
        for (Entry<String, FilterMeta> set : filters.entrySet()) {
            if (set.getValue().getFilterValue() == null) {
                continue;
            }
            String fieldName = getCorrectFieldName(set.getKey(), criteria);
            if (fieldName.endsWith(".id")) {
                cr.add(Restrictions.eq(fieldName,
                        Long.parseLong(set.getValue().getFilterValue().toString())));
            } else {
                boolean criterionAdded = false;
                try {
                    Class<?> fieldType = clazz.getDeclaredField(fieldName)
                            .getType();
                    if (fieldType.equals(Long.class)
                            || fieldType.equals(long.class)) {
                        criterionAdded = true;
                        cr.add(Restrictions.eq(fieldName,
                                Long.parseLong(set.getValue().getFilterValue().toString())));
                    } else if (fieldType.equals(Integer.class)
                            || fieldType.equals(int.class)) {
                        criterionAdded = true;
                        cr.add(Restrictions.eq(fieldName,
                                Integer.parseInt(set.getValue().getFilterValue().toString())));
                    } else if (fieldType.equals(Boolean.class)
                            || fieldType.equals(boolean.class)) {
                        criterionAdded = true;
                        cr.add(Restrictions.eq(fieldName, (set.getValue().getFilterValue()
                                .equals("true")) ? Boolean.TRUE : Boolean.FALSE));
                    } else if (fieldType.equals(Date.class)) {
                        criterionAdded = true;
                        cr.add(Restrictions.sqlRestriction("to_char("
                                + (ValidationHelper.isNullOrEmpty(clazz
                                .getDeclaredField(fieldName)
                                .getAnnotation(Column.class).name()) ? fieldName
                                : clazz.getDeclaredField(fieldName)
                                .getAnnotation(Column.class)
                                .name()) + ", '"
                                + DateTimeHelper.getDatePattern() + "') like '"
                                + set.getValue().getFilterValue() + "%'"));
                    } else if (fieldType.isEnum()) {
                        boolean checkInstanceOf = false;
                        for (Object o : fieldType.getEnumConstants()) {
                            if (!checkInstanceOf) {
                                checkInstanceOf = o instanceof DbEnum;
                                if (!checkInstanceOf) {
                                    break;
                                }
                            }
                            if (((DbEnum) o).getRealName().equals(
                                    set.getValue().getFilterValue())) {
                                cr.add(Restrictions.eq(fieldName,
                                        ((DbEnum) o).getRealObject()));
                                criterionAdded = true;
                                break;
                            }
                        }
                    }
                } catch (SecurityException e) {
                    LogHelper.log(log, e);
                } catch (NumberFormatException e) {
                    cr.add(Restrictions.eq("id", -1l));
                } catch (NoSuchFieldException e) {
                }
                if (!criterionAdded) {
                    if ("surname".equals(fieldName) || "name".equals(fieldName)) {
                        cr.add(Restrictions.like(fieldName,
                                set.getValue().getFilterValue().toString(), MatchMode.START)
                                .ignoreCase());
                    } else {
                        cr.add(Restrictions.like(fieldName,
                                set.getValue().getFilterValue().toString(), MatchMode.ANYWHERE)
                                .ignoreCase());
                    }
                }
            }
        }
        return cr.toArray(new Criterion[0]);
    }

    @SuppressWarnings("unused")
    private String getFilterMethodName(String fieldName) {
        return "get" + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1) + "CriterionFilter";
    }

    @Override
    public boolean isRowAvailable() {
        if (list == null)
            return false;

        int rowIndex = getRowIndex();
        if (rowIndex >= 0 && rowIndex < list.size())
            return true;
        else
            return false;
    }

    @Override
    public int getRowCount() {
        return this.totalNumRows;
    }

    @Override
    public T getRowData() {
        if (list == null) {
            return null;
        } else if (!isRowAvailable()) {
            throw new IllegalArgumentException();
        } else {
            int dataIndex = getRowIndex();

            return list.get(dataIndex);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getRowData(String rowKey) {
        try {
            return (T) DaoManager.get(clazz, Long.parseLong(rowKey));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    @Override
    public int getRowIndex() {
        return (rowIndex % this.getPageSize());
    }

    @Override
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    @Override
    public List<T> getWrappedData() {
        return list;
    }

    @Override
    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public void setWrappedData(Object list) {
        this.list = (List) list;
    }
}
