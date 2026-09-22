package it.nexera.ris.web.beans;

import it.nexera.ris.common.enums.PriorityAlertTypes;
import it.nexera.ris.common.enums.UserPreferenceType;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.UserPreference;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Urgency;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named("urgencyMessageBean")
@SessionScoped
public class UrgencyMessageBean extends PageBean implements Serializable {

    private static final long serialVersionUID = 2864993889826903463L;

    private static final String S_CODE = "s";

    private static final String P_CODE = "p";

    private String data;

    private String patientFullName;

    private String itemsDescription;

    private String asapSectorDescription;

    private Urgency urgency;

    @Override
    protected void onConstruct() {
    }

    public void processMessage() {
        try {
            String dataMsg = getData();
            if (!ValidationHelper.isNullOrEmpty(dataMsg)) {
                UserPreference preference = DaoManager.get(UserPreference.class, new Criterion[]{
                        Restrictions.eq("user.id", getCurrentUser().getId()),
                        Restrictions.eq("type", UserPreferenceType.PRIORITY_ALERT)
                });
                if (ValidationHelper.isNullOrEmpty(preference)
                        || ValidationHelper.isNullOrEmpty(preference.getPriorityAlertType())
                        || preference.getPriorityAlertType().equals(PriorityAlertTypes.NOBODY)) {
                    return;
                }
                dataMsg = dataMsg.replaceAll("receiveUrgentRequest\\?", "");
                String[] parameters = dataMsg.split("&");
                String sectorId = getParameterValue(parameters, "sectorId");
                if (getCurrentUser().getSectors().contains(Long.parseLong(sectorId))) {
                    String urgencyId = getParameterValue(parameters, "urgencyId");
                    Urgency urgency = DaoManager.get(Urgency.class, Long.parseLong(urgencyId));
                    if ((preference.getPriorityAlertType().equals(PriorityAlertTypes.RED) && S_CODE.equalsIgnoreCase(urgency.getCode()))
                            || (preference.getPriorityAlertType().equals(PriorityAlertTypes.YELLOW)
                            && (S_CODE.equalsIgnoreCase(urgency.getCode()) || P_CODE.equalsIgnoreCase(urgency.getCode())))) {
                        setPatientFullName(getParameterValue(parameters, "patientFullName"));
                        setItemsDescription(getParameterValue(parameters, "itemsDescription"));
                        setAsapSectorDescription(getParameterValue(parameters, "asapSectorDescription"));
                        setUrgency(urgency);
                        executeJS("PF('receiveUrgentRequestDlgWv').show();");
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private String getParameterValue(String[] parameters, String parameterName) {
        for (String parameter : parameters) {
            if (parameter.startsWith(parameterName)) {
                return parameter.split("=")[1];
            }
        }

        return "";
    }

    public String getPatientFullName() {
        return patientFullName;
    }

    public void setPatientFullName(String patientFullName) {
        this.patientFullName = patientFullName;
    }

    public String getItemsDescription() {
        return itemsDescription;
    }

    public void setItemsDescription(String itemsDescription) {
        this.itemsDescription = itemsDescription;
    }

    public String getAsapSectorDescription() {
        return asapSectorDescription;
    }

    public void setAsapSectorDescription(String asapSectorDescription) {
        this.asapSectorDescription = asapSectorDescription;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
