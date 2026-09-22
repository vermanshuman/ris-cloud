package it.nexera.ris.common.helpers.logic;

import it.nexera.ris.common.enums.DoseClass;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestBase;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestItemBase;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RadiologyExam;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenerationTagsHelper extends BaseHelper {
    private static final String ID_IN_SESSION_FOR_TAGS = "RadiologyExamsIdsForPrint";

    private static final String ID_IN_SESSION_FOR_PRINT = "RadiologyExamRequestsItemsIdsForTads";

    public static <T extends RadiologyExamRequestItemBase> String generatePatientTable(String name, String surname,
                                                                                       Date birthDate, String asapSectorDescription,
                                                                                       String examsDescription,
                                                                                       List<T> radiologyExamRequestItems) {
        StringBuilder sb = new StringBuilder(
                "<table width=\"100%\" border=\"0\">");
        try {
            sb.append("<thead>");
            sb.append("<tr>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("userLastname"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("userFirstname"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper
                    .getString("workListCancelPatientBirthDate"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("workListCancelPatientSector"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("workListCancelPatientStanza"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper
                    .getString("workListCancelPatientAccessNumber"));
            sb.append("</span></th>");
            sb.append("</tr>");
            sb.append("</thead>");
            sb.append("<tbody>");
            sb.append("<tr>");
            sb.append("<td style='text-align:center;'>");
            sb.append(surname);
            sb.append("</td>");
            sb.append("<td style='text-align:center;'>");
            sb.append(name);
            sb.append("</td>");
            sb.append("<td style='text-align:center;'>");
            sb.append(DateTimeHelper.toString(birthDate));
            sb.append("</td>");
            sb.append("<td style='text-align:center;'>");
            if (!ValidationHelper.isNullOrEmpty(asapSectorDescription)) {
                sb.append(asapSectorDescription);
            }
            sb.append("</td>");
            sb.append("<td style='text-align:center;'>");
            if (!ValidationHelper.isNullOrEmpty(examsDescription)) {
                sb.append(examsDescription);
            }
            sb.append("</td>");
            sb.append("<td style='text-align:center;'>");
            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestItems)
                    && !ValidationHelper
                    .isNullOrEmpty(radiologyExamRequestItems.get(0))
                    && !ValidationHelper
                    .isNullOrEmpty(radiologyExamRequestItems.get(0)
                            .getAccessNumber())) {
                sb.append(radiologyExamRequestItems.get(0).getAccessNumber());
            }
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</tbody>");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        sb.append("</table>");
        return sb.toString();
    }

    public static <T extends RadiologyExamRequestItemBase> String generatetCancelExamItemsTable(
            List<T> radiologyExamRequestItems, RadiologyExamRequestBase radiologyExamRequest) {
        StringBuilder sb = new StringBuilder(
                "<table width=\"100%\" border=\"0\">");
        try {
            sb.append("<thead>");
            sb.append("<tr>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("workListCancelExamId"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper
                    .getString("workListCancelExamDescription"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("workListCancelExamDate"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("workListCancelExamOra"));
            sb.append("</span></th>");
            sb.append("</tr>");
            sb.append("</thead>");
            sb.append("<tbody>");

            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestItems)) {
                for (T examItem : radiologyExamRequestItems) {
                    sb.append("<tr>");
                    sb.append("<td style='text-align:center;'>");
                    sb.append(examItem.getId());
                    sb.append("</td>");
                    sb.append("<td style='text-align:center;'>");
                    if (!ValidationHelper.isNullOrEmpty(examItem
                            .getRadiologyExam())
                            && !ValidationHelper.isNullOrEmpty(examItem
                            .getRadiologyExam().getDescription())) {
                        sb.append(examItem.getRadiologyExam().getDescription());
                    }
                    sb.append("</td>");
                    if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest)
                            && !ValidationHelper.isNullOrEmpty(radiologyExamRequest
                            .getLatestActionDate())) {
                        sb.append("<td style='text-align:center;'>");
                        sb.append(DateTimeHelper.toString(radiologyExamRequest
                                .getLatestActionDate()));
                        sb.append("</td>");
                        sb.append("<td style='text-align:center;'>");
                        sb.append(DateTimeHelper.toStringTime(radiologyExamRequest
                                .getLatestActionDate()));
                        sb.append("</td>");
                    }
                    sb.append("</tr>");
                }
            }
            sb.append("</tbody>");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        sb.append("</table>");
        return sb.toString();
    }

    public static <T extends RadiologyExamRequestItemBase> String generateExamsTable(
            List<T> radiologyExamRequestItems) {

        StringBuilder sb = new StringBuilder(
                "<table width=\"100%\" style=\"border-collapse: collapse;\" border=\"1px solid black\" bordercolor=\"black\">");
        try {
            sb.append("<thead>");
            sb.append("<tr>");
            sb.append("<th style='text-align:left;'><span>");
            sb.append(ResourcesHelper.getString("workListExamDescriptions"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper
                    .getString("workListDoseClass"));
            sb.append("</span></th>");
            sb.append("</tr>");
            sb.append("</thead>");
            sb.append("<tbody>");

            if (!ValidationHelper.isNullOrEmpty(SessionHelper.getIds(ID_IN_SESSION_FOR_TAGS))) {
                List<Long> ids = SessionHelper.getIds(ID_IN_SESSION_FOR_TAGS);
                List<RadiologyExam> radiologyExams = DaoManager
                        .load(RadiologyExam.class,
                                new Criterion[]{
                                        Restrictions.in("id", ids)
                                });
                if (!ValidationHelper.isNullOrEmpty(radiologyExams)) {
                    for (RadiologyExam aggregateExam : radiologyExams) {
                        sb.append("<tr>");
                        sb.append("<td style='text-align:left;'>");
                        if (!ValidationHelper.isNullOrEmpty(aggregateExam.getDescription())) {
                            sb.append(aggregateExam.getDescription());
                        }
                        sb.append("</td>");
                        sb.append("<td style='text-align:center;'>");
                        if (!ValidationHelper.isNullOrEmpty(aggregateExam.getDoseClass())) {
                            sb.append(aggregateExam.getDoseClass().toString());
                        }
                        sb.append("</td>");
                        sb.append("</tr>");
                    }
                }
            }

            sb.append("</tbody>");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        sb.append("</table>");
        return sb.toString();
    }

    public static <T extends RadiologyExamRequestItemBase> String generateExamsAggregatedTable() {

        StringBuilder sb = new StringBuilder(
                "<table width=\"100%\" style=\"border-collapse: collapse;\" border=\"1px solid black\" bordercolor=\"black\">");
        try {
            sb.append("<thead>");
            sb.append("<tr>");
            sb.append("<th style='text-align:left;'><span>");
            sb.append(ResourcesHelper.getString("workListExamDescriptions"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper
                    .getString("workListDoseClass"));
            sb.append("</span></th>");
            sb.append("</tr>");
            sb.append("</thead>");
            sb.append("<tbody>");

            if (!ValidationHelper.isNullOrEmpty(SessionHelper.getIds(ID_IN_SESSION_FOR_PRINT))) {
                List<Long> ids = SessionHelper.getIds(ID_IN_SESSION_FOR_PRINT);
                List<RadiologyExamRequestItem> radiologyExamItems = DaoManager
                        .load(RadiologyExamRequestItem.class,
                                new Criterion[]{
                                        Restrictions.in("id", ids)
                                });
                if (!ValidationHelper.isNullOrEmpty(radiologyExamItems)) {
                    Set<Long> radiologyExamRequestIds = new HashSet<>();

                    for (RadiologyExamRequestItem radiologyExamItem : radiologyExamItems) {
                        Long radiologyExamRequestId = radiologyExamItem.getRadiologyExamRequestId();
                        radiologyExamRequestIds.add(radiologyExamRequestId);
                    }
                    if (radiologyExamRequestIds.size() == 1) {
                        for (RadiologyExamRequestItem aggregateExam : radiologyExamItems) {
                            sb.append("<tr>");
                            sb.append("<td style='text-align:left;'>");
                            if (!ValidationHelper.isNullOrEmpty(aggregateExam.getRadiologyExam().getDescription())) {
                                sb.append(aggregateExam.getRadiologyExam().getDescription());
                            }
                            sb.append("</td>");
                            sb.append("<td style='text-align:center;'>");
                            if (!ValidationHelper.isNullOrEmpty(aggregateExam.getRadiologyExam().getDoseClass())) {
                                sb.append(aggregateExam.getRadiologyExam().getDoseClass().toString());
                            }
                            sb.append("</td>");
                            sb.append("</tr>");
                        }
                    } else {
                        Map<String, Set<RadiologyExam>> itemsByRegionalCode = new HashMap<>();
                        for (RadiologyExamRequestItem radiologyExamItem : radiologyExamItems) {
                            if (!itemsByRegionalCode.containsKey(radiologyExamItem.getRadiologyExam().getRegionalCode())) {
                                itemsByRegionalCode.put(radiologyExamItem.getRadiologyExam().getRegionalCode(), new HashSet<RadiologyExam>());
                            }
                            itemsByRegionalCode.get(radiologyExamItem.getRadiologyExam().getRegionalCode()).add(radiologyExamItem.getRadiologyExam());
                        }
                        for (Map.Entry<String, Set<RadiologyExam>> entry : itemsByRegionalCode.entrySet()) {
                            boolean doseAdded = false;
                            for (RadiologyExam radiologyExamItem : entry.getValue()) {
                                sb.append("<tr>");
                                sb.append("<td style='text-align:left;'>");
                                if (!ValidationHelper.isNullOrEmpty(radiologyExamItem.getDescription())) {
                                    sb.append(radiologyExamItem.getDescription());
                                }
                                sb.append("</td>");
                                if (!doseAdded) {
                                    doseAdded = true;
                                    sb.append("<td style='text-align:center;' rowspan='");
                                    sb.append(entry.getValue().size());
                                    sb.append("'>");
                                    if (!ValidationHelper.isNullOrEmpty(radiologyExamItem.getDoseClass())) {
                                        sb.append(radiologyExamItem.getDoseClass().toString());
                                    }
                                    sb.append("</td>");
                                }
                                sb.append("</tr>");
                            }
                        }
                    }
                }
            }

            sb.append("</tbody>");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        sb.append("</table>");
        return sb.toString();
    }

    public static <T extends RadiologyExamRequestItemBase> String generatetPerformanceList(
            List<T> radiologyExamRequestItems, RadiologyExamRequestBase radiologyExamRequest) {
        StringBuilder sb = new StringBuilder(
                "<table width=\"100%\" border=\"0\">");

        try {
            sb.append("<thead>");
            sb.append("<tr>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("performanceListDate"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("performanceListCode"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper.getString("performanceListDescription"));
            sb.append("</span></th>");
            sb.append("</tr>");
            sb.append("</thead>");
            sb.append("<tbody>");

            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestItems)) {
                for (T examItem : radiologyExamRequestItems) {
                    sb.append("<tr>");
                    sb.append("<td style='text-align:center;'>");
                    sb.append(DateTimeHelper.toFormatedString(
                            examItem.getReserveDate(),
                            DateTimeHelper.getDatePatternWithMinutes()));
                    sb.append("</td>");
                    if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest)) {
                        sb.append("<td style='text-align:center;'>");
                        sb.append(ValidationHelper.isNullOrEmpty(examItem
                                .getRadiologyExam().getCode()) ? "" : examItem
                                .getRadiologyExam().getCode());
                        sb.append("</td>");
                        sb.append("<td style='text-align:center;'>");
                        sb.append(ValidationHelper.isNullOrEmpty(examItem
                                .getRadiologyExam().getDescription()) ? ""
                                : examItem.getRadiologyExam().getDescription());
                        sb.append("</td>");
                    }
                    sb.append("</tr>");
                }
            }

            sb.append("</tbody>");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        sb.append("</table>");
        return sb.toString();
    }

    public static <T extends RadiologyExamRequestItemBase> String generateDeliveryDoseTable() {
        StringBuilder sb = new StringBuilder(
                "<table width=\"100%\" class=\"deliveryDose\" style=\"border-collapse: collapse;\" border=\"1px solid black\" bordercolor=\"black\">");
        try {
            sb.append("<thead>");
            sb.append("<tr>");
            sb.append("<th style='text-align:left;'><span>");
            sb.append(ResourcesHelper.getString("workListAccessionNumber"));
            sb.append("</span></th>");
            sb.append("<th><span>");
            sb.append(ResourcesHelper
                    .getString("workListDoseAdministered"));
            sb.append("</span></th>");
            sb.append("</tr>");
            sb.append("</thead>");
            sb.append("<tbody>");

            if (!ValidationHelper.isNullOrEmpty(SessionHelper.getIds(ID_IN_SESSION_FOR_PRINT))) {
                List<Long> ids = SessionHelper.getIds(ID_IN_SESSION_FOR_PRINT);
                List<RadiologyExamRequestItem> radiologyExamItems = DaoManager
                        .load(RadiologyExamRequestItem.class,
                                new Criterion[]{
                                        Restrictions.in("id", ids)
                                });
                if (!ValidationHelper.isNullOrEmpty(radiologyExamItems)) {
                    List<RadiologyExamRequestItem> uniqueAccessNumbers = radiologyExamItems.stream()
                            .filter(r -> StringUtils.isNotBlank(r.getAccessNumber()))
                            .collect(Collectors.toMap(
                                    RadiologyExamRequestItem::getAccessNumber,
                                    Function.identity(),
                                    (existing, replacement) -> existing
                            ))
                            .values()
                            .stream()
                            .collect(Collectors.toList());

                    for (RadiologyExamRequestItem radiologyExamItem : uniqueAccessNumbers) {
                        sb.append("<tr>");
                        sb.append("<td style='text-align:left;'>");
                        sb.append(radiologyExamItem.getAccessNumber());
                        sb.append("</td>");
                        sb.append("<td style='text-align:center;'>");
                        if (!ValidationHelper.isNullOrEmpty(radiologyExamItem.getDeliverydose())) {
                            sb.append(radiologyExamItem.getDeliverydose());
                        }
                        sb.append("</td>");
                        sb.append("</tr>");
                    }
                }
            }

            sb.append("</tbody>");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        sb.append("</table>");
        return sb.toString();
    }
}
