package it.nexera.ris.web.services.servlets;

import it.nexera.ris.common.helpers.logic.UrgencyHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ShowUrgentRequestMsg extends HttpServlet {

    private static final long serialVersionUID = 2050858710429302506L;

    public transient final Logger log = LogManager.getLogger(getClass());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String[] idsArr = req.getParameter("ids").split("_");
        UrgencyHelper.processIds(idsArr);
    }

}
