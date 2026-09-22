package it.nexera.ris.web.services.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.SessionHolder;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.web.beans.wrappers.TokenWrapper;
import it.nexera.ris.web.common.Constants;
import it.nexera.ris.web.dto.ResponseDto;
import it.nexera.ris.web.dto.SignFileDto;
import it.nexera.ris.web.handlers.APIHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class GetUnsignedFileServlet extends HttpServlet {

    private static final long serialVersionUID = -626160817505748432L;

    protected static transient final Logger log = LogManager.getLogger(GetUnsignedFileServlet.class);

    public static final String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJOZXhlcmEiLCJVc2VybmFtZSI6IlN1cHBvcnQiLCJleHAiOjE5NTE2MzQwODIsImlhdCI6MTYzNjEwMTI4Mn0.TE3dejg9gS5C4aVLgsKy3-LaN0CuGE5zGfMqftxlmZyBcRejEqNwnTcGgEyzrk1yNiqez4FmH9_MTQTXpPSTIA";

    private static final Base64 CODER = new Base64();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String docId = req.getParameter("docId");
        String token = req.getParameter("token");
        String source = req.getParameter("source");

        SessionHolder.getInstance().openSession(SessionNames.GET_FILE_FOR_SIGN);
        Session session = null;

        SignFileDto signFileDto = null;

        if (!ValidationHelper.isNullOrEmpty(docId) && TOKEN.equals(token) && ValidationHelper.isNullOrEmpty(source)) {
            try {
                session = SessionHolder.getInstance().getSession(SessionNames.GET_FILE_FOR_SIGN);
                FileEntity fileEntity = ConnectionManager.get(FileEntity.class, Long.valueOf(docId), session);

                if (!ValidationHelper.isNullOrEmpty(fileEntity)) {
                    signFileDto = createFseFile(fileEntity);
                }
                if (!ValidationHelper.isNullOrEmpty(signFileDto)) {
                    Gson gson = new Gson();
                    String json = gson.toJson(signFileDto);
                    PrintWriter out = null;
                    try {
                        out = resp.getWriter();
                        resp.setContentType("application/json");
                        resp.setCharacterEncoding("UTF-8");
                        out.print(json);
                    } finally {
                        if (out != null) {
                            out.flush();
                            out.close();
                        }
                    }
                } else {
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    resp.getWriter().write("Operation failed, please check your request");
                   // throw new Exception("Operation failed, please check your request");
                }

            } catch (Exception e) {
                LogHelper.log(log, e);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(e.getMessage());
            } finally {
                if (session != null && session.isOpen()) {
                    SessionHolder.getInstance().closeSession(SessionNames.GET_FILE_FOR_SIGN);
                }
            }
        }else {
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json");
            ResponseDto response = new ResponseDto();
            Gson gson = new GsonBuilder().serializeNulls().create();
            Long idDoc = null;
            if (ValidationHelper.isNullOrEmpty(docId) || ValidationHelper.isNullOrEmpty(token)) {
                resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                response.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                response.setResultDescription(Constants.API_DOCID_TOKEN_NOT_VALID);
            }else {
                try {
                    idDoc = Long.parseLong(docId.trim());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                try {
                    session = SessionHolder.getInstance().getSession(SessionNames.GET_FILE_FOR_SIGN);
                    TokenWrapper tokenWrapper = APIHelper.checkTokenValidity(token, response, null, session);
                    if (tokenWrapper.isValidToken()) {
                        FileEntity fileEntity = ConnectionManager.get(FileEntity.class, idDoc, session);
                        if (!ValidationHelper.isNullOrEmpty(fileEntity)) {
                            signFileDto = createFseFile(fileEntity);
                        }
                        if (!ValidationHelper.isNullOrEmpty(signFileDto)) {

                            String json = gson.toJson(signFileDto);
                            PrintWriter out = null;
                            try {
                                out = resp.getWriter();
                                out.print(json);
                            } finally {
                                if (out != null) {
                                    out.flush();
                                    out.close();
                                }
                            }
                        } else {
                            throw new Exception("Operation failed, please check your request");
                        }
                    }

                } catch (Exception ex) {
                    LogHelper.log(log, ex);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setResultCode(Constants.API_FAILURE_CODE);
                    response.setResultDescription(ex.getMessage());
                } finally {
                    if (session != null && session.isOpen()) {
                        SessionHolder.getInstance().closeSession(SessionNames.GET_FILE_FOR_SIGN);
                    }
                }
            }
            resp.getWriter().write(gson.toJson(response));
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
