package it.nexera.ris.persistence.beans.entities.domain.history;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DVDProducer;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Pacs;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "historical_produce_cd")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "HISTORICAL_PRODUCE_CD_SEQ", allocationSize = 1)
public class HistoricalProduceCD extends IndexedEntity {

    private static final long serialVersionUID = -8698065920451254099L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "radiology_exam_request_id")
    private RadiologyExamRequest radiologyExamRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pacs_id")
    private Pacs pacs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_id")
    private DVDProducer robot;

    @Column(name = "action_date")
    private Date actionDate;

    public RadiologyExamRequest getRadiologyExamRequest() {
        return radiologyExamRequest;
    }

    public void setRadiologyExamRequest(RadiologyExamRequest radiologyExamRequest) {
        this.radiologyExamRequest = radiologyExamRequest;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Pacs getPacs() {
        return pacs;
    }

    public void setPacs(Pacs pacs) {
        this.pacs = pacs;
    }

    public DVDProducer getRobot() {
        return robot;
    }

    public void setRobot(DVDProducer robot) {
        this.robot = robot;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }
}
