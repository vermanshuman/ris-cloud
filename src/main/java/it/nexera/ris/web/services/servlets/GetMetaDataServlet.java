package it.nexera.ris.web.services.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.WaitingListRegistrationStates;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequest;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.TokenWrapper;
import it.nexera.ris.web.common.Constants;
import it.nexera.ris.web.dto.MetaDataRequestDTO;
import it.nexera.ris.web.dto.MetaDataResponseDTO;
import it.nexera.ris.web.dto.Metadata;
import it.nexera.ris.web.handlers.APIHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.util.*;

public class GetMetaDataServlet extends HttpServlet {

    public transient final Logger log = LogManager.getLogger(getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            session = HibernateUtil.getSessionFactory(false).openSession();

            MetaDataResponseDTO responseDTO = new MetaDataResponseDTO();

            BufferedReader reader = request.getReader();
            Gson gson = new GsonBuilder().serializeNulls().create();

            MetaDataRequestDTO metaDataBean = gson.fromJson(reader, MetaDataRequestDTO.class);

            Long userId = null;

            try {
                userId = Long.parseLong(metaDataBean.getUserId().trim());
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            if (metaDataBean == null || userId == null || userId <= 0
                    || metaDataBean.getToken() == null || metaDataBean.getToken().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                responseDTO.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                responseDTO.setResultDescription(String.format(Constants.API_ID_TOKEN_NOT_VALID, "user_id"));
            } else {
                if (metaDataBean.getDateFrom() != null && !metaDataBean.getDateFrom().trim().isEmpty() && !DateTimeHelper.isValidFormat(DateTimeHelper.getDatePattern(),
                        metaDataBean.getDateFrom())) {
                    response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                    responseDTO.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                    responseDTO.setResultDescription(Constants.API_GET_METADATA_INVALID_DATE);
                } else {
                    TokenWrapper tokenWrapper = APIHelper.checkTokenValidity(metaDataBean.getToken(), responseDTO,
                            userId, session);
                    if (tokenWrapper.isValidToken()) {
                        Date dateFrom;
                        if (metaDataBean.getDateFrom() != null && !metaDataBean.getDateFrom().trim().isEmpty()) {
                            dateFrom = DateTimeHelper.fromString(metaDataBean.getDateFrom(), DateTimeHelper.getDatePattern());
                        } else {
                            int signatureDaysLimit = 0;
                            if (ApplicationSettingsHolder.getInstance()
                                    .getByKey(ApplicationSettingsKeys.SIGNATURE_DAYS_LIMIT)
                                    .getValue() != null) {
                                signatureDaysLimit = Integer
                                        .parseInt(ApplicationSettingsHolder
                                                .getInstance()
                                                .getByKey(
                                                        ApplicationSettingsKeys.SIGNATURE_DAYS_LIMIT)
                                                .getValue());
                            }
                            Calendar difference = Calendar.getInstance();
                            difference.set(Calendar.HOUR_OF_DAY, 0);
                            difference.set(Calendar.MINUTE, 0);
                            difference.set(Calendar.SECOND, 0);
                            difference.set(Calendar.MILLISECOND, 0);
                            difference.add(Calendar.DAY_OF_MONTH, -signatureDaysLimit);
                            dateFrom = difference.getTime();
                        }
                        List<Criterion> criterionList = new LinkedList<>();
                        criterionList.add( Restrictions.eq("userClosingReportId", userId));
                        criterionList.add( Restrictions.ge("radiologyExamRequestItems.reportDate", dateFrom));

                        String mimeType = "application/pdf";
                        if(metaDataBean.getCda2() == null || !metaDataBean.getCda2()){
                            criterionList.add(Restrictions.eq("waitingListRegistrationState", WaitingListRegistrationStates.REPORTED));
                        }else {
                            mimeType = "application/xml";
                            criterionList.add(Restrictions.eq("waitingListRegistrationState", WaitingListRegistrationStates.SIGNED));
                            criterionList.add(Restrictions.eq("cda2State", "NOT_SIGNED"));
                        }
                        List<RadiologyExamRequest> radiologyExamRequests = ConnectionManager.load(
                                RadiologyExamRequest.class,
                                new CriteriaAlias[]{new CriteriaAlias("radiologyExamRequestItems", "radiologyExamRequestItems", JoinType.INNER_JOIN)},
                                criterionList.toArray(new Criterion[0]), session);

                        List<Metadata> metadatas = new ArrayList<>();


                        for (RadiologyExamRequest radiologyExamRequest : radiologyExamRequests) {
                            Metadata metadata = new Metadata();
                            RadiologyExamRequestItem radiologyExamRequestItem = null;
                            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest.getRadiologyExamRequestItems())) {
                                radiologyExamRequestItem = radiologyExamRequest.getRadiologyExamRequestItems().get(0);
                            }
                            if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestItem) &&
                                    !ValidationHelper.isNullOrEmpty(radiologyExamRequestItem.getFileEntity())) {
                                metadata.setIdDoc(radiologyExamRequestItem.getFileEntity().getId());
                                if(metaDataBean.getCda2() != null && metaDataBean.getCda2()){
                                    metadata.setFileName(FilenameUtils.removeExtension(radiologyExamRequestItem.getFileEntity().getName()).concat(".xml"));
                                } else
                                    metadata.setFileName(radiologyExamRequestItem.getFileEntity().getName());
                                metadata.setMimeType(mimeType);
                                if (!ValidationHelper.isNullOrEmpty(radiologyExamRequestItem.getFileEntity().getPath())) {
                                    File file = new File(radiologyExamRequestItem.getFileEntity().getPath());
                                    if (file.exists()) {
                                        metadata.setFileSize(file.length() / 1024);
                                    }
                                }
                                if (!ValidationHelper.isNullOrEmpty(
                                        radiologyExamRequestItem.getFileEntity().getCreateDate())) {
                                    metadata.setCreateDate(DateTimeHelper.toFormatedString(radiologyExamRequestItem.getFileEntity().getCreateDate(),
                                            DateTimeHelper.getDatePatternWithSeconds()));
                                }
                                if(!ValidationHelper.isNullOrEmpty(radiologyExamRequest.getPatient())){
                                    metadata.setPatient(radiologyExamRequest.getPatient().getFullName());
                                }
                                metadata.setRadiologyExamId(radiologyExamRequest.getId());
                            }
                            metadatas.add(metadata);
                        }
                        response.setStatus(HttpServletResponse.SC_OK);
                        responseDTO.setResultCode(Constants.API_SUCCESS_CODE);
                        responseDTO.setResultDescription(Constants.API_GET_METADATA_SUCCESS);
                        responseDTO.setMetadata(metadatas);
                    }

                }

            }
            response.getWriter().write(gson.toJson(responseDTO));
        } catch (Exception ex) {
            LogHelper.log(log, ex);
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
