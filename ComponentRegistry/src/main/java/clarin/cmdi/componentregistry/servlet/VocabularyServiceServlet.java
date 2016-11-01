package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
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

    /**
     * Vocabulary service path on external service
     */
    private static final String VOCABULARY_SERVICE_PATH = "conceptscheme";
    private static final String VOCABULARY_PAGE_SERVICE_PATH_FORMAT = VOCABULARY_SERVICE_PATH + "/%s"; //placeholder for id

    /**
     * Vocabulary service path on this service
     */
    private final static String CONCEPT_SCHEMES_PATH = "/conceptscheme";
    /**
     * Vocabulary page redirect path on this service
     */
    private static final String VOCAB_PAGE_PATH = "/conceptscheme/vocabpage";

    private transient WebResource service;
    private URI serviceUri;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.serviceUri = UriBuilder.fromUri(Configuration.getInstance().getClavasRestUrl()).build();
        this.service = Client.create().resource(serviceUri);

        logger.info("Instantiated vocabulary servlet on URI {}", serviceUri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getPathInfo();
        if (path != null) {
            if (path.equals(CONCEPT_SCHEMES_PATH)) {
                //proxy the response (list of concept schemes) from the original service
                serveConceptSchemes(req, resp);
                return;
            } else if (path.equals(VOCAB_PAGE_PATH)) {
                //redirect the client to the vocabulary page on the original service
                serveVocabularyPage(req, resp);
                return;
            }
            //TODO: else if path is '/items'...
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
    }

    private void serveConceptSchemes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        WebResource serviceReq = service.path(VOCABULARY_SERVICE_PATH);

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
            serviceReq = setFormatFromAcceptHeader(req, serviceReq);
        }

        logger.debug("Forwarding vocabulary service request to {}", serviceReq.toString());
        forwardResponse(serviceReq, resp);
    }

    private WebResource setFormatFromAcceptHeader(HttpServletRequest req, WebResource serviceReq) {
        final String acceptHeader = req.getHeader("Accept");
        if (acceptHeader != null) {
            if (acceptHeader.contains(MediaType.APPLICATION_JSON)) {
                serviceReq = serviceReq.queryParam("format", "json");
                //TODO: forward
            } else if (acceptHeader.contains(MediaType.TEXT_HTML)) {
                serviceReq = serviceReq.queryParam("format", "html");
                //TODO: redirect
            }
        }
        return serviceReq;
    }

    private void serveVocabularyPage(HttpServletRequest req, HttpServletResponse resp) throws IllegalArgumentException, UriBuilderException, IOException {
        // get id from query parameter
        final String id;
        {
            final String[] idParam = req.getParameterValues("id");
            if (idParam == null || idParam.length == 0) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "vocabulary id must be provided via 'id' query parameter");
                return;
            } else {
                id = idParam[0];
            }
        }
        // construct redirect URI to send client to the right page at the service
        final StringBuilder targetUriBuilder = new StringBuilder(
                UriBuilder.fromUri(serviceUri)
                        .path(String.format(VOCABULARY_PAGE_SERVICE_PATH_FORMAT, id))
                        .build().toString());
        // append request format specifier depending on accept header
        final String acceptHeader = req.getHeader("Accept");
        if (acceptHeader != null) {
            if (acceptHeader.contains(MediaType.APPLICATION_JSON)) {
                targetUriBuilder.append(".json");
                //forward JSON content
                final WebResource serviceReq = Client.create().resource(targetUriBuilder.toString());
                forwardResponse(serviceReq, resp);
                return;
            } else if (acceptHeader.contains(MediaType.TEXT_HTML)) {
                targetUriBuilder.append(".html");
            }
        }
        // redirect to page at service (all cases except for JSON)
        resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
        resp.sendRedirect(resp.encodeRedirectURL(targetUriBuilder.toString()));
    }

    private void forwardResponse(WebResource request, HttpServletResponse resp) throws IOException {
        //make GET request and copy directly to response stream
        try (final InputStream serviceResultStream = request.get(InputStream.class)) {
            try (final ServletOutputStream responseOutStream = resp.getOutputStream()) {
                ByteStreams.copy(serviceResultStream, responseOutStream);
                responseOutStream.close();
            }
        }
    }
}
