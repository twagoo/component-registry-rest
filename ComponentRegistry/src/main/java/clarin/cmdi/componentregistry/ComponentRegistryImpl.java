package clarin.cmdi.componentregistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class ComponentRegistryImpl implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryImpl.class);

    //bean will be injected
    private Configuration configuration;

    private final static ComponentRegistry INSTANCE = new ComponentRegistryImpl();

    private ComponentRegistryImpl() {
    }

    public static ComponentRegistry getInstance() {
        return INSTANCE;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private File getComponentDir() {
        return configuration.getComponentDir();
    }

    public List<MDComponent> getMDComponents() {
        Collection files = FileUtils.listFiles(getComponentDir(), new WildcardFileFilter("component*.xml"), TrueFileFilter.TRUE);
        List<MDComponent> result = new ArrayList<MDComponent>();
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            CMDComponentSpec spec;
            try {
                spec = MDMarshaller.unmarshal(CMDComponentSpec.class, new FileInputStream(file));
                List<CMDComponentType> cmdComponents = spec.getCMDComponent();
                if (cmdComponents.size() != 1) {
                    throw new RuntimeException("a component can consist of only one CMDComponent.");
                }
                result.add(new MDComponent(cmdComponents.get(0)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public List<String> getComponentDescriptions() {
        Collection files = FileUtils.listFiles(getComponentDir(), new WildcardFileFilter("description.xml"), TrueFileFilter.TRUE);
        List<String> result = new ArrayList<String>();
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            try {
                result.add(FileUtils.readFileToString(file));
            } catch (IOException e) {
                LOG.error("Cannot retrieve component description", e);
            }
        }
        return result;
    }

    public String getMDProfile(String profileId) {
        String result = null;
        String id = stripRegistryId(profileId);
        File file = new File(configuration.getProfileDir(), id + File.separator + id + ".xml");
        try {
            result = FileUtils.readFileToString(file);
        } catch (IOException e) {
            LOG.error("Cannot retrieve profile", e);
        }
        return result;
    }

    public String getMDComponent(String componentId) {
        String result = null;
        String id = stripRegistryId(componentId);
        File file = new File(configuration.getComponentDir(), id + File.separator + id + ".xml");
        try {
            result = FileUtils.readFileToString(file);
        } catch (IOException e) {
            LOG.error("Cannot retrieve profile", e);
        }
        return result;
    }

    private String stripRegistryId(String id) {
        return StringUtils.removeStart(id, "clarin.eu:cr1:");
    }

    public List<MDComponent> getMDProfiles() {
        return Collections.EMPTY_LIST;
    }

    public List<String> getProfileDescriptions() {
        Collection files = FileUtils.listFiles(configuration.getProfileDir(), new WildcardFileFilter("description.xml"),
                TrueFileFilter.TRUE);
        List<String> result = new ArrayList<String>();
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            try {
                result.add(FileUtils.readFileToString(file));
            } catch (IOException e) {
                LOG.error("Cannot retrieve profile description", e);
            }
        }
        return result;
    }

    public int registerMDComponent(ComponentDescription description, String content) {
        LOG.info("Attempt to register component: " + description);
        return register(configuration.getComponentDir(), description, content, "component");
    }

    public int registerMDProfile(ProfileDescription profileDescription, String profileContent) {
        LOG.info("Attempt to register profile: " + profileDescription);
        return register(configuration.getProfileDir(), profileDescription, profileContent, "profile");

    }

    private int register(File storageDir, AbstractDescription description, String content, String type) {
        //Check if name not already exists, create profile dir put store all files.
        //Handle all errors and rollback if something didn't work. Put this all in a separate DAO or something
        //Create storage package
        //Create id, creationdate etc...
        String id = description.getId();
        File dir = new File(storageDir, id);
        boolean success = false;
        try {
            boolean dirCreated = dir.mkdir(); //Check if file is not there already TODO Patrick
            if (dirCreated) {
                writeDescription(dir, description);
                writeProfile(dir, description.getId() + ".xml", content);
                success = true;
            }
        } catch (IOException e) {
            LOG.error("Register failed:", e);
        } catch (JAXBException e) {
            LOG.error("Register failed:", e);
        } finally {
            if (!success) { //TODO Patrick make a test for this also what about synchronisation?
                LOG.info("Registration of " + type + " " + description + " unsuccessful. Cleaning up created folders.");
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    LOG.error("Error in registration. Cleaning up " + type + " failed: " + dir + " :", e);
                }
                return -1;
            }
        }
        LOG.info("Succesfully registered a " + type + " in " + dir + " " + type + "= " + description);
        return 0;

    }

    private void writeDescription(File profileDir, AbstractDescription description) throws IOException, JAXBException {
        File metadataFile = new File(profileDir, "description.xml");
        Writer writer = new FileWriter(metadataFile);
        MDMarshaller.marshall(description, writer);
        LOG.info("Saving metadata successful " + metadataFile);
    }

    private void writeProfile(File profileDir, String profileName, String content) throws IOException {
        File file = new File(profileDir, profileName);
        FileUtils.writeStringToFile(file, content, "UTF-8");
        LOG.info("Saving profile successful " + file);
    }

    public List<MDProfile> searchMDProfiles(String searchPattern) {
        return Collections.EMPTY_LIST;
    }

}
