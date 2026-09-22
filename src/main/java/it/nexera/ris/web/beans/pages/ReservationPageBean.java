package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.AsapSectorHelper;
import it.nexera.ris.common.helpers.logic.GeneralFunctionsHelper;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.AsapSector;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.calendar.*;
import it.nexera.ris.web.beans.PageBean;
import it.nexera.ris.web.beans.wrappers.logic.AvailableRange;
import it.nexera.ris.web.beans.wrappers.logic.DateAssignment;
import it.nexera.ris.web.beans.wrappers.logic.Event;
import it.nexera.ris.web.beans.wrappers.logic.ScheduleEventEx;
import it.nexera.ris.web.common.DateCalendar;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleModel;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Named("reservationPageViewBean")
@ViewScoped
public class ReservationPageBean extends PageBean implements Serializable {

    private static final long serialVersionUID = -6415315558532493426L;

    private static String SEPARATOR = "separator";

    private static String INIT_DATE_RESERVATION_PAGE_IN_SESSION = "initDateReservationPageInSession";

    private static String INITIAL_DATE_RESERVATION_PAGE_IN_SESSION = "initialDateReservationPageInSession";

    private static String EV_CAL_ID_TO_CHANGE_RESERVATION_PAGE_IN_SESSION = "eventCalendarIdToChangeReservationPageInSession";

    private static String EV_CAL_ID_TO_CHANGE_DATECONFIRMATION_IN_SESSION = "eventCalendarIdToChangeDateConfirmationInSession";

    private final String ID_IN_SESSION_FOR_TAGS = "RadiologyExamsIdsForPrint";

    private List<SelectItem> calendars;

    private String[] selectedSlots;

    private Event selectedSlot;

    private boolean oldCalendar;

    private Boolean cannotAssignToOld;

    private String droppableId;

    private String draggableId;

    private Date currentDay;

    private RadiologyExamRequest selected;

    private ScheduleModel eventModel;

    private Long calendarId;

    private DateAssignment dateAssigment;

    private List<SelectItem> slots;

    private Boolean onePatientAssigment;

    private List<Event> firstAvailable;

    private Date firstAvailEndDate;

    private String scheduleView;

    private List<RadiologyExamRequest> patientList;

    private List<RadiologyExamRequest> patientListFiltered;

    private String surname;

    private List<SelectItem> templates;

    private Long selectedTemplateId;

    private String minTime;

    private Map<Long, Long> calendarSector;

    private AsapSector selectedAsapSectorId;

    private List<AsapSector> asapSectors;

    private Map<DateCalendar, List<Event>> dateEventMap;

    private List<Event> mainList;

