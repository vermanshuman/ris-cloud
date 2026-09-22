package it.nexera.ris.web.services.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.enums.ConservationStates;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.executors.ThreadExecutor;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.SinglePersistenceSessionAction;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.TokenWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestItemWrapper;
import it.nexera.ris.web.beans.wrappers.logic.RadExamRequestWrapper;
import it.nexera.ris.web.common.Constants;
import it.nexera.ris.web.dto.ResponseDto;
import it.nexera.ris.web.dto.SignFileSecurityDto;
import it.nexera.ris.web.handlers.APIHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpdateSingedFileMassiveServlet extends HttpServlet {
    private static final long serialVersionUID = 3492620738755803544L;

    private static final String OK = "OK";

    protected static transient final Logger log = LogManager.getLogger(UpdateSingedFileMassiveServlet.class);

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
            } else {
                LogHelper.debugInfo(log, "Validate token");
                TokenWrapper tokenWrapper = APIHelper.checkTokenValidity(signFileDto.getToken(), responseDto,
                        signFileDto.getUserId(), session);
                if (tokenWrapper.isValidToken()) {
                    LogHelper.debugInfo(log, "Token Validated");
                    if (OK.equals(signFileDto.getResult())) {
                        saveSignedPdf(CODER.decode(signFileDto.getData()), signFileDto.getFileName(),
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

    private void saveSignedPdf(byte[] data, String filename, Long userId, Long requestId, User user, final Session session) throws Exception {
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
                if(radiologyExamRequest.getConservationState() != null
                        && radiologyExamRequest.getConservationState().equals(ConservationStates.SENT)){
                    radiologyExamRequest.setConservationState(ConservationStates.RESEND);
                    ConnectionManager.save(radiologyExamRequest, session);
                }
                radiologyExamRequest.setCda2State("NOT_SIGNED");
                APIHelper apiHelper = new APIHelper();
                List<Long> radExItemIds = new ArrayList<>();
                RadiologyExamRequestItem radiologyExamRequestItem = items.get(0);
                for (RadiologyExamRequestItem item : items) {
                    item.setNotSignFileEntity(item.getFileEntity());
                    ConnectionManager.save(item, session);
                    radExItemIds.add(item.getId());
                }
                apiHelper.toSignState(radiologyExamRequest,
                        radExItemIds, radiologyExamRequestItem.getReportResult(),
                        file, radExItemIds.size() > 1, new Date(), user, session);

                if (!ValidationHelper.isNullOrEmpty(radExItemIds)) {
                    final HL7RepoHelper hl7Helper = new HL7RepoHelper();
                    final FileEntity fileEntity = items.get(0).getFileEntity();
                    Pair<List<RadExamRequestWrapper>, List<RadExamRequestItemWrapper>> pair =
                            hl7Helper.getPairRequestsItems(radExItemIds, session);
                    if (!ValidationHelper.isNullOrEmpty(pair.getFirst())
                            && !ValidationHelper.isNullOrEmpty(pair.getSecond())) {
                        List<Long> itemIds = new ArrayList<>();
                        for (RadExamRequestItemWrapper radExamRequestItemWrapper : pair.getSecond()) {
                            itemIds.add(radExamRequestItemWrapper.getId());
                        }
                        List<RadExamRequestWrapper> allRequestsWrappers = pair.getFirst();
                        if (!ValidationHelper.isNullOrEmpty(allRequestsWrappers)) {
                            for (final RadExamRequestWrapper requestWrapper : allRequestsWrappers) {
                                if (!ValidationHelper.isNullOrEmpty(requestWrapper.getAsapPlaceOrderNumber())) {

                                    ThreadExecutor.execute(new Action() {
                                        @Override
                                        public void execute() throws Exception {
                                            TransactionExecuter.execute(
                                                    new SinglePersistenceSessionAction() {
                                                        @Override
                                                        public void execute() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
                                                            Session session = null;
                                                            try {
                                                                session = HibernateUtil.getSessionFactory(false).openSession();
                                                                hl7Helper.sendMsgForDocument(requestWrapper, fileEntity, session);
                                                            }catch(Exception e){
                                                                e.printStackTrace();
                                                                log.error("Error in sendMsgForDocument ", e);
                                                            }finally {
                                                                if (session != null && session.isOpen()) {
                                                                    session.close();
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    });

                                }
                                if (!ValidationHelper.isNullOrEmpty(requestWrapper.getForwarded())
                                        && requestWrapper.getForwarded()) {
                                    ThreadExecutor.execute(new Action() {
                                        @Override
                                        public void execute() throws Exception {
                                            TransactionExecuter.execute(
                                                    new SinglePersistenceSessionAction() {
                                                        @Override
                                                        public void execute() throws HibernateException {
                                                            Session session = null;
                                                            try {
                                                                session = HibernateUtil.getSessionFactory(false).openSession();
                                                                hl7Helper.sendRepoMsgsForDocument(requestWrapper, fileEntity, true, session);
                                                            }catch(Exception e){
                                                                e.printStackTrace();
                                                                log.error("Error in sendRepoMsgsForDocument ", e);
                                                            }finally {
                                                                if (session != null && session.isOpen()) {
                                                                    session.close();
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
            LogHelper.debugInfo(log, "received signed file <" + file.getName() + ">");
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in save signed pdf ", e);
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
