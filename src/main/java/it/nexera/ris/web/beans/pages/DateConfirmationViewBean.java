package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.annotations.ReattachIgnore;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.helpers.logic.GeneralFunctionsHelper;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
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
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleModel;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Named("dateConfirmationViewBean")
@ViewScoped
public class DateConfirmationViewBean extends PageBean implements Serializable {

    private static final long serialVersionUID = -7262611006421487936L;

    private static String INIT_DATE_DATECONFIRMATION_IN_SESSION = "initDateDateConfirmationInSession";

    private static String INITIAL_DATE_DATECONFIRMATIONE_IN_SESSION = "initialDateDateConfirmationInSession";

    private static String EV_CAL_ID_TO_CHANGE_DATECONFIRMATION_IN_SESSION = "eventCalendarIdToChangeDateConfirmationInSession";

    private final String ID_IN_SESSION_FOR_TAGS = "RadiologyExamsIdsForPrint";

    private List<SelectItem> calendars;

    private String[] selectedSlots;

    private Event selectedSlot;

    private boolean oldCalendar;

    private Boolean cannotAssignToOld;

    private String droppableId;

    private String draggableId;

    // fields after change to viewState scope

    private Date currentDay;

    @ReattachIgnore
    private RadiologyExamRequest selected;

    private ScheduleModel eventModel;

    private Long calendarId;

    private DateAssignment dateAssigment;

    private List<SelectItem> slots;

    private Boolean onePatientAssigment;

    private List<Event> firstAvailable;

    private LocalDateTime firstAvailEndDate;

    private String scheduleView;

    private List<SelectItem> templates;

    private Long selectedTemplateId;

    private String minTime;

    private PageTypes returnPage;

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

