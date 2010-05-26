package clarin.cmdi.componentregistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.model.UserMapping;

public class ComponentRegistryFactory {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryFactory.class);
    private static final ComponentRegistryFactory INSTANCE = new ComponentRegistryFactory();

    private UserMapping userMap = null;

    private ComponentRegistryImpl publicRegistry = new ComponentRegistryImpl(true);
    private Map<String, ComponentRegistry> registryMap = new ConcurrentHashMap<String, ComponentRegistry>();

    private ComponentRegistryFactory() {
        init();
        try {
            loadUserMap();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load userMap", e);
        } catch (JAXBException e) {
            throw new RuntimeException("Cannot load userMap", e);
        }
    }

    private void init() {
        publicRegistry.setResourceConfig(Configuration.getInstance().getPublicResourceConfig());
    }

    private void loadUserMap() throws IOException, JAXBException {
        File userDirMappingFile = Configuration.getInstance().getUserDirMappingFile();
        if (userDirMappingFile.exists()) {
            userMap = MDMarshaller.unmarshal(UserMapping.class, userDirMappingFile, null);
        } else {
            userMap = new UserMapping();
        }
    }

    void reset() {
        registryMap = new ConcurrentHashMap<String, ComponentRegistry>();
        publicRegistry = new ComponentRegistryImpl(true);
    }

    public static ComponentRegistryFactory getInstance() {
        return INSTANCE;
    }

    public ComponentRegistry getPublicRegistry() {
        return publicRegistry;
    }

    public synchronized ComponentRegistry getComponentRegistry(boolean userspace, Principal principal) {
        ComponentRegistry result = null;
        if (userspace) {
            if (principal != null && !"anonymous".equals(principal.getName())) {
                String name = principal.getName();
                String user = getUserDir(name);
                result = registryMap.get(user);
                if (result == null) {
                    LOG.info("Creating workspace for user: " + name + " workspace name: " + user);
                    result = createNewUserRegistry(user);
                    registryMap.put(user, result);
                }
            } else {
                throw new IllegalArgumentException("No user credentials available cannot create userspace.");
            }
        } else {
            result = getPublicRegistry();
        }
        return result;
    }

    private ComponentRegistry createNewUserRegistry(String user) {
        ComponentRegistryImpl result = new ComponentRegistryImpl(false);
        ResourceConfig config = new ResourceConfig();
        File userResourceDir = new File(Configuration.getInstance().getRegistryRoot(), ResourceConfig.USERS_DIR_NAME + File.separator
                + user);
        config.setResourceRoot(userResourceDir);
        config.init();
        result.setResourceConfig(config);
        return result;
    }

    private String getUserDir(String name) {
        UserMapping.User user = userMap.findUser(name);
        if (user == null) {
            user = new UserMapping.User();
            user.setName(name);
            user.setUserDir("user" + userMap.getUsers().size());
            userMap.addUsers(user);
            saveUserMap();
        }
        return user.getUserDir();
    }

    private void saveUserMap() {
        try {
            MDMarshaller.marshal(userMap, new FileOutputStream(Configuration.getInstance().getUserDirMappingFile()));
        } catch (IOException e) {//Manual intervention is probably needed so just throwing RuntimeExceptions if we cannot save the mapping we cannot do a lot so that needs to be addressed asap.
            throw new RuntimeException("Cannot save userMapping.", e);
        } catch (JAXBException e) {
            throw new RuntimeException("Cannot save userMapping.", e);
        }
    }

}
