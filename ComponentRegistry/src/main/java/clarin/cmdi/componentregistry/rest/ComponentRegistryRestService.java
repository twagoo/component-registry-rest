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
import clarin.cmdi.componentregistry.MDValidator;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
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
        List<ComponentDescription> components = registry.getComponentDescriptions();
        LOG.info("Releasing " + components.size() + " registered components into the world");
        return components;
    }

    @GET
    @Path("/profiles")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ProfileDescription> getRegisteredProfiles() {
        List<ProfileDescription> profiles = registry.getProfileDescriptions();
        LOG.info("Releasing " + profiles.size() + " registered profiles into the world");
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
    @Path("/profiles/{profileId}")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CMDComponentSpec getRegisteredProfile(@PathParam("profileId") String profileId) {
        LOG.info("Profile with id:" + profileId + " is requested.");
        return registry.getMDProfile(profileId);
    }

    @POST
    @Path("/profiles")
    @Produces( { MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes("multipart/form-data")
    public synchronized RegisterResponse registeredProfile(@FormDataParam("profileData") InputStream input,
            @FormDataParam("name") String name, @FormDataParam("creatorName") String creatorName,
            @FormDataParam("description") String description) {
        ProfileDescription desc = new ProfileDescription();
        desc.setCreatorName(creatorName);
        desc.setName(name);
        desc.setRegistrationDate(new Date().toString());
        desc.setDescription(description);
        String id = "p_" + IdSequence.get();
        desc.setId(id);
        desc.setXlink("link:" + id);
        LOG.info("Trying to register Profile: " + desc);
        MDValidator validator = new MDValidator();
        CMDComponentSpec spec = validator.validateInputStream(input);
        RegisterResponse response = new RegisterResponse();
        if (spec != null) {
            registry.registerMDProfile(desc, spec);
            response.setRegistered(true);
            response.setProfileDescription(desc);
        } else {
            response.setRegistered(false);
            response.addError(validator.getErrorMessage());
        }
        return response;
    }
}
