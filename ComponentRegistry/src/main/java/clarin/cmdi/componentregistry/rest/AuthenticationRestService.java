package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import clarin.cmdi.componentregistry.model.AuthenticationInfo;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.security.Principal;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication resource to be used by the client to retrieve the current
 * authentication status and/or to force an authentication request if the user
 * is not authenticated.
 *
 * <p>
 * A 'GET' on this resource will return a JSON or XML structure with the
 * following information:</p>
 * <ul>
 * <li>authentication (true/false)</li>
 * <li>username (string)</li>
 * <li>displayName</li>
 * (string)
 * </ul>
 *
 * <p>
 * A 'POST' to this resource will trigger an authentication request (by means of
 * a 401) response code if the user is not yet authenticated. In case of a
 * successful authentication, it will respond with a redirect (303) to this same
 * resource.</p>
 *
 * <p>
 * A query parameter 'redirect' is accepted on the GET. If it is present, the
 * service will respond with a redirect to the provided URI. This way, the
 * client can make sure that the user is lead back to the front end in the
 * desired state. Passing the 'redirect' query parameter in the POST response
 * will cause it to be preserved in the redirect to the GET. To execute a
 * 'login' action, a front end application will therefore typically send a POST
 * to {@code <SERVICE_BASE_URI>/authentication?redirect=<FRONT_END_URI>}.</p>
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@Path("/authentication")
@Service
@Transactional(rollbackFor = {Exception.class, ValidationException.class})
@Api(value = "/authentication", description = "REST resource for handling the authentication status", produces = MediaType.APPLICATION_XML)
public class AuthenticationRestService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationRestService.class);

    @Context
    private SecurityContext security;
    @Context
    private UriInfo uriInfo;
    @Autowired
    private Configuration configuration;
    @Autowired
    private UserDao userDao;

    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Information on the current authentication state. Pass 'redirect' query parameter to make this method redirect to the URI specified as its value.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "If no query parameters are passed, with the authentications status in its body"),
        @ApiResponse(code = 303, message = "A redirect to the URI provided as the value of the 'redirect' parameter")
    })
    public Response getAuthenticationInformation(@QueryParam("redirect") @DefaultValue("") String redirectUri) throws JSONException, AuthenticationRequiredException {
        final Principal userPrincipal = security.getUserPrincipal();

        final AuthenticationInfo authInfo;
        if (userPrincipal == null) {
            logger.trace("Unauthenticated (userPrincipal == null)");
            authInfo = new AuthenticationInfo(false);
        } else if(userPrincipal.getName() == null || userPrincipal.getName().isEmpty() || ComponentRegistryFactory.ANONYMOUS_USER.equals(userPrincipal.getName())) {
            logger.debug("User principal set but no user name ({}): {}", userPrincipal.getName(), userPrincipal);
            authInfo = new AuthenticationInfo(false);
        } else {
            final UserCredentials credentials = new UserCredentials(userPrincipal);
            Long id = userDao.getByPrincipalName(userPrincipal.getName()).getId();
            authInfo = new AuthenticationInfo(credentials, id, configuration.isAdminUser(userPrincipal));
        }

        if (Strings.isNullOrEmpty(redirectUri)) {
            return Response.ok(authInfo).build();
        } else {
            return Response.seeOther(URI.create(redirectUri)).entity(authInfo).build();
        }
    }

    @POST
    @ApiOperation(value = "Triggers the service to require the client to authenticate by means of the configured authentication mechanism. Notice that this might require user interaction!")
    @ApiResponses(value = {
        @ApiResponse(code = 303, message = "A redirect, either to a Shibboleth authentication page/discovery service or other identification mechanism, and ultimately to the same URI as requested (which should be picked up as a GET)"),
        @ApiResponse(code = 401, message = "If unauthenticated, a request to authenticate may be returned (not in case of Shibboleth authentication)")
    })
    public Response triggerAuthenticationRequest() {
        logger.debug("Client has triggered authentication request");

        //done - redirect to GET
        return Response.seeOther(uriInfo.getRequestUri()).build();
    }
}
