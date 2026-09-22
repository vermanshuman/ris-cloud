package it.nexera.ris.web.services.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.AppToken;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.common.Constants;
import it.nexera.ris.web.dto.LoginResponseDto;
import it.nexera.ris.web.handlers.TokenManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 8018068810155594446L;

    public transient final Logger log = LogManager.getLogger(getClass());

    private static final Base64 CODER = new Base64();

    private PasswordEncoder passwordEncoder;

    @Override
    public void init() throws ServletException {
        ApplicationContext context = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        passwordEncoder = context.getBean("passwordEncoder", PasswordEncoder.class);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        try {
            response.setCharacterEncoding("UTF-8");
            session = HibernateUtil.getSessionFactory(false).openSession();
            String userFiscalCode = request.getParameter("userFiscalCode");

            Gson gson = new GsonBuilder().serializeNulls().create();
            LoginResponseDto loginResponseDto = new LoginResponseDto();
            response.setContentType("application/json");
            boolean isAuthFailed = false;
            User loggedInuser = null;
            if (userFiscalCode == null || userFiscalCode.isEmpty()) {
                String authorization = request.getHeader(Constants.AUTHORIZATION_HEADER_NAME);
                LogHelper.debugInfo(log, "Request received for login api " + authorization);
                if(authorization == null || authorization.isEmpty())
                    isAuthFailed = true;
                else {
                    String base64Credentials = authorization.substring("Basic".length()).trim();
                    byte[] credDecoded = CODER.decode(base64Credentials);
                    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                    final String[] values = credentials.split(":", 2);
                    String userName = values[0];
                    String password = values[1];

                    Optional<User> loggedInUserOpt = findUserByLoginAndPassword(userName, password, session);
                    if (loggedInUserOpt.isPresent()) {
                        loggedInuser = loggedInUserOpt.get();
                    } else {
                        isAuthFailed = true;
                    }
                }
            }else {
                LogHelper.debugInfo(log, "Request received for login api " + userFiscalCode);
                List<User> usersByCF = ConnectionManager.load(User.class,
                        new Criterion[]{
                                Restrictions.eq("fiscalCode", userFiscalCode.trim()).ignoreCase()
                        }, session);

                if(usersByCF == null || usersByCF.isEmpty()){
                    isAuthFailed = true;
                }else {
                    loggedInuser = usersByCF.get(0);
                }
            }
            if(isAuthFailed){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                loginResponseDto.setResultCode(Constants.API_FAILURE_CODE);
                loginResponseDto.setResultDescription(Constants.API_CF_NOT_VALID);
            } else {
                loginResponseDto.setResultCode(Constants.API_SUCCESS_CODE);
                loginResponseDto.setResultDescription(Constants.API_LOGIN_SUCCESS);
                loginResponseDto.setUser_id(loggedInuser.getId());
                loginResponseDto.setToken(TokenManager.getInstance().generateToken(loggedInuser));
                AppToken appToken = ConnectionManager.get(AppToken.class,
                        new Criterion[]{
                                Restrictions.eq("userId", loggedInuser.getId())
                        }, session);

                if(appToken == null){
                    appToken = new AppToken();
                    appToken.setUserId(loggedInuser.getId());
                }
                appToken.setToken(loginResponseDto.getToken());
                int expirationDuration = 30;
                if (!ValidationHelper
                        .isNullOrEmpty(ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.TOKEN_EXPIRATION))){
                    expirationDuration = Integer.parseInt(ApplicationSettingsHolder.getInstance()
                            .getByKey(ApplicationSettingsKeys.TOKEN_EXPIRATION).getValue());
                }
                appToken.setCreateDate(DateTimeHelper.getNow());
                Date expirationDate = DateTimeHelper.addMinutes(appToken.getCreateDate(),expirationDuration );
                appToken.setExpirationDate(expirationDate);
                ConnectionManager.save(appToken, true, session);
            }
            response.getWriter().write(gson.toJson(loginResponseDto));
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.log(log, e);
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private Optional<User> findUserByLoginAndPassword(String login, String password, Session session) {
        List<User> users = ConnectionManager.load(User.class,
                new Criterion[] {Restrictions.eq("login", login)}, session);

        return users.stream().findFirst()
                .filter(u -> passwordEncoder.matches(password, u.getPassword()));
    }
}
