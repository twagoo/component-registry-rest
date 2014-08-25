package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AllowedAttributetypesXML;
import clarin.cmdi.componentregistry.AuthenticationFailException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.RegistrySpace;
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
import com.google.common.collect.Lists;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles CRUD operations on
 * {@link ComponentDescription}, {@link ProfileDescription} and {@link Comment}s
 *
 * @author twago@mpi.nl
 * @author olsha@mpi.nl
 * @author george.georgovassilis@mpi.nl
 *
 */
@Path("/registry")
@Service
@Transactional(rollbackFor = {Exception.class, ValidationException.class})
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
    private HttpServletResponse response;
    @Context
    private ServletContext servletContext;
    @InjectParam(value = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;
    @InjectParam(value = "mdMarshaller")
    private MDMarshaller marshaller;
    @Autowired
    private GroupService groupService;

    private ComponentRegistry getBaseRegistry() throws AuthenticationFailException {
        Principal userPrincipal = this.checkAndGetUserPrincipal();
        UserCredentials userCredentials = this.getUserCredentials(userPrincipal);
        return componentRegistryFactory.getBaseRegistry(userCredentials);
    }

    private ComponentRegistry getRegistry(RegistrySpace space, Number groupId) {
        Principal userPrincipal = security.getUserPrincipal();
        UserCredentials userCredentials = this.getUserCredentials(userPrincipal);
        try {
            return componentRegistryFactory.getComponentRegistry(space, null, userCredentials, groupId);
        } catch (UserUnauthorizedException uuEx) {
            LOG.warn("Unauthorized access to {} registry by user {}", space,
                    userCredentials);
            LOG.debug("Details for unauthorized access", uuEx);
            throw new WebApplicationException(uuEx, Status.FORBIDDEN);
        }
    }

    /**
     *
     * @return Principal of current request
     * @throws IllegalArgumentException If no user principal found
     */
    private Principal checkAndGetUserPrincipal()
            throws AuthenticationFailException {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new AuthenticationFailException("No user principal found.");
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

    private ComponentRegistry initialiseRegistry(String space, String groupId) throws AuthenticationFailException {
        //checking credentials 
        RegistrySpace regSpace = RegistrySpace.valueOf(space.toUpperCase());
        UserCredentials user = this.getUserCredentials(this.checkAndGetUserPrincipal());
        // initializing the registry
        Number groupIdNumber = null;
        if (groupId != null && !groupId.isEmpty()) {
            groupIdNumber = Integer.parseInt(groupId);
        }

        return this.getRegistry(regSpace, groupIdNumber);
    }

    private boolean checkRegistrySpaceString(String registrySpace) {
        return (registrySpace.equalsIgnoreCase("group") || registrySpace.equalsIgnoreCase("private") || registrySpace.equalsIgnoreCase("published"));
    }

    @Override
    @GET
    @Path("/components")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public List<ComponentDescription> getRegisteredComponents(
            @QueryParam(REGISTRY_SPACE_PARAM) @DefaultValue("published") String registrySpace,
            @QueryParam(GROUPID_PARAM) String groupId)
            throws ComponentRegistryException, IOException {
        long start = System.currentTimeMillis();

        if (!checkRegistrySpaceString(registrySpace)) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), "illegal registry space");
            return new ArrayList<ComponentDescription>();
        }

        try {
            ComponentRegistry cr = this.initialiseRegistry(registrySpace, groupId);
            List<ComponentDescription> result = cr.getComponentDescriptions();
            LOG.debug(
                    "Releasing {} registered components into the world ({} millisecs)",
                    result.size(), (System.currentTimeMillis() - start));

            return result;
        } catch (AuthenticationFailException e) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e.toString());
            return new ArrayList<ComponentDescription>();

        } catch (UserUnauthorizedException e) {
            response.sendError(Status.FORBIDDEN.getStatusCode(), e.toString());
            return new ArrayList<ComponentDescription>();

        
        
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), e.toString());
            return new ArrayList<ComponentDescription>();
        }

    }

    @Override
    @GET
    @Path("/profiles")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public List<ProfileDescription> getRegisteredProfiles(
            @QueryParam(REGISTRY_SPACE_PARAM) @DefaultValue("published") String registrySpace,
            @QueryParam(METADATA_EDITOR_PARAM) @DefaultValue("false") boolean metadataEditor,
            @QueryParam(GROUPID_PARAM) String groupId)
            throws ComponentRegistryException, IOException {

        long start = System.currentTimeMillis();

        if (!checkRegistrySpaceString(registrySpace)) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), "illegal registry space");
            return new ArrayList<ProfileDescription>();
        }
        try {
            ComponentRegistry cr = this.initialiseRegistry(registrySpace, groupId);
            List<ProfileDescription> result = (metadataEditor) ? cr.getProfileDescriptionsForMetadaEditor() : cr.getProfileDescriptions();
            LOG.debug(
                    "Releasing {} registered components into the world ({} millisecs)",
                    result.size(), (System.currentTimeMillis() - start));

            return result;
        } catch (AuthenticationFailException e) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e.toString());
            return new ArrayList<ProfileDescription>();

        } catch (UserUnauthorizedException e) {
            response.sendError(Status.FORBIDDEN.getStatusCode(), e.toString());
            return new ArrayList<ProfileDescription>();
        
        
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), e.toString());
            return new ArrayList<ProfileDescription>();
        }
    }

    @Override
    @GET
    @Path("/components/{componentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response getRegisteredComponent(
            @PathParam("componentId") String componentId) throws IOException {
        LOG.debug("Component with id: {} is requested.", componentId);
        try {
            CMDComponentSpec mdComponent = this.getBaseRegistry().getMDComponentAccessControlled(componentId);
            return Response.ok(mdComponent).build();
        } catch (ItemNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (ComponentRegistryException e1) {
            return Response.serverError().status(Status.CONFLICT).build();
        } catch (AuthenticationFailException e) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        } catch (UserUnauthorizedException e) {
            return Response.serverError().status(Status.FORBIDDEN).build();
        }
    }

    @Override
    @GET
    @Path("/profiles/{profileId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response getRegisteredProfile(
            @PathParam("profileId") String profileId) throws IOException {
        LOG.debug("Profile with id {} is requested.", profileId);
        try {
            CMDComponentSpec mdProfile = this.getBaseRegistry().getMDProfileAccessControled(profileId);
            return Response.ok(mdProfile).build();
        } catch (ItemNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (ComponentRegistryException e1) {
            return Response.serverError().status(Status.CONFLICT).build();
        } catch (UserUnauthorizedException e) {
            return Response.serverError().status(Status.FORBIDDEN).build();
        } catch (AuthenticationFailException e) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }
    }

    @Override
    @GET
    @Path("/components/{componentId}/{rawType}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public Response getRegisteredComponentRawType(
            @PathParam("componentId") final String componentId, @PathParam("rawType") String rawType) throws ComponentRegistryException {

        LOG.debug("Component with id: {} and rawType: {} is requested.", componentId, rawType);
        try {
            final ComponentRegistry registry = this.getBaseRegistry();
            try {
                ComponentDescription desc = registry.getComponentDescriptionAccessControlled(componentId);
                StreamingOutput result = null;
                String fileName = desc.getName() + "." + rawType;
                if ("xml".equalsIgnoreCase(rawType)) {
                    result = new StreamingOutput() {
                        @Override
                        public void write(OutputStream output) throws IOException,
                                WebApplicationException {
                            try {
                                try {
                                    try {
                                        registry.getMDComponentAsXml(componentId, output);
                                    } catch (ItemNotFoundException e1) {
                                        LOG.warn("Could not retrieve component {}",
                                                componentId);
                                        LOG.debug("Details", e1);
                                        throw new WebApplicationException(e1, Response
                                                .serverError()
                                                .status(Status.INTERNAL_SERVER_ERROR)
                                                .build());
                                    }
                                } catch (ComponentRegistryException e) {
                                    LOG.warn("Could not retrieve component {}",
                                            componentId);
                                    LOG.debug("Details", e);
                                    throw new WebApplicationException(e, Response
                                            .serverError()
                                            .status(Status.INTERNAL_SERVER_ERROR)
                                            .build());
                                }

                            } catch (UserUnauthorizedException e2) {
                                LOG.error(e2.toString());
                            }
                        }
                    };
                    return createDownloadResponse(result, fileName);
                } else if ("xsd".equalsIgnoreCase(rawType)) {
                    result = new StreamingOutput() {
                        @Override
                        public void write(OutputStream output) throws IOException,
                                WebApplicationException {
                            try {
                                try {
                                    try {
                                        registry.getMDComponentAsXsd(componentId, output);
                                    } catch (ItemNotFoundException e1) {
                                        LOG.warn("Could not retrieve component {}",
                                                componentId);
                                        LOG.debug("Details", e1);
                                        throw new WebApplicationException(e1, Response
                                                .serverError()
                                                .status(Status.INTERNAL_SERVER_ERROR)
                                                .build());
                                    }
                                } catch (ComponentRegistryException e) {
                                    LOG.warn("Could not retrieve component {}",
                                            componentId);
                                    LOG.debug("Details", e);
                                    throw new WebApplicationException(e, Response
                                            .serverError()
                                            .status(Status.INTERNAL_SERVER_ERROR)
                                            .build());
                                }
                            } catch (UserUnauthorizedException e2) {
                                LOG.error(e2.toString());
                            }

                        }
                    };
                    return createDownloadResponse(result, fileName);
                } else {
                    return Response.status(Status.NOT_FOUND).entity("Usupported raw type " + rawType).build();
                }


            } catch (UserUnauthorizedException e2) {
                return Response.status(Status.FORBIDDEN).build();
            }
        } catch (ItemNotFoundException e3) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (AuthenticationFailException e) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }
    }

    @Override
    @GET
    @Path("/components/usage/{componentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public List<BaseDescription> getComponentUsage(
            @PathParam("componentId") String componentId) throws ComponentRegistryException, IOException {

        final long start = System.currentTimeMillis();
        try {
            ComponentRegistry registry = this.getBaseRegistry();
            List<ComponentDescription> components = registry.getUsageInComponents(componentId);
            List<ProfileDescription> profiles = registry.getUsageInProfiles(componentId);

            LOG.debug(
                    "Found {} components and {} profiles that use component {} ({} millisecs)",
                    components.size(), profiles.size(), componentId,
                    (System.currentTimeMillis() - start));

            List<BaseDescription> usages = new ArrayList<BaseDescription>(components.size() + profiles.size());
            usages.addAll(components);
            usages.addAll(profiles);

            return usages;
        } catch (ComponentRegistryException e) {
            LOG.warn("Could not retrieve profile usage {}", componentId);
            LOG.debug("Details", e);
            return new ArrayList<BaseDescription>();
        } catch (AuthenticationFailException e1) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode());
            return new ArrayList<BaseDescription>();
        }
    }

    @Override
    @GET
    @Path("/profiles/{profileId}/comments")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public List<Comment> getCommentsFromProfile(
            @PathParam("profileId") String profileId)
            throws IOException {
        long start = System.currentTimeMillis();
        try {
            List<Comment> comments = this.getBaseRegistry().getCommentsInProfile(profileId);
            LOG.debug(
                    "Releasing {} registered comments in profile into the world ({} millisecs)",
                    comments.size(), (System.currentTimeMillis() - start));
            return comments;
        } catch (ComponentRegistryException e) {
            response.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            return new ArrayList<Comment>();
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), e.getMessage());
            return new ArrayList<Comment>();
        } catch (UserUnauthorizedException e) {
            response.sendError(Status.FORBIDDEN.getStatusCode(), e.getMessage());
            return new ArrayList<Comment>();
        } catch (AuthenticationFailException e1) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e1.getMessage());
            return new ArrayList<Comment>();
        }
    }

    @Override
    @GET
    @Path("/components/{componentId}/comments")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public List<Comment> getCommentsFromComponent(
            @PathParam("componentId") String componentId)
            throws IOException {
        long start = System.currentTimeMillis();
        try {
            List<Comment> comments = this.getBaseRegistry().getCommentsInComponent(componentId);
            LOG.debug(
                    "Releasing {} registered comments in Component into the world ({} millisecs)",
                    comments.size(), (System.currentTimeMillis() - start));
            return comments;
        } catch (ComponentRegistryException e) {
            response.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            return new ArrayList<Comment>();
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), e.getMessage());
            return new ArrayList<Comment>();
        } catch (UserUnauthorizedException e1) {
            response.sendError(Status.FORBIDDEN.getStatusCode(), e1.getMessage());
            return new ArrayList<Comment>();
        } catch (AuthenticationFailException e1) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e1.getMessage());
            return new ArrayList<Comment>();
        }
    }

    @Override
    @GET
    @Path("/profiles/{profileId}/comments/{commentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Comment getSpecifiedCommentFromProfile(
            @PathParam("profileId") String profileId,
            @PathParam("commentId") String commentId)
            throws IOException {

        LOG.debug("Comments of profile with id {} are requested.", commentId);
        try {

            return this.getBaseRegistry().getSpecifiedCommentInProfile(profileId, commentId);
        } catch (ComponentRegistryException e) {
            response.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            return new Comment();
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), e.getMessage());
            return new Comment();
        } catch (UserUnauthorizedException e1) {
            response.sendError(Status.FORBIDDEN.getStatusCode(), e1.getMessage());
            return new Comment();
        } catch (AuthenticationFailException e1) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e1.getMessage());
            return new Comment();
        }
    }

    @Override
    @GET
    @Path("/components/{componentId}/comments/{commentId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Comment getSpecifiedCommentFromComponent(
            @PathParam("componentId") String componentId,
            @PathParam("commentId") String commentId)
            throws IOException {
        LOG.debug("Comments of component with id {} are requested.", commentId);
        try {
            Comment result = this.getBaseRegistry().getSpecifiedCommentInComponent(componentId, commentId);
            return result;
        } catch (ComponentRegistryException e) {
            response.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            return new Comment();
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), e.getMessage());
            return new Comment();
        } catch (UserUnauthorizedException e1) {
            response.sendError(Status.FORBIDDEN.getStatusCode(), e1.getMessage());
            return new Comment();
        } catch (AuthenticationFailException e1) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e1.getMessage());
            return new Comment();
        }
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
    // TODO: test via POSTMAN
    @Override
    @POST
    @Path("/profiles/{profileId}")
    public Response manipulateRegisteredProfile(
            @PathParam("profileId") String profileId,
            @FormParam("method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            return this.deleteRegisteredProfile(profileId);
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
            @FormParam("method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            return this.deleteCommentFromProfile(profileId, commentId);
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
            @FormParam("method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            return this.deleteCommentFromComponent(componentId, commentId);
        } else {
            return Response.ok().build();
        }
    }

    // TODO: test via POSTMAN
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
            ComponentRegistry registry = this.getBaseRegistry();
            ProfileDescription desc = registry.getProfileDescriptionAccessControlled(profileId);
            if (desc != null) {
                if (desc.isPublic()) {
                    return Response.status(Status.CONFLICT).entity("Cannot publish already published profile.")
                            .build();
                }
                desc.setPublic(true);
                this.updateDescription(desc, name, description, domainName, group);
                return this.register(input, desc, new PublishAction(principal), registry);
            } else {
                LOG.error("Update of nonexistent profile {} failed.", profileId);
                return Response
                        .serverError()
                        .entity("Invalid id, cannot update nonexistent profile")
                        .build();
            }

        } catch (AuthenticationFailException e) {
            return Response.serverError().status(Status.UNAUTHORIZED)
                    .build();
        } catch (ItemNotFoundException e1) {
            return Response.status(Status.NOT_FOUND).entity(e1.getMessage()).build();
        } catch (ComponentRegistryException e) {
            LOG.warn("Could not retrieve profile {}", profileId);
            LOG.debug("Details", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (UserUnauthorizedException ex) {
            return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                    .build();
        }
    }

    @Override
    @POST
    @Path("/profiles/{profileId}/update")
    @Consumes("multipart/form-data")
    public Response updateRegisteredProfile(
            @PathParam("profileId") String profileId,
            @FormDataParam(DATA_FORM_FIELD) InputStream input,
            @FormDataParam(NAME_FORM_FIELD) String name,
            @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
            @FormDataParam(GROUP_FORM_FIELD) String group,
            @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
        try {
            ComponentRegistry br = this.getBaseRegistry();
            ProfileDescription desc = br.getProfileDescriptionAccessControlled(profileId);
            if (desc != null) {
                if (desc.isPublic()) {
                    return Response.status(Status.CONFLICT).entity("Cannot update already published profile.")
                            .build();

                }
                Number groupId;
                RegistrySpace space;
                List<Number> groupIds = br.getItemGroups(profileId);
                if (groupIds == null || groupIds.isEmpty()) {
                    groupId = null;
                    space = RegistrySpace.PRIVATE;
                } else {
                    groupId = groupIds.get(0);
                    space = RegistrySpace.GROUP;
                }

                updateDescription(desc, name, description, domainName, group);
                ComponentRegistry cr = this.getRegistry(space, groupId);
                return register(input, desc, new UpdateAction(), cr);
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
            return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                    .build();

        } catch (ItemNotFoundException ex2) {
            return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                    .build();
        } catch (AuthenticationFailException e1) {
            return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
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
    // twan: why do we need it?
    // TODO: test via POSTMAN
    @Override
    @POST
    @Path("/components/{componentId}")
    public Response manipulateRegisteredComponent(
            @PathParam("componentId") String componentId,
            @FormParam("method") String method) {
        if ("delete".equalsIgnoreCase(method)) {
            return this.deleteRegisteredComponent(componentId);
        } else {
            return Response.ok("Nothing to do, not 'delete' method").build();
        }
    }

    // TODO: test via POSTMAN
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
            ComponentRegistry registry = this.getBaseRegistry();
            ComponentDescription desc = registry.getComponentDescriptionAccessControlled(componentId);
            if (desc != null) {
                if (desc.isPublic()) {
                    return Response.status(Status.CONFLICT).entity("Cannot publish already published omponent.")
                            .build();
                }
                desc.setPublic(true);
                this.updateDescription(desc, name, description, domainName, group);
                return this.register(input, desc, new PublishAction(principal), registry);
            } else {
                LOG.error("Update of nonexistent id (" + componentId
                        + ") failed.");
                return Response
                        .serverError()
                        .entity("Invalid id, cannot update nonexistent profile")
                        .build();
            }
        } catch (AuthenticationFailException e) {
            LOG.warn("Could not retrieve component {}", componentId);
            LOG.debug("Details", e);
            return Response.serverError().status(Status.UNAUTHORIZED)
                    .build();
        } catch (ComponentRegistryException e) {
            LOG.warn("Could not retrieve component {}", componentId);
            LOG.debug("Details", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (ItemNotFoundException e) {
            LOG.warn("Could not retrieve component {}", componentId);
            LOG.debug("Details", e);
            return Response.serverError().status(Status.NOT_FOUND)
                    .build();
        } catch (UserUnauthorizedException ex) {
            return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                    .build();
        }
    }

    @Override
    @POST
    @Path("/components/{componentId}/update")
    @Consumes("multipart/form-data")
    public Response updateRegisteredComponent(
            @PathParam("componentId") String componentId,
            @FormDataParam(DATA_FORM_FIELD) InputStream input,
            @FormDataParam(NAME_FORM_FIELD) String name,
            @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
            @FormDataParam(GROUP_FORM_FIELD) String group,
            @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
        try {
            ComponentRegistry br = this.getBaseRegistry();
            ComponentDescription desc = br.getComponentDescriptionAccessControlled(componentId);
            if (desc != null) {
                if (desc.isPublic()) {
                    return Response.status(Status.CONFLICT).entity("Cannot update already published component.")
                            .build();
                }
                Number groupId;
                RegistrySpace space;
                List<Number> groupIds = br.getItemGroups(componentId);
                if (groupIds == null || groupIds.isEmpty()) {
                    groupId = null;
                    space = RegistrySpace.PRIVATE;
                } else {
                    groupId = groupIds.get(0);
                    space = RegistrySpace.GROUP;
                }

                this.updateDescription(desc, name, description, domainName, group);
                ComponentRegistry cr = this.getRegistry(space, groupId);
                return this.register(input, desc, new UpdateAction(), cr);
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
            return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                    .build();
        } catch (ItemNotFoundException ex2) {
            return Response.status(Status.FORBIDDEN).entity(ex2.getMessage())
                    .build();
        } catch (AuthenticationFailException e1) {
            return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
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
            @PathParam("componentId") String componentId) {
        try {
            ComponentRegistry registry = this.getBaseRegistry();
            LOG.debug("Component with id {} set for deletion.", componentId);
            registry.deleteMDComponent(componentId, false);
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
        } catch (ItemNotFoundException e) {
            LOG.warn("Component with id " + componentId + " is not found.");
            return Response.serverError().status(Status.NOT_FOUND)
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
            return Response.serverError().status(Status.FORBIDDEN)
                    .entity("" + e.getMessage()).build();
        } catch (AuthenticationFailException e1) {
            return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                    .build();
        }

        LOG.info("Component with id: {} deleted.", componentId);
        return Response.ok("Component with id" + componentId + " deleted.").build();
    }

    @Override
    @DELETE
    @Path("/profiles/{profileId}")
    public Response deleteRegisteredProfile(
            @PathParam("profileId") String profileId) {
        try {
            LOG.debug("Profile with id: {} set for deletion.", profileId);
            this.getBaseRegistry().deleteMDProfile(profileId);
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
        } catch (ItemNotFoundException e) {
            LOG.warn("Profile with id " + profileId + " is not found.");
            return Response.serverError().status(Status.NOT_FOUND)
                    .build();
        } catch (IOException e) {
            LOG.error("Profile with id: " + profileId + " deletion failed.", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (UserUnauthorizedException e) {
            LOG.info("Profile with id: {} deletion failed: {}", profileId,
                    e.getMessage());
            LOG.debug("Deletion failure details:", e);
            return Response.serverError().status(Status.FORBIDDEN)
                    .entity("" + e.getMessage()).build();
        } catch (AuthenticationFailException e1) {
            return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                    .build();
        }

        LOG.info("Profile with id: {} deleted.", profileId);
        return Response.ok().build();
    }

    @Override
    @DELETE
    @Path("/profiles/{profileId}/comments/{commentId}")
    public Response deleteCommentFromProfile(
            @PathParam("profileId") String profileId,
            @PathParam("commentId") String commentId) {
        try {
            final ComponentRegistry registry = this.getBaseRegistry();
            try {
                final Comment comment = registry.getSpecifiedCommentInProfile(profileId, commentId);
                if (comment != null
                        && profileId.equals(comment.getComponentId())) {
                    LOG.debug("Comment with id: {} set for deletion.", commentId);
                    registry.deleteComment(commentId);
                } else {
                    throw new ComponentRegistryException(
                            "Comment not found for specified profile");
                }
            } catch (ItemNotFoundException e1) {
                LOG.info("Comment with id: {} deletion failed: {}", commentId,
                        e1.getMessage());
                LOG.debug("Deletion failure details:", e1);
                return Response.serverError().status(Status.NOT_FOUND)
                        .entity("" + e1.getMessage()).build();
            }

        } catch (DeleteFailedException e) {
            LOG.info("Comment with id: {} deletion failed: {}", commentId,
                    e.getMessage());
            LOG.debug("Deletion failure details:", e);
            return Response.serverError().status(Status.FORBIDDEN)
                    .entity("" + e.getMessage()).build();
        } catch (ComponentRegistryException e) {
            LOG.info("Could not find comment " + commentId + " for " + profileId);
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
            return Response.serverError().status(Status.FORBIDDEN)
                    .entity("" + e.getMessage()).build();
        } catch (AuthenticationFailException e1) {
            return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                    .build();
        }

        LOG.info("Comment with id: {} deleted.", commentId);
        return Response.ok("Comment with id " + commentId + " deleted.").build();
    }

    @Override
    @DELETE
    @Path("/components/{componentId}/comments/{commentId}")
    public Response deleteCommentFromComponent(
            @PathParam("componentId") String componentId,
            @PathParam("commentId") String commentId) {
        try {
            final ComponentRegistry registry = this.getBaseRegistry();
            final Comment comment = registry.getSpecifiedCommentInComponent(componentId, commentId);
            if (comment != null
                    && componentId.equals(comment.getComponentId())) {
                LOG.debug("Comment with id: {} set for deletion.", commentId);
                registry.deleteComment(commentId);
            } else {
                throw new ComponentRegistryException(
                        "Comment not found for specified component");
            }
        } catch (ItemNotFoundException e) {
            LOG.info("Comment with id: {} deletion failed: {}", commentId,
                    e.getMessage());
            LOG.debug("Deletion failure details:", e);
            return Response.serverError().status(Status.NOT_FOUND)
                    .entity("" + e.getMessage()).build();
        } catch (DeleteFailedException e) {
            LOG.info("Comment with id: {} deletion failed: {}", commentId,
                    e.getMessage());
            LOG.debug("Deletion failure details:", e);
            return Response.serverError().status(Status.FORBIDDEN)
                    .entity("" + e.getMessage()).build();
        } catch (ComponentRegistryException e) {
            LOG.info("Could not retrieve component " + componentId + " for the component " + componentId);
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
            return Response.serverError().status(Status.FORBIDDEN)
                    .entity("" + e.getMessage()).build();
        } catch (AuthenticationFailException e1) {
            return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                    .build();
        }

        LOG.info("Comment with id: {} deleted.", commentId);
        return Response.ok("Comment with id " + commentId + " deleted.").build();
    }

    @Override
    @GET
    @Path("/profiles/{profileId}/{rawType}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public Response getRegisteredProfileRawType(
            @PathParam("profileId") final String profileId,
            @PathParam("rawType") String rawType) throws ComponentRegistryException, IllegalArgumentException {


        LOG.debug("Profile with id {} and rawType {} is requested.", profileId,
                rawType);
        try {
            final ComponentRegistry registry = this.getBaseRegistry();

            ProfileDescription desc = registry.getProfileDescriptionAccessControlled(profileId);
            if (desc == null) {
                return Response.status(Status.NOT_FOUND).build();
            }

            StreamingOutput result = null;
            String fileName = desc.getName() + "." + rawType;
            if ("xml".equalsIgnoreCase(rawType)) {
                result = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException,
                            WebApplicationException {
                        try {
                            registry.getMDProfileAsXml(profileId, output);
                        } catch (Exception e) {
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
                        } catch (Exception e) {
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
                return Response.status(Status.NOT_FOUND).entity("Unsupported raw type " + rawType).build();
            }
            return createDownloadResponse(result, fileName);
        } catch (UserUnauthorizedException ex) {
            return Response.status(Status.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return Response.serverError().status(Status.NOT_FOUND)
                    .entity("" + e.getMessage()).build();
        } catch (AuthenticationFailException e1) {
            return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                    .build();
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
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @Consumes("multipart/form-data")
    public Response registerProfile(
            @FormDataParam(DATA_FORM_FIELD) InputStream input,
            @FormDataParam(NAME_FORM_FIELD) String name,
            @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
            @FormDataParam(GROUP_FORM_FIELD) String group,
            @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
        try {
            Principal principal = checkAndGetUserPrincipal();
            UserCredentials userCredentials = getUserCredentials(principal);
            ProfileDescription desc = this.createNewProfileDescription();
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
            desc.setPublic(false);
            LOG.debug("Trying to register Profile: {}", desc);
            ComponentRegistry cr = this.getRegistry(RegistrySpace.PRIVATE, null);
            return this.register(input, desc, new NewAction(), cr);
        } catch (AuthenticationFailException e) {
            LOG.debug("Details", e);
            return Response.serverError().status(Status.UNAUTHORIZED)
                    .build();
        } catch (UserUnauthorizedException ex) {
            return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                    .build();
        }
    }

    @Override
    @POST
    @Path("/components")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @Consumes("multipart/form-data")
    public Response registerComponent(
            @FormDataParam(DATA_FORM_FIELD) InputStream input,
            @FormDataParam(NAME_FORM_FIELD) String name,
            @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
            @FormDataParam(GROUP_FORM_FIELD) String group,
            @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
        try {
            Principal principal = checkAndGetUserPrincipal();
            UserCredentials userCredentials = getUserCredentials(principal);
            ComponentDescription desc = this.createNewComponentDescription();
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
            desc.setPublic(false);
            LOG.debug("Trying to register Component: {}", desc);
            ComponentRegistry cr = this.getRegistry(RegistrySpace.PRIVATE, null);
            return this.register(input, desc, new NewAction(), cr);
        } catch (AuthenticationFailException e) {
            LOG.debug("Details", e);
            return Response.serverError().status(Status.UNAUTHORIZED)
                    .build();
        } catch (UserUnauthorizedException ex) {
            return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                    .build();
        }
    }

    @Override
    @POST
    @Path("/components/{componentId}/comments")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @Consumes("multipart/form-data")
    public Response registerCommentInComponent(
            @FormDataParam(DATA_FORM_FIELD) InputStream input,
            @PathParam("componentId") String componentId) throws ComponentRegistryException {
        try {
            ComponentRegistry registry = this.getBaseRegistry();
            ComponentDescription description = registry.getComponentDescriptionAccessControlled(componentId);

            LOG.debug("Trying to register comment to {}", componentId);

            return this.registerComment(input, description, registry);
        } catch (AuthenticationFailException e) {
            LOG.debug("Details", e);
            return Response.serverError().status(Status.UNAUTHORIZED)
                    .build();
        } catch (UserUnauthorizedException ex) {
            return Response.status(Status.FORBIDDEN)
                    .build();
        } catch (ItemNotFoundException e) {
            return Response.serverError().status(Status.NOT_FOUND)
                    .entity("" + e.getMessage()).build();
        }
    }

    // TODO test with the POSTMAN
    @Override
    @POST
    @Path("/profiles/{profileId}/comments")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @Consumes("multipart/form-data")
    public Response registerCommentInProfile(
            @FormDataParam(DATA_FORM_FIELD) InputStream input,
            @PathParam("profileId") String profileId)
            throws ComponentRegistryException {
        try {
            ComponentRegistry registry = this.getBaseRegistry();
            ProfileDescription description = registry
                    .getProfileDescriptionAccessControlled(profileId);

            LOG.debug("Trying to register comment to {}", profileId);

            return this.registerComment(input, description, registry);
        } catch (AuthenticationFailException e) {
            LOG.debug("Details", e);
            return Response.serverError().status(Status.UNAUTHORIZED)
                    .build();
        } catch (UserUnauthorizedException ex) {
            return Response.status(Status.FORBIDDEN)
                    .build();
        } catch (ItemNotFoundException e1) {
            return Response.serverError().status(Status.NOT_FOUND)
                    .entity("" + e1.getMessage()).build();
        }
    }

    @Override
    @GET
    @Path("/pingSession")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
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

    private Response register(InputStream input, BaseDescription desc, RegisterAction action, ComponentRegistry registry) throws UserUnauthorizedException {
        try {


            DescriptionValidator descriptionValidator = new DescriptionValidator(
                    desc);
            MDValidator validator = new MDValidator(input, desc, registry, marshaller);
            RegisterResponse response = new RegisterResponse();
            //obsolete. Make it setstatus
            response.setIsPrivate(!desc.isPublic());
            this.validate(response, descriptionValidator, validator);
            if (response.getErrors().isEmpty()) {

                CMDComponentSpec spec = validator.getCMDComponentSpec();

                // removing filename from spec before it gets extended.
                // recursion over all the components
                setFileNamesFromListToNull(Collections.singletonList(spec.getCMDComponent()));

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
                } catch (ItemNotFoundException ex) {
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
     * @throws ComponentRegistryException if recursion is detected or something
     * goes wrong while trying to detect recursion
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
                    Lists.newArrayList(specCopy.getCMDComponent()), desc.getId());
        } catch (JAXBException ex) {
            throw new ComponentRegistryException(
                    "Unmarshalling failed while preparing recursion detection",
                    ex);
        }
    }

    private Response registerComment(InputStream input, BaseDescription description, ComponentRegistry registry) throws UserUnauthorizedException, AuthenticationFailException {
        try {
            CommentValidator validator = new CommentValidator(input, description, marshaller);
            CommentResponse responseLocal = new CommentResponse();

            responseLocal.setIsPrivate(!description.isPublic());
            this.validateComment(responseLocal, validator);
            if (responseLocal.getErrors().isEmpty()) {
                Comment com = validator.getCommentSpec();
                // int returnCode = action.executeComment(com, response,
                // registry, principal.getName());

                // If user name is left empty, fill it using the user's display
                // name

                Principal principal = this.checkAndGetUserPrincipal();
                UserCredentials userCredentials = this.getUserCredentials(principal);
                if (null == com.getUserName() || "".equals(com.getUserName())) {
                    if (userCredentials != null) {
                        com.setUserName(userCredentials.getDisplayName());
                    } else {
                        com.setUserName(principal.getName());
                    }
                }
                try {
                    int returnCode = registry.registerComment(com,
                            principal.getName());
                    if (returnCode == 0) {
                        responseLocal.setRegistered(true);
                        responseLocal.setComment(com);
                    } else {
                        responseLocal.setRegistered(false);
                        responseLocal.addError("Unable to post at this moment. Internal server error.");
                    }
                    if (com.getComponentId() != null) {
                        LOG.info("Posted new comment on component {}",
                                com.getComponentId());
                    } else {
                        LOG.info("Posted new comment on profile {}",
                                com.getComponentId());
                    }
                } catch (ItemNotFoundException e) {
                    return Response.serverError().status(Status.NOT_FOUND)
                            .entity("" + e.getMessage()).build();
                }
            } else {
                LOG.warn(
                        "Posting of comment failed with validation errors: {}",
                        Arrays.toString(responseLocal.getErrors().toArray()));
                responseLocal.setRegistered(false);
            }
            return Response.ok(responseLocal).build();
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
     * {@link #APPLICATION_BASE_URL_PARAM} context parameter. If correctly
     * configured, it should look something like
     * "http://catalog.clarin.eu/ds/ComponentRegistry". <em>Be aware that this
     * can also be null if configured incorrectly!</em>
     *
     * @see #APPLICATION_BASE_URL_PARAM
     */
    private String getApplicationBaseURI() {
        return servletContext.getInitParameter(APPLICATION_BASE_URL_PARAM);
    }

    private void validate(RegisterResponse response, Validator... validators) throws UserUnauthorizedException {
        for (Validator validator : validators) {
            if (!validator.validate()) {
                for (String error : validator.getErrorMessages()) {
                    response.addError(error);
                }
            }
        }
    }

    private void validateComment(CommentResponse response,
            Validator... validators) throws UserUnauthorizedException {
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
    @Override
    public void setComponentRegistryFactory(
            ComponentRegistryFactory componentRegistryFactory) {
        this.componentRegistryFactory = componentRegistryFactory;
    }

    /**
     *
     * @param listofcomponents a list of components whose file-names and whose
     * childrens' filenames are to be set to null
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
     * @param currentcomponent a component whose file-name and whose children
     * filenames are to be set to null
     */
    protected void setFileNamesToNullCurrent(CMDComponentType currentcomponent) {
        currentcomponent.setFilename(null);
        setFileNamesFromListToNull(currentcomponent.getCMDComponent());
    }

    /**
     *
     * @param isPublished if "true" then profiles and components from the user's
     * workspace, otherwise -- public
     * @param limit the number of items to be displayed
     * @return rss for the components in the database to which we are currently
     * connected
     * @throws ComponentRegistryException
     * @throws ParseException
     */
    @Override
    @GET
    @Path("/components/rss")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Rss getRssComponent(@QueryParam(GROUPID_PARAM) String groupId,
            @QueryParam(REGISTRY_SPACE_PARAM) @DefaultValue("published") String registrySpace,
            @QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
            throws ComponentRegistryException, ParseException, IOException {
        List<ComponentDescription> components = null;
        try {
            ComponentRegistry cr = this.initialiseRegistry(registrySpace, groupId);
            components = cr.getComponentDescriptions();
        } catch (AuthenticationFailException e) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e.toString());
            return new Rss();
        } catch (UserUnauthorizedException e) {
            response.sendError(Status.FORBIDDEN.getStatusCode(), e.toString());
            return new Rss();
        }
        catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), e.toString());
            return new Rss();
        }
        // obsolete, add group Id
        final RssCreatorDescriptions instance = new RssCreatorDescriptions(!registrySpace.equalsIgnoreCase("published"), getApplicationBaseURI(), "components",
                Integer.parseInt(limit), components,
                ComponentUtils.COMPARE_ON_DATE);
        final Rss rss = instance.getRss();
        LOG.debug("Releasing RSS of {} most recently registered components",
                limit);
        return rss;
    }

    /**
     *
     * @param isPublished if "true" then profiles and components from the user's
     * workspace, otherwise -- public
     * @param limit the number of items to be displayed
     * @return rss for the profiles in the database to which we are currently
     * connected
     * @throws ComponentRegistryException
     * @throws ParseException
     */
    @Override
    @GET
    @Path("/profiles/rss")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Rss getRssProfile(@QueryParam(GROUPID_PARAM) String groupId,
            @QueryParam(REGISTRY_SPACE_PARAM) @DefaultValue("published") String registrySpace,
            @QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
            throws ComponentRegistryException, ParseException, IOException {
        List<ProfileDescription> profiles = null;
        try {
            ComponentRegistry cr = this.initialiseRegistry(registrySpace, groupId);
            profiles = cr.getProfileDescriptions();
        } catch (AuthenticationFailException e) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e.toString());
            return new Rss();
        } catch (UserUnauthorizedException e) {
            response.sendError(Status.FORBIDDEN.getStatusCode(), e.toString());
            return new Rss();
        
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), e.toString());
            return new Rss();
        }
        final RssCreatorDescriptions instance = new RssCreatorDescriptions(
                !registrySpace.equalsIgnoreCase("published"), getApplicationBaseURI(), "profiles",
                Integer.parseInt(limit), profiles,
                ComponentUtils.COMPARE_ON_DATE);
        final Rss rss = instance.getRss();
        LOG.debug("Releasing RSS of {} most recently registered profiles",
                limit);
        return rss;
    }

    /**
     *
     * @param profileId the Id of a profile whose comments are to be rss-ed
     * @param isPublished if "true" then profiles and components from the user's
     * workspace, otherwise -- public
     * @param limit the number of items to be displayed
     * @return rss of the comments for a chosen profile
     * @throws ComponentRegistryException
     * @throws IOException
     * @throws JAXBException
     * @throws ParseException
     */
    @Override
    @GET
    @Path("/profiles/{profileId}/comments/rss")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Rss getRssOfCommentsFromProfile(
            @PathParam("profileId") String profileId,
            @QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
            throws ComponentRegistryException, IOException, JAXBException,
            ParseException {
        try {

            ComponentRegistry cr = this.getBaseRegistry();

            final List<Comment> comments = cr.getCommentsInProfile(profileId);
            final ProfileDescription pd = cr.getProfileDescriptionAccessControlled(profileId);
            final String profileName = pd.getName();
            boolean profileIsPrivate = !pd.isPublic();
            // obsolete, status must be involved, not boolean profileIsPrivate
            final RssCreatorComments instance = new RssCreatorComments(profileIsPrivate,
                    getApplicationBaseURI(), Integer.parseInt(limit), profileId,
                    profileName, "profile", comments, Comment.COMPARE_ON_DATE);
            final Rss rss = instance.getRss();
            LOG.debug("Releasing RSS of {} most recent post on profile {}", limit,
                    profileId);
            return rss;
        } catch (UserUnauthorizedException ex) {
            response.sendError(Status.FORBIDDEN.getStatusCode());
            return new Rss();
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode());
            return new Rss();
        } catch (AuthenticationFailException e) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e.toString());
            return new Rss();
        }
    }

    /**
     *
     * @param componentId the Id of a component whose comments are to be rss-ed
     * @param isPublished if "true" then profiles and components from the user's
     * workspace, otherwise -- public
     * @param limit the number of items to be displayed
     * @return rss of the comments for a chosen component
     * @throws ComponentRegistryException
     * @throws IOException
     * @throws JAXBException
     * @throws ParseException
     */
    @Override
    @GET
    @Path("/components/{componentId}/comments/rss")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Rss getRssOfCommentsFromComponent(
            @PathParam("componentId") String componentId,
            @QueryParam(NUMBER_OF_RSSITEMS)
            @DefaultValue("20") String limit)
            throws ComponentRegistryException, IOException, JAXBException,
            ParseException {
        try {
            ComponentRegistry cr = this.getBaseRegistry();
            final List<Comment> comments = cr.getCommentsInComponent(componentId);
            final ComponentDescription cd = cr.getComponentDescriptionAccessControlled(componentId);
            final String componentName = cd.getName();
            final boolean isPrivate = !cd.isPublic();
            //oboslete. status must be involved, not boolean isPrivate
            final RssCreatorComments instance = new RssCreatorComments(isPrivate,
                    getApplicationBaseURI(), Integer.parseInt(limit), componentId,
                    componentName, "component", comments, Comment.COMPARE_ON_DATE);
            final Rss rss = instance.getRss();
            LOG.debug("Releasing RSS of {} most recent post on component {}",
                    limit, componentId);
            return rss;
        } catch (UserUnauthorizedException e) {
            response.sendError(Status.FORBIDDEN.getStatusCode());
            return new Rss();
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode());
            return new Rss();
        } catch (AuthenticationFailException e1) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode());
            return new Rss();
        }

    }

    @Override
    @GET
    @Path("/AllowedTypes")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public AllowedAttributetypesXML getAllowedAttributeTypes()
            throws ComponentRegistryException, IOException, JAXBException,
            ParseException {
        return (new AllowedAttributetypesXML());
    }

    @Override
    @GET
    @Path("/groups/usermembership")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public List<Group> getGroupsTheCurrentUserIsAMemberOf() {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            return new ArrayList<Group>();
        }
        List<Group> groups = groupService.getGroupsOfWhichUserIsAMember(principal.getName());
        return groups;
    }

    @Override
    @GET
    @Path("/items/{itemId}/groups")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public List<Group> getGroupsTheItemIsAMemberOf(@PathParam("itemId") String itemId) {
        return groupService.getGroupsTheItemIsAMemberOf(itemId);
    }

    @Override
    @POST
    @Path("/items/{itemId}/transferownership")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response transferItemOwnershipToGroup(@PathParam("itemId") String itemId,
            @QueryParam(GROUPID_PARAM) long groupId) throws IOException {
        Principal principal = security.getUserPrincipal();
        try {
            groupService.transferItemOwnershipFromUserToGroupId(principal.getName(), groupId, itemId);
            return Response.ok("Ownership transferred").build();
        } catch (UserUnauthorizedException e) {
            return Response.status(Status.FORBIDDEN).build();
        }
    }

    @Override
    @GET
    @Path("/items/{itemId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public BaseDescription getBaseDescription(@PathParam("itemId") String itemId) throws ComponentRegistryException, IOException {
        LOG.debug("Item with id: {} is requested.", itemId);
        try {
            ComponentRegistry cr = this.getBaseRegistry();
            BaseDescription description;
            if (itemId.startsWith(ComponentDescription.COMPONENT_PREFIX)) {
                description = cr.getComponentDescriptionAccessControlled(itemId);
                return description;
            };
            if (itemId.startsWith(ProfileDescription.PROFILE_PREFIX)) {
                description = cr.getProfileDescriptionAccessControlled(itemId);
                return description;
            };
            response.sendError(Status.BAD_REQUEST.getStatusCode());
            return new BaseDescription();


        } catch (UserUnauthorizedException ex2) {
            response.sendError(Status.FORBIDDEN.getStatusCode(), ex2.getMessage());
            return new BaseDescription();
        } catch (ItemNotFoundException e) {
            response.sendError(Status.NOT_FOUND.getStatusCode(), e.getMessage());
            return new BaseDescription();
        } catch (AuthenticationFailException e) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode(), e.toString());
            return new BaseDescription();
        }
    }

    // Group Service (added by Olha)
    @Override
    @POST
    @Path("/groups/create")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response createNewGroup(@QueryParam("groupName") String groupName) throws IOException {

        try {
            Principal principal = this.checkAndGetUserPrincipal();
            Number id = groupService.createNewGroup(groupName, principal.getName());
            return Response.ok("Group with the name " + groupName + " is created and given an id " + id).build();
        } catch (AuthenticationFailException e) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @Override
    @GET
    @Path("/groups/principal")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public List<Group> getGroupsOwnedByUser(@QueryParam("principalName") String pricipalName) throws IOException {

        try {
            Principal principal = this.checkAndGetUserPrincipal();
            return groupService.getGroupsOwnedByUser(principal.getName());
        } catch (AuthenticationFailException e) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode());
            return new ArrayList<Group>();
        }
    }

    @Override
    @GET
    @Path("/groups/names")
    public Response listGroupNames() throws IOException {

        try {
            Principal principal = this.checkAndGetUserPrincipal();
            List<String> result = groupService.listGroupNames();
            //final GenericEntity<List<String>> entity  = new GenericEntity<List<String>>(result){};
            //return Response.status(Status.OK).entity(entity).build();
            StringsWrapper ids = new StringsWrapper();
            ids.setStrings(result);
            return Response.status(Status.OK).entity(ids).build();
        } catch (AuthenticationFailException e) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @Override
    @GET
    @Path("/groups/ownership")
    public Response isOwner(@QueryParam("groupName") String groupName) throws IOException {

        try {
            Principal principal = this.checkAndGetUserPrincipal();
            Boolean isOwner = groupService.isUserOwnerOfGroup(groupName, principal.getName());
            return Response.ok(isOwner.toString()).build();
        } catch (AuthenticationFailException e) {
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }
    }

    @Override
    @POST
    @Path("/groups/makemember")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response makeGroupMember(@QueryParam("groupName") String groupName, @QueryParam("principalName") String principalName) throws IOException {
        try {
            ComponentRegistry registry = this.getBaseRegistry();
            Number id = registry.makeGroupMember(principalName, groupName);
            return Response.ok(id.toString()).build();
        } catch (UserUnauthorizedException e) {
            return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (ItemNotFoundException e) {
            return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
        
        } catch (AuthenticationFailException e) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }
    
//    @Override
//    @DELETE
//    @Path("/groups/removemember")
//    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
//        MediaType.APPLICATION_JSON})
//    public Response removeGroupMember(@QueryParam("groupName") String groupName, @QueryParam("principalName") String principalName) throws IOException {
//        try {
//            ComponentRegistry registry = this.getBaseRegistry();
//            Number id = registry.removeGroupMember(principalName, groupName);
//            return Response.ok(id.toString()).build();
//        } catch (UserUnauthorizedException e) {
//            return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
//        } catch (ItemNotFoundException e) {
//            return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
//        
//        } catch (AuthenticationFailException e) {
//            return Response.status(Status.UNAUTHORIZED).build();
//        }
//    }

    @Override
    @GET
    @Path("/groups/profiles")
    public Response listProfiles(@QueryParam(GROUPID_PARAM) String groupId) throws IOException {

        try {
            Principal principal = this.checkAndGetUserPrincipal();
            List<String> result = groupService.getProfileIdsInGroup(Long.parseLong(groupId));
            StringsWrapper ids = new StringsWrapper();
            ids.setStrings(result);
            return Response.status(Status.OK).entity(ids).build();
        } catch (AuthenticationFailException e) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @Override
    @GET
    @Path("/groups/components")
    public Response listComponents(@QueryParam(GROUPID_PARAM) String groupId) throws IOException {

        try {
            Principal principal = this.checkAndGetUserPrincipal();
            List<String> result = groupService.getComponentIdsInGroup(Long.parseLong(groupId));
            StringsWrapper ids = new StringsWrapper();
            ids.setStrings(result);
            return Response.status(Status.OK).entity(ids).build();
        } catch (AuthenticationFailException e) {
            response.sendError(Status.UNAUTHORIZED.getStatusCode());
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @Override
    @GET
    @Path("/groups/nameById")
    public Response getGroupNameById(@QueryParam(GROUPID_PARAM) String groupId) throws IOException {

        try {
            Principal principal = this.checkAndGetUserPrincipal();
            String name = groupService.getGroupNameById(Long.parseLong(groupId));
            return Response.ok(name).build();
        } catch (AuthenticationFailException e) {
            return Response.status(Status.UNAUTHORIZED).build();
        } catch (ItemNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @Override
    @GET
    @Path("/groups/idByName")
    public Response getGroupIdByName(@QueryParam("groupName") String groupName) throws IOException {

        try {
            Principal principal = this.checkAndGetUserPrincipal();
            Number id = groupService.getGroupIdByName(groupName);
            return Response.ok(id.toString()).build();
        } catch (AuthenticationFailException e) {
            return Response.status(Status.UNAUTHORIZED).build();
        } catch (ItemNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @XmlRootElement(name = "Identifiers")
    public static class StringsWrapper {

        @XmlElement(name = "item")
        List<String> strings = new ArrayList<String>();

        StringsWrapper() {
        }

        public void setStrings(List<String> strings) {
            this.strings = strings;
        }
        
       
    }
}
