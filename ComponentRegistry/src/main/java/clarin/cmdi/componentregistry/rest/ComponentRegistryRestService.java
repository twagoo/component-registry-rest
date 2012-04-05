package clarin.cmdi.componentregistry.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.CommentResponse;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.inject.Inject;

@Path("/registry")
public class ComponentRegistryRestService {

    @Context
    private UriInfo uriInfo;
    @Context
    private SecurityContext security;
    @Context
    private HttpServletRequest request;
    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryRestService.class);
    public static final String DATA_FORM_FIELD = "data";
    public static final String NAME_FORM_FIELD = "name";
    public static final String DESCRIPTION_FORM_FIELD = "description";
    public static final String GROUP_FORM_FIELD = "group";
    public static final String DOMAIN_FORM_FIELD = "domainName";
    public static final String USERSPACE_PARAM = "userspace";
    public static final String METADATA_EDITOR_PARAM = "mdEditor";
    @Inject(value = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;

    private ComponentRegistry getRegistry(boolean userspace) {
	Principal userPrincipal = security.getUserPrincipal();
	UserCredentials userCredentials = getUserCredentials(userPrincipal);
	return getRegistry(userspace, userCredentials);
    }

    private ComponentRegistry getRegistry(boolean userspace, UserCredentials userCredentials) {
	return componentRegistryFactory.getComponentRegistry(userspace, userCredentials);
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
	List<ComponentDescription> components = getRegistry(userspace).getComponentDescriptions();
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
	    profiles = getRegistry(userspace).getProfileDescriptionsForMetadaEditor();
	} else {
	    profiles = getRegistry(userspace).getProfileDescriptions();
	}

	LOG.info("Releasing " + profiles.size() + " registered profiles into the world (" + (System.currentTimeMillis() - start)
		+ " millisecs)");
	return profiles;
    }

    @GET
    @Path("/components/{componentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public CMDComponentSpec getRegisteredComponent(@PathParam("componentId") String componentId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	LOG.info("Component with id: " + componentId + " is requested.");
	return getRegistry(userspace).getMDComponent(componentId);
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
	ComponentRegistry result = getRegistry(false);
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
    public CMDComponentSpec getRegisteredProfile(@PathParam("profileId") String profileId,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	LOG.info("Profile with id: " + profileId + " is requested.");
	return getRegistry(userspace).getMDProfile(profileId);
    }

    @GET
    @Path("/components/usage/{componentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<AbstractDescription> getComponentUsage(@PathParam("componentId") String componentId, @QueryParam(USERSPACE_PARAM) @DefaultValue("true") boolean userspace) throws ComponentRegistryException {
	try {
	    final long start = System.currentTimeMillis();
	    ComponentRegistry registry = getRegistry(userspace);
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
	List<Comment> comments = getRegistry(userspace).getCommentsInProfile(profileId);
	LOG.info("Releasing " + comments.size() + " registered comments in Profile into the world (" + (System.currentTimeMillis() - start)
		+ " millisecs)");
	return comments;
    }

    @GET
    @Path("/components/{componentId}/comments")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Comment> getCommentsFromComponent(@PathParam("componentId") String componentId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	long start = System.currentTimeMillis();
	List<Comment> comments = getRegistry(userspace).getCommentsInComponent(componentId);
	LOG.info("Releasing " + comments.size() + " registered comments in Component into the world (" + (System.currentTimeMillis() - start)
		+ " millisecs)");
	return comments;
    }

    @GET
    @Path("/profiles/{profileId}/comments/{commentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Comment getSpecifiedCommentFromProfile(@PathParam("profileId") String profileId, @PathParam("commentId") String commentId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	LOG.info(" Comments of component with id" + commentId + " are requested.");
	return getRegistry(userspace).getSpecifiedCommentInProfile(profileId, commentId);
    }

    @GET
    @Path("/components/{componentId}/comments/{commentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Comment getSpecifiedCommentFromComponent(@PathParam("componentId") String componentId, @PathParam("commentId") String commentId, @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) throws ComponentRegistryException {
	LOG.info(" Comments of component with id" + commentId + " are requested.");
	return getRegistry(userspace).getSpecifiedCommentInComponent(componentId, commentId);
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
    public Response manipulateCommentFromProfile(@PathParam("profileId") String profileId, @PathParam("profileId") String commentId, @FormParam("method") String method,
	    @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
	if ("delete".equalsIgnoreCase(method)) {
	    return deleteCommentFromProfile(profileId, commentId, userspace);
	} else {
	    return Response.ok().build();
	}
    }

    @POST
    @Path("/components/{componentId}/comments/{commentId}")
    public Response manipulateCommentFromComponent(@PathParam("componentId") String componentId, @PathParam("profileId") String commentId, @FormParam("method") String method,
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
	    ProfileDescription desc = getRegistry(true).getProfileDescription(profileId);
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
	    ProfileDescription desc = getRegistry(userspace).getProfileDescription(profileId);
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
	    ComponentDescription desc = getRegistry(true).getComponentDescription(componentId);
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
	    ComponentDescription desc = getRegistry(userspace).getComponentDescription(componentId);
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
	    ComponentRegistry registry = getRegistry(userspace);
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
	    getRegistry(userspace).deleteMDProfile(profileId, principal);
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
	    final ComponentRegistry registry = getRegistry(userspace);
	    final Comment comment = registry.getSpecifiedCommentInProfile(profileId, commentId);
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
	    final ComponentRegistry registry = getRegistry(userspace);
	    final Comment comment = registry.getSpecifiedCommentInComponent(componentId, commentId);
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
	    ComponentRegistry registry = getRegistry(userspace, userCredentials);
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
	    ComponentRegistry registry = getRegistry(userspace, userCredentials);
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
	    ComponentRegistry registry = getRegistry(userspace, userCredentials);
	    DescriptionValidator descriptionValidator = new DescriptionValidator(desc);
	    MDValidator validator = new MDValidator(input, desc, registry, getRegistry(true), componentRegistryFactory.getPublicRegistry());
	    RegisterResponse response = new RegisterResponse();
	    response.setIsInUserSpace(userspace);
	    validate(response, descriptionValidator, validator);
	    if (response.getErrors().isEmpty()) {
		CMDComponentSpec spec = validator.getCMDComponentSpec();
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
	    if (response.isRegistered()) {
		LOG.info("Registration of {} was successful", response.getDescription().getId());
	    }
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

    private Comment createNewComment() {
	Comment com = Comment.createANewComment();
	return com;
    }

    private String createXlink(String id) {
	URI uri = uriInfo.getRequestUriBuilder().path(id).build();
	return uri.toString();
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
}
