package clarin.cmdi.componentregistry.rest;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryImpl;
import clarin.cmdi.componentregistry.IdSequence;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.multipart.FormDataParam;

@Path("/registry")
public class ComponentRegistryRestService {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryRestService.class);

    private ComponentRegistry registry = ComponentRegistryImpl.getInstance();

    @GET
    @Path("/components")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ComponentDescription> getRegisteredComponents() {
        long start = System.currentTimeMillis();
        List<ComponentDescription> components = registry.getComponentDescriptions();
        LOG.info("Releasing " + components.size() + " registered components into the world ("+(System.currentTimeMillis()-start)+" millisecs)");
        return components;
    }

    @GET
    @Path("/profiles")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ProfileDescription> getRegisteredProfiles() {
        long start = System.currentTimeMillis();
        List<ProfileDescription> profiles = registry.getProfileDescriptions();
        LOG.info("Releasing " + profiles.size() + " registered profiles into the world ("+(System.currentTimeMillis()-start)+" millisecs)");
        return profiles;
    }

    @GET
    @Path("/components/{componentId}")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CMDComponentSpec getRegisteredComponent(@PathParam("componentId") String componentId) {
        LOG.info("Component with id:" + componentId + " is requested.");
        return registry.getMDComponent(componentId);
    }

    @GET
    @Path("/components/{profileId}/{rawType}")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML })
    public String getRegisteredComponentRawType(@PathParam("profileId") String componentId, @PathParam("rawType") String rawType) {
        LOG.info("Component with id:" + componentId + " and rawType:" + rawType + " is requested.");
        String result = "";
        if ("xml".equalsIgnoreCase(rawType)) {
            result = registry.getMDComponentAsXml(componentId);
        } else if ("xsd".equalsIgnoreCase(rawType)) {
            result = registry.getMDComponentAsXsd(componentId);
        } else {
            throw new WebApplicationException(Response.serverError().entity(
                    "unsupported rawType:" + rawType + " (only xml or xsd are supported)").build());
        }
        return result;
    }

    @GET
    @Path("/profiles/{profileId}")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CMDComponentSpec getRegisteredProfile(@PathParam("profileId") String profileId) {
        LOG.info("Profile with id:" + profileId + " is requested.");
        return registry.getMDProfile(profileId);
    }

    @GET
    @Path("/profiles/{profileId}/{rawType}")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public String getRegisteredProfileRawType(@PathParam("profileId") String profileId, @PathParam("rawType") String rawType) {
        LOG.info("Profile with id:" + profileId + " and rawType:" + rawType + " is requested.");
        String result = "";
        if ("xml".equalsIgnoreCase(rawType)) {
            result = registry.getMDProfileAsXml(profileId);
        } else if ("xsd".equalsIgnoreCase(rawType)) {
            result = registry.getMDProfileAsXsd(profileId);
        } else {
            throw new WebApplicationException(Response.serverError().entity(
                    "unsupported rawType:" + rawType + " (only xml or xsd are supported)").build());
        }
        return result;
    }

   
    @POST
    @Path("/profiles")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes("multipart/form-data")
    public RegisterResponse registeredProfile(@FormDataParam("data") InputStream input, @FormDataParam("name") String name,
            @FormDataParam("creatorName") String creatorName, @FormDataParam("description") String description) {
        ProfileDescription desc = createNewProfileDescription();
        desc.setCreatorName(creatorName);
        desc.setName(name);
        desc.setDescription(description);
        desc.setRegistrationDate(AbstractDescription.DATE_FORMAT.format(new Date()));
        LOG.info("Trying to register Profile: " + desc);
        return register(input, desc);
    }

    @POST
    @Path("/components")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes("multipart/form-data")
    public RegisterResponse registeredComponent(@FormDataParam("data") InputStream input, @FormDataParam("name") String name,
            @FormDataParam("creatorName") String creatorName, @FormDataParam("description") String description,
            @FormDataParam("group") String group) {
        ComponentDescription desc = createNewComponentDescription();
        desc.setCreatorName(creatorName);
        desc.setName(name);
        desc.setDescription(description);
        desc.setGroupName(group);
        desc.setRegistrationDate(AbstractDescription.DATE_FORMAT.format(new Date()));
        LOG.info("Trying to register Component: " + desc);
        return register(input, desc);
    }

    private RegisterResponse register(InputStream input, AbstractDescription desc) {
        DescriptionValidator descriptionValidator = new DescriptionValidator(desc);
        MDValidator validator = new MDValidator(input, desc);
        RegisterResponse response = new RegisterResponse();
        validate(response, descriptionValidator, validator);
        if (response.getErrors().isEmpty()) {
            CMDComponentSpec spec = validator.getCMDComponentSpec();
            int returnCode = spec.isIsProfile() ? registry.registerMDProfile((ProfileDescription) desc, spec) : registry
                    .registerMDComponent((ComponentDescription) desc, spec);
            if (returnCode == 0) {
                response.setRegistered(true);
                response.setDescription(desc);
            } else {
                //TODO PD, check and validate register response
                response.setRegistered(false);
                response.addError("Unable to register at this moment. Internal server errors.");
            }
        } else {
            response.setRegistered(false);
        }
        response.setIsProfile(desc.isProfile());
        return response;
    }

    private ComponentDescription createNewComponentDescription() {
        ComponentDescription desc = new ComponentDescription();
        String id = "c_" + IdSequence.get();
        desc.setId(id);
        desc.setXlink("link:" + id);
        return desc;
    }

    private ProfileDescription createNewProfileDescription() {
        ProfileDescription desc = new ProfileDescription();
        String id = "p_" + IdSequence.get();
        desc.setId(id);
        desc.setXlink("link:" + id);
        return desc;
    }

    private void validate(RegisterResponse response, Validator... validators) {
        for (Validator validator : validators) {
            if (!validator.validate()) {
                response.addError(validator.getErrorMessage());
            }
        }
    }
}
