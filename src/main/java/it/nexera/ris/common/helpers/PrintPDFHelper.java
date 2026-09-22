package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.helpers.logic.GeneralFunctionsHelper;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import java.util.List;

public class PrintPDFHelper extends BaseHelper {
    public static void chooseTemplate(List<SelectItem> templates,
                                      RadiologyExamRequest radiologyExamRequest, UserWrapper userWrapper) {
        try {
            if (!ValidationHelper.isNullOrEmpty(templates)) {
                if (templates.size() == 1) {
                    printReport(radiologyExamRequest, Long.valueOf(templates
                            .get(0).getValue().toString()), userWrapper, true);
                } else {
                    PFRequestContextHelper.executeJS("PF('templates').show();");
                }
            } else {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        ResourcesHelper.getValidation("warning"),
                        ResourcesHelper.getValidation("noDocumentTemplates"));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void printReport(RadiologyExamRequest radiologyExamRequest,
                                   Long id, UserWrapper userWrapper, boolean withRedirect) {
        GeneralFunctionsHelper.showReport(radiologyExamRequest, id,
                userWrapper, null, null, false, null);

        if (withRedirect) {
            RedirectHelper.goTo(PageTypes.WAITINGLIST_REGISTRATION_LIST);
        }
    }
}
