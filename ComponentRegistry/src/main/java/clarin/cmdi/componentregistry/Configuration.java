package clarin.cmdi.componentregistry;

import java.io.File;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;

public class Configuration {

    private File registryRoot;

    //NOTE: Default values, can be overwritten in applicationContext.xml
    private String generalComponentSchema = "http://www.clarin.eu/cmd/general-component-schema.xsd";
    private String component2SchemaXsl = "http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl";//"http://www.clarin.eu/cmd/comp2schema.xsl";
    private String isocatRestUrl = "http://www.isocat.org/rest/";

    private Set<String> adminUsers = new HashSet<String>();
    private ResourceConfig publicResourceConfig;

    private Map<String, String> schemaLocations = new HashMap<String, String>();
    {//Default values
        schemaLocations.put(CMDComponentSpec.class.getName(),
                "http://www.clarin.eu/cmd http://www.clarin.eu/cmd/general-component-schema.xsd");
    }

    private List<String> displayNameShibbolethKeys = new ArrayList<String>();
    {//Default values
        displayNameShibbolethKeys.add("displayName");
        displayNameShibbolethKeys.add("commonName");
    }
    private final static Configuration INSTANCE = new Configuration();

    private Configuration() {
    }

    public static Configuration getInstance() {
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

    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }

    public boolean isAdminUser(Principal principal) {
        if (principal != null) {
            return adminUsers.contains(principal.getName());
        }
        return false;
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

    public File getUserDirMappingFile() {
        return new File(registryRoot, "userMapping.xml");
    }

    public void setIsocatRestUrl(String isocatRestUrl) {
        this.isocatRestUrl = isocatRestUrl;
    }

    public String getIsocatRestUrl() {
        return isocatRestUrl;
    }

    public List<String> getDisplayNameShibbolethKeys() {
        return displayNameShibbolethKeys;
    }

    public void setDisplayNameShibbolethKeys(List<String> displayNameShibbolethKeys) {
        this.displayNameShibbolethKeys = displayNameShibbolethKeys;
    }

}
