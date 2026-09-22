package it.nexera.ris.persistence.beans.entities.domain.common;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

/**
 * Classe per
 */

@javax.persistence.Entity
@Table(name = "service_execution_time")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "SERV_EXEC_TIME_SEQ", allocationSize = 1)
public class ServiceExecutionTime extends IndexedEntity {
    private static final long serialVersionUID = 2668431651936431560L;

    @Column(name = "service_key", unique = true)
    private String serviceKey;

    @Column(name = "last_exec_time")
    private Date lastExecTime;

    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * Ottiene serviceKey
     *
     * @return il serviceKey del ServiceExecutionTime
     */
    public String getServiceKey() {
        return serviceKey;
    }

    /**
     * Imposta serviceKey del ServiceExecutionTime
     *
     * @param serviceKey per impostare
     */
    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    /**
     * Ottiene lastExecTime
     *
     * @return il lastExecTime del ServiceExecutionTime
     */
    public Date getLastExecTime() {
        return lastExecTime;
    }

    /**
     * Imposta lastExecTime del ServiceExecutionTime
     *
     * @param lastExecTime per impostare
     */
    public void setLastExecTime(Date lastExecTime) {
        this.lastExecTime = lastExecTime;
    }

    /**
     * Ottiene ipAddress
     *
     * @return il ipAddress del ServiceExecutionTime
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Imposta ipAddress del ServiceExecutionTime
     *
     * @param ipAddress per impostare
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
