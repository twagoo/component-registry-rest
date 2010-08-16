package clarin.cmdi.componentregistry.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.ComponentDescription;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * Disclaimer: This class is only usable to automate the initialisation of an ComponentRegistry. It fills the registry with
 * components/profiles from the file system, it is not meant to be used as a quick help setting up a Clarin Registry. Use the registry web
 * interface for "proper" adding of profiles/components.
 * 
 */
public class RegistryFiller {

    private final static Logger LOG = LoggerFactory.getLogger(RegistryFiller.class);

    private WebResource service;

    private int failed = 0;

    private Set<RegObject> unresolvedComponents;
    private Map<RegObject, CMDComponentSpec> resolvedComponents;

    private List<RegObject> profiles = new ArrayList<RegObject>();
    private List<RegObject> components = new ArrayList<RegObject>();

    private RegistryToolHelper helper;

    // Properties:
    //userName=tomcat 
    //password=tomcat
    //registryFillerUrl=http://localhost:8080/ComponentRegistry/rest/registry
    //registryMigrationUrl=http://lux16.mpi.nl:8080/ds/ComponentRegistry/rest/registry
    Properties properties = new Properties();
    static final String FILLER_URL_PROP = "registryFillerUrl";
    static final String MIGRATION_URL_PROP = "registryMigrationUrl";
    private static final String PASSWORD_PROP = "password";
    private static final String USER_NAME_PROP = "userName";

    public RegistryFiller(String urlPropName) throws IOException {
        URL urlResource = ClassLoader.getSystemResource("registry.properties");
        properties.load(urlResource.openStream());
        String url = properties.getProperty(urlPropName);
        URI uri = UriBuilder.fromUri(url).build();
        Client client = Client.create();
        service = client.resource(uri);
        unresolvedComponents = new HashSet<RegObject>();
        resolvedComponents = new HashMap<RegObject, CMDComponentSpec>();
        helper = new RegistryToolHelper(service, properties.getProperty(USER_NAME_PROP), properties.getProperty(PASSWORD_PROP));
    }

    /**
     * Uses a heuristic to resolve components which are linked together through fileName. It will try to find the component with a name
     * equal to the filename (without extension) and set the registered id correct.
     * @param args RegistryFiller "Test files" imdi /Users/patdui/Workspace/Clarin/metadata/toolkit/components/imdi/component*.xml
     * @throws IOException when properties cannot be loaded
     * 
     */
    public static void main(String[] args) throws IOException {
        LOG.info("RegistryFiller started with arguments: " + Arrays.toString(args));
        System.out.print("RegistryFiller is not working properly so not doing anything.");
//        if (args.length == 0 || args.length < 3) {
//            printUsage();
//        }
//        RegistryFiller filler = new RegistryFiller(FILLER_URL_PROP);
//        String description = args[0];
//        String group = args[1];
//        int nrOfFailed = 0;
//        for (int i = 2; i < args.length; i++) {
//            File file = new File(args[i]);
//            LOG.info("Adding " + (i - 3) + "/" + (args.length - 4) + ": " + file.getName());
//            try {
//                CMDComponentSpec spec = MDMarshaller.unmarshal(CMDComponentSpec.class, file, null);
//                if (spec.isIsProfile()) {
//                    filler.addProfile(file, FilenameUtils.getBaseName(file.getName()), description, group);
//                } else {
//                    filler.addComponent(file, FilenameUtils.getBaseName(file.getName()), description, group);
//                }
//            } catch (Exception e) {
//                nrOfFailed++;
//                LOG.error("Error in file: " + file, e);
//            }
//        }
//        nrOfFailed = filler.register();
//        if (nrOfFailed > 0) {
//            LOG.error("Failed to register " + nrOfFailed + " components/profiles.");
//        } else {
//            LOG.info("Everything registered ok.");
//        }
    }

    private static void printUsage() {
        System.out.println("usage:  <description> <groupType> <xml file(s)>");
        System.out.println("It also needs a filled in registry.properties");
        System.exit(0);
    }

    public void addProfile(File file, String name, String description, String group) {
        profiles.add(new RegObject(file, name, description, group));
    }

    public void addComponent(File file, String name, String description, String group) {
        components.add(new RegObject(file, name, description, group));
    }

    public int register() {
        LOG.info("Registering " + components.size() + " components ...");
        register(components, false);
        LOG.info("Registering " + profiles.size() + " profiles ...");
        register(profiles, true);
        failed += helper.getNrOfFailed();
        return failed;
    }

    private void register(List<RegObject> regObjects, boolean registerProfiles) {
        for (int i = 0; i < regObjects.size(); i++) {
            RegObject regObject = regObjects.get(i);
            LOG.info("Resolving " + (i + 1) + "/" + regObjects.size() + ": " + regObject.getName());
            try {
                replaceFileNameForIds(regObject);
            } catch (Exception e) {
                failed++;
                LOG.error("Error in file: " + regObject.getFile(), e);
            }
        }
        try {
            if (registerProfiles) {
                registerProfiles();
            } else {
                registerComponents();
            }
        } catch (Exception e) {
            failed++;
            LOG.error("Error:", e);
        }
    }

    private void registerProfiles() throws Exception {
        int i = 1;
        for (RegObject regObject : resolvedComponents.keySet()) {
            LOG.info("Registering " + i++ + "/" + resolvedComponents.size() + ": " + regObject.getName());
            CMDComponentSpec comp = resolvedComponents.get(regObject);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MDMarshaller.marshal(comp, out);
            ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
            helper.registerProfile(input, regObject.getDescription(), regObject.getName());
        }
        if (!unresolvedComponents.isEmpty()) {
            LOG.error("Cannot resolve all profiles manual intervention is needed.");
            LOG.error("Files that could not be registered are:");
            for (RegObject regObject : unresolvedComponents) {
                LOG.error("- " + regObject.getFile());
            }
            failed += unresolvedComponents.size();
        }
    }

