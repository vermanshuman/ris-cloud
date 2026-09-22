package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "app_token")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "APP_TOKEN_SEQ", allocationSize = 1)
public class AppToken extends IndexedEntity {
    private static final long serialVersionUID = -6070893047935033011L;

    public transient final Logger log = LogManager.getLogger(AppToken.class);

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 1000)
    private String token;

    @Column(name = "expiration_date")
    private Date expirationDate;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
