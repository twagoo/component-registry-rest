package clarin.cmdi.componentregistry;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService
public class ComponentRegistryWSImpl {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryWSImpl.class);
    private ComponentRegistry registry = ComponentRegistryImpl.getInstance();

    @WebMethod
    public List<String> getRegisteredComponent() {
        List<String> result = registry.getComponentDescriptions();
        LOG.info("Releasing " + result.size() + " registered components into the world");
        return result;
    }

    @WebMethod
    public String getRegisteredProfiles() {
        StringBuilder result = new StringBuilder();
        result.append("<profiles>\n");
        List<String> profiles = registry.getProfileDescriptions();
        for (String profile : profiles) {
            result.append(profile);
        }
        result.append("\n</profiles>");
        LOG.info("Releasing " + profiles.size() + " registered profiles into the world");
        return StringEscapeUtils.escapeXml(result.toString());
    }

    @WebMethod
    public String getRegisteredProfile(String id) {
        LOG.info("Profile with id:" + id + " is requested.");
        return registry.getMDProfile(id);
    }
}
