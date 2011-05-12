package clarin.cmdi.componentregistry.impl.filesystem;

import clarin.cmdi.componentregistry.Configuration;
import java.io.File;

public class FileSystemConfiguration extends Configuration {

    private File registryRoot;

    private ResourceConfig publicResourceConfig;


    private final static FileSystemConfiguration INSTANCE = new FileSystemConfiguration();

    private FileSystemConfiguration() {
    }

    public static FileSystemConfiguration getInstance() {
        return INSTANCE;
    }

    public void init() {
        publicResourceConfig = new ResourceConfig();
        publicResourceConfig.setResourceRoot(registryRoot);
        publicResourceConfig.init();
    }

    public ResourceConfig getPublicResourceConfig() {
        return publicResourceConfig;
    }

    public void setRegistryRoot(File registryRoot) {
        this.registryRoot = registryRoot;
    }

    public File getRegistryRoot() {
        return registryRoot;
    }

    public File getUserDirMappingFile() {
        return new File(registryRoot, "userMapping.xml");
    }

}
