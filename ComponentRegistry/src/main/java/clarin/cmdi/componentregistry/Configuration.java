package clarin.cmdi.componentregistry;

import java.io.File;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;

public class Configuration {
    public static final String DELETED_DIR_NAME = "deleted";

    private final static Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private File registryRoot;
    private File componentDir;
    private File profileDir;
    private File profileDeletionDir;
    private File componentDeletionDir;
    //NOTE: Default values, can be overwritten in applicationContext.xml
    private String generalComponentSchema = "http://www.clarin.eu/cmd/general-component-schema.xsd";
    private String component2SchemaXsl ="http://www.clarin.eu/cmd/comp2schema.xsl";
    private Set<String> adminUsers;

    private Map<String, String> schemaLocations = new HashMap<String, String>(); 
    {
        schemaLocations.put(CMDComponentSpec.class.getName(), "http://www.clarin.eu/cmd http://www.clarin.eu/cmd/general-component-schema.xsd");
    }
    
    
    private final static Configuration INSTANCE = new Configuration();

    private Configuration() {
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }
    
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

    public void setComponentSpecSchemaLocation(String componentSpecSchemaLocation) {
        schemaLocations.put(CMDComponentSpec.class.getName(), componentSpecSchemaLocation);
    }
    
    public String getSchemaLocation(String key) {
        return schemaLocations.get(key);
    }

    public void setGeneralComponentSchema(String generalComponentSchema) {
        this.generalComponentSchema = generalComponentSchema;
    }

    public String getGeneralComponentSchema() {
        return generalComponentSchema;
    }

    public void setComponent2SchemaXsl(String component2SchemaXsl) {
        this.component2SchemaXsl = component2SchemaXsl;
    }

    public String getComponent2SchemaXsl() {
        return component2SchemaXsl;
    }
}
