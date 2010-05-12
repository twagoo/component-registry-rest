package clarin.cmdi.componentregistry;

import java.io.File;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentRegistryFactory {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryFactory.class);
    private static final ComponentRegistryFactory INSTANCE = new ComponentRegistryFactory();

    private Map<String, String> userMap = new HashMap<String, String>();

    private ComponentRegistryImpl publicRegistry = new ComponentRegistryImpl(true);
    private Map<String, ComponentRegistry> registryMap = new HashMap<String, ComponentRegistry>();

    private ComponentRegistryFactory() {
        init();
    }

    private void init() {
        publicRegistry.setResourceConfig(Configuration.getInstance().getPublicResourceConfig());
    }

    public void reset() {
        registryMap = new HashMap<String, ComponentRegistry>();
    }

    public static ComponentRegistryFactory getInstance() {
        return INSTANCE;
    }

    public ComponentRegistry getPublicRegistry() {
        return publicRegistry;
    }

    public ComponentRegistry getComponentRegistry(boolean userspace, Principal principal) {
        ComponentRegistry result = getPublicRegistry();
        if (userspace) {
            if (principal != null) {
                String name = principal.getName();//anonymous
                String user = getUser(name);
                result = registryMap.get(user);
                if (result == null) {
                    LOG.info("Creating workspace for user: " + name + " workspace name: " + user);
                    result = createNewUserRegistry(user);
                    registryMap.put(user, result);
                }
            }
        }
        return result;
    }

    private ComponentRegistry createNewUserRegistry(String user) {
        ComponentRegistryImpl result = new ComponentRegistryImpl(false);
        ResourceConfig config = new ResourceConfig();
        File userResourceDir = new File(Configuration.getInstance().getRegistryRoot(), ResourceConfig.USERS_DIR_NAME + File.separator + user);
        config.setResourceRoot(userResourceDir);
        config.init();
        result.setResourceConfig(config);
        return result;
    }

    private String getUser(String name) {
        String result = userMap.get(name);
        if (result == null) {
            result = "user1";
        }
        return result;
    }

}
