package clarin.cmdi.componentregistry.tools;

import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryImpl;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;

/**
 * 
 * Used for migrating data inside one registry (For instance to change the format of the dates in descriptions).
 * 
 */
public class MigrateData {

    private final static Log LOG = LogFactory.getLog(MigrateData.class);

    private final static DateFormat OLD_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz", Locale.UK);

    private boolean hasErrors = false;

    public void migrateDescriptions(ComponentRegistry registry, Principal principal) {
        migrateDescriptions(registry, principal, registry.getComponentDescriptions());
        migrateDescriptions(registry, principal, registry.getProfileDescriptions());
    }

    /*
     * Transforms the "registrationDate" into the new UTC format. Also re-marshalls CMDComponentSpec's
     */
    private void migrateDescriptions(ComponentRegistry registry, Principal principal,
            List<? extends AbstractDescription> componentDescriptions) {
        for (AbstractDescription desc : componentDescriptions) {
            String oldDate = desc.getRegistrationDate();
            try {
                try {
                    //Date date = DateUtils.parseDate(oldDate, new String[] {DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()});
                    Date date = OLD_DATE_FORMAT.parse(oldDate);
                    String formatUTCDate = DateFormatUtils.formatUTC(date, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
                    desc.setRegistrationDate(formatUTCDate);
                } catch (ParseException e) {
                    hasErrors = true;
                    LOG.error("Cannot migrate date from description: " + desc + "\n error = ", e);
                }
                CMDComponentSpec spec;
                if (desc.isProfile()) {
                    spec = registry.getMDProfile(desc.getId());
                } else {
                    spec = registry.getMDComponent(desc.getId());
                }
                registry.update(desc, principal, spec);
            } catch (Exception e) {
                hasErrors = true;
                LOG.error("Cannot migrate unknown error: " + desc + "\n error = ", e);
            }
        }
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    private static void printUsage() {
        System.out.println("Usage: migrateData <userName>");
        System.out.println("Startup and use 'applicationContext.xml' in classpath to configure");
        System.exit(0);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            printUsage();
        }
        new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" }); //loads and instantiates the beans
        final String adminUser = args[0];
        ComponentRegistryImpl registry = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        Principal principal = new Principal() {
            public String getName() {
                return adminUser;
            }
        };
        MigrateData migrate = new MigrateData();
        migrate.migrateDescriptions(registry, principal);
    }

}
