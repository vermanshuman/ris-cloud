package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.EmailTemplate;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.TemplateEntity;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class MailHelper {

//    public static void sendEmail(User user, EmailTemplate.EmailTypes type) throws MessagingException, IllegalAccessException, PersistenceBeanException, InstantiationException {
//        String serviceIp = ApplicationSettingsHolder.getInstance()
//                .getByKey(ApplicationSettingsKeys.SMTP_SERVER).getValue();
//        String password = ApplicationSettingsHolder.getInstance()
//                .getByKey(ApplicationSettingsKeys.SMTP_PASSWORD).getValue();
//        String username = ApplicationSettingsHolder.getInstance()
//                .getByKey(ApplicationSettingsKeys.SMTP_USERNAME).getValue();
//        String port = ApplicationSettingsHolder.getInstance()
//                .getByKey(ApplicationSettingsKeys.SMTP_PORT).getValue();
//        if (ValidationHelper.isNullOrEmpty(serviceIp)
//                || ValidationHelper.isNullOrEmpty(password)
//                || ValidationHelper.isNullOrEmpty(username)
//                || ValidationHelper.isNullOrEmpty(port)) {
//            throw new MessagingException("Not all params are set for SMTP");
//        }
//        EmailTemplate email = DaoManager.get(EmailTemplate.class, new Criterion[]{
//                Restrictions.eq("emailType", type)
//        });
//        String subject = email.getSubject();
//        String body = email.getBody();
//        TemplateEntity wrappedEntity = new TemplateEntity(user, UserHolder.getInstance().getCurrentUser());
//        String emailBody = TemplateToPdfHelper.replaceMailTags(body, wrappedEntity);
//        sendEmail(true, serviceIp, password, true, username, port, username, user.getEmail(),
//                subject, emailBody);
//    }

    private static void sendEmail(boolean auth, String serviceIp, String password, boolean secure, String username,
                                  String port, String fromEmail, String toEmail, String subject, String messageBody) throws MessagingException {
        Session mailSession;
        Properties props = System.getProperties();
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.host", serviceIp);
        if (auth) {
            props.put("mail.smtp.auth", Boolean.TRUE);
            Authenticator authenticator = new MyAuthenticator(username, password);
            mailSession = Session.getDefaultInstance(props, authenticator);
        } else {
            mailSession = Session.getDefaultInstance(props);
        }

        MimeMessage message = new MimeMessage(mailSession);

        Transport t;

        if (secure) {
            t = mailSession.getTransport("smtps");
        } else {
            t = mailSession.getTransport("smtp");
        }
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(subject);
        message.setText(messageBody);
        message.setContent(messageBody, "text/html; charset=utf-8");
        t.connect(serviceIp, username, password);
        t.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        t.close();
    }

    static class MyAuthenticator extends Authenticator {
        private String user;
        private String password;

        MyAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            String user = this.user;
            String password = this.password;
            return new PasswordAuthentication(user, password);
        }
    }

    public enum EmailTag {
        CURRENT_DATE(1L, "current_date", "getCurrentDate"),
        PAGE_BREAK(2L, "page_break"),
        USERNAME(3L, "username", "getLogin"),
        PASSWORD(4L, "password", "getNoEncryptedPassword");

        private final Long id;

        private final String tag;

        private String method;

        EmailTag(Long id, String tag, String method) {
            this.id = id;
            this.tag = tag;
            this.method = method;
        }

        EmailTag(Long id, String tag) {
            this.id = id;
            this.tag = tag;
        }

        public String getTag() {
            return '%' + tag + '%';
        }

        public String getMethod() {
            return method;
        }

        public Long getId() {
            return id;
        }
    }
}
