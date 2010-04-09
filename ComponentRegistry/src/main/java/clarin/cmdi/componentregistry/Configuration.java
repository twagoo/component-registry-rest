package clarin.cmdi.componentregistry;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private final static Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private File registryRoot;
    private File componentDir;
    private File profileDir;

    public void init() {
        boolean isValid = true;
        isValid &= validateAndCreate(registryRoot);
        componentDir = new File(registryRoot, "components");
        isValid &= validateAndCreate(componentDir);
        profileDir = new File(registryRoot, "profiles");
        isValid &= validateAndCreate(profileDir);
        if (isValid) {
            LOG.info("Initialized Component Registry succesfully. Registry is located in: "+registryRoot);
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

    public File getComponentDir() {
        return componentDir;
    }

    public File getProfileDir() {
        return profileDir;
    }

}
