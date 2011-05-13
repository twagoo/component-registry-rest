package clarin.cmdi.componentregistry.impl.filesystem;

import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserCredentials;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.model.UserMapping;
import clarin.cmdi.componentregistry.model.UserMapping.User;

public class ComponentRegistryFactoryImpl implements ComponentRegistryFactory {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryFactoryImpl.class);
    private static ComponentRegistryFactoryImpl INSTANCE = null;
    private UserMapping userMap = null;
    private ComponentRegistryImpl publicRegistry = new ComponentRegistryImpl(true);
    private Map<String, ComponentRegistry> registryMap = new ConcurrentHashMap<String, ComponentRegistry>();
    private Configuration configuration;
    private FileSystemConfiguration fsConfiguration;

    private ComponentRegistryFactoryImpl() {
    }

    private void init() {
        publicRegistry.setResourceConfig(fsConfiguration.getPublicResourceConfig());
        try {
            loadUserMap();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load userMap", e);
        } catch (JAXBException e) {
            throw new RuntimeException("Cannot load userMap", e);
        }
    }

    private synchronized void loadUserMap() throws IOException, JAXBException {
        File userDirMappingFile = fsConfiguration.getUserDirMappingFile();
        if (userDirMappingFile.exists()) {
            userMap = MDMarshaller.unmarshal(UserMapping.class, userDirMappingFile, null);
        } else {
            userMap = new UserMapping();
        }
    }

    synchronized void reset() {
        registryMap = new ConcurrentHashMap<String, ComponentRegistry>();
        userMap = new UserMapping();
        publicRegistry = new ComponentRegistryImpl(true);
    }

    public static synchronized ComponentRegistryFactoryImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComponentRegistryFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public ComponentRegistry getPublicRegistry() {
        return publicRegistry;
    }

    @Override
    public synchronized ComponentRegistry getComponentRegistry(boolean userspace, UserCredentials credentials) {
        ComponentRegistry result = null;
        if (userspace) {
            if (credentials != null && !ANONYMOUS_USER.equals(credentials.getPrincipalName())) {
                String principalName = credentials.getPrincipalName();
                String userDir = getOrCreateUserDir(principalName, credentials.getDisplayName());
                result = loadWorkspace(principalName, userDir);
            } else {
                throw new IllegalArgumentException("No user credentials available cannot load userspace.");
            }
        } else {
            result = getPublicRegistry();
        }
        return result;
    }

    private synchronized ComponentRegistry loadWorkspace(String name, String userDir) {
        ComponentRegistry result;
        result = registryMap.get(userDir);
        if (result == null) {
            LOG.info("Loading workspace for user: " + name + " workspace name: " + userDir);
            result = createNewUserRegistry(userDir);
            registryMap.put(userDir, result);
        }
        return result;
    }

    @Override
    public synchronized ComponentRegistry getOtherUserComponentRegistry(Principal adminPrincipal, String principalNameMD5) {
        User user = getUserDir(principalNameMD5);
        ComponentRegistry result = null;
        if (user != null) {
            if (configuration.isAdminUser(adminPrincipal)) {
                result = loadWorkspace(adminPrincipal.getName(), user.getUserDir());
            } else {
                throw new IllegalArgumentException("User is not admin user cannot load userspace.");
            }
        } else {
            result = getPublicRegistry();
        }
        return result;
    }

    private ComponentRegistry createNewUserRegistry(String user) {
        ComponentRegistryImpl result = new ComponentRegistryImpl(false);
        ResourceConfig config = new ResourceConfig();
        File userResourceDir = new File(fsConfiguration.getRegistryRoot(), ResourceConfig.USERS_DIR_NAME + File.separator
                + user);
        config.setResourceRoot(userResourceDir);
        config.init();
        result.setResourceConfig(config);
        return result;
    }

    private User getUserDir(String principalNameMD5) {
        UserMapping.User user = userMap.findUser(principalNameMD5);
        return user;
    }

    synchronized String getOrCreateUserDir(String principalName, String displayName) {
        if (principalName == null) {
            return null;
        }
        UserMapping.User user = getUserDir(DigestUtils.md5Hex(principalName));
        if (user == null) {
            user = new UserMapping.User();
            user.setPrincipalName(principalName);
            user.setName(displayName);
            user.setUserDir(ResourceConfig.USER_DIR_PREFIX + userMap.getUsers().size());
            userMap.addUsers(user);
            saveUserMap();
        }
        return user.getUserDir();
    }

    @Deprecated
    public UserMapping getUserMap() {
        return userMap;
    }

    private synchronized void saveUserMap() {
        try {
            MDMarshaller.marshal(userMap, new FileOutputStream(fsConfiguration.getUserDirMappingFile()));
        } catch (IOException e) {//Manual intervention is probably needed so just throwing RuntimeExceptions if we cannot save the mapping we cannot do a lot so that needs to be addressed asap.
            throw new RuntimeException("Cannot save userMapping.", e);
        } catch (JAXBException e) {
            throw new RuntimeException("Cannot save userMapping.", e);
        }
    }

    @Override
    public synchronized List<ComponentRegistry> getAllUserRegistries() {
        List<ComponentRegistry> result = new ArrayList<ComponentRegistry>();
        List<User> users = userMap.getUsers();
        for (User user : users) {
            ComponentRegistry registry = loadWorkspace("internal use", user.getUserDir());
            result.add(registry);
        }
        return result;
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setFileSystemConfiguration(FileSystemConfiguration configuration) {
        this.fsConfiguration = configuration;
    }
}
