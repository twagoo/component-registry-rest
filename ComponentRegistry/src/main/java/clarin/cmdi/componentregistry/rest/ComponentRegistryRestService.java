package clarin.cmdi.componentregistry.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.multipart.FormDataParam;

@Path("/registry")
public class ComponentRegistryRestService {

    @Context
    UriInfo uriInfo;
    @Context
    SecurityContext security;

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryRestService.class);

    public static final String DATA_FORM_FIELD = "data";
    public static final String NAME_FORM_FIELD = "name";
    public static final String DESCRIPTION_FORM_FIELD = "description";
    public static final String GROUP_FORM_FIELD = "group";
    public static final String DOMAIN_FORM_FIELD = "domainName";
    public static final String USERSPACE_PARAM = "userspace";

    private ComponentRegistry getRegistry(boolean userspace) {
        return ComponentRegistryFactory.getInstance().getComponentRegistry(userspace, security.getUserPrincipal());
    }

    @GET
    @Path("/components")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ComponentDescription> getRegisteredComponents(@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        long start = System.currentTimeMillis();
        List<ComponentDescription> components = getRegistry(userspace).getComponentDescriptions();
        LOG.info("Releasing " + components.size() + " registered components into the world (" + (System.currentTimeMillis() - start)
                + " millisecs)");
        return components;
    }

    @GET
    @Path("/profiles")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ProfileDescription> getRegisteredProfiles(@QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        long start = System.currentTimeMillis();
        List<ProfileDescription> profiles = getRegistry(userspace).getProfileDescriptions();
        LOG.info("Releasing " + profiles.size() + " registered profiles into the world (" + (System.currentTimeMillis() - start)
                + " millisecs)");
        return profiles;
    }

    @GET
    @Path("/components/{componentId}")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CMDComponentSpec getRegisteredComponent(@PathParam("componentId") String componentId,
            @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        LOG.info("Component with id: " + componentId + " is requested.");
        return getRegistry(userspace).getMDComponent(componentId);
    }

    @DELETE
    @Path("/components/{componentId}")
    public Response deleteRegisteredComponent(@PathParam("componentId") String componentId,
            @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new IllegalArgumentException("no user principal found.");
        }
        ComponentRegistry registry = getRegistry(userspace);
        LOG.info("Component with id: " + componentId + " set for deletion.");
        try {
            List<ProfileDescription> profiles = registry.getUsageInProfiles(componentId);
            List<ComponentDescription> components = registry.getUsageInComponents(componentId);
            if (profiles.isEmpty() && components.isEmpty()) {
                registry.deleteMDComponent(componentId, principal);
            } else {
                return Response.status(Status.FORBIDDEN).entity(createStillInUseMessage(profiles, components)).build();
            }
        } catch (IOException e) {
            LOG.info("Component with id: " + componentId + " deletion failed.", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserUnauthorizedException e) {
            LOG.info("Component with id: " + componentId + " deletion failed: " + e.getMessage());
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }
        LOG.info("Component with id: " + componentId + " deleted.");
        return Response.ok().build();
    }

    private String createStillInUseMessage(List<ProfileDescription> profiles, List<ComponentDescription> components) {
        StringBuilder result = new StringBuilder();
        if (!profiles.isEmpty()) {
            result.append("Still used by the following profiles: \n");
            for (ProfileDescription profileDescription : profiles) {
                result.append(" - " + profileDescription.getName() + "\n");
            }
        }
        if (!components.isEmpty()) {
            result.append("Still used by the following components: \n");
            for (ComponentDescription componentDescription : components) {
                result.append(" - " + componentDescription.getName() + "\n");
            }
        }
        result.append("Try to change above mentioned references first.");
        return result.toString();
    }

    @GET
    @Path("/components/{componentId}/{rawType}")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML })
    public Response getRegisteredComponentRawType(@PathParam("componentId") String componentId, @PathParam("rawType") String rawType,
            @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        LOG.info("Component with id: " + componentId + " and rawType:" + rawType + " is requested.");
        String result = "";
        ComponentRegistry registry = getRegistry(userspace);
        ComponentDescription desc = registry.getComponentDescription(componentId);
        if (desc == null) {
            return null;
        }
        String fileName = desc.getName() + "." + rawType;
        if ("xml".equalsIgnoreCase(rawType)) {
            result = registry.getMDComponentAsXml(componentId);
        } else if ("xsd".equalsIgnoreCase(rawType)) {
            result = registry.getMDComponentAsXsd(componentId);
        } else {
            throw new WebApplicationException(Response.serverError().entity(
                    "unsupported rawType: " + rawType + " (only xml or xsd are supported)").build());
        }
        return createDownloadResponse(result, fileName);
    }

    @GET
    @Path("/profiles/{profileId}")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CMDComponentSpec getRegisteredProfile(@PathParam("profileId") String profileId,
            @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        LOG.info("Profile with id: " + profileId + " is requested.");
        return getRegistry(userspace).getMDProfile(profileId);
    }

    /**
     * 
     * Purely helper method for my front-end (FLEX) which only does post/get requests. The query param is checked and the "proper" method is
     * called.
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

    /**
     * 
     * Purely helper method for my front-end (FLEX) which van only do post/get requests. The query param is checked and the "proper" method
     * is called.
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

    @DELETE
    @Path("/profiles/{profileId}")
    public Response deleteRegisteredProfile(@PathParam("profileId") String profileId,
            @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new IllegalArgumentException("no user principal found.");
        }
        LOG.info("Profile with id: " + profileId + " set for deletion.");
        try {
            getRegistry(userspace).deleteMDProfile(profileId, principal);
        } catch (IOException e) {
            LOG.info("Profile with id: " + profileId + " deletion failed.", e);
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserUnauthorizedException e) {
            LOG.info("Profile with id: " + profileId + " deletion failed: " + e.getMessage());
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        }
        LOG.info("Profile with id: " + profileId + " deleted.");
        return Response.ok().build();
    }

    @GET
    @Path("/profiles/{profileId}/{rawType}")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML })
    public Response getRegisteredProfileRawType(@PathParam("profileId") String profileId, @PathParam("rawType") String rawType,
            @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        LOG.info("Profile with id: " + profileId + " and rawType:" + rawType + " is requested.");
        String result = "";
        ComponentRegistry registry = getRegistry(userspace);
        ProfileDescription desc = registry.getProfileDescription(profileId);
        if (desc == null) {
            return null;
        }
        String fileName = desc.getName() + "." + rawType;
        if ("xml".equalsIgnoreCase(rawType)) {
            result = registry.getMDProfileAsXml(profileId);
        } else if ("xsd".equalsIgnoreCase(rawType)) {
            result = registry.getMDProfileAsXsd(profileId);
        } else {
            throw new WebApplicationException(Response.serverError().entity(
                    "unsupported rawType: " + rawType + " (only xml or xsd are supported)").build());
        }
        return createDownloadResponse(result, fileName);

    }

    private Response createDownloadResponse(String result, String fileName) {
        //Making response so it triggers browsers native save as dialog.
        Response response = Response.ok().type("application/x-download").header("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"").entity(result).build();
        return response;

    }

    @POST
    @Path("/profiles")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes("multipart/form-data")
    public RegisterResponse registerProfile(@FormDataParam(DATA_FORM_FIELD) InputStream input, @FormDataParam(NAME_FORM_FIELD) String name,
            @FormDataParam(DESCRIPTION_FORM_FIELD) String description, @FormDataParam(DOMAIN_FORM_FIELD) String domainName,
            @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new IllegalArgumentException("no user principal found.");
        }
        ProfileDescription desc = createNewProfileDescription();
        desc.setCreatorName(principal.getName());
        desc.setName(name);
        desc.setDescription(description);
        desc.setDomainName(domainName);
        desc.setRegistrationDate(createNewDate());
        LOG.info("Trying to register Profile: " + desc);
        return register(input, desc, principal, userspace);
    }

    @POST
    @Path("/components")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes("multipart/form-data")
    public RegisterResponse registerComponent(@FormDataParam(DATA_FORM_FIELD) InputStream input,
            @FormDataParam(NAME_FORM_FIELD) String name, @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
            @FormDataParam(GROUP_FORM_FIELD) String group, @FormDataParam(DOMAIN_FORM_FIELD) String domainName,
            @QueryParam(USERSPACE_PARAM) @DefaultValue("false") boolean userspace) {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new IllegalArgumentException("no user principal found.");
        }
        ComponentDescription desc = createNewComponentDescription();
        desc.setCreatorName(principal.getName());
        desc.setName(name);
        desc.setDescription(description);
        desc.setGroupName(group);
        desc.setDomainName(domainName);
        desc.setRegistrationDate(createNewDate());
        LOG.info("Trying to register Component: " + desc);
        return register(input, desc, principal, userspace);
    }

    private String createNewDate() {
        return DateFormatUtils.formatUTC(new Date(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
    }

    private RegisterResponse register(InputStream input, AbstractDescription desc, Principal principal, boolean userspace) {
        try {
            ComponentRegistry registry = getRegistry(userspace);
            DescriptionValidator descriptionValidator = new DescriptionValidator(desc);
            MDValidator validator = new MDValidator(input, desc, registry);
            RegisterResponse response = new RegisterResponse();
            response.setIsInUserSpace(userspace);
            validate(response, descriptionValidator, validator);
            if (response.getErrors().isEmpty()) {
                CMDComponentSpec spec = validator.getCMDComponentSpec();
                int returnCode = spec.isIsProfile() ? registry.registerMDProfile((ProfileDescription) desc, spec) : registry
                        .registerMDComponent((ComponentDescription) desc, spec);
                if (returnCode == 0) {
                    response.setRegistered(true);
                    response.setDescription(desc);
                } else {
                    response.setRegistered(false);
                    response.addError("Unable to register at this moment. Internal server error.");
                }
            } else {
                LOG.info("Registration failed with validation errors:" + Arrays.toString(response.getErrors().toArray()));
                response.setRegistered(false);
            }
            response.setIsProfile(desc.isProfile());
            return response;
        } finally {
            try {
                input.close();//either we read the input or there was an exception, we need to close it.
            } catch (IOException e) {
                LOG.error("Error when closing inputstream: ", e);
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

    private void validate(RegisterResponse response, Validator... validators) {
        for (Validator validator : validators) {
            if (!validator.validate()) {
                for (String error : validator.getErrorMessages()) {
                    response.addError(error);
                }
            }
        }
    }
}
