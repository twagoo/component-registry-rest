package clarin.cmdi.componentregistry.rest;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.HttpHeaders;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy security filter, very handy for unit testing.
 *
 */
public class DummySecurityFilter implements Filter {
    
    private final static Logger logger = LoggerFactory.getLogger(DummySecurityFilter.class);
    public static final String ALLOWED_USERS_PARAM = "allowedUsers";
    private final static List<String> DEFAULT_ALLOWED_USERS = Arrays.asList(DummyPrincipal.DUMMY_PRINCIPAL.getName());
    private List<String> allowedUsers;

    /**
     * Dummy validation for unit tests
     *
     * @param username
     * @param password
     * @return
     */
    private boolean isValid(String username, String password) {
        return allowedUsers.contains(username);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Principal principalResult = null;
        HttpServletRequest req = (HttpServletRequest) request;
        String authentication = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (authentication != null) { //if no authentication then do nothing
            logger.info("Check auth '{}'. Allowed users: {}", authentication, allowedUsers);
            if (!authentication.startsWith("Basic ")) {
                throw new MappableContainerException(new AuthenticationException("Only HTTP Basic authentication is supported"));
            }
            authentication = authentication.substring("Basic ".length());
            String base64Decode = new String(Base64.decode(authentication.getBytes()));
            String[] values = base64Decode.split(":");
            if (values.length < 2) {
                throw new MappableContainerException(new AuthenticationException("Invalid syntax for username and password"));
            }
            final String username = values[0];
            String password = values[1];
            if ((username == null) || (password == null)) {
                throw new MappableContainerException(new AuthenticationException("Missing username or password"));
            }
            if (!isValid(username, password)) {
                logger.info("Invalid user/password: '{}'/'{}'", username, password);
                throw new MappableContainerException(new AuthenticationException("Invalid user/password"));
            }
            principalResult = new DummyPrincipal(username);
        }

        final Principal principal = principalResult;
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(req) {

            public boolean isUserInRole(String role) {
                return true;
            }

            public boolean isSecure() {
                return false;
            }

            public Principal getUserPrincipal() {
                return principal;
            }

            @Override
            public String getAuthType() {
                return HttpServletRequest.BASIC_AUTH;
            }
        };
        chain.doFilter(wrapper, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Filter configuration", filterConfig.getInitParameterNames());
        final String allowedUsersParam = filterConfig.getInitParameter(ALLOWED_USERS_PARAM);
        if (allowedUsersParam == null) {
            allowedUsers = DEFAULT_ALLOWED_USERS;
        } else {
            logger.info("Custom allowed users parameter: {}", allowedUsersParam);
            allowedUsers = Arrays.asList(allowedUsersParam.split("\\s"));
        }
    }
}
