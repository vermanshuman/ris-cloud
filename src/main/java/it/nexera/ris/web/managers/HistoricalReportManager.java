package it.nexera.ris.web.managers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import it.nexera.ris.persistence.materialized.HistoricalReportMV;
import it.nexera.ris.web.beans.wrappers.HistoricalReportWrapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HistoricalReportManager<T extends HistoricalReportWrapper> implements Serializable {

    private static final long serialVersionUID = -3927043674114021612L;

    protected transient final Logger log = LogManager.getLogger(getClass());

    private List<HistoricalReportMV> reportList;

    public HistoricalReportManager(List<HistoricalReportMV> reportList) {
        this.reportList = reportList;
    }

    private Map<Long, String> getMapIdDescription(List<Object[]> list) {
        Map<Long, String> map = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            for (Object[] ob : list) {
                Long key = ((BigDecimal) ob[0]).longValue();
                String value = (String) ob[1];
                map.put(key, value);
            }
        }
        return map;
    }


    private Map<Long, List<String>> getMapByRadDic(List<Object[]> list) {
        Map<Long, List<String>> map = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            for (Object[] ob : list) {
                Long key = ((BigDecimal) ob[0]).longValue();
                String value = (String) ob[1];
                List<String> listValue;
                if (map.containsKey(key)) {
                    listValue = map.get(key);
                    listValue.add(value);
                    map.put(key, listValue);
                } else {
                    listValue = new ArrayList<>();
                    listValue.add(value);
                    map.put(key, listValue);
                }
            }
        }
        return map;
    }

    private void associateDescriptionToRadExam(List<T> resultList) throws PersistenceBeanException, IllegalAccessException {
        associateDescriptionToRadExam(resultList, null);
    }
    private void associateDescriptionToRadExam(List<T> resultList, Session session) throws PersistenceBeanException, IllegalAccessException {
        if (getReportList() != null) {
            List<RadiologyExam> radiologyExams;
            if(session == null){
                radiologyExams = DaoManager.load(RadiologyExam.class, new Criterion[]{
                        Restrictions.in("id", getRadExamListIds())
                });
            }else {
                radiologyExams = ConnectionManager.load(RadiologyExam.class, new Criterion[]{
                        Restrictions.in("id", getRadExamListIds())
                }, session);
            }
            for (HistoricalReportMV historicalReportMV : getReportList()) {
                HistoricalReportWrapper wrapper =
                        getElementWrapperById(historicalReportMV.getId(), (List<HistoricalReportWrapper>) resultList);
                List<String> radDesc = new ArrayList<>();
                if (wrapper != null) {
                    Set<Long> idsAsLong = historicalReportMV.getRadiologyExamDescriptionIdsAsLong();
                    for (RadiologyExam radiologyExam : radiologyExams) {
                        if (idsAsLong.contains(radiologyExam.getId())) {
                            radDesc.add(radiologyExam.getDescription());
                        }
                    }
                    wrapper.setRadiologyExamsDescr(radDesc);
                }
            }
        }
    }

    private HistoricalReportWrapper getElementWrapperById(Long id, List<HistoricalReportWrapper> list) {
        for (HistoricalReportWrapper element : list) {
            if (id.equals(element.getHistoricalReport().getId())) {
                return element;
            }
        }
        return null;
    }

    public List<T> getResultList() throws PersistenceBeanException, IllegalAccessException {
        return getResultList(null);
    }
    public List<T> getResultList(Session session) throws PersistenceBeanException, IllegalAccessException {
        List<T> resultList = new ArrayList<>();
        if (getReportList() == null || getReportList().isEmpty()) {
            return null;
        }
        for (HistoricalReportMV request : getReportList()) {
            HistoricalReportWrapper wrapper = new HistoricalReportWrapper();
            wrapper.setHistoricalReport(request);
            resultList.add((T) wrapper);
        }
        associateDescriptionToRadExam(resultList, session);
        return resultList;
    }

    private Set<Long> getRadExamListIds() {
        if (getReportList() == null || getReportList().isEmpty()) {
            return new HashSet<>();
        }
        Set<Long> result = new HashSet<>();
        for (HistoricalReportMV historicalReportMV : getReportList()) {
            result.addAll(historicalReportMV.getRadiologyExamDescriptionIdsAsLong());
        }
        return result;
    }

    public List<HistoricalReportMV> getReportList() {
        return reportList;
    }

    public void setReportList(List<HistoricalReportMV> reportList) {
        this.reportList = reportList;
    }
}
