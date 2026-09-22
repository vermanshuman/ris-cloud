package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestShort;
import it.nexera.ris.persistence.beans.entities.domain.RequestNote;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestItemWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class AggregationHelper {
    private static final Long COUNT_HL7_FIELDS = 1l;

    public static List<RadiologyExamRequest> getRadiologyExamRequestsForAggregation(
            RadiologyExamRequest selectedRequest, Long currentUserId)
            throws HibernateException, IllegalAccessException,
            PersistenceBeanException {
        List<RadiologyExamRequest> radExReqForAggregation = null;
        if (!ValidationHelper.isNullOrEmpty(selectedRequest)
                && !ValidationHelper.isNullOrEmpty(selectedRequest
                .getWaitingListRegistrationState())
                && !ValidationHelper
                .isNullOrEmpty(selectedRequest.getPatient())
                && !ValidationHelper.isNullOrEmpty(selectedRequest
                .getAsapSectorCode())
                && !ValidationHelper.isNullOrEmpty(selectedRequest
                .getExamType())
                && !ValidationHelper.isNullOrEmpty(selectedRequest.getSector())) {
            WaitingListRegistrationStates state = null;
            if (selectedRequest.getWaitingListRegistrationState().equals(WaitingListRegistrationStates.DRAFT)
                    || selectedRequest.getWaitingListRegistrationState().equals(WaitingListRegistrationStates.IN_READING)) {
                state = WaitingListRegistrationStates.PERFORMED;
            } else {
                state = selectedRequest.getWaitingListRegistrationState();
            }
            List<RadiologyExamRequest> localRequests = DaoManager.load(
                    RadiologyExamRequest.class,
                    new Criterion[]{
                            Restrictions.ne("id", selectedRequest.getId()),
                            Restrictions.eq("waitingListRegistrationState",
                                    state),
                            Restrictions.eq("patient",
                                    selectedRequest.getPatient()),
                            Restrictions.eq("examType",
                                    selectedRequest.getExamType()),
                            Restrictions.eq("asapSectorCode",
                                    selectedRequest.getAsapSectorCode()),
                            Restrictions.eq("sector",
                                    selectedRequest.getSector())
                    });
            if (!ValidationHelper.isNullOrEmpty(localRequests)) {
                switch (selectedRequest.getWaitingListRegistrationState()) {
                    case DRAFT:
                    case IN_READING:
                    case PERFORMED:
                        radExReqForAggregation = filterInLockCase(
                                localRequests, currentUserId);
                        break;

                    default:
                        radExReqForAggregation = localRequests;
                        break;
                }
            }
        }
        return radExReqForAggregation;
    }

    public static List<RadiologyExamRequestShort> getRadiologyExamRequestsForAggregationWL(
            RadiologyExamRequestShort selectedRequest, Long currentUserId)
            throws HibernateException, IllegalAccessException,
            PersistenceBeanException {
        List<RadiologyExamRequestShort> radExReqForAggregation = null;
        if (!ValidationHelper.isNullOrEmpty(selectedRequest)
                && !ValidationHelper.isNullOrEmpty(selectedRequest
                .getWaitingListRegistrationState())
                && !ValidationHelper
                .isNullOrEmpty(selectedRequest.getPatient())
                && !ValidationHelper.isNullOrEmpty(selectedRequest
                .getAsapSectorCode())
                && !ValidationHelper.isNullOrEmpty(selectedRequest
                .getExamType())
                && !ValidationHelper.isNullOrEmpty(selectedRequest.getSector())) {
            WaitingListRegistrationStates state = null;
            if (selectedRequest.getWaitingListRegistrationState().equals(WaitingListRegistrationStates.DRAFT)
                    || selectedRequest.getWaitingListRegistrationState().equals(WaitingListRegistrationStates.IN_READING)) {
                state = WaitingListRegistrationStates.PERFORMED;
            } else {
                state = selectedRequest.getWaitingListRegistrationState();
            }
            List<RadiologyExamRequestShort> localRequests = DaoManager.load(
                    RadiologyExamRequestShort.class,
                    new Criterion[]{
                            Restrictions.ne("id", selectedRequest.getId()),
                            Restrictions.eq("waitingListRegistrationState",
                                    state),
                            Restrictions.eq("patient",
                                    selectedRequest.getPatient()),
                            Restrictions.eq("examType",
                                    selectedRequest.getExamType()),
                            Restrictions.eq("asapSectorCode",
                                    selectedRequest.getAsapSectorCode()),
                            Restrictions.eq("sector",
                                    selectedRequest.getSector())
                    });
            if (!ValidationHelper.isNullOrEmpty(localRequests)) {
                switch (selectedRequest.getWaitingListRegistrationState()) {
                    case DRAFT:
                    case IN_READING:
                    case PERFORMED:
                        radExReqForAggregation = filterInLockCaseWL(
                                localRequests, currentUserId);
                        break;

                    default:
                        radExReqForAggregation = localRequests;
                        break;
                }
            }
        }
        return radExReqForAggregation;
    }

    private static List<RadiologyExamRequest> filterInLockCase(
            List<RadiologyExamRequest> inputRequests, Long currentUserId) {
        List<RadiologyExamRequest> requests = new ArrayList<RadiologyExamRequest>();

        if (!ValidationHelper.isNullOrEmpty(inputRequests)) {
            for (RadiologyExamRequest request : inputRequests) {
                if (request.getUserClosingReportId() == null) {
                    requests.add(request);
                } else {
                    if (request.getUserClosingReportId().equals(currentUserId)) {
                        requests.add(request);
                    }
                }
            }
        }

        return requests;
    }

    private static List<RadiologyExamRequestShort> filterInLockCaseWL(
            List<RadiologyExamRequestShort> inputRequests, Long currentUserId) {
        List<RadiologyExamRequestShort> requests = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(inputRequests)) {
            for (RadiologyExamRequestShort request : inputRequests) {
                if (request.getUserClosingReportId() == null) {
                    requests.add(request);
                } else {
                    if (request.getUserClosingReportId().equals(currentUserId)) {
                        requests.add(request);
                    }
                }
            }
        }

        return requests;
    }

    public static void aggregateActionAcceta(RadiologyExamRequest request,
                                             RadExamRequestWrapper radExamWrapper,
                                             List<RadExamRequestWrapper> radExamRequestWrappersForAggregation)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(request)
                && !ValidationHelper.isNullOrEmpty(radExamWrapper)
                && !ValidationHelper
                .isNullOrEmpty(radExamRequestWrappersForAggregation)) {
            List<Long> radiologyExamIds = new ArrayList<Long>();
            List<Long> radiologyExamRequestItemIds = new ArrayList<Long>();
            List<Long> radiologyExamRequestIds = new ArrayList<Long>();
            List<Long> itemsToNonDelete = new ArrayList<Long>();
            List<Long> requsetsToNonDelete = new ArrayList<Long>();

            for (RadExamRequestItemWrapper reriw : radExamWrapper
                    .getRadExamRequestItemWrappers()) {
                radiologyExamIds.add(reriw.getRadiologyExam().getId());
                radiologyExamRequestItemIds.add(reriw.getId());
            }

            radiologyExamRequestIds.add(request.getId());

            for (RadExamRequestWrapper rerw : radExamRequestWrappersForAggregation) {
                if (!ValidationHelper.isNullOrEmpty(rerw
                        .getRadExamRequestItemWrappers())) {
                    radiologyExamRequestIds.add(rerw.getId());

                    for (RadExamRequestItemWrapper reriw : rerw
                            .getRadExamRequestItemWrappers()) {
                        if (!ValidationHelper.isNullOrEmpty(reriw)
                                && !ValidationHelper.isNullOrEmpty(reriw
                                .getRadiologyExam())) {
                            if (reriw.getSelected().booleanValue()) {
                                if ((reriw.getRadiologyExam().getMultiple()
                                        .booleanValue() || !radiologyExamIds
                                        .contains(reriw.getRadiologyExam()
                                                .getId()))) {
                                    radiologyExamIds.add(reriw
                                            .getRadiologyExam().getId());
                                    radiologyExamRequestItemIds.add(reriw
                                            .getId());
                                }
                            } else {
                                itemsToNonDelete.add(reriw.getId());
                                requsetsToNonDelete.add(rerw.getId());
                            }
                        }
                    }
                }
            }

            List<RadiologyExamRequest> allNeededRadiologyExamRequests = DaoManager
                    .load(RadiologyExamRequest.class, new Criterion[]{
                            Restrictions.in("id", radiologyExamRequestIds)
                    });
            List<RadiologyExamRequestItem> allNeededRadiologyExamRequestItems = DaoManager
                    .load(RadiologyExamRequestItem.class, new Criterion[]{
                            Restrictions.in("radiologyExamRequest",
                                    allNeededRadiologyExamRequests)
                    });

            if (!ValidationHelper
                    .isNullOrEmpty(allNeededRadiologyExamRequestItems)) {
                List<RadiologyExamRequestItem> selectedItems = new ArrayList<RadiologyExamRequestItem>();//FIXME: DELETE AFTER CREATE TRIGGER
                for (RadiologyExamRequestItem reri : allNeededRadiologyExamRequestItems) {
                    if (radiologyExamRequestItemIds.contains(reri.getId())) {
                        RadiologyExamRequestItem baseReri = request
                                .getRadiologyExamRequestItems().get(0);

                        reri.setRadiologyExamRequest(request);
                        reri.setEventCalendarRegistration(null);
                        reri.setEventCalendarRegistrations(null);

                        reri.setWaitingListRegistrationState(request
                                .getWaitingListRegistrationState());

                        reri.setAccessNumber(baseReri.getAccessNumber());
                        reri.setAccessNumberCode(baseReri.getAccessNumberCode());
                        reri.setAccessNumberId(baseReri.getAccessNumberId());
                        reri.setAccessNumberYear(baseReri.getAccessNumberYear());

                        reri.setOperator(baseReri.getOperator());
                        reri.setAcceptDate(baseReri.getAcceptDate());
                        reri.setCardNumber(baseReri.getCardNumber());

                        reri.getRadiologyExamRequest().setPerformDate(
                                baseReri.getPerformDate());

                        if (WaitingListRegistrationStates.DRAFT
                                .equals(reri.getRadiologyExamRequest()
                                        .getWaitingListRegistrationState())
                                || WaitingListRegistrationStates.REPORTED
                                .equals(reri.getRadiologyExamRequest()
                                        .getWaitingListRegistrationState())) {
                            reri.getRadiologyExamRequest()
                                    .setLatestActionPerformDate(
                                            baseReri.getPerformDate());
                        }

                        reri.setPerformDate(baseReri.getPerformDate());
                        reri.setTrsm(baseReri.getTrsm());

                        DaoManager.save(reri);
                        selectedItems.add(reri);
                    } else {
                        if (!itemsToNonDelete.contains(reri.getId())) {
                            DaoManager.remove(reri);
                        }
                    }
                }
                request.udateItemsDescription(selectedItems);//FIXME: DELETE AFTER CREATE TRIGGER
                DaoManager.save(request);//FIXME: DELETE AFTER CREATE TRIGGER
            }

            updateRequests(radExamRequestWrappersForAggregation, itemsToNonDelete);//FIXME: DELETE AFTER CREATE TRIGGER

            if (!ValidationHelper.isNullOrEmpty(allNeededRadiologyExamRequests)) {
                for (RadiologyExamRequest rer : allNeededRadiologyExamRequests) {
                    if (!rer.getId().equals(request.getId())
                            && !requsetsToNonDelete.contains(rer.getId())) {
                        HistoricalReport historicalReport = DaoManager.get(
                                HistoricalReport.class, new Criterion[]{
                                        Restrictions
                                                .eq("radiologyExamRequest", rer)
                                });
                        if (historicalReport != null) {
                            DaoManager.remove(historicalReport);
                        }
                        for (RequestNote note : rer.getRequestNotes()) {
                            note.setRadiologyExamRequest(request);
                            DaoManager.save(note);
                        }

                        if (rer.getHl7FieldsFromAsap() != null) {
                            Long count = DaoManager.getCount(
                                    RadiologyExamRequest.class, "id",
                                    new Criterion[]{
                                            Restrictions.eq(
                                                    "hl7FieldsFromAsap.id",
                                                    rer.getHl7FieldsFromAsap()
                                                            .getId())
                                    });

                            if (COUNT_HL7_FIELDS.equals(count)) {
                                DaoManager.remove(rer.getHl7FieldsFromAsap());
                            }
                        }

                        DaoManager.remove(rer);
                    }
                }
            }
        }
    }

    private static void updateRequests(
            List<RadExamRequestWrapper> radExamRequestWrappersForAggregation,
            List<Long> itemIds)
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        for (RadExamRequestWrapper wrapper : radExamRequestWrappersForAggregation) {
            RadiologyExamRequest request = DaoManager
                    .get(RadiologyExamRequest.class, wrapper.getId());

            if (request != null) {
                List<RadiologyExamRequestItem> items = DaoManager
                        .load(RadiologyExamRequestItem.class, Restrictions.eq(
                                "radiologyExamRequest.id", request.getId()));

                request.udateItemsDescription(checkItems(items, itemIds));
                DaoManager.save(request);
            }
        }
    }

    private static List<RadiologyExamRequestItem> checkItems(
            List<RadiologyExamRequestItem> items, List<Long> itemIds) {
        List<RadiologyExamRequestItem> newItems = new ArrayList<RadiologyExamRequestItem>();
        if (items != null && itemIds != null) {
            for (RadiologyExamRequestItem item : items) {
                if (itemIds.contains(item.getId())) {
                    newItems.add(item);
                }
            }
        }

        return newItems;
    }

    public static List<Long> getRequestItemsIdsForDocumentGeneration(
            RadExamRequestWrapper selectedRequestWrapper,
            List<RadExamRequestItemWrapper> requestsItemsWrappersForAggregation) {
        List<Long> radiologyExamRequestItemIds = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(selectedRequestWrapper)
                && !ValidationHelper.isNullOrEmpty(selectedRequestWrapper.getRadExamRequestItemWrappers())) {

            radiologyExamRequestItemIds.addAll(getSeletedItemsIdsFromRequestWrapper(selectedRequestWrapper));

            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestItemIds)
                    && !ValidationHelper.isNullOrEmpty(requestsItemsWrappersForAggregation)) {
                for (RadExamRequestItemWrapper reriw : requestsItemsWrappersForAggregation) {
                    if (reriw.getSelected()) {
                        radiologyExamRequestItemIds.add(reriw.getId());
                    }
                }
            }
        }

        return radiologyExamRequestItemIds;
    }

    public static List<Long> getSeletedItemsIdsFromRequestWrapper(
            RadExamRequestWrapper radExamRequestWrapper) {
        List<Long> selectedItemsIds = null;
        if (!ValidationHelper.isNullOrEmpty(radExamRequestWrapper
                .getRadExamRequestItemWrappers())) {
            selectedItemsIds = new ArrayList<Long>();
            for (RadExamRequestItemWrapper reriw : radExamRequestWrapper
                    .getRadExamRequestItemWrappers()) {
                if (reriw.getSelected().booleanValue()) {
                    selectedItemsIds.add(reriw.getId());
                }
            }
        }
        return selectedItemsIds;
    }

    public static Pair<RadExamRequestWrapper, List<RadExamRequestWrapper>> getPairForAggregation(
            RadiologyExamRequest selectedRequest, Long currentUserId)
            throws HibernateException, IllegalAccessException,
            PersistenceBeanException, InstantiationException {
        Pair<RadExamRequestWrapper, List<RadExamRequestWrapper>> pair = null;
        if (!ValidationHelper.isNullOrEmpty(selectedRequest)) {
            List<RadiologyExamRequest> radExReqForAggregation = AggregationHelper
                    .getRadiologyExamRequestsForAggregation(selectedRequest,
                            currentUserId);
            if (!ValidationHelper.isNullOrEmpty((radExReqForAggregation))) {
                List<RadExamRequestWrapper> requestToAggregationWrappers = new ArrayList<RadExamRequestWrapper>();
                RadExamRequestWrapper selectedWrapper = new RadExamRequestWrapper();

                for (RadiologyExamRequest rer : radExReqForAggregation) {
                    requestToAggregationWrappers.add(rer
                            .getRadExamRequestWrapperFromRequest());
                }
                selectedWrapper = selectedRequest
                        .getRadExamRequestWrapperFromRequest();

                pair = new Pair<RadExamRequestWrapper, List<RadExamRequestWrapper>>(
                        selectedWrapper, requestToAggregationWrappers);
            }
        }

        return pair;
    }

    public static Pair<RadExamRequestWrapper, List<RadExamRequestWrapper>> getPairForAggregationWL(
            Long selectedRequestId, Long currentUserId)
            throws HibernateException, IllegalAccessException,
            PersistenceBeanException, InstantiationException {
        Pair<RadExamRequestWrapper, List<RadExamRequestWrapper>> pair = null;
        if (!ValidationHelper.isNullOrEmpty(selectedRequestId)) {
            RadiologyExamRequestShort selectedRequest = DaoManager.get(RadiologyExamRequestShort.class, selectedRequestId);
            List<RadiologyExamRequestShort> radExReqForAggregation = AggregationHelper
                    .getRadiologyExamRequestsForAggregationWL(selectedRequest,
                            currentUserId);
            if (!ValidationHelper.isNullOrEmpty((radExReqForAggregation))) {
                List<RadExamRequestWrapper> requestToAggregationWrappers = new ArrayList<>();

                for (RadiologyExamRequestShort rer : radExReqForAggregation) {
                    requestToAggregationWrappers.add(rer
                            .getRadExamRequestWrapperFromRequest());
                }

                pair = new Pair<>(selectedRequest.getRadExamRequestWrapperFromRequest(), requestToAggregationWrappers);
            }
        }

        return pair;
    }

    public static List<Long> getRadExamItemIdsWithSameFileEntity(
            RadiologyExamRequest request) throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        List<Long> itemIds = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(request)
                && !ValidationHelper.isNullOrEmpty(request
                .getRadiologyExamRequestItems())) {
            request.getRadiologyExamRequestItems().get(0).loadFileEntity();
            if (!ValidationHelper.isNullOrEmpty(request
                    .getRadiologyExamRequestItems().get(0).getFileEntity())) {
                List<RadiologyExamRequestItem> radItemsWithSameFileEntity = DaoManager
                        .load(RadiologyExamRequestItem.class,
                                new Criterion[]{
                                        Restrictions.eq("fileEntity", request
                                                .getRadiologyExamRequestItems()
                                                .get(0).getFileEntity())
                                });
                if (!ValidationHelper.isNullOrEmpty(radItemsWithSameFileEntity)) {
                    for (RadiologyExamRequestItem reri : radItemsWithSameFileEntity) {
                        itemIds.add(reri.getId());
                    }
                }
            } else {
                for (RadiologyExamRequestItem item : request
                        .getRadiologyExamRequestItems()) {
                    itemIds.add(item.getId());
                }
            }
        }

        return itemIds;
    }

    public static List<Long> getRadExamItemIdsWithSameFileEntityDraft(
            RadiologyExamRequest request) throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        List<Long> itemIds = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(request)
                && !ValidationHelper.isNullOrEmpty(request
                .getRadiologyExamRequestItems())) {
            request.getRadiologyExamRequestItems().get(0).loadFileEntity();
            if (!ValidationHelper.isNullOrEmpty(request
                    .getRadiologyExamRequestItems().get(0).getFileEntity())) {
                List<RadiologyExamRequestItem> radItemsWithSameFileEntity = DaoManager
                        .load(RadiologyExamRequestItem.class,
                                new Criterion[]{
                                        Restrictions.or(
                                                Restrictions.eq("fileEntity", request
                                                        .getRadiologyExamRequestItems()
                                                        .get(0).getFileEntity()),
                                                Restrictions.eq("aggregationRequestId", request
                                                        .getId()))
                                });
                if (!ValidationHelper.isNullOrEmpty(radItemsWithSameFileEntity)) {
                    for (RadiologyExamRequestItem reri : radItemsWithSameFileEntity) {
                        itemIds.add(reri.getId());
                    }
                }
            } else {
                List<RadiologyExamRequestItem> radItemsWithSameFileEntity = DaoManager
                        .load(RadiologyExamRequestItem.class,
                                new Criterion[]{
                                        Restrictions.eq("aggregationRequestId", request
                                                .getId())
                                });
                radItemsWithSameFileEntity.addAll(request
                        .getRadiologyExamRequestItems());
                for (RadiologyExamRequestItem item : radItemsWithSameFileEntity) {
                    itemIds.add(item.getId());
                }
            }
        }

        return itemIds;
    }

    public static List<Long> getRadExamItemIdsWithSignFileEntity(
            RadiologyExamRequest request) throws HibernateException,
            IllegalAccessException, PersistenceBeanException {
        List<Long> itemIds = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(request)
                && !ValidationHelper.isNullOrEmpty(request
                .getRadiologyExamRequestItems())) {
            request.getRadiologyExamRequestItems().get(0).loadFileEntity();
            if (!ValidationHelper.isNullOrEmpty(request
                    .getRadiologyExamRequestItems().get(0).getSignFileEntity())) {
                List<RadiologyExamRequestItem> radItemsWithSameFileEntity = DaoManager
                        .load(RadiologyExamRequestItem.class,
                                new Criterion[]{
                                        Restrictions.eq("fileEntity", request
                                                .getRadiologyExamRequestItems()
                                                .get(0).getSignFileEntity())
                                });
                if (!ValidationHelper.isNullOrEmpty(radItemsWithSameFileEntity)) {
                    for (RadiologyExamRequestItem reri : radItemsWithSameFileEntity) {
                        itemIds.add(reri.getId());
                    }
                }
            } else {
                for (RadiologyExamRequestItem item : request
                        .getRadiologyExamRequestItems()) {
                    itemIds.add(item.getId());
                }
            }
        }

        return itemIds;
    }

    public static List<RadExamRequestItemWrapper> getItemsWrappersFromRequestsWrappers(
            List<RadExamRequestWrapper> requestsWrappers) {
        List<RadExamRequestItemWrapper> result = null;

        if (!ValidationHelper.isNullOrEmpty(requestsWrappers)) {
            result = new ArrayList<RadExamRequestItemWrapper>();

            for (RadExamRequestWrapper requestWrapper : requestsWrappers) {
                if (requestWrapper != null) {
                    result.addAll(requestWrapper
                            .getRadExamRequestItemWrappers());
                }
            }
        }

        return result;
    }
}
