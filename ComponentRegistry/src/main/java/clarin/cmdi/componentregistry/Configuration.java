package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class Configuration {
    //NOTE: Default values, can be overwritten in applicationContext.xml
    private String generalComponentSchema = "http://www.clarin.eu/cmd/general-component-schema.xsd";
    private String component2SchemaXsl = "http://www.clarin.eu/cmd/xslt/comp2schema-v2/comp2schema.xsl";//"http://www.clarin.eu/cmd/comp2schema.xsl";
    private String isocatRestUrl = "http://www.isocat.org/rest/";

    private Set<String> adminUsers = new HashSet<String>();

    private List<String> displayNameShibbolethKeys = new ArrayList<String>();
    {//Default values
        displayNameShibbolethKeys.add("displayName");
        displayNameShibbolethKeys.add("commonName");
    }
    
    private Map<String, String> schemaLocations = new HashMap<String, String>();
    {//Default values
        schemaLocations.put(CMDComponentSpec.class.getName(),
                "http://www.clarin.eu/cmd/ http://www.clarin.eu/cmd/general-component-schema.xsd");
    }

    public Configuration() {
    }

    public String getComponent2SchemaXsl() {
        return component2SchemaXsl;
    }

    public List<String> getDisplayNameShibbolethKeys() {
        return displayNameShibbolethKeys;
    }

    public String getGeneralComponentSchema() {
        return generalComponentSchema;
    }

    public String getIsocatRestUrl() {
        return isocatRestUrl;
    }

    public String getSchemaLocation(String key) {
        return schemaLocations.get(key);
    }

    public boolean isAdminUser(Principal principal) {
        if (principal != null) {
            return adminUsers.contains(principal.getName());
        }
        return false;
    }

    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }

    public void setComponent2SchemaXsl(String component2SchemaXsl) {
        this.component2SchemaXsl = component2SchemaXsl;
    }

    public void setComponentSpecSchemaLocation(String componentSpecSchemaLocation) {
        schemaLocations.put(CMDComponentSpec.class.getName(), componentSpecSchemaLocation);
    }

    public void setDisplayNameShibbolethKeys(List<String> displayNameShibbolethKeys) {
        this.displayNameShibbolethKeys = displayNameShibbolethKeys;
    }

    public void setGeneralComponentSchema(String generalComponentSchema) {
        this.generalComponentSchema = generalComponentSchema;
    }

    public void setIsocatRestUrl(String isocatRestUrl) {
        this.isocatRestUrl = isocatRestUrl;
    }

}
