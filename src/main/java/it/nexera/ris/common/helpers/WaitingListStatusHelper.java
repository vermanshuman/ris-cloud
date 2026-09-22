package it.nexera.ris.common.helpers;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.message.ACK;
import ca.uhn.hl7v2.model.v26.message.ORM_O01;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.Hl7RequestSendingStatus;
import it.nexera.ris.common.enums.UserActivityLogStates;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.validation.AssignValidationException;
import it.nexera.ris.common.executors.ThreadExecutor;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.logic.hl7.BaseHl7MessageHelper;
import it.nexera.ris.common.helpers.logic.hl7.ORMHelper;
import it.nexera.ris.common.security.crypto.MD5;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.SinglePersistenceSessionAction;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestItemBase;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistration;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistrationSlot;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestItemWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import it.nexera.ris.web.beans.wrappers.logic.WaitingListRegistrationStateWrapper;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import java.io.Serializable;
import java.util.*;

public class WaitingListStatusHelper extends BaseHelper implements Serializable {
    private static final long serialVersionUID = 2846905912100407662L;

    private static final Logger hl7ErrorLog = CustomLibLoggerFactory.getHl7ErrorLogger();

    private static final Logger hl7InfoLog = CustomLibLoggerFactory.getHl7InfoLogger();

    private static final Logger activityInfoLog = CustomLibLoggerFactory.getActivityInfoLoger();

    private String hl7HostValue = ApplicationSettingsHolder
            .getInstance()
            .getByKey(
                    ApplicationSettingsKeys.HL7_SEND_IP)
            .getValue();

    private String hl7PortValue = ApplicationSettingsHolder
            .getInstance()
            .getByKey(
                    ApplicationSettingsKeys.HL7_SEND_PORT)
            .getValue();

    private String hl7SendRetryTimeValue = ApplicationSettingsHolder
            .getInstance()
            .getByKey(
                    ApplicationSettingsKeys.HL7_ORM_SEND_RETRY_TIME)
            .getValue();

    private RadiologyExamRequest asapSplittedRequest;

    public List<Long> getSelectedIds(RadiologyExamRequest request) {
        List<Long> radExItemIds = new ArrayList<Long>();

        for (RadiologyExamRequestItemBase wlrf : request
                .getRadiologyExamRequestItems()) {
            if (Boolean.TRUE.equals(wlrf.getSelected())) {
                radExItemIds.add(wlrf.getId());
            }
        }
        return radExItemIds;
    }

    public void toCancelState(RadiologyExamRequest request,
                              List<Long> itemsIds, String comment, FileEntity fileEntity)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, AssignValidationException {
        generalLogic(request, itemsIds, WaitingListRegistrationStates.DELETED,
                null, comment, null, fileEntity, Boolean.FALSE, null, null);

        LogHelper.log(hl7InfoLog,
                "\nsend Orm Msg for the 'cancel' action on request with id = "
                        + request.getId());
        sendOrmMsg(request);
    }

    public void toRequiredState(RadiologyExamRequest request,
                                List<Long> radExItemIds) throws PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            HibernateException, InstantiationException,
            AssignValidationException {
        generalLogic(request, radExItemIds,
                WaitingListRegistrationStates.REQUIRED, null, null, null, null,
                Boolean.FALSE, null, null);

        LogHelper.log(hl7InfoLog,
                "\nsend Orm Msg for the 'request' action on request with id = "
                        + request.getId());
        sendOrmMsg(request);
    }

    public void toAnnuledState(RadiologyExamRequest request,
                               List<Long> radExItemIds, String comment, FileEntity fileEntity)
            throws PersistenceBeanException, CloneNotSupportedException,
            IllegalAccessException, HibernateException, InstantiationException,
            AssignValidationException {
        generalLogic(request, radExItemIds,
                WaitingListRegistrationStates.ANNULLED, null, comment, null,
                fileEntity, Boolean.FALSE, null, null);

        LogHelper.log(hl7InfoLog,
                "\nsend Orm Msg for the 'annulate' action on request with id = "
                        + request.getId());
        sendOrmMsg(request);
    }

    public void toReservedState(RadiologyExamRequest request, List<Long> radExItemIds, Boolean reserveToday)
            throws PersistenceBeanException, InstantiationException, AssignValidationException, CloneNotSupportedException, IllegalAccessException {
        final RadiologyExamRequest toSend = prepareRadiologyRequestToSend(request, radExItemIds, WaitingListRegistrationStates.RESERVED, reserveToday, null);
        LogHelper.log(hl7InfoLog,
                "\nsend Orm Msg for the 'reserved' action on request with id = "+ request.getId());
        final Long currentUserId = UserHolder.getInstance()
                .getCurrentUser().getId();

        ThreadExecutor.execute(new Action() {
            @Override
            public void execute() throws Exception {
                TransactionExecuter.execute(
                        new SinglePersistenceSessionAction() {
                            @Override
                            public void execute() {
                                sendOrmMsg(toSend, currentUserId, true, getSession());
                            }
                        });
            }
        });
        LogHelper.log(hl7InfoLog, "\nfinish sendHL7 msg on request with id = " + request.getId());
    }

    private RadiologyExamRequest prepareRadiologyRequestToSend(RadiologyExamRequest source, List<Long> radExItemIds, WaitingListRegistrationStates state, Boolean reserveToday, Date performDate)
            throws PersistenceBeanException, InstantiationException, AssignValidationException, CloneNotSupportedException, IllegalAccessException {
        RadiologyExamRequest fromGeneral = generalLogic(source, radExItemIds,
                state, reserveToday, null,
                null, null, Boolean.FALSE, null, performDate);
        return fromGeneral == null ? source : fromGeneral;
    }

