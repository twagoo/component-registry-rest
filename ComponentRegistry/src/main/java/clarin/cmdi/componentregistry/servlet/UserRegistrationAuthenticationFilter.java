package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.UserCredentials;
import java.io.IOException;
import java.security.Principal;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates the user in the database on authentication if not already registered.
 * Solution based on {@link http://stackoverflow.com/a/1724233}
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class UserRegistrationAuthenticationFilter implements Filter {

    private final static Logger logger = LoggerFactory.getLogger(UserRegistrationAuthenticationFilter.class);

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final Principal user = httpRequest.getUserPrincipal();
            final HttpSession session = httpRequest.getSession(false);

            if (user != null && (session == null || session.getAttribute("user") == null)) {
                httpRequest.getSession().setAttribute("user", user);
                logger.debug("Authenticated session started for " + user.getName());
                if (componentRegistryFactory.getOrCreateUser(new UserCredentials(user)) == null) {
                    logger.warn("User could not retrieved or registered: {}", user);
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
