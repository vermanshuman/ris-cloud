package it.nexera.ris.web.handlers;

import com.google.gson.Gson;
import it.nexera.ris.common.enums.Hl7RequestSendingStatus;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.validation.AssignValidationException;
import it.nexera.ris.common.helpers.HistoricalReportHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.WaitingListStatusHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.RadiologyExamRequestItemBase;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistration;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistrationSlot;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sector;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.TokenWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestItemWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import it.nexera.ris.web.common.Constants;
import it.nexera.ris.web.dto.ResponseDto;
import it.nexera.ris.web.dto.SignFileSecurityDto;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.lang.JoseException;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.*;

public class APIHelper {
    public static final Logger log = LogManager.getLogger(APIHelper.class);

    private RadiologyExamRequest asapSplittedRequest;

    public static TokenWrapper checkTokenValidity(String authorization, ResponseDto response, Long userId, Session session)
            throws MalformedClaimException, InstantiationException, IllegalAccessException {
        TokenWrapper tokenWrapper = new TokenWrapper();
        tokenWrapper.setValidToken(true);
        if (authorization != null) {
            User tokenUser = TokenManager.getInstance().validateAuthBearer(authorization, session);
            //JwtClaims jwtClaims = TokenManager.getInstance().validateAuthBearer(authorization, session);
            if (tokenUser != null) {
                LogHelper.debugInfo(log, "Token Found");
                String username = tokenUser.getLogin();
                LogHelper.debugInfo(log, "UserName " + username);
                User user = null;
                try {
                    List<User> users = ConnectionManager.load(User.class,
                            new Criterion[]
                                    {
                                            Restrictions.eq("login", username)
                                    }, session);

                    if (!ValidationHelper.isNullOrEmpty(users)) {
                        user = users.get(0);

                    }
                } catch (Exception e) {
                    LogHelper.log(log, e);
                    e.printStackTrace();
                }
                LogHelper.debugInfo(log, "user " + user);
                LogHelper.debugInfo(log, "userId " + userId);
                tokenWrapper.setUser(user);
                if (user == null || (userId != null && !user.getId().equals(userId))) {
                    response.setResultCode(Constants.API_FAILURE_CODE);
                    response.setResultDescription(Constants.API_TOKEN_NOT_VALID);
                    tokenWrapper.setValidToken(false);
                    return tokenWrapper;
                }
            } else {
                response.setResultCode(Constants.API_FAILURE_CODE);
                response.setResultDescription(Constants.API_TOKEN_NOT_VALID);
                tokenWrapper.setValidToken(false);
                return tokenWrapper;
            }
        } else {
            response.setResultCode(Constants.API_FAILURE_CODE);
            response.setResultDescription(Constants.API_TOKEN_NOT_VALID);
            tokenWrapper.setValidToken(false);
            return tokenWrapper;

        }

        return tokenWrapper;
    }

