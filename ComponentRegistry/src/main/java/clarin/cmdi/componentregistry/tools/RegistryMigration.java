package clarin.cmdi.componentregistry.tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistryImpl;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

/**
 * Can be used to migrate a registry from one location(Registry Dir) into another Registry by url.
 * 
 */
public class RegistryMigration {

    private final static Logger LOG = LoggerFactory.getLogger(RegistryMigration.class);
    private Configuration config;
    private ComponentRegistryImpl registry;

    /*
     * /tmp/ComponentRegistry
     */
    public RegistryMigration(String sourceDir) {
        config = new Configuration();
        config.setRegistryRoot(new File(sourceDir));
        config.init();
        registry = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        registry.setConfiguration(config);
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
            filler.addComponent(file, desc.getName(), desc.getCreatorName(), desc.getDescription(), desc.getGroupName());
        }
    }

    private void addProfiles(RegistryFiller filler) {
        List<ProfileDescription> descriptions = registry.getProfileDescriptions();
        for (ProfileDescription desc : descriptions) {
            File file = registry.getProfileFile(desc.getId());
            filler.addProfile(file, desc.getName(), desc.getCreatorName(), desc.getDescription(), "");
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
        RegistryMigration migration = new RegistryMigration(args[0]);
        migration.migrate();
    }

    private static void printUsage() {
        System.out.println("usage: <source registry directory>");
        System.out.println("It also needs a filled in registry.properties");
        System.exit(0);
    }

}
