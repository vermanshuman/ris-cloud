package it.nexera.ris.web.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.util.IOUtils;

import java.io.IOException;

@AtmosphereHandlerService(path = "/notify")
public class NotifyAtmosphereHandler extends AbstractReflectorAtmosphereHandler {

    private static final Logger logger = LogManager.getLogger(NotifyAtmosphereHandler.class);

    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {

        AtmosphereRequest request = resource.getRequest();
        if (request.getMethod().equalsIgnoreCase("POST")) {
            Object o;
            try {
                o = IOUtils.readEntirely(resource);
            } catch (IOException e) {
                logger.warn(e);
                return;
            }

            if (!IOUtils.isBodyEmpty(o)) {
                logger.warn("{} received an empty body", request);
                resource.getBroadcaster().broadcast(o);
            }
        }
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        if (!(event.isClosedByClient() || event.isCancelled())) {
            super.onStateChange(event);
        }
    }

}
