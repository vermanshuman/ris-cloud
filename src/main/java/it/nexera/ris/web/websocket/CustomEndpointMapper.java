package it.nexera.ris.web.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atmosphere.config.service.EndpointMapperService;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.util.EndpointMapper;
import org.atmosphere.util.uri.UriTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Based on {@link org.atmosphere.util.DefaultEndpointMapper}
 */
@EndpointMapperService
public class CustomEndpointMapper<U> implements EndpointMapper<U> {

    private static final Logger logger = LogManager.getLogger(CustomEndpointMapper.class);

    @Override
    public void configure(AtmosphereConfig config) {
    }

    protected U match(String path, Map<String, U> handlers) {
        U handler = handlers.get(path);

        if (handler == null) {
            final Map<String, String> m = new HashMap<>();
            for (Map.Entry<String, U> e : handlers.entrySet()) {
                UriTemplate t = null;
                try {
                    t = new UriTemplate(e.getKey());
                    logger.trace("Trying to map {} to {}", t, path);
                    if (t.match(path, m)) {
                        handler = e.getValue();
                        logger.trace("Mapped {} to {}", t, e.getValue());
                        break;
                    }
                } finally {
                    if (t != null) t.destroy();
                }
            }
        }
        return handler;
    }

    @Override
    public U map(AtmosphereRequest req, Map<String, U> handlers) {
        String path = pathInfo(req);

        U handler = map(path, handlers);

        // Glassfish 3.1.2 issue
        if (handler == null && req.getContextPath().length() < path.length()) {
            path = path.substring(req.getContextPath().length());
            handler = map(path, handlers);
        }
        req.setAttribute(FrameworkConfig.MAPPED_PATH, path);
        return handler;
    }

    @Override
    public U map(String path, Map<String, U> handlers) {

        if (path == null || path.isEmpty()) {
            path = "/";
        }

        U handler = match(path, handlers);
        if (handler == null) {
            handler = match(path + (path.endsWith("/") ? "all" : "/all"), handlers);

            if (handler == null) {
                handler = match(path + "*", handlers);

                if (handler == null) {
                    String p = path.lastIndexOf("/") <= 0 ? "/" : path.substring(0, path.lastIndexOf("/"));
                    while (p.contains("/")) {
                        handler = match(p, handlers);

                        if (handler != null) {
                            break;
                        }
                        p = p.substring(0, p.lastIndexOf("/"));
                    }
                }
            }
        }
        return handler;
    }

    private String pathInfo(AtmosphereRequest request) {
        String path = null;
        try {
            path = request.getPathInfo();
        } catch (IllegalStateException ex) {
            // ignore
        }

        if (path == null || path.isEmpty()) {
            path = "/";
        }
        return path;
    }
}
