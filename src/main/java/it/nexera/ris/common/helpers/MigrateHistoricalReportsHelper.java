package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.persistence.beans.entities.domain.history.OldHistoricalReport;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.Query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MigrateHistoricalReportsHelper {
    public static final Logger log = LogManager.getLogger(MigrateHistoricalReportsHelper.class);

    @SuppressWarnings("unchecked")
    public boolean migrateFromOldDB() {
        try {
            log.info("___________Start migration___________");
            Query query = DaoManager.getSession().createQuery(
                    "from OldHistoricalReport");

            List<OldHistoricalReport> oldHistoricalReports = query.list();

            log.info("Load old completed");
            log.info("count of loaded " + oldHistoricalReports.size()
                    + " oldHistoricalReports");

            query = DaoManager.getSession().createSQLQuery(
                    "select id_in_old_db from historical_report");

            List<BigDecimal> requestsIdsDecimal = query.list();

            log.info("count of id_in_old_db from historical_report = "
                    + requestsIdsDecimal.size());

            List<Long> historyIds = new ArrayList<Long>();
            if (!ValidationHelper.isNullOrEmpty(requestsIdsDecimal)) {
                for (BigDecimal bd : requestsIdsDecimal) {
                    if (bd != null) {
                        historyIds.add(Long.valueOf(bd.longValue()));
                    }
                }
            }

            List<HistoricalReport> historicalReportsToSave = new ArrayList<HistoricalReport>();

            log.info("___________Start main action___________");

            if (!ValidationHelper.isNullOrEmpty(oldHistoricalReports)) {
                int i = 1;
                int j = 0;
                for (Object object : oldHistoricalReports) {
                    OldHistoricalReport oldHistoricalReport = (OldHistoricalReport) object;

                    HistoricalReport historicalReport = oldHistoricalReport
                            .getNewHistoricalReport(historyIds);

                    if (historicalReport != null
                            && historicalReport.getIdInOldDb() != null) {
                        historicalReportsToSave.add(historicalReport);

                        if (j == 99) {
                            saveToTransactionFromOld(historicalReportsToSave, i);
                            historicalReportsToSave.clear();
                            ++i;

                            j = 0;
                        } else {
                            ++j;
                        }
                    }
                }

                if (!ValidationHelper.isNullOrEmpty(historicalReportsToSave)) {
                    saveToTransactionFromOld(historicalReportsToSave, -1);
                }
            }
            log.info("___________Migration end___________");
            return true;
        } catch (Exception e) {
            LogHelper.log(log, e);
            return false;
        }
    }

    private void saveToTransactionFromOld(
            List<HistoricalReport> historicalReports, int id) {
        if (!ValidationHelper.isNullOrEmpty(historicalReports)) {
            try {
                final List<HistoricalReport> fHistoricalReports = historicalReports;
                final int fId = id;
                TransactionExecuter.execute(new Action() {
                    @Override
                    public void execute() throws Exception {
                        saveToTransactionFromOldAction(fHistoricalReports, fId);
                    }

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onException(Exception e) throws Exception {
                    }
                });

                clearSession();
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    private void saveToTransactionFromOldAction(
            List<HistoricalReport> historicalReports, int id)
            throws HibernateException, PersistenceBeanException {

        for (HistoricalReport historicalReport : historicalReports) {
            if (historicalReport != null) {
                if (historicalReport.getFileEntity() != null) {
                    DaoManager.save(historicalReport.getFileEntity());
                }
                DaoManager.save(historicalReport);
            }
        }
        if (id % 10 == 0 || id == -1) {
            log.info("Block " + id + " completed");
        }
    }

    private void clearSession() {
        try {
            DaoManager.getSession().flush();
            DaoManager.getSession().clear();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }
}
