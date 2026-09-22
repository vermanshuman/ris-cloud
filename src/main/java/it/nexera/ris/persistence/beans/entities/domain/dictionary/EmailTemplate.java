package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "dic_email_template")
@SequenceGenerator(name = "ID_SEQ_GEN", initialValue = 1, sequenceName = "DIC_EMAIL_TEMPLATE_SEQ", allocationSize = 1)
public class EmailTemplate extends IndexedEntity {

    private static final long serialVersionUID = 4736099875265872888L;

    @Column(name = "emailtype")
    @Enumerated(EnumType.STRING)
    private EmailTypes emailType;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body", columnDefinition = "CLOB")
    private String body;

    public EmailTypes getEmailType() {
        return emailType;
    }

    public void setEmailType(EmailTypes emailType) {
        this.emailType = emailType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public enum EmailTypes {
        CREATE_USER;

        @Override
        public String toString() {
            return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
        }
    }

}
