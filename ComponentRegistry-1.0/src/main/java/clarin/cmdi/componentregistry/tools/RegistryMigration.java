package clarin.cmdi.componentregistry.tools;

import java.io.File;
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
    private final String targetUrl;
    private ComponentRegistryImpl registry;

    /*
     * http://lux16.mpi.nl:8080/ds/ComponentRegistry/rest/registry
     * 
     * /tmp/ComponentRegistry
     */
    public RegistryMigration(String sourceDir, String targetUrl) {
        this.targetUrl = targetUrl;
        config = new Configuration();
        config.setRegistryRoot(new File(sourceDir));
        config.init();
        registry = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        registry.setConfiguration(config);
    }

    private void migrate() {
        RegistryFiller filler = new RegistryFiller(targetUrl);
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
     */
    public static void main(String[] args) {
        LOG.info("RegistryMigration started with arguments: " + Arrays.toString(args));
        if (args.length != 2) {
            printUsage();
        }
        RegistryMigration migration = new RegistryMigration(args[0], args[1]);
        migration.migrate();
    }

    private static void printUsage() {
        System.out.println("usage: <source registry directory> <target registry URL>");
        System.exit(0);
    }

}
