package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
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

    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(ConceptRegistryServlet.class);
    private static final String CCR2DCIF_XSL_RESOURCE = "/ccr2dcif.xsl";

    private transient WebResource service;
    private Templates ccr2dcifTemplates;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final URI uri = UriBuilder.fromUri(Configuration.getInstance().getCcrRestUrl()).build();
        final Client client = Client.create();
        service = client.resource(uri);

        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Source ccr2dcifSource = new StreamSource(getClass().getResourceAsStream(CCR2DCIF_XSL_RESOURCE));
            ccr2dcifTemplates = transformerFactory.newTemplates(ccr2dcifSource);
        } catch (TransformerConfigurationException ex) {
            throw new ServletException("Could not initialise CCR to DCIF transformation templates", ex);
        }

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
        final InputStream result = requestResource.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        logger.debug("CCR result: {}", result);

        final String acceptHeader = req.getHeader("Accept");
        if (acceptHeader.contains(MediaType.APPLICATION_JSON)) {
            serveJSON(resp, result);
        } else {
            serveDCIF(resp, result);
        }
    }

    private void serveDCIF(HttpServletResponse resp, final InputStream ccrResponse) throws IOException, ServletException {
        // Prepare response
        resp.setContentType(MediaType.APPLICATION_XML);
        resp.setCharacterEncoding("UTF-8");

        try {
            logger.debug("Transforming CCR response to response writer");
            final Transformer transformer = ccr2dcifTemplates.newTransformer();
            final Source source = new StreamSource(ccrResponse);
            final Result result = new StreamResult(resp.getWriter());
            transformer.transform(source, result);
        } catch (TransformerConfigurationException ex) {
            throw new ServletException("Could not configure transformer for converting CCR output to DCIF", ex);
        } catch (TransformerException ex) {
            throw new ServletException("Could not transform CCR output to DCIF", ex);
        } finally {
            ccrResponse.close();
            resp.flushBuffer();
        }
    }

    private void serveJSON(HttpServletResponse resp, final InputStream ccrResponse) throws IOException {
        //TODO: Implement JSON response
        resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "JSON not supported yet - to be implemented in backend");
    }
}
