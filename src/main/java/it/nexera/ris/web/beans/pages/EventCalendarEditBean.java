package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.EventCalendarStatuses;
import it.nexera.ris.common.enums.EventCalendarWeekdayAvailabilityTypes;
import it.nexera.ris.common.enums.SlotSelectionTypes;
import it.nexera.ris.common.enums.Weekdays;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendar;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarDiscardDay;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarRegistration;
import it.nexera.ris.persistence.beans.entities.domain.calendar.EventCalendarWeekDay;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Diagnostic;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import javax.faces.application.FacesMessage;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named("eventCalendarEditBean")
@RequestScoped
public class EventCalendarEditBean extends EntityEditPageBean<EventCalendar> {
    private static final Long MAX_TIME = 22l;

    private static final Long MIN_TIME = 6l;

    private List<SelectItem> statuses;

    private EventCalendarStatuses status;

    private List<SelectItem> diagnostics;

    private Long diagnosticId;

    private List<SelectItem> avWeekday;

    private List<String> weekdaySelection;

    private EventCalendarWeekDay editWeekday;

    private EventCalendarDiscardDay editDiscardDay;

    private String availabilityKind;

    private String overbookingAccepted;

    private String slotSelectionType;

    private List<SelectItem> slotSelectionTypes;

    private Long availabilityTypeLong;

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (!this.getEntity().isNew()) {
            this.setStatus(this.getEntity().getStatus());
            this.setDiagnosticId(this.getEntity().getDiagnostic().getId());

            if (this.getDiscardDays() == null) {
                this.setDiscardDays(DaoManager
                        .load(EventCalendarDiscardDay.class, new Criterion[]{
                                Restrictions.eq("eventCalendar.id",
                                        this.getEntity().getId())
                        }, Order.asc("discardDay")));

                for (EventCalendarDiscardDay item : this.getDiscardDays()) {
                    item.setTempId(UUID.randomUUID().toString());
                }
            }
            if (this.getWeekdays() == null) {
                this.setWeekdays(DaoManager.load(EventCalendarWeekDay.class,
                        new Criterion[]{
                                Restrictions.eq("eventCalendar.id",
                                        this.getEntity().getId())
                        }, Order.asc("weekDay")));

                for (EventCalendarWeekDay item : this.getWeekdays()) {
                    item.setTempId(UUID.randomUUID().toString());
                }
            }

            if (!this.isPostback() && this.getWeekdays() != null) {
                this.setOldWeekDays(new ArrayList<EventCalendarWeekDay>());
                for (EventCalendarWeekDay item : this.getWeekdays()) {
                    this.getOldWeekDays().add(new EventCalendarWeekDay(item));
                }
            }
        }

