package clarin.cmdi.componentregistry.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class UnavailableServlet extends HttpServlet {
    
    Logger logger = LoggerFactory.getLogger(UnavailableServlet.class);
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Request to {} from {} while front end unavailable", req.getRequestURI(), req.getRemoteAddr());
        resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        //resp.setContentType("text/html");
        resp.getWriter().write("<html>"
                + "<head><title>Component Registry</title></head>"
                + "<body>"
                + " <h1>Unavailable</h1>"
                + " <p>The Component Regsistry front end is currently unavailable.</p>"
                + " <p>Please check the <a href=\"https://www.clarin.eu/status\">CLARIN infrastructure status page</a> for up-to-date information."
                + "</body>"
                + "</html>");
    }
    
}
