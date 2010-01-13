package clarin.cmdi.componentregistry.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import clarin.cmdi.componentregistry.rest.ComponentRegistryRestService;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;


/**
 * Disclaimer:
 * This class is only usable to automate the initialisation of an ComponentRegistry. 
 * It fill the registry with components/profiles from the file system, it is not meant to be used as a quick help setting up a Clarin Registry. 
 * Use the registry web interface for "proper" adding of profiles/components.
 *
 */
public class RegistryFiller {

    private final static Logger LOG = LoggerFactory.getLogger(RegistryFiller.class);

    private static final File WORK_DIR = new File("/tmp/RegistryFiller");

    private WebResource service;

    private static int failed = 0;

    private Set<File> unresolvedFiles;

    private List<File> resolvedFiles;

    RegistryFiller() {
        WORK_DIR.mkdir();
        LOG.info("Will write resolved components in "+WORK_DIR);
        URI uri = UriBuilder.fromUri("http://localhost/").port(8080).path("ComponentRegistry/rest/registry").build();
        Client client = Client.create();
        service = client.resource(uri);
        unresolvedFiles = new HashSet<File>();
        resolvedFiles = new ArrayList<File>();
    }

    /**
     * RegistryFiller "P.Duin" "Test files" imdi -c /Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi/component*.xml
     * @param args
     * 
     * Uses a heuristic to resolve components which are linked together through fileName. 
     * It will try to find the component with a name equal to the filename (without extension) and set the registered id correct.
     * 
     */
    public static void main(String[] args)  {
        LOG.info("RegistryFiller started with arguments: " + Arrays.toString(args));
        if (args.length == 0 || args.length < 5) {
            printUsage();
        }

        RegistryFiller filler = new RegistryFiller();
        String creatorName = args[0];
        String description = args[1];
        String group = args[2];
        boolean registerProfiles = "-p".equals(args[3]); //Otherwise -c
        for (int i = 4; i < args.length; i++) {
            File file = new File(args[i]);
            LOG.info("Registering " + (i - 3) + "/" + (args.length - 4) + ": " + file.getName());
            try {
                if (registerProfiles) {
                    filler.registerProfile(file, creatorName, description);
                } else {
                    filler.replaceFileNameForIds(file);
                }
            } catch (Exception e) {
                failed++;
                LOG.error("Error in file: " + file, e);
            }
        }
        try {
            filler.registerComponents(creatorName, description, group);
        } catch (Exception e) {
            failed++;
            LOG.error("Error:", e);
        }
        if (failed > 0) {
            LOG.error("Failed to register " + failed + " components/profiles.");
        } else {
            LOG.info("Everything registered ok.");
        }
    }

    private void replaceFileNameForIds(File file) throws JAXBException, IOException {
        CMDComponentSpec comp = MDMarshaller.unmarshal(CMDComponentSpec.class, file);
        if (comp == null) {
            failed++;
            return;
        }
        List<CMDComponentType> cmdComponents = comp.getCMDComponent();
        boolean resolved = replaceFileNames(cmdComponents);
        if (!resolved) {
            unresolvedFiles.add(file);
        } else {
            File newFile = new File(WORK_DIR, file.getName());
            if (newFile.exists()) {
                newFile.delete();
            }
            newFile.createNewFile();
            MDMarshaller.marshal(comp, new FileOutputStream(newFile));
            resolvedFiles.add(newFile);
        }
    }

    private boolean replaceFileNames(List<CMDComponentType> cmdComponents) {
        boolean resolved = true;
        for (CMDComponentType cmdComponentType : cmdComponents) {
            if (cmdComponentType.getComponentId() == null && cmdComponentType.getName() == null) { //otherwise inline or top level component no need to do anything
                String name = FilenameUtils.getBaseName(cmdComponentType.getFilename());
                    List<ComponentDescription> descriptions = service.path("/components").get(new GenericType<List<ComponentDescription>>() {
                    });
                    for (ComponentDescription componentDescription : descriptions) {
                        if (componentDescription.getName().equals(name)) {
                            LOG.info("Replacing fileName: " + cmdComponentType.getFilename() + " with componentId: "
                                    + componentDescription.getId());
                            cmdComponentType.setComponentId(componentDescription.getId());
                            break;
                        }
                    }
                if (cmdComponentType.getComponentId() == null) {
                    resolved = false;
                }
            }
            if (resolved) {
                resolved = replaceFileNames(cmdComponentType.getCMDComponent());
            }
        }
        return resolved;
    }

    private void registerComponents(String creatorName, String description, String group) throws IOException, JAXBException {
        for (File file : resolvedFiles) {
            registerComponent(file, creatorName, description, group);
        }
        resolvedFiles = new ArrayList<File>();
        Set<File> unresolvedCopy = new HashSet<File>(unresolvedFiles);
        unresolvedFiles = new HashSet<File>();
        for (File file : unresolvedCopy) {
            replaceFileNameForIds(file);
        }
        if (unresolvedFiles.size() < unresolvedCopy.size()) {
            registerComponents(creatorName, description, group);// recursion
        } else if (!unresolvedFiles.isEmpty()){
            LOG.error("Cannot resolve nested components manual intervention is needed.");
            LOG.error("Files that could not be registered are:");
            for (File file : unresolvedFiles) {
                LOG.error("- "+file);
            }
            failed += unresolvedFiles.size();
        }
    }

    private void registerComponent(File file, String creatorName, String description, String group) throws IOException {
        FormDataMultiPart form = createForm(file, creatorName, description);
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, group);
        RegisterResponse response = service.path("/components").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        handleResult(response);
    }

    private static void printUsage() {
        System.out.println("usage: <creatorName> <description> <groupType> <-c|-p (components or profiles)> <xml file(s)>");
        System.exit(0);
    }

    private void registerProfile(File file, String creatorName, String description) throws IOException {
        FormDataMultiPart form = createForm(file, creatorName, description);
        RegisterResponse response = service.path("/profiles").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        handleResult(response);
    }

    private FormDataMultiPart createForm(File file, String creatorName, String description) throws IOException {
        FormDataMultiPart form = new FormDataMultiPart();
        Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); //To handle unicode chars.
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, new ByteArrayInputStream(IOUtils.toByteArray(reader, "UTF-8")),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, FilenameUtils.getBaseName(file.getName()));
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, description);
        form.field(ComponentRegistryRestService.CREATOR_NAME_FORM_FIELD, creatorName);
        return form;
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
