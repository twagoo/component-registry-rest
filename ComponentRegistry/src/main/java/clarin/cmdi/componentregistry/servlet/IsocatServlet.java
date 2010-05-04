package clarin.cmdi.componentregistry.servlet;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * 
 * Servlet responsible for providing a bridge to the isocat REST interface.
 * Can be called by the frontend to circumvent crosscripting and FLASH/Browser limitation in setting the headers of a request. 
 *
 */
public class IsocatServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private WebResource service;
    private final static String DCIF_XML = "application/dcif+xml";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        URI uri = UriBuilder.fromUri("http://www.isocat.org/rest/").build();
        Client client = Client.create();
        service = client.resource(uri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keywords = req.getParameter("keywords");
        resp.setContentType(MediaType.TEXT_XML);
        String result = service.path("/user/guest/search").queryParam("keywords", keywords).queryParam("dcif-mode", "list").accept(DCIF_XML).get(
                String.class);
        resp.getWriter().write(result);
        resp.flushBuffer();
    }

}