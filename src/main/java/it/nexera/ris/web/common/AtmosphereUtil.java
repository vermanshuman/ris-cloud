package it.nexera.ris.web.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.MetaBroadcaster;
import org.atmosphere.util.ServletContextFactory;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.concurrent.Future;

public class AtmosphereUtil {

    private static final Logger logger = LogManager.getLogger(AtmosphereUtil.class);

    public static AtmosphereFramework getFramework() {
        ServletContext context = ServletContextFactory.getDefault().getServletContext();
        if (context != null) {
            return (AtmosphereFramework) context.getAttribute(
                    AtmosphereServlet.class.getSimpleName());
        }
        return null;
    }

    public static Future<List<Broadcaster>> broadcastTo(String broadcasterID, Object message) {
        AtmosphereFramework framework = getFramework();
        if (framework != null) {
            MetaBroadcaster broadcaster = framework.metaBroadcaster();
            return broadcaster.broadcastTo(broadcasterID, message);
        }
        logger.error("Failed broadcast. AtmosphereFramework is null");
        return null;
    }
}
