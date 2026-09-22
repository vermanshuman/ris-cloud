package it.nexera.ris.web.beans.wrappers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.persistence.materialized.HistoricalReportMV;
import org.hibernate.HibernateException;

import java.util.List;

public class HistoricalReportWrapper implements IEntity {

    private HistoricalReportMV historicalReport;

    private List<String>  radiologyExamsDescr;

    public String getRadExamsLine() {
        StringBuilder sb = new StringBuilder();
        if (!ValidationHelper.isNullOrEmpty(getRadiologyExamsDescr())) {
            for (String radiologyExamDes : getRadiologyExamsDescr()) {
                sb.append(radiologyExamDes);
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public HistoricalReportMV getHistoricalReport() {
        return historicalReport;
    }

    public void setHistoricalReport(HistoricalReportMV historicalReport) {
        this.historicalReport = historicalReport;
    }

    public List<String> getRadiologyExamsDescr() {
        return radiologyExamsDescr;
    }

    public void setRadiologyExamsDescr(List<String> radiologyExamsDescr) {
        this.radiologyExamsDescr = radiologyExamsDescr;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public boolean isCustomId() {
        return false;
    }

    @Override
    public boolean getDeletable() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        return false;
    }

    @Override
    public boolean getEditable() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        return false;
    }
}
