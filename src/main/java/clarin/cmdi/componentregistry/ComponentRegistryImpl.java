package clarin.cmdi.componentregistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

    //TODO PD read in all files and marshall them, keep all in memory and check all on startup.

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
        initCache();
    }

    private void initCache() {

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
            spec = MDMarshaller.unmarshal(CMDComponentSpec.class, file);
            List<CMDComponentType> cmdComponents = spec.getCMDComponent();
            if (cmdComponents.size() != 1) {
                throw new RuntimeException("a component can consist of only one CMDComponent.");
            }
            result.add(new MDComponent(cmdComponents.get(0)));
        }
        return result;
    }

    public List<ComponentDescription> getComponentDescriptions() {
        Collection files = FileUtils.listFiles(getComponentDir(), new WildcardFileFilter("description.xml"), TrueFileFilter.TRUE);
        List<ComponentDescription> result = new ArrayList<ComponentDescription>();
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            ComponentDescription desc = MDMarshaller.unmarshal(ComponentDescription.class, file);
            if (desc != null)
                result.add(desc);
        }
        return result;
    }

    public CMDComponentSpec getMDProfile(String profileId) {
        CMDComponentSpec result = null;
        File file = getProfileFile(profileId);
        result = MDMarshaller.unmarshal(CMDComponentSpec.class, file);
        return result;
    }

    public String getMDProfileAsXml(String profileId) {
        String result = null;
        File file = getProfileFile(profileId);
        try {
            result = IOUtils.toString(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            LOG.error("Cannot retrieve file: " + file, e);
        } catch (IOException e) {
            LOG.error("Cannot retrieve content from file: " + file, e);
        }
        return result;
    }

    public String getMDProfileAsXsd(String profileId) {
        File file = getProfileFile(profileId);
        Writer writer = new StringWriter();
        MDMarshaller.generateXsd(file, writer);
        return writer.toString(); //TODO Patrick need to figure what happens with all the exceptions
    }

    private File getProfileFile(String profileId) {
        String id = stripRegistryId(profileId);
        File file = new File(configuration.getProfileDir(), id + File.separator + id + ".xml");
        return file;
    }

    public CMDComponentSpec getMDComponent(String componentId) {
        CMDComponentSpec result = null;
        String id = stripRegistryId(componentId);
        File file = new File(configuration.getComponentDir(), id + File.separator + id + ".xml");
        result = MDMarshaller.unmarshal(CMDComponentSpec.class, file);
        return result;
    }

    public String getMDComponentAsXml(String componentId) {
        String result = null;
        File file = getComponentFile(componentId);
        try {
            result = IOUtils.toString(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            LOG.error("Cannot retrieve file: " + file, e);
        } catch (IOException e) {
            LOG.error("Cannot retrieve content from file: " + file, e);
        }
        return result;
    }

    public String getMDComponentAsXsd(String componentId) {
        File file = getComponentFile(componentId);
        Writer writer = new StringWriter();
        MDMarshaller.generateXsd(file, writer);
        return writer.toString();
    }

    private File getComponentFile(String componentId) {
        String id = stripRegistryId(componentId);
        File file = new File(configuration.getComponentDir(), id + File.separator + id + ".xml");
        return file;
    }

    private String stripRegistryId(String id) {
        return StringUtils.removeStart(id, "clarin.eu:cr1:");
    }

    public List<MDComponent> getMDProfiles() {
        return Collections.EMPTY_LIST;
    }

    public List<ProfileDescription> getProfileDescriptions() {
        Collection files = FileUtils.listFiles(configuration.getProfileDir(), new WildcardFileFilter("description.xml"),
                TrueFileFilter.TRUE);
        List<ProfileDescription> result = new ArrayList<ProfileDescription>();
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            ProfileDescription desc = MDMarshaller.unmarshal(ProfileDescription.class, file);
            if (desc != null)
                result.add(desc);
        }
        return result;
    }

    /**
     * CMDComponentSpec and description are assumed to be valid.
     */
    public synchronized int registerMDComponent(ComponentDescription description, CMDComponentSpec spec) {
        LOG.info("Attempt to register component: " + description);
        return register(configuration.getComponentDir(), description, spec, "component");
    }

    /**
     * CMDComponentSpec and description are assumed to be valid.
     */
    public synchronized int registerMDProfile(ProfileDescription profileDescription, CMDComponentSpec spec) {
        LOG.info("Attempt to register profile: " + profileDescription);
        return register(configuration.getProfileDir(), profileDescription, spec, "profile");

    }

    private synchronized int register(File storageDir, AbstractDescription description, CMDComponentSpec spec, String type) {
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
                writeProfile(dir, description.getId() + ".xml", spec);
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
        MDMarshaller.marshal(description, writer);
        LOG.info("Saving metadata successful " + metadataFile);
    }

    private void writeProfile(File profileDir, String profileName, CMDComponentSpec spec) throws IOException, JAXBException {
        File file = new File(profileDir, profileName);
        MDMarshaller.marshal(spec, new FileWriter(file));
        LOG.info("Saving profile successful " + file);
    }

    public List<MDProfile> searchMDProfiles(String searchPattern) {
        return Collections.EMPTY_LIST;
    }

}