    private RadiologyExamRequest generalLogic(RadiologyExamRequest request,
                                              List<Long> itemsIds, WaitingListRegistrationStates state,
                                              Boolean reserveToday, String comment, String reportResult,
                                              FileEntity fileEntity, Boolean isAggregation, Date reportDate, Date performDate)
            throws PersistenceBeanException, CloneNotSupportedException,
            IllegalAccessException, HibernateException, InstantiationException,
            AssignValidationException {
        if (!ValidationHelper.isNullOrEmpty(request.getRadiologyExamRequestItems()) && !ValidationHelper.isNullOrEmpty(itemsIds)) {
            boolean isFromComplete = WaitingListRegistrationStates
                    .REPORTED.equals(request.getWaitingListRegistrationState());
            if (!isAggregation) {
                if (request.getRadiologyExamRequestItems().size() == itemsIds.size()) {
                    request.setWaitingListRegistrationState(state);
                    if (!isFromComplete || !WaitingListRegistrationStates.SIGNED.equals(state)) {
                        switch (request.getWaitingListRegistrationState()) {
                            case DRAFT:
                            case SIGNED:
                            case REPORTED:
                                if (!ValidationHelper.isNullOrEmpty(request.getHl7FieldsFromAsap())) {
                                    request.setSendingStatus(Hl7RequestSendingStatus.NOT_SENT);
                                }
                                request.setReferringDoctor(UserHolder.getInstance().getCurrentUser().getFullname());
                                break;

                            default:
                                break;
                        }
                    }
                    DaoManager.save(request);
                    String accessNumber = null;
                    Long cardNumber = null;

                    if (WaitingListRegistrationStates.ACCEPTED.equals(state)) {
                        accessNumber = getGeneratedAccessNumberId();
                        cardNumber = generateCardNumber(request.getExamType(), request.getSector());
                    }
                    if (isFromComplete && WaitingListRegistrationStates.SIGNED.equals(state)) {
                        for (RadiologyExamRequestItem item : request.getRadiologyExamRequestItems()) {
                            item.setSignFileEntity(fileEntity);
                            item.setFileEntity(fileEntity);
                            item.setWaitingListRegistrationState(state);
                            DaoManager.save(item);
                        }
                    } else {
                        for (RadiologyExamRequestItem item : request.getRadiologyExamRequestItems()) {
                            setFieldsInRequestItem(state, comment, reportResult,
                                    item, request, UserHolder.getInstance().getCurrentUser()
                                            .getFullname(), reserveToday,
                                    fileEntity, accessNumber, cardNumber, reportDate, performDate);
                            item.setReportSaveDate(new Date());
                            item.setRadiologyExamRequest(request);
                            DaoManager.save(item);
                        }
                    }
                    if (WaitingListRegistrationStates.RESERVED.equals(state)) {
                        if (reserveToday) {
                            request.setLatestActionDate(new Date());
                            request.setReserveDate(new Date());
                            request.setAssignUserId(UserHolder.getInstance().getCurrentUser().getId());
                            request.setAssignDate(new Date());

                            if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())
                                    && !WaitingListRegistrationStates.REPORTED.equals(request.getWaitingListRegistrationState())) {
                                request.setLatestActionPerformDate(new Date());
                            }
                        } else {
                            Date ecrDate = null;
                            for (RadiologyExamRequestItemBase radiologyExamRequestItem : request.getRadiologyExamRequestItems()) {
                                if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestItem.getEventCalendarRegistration())
                                        && !ValidationHelper.isNullOrEmpty(radiologyExamRequestItem.getEventCalendarRegistration().getDate())) {
                                    ecrDate = radiologyExamRequestItem.getEventCalendarRegistration().getDate();
                                    break;
                                }
                            }
                            if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())
                                    && !WaitingListRegistrationStates.REPORTED.equals(request.getWaitingListRegistrationState())) {
                                request.setLatestActionPerformDate(ecrDate);
                            }
                            request.setLatestActionDate(ecrDate);
                            request.setReserveDate(ecrDate);
                        }
                    } else if (WaitingListRegistrationStates.REQUIRED
                            .equals(state)) {
                        request.setRequestDate(new Date());
                        request.setLatestActionDate(new Date());

                        if (!WaitingListRegistrationStates.DRAFT.equals(
                                request.getWaitingListRegistrationState())
                                && !WaitingListRegistrationStates.REPORTED
                                .equals(request
                                        .getWaitingListRegistrationState())) {
                            request.setLatestActionPerformDate(new Date());
                        }
                    } else if (WaitingListRegistrationStates.ANNULLED
                            .equals(state)
                            || WaitingListRegistrationStates.DELETED
                            .equals(state)) {
                        request.setReserveDate(null);
                    } else if (!isFromComplete || !WaitingListRegistrationStates.SIGNED.equals(state)) {
                        request.setLatestActionDate(new Date());
                        if (!WaitingListRegistrationStates.DRAFT.equals(
                                request.getWaitingListRegistrationState())
                                && !WaitingListRegistrationStates.REPORTED
                                .equals(request
                                        .getWaitingListRegistrationState())) {
                            request.setLatestActionPerformDate(new Date());
                        }
                    }
                    DaoManager.save(request);
                    if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())
                            && !WaitingListRegistrationStates.IN_READING.equals(request.getWaitingListRegistrationState())) {
                        saveHistoryForRequest(request);
                    }
                } else {
                    return splitRadiologyExamRequest(request, itemsIds, state,
                            reserveToday, comment, reportResult, fileEntity,
                            UserHolder.getInstance().getCurrentUser()
                                    .getFullname(), reportDate);
                }
            } else {
                Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair = getPairRequestsItems(itemsIds);
                if (!ValidationHelper.isNullOrEmpty(pair)) {
                    List<RadExamRequestWrapper> requestsWrappers = pair
                            .getFirst();
                    if (!ValidationHelper.isNullOrEmpty(requestsWrappers)) {
                        for (RadExamRequestWrapper rerw : requestsWrappers) {
                            RadiologyExamRequest localRequest = DaoManager.get(
                                    RadiologyExamRequest.class, rerw.getId());
                            if (!ValidationHelper.isNullOrEmpty(localRequest)) {
                                if (rerw.getRadExamRequestItemWrappers().size() == localRequest
                                        .getRadiologyExamRequestItems().size()) {
                                    // simple aggregation
                                    localRequest
                                            .setLatestActionDate(new Date());
                                    localRequest
                                            .setWaitingListRegistrationState(state);

                                    if (!WaitingListRegistrationStates.DRAFT
                                            .equals(localRequest
                                                    .getWaitingListRegistrationState())
                                            && !WaitingListRegistrationStates.REPORTED
                                            .equals(localRequest
                                                    .getWaitingListRegistrationState())
                                            && !WaitingListRegistrationStates.SIGNED
                                            .equals(localRequest
                                                    .getWaitingListRegistrationState())) {
                                        localRequest.setLatestActionPerformDate(
                                                new Date());
                                    }
                                    if (!isFromComplete || !WaitingListRegistrationStates.SIGNED.equals(state)) {
                                        switch (localRequest
                                                .getWaitingListRegistrationState()) {
                                            case DRAFT:
                                            case SIGNED:
                                            case REPORTED:
                                                if (!ValidationHelper
                                                        .isNullOrEmpty(localRequest
                                                                .getHl7FieldsFromAsap())) {
                                                    localRequest
                                                            .setSendingStatus(Hl7RequestSendingStatus.NOT_SENT);
                                                }
                                                localRequest
                                                        .setReferringDoctor(UserHolder
                                                                .getInstance()
                                                                .getCurrentUser()
                                                                .getFullname());
                                                break;

                                            default:
                                                break;
                                        }
                                    }
                                    DaoManager.save(localRequest);

                                    String accessNumber = null;
                                    Long cardNumber = null;

                                    if (WaitingListRegistrationStates.ACCEPTED
                                            .equals(state)) {
                                        accessNumber = getGeneratedAccessNumberId();
                                        cardNumber = generateCardNumber(
                                                localRequest.getExamType(),
                                                localRequest.getSector());
                                    }
                                    for (RadiologyExamRequestItem item : localRequest
                                            .getRadiologyExamRequestItems()) {
                                        if (isFromComplete && WaitingListRegistrationStates.SIGNED.equals(state)) {
                                            item.setSignFileEntity(fileEntity);
                                            item.setFileEntity(fileEntity);
                                            item.setWaitingListRegistrationState(state);
                                            DaoManager.save(item);
                                        } else {
                                            setFieldsInRequestItem(state, comment,
                                                    reportResult, item, localRequest, UserHolder
                                                            .getInstance()
                                                            .getCurrentUser()
                                                            .getFullname(),
                                                    reserveToday, fileEntity,
                                                    accessNumber, cardNumber, reportDate, null);
                                            item.setRadiologyExamRequest(localRequest);
                                            DaoManager.save(item);
                                        }
                                    }
                                    if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())
                                            && !WaitingListRegistrationStates.IN_READING.equals(request.getWaitingListRegistrationState())) {
                                        saveHistoryForRequest(localRequest);
                                    }
                                } else {
                                    // aggregation with split
                                    return splitRadiologyExamRequest(localRequest,
                                            rerw.getItemsIds(), state,
                                            reserveToday, comment,
                                            reportResult, fileEntity,
                                            UserHolder.getInstance()
                                                    .getCurrentUser()
                                                    .getFullname(), reportDate);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void generateAccessNumber(RadiologyExamRequestItem requestItem,
                                      RadiologyExamRequest radiologyExamRequest, String accessNumber) throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        requestItem.setAccessNumberCode(radiologyExamRequest.getSector().getAccNumPrefix());

        requestItem.setAccessNumberYear((long) Calendar.getInstance().get(Calendar.YEAR));

        requestItem.setAccessNumberId(accessNumber);
    }

    private Long generateCardNumber(ExamType examType, Sector risSector)
            throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        CardNumber cardNumber = null;

        try {
            cardNumber = DaoManager.get(CardNumber.class, new Criterion[]{
                    Restrictions.eq("examType.id", examType.getId()),
                    Restrictions.eq("risSector.id", risSector.getId())
            });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (cardNumber != null) {
            try {
                cardNumber
                        .setCardNumber(cardNumber.getCardNumber().longValue() + 1);

                DaoManager.save(cardNumber);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } else {
            cardNumber = new CardNumber();
            cardNumber.setExamType(examType);
            cardNumber.setRisSector(risSector);
            cardNumber.setCardNumber(new Long(1));

            try {
                DaoManager.save(cardNumber);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return cardNumber.getCardNumber();
    }

    private String getGeneratedAccessNumberId() {
        Long currentYear = new Long(Calendar.getInstance().get(Calendar.YEAR));
        AccessNumberForYear accessNumberYear = null;
        try {
            accessNumberYear = DaoManager.get(AccessNumberForYear.class,
                    new Criterion[]{
                            Restrictions.eq("accessNumberYear", currentYear)
                    });

        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (accessNumberYear != null) {
            try {
                if (accessNumberYear.getAccessNumberId() == null) {
                    accessNumberYear.setAccessNumberId(getNumber(currentYear));
                } else {
                    accessNumberYear.setAccessNumberId(accessNumberYear
                            .getAccessNumberId().longValue() + 1);
                }

                DaoManager.save(accessNumberYear);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            return String.format("%06d", accessNumberYear.getAccessNumberId());
        } else {
            accessNumberYear = new AccessNumberForYear();
            accessNumberYear.setAccessNumberId(getNumber(currentYear));
            accessNumberYear.setAccessNumberYear(currentYear);

            try {
                DaoManager.save(accessNumberYear);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            return String.format("%06d", accessNumberYear.getAccessNumberId());
        }
    }

    private Long getNumber(Long currentYear) {
        Long number = null;
        try {
            String str = (String) DaoManager
                    .getSession()
                    .createQuery(
                            "select coalesce(max(accessNumberId) + '0',1) from RadiologyExamRequestItem where accessNumberYear = "
                                    + currentYear).uniqueResult();

            number = Long.valueOf(str);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (number == null) {
            return new Long(1l);
        }

        return number.longValue() + 1;
    }

    private void eventCalendarLogic(WaitingListRegistrationStates state,
                                    RadiologyExamRequestItem item, Boolean reserveToday)
            throws PersistenceBeanException, InstantiationException,
            IllegalAccessException, HibernateException,
            AssignValidationException {
        if (!WaitingListRegistrationStates.REQUIRED.equals(state)) {
            EventCalendarRegistration ecr = item.getEventCalendarRegistration();

            if (ecr != null && !ValidationHelper.isNullOrEmpty(ecr.getSlots())) {
                for (EventCalendarRegistrationSlot slot : ecr.getSlots()) {
                    DaoManager.remove(slot);
                }

                ecr.setSlots(null);
            }

            if (Boolean.FALSE.equals(reserveToday) && ecr != null) {
                ecr.setPackageId(UUID.randomUUID().toString());
                ecr.setRadiologyExamRequestItem(item);
                DaoManager.save(ecr);

                item.setReserveDate(ecr.getDate());

                ecr.setSlots(ecr.getSlotsToSave());
                if (!ValidationHelper.isNullOrEmpty(ecr.getSlots())) {
                    for (EventCalendarRegistrationSlot slot : ecr.getSlots()) {
                        DaoManager.save(slot);
                    }
                }
            } else {
                if (ecr == null) {
                    ecr = DaoManager.get(EventCalendarRegistration.class,
                            new Criterion[]{
                                    Restrictions.eq("radiologyExamRequestItem.id",
                                            item.getId())
                            });
                }

                if (ecr != null) {
                    DaoManager.remove(ecr);
                }

                if (WaitingListRegistrationStates.RESERVED.equals(state)
                        && Boolean.TRUE.equals(reserveToday)) {
                    item.setWaitingListRegistrationState(WaitingListRegistrationStates.RESERVED);
                    item.setReserveDate(new Date());
                }
            }
        }
    }

    private RadiologyExamRequest splitRadiologyExamRequest(RadiologyExamRequest request,
                                                           List<Long> itemsIds, WaitingListRegistrationStates state,
                                                           Boolean reserveToday, String comment, String reportResult,
                                                           FileEntity fileEntity, String currUserFullName, Date reportDate)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, AssignValidationException {
        RadiologyExamRequest splittedRequest = request.clone();
        request.setUserClosingReportId(null);
        splittedRequest.setWaitingListRegistrationState(state);

        DaoManager.save(splittedRequest);

        List<Long> itemsToRemoveIds = new ArrayList<Long>();

        String accessNumber = null;
        Long cardNumber = null;

        if (WaitingListRegistrationStates.ACCEPTED.equals(state)) {
            accessNumber = getGeneratedAccessNumberId();
            cardNumber = generateCardNumber(request.getExamType(),
                    request.getSector());
        }

        if (!ValidationHelper.isNullOrEmpty(request.getRadiologyExamRequestItems())) {
            for (RadiologyExamRequestItem item : request.getRadiologyExamRequestItems()) {
                if (itemsIds.contains(item.getId())) {
                    itemsToRemoveIds.add(item.getId());
                    item.setRadiologyExamRequest(splittedRequest);
                    setFieldsInRequestItem(state, comment, reportResult, item,
                            request, currUserFullName, reserveToday, fileEntity,
                            accessNumber, cardNumber, reportDate, null);

                    DaoManager.save(item);
                }
            }
        }

        List<RadiologyExamRequestItem> itemsToRemove = new ArrayList<RadiologyExamRequestItem>();
        List<RadiologyExamRequestItem> itemsToAdd = new ArrayList<RadiologyExamRequestItem>();

        if (!ValidationHelper.isNullOrEmpty(itemsToRemoveIds)) {
            for (RadiologyExamRequestItem reri : request.getRadiologyExamRequestItems()) {
                if (itemsToRemoveIds.contains(reri.getId())) {
                    itemsToRemove.add(reri);
                } else {
                    itemsToAdd.add(reri);
                }
            }
        }

        request.setRadiologyExamRequestItems(itemsToAdd);
        splittedRequest.setRadiologyExamRequestItems(itemsToRemove);

        request.udateItemsDescription(itemsToAdd);//FIXME: DELETE AFTER CREATE TRIGGER
        splittedRequest.udateItemsDescription(itemsToRemove);//FIXME: DELETE AFTER CREATE TRIGGER

        DaoManager.save(splittedRequest);
        DaoManager.save(request);

        logActivity(request.getId(), splittedRequest.getId());

        if (WaitingListRegistrationStates.RESERVED.equals(state)) {
            if (reserveToday) {
                splittedRequest.setLatestActionDate(new Date());
                splittedRequest.setReserveDate(new Date());

                if (!WaitingListRegistrationStates.DRAFT.equals(
                        request.getWaitingListRegistrationState())
                        && !WaitingListRegistrationStates.REPORTED
                        .equals(request
                                .getWaitingListRegistrationState())) {
                    splittedRequest.setLatestActionPerformDate(new Date());
                }
            } else {
                if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())
                        && !WaitingListRegistrationStates.REPORTED.equals(request.getWaitingListRegistrationState())) {
                    splittedRequest.setLatestActionPerformDate(splittedRequest
                            .getRadiologyExamRequestItems().get(0)
                            .getEventCalendarRegistration()
                            .getDate());
                }

                splittedRequest.setLatestActionDate(splittedRequest
                        .getRadiologyExamRequestItems().get(0)
                        .getEventCalendarRegistration().getDate());
                splittedRequest.setReserveDate(splittedRequest
                        .getRadiologyExamRequestItems().get(0)
                        .getEventCalendarRegistration().getDate());
            }
        } else if (WaitingListRegistrationStates.REQUIRED
                .equals(state)) {
            splittedRequest.setRequestDate(new Date());
            splittedRequest.setLatestActionDate(new Date());

            if (!WaitingListRegistrationStates.DRAFT.equals(
                    request.getWaitingListRegistrationState())
                    && !WaitingListRegistrationStates.REPORTED
                    .equals(request
                            .getWaitingListRegistrationState())) {
                splittedRequest.setLatestActionPerformDate(new Date());
            }
        } else if (WaitingListRegistrationStates.ANNULLED
                .equals(state)
                || WaitingListRegistrationStates.DELETED
                .equals(state)) {
            splittedRequest.setReserveDate(null);
        } else {
            splittedRequest.setLatestActionDate(new Date());

            if (!WaitingListRegistrationStates.DRAFT.equals(
                    request.getWaitingListRegistrationState())
                    && !WaitingListRegistrationStates.REPORTED
                    .equals(request
                            .getWaitingListRegistrationState())) {
                splittedRequest.setLatestActionPerformDate(new Date());
            }
        }

        switch (splittedRequest.getWaitingListRegistrationState()) {
            case DRAFT:
            case REPORTED:
                if (!ValidationHelper.isNullOrEmpty(splittedRequest
                        .getHl7FieldsFromAsap())) {
                    splittedRequest
                            .setSendingStatus(Hl7RequestSendingStatus.NOT_SENT);
                    asapSplittedRequest = splittedRequest;
                }
                splittedRequest.setReferringDoctor(currUserFullName);
                break;

            default:
                break;
        }

        DaoManager.save(splittedRequest);
        DaoManager.save(request);

        refillExamsName(request);

        if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())
                && !WaitingListRegistrationStates.IN_READING.equals(request.getWaitingListRegistrationState())) {
            saveHistoryForRequest(request);
        }
        if (!WaitingListRegistrationStates.DRAFT.equals(splittedRequest.getWaitingListRegistrationState())
                && !WaitingListRegistrationStates.IN_READING.equals(splittedRequest.getWaitingListRegistrationState())) {
            saveHistoryForRequest(splittedRequest);
        }
        return splittedRequest;
    }

    private void refillExamsName(RadiologyExamRequest request)
            throws PersistenceBeanException {
        String examsName = "";

        for (RadiologyExamRequestItem radExanReqItem : request
                .getRadiologyExamRequestItems()) {
            examsName = (examsName.isEmpty() ? examsName : (examsName + ", "))
                    + radExanReqItem.getRadiologyExam().getDescription();
        }

        for (RadiologyExamRequestItem radExanReqItem : request
                .getRadiologyExamRequestItems()) {
            if (radExanReqItem.getEventCalendarRegistrations() != null) {
                for (EventCalendarRegistration ecr : radExanReqItem
                        .getEventCalendarRegistrations()) {
                    ecr.setExamsName(examsName);

                    DaoManager.save(ecr);
                }
            }
        }
    }

    private void setFieldsInRequestItem(WaitingListRegistrationStates state,
                                        String comment, String reportResult, RadiologyExamRequestItem item,
                                        RadiologyExamRequest radiologyExamRequest, String currUserFullname, Boolean reserveToday,
                                        FileEntity fileEntity, String accessNumber, Long cardNumber, Date reportDate, Date performDate)
            throws PersistenceBeanException, IllegalAccessException,
            InstantiationException, HibernateException,
            AssignValidationException {
        if (performDate == null) {
            performDate = new Date();
        }
        if (WaitingListRegistrationStates.REQUIRED.equals(state)) {
            item.setRequestDate(new Date());
            if (item.getEventCalendarRegistrations() != null) {
                for (EventCalendarRegistration ecr : item
                        .getEventCalendarRegistrations()) {
                    ecr.setDate(null);
                }
            }
        } else if (WaitingListRegistrationStates.PERFORMED.equals(state)) {
            item.setTrsm(currUserFullname);
            item.setPerformDate(performDate);
            radiologyExamRequest.setPerformDate(performDate);

            if (WaitingListRegistrationStates.DRAFT
                    .equals(radiologyExamRequest
                            .getWaitingListRegistrationState())
                    || WaitingListRegistrationStates.REPORTED
                    .equals(radiologyExamRequest
                            .getWaitingListRegistrationState())) {
                radiologyExamRequest
                        .setLatestActionPerformDate(new Date());
            }
        } else if (WaitingListRegistrationStates.REPORTED.equals(state)) {
            item.setReportDate(reportDate);
            item.setFileEntity(fileEntity);
            item.setReportResult(reportResult);
        } else if (WaitingListRegistrationStates.SIGNED.equals(state)) {
            item.setReportDate(reportDate);
            item.setSignFileEntity(fileEntity);
            item.setFileEntity(fileEntity);
            item.setReportResult(reportResult);
        } else if (WaitingListRegistrationStates.DRAFT.equals(state)) {
            item.setReportDate(null);
            item.setReportResult(reportResult);
        } else if (WaitingListRegistrationStates.ACCEPTED.equals(state)) {
            item.setOperator(currUserFullname);
            item.setAcceptDate(new Date());

            item.setCardNumber(cardNumber);

            if (!WaitingListRegistrationStates.PERFORMED.equals(item
                    .getWaitingListRegistrationState())
                    && !WaitingListRegistrationStates.IN_READING.equals(item
                    .getWaitingListRegistrationState())) {
                if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest.getSector())) {
                    generateAccessNumber(item, radiologyExamRequest, accessNumber);
                }
            } else {
                radiologyExamRequest.setPerformDate(null);

                if (WaitingListRegistrationStates.DRAFT
                        .equals(radiologyExamRequest
                                .getWaitingListRegistrationState())
                        || WaitingListRegistrationStates.REPORTED
                        .equals(radiologyExamRequest
                                .getWaitingListRegistrationState())) {
                    radiologyExamRequest
                            .setLatestActionPerformDate(null);
                }

                item.setPerformDate(null);
                item.setTrsm(null);
            }
        } else if (WaitingListRegistrationStates.DELETED.equals(state)) {
            item.setReserveDate(null);
            item.setFileEntity(fileEntity);
            item.setDeleteComment(comment);
            item.setDeleteUser(currUserFullname);

            deleteRegistrationForRequestItem(item);
        } else if (WaitingListRegistrationStates.ANNULLED.equals(state)) {
            item.setReserveDate(null);
            item.setFileEntity(fileEntity);
            item.setDeleteComment(comment);

            deleteRegistrationForRequestItem(item);
        }

        item.setWaitingListRegistrationState(state);

        if (WaitingListRegistrationStates.RESERVED.equals(state)
                || WaitingListRegistrationStates.REQUIRED.equals(state)) {
            eventCalendarLogic(state, item, reserveToday);
        }
    }

    public void toAcceptedState(RadiologyExamRequest selectedRequest,
                                List<Long> radExItemIds) throws HibernateException,
            PersistenceBeanException, CloneNotSupportedException,
            IllegalAccessException, InstantiationException,
            AssignValidationException {
        generalLogic(selectedRequest, radExItemIds,
                WaitingListRegistrationStates.ACCEPTED, null, null, null, null,
                Boolean.FALSE, null, null);
    }

    public void toPerformedState(final RadiologyExamRequest selectedRequest, List<Long> radExItemIds, Date performDate)
            throws PersistenceBeanException, InstantiationException, AssignValidationException, CloneNotSupportedException, IllegalAccessException {
        final RadiologyExamRequest toSend = prepareRadiologyRequestToSend(selectedRequest, radExItemIds, WaitingListRegistrationStates.PERFORMED, null, performDate);

        LogHelper.log(hl7InfoLog,
                "\nsend Orm Msg for the 'perform' action on request with id = "
                        + selectedRequest.getId());
        final Long currentUserId = UserHolder.getInstance().getCurrentUser().getId();
        ThreadExecutor.execute(new Action() {
            @Override
            public void execute() throws Exception {
                TransactionExecuter.execute(
                        new SinglePersistenceSessionAction() {
                            @Override
                            public void execute() {
                                sendOrmMsg(toSend, currentUserId, false, getSession());
                            }
                        }
                );
            }
        });

    }

    public void toReportedState(RadiologyExamRequest selectedRequest,
                                List<Long> radExItemIds, String reportResult,
                                FileEntity fileEntity, Boolean isAggregation, Date reportDate)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, AssignValidationException {
        generalLogic(selectedRequest, radExItemIds,
                WaitingListRegistrationStates.REPORTED, null, null,
                reportResult, fileEntity, isAggregation, reportDate, null);
    }


    public void toSignState(RadiologyExamRequest selectedRequest,
                            List<Long> radExItemIds, String reportResult,
                            FileEntity fileEntity, Boolean isAggregation, Date reportDate)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, AssignValidationException {
        generalLogic(selectedRequest, radExItemIds,
                WaitingListRegistrationStates.SIGNED, null, null,
                reportResult, fileEntity, isAggregation, reportDate, null);
    }


    public void performReopening(RadiologyExamRequest selectedRequest,
                                 List<Long> radExItemIds, String reportResult,
                                 FileEntity fileEntity, Boolean isAggregation) throws PersistenceBeanException, InstantiationException, AssignValidationException, CloneNotSupportedException, IllegalAccessException {
        toDraftState(selectedRequest, radExItemIds, reportResult, fileEntity, isAggregation);
        sendOrmMsg(selectedRequest);
    }

    public void toDraftState(RadiologyExamRequest selectedRequest,
                             List<Long> radExItemIds, String reportResult,
                             FileEntity fileEntity, Boolean isAggregation)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, AssignValidationException {
        generalLogic(selectedRequest, radExItemIds,
                WaitingListRegistrationStates.DRAFT, null, null, reportResult,
                fileEntity, isAggregation, null, null);
    }

    public static Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> getPairRequestsItems(
            List<Long> radExamReqIds) {
        Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair = null;
        if (!ValidationHelper.isNullOrEmpty(radExamReqIds)) {
            List<RadExamRequestItemWrapper> radExamRequestItemsWrappers = new ArrayList<RadExamRequestItemWrapper>();
            List<RadExamRequestWrapper> radExamRequestsWrappers = new ArrayList<RadExamRequestWrapper>();
            try {
                List<RadiologyExamRequestItem> requestItems = DaoManager.load(
                        RadiologyExamRequestItem.class, new Criterion[]{
                                Restrictions.in("id", radExamReqIds)
                        });
                List<Long> reqIds = new ArrayList<Long>();
                if (!ValidationHelper.isNullOrEmpty(requestItems)) {
                    for (RadiologyExamRequestItem reri : requestItems) {
                        if (!reqIds.contains(reri.getRadiologyExamRequest()
                                .getId())) {
                            reqIds.add(reri.getRadiologyExamRequest().getId());
                        }
                    }
                    if (!ValidationHelper.isNullOrEmpty(reqIds)) {
                        List<RadiologyExamRequest> radExamRequests = DaoManager
                                .load(RadiologyExamRequest.class,
                                        new Criterion[]{
                                                Restrictions.in("id", reqIds)
                                        });
                        if (!ValidationHelper.isNullOrEmpty(radExamRequests)) {
                            List<RadExamRequestWrapper> radiologyExamRequestsWrappers = new ArrayList<RadExamRequestWrapper>();
                            for (RadiologyExamRequest rer : radExamRequests) {
                                RadExamRequestWrapper rerw = rer
                                        .getRadExamRequestWrapperFromRequest();
                                if (!ValidationHelper.isNullOrEmpty(rerw
                                        .getRadExamRequestItemWrappers())) {
                                    List<RadExamRequestItemWrapper> reriws = new ArrayList<RadExamRequestItemWrapper>();
                                    for (RadExamRequestItemWrapper reriw : rerw
                                            .getRadExamRequestItemWrappers()) {
                                        if (radExamReqIds.contains(reriw
                                                .getId())) {
                                            reriws.add(reriw);
                                        }
                                    }
                                    radExamRequestItemsWrappers.addAll(reriws);
                                    rerw.setRadExamRequestItemWrappers(reriws);
                                }
                                radExamRequestsWrappers.add(rerw);
                            }
                            radExamRequestsWrappers
                                    .addAll(radiologyExamRequestsWrappers);
                        }
                    }
                }
                pair = new Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>>(
                        radExamRequestsWrappers, radExamRequestItemsWrappers);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return pair;
    }

    public static Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> getPairRequestsItemsShort(
            List<Long> radExamReqIds) {
        Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair = null;
        if (!ValidationHelper.isNullOrEmpty(radExamReqIds)) {
            List<RadExamRequestItemWrapper> radExamRequestItemsWrappers = new ArrayList<>();
            List<RadExamRequestWrapper> radExamRequestsWrappers = new ArrayList<>();
            try {
                List<RadiologyExamRequestItemShort> requestItems = DaoManager.load(
                        RadiologyExamRequestItemShort.class, new Criterion[]{
                                Restrictions.in("id", radExamReqIds)
                        });
                List<Long> reqIds = new ArrayList<Long>();
                if (!ValidationHelper.isNullOrEmpty(requestItems)) {
                    for (RadiologyExamRequestItemShort reri : requestItems) {
                        if (!reqIds.contains(reri.getRadiologyExamRequest()
                                .getId())) {
                            reqIds.add(reri.getRadiologyExamRequest().getId());
                        }
                    }
                    if (!ValidationHelper.isNullOrEmpty(reqIds)) {
                        List<RadiologyExamRequestShort> radExamRequests = DaoManager
                                .load(RadiologyExamRequestShort.class,
                                        new Criterion[]{
                                                Restrictions.in("id", reqIds)
                                        });
                        if (!ValidationHelper.isNullOrEmpty(radExamRequests)) {
                            for (RadiologyExamRequestShort rer : radExamRequests) {
                                RadExamRequestWrapper rerw = rer
                                        .getRadExamRequestWrapperFromRequest();
                                if (!ValidationHelper.isNullOrEmpty(rerw
                                        .getRadExamRequestItemWrappers())) {
                                    List<RadExamRequestItemWrapper> reriws = new ArrayList<>();
                                    for (RadExamRequestItemWrapper reriw : rerw
                                            .getRadExamRequestItemWrappers()) {
                                        if (radExamReqIds.contains(reriw
                                                .getId())) {
                                            reriws.add(reriw);
                                        }
                                    }
                                    radExamRequestItemsWrappers.addAll(reriws);
                                    rerw.setRadExamRequestItemWrappers(reriws);
                                }
                                radExamRequestsWrappers.add(rerw);
                            }
                            List<RadExamRequestWrapper> radiologyExamRequestsWrappers = new ArrayList<>();
                            radExamRequestsWrappers.addAll(radiologyExamRequestsWrappers);
                        }
                    }
                }
                pair = new Pair<>(radExamRequestsWrappers, radExamRequestItemsWrappers);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return pair;
    }

    public static List<RadiologyExamRequest> getRadiologyExamRequestsAfterSplit(
            RadiologyExamRequest radExRequest)
            throws CloneNotSupportedException {
        List<RadiologyExamRequest> radiologyExamRequests = new ArrayList<RadiologyExamRequest>();

        if (radExRequest != null) {
            List<RadiologyExamRequestItem> raExamRequestItems = radExRequest
                    .getRadiologyExamRequestItems();
            if (!ValidationHelper.isNullOrEmpty(raExamRequestItems)) {
                List<Long> examTypesIds = new ArrayList<Long>();
                for (RadiologyExamRequestItem reri : raExamRequestItems) {
                    if (reri.getRadiologyExam() != null
                            && reri.getRadiologyExam().getExamType() != null
                            && !examTypesIds.contains(reri.getRadiologyExam()
                            .getExamType().getId())) {
                        examTypesIds.add(reri.getRadiologyExam().getExamType()
                                .getId());
                    }
                }
                if (!ValidationHelper.isNullOrEmpty(examTypesIds)) {
                    for (Long l : examTypesIds) {
                        RadiologyExamRequest request = radExRequest.clone();
                        List<RadiologyExamRequestItem> requestItems = new ArrayList<RadiologyExamRequestItem>();
                        for (RadiologyExamRequestItem reri : raExamRequestItems) {
                            if (reri.getRadiologyExam() != null
                                    && reri.getRadiologyExam().getExamType() != null
                                    && l.equals(reri.getRadiologyExam()
                                    .getExamType().getId())) {
                                requestItems.add(reri);
                            }
                        }
                        request.setExamType(requestItems.get(0)
                                .getRadiologyExam().getExamType());
                        request.setRadiologyExamRequestItems(requestItems);

                        if (request.getRadiologyExamRequestItems().get(0)
                                .getReserveDate() != null) {
                            request.setLatestActionDate(request
                                    .getRadiologyExamRequestItems().get(0)
                                    .getReserveDate());

                            if (!WaitingListRegistrationStates.DRAFT
                                    .equals(request
                                            .getWaitingListRegistrationState())
                                    && !WaitingListRegistrationStates.REPORTED
                                    .equals(request
                                            .getWaitingListRegistrationState())) {
                                request
                                        .setLatestActionPerformDate(request
                                                .getRadiologyExamRequestItems().get(0)
                                                .getReserveDate());
                            }
                        }

                        radiologyExamRequests.add(request);
                    }
                }
            }
        }

        return radiologyExamRequests;
    }

    public void sendOrmMsg(RadiologyExamRequest request) {
        try {
            sendOrmMsg(request, DaoManager.getSession());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void sendOrmMsg(RadiologyExamRequest request, Session session) {
        try {
            sendOrmMsg(request, UserHolder.getInstance().getCurrentUser().getId(), true, session);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void sendOrmMsg(RadiologyExamRequest request, Long userId, boolean updateRequest, Session session) {
        if (asapSplittedRequest != null) {
            request = asapSplittedRequest;
            asapSplittedRequest = null;
        }
        if (request != null) {
            if (updateRequest) {
                request.setSendingStatus(Hl7RequestSendingStatus.NOT_SENT);
                request.setLastHl7OrmSenderUserId(userId);
                try {
                    ConnectionManager.save(request, session);
                } catch (HibernateException e1) {
                    LogHelper.log(hl7ErrorLog, e1);
                }
            }

            if (hl7HostValue != null && hl7PortValue != null
                    && hl7SendRetryTimeValue != null) {
                try {
                    ORM_O01 msg = new ORM_O01();
                    msg.getParser().getParserConfiguration().setValidating(false);
                    msg = ORMHelper.getFilledMessage(msg, request, false, session);

                    if (msg != null) {
                        String host = hl7HostValue;
                        Integer port = Integer.valueOf(hl7PortValue);
                        Integer timeout = Integer
                                .valueOf(hl7SendRetryTimeValue);

                        LogHelper
                                .log(hl7InfoLog, "Orm Msg for the cancel request: \n" + msg.encode());

                        Message response = BaseHl7MessageHelper.sendMessage(
                                msg, host, port, timeout);

                        LogHelper
                                .log(hl7InfoLog, "Orm Msg for the cancel response: \n" + response.encode());

                        if (response != null && updateRequest
                                && BaseHl7MessageHelper.OK_RESPONSE
                                .equals(((ACK) response).getMSA()
                                        .getAcknowledgmentCode()
                                        .getValue())) {
                            if(!ValidationHelper.isNullOrEmpty(((ACK) response).getMSA().getMsa4_ExpectedSequenceNumber().getValue())){
                                Hl7FieldsFromAsap hl7FieldsFromAsap = null;
                                if (request.getHl7FieldsFromAsap() != null) {
                                    hl7FieldsFromAsap = request.getHl7FieldsFromAsap();
                                } else {
                                    hl7FieldsFromAsap = new Hl7FieldsFromAsap();
                                }
                                hl7FieldsFromAsap.setPlacerOrderNumber("999" +
                                        ((ACK) response).getMSA().getMsa4_ExpectedSequenceNumber().getValue());
                                ConnectionManager.save(hl7FieldsFromAsap, session);
                                request.setHl7FieldsFromAsap(hl7FieldsFromAsap);
                            }
                            request.setSendingStatus(Hl7RequestSendingStatus.SENT);
                            ConnectionManager.save(request, session);
                        }
                    }
                } catch (Exception e) {
                    LogHelper.log(hl7ErrorLog, e);
                }
            }
        }
    }

    private void deleteRegistrationForRequestItem(
            RadiologyExamRequestItem requestItem) {
        try {
            requestItem = DaoManager.get(RadiologyExamRequestItem.class, requestItem.getId());
            if (requestItem != null) {
                if (!ValidationHelper.isNullOrEmpty(requestItem
                        .getEventCalendarRegistrations())) {
                    for (EventCalendarRegistration ecr : requestItem
                            .getEventCalendarRegistrations()) {
                        if (!ValidationHelper.isNullOrEmpty(ecr.getSlots())) {
                            for (EventCalendarRegistrationSlot ecrs : ecr
                                    .getSlots()) {
                                DaoManager.remove(ecrs);
                            }
                        }
                        DaoManager.remove(ecr);
                    }
                }

                requestItem.setReserveDate(null);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static List<WaitingListRegistrationStateWrapper> getWaitinglistStateWrappers() {
        List<WaitingListRegistrationStateWrapper> list = new ArrayList<WaitingListRegistrationStateWrapper>();

        for (WaitingListRegistrationStates state : WaitingListRegistrationStates
                .values()) {
            if (state.equals(WaitingListRegistrationStates.REQUIRED)
                    || state.equals(WaitingListRegistrationStates.RESERVED)
                    || state.equals(WaitingListRegistrationStates.DELETED)
                    || state.equals(WaitingListRegistrationStates.ANNULLED)) {
                list.add(new WaitingListRegistrationStateWrapper(state));
            }
        }

        return list;
    }

    public static List<WaitingListRegistrationStateWrapper> getWorklistStateWrappers() {
        List<WaitingListRegistrationStateWrapper> list = new ArrayList<WaitingListRegistrationStateWrapper>();

        for (WaitingListRegistrationStates state : WaitingListRegistrationStates
                .values()) {
            if (!state.equals(WaitingListRegistrationStates.REQUIRED)
                    && !state.equals(WaitingListRegistrationStates.ANNULLED)
                    && !state.equals(WaitingListRegistrationStates.SUSPENDED)) {
                list.add(new WaitingListRegistrationStateWrapper(state));
            }
        }

        return list;
    }

    public void saveHistoryForRequest(RadiologyExamRequest request) {
        try {
            switch (request.getWaitingListRegistrationState()) {
                case RESERVED:
                case ACCEPTED:
                case IN_READING:
                case PERFORMED:
                case DRAFT:
                case SIGNED:
                case REPORTED:
                    HistoricalReportHelper.getInstance()
                            .saveHistoricalReportForRadiologyExamRequest(
                                    request, null, null, false);
                    break;

                case DELETED:
                case REQUIRED:
                case ANNULLED:
                    HistoricalReport historicalReport = DaoManager.get(
                            HistoricalReport.class, new Criterion[]{
                                    Restrictions
                                            .eq("radiologyExamRequest", request)
                            });
                    if (historicalReport != null) {
                        DaoManager.remove(historicalReport);
                    }
                default:
                    break;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void openPSD(String psdNumber) {
        try {
            String asapSioUrl = ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.ASAP_SIO_ADDRESS)
                    .getValue();
            String asapSioUser = ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.ASAP_SIO_USER_FOR_PSD)
                    .getValue();
            String asapSioPassword = ApplicationSettingsHolder
                    .getInstance()
                    .getByKey(ApplicationSettingsKeys.ASAP_SIO_PASSWORD_FOR_PSD)
                    .getValue();

            if (!ValidationHelper.isNullOrEmpty(asapSioUrl)
                    && !ValidationHelper.isNullOrEmpty(asapSioUser)
                    && !ValidationHelper.isNullOrEmpty(asapSioPassword)) {
                if (!ValidationHelper.isNullOrEmpty(psdNumber)) {
                    String url = getUrlForPSD(asapSioUrl, asapSioUser,
                            asapSioPassword, psdNumber);

                    StringBuilder sb = new StringBuilder();
                    sb.append("window.open('");
                    sb.append(url);
                    sb.append("','_blank');");

                    PFRequestContextHelper.executeJS(sb.toString());
                }
            } else {
                MessageHelper
                        .addGlobalMessage(
                                FacesMessage.SEVERITY_WARN,
                                ResourcesHelper.getValidation("warning"),
                                ResourcesHelper
                                        .getValidation("workListAsapSioSettingsForPSDIsEmpty"));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private static String getUrlForPSD(String asapSioUrl, String asapSioUser,
                                       String asapSioPassword, String psdNumber) {
        StringBuilder url = new StringBuilder();

        StringBuilder sbForChk = new StringBuilder();
        sbForChk.append(asapSioUser);
        sbForChk.append(MD5.encodeString(asapSioPassword, null));
        sbForChk.append(psdNumber);

        String chk = MD5.encodeString(sbForChk.toString(), null);

        url.append(asapSioUrl);
        url.append("RemoteLoginRis.jsf?");
        url.append("user=");
        url.append(asapSioUser);
        url.append("&psd=");
        url.append(psdNumber);
        url.append("&chk=");
        url.append(chk);

        return url.toString();
    }

    public static void logActivity(UserActivityLogStates state, Long requestId) {
        LogHelper.log(activityInfoLog, "User <"
                + UserHolder.getInstance().getCurrentUser().getId().toString()
                + "> execute <" + state + "> on request with id = <"
                + requestId + ">");
    }

    public static void logActivity(Long requestId, Long splitRequestId) {
        LogHelper.log(activityInfoLog,
                "User <" + UserHolder.getInstance().getCurrentUser().getId()
                        .toString() + "> execute split on request with id = <"
                        + requestId + ">" + ", id of new request = <" + splitRequestId
                        + ">");
    }

    public static void logActivityBlockRequest(Long requestId) {
        LogHelper.log(activityInfoLog, "User <"
                + UserHolder.getInstance().getCurrentUser().getId().toString()
                + "> try to preform blocking of request with id = <"
                + requestId + ">");
    }

    public static void logActivityUnblockRequest(Long requestId) {
        LogHelper.log(activityInfoLog, "User <"
                + UserHolder.getInstance().getCurrentUser().getId().toString()
                + "> try to preform unblocking of request with id = <"
                + requestId + ">");
    }

    public static void logActivityBlockRequestSuccess() {
        LogHelper.log(activityInfoLog, "User <"
                + UserHolder.getInstance().getCurrentUser().getId().toString()
                + "> successfully preform blocking of requests");
    }

    public static void logActivityBlockRequestFailure() {
        LogHelper.log(activityInfoLog, "User <"
                + UserHolder.getInstance().getCurrentUser().getId().toString()
                + "> failure preform blocking of requests");
    }

    public static void logActivityUnblockRequestSuccess() {
        LogHelper.log(activityInfoLog, "User <"
                + UserHolder.getInstance().getCurrentUser().getId().toString()
                + "> successfully preform unblocking of requests");
    }

    public static void logActivityUnblockRequestFailure() {
        LogHelper.log(activityInfoLog, "User <"
                + UserHolder.getInstance().getCurrentUser().getId().toString()
                + "> failure preform unblocking of requests");
    }

    public static void logActivityBlockRequestCanNot() {
        LogHelper.log(activityInfoLog, "RequestsItemsWrappers list is empty");
    }
}
