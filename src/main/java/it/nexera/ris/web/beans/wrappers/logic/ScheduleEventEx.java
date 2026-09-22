package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.ScheduleViewTypes;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import org.primefaces.model.DefaultScheduleEvent;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ScheduleEventEx extends DefaultScheduleEvent<Object> {
    private static final String NULL = "null";

    private Integer slotNumber;

    private Boolean overbooked;

    private Long dbId;

    private Long requestItemId;

    private static String SEPARATOR = "separator";

    private static final long serialVersionUID = -979475859560230122L;

    public ScheduleEventEx() {
        super();
    }

    public ScheduleEventEx(Event event, ScheduleViewTypes viewTypes) {
        super();
        this.setAllDay(event.isAllDay());
        this.setDbId(event.getEcId());
        this.setId(event.getId().toString());
        this.setStartDate(DateTimeHelper.toLocalDateTime(event.getStartDate()));
        this.setEndDate(DateTimeHelper.toLocalDateTime(event.getEndDate()));
        this.setSlotNumber(event.getSlotNumber());
        this.setOverbooked(event.isOverbooked());
        this.setRequestItemId(event.getRadiologyExamRequestItemId());
        this.setStyleClass("free_schedule_cell");

        if (event.isAllDay()) {
            this.setTitle(event.getName());
            if (event.isFull()) {
                this.setStyleClass("assign_schedule_cell");
                this.setEditable(false);
                if (Boolean.TRUE.equals(event.getForwarded())) {
                    this.setStyleClass("eventFromHl7");
                }
            } else if (!event.isBusy()) {
                this.setStyleClass("empty_schedule_cell");
                this.setEditable(true);
            } else if (event.isOverbookingAvailable()) {
                this.setStyleClass("overbooked_free_schedule_cell");
                this.setEditable(true);
            } else {
                this.setStyleClass("free_schedule_cell");
                this.setEditable(true);
            }
        } else {
            if (event.isPatientChangeStatus()) {
                this.setStyleClass("patient_change_status_schedule_cell");
                this.setTitle(getCurrentTitleByViewType(viewTypes, event));
                this.setEditable(false);
            } else if (event.isOverbooked()
                    && ValidationHelper.isNullOrEmpty(getCurrentTitleByViewType(viewTypes, event))) {
                this.setStyleClass("overbooked_free_schedule_cell");
                this.setTitle(ResourcesHelper
                        .getString("dateConfirmationOverbooking"));
                this.setEditable(true);
            } else if (event.isOverbooked()) {
                this.setStyleClass("overbooked_assign_schedule_cell");
                this.setTitle(getCurrentTitleByViewType(viewTypes, event));
                this.setEditable(false);
            } else if (!ValidationHelper.isNullOrEmpty(getCurrentTitleByViewType(viewTypes, event))) {
                this.setStyleClass("assign_schedule_cell");
                if (Boolean.TRUE.equals(event.getForwarded())) {
                    this.setStyleClass("eventFromHl7");
                }
                this.setTitle(getCurrentTitleByViewType(viewTypes, event));
                this.setEditable(false);
            } else {
                this.setStyleClass("free_schedule_cell");
                if (event.getSlotNumber() != null) {
                    this.setTitle(String.format("%s %d", ResourcesHelper
                            .getString("dateConfirmationSlotNumber"), event
                            .getSlotNumber()));
                } else {
                    this.setTitle("");
                }
                this.setEditable(true);
            }
        }

        long startTimeInMillis = getStartDate().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long endTimeInMillis = getEndDate().atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        String classStr = "idClass" + this.getDbId() + SEPARATOR
                + startTimeInMillis + SEPARATOR + endTimeInMillis
                + SEPARATOR + this.getOverbooked() + SEPARATOR
                + this.getSlotNumber() + SEPARATOR + this.getRequestItemId();

        this.setStyleClass(this.getStyleClass() + ' '
                + classStr.replaceAll(" ", "@"));
    }


    private String getCurrentTitleByViewType(ScheduleViewTypes type, Event event) {
        String str = "";
        if (ScheduleViewTypes.timeGridDay.equals(type)) {
            str = event.getPatientName();
        } else if (ScheduleViewTypes.timeGridWeek.equals(type)) {
            str = event.getPatientWeek();
        } else if (ScheduleViewTypes.dayGridMonth.equals(type)) {
            str = event.getPatientWeek();
        }
        return str;
    }

    public ScheduleEventEx(String idStr) throws ParseException {
        super();
        String[] params = idStr.split(SEPARATOR);

        this.setDbId(params[0].contains(NULL) ? null : Long
                .parseLong(params[0]));

        if (!params[1].contains(NULL)) {
            long startTimeInMillis = Long.parseLong(params[1]);
            this.setStartDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeInMillis), ZoneId.systemDefault()));
        }

        if (!params[2].contains(NULL)) {
            long endTimeInMillis = Long.parseLong(params[2]);
            this.setEndDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeInMillis), ZoneId.systemDefault()));
        }

        this.setOverbooked(params[3].contains(NULL) ? null : Boolean
                .parseBoolean(params[3]));
        this.setSlotNumber(params[4].contains(NULL) ? null : Integer
                .parseInt(params[4]));
        this.setRequestItemId(params[5].contains(NULL) ? null : Long
                .parseLong(params[5]));
    }

    public ScheduleEventEx updateValues(Event event) {
        return this;
    }

    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public Boolean getOverbooked() {
        return overbooked;
    }

    public void setOverbooked(Boolean overbooked) {
        this.overbooked = overbooked;
    }

    public Long getRequestItemId() {
        return requestItemId;
    }

    public void setRequestItemId(Long requestItemId) {
        this.requestItemId = requestItemId;
    }


}
