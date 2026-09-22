package it.nexera.ris.web.services.servlets;

import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.entities.domain.FileEntity;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadPdf extends HttpServlet {

    private static final long serialVersionUID = 3477956457175097966L;

    public transient final Logger log = LogManager.getLogger(getClass());

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory(false).openSession();

            String id = request.getParameter("id");
            if (id == null || id.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }
            FileEntity file = (FileEntity) session.get(FileEntity.class,
                    Long.valueOf(id));

            if (file != null && file.getName() != null) {
                byte[] data = null;
                if (file.getContent() != null) {
                    data = file.getContent();
                } else if (file.getPath() != null) {
                    Path path = Paths.get(file.getPath());

                    if (Files.exists(path) && !Files.isDirectory(path)) {
                        data = Files.readAllBytes(path);
                    }
                }
                if (data != null) {
                    sendFile(file.getName(), data, response);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private void sendFile(String fileName, byte[] data,
                          HttpServletResponse response) {
        ServletOutputStream output = null;
        try {
            int length = data.length;

            response.reset();
            response.setContentType("application/pdf");
            response.setHeader("Content-Length", String.valueOf(length));
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");

            output = response.getOutputStream();
            output.write(data);
            output.flush();
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
        }
    }

}
