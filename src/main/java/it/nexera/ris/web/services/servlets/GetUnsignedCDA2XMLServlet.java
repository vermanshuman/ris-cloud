package it.nexera.ris.web.services.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.XmlHelper;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.materialized.HistoricalReportMV;
import it.nexera.ris.web.beans.wrappers.TokenWrapper;
import it.nexera.ris.web.common.Constants;
import it.nexera.ris.web.dto.ResponseDto;
import it.nexera.ris.web.dto.SignFileDto;
import it.nexera.ris.web.handlers.APIHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GetUnsignedCDA2XMLServlet extends HttpServlet {

    private static final long serialVersionUID = 8592247720394503339L;

    protected static final Logger log = LogManager.getLogger(GetUnsignedCDA2XMLServlet.class);

    private static final Base64 CODER = new Base64();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        ResponseDto responseDTO = new ResponseDto();
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            String radiologyExamRequestValue = request.getParameter("radiologyExamRequestId");
            String token = request.getParameter("token");
            String userIdValue = request.getParameter("userId");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            if (token == null || token.trim().isEmpty() || radiologyExamRequestValue == null ||
                    radiologyExamRequestValue.trim().isEmpty() || userIdValue == null || userIdValue.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                responseDTO.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                responseDTO.setResultDescription(String.format(Constants.API_ID_TOKEN_NOT_VALID,
                        "userId e radiologyExamRequestId"));
                response.getWriter().write(gson.toJson(responseDTO));
            }else {
                session = HibernateUtil.getSessionFactory(false).openSession();
                TokenWrapper tokenWrapper = APIHelper.checkTokenValidity(token, responseDTO,
                        Long.parseLong(userIdValue.trim()), session);
                if (tokenWrapper.isValidToken()) {
                    RadiologyExamRequest radiologyExamRequest = ConnectionManager.get(RadiologyExamRequest.class,
                            Long.valueOf(radiologyExamRequestValue), session);
                    if(radiologyExamRequest != null){
                        List<HistoricalReportMV> historicalReports = ConnectionManager.load(HistoricalReportMV.class,
                                new Criterion[]{
                                Restrictions.eq("radiologyExamRequestId", radiologyExamRequest.getId())
                        }, session);
                        List<String> accessNumbers = new ArrayList<>();
                        if(!ValidationHelper.isNullOrEmpty(historicalReports)){
                            try {
                                SignFileDto signFileDto = new SignFileDto();
                                if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest.getRadiologyExamRequestItems())) {
                                    if (!ValidationHelper.isNullOrEmpty(radiologyExamRequest.getRadiologyExamRequestItems().get(0))
                                            && !ValidationHelper.isNullOrEmpty(
                                                    radiologyExamRequest.getRadiologyExamRequestItems().get(0).getFileEntity())) {
                                        signFileDto.setFileName(FilenameUtils.removeExtension(
                                                radiologyExamRequest.getRadiologyExamRequestItems().get(0).
                                                        getFileEntity().getName()).concat(".xml"));
                                    }


                                    for (RadiologyExamRequestItem item : radiologyExamRequest.getRadiologyExamRequestItems()) {
                                        if (!accessNumbers.contains(item.getAccessNumber())) {
                                            accessNumbers.add(item.getAccessNumber());
                                        }
                                    }
                                }
                                signFileDto.setMimeType("application/xml");
                                HistoricalReportMV historicalReportMV = historicalReports.get(0);
                                historicalReportMV.setRadiologyExamRequest(radiologyExamRequest);
                                historicalReportMV.setRadiologyExamRequestItems(radiologyExamRequest.getRadiologyExamRequestItems());
                                if (!ValidationHelper.isNullOrEmpty(historicalReportMV.getFileEntityId())) {
                                    try {
                                        historicalReportMV.setFileEntity(ConnectionManager.get(
                                                FileEntity.class, historicalReportMV.getFileEntityId(), session));
                                    } catch (Exception e) {
                                        LogHelper.log(log, e);
                                    }
                                }

                                if (StringUtils.isNotBlank(historicalReportMV.getPatientFiscalCode())) {
                                    try {
                                        historicalReportMV.setPatientId(ConnectionManager.getField(Patient.class, "id",
                                                new Criterion[]{
                                                        Restrictions.eq("fiscalCode",
                                                                historicalReportMV.getPatientFiscalCode())
                                        }, null, session));
                                    } catch (Exception e) {
                                        LogHelper.log(log, e);
                                    }

                                    try {
                                        String value = ConnectionManager.getField(Patient.class, "sexType",
                                                new Criterion[]{
                                                        Restrictions.eq("fiscalCode",
                                                                historicalReportMV.getPatientFiscalCode())
                                                }, null, session);

                                        if (StringUtils.isNotBlank(value)) {
                                            historicalReportMV.setSexType(value.substring(0, 1));
                                        } else {
                                            historicalReportMV.setSexType("");
                                        }
                                    } catch (Exception e) {
                                        LogHelper.log(log, e);
                                    }
                                }

                                if(!ValidationHelper.isNullOrEmpty(radiologyExamRequest.getPatient())){
                                    Patient patient = radiologyExamRequest.getPatient();
                                    if(!ValidationHelper.isNullOrEmpty(patient)){
                                        if(!ValidationHelper.isNullOrEmpty(patient.getProvince())){
                                            historicalReportMV.setPatientCounty(patient.getProvince().getCode());
                                        }else
                                            historicalReportMV.setPatientCounty("");
                                        historicalReportMV.setPatientCity( ConnectionManager.getField(
                                                City.class, "description", new Criterion[]{
                                                Restrictions.eq("id", patient.getCityId())
                                        }, null, session));

                                        historicalReportMV.setPatientCap(patient.getCap());
                                        historicalReportMV.setPatientAddress(patient.getAddress());
                                        historicalReportMV.setPatientBirthCity(ConnectionManager.getField(City.class, "description", new Criterion[]{
                                                Restrictions.eq("id", patient.getBirthCityId())
                                        }, null, session));
                                        if(!ValidationHelper.isNullOrEmpty(patient.getBirthCity())){
                                            historicalReportMV.setPatientBirthCityCode(patient.getBirthCity().getCode());
                                        }
                                    }
                                }

                                if(radiologyExamRequest.getUserClosingReportId() != null){
                                    historicalReportMV.setClosingUser(ConnectionManager.get(User.class,
                                            radiologyExamRequest.getUserClosingReportId(), session));
                                    historicalReportMV.setUserFirstName(historicalReportMV.getClosingUser().getFirstName());
                                    historicalReportMV.setUserLastName(historicalReportMV.getClosingUser().getLastName());
                                    historicalReportMV.setUserFiscalCode(historicalReportMV.getClosingUser().getFiscalCode());
                                }
                                historicalReportMV.setAccessNumberRequest(historicalReportMV.generateFormatString(accessNumbers));

                                if(radiologyExamRequest.getSector() != null)
                                    historicalReportMV.setSectorDescriptionRequest(radiologyExamRequest.getSector().getDescription());
                                else
                                    historicalReportMV.setSectorDescriptionRequest("");

                                if(historicalReportMV.getPatientCity() == null)
                                    historicalReportMV.setPatientCity("");

                                if(historicalReportMV.getPatientCap() == null)
                                    historicalReportMV.setPatientCap("");

                                if(historicalReportMV.getPatientAddress() == null)
                                    historicalReportMV.setPatientAddress("");

                                if(historicalReportMV.getPatientBirthCity() == null)
                                    historicalReportMV.setPatientBirthCity("");

                                if(historicalReportMV.getPatientBirthCityCode() == null)
                                    historicalReportMV.setPatientBirthCityCode("");

                                if(historicalReportMV.getUserFirstName() == null)
                                    historicalReportMV.setUserFirstName("");

                                if(historicalReportMV.getUserLastName() == null)
                                    historicalReportMV.setUserLastName("");

                                if(historicalReportMV.getUserFiscalCode() == null)
                                    historicalReportMV.setUserFiscalCode("");

                                if(historicalReportMV.getAccessNumberRequest() == null)
                                    historicalReportMV.setAccessNumberRequest("");

                                String xml = XmlHelper.createCdaXml(historicalReportMV, session);
                                signFileDto.setData(CODER.encodeAsString(xml.getBytes(StandardCharsets.ISO_8859_1)));
                                signFileDto.setRadiologyExamRequestId(radiologyExamRequest.getId());
                                if(!ValidationHelper.isNullOrEmpty(radiologyExamRequest.getUserClosingReportId()))
                                    signFileDto.setUserId(radiologyExamRequest.getUserClosingReportId());
                                response.getWriter().write(gson.toJson(signFileDto));
                            } catch (Exception e) {
                                LogHelper.log(log, e);
                            }
                        }
                    }else {
                        response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                        responseDTO.setResultCode(Constants.ENTITY_ID_NOT_FOUND);
                        responseDTO.setResultDescription(String.format(Constants.ENTITY_ID_NOT_FOUND_MESSAGE,
                                "radiologyExamRequestId"));
                        response.getWriter().write(gson.toJson(responseDTO));
                    }
                }else {
                    response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                    responseDTO.setResultCode(Constants.API_FAILURE_CODE);
                    responseDTO.setResultDescription(Constants.API_TOKEN_NOT_VALID);
                    response.getWriter().write(gson.toJson(responseDTO));
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            LogHelper.log(log, ex);
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

    }

    private SignFileDto createFseFile(FileEntity fileEntity) throws IOException {
        File file = new File(fileEntity.getPath());
        FileInputStream fis = null;
        SignFileDto signFileDto = new SignFileDto();
        ServletContext context = getServletConfig().getServletContext();
        if (ValidationHelper.isNullOrEmpty(fileEntity.getContent())) {
            byte[] bytes = new byte[(int) file.length()];
            try {
                fis = new FileInputStream(file);
                fis.read(bytes);
            } catch (IOException e) {
                LogHelper.log(log, e);
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
            signFileDto.setData(CODER.encodeAsString(bytes));
        } else {
            signFileDto.setData(CODER.encodeAsString(fileEntity.getContent()));
        }

        signFileDto.setMimeType(context.getMimeType(file.getAbsolutePath()));
        signFileDto.setFileName(file.getName());
        signFileDto.setId(fileEntity.getId());

        return signFileDto;
    }
}
