package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.GroupService;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.GROUPID_PARAM;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@Path("/items")
@Service
@Singleton
@Api(value = "/items", produces = MediaType.APPLICATION_XML)
public class ItemsService extends AbstractComponentRegistryRestService {

    private final static Logger logger = LoggerFactory.getLogger(ItemsService.class);

    @Context
    private HttpServletResponse servletResponse;
    
    @InjectParam(value = "GroupService")
    private GroupService groupService;

    @GET
    @Path("/{itemId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @ApiOperation(value = "The description (metadata) of a single component or profile item")
    @ApiResponses(value = {
        @ApiResponse(code = 401, message = "Item requires authorisation and user is not authenticated"),
        @ApiResponse(code = 403, message = "Non-public item is not owned by current user and user is no administrator"),
        @ApiResponse(code = 404, message = "Item does not exist")
    })
    public BaseDescription getBaseDescription(@PathParam("itemId") String itemId) throws ComponentRegistryException, IOException {
        logger.debug("Item with id: {} is requested.", itemId);
        try {
            ComponentRegistry cr = this.getBaseRegistry();
            BaseDescription description;
            if (itemId.startsWith(ComponentDescription.COMPONENT_PREFIX)) {
                description = cr.getComponentDescriptionAccessControlled(itemId);
                return description;
            }
            if (itemId.startsWith(ProfileDescription.PROFILE_PREFIX)) {
                description = cr.getProfileDescriptionAccessControlled(itemId);
                return description;
            }
            servletResponse.sendError(Response.Status.BAD_REQUEST.getStatusCode());
            return new BaseDescription();

        } catch (UserUnauthorizedException ex2) {
            servletResponse.sendError(Response.Status.FORBIDDEN.getStatusCode(), ex2.getMessage());
            return new BaseDescription();
        } catch (ItemNotFoundException e) {
            servletResponse.sendError(Response.Status.NOT_FOUND.getStatusCode(), e.getMessage());
            return new BaseDescription();
        } catch (AuthenticationRequiredException e) {
            servletResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode(), e.toString());
            return new BaseDescription();
        }
    }

    @GET
    @Path("/{itemId}/groups")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns a listing of groups to which an item belongs")
    public List<Group> getGroupsTheItemIsAMemberOf(@PathParam("itemId") String itemId) {
        return groupService.getGroupsTheItemIsAMemberOf(itemId);
    }

    @POST
    @Path("/{itemId}/transferownership")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Transfers an item to the specified group (either from the private space or another group)")
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "Current user has no access")
    })
    public Response transferItemOwnershipToGroup(@PathParam("itemId") String itemId,
            @QueryParam(GROUPID_PARAM) long groupId) throws IOException {
        Principal principal = security.getUserPrincipal();
        try {
            groupService.transferItemOwnershipFromUserToGroupId(principal.getName(), groupId, itemId);
            return Response.ok("Ownership transferred").build();
        } catch (UserUnauthorizedException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
}
