package it.nexera.ris.persistence.beans.entities;

import java.util.ArrayList;
import java.util.List;

public class DatabaseMaterializedView {
    private static final String UNIQUE_TSRM = "create materialized view unique_tsrm " +
            "refresh on demand " +
            "START WITH (SYSDATE) NEXT (SYSDATE + 1) " +
            "as select unique tsrm from historical_report where tsrm is not null and id_in_old_db is null";

    private static final String UNIQUE_REFERRING_DOCTOR = "create materialized view unique_referring_doctor " +
            "refresh on demand " +
            "START WITH (SYSDATE) NEXT (SYSDATE + 1) " +
            "as select unique referring_doctor from historical_report where referring_doctor is not null";

    private static final String MOST_USED_EXAMS = "create materialized view most_used_exams " +
            "refresh on demand " +
            "START WITH (SYSDATE) NEXT (SYSDATE + 1) " +
            "as SELECT EX.ID, EX.CODE, COUNT(ERI.ID) as count " +
            "FROM DIC_RADIOLOGY_EXAM EX " +
            "LEFT OUTER JOIN RAD_EXAM_REQUEST_ITEM ERI ON eri.rad_exam_id = EX.ID " +
            "GROUP BY EX.ID, EX.CODE";

    private static final String HISTORICAL_REPORT_MV = "create materialized view historical_report_mview\n" +
            "    refresh on demand START WITH (SYSDATE) NEXT (TRUNC(SYSDATE) + 1 + 2 / 24) as\n" +
            "select base.*, his.REPORT_RESULT, his.REPORT_RESULT_WITHOUT_TAGS\n" +
            "from (SELECT his.id,\n" +
            "             his.create_date,\n" +
            "             his.create_user_id,\n" +
            "             his.update_date,\n" +
            "             his.update_user_id,\n" +
            "             his.version,\n" +
            "             his.accept_date,\n" +
            "             his.access_number,\n" +
            "             his.hospital_code,\n" +
            "             his.id_in_old_db,\n" +
            "             his.latest_action_date,\n" +
            "             his.operator,\n" +
            "             his.pat_birth_date,\n" +
            "             his.pat_fiscal_code,\n" +
            "             his.pat_name,\n" +
            "             his.pat_surname,\n" +
            "             his.perform_date,\n" +
            "             his.referring_doctor,\n" +
            "             his.report_date,\n" +
            "             his.request_date,\n" +
            "             his.requesting_doctor,\n" +
            "             his.reserve_date,\n" +
            "             his.sector_id,\n" +
            "             his.tsrm,\n" +
            "             his.wl_registration_state,\n" +
            "             his.exam_type_id,\n" +
            "             his.file_entity_id,\n" +
            "             his.rad_ex_request_id,\n" +
            "             (case\n" +
            "                  when sec.description is not null then sec.description\n" +
            "                  when sec_h.description is not null then sec_h.description\n" +
            "                 end)                                            as sec_description,\n" +
            "             listagg(dre.id, ';') within group (order by dre.id) as rad_ex_descr\n" +
            "      FROM historical_report his\n" +
            "               left outer join HISTORY_RADIOLOGY_EXAM hre on hre.HISTORICAL_REPORT_ID = his.ID\n" +
            "               left outer join DIC_RADIOLOGY_EXAM dre on dre.ID = hre.RADIOLOGY_EXAME_ID\n" +
            "               left outer JOIN radiology_exam_request rad ON his.rad_ex_request_id = rad.id\n" +
            "               left outer JOIN dic_sector sec ON rad.sector_id = sec.id\n" +
            "               left outer JOIN dic_sector sec_h ON his.sector_id = sec_h.id\n" +
            "      group by his.id, his.create_date, his.create_user_id, his.update_date, his.update_user_id, his.version,\n" +
            "               his.accept_date, his.access_number, his.hospital_code, his.id_in_old_db, his.latest_action_date,\n" +
            "               his.operator, his.pat_birth_date, his.pat_fiscal_code, his.pat_name, his.pat_surname, his.perform_date,\n" +
            "               his.referring_doctor, his.report_date, his.request_date, his.requesting_doctor, his.reserve_date,\n" +
            "               his.sector_id, his.tsrm, his.wl_registration_state, his.exam_type_id, his.file_entity_id,\n" +
            "               his.rad_ex_request_id, (case\n" +
            "                                           when sec.description is not null then sec.description\n" +
            "                                           when sec_h.description is not null then sec_h.description\n" +
            "          end)) base\n" +
            "         inner join HISTORICAL_REPORT his on his.ID = base.id";

    public static List<String> getMViews() {
        List<String> mViews = new ArrayList<>();
        mViews.add(UNIQUE_TSRM);
        mViews.add(UNIQUE_REFERRING_DOCTOR);
        mViews.add(MOST_USED_EXAMS);
        mViews.add(HISTORICAL_REPORT_MV);

        return mViews;
    }
}
