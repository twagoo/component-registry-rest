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
 * Servlet responsible for providing a bridge to the isocat REST interface. Can be called by the frontend to circumvent crosscripting and
 * FLASH/Browser limitation in setting the headers of a request.
 *
 */
public class IsocatServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(IsocatServlet.class);
    private static final long serialVersionUID = 1L;
    private transient WebResource service;
    private final static String DCIF_XML = "application/dcif+xml";

    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	URI uri = UriBuilder.fromUri(Configuration.getInstance().getIsocatRestUrl()).build();
	Client client = Client.create();
	service = client.resource(uri);
	logger.info("Instantiated ISOcat servlet on URI {}", uri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	queryParams.add("dcif-mode", "list");
	String keywords = req.getParameter("keywords");
	logger.debug("ISOcat request: keywords = {}", keywords);
	queryParams.add("keywords", keywords);
	String type = req.getParameter("type"); //simple, complex or no type param to search in everything
	logger.debug("ISOcat request: type = {}", type);
	if (type != null) {
	    queryParams.add("type", type);
	}
	queryParams.add("profile", "Metadata"); //always search in metadata profile
	resp.setContentType(MediaType.TEXT_XML);

	final WebResource requestResource = service.path("/user/guest/search").queryParams(queryParams);
	logger.info("Forwarding ISOcat request to {}", requestResource.getURI());
	
	final String result = requestResource.accept(DCIF_XML).get(String.class);
	logger.debug("ISOcat result: {}", result);
	resp.getWriter().write(result);
	resp.flushBuffer();
    }
}
