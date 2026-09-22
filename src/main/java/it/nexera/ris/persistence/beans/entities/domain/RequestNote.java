package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "REQUEST_NOTE")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "REQUEST_NOTE_SEQ", allocationSize = 1)
public class RequestNote extends IndexedEntity {
    private static final long serialVersionUID = -3034678913265231956L;

    @Transient
    protected static transient final Logger log = LogManager.getLogger(RequestNote.class);

    @Column(name = "note", length = 1024)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rad_exam_request_id")
    private RadiologyExamRequest radiologyExamRequest;

    @Column(name = "from_sio")
    private Boolean fromSio;

    @Column(name = "perform_date")
    private Date performDate;

    @Transient
    private String userFullName;

    @Transient
    public String getCreateUserFullName() {
        if (userFullName == null) {
            if (Boolean.TRUE.equals(fromSio)) {
                userFullName = ResourcesHelper.getString("noteFromSioUser");
                return userFullName;
            }
            try {
                List<Object> names = DaoManager.getFields(User.class, new Criterion[]{
                        Restrictions.eq("id", this.getCreateUserId())
                }, null, false, "lastName", "firstName");

                if (!ValidationHelper.isNullOrEmpty(names)
                        && !ValidationHelper
                        .isNullOrEmpty(((Object[]) names.get(0))[0])
                        && !ValidationHelper
                        .isNullOrEmpty(((Object[]) names.get(0))[1])) {
                    userFullName = String.format("%s %s",
                            ((Object[]) names.get(0))[0],
                            ((Object[]) names.get(0))[1]);
                    return userFullName;
                } else {
                    userFullName = "";
                    return userFullName;
                }
            } catch (HibernateException | IllegalAccessException
                    | PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }

        return userFullName;
    }

    public boolean getCanEditAndDelete() {
        if (getCreateUserId() != null
                && UserHolder.getInstance().getCurrentUser() != null) {
            return getCreateUserId().equals(
                    UserHolder.getInstance().getCurrentUser().getId());
        } else {
            return false;
        }
    }

    public String getNote() {
        return note == null ? "" : note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public RadiologyExamRequest getRadiologyExamRequest() {
        return radiologyExamRequest;
    }

    public void setRadiologyExamRequest(
            RadiologyExamRequest radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    public Boolean getFromSio() {
        return fromSio;
    }

    public void setFromSio(Boolean fromSio) {
        this.fromSio = fromSio;
    }

    public Date getPerformDate() {
        return performDate;
    }

    public void setPerformDate(Date performDate) {
        this.performDate = performDate;
    }
}
