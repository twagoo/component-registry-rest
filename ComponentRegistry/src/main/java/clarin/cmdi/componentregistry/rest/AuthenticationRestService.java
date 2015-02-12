package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import clarin.cmdi.componentregistry.model.AuthenticationInfo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.URI;
import java.security.Principal;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
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

    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Information on the current authentication state")
    public AuthenticationInfo getAuthenticationInformation() throws JSONException, AuthenticationRequiredException {
        final Principal userPrincipal = security.getUserPrincipal();

        if (userPrincipal == null || ComponentRegistryFactory.ANONYMOUS_USER.equals(userPrincipal.getName())) {
            return new AuthenticationInfo(false);
        } else {
            return new AuthenticationInfo(new UserCredentials(userPrincipal));
        }
    }

    @POST
    @ApiOperation(value = "Triggers the service to require the client to authenticate by means of the configured authentication mechanism. Notice that this might require user interaction!")
    @ApiResponses(value = {
        @ApiResponse(code = 302, message = "A redirect, either to a Shibboleth authentication page/discovery service or other identification mechanism, and ultimately once authenticated, to the application front end"),
        @ApiResponse(code = 401, message = "If unauthenticated, a request to authenticate may be returned (not in case of Shibboleth authentication)")
    })
    public Response triggerAuthenticationRequest() {
        logger.debug("Client has triggered authentication request");

        //done - redirect to front end
        final URI frontEndUri = uriInfo.getBaseUri().resolve("..");
        return Response.seeOther(frontEndUri).build();
    }
}
