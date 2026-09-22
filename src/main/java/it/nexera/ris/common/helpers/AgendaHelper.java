package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.validation.AssignValidationException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.calendar.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.ExamType;
import it.nexera.ris.web.beans.wrappers.logic.AvailableRange;
import it.nexera.ris.web.beans.wrappers.logic.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.application.FacesMessage;
import java.util.*;

public class AgendaHelper extends BaseHelper {
    public transient final static Logger log = LogManager.getLogger(AgendaHelper.class);

    public static void goToWaitinglistRegistrationList() {
        RedirectHelper.goTo(PageTypes.WAITINGLIST_REGISTRATION_LIST);
    }

    public static void showErrorMessage() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                ResourcesHelper.getValidation("assignFailed"),
                ResourcesHelper.getValidation("notAssignedToTheSector"));
    }

    public static boolean checkInInterval(Date date1, Date date2,
                                          Date intervalStart, Date intervalEnd) {
        return DateTimeHelper.getDateDiffInMin(date1, intervalStart) >= 0
                && date1.before(intervalEnd) && date2.after(intervalStart)
                && DateTimeHelper.getDateDiffInMin(date1, intervalStart) <= 0;
    }

    public static List<EventCalendarRegistrationShort> loadListRegistrations(
            Long weekdayId, Date date, List<EventCalendarRegistrationShort> totalList) {
        List<EventCalendarRegistrationShort> retList = null;
        try {
            if (totalList != null && date != null) {
                for (EventCalendarRegistrationShort item : totalList) {
                    if (item.getEventCalendarWeekday() != null
                            && item.getDate() != null
                            && item.getEventCalendarWeekday().getId()
                            .equals(weekdayId)
                            && DateTimeHelper.getDate(item.getDate())
                            .equals(DateTimeHelper.getDate(date))) {
                        if (retList == null) {
                            retList = new ArrayList<EventCalendarRegistrationShort>();
                        }

                        retList.add(item);
                    }
                }
            } else {
                return totalList;
            }

        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return retList;
    }

    public static Integer getSlotNumber(String slotStr) {
        Integer res = null;

        try {
            if (slotStr.contains("overbook_")) {
                slotStr = slotStr.substring("overbook_".length());
            }

            res = Integer.parseInt(slotStr);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return res;
    }

    public static void internalButtonAction(RadiologyExamRequest radExReq, Long userId)
            throws HibernateException, PersistenceBeanException,
            CloneNotSupportedException, IllegalAccessException,
            InstantiationException, AssignValidationException {
        WaitingListStatusHelper wlsh = new WaitingListStatusHelper();
        List<Long> radExItemIds = null;

        if (radExReq != null) {
            radExReq.setAssignDate(new Date());
            radExReq.setAssignUserId(userId);
        }

        if (getSelectedToTable(radExReq) != null
                && getSelectedToTable(radExReq)[0].getSelectedRadItemsList() != null) {
            radExItemIds = getSelectedIds(getSelectedToTable(radExReq)[0]
                    .getSelectedRadItemsList());

            wlsh.toReservedState(radExReq, radExItemIds, Boolean.FALSE);
        } else if (radExReq != null
                && !ValidationHelper.isNullOrEmpty(radExReq
                .getRadiologyExamRequestItems())) {
            radExItemIds = getSelectedIds(radExReq
                    .getRadiologyExamRequestItems());

            wlsh.toReservedState(radExReq, radExItemIds, Boolean.FALSE);
        }
    }

    public static RadiologyExamRequest[] getSelectedToTable(
            RadiologyExamRequest radExReq) {
        if (radExReq != null) {
            return new RadiologyExamRequest[]{
                    radExReq
            };
        }

        return null;
    }

    public static List<Long> getSelectedIds(List<RadiologyExamRequestItem> reris) {
        List<Long> radExItemIds = new ArrayList<Long>();

        if (!ValidationHelper.isNullOrEmpty(reris)) {
            for (RadiologyExamRequestItem wlrf : reris) {
                if (Boolean.TRUE.equals(wlrf.getSelected())) {
                    radExItemIds.add(wlrf.getId());
                }
            }
        }

        return radExItemIds;
    }

    public static boolean validateRanges(Date date,
                                         EventCalendarWeekDay weekday, Date startDate, Date endDate,
                                         EventCalendarRegistration registration,
                                         RadiologyExamRequest radExReq) throws HibernateException,
            PersistenceBeanException {
        Calendar c = Calendar.getInstance();

        c.setTime(date);

        if (startDate.compareTo(endDate) >= 0) {
            return false;
        }

        for (AvailableRange range : getRanges(c, weekday, registration,
                radExReq)) {
            if (startDate.compareTo(range.getStartDate()) >= 0
                    && endDate.compareTo(range.getEndDate()) <= 0) {
                return true;
            }
        }

        return false;
    }

    public static List<AvailableRange> getRanges(Calendar c,
                                                 EventCalendarWeekDay weekday, EventCalendarRegistration reg,
                                                 RadiologyExamRequest radExReq) throws HibernateException,
            PersistenceBeanException {
        Calendar c1 = Calendar.getInstance();

        c1.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                DateTimeHelper.getHour(weekday.getStartTimeStr()),
                DateTimeHelper.getMinute(weekday.getStartTimeStr()), 0);
        c1.set(Calendar.MILLISECOND, 0);

        Calendar c2 = Calendar.getInstance();

        c2.set(c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                DateTimeHelper.getHour(weekday.getEndTimeStr()),
                weekday.getOverbooking() ? (DateTimeHelper.getMinute(weekday
                        .getEndTimeStr()) + (ValidationHelper
                        .isNullOrEmpty(radExReq) ? 60 : weekday
                        .getAverageDuration().intValue())) : DateTimeHelper
                        .getMinute(weekday.getEndTimeStr()), 0);
        c2.set(Calendar.MILLISECOND, 0);

        List<AvailableRange> ranges = new ArrayList<AvailableRange>();
        List<EventCalendarRegistration> registrations = getRegistrationsForDynamicDay(
                weekday, c1, c2, true);

        if (reg != null) {
            registrations = getRegistrationsNotChanges(registrations, reg);
        }

        if (registrations != null && !registrations.isEmpty()) {
            for (EventCalendarRegistration registration : registrations) {
                if (registration.getFromDate().compareTo(c1.getTime()) != 0) {
                    ranges.add(new AvailableRange(c1.getTime(), registration
                            .getFromDate(), DateTimeHelper.getDateDiffInMin(
                            registration.getFromDate(), c1.getTime())));
                }

                c1.setTime(registration.getToDate());
            }

            if (c2.getTime().compareTo(c1.getTime()) != 0) {
                ranges.add(new AvailableRange(c1.getTime(), c2.getTime(),
                        DateTimeHelper.getDateDiffInMin(c2.getTime(),
                                c1.getTime())));
            }
        } else {
            ranges.add(new AvailableRange(c1.getTime(), c2.getTime(),
                    DateTimeHelper.getDateDiffInMin(c2.getTime(), c1.getTime())));
        }

        return ranges;
    }

    private static List<EventCalendarRegistration> getRegistrationsNotChanges(
            List<EventCalendarRegistration> registrations,
            EventCalendarRegistration registrationToChange) {
        List<EventCalendarRegistration> regNotChanges = new ArrayList<>();

        for (EventCalendarRegistration r : registrations) {
            if (r.getId().equals(registrationToChange.getId())) {
                regNotChanges.add(r);
            }
        }

        return regNotChanges;
    }

    public static List<EventCalendarRegistration> getRegistrationsForDynamicDay(
            EventCalendar ec, Date c1, Date c2) {
        List<EventCalendarRegistration> ecrList = null;
        List<EventCalendarRegistration> evencrList = new ArrayList<EventCalendarRegistration>();
        List<Long> ids = new ArrayList<Long>();

        try {
            ecrList = DaoManager
                    .load(EventCalendarRegistration.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("eventCalendarWeekday",
                                            "ecw", JoinType.LEFT_OUTER_JOIN)
                            },
                            new Criterion[]{
                                    Restrictions.eq("ecw.eventCalendar.id",
                                            ec.getId()),
                                    Restrictions.isNotNull("fromDate"),
                                    Restrictions.isNotNull("toDate"),
                                    Restrictions.between("fromDate",
                                            DateTimeHelper.getDayStart(c1),
                                            DateTimeHelper.getDayEnd(c2)),
                                    Restrictions.between("toDate",
                                            DateTimeHelper.getDayStart(c1),
                                            DateTimeHelper.getDayEnd(c2))
                            }, Order.asc("fromDate"));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (!ValidationHelper.isNullOrEmpty(ecrList)) {
            for (EventCalendarRegistration cal : ecrList) {
                if (!ids.contains(cal.getRadiologyExamRequestItem()
                        .getRadiologyExamRequest().getId())) {
                    ids.add(cal.getRadiologyExamRequestItem()
                            .getRadiologyExamRequest().getId());
                    evencrList.add(cal);
                }
            }
        }

        return evencrList;
    }

    public static List<EventCalendarRegistration> getRegistrationsForDynamicDay(
            EventCalendarWeekDay weekday, Calendar c1, Calendar c2,
            boolean unique) {
        List<EventCalendarRegistration> registrations = null;
        List<EventCalendarRegistration> ecrList = null;

        if (weekday != null) {
            try {
                ecrList = DaoManager.load(
                        EventCalendarRegistration.class,
                        new Criterion[]{
                                Restrictions.eq("eventCalendarWeekday.id",
                                        weekday.getId()),
                                Restrictions.isNotNull("fromDate"),
                                Restrictions.isNotNull("toDate"),
                                Restrictions.between("fromDate", c1.getTime(),
                                        c2.getTime()),
                                Restrictions.between("toDate", c1.getTime(),
                                        c2.getTime())
                        }, Order.asc("fromDate"));
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        if (ecrList != null) {
            if (unique) {
                List<String> ids = new ArrayList<String>();
                registrations = new ArrayList<EventCalendarRegistration>();

                for (EventCalendarRegistration ecr : ecrList) {
                    if (!ids.contains(ecr.getPackageId())) {
                        ids.add(ecr.getPackageId());
                        registrations.add(ecr);
                    }
                }
            } else {
                return ecrList;
            }
        }

        return registrations;
    }

    public static List<EventCalendarRegistration> getRegistrationsForDynamicDayWeekView(
            EventCalendarWeekDay weekday, Calendar c1, Calendar c2,
            List<EventCalendarRegistration> totalList) {
        List<EventCalendarRegistration> registrations = null;

        if (totalList != null) {
            registrations = new ArrayList<EventCalendarRegistration>();

            for (EventCalendarRegistration item : totalList) {
                if (item.getEventCalendarWeekday().getId()
                        .equals(weekday.getId())
                        && DateTimeHelper.between(item.getFromDate(),
                        c1.getTime(), c2.getTime())
                        && DateTimeHelper.between(item.getToDate(),
                        c1.getTime(), c2.getTime())) {
                    registrations.add(item);
                }
            }
        }

        return registrations;
    }

    public static List<EventCalendarRegistration> getRegistrationsForDynamicDayMonthView(
            EventCalendarWeekDay weekday, Calendar c1, Calendar c2,
            List<EventCalendarRegistration> totalList) {
        List<EventCalendarRegistration> registrations = null;

        if (totalList != null) {
            List<String> ids = new ArrayList<String>();

            registrations = new ArrayList<EventCalendarRegistration>();

            for (EventCalendarRegistration item : totalList) {
                if (!ids.contains(item.getPackageId())
                        && item.getEventCalendarWeekday().getId()
                        .equals(weekday.getId())
                        && DateTimeHelper.between(item.getFromDate(),
                        c1.getTime(), c2.getTime())
                        && DateTimeHelper.between(item.getToDate(),
                        c1.getTime(), c2.getTime())) {
                    ids.add(item.getPackageId());
                    registrations.add(item);
                }
            }
        }

        return registrations;
    }

    public static void populateDynamicEventsForMonthView(List<Event> list,
                                                         EventCalendar ec, Calendar c, EventCalendarWeekDay weekday,
                                                         List<EventCalendarRegistration> totalList, boolean beforeToday,
                                                         boolean isOldCalendar, RadiologyExamRequest radExReq)
            throws PersistenceBeanException {
        Calendar c1 = Calendar.getInstance();

        c1.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                DateTimeHelper.getHour(weekday.getStartTimeStr()),
                DateTimeHelper.getMinute(weekday.getStartTimeStr()), 0);
        c1.set(Calendar.MILLISECOND, 0);

        Calendar c2 = Calendar.getInstance();

        c2.set(c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                DateTimeHelper.getHour(weekday.getEndTimeStr()),
                weekday.getOverbooking() ? (DateTimeHelper.getMinute(weekday
                        .getEndTimeStr()) + (ValidationHelper
                        .isNullOrEmpty(radExReq) ? 60 : weekday
                        .getAverageDuration().intValue())) : DateTimeHelper
                        .getMinute(weekday.getEndTimeStr()), 0);
        c2.set(Calendar.MILLISECOND, 0);

        Event item = EventCalendar.copy(ec);

        item.setId(ListHelper.getMaxId(list) + 1);
        item.setStartDate(c1.getTime());
        item.setEndDate(c1.getTime());
        item.setAllDay(true);
        item.setOverbookingAvailable(weekday.getOverbooking().booleanValue());

        List<EventCalendarRegistration> registrations = getRegistrationsForDynamicDayMonthView(
                weekday, c1, c2, totalList);

        if (registrations == null) {
            registrations = new ArrayList<EventCalendarRegistration>();
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%s - %s", weekday.getStartTimeStr(),
                weekday.getEndTimeStr()));
        sb.append(String.format(" (%d %s)", registrations.size(),
                ResourcesHelper.getString("dateConfirmationAlreadyAssigned")));

        item.setName(sb.toString());

        List<AvailableRange> ranges = getRanges(c, weekday, null, radExReq);

        item.setFull(isOldCalendar || beforeToday);
        item.setBusy(false);

        if (ranges.isEmpty()) {
            item.setFull(true);
        } else if (ranges.size() > 1) {
            item.setBusy(true);
        } else if (ranges.size() == 1) {
            item.setBusy(!(ranges.get(0).getFromTime()
                    .equals(weekday.getStartTimeStr()) && ranges.get(0)
                    .getToTime().equals(weekday.getEndTimeStr())));
        }

        if (!(isOldCalendar || beforeToday) || item.isBusy()) {
            list.add(item);
        }
    }

    public static boolean addRegistrationsToList(List<Event> list,
                                                 List<EventCalendarRegistrationShort> registrations)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        boolean empty = true;

        for (EventCalendarRegistrationShort reg : registrations) {
            boolean present = false;
            for (Event item : list) {
                if (item.getRadiologyExamRequestItemId() != null
                        && item.getRadiologyExamRequestItemId()
                        .equals(reg.getRadiologyExamRequestItemId())) {
                    present = true;
                    break;
                }
            }

            if (!present) {
                if (reg.getRadiologyExamRequestItemId() != null) {
                    Event event = new Event();
                    event.setRadiologyExamRequestItemId(reg
                            .getRadiologyExamRequestItemId());
                    event.setId(ListHelper.getMaxId(list) + 1);
                    list.add(event);
                    empty = false;
                }
            }
        }

        return !empty;
    }

    public static void populateSlotEventsForWeekView(List<Event> list,
                                                     EventCalendar ec, Calendar c, EventCalendarWeekDay weekday,
                                                     boolean showTakenSlots, boolean isDayView, boolean forWorkingList,
                                                     boolean forPatientLoading,
                                                     List<EventCalendarRegistrationShort> totalList, boolean beforeToday,
                                                     boolean oldCalendar) throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        int slotLength = getSlotLength(weekday.getStartTimeStr(),
                weekday.getEndTimeStr(), weekday.getSlotNumber());

        Calendar c1 = Calendar.getInstance();

        c1.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                DateTimeHelper.getHour(weekday.getStartTimeStr()),
                DateTimeHelper.getMinute(weekday.getStartTimeStr()), 0);

        List<EventCalendarRegistrationShort> listRegs = loadListRegistrations(
                weekday.getId(), DateTimeHelper.getDate(c1.getTime()),
                totalList);

        addEventsToSlotList(list, listRegs, ec, c1, weekday, slotLength,
                showTakenSlots, isDayView, forWorkingList, forPatientLoading,
                beforeToday, oldCalendar, false);

        if (weekday != null && Boolean.TRUE.equals(weekday.getOverbooking())) {
            addEventsToSlotList(list, listRegs, ec, c1, weekday, slotLength,
                    showTakenSlots, isDayView, forWorkingList,
                    forPatientLoading, beforeToday, oldCalendar, true);
        }
    }

    private static List<CountSlot> getCountSlots(int max, List<EventCalendarRegistrationShort> listRegs) throws PersistenceBeanException, IllegalAccessException {
        List<CountSlot> resultList = new ArrayList<>();
        if(ValidationHelper.isNullOrEmpty(listRegs)){
            return resultList;
        }
        StringBuilder slotNum = new StringBuilder();
        StringBuilder eventIds = new StringBuilder();
        for (int i = 1; i <= max; i++) {
            slotNum.append(i);
            if (i < max) {
                slotNum.append(",");
            }
        }
        Iterator<EventCalendarRegistrationShort> iterator = listRegs.iterator();
        while (iterator.hasNext()){
            eventIds.append(iterator.next().getId());
            if(iterator.hasNext()){
                eventIds.append(",");
            }
        }
        resultList = DaoManager.getSession().createSQLQuery(String.format("select SLOT_NUMBER, OVERBOOKED, count(id) as " +
                "id_count, EVENT_CALENDAR_REGISTRATION_ID as event_calendar_id\n" +
                "from event_calendar_reg_slot\n" +
                "where SLOT_NUMBER in (%s)\n" +
                "  and EVENT_CALENDAR_REGISTRATION_ID in (%s)\n" +
                "group by SLOT_NUMBER, OVERBOOKED, EVENT_CALENDAR_REGISTRATION_ID", slotNum, eventIds)).addEntity(CountSlot.class).list();
        return resultList;
    }

    private static void addEventsToSlotList(List<Event> list,
                                            List<EventCalendarRegistrationShort> listRegs, EventCalendar ec,
                                            Calendar c1, EventCalendarWeekDay weekday, int slotLength,
                                            boolean showTakenSlots, boolean isDayView, boolean forWorkingList,
                                            boolean forPatientLoading, boolean beforeToday,
                                            boolean oldCalendar, boolean isOverBook) throws HibernateException,
            InstantiationException, IllegalAccessException,
            PersistenceBeanException {
        int max = isOverBook ? weekday.getMaximumOverbooking().intValue()
                : weekday.getSlotNumber().intValue();

        Map<Integer, EventCalendarRegistrationShort> registrationMap = null;
        List<CountSlot> countSlotList = getCountSlots(max, listRegs);
        for (int i = 0; i < max; i++) {
            Event item = null;

            item = EventCalendar.copy(ec);

            item.setId(ListHelper.getMaxId(list) + 1);
            item.setOverbooked(isOverBook);
            item.setStartDate(c1.getTime());
            c1.add(Calendar.MINUTE, slotLength);
            item.setEndDate(c1.getTime());

            EventCalendarRegistrationShort registration = null;

            if (forPatientLoading) {
                List<EventCalendarRegistrationShort> registrations = checkSlotPresenceList(
                        listRegs, i + 1, isOverBook);
                if (!ValidationHelper.isNullOrEmpty(registrations)) {
                    if (addRegistrationsToList(list, registrations)) {
                        // return;
                    }
                }
            } else {
                //registration = checkSlotPresence(listRegs, i + 1, isOverBook);
                if (registrationMap == null) {
                    registrationMap = buildRegistrationMap(listRegs,
                            isOverBook);
                }

                registration = registrationMap.get(new Integer(i + 1));
            }

            item.setAllDay(false);
            item.setSlotNumber(i + 1);

            if (registration != null) {
                if (registration.getRadiologyExamRequestItemId() != null) {
                    // if (forWorkingList)
                    // {
                    item.setRadiologyExamRequestItemId(registration
                            .getRadiologyExamRequestItemId());
                    // }

                    StringBuffer sb = new StringBuffer();

                    sb.append(registration.getPatientSurnameName(c1));

                    if (!ValidationHelper.isNullOrEmpty(
                            registration.getAsapSectorDescription(c1))) {
                        sb.append("; ");
                        sb.append(ResourcesHelper
                                .getString("dateConfirmationSector"));
                        sb.append(": ");
                        if (!ValidationHelper.isNullOrEmpty(registration.getAsapActivityLine(c1))) {
                            sb.append(registration.getAsapActivityLine(c1)).append(" - ");
                        }
                        sb.append(registration.getAsapSectorDescription(c1));
                    }

                    sb.append(". ");

                    StringBuffer sb1 = new StringBuffer();

                    boolean addDivider = false;
                    int ctr = 0;

                    for (EventCalendarRegistrationShort reg : listRegs) {
                        if (reg.getSlots() != null
                                && reg.getRadiologyExamRequestItemId() != null
                                && !ValidationHelper.isNullOrEmpty(reg.getRadExamDescription(c1))) {
                            CountSlot countSlot =  new CountSlot(reg.getId(), item.getSlotNumber(), item.isOverbooked());
                            if(countSlotList.contains(countSlot) && countSlotList.get(countSlotList.indexOf(countSlot)).getCountSlot() > 0) {
                                if (addDivider) {
                                    sb1.append(", ");
                                } else {
                                    addDivider = true;
                                }

                                sb1.append(reg.getRadExamDescription(c1));
                                ctr++;
                            }
                        }
                    }

                    sb.append(ResourcesHelper
                            .getString(ctr > 1 ? "dateConfirmationExams"
                                    : "dateConfirmationExam"));
                    sb.append(": ");
                    sb.append(sb1);

                    if (forWorkingList) {
                        item.setPatientName(registration
                                .getPatientSurnameName(c1));
                    } else {
                        item.setPatientName(sb.toString());
                    }
                    item.setExams(sb1.toString());

                    item.setPatientWeek(String.format("%s",
                            registration.getPatientSurnameName(c1)));
                } else {
                    item.setPatientChangeStatus(true);

                    if (!ValidationHelper.isNullOrEmpty(registration
                            .getPatientSurnameName(c1))) {
                        if (!forWorkingList) {
                            StringBuffer sb = new StringBuffer();
                            sb.append(registration.getPatientSurnameName(c1));
                            if (!ValidationHelper
                                    .isNullOrEmpty(registration
                                            .getSectorName())) {
                                sb.append("; ");
                                sb.append(ResourcesHelper
                                        .getString("dateConfirmationSector"));
                                sb.append(": ");
                                sb.append(registration.getSectorName());
                            }

                            sb.append(". ");

                            if (!ValidationHelper
                                    .isNullOrEmpty(registration
                                            .getExamsName())) {
                                sb.append(ResourcesHelper
                                        .getString(registration
                                                .getExamsName().indexOf(
                                                        ", ") > -1 ? "dateConfirmationExams"
                                                : "dateConfirmationExam"));
                                sb.append(": ");
                                sb.append(registration.getExamsName());
                            }

                            item.setPatientName(sb.toString());
                        } else {
                            item.setPatientName(registration
                                    .getPatientSurnameName(c1));
                        }

                        item.setExams(registration.getExamsName());

                        item.setPatientWeek(registration
                                .getPatientSurnameName(c1));
                    } else {
                        item.setPatientWeek(ResourcesHelper
                                .getString("patientMovedFromSystem"));
                        item.setPatientName(ResourcesHelper
                                .getString("patientMovedFromSystem"));
                    }
                }
                if (registration.getRadiologyExamRequestItemId() != null && Boolean.TRUE.equals(registration.getForwarded(c1))) {
                    item.setForwarded(Boolean.TRUE);
                }
                //In case of forwarded request assigned on the same slot
                checkDoubleSlot(list,
                        listRegs, ec,
                        c1, weekday, slotLength,
                        showTakenSlots, isDayView, forWorkingList,
                        forPatientLoading, beforeToday,
                        oldCalendar, isOverBook, i + 1, item, registration);
            }

            if ((showTakenSlots)
                    || ValidationHelper.isNullOrEmpty(item.getPatientName())) {
                // combine expression
                if (!ValidationHelper.isNullOrEmpty(item.getPatientName())
                        || !(oldCalendar || beforeToday)) {
                    list.add(item);
                }
            }
        }
    }

    private static <T extends EventCalendarRegistrationBase> Map<Integer, T> buildRegistrationMap(
            List<T> listReg, boolean overbooked) {
        Map<Integer, T> registrationMap = new HashMap<Integer, T>();

        if (listReg != null) {
            try {
                List<Object> ids = DaoManager.getFields(
                        EventCalendarRegistrationSlot.class, new Criterion[]{
                                Restrictions.in("registration.id",
                                        getIds(listReg)),
                                Restrictions.eq("overbooked", overbooked)
                        }, null, true, "registration.id", "slotNumber");

                if (!ValidationHelper.isNullOrEmpty(ids)) {
                    for (T item : listReg) {
                        Integer slotNumber = getSlotNumber(ids, item);

                        if (slotNumber != null) {
                            item.setSlotNumber(slotNumber);
                            item.setContainsSlot(Boolean.TRUE);

                            if (!registrationMap.containsKey(slotNumber)) {
                                registrationMap.put(slotNumber, item);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return registrationMap;
    }

    private static <T extends EventCalendarRegistrationBase> Integer getSlotNumber(
            List<Object> objects, T item) {
        if (!ValidationHelper.isNullOrEmpty(objects) && item != null) {
            for (Object object : objects) {
                if (((Object[]) object)[0] != null
                        && ((Object[]) object)[0].equals(item.getId())) {
                    return (Integer) ((Object[]) object)[1];
                }
            }
        }

        return null;
    }

    public static void checkDoubleSlot(List<Event> list,
                                       List<EventCalendarRegistrationShort> listRegs, EventCalendar ec,
                                       Calendar c1, EventCalendarWeekDay weekday, int slotLength,
                                       boolean showTakenSlots, boolean isDayView, boolean forWorkingList,
                                       boolean forPatientLoading, boolean beforeToday, boolean oldCalendar,
                                       boolean isOverBook, Integer slot, Event anotherEvent,
                                       EventCalendarRegistrationShort anotherRegistration) {
        try {
            EventCalendarRegistrationShort registration = null;

            for (EventCalendarRegistrationShort item : listRegs) {
                if (!item.getRadiologyExamRequestId(c1)
                        .equals(anotherRegistration.getRadiologyExamRequestId(c1))
                        && (Boolean.TRUE.equals(item.getContainsSlot())
                        && slot != null
                        && slot.equals(item.getSlotNumber()))) {
                    registration = item;
                    break;
                }
            }

            if (registration != null) {
                Event item = EventCalendar.copy(ec);
                item.setAllDay(false);
                item.setId(anotherEvent.getId() + 1);
                item.setStartDate(anotherEvent.getStartDate());
                item.setEndDate(anotherEvent.getEndDate());
                item.setOverbooked(isOverBook);
                item.setAllDay(false);
                item.setSlotNumber(slot);
                if (registration.getRadiologyExamRequestItemId() != null) {
                    //  if (forWorkingList)
                    //   {
                    item.setRadiologyExamRequestItemId(
                            registration.getRadiologyExamRequestItemId());
                    //   }

                    if (isDayView) {
                        StringBuffer sb = new StringBuffer();

                        sb.append(registration.getPatientFullname(c1));

                        if (!ValidationHelper.isNullOrEmpty(
                                registration.getAsapSectorDescription(c1))) {
                            sb.append("; ");
                            sb.append(ResourcesHelper
                                    .getString("dateConfirmationSector"));
                            sb.append(": ");
                            if (!ValidationHelper.isNullOrEmpty(registration.getAsapActivityLine(c1))) {
                                sb.append(registration.getAsapActivityLine(c1)).append(" - ");
                            }
                            sb.append(registration.getAsapSectorDescription(c1));
                        }

                        sb.append(". ");

                        StringBuffer sb1 = new StringBuffer();

                        boolean addDivider = false;
                        int ctr = 0;

                        for (EventCalendarRegistrationShort reg : listRegs) {
                            if (reg.getSlots() != null
                                    && reg.getRadiologyExamRequestItemId() != null
                                    && !ValidationHelper.isNullOrEmpty(
                                    reg.getRadExamDescription(c1))
                                    && reg.containsSlot(item.getSlotNumber(),
                                    item.isOverbooked())) {
                                if (addDivider) {
                                    sb1.append(", ");
                                } else {
                                    addDivider = true;
                                }

                                sb1.append(reg.getRadExamDescription(c1));
                                ctr++;
                            }
                        }

                        sb.append(ResourcesHelper
                                .getString(ctr > 1 ? "dateConfirmationExams"
                                        : "dateConfirmationExam"));
                        sb.append(": ");
                        sb.append(sb1);

                        if (forWorkingList) {
                            item.setPatientName(
                                    registration.getPatientFullName());
                            item.setExams(sb1.toString());
                        } else {
                            item.setPatientName(sb.toString());
                        }
                    } else {
                        item.setPatientName(String.format("%s",
                                registration.getPatientFullname(c1)));
                    }
                } else {
                    item.setPatientChangeStatus(true);

                    if (!ValidationHelper
                            .isNullOrEmpty(registration.getPatientFullName())) {
                        if (isDayView) {
                            if (!forWorkingList) {
                                StringBuffer sb = new StringBuffer();
                                sb.append(registration.getPatientFullName());
                                if (!ValidationHelper.isNullOrEmpty(
                                        registration.getSectorName())) {
                                    sb.append("; ");
                                    sb.append(ResourcesHelper.getString(
                                            "dateConfirmationSector"));
                                    sb.append(": ");
                                    sb.append(registration.getSectorName());
                                }

                                sb.append(". ");

                                if (!ValidationHelper.isNullOrEmpty(
                                        registration.getExamsName())) {
                                    sb.append(ResourcesHelper.getString(
                                            registration.getExamsName()
                                                    .indexOf(", ") > -1
                                                    ? "dateConfirmationExams"
                                                    : "dateConfirmationExam"));
                                    sb.append(": ");
                                    sb.append(registration.getExamsName());
                                }

                                item.setPatientName(sb.toString());
                            } else {
                                item.setPatientName(
                                        registration.getPatientFullName());
                                item.setExams(registration.getExamsName());
                            }
                        } else {
                            item.setPatientName(
                                    registration.getPatientFullName());
                        }
                    } else {
                        item.setPatientName(ResourcesHelper
                                .getString("patientMovedFromSystem"));
                    }
                }
                item.setPatientWeek(item.getPatientName());
                if (registration.getRadiologyExamRequestItemId() != null
                        && Boolean.TRUE.equals(registration.getForwarded(c1))) {
                    item.setForwarded(Boolean.TRUE);
                }
                if ((showTakenSlots) || ValidationHelper
                        .isNullOrEmpty(item.getPatientName())) {
                    // combine expression
                    if (!ValidationHelper.isNullOrEmpty(item.getPatientName())
                            || !(oldCalendar || beforeToday)) {
                        list.add(item);
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static int getSlotLength(String time1, String time2,
                                    Integer slotNumber) {
        return DateTimeHelper.getTimeDiff(time1, time2) / slotNumber.intValue();
    }

    private static List<EventCalendarRegistrationShort> checkSlotPresenceList(
            List<EventCalendarRegistrationShort> list, Integer slotNumber,
            boolean overbooked) {
        List<EventCalendarRegistrationShort> retList = new ArrayList<EventCalendarRegistrationShort>();

        if (list != null) {
            for (EventCalendarRegistrationShort item : list) {
                if (item.containsSlot(slotNumber, overbooked)) {
                    retList.add(item);
                }
            }
        }

        return retList;
    }

    public static <T extends EventCalendarRegistrationBase> boolean checkSlotPresence(Class<T> bean,
                                                                                      List<Long> list, Integer slotNumber, boolean overbooked) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(list)) {
            return DaoManager.getCount(bean, "id",
                    new CriteriaAlias[]{
                            new CriteriaAlias("slots", "slots", JoinType.INNER_JOIN)
                    },
                    new Criterion[]{
                            Restrictions.eq("slots.slotNumber", slotNumber),
                            Restrictions.eq("slots.overbooked", overbooked),
                            Restrictions.in("id", list)
                    }) > 0L;

        }
        return false;
    }

    public static void populateSlotEventsForMonthView(List<Event> list,
                                                      EventCalendar ec, Calendar c, EventCalendarWeekDay weekday,
                                                      List<EventCalendarRegistrationShort> totalList, boolean beforeToday,
                                                      boolean oldCalendar) throws HibernateException,
            PersistenceBeanException {
        Calendar c1 = Calendar.getInstance();

        c1.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                DateTimeHelper.getHour(weekday.getStartTimeStr()),
                DateTimeHelper.getMinute(weekday.getStartTimeStr()), 0);

        Calendar c2 = Calendar.getInstance();

        c2.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                DateTimeHelper.getHour(weekday.getEndTimeStr()),
                DateTimeHelper.getMinute(weekday.getEndTimeStr()), 0);

        Event item = EventCalendar.copy(ec);

        item.setId(ListHelper.getMaxId(list) + 1);
        item.setStartDate(c1.getTime());
        item.setEndDate(c1.getTime());
        item.setAllDay(true);
        item.setOverbookingAvailable(Boolean.TRUE.equals(weekday
                .getOverbooking()));

        List<EventCalendarRegistrationShort> registrations = getRegistrationsForSlotDay(
                weekday, c1, totalList);

        if (registrations == null) {
            registrations = new ArrayList<EventCalendarRegistrationShort>();
        }

        StringBuilder sb = new StringBuilder();
        int avail = weekday.getOverbooking().booleanValue() ? weekday
                .getSlotNumber().intValue()
                + weekday.getMaximumOverbooking().intValue() : weekday
                .getSlotNumber().intValue();
        int assign = 0;

        for (EventCalendarRegistrationShort reg : registrations) {
            if (reg.getSlots() != null) {
                assign += reg.getSlots().size();
            }
        }

        sb.append(String.format(
                ResourcesHelper.getString("dateConfirmationAvailableSlots"),
                assign, avail));

        item.setFull((oldCalendar || beforeToday) || avail == assign);
        item.setBusy(assign > 0);
        item.setName(sb.toString());

        if (!(oldCalendar || beforeToday) || item.isBusy()) {
            list.add(item);
        }
    }

    public static List<EventCalendarRegistrationShort> getRegistrationsForSlotDay(
            EventCalendarWeekDay weekday, Calendar c1,
            List<EventCalendarRegistrationShort> totalList) {
        List<EventCalendarRegistrationShort> registrations = new ArrayList<EventCalendarRegistrationShort>();

        if (totalList != null) {
            List<String> ids = new ArrayList<String>();

            for (EventCalendarRegistrationShort item : totalList) {
                if (!ids.contains(item.getPackageId())
                        && item.getEventCalendarWeekday().getId()
                        .equals(weekday.getId())
                        && DateTimeHelper.getDate(item.getDate()).equals(
                        DateTimeHelper.getDate(c1.getTime()))) {
                    ids.add(item.getPackageId());
                    registrations.add(item);
                }
            }
        }

        return registrations;
    }

    public static List<EventCalendarRegistrationShort> getRegistrationsForSlotDay(
            EventCalendar ec, Date c1, Date c2) {
        List<EventCalendarRegistrationShort> ecrList = null;

        try {
            ecrList = DaoManager
                    .load(EventCalendarRegistrationShort.class,
                            new CriteriaAlias("eventCalendarWeekday", "ecw",
                                    JoinType.LEFT_OUTER_JOIN),
                            new Criterion[]{
                                    Restrictions.eq("ecw.eventCalendar.id",
                                            ec.getId()),
                                    Restrictions.between("date",
                                            DateTimeHelper.getDayStart(c1),
                                            DateTimeHelper.getDayEnd(c2))
                            });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return ecrList;
    }

    public static Integer getMinTimeByCalendars(Long calendarId)
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        Integer currentMinTime = null;

        if (calendarId != null) {
            EventCalendar current = DaoManager.get(EventCalendar.class,
                    calendarId);

            if (current != null && current.getWeekDays() != null
                    && current.getWeekDays().size() > 0) {
                for (EventCalendarWeekDay weekDay : current.getWeekDays()) {
                    if (!ValidationHelper.isNullOrEmpty(weekDay
                            .getStartTimeStr())) {
                        String hour = weekDay.getStartTimeStr().substring(0,
                                weekDay.getStartTimeStr().indexOf(":"));
                        try {
                            Integer dayMinTime = Integer.parseInt(hour);
                            if (currentMinTime == null
                                    || currentMinTime > dayMinTime) {
                                currentMinTime = dayMinTime;
                            }
                        } catch (Exception e) {
                            LogHelper.log(log, e);
                        }
                    }
                }
            }
        }

        return currentMinTime;
    }

    public static boolean isDiscardDate(Date date,
                                        List<EventCalendarDiscardDay> list) {
        for (EventCalendarDiscardDay day : list) {
            if (!day.getEachYear().booleanValue()
                    && date.compareTo(day.getDiscardDay()) == 0) {
                return true;
            } else if (day.getEachYear().booleanValue()) {
                Calendar c1 = Calendar.getInstance();
                c1.setTime(date);

                Calendar c2 = Calendar.getInstance();
                c2.setTime(day.getDiscardDay());

                if (c2.get(Calendar.DATE) == c1.get(Calendar.DATE)
                        && c2.get(Calendar.MONTH) == c1.get(Calendar.MONTH)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void populateDynamicEventsForWeekView(List<Event> list,
                                                        EventCalendar ec, Calendar c, EventCalendarWeekDay weekday,
                                                        boolean showTakenSlots, boolean isDayView, boolean forWorkingList,
                                                        boolean forPatientLoading,
                                                        List<EventCalendarRegistration> totalList, boolean beforeToday,
                                                        boolean oldCalendar, RadiologyExamRequest radExReq)
            throws PersistenceBeanException, HibernateException,
            InstantiationException, IllegalAccessException {
        Calendar c1 = Calendar.getInstance();

        c1.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                DateTimeHelper.getHour(weekday.getStartTimeStr()),
                DateTimeHelper.getMinute(weekday.getStartTimeStr()), 0);
        c1.set(Calendar.MILLISECOND, 0);

        Calendar c2 = Calendar.getInstance();

        c2.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DATE),
                DateTimeHelper.getHour(weekday.getEndTimeStr()),
                DateTimeHelper.getMinute(weekday.getEndTimeStr()), 0);
        c2.set(Calendar.MILLISECOND, 0);

        List<EventCalendarRegistration> registrations = getRegistrationsForDynamicDayWeekView(
                weekday, c1, c2, totalList);

        if (!ValidationHelper.isNullOrEmpty(registrations)) {
            List<String> ids = new ArrayList<String>();

            for (EventCalendarRegistration registration : registrations) {
                if (ids.contains(registration.getPackageId())) {
                    continue;
                } else {
                    ids.add(registration.getPackageId());
                }

                if (!(oldCalendar || beforeToday)
                        && registration.getFromDate().compareTo(c1.getTime()) != 0) {
                    Event item = EventCalendar.copy(ec);

                    item.setId(ListHelper.getMaxId(list) + 1);
                    item.setStartDate(c1.getTime());
                    item.setEndDate(registration.getFromDate());
                    item.setForwarded(registration
                            .getRadiologyExamRequestItem().getForwarded());
                    list.add(item);
                }

                Event item = EventCalendar.copy(ec);

                item.setId(ListHelper.getMaxId(list) + 1);
                item.setStartDate(registration.getFromDate());
                item.setEndDate(registration.getToDate());
                item.setForwarded(registration
                        .getRadiologyExamRequestItem().getForwarded());
                if (forWorkingList) {
                    item.setRadiologyExamRequestItemId(registration
                            .getRadiologyExamRequestItem().getId());
                    if (item.getRadiologyExamRequestItemId() != null) {
                        item.setRegistration(registration);
                    }
                }

                if (isDayView) {
                    StringBuffer sb = new StringBuffer();

                    sb.append(registration.getRadiologyExamRequestItem()
                            .getRadiologyExamRequest().getPatientFullname());

                    if (!ValidationHelper.isNullOrEmpty(registration
                            .getRadiologyExamRequestItem()
                            .getRadiologyExamRequest()
                            .getAsapSectorDescription())) {
                        sb.append("; ");
                        sb.append(ResourcesHelper
                                .getString("dateConfirmationSector"));
                        sb.append(": ");
                        if (!ValidationHelper.isNullOrEmpty(registration.getRadiologyExamRequestItem()
                                .getRadiologyExamRequest().getAsapActivityLine())) {
                            sb.append(registration.getRadiologyExamRequestItem()
                                    .getRadiologyExamRequest().getAsapActivityLine())
                                    .append(" - ");
                        }

                        sb.append(registration.getRadiologyExamRequestItem()
                                .getRadiologyExamRequest()
                                .getAsapSectorDescription());
                    }

                    sb.append(". ");

                    StringBuffer sb1 = new StringBuffer();
                    boolean addDivider = false;
                    int ctr = 0;

                    for (EventCalendarRegistration reg : registrations) {
                        if (registration.getPackageId().equals(
                                reg.getPackageId())) {
                            if (addDivider) {
                                sb1.append(", ");
                            } else {
                                addDivider = true;
                            }

                            sb1.append(reg.getRadiologyExamRequestItem()
                                    .getRadiologyExam().getDescription());

                            if (forPatientLoading
                                    && !reg.getId()
                                    .equals(registration.getId())) {
                                reg.getRadiologyExamRequestItem()
                                        .setEventCalendarRegistration(reg);

                                Event event = new Event();

                                event.setId(ListHelper.getMaxId(list) + 2);
                                event.setRadiologyExamRequestItemId(reg
                                        .getRadiologyExamRequestItem().getId());
                                item.setForwarded(reg
                                        .getRadiologyExamRequestItem().getForwarded());
                                list.add(event);
                            }

                            ctr++;
                        }
                    }

                    sb.append(ResourcesHelper
                            .getString(ctr > 1 ? "dateConfirmationExams"
                                    : "dateConfirmationExam"));
                    sb.append(": ");
                    sb.append(sb1);

                    if (forWorkingList) {
                        item.setExams(sb1.toString());
                    } else {
                        item.setPatientName(sb.toString());
                    }
                } else {
                    item.setPatientName(String.format("%s - %s", registration
                                    .getRadiologyExamRequestItem()
                                    .getRadiologyExamRequest().getPatientFullname(),
                            registration.getRadiologyExamRequestItem()
                                    .getRadiologyExam().getDescription()));
                    item.setPatientWeek(String.format("%s - %s", registration
                                    .getRadiologyExamRequestItem()
                                    .getRadiologyExamRequest().getPatientFullname(),
                            registration.getRadiologyExamRequestItem()
                                    .getRadiologyExam().getDescription()));
                }

                if (showTakenSlots) {
                    list.add(item);
                }

                c1.setTime(registration.getToDate());
            }
        }

        if (!(oldCalendar || beforeToday)
                && c1.getTime().compareTo(c2.getTime()) != 0) {
            Event item = EventCalendar.copy(ec);

            item.setId(ListHelper.getMaxId(list) + 1);
            item.setStartDate(c1.getTime());
            item.setEndDate(c2.getTime());

            if (!forWorkingList) {
                list.add(item);
            }

            c1.setTime(c2.getTime());
        }

        if (!(oldCalendar || beforeToday)
                && weekday.getOverbooking().booleanValue()) {
            Event item = EventCalendar.copy(ec);
            Calendar c3 = (Calendar) c1.clone();

            item.setId(ListHelper.getMaxId(list) + 1);
            item.setStartDate(c1.getTime());

            Calendar c4 = (Calendar) c2.clone();
            c4.add(Calendar.MINUTE, weekday.getAverageDuration().intValue());

            if (radExReq != null) {
                c1.add(Calendar.MINUTE, weekday.getAverageDuration().intValue());
            } else {
                c1.add(Calendar.MINUTE, 60);
            }

            item.setEndDate(c1.getTime());
            item.setOverbooked(true);

            List<EventCalendarRegistration> registrationsOverb = getRegistrationsForDynamicDayWeekView(
                    weekday, c3, c4, totalList);

            if (!ValidationHelper.isNullOrEmpty(registrationsOverb)) {
                if (registrationsOverb.get(0).getFromDate()
                        .compareTo(c3.getTime()) != 0) {
                    item.setEndDate(registrationsOverb.get(0).getFromDate());
                    list.add(item);
                }

                for (EventCalendarRegistration registration : registrationsOverb) {
                    Event itemWithReg = EventCalendar.copy(ec);

                    itemWithReg.setId(ListHelper.getMaxId(list) + 1);
                    itemWithReg.setOverbooked(true);
                    itemWithReg.setStartDate(registration.getFromDate());
                    itemWithReg.setEndDate(registration.getToDate());
                    itemWithReg.setPatientName(registration
                            .getRadiologyExamRequestItem()
                            .getRadiologyExamRequest().getPatientFullname());
                    itemWithReg.setRadiologyExamRequestItemId(registration
                            .getRadiologyExamRequestItem().getId());
                    item.setForwarded(registration
                            .getRadiologyExamRequestItem().getForwarded());
                    if (itemWithReg.getRadiologyExamRequestItemId() != null) {
                        itemWithReg.setRegistration(registration);
                    }

                    if (list.get(list.size() - 1).getEndDate()
                            .compareTo(itemWithReg.getStartDate()) != 0) {
                        //It is need to add unoccupied Event between two occupied
                        Event ev = EventCalendar.copy(ec);
                        ev.setId(ListHelper.getMaxId(list) + 1);
                        ev.setOverbooked(true);
                        ev.setStartDate(list.get(list.size() - 1).getEndDate());
                        ev.setEndDate(itemWithReg.getStartDate());

                        list.add(ev);
                    }

                    list.add(itemWithReg);
                }

                if (registrationsOverb.get(registrationsOverb.size() - 1)
                        .getToDate().compareTo(c4.getTime()) != 0) {
                    Calendar lastDate = Calendar.getInstance();
                    lastDate.setTime(registrationsOverb.get(
                            registrationsOverb.size() - 1).getToDate());
                    lastDate.add(Calendar.HOUR, 1);
                    Event itemLast = EventCalendar.copy(ec);
                    itemLast.setId(ListHelper.getMaxId(list) + 1);
                    itemLast.setOverbooked(true);
                    itemLast.setStartDate(registrationsOverb.get(
                            registrationsOverb.size() - 1).getToDate());
                    itemLast.setEndDate(radExReq != null ? c4.getTime()
                            : lastDate.getTime());

                    list.add(itemLast);
                }
            } else {
                if (!forWorkingList) {
                    list.add(item);
                }
            }
        }
    }

    public static String configMinTime(RadiologyExamRequest radExReq,
                                       Long calendarId) throws HibernateException, IllegalAccessException,
            PersistenceBeanException, InstantiationException {
        Integer currentMinTime = null;

        if (radExReq != null
                && !ValidationHelper.isNullOrEmpty(radExReq
                .getRadiologyExamRequestItems())) {
            for (RadiologyExamRequestItem itemOfSelectedRequest : radExReq
                    .getRadiologyExamRequestItems()) {
                if (!ValidationHelper.isNullOrEmpty(itemOfSelectedRequest
                        .getEventCalendarRegistrations())) {
                    for (EventCalendarRegistration ecr : itemOfSelectedRequest
                            .getEventCalendarRegistrations()) {
                        if (!ValidationHelper.isNullOrEmpty(ecr
                                .getEventCalendarWeekday())
                                || !ValidationHelper.isNullOrEmpty(ecr
                                .getEventCalendarWeekday()
                                .getEventCalendar())
                                || !ValidationHelper.isNullOrEmpty(ecr
                                .getEventCalendarWeekday()
                                .getEventCalendar().getWeekDays())) {
                            for (EventCalendarWeekDay weekday : ecr
                                    .getEventCalendarWeekday()
                                    .getEventCalendar().getWeekDays()) {
                                String hour = weekday.getStartTimeStr()
                                        .substring(
                                                0,
                                                weekday.getStartTimeStr()
                                                        .indexOf(":"));
                                if (weekday.getEventCalendar().getId()
                                        .equals(calendarId)) {
                                    try {
                                        Integer dayMinTime = Integer
                                                .parseInt(hour);

                                        if (currentMinTime == null
                                                || currentMinTime > dayMinTime) {
                                            currentMinTime = dayMinTime;
                                        }
                                    } catch (Exception e) {
                                        LogHelper.log(log, e);
                                    }
                                } else {
                                    currentMinTime = getMinTimeByCalendars(calendarId);
                                }
                            }
                        }
                    }
                } else {
                    currentMinTime = getMinTimeByCalendars(calendarId);
                }
            }
        } else {
            currentMinTime = getMinTimeByCalendars(calendarId);
        }

        return currentMinTime == null ? "01:00" : (currentMinTime + ":00");
    }

    public static List<Long> loadListRegistrations(Long weekdayId, Date date, boolean unique) {
        try {
            if (!unique) {
                return DaoManager.loadField(
                        EventCalendarRegistration.class, "id", Long.class, null,
                        new Criterion[]{
                                Restrictions.eq("eventCalendarWeekday.id", weekdayId),
                                Restrictions.between("date", DateTimeHelper.getDayStart(date),
                                        DateTimeHelper.getDayEnd(date))
                        });
            } else {
                Query query = DaoManager.getSession().createQuery("select min(ecr.id) from EventCalendarRegistration ecr " +
                        "where ecr.eventCalendarWeekday.id=:weekDayId and ecr.date between :startDate and :endDate " +
                        "group by ecr.packageId");
                query.setParameter("weekDayId", weekdayId);
                query.setParameter("startDate", DateTimeHelper.getDayStart(date));
                query.setParameter("endDate", DateTimeHelper.getDayEnd(date));
                return query.list();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    public static List<Long> getExamTypeIds(Long calendarId) {
        EventCalendar eventCalendar = null;
        try {
            eventCalendar = (EventCalendar) DaoManager.get(EventCalendar.class,
                    new Criterion[]{
                            Restrictions.eq("id", calendarId)
                    });
        } catch (HibernateException | InstantiationException
                | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }

        List<Long> ids = new ArrayList<Long>();

        if (eventCalendar != null
                && !ValidationHelper.isNullOrEmpty(eventCalendar
                .getDiagnostic())
                && !ValidationHelper.isNullOrEmpty(eventCalendar
                .getDiagnostic().getExamTypes())) {
            List<ExamType> examType = eventCalendar.getDiagnostic()
                    .getExamTypes();

            for (ExamType et : examType) {
                ids.add(et.getId());
            }
        }

        return ids;
    }

    public static List<RadiologyExamRequest> loadRadExReq(String asapSectorId,
                                                          String surname, Map<Long, Long> calendarSector, Long calendarId,
                                                          List<Long> examTypeIds) {
        List<RadiologyExamRequest> radExReq = null;

        try {
            radExReq = DaoManager
                    .load(RadiologyExamRequest.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("patient", "p",
                                            JoinType.INNER_JOIN)
                            },
                            new Criterion[]{
                                    (ValidationHelper
                                            .isNullOrEmpty(asapSectorId) ? null
                                            : Restrictions.eq("asapSectorId",
                                            Long.valueOf(asapSectorId))),
                                    Restrictions.ilike("p.surname",
                                            surname.toUpperCase(),
                                            MatchMode.START),
                                    Restrictions
                                            .eq("waitingListRegistrationState",
                                            WaitingListRegistrationStates.REQUIRED),
                                    Restrictions.isNull("reserveDate"),
                                    Restrictions.eq("sector.id",
                                            calendarSector.get(calendarId)),
                                    Restrictions.in("examType.id", examTypeIds)
                            }, Order.asc("p.surname"));
        } catch (HibernateException | IllegalAccessException
                | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }

        if (radExReq != null) {
            return radExReq;
        } else {
            return new ArrayList<RadiologyExamRequest>();
        }
    }

    public static boolean checkIsInNormalSlot(int slot,
                                              EventCalendarWeekDay weekday, Date toCheck) {
        Integer startHour = Integer.parseInt(weekday.getStartTimeStr().split(
                ":")[0]);
        Integer startMinute = Integer.parseInt(weekday.getStartTimeStr().split(
                ":")[1]);

        Calendar slotStart = Calendar.getInstance();
        slotStart.setTime(toCheck);
        slotStart.set(Calendar.HOUR_OF_DAY, startHour);
        slotStart.set(Calendar.MINUTE, startMinute);
        slotStart.add(Calendar.MINUTE, getSlotLength(weekday.getStartTimeStr(), weekday.getEndTimeStr(), weekday.getSlotNumber())
                * (slot - 1));

        Calendar slotEnd = Calendar.getInstance();
        slotEnd.setTime(toCheck);
        slotEnd.set(Calendar.HOUR_OF_DAY, startHour);
        slotEnd.set(Calendar.MINUTE, startMinute);
        slotEnd.add(Calendar.MINUTE, getSlotLength(weekday.getStartTimeStr(), weekday.getEndTimeStr(), weekday.getSlotNumber())
                * slot);

        if (toCheck.getTime() == slotStart.getTimeInMillis()
                || (toCheck.after(slotStart.getTime()) && toCheck
                .before(slotEnd.getTime()))) {
            return true;
        }
        return false;
    }

    public static boolean checkIsInDay(EventCalendarWeekDay weekday, Date toCheck) {
        Integer startHour = Integer.parseInt(weekday.getStartTimeStr().split(
                ":")[0]);
        Integer startMinute = Integer.parseInt(weekday.getStartTimeStr().split(
                ":")[1]);

        Calendar slotStart = Calendar.getInstance();
        slotStart.setTime(toCheck);
        slotStart.set(Calendar.HOUR_OF_DAY, startHour);
        slotStart.set(Calendar.MINUTE, startMinute);

        Integer endHour = Integer.parseInt(weekday.getEndTimeStr().split(
                ":")[0]);
        Integer endMinute = Integer.parseInt(weekday.getEndTimeStr().split(
                ":")[1]);

        Calendar slotEnd = Calendar.getInstance();
        slotEnd.setTime(toCheck);
        slotEnd.set(Calendar.HOUR_OF_DAY, endHour);
        slotEnd.set(Calendar.MINUTE, endMinute);

        if (weekday.getOverbooking()) {
            for (int i = 0; i < weekday.getMaximumOverbooking(); i++) {
                slotEnd.add(Calendar.MINUTE, getSlotLength(weekday.getStartTimeStr(), weekday.getEndTimeStr(), weekday.getSlotNumber()));
            }

        }

        if (toCheck.getTime() == slotStart.getTimeInMillis()
                || toCheck.getTime() == slotEnd.getTimeInMillis()
                || (toCheck.after(slotStart.getTime()) && toCheck
                .before(slotEnd.getTime()))) {
            return true;
        }
        return false;
    }

    public static boolean checkIsOverbooking(int slot, EventCalendarWeekDay weekday, Date toCheck) {
        Integer startHour = Integer.parseInt(weekday.getEndTimeStr().split(
                ":")[0]);
        Integer startMinute = Integer.parseInt(weekday.getEndTimeStr().split(
                ":")[1]);

        Calendar slotStart = Calendar.getInstance();
        slotStart.setTime(toCheck);
        slotStart.set(Calendar.HOUR_OF_DAY, startHour);
        slotStart.set(Calendar.MINUTE, startMinute);
        slotStart.add(Calendar.MINUTE, getSlotLength(weekday.getStartTimeStr(), weekday.getEndTimeStr(), weekday.getSlotNumber())
                * (slot - 1));

        Calendar slotEnd = Calendar.getInstance();
        slotEnd.setTime(toCheck);
        slotEnd.set(Calendar.HOUR_OF_DAY, startHour);
        slotEnd.set(Calendar.MINUTE, startMinute);
        slotEnd.add(Calendar.MINUTE, getSlotLength(weekday.getStartTimeStr(), weekday.getEndTimeStr(), weekday.getSlotNumber())
                * slot);

        if (toCheck.getTime() == slotStart.getTimeInMillis()
                || toCheck.getTime() == slotEnd.getTimeInMillis()
                || (toCheck.after(slotStart.getTime()) && toCheck
                .before(slotEnd.getTime()))) {
            return true;
        }
        return false;
    }

}