    private List<Event> filterEventListByDate(List<Event> list, Date date) {
        List<Event> resultList = new ArrayList<>();
        for (Event event : list) {
            if (event.getStartDate().after(DateTimeHelper.getDayStart(date))
                    && event.getEndDate().before(DateTimeHelper.getDayEnd(date))) {
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
            this.returnPage = (PageTypes) this.getSession().get("returnPage");
            this.setScheduleView(ScheduleViewTypes.timeGridWeek.name());

            RadiologyExamRequest wlru = WaitingListSessionHelper
                    .restoreFromSession();

            if (wlru == null) {
                this.setOnePatientAssigment(Boolean.FALSE);
            } else {
                this.selected = wlru;
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

            this.firstAvailEndDate = LocalDateTime.now().plusMonths(1);
            this.setFirstAvailable(new ArrayList<Event>());
        }

        fillCalendars(true);

        configMinTime();

        if (this.getDateAssigment() == null) {
            this.setDateAssigment(new DateAssignment());
        }
    }

    public void onInitDateChange(SelectEvent<LocalDate> event) {
        this.setInitialDate(event.getObject());
    }

    protected void fillAvailableDays() throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        if (this.getSelected() != null) {
            this.setFirstAvailable(new ArrayList<Event>());
            populateEvents(DateTimeHelper.getDate(new Date()),
                    DateTimeHelper.fromLocalDateTime(getFirstAvailEndDate()), false, false,
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

                                if (this.getCalendarId() == null && Boolean.TRUE
                                        .equals(ec.getIsDefault())) {
                                    this.setCalendarId(ec.getId());
                                }
                            }
                        }
                    }
                }
            } else {
                this.setCalendars(ComboboxHelper.newFillList(
                        EventCalendar.class,
                        "name",
                        new Criterion[]{
                                Restrictions.in("diagnostic.id", UserHolder
                                        .getInstance().getCurrentUser()
                                        .getDiagnostics()),
                                Restrictions.isNull("newCalendar")
                        }, "name"));
            }
        } else {
            this.setCalendars(ComboboxHelper.newFillList(
                    EventCalendar.class,
                    "name",
                    new Criterion[]{
                            Restrictions.in("diagnostic.id", UserHolder
                                    .getInstance().getCurrentUser()
                                    .getDiagnostics()),
                            Restrictions.isNull("newCalendar")
                    }, "name"));
        }

        Long id = (Long) this.getSession().get(
                EV_CAL_ID_TO_CHANGE_DATECONFIRMATION_IN_SESSION);
        this.getSession().remove(
                EV_CAL_ID_TO_CHANGE_DATECONFIRMATION_IN_SESSION);

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

    public void onDrop() {
        if (this.getDraggableId() != null && this.getDroppableId() != null) {
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

    private void onEventSelectInternal(ScheduleEventEx selectEvent) {
        Boolean edit = this.getOnePatientAssigment() == null ? Boolean.FALSE
                : this.getOnePatientAssigment();

        try {
            EventCalendar ec = DaoManager.get(EventCalendar.class, selectEvent.getDbId());

            boolean old = ec.getNewCalendar() != null;

            while (ec.getNewCalendar() != null) {
                ec = ec.getNewCalendar();
            }

            boolean showDlg = true;

            if (edit) {
                if (old) {
                    this.setCannotAssignToOld(Boolean.TRUE);
                } else {
                    Calendar c = Calendar.getInstance();
                    c.setTime(DateTimeHelper.fromLocalDateTime(selectEvent.getStartDate()));

                    populateAssignForm(selectEvent.getOverbooked(), selectEvent.getSlotNumber(), ec, c);

                    showDlg = !(!this.getDateAssigment().isAssignedSlot()
                            && !this.getDateAssigment().isAssignNotAvail()
                            && this.getDateAssigment().getSlots()
                            && !ValidationHelper.isNullOrEmpty(this.getSelectedSlots())
                            && this.getSelectedSlots().length == 1
                            && ec.getSlotSelectionType().equals(SlotSelectionTypes.MONO));

                }

                if (showDlg) {
                    PFRequestContextHelper.executeJS("PF('eventDialogWV').show();");
                } else {
                    PFRequestContextHelper.executeJS("autoSaveRC();");
                }
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

                    RedirectHelper.goTo(PageTypes.DATE_CONFIRMATION);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void onEventSelect(SelectEvent<ScheduleEventEx> selectEvent) {
        if (selectEvent.getObject() != null) {
            this.onEventSelectInternal(selectEvent.getObject());
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
                                ecr.getToDate(), startDate, endDate)) {
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
        this.getSession().put(EV_CAL_ID_TO_CHANGE_DATECONFIRMATION_IN_SESSION,
                this.getCalendarId());

        if (returnPage != null) {
            if (PageTypes.RADIOLOGY_EXAM_REQUEST.equals(returnPage)) {

                try {
                    TransactionExecuter.execute(new Action() {
                        @Override
                        public void execute() throws Exception {
                            if (getSelected() != null) {
                                for (RadiologyExamRequestItem item : getSelected()
                                        .getRadiologyExamRequestItems()) {
                                    DaoManager.remove(item);
                                }
                                DaoManager.remove(getSelected());
                            }

                        }

                        @Override
                        public void onSuccess() {
                            RedirectHelper.goTo(PageTypes.PATIENT_SEARCH);
                        }

                        @Override
                        public void onException(Exception e) throws Exception {
                            MessageHelper
                                    .addGlobalMessage(
                                            FacesMessage.SEVERITY_ERROR,
                                            ResourcesHelper
                                                    .getValidation("dateRollbackOperationFailed"),
                                            "");
                            LogHelper.log(log, e);
                        }

                    });
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            } else {
                RedirectHelper.goTo(returnPage);
            }
        } else {
            RedirectHelper.goTo(PageTypes.DATE_CONFIRMATION);
        }
    }

    private void populateAssignForm(boolean overbooked, Integer slotNumber,
                                    EventCalendar ec, Calendar c) throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        this.cleanValidation();

        this.setSlots(new ArrayList<>());
        this.setDateAssigment(new DateAssignment());
        this.getDateAssigment().setDate(c.getTime());

        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        if (c.getTime().before(c1.getTime())) {
            this.getDateAssigment().setAssignNotAvail(true);
        }

        List<EventCalendarWeekDay> weekDays = DaoManager.load(EventCalendarWeekDay.class, new CriteriaAlias[]{
                new CriteriaAlias("eventCalendar", "ec", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("ec.id", ec.getId()),
                Restrictions.eq("weekDay", Weekdays.getByDate(c.getTime()))
        });
        for (EventCalendarWeekDay weekday : weekDays) {
            getDateAssigment().setAverageDuration(weekday.getAverageDuration());
            if (EventCalendarWeekdayAvailabilityTypes.SLOT.equals(weekday.getAvailabilityType())) {
                populateEditSlotsForm(overbooked, slotNumber, c, weekday);
            } else {
                populateEditDynamicForm(ec, c, weekday);
            }
        }
    }

    private void populateEditDynamicForm(EventCalendar ec, Calendar c, EventCalendarWeekDay weekday) throws HibernateException {
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

    public void onRowSelect(SelectEvent<?> event) {
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

        List<Long> ids = AgendaHelper.loadListRegistrations(weekday.getId(),
                DateTimeHelper.getDayStart(c.getTime()), true);

        for (int i = 1; i <= weekday.getSlotNumber().intValue(); i++) {
            if (!AgendaHelper.checkSlotPresence(EventCalendarRegistration.class, ids, i, false)) {
                getSlots().add(new SelectItem(String.valueOf(i), String.format("%s %d",
                        ResourcesHelper.getString("dateConfirmationSlotNumber"), i)));
            } else if (slotNumber != null && i == slotNumber) {
                getDateAssigment().setAssignedSlot(true);
            }
        }
        if (weekday.getOverbooking()) {
            for (int i = 1; i <= weekday.getMaximumOverbooking(); i++) {
                if (!AgendaHelper.checkSlotPresence(EventCalendarRegistration.class, ids, i, true)) {
                    this.getSlots().add(new SelectItem(String.format("overbook_%d", i), String.format(
                            "%s %d", ResourcesHelper.getString("dateConfirmationOverbookingSlot"), i)));
                }
            }
        }

        if (overbooked) {
            if (slotNumber != null) {
                this.setSelectedSlots(new String[]{String.format("overbook_%d", slotNumber)});
            }
        } else {
            if (slotNumber != null) {
                this.setSelectedSlots(new String[]{slotNumber.toString()});
            }
        }
    }

    public void saveEvent() {
        try {
            saveEventInternal();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        
        /*try
        {
            this.fillAvailableDays();
        }
        catch(Exception e)
        {
            LogHelper.log(log, e);
        }*/
    }

    private void saveEventInternal() throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            if (this.getSelected() != null) {
                List<RadiologyExamRequestItem> selectedRadItemsList = this
                        .getSelected().getSelectedRadItemsList() != null
                        ? this.getSelected().getSelectedRadItemsList()
                        : this.getSelected()
                        .getRadiologyExamRequestItems();
                if (!ValidationHelper.isNullOrEmpty(selectedRadItemsList)) {
                    Date date = this.getDateAssigment().getDate();

                    EventCalendarWeekDay weekday = DaoManager.get(EventCalendarWeekDay.class, getDateAssigment().getEcId());
                    for (RadiologyExamRequestItem radExamRequestItem : selectedRadItemsList) {
                        EventCalendarRegistration ecwlr = new EventCalendarRegistration();

                        if (!ValidationHelper.isNullOrEmpty(radExamRequestItem.getEventCalendarRegistrations())) {
                            for (EventCalendarRegistration ecrToDel : radExamRequestItem.getEventCalendarRegistrations()) {
                                DaoManager.getSession().flush();
                                DaoManager.getSession().clear();
                                DaoManager.remove(ecrToDel);
                            }
                        }

                        ecwlr.setDate(date);
                        ecwlr.setPatientName(radExamRequestItem.getRadiologyExamRequest().getPatient().getName());
                        ecwlr.setPatientSurname(radExamRequestItem.getRadiologyExamRequest().getPatient().getSurname());

                        if (radExamRequestItem.getRadiologyExamRequest().getAsapSectorCode() != null) {
                            ecwlr.setSectorName(radExamRequestItem.getRadiologyExamRequest().getAsapSectorCode());
                        }
                        ecwlr.setPatientBirthDay(radExamRequestItem.getRadiologyExamRequest().getPatient().getBirthDate());

                        if (ecwlr.getExamsName() == null) {
                            ecwlr.setExamsName("");
                        }
                        for (RadiologyExamRequestItem radExanReqItem : getSelected().getRadiologyExamRequestItems()) {
                            ecwlr.setExamsName((ecwlr.getExamsName().isEmpty() ? ecwlr.getExamsName() : (ecwlr.getExamsName() + ", "))
                                    + radExanReqItem.getRadiologyExam().getDescription());
                        }

                        if (this.getDateAssigment().getSlots() != null && this.getDateAssigment().getSlots()) {
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
                                if (AgendaHelper.getSlotNumber(this.getSelectedSlots()[i]) < minSlot) {
                                    minSlot = AgendaHelper.getSlotNumber(this.getSelectedSlots()[i]);
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

                                if (this.getSelectedSlots()[i].contains("overbook_")) {
                                    overbooked = true;
                                }

                                slotNumber = AgendaHelper.getSlotNumber(this
                                        .getSelectedSlots()[i]);

                                EventCalendarRegistrationSlot regSlot = new EventCalendarRegistrationSlot(ecwlr, slotNumber, overbooked);

                                if (!slots.isEmpty() && !(slots.get(i - 1).getSlotNumber().equals(weekday.getSlotNumber())
                                        && regSlot.getSlotNumber() == 1 && overbooked)
                                        && ((!slots.get(i - 1).getOverbooked() && overbooked)
                                        || (regSlot.getSlotNumber() - slots.get(i - 1).getSlotNumber() != 1))) {
                                    addFieldExeption("slotsSelection", "slotsMustBeSequential");
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

                            if (!ValidationHelper.isNullOrEmpty(weekday.getEventCalendarRegistrations())) {
                                for (EventCalendarRegistration registration : weekday.getEventCalendarRegistrations()) {
                                    if (!registration.getRadiologyExamRequestItem().getRadiologyExamRequest().getId()
                                            .equals(this.getSelected().getId())) {
                                        //if it is the same request, then we do not need this check because in this case its time rewrite
                                        if (DateTimeHelper.timeBetweenTwoDate(registration.getFromDate(),
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
        /*
         * try { if (this.getSelected() != null) { if (result) {
         * WaitingListSessionHelper .backupToSession(this.getSelected());
         * this.setSelected(null); this.setOnePatientAssigment(null);
         * DaoManager.getSession().flush(); DaoManager.getSession().clear();
         * this.setSelected(WaitingListSessionHelper .restoreFromSession()); } }
         * } catch(Exception e) { LogHelper.log(log, e); }
         */
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
                            ResourcesHelper.getString("dateOperationSuccess"), "");
                    chooseTemplate();
                    if (!ValidationHelper.isNullOrEmpty(getTemplates())) {
                        PFRequestContextHelper.executeJS("PF('printPDFDialog').show();");
                    } else {
                        goBack();
                    }
                }

                @Override
                public void onException(Exception e) throws Exception {
                    MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                            ResourcesHelper.getValidation("dateOperationFailed"), "");
                    LogHelper.log(log, e);
                }
            });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void loadTemplate() {
        if (ValidationHelper.isNullOrEmpty(getTemplates())) {
            chooseTemplate();
        }

        putIdsForTag();

        PrintPDFHelper.chooseTemplate(this.getTemplates(), this.getSelected(), getCurrentUser());

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
                    getCurrentUser(), true);

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

    public List<Long> getSelectedIds(List<RadiologyExamRequestItem> reris) {
        return AgendaHelper.getSelectedIds(reris);
    }

    private List<Event> loadEventsByDates(Date start, Date end)
            throws PersistenceBeanException,
            HibernateException, InstantiationException, IllegalAccessException {
        boolean isMonthView = false;
        boolean isDayView = false;

        if (DateTimeHelper.getDateDiffInDay(end, start) > 15) {
            isMonthView = true;
        }

        if (DateTimeHelper.getDateDiffInDay(end, start) < 3) {
            isDayView = true;
        }

        List<Event> list = new ArrayList<>();

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

        if (!isMonthView) {
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
                                .getRegistrationsForDynamicDay(ec, start,
                                        end);

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

    public void hideFirstAvailable() {
        this.setSelectedSlot(null);
    }

    public void initCurrentDate() {
        this.setInitDate(LocalDate.now());
    }

    public TimeZone getTimeZone() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        return c.getTimeZone();
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

    public LocalDateTime getFirstAvailEndDate() {
        return firstAvailEndDate;
    }

    public void setFirstAvailEndDate(LocalDateTime firstAvailEndDate) {
        this.firstAvailEndDate = firstAvailEndDate;

        try {/*
            if (this.getCalendars() != null)
            {
                fillAvailableDays();
            }
        */
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
        return (LocalDate) this.getSession().get(INIT_DATE_DATECONFIRMATION_IN_SESSION);
    }

    public void setInitDate(LocalDate initDate) {
        this.getSession().put(INIT_DATE_DATECONFIRMATION_IN_SESSION, initDate);
    }

    public LocalDate getInitialDate() {
        return (LocalDate) this.getSession().get(INITIAL_DATE_DATECONFIRMATIONE_IN_SESSION);
    }

    public void setInitialDate(LocalDate initialDate) {
        this.getSession().put(INITIAL_DATE_DATECONFIRMATIONE_IN_SESSION, initialDate);
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

    public boolean isOldCalendar() {
        return oldCalendar;
    }

    public void setOldCalendar(boolean oldCalendar) {
        this.oldCalendar = oldCalendar;
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