        this.fillLists();
    }

    public boolean getShowDiagnostic() {
        return !this.getEntity().isNew();
    }

    public void fillLists() throws HibernateException, PersistenceBeanException,
            IllegalAccessException {
        this.setStatuses(
                ComboboxHelper.fillList(EventCalendarStatuses.values()));
        this.setDiagnostics(ComboboxHelper.fillList(Diagnostic.class));

        editWeekday = new EventCalendarWeekDay();
        editDiscardDay = new EventCalendarDiscardDay();

        avWeekday = ComboboxHelper.fillList(Weekdays.class, false, false);

        if (this.getDiscardDays() == null) {
            this.setDiscardDays(new ArrayList<EventCalendarDiscardDay>());
        }
        if (this.getWeekdays() == null) {
            this.setWeekdays(new ArrayList<EventCalendarWeekDay>());
        }

        this.setSlotSelectionTypes(new ArrayList<SelectItem>());
        for (SlotSelectionTypes type : SlotSelectionTypes.values()) {
            this.getSlotSelectionTypes()
                    .add(new SelectItem(type.name(), type.toString()));
        }

        this.setSlotSelectionType(
                this.getEntity().getSlotSelectionType().name());
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            this.addRequiredFieldExeption("tabs:name");
        } else if (!ValidationHelper.isUnique(EventCalendar.class, "name",
                this.getEntity().getName(), new Criterion[]{
                        Restrictions.isNull("newCalendar")
                }, null, this.getEntityId())) {
            this.addFieldExeption("tabs:name", "eventCalendarNameAlreadyInUse");
        }

        if (ValidationHelper
                .isNullOrEmpty(this.getEntity().getValidationPeriodStartDate())) {
            this.addRequiredFieldExeption("tabs:validationPeriodStartDate");
        }

        if (ValidationHelper
                .isNullOrEmpty(this.getEntity().getValidationPeriodEndDate())) {
            this.addRequiredFieldExeption("tabs:validationPeriodEndDate");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCupId())) {
            this.addRequiredFieldExeption("tabs:cupId");
        } else {
            Long count = 0l;

            try {
                count = DaoManager.getCount(EventCalendar.class, "id",
                        new Criterion[]{
                                Restrictions.ne("id",
                                        getEntity().getId() == null ? 0l
                                                : getEntity().getId()),
                                Restrictions.eq("cupId",
                                        this.getEntity().getCupId())
                        });
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            if (count != 0l) {
                this.addRequiredFieldExeption("tabs:cupId");
            }

        }

        if (!ValidationHelper
                .isNullOrEmpty(this.getEntity().getValidationPeriodStartDate())
                && !ValidationHelper.isNullOrEmpty(
                this.getEntity().getValidationPeriodEndDate())
                && this.getEntity().getValidationPeriodEndDate().before(
                this.getEntity().getValidationPeriodStartDate())) {
            this.addFieldExeption("tabs:validationPeriodEndDate",
                    "eventCalendarEndDateBeforeStartDate");
        }

        if (ValidationHelper.isNullOrEmpty(this.getStatus())) {
            this.addRequiredFieldExeption("tabs:status");
        }

        if (ValidationHelper.isNullOrEmpty(this.getDiagnosticId())) {
            this.addRequiredFieldExeption("tabs:diagnostic");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        boolean inheretedSaving = false;

        this.getEntity().setStatus(this.getStatus());
        this.getEntity().setDiagnostic(
                DaoManager.get(Diagnostic.class, getDiagnosticId()));

        if (!ValidationHelper.isNullOrEmpty(this.getSlotSelectionType())) {
            this.getEntity().setSlotSelectionType(
                    SlotSelectionTypes.valueOf(this.getSlotSelectionType()));
        }

        EventCalendar oldEc = null;

        if (!this.getEntity().isNew() && !this.getEntity().getDeletable()) {
            Long oldId = this.getEntity().getId();
            DaoManager.getSession().evict(this.getEntity());
            this.setEntity(this.getEntity().createInheritedCopy());
            oldEc = DaoManager.get(EventCalendar.class, oldId);

            oldEc.setNewCalendar(this.getEntity());
            DaoManager.save(oldEc);

            inheretedSaving = true;
        }

        if (this.getEntity().getIsDefault() != null
                && this.getEntity().getIsDefault().booleanValue()) {
            for (EventCalendar ec : DaoManager.load(EventCalendar.class,
                    new Criterion[]{
                            Restrictions.isNull("newCalendar"),
                            Restrictions.eq("isDefault", Boolean.TRUE),
                            Restrictions
                                    .ne("id",
                                    this.getEntity().getId() == null
                                            ? (oldEc == null ? 0l
                                            : oldEc.getId()
                                            .longValue())
                                            : this.getEntity().getId()
                                            .longValue())
                    })) {
                ec.setIsDefault(Boolean.FALSE);
                DaoManager.save(ec);
            }
        }

        DaoManager.save(this.getEntity());

        if (inheretedSaving) {
            // processInheretWeekdaysSaving(oldEc);
        } else {
            List<Long> ecwdIds = DaoManager.loadIds(EventCalendarWeekDay.class,
                    null, new Criterion[]{
                            Restrictions.eq("eventCalendar.id",
                                    this.getEntity().getId())
                    });

            if (!ValidationHelper.isNullOrEmpty(ecwdIds)) {
                for (Long weekday : ecwdIds) {
                    if (!ListHelper.contains(this.getWeekdays(), weekday)) {
                        DaoManager.remove(EventCalendarWeekDay.class, weekday);
                    }
                }
            }

            List<Long> ecddIds = DaoManager.loadIds(
                    EventCalendarDiscardDay.class, null, new Criterion[]{
                            Restrictions.eq("eventCalendar.id",
                                    this.getEntity().getId())
                    });

            if (!ValidationHelper.isNullOrEmpty(ecddIds)) {
                for (Long discardDay : ecddIds) {
                    if (!ListHelper.contains(this.getDiscardDays(), discardDay)) {
                        DaoManager.remove(EventCalendarDiscardDay.class,
                                discardDay);
                    }
                }
            }

            for (EventCalendarWeekDay weekday : this.getWeekdays()) {
                weekday.setEventCalendar(getEntity());
                DaoManager.save(weekday);
            }
        }

        for (EventCalendarDiscardDay discardDay : this.getDiscardDays()) {
            if (!discardDay.isNew() && inheretedSaving) {
                DaoManager.getSession().evict(discardDay);
                EventCalendarDiscardDay newDd = new EventCalendarDiscardDay(
                        discardDay);
                newDd.setEventCalendar(getEntity());
                DaoManager.save(newDd);
            } else {
                discardDay.setEventCalendar(getEntity());
                DaoManager.save(discardDay);
            }
        }
    }

    // private void processInheretWeekdaysSaving(EventCalendar oldEc)
    // throws PersistenceBeanException
    // {
    // boolean globalMergePermission = oldEc.getDiagnostic().equals(
    // this.getEntity().getDiagnostic());
    //
    // for (EventCalendarWeekDay weekday : this.getWeekdays())
    // {
    // if (!weekday.isNew())
    // {
    // if (globalMergePermission)
    // {
    // EventCalendarWeekDay oldWd = null;
    //
    // for (EventCalendarWeekDay item : this.getOldWeekDays())
    // {
    // if (item.getWeekDay().equals(weekday.getWeekDay()))
    // {
    // oldWd = item;
    // break;
    // }
    // }
    //
    // if ((oldWd.getOverbooking() == null || !oldWd
    // .getOverbooking())
    // && ((oldWd.getSlotNumber() == null && weekday
    // .getSlotNumber() == null) || (oldWd
    // .getSlotNumber() != null && weekday
    // .getSlotNumber() != null)))
    // {
    // List<EventCalendarRegistration> registrations = weekday
    // .getEventCalendarRegistrations();
    // if (registrations == null || registrations.isEmpty())
    // {
    // weekday.setCanBeMerged(true);
    // }
    // else
    // {
    // Date minDay = null;
    // Date maxDay = null;
    // boolean slots = weekday.getSlotNumber() != null;
    // Date minStartTime = null;
    // Date maxEndTime = null;
    //
    // for (EventCalendarRegistration reg : registrations)
    // {
    // if (minDay == null
    // || minDay.after(reg.getDate()))
    // {
    // minDay = reg.getDate();
    // }
    //
    // if (maxDay == null
    // || maxDay.before(reg.getDate()))
    // {
    // maxDay = reg.getDate();
    // }
    //
    // if (!slots)
    // {
    // if (minStartTime == null
    // || minStartTime.after(reg
    // .getFromDate()))
    // {
    // minStartTime = reg.getFromDate();
    // }
    //
    // if (maxEndTime == null
    // || maxEndTime.before(reg
    // .getToDate()))
    // {
    // maxEndTime = reg.getToDate();
    // }
    // }
    // }
    //
    // if (minDay.after(this.getEntity()
    // .getValidationPeriodStartDate())
    // && maxDay.before(this.getEntity()
    // .getValidationPeriodEndDate()))
    // {
    // if (slots)
    // {
    // if (weekday.getSlotNumber().equals(
    // oldWd.getSlotNumber())
    // && weekday.getStartTimeStr()
    // .equals(oldWd
    // .getStartTimeStr())
    // && weekday.getEndTimeStr().equals(
    // oldWd.getEndTimeStr()))
    // {
    // weekday.setCanBeMerged(true);
    // }
    // }
    // else
    // {
    // Calendar minWd = Calendar.getInstance();
    // minWd.setTime(DateTimeHelper.fromString(
    // weekday.getStartTimeStr(),
    // DateTimeHelper.getTimePattern()));
    // Calendar maxWd = Calendar.getInstance();
    // maxWd.setTime(DateTimeHelper.fromString(
    // weekday.getEndTimeStr(),
    // DateTimeHelper.getTimePattern()));
    //
    // Calendar minReg = Calendar.getInstance();
    // minReg.setTime(minStartTime);
    // Calendar maxReg = Calendar.getInstance();
    // maxReg.setTime(maxEndTime);
    //
    // if (this.compareTime(minWd, minReg)
    // && this.compareTime(maxReg, maxWd))
    // {
    // weekday.setCanBeMerged(true);
    // }
    // }
    // }
    //
    // }
    // }
    // }
    //
    // if (weekday.isCanBeMerged())
    // {
    // weekday.setEventCalendar(getEntity());
    // DaoManager.save(weekday);
    // }
    // else
    // {
    // DaoManager.getSession().evict(weekday);
    // EventCalendarWeekDay newWd = new EventCalendarWeekDay(
    // weekday);
    // newWd.setEventCalendar(getEntity());
    // DaoManager.save(newWd);
    // }
    // }
    // else
    // {
    // weekday.setEventCalendar(getEntity());
    // DaoManager.save(weekday);
    // }
    // }
    //
    // }
    //
    // private boolean compareTime(Calendar min, Calendar max)
    // {
    // if (min.get(Calendar.HOUR_OF_DAY) < max.get(Calendar.HOUR_OF_DAY))
    // {
    // return true;
    // }
    // else if (min.get(Calendar.HOUR_OF_DAY) == max.get(Calendar.HOUR_OF_DAY))
    // {
    // if (min.get(Calendar.MINUTE) <= max.get(Calendar.MINUTE))
    // {
    // return true;
    // }
    // }
    //
    // return false;
    // }

    private boolean validateWeekday() {
        boolean valid = true;

        if (ValidationHelper.isNullOrEmpty(this.getWeekdaySelection())) {
            this.addRequiredFieldExeption("weekday");
            valid = false;
        }

        if (ValidationHelper
                .isNullOrEmpty(this.getEditWeekday().getStartTimeStr())) {
            this.addRequiredFieldExeption("startTime");
            valid = false;
        } else if (!ValidationHelper
                .checkTimeFormat(this.getEditWeekday().getStartTimeStr())) {
            this.addFieldExeption("startTime", "enterCorrectTime");
            valid = false;
        } else if (DateTimeHelper
                .getHour(this.getEditWeekday().getStartTimeStr()) < MIN_TIME) {
            this.addFieldExeption("startTime", "incorrectStartTime");
            valid = false;
        }

        if (ValidationHelper
                .isNullOrEmpty(this.getEditWeekday().getEndTimeStr())) {
            this.addRequiredFieldExeption("endTime");
            valid = false;
        } else if (!ValidationHelper
                .checkTimeFormat(this.getEditWeekday().getEndTimeStr())) {
            this.addFieldExeption("endTime", "enterCorrectTime");
            valid = false;
        } else if (DateTimeHelper
                .getHour(this.getEditWeekday().getEndTimeStr()) > MAX_TIME
                || (DateTimeHelper.getHour(
                this.getEditWeekday().getEndTimeStr()) == MAX_TIME
                && DateTimeHelper.getMinute(
                this.getEditWeekday().getEndTimeStr()) != 0)) {
            this.addFieldExeption("endTime", "incorrectEndTime");
            valid = false;
        }

        if (valid && !isCorrectTimeRageStr(
                this.getEditWeekday().getStartTimeStr(),
                this.getEditWeekday().getEndTimeStr())) {
            this.addFieldExeption("startTime", "enterCorrectTimeRange");
            this.addFieldExeption("endTime", "enterCorrectTimeRange",
                    Boolean.FALSE);
            valid = false;
        }

        if (ValidationHelper
                .isNullOrEmpty(this.getEditWeekday().getAvailabilityType())) {
            this.addRequiredFieldExeption("availabilityKind");
            valid = false;
        } else {
            if (this.getEditWeekday().getAvailabilityType()
                    .equals(EventCalendarWeekdayAvailabilityTypes.SLOT)
                    && ValidationHelper.isNullOrEmpty(
                    this.getEditWeekday().getSlotNumber())) {
                this.addRequiredFieldExeption("slotNumber");
                valid = false;
            }
        }

        if (ValidationHelper.isNullOrEmpty(this.getOverbookingAccepted())) {
            this.addRequiredFieldExeption("overbookingAccepted");
            valid = false;
        } else if (this.getOverbookingAccepted().equals("1")
                && this.getEditWeekday().getAvailabilityType()
                .equals(EventCalendarWeekdayAvailabilityTypes.SLOT)
                && ValidationHelper.isNullOrEmpty(
                this.getEditWeekday().getMaximumOverbooking())
                || !ValidationHelper.isNullOrEmpty(
                this.getEditWeekday().getMaximumOverbooking())
                && this.getEditWeekday().getMaximumOverbooking() > 10) {
            if (!ValidationHelper.isNullOrEmpty(
                    this.getEditWeekday().getMaximumOverbooking())
                    && this.getEditWeekday().getMaximumOverbooking() > 10) {
                this.addFieldExeption("maximumOverbooking",
                        "maxOverbookingFail");
            } else {
                this.addRequiredFieldExeption("maximumOverbooking");
            }
            valid = false;
        }

        if (ValidationHelper
                .isNullOrEmpty(this.getEditWeekday().getAverageDuration())) {
            this.addRequiredFieldExeption("averageDuration");
            valid = false;
        } else if (valid && !DateTimeHelper.timeLessThanAvailableTime(
                this.getEditWeekday().getStartTimeStr(),
                this.getEditWeekday().getEndTimeStr(), MAX_TIME, MIN_TIME,
                this.getEditWeekday().getAverageDuration())) {
            this.addFieldExeptionWithParametr("averageDuration",
                    "incorrectAverageDuration",
                    DateTimeHelper.getAvailableAverageDuration(
                            this.getEditWeekday().getStartTimeStr(),
                            this.getEditWeekday().getEndTimeStr(), MAX_TIME,
                            MIN_TIME));
            valid = false;
        }

        return valid;
    }

    private boolean isCorrectTimeRageStr(String str1, String str2) {
        int h1 = Integer.parseInt(str1.substring(0, 2));
        int h2 = Integer.parseInt(str2.substring(0, 2));

        int m1 = Integer.parseInt(str1.substring(3, 5));
        int m2 = Integer.parseInt(str2.substring(3, 5));

        if (str1.equals(str2)) {
            return false;
        } else if (h1 > h2 || h1 == h2 && m1 > m2) {
            return false;
        } else {
            return true;
        }
    }

    public void saveWeekday() {
        this.cleanValidation();
        if (validateWeekday()) {
            this.getEditWeekday().setOverbooking(
                    Integer.parseInt(this.getOverbookingAccepted()) == 1
                            ? Boolean.TRUE : Boolean.FALSE);

            if (!ValidationHelper.isNullOrEmpty(this.getWeekdayEditName())
                    && !this.getWeekdaySelection()
                    .contains(this.getWeekdayEditName())) {
                if (!ValidationHelper.isNullOrEmpty(this.getWeekdays())) {
                    for (EventCalendarWeekDay item : this.getWeekdays()) {
                        if (item.getWeekDay().name()
                                .equals(this.getWeekdayEditName())) {
                            this.getWeekdays().remove(item);
                            this.getEditWeekday().setTempId("");
                            break;
                        }
                    }
                }
            }

            if (ValidationHelper
                    .isNullOrEmpty(this.getEditWeekday().getTempId())) {
                for (String item : this.getWeekdaySelection()) {
                    boolean isWeekDayHasThisDay = false;
                    int indexOfDay = 0;
                    this.getEditWeekday().setWeekDay(Weekdays.valueOf(item));
                    for (EventCalendarWeekDay day : this.getWeekdays()) {
                        if (day.getWeekDay()
                                .equals(this.getEditWeekday().getWeekDay())) {
                            isWeekDayHasThisDay = true;
                            break;
                        }
                        indexOfDay++;
                    }
                    if (!isWeekDayHasThisDay) {
                        this.getWeekdays().add(new EventCalendarWeekDay(
                                this.getEditWeekday()));
                    } else {
                        updateEventCalendarWeekDay(this.getWeekdays().get(indexOfDay));
                    }
                }
            } else {
                List<EventCalendarWeekDay> listToAdd = new ArrayList<EventCalendarWeekDay>();
                boolean canAddToList = true;
                for (EventCalendarWeekDay item : this.getWeekdays()) {
                    if (this.getWeekdaySelection()
                            .contains(item.getWeekDay().name())) {
                        updateEventCalendarWeekDay(item);
                        //
                        // item.setAvailabilityType(this.getEditWeekday()
                        // .getAvailabilityType());
                        // item.setAverageDuration(this.getEditWeekday()
                        // .getAverageDuration());
                        // item.setEndTimeStr(this.getEditWeekday().getEndTimeStr());
                        // item.setMaximumOverbooking(this.getEditWeekday()
                        // .getMaximumOverbooking());
                        // item.setOverbooking(this.getEditWeekday().getOverbooking());
                        // item.setSlotNumber(this.getEditWeekday().getSlotNumber());
                        // item.setStartTimeStr(this.getEditWeekday()
                        // .getStartTimeStr());
                        // if (this.getWeekdaySelection().indexOf(
                        // item.getWeekDay().name()) == -1)
                        // {
                        // item.setWeekDay(Weekdays.valueOf(this
                        // .getWeekdaySelection().get(0)));
                        // }

                        List<String> oldDays = getOldDays();

                        if (this.getWeekdaySelection().size() > 1
                                && canAddToList) {
                            for (String weekdaySelection : this
                                    .getWeekdaySelection()) {
                                if (oldDays.contains(weekdaySelection)) {
                                    continue;
                                }

                                this.getEditWeekday().setWeekDay(
                                        Weekdays.valueOf(weekdaySelection));

                                listToAdd.add(new EventCalendarWeekDay(
                                        this.getEditWeekday()));
                            }

                            canAddToList = false;
                        }
                    }
                }

                this.getWeekdays().addAll(listToAdd);
            }
            this.setWeekdayEditName(null);
            this.setEditWeekday(new EventCalendarWeekDay());
            this.setAvailabilityKind(null);
            this.setOverbookingAccepted(null);
            this.setWeekdaySelection(null);
            this.setAvailabilityTypeLong(Long.valueOf(0));
            executeJS("PF('addWeekdayDialog').hide();");
        } else {

        }
    }

    private List<String> getOldDays() {
        List<String> oldDays = new ArrayList<String>();
        if (!ValidationHelper.isNullOrEmpty(this.getWeekdays())) {
            for (EventCalendarWeekDay item : this.getWeekdays()) {
                oldDays.add(item.getWeekDay().name());
            }
        }

        return oldDays;
    }

    private void updateEventCalendarWeekDay(EventCalendarWeekDay eventCalendarWeekDay) {
            eventCalendarWeekDay.setAvailabilityType(
                    this.getEditWeekday().getAvailabilityType());
            eventCalendarWeekDay
                    .setEndTimeStr(this.getEditWeekday().getEndTimeStr());
            eventCalendarWeekDay
                    .setEventCalendar(this.getEditWeekday().getEventCalendar());
            eventCalendarWeekDay.setMaximumOverbooking(
                    this.getEditWeekday().getMaximumOverbooking());
            eventCalendarWeekDay
                    .setOverbooking(this.getEditWeekday().getOverbooking());
            eventCalendarWeekDay
                    .setSlotNumber(this.getEditWeekday().getSlotNumber());
            eventCalendarWeekDay
                    .setStartTimeStr(this.getEditWeekday().getStartTimeStr());
            eventCalendarWeekDay.setAverageDuration(
                    this.getEditWeekday().getAverageDuration());
    }

    public void editWeekday()
            throws HibernateException, PersistenceBeanException {
        EventCalendarWeekDay editItem = null;
        for (EventCalendarWeekDay item : this.getWeekdays()) {
            if (item.getWeekDay().name().equals(this.getWeekdayEditName())) {
                editItem = item;
            }
        }

        this.getWeekdaySelection().clear();
        this.getWeekdaySelection().add(editItem.getWeekDay().name());
        this.setEditWeekday(editItem);
        this.setAvailabilityKind(editItem.getAvailabilityKindName().equals(
                EventCalendarWeekdayAvailabilityTypes.SLOT) ? "1" : "2");
        this.setOverbookingAccepted(
                editItem.getOverbooking().booleanValue() ? "1" : "2");
    }

    public void addWeekDay() throws HibernateException, PersistenceBeanException {
        cleanValidation();
        this.getWeekdaySelection().clear();
        this.setEditWeekday(new EventCalendarWeekDay());
        this.setAvailabilityKind(null);
        this.setOverbookingAccepted(null);
        this.setAvailabilityTypeLong(Long.valueOf(0));
    }

    public void editDiscardDay() {
        EventCalendarDiscardDay editItem = null;
        if (!ValidationHelper.isNullOrEmpty(this.getDiscardDayEditId())) {
            for (EventCalendarDiscardDay item : this.getDiscardDays()) {
                if (item.getTempId() != null
                        && item.getTempId().equals(this.getDiscardDayEditId())) {
                    editItem = item;
                }
            }
        }
        this.setEditDiscardDay(editItem);
    }

    public void deleteWeekday() {
        int index = -1;
        for (EventCalendarWeekDay item : this.getWeekdays()) {
            if (item.getWeekDay().name().equals(this.getWeekdayEditName())) {
                index = this.getWeekdays().indexOf(item);
            }
        }

        if (index > -1) {
            this.getWeekdays().remove(index);
        }
    }

    public void validateDeleteWeekday() {
        Long calendarCount = 0l;
        try {
            calendarCount = DaoManager.getCount(EventCalendarRegistration.class,
                    "id", new Criterion[]{
                            Restrictions.eq("eventCalendarWeekday.id",
                                    getWeekdayIdByName())
                    });
        } catch (IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        if (calendarCount <= 0l) {
            executeJS("PF('deleteWeekdayDialog').show();");
        } else {
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    ResourcesHelper.getValidation("deleteDayFailHeader"),
                    ResourcesHelper.getValidation("deleteDayFail"));
        }
    }

    public Long getWeekdayIdByName() {
        for (EventCalendarWeekDay item : this.getWeekdays()) {
            if (item.getWeekDay().name().equals(this.getWeekdayEditName())) {
                return item.getId();
            }
        }
        return null;
    }

    public void deleteDiscardDay() {
        int index = -1;
        for (EventCalendarDiscardDay item : this.getDiscardDays()) {
            if (item.getTempId().equals(this.getDiscardDayEditId())) {
                index = this.getDiscardDays().indexOf(item);
            }
        }

        if (index > -1) {
            this.getDiscardDays().remove(index);
        }
    }

    private boolean validateDiscardDay() {
        boolean valid = true;

        if (ValidationHelper
                .isNullOrEmpty(this.getEditDiscardDay().getDiscardDay())) {
            this.addRequiredFieldExeption("discardDay");
            valid = false;
        }

        return valid;
    }

    public void saveDiscardDay() {
        this.cleanValidation();
        if (!validateDiscardDay()) {
            this.setDiscardDayValidationFailed(true);
            return;
        }

        if (ValidationHelper
                .isNullOrEmpty(this.getEditDiscardDay().getTempId())) {
            this.getEditDiscardDay().setTempId(UUID.randomUUID().toString());
            this.getDiscardDays()
                    .add(new EventCalendarDiscardDay(this.getEditDiscardDay()));
        } else {
            for (EventCalendarDiscardDay item : this.getDiscardDays()) {
                if (item.getTempId() != null
                        && item.getTempId().equals(this.getDiscardDayEditId())) {
                    item.setDiscardDay(
                            this.getEditDiscardDay().getDiscardDay());
                    item.setEachYear(this.getEditDiscardDay().getEachYear());
                }
            }
        }
        this.setDiscardDayValidationFailed(false);
        this.setDiscardDayEditId(null);
        this.setEditDiscardDay(new EventCalendarDiscardDay());
    }

    public List<SelectItem> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<SelectItem> statuses) {
        this.statuses = statuses;
    }

    public EventCalendarStatuses getStatus() {
        return status;
    }

    public void setStatus(EventCalendarStatuses status) {
        this.status = status;
    }

    @SuppressWarnings("unchecked")
    public List<EventCalendarWeekDay> getWeekdays() {
        return (List<EventCalendarWeekDay>) this.getViewState()
                .get("EventCalendarWeekDay");
    }

    public void setWeekdays(List<EventCalendarWeekDay> weekdays) {
        this.getViewState().put("EventCalendarWeekDay", weekdays);
    }

    @SuppressWarnings("unchecked")
    public List<EventCalendarDiscardDay> getDiscardDays() {
        return (List<EventCalendarDiscardDay>) this.getViewState()
                .get("EventCalendarDiscardDay");
    }

    public void setDiscardDays(List<EventCalendarDiscardDay> discardDays) {
        this.getViewState().put("EventCalendarDiscardDay", discardDays);
    }

    public EventCalendarWeekDay getEditWeekday() {
        return editWeekday;
    }

    public void setEditWeekday(EventCalendarWeekDay editWeekday) {
        this.editWeekday = editWeekday;
    }

    public List<SelectItem> getAvWeekday() {
        return avWeekday;
    }

    public void setAvWeekday(List<SelectItem> avWeekday) {
        this.avWeekday = avWeekday;
    }

    public String getAvailabilityKind() {
        return availabilityKind;
    }

    public void setAvailabilityKind(String availabilityKind) {
        this.availabilityKind = availabilityKind;
    }

    public String getOverbookingAccepted() {
        return overbookingAccepted;
    }

    public void setOverbookingAccepted(String overbookingAccepted) {
        this.overbookingAccepted = overbookingAccepted;
    }

    public boolean getDiscardDayValidationFailed() {
        return this.getViewState().get("DiscardDayvalidateFail") == null ? false
                : (Boolean) this.getViewState().get("DiscardDayvalidateFail");
    }

    public void setDiscardDayValidationFailed(boolean value) {
        this.getViewState().put("DiscardDayvalidateFail",
                Boolean.valueOf(value));
    }

    public boolean getWeekDayValidationFailed() {
        return this.getViewState().get("WeekDayValidationFailed") == null
                ? false
                : (Boolean) this.getViewState().get("WeekDayValidationFailed");
    }

    public void setWeekDayValidationFailed(boolean value) {
        this.getViewState().put("WeekDayValidationFailed",
                Boolean.valueOf(value));
    }

    public EventCalendarDiscardDay getEditDiscardDay() {
        return editDiscardDay;
    }

    public void setEditDiscardDay(EventCalendarDiscardDay editDiscardDay) {
        this.editDiscardDay = editDiscardDay;
    }

    public String getWeekdayEditName() {
        return (String) this.getViewState().get("weekdayEditName");
    }

    public void setWeekdayEditName(String weekdayEditName) {
        this.getViewState().put("weekdayEditName", weekdayEditName);
    }

    public String getDiscardDayEditId() {
        return (String) this.getViewState().get("discardDayEditId");
    }

    public void setDiscardDayEditId(String discardDayEditId) {
        this.getViewState().put("discardDayEditId", discardDayEditId);
    }

    public List<String> getWeekdaySelection() {
        return weekdaySelection;
    }

    public void setWeekdaySelection(List<String> weekdaySelection) {
        this.weekdaySelection = weekdaySelection;
    }

    public String getSlotSelectionType() {
        return slotSelectionType;
    }

    public void setSlotSelectionType(String slotSelectionType) {
        this.slotSelectionType = slotSelectionType;
    }

    public List<SelectItem> getSlotSelectionTypes() {
        return slotSelectionTypes;
    }

    public void setSlotSelectionTypes(List<SelectItem> slotSelectionTypes) {
        this.slotSelectionTypes = slotSelectionTypes;
    }

    @SuppressWarnings("unchecked")
    public List<EventCalendarWeekDay> getOldWeekDays() {
        return (List<EventCalendarWeekDay>) this.getViewState()
                .get("oldWeekDays");
    }

    public void setOldWeekDays(List<EventCalendarWeekDay> oldWeekDays) {
        this.getViewState().put("oldWeekDays", oldWeekDays);
    }

    public Long getAvailabilityTypeLong() {
        if (this.getEditWeekday() != null
                && this.getEditWeekday().getAvailabilityType() != null) {
            int i = 0;
            for (EventCalendarWeekdayAvailabilityTypes type : EventCalendarWeekdayAvailabilityTypes
                    .values()) {
                i++;
                if (type.equals(this.getEditWeekday().getAvailabilityType())) {
                    availabilityTypeLong = (long) i;
                }
            }
        }
        return availabilityTypeLong;
    }

    public void setAvailabilityTypeLong(Long availabilityTypeLong) {
        this.availabilityTypeLong = availabilityTypeLong;
        if (this.availabilityTypeLong != null
                && (this.availabilityTypeLong.intValue() == 1
                || this.availabilityTypeLong.intValue() == 2)) {
            this.getEditWeekday()
                    .setAvailabilityType(EventCalendarWeekdayAvailabilityTypes
                            .values()[this.availabilityTypeLong.intValue()
                            - 1]);
        }
    }

    public List<SelectItem> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<SelectItem> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Long getDiagnosticId() {
        return diagnosticId;
    }

    public void setDiagnosticId(Long diagnosticId) {
        this.diagnosticId = diagnosticId;
    }
}
