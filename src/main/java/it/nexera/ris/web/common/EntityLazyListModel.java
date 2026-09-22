package it.nexera.ris.web.common;

import it.nexera.ris.common.enums.DbEnum;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.view.BaseWorklistView;
import it.nexera.ris.persistence.view.ShortWorklistView;
import it.nexera.ris.persistence.view.WorklistView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.QueryTimeoutException;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import javax.faces.context.FacesContext;
import javax.persistence.Column;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

public class EntityLazyListModel<T extends IEntity> extends LazyDataModel<T> {
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

    public static final String SORTFIELDS_IN_SESSION = "sortFieldsInSession";

    public static final String SORTFIELDS_WAITING_PAGE_IN_SESSION = "SORTFIELD_WAITING_PAGE_IN_SESSION";

    private final boolean showOrderDateForWorkList;

    private static final String LATEST_ACTION_PERFORM_DATE_WORKLIST_FIELD = "latestActionPerformDate";

    private static final String RESERVE_DATE_WORKLIST_FIELD = "reserveDate";

    public EntityLazyListModel(Class<? extends IEntity> clazz, Order[] orders) {
        this(clazz, null, orders);
    }

    public EntityLazyListModel(Class<? extends IEntity> clazz,
                               Criterion[] restrictions, Order[] orders) {
        this(clazz, restrictions, orders, null);
    }

    public EntityLazyListModel(Class<? extends IEntity> clazz,
                               Criterion[] restrictions, Order[] orders,
                               CriteriaAlias[] criteriaAliases) {
        super();
        this.clazz = clazz;
        this.orders = orders;
        this.restrictions = restrictions;
        this.criteriaAliases = criteriaAliases;
        this.showOrderDateForWorkList = UserHolder.getInstance().getCurrentUser().getShowOrderDate();
    }

    @Override
    public String getRowKey(T entity) {
        return String.valueOf(entity.getId());
    }

    @Override
    public int count(Map<String, FilterMeta> map) {
        Long rowCount = 0l;

        try {
            this.aliases = new ArrayList<String>();
            Criteria countCriteria = DaoManager.getSession().createCriteria(
                    clazz, "thisClass");
            countCriteria.setTimeout(DaoManager.getQueryTimeout());

            if (WorklistView.class.getName().equals(clazz.getName())) {
                countCriteria.setProjection(Projections.countDistinct("id"));
            } else {
                countCriteria.setProjection(Projections.rowCount());
            }

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

            try {
                rowCount = (Long) countCriteria.uniqueResult();
            } catch (QueryTimeoutException e) {
                DaoManager.showQueryTimeoutMessage();
            }

            this.setRowCount(rowCount == null ? 0 : rowCount.intValue());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return rowCount == null ? 0 : rowCount.intValue();

    }

    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();

            if (PageTypes.WORKLIST.equals(PageTypes.getPageTypeByPath(request.getServletPath()))) {
                changeSortFieldForWorkList(sortBy);
                SessionHelper.put(sortBy.values(), SORTFIELDS_IN_SESSION);

            } else if (PageTypes.WAITINGLIST_REGISTRATION_LIST.equals(
                    PageTypes.getPageTypeByPath(request.getServletPath()))) {

                SessionHelper.put(sortBy.values(), SORTFIELDS_WAITING_PAGE_IN_SESSION);
            }
        }

        try {
            this.list = loadList(first, pageSize, sortBy, filterBy);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        this.calculated = true;
        return list;
    }

