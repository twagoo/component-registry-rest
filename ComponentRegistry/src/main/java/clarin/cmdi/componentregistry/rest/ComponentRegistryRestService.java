package clarin.cmdi.componentregistry.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryImpl;

@Path("/registry")
public class ComponentRegistryRestService {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryRestService.class);

    private ComponentRegistry registry = ComponentRegistryImpl.getInstance();

    @GET
    @Path("/components")
    @Produces("text/xml")
    public String getRegisteredComponents() {
        List<String> components = registry.getComponentDescriptions();
        LOG.info("Releasing " + components.size() + " registered components into the world");
        StringBuilder result = new StringBuilder();
        result.append("<components>\n");
        for (String component : components) {
            String c = component.replaceFirst("<[?]xml.*[?]>\\n", ""); //TODO Patrick remove line
            result.append(c.trim() + "\n");
        }
        result.append("</components>\n");
        return result.toString();
    }

    @GET
    @Path("/profiles")
    @Produces("text/xml")
    public String getRegisteredProfiles() {
        List<String> profiles = registry.getProfileDescriptions();
        LOG.info("Releasing " + profiles.size() + " registered profiles into the world");
        StringBuilder result = new StringBuilder();
        result.append("<profiles>\n");
        for (String profile : profiles) {
            String p = profile.replaceFirst("<[?]xml.*[?]>\\n", "");
            result.append(p.trim() + "\n");
        }
        result.append("</profiles>\n");
        return result.toString();
    }

    @GET
    @Path("/components/{componentId}")
    @Produces("text/xml")
    public String getRegisteredComponent(@PathParam("componentId") String componentId) {
        LOG.info("Component with id:" + componentId + " is requested.");
        return registry.getMDComponent(componentId);
    }

    @GET
    @Path("/profiles/{profileId}")
    @Produces("text/xml")
    public String getRegisteredProfile(@PathParam("profileId") String profileId) {
        LOG.info("Profile with id:" + profileId + " is requested.");
        return registry.getMDProfile(profileId);
    }
}
