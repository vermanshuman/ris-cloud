package it.nexera.ris.web.services.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.history.HistoricalReport;
import it.nexera.ris.web.beans.wrappers.TokenWrapper;
import it.nexera.ris.web.common.Constants;
import it.nexera.ris.web.dto.ResponseDto;
import it.nexera.ris.web.dto.SignFileSecurityDto;
import it.nexera.ris.web.handlers.APIHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class UpdateSignedCDA2Servlet extends HttpServlet {

    private static final long serialVersionUID = 4299641008677301529L;

    private static final String OK = "OK";

    protected static final Logger log = LogManager.getLogger(UpdateSignedCDA2Servlet.class);

    private static final Base64 CODER = new Base64();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Session session = null;
        ResponseDto responseDto = new ResponseDto();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            session = HibernateUtil.getSessionFactory(false).openSession();
            SignFileSecurityDto signFileDto = APIHelper.readJsonFromRequest(req);

            LogHelper.debugInfo(log, "signFileDto " + signFileDto);

            if (signFileDto == null || signFileDto.getToken() == null
                    || signFileDto.getFileName() == null
                    || signFileDto.getUserId() == null
                    || signFileDto.getRadiologyExamRequestId() == null) {
                resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                responseDto.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                responseDto.setResultDescription(String.format(Constants.API_ID_TOKEN_NOT_VALID, "user_id"));
            } else if (StringUtils.isBlank(signFileDto.getMimeType())
                    || !signFileDto.getMimeType().equalsIgnoreCase("application/xml")) {
                resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                responseDto.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                responseDto.setResultDescription(String.format(Constants.MIME_TYPE_NOT_VALID, "user_id"));
            } else {
                LogHelper.debugInfo(log, "Validate token");
                TokenWrapper tokenWrapper = APIHelper.checkTokenValidity(signFileDto.getToken(), responseDto,
                        signFileDto.getUserId(), session);
                if (tokenWrapper.isValidToken()) {
                    LogHelper.debugInfo(log, "Token Validated");
                    if (OK.equals(signFileDto.getResult())) {
                        saveSignedXML(CODER.decode(signFileDto.getData()), signFileDto.getFileName(),
                                signFileDto.getUserId(), signFileDto.getRadiologyExamRequestId(), tokenWrapper.getUser(), session);
                        responseDto.setResultCode(Constants.API_SUCCESS_CODE);
                        responseDto.setResultDescription("Data saved");
                    } else {
                        responseDto.setResultCode(Constants.API_FAILURE_CODE);
                        responseDto.setResultDescription(Constants.API_MASSIVE_SIGNED_FILE_FAILURE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.debugInfo(log, "Exception Thrown " + ExceptionUtils.getStackTrace(e));
            LogHelper.debugInfo(log, "Exception Message " + e.getMessage());
            // LogHelper.log(log, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseDto.setResultCode(Constants.API_FAILURE_CODE);
            responseDto.setResultDescription(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        resp.getWriter().write(gson.toJson(responseDto));
    }

    private void saveSignedXML(byte[] data, String filename, Long userId, Long requestId, User user, final Session session) throws Exception {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            FileEntity file = new FileEntity();
            if (Boolean.TRUE.equals(
                    Boolean.valueOf(ResourcesHelper.getString("saveOnHard")))) {
                File path = FileEntityHelper.locateOrCreateSavingDir();
                file.setPath(
                        path.getAbsolutePath() + File.separator + filename);
                FileHelper.writeFileToFolder(filename, path, data);
                LogHelper.debugInfo(log, "Signed file save to this path  <" + file.getPath() + ">");
            } else {
                file.setContent(data);
                LogHelper.debugInfo(log, "Signed file save by content");

            }
            file.setName(filename);
            file.setCreateUserId(userId);
            file.setCreateDate(new Date());
            session.save(file);
            LogHelper.debugInfo(log, "Create file");
            FileHelper.writeFileToFolder(filename,
                    new File(FileHelper.getLocalFileDir()), data);
            LogHelper.debugInfo(log, "Created file");
            List<RadiologyExamRequestItem> items = ConnectionManager.load(RadiologyExamRequestItem.class,
                    new Criterion[]{Restrictions.eq("radiologyExamRequestId", requestId)}, session);
            if (!ValidationHelper.isNullOrEmpty(items)) {
                RadiologyExamRequest radiologyExamRequest = ConnectionManager.get(RadiologyExamRequest.class, requestId, session);
                radiologyExamRequest.setCda2State("SIGNED");
                radiologyExamRequest.setCda2FileEntity(file);
                radiologyExamRequest.setCda2SignDate(new Date());
                session.save(radiologyExamRequest);

                List<HistoricalReport> historicalReports = ConnectionManager.load(HistoricalReport.class,
                        new Criterion[]{
                                Restrictions.eq("radiologyExamRequest.id", radiologyExamRequest.getId())
                        }, session);
                if(!ValidationHelper.isNullOrEmpty(historicalReports)){
                    historicalReports.get(0).setCda2FileEntity(file);
                    session.save(historicalReports.get(0));
                }
            }
            LogHelper.debugInfo(log, "received signed CDA2 xml file <" + file.getName() + ">");
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in save signed xml ", e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ex) {
                    log.error("Error in rollback ", e);
                }
            }
            throw new Exception(e);
        }
    }
}
