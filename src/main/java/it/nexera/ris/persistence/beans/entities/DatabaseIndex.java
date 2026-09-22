package it.nexera.ris.persistence.beans.entities;

import java.util.ArrayList;
import java.util.List;

public class DatabaseIndex {

    public static final String ALLOW_WAIT_FOR_DDL = "ALTER SESSION SET DDL_LOCK_TIMEOUT = 900";

    private static final String HREPORT_MVIEW_WLR_STATE = "CREATE BITMAP INDEX hreport_mview_wlr_state ON" +
            " historical_report_mview ( wl_registration_state )" +
            " COMPUTE STATISTICS";

    private static final String HREPORT_MVIEW_PAT_SURNAME = "CREATE INDEX hreport_mview_pat_surname ON" +
            " historical_report_mview ( lower(pat_surname) )" +
            " COMPUTE STATISTICS";

    private static final String HREPORT_MVIEW_PAT_NAME = "CREATE INDEX hreport_mview_pat_name ON" +
            " historical_report_mview ( lower(pat_name) )" +
            " COMPUTE STATISTICS";

    private static final String HREPORT_MVIEW_LAD = "CREATE INDEX hreport_mview_lad ON" +
            " historical_report_mview ( latest_action_date desc, 0 )" +
            " COMPUTE STATISTICS";

    private static final String HREPORT_MVIEW_PERFORM_DATE = "CREATE INDEX hreport_mview_perform_date ON" +
            " historical_report_mview ( perform_date, 0 )" +
            " COMPUTE STATISTICS";

    private static final String HREPORT_MVIEW_REPORT_RESULT = "CREATE INDEX hreport_mview_report_result ON" +
            " historical_report_mview(report_result)" +
            " INDEXTYPE IS CTXSYS.CONTEXT PARAMETERS" +
            " ('FILTER CTXSYS.NULL_FILTER SECTION GROUP CTXSYS.HTML_SECTION_GROUP SYNC (ON COMMIT)')";

    public static List<String> getIndexes() {
        List<String> indexes = new ArrayList<>();
        indexes.add(HREPORT_MVIEW_WLR_STATE);
        indexes.add(HREPORT_MVIEW_PAT_SURNAME);
        indexes.add(HREPORT_MVIEW_PAT_NAME);
        indexes.add(HREPORT_MVIEW_LAD);
        indexes.add(HREPORT_MVIEW_PERFORM_DATE);
        indexes.add(HREPORT_MVIEW_REPORT_RESULT);

        return indexes;
    }
}
