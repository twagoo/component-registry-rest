package clarin.cmdi.componentregistry.tools;

import java.io.File;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryImpl;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.model.AbstractDescription;

public class MigrateData {

    private final static Log LOG = LogFactory.getLog(MigrateData.class);

    private final static DateFormat OLD_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz", Locale.UK);
    
    private boolean hasErrors = false;

    public void migrateDescriptions(ComponentRegistry registry, Principal principal) {
        migrateDescriptions(registry, principal, registry.getComponentDescriptions());
        migrateDescriptions(registry, principal, registry.getProfileDescriptions());
    }

    /*
     * Transforms the "registrationDate" into the new UTC format.
     */
    private void migrateDescriptions(ComponentRegistry registry, Principal principal,
            List<? extends AbstractDescription> componentDescriptions) {
        for (AbstractDescription desc : componentDescriptions) {
            String oldDate = desc.getRegistrationDate();
            try {
                //Date date = DateUtils.parseDate(oldDate, new String[] {DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()});
                Date date = OLD_DATE_FORMAT.parse(oldDate);
                String formatUTCDate = DateFormatUtils.formatUTC(date, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
                desc.setRegistrationDate(formatUTCDate);
                registry.updateDescription(desc, principal);
            } catch (Exception e) {
                hasErrors = true;
                LOG.error("Cannot migrate date from description: " + desc + "\n error = ", e);
            }
        }
    }
    
    public boolean hasErrors() {
        return hasErrors;
    }

    private static void printUsage() {
        System.out.println("Usage: migrateDate <registryRootDir> <adminUserName>");
        System.exit(0);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        LOG.info("Started with: " + Arrays.toString(args));
        if (args.length != 2) {
            printUsage();
        }
        String registryRoot = args[0];
        final String adminUser = args[1];
        Configuration config = new Configuration();
        config.setRegistryRoot(new File(registryRoot));
        Set<String> adminUsers = new HashSet<String>();
        adminUsers.add(adminUser);
        config.setAdminUsers(adminUsers);
        config.init();
        ComponentRegistryImpl registry = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        registry.setConfiguration(config);
        Principal principal = new Principal() {
            public String getName() {
                return adminUser;
            }
        };
        MigrateData migrate = new MigrateData();
        migrate.migrateDescriptions(registry, principal);
    }

}
