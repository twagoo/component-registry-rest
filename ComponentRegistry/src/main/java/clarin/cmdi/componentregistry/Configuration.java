package clarin.cmdi.componentregistry;

import java.io.File;
import java.security.Principal;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    public static final String DELETED_DIR_NAME = "deleted";

    private final static Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private File registryRoot;
    private File componentDir;
    private File profileDir;
    private File profileDeletionDir;
    private File componentDeletionDir;

    private Set<String> adminUsers;

    public void init() {
        boolean isValid = true;
        isValid &= validateAndCreate(registryRoot);
        componentDir = new File(registryRoot, "components");
        isValid &= validateAndCreate(componentDir);
        componentDeletionDir = new File(componentDir, DELETED_DIR_NAME);
        isValid &= validateAndCreate(componentDeletionDir);
        profileDir = new File(registryRoot, "profiles");
        isValid &= validateAndCreate(profileDir);
        profileDeletionDir = new File(profileDir, DELETED_DIR_NAME);
        isValid &= validateAndCreate(profileDeletionDir);
        if (isValid) {
            LOG.info("Initialized Component Registry succesfully. Registry is located in: " + registryRoot);
        }
    }

    private boolean validateAndCreate(File file) {
        boolean result = true;
        if (!file.exists()) {
            result = file.mkdir();
        }
        if (!result)
            LOG.error("Cannot create registry root: " + registryRoot);
        return result;
    }

    public void setRegistryRoot(File registryRoot) {
        this.registryRoot = registryRoot;
    }

    public File getRegistryRoot() {
        return registryRoot;
    }

    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }

    public File getComponentDir() {
        return componentDir;
    }

    public File getProfileDir() {
        return profileDir;
    }

    public File getProfileDeletionDir() {
        return profileDeletionDir;
    }

    public File getComponentDeletionDir() {
        return componentDeletionDir;
    }

    public boolean isAdminUser(Principal principal) {
        return adminUsers.contains(principal.getName());
    }

}