    public static SignFileSecurityDto readJsonFromRequest(HttpServletRequest req) {
        StringBuilder jb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            log.error("Error in readJsonFromRequest", e);
        }
        Gson gson = new Gson();
        LogHelper.debugInfo(log, "Json Received is " + jb);
        return gson.fromJson(jb.toString(), SignFileSecurityDto.class);
    }

    public void toSignState(RadiologyExamRequest selectedRequest,
                            List<Long> radExItemIds, String reportResult,
                            FileEntity fileEntity, Boolean isAggregation, Date reportDate, User user, Session session)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, AssignValidationException {
        generalLogic(selectedRequest, radExItemIds,
                WaitingListRegistrationStates.SIGNED, null, null,
                reportResult, fileEntity, isAggregation, reportDate, null, user, session);
    }

    private RadiologyExamRequest generalLogic(RadiologyExamRequest request,
                                              List<Long> itemsIds, WaitingListRegistrationStates state,
                                              Boolean reserveToday, String comment, String reportResult,
                                              FileEntity fileEntity, Boolean isAggregation, Date reportDate, Date performDate,
                                              User user, Session session)
            throws PersistenceBeanException,
            IllegalAccessException, HibernateException, InstantiationException, AssignValidationException, CloneNotSupportedException {
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
                                request.setReferringDoctor(user.getFullname());
                                break;

                            default:
                                break;
                        }
                    }
                    session.save(request);
                    String accessNumber = null;
                    Long cardNumber = null;

                    if (WaitingListRegistrationStates.ACCEPTED.equals(state)) {
                        accessNumber = getGeneratedAccessNumberId(session);
                        cardNumber = generateCardNumber(request.getExamType(), request.getSector(), session);
                    }
                    if (isFromComplete && WaitingListRegistrationStates.SIGNED.equals(state)) {
                        for (RadiologyExamRequestItem item : request.getRadiologyExamRequestItems()) {
                            item.setSignFileEntity(fileEntity);
                            item.setFileEntity(fileEntity);
                            item.setWaitingListRegistrationState(state);
                            session.save(item);
                        }
                    } else {
                        for (RadiologyExamRequestItem item : request.getRadiologyExamRequestItems()) {
                            setFieldsInRequestItem(state, comment, reportResult,
                                    item, request, user.getFullname(), reserveToday,
                                    fileEntity, accessNumber, cardNumber, reportDate, performDate, session);
                            item.setReportSaveDate(new Date());
                            item.setRadiologyExamRequest(request);
                            session.save(item);
                        }
                    }
                    if (WaitingListRegistrationStates.RESERVED.equals(state)) {
                        if (reserveToday) {
                            request.setLatestActionDate(new Date());
                            request.setReserveDate(new Date());
                            request.setAssignUserId(user.getId());
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
                    session.save(request);
                    if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())
                            && !WaitingListRegistrationStates.IN_READING.equals(request.getWaitingListRegistrationState())) {
                        saveHistoryForRequest(request, session);
                    }
                } else {
                    return splitRadiologyExamRequest(request, itemsIds, state,
                            reserveToday, comment, reportResult, fileEntity,
                           user.getFullname(), reportDate, session);
                }
            } else {
                Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair = getPairRequestsItems(itemsIds, session);
                if (!ValidationHelper.isNullOrEmpty(pair)) {
                    List<RadExamRequestWrapper> requestsWrappers = pair
                            .getFirst();
                    if (!ValidationHelper.isNullOrEmpty(requestsWrappers)) {
                        for (RadExamRequestWrapper rerw : requestsWrappers) {
                            RadiologyExamRequest localRequest = ConnectionManager.get(
                                    RadiologyExamRequest.class, rerw.getId(), session);
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
                                                        .setReferringDoctor(user.getFullname());
                                                break;

                                            default:
                                                break;
                                        }
                                    }
                                    session.save(localRequest);

                                    String accessNumber = null;
                                    Long cardNumber = null;

                                    if (WaitingListRegistrationStates.ACCEPTED
                                            .equals(state)) {
                                        accessNumber = getGeneratedAccessNumberId(session);
                                        cardNumber = generateCardNumber(
                                                localRequest.getExamType(),
                                                localRequest.getSector(), session);
                                    }
                                    for (RadiologyExamRequestItem item : localRequest
                                            .getRadiologyExamRequestItems()) {
                                        if (isFromComplete && WaitingListRegistrationStates.SIGNED.equals(state)) {
                                            item.setSignFileEntity(fileEntity);
                                            item.setFileEntity(fileEntity);
                                            item.setWaitingListRegistrationState(state);
                                            session.save(item);
                                        } else {
                                            setFieldsInRequestItem(state, comment,
                                                    reportResult, item, localRequest, user.getFullname(),
                                                    reserveToday, fileEntity,
                                                    accessNumber, cardNumber, reportDate, null, session);
                                            item.setRadiologyExamRequest(localRequest);
                                            session.save(item);
                                        }
                                    }
                                    if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())
                                            && !WaitingListRegistrationStates.IN_READING.equals(request.getWaitingListRegistrationState())) {
                                        saveHistoryForRequest(localRequest, session);
                                    }
                                } else {
                                    // aggregation with split
                                    return splitRadiologyExamRequest(localRequest,
                                            rerw.getItemsIds(), state,
                                            reserveToday, comment,
                                            reportResult, fileEntity,
                                            user.getFullname(), reportDate, session);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getGeneratedAccessNumberId(Session session) {
        Long currentYear = new Long(Calendar.getInstance().get(Calendar.YEAR));
        AccessNumberForYear accessNumberYear = null;
        try {
            accessNumberYear = ConnectionManager.get(AccessNumberForYear.class,
                    new Criterion[]{
                            Restrictions.eq("accessNumberYear", currentYear)
                    }, session);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in getGeneratedAccessNumberId", e);
        }

        if (accessNumberYear != null) {
            try {
                if (accessNumberYear.getAccessNumberId() == null) {
                    accessNumberYear.setAccessNumberId(getNumber(currentYear, session));
                } else {
                    accessNumberYear.setAccessNumberId(accessNumberYear
                            .getAccessNumberId().longValue() + 1);
                }

                session.save(accessNumberYear);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error in getGeneratedAccessNumberId", e);
            }

            return String.format("%06d", accessNumberYear.getAccessNumberId());
        } else {
            accessNumberYear = new AccessNumberForYear();
            accessNumberYear.setAccessNumberId(getNumber(currentYear, session));
            accessNumberYear.setAccessNumberYear(currentYear);

            try {
                session.save(accessNumberYear);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error in getGeneratedAccessNumberId", e);
            }

            return String.format("%06d", accessNumberYear.getAccessNumberId());
        }
    }

    private Long getNumber(Long currentYear, Session session) {
        Long number = null;
        try {
            String str = (String) session
                    .createQuery(
                            "select coalesce(max(accessNumberId) + '0',1) from RadiologyExamRequestItem where accessNumberYear = "
                                    + currentYear).uniqueResult();

            number = Long.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in getNumber", e);
        }

        if (number == null) {
            return new Long(1l);
        }

        return number.longValue() + 1;
    }

    private Long generateCardNumber(ExamType examType, Sector risSector, Session session)
            throws HibernateException {
        CardNumber cardNumber = null;

        try {
            cardNumber = ConnectionManager.get(CardNumber.class, new Criterion[]{
                    Restrictions.eq("examType.id", examType.getId()),
                    Restrictions.eq("risSector.id", risSector.getId())
            }, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in generateCardNumber", e);
        }

        if (cardNumber != null) {
            try {
                cardNumber
                        .setCardNumber(cardNumber.getCardNumber().longValue() + 1);

                session.save(cardNumber);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error in generateCardNumber", e);
            }
        } else {
            cardNumber = new CardNumber();
            cardNumber.setExamType(examType);
            cardNumber.setRisSector(risSector);
            cardNumber.setCardNumber(new Long(1));

            try {
                session.save(cardNumber);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error in generateCardNumber", e);
            }
        }

        return cardNumber.getCardNumber();
    }

    private void setFieldsInRequestItem(WaitingListRegistrationStates state,
                                        String comment, String reportResult, RadiologyExamRequestItem item,
                                        RadiologyExamRequest radiologyExamRequest, String currUserFullname, Boolean reserveToday,
                                        FileEntity fileEntity, String accessNumber, Long cardNumber,
                                        Date reportDate, Date performDate, Session session)
            throws IllegalAccessException, InstantiationException, HibernateException {
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

            deleteRegistrationForRequestItem(item, session);
        } else if (WaitingListRegistrationStates.ANNULLED.equals(state)) {
            item.setReserveDate(null);
            item.setFileEntity(fileEntity);
            item.setDeleteComment(comment);

            deleteRegistrationForRequestItem(item, session);
        }

        item.setWaitingListRegistrationState(state);

        if (WaitingListRegistrationStates.RESERVED.equals(state)
                || WaitingListRegistrationStates.REQUIRED.equals(state)) {
            eventCalendarLogic(state, item, reserveToday, session);
        }
    }

    private void deleteRegistrationForRequestItem(RadiologyExamRequestItem requestItem, Session session) {
        try {
            requestItem = ConnectionManager.get(RadiologyExamRequestItem.class, requestItem.getId(),session);
            if (requestItem != null) {
                if (!ValidationHelper.isNullOrEmpty(requestItem
                        .getEventCalendarRegistrations())) {
                    for (EventCalendarRegistration ecr : requestItem
                            .getEventCalendarRegistrations()) {
                        if (!ValidationHelper.isNullOrEmpty(ecr.getSlots())) {
                            for (EventCalendarRegistrationSlot ecrs : ecr
                                    .getSlots()) {
                                ConnectionManager.remove(ecrs, session);
                            }
                        }
                        ConnectionManager.remove(ecr, session);
                    }
                }

                requestItem.setReserveDate(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in deleteRegistrationForRequestItem", e);
        }
    }

    private void generateAccessNumber(RadiologyExamRequestItem requestItem,
                                      RadiologyExamRequest radiologyExamRequest, String accessNumber) throws HibernateException {
        requestItem.setAccessNumberCode(radiologyExamRequest.getSector().getAccNumPrefix());

        requestItem.setAccessNumberYear((long) Calendar.getInstance().get(Calendar.YEAR));

        requestItem.setAccessNumberId(accessNumber);
    }

    private void eventCalendarLogic(WaitingListRegistrationStates state,
                                    RadiologyExamRequestItem item, Boolean reserveToday, Session session)
            throws InstantiationException, IllegalAccessException, HibernateException {
        if (!WaitingListRegistrationStates.REQUIRED.equals(state)) {
            EventCalendarRegistration ecr = item.getEventCalendarRegistration();

            if (ecr != null && !ValidationHelper.isNullOrEmpty(ecr.getSlots())) {
                for (EventCalendarRegistrationSlot slot : ecr.getSlots()) {
                    ConnectionManager.remove(slot, session);
                }

                ecr.setSlots(null);
            }

            if (Boolean.FALSE.equals(reserveToday) && ecr != null) {
                ecr.setPackageId(UUID.randomUUID().toString());
                ecr.setRadiologyExamRequestItem(item);
                ConnectionManager.save(ecr, session);

                item.setReserveDate(ecr.getDate());

                ecr.setSlots(ecr.getSlotsToSave());
                if (!ValidationHelper.isNullOrEmpty(ecr.getSlots())) {
                    for (EventCalendarRegistrationSlot slot : ecr.getSlots()) {
                        ConnectionManager.save(slot, session);
                    }
                }
            } else {
                if (ecr == null) {
                    ecr = ConnectionManager.get(EventCalendarRegistration.class,
                            new Criterion[]{
                                    Restrictions.eq("radiologyExamRequestItem.id",
                                            item.getId())
                            }, session);
                }

                if (ecr != null) {
                    ConnectionManager.remove(ecr, session);
                }

                if (WaitingListRegistrationStates.RESERVED.equals(state)
                        && Boolean.TRUE.equals(reserveToday)) {
                    item.setWaitingListRegistrationState(WaitingListRegistrationStates.RESERVED);
                    item.setReserveDate(new Date());
                }
            }
        }
    }

    public void saveHistoryForRequest(RadiologyExamRequest request, Session session) {
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
                                    request, null, session, false);
                    break;

                case DELETED:
                case REQUIRED:
                case ANNULLED:
                    HistoricalReport historicalReport = ConnectionManager.get(
                            HistoricalReport.class, new Criterion[]{
                                    Restrictions
                                            .eq("radiologyExamRequest", request)
                            }, session);
                    if (historicalReport != null) {
                        ConnectionManager.remove(historicalReport, session);
                    }
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in saveHistoryForRequest", e);
        }
    }

    private RadiologyExamRequest splitRadiologyExamRequest(RadiologyExamRequest request,
                                                           List<Long> itemsIds, WaitingListRegistrationStates state,
                                                           Boolean reserveToday, String comment, String reportResult,
                                                           FileEntity fileEntity, String currUserFullName, Date reportDate,
                Session session)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, AssignValidationException {
        RadiologyExamRequest splittedRequest = request.clone();
        request.setUserClosingReportId(null);
        splittedRequest.setWaitingListRegistrationState(state);

        session.save(splittedRequest);

        List<Long> itemsToRemoveIds = new ArrayList<Long>();

        String accessNumber = null;
        Long cardNumber = null;

        if (WaitingListRegistrationStates.ACCEPTED.equals(state)) {
            accessNumber = getGeneratedAccessNumberId(session);
            cardNumber = generateCardNumber(request.getExamType(),
                    request.getSector(), session);
        }

        if (!ValidationHelper.isNullOrEmpty(request.getRadiologyExamRequestItems())) {
            for (RadiologyExamRequestItem item : request.getRadiologyExamRequestItems()) {
                if (itemsIds.contains(item.getId())) {
                    itemsToRemoveIds.add(item.getId());
                    item.setRadiologyExamRequest(splittedRequest);
                    setFieldsInRequestItem(state, comment, reportResult, item,
                            request, currUserFullName, reserveToday, fileEntity,
                            accessNumber, cardNumber, reportDate, null, session);

                    session.save(item);
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

        session.save(splittedRequest);
        session.save(request);

        WaitingListStatusHelper.logActivity(request.getId(), splittedRequest.getId());

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

        session.save(splittedRequest);
        session.save(request);

        refillExamsName(request, session);

        if (!WaitingListRegistrationStates.DRAFT.equals(request.getWaitingListRegistrationState())
                && !WaitingListRegistrationStates.IN_READING.equals(request.getWaitingListRegistrationState())) {
            saveHistoryForRequest(request, session);
        }
        if (!WaitingListRegistrationStates.DRAFT.equals(splittedRequest.getWaitingListRegistrationState())
                && !WaitingListRegistrationStates.IN_READING.equals(splittedRequest.getWaitingListRegistrationState())) {
            saveHistoryForRequest(splittedRequest, session);
        }
        return splittedRequest;
    }

    private void refillExamsName(RadiologyExamRequest request, Session session)
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

                    session.save(ecr);
                }
            }
        }
    }


    public static Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> getPairRequestsItems(
            List<Long> radExamReqIds, Session session) {
        Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair = null;
        if (!ValidationHelper.isNullOrEmpty(radExamReqIds)) {
            List<RadExamRequestItemWrapper> radExamRequestItemsWrappers = new ArrayList<>();
            List<RadExamRequestWrapper> radExamRequestsWrappers = new ArrayList<>();
            try {
                List<RadiologyExamRequestItem> requestItems = ConnectionManager.load(
                        RadiologyExamRequestItem.class, new Criterion[]{
                                Restrictions.in("id", radExamReqIds)
                        }, session);
                List<Long> reqIds = new ArrayList<>();
                if (!ValidationHelper.isNullOrEmpty(requestItems)) {
                    for (RadiologyExamRequestItem reri : requestItems) {
                        if (!reqIds.contains(reri.getRadiologyExamRequest()
                                .getId())) {
                            reqIds.add(reri.getRadiologyExamRequest().getId());
                        }
                    }
                    if (!ValidationHelper.isNullOrEmpty(reqIds)) {
                        List<RadiologyExamRequest> radExamRequests = ConnectionManager
                                .load(RadiologyExamRequest.class,
                                        new Criterion[]{
                                                Restrictions.in("id", reqIds)
                                        }, session);
                        if (!ValidationHelper.isNullOrEmpty(radExamRequests)) {
                            List<RadExamRequestWrapper> radiologyExamRequestsWrappers = new ArrayList<>();
                            for (RadiologyExamRequest rer : radExamRequests) {
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
                            radExamRequestsWrappers
                                    .addAll(radiologyExamRequestsWrappers);
                        }
                    }
                }
                pair = new Pair<>(
                        radExamRequestsWrappers, radExamRequestItemsWrappers);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error in getPairRequestsItems", e);
            }
        }
        return pair;
    }
}
