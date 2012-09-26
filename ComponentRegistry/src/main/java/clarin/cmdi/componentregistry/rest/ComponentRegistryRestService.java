package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ComponentStatus;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.Owner;
import clarin.cmdi.componentregistry.RssCreatorComments;
import clarin.cmdi.componentregistry.RssCreatorDescriptions;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.CommentResponse;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import clarin.cmdi.componentregistry.rss.Rss;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/registry")
public class ComponentRegistryRestService {

    public static final String APPLICATION_BASE_URL_PARAM = "componentRegistryServiceRootUrl";
    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext security;
    @Context
    private HttpServletRequest request;
    @Context
    private ServletContext servletContext;
    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryRestService.class);
    public static final String DATA_FORM_FIELD = "data";
    public static final String NAME_FORM_FIELD = "name";
    public static final String DESCRIPTION_FORM_FIELD = "description";
    public static final String GROUP_FORM_FIELD = "group";
    public static final String DOMAIN_FORM_FIELD = "domainName";
    public static final String USERSPACE_PARAM = "userspace";
    public static final String METADATA_EDITOR_PARAM = "mdEditor";
    public static final String NUMBER_OF_RSSITEMS = "limit";
    @Inject(value = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;

    /**
     * Converts userspace boolean to component status. Temporary solution!!!
     *
     * TODO: Replace all calls to getRegistry that use this by calls using ComponentStatus
     *
     * @param userSpace
     * @return
     * @deprecated All calls should go directly to {@link #getRegistry(clarin.cmdi.componentregistry.ComponentStatus)}
     */
    @Deprecated
    private static ComponentStatus getStatus(boolean userSpace) {
	if (userSpace) {
	    return ComponentStatus.DEVELOPMENT;
	} else {
	    return ComponentStatus.PUBLIC;
	}
    }

    private ComponentRegistry getRegistry(ComponentStatus status) {
	Principal userPrincipal = security.getUserPrincipal();
	UserCredentials userCredentials = getUserCredentials(userPrincipal);
	return getRegistry(status, null, userCredentials);
    }

    private ComponentRegistry getRegistry(ComponentStatus status, Owner owner, UserCredentials userCredentials) {
	try {
	    return componentRegistryFactory.getComponentRegistry(status, owner, userCredentials);
	} catch (UserUnauthorizedException uuEx) {
	    //TODO: Throw actual exception and catch nicely
	    throw new RuntimeException("Cannot access requested registry", uuEx);
	}
    }

    /**
     *
     * @return Principal of current request
     * @throws IllegalArgumentException If no user principal found
     */
    private Principal checkAndGetUserPrincipal() throws UserUnauthorizedException {
	Principal principal = security.getUserPrincipal();
	if (principal == null) {
	    throw new UserUnauthorizedException("no user principal found.");
	}
	return principal;
    }

    private UserCredentials getUserCredentials(Principal userPrincipal) {
	UserCredentials userCredentials = null;
	if (userPrincipal != null) {
	    userCredentials = new UserCredentials(userPrincipal);
	}
	return userCredentials;
    }

    @GET
    @Path("/components")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ComponentDescription> getRegisteredComponents(@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	long start = System.currentTimeMillis();
	List<ComponentDescription> components = getRegistry(getStatus(userspace)).getComponentDescriptions();
	LOG.info("Releasing " + components.size() + " registered components into the world (" + (System.currentTimeMillis() - start)
		+ " millisecs)");
	return components;
    }

    @GET
    @Path("/profiles")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ProfileDescription> getRegisteredProfiles(@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace,
	    @QueryParam(METADATA_EDITOR_PARAM) @DefaultValue("false") boolean metadataEditor) throws ComponentRegistryException {
	long start = System.currentTimeMillis();
	List<ProfileDescription> profiles;
	if (metadataEditor) {
	    profiles = getRegistry(getStatus(userspace)).getProfileDescriptionsForMetadaEditor();
	} else {
	    profiles = getRegistry(getStatus(userspace)).getProfileDescriptions();
	}

	LOG.info("Releasing " + profiles.size() + " registered profiles into the world (" + (System.currentTimeMillis() - start)
		+ " millisecs)");
	return profiles;
    }

    @GET
    @Path("/components/{componentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRegisteredComponent(@PathParam("componentId") String componentId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	LOG.info("Component with id: " + componentId + " is requested.");
	CMDComponentSpec mdComponent = getRegistry(getStatus(userspace)).getMDComponent(componentId);
	if (mdComponent == null) {
	    return Response.status(Status.NOT_FOUND).build();
	} else {
	    return Response.ok(mdComponent).build();
	}
    }

    @GET
    @Path("/components/{componentId}/{rawType}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public Response getRegisteredComponentRawType(@PathParam("componentId") final String componentId, @PathParam("rawType") String rawType) {
	LOG.info("Component with id: " + componentId + " and rawType:" + rawType + " is requested.");
	StreamingOutput result = null;
	try {
	    final ComponentRegistry registry = findRegistry(componentId, new ComponentClosure());
	    if (registry == null) {
		return Response.status(Status.NOT_FOUND).entity("Id: " + componentId + " is not registered, cannot create data.").build();
	    }
	    ComponentDescription desc = registry.getComponentDescription(componentId);
	    checkAndThrowDescription(desc, componentId);
	    String fileName = desc.getName() + "." + rawType;
	    if ("xml".equalsIgnoreCase(rawType)) {
		result = new StreamingOutput() {
		    @Override
		    public void write(OutputStream output) throws IOException, WebApplicationException {
			try {
			    registry.getMDComponentAsXml(componentId, output);
			} catch (ComponentRegistryException e) {
			    LOG.info("Could not retrieve component", e);
			    throw new WebApplicationException(e, Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build());
			}
		    }
		};
	    } else if ("xsd".equalsIgnoreCase(rawType)) {
		result = new StreamingOutput() {
		    @Override
		    public void write(OutputStream output) throws IOException, WebApplicationException {
			try {
			    registry.getMDComponentAsXsd(componentId, output);
			} catch (ComponentRegistryException e) {
			    LOG.info("Could not retrieve component", e);
			    throw new WebApplicationException(e, Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build());
			}

		    }
		};
	    } else {
		throw new WebApplicationException(Response.serverError().entity(
			"unsupported rawType: " + rawType + " (only xml or xsd are supported)").build());
	    }
	    return createDownloadResponse(result, fileName);
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve component", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	}
    }

    public ComponentRegistry findRegistry(String id, RegistryClosure<? extends AbstractDescription> clos) throws ComponentRegistryException {
	AbstractDescription desc = null;
	ComponentRegistry result = getRegistry(getStatus(false));
	desc = clos.getDescription(result, id);
	if (desc == null) {
	    List<ComponentRegistry> userRegs = componentRegistryFactory.getAllUserRegistries();
	    for (ComponentRegistry reg : userRegs) {
		desc = clos.getDescription(reg, id);
		if (desc != null) {
		    result = reg;
		    break;
		}
	    }
	}
	return result;
    }

    @GET
    @Path("/profiles/{profileId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRegisteredProfile(@PathParam("profileId") String profileId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	LOG.info("Profile with id: " + profileId + " is requested.");
	CMDComponentSpec mdProfile = getRegistry(getStatus(userspace)).getMDProfile(profileId);
	if (mdProfile == null) {
	    return Response.status(Status.NOT_FOUND).build();
	} else {
	    return Response.ok(mdProfile).build();
	}
    }

    @GET
    @Path("/components/usage/{componentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<AbstractDescription> getComponentUsage(@PathParam("componentId") String componentId, @QueryParam(USERSPACE_PARAM) @DefaultValue("true") boolean userspace) throws ComponentRegistryException {
	try {
	    final long start = System.currentTimeMillis();
	    ComponentRegistry registry = getRegistry(getStatus(userspace));
	    List<ComponentDescription> components = registry.getUsageInComponents(componentId);
	    List<ProfileDescription> profiles = registry.getUsageInProfiles(componentId);

	    LOG.info("Found " + components.size() + " components and " + profiles.size() + " profiles that use component " + componentId
		    + " (" + (System.currentTimeMillis() - start) + " millisecs)");

	    List<AbstractDescription> usages = new ArrayList<AbstractDescription>(components.size() + profiles.size());
	    usages.addAll(components);
	    usages.addAll(profiles);

	    return usages;
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve profile usage", e);
	    throw e;
	}
    }

    @GET
    @Path("/profiles/{profileId}/comments")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Comment> getCommentsFromProfile(@PathParam("profileId") String profileId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	long start = System.currentTimeMillis();
	final Principal principal = security.getUserPrincipal();
	List<Comment> comments = getRegistry(getStatus(userspace)).getCommentsInProfile(profileId, principal);
	LOG.info("Releasing " + comments.size() + " registered comments in Profile into the world (" + (System.currentTimeMillis() - start)
		+ " millisecs)");
	return comments;
    }

    @GET
    @Path("/components/{componentId}/comments")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Comment> getCommentsFromComponent(@PathParam("componentId") String componentId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	long start = System.currentTimeMillis();
	final Principal principal = security.getUserPrincipal();
	List<Comment> comments = getRegistry(getStatus(userspace)).getCommentsInComponent(componentId, principal);
	LOG.info("Releasing " + comments.size() + " registered comments in Component into the world (" + (System.currentTimeMillis() - start)
		+ " millisecs)");
	return comments;
    }

    @GET
    @Path("/profiles/{profileId}/comments/{commentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Comment getSpecifiedCommentFromProfile(@PathParam("profileId") String profileId, @PathParam("commentId") String commentId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	LOG.info(" Comments of component with id" + commentId + " are requested.");
	final Principal principal = security.getUserPrincipal();
	return getRegistry(getStatus(userspace)).getSpecifiedCommentInProfile(profileId, commentId, principal);
    }

    @GET
    @Path("/components/{componentId}/comments/{commentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Comment getSpecifiedCommentFromComponent(@PathParam("componentId") String componentId, @PathParam("commentId") String commentId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	LOG.info(" Comments of component with id" + commentId + " are requested.");
	final Principal principal = security.getUserPrincipal();
	return getRegistry(getStatus(userspace)).getSpecifiedCommentInComponent(componentId, commentId, principal);
    }

    /**
     *
     * Purely helper method for my front-end (FLEX) which only does post/get requests. The query param is checked and the "proper" method is
     * called.
     *
     * @param profileId
     * @param method
     * @return
     */
    @POST
    @Path("/profiles/{profileId}")
    public Response manipulateRegisteredProfile(@PathParam("profileId") String profileId, @FormParam("method") String method,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	if ("delete".equalsIgnoreCase(method)) {
	    return deleteRegisteredProfile(profileId, userspace);
	} else {
	    return Response.ok().build();
	}
    }

    @POST
    @Path("/profiles/{profileId}/comments/{commentId}")
    public Response manipulateCommentFromProfile(@PathParam("profileId") String profileId, @PathParam("commentId") String commentId, @FormParam("method") String method,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	if ("delete".equalsIgnoreCase(method)) {
	    return deleteCommentFromProfile(profileId, commentId, userspace);
	} else {
	    return Response.ok().build();
	}
    }

    @POST
    @Path("/components/{componentId}/comments/{commentId}")
    public Response manipulateCommentFromComponent(@PathParam("componentId") String componentId, @PathParam("commentId") String commentId, @FormParam("method") String method,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	if ("delete".equalsIgnoreCase(method)) {
	    return deleteCommentFromComponent(componentId, commentId, userspace);
	} else {
	    return Response.ok().build();
	}
    }

    @POST
    @Path("/profiles/{profileId}/publish")
    @Consumes("multipart/form-data")
    public Response publishRegisteredProfile(@PathParam("profileId") String profileId, @FormDataParam(DATA_FORM_FIELD) InputStream input,
	    @FormDataParam(NAME_FORM_FIELD) String name, @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
	    @FormDataParam(GROUP_FORM_FIELD) String group, @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    ProfileDescription desc = getRegistry(getStatus(true)).getProfileDescription(profileId);
	    if (desc != null) {
		updateDescription(desc, name, description, domainName, group);
		return register(input, desc, getUserCredentials(principal), true, new PublishAction(principal));
	    } else {
		LOG.error("Update of nonexistent id (" + profileId + ") failed.");
		return Response.serverError().entity("Invalid id, cannot update nonexistent profile").build();
	    }
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve component", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (UserUnauthorizedException ex) {
	    return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
	}
    }

    @POST
    @Path("/profiles/{profileId}/update")
    @Consumes("multipart/form-data")
    public Response updateRegisteredProfile(@PathParam("profileId") String profileId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace, @FormDataParam(DATA_FORM_FIELD) InputStream input,
	    @FormDataParam(NAME_FORM_FIELD) String name, @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
	    @FormDataParam(GROUP_FORM_FIELD) String group, @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    UserCredentials userCredentials = getUserCredentials(principal);
	    ProfileDescription desc = getRegistry(getStatus(userspace)).getProfileDescription(profileId);
	    if (desc != null) {
		updateDescription(desc, name, description, domainName, group);
		return register(input, desc, userCredentials, userspace, new UpdateAction(principal));
	    } else {
		LOG.error("Update of nonexistent id (" + profileId + ") failed.");
		return Response.serverError().entity("Invalid id, cannot update nonexistent profile").build();
	    }
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve component", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (UserUnauthorizedException ex) {
	    return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
	}

    }

    /**
     *
     * Purely helper method for my front-end (FLEX) which van only do post/get requests. The query param is checked and the "proper" method
     * is called.
     *
     * @param componentId
     * @param method
     * @return
     */
    @POST
    @Path("/components/{componentId}")
    public Response manipulateRegisteredComponent(@PathParam("componentId") String componentId, @FormParam("method") String method,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	if ("delete".equalsIgnoreCase(method)) {
	    return deleteRegisteredComponent(componentId, userspace);
	} else {
	    return Response.ok().build();
	}
    }

    @POST
    @Path("/components/{componentId}/publish")
    @Consumes("multipart/form-data")
    public Response publishRegisteredComponent(@PathParam("componentId") String componentId,
	    @FormDataParam(DATA_FORM_FIELD) InputStream input, @FormDataParam(NAME_FORM_FIELD) String name,
	    @FormDataParam(DESCRIPTION_FORM_FIELD) String description, @FormDataParam(GROUP_FORM_FIELD) String group,
	    @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    // TODO: Get status from parameter
	    ComponentDescription desc = getRegistry(getStatus(true)).getComponentDescription(componentId);
	    if (desc != null) {
		updateDescription(desc, name, description, domainName, group);
		return register(input, desc, getUserCredentials(principal), true, new PublishAction(principal));
	    } else {
		LOG.error("Update of nonexistent id (" + componentId + ") failed.");
		return Response.serverError().entity("Invalid id, cannot update nonexistent profile").build();
	    }
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve component", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (UserUnauthorizedException ex) {
	    return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
	}
    }

    @POST
    @Path("/components/{componentId}/update")
    @Consumes("multipart/form-data")
    public Response updateRegisteredComponent(@PathParam("componentId") String componentId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace, @FormDataParam(DATA_FORM_FIELD) InputStream input,
	    @FormDataParam(NAME_FORM_FIELD) String name, @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
	    @FormDataParam(GROUP_FORM_FIELD) String group, @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    ComponentDescription desc = getRegistry(getStatus(userspace)).getComponentDescription(componentId);
	    if (desc != null) {
		updateDescription(desc, name, description, domainName, group);
		return register(input, desc, getUserCredentials(principal), userspace, new UpdateAction(principal));
	    } else {
		LOG.error("Update of nonexistent id (" + componentId + ") failed.");
		return Response.serverError().entity("Invalid id, cannot update nonexistent component").build();
	    }
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve component", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (UserUnauthorizedException ex) {
	    return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
	}
    }

    private void updateDescription(AbstractDescription desc, String name, String description, String domainName, String group) {
	desc.setName(name);
	desc.setDescription(description);
	desc.setDomainName(domainName);
	desc.setGroupName(group);
	desc.setRegistrationDate(AbstractDescription.createNewDate());
    }

    @DELETE
    @Path("/components/{componentId}")
    public Response deleteRegisteredComponent(@PathParam("componentId") String componentId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    ComponentRegistry registry = getRegistry(getStatus(userspace));
	    LOG.info("Component with id: " + componentId + " set for deletion.");
	    registry.deleteMDComponent(componentId, principal, false);
	} catch (DeleteFailedException e) {
	    LOG.info("Component with id: " + componentId + " deletion failed.", e);
	    return Response.status(Status.FORBIDDEN).entity("" + e.getMessage()).build();
	} catch (ComponentRegistryException e) {
	    LOG.info("Component with id: " + componentId + " deletion failed.", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (IOException e) {
	    LOG.info("Component with id: " + componentId + " deletion failed.", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (UserUnauthorizedException e) {
	    LOG.info("Component with id: " + componentId + " deletion failed: " + e.getMessage());
	    return Response.serverError().status(Status.UNAUTHORIZED).entity("" + e.getMessage()).build();
	}
	LOG.info("Component with id: " + componentId + " deleted.");
	return Response.ok().build();
    }

    @DELETE
    @Path("/profiles/{profileId}")
    public Response deleteRegisteredProfile(@PathParam("profileId") String profileId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    LOG.info("Profile with id: " + profileId + " set for deletion.");
	    getRegistry(getStatus(userspace)).deleteMDProfile(profileId, principal);
	} catch (DeleteFailedException e) {
	    LOG.info("Profile with id: " + profileId + " deletion failed: " + e.getMessage());
	    return Response.serverError().status(Status.FORBIDDEN).entity("" + e.getMessage()).build();
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve component", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (IOException e) {
	    LOG.info("Profile with id: " + profileId + " deletion failed.", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (UserUnauthorizedException e) {
	    LOG.info("Profile with id: " + profileId + " deletion failed: " + e.getMessage());
	    return Response.serverError().status(Status.UNAUTHORIZED).entity("" + e.getMessage()).build();
	}
	LOG.info("Profile with id: " + profileId + " deleted.");
	return Response.ok().build();
    }

    @DELETE
    @Path("/profiles/{profileId}/comments/{commentId}")
    public Response deleteCommentFromProfile(@PathParam("profileId") String profileId, @PathParam("commentId") String commentId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	try {
	    final Principal principal = checkAndGetUserPrincipal();
	    final ComponentRegistry registry = getRegistry(getStatus(userspace));
	    final Comment comment = registry.getSpecifiedCommentInProfile(profileId, commentId, principal);
	    if (comment != null && profileId.equals(comment.getProfileDescriptionId())) {
		LOG.info("Comment with id: " + commentId + " set for deletion.");
		registry.deleteComment(commentId, principal);
	    } else {
		throw new ComponentRegistryException("Comment not found for specified profile");
	    }
	} catch (DeleteFailedException e) {
	    LOG.info("Comment with id: " + commentId + " deletion failed: " + e.getMessage());
	    return Response.serverError().status(Status.FORBIDDEN).entity("" + e.getMessage()).build();
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve component", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (IOException e) {
	    LOG.info("Comment with id: " + commentId + " deletion failed.", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (UserUnauthorizedException e) {
	    LOG.info("Comment with id: " + commentId + " deletion failed: " + e.getMessage());
	    return Response.serverError().status(Status.UNAUTHORIZED).entity("" + e.getMessage()).build();
	}
	LOG.info("Comment with id: " + commentId + " deleted.");
	return Response.ok().build();
    }

    @DELETE
    @Path("/components/{componentId}/comments/{commentId}")
    public Response deleteCommentFromComponent(@PathParam("componentId") String componentId, @PathParam("commentId") String commentId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	try {
	    final Principal principal = checkAndGetUserPrincipal();
	    final ComponentRegistry registry = getRegistry(getStatus(userspace));
	    final Comment comment = registry.getSpecifiedCommentInComponent(componentId, commentId, principal);
	    if (comment != null && componentId.equals(comment.getComponentDescriptionId())) {
		LOG.info("Comment with id: " + commentId + " set for deletion.");
		registry.deleteComment(commentId, principal);
	    } else {
		throw new ComponentRegistryException("Comment not found for specified component");
	    }
	} catch (DeleteFailedException e) {
	    LOG.info("Comment with id: " + commentId + " deletion failed: " + e.getMessage());
	    return Response.serverError().status(Status.FORBIDDEN).entity("" + e.getMessage()).build();
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve component", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (IOException e) {
	    LOG.info("Comment with id: " + commentId + " deletion failed.", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	} catch (UserUnauthorizedException e) {
	    LOG.info("Comment with id: " + commentId + " deletion failed: " + e.getMessage());
	    return Response.serverError().status(Status.UNAUTHORIZED).entity("" + e.getMessage()).build();
	}
	LOG.info("Comment with id: " + commentId + " deleted.");
	return Response.ok().build();
    }

    @GET
    @Path("/profiles/{profileId}/{rawType}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public Response getRegisteredProfileRawType(@PathParam("profileId") final String profileId, @PathParam("rawType") String rawType) {
	LOG.info("Profile with id: " + profileId + " and rawType:" + rawType + " is requested.");
	StreamingOutput result = null;
	try {
	    final ComponentRegistry registry = findRegistry(profileId, new ProfileClosure());
	    if (registry == null) {
		return Response.status(Status.NOT_FOUND).entity("Id: " + profileId + " is not registered, cannot create data.").build();
	    }
	    ProfileDescription desc = registry.getProfileDescription(profileId);
	    checkAndThrowDescription(desc, profileId);
	    String fileName = desc.getName() + "." + rawType;

	    if ("xml".equalsIgnoreCase(rawType)) {
		result = new StreamingOutput() {
		    @Override
		    public void write(OutputStream output) throws IOException, WebApplicationException {
			try {
			    registry.getMDProfileAsXml(profileId, output);
			} catch (ComponentRegistryException e) {
			    LOG.warn("Could not retrieve component", e);
			    throw new WebApplicationException(e, Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build());
			}
		    }
		};
	    } else if ("xsd".equalsIgnoreCase(rawType)) {
		result = new StreamingOutput() {
		    @Override
		    public void write(OutputStream output) throws IOException, WebApplicationException {
			try {
			    registry.getMDProfileAsXsd(profileId, output);
			} catch (ComponentRegistryException e) {
			    LOG.warn("Could not retrieve component", e);
			    throw new WebApplicationException(e, Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build());
			}
		    }
		};
	    } else {
		throw new WebApplicationException(Response.serverError().entity(
			"unsupported rawType: " + rawType + " (only xml or xsd are supported)").build());
	    }
	    return createDownloadResponse(result, fileName);
	} catch (ComponentRegistryException e) {
	    LOG.info("Could not retrieve component", e);
	    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	}
    }

    private void checkAndThrowDescription(AbstractDescription desc, String id) {
	if (desc == null) {
	    throw new WebApplicationException(Response.serverError().entity("Incorrect id:" + id + "cannot handle request").build());
	}
    }

    private Response createDownloadResponse(StreamingOutput result, String fileName) {
	//Making response so it triggers browsers native save as dialog.
	Response response = Response.ok().type("application/x-download").header("Content-Disposition",
		"attachment; filename=\"" + fileName + "\"").entity(result).build();
	return response;

    }

    @POST
    @Path("/profiles")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes("multipart/form-data")
    public Response registerProfile(@FormDataParam(DATA_FORM_FIELD) InputStream input, @FormDataParam(NAME_FORM_FIELD) String name,
	    @FormDataParam(DESCRIPTION_FORM_FIELD) String description, @FormDataParam(GROUP_FORM_FIELD) String group, @FormDataParam(DOMAIN_FORM_FIELD) String domainName,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    UserCredentials userCredentials = getUserCredentials(principal);
	    ProfileDescription desc = createNewProfileDescription();
	    desc.setCreatorName(userCredentials.getDisplayName());
	    desc.setUserId(userCredentials.getPrincipalName()); // Hash used to be created here, now Id is constructed by impl
	    desc.setName(name);
	    desc.setDescription(description);
	    desc.setGroupName(group);
	    desc.setDomainName(domainName);
	    LOG.info("Trying to register Profile: " + desc);
	    return register(input, desc, userCredentials, userspace, new NewAction());
	} catch (UserUnauthorizedException ex) {
	    return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
	}
    }

    @POST
    @Path("/components")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes("multipart/form-data")
    public Response registerComponent(@FormDataParam(DATA_FORM_FIELD) InputStream input, @FormDataParam(NAME_FORM_FIELD) String name,
	    @FormDataParam(DESCRIPTION_FORM_FIELD) String description, @FormDataParam(GROUP_FORM_FIELD) String group,
	    @FormDataParam(DOMAIN_FORM_FIELD) String domainName, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    UserCredentials userCredentials = getUserCredentials(principal);
	    ComponentDescription desc = createNewComponentDescription();
	    desc.setCreatorName(userCredentials.getDisplayName());
	    desc.setUserId(userCredentials.getPrincipalName()); // Hash used to be created here, now Id is constructed by impl
	    desc.setName(name);
	    desc.setDescription(description);
	    desc.setGroupName(group);
	    desc.setDomainName(domainName);
	    LOG.info("Trying to register Component: " + desc);
	    return register(input, desc, userCredentials, userspace, new NewAction());
	} catch (UserUnauthorizedException ex) {
	    return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
	}
    }

    @POST
    @Path("/components/{componentId}/comments")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes("multipart/form-data")
    public Response registerCommentInComponent(@FormDataParam(DATA_FORM_FIELD) InputStream input,
	    @PathParam("componentId") String componentId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    UserCredentials userCredentials = getUserCredentials(principal);
	    if (null == componentRegistryFactory.getOrCreateUser(userCredentials)) {
		throw new UserUnauthorizedException("Cannot materialize authenticated user");
	    }
	    // TODO: Add user/group param
	    ComponentRegistry registry = getRegistry(getStatus(userspace), null, userCredentials);
	    ComponentDescription description = registry.getComponentDescription(componentId);
	    if (description != null) {
		LOG.info("Trying to register comment to " + componentId);
		return registerComment(input, registry, userspace, description, principal, userCredentials);
	    } else {
		LOG.error("Attempt to post comment on nonexistent component id (" + componentId + ") failed.");
		return Response.serverError().entity("Invalid id, cannot comment on nonexistent component").build();
	    }
	} catch (UserUnauthorizedException ex) {
	    return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
	}
    }

    @POST
    @Path("/profiles/{profileId}/comments")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes("multipart/form-data")
    public Response registerCommentInProfile(@FormDataParam(DATA_FORM_FIELD) InputStream input,
	    @PathParam("profileId") String profileId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	try {
	    Principal principal = checkAndGetUserPrincipal();
	    UserCredentials userCredentials = getUserCredentials(principal);
	    if (null == componentRegistryFactory.getOrCreateUser(userCredentials)) {
		throw new UserUnauthorizedException("Cannot materialize authenticated user");
	    }
	    // TODO: Add user/group param
	    ComponentRegistry registry = getRegistry(getStatus(userspace), null, userCredentials);
	    ProfileDescription description = registry.getProfileDescription(profileId);
	    if (description != null) {
		LOG.info("Trying to register comment to " + profileId);
		return registerComment(input, registry, userspace, description, principal, userCredentials);
	    } else {
		LOG.error("Attempt to post comment on nonexistent profile id (" + profileId + ") failed.");
		return Response.serverError().entity("Invalid id, cannot comment on nonexistent profile").build();
	    }
	} catch (UserUnauthorizedException ex) {
	    return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
	}
    }

    @GET
    @Path("/pingSession")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response pingSession() {
	boolean stillActive = false;
	Principal userPrincipal = security.getUserPrincipal();
	LOG.info("ping by user: " + (userPrincipal == null ? "null" : userPrincipal.getName()));
	if (request != null) {
	    if (userPrincipal != null && !ComponentRegistryFactory.ANONYMOUS_USER.equals(userPrincipal.getName())) {
		stillActive = !((HttpServletRequest) request).getSession().isNew();
	    }
	}
	return Response.ok().entity("<session stillActive=\"" + stillActive + "\"/>").build();
    }

    private Response register(InputStream input, AbstractDescription desc, UserCredentials userCredentials, boolean userspace,
	    RegisterAction action) {
	try {
	    // TODO: Add user/group param
	    ComponentRegistry registry = getRegistry(getStatus(userspace), null, userCredentials);
	    DescriptionValidator descriptionValidator = new DescriptionValidator(desc);
	    MDValidator validator = new MDValidator(input, desc, registry, getRegistry(getStatus(true)), componentRegistryFactory.getPublicRegistry());
	    RegisterResponse response = new RegisterResponse();
	    response.setIsInUserSpace(userspace);
	    validate(response, descriptionValidator, validator);
	    if (response.getErrors().isEmpty()) {

		CMDComponentSpec spec = validator.getCMDComponentSpec();

		// Olha: removing filename from spec before it gets extended!!! recursion over all the components
		setFileNamesFromListToNull(spec.getCMDComponent());



		try {


		    // Expand to check for recursion
		    registry.getExpander().expandNestedComponent(spec.getCMDComponent(), desc.getId());



		    // Add profile


		    int returnCode = action.execute(desc, spec, response, registry);
		    if (returnCode == 0) {
			response.setRegistered(true);
			response.setDescription(desc);
		    } else {
			response.setRegistered(false);
			response.addError("Unable to register at this moment. Internal server error.");
		    }
		} catch (ComponentRegistryException ex) {
		    // Recursion detected
		    response.setRegistered(false);
		    response.addError("Error while expanding specification. " + ex.getMessage());
		}
	    } else {
		LOG.info("Registration failed with validation errors:" + Arrays.toString(response.getErrors().toArray()));
		response.setRegistered(false);
	    }
	    response.setIsProfile(desc.isProfile());
	    return Response.ok(response).build();
	} finally {
	    try {
		input.close();//either we read the input or there was an exception, we need to close it.
	    } catch (IOException e) {
		LOG.error("Error when closing inputstream: ", e);
	    }
	}
    }

    private Response registerComment(InputStream input, ComponentRegistry registry, boolean userspace,
	    AbstractDescription description, Principal principal, UserCredentials userCredentials) {
	try {
	    CommentValidator validator = new CommentValidator(input, description);
	    CommentResponse response = new CommentResponse();
	    response.setIsInUserSpace(userspace);
	    validateComment(response, validator);
	    if (response.getErrors().isEmpty()) {
		Comment com = validator.getCommentSpec();
		//int returnCode = action.executeComment(com, response, registry, principal.getName());

		// If user name is left empty, fill it using the user's display name
		if (null == com.getUserName() || "".equals(com.getUserName())) {
		    if (userCredentials != null) {
			com.setUserName(userCredentials.getDisplayName());
		    } else {
			com.setUserName(principal.getName());
		    }
		}

		int returnCode = registry.registerComment(com, principal.getName());
		if (returnCode == 0) {
		    response.setRegistered(true);
		    response.setComment(com);
		} else {
		    response.setRegistered(false);
		    response.addError("Unable to register at this moment. Internal server error.");
		}
	    } else {
		LOG.info("Registration failed with validation errors:" + Arrays.toString(response.getErrors().toArray()));
		response.setRegistered(false);
	    }
	    return Response.ok(response).build();
	} catch (ComponentRegistryException ex) {
	    LOG.error("Error while inserting comment: ", ex);
	    return Response.serverError().entity(ex.getMessage()).build();
	} finally {
	    try {
		input.close();//either we read the input or there was an exception, we need to close it.
	    } catch (IOException e) {
		LOG.error("Error when closing inputstream: ", e);
		return Response.serverError().build();
	    }
	}
    }

    private ComponentDescription createNewComponentDescription() {
	ComponentDescription desc = ComponentDescription.createNewDescription();
	desc.setHref(createXlink(desc.getId()));
	return desc;
    }

    private ProfileDescription createNewProfileDescription() {
	ProfileDescription desc = ProfileDescription.createNewDescription();
	desc.setHref(createXlink(desc.getId()));
	return desc;
    }

    private String createXlink(String id) {
	URI uri = uriInfo.getRequestUriBuilder().path(id).build();
	return uri.toString();
    }

    /**
     *
     * @return The application's base URI as configured in the "componentRegistryServiceRootUrl" context parameter.
     * If correctly configured, it should look something like "http://catalog.clarin.eu/ds/ComponentRegistry".
     * <em>Be aware that this can also be null if configured incorrectly!</em>
     *
     * @see #APPLICATION_BASE_URL_PARAM
     */
    private String getApplicationBaseURI() {
	return servletContext.getInitParameter(APPLICATION_BASE_URL_PARAM);
    }

    private void validate(RegisterResponse response, Validator... validators) {
	for (Validator validator : validators) {
	    if (!validator.validate()) {
		for (String error : validator.getErrorMessages()) {
		    response.addError(error);
		}
	    }
	}
    }

    private void validateComment(CommentResponse response, Validator... validators) {
	for (Validator validator : validators) {
	    if (!validator.validate()) {
		for (String error : validator.getErrorMessages()) {
		    response.addError(error);
		}
	    }
	}
    }

    /**
     * @param componentRegistryFactory the componentRegistryFactory to set
     */
    public void setComponentRegistryFactory(ComponentRegistryFactory componentRegistryFactory) {
	this.componentRegistryFactory = componentRegistryFactory;
    }

    /// two muchually recursive methods below are used to set filenames of components (and their child components) to null
    /*
     * @param List<CMDComponentType> listofcomponents the list of components whose filenames (and the children's names) are to be set to null
     */
    public void setFileNamesFromListToNull(List<CMDComponentType> listofcomponents) {

	for (CMDComponentType currentcomponent : listofcomponents) {
	    setFileNamesToNullCurrent(currentcomponent);
	}

    }

    /*
     * @param CMDComponentType currentcomponent the component whose filename (and whose children filenames) is to be set to null
     */
    public void setFileNamesToNullCurrent(CMDComponentType currentcomponent) {

	currentcomponent.setFilename(null);
	setFileNamesFromListToNull(currentcomponent.getCMDComponent());

    }

    /*
     * generating rss: commom part for profile and component descriptions
     * 
     */
    
     private <T extends AbstractDescription> Rss getRss(boolean userspace,String limit, List<T> descs, String kindofdesc) throws ComponentRegistryException, ParseException {
	
        String baseUri = getApplicationBaseURI()+"/";
        RssCreatorDescriptions rssCreator = new RssCreatorDescriptions(baseUri);
        rssCreator.setVersion(2.0);
        
        
        int limitInt = Integer.parseInt(limit);
        
        if (descs.size()<limitInt) {limitInt = descs.size();};
        List<T> sublist = descs.subList(0, limitInt);
        Collections.sort(sublist, AbstractDescription.COMPARE_ON_DATE);
        
        
        if (userspace)   {rssCreator.setTitle("Workspace "+kindofdesc);} 
        else {rssCreator.setTitle("Public "+kindofdesc);}
         
        Rss rss =rssCreator.makeRss(sublist);

	return rss;
    }
    ////////////////////////////////////////////////

    @GET
    @Path("/components/rss")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Rss getRssComponent(@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace, @QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit) throws ComponentRegistryException, ParseException {
    
        List<ComponentDescription> components = getRegistry(getStatus(userspace)).getComponentDescriptions();
      Rss rss = getRss(userspace, limit, components, "components");
        
      LOG.info("Releasing " + limit + "most recent registered components into the world sorted by their registration date-and-time");
	
        
       return rss;
    }

    ////////////////////////////////////////////////
    @GET
    @Path("/profiles/rss")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Rss getRssProfile(@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace, @QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit) throws ComponentRegistryException, ParseException {

	//http://www.clarin.eu/cmdi/profiles 

	// ?? How to get rid of the deprecated stuff ??
	List<ProfileDescription> profiles = getRegistry(getStatus(userspace)).getProfileDescriptions();
	Rss rss = getRss(userspace, limit, profiles, "profiles");

	LOG.info("Releasing " + limit + "most recent registered profiles into the world sorted by their registration date-and-time");
	return rss;
    }

    ///////////////////////////////////////////////////////////
    @GET
    @Path("/profiles/{profileId}/comments/rss")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Rss getRssOfCommentsFromProfile(@PathParam("profileId") String profileId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace, @QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit) throws ComponentRegistryException, IOException, JAXBException, ParseException {


	// ?? this is for debugging. But, anyway, how to get all the profiles without the need to type them in?
	// what if a user do not remember his/her id of profile?
        /* grabbing all registered profile names from the register and outputting them  on the tomcat terminal */
	List<ProfileDescription> lprfaux = getRegisteredProfiles(userspace, true);


	for (ProfileDescription currentProfile : lprfaux) {

	    String currentProfileId = currentProfile.getId();
	    LOG.debug(currentProfileId);

	}
	/* end of grabbing */


	// TODO: add sorting !

	final Principal principal = security.getUserPrincipal();
	List<Comment> comments = getRegistry(getStatus(userspace)).getCommentsInProfile(profileId, principal);

        String baseUri = getApplicationBaseURI()+"/";
        
        LOG.debug("Basis uri "+baseUri);
        
        
        int limitInt = Integer.parseInt(limit);
        
        if (comments.size()<limitInt) {limitInt = comments.size();};
        List<Comment> sublist = comments.subList(0, limitInt);
        
        
         
        RssCreatorComments instance = new RssCreatorComments(baseUri);
        instance.setFlagIsFromProfile(true);
        instance.setDescription("Update of comments for current profile");
        instance.setTitle("Comments feed for the profile "+profileId);
        
        Rss result = instance.makeRss(sublist);
        
        
        
        // testing stuff
        String path=openTestDir("testRss");
        String os = MDMarshaller.marshalToString(result);
        writeStringToFile(os, path + "testRssResl.xml");
        
        
       
        LOG.debug("The amount of items: "+Integer.toString(result.getChannel().getItem().size()));
        LOG.debug("The amount of comments: "+Integer.toString(comments.size()));
        // end of testing stuff
        

	return result;
    }

    ////////////////////////////////////////////////////////
    // temporarily for rss-bug fixing
    /**
     *
     * @param bytes is an array of bytes to be written in the file filename (from scratch!)
     * @param filename is the name of the file where the array "bytes" is to be written to
     * @throws IOException
     * @throws JAXBException
     */
    public static void writeBytesToFile(byte[] bytes, String filename) throws IOException, JAXBException {

	File file = new File(filename);
	FileOutputStream fop = new FileOutputStream(file);

	fop.write(bytes);

	fop.flush();
	fop.close();


    }

    /**
     *
     * @param str is a string which is to be written into the filename (from scratch!)
     * @param filename is a filename where the string is to be written to
     * @throws IOException
     * @throws JAXBException
     */
    public static void writeStringToFile(String str, String filename) throws IOException, JAXBException {

	writeBytesToFile(str.getBytes(), filename);


    }

    /**
     *
     * @param os is an output stream which is to be written into the filename (from scratch!)
     * @param filename is a filename where the stream is to be written to
     * @throws IOException
     * @throws JAXBException
     */
    public static void writeStreamToFile(ByteArrayOutputStream os, String filename) throws IOException, JAXBException {

	writeBytesToFile(os.toByteArray(), filename);


    }

    /**
     *
     * @param cdesc is a component which is to be written into the filename (from scratch!)
     * @param filename is a filename where the component is to be written to
     * @throws IOException
     * @throws JAXBException
     */
    public static void writeComponentIntoFile(ComponentDescription cdesc, String filename) throws IOException, JAXBException {


	ByteArrayOutputStream os = new ByteArrayOutputStream();
	MDMarshaller.marshal(cdesc, os);

	writeStreamToFile(os, filename);

    }

    /**
     * opens a temporary sub-directory dirName in /target/
     *
     * @param dirName is the name of the temporary subdirectory which is to be opened
     * @return the absolute part for this directory
     */
    public static String openTestDir(String dirName) {

	File testDir = new File("target/" + dirName);


	testDir.mkdir();

	System.out.println(dirName);
	//String retval = new File(testDir, dirName).getAbsolutePath();
	String retval = new File(testDir, "/").getAbsolutePath();

	return (retval);

    }
}
