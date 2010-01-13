package clarin.cmdi.componentregistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentSpec.Header;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class ComponentRegistryImpl implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryImpl.class);

    //bean will be injected
    private Configuration configuration;

    //cache fields
    private Map<String, ComponentDescription> componentDescriptions;
    private Map<String, ProfileDescription> profileDescriptions;
    private Map<String, CMDComponentSpec> componentsCache;
    private Map<String, CMDComponentSpec> profilesCache;

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
        LOG.info("Initializing cache..");
        LOG.info("CACHE: Reading and parsing all component descriptions.");
        this.componentDescriptions = loadComponentDescriptions();
        LOG.info("CACHE: Reading and parsing all profile descriptions.");
        this.profileDescriptions = loadProfileDescriptions();
        LOG.info("CACHE: Reading and parsing all components.");
        this.componentsCache = loadComponents();
        LOG.info("CACHE: Reading and parsing all profiles.");
        this.profilesCache = loadProfiles();
        LOG.info("CACHE: Loaded "+profileDescriptions.size()+" profile descriptions, "+profilesCache.size()+" profiles.");
        LOG.info("CACHE: Loaded "+componentDescriptions.size()+" components descriptions, "+componentsCache.size()+" components.");
        LOG.info("CACHE initialized. Any occured errors should be adressed, files could be corrupt."
                + " Components and Profiles with errors will not be shown to users.");
    }

    private Map<String, CMDComponentSpec> loadProfiles() {
        Map<String, CMDComponentSpec> result = new HashMap<String, CMDComponentSpec>();
        for (Iterator<String> iter = profileDescriptions.keySet().iterator(); iter.hasNext();) {
            String id = iter.next();
            File file = getProfileFile(id);
            CMDComponentSpec spec = MDMarshaller.unmarshal(CMDComponentSpec.class, file);
            if (spec != null) {
                result.put(id, spec);
            } else {
                iter.remove(); // cannot load actual profile so remove description from cache.
            }
        }
        return result;
    }

    private Map<String, CMDComponentSpec> loadComponents() {
        Map<String, CMDComponentSpec> result = new HashMap<String, CMDComponentSpec>();
        for (Iterator<String> iter = componentDescriptions.keySet().iterator(); iter.hasNext();) {
            String id = iter.next();
            File file = getComponentFile(id);
            CMDComponentSpec spec = MDMarshaller.unmarshal(CMDComponentSpec.class, file);
            if (spec != null) {
                result.put(id, spec);
            } else {
                iter.remove(); // cannot load actual component so remove description from cache.
            }
        }
        return result;
    }

    private Map<String, ProfileDescription> loadProfileDescriptions() {
        Collection files = FileUtils.listFiles(configuration.getProfileDir(), new WildcardFileFilter("description.xml"),
                TrueFileFilter.TRUE);
        Map<String, ProfileDescription> result = new HashMap<String, ProfileDescription>();
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            ProfileDescription desc = MDMarshaller.unmarshal(ProfileDescription.class, file);
            if (desc != null)
                result.put(desc.getId(), desc);
        }
        return result;
    }

    private Map<String, ComponentDescription> loadComponentDescriptions() {
        Collection files = FileUtils.listFiles(getComponentDir(), new WildcardFileFilter("description.xml"), TrueFileFilter.TRUE);
        Map<String, ComponentDescription> result = new HashMap<String, ComponentDescription>();
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            ComponentDescription desc = MDMarshaller.unmarshal(ComponentDescription.class, file);
            if (desc != null)
                result.put(desc.getId(), desc);
        }
        return result;
    }

    private void updateCache(CMDComponentSpec spec, AbstractDescription description) {
        if (description.isProfile()) {
            profileDescriptions.put(description.getId(), (ProfileDescription) description);
            profilesCache.put(description.getId(), spec);
        } else {
            componentDescriptions.put(description.getId(), (ComponentDescription) description);
            componentsCache.put(description.getId(), spec);
        }
    }

    private File getComponentDir() {
        return configuration.getComponentDir();
    }

    public List<ComponentDescription> getComponentDescriptions() {
        return new ArrayList(componentDescriptions.values());
    }

    public CMDComponentSpec getMDProfile(String profileId) {
        CMDComponentSpec result = profilesCache.get(profileId);
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
        return writer.toString();
    }

    private File getProfileFile(String profileId) {
        String id = stripRegistryId(profileId);
        File file = new File(configuration.getProfileDir(), id + File.separator + id + ".xml");
        return file;
    }

    public CMDComponentSpec getMDComponent(String componentId) {
        CMDComponentSpec result = componentsCache.get(componentId);
        return result;
    }

    public String getMDComponentAsXml(String componentId) {
        String result = null;
        File file = getComponentFile(componentId);
        try {
            result = IOUtils.toString(new FileInputStream(file), "UTF-8");
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
        return StringUtils.removeStart(id, REGISTRY_ID);
    }

    public List<ProfileDescription> getProfileDescriptions() {
        return new ArrayList(profileDescriptions.values());
    }

    /**
     * CMDComponentSpec and description are assumed to be valid.
     */
    public int registerMDComponent(ComponentDescription description, CMDComponentSpec spec) {
        LOG.info("Attempt to register component: " + description);
        return register(configuration.getComponentDir(), description, spec, "component");
    }

    /**
     * CMDComponentSpec and description are assumed to be valid.
     */
    public int registerMDProfile(ProfileDescription profileDescription, CMDComponentSpec spec) {
        LOG.info("Attempt to register profile: " + profileDescription);
        return register(configuration.getProfileDir(), profileDescription, spec, "profile");
    }

    private int register(File storageDir, AbstractDescription description, CMDComponentSpec spec, String type) {
        String strippedId = stripRegistryId(description.getId());
        File dir = new File(storageDir, strippedId);
        boolean success = false;
        try {
            boolean dirCreated = dir.mkdir();
            if (dirCreated) {
                writeDescription(dir, description);
                enrichSpecHeader(spec, description);
                writeCMDComponentSpec(dir, strippedId + ".xml", spec);
                success = true;
            }
        } catch (IOException e) {
            LOG.error("Register failed:", e);
        } catch (JAXBException e) {
            LOG.error("Register failed:", e);
        } finally {
            if (!success) { 
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
        updateCache(spec, description);
        return 0;
    }

    private void enrichSpecHeader(CMDComponentSpec spec, AbstractDescription description) {
        Header header = spec.getHeader();
        header.setID(description.getId());
        if (StringUtils.isEmpty(header.getName())) {
            header.setName(description.getName());
        }
        if (StringUtils.isEmpty(header.getDescription())) {
            header.setDescription(description.getDescription());
        }
        
    }

    private void writeDescription(File profileDir, AbstractDescription description) throws IOException, JAXBException {
        File metadataFile = new File(profileDir, "description.xml");
        FileOutputStream fos = new FileOutputStream(metadataFile);
        MDMarshaller.marshal(description, fos);
        LOG.info("Saving metadata is successful " + metadataFile);
    }

    private void writeCMDComponentSpec(File profileDir, String profileName, CMDComponentSpec spec) throws IOException, JAXBException {
        File file = new File(profileDir, profileName);
        FileOutputStream fos = new FileOutputStream(file);
        MDMarshaller.marshal(spec, fos);
        LOG.info("Saving profile/component is successful " + file);
    }

    public List<MDProfile> searchMDProfiles(String searchPattern) {
        return Collections.EMPTY_LIST;
    }

}
