package clarin.cmdi.componentregistry;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceConfig {

    private static final String PROFILES_DIR_NAME = "profiles";
    private static final String COMPONENTS_DIR_NAME = "components";
    static final String DELETED_DIR_NAME = "deleted";
    static final String USERS_DIR_NAME = "users";


    private final static Logger LOG = LoggerFactory.getLogger(ResourceConfig.class);

    private File componentDir;
    private File profileDir;
    private File profileDeletionDir;
    private File componentDeletionDir;
    private File resourceRoot;

    public void init() {
        boolean isValid = true;
        isValid &= validateAndCreate(resourceRoot);
        componentDir = new File(resourceRoot, COMPONENTS_DIR_NAME);
        isValid &= validateAndCreate(componentDir);
        componentDeletionDir = new File(componentDir, DELETED_DIR_NAME);
        isValid &= validateAndCreate(componentDeletionDir);
        profileDir = new File(resourceRoot, PROFILES_DIR_NAME);
        isValid &= validateAndCreate(profileDir);
        profileDeletionDir = new File(profileDir, DELETED_DIR_NAME);
        isValid &= validateAndCreate(profileDeletionDir);
        if (isValid) {
            LOG.info("Initialized Component Registry succesfully. Registry is located in: " + resourceRoot);
        }
    }

    private boolean validateAndCreate(File file) {
        boolean result = true;
        if (!file.exists()) {
            result = file.mkdirs();
        }
        if (!result)
            LOG.error("Cannot create resource root: " + resourceRoot);
        return result;
    }

    public void setResourceRoot(File resourceRoot) {
        this.resourceRoot = resourceRoot;
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

}
