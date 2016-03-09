package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.ComponentSpec;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class Configuration {

    private final static Logger LOG = LoggerFactory.getLogger(Configuration.class);
    //NOTE: Default values, can be overwritten in applicationContext.xml
    private String generalComponentSchema = "https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd";
    private String component2SchemaXsl = "https://infra.clarin.eu/CMDI/1.x/xslt/comp2schema.xsl";
    private String ccrRestUrl = "https://openskos.meertens.knaw.nl/ccr/api/";
    private Collection<String> adminUsers = new HashSet<String>();
    private List<String> displayNameShibbolethKeys = new ArrayList<String>();

    {//Default values
        displayNameShibbolethKeys.add("displayName");
        displayNameShibbolethKeys.add("commonName");
    }
    private final Map<String, String> schemaLocations = new HashMap<String, String>();

    {//Default values
        schemaLocations.put(ComponentSpec.class.getName(),
                "http://www.clarin.eu/cmd https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd");
    }
    private final static Configuration INSTANCE = new Configuration();

    private Configuration() {
    }

    public static Configuration getInstance() {
        return INSTANCE;
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

    public String getCcrRestUrl() {
        return ccrRestUrl;
    }

    public String getSchemaLocation(String key) {
        return schemaLocations.get(key);
    }

    public boolean isAdminUser(Principal principal) {
        if (principal != null) {
            return principal.getName().trim().length() > 0 // user name must be set (in case an empty entry is in admin users list)
                    && adminUsers.contains(principal.getName());
        }
        return false;
    }

    public boolean isAdminUser(String name) {
        if (name != null) {
            return name.trim().length() > 0 // user name must be set (in case an empty entry is in admin users list)
                    && adminUsers.contains(name);
        }
        return false;
    }

    public void setAdminUsers(Collection<String> adminUsers) {
        LOG.debug("Setting adminUsers to {}", Arrays.toString(adminUsers.toArray()));
        this.adminUsers = adminUsers;
    }

    /**
     *
     * @param adminUsers Whitespace-separated list of admin users
     */
    public void setAdminUsersList(String adminUsersList) {
        String[] adminUsersArray = adminUsersList.trim().split("\\s+");
        if (LOG.isDebugEnabled()) {
            LOG.info("Setting adminUsersList to {}", Arrays.toString(adminUsersArray));
        }
        setAdminUsers(Arrays.asList(adminUsersArray));
    }

    public void setComponent2SchemaXsl(String component2SchemaXsl) {
        LOG.info("Setting component2SchemaXsl to {}", component2SchemaXsl);
        this.component2SchemaXsl = component2SchemaXsl;
    }

    public void setComponentSpecSchemaLocation(String componentSpecSchemaLocation) {
        LOG.info("Setting componentSpecSchemaLocation to {}", componentSpecSchemaLocation);
        schemaLocations.put(ComponentSpec.class.getName(), componentSpecSchemaLocation);
    }

    public void setDisplayNameShibbolethKeys(List<String> displayNameShibbolethKeys) {
        LOG.info("Setting displayNameShibbolethKeys to {}", displayNameShibbolethKeys);
        this.displayNameShibbolethKeys = displayNameShibbolethKeys;
    }

    public void setGeneralComponentSchema(String generalComponentSchema) {
        LOG.info("Setting generalComponentSchema to {}", generalComponentSchema);
        this.generalComponentSchema = generalComponentSchema;
    }

    public void setCcrRestUrl(String ccrRestUrl) {
        LOG.info("Setting ccrRestUrl to {}", ccrRestUrl);
        this.ccrRestUrl = ccrRestUrl;
    }

    public String[] getAdminUsersArray() {
        return adminUsers.toArray(new String[0]);
    }
}
