package it.nexera.ris.persistence.beans.entities.domain.calendar;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItemShort;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.persistence.*;
import java.util.Calendar;
import java.util.List;

@javax.persistence.Entity
@Table(name = "event_calendar_registration")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "EVENT_CALENDAR_REG_SEQ", allocationSize = 1)
public class EventCalendarRegistrationShort
        extends EventCalendarRegistrationBase {
    private static final long serialVersionUID = -7399962569346355511L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_calendar_weekday_id", foreignKey = @ForeignKey(name = "FK_EV_CAL_REG_EV_CAL_WEEKDAY"))
    private EventCalendarWeekDay eventCalendarWeekday;

    @Column(name = "request_item_id")
    private Long radiologyExamRequestItemId;

    @Transient
    private String toStringName;

    @Transient
    private RadiologyExamRequestItem radiologyExamRequestItem;

    @Transient
    private String patientSurname;

    @Transient
    private String patientName;

    @Transient
    private String radExamDescription;

    @Transient
    private Boolean forwarded;

    @Transient
    private Long radiologyExamRequestId;

    @Transient
    private String asapSectorDescription;

    @Transient
    private String asapActivityLine;

    public RadiologyExamRequestItem getRadiologyExamRequestItem() {
        if (radiologyExamRequestItem == null && getRadiologyExamRequestItemId() != null) {
            try {
                radiologyExamRequestItem = DaoManager.get(
                        RadiologyExamRequestItem.class,
                        getRadiologyExamRequestItemId());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return radiologyExamRequestItem;
    }

    private void loadLazyFields(Calendar c1) {
        try {
            Calendar lessCalendar = Calendar.getInstance();
            lessCalendar.add(Calendar.DAY_OF_MONTH, -30);
            List<Object> fields;
            if (c1.after(lessCalendar)) {
                fields = DaoManager
                        .getFields(RadiologyExamRequestItemShort.class, new Criterion[]{
                                        Restrictions.eq("id",
                                                this.getRadiologyExamRequestItemId())
                                }, new CriteriaAlias[]{
                                        new CriteriaAlias("radiologyExamRequest",
                                                "radiologyExamRequest", JoinType.INNER_JOIN),
                                        new CriteriaAlias("radiologyExamRequest.patient", "patient",
                                                JoinType.INNER_JOIN),
                                        new CriteriaAlias("radiologyExam", "radiologyExam",
                                                JoinType.INNER_JOIN)
                                }, true, "patient.surname", "patient.name",
                                "radiologyExamRequest.id",
                                "radiologyExamRequest.asapSectorDescription",
                                "radiologyExam.description", "forwarded", "radiologyExamRequest.asapActivityLine");
            } else {
                fields = DaoManager
                        .getFields(RadiologyExamRequestItem.class, new Criterion[]{
                                        Restrictions.eq("id",
                                                this.getRadiologyExamRequestItemId())
                                }, new CriteriaAlias[]{
                                        new CriteriaAlias("radiologyExamRequest",
                                                "radiologyExamRequest", JoinType.INNER_JOIN),
                                        new CriteriaAlias("radiologyExamRequest.patient", "patient",
                                                JoinType.INNER_JOIN),
                                        new CriteriaAlias("radiologyExam", "radiologyExam",
                                                JoinType.INNER_JOIN)
                                }, true, "patient.surname", "patient.name",
                                "radiologyExamRequest.id",
                                "radiologyExamRequest.asapSectorDescription",
                                "radiologyExam.description", "forwarded", "radiologyExamRequest.asapActivityLine");
            }
            if (!ValidationHelper.isNullOrEmpty(fields)) {
                Object[] field = (Object[]) fields.get(0);

                patientSurname = (String) field[0];
                patientName = (String) field[1];
                radiologyExamRequestId = (Long) field[2];
                asapSectorDescription = (String) field[3];
                radExamDescription = (String) field[4];
                forwarded = (Boolean) field[5];
                asapActivityLine = (String) field[6];
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public String getPatientFullname(Calendar c1) {
        return String.format("%s %s",
                this.getPatientName(c1) == null ? "" : this.getPatientName(c1),
                this.getPatientSurname(c1) == null ? ""
                        : this.getPatientSurname(c1));
    }

    public String getPatientSurnameName(Calendar c1) {
        return String.format("%s %s",
                this.getPatientSurname(c1) == null ? ""
                        : this.getPatientSurname(c1),
                this.getPatientName(c1) == null ? "" : this.getPatientName(c1));
    }

    @Override
    public String toString() {
        if (toStringName == null) {
            List<Object> names = null;

            try {
                names = DaoManager.getFields(EventCalendarRegistration.class,
                        new Criterion[]{
                                Restrictions.eq("id", this.getId())
                        }, new CriteriaAlias[]{
                                new CriteriaAlias("eventCalendarWeekday",
                                        "eventCalendarWeekday",
                                        JoinType.INNER_JOIN),
                                new CriteriaAlias(
                                        "eventCalendarWeekday.eventCalendar",
                                        "eventCalendar", JoinType.INNER_JOIN)
                        }, true, "eventCalendar.name");
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            if (!ValidationHelper.isNullOrEmpty(names)) {
                toStringName = (String) names.get(0);
            } else {
                toStringName = "";
            }
        }

        return toStringName;
    }

    public Long getRadiologyExamRequestItemId() {
        return radiologyExamRequestItemId;
    }

    public EventCalendarWeekDay getEventCalendarWeekday() {
        return eventCalendarWeekday;
    }

    public String getPatientSurname(Calendar c1) {
        if (patientSurname == null) {
            loadLazyFields(c1);
        }

        return patientSurname;
    }

    public String getPatientName(Calendar c1) {
        if (patientName == null) {
            loadLazyFields(c1);
        }

        return patientName;
    }

    public String getRadExamDescription(Calendar c1) {
        if (radExamDescription == null) {
            loadLazyFields(c1);
        }

        return radExamDescription;
    }

    public Boolean getForwarded(Calendar c1) {
        if (forwarded == null) {
            loadLazyFields(c1);
        }

        return forwarded;
    }

    public Long getRadiologyExamRequestId(Calendar c1) {
        if (radiologyExamRequestId == null) {
            loadLazyFields(c1);
        }

        return radiologyExamRequestId;
    }

    public String getAsapSectorDescription(Calendar c1) {
        if (asapSectorDescription == null) {
            loadLazyFields(c1);
        }

        return asapSectorDescription;
    }

    public String getAsapActivityLine(Calendar c1) {
        if (asapActivityLine == null) {
            loadLazyFields(c1);
        }

        return asapActivityLine;
    }
}
