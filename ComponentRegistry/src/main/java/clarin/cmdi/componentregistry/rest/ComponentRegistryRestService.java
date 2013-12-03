package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AllowedAttributetypesXML;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ComponentStatus;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.Owner;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.impl.database.GroupService;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.CommentResponse;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssCreatorComments;
import clarin.cmdi.componentregistry.rss.RssCreatorDescriptions;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles CRUD operations on {@link ComponentDescription}, {@link ProfileDescription} and {@link Comment}s
 * @author twago@mpi.nl
 * @author olsha@mpi.nl
 * @author george.georgovassilis@mpi.nl
 *
 */
@Path("/registry")
@Service
@Transactional(rollbackFor= {Exception.class, ValidationException.class})
public class ComponentRegistryRestService implements
		IComponentRegistryRestService {

	private final static Logger LOG = LoggerFactory
			.getLogger(IComponentRegistryRestService.class);
	@Context
	private UriInfo uriInfo;
	@Context
	private SecurityContext security;
	@Context
	private HttpServletRequest request;
	@Context
	private ServletContext servletContext;
	@InjectParam(value = "componentRegistryFactory")
	private ComponentRegistryFactory componentRegistryFactory;
	@InjectParam(value = "mdMarshaller")
	private MDMarshaller marshaller;
	
	@Autowired
	private GroupService groupService;

	/**
	 * Converts userspace boolean to component status. Temporary solution!!!
	 * 
	 * TODO: Replace all calls to getRegistry that use this by calls using
	 * ComponentStatus
	 * 
	 * 
	 * 
	 * @param userSpace
	 * @return
	 * @deprecated All calls should go directly to
	 *             {@link #getRegistry(clarin.cmdi.componentregistry.ComponentStatus)}
	 */
	@Deprecated
	private static ComponentStatus getStatus(boolean userSpace) {
		if (userSpace) {
			return ComponentStatus.PRIVATE;
		} else {
			return ComponentStatus.PUBLISHED;
		}
	}

	private ComponentRegistry getRegistry(ComponentStatus status) {
		Principal userPrincipal = security.getUserPrincipal();
		UserCredentials userCredentials = getUserCredentials(userPrincipal);
		return getRegistry(status, null, userCredentials);
	}

	private ComponentRegistry getRegistry(ComponentStatus status, Owner owner,
			UserCredentials userCredentials) {
		try {
			return componentRegistryFactory.getComponentRegistry(status, owner,
					userCredentials);
		} catch (UserUnauthorizedException uuEx) {
			LOG.warn("Unauthorized access to {} registry by user {}", status,
					userCredentials);
			LOG.debug("Details for unauthorized access", uuEx);
			throw new WebApplicationException(uuEx, Status.UNAUTHORIZED);
		}
	}

	/**
	 * 
	 * @return Principal of current request
	 * @throws IllegalArgumentException
	 *             If no user principal found
	 */
	private Principal checkAndGetUserPrincipal()
			throws UserUnauthorizedException {
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
	
	@Override
	@GET
	@Path("/components")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public List<ComponentDescription> getRegisteredComponents(
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace,
			@QueryParam(GROUPID_PARAM) String groupId)
			throws ComponentRegistryException {
		long start = System.currentTimeMillis();
		
		List<ComponentDescription> components = null;
		ComponentRegistry cr = getRegistry(getStatus(userspace));
		if (groupId == null||groupId.isEmpty())
		    components = cr.getComponentDescriptions();
		else {
		    Principal principal = security.getUserPrincipal();
		    UserCredentials credentials = getUserCredentials(principal);
 		    components = cr.getComponentDescriptionsInGroup(principal.getName(), groupId);

		}
		LOG.debug(
				"Releasing {} registered components into the world ({} millisecs)",
				components.size(), (System.currentTimeMillis() - start));
		return components;
	}

	@Override
	@GET
	@Path("/profiles")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public List<ProfileDescription> getRegisteredProfiles(
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace,
			@QueryParam(METADATA_EDITOR_PARAM) @DefaultValue("false") boolean metadataEditor,
			@QueryParam(GROUPID_PARAM) String groupId
			)
			throws ComponentRegistryException {
		long start = System.currentTimeMillis();

		List<ProfileDescription> profiles;
		ComponentRegistry cr = getRegistry(getStatus(userspace));
		if (metadataEditor) {
		    if (groupId == null || groupId.isEmpty())
			profiles = cr.getProfileDescriptionsForMetadaEditor();
		    else
			profiles = cr.getProfileDescriptionsForMetadaEditor(groupId);
		} else {
		    if (groupId == null || groupId.isEmpty())
			profiles = cr.getProfileDescriptions();
		    else
			profiles = cr.getProfileDescriptionsInGroup(groupId);
		}

		LOG.debug(
				"Releasing {} registered profiles into the world ({} millisecs)",
				profiles.size(), (System.currentTimeMillis() - start));
		return profiles;
	}

	@Override
	@GET
	@Path("/components/{componentId}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Response getRegisteredComponent(
			@PathParam("componentId") String componentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace
			)
			throws ComponentRegistryException {
		LOG.debug("Component with id: {} is requested.", componentId);
		CMDComponentSpec mdComponent = getRegistry(getStatus(userspace))
				.getMDComponent(componentId);
		if (mdComponent == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			return Response.ok(mdComponent).build();
		}
	}

	@Override
	@GET
	@Path("/components/{componentId}/{rawType}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public Response getRegisteredComponentRawType(
			@PathParam("componentId") final String componentId,
			@PathParam("rawType") String rawType) {
		LOG.debug("Component with id: {} and rawType: {} is requested.",
				componentId, rawType);
		StreamingOutput result = null;
		try {
			final ComponentRegistry registry = findRegistry(componentId,
					new ComponentClosure());
			if (registry == null) {
				return Response
						.status(Status.NOT_FOUND)
						.entity("Id: " + componentId
								+ " is not registered, cannot create data.")
						.build();
			}
			ComponentDescription desc = registry
					.getComponentDescription(componentId);
			checkAndThrowDescription(desc, componentId);
			String fileName = desc.getName() + "." + rawType;
			if ("xml".equalsIgnoreCase(rawType)) {
				result = new StreamingOutput() {
					@Override
					public void write(OutputStream output) throws IOException,
							WebApplicationException {
						try {
							registry.getMDComponentAsXml(componentId, output);
						} catch (ComponentRegistryException e) {
							LOG.warn("Could not retrieve component {}",
									componentId);
							LOG.debug("Details", e);
							throw new WebApplicationException(e, Response
									.serverError()
									.status(Status.INTERNAL_SERVER_ERROR)
									.build());
						}
					}
				};
			} else if ("xsd".equalsIgnoreCase(rawType)) {
				result = new StreamingOutput() {
					@Override
					public void write(OutputStream output) throws IOException,
							WebApplicationException {
						try {
							registry.getMDComponentAsXsd(componentId, output);
						} catch (ComponentRegistryException e) {
							LOG.warn("Could not retrieve component {}",
									componentId);
							LOG.debug("Details", e);
							throw new WebApplicationException(e, Response
									.serverError()
									.status(Status.INTERNAL_SERVER_ERROR)
									.build());
						}

					}
				};
			} else {
				throw new WebApplicationException(Response
						.serverError()
						.entity("unsupported rawType: " + rawType
								+ " (only xml or xsd are supported)").build());
			}
			return createDownloadResponse(result, fileName);
		} catch (ComponentRegistryException e) {
			LOG.warn("Could not retrieve component {}", componentId);
			LOG.debug("Details", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

	@Override
	public ComponentRegistry findRegistry(String id,
			RegistryClosure<? extends BaseDescription> clos)
			throws ComponentRegistryException {
		BaseDescription desc = null;
		ComponentRegistry result = getRegistry(getStatus(false));
		desc = clos.getDescription(result, id);
		if (desc == null) {
			List<ComponentRegistry> userRegs = componentRegistryFactory
					.getAllUserRegistries();
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

	@Override
	@GET
	@Path("/profiles/{profileId}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Response getRegisteredProfile(
			@PathParam("profileId") String profileId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace)
			throws ComponentRegistryException {
		LOG.debug("Profile with id {} is requested.", profileId);
		CMDComponentSpec mdProfile = getRegistry(getStatus(userspace))
				.getMDProfile(profileId);
		if (mdProfile == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			return Response.ok(mdProfile).build();
		}
	}

	@Override
	@GET
	@Path("/components/usage/{componentId}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public List<BaseDescription> getComponentUsage(
			@PathParam("componentId") String componentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace)
			throws ComponentRegistryException {
		try {
			final long start = System.currentTimeMillis();
			ComponentRegistry registry = getRegistry(getStatus(userspace));
			List<ComponentDescription> components = registry
					.getUsageInComponents(componentId);
			List<ProfileDescription> profiles = registry
					.getUsageInProfiles(componentId);

			LOG.debug(
					"Found {} components and {} profiles that use component {} ({} millisecs)",
					components.size(), profiles.size(), componentId,
					(System.currentTimeMillis() - start));

			List<BaseDescription> usages = new ArrayList<BaseDescription>(
					components.size() + profiles.size());
			usages.addAll(components);
			usages.addAll(profiles);

			return usages;
		} catch (ComponentRegistryException e) {
			LOG.warn("Could not retrieve profile usage {}", componentId);
			LOG.debug("Details", e);
			throw e;
		}
	}

	@Override
	@GET
	@Path("/profiles/{profileId}/comments")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public List<Comment> getCommentsFromProfile(
			@PathParam("profileId") String profileId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace)
			throws ComponentRegistryException {
		long start = System.currentTimeMillis();
		final Principal principal = security.getUserPrincipal();
		List<Comment> comments = getRegistry(getStatus(userspace))
				.getCommentsInProfile(profileId, principal);
		LOG.debug(
				"Releasing {} registered comments in profile into the world ({} millisecs)",
				comments.size(), (System.currentTimeMillis() - start));
		return comments;
	}

	@Override
	@GET
	@Path("/components/{componentId}/comments")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public List<Comment> getCommentsFromComponent(
			@PathParam("componentId") String componentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace)
			throws ComponentRegistryException {
		long start = System.currentTimeMillis();
		final Principal principal = security.getUserPrincipal();
		List<Comment> comments = getRegistry(getStatus(userspace))
				.getCommentsInComponent(componentId, principal);
		LOG.debug(
				"Releasing {} registered comments in Component into the world ({} millisecs)",
				comments.size(), (System.currentTimeMillis() - start));
		return comments;
	}

	@Override
	@GET
	@Path("/profiles/{profileId}/comments/{commentId}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Comment getSpecifiedCommentFromProfile(
			@PathParam("profileId") String profileId,
			@PathParam("commentId") String commentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace)
			throws ComponentRegistryException {
		LOG.debug("Comments of profile with id {} are requested.", commentId);
		final Principal principal = security.getUserPrincipal();
		return getRegistry(getStatus(userspace)).getSpecifiedCommentInProfile(
				profileId, commentId, principal);
	}

	@Override
	@GET
	@Path("/components/{componentId}/comments/{commentId}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Comment getSpecifiedCommentFromComponent(
			@PathParam("componentId") String componentId,
			@PathParam("commentId") String commentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace)
			throws ComponentRegistryException {
		LOG.debug("Comments of component with id {} are requested.", commentId);
		final Principal principal = security.getUserPrincipal();
		return getRegistry(getStatus(userspace))
				.getSpecifiedCommentInComponent(componentId, commentId,
						principal);
	}

	/**
	 * 
	 * Purely helper method for my front-end (FLEX) which only does post/get
	 * requests. The query param is checked and the "proper" method is called.
	 * 
	 * @param profileId
	 * @param method
	 * @return
	 */
	@Override
	@POST
	@Path("/profiles/{profileId}")
	public Response manipulateRegisteredProfile(
			@PathParam("profileId") String profileId,
			@FormParam("method") String method,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		if ("delete".equalsIgnoreCase(method)) {
			return deleteRegisteredProfile(profileId, userspace);
		} else {
			return Response.ok().build();
		}
	}

	@Override
	@POST
	@Path("/profiles/{profileId}/comments/{commentId}")
	public Response manipulateCommentFromProfile(
			@PathParam("profileId") String profileId,
			@PathParam("commentId") String commentId,
			@FormParam("method") String method,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		if ("delete".equalsIgnoreCase(method)) {
			return deleteCommentFromProfile(profileId, commentId, userspace);
		} else {
			return Response.ok().build();
		}
	}

	@Override
	@POST
	@Path("/components/{componentId}/comments/{commentId}")
	public Response manipulateCommentFromComponent(
			@PathParam("componentId") String componentId,
			@PathParam("commentId") String commentId,
			@FormParam("method") String method,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		if ("delete".equalsIgnoreCase(method)) {
			return deleteCommentFromComponent(componentId, commentId, userspace);
		} else {
			return Response.ok().build();
		}
	}

	@Override
	@POST
	@Path("/profiles/{profileId}/publish")
	@Consumes("multipart/form-data")
	public Response publishRegisteredProfile(
			@PathParam("profileId") String profileId,
			@FormDataParam(DATA_FORM_FIELD) InputStream input,
			@FormDataParam(NAME_FORM_FIELD) String name,
			@FormDataParam(DESCRIPTION_FORM_FIELD) String description,
			@FormDataParam(GROUP_FORM_FIELD) String group,
			@FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
		try {
			Principal principal = checkAndGetUserPrincipal();
			ProfileDescription desc = getRegistry(getStatus(true))
					.getProfileDescription(profileId);
			if (desc != null) {
				updateDescription(desc, name, description, domainName, group);
				return register(input, desc, getUserCredentials(principal),
						true, new PublishAction(principal));
			} else {
				LOG.error("Update of nonexistent profile {} failed.", profileId);
				return Response
						.serverError()
						.entity("Invalid id, cannot update nonexistent profile")
						.build();
			}
		} catch (ComponentRegistryException e) {
			LOG.warn("Could not retrieve profile {}", profileId);
			LOG.debug("Details", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (UserUnauthorizedException ex) {
			return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage())
					.build();
		}
	}

	@Override
	@POST
	@Path("/profiles/{profileId}/update")
	@Consumes("multipart/form-data")
	public Response updateRegisteredProfile(
			@PathParam("profileId") String profileId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace,
			@FormDataParam(DATA_FORM_FIELD) InputStream input,
			@FormDataParam(NAME_FORM_FIELD) String name,
			@FormDataParam(DESCRIPTION_FORM_FIELD) String description,
			@FormDataParam(GROUP_FORM_FIELD) String group,
			@FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
		try {
			Principal principal = checkAndGetUserPrincipal();
			UserCredentials userCredentials = getUserCredentials(principal);
			ProfileDescription desc = getRegistry(getStatus(userspace))
					.getProfileDescription(profileId);
			if (desc != null) {
				updateDescription(desc, name, description, domainName, group);
				return register(input, desc, userCredentials, userspace,
						new UpdateAction(principal));
			} else {
				LOG.error("Update of nonexistent id (" + profileId
						+ ") failed.");
				return Response
						.serverError()
						.entity("Invalid id, cannot update nonexistent profile")
						.build();
			}
		} catch (ComponentRegistryException e) {
			LOG.warn("Could not retrieve profile {}", profileId);
			LOG.debug("Details", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (UserUnauthorizedException ex) {
			return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage())
					.build();
		}

	}

	/**
	 * 
	 * Purely helper method for my front-end (FLEX) which van only do post/get
	 * requests. The query param is checked and the "proper" method is called.
	 * 
	 * @param componentId
	 * @param method
	 * @return
	 */
	@Override
	@POST
	@Path("/components/{componentId}")
	public Response manipulateRegisteredComponent(
			@PathParam("componentId") String componentId,
			@FormParam("method") String method,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		if ("delete".equalsIgnoreCase(method)) {
			return deleteRegisteredComponent(componentId, userspace);
		} else {
			return Response.ok().build();
		}
	}

	@Override
	@POST
	@Path("/components/{componentId}/publish")
	@Consumes("multipart/form-data")
	public Response publishRegisteredComponent(
			@PathParam("componentId") String componentId,
			@FormDataParam(DATA_FORM_FIELD) InputStream input,
			@FormDataParam(NAME_FORM_FIELD) String name,
			@FormDataParam(DESCRIPTION_FORM_FIELD) String description,
			@FormDataParam(GROUP_FORM_FIELD) String group,
			@FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
		try {
			Principal principal = checkAndGetUserPrincipal();
			// TODO: Get status from parameter
			ComponentDescription desc = getRegistry(getStatus(true))
					.getComponentDescription(componentId);
			if (desc != null) {
				updateDescription(desc, name, description, domainName, group);
				return register(input, desc, getUserCredentials(principal),
						true, new PublishAction(principal));
			} else {
				LOG.error("Update of nonexistent id (" + componentId
						+ ") failed.");
				return Response
						.serverError()
						.entity("Invalid id, cannot update nonexistent profile")
						.build();
			}
		} catch (ComponentRegistryException e) {
			LOG.warn("Could not retrieve component {}", componentId);
			LOG.debug("Details", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (UserUnauthorizedException ex) {
			return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage())
					.build();
		}
	}

	@Override
	@POST
	@Path("/components/{componentId}/update")
	@Consumes("multipart/form-data")
	public Response updateRegisteredComponent(
			@PathParam("componentId") String componentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace,
			@FormDataParam(DATA_FORM_FIELD) InputStream input,
			@FormDataParam(NAME_FORM_FIELD) String name,
			@FormDataParam(DESCRIPTION_FORM_FIELD) String description,
			@FormDataParam(GROUP_FORM_FIELD) String group,
			@FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
		try {
			Principal principal = checkAndGetUserPrincipal();
			ComponentDescription desc = getRegistry(getStatus(userspace))
					.getComponentDescription(componentId);
			if (desc != null) {
				updateDescription(desc, name, description, domainName, group);
				return register(input, desc, getUserCredentials(principal),
						userspace, new UpdateAction(principal));
			} else {
				LOG.error("Update of nonexistent id (" + componentId
						+ ") failed.");
				return Response
						.serverError()
						.entity("Invalid id, cannot update nonexistent component")
						.build();
			}
		} catch (ComponentRegistryException e) {
			LOG.warn("Could not retrieve component {}", componentId);
			LOG.debug("Details", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (UserUnauthorizedException ex) {
			return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage())
					.build();
		}
	}

	private void updateDescription(BaseDescription desc, String name,
			String description, String domainName, String group) {
		desc.setName(name);
		desc.setDescription(description);
		desc.setDomainName(domainName);
		desc.setGroupName(group);
		desc.setRegistrationDate(new Date());
	}

	@Override
	@DELETE
	@Path("/components/{componentId}")
	public Response deleteRegisteredComponent(
			@PathParam("componentId") String componentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		try {
			Principal principal = checkAndGetUserPrincipal();
			ComponentRegistry registry = getRegistry(getStatus(userspace));
			LOG.debug("Component with id {} set for deletion.", componentId);
			registry.deleteMDComponent(componentId, principal, false);
		} catch (DeleteFailedException e) {
			LOG.info("Component with id {} deletion failed. Reason: {}",
					componentId, e.getMessage());
			LOG.debug("Deletion failure details:", e);
			return Response.status(Status.FORBIDDEN)
					.entity("" + e.getMessage()).build();
		} catch (ComponentRegistryException e) {
			LOG.warn("Component with id " + componentId + " deletion failed.",
					e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (IOException e) {
			LOG.error("Component with id " + componentId + " deletion failed.",
					e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (UserUnauthorizedException e) {
			LOG.info("Component with id {} deletion failed: {}", componentId,
					e.getMessage());
			LOG.debug("Deletion failure details:", e);
			return Response.serverError().status(Status.UNAUTHORIZED)
					.entity("" + e.getMessage()).build();
		}
		LOG.info("Component with id: {} deleted.", componentId);
		return Response.ok().build();
	}

	@Override
	@DELETE
	@Path("/profiles/{profileId}")
	public Response deleteRegisteredProfile(
			@PathParam("profileId") String profileId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		try {
			Principal principal = checkAndGetUserPrincipal();
			LOG.debug("Profile with id: {} set for deletion.", profileId);
			getRegistry(getStatus(userspace)).deleteMDProfile(profileId,
					principal);
		} catch (DeleteFailedException e) {
			LOG.info("Profile with id: {} deletion failed: {}", profileId,
					e.getMessage());
			LOG.debug("Deletion failure details:", e);
			return Response.serverError().status(Status.FORBIDDEN)
					.entity("" + e.getMessage()).build();
		} catch (ComponentRegistryException e) {
			LOG.warn("Could not retrieve component", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (IOException e) {
			LOG.error("Profile with id: " + profileId + " deletion failed.", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (UserUnauthorizedException e) {
			LOG.info("Profile with id: {} deletion failed: {}", profileId,
					e.getMessage());
			LOG.debug("Deletion failure details:", e);
			return Response.serverError().status(Status.UNAUTHORIZED)
					.entity("" + e.getMessage()).build();
		}
		LOG.info("Profile with id: {} deleted.", profileId);
		return Response.ok().build();
	}

	@Override
	@DELETE
	@Path("/profiles/{profileId}/comments/{commentId}")
	public Response deleteCommentFromProfile(
			@PathParam("profileId") String profileId,
			@PathParam("commentId") String commentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		try {
			final Principal principal = checkAndGetUserPrincipal();
			final ComponentRegistry registry = getRegistry(getStatus(userspace));
			final Comment comment = registry.getSpecifiedCommentInProfile(
					profileId, commentId, principal);
			if (comment != null
					&& profileId.equals(comment.getComponentId())) {
				LOG.debug("Comment with id: {} set for deletion.", commentId);
				registry.deleteComment(commentId, principal);
			} else {
				throw new ComponentRegistryException(
						"Comment not found for specified profile");
			}
		} catch (DeleteFailedException e) {
			LOG.info("Comment with id: {} deletion failed: {}", commentId,
					e.getMessage());
			LOG.debug("Deletion failure details:", e);
			return Response.serverError().status(Status.FORBIDDEN)
					.entity("" + e.getMessage()).build();
		} catch (ComponentRegistryException e) {
			LOG.info("Could not retrieve component", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (IOException e) {
			LOG.error("Comment with id: " + commentId + " deletion failed.", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (UserUnauthorizedException e) {
			LOG.info("Comment with id: {} deletion failed: {}", commentId,
					e.getMessage());
			LOG.debug("Deletion failure details:", e);
			return Response.serverError().status(Status.UNAUTHORIZED)
					.entity("" + e.getMessage()).build();
		}
		LOG.info("Comment with id: {} deleted.", commentId);
		return Response.ok().build();
	}

	@Override
	@DELETE
	@Path("/components/{componentId}/comments/{commentId}")
	public Response deleteCommentFromComponent(
			@PathParam("componentId") String componentId,
			@PathParam("commentId") String commentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		try {
			final Principal principal = checkAndGetUserPrincipal();
			final ComponentRegistry registry = getRegistry(getStatus(userspace));
			final Comment comment = registry.getSpecifiedCommentInComponent(
					componentId, commentId, principal);
			if (comment != null
					&& componentId.equals(comment.getComponentId())) {
				LOG.debug("Comment with id: {} set for deletion.", commentId);
				registry.deleteComment(commentId, principal);
			} else {
				throw new ComponentRegistryException(
						"Comment not found for specified component");
			}
		} catch (DeleteFailedException e) {
			LOG.info("Comment with id: {} deletion failed: {}", commentId,
					e.getMessage());
			LOG.debug("Deletion failure details:", e);
			return Response.serverError().status(Status.FORBIDDEN)
					.entity("" + e.getMessage()).build();
		} catch (ComponentRegistryException e) {
			LOG.info("Could not retrieve component", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (IOException e) {
			LOG.error("Comment with id: " + commentId + " deletion failed.", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		} catch (UserUnauthorizedException e) {
			LOG.info("Comment with id: {} deletion failed: {}", commentId,
					e.getMessage());
			LOG.debug("Deletion failure details:", e);
			return Response.serverError().status(Status.UNAUTHORIZED)
					.entity("" + e.getMessage()).build();
		}
		LOG.info("Comment with id: {} deleted.", commentId);
		return Response.ok().build();
	}

	@Override
	@GET
	@Path("/profiles/{profileId}/{rawType}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public Response getRegisteredProfileRawType(
			@PathParam("profileId") final String profileId,
			@PathParam("rawType") String rawType) {
		LOG.debug("Profile with id {} and rawType {} is requested.", profileId,
				rawType);
		StreamingOutput result = null;
		try {
			final ComponentRegistry registry = findRegistry(profileId,
					new ProfileClosure());
			if (registry == null) {
				return Response
						.status(Status.NOT_FOUND)
						.entity("Id: " + profileId
								+ " is not registered, cannot create data.")
						.build();
			}
			ProfileDescription desc = registry.getProfileDescription(profileId);
			checkAndThrowDescription(desc, profileId);
			String fileName = desc.getName() + "." + rawType;

			if ("xml".equalsIgnoreCase(rawType)) {
				result = new StreamingOutput() {
					@Override
					public void write(OutputStream output) throws IOException,
							WebApplicationException {
						try {
							registry.getMDProfileAsXml(profileId, output);
						} catch (ComponentRegistryException e) {
							LOG.warn("Could not retrieve component {}",
									profileId);
							LOG.debug("Details", e);
							throw new WebApplicationException(e, Response
									.serverError()
									.status(Status.INTERNAL_SERVER_ERROR)
									.build());
						}
					}
				};
			} else if ("xsd".equalsIgnoreCase(rawType)) {
				result = new StreamingOutput() {
					@Override
					public void write(OutputStream output) throws IOException,
							WebApplicationException {
						try {
							registry.getMDProfileAsXsd(profileId, output);
						} catch (ComponentRegistryException e) {
							LOG.warn("Could not retrieve component {}",
									profileId);
							LOG.debug("Details", e);
							throw new WebApplicationException(e, Response
									.serverError()
									.status(Status.INTERNAL_SERVER_ERROR)
									.build());
						}
					}
				};
			} else {
				throw new WebApplicationException(Response
						.serverError()
						.entity("unsupported rawType: " + rawType
								+ " (only xml or xsd are supported)").build());
			}
			return createDownloadResponse(result, fileName);
		} catch (ComponentRegistryException e) {
			LOG.warn("Could not retrieve component {}", profileId);
			LOG.debug("Details", e);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

	private void checkAndThrowDescription(BaseDescription desc, String id) {
		if (desc == null) {
			throw new WebApplicationException(Response.serverError()
					.entity("Incorrect id:" + id + "cannot handle request")
					.build());
		}
	}

	private Response createDownloadResponse(StreamingOutput result,
			String fileName) {
		// Making response so it triggers browsers native save as dialog.
		Response response = Response
				.ok()
				.type("application/x-download")
				.header("Content-Disposition",
						"attachment; filename=\"" + fileName + "\"")
				.entity(result).build();
		return response;

	}

	@Override
	@POST
	@Path("/profiles")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	@Consumes("multipart/form-data")
	public Response registerProfile(
			@FormDataParam(DATA_FORM_FIELD) InputStream input,
			@FormDataParam(NAME_FORM_FIELD) String name,
			@FormDataParam(DESCRIPTION_FORM_FIELD) String description,
			@FormDataParam(GROUP_FORM_FIELD) String group,
			@FormDataParam(DOMAIN_FORM_FIELD) String domainName,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		try {
			Principal principal = checkAndGetUserPrincipal();
			UserCredentials userCredentials = getUserCredentials(principal);
			ProfileDescription desc = createNewProfileDescription();
			desc.setCreatorName(userCredentials.getDisplayName());
			desc.setUserId(userCredentials.getPrincipalName()); // Hash used to
																// be created
																// here, now Id
																// is
																// constructed
																// by impl
			desc.setName(name);
			desc.setDescription(description);
			desc.setGroupName(group);
			desc.setDomainName(domainName);
			LOG.debug("Trying to register Profile: {}", desc);
			return register(input, desc, userCredentials, userspace,
					new NewAction());
		} catch (UserUnauthorizedException ex) {
			return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage())
					.build();
		}
	}

	@Override
	@POST
	@Path("/components")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	@Consumes("multipart/form-data")
	public Response registerComponent(
			@FormDataParam(DATA_FORM_FIELD) InputStream input,
			@FormDataParam(NAME_FORM_FIELD) String name,
			@FormDataParam(DESCRIPTION_FORM_FIELD) String description,
			@FormDataParam(GROUP_FORM_FIELD) String group,
			@FormDataParam(DOMAIN_FORM_FIELD) String domainName,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
		try {
			Principal principal = checkAndGetUserPrincipal();
			UserCredentials userCredentials = getUserCredentials(principal);
			ComponentDescription desc = createNewComponentDescription();
			desc.setCreatorName(userCredentials.getDisplayName());
			desc.setUserId(userCredentials.getPrincipalName()); // Hash used to
																// be created
																// here, now Id
																// is
																// constructed
																// by impl
			desc.setName(name);
			desc.setDescription(description);
			desc.setGroupName(group);
			desc.setDomainName(domainName);
			LOG.debug("Trying to register Component: {}", desc);
			return register(input, desc, userCredentials, userspace,
					new NewAction());
		} catch (UserUnauthorizedException ex) {
			return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage())
					.build();
		}
	}

	@Override
	@POST
	@Path("/components/{componentId}/comments")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	@Consumes("multipart/form-data")
	public Response registerCommentInComponent(
			@FormDataParam(DATA_FORM_FIELD) InputStream input,
			@PathParam("componentId") String componentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace)
			throws ComponentRegistryException {
		try {
			Principal principal = checkAndGetUserPrincipal();
			UserCredentials userCredentials = getUserCredentials(principal);
			if (null == componentRegistryFactory
					.getOrCreateUser(userCredentials)) {
				throw new UserUnauthorizedException(
						"Cannot materialize authenticated user");
			}
			// TODO: Add user/group param
			ComponentRegistry registry = getRegistry(getStatus(userspace),
					null, userCredentials);
			ComponentDescription description = registry
					.getComponentDescription(componentId);
			if (description != null) {
				LOG.debug("Trying to register comment to {}", componentId);
				return registerComment(input, registry, userspace, description,
						principal, userCredentials);
			} else {
				LOG.warn(
						"Attempt to post comment on nonexistent component id {} failed.",
						componentId);
				return Response
						.serverError()
						.entity("Invalid id, cannot comment on nonexistent component")
						.build();
			}
		} catch (UserUnauthorizedException ex) {
			return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage())
					.build();
		}
	}

	@Override
	@POST
	@Path("/profiles/{profileId}/comments")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	@Consumes("multipart/form-data")
	public Response registerCommentInProfile(
			@FormDataParam(DATA_FORM_FIELD) InputStream input,
			@PathParam("profileId") String profileId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace)
			throws ComponentRegistryException {
		try {
			Principal principal = checkAndGetUserPrincipal();
			UserCredentials userCredentials = getUserCredentials(principal);
			if (null == componentRegistryFactory
					.getOrCreateUser(userCredentials)) {
				throw new UserUnauthorizedException(
						"Cannot materialize authenticated user");
			}
			// TODO: Add user/group param
			ComponentRegistry registry = getRegistry(getStatus(userspace),
					null, userCredentials);
			ProfileDescription description = registry
					.getProfileDescription(profileId);
			if (description != null) {
				LOG.debug("Trying to register comment to {}", profileId);
				return registerComment(input, registry, userspace, description,
						principal, userCredentials);
			} else {
				LOG.warn(
						"Attempt to post comment on nonexistent profile id {} failed.",
						profileId);
				return Response
						.serverError()
						.entity("Invalid id, cannot comment on nonexistent profile")
						.build();
			}
		} catch (UserUnauthorizedException ex) {
			return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage())
					.build();
		}
	}

	@Override
	@GET
	@Path("/pingSession")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Response pingSession() {
		boolean stillActive = false;
		Principal userPrincipal = security.getUserPrincipal();
		if (LOG.isInfoEnabled()) {
			LOG.debug("ping by <{}>",
					(userPrincipal == null ? "unauthorized user"
							: userPrincipal.getName()));
		}
		if (request != null) {
			if (userPrincipal != null
					&& !ComponentRegistryFactory.ANONYMOUS_USER
							.equals(userPrincipal.getName())) {
				stillActive = !((HttpServletRequest) request).getSession()
						.isNew();
			}
		}
		return Response
				.ok()
				.entity(String.format("<session stillActive=\"%s\"/>",
						stillActive)).build();
	}

	private Response register(InputStream input, BaseDescription desc,
			UserCredentials userCredentials, boolean userspace,
			RegisterAction action) {
		try {
			// TODO: Add user/group param
			ComponentRegistry registry = getRegistry(getStatus(userspace),
					null, userCredentials);
			DescriptionValidator descriptionValidator = new DescriptionValidator(
					desc);
			MDValidator validator = new MDValidator(input, desc, registry,
					getRegistry(getStatus(true)),
					componentRegistryFactory.getPublicRegistry(), marshaller);
			RegisterResponse response = new RegisterResponse();
			response.setIsInUserSpace(userspace);
			validate(response, descriptionValidator, validator);
			if (response.getErrors().isEmpty()) {

				CMDComponentSpec spec = validator.getCMDComponentSpec();

				// removing filename from spec before it gets extended.
				// recursion over all the components
				setFileNamesFromListToNull(spec.getCMDComponent());

				try {
					checkForRecursion(validator, registry, desc);

					// Add profile
					int returnCode = action.execute(desc, spec, response,
							registry);
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
					response.addError("Error while expanding specification. "
							+ ex.getMessage());
				}
			} else {
				LOG.warn("Registration failed with validation errors: {}",
						Arrays.toString(response.getErrors().toArray()));
				response.setRegistered(false);
			}
			LOG.info("Registered new {} {}", desc.isProfile() ? "profile"
					: "component", desc);
			response.setIsProfile(desc.isProfile());
			return Response.ok(response).build();
		} finally {
			try {
				input.close();// either we read the input or there was an
								// exception, we need to close it.
			} catch (IOException e) {
				LOG.error("Error when closing inputstream: ", e);
			}
		}
	}

	/**
	 * 
	 * @param validator
	 * @param registry
	 * @param desc
	 * @throws ComponentRegistryException
	 *             if recursion is detected or something goes wrong while trying
	 *             to detect recursion
	 */
	private void checkForRecursion(MDValidator validator,
			ComponentRegistry registry, BaseDescription desc)
			throws ComponentRegistryException {
		try {
			// Expand to check for recursion. Operate on copy so that original
			// does not get expanded.
			final CMDComponentSpec specCopy = validator
					.getCopyOfCMDComponentSpec();
			// In case of recursion, the following will throw a
			// ComponentRegistryException
			registry.getExpander().expandNestedComponent(
					specCopy.getCMDComponent(), desc.getId());
		} catch (JAXBException ex) {
			throw new ComponentRegistryException(
					"Unmarshalling failed while preparing recursion detection",
					ex);
		}
	}

	private Response registerComment(InputStream input,
			ComponentRegistry registry, boolean userspace,
			BaseDescription description, Principal principal,
			UserCredentials userCredentials) {
		try {
			CommentValidator validator = new CommentValidator(input,
					description, marshaller);
			CommentResponse response = new CommentResponse();
			response.setIsInUserSpace(userspace);
			validateComment(response, validator);
			if (response.getErrors().isEmpty()) {
				Comment com = validator.getCommentSpec();
				// int returnCode = action.executeComment(com, response,
				// registry, principal.getName());

				// If user name is left empty, fill it using the user's display
				// name
				if (null == com.getUserName() || "".equals(com.getUserName())) {
					if (userCredentials != null) {
						com.setUserName(userCredentials.getDisplayName());
					} else {
						com.setUserName(principal.getName());
					}
				}

				int returnCode = registry.registerComment(com,
						principal.getName());
				if (returnCode == 0) {
					response.setRegistered(true);
					response.setComment(com);
				} else {
					response.setRegistered(false);
					response.addError("Unable to post at this moment. Internal server error.");
				}
				if (com.getComponentId() != null) {
					LOG.info("Posted new comment on component {}",
							com.getComponentId());
				} else {
					LOG.info("Posted new comment on profile {}",
							com.getComponentId());
				}
			} else {
				LOG.warn(
						"Posting of comment failed with validation errors: {}",
						Arrays.toString(response.getErrors().toArray()));
				response.setRegistered(false);
			}
			return Response.ok(response).build();
		} catch (ComponentRegistryException ex) {
			LOG.error("Error while inserting comment: ", ex);
			return Response.serverError().entity(ex.getMessage()).build();
		} finally {
			try {
				input.close();// either we read the input or there was an
								// exception, we need to close it.
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
	 * @return The application's base URI as configured in the
	 *         {@link #APPLICATION_BASE_URL_PARAM} context parameter. If
	 *         correctly configured, it should look something like
	 *         "http://catalog.clarin.eu/ds/ComponentRegistry".
	 *         <em>Be aware that this
	 * can also be null if configured incorrectly!</em>
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

	private void validateComment(CommentResponse response,
			Validator... validators) {
		for (Validator validator : validators) {
			if (!validator.validate()) {
				for (String error : validator.getErrorMessages()) {
					response.addError(error);
				}
			}
		}
	}

	/**
	 * @param componentRegistryFactory
	 *            the componentRegistryFactory to set
	 */
	@Override
	public void setComponentRegistryFactory(
			ComponentRegistryFactory componentRegistryFactory) {
		this.componentRegistryFactory = componentRegistryFactory;
	}

	/**
	 * 
	 * @param listofcomponents
	 *            a list of components whose file-names and whose childrens'
	 *            filenames are to be set to null
	 */
	@Override
	public void setFileNamesFromListToNull(
			List<CMDComponentType> listofcomponents) {

		for (CMDComponentType currentcomponent : listofcomponents) {
			setFileNamesToNullCurrent(currentcomponent);
		}

	}

	/**
	 * 
	 * @param currentcomponent
	 *            a component whose file-name and whose children filenames are
	 *            to be set to null
	 */
	protected void setFileNamesToNullCurrent(CMDComponentType currentcomponent) {
		currentcomponent.setFilename(null);
		setFileNamesFromListToNull(currentcomponent.getCMDComponent());
	}

	/**
	 * 
	 * @param userspace
	 *            if "true" then profiles and components from the user's
	 *            workspace, otherwise -- public
	 * @param limit
	 *            the number of items to be displayed
	 * @return rss for the components in the database to which we are currently
	 *         connected
	 * @throws ComponentRegistryException
	 * @throws ParseException
	 */
	@Override
	@GET
	@Path("/components/rss")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Rss getRssComponent(
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace,
			@QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
			throws ComponentRegistryException, ParseException {
		final List<ComponentDescription> components = getRegistry(
				getStatus(userspace)).getComponentDescriptions();
		final RssCreatorDescriptions instance = new RssCreatorDescriptions(
				userspace, getApplicationBaseURI(), "components",
				Integer.parseInt(limit), components,
				ComponentUtils.COMPARE_ON_DATE);
		final Rss rss = instance.getRss();
		LOG.debug("Releasing RSS of {} most recently registered components",
				limit);
		return rss;
	}

	/**
	 * 
	 * @param userspace
	 *            if "true" then profiles and components from the user's
	 *            workspace, otherwise -- public
	 * @param limit
	 *            the number of items to be displayed
	 * @return rss for the profiles in the database to which we are currently
	 *         connected
	 * @throws ComponentRegistryException
	 * @throws ParseException
	 */
	@Override
	@GET
	@Path("/profiles/rss")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Rss getRssProfile(
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace,
			@QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
			throws ComponentRegistryException, ParseException {
		final List<ProfileDescription> profiles = getRegistry(
				getStatus(userspace)).getProfileDescriptions();
		final RssCreatorDescriptions instance = new RssCreatorDescriptions(
				userspace, getApplicationBaseURI(), "profiles",
				Integer.parseInt(limit), profiles,
				ComponentUtils.COMPARE_ON_DATE);
		final Rss rss = instance.getRss();
		LOG.debug("Releasing RSS of {} most recently registered profiles",
				limit);
		return rss;
	}

	/**
	 * 
	 * @param profileId
	 *            the Id of a profile whose comments are to be rss-ed
	 * @param userspace
	 *            if "true" then profiles and components from the user's
	 *            workspace, otherwise -- public
	 * @param limit
	 *            the number of items to be displayed
	 * @return rss of the comments for a chosen profile
	 * @throws ComponentRegistryException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws ParseException
	 */
	@Override
	@GET
	@Path("/profiles/{profileId}/comments/rss")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Rss getRssOfCommentsFromProfile(
			@PathParam("profileId") String profileId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace,
			@QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
			throws ComponentRegistryException, IOException, JAXBException,
			ParseException {
		final Principal principal = security.getUserPrincipal();
		final List<Comment> comments = getRegistry(getStatus(userspace))
				.getCommentsInProfile(profileId, principal);
		final String profileName = getRegistry(getStatus(userspace))
				.getProfileDescription(profileId).getName();
		final RssCreatorComments instance = new RssCreatorComments(userspace,
				getApplicationBaseURI(), Integer.parseInt(limit), profileId,
				profileName, "profile", comments, Comment.COMPARE_ON_DATE);
		final Rss rss = instance.getRss();
		LOG.debug("Releasing RSS of {} most recent post on profile {}", limit,
				profileId);
		return rss;
	}

	/**
	 * 
	 * @param componentId
	 *            the Id of a component whose comments are to be rss-ed
	 * @param userspace
	 *            if "true" then profiles and components from the user's
	 *            workspace, otherwise -- public
	 * @param limit
	 *            the number of items to be displayed
	 * @return rss of the comments for a chosen component
	 * @throws ComponentRegistryException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws ParseException
	 */
	@Override
	@GET
	@Path("/components/{componentId}/comments/rss")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public Rss getRssOfCommentsFromComponent(
			@PathParam("componentId") String componentId,
			@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace,
			@QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
			throws ComponentRegistryException, IOException, JAXBException,
			ParseException {
		final Principal principal = security.getUserPrincipal();
		final List<Comment> comments = getRegistry(getStatus(userspace))
				.getCommentsInComponent(componentId, principal);
		final String componentName = getRegistry(getStatus(userspace))
				.getComponentDescription(componentId).getName();
		final RssCreatorComments instance = new RssCreatorComments(userspace,
				getApplicationBaseURI(), Integer.parseInt(limit), componentId,
				componentName, "component", comments, Comment.COMPARE_ON_DATE);
		final Rss rss = instance.getRss();
		LOG.debug("Releasing RSS of {} most recent post on component {}",
				limit, componentId);
		return rss;
	}

	@Override
	@GET
	@Path("/AllowedTypes")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public AllowedAttributetypesXML getAllowedAttributeTypes()
			throws ComponentRegistryException, IOException, JAXBException,
			ParseException {
		return (new AllowedAttributetypesXML());
	}

	@Override
	@GET
	@Path("/groups/usermembership")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public List<Group> getGroupsTheCurrentUserIsAMemberOf() {
	    Principal principal = security.getUserPrincipal();
	    if (principal == null)
		return new ArrayList<Group>();
	    List<Group> groups = groupService.getGroupsOfWhichUserIsAMember(principal.getName());
	    return groups;
	}

	@Override
	@GET
	@Path("/items/{itemId}/groups")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public List<Group> getGroupsTheItemIsAMemberOf(@PathParam("itemId") String itemId) {
	    return groupService.getGroupsTheItemIsAMemberOf(itemId);
	}
	
	@Override
	@POST
	@Path("/items/{itemId}/transferownership")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public void transferItemOwnershipToGroup(@PathParam("itemId") String itemId, @QueryParam("groupId") long groupId) {
	    Principal principal = security.getUserPrincipal();
	    groupService.transferItemOwnershipFromUserToGroupId(principal.getName(), groupId, itemId);
	}

	@Override
	@GET
	@Path("/items/{itemId}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML,
			MediaType.APPLICATION_JSON })
	public BaseDescription getComponentDescription(@PathParam("itemId") String itemId) throws ComponentRegistryException{
			LOG.debug("Item with id: {} is requested.", itemId);
			BaseDescription description = getRegistry(getStatus(false)).getComponentDescription(itemId);
			if (description == null)
				description = getRegistry(getStatus(true)).getComponentDescription(itemId);
			if (description == null)
				description = getRegistry(getStatus(false)).getProfileDescription(itemId);
			if (description == null)
				description = getRegistry(getStatus(true)).getProfileDescription(itemId);
			return description;
	}

}
