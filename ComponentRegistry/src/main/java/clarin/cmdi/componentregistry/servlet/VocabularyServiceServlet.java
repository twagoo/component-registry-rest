package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Servlet responsible for providing a bridge to the CLAVAS REST interface. Can
 * be called by the front end to circumvent cross-scripting and browser
 * limitation in setting the headers of a request.
 *
 */
public class VocabularyServiceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(VocabularyServiceServlet.class);
    private final static String CONCEPT_SCHEMES_PATH = "/conceptscheme";

    private transient WebResource service;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final URI uri = UriBuilder.fromUri(Configuration.getInstance().getClavasRestUrl()).build();
        final Client client = Client.create();
        service = client.resource(uri);

        logger.info("Instantiated vocabulary servlet on URI {}", uri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getPathInfo();
        if (path != null) {
            if (path.equals(CONCEPT_SCHEMES_PATH)) {
                serveConceptSchemes(req, resp);
                return;
            }
        }
        //TODO: else if path is '/items'...
        resp.setStatus(404);
        resp.getWriter().write("Not found");
    }

    private void serveConceptSchemes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        WebResource serviceReq = service.path("conceptscheme");

        //forward query params to service request
        final Map<String, String[]> params = req.getParameterMap();
        if (params != null) {
            for (Map.Entry<String, String[]> param : params.entrySet()) {
                for (String value : param.getValue()) {
                    serviceReq = serviceReq.queryParam(param.getKey(), value);
                }
            }
        }

        //set request format according to Accept header (if no explicit 'format' in request)
        if (params == null || !params.containsKey("format")) {
            final String acceptHeader = req.getHeader("Accept");
            if (acceptHeader != null) {
                if (acceptHeader.contains(MediaType.APPLICATION_JSON)) {
                    serviceReq = serviceReq.queryParam("format", "json");
                } else if (acceptHeader.contains(MediaType.TEXT_HTML)) {
                    serviceReq = serviceReq.queryParam("format", "html");
                }
            }
        }

        logger.debug("Forwarding vocabulary service request to {}", serviceReq.toString());

        //make GET request and copy directly to response stream
        try (final InputStream serviceResultStream = serviceReq.get(InputStream.class)) {
            try (final ServletOutputStream responseOutStream = resp.getOutputStream()) {
                ByteStreams.copy(serviceResultStream, responseOutStream);
                responseOutStream.close();
            }
        }
    }
}
