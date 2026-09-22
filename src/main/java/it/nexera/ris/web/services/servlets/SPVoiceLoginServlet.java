package it.nexera.ris.web.services.servlets;

import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class SPVoiceLoginServlet extends HttpServlet {

    public static final Log log = LogFactory.getLog(SPVoiceLoginServlet.class);

    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String UTF_8_CHARACTER_ENCODING = StandardCharsets.UTF_8.name();

    private SPVoiceLoginService service;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        service = new SPVoiceLoginService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        SPVoiceLoginService.SimpleHttpResponse response;

        HttpSession httpSession = req.getSession(false);
        if (httpSession != null && httpSession.getAttribute(UserHolder.USER_HOLDER_ATTRIBUTE) != null) {

            UserWrapper userWrapper = (UserWrapper) httpSession.getAttribute(UserHolder.USER_HOLDER_ATTRIBUTE);

            @SuppressWarnings("unchecked")
            Map<String, String[]> parameters = req.getParameterMap();
            Map<String, String> paramsFirstValue = parameters.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> ArrayUtils.isNotEmpty(e.getValue()) ? e.getValue()[0] : ""));

            response = service.loginToVoiceRecognitionService(userWrapper.getId(), paramsFirstValue);

        } else {
            log.error("Unable to retrieve currently authenticated user");
            response = new SPVoiceLoginService.SimpleHttpResponse(SC_INTERNAL_SERVER_ERROR);
        }

        resp.setContentType(JSON_CONTENT_TYPE);
        resp.setCharacterEncoding(UTF_8_CHARACTER_ENCODING);
        resp.setStatus(response.getCode());
        if (response.getContent() != null) {
            resp.getWriter().write(response.getContent());
        }
    }
}
