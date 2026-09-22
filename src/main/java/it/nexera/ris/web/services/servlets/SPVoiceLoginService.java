package it.nexera.ris.web.services.servlets;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.IOException;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class SPVoiceLoginService {

    public static final Logger log = LogManager.getLogger(SPVoiceLoginService.class);

    public SimpleHttpResponse loginToVoiceRecognitionService(Long userId,
                                                             Map<String, String> requestParams) {
        SimpleHttpResponse response;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory(false).openSession();
            User user = ConnectionManager.get(User.class, userId, session);

            if (StringUtils.isEmpty(user.getSpvoiceUsername())
                    || StringUtils.isEmpty(user.getSpvoicePassword())) {

                response = SimpleHttpResponse.fromErrorMsg(SC_NOT_FOUND,
                        ResourcesHelper.getString("noSpvoiceCredentialsConfigured"));
            } else {

                requestParams.put("password", user.getSpvoicePassword());
                requestParams.put("user", user.getSpvoiceUsername());

                response = loginToVoiceRecognitionService(requestParams);
            }

        } catch (InstantiationException | IllegalAccessException | IOException e) {
            LogHelper.log(log, e);
            response = SimpleHttpResponse.fromErrorMsg(SC_INTERNAL_SERVER_ERROR, e.getMessage());

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return response;
    }

    private SimpleHttpResponse loginToVoiceRecognitionService(Map<String, String> params) throws IOException {
        HttpClient client = new HttpClient();

        PostMethod method = new PostMethod(ResourcesHelper.getProperty("spvoiceApiUrl").concat("/login.php"));

        NameValuePair[] body = params.entrySet().stream()
                .map(e -> new NameValuePair(e.getKey(), e.getValue()))
                .toArray(NameValuePair[]::new);
        method.setRequestBody(body);

        int code = client.executeMethod(method);
        return new SimpleHttpResponse(code, method.getResponseBodyAsString());
    }

    public static class SimpleHttpResponse {

        private int code;

        private String content;

        public SimpleHttpResponse(int code, String content) {
            this.code = code;
            this.content = content;
        }

        public SimpleHttpResponse(int code) {
            this.code = code;
        }

        public static SimpleHttpResponse fromErrorMsg(int code, String error) {
            return new SimpleHttpResponse(code,
                    String.format("{\"res\": \"error\", \"msg\": \"%s\"}", error));
        }

        public int getCode() {
            return code;
        }

        public String getContent() {
            return content;
        }
    }
}
