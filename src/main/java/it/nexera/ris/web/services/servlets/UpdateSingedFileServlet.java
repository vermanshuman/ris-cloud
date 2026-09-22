package it.nexera.ris.web.services.servlets;

import com.google.gson.Gson;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import it.nexera.ris.persistence.beans.entities.domain.RadiologyExamRequestItem;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.dto.SignFileSecurityDto;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class UpdateSingedFileServlet extends HttpServlet {

    private static final long serialVersionUID = 1162153003188857706L;

    private static final String OK = "OK";

    protected static transient final Logger log = LogManager.getLogger(UpdateSingedFileServlet.class);

    private static final Base64 CODER = new Base64();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory(false).openSession();
            SignFileSecurityDto signFileDto = readJsonFromRequest(req);

            if (signFileDto != null && GetUnsignedFileServlet.TOKEN.equals(signFileDto.getToken())) {
                if (OK.equals(signFileDto.getResult())) {
                    saveSignedPdf(CODER.decode(signFileDto.getData()), signFileDto.getFileName(), signFileDto.getUserId(), signFileDto.getRadiologyExamRequestId(), signFileDto.getViewId(), session);
                    resp.setContentType("text/plain");
                    resp.setCharacterEncoding("UTF-8");
                    resp.getWriter().write("Data saved");
                } else {
                    ApplicationSettingsHolder.getInstance().createNewLocalValue(signFileDto.getViewId(), signFileDto.getDescription());
                }
            } else {
                throw new Exception("Data not saved");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private SignFileSecurityDto readJsonFromRequest(HttpServletRequest req) {
        StringBuilder jb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        Gson gson = new Gson();
        return gson.fromJson(jb.toString(), SignFileSecurityDto.class);
    }

    private void saveSignedPdf(byte[] data, String filename, Long userId, Long requestId, String viewId, Session session) {
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
            FileHelper.writeFileToFolder(filename,
                    new File(FileHelper.getLocalFileDir()), data);

            List<RadiologyExamRequestItem> items = ConnectionManager.load(RadiologyExamRequestItem.class,
                    new Criterion[]{Restrictions.eq("radiologyExamRequestId", requestId)}, session);

            if (!ValidationHelper.isNullOrEmpty(items)) {
                for (RadiologyExamRequestItem item : items) {
                    item.setSignFileEntity(file);
                    session.save(item);
                }
            }
            LogHelper.debugInfo(log, "Put in ApplicationSettingsHolder received signed file <" + file.getName() + ">");
            ApplicationSettingsHolder.getInstance().createNewLocalValue(viewId, file);

            tx.commit();
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ex) {
                    LogHelper.log(log, ex);
                }
            }
        }
    }
}
