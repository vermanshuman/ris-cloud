package it.nexera.ris.common.helpers.logic;

import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.web.common.AtmosphereUtil;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class UrgencyHelper extends BaseHelper {

    public static void processIds(String[] idsArr) {
        String patientFullName = "";
        String asapSectorDescription = "";
        StringBuilder itemsDescription = new StringBuilder();
        Long sectorId = 0L;
        Long urgencyId = 0L;
        PersistenceSession persistenceSession = new PersistenceSession();
        try {
            Session session = persistenceSession.getSession();
            for (int i = 0; i < idsArr.length; ++i) {
                String id = idsArr[i];
                if (ValidationHelper.isNullOrEmptyMultiple(patientFullName, asapSectorDescription)) {
                    RadiologyExamRequest examRequest = ConnectionManager.get(RadiologyExamRequest.class, Long.parseLong(id), session);
                    patientFullName = examRequest.getPatientFullname();
                    asapSectorDescription = examRequest.getAsapSectorDescription();
                    String description = examRequest.getItemsDescription();
                    itemsDescription.append(description == null ? "" : description.replaceAll(", ", " <br/> "));
                    sectorId = examRequest.getSector().getId();
                    urgencyId = examRequest.getUrgency().getId();
                } else {
                    String itemDescription = ConnectionManager.getField(RadiologyExamRequest.class, "itemsDescription", new Criterion[]{
                            Restrictions.eq("id", Long.parseLong(id))
                    }, null, session);
                    itemsDescription.append(itemDescription.replaceAll(", ", " <br/> "));
                }
                if ((i + 1) < idsArr.length) {
                    itemsDescription.append(" <br/> ");
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            persistenceSession.closeSession();
        }
        final StringBuilder strb = new StringBuilder();
        strb.append("receiveUrgentRequest?patientFullName=")
                .append(patientFullName)
                .append("&asapSectorDescription=")
                .append(asapSectorDescription)
                .append("&itemsDescription=")
                .append(itemsDescription)
                .append("&sectorId=")
                .append(sectorId)
                .append("&urgencyId=")
                .append(urgencyId);
        AtmosphereUtil.broadcastTo("/notify", strb.toString());
    }
}