    private void changeSortFieldForWorkList(Map<String, SortMeta> sortBy) {
        Optional<SortMeta> sortMeta = sortBy.entrySet().stream()
                .filter(entry -> entry.getKey().equals(LATEST_ACTION_PERFORM_DATE_WORKLIST_FIELD))
                .map(Map.Entry::getValue)
                .findAny();
        if (sortMeta.isPresent() && this.showOrderDateForWorkList
                && sortMeta.get().getOrder().equals(SortOrder.DESCENDING)) {

            sortBy.remove(LATEST_ACTION_PERFORM_DATE_WORKLIST_FIELD);
            sortBy.put(RESERVE_DATE_WORKLIST_FIELD, SortMeta.builder()
                    .field(RESERVE_DATE_WORKLIST_FIELD)
                    .order(SortOrder.DESCENDING).build());
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> loadList(Integer first, Integer pageSize, Map<String, SortMeta> sortBy,
                             Map<String, FilterMeta> filters)
            throws PersistenceBeanException, IllegalAccessException {
        Criteria criteria = DaoManager.getSession().createCriteria(clazz, "thisClass");
        criteria.setTimeout(DaoManager.getQueryTimeout());

        if (WorklistView.class.getName().equals(clazz.getName())
                || ShortWorklistView.class.getName().equals(clazz.getName())) {
            BaseWorklistView obj = new BaseWorklistView();
            ProjectionList proList = Projections.projectionList();
            List<String> fields = new ArrayList<String>();

            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(javax.persistence.Column.class)
                        || field.isAnnotationPresent(
                        javax.persistence.JoinColumn.class)) {
                    proList.add(Projections.property(field.getName()));
                    fields.add(field.getName());
                }
            }

            proList.add(Projections.property("id"));
            fields.add("id");

            criteria.setProjection(Projections.distinct(proList));

            String[] fieldsStr = new String[fields.size()];
            fieldsStr = fields.toArray(fieldsStr);

            criteria.setResultTransformer(new AliasToBeanResultTransformer(BaseWorklistView.class, fieldsStr));
        }

        if (pageSize != null) {
            criteria.setMaxResults(pageSize.intValue());
        }
        if (first != null) {
            criteria.setFirstResult(first.intValue());
        }
        if (criteriaAliases != null) {
            for (CriteriaAlias ca : criteriaAliases) {
                criteria.createAlias(ca.getTable(), ca.getAliasName(),
                        ca.getJoinType());
            }
        }

        this.aliases = new ArrayList<String>();

        for (Criterion criterion : getCriterion(filters, criteria)) {
            criteria.add(criterion);
        }

        if (restrictions != null) {
            for (Criterion criterion : restrictions) {
                criteria.add(criterion);
            }
        }

        if (sortBy.isEmpty()) {
            for (Order order : this.orders) {
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
        try {
            return criteria.list();
        } catch (QueryTimeoutException e) {
            DaoManager.showQueryTimeoutMessage();
            return new ArrayList<>();
        }
    }

    /*
     * (non-Javadoc)
     *
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

        if (oldFieldName.contains("[0]")) {
            fieldName = oldFieldName.replace("[0]", "");
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

    private Criterion[] getCriterion(Map<String, FilterMeta> filters, Criteria criteria) throws HibernateException,
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
            } else if (isDbField(fieldName)) {
                cr.add(Restrictions.sqlRestriction(" CAST( this_." + fieldName + " AS varchar(255)) LIKE '%" + set
                        .getValue().toString() + "%'"));
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
                            if (((DbEnum) o).getRealName().toLowerCase().contains(
                                    set.getValue().getFilterValue().toString().toLowerCase())) {
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

    private boolean isDbField(String name) {
        return name.equals("port_host_pacs") || name.equals("dicom_port");
    }

    public boolean isRowAvailable() {
        if (list == null) {
            return false;
        }

        int rowIndex = getRowIndex();
        if (rowIndex >= 0 && rowIndex < list.size()) {
            return true;
        } else {
            return false;
        }
    }

    public int getRowCount() {
        if (!this.calculated) {
            try {
                if (restrictions == null) {
                    this.totalNumRows = DaoManager.getCount(this.clazz, "id")
                            .intValue();
                } else {
                    if (criteriaAliases != null && criteriaAliases.length != 0) {
                        this.totalNumRows = DaoManager.getCount(this.clazz,
                                "id", criteriaAliases, restrictions).intValue();
                    } else {
                        this.totalNumRows = DaoManager.getCount(this.clazz,
                                "id", "thisClass", restrictions).intValue();
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return this.totalNumRows;
    }

    public T getRowData() {
        if (ValidationHelper.isNullOrEmpty(list)) {
            return null;
        } else if (!isRowAvailable()) {
            throw new IllegalArgumentException();
        } else {
            int dataIndex = getRowIndex();

            if (dataIndex >= 0) {
                return list.get(dataIndex);
            } else {
                return null;
            }
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

    public int getRowIndex() {
        if (getPageSize() != 0) {
            return (rowIndex % getPageSize());
        } else {
            return 0;
        }
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public List<T> getWrappedData() {
        return list;
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    public void setWrappedData(Object list) {
        this.list = (List) list;
    }

}