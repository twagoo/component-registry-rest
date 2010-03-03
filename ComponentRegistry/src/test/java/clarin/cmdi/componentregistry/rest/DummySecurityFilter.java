package clarin.cmdi.componentregistry.rest;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.ws.rs.core.SecurityContext;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Dummy security filter, very handy for unit testing.
 * 
 */
public class DummySecurityFilter implements ContainerRequestFilter {

    private final List<String> ALLOWED_USERS = Arrays.asList(DummyPrincipal.DUMMY_PRINCIPAL.getName());

    public ContainerRequest filter(ContainerRequest request) {
        Principal principalResult = null;
        String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
        if (authentication != null) { //if no authentication then do nothing
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
                throw new MappableContainerException(new AuthenticationException("Invalid user/password"));
            }
            principalResult = new DummyPrincipal(username);
        }
        
        final Principal principal = principalResult;
        request.setSecurityContext(new SecurityContext() {

            public boolean isUserInRole(String role) {
                return true;
            }

            public boolean isSecure() {
                return false;
            }

            public Principal getUserPrincipal() {
                return principal;
            }

            public String getAuthenticationScheme() {
                return SecurityContext.BASIC_AUTH;
            }
        });
        return request;
    }

    /**
     * Dummy validation for unit tests
     * @param username
     * @param password
     * @return
     */
    private boolean isValid(String username, String password) {
        return ALLOWED_USERS.contains(username);
    }

}