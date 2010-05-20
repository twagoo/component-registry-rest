package clarin.cmdi.componentregistry.tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ComponentRegistryImpl;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 * Can be used to migrate a registry from one location(Registry Dir) into another Registry by url.
 * 
 */
public class RegistryMigration {

    private final static Logger LOG = LoggerFactory.getLogger(RegistryMigration.class);
    private ComponentRegistryImpl registry;

    public RegistryMigration(String sourceDir) {
        registry = (ComponentRegistryImpl) ComponentRegistryFactory.getInstance().getPublicRegistry(); //TODO PD not using userRegistry here so won't work correctly anymore, I should do this in the admin page
    }

    private void migrate() throws IOException {
        RegistryFiller filler = new RegistryFiller(RegistryFiller.MIGRATION_URL_PROP);
        addComponents(filler);
        addProfiles(filler);
        int nrOfFailed = filler.register();
        if (nrOfFailed > 0) {
            LOG.error("Failed to migrate " + nrOfFailed + " components/profiles.");
        } else {
            LOG.info("Everything migrated ok.");
        }
    }

    private void addComponents(RegistryFiller filler) {
        List<ComponentDescription> descriptions = registry.getComponentDescriptions();
        for (ComponentDescription desc : descriptions) {
            File file = registry.getComponentFile(desc.getId());
            filler.addComponent(file, desc.getName(), desc.getDescription(), desc.getGroupName());
        }
    }

    private void addProfiles(RegistryFiller filler) {
        List<ProfileDescription> descriptions = registry.getProfileDescriptions();
        for (ProfileDescription desc : descriptions) {
            File file = registry.getProfileFile(desc.getId());
            filler.addProfile(file, desc.getName(), desc.getDescription(), "");
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        LOG.info("RegistryMigration started with arguments: " + Arrays.toString(args));
        if (args.length != 1) {
            printUsage();
        }
        new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" }); //loads and instantiates the beans
        RegistryMigration migration = new RegistryMigration(args[0]);
        migration.migrate();
    }

    private static void printUsage() {
        System.out.println("usage: configure registry.properties and applicationContext.xml "
                + "(applicationContext should contain the registryDir of the source url)");
        System.exit(0);
    }

}
