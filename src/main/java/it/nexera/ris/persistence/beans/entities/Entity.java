package it.nexera.ris.persistence.beans.entities;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import org.hibernate.HibernateException;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Date;

@MappedSuperclass
public abstract class Entity implements IEntity {

    private static final long serialVersionUID = -4635485731205442953L;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "create_user_id")
    private Long createUserId;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "update_user_id")
    private Long updateUserId;

    public boolean isNew() {
        if (this.getId() == null || this.getId() <= 0 || this.isCustomId()) {
            return true;
        }

        return false;
    }

    public boolean getIsNew() {
        return isNew();
    }

    @Transient
    public String getStrId() {
        return String.valueOf(getId());
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Long getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Long updateUserId) {
        this.updateUserId = updateUserId;
    }

    public boolean getDeletable() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return true;
    }

    public boolean getEditable() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return true;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "\r\n \"createDate\":" + createDate +
                "\r\n \"createUserId\":" + createUserId +
                "\r\n \"updateDate\":" + updateDate +
                "\r\n \"updateUserId\":" + updateUserId +
                '}';
    }
}
