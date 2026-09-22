package it.nexera.ris.persistence;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.factories.CustomLibLoggerFactory;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import it.nexera.ris.web.common.AtmosphereUtil;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HttpSessionCollector implements HttpSessionListener {

    private static final List<HttpSession> sessions = new CopyOnWriteArrayList<>();

    private static final Integer DEFAULT_TIMEOUT_WITHOUT_USER = 5; //in minutes

    private static final Logger serviceActivityLog = CustomLibLoggerFactory.getServiceInfoLogger();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        sessions.add(event.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        sessions.remove(event.getSession());
    }

    public static synchronized void checkUniqueUser(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)
                && !ValidationHelper.isNullOrEmpty(sessions)) {
            for (HttpSession session : sessions) {
                try {
                    UserWrapper user = (UserWrapper) session
                            .getAttribute("USER_HOLDER_ATTRIBUTE");

                    if (user != null && id.equals(user.getId())) {
                        StringBuilder sb = new StringBuilder();

                        sb.append(user.getId());
                        sb.append("__separator__");
                        sb.append(SessionHelper.get("my_session_id"));

                        AtmosphereUtil.broadcastTo("/notify", sb.toString());
                        session.setMaxInactiveInterval(30);
                    }
                } catch (IllegalStateException ignored){
                }
            }
        }
    }

    public static synchronized void cleanEmptySessions(String serviceName) {
        if (!ValidationHelper.isNullOrEmpty(sessions)) {
            int timeoutWithoutUser = loadTimeoutWithoutUser();
            for (HttpSession session : sessions) {
                if (session.getAttribute(UserHolder.USER_HOLDER_ATTRIBUTE) == null
                        && DateTimeHelper.differenceInMinutes(new Date(session.getLastAccessedTime()), new Date())
                        > timeoutWithoutUser) {
                    String ip = (String) session.getAttribute("javamelody.remoteAddr");
                    LogHelper.log(serviceActivityLog, String.format("Service <%s> killed empty session on <%s>",
                            serviceName, ip));
                    session.invalidate();
                }
            }
        }
    }

    public static synchronized void checkEmptySession(String serviceName) {
        if (!ValidationHelper.isNullOrEmpty(sessions)) {
            for (HttpSession session : sessions) {
                if (DateTimeHelper.differenceInMinutes(
                        new Date(session.getLastAccessedTime()),
                        new Date()) > loadTimeout()
                        && session
                        .getAttribute("ON_DOCUMENT_GENERATION") != null
                        && ((Boolean) session
                        .getAttribute("ON_DOCUMENT_GENERATION"))) {
                    UserWrapper user = (UserWrapper) session.getAttribute(UserHolder.USER_HOLDER_ATTRIBUTE);
                    String ip = (String) session.getAttribute("javamelody.remoteAddr");
                    LogHelper.log(serviceActivityLog, String.format("Service <%s> killed session with user login <%s> on <%s>",
                            serviceName, user == null ? "" : user.getLogin(), ip));
                    session.invalidate();
                }
            }
        }
    }

    public static void closeSessionById(String sessionId) {
        if (!ValidationHelper.isNullOrEmpty(sessions)) {
            for (HttpSession session : sessions) {
                if (session.getId().equals(sessionId)) {
                    session.invalidate();
                    return;
                }
            }
        }
    }

    private static int loadTimeout() {
        if (!ValidationHelper
                .isNullOrEmpty(ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT))
                && !ValidationHelper
                .isNullOrEmpty(ApplicationSettingsHolder.getInstance()
                        .getByKey(
                                ApplicationSettingsKeys.SESSION_TIMEOUT)
                        .getValue())) {
            return Integer.parseInt(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT)
                    .getValue());
        }

        return 120;
    }

    private static int loadTimeoutWithoutUser() {
        if (!ValidationHelper.isNullOrEmpty(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT_WITHOUT_USER))
                && !ValidationHelper.isNullOrEmpty(ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT_WITHOUT_USER).getValue())) {
            return Integer.parseInt(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT_WITHOUT_USER).getValue());
        }

        return DEFAULT_TIMEOUT_WITHOUT_USER;
    }

}
