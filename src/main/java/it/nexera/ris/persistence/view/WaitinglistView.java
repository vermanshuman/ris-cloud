package it.nexera.ris.persistence.view;

import it.nexera.ris.common.annotations.View;
import it.nexera.ris.common.helpers.BaseHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "waitinglist_view")
@View(sql = WaitinglistView.QUERY)
public class WaitinglistView extends BaseWaitinglistView {

    private static final long serialVersionUID = -6151440877318379666L;

    protected static transient final Logger log = LogManager.getLogger(BaseHelper.class);

    protected static final String QUERY = "CREATE OR REPLACE VIEW waitinglist_view "
            + "AS SELECT     "
            + "p.birth_date BIRTH_DATE,  "
            + "p.name NAME,  "
            + "p.SURNAME SURNAME,  "
            + "p.FISCAL_CODE FISCAL_CODE,  "

            + "request.ID ID,  "
            + "request.CREATE_DATE CREATE_DATE, "
            + "request.CREATE_USER_ID CREATE_USER_ID, "
            + "request.UPDATE_DATE UPDATE_DATE, "
            + "request.UPDATE_USER_ID UPDATE_USER_ID, "
            + "request.wl_registration_state wl_registration_state,  "
            + "request.exam_type_id exam_type_id,  "
            + "request.urgency_id urgency_id,  "
            + "request.asap_sector_desctiption asap_sector_desctiption, "
            + "request.asap_activity_line asap_activity_line, "
            + "request.disabled_request disabled_request, "
            + "request.request_date request_date, "
            + "request.reserve_date reserve_date, "
            + "request.forwarded forwarded, "
            + "request.items_descr items_descr, "
            + "request.hl7_fields_form_asap_id hl7_fields_form_asap_id, "
            + "request.transport_type transport_type, "

            + "dic_ex_type.description exam_type_description "

            + "FROM PATIENT p LEFT JOIN RADIOLOGY_EXAM_REQUEST request "
            + "ON p.ID = request.patient_id "

            + "LEFT JOIN dic_exam_type dic_ex_type "
            + "ON dic_ex_type.ID = request.exam_type_id "

            + "WHERE request.id IS NOT NULL";


}
