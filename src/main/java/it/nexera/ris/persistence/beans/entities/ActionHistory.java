package it.nexera.ris.persistence.beans.entities;

import it.nexera.ris.common.enums.PatientHistoryActionType;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.util.Date;

@MappedSuperclass
public class ActionHistory extends IndexedEntity {
    private static final long serialVersionUID = -8147724162044897783L;

    @Column(name = "action_date")
    private Date actionDate;

    @Column
    private String username;

    @Column
    private String ip;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private PatientHistoryActionType actionType;

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public PatientHistoryActionType getActionType() {
        return actionType;
    }

    public void setActionType(PatientHistoryActionType actionType) {
        this.actionType = actionType;
    }

}