    @Override
    protected void onConstruct() {
        try {
            pageLoad();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void generateWorkingList() {
        if (!ValidationHelper.isNullOrEmpty(getCurrentDay())) {
            try {
                WorkingListHelper.convertAndViewInTab(filterEventListByDate(getMainList(), getCurrentDay()), getCurrentDay());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    private List<Event> filterEventListByDate(List<Event> list, Date date){
        List<Event> resultList = new ArrayList<>();
        for(Event event: list){
            if(event.getStartDate().after(DateTimeHelper.getDayStart(date))
                    && event.getEndDate().before(DateTimeHelper.getDayEnd(date))){
                resultList.add(event);
            }
        }
        return resultList;
    }

    public void goBack() {
        AgendaHelper.goToWaitinglistRegistrationList();
    }

    public void calendarChangeEvent() {
        try {
            fillAvailableDays();
            configMinTime();
            loadPatients();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void pageLoad() throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (!this.isPostback()) {
            if (ValidationHelper.isNullOrEmpty(this.getInitDate())) {
                initCurrentDate();
            }

            this.setScheduleView(ScheduleViewTypes.timeGridWeek.name());

            RadiologyExamRequest wlru = WaitingListSessionHelper
                    .restoreFromSession();

            if (wlru == null) {
                this.setOnePatientAssigment(Boolean.FALSE);
            } else {
                this.setSelected(wlru);
                this.setOnePatientAssigment(Boolean.TRUE);
            }

            setEventModel(new LazyScheduleModel() {
                @Override
                public void loadEvents(LocalDateTime start, LocalDateTime end) {

                    Date startDate = DateTimeHelper.fromLocalDateTime(start);
                    Date endDate = DateTimeHelper.fromLocalDateTime(end);

                    setCurrentDay(new Date(
                            (startDate.getTime() + endDate.getTime()) / 2L));
                    try {
                        ScheduleViewTypes viewTypes = ScheduleViewTypes.timeGridWeek;

                        if (DateTimeHelper.getDateDiffInDay(endDate, startDate) > 15) {
                            viewTypes = ScheduleViewTypes.dayGridMonth;
                        } else if (DateTimeHelper.getDateDiffInDay(endDate, startDate) < 3) {
                            viewTypes = ScheduleViewTypes.timeGridDay;
                        }
                        setMainList(loadEventsByDates(startDate, endDate));
                        for (Event event : getMainList()) {
                            addEvent(new ScheduleEventEx(event, viewTypes));
                        }
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                }
            });

            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, 1);

            this.setFirstAvailEndDate(DateTimeHelper.getDate(c.getTime()));

            setSurname("");
            setCalendarSector(new HashMap<Long, Long>());
        }

        fillCalendars(true);
        loadPatients();
        configMinTime();

        if (this.getDateAssigment() == null) {
            this.setDateAssigment(new DateAssignment());
        }
    }

    public void onInitDateChange(SelectEvent<LocalDate> event) {
        this.setInitialDate( event.getObject());
    }

    protected void fillAvailableDays() throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (this.getSelected() != null) {
            this.setFirstAvailable(new ArrayList<Event>());
            populateEvents(DateTimeHelper.getDate(new Date()),
                    this.getFirstAvailEndDate(), false, false,
                    this.getFirstAvailable(), false, false, false);
        }
    }

    private void fillCalendars(boolean viewRequest)
            throws PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (this.getSelected() == null || viewRequest) {
            if (!ValidationHelper.isNullOrEmpty(this.getSelected())) {
                this.setCalendars(new ArrayList<SelectItem>());

                List<EventCalendar> eventCalendars = DaoManager.load(
                        EventCalendar.class,
                        new Criterion[]{
                                Restrictions.in("diagnostic.id", UserHolder
                                        .getInstance().getCurrentUser()
                                        .getDiagnostics()),
                                Restrictions.isNull("newCalendar")
                        });

                if (!ValidationHelper.isNullOrEmpty(eventCalendars)) {
                    List<Long> toCheckRadiologyExamIds = GeneralFunctionsHelper
                            .exportRadiologyExamIdsFromRadiologyExamRequest(this
                                    .getSelected());
                    if (!ValidationHelper
                            .isNullOrEmpty(toCheckRadiologyExamIds)) {
                        for (EventCalendar ec : eventCalendars) {
                            List<Long> fullRadiologyExamIds = GeneralFunctionsHelper
                                    .exportRadiologyExamIdsFromCalendar(ec);
                            if (!ValidationHelper
                                    .isNullOrEmpty(toCheckRadiologyExamIds)
                                    && fullRadiologyExamIds
                                    .containsAll(toCheckRadiologyExamIds)) {
                                this.getCalendars()
                                        .add(new SelectItem(ec.getId(), ec
                                                .getName()));
                            }
                        }
                    }
                }
            } else {
                this.setCalendars(fillCalendarsCombobox());
            }
        } else {
            this.setCalendars(fillCalendarsCombobox());
        }

        Long id = (Long) this.getSession().get(
                EV_CAL_ID_TO_CHANGE_RESERVATION_PAGE_IN_SESSION);
        this.getSession().remove(
                EV_CAL_ID_TO_CHANGE_RESERVATION_PAGE_IN_SESSION);

        if (ValidationHelper.isNullOrEmpty(this.getCalendarId())) {
            if (!ValidationHelper.isNullOrEmpty(id)) {
                this.setCalendarId(id);
            } else if (!ValidationHelper.isNullOrEmpty(this.getCalendars())) {
                List<Long> ids = new ArrayList<Long>();
                for (SelectItem item : this.getCalendars()) {
                    ids.add((Long) item.getValue());
                }

                List<EventCalendar> listDefault = DaoManager.load(
                        EventCalendar.class,
                        new Criterion[]{
                                Restrictions.in("diagnostic.id", UserHolder
                                        .getInstance().getCurrentUser()
                                        .getDiagnostics()),
                                Restrictions.isNull("newCalendar"),
                                Restrictions.eq("isDefault", Boolean.TRUE),
                                Restrictions.in("id", ids)
                        });

                if (!ValidationHelper.isNullOrEmpty(listDefault)) {
                    this.setCalendarId(listDefault.get(0).getId());
                } else {
                    this.setCalendarId((Long) this.getCalendars().get(0)
                            .getValue());
                }
            }
        }
    }

    private List<SelectItem> fillCalendarsCombobox()
            throws PersistenceBeanException, IllegalAccessException {
        List<EventCalendar> calendars = DaoManager.load(
                EventCalendar.class,
                new Criterion[]{
                        Restrictions.in("diagnostic.id", UserHolder
                                .getInstance().getCurrentUser()
                                .getDiagnostics()),
                        Restrictions.isNull("newCalendar")
                }, new Order[]{
                        Order.asc("name")
                });

        List<SelectItem> list = new ArrayList<SelectItem>();

        for (EventCalendar item : calendars) {
            list.add(new SelectItem(item.getId(), item.toString()));
            getCalendarSector().put(item.getId(),
                    item.getDiagnostic().getSector().getId());
        }

        return list;
    }

    public void onDrop() {
        if (this.getDraggableId() != null && this.getDroppableId() != null) {
            String[] params = this.getDraggableId().split(SEPARATOR);
            if (params.length == 1) {
                try {
                    Long id = Long.parseLong(params[0]);
                    RadiologyExamRequest radiologyExamRequest = DaoManager.get(
                            RadiologyExamRequest.class, id);
                    if (!getCurrentUser().getSectors().contains(
                            radiologyExamRequest.getSector().getId())) {
                        AgendaHelper.showErrorMessage();
                        return;
                    }
                    setSelected(radiologyExamRequest);
                    ScheduleEventEx dropEvent = new ScheduleEventEx(
                            this.getDroppableId());
                    this.setOnePatientAssigment(Boolean.TRUE);
                    this.onEventSelectInternal(dropEvent);
                } catch (HibernateException | InstantiationException
                        | IllegalAccessException | PersistenceBeanException
                        | ParseException e) {
                    LogHelper.log(log, e);
                }
            } else {
                ScheduleEventEx dragEvent = null;
                ScheduleEventEx dropEvent = null;
                try {
                    dragEvent = new ScheduleEventEx(this.getDraggableId());
                    dropEvent = new ScheduleEventEx(this.getDroppableId());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                if (dragEvent != null && dropEvent != null) {
                    RadiologyExamRequest wlru = null;
                    try {
                        wlru = loadFromEvent(dragEvent);
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }

                    if (wlru != null) {
                        if (WaitingListRegistrationStates.REQUIRED.equals(wlru
                                .getWaitingListRegistrationState())
                                || WaitingListRegistrationStates.RESERVED
                                .equals(wlru
                                        .getWaitingListRegistrationState())) {
                            try {
                                if (!getCurrentUser().getSectors().contains(
                                        wlru.getSector().getId())) {
                                    AgendaHelper.showErrorMessage();
                                    return;
                                }
                                this.setSelected(wlru);
                                this.setOnePatientAssigment(Boolean.TRUE);
                            } catch (Exception e) {
                                LogHelper.log(log, e);
                            }

                            this.onEventSelectInternal(dropEvent);
                        } else {
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_WARN,
                                            ResourcesHelper
                                                    .getValidation("warning"),
                                            ResourcesHelper
                                                    .getValidation("dateConfirmationReserveNotPossible"));
                        }
                    }
                }
            }
        }
    }

    private void onEventSelectInternal(ScheduleEventEx selectEvent) {
        Boolean edit = this.getOnePatientAssigment() == null ? Boolean.FALSE
                : this.getOnePatientAssigment();

        try {
            EventCalendar ec = DaoManager.get(EventCalendar.class,
                    selectEvent.getDbId());

            boolean old = ec.getNewCalendar() != null;

            while (ec.getNewCalendar() != null) {
                ec = ec.getNewCalendar();
            }

            boolean showDlg = true;

            if (edit.booleanValue()) {
                if (old) {
                    this.setCannotAssignToOld(Boolean.TRUE);
                } else {
                    Calendar c = Calendar.getInstance();
                    Date dateConverted = DateTimeHelper.fromLocalDateTime(selectEvent.getStartDate());

                    c.setTime(dateConverted);

                    populateAssignForm(selectEvent.getOverbooked()
                            .booleanValue(), selectEvent.getSlotNumber(), ec, c);

                    showDlg = !(!this.getDateAssigment().isAssignedSlot()
                            && !this.getDateAssigment().isAssignNotAvail()
                            && this.getDateAssigment().getSlots()
                            .booleanValue()
                            && !ValidationHelper.isNullOrEmpty(this
                            .getSelectedSlots())
                            && this.getSelectedSlots().length == 1 && ec
                            .getSlotSelectionType().equals(
                                    SlotSelectionTypes.MONO));
                }

                if (showDlg) {
                    PFRequestContextHelper
                            .executeJS("PF('eventDialogWV').show();");
                } else {
                    PFRequestContextHelper
                            .executeJS("PF('saveEventBtnWV').jq[0].click();");
                }

                // this.getSession().put("initialDate", this.getCurrentDay());
            } else {
                RadiologyExamRequest wlru = loadFromEvent(selectEvent);

                if (wlru != null
                        && !ValidationHelper.isNullOrEmpty(wlru
                        .getRadiologyExamRequestItems())) {
                    WaitingListSessionHelper.backupToSession(wlru);
                    if (ec != null) {
                        this.getSession()
                                .put(EV_CAL_ID_TO_CHANGE_DATECONFIRMATION_IN_SESSION,
                                        ec.getId());
                    }

                    // this.getSession().put("initialDate", getCurrentDay());
                    this.getSession().put("returnPage", this.getCurrentPage());
                    RedirectHelper.goTo(PageTypes.DATE_CONFIRMATION);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void onEventSelect(SelectEvent<ScheduleEventEx> selectEvent) {
        if (selectEvent.getObject() != null) {
            this.onEventSelectInternal(selectEvent
                    .getObject());
        }
    }

    private RadiologyExamRequest loadFromEvent(ScheduleEventEx selectEvent)
            throws PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        RadiologyExamRequest wlru = null;
        Date startDate = DateTimeHelper.fromLocalDateTime(selectEvent.getStartDate());
        Date endDate = DateTimeHelper.fromLocalDateTime(selectEvent.getEndDate());

        List<Event> list = new ArrayList<Event>();

        Calendar cStart = Calendar.getInstance();
        cStart.setTime(startDate);
        cStart.set(Calendar.HOUR_OF_DAY, 0);

        Calendar cEnd = Calendar.getInstance();
        cEnd.setTime(startDate);
        cEnd.set(Calendar.HOUR_OF_DAY, 22);

        populateEvents(cStart.getTime(), cEnd.getTime(), false, true, list,
                true, true, true);

        List<RadiologyExamRequestItem> listWlr = new ArrayList<RadiologyExamRequestItem>();

        if (!ValidationHelper.isNullOrEmpty(list)) {
            for (Event event : list) {
                RadiologyExamRequestItem radiologyExamRequestItem = null;

                if (event.getRadiologyExamRequestItemId() != null) {
                    radiologyExamRequestItem = DaoManager.get(
                            RadiologyExamRequestItem.class,
                            event.getRadiologyExamRequestItemId());
                }
                if (radiologyExamRequestItem != null && !ValidationHelper
                        .isNullOrEmpty(radiologyExamRequestItem
                                .getEventCalendarRegistrations())) {
                    EventCalendarRegistration ecr = radiologyExamRequestItem
                            .getEventCalendarRegistrations().get(0);

                    if (ValidationHelper.isNullOrEmpty(ecr.getSlots())) {
                        if (AgendaHelper.checkInInterval(ecr.getFromDate(),
                                ecr.getToDate(), startDate,
                                endDate)) {
                            listWlr.add(radiologyExamRequestItem);
                        }
                    } else {
                        if (ecr.containsSlot(selectEvent.getSlotNumber(),
                                selectEvent.getOverbooked().booleanValue()) && ecr.getRadiologyExamRequestItem().getId().equals(selectEvent.getRequestItemId())) {
                            listWlr.add(radiologyExamRequestItem);
                        }
                    }
                }
            }

            if (!ValidationHelper.isNullOrEmpty(listWlr)) {
                wlru = listWlr.get(0).getRadiologyExamRequest();
            } else {
                PFRequestContextHelper
                        .executeJS("PF('patientNotFoundDialogWV').show();");
            }
        }

        return wlru;
    }

    public void newChange() {
        this.getSession().put(EV_CAL_ID_TO_CHANGE_RESERVATION_PAGE_IN_SESSION,
                this.getCalendarId());
        RedirectHelper.goTo(PageTypes.RESERVATION_PAGE);
    }

    private void populateAssignForm(boolean overbooked, Integer slotNumber,
                                    EventCalendar ec, Calendar c) throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        this.cleanValidation();

        this.setSlots(new ArrayList<SelectItem>());
        this.setDateAssigment(new DateAssignment());
        this.getDateAssigment().setDate(c.getTime());

        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        if (c.getTime().before(c1.getTime())) {
            this.getDateAssigment().setAssignNotAvail(true);
        }

        for (EventCalendarWeekDay weekday : ec.getWeekDays()) {
            if (c.get(Calendar.DAY_OF_WEEK) == weekday.getWeekDay().getNumber()
                    .intValue()) {
                this.getDateAssigment().setAverageDuration(
                        weekday.getAverageDuration());

                if (EventCalendarWeekdayAvailabilityTypes.SLOT.equals(weekday
                        .getAvailabilityType())) {
                    populateEditSlotsForm(overbooked, slotNumber, c, weekday);
                } else {
                    populateEditDynamicForm(ec, c, weekday);
                }
            }
        }
    }

    private void populateEditDynamicForm(EventCalendar ec, Calendar c,
                                         EventCalendarWeekDay weekday) throws PersistenceBeanException,
            HibernateException, InstantiationException, IllegalAccessException {
        this.getDateAssigment().setEcId(weekday.getId());
        this.getDateAssigment().setSlots(Boolean.FALSE);

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

        List<EventCalendarRegistration> registrations = AgendaHelper
                .getRegistrationsForDynamicDay(weekday, c1, c2, true);
        if (registrations != null && !registrations.isEmpty()) {
            List<AvailableRange> ranges = new ArrayList<AvailableRange>();
            for (EventCalendarRegistration registration : registrations) {
                if (registration.getFromDate().compareTo(c1.getTime()) != 0
                        && DateTimeHelper.getDateDiffInMin(
                        registration.getFromDate(), c1.getTime()) >= weekday
                        .getAverageDuration().longValue()) {
                    this.getDateAssigment().setTimeFrom(
                            DateTimeHelper.getTimeString(c1));
                    c1.add(Calendar.MINUTE, weekday.getAverageDuration()
                            .intValue());
                    this.getDateAssigment().setTimeTo(
                            DateTimeHelper.getTimeString(c1));
                    return;
                } else if (registration.getFromDate().compareTo(c1.getTime()) != 0) {
                    ranges.add(new AvailableRange(c1.getTime(), registration
                            .getFromDate(), DateTimeHelper.getDateDiffInMin(
                            registration.getFromDate(), c1.getTime())));
                }

                c1.setTime(registration.getToDate());
            }

            if (c2.getTime().compareTo(c1.getTime()) != 0
                    && DateTimeHelper.getDateDiffInMin(c2.getTime(),
                    c1.getTime()) >= weekday.getAverageDuration()
                    .longValue()) {
                this.getDateAssigment().setTimeFrom(
                        DateTimeHelper.getTimeString(c1));
                c1.add(Calendar.MINUTE, weekday.getAverageDuration().intValue());
                this.getDateAssigment().setTimeTo(
                        DateTimeHelper.getTimeString(c1));
                return;
            } else if (c2.getTime().compareTo(c1.getTime()) != 0) {
                ranges.add(new AvailableRange(c1.getTime(), c2.getTime(),
                        DateTimeHelper.getDateDiffInMin(c2.getTime(),
                                c1.getTime())));
            }

            if (!ValidationHelper.isNullOrEmpty(ranges)) {
                int max = 0;
                for (AvailableRange range : ranges) {
                    if (range.getMinutesRange() > max) {
                        max = range.getMinutesRange();
                    }
                }

                Collections.sort(ranges);

                this.getDateAssigment()
                        .setTimeFrom(ranges.get(0).getFromTime());
                this.getDateAssigment().setTimeTo(ranges.get(0).getToTime());
                return;
            }
        } else {
            this.getDateAssigment().setTimeFrom(
                    DateTimeHelper.getTimeString(c1));
            c1.add(Calendar.MINUTE, weekday.getAverageDuration().intValue());
            this.getDateAssigment().setTimeTo(DateTimeHelper.getTimeString(c1));
        }
    }

    public void onRowSelect(SelectEvent event) {
        try {
            if (this.getSelectedSlot() != null) {
                EventCalendar ec = DaoManager.get(EventCalendar.class, this
                        .getSelectedSlot().getEcId());

                Calendar c = Calendar.getInstance();
                c.setTime(this.getSelectedSlot().getStartDate());

                populateAssignForm(this.getSelectedSlot().isOverbooked(), this
                        .getSelectedSlot().getSlotNumber(), ec, c);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void populateEditSlotsForm(boolean overbooked, Integer slotNumber,
                                       Calendar c, EventCalendarWeekDay weekday)
            throws PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        this.getDateAssigment().setEcId(weekday.getId());
        this.getDateAssigment().setSlots(Boolean.TRUE);
        this.getDateAssigment().setAssignedSlot(false);

        List<Long> ecrList = AgendaHelper.loadListRegistrations(weekday.getId(),
                DateTimeHelper.getDayStart(c.getTime()), true);

        for (int i = 1; i <= weekday.getSlotNumber(); i++) {
            if (!AgendaHelper.checkSlotPresence(EventCalendarRegistration.class, ecrList, i, false)) {
                this.getSlots().add(new SelectItem(String.valueOf(i), String.format("%s %d",
                        ResourcesHelper.getString("dateConfirmationSlotNumber"), i)));
            } else if (slotNumber != null && i == slotNumber) {
                this.getDateAssigment().setAssignedSlot(true);
            }
        }
        if (weekday.getOverbooking()) {
            for (int i = 1; i <= weekday.getMaximumOverbooking(); i++) {
                if (!AgendaHelper.checkSlotPresence(EventCalendarRegistration.class, ecrList, i, true)) {
                    this.getSlots().add(new SelectItem(String.format("overbook_%d", i), String.format("%s %d",
                            ResourcesHelper.getString("dateConfirmationOverbookingSlot"), i)));
                }
            }
        }

        if (overbooked) {
            for (SelectItem item : this.getSlots()) {
                if (item.getValue().toString().contains("overbook_")) {
                    this.setSelectedSlots(new String[]{
                            item.getValue().toString()
                    });
                    break;
                }
            }
        } else {
            if (slotNumber != null) {
                this.setSelectedSlots(new String[]{
                        slotNumber.toString()
                });
            }
        }
    }

    public void saveEvent() {
        try {
            saveEventInternal();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            this.fillAvailableDays();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void saveEventInternal() throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            if (this.getSelected() != null) {
                if (!ValidationHelper.isNullOrEmpty(this.getSelected()
                        .getRadiologyExamRequestItems())) {
                    Date date = this.getDateAssigment().getDate();

                    for (RadiologyExamRequestItem radExamRequestItem : this
                            .getSelected().getRadiologyExamRequestItems()) {
                        EventCalendarRegistration ecwlr = new EventCalendarRegistration();

                        if (!ValidationHelper.isNullOrEmpty(radExamRequestItem
                                .getEventCalendarRegistrations())) {
                            for (EventCalendarRegistration ecrToDel : radExamRequestItem
                                    .getEventCalendarRegistrations()) {
                                DaoManager.remove(ecrToDel);
                            }
                        }

                        ecwlr.setDate(date);
                        ecwlr.setPatientName(radExamRequestItem
                                .getRadiologyExamRequest().getPatient()
                                .getName());
                        ecwlr.setPatientSurname(radExamRequestItem
                                .getRadiologyExamRequest().getPatient()
                                .getSurname());

                        if (radExamRequestItem.getRadiologyExamRequest()
                                .getAsapSectorCode() != null) {
                            ecwlr.setSectorName(radExamRequestItem
                                    .getRadiologyExamRequest()
                                    .getAsapSectorCode());
                        }
                        ecwlr.setPatientBirthDay(radExamRequestItem
                                .getRadiologyExamRequest().getPatient()
                                .getBirthDate());

                        if (ecwlr.getExamsName() == null) {
                            ecwlr.setExamsName("");
                        }
                        for (RadiologyExamRequestItem radExanReqItem : this
                                .getSelected().getRadiologyExamRequestItems()) {
                            ecwlr.setExamsName((ecwlr.getExamsName().isEmpty() ? ecwlr
                                    .getExamsName()
                                    : (ecwlr.getExamsName() + ", "))
                                    + radExanReqItem.getRadiologyExam()
                                    .getDescription());
                        }

                        EventCalendarWeekDay weekday = DaoManager.get(
                                EventCalendarWeekDay.class, this
                                        .getDateAssigment().getEcId());

                        if (this.getDateAssigment().getSlots() != null
                                && this.getDateAssigment().getSlots()
                                .booleanValue()) {
                            this.cleanValidation();

                            int slotLength = AgendaHelper.getSlotLength(
                                    weekday.getStartTimeStr(),
                                    weekday.getEndTimeStr(),
                                    weekday.getSlotNumber());

                            int minSlot = AgendaHelper.getSlotNumber(this
                                    .getSelectedSlots()[0]);

                            Calendar c1 = Calendar.getInstance();
                            c1.setTime(this.getDateAssigment().getDate());
                            c1.set(Calendar.HOUR_OF_DAY, DateTimeHelper
                                    .getHour(weekday.getStartTimeStr()));
                            c1.set(Calendar.MINUTE, DateTimeHelper
                                    .getMinute(weekday.getStartTimeStr()));

                            for (int i = 0; i < this.getSelectedSlots().length; i++) {
                                if (AgendaHelper.getSlotNumber(this
                                        .getSelectedSlots()[i]) < minSlot) {
                                    minSlot = AgendaHelper.getSlotNumber(this
                                            .getSelectedSlots()[i]);
                                }
                            }

                            for (int i = 0; i < minSlot - 1; i++) {
                                c1.add(Calendar.MINUTE, slotLength);
                            }

                            this.getDateAssigment().setDate(c1.getTime());

                            List<EventCalendarRegistrationSlot> slots = new ArrayList<EventCalendarRegistrationSlot>();

                            for (int i = 0; i < this.getSelectedSlots().length; i++) {
                                Integer slotNumber = null;
                                boolean overbooked = false;

                                if (this.getSelectedSlots()[i]
                                        .indexOf("overbook_") > -1) {
                                    overbooked = true;
                                }

                                slotNumber = AgendaHelper.getSlotNumber(this
                                        .getSelectedSlots()[i]);

                                EventCalendarRegistrationSlot regSlot = new EventCalendarRegistrationSlot(
                                        ecwlr, slotNumber, overbooked);

                                if (!slots.isEmpty()
                                        && !(slots
                                        .get(i - 1)
                                        .getSlotNumber()
                                        .equals(weekday.getSlotNumber())
                                        && regSlot.getSlotNumber()
                                        .intValue() == 1 && overbooked)
                                        && ((!slots.get(i - 1).getOverbooked() && overbooked) || (regSlot
                                        .getSlotNumber().intValue()
                                        - slots.get(i - 1)
                                        .getSlotNumber()
                                        .intValue() != 1))) {
                                    addFieldExeption("slotsSelection",
                                            "slotsMustBeSequential");
                                    return;
                                }

                                slots.add(regSlot);
                            }

                            ecwlr.setSlotsToSave(new ArrayList<EventCalendarRegistrationSlot>());
                            ecwlr.getSlotsToSave().addAll(slots);
                        } else {
                            this.cleanValidation();

                            if (ValidationHelper.isNullOrEmpty(this
                                    .getDateAssigment().getTimeFrom())) {
                                this.addRequiredFieldExeption("startTime");
                            }

                            if (ValidationHelper.isNullOrEmpty(this
                                    .getDateAssigment().getTimeTo())) {
                                this.addRequiredFieldExeption("endTime");
                            }

                            if (this.getValidationFailed()) {
                                return;
                            }

                            if (!ValidationHelper.isNullOrEmpty(weekday
                                    .getEventCalendarRegistrations())) {
                                for (EventCalendarRegistration registration : weekday
                                        .getEventCalendarRegistrations()) {
                                    if (!registration
                                            .getRadiologyExamRequestItem()
                                            .getRadiologyExamRequest().getId()
                                            .equals(this.getSelected().getId())) {
                                        //if it is the same rekvest, then we do not need this check because in this case its time rewrite
                                        if (DateTimeHelper.timeBetweenTwoDate(
                                                registration.getFromDate(),
                                                registration.getToDate(), this
                                                        .getDateAssigment()
                                                        .getTimeFrom(), this
                                                        .getDateAssigment()
                                                        .getTimeTo(), this
                                                        .getDateAssigment()
                                                        .getDate())) {
                                            setSelected(null);
                                            MessageHelper
                                                    .addGlobalMessage(
                                                            FacesMessage.SEVERITY_ERROR,
                                                            ResourcesHelper
                                                                    .getValidation("assignFailed"),
                                                            ResourcesHelper
                                                                    .getValidation("notAssignetOnNonFreeTime"));
                                            return;
                                        }
                                    }
                                }
                            }

                            Calendar c = Calendar.getInstance();
                            c.setTime(this.getDateAssigment().getDate());
                            c.set(Calendar.HOUR_OF_DAY, DateTimeHelper
                                    .getHour(this.getDateAssigment()
                                            .getTimeFrom()));
                            c.set(Calendar.MINUTE, DateTimeHelper
                                    .getMinute(this.getDateAssigment()
                                            .getTimeFrom()));

                            Calendar c1 = Calendar.getInstance();
                            c1.setTime(this.getDateAssigment().getDate());
                            c1.set(Calendar.HOUR_OF_DAY, DateTimeHelper
                                    .getHour(this.getDateAssigment()
                                            .getTimeTo()));
                            c1.set(Calendar.MINUTE, DateTimeHelper
                                    .getMinute(this.getDateAssigment()
                                            .getTimeTo()));

                            if (getDateAssigment().getTimeFrom().equals(
                                    getDateAssigment().getTimeTo())
                                    || (!AgendaHelper.validateRanges(this
                                            .getDateAssigment().getDate(),
                                    weekday, c.getTime(), c1.getTime(),
                                    ecwlr, getSelected()))) {
                                this.addFieldExeption("endTime",
                                        "timeOutOfRange");
                                this.addFieldExeption("startTime",
                                        "timeOutOfRange");

                                return;
                            }

                            this.getDateAssigment().setDate(c.getTime());

                            ecwlr.setFromDate(c.getTime());
                            ecwlr.setToDate(c1.getTime());
                        }

                        ecwlr.setEventCalendarWeekday(weekday);

                        ecwlr.setRadiologyExamRequestItem(radExamRequestItem);
                        radExamRequestItem.setEventCalendarRegistration(ecwlr);
                    }
                }

                assign();
            }
        } catch (Throwable e) {
            LogHelper.log(log, e);
        }
    }

    public void assign() {
        try {
            TransactionExecuter.execute(new Action() {
                @Override
                public void execute() throws Exception {
                    AgendaHelper.internalButtonAction(getSelected(), getCurrentUser().getId());
                }

                @Override
                public void onSuccess() {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                            ResourcesHelper.getString("dateOperationSuccess"),
                            "");
                    chooseTemplate();
                    if (!ValidationHelper.isNullOrEmpty(getTemplates())) {
                        PFRequestContextHelper
                                .executeJS("PF('printPDFDialog').show();");
                    } else {
                        goBack();
                    }
                }

                @Override
                public void onException(Exception e) throws Exception {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper
                                    .getValidation("dateOperationFailed"), "");
                    LogHelper.log(log, e);
                }

            });

            clearSession();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public List<Long> getSelectedIds(List<RadiologyExamRequestItem> reris) {
        return AgendaHelper.getSelectedIds(reris);
    }

    private List<Event> loadEventsByDates(Date start, Date end)
            throws PersistenceBeanException, InterruptedException,
            HibernateException, InstantiationException, IllegalAccessException {
        boolean isMonthView = false;
        boolean isDayView = false;

        if (DateTimeHelper.getDateDiffInDay(end, start) > 15) {
            isMonthView = true;
        }

        if (DateTimeHelper.getDateDiffInDay(end, start) < 3) {
            isDayView = true;
        }

        List<Event> list = new ArrayList<Event>();

        populateEvents(start, end, isMonthView, isDayView, list, true, false,
                false);

        return list;
    }

    private void populateEvents(Date start, Date end, boolean isMonthView,
                                boolean isDayView, List<Event> list, boolean showTakenSlots,
                                boolean forWorkingList, boolean forPatientLoading)
            throws PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        EventCalendar ec = null;


        if(!isMonthView) {
            if (!ValidationHelper.isNullOrEmpty(getDateEventMap())) {
                DateCalendar key = new DateCalendar(start, end, getCalendarId());
                for (Map.Entry<DateCalendar, List<Event>> entry : getDateEventMap().entrySet()) {
                    if (entry.getKey().equals(key)) {
                        list.clear();
                        list.addAll(entry.getValue());
                        return;
                    }
                }
            } else {
                setDateEventMap(new HashMap<DateCalendar, List<Event>>());
            }
        }

        if (this.getCalendarId() == null
                && !ValidationHelper.isNullOrEmpty(this.getCalendars())) {
            this.setCalendarId((Long) this.getCalendars().get(0).getValue());
        }

        if (this.getCalendarId() != null) {

            ec = DaoManager.get(EventCalendar.class, this.getCalendarId());

            if (ec != null) {
                boolean iterate = true;
                this.oldCalendar = false;

                while (iterate) {
                    Long calendarCount = DaoManager.getCount(
                            EventCalendarRegistration.class, "id",
                            new CriteriaAlias[]{
                                    new CriteriaAlias("eventCalendarWeekday",
                                            "ecw", JoinType.INNER_JOIN)
                            }, new Criterion[]{
                                    Restrictions.eq("ecw.eventCalendar.id",
                                            ec.getId())
                            });

                    if (ec.getValidationPeriodEndDate().after(start)
                            && ec.getValidationPeriodStartDate().before(end)
                            && (!this.oldCalendar || calendarCount > 0)) {
                        List<EventCalendarRegistrationShort> totalSlotsList = AgendaHelper
                                .getRegistrationsForSlotDay(ec, start, end);
                        List<EventCalendarRegistration> totalDynamicList = AgendaHelper
                                .getRegistrationsForDynamicDay(ec, start, end);
                        Calendar c = Calendar.getInstance();
                        c.setTime(start);

                        while (c.getTime().before(end)) {
                            c.set(Calendar.HOUR_OF_DAY, 0);
                            if (c.getTime().before(
                                    ec.getValidationPeriodStartDate())
                                    || c.getTime().after(
                                    ec.getValidationPeriodEndDate())
                                    || AgendaHelper.isDiscardDate(c.getTime(),
                                    ec.getDiscardDays())) {
                                c.add(Calendar.DATE, 1);
                                continue;
                            }

                            for (EventCalendarWeekDay weekday : ec
                                    .getWeekDays()) {
                                if (weekday != null
                                        && c.get(Calendar.DAY_OF_WEEK) == weekday
                                        .getWeekDay().getNumber()
                                        .intValue()) {
                                    boolean beforeToday = checkBeforeToday(c);
                                    if (EventCalendarWeekdayAvailabilityTypes.SLOT
                                            .equals(weekday
                                                    .getAvailabilityType())) {
                                        populateSlotEvents(isMonthView,
                                                isDayView, list, ec, c,
                                                weekday, showTakenSlots,
                                                forWorkingList,
                                                forPatientLoading,
                                                totalSlotsList, beforeToday);
                                    } else if (EventCalendarWeekdayAvailabilityTypes.DYNAMIC
                                            .equals(weekday
                                                    .getAvailabilityType())) {
                                        populateDynamicEvents(isMonthView,
                                                isDayView, list, ec, c,
                                                weekday, showTakenSlots,
                                                forWorkingList,
                                                forPatientLoading,
                                                totalDynamicList, beforeToday);
                                    }
                                }
                            }

                            c.add(Calendar.DATE, 1);
                        }
                    }

                    ec = ec.getOldCalendar();

                    if (ec == null) {
                        iterate = false;
                    } else {
                        this.oldCalendar = true;
                    }
                }
                if (!isMonthView) {
                    getDateEventMap().put(new DateCalendar(start, end, getCalendarId()), list);
                }
            }
        }
    }

    public List<AsapSector> completeText(String query) {
        this.setAsapSectors(new ArrayList<AsapSector>());

        if (ValidationHelper.isNullOrEmpty(query)) {
            this.getAsapSectors().addAll(AsapSectorHelper.getASAPSIOSectorsFromDB(null));
        } else {
            this.getAsapSectors().addAll(AsapSectorHelper.getASAPSIOSectorsFromDB(query));
        }

        return this.getAsapSectors();
    }

    public Date getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(Date currentDay) {
        this.currentDay = currentDay;
    }

    private void populateDynamicEvents(boolean isMonthView, boolean isDayView,
                                       List<Event> list, EventCalendar ec, Calendar c,
                                       EventCalendarWeekDay weekday, boolean showTakenSlots,
                                       boolean forWorkingList, boolean forPatientLoading,
                                       List<EventCalendarRegistration> totalList, boolean beforeToday)
            throws PersistenceBeanException, HibernateException,
            InstantiationException, IllegalAccessException {
        if (isMonthView) {
            AgendaHelper.populateDynamicEventsForMonthView(list, ec, c,
                    weekday, totalList, beforeToday, isOldCalendar(),
                    getSelected());
        } else {
            AgendaHelper.populateDynamicEventsForWeekView(list, ec, c, weekday,
                    showTakenSlots, isDayView, forWorkingList,
                    forPatientLoading, totalList, beforeToday, isOldCalendar(),
                    this.getSelected());
        }
    }

    private void populateSlotEvents(boolean isMonthView, boolean isDayView,
                                    List<Event> list, EventCalendar ec, Calendar c,
                                    EventCalendarWeekDay weekday, boolean showTakenSlots,
                                    boolean forWorkingList, boolean forPatientLoading,
                                    List<EventCalendarRegistrationShort> totalList, boolean beforeToday)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (isMonthView) {
            AgendaHelper.populateSlotEventsForMonthView(list, ec, c, weekday,
                    totalList, beforeToday, isOldCalendar());
        } else {
            AgendaHelper.populateSlotEventsForWeekView(list, ec, c, weekday,
                    showTakenSlots, isDayView, forWorkingList,
                    forPatientLoading, totalList, beforeToday, isOldCalendar());
        }
    }

    private boolean checkBeforeToday(Calendar c) {
        return DateTimeHelper.getDateDiffInDay(c.getTime(), Calendar
                .getInstance().getTime()) < 0;
    }

    private void configMinTime() throws HibernateException,
            IllegalAccessException, PersistenceBeanException,
            InstantiationException {
        this.setMinTime(AgendaHelper.configMinTime(this.getSelected(),
                getCalendarId()));
    }

    public void loadPatients() {
        setPatientList(AgendaHelper.loadRadExReq(getSelectedAsapSectorId() == null ? null
                        : getSelectedAsapSectorId().getStrId(), getSurname(),
                getCalendarSector(), getCalendarId(), getExamTypeIds()));
    }

    public List<Long> getExamTypeIds() {
        return AgendaHelper.getExamTypeIds(getCalendarId());
    }

    public void loadTemplate() {
        if (ValidationHelper.isNullOrEmpty(getTemplates())) {
            chooseTemplate();
        }

        putIdsForTag();

        PrintPDFHelper.chooseTemplate(this.getTemplates(), this.getSelected(),
                getCurrentUser());

        removeIdsForTag();
    }

    public void chooseTemplate() {
        try {
            setTemplates(GeneralFunctionsHelper.fillTemplates(
                    DocumentGenerationPlaces.RESERVATION, this.getSelected()
                            .getSector().getId(), null));
        } catch (HibernateException | IllegalAccessException
                | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
    }

    public void printReport() {
        if (!ValidationHelper.isNullOrEmpty(getSelected())) {
            putIdsForTag();

            PrintPDFHelper.printReport(getSelected(), getSelectedTemplateId(),
                    getCurrentUser(), false);

            removeIdsForTag();
        }
    }

    private void removeIdsForTag() {
        SessionHelper.removeObject(ID_IN_SESSION_FOR_TAGS);
    }

    private void putIdsForTag() {
        List<Long> radExamsIds = new ArrayList<Long>();
        if (!ValidationHelper.isNullOrEmpty(this.getSelected()
                .getRadiologyExamRequestItems())) {
            for (RadiologyExamRequestItem reri : this.getSelected()
                    .getRadiologyExamRequestItems()) {
                Long id = reri.getRadiologyExam().getId();
                radExamsIds.add(id);
            }
        }

        SessionHelper.putIds(radExamsIds, ID_IN_SESSION_FOR_TAGS);
    }

    public void hideFirstAvailable() {
        this.setSelectedSlot(null);
    }

    public void initCurrentDate() {
        this.setInitDate(LocalDate.now());
    }

    public TimeZone getTimeZone() {
        return DateTimeHelper.getTimeZone();
    }

    public RadiologyExamRequest getSelected() {
        return selected;
    }

    public void setSelected(RadiologyExamRequest selected) {
        this.selected = selected;
        try {
            fillCalendars(true);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public void setEventModel(ScheduleModel eventModel) {
        this.eventModel = eventModel;
    }

    public List<SelectItem> getCalendars() {
        return calendars;
    }

    public void setCalendars(List<SelectItem> calendars) {
        this.calendars = calendars;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public String getTitle() {
        if (!ValidationHelper.isNullOrEmpty(this.getCalendarId())
                && !ValidationHelper.isNullOrEmpty(this.getCalendars())) {
            for (SelectItem item : this.getCalendars()) {
                if (item.getValue().equals(this.getCalendarId())) {
                    return item.getLabel();
                }
            }
        }
        return ResourcesHelper.getString("dateConfirmation");
    }

    public DateAssignment getDateAssigment() {
        return dateAssigment;
    }

    public void setDateAssigment(DateAssignment dateAssigment) {
        this.dateAssigment = dateAssigment;
    }

    public List<SelectItem> getSlots() {
        return slots;
    }

    public void setSlots(List<SelectItem> slots) {
        this.slots = slots;
    }

    public Event getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(Event selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public Boolean getOnePatientAssigment() {
        return onePatientAssigment;
    }

    public void setOnePatientAssigment(Boolean onePatientAssigment) {
        this.onePatientAssigment = onePatientAssigment;
    }

    public List<Event> getFirstAvailable() {
        return firstAvailable;
    }

    public void setFirstAvailable(List<Event> firstAvailable) {
        this.firstAvailable = firstAvailable;
    }

    public Date getFirstAvailEndDate() {
        return firstAvailEndDate;
    }

    public void setFirstAvailEndDate(Date firstAvailEndDate) {
        this.firstAvailEndDate = firstAvailEndDate;

        try {
            if (this.getCalendars() != null) {
                fillAvailableDays();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public Date getMindate() {
        return Calendar.getInstance().getTime();
    }

    public RadiologyExamRequest[] getSelectedToTable() {
        return AgendaHelper.getSelectedToTable(getSelected());
    }

    public String[] getSelectedSlots() {
        return selectedSlots;
    }

    public void setSelectedSlots(String[] selectedSlots) {
        this.selectedSlots = selectedSlots;
    }

    public LocalDate getInitDate() {
        return (LocalDate) this.getSession().get(
                INIT_DATE_RESERVATION_PAGE_IN_SESSION);
    }

    public void setInitDate(LocalDate initDate) {
        this.getSession().put(INIT_DATE_RESERVATION_PAGE_IN_SESSION, initDate);
    }

    public LocalDate getInitialDate() {
        return (LocalDate) this.getSession().get(INITIAL_DATE_RESERVATION_PAGE_IN_SESSION);
    }

    public void setInitialDate(LocalDate initialDate) {
        this.getSession().put(INITIAL_DATE_RESERVATION_PAGE_IN_SESSION,
                initialDate);
    }

    public String getScheduleView() {
        return scheduleView;
    }

    public void setScheduleView(String scheduleView) {
        this.scheduleView = scheduleView;
    }

    public Boolean getCannotAssignToOld() {
        return cannotAssignToOld;
    }

    public void setCannotAssignToOld(Boolean cannotAssignToOld) {
        this.cannotAssignToOld = cannotAssignToOld;
    }

    public String getDroppableId() {
        return droppableId;
    }

    public void setDroppableId(String droppableId) {
        this.droppableId = droppableId;
    }

    public String getDraggableId() {
        return draggableId;
    }

    public void setDraggableId(String draggableId) {
        this.draggableId = draggableId;
    }

    public String getMinTime() {
        return minTime;
    }

    public void setMinTime(String minTime) {
        this.minTime = minTime;
    }

    public List<RadiologyExamRequest> getPatientList() {
        return patientList;
    }

    public void setPatientList(List<RadiologyExamRequest> patientList) {
        this.patientList = patientList;
    }

    public List<RadiologyExamRequest> getPatientListFiltered() {
        return patientListFiltered;
    }

    public void setPatientListFiltered(
            List<RadiologyExamRequest> patientListFiltered) {
        this.patientListFiltered = patientListFiltered;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public List<SelectItem> getTemplates() {
        return templates;
    }

    public void setTemplates(List<SelectItem> templates) {
        this.templates = templates;
    }

    public Long getSelectedTemplateId() {
        return selectedTemplateId;
    }

    public void setSelectedTemplateId(Long selectedTemplateId) {
        this.selectedTemplateId = selectedTemplateId;
    }

    public Map<Long, Long> getCalendarSector() {
        return calendarSector;
    }

    public void setCalendarSector(Map<Long, Long> calendarSector) {
        this.calendarSector = calendarSector;
    }

    public boolean isOldCalendar() {
        return oldCalendar;
    }

    public void setOldCalendar(boolean oldCalendar) {
        this.oldCalendar = oldCalendar;
    }

    public AsapSector getSelectedAsapSectorId() {
        return selectedAsapSectorId;
    }

    public void setSelectedAsapSectorId(AsapSector selectedAsapSectorId) {
        this.selectedAsapSectorId = selectedAsapSectorId;
    }

    public List<AsapSector> getAsapSectors() {
        return asapSectors;
    }

    public void setAsapSectors(List<AsapSector> asapSectors) {
        this.asapSectors = asapSectors;
    }

    public Map<DateCalendar, List<Event>> getDateEventMap() {
        return dateEventMap;
    }

    public void setDateEventMap(Map<DateCalendar, List<Event>> dateEventMap) {
        this.dateEventMap = dateEventMap;
    }

    public List<Event> getMainList() {
        return mainList;
    }

    public void setMainList(List<Event> mainList) {
        this.mainList = mainList;
    }
}