    private void replaceFileNameForIds(RegObject regObject) throws JAXBException, IOException {
        CMDComponentSpec comp = MDMarshaller.unmarshal(CMDComponentSpec.class, regObject.getFile(), null);
        if (comp == null) {
            failed++;
            return;
        }
        List<CMDComponentType> cmdComponents = comp.getCMDComponent();
        boolean resolved = replaceFileNames(cmdComponents, regObject);
        if (!resolved) {
            unresolvedComponents.add(regObject);
        } else {
            resolvedComponents.put(regObject, comp);
        }
    }

    private boolean replaceFileNames(List<CMDComponentType> cmdComponents, RegObject regObject) {
        boolean resolved = true;
        List<ComponentDescription> descriptions = service.path("/components").get(new GenericType<List<ComponentDescription>>() {
        });
        for (CMDComponentType cmdComponentType : cmdComponents) {
            if (cmdComponentType.getFilename() != null) { //nested component so try to resolve the fileName to the id.
                String name = FilenameUtils.getBaseName(cmdComponentType.getFilename());
                List<ComponentDescription> matched = new ArrayList<ComponentDescription>();
                for (ComponentDescription componentDescription : descriptions) {
                    if (componentDescription.getName().equals(name)) {
                        matched.add(componentDescription);
                    }
                }
                if (!matched.isEmpty()) {
                    if (matched.size() == 1) {
                        ComponentDescription desc = matched.get(0);
                        setComponentId(cmdComponentType, desc);
                    } else {
                        LOG.info("Found multiple matching descriptions on name, making a best effort guess on group name.");
                        String group = guessGroupName(cmdComponentType, regObject);
                        for (ComponentDescription desc : matched) {
                            if (StringUtils.isEmpty(group) || group.equals(desc.getGroupName())) { //NOTE: assuming here name is unique which is not guaranteed. Need to check manually if actually correct. Should be ok most of the times.
                                setComponentId(cmdComponentType, desc);
                                break;
                            }
                        }
                    }
                }
                resolved &= componentExist(cmdComponentType.getComponentId());
            } else {
                resolved &= replaceFileNames(cmdComponentType.getCMDComponent(), regObject);//Recursion
            }
        }
        return resolved;
    }

    private void setComponentId(CMDComponentType cmdComponentType, ComponentDescription matched) {
        LOG.info("Replacing fileName: " + cmdComponentType.getFilename() + " with componentId: " + matched.getId());
        cmdComponentType.setComponentId(matched.getId());
    }

    private String guessGroupName(CMDComponentType cmdComponentType, RegObject regObject) {
        String result = FilenameUtils.getName(FilenameUtils.getFullPathNoEndSeparator(cmdComponentType.getFilename()));
        if (StringUtils.isEmpty(result)) {
            result = regObject.getGroup();
        } else if (result.equals("..")) {
            result = regObject.getGroup().split("/")[0]; //Assuming someone uses as group for instance: clarin/webservices
        }
        return result;
    }

    private boolean componentExist(String id) {
        if (id == null) {
            return false;
        }
        try {
            service.path("/components/" + id).get(CMDComponentSpec.class);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() != HttpURLConnection.HTTP_NO_CONTENT) {
                LOG.error("Unexpected exception while getting component: ", e);
            }
            return false;
        }
        return true;
    }

    private void registerComponents() throws IOException, JAXBException {
        int i = 1;
        for (RegObject regObject : resolvedComponents.keySet()) {
            LOG.info("Registering " + i++ + "/" + resolvedComponents.size() + ": " + regObject.getName());
            CMDComponentSpec comp = resolvedComponents.get(regObject);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MDMarshaller.marshal(comp, out);
            ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
            helper.registerComponent(input, regObject.getDescription(), regObject.getGroup(), regObject.getName());
        }
        resolvedComponents = new HashMap<RegObject, CMDComponentSpec>();
        Set<RegObject> unresolvedCopy = new HashSet<RegObject>(unresolvedComponents);
        unresolvedComponents = new HashSet<RegObject>();
        for (RegObject regObject : unresolvedCopy) {
            replaceFileNameForIds(regObject);
        }
        if (unresolvedComponents.size() < unresolvedCopy.size()) {
            registerComponents();// recursion
        } else if (!unresolvedComponents.isEmpty()) {
            LOG.error("Cannot resolve nested components manual intervention is needed.");
            LOG.error("Files that could not be registered are:");
            for (RegObject regObject : unresolvedComponents) {
                LOG.error("- " + regObject.getFile());
            }
            failed += unresolvedComponents.size();
        }
    }

    //    private InputStream createInputStream(File file) throws IOException {
    //        Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); //To handle unicode chars.
    //        InputStream input = new ByteArrayInputStream(IOUtils.toByteArray(reader, "UTF-8"));
    //        return input;
    //    }

    private class RegObject {

        private final File file;
        private final String name;
        private final String description;
        private final String group;

        public RegObject(File file, String name, String description, String group) {
            this.file = file;
            this.name = name;
            this.description = description;
            this.group = group;
        }

        public File getFile() {
            return file;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getGroup() {
            return group;
        }

        @Override
        public int hashCode() {
            return file.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RegObject) {
                RegObject other = (RegObject) obj;
                return file.equals(other.getFile());
            }
            return false;
        }

        @Override
        public String toString() {
            return "name= " + name + "\t file= " + file.toString();
        }
    }

}
