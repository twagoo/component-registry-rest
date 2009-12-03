package clarin.cmdi.componentregistry.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistryImpl;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import clarin.cmdi.componentregistry.rest.ComponentRegistryRestService;

public class RegistryFiller {

    private final static Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private static final String REGISTRY_ROOT = "/Users/patdui/Workspace/Clarin/MyRegistry"; //"/tmp/componentRegistryTest";
    private ComponentRegistryRestService service;

    private static int failed = 0;

    RegistryFiller() {
        ComponentRegistryImpl registry = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        Configuration configuration = new Configuration();
        configuration.setRegistryRoot(new File(REGISTRY_ROOT));
        configuration.init();
        registry.setConfiguration(configuration);
        service = new ComponentRegistryRestService();
    }

    /**
     * RegistryFiller "P.Duin" "Test files" imdi -c /Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi/component*.xml
     * @param args
     */
    public static void main(String[] args) {
        LOG.info("RegistryFiller started with arguments: " + Arrays.toString(args));
        RegistryFiller filler = new RegistryFiller();
        String creatorName = args[0];
        String description = args[1];
        String group = args[2];
        boolean registerProfiles = "-p".equals(args[3]); //Otherwise -c
        for (int i = 4; i < args.length; i++) {
            File file = new File(args[i]);
            LOG.info("Registering " + (i - 3) + "/" +(args.length - 4) + ": "+file.getName());
            try {
                if (registerProfiles) {
                    filler.registerProfile(file, creatorName, description);
                } else {
                    filler.registerComponent(file, creatorName, description, group);
                }
            } catch (FileNotFoundException e) {
                failed++;
                LOG.error("Error in file: "+file, e);
            }
        }
        if (failed > 0) {
            LOG.error("Failed to register "+failed+" components/profiles.");
        } else {
            LOG.info("Everything registered ok.");
        }
    }

    private void registerProfile(File file, String creatorName, String description) throws FileNotFoundException {
        RegisterResponse registeredProfile = service.registeredProfile(new FileInputStream(file), file.getName(), creatorName, description);
        handleResult(registeredProfile);
    }

    private void registerComponent(File file, String creatorName, String description, String group) throws FileNotFoundException {
        RegisterResponse registeredComponent = service.registeredComponent(new FileInputStream(file), file.getName(), creatorName,
                description, group);
        handleResult(registeredComponent);
    }

    private void handleResult(RegisterResponse response) {
        if (response.isRegistered()) {
            LOG.info("Registration ok.");
        } else {
            failed++;
            LOG.error("Registration failed with the following errors:");
            for (int i = 0; i < response.getErrors().size(); i++) {
                LOG.error(response.getErrors().get(i));
            }
        }
    }

}
