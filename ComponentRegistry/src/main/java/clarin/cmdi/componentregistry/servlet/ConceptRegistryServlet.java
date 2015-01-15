package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.net.URI;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Servlet responsible for providing a bridge to the CLARIN Concept Registry
 * (CCR) REST interface. Can be called by the front end to circumvent
 * cross-scripting and FLASH/Browser limitation in setting the headers of a
 * request.
 *
 */
public class ConceptRegistryServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(ConceptRegistryServlet.class);
    private static final long serialVersionUID = 1L;
    private transient WebResource service;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        URI uri = UriBuilder.fromUri(Configuration.getInstance().getCcrRestUrl()).build();
        Client client = Client.create();
        service = client.resource(uri);
        logger.info("Instantiated CCR servlet on URI {}", uri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

        // Set keywords
        String keywords = req.getParameter("keywords");
        logger.debug("ISOcat request: keywords = {}", keywords);
        queryParams.add("q", keywords);

        //TODO: Scope by concept scheme?
        //TODO: Set fields to be returned (fl=...)
        final WebResource requestResource = service.path("find-concepts").queryParams(queryParams);
        logger.info("Forwarding CCR request to {}", requestResource.getURI());

        // Send request to CCR
        final String result = requestResource.accept(MediaType.APPLICATION_XML).get(String.class);
        logger.debug("CCR result: {}", result);

        final String acceptHeader = req.getHeader("Accept");
        if (acceptHeader.contains(MediaType.APPLICATION_JSON)) {
            serveJSON(resp, result);
        } else {
            serveDCIF(resp, result);
        }
    }

    private void serveDCIF(HttpServletResponse resp, final String ccrResult) throws IOException {
        // Prepare response
        // TODO: transform to DCIF
        resp.setContentType(MediaType.APPLICATION_XML);
        resp.setCharacterEncoding("UTF-8");

        resp.getWriter().write(ccrResult);
        resp.flushBuffer();
    }

    private void serveJSON(HttpServletResponse resp, String result) throws IOException {
        //TODO: Implement JSON response
        resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "JSON not supported yet - to be implemented in backend");
    }
}
