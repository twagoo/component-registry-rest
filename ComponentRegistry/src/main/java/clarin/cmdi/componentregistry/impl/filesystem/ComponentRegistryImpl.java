package clarin.cmdi.componentregistry.impl.filesystem;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.impl.ComponentRegistryImplBase;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class ComponentRegistryImpl extends ComponentRegistryImplBase implements ComponentRegistry {

    public static final String DESCRIPTION_FILE_NAME = "description.xml";
    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryImpl.class);
    private ResourceConfig resourceConfig;
    //cache fields
    private Map<String, ComponentDescription> componentDescriptions;
    private Map<String, ProfileDescription> profileDescriptions;
    private Map<String, CMDComponentSpec> componentsCache;
    private Map<String, CMDComponentSpec> profilesCache;
    private final boolean isPublic;

    /*
     * Use the ComponentRegistryFactory
     */
    ComponentRegistryImpl(boolean isPublic) {
	this.isPublic = isPublic;
    }

    public void setResourceConfig(ResourceConfig resourceConfig) {
	this.resourceConfig = resourceConfig;
	initCache();
    }

    void initCache() {
	LOG.info("Initializing cache..");
	LOG.info("CACHE: Reading and parsing all component descriptions.");
	this.componentDescriptions = loadComponentDescriptions();
	LOG.info("CACHE: Reading and parsing all profile descriptions.");
	this.profileDescriptions = loadProfileDescriptions();
	LOG.info("CACHE: Loaded " + profileDescriptions.size() + " profile descriptions.");
	LOG.info("CACHE: Loaded " + componentDescriptions.size() + " components descriptions.");
	LOG.info("CACHE initialized. Any occured errors should be adressed, files could be corrupt."
		+ " Components and Profiles with errors will not be shown to users.");
	componentsCache = Collections.synchronizedMap(new LRUMap(100));
	profilesCache = Collections.synchronizedMap(new LRUMap(100));
    }

    CMDComponentSpec getUncachedProfile(String id) {
	File file = getProfileFile(id);
	CMDComponentSpec spec = null;
	if (file.exists()) {
	    spec = MDMarshaller.unmarshal(CMDComponentSpec.class, file, MDMarshaller.
		    getCMDComponentSchema());
	}
	return spec;
    }

    CMDComponentSpec getUncachedComponent(String id) {
	File file = getComponentFile(id);
	CMDComponentSpec spec = null;
	if (file.exists()) {
	    spec = MDMarshaller.unmarshal(CMDComponentSpec.class, file, MDMarshaller.
		    getCMDComponentSchema());
	}
	return spec;
    }

    private void hashUserId(AbstractDescription desc) {
	if (desc.getUserId() != null) {
	    desc.setUserId(UserCredentials.getPrincipalNameMD5Hex(desc.getUserId()));
	}
    }

    private Map<String, ProfileDescription> loadProfileDescriptions() {
	Collection files = FileUtils.listFiles(getProfileDir(), new WildcardFileFilter(DESCRIPTION_FILE_NAME), DIRS_WITH_DESCRIPTIONS);
	Map<String, ProfileDescription> result = new ConcurrentHashMap<String, ProfileDescription>();
	for (Iterator iterator = files.iterator(); iterator.hasNext();) {
	    File file = (File) iterator.next();
	    ProfileDescription desc = MDMarshaller.unmarshal(ProfileDescription.class, file, null);
	    if (desc != null) {
		result.put(desc.getId(), desc);
	    }
	}
	return result;
    }
    private final static IOFileFilter DIRS_WITH_DESCRIPTIONS = new NotFileFilter(new NameFileFilter(ResourceConfig.DELETED_DIR_NAME));

    private Map<String, ComponentDescription> loadComponentDescriptions() {
	Collection files = FileUtils.listFiles(getComponentDir(), new WildcardFileFilter(DESCRIPTION_FILE_NAME), DIRS_WITH_DESCRIPTIONS);
	Map<String, ComponentDescription> result = new ConcurrentHashMap<String, ComponentDescription>();
	for (Iterator iterator = files.iterator(); iterator.hasNext();) {
	    File file = (File) iterator.next();
	    ComponentDescription desc = MDMarshaller.unmarshal(ComponentDescription.class, file, null);
	    if (desc != null) {
		result.put(desc.getId(), desc);
	    }
	}
	return result;
    }

    private void updateCache(AbstractDescription description) {
	if (description.isProfile()) {
	    profileDescriptions.put(description.getId(), (ProfileDescription) description);
	    profilesCache.remove(description.getId());
	} else {
	    componentDescriptions.put(description.getId(), (ComponentDescription) description);
	    componentsCache.remove(description.getId());
	}
    }

    private File getComponentDir() {
	return resourceConfig.getComponentDir();
    }

    private File getProfileDir() {
	return resourceConfig.getProfileDir();
    }

    @Override
    public List<ComponentDescription> getComponentDescriptions() {
	List<ComponentDescription> result = new ArrayList<ComponentDescription>(componentDescriptions.
		values());
	Collections.sort(result, ComponentDescription.COMPARE_ON_GROUP_AND_NAME);
	return result;
    }

    @Override
    public ComponentDescription getComponentDescription(String id) {
	return componentDescriptions.get(id);
    }

    @Override
    public CMDComponentSpec getMDProfile(String profileId) {
	CMDComponentSpec result = profilesCache.get(profileId);
	if (result == null && !profilesCache.containsKey(profileId)) {
	    result = getUncachedProfile(profileId);
	    profilesCache.put(profileId, result);
	}
	return result;
    }

    @Override
    public void getMDProfileAsXml(String profileId, OutputStream output) throws ComponentRegistryException {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderImpl.
		expandProfile(profileId, this);
	writeXml(expandedSpec, output);
    }

    @Override
    public void getMDProfileAsXsd(String profileId, OutputStream outputStream) throws ComponentRegistryException {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderImpl.
		expandProfile(profileId, this);
	writeXsd(expandedSpec, outputStream);
    }

    private File getProfileFile(String profileId) {
	String id = stripRegistryId(profileId);
	File file = new File(getProfileDir(), id + File.separator + id + ".xml");
	return file;
    }

    @Override
    public CMDComponentSpec getMDComponent(String componentId) {
	CMDComponentSpec result = componentsCache.get(componentId);
	if (result == null && !componentsCache.containsKey(componentId)) {
	    result = getUncachedComponent(componentId);
	    componentsCache.put(componentId, result);
	}
	return result;
    }

    @Override
    public void getMDComponentAsXml(String componentId, OutputStream output) throws ComponentRegistryException {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderImpl.
		expandComponent(componentId, this);
	writeXml(expandedSpec, output);
    }

    @Override
    public void getMDComponentAsXsd(String componentId, OutputStream outputStream) throws ComponentRegistryException {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderImpl.
		expandComponent(componentId, this);
	writeXsd(expandedSpec, outputStream);
    }

    private File getComponentFile(String componentId) {
	String id = stripRegistryId(componentId);
	File file = new File(getComponentDir(), id + File.separator + id + ".xml");
	return file;
    }

    @Override
    public List<ProfileDescription> getProfileDescriptions() {
	List<ProfileDescription> result = new ArrayList<ProfileDescription>(profileDescriptions.
		values());
	Collections.sort(result, AbstractDescription.COMPARE_ON_NAME);
	return result;
    }

    @Override
    public ProfileDescription getProfileDescription(String id) {
	return profileDescriptions.get(id);
    }

    @Override
    public int publish(AbstractDescription desc, CMDComponentSpec spec, Principal principal) {
	int result = 0;
	if (!isPublic()) { //if already in public workspace there is nothing todo
	    try {
		if (desc.isProfile()) {
		    deleteMDProfile(desc.getId(), principal);
		} else {
		    deleteMDComponent(desc.getId(), principal, true);
		}
		desc.setHref(AbstractDescription.createPublicHref(desc.getHref()));
		desc.setUserId(principal.getName());
		result = ComponentRegistryFactoryImpl.getInstance().
			getPublicRegistry().register(desc, spec);
		//This is not nice this leaves us in a state where the spec can be deleted but not registered in public space.
		//NOTE deleted means it is moved to deleted directory, so an admin can still reach it.
		//In practice this will probably also not be so much of an issue. Nonetheless this screams for transactions and a database.
	    } catch (Exception e) {
		LOG.error("Delete failed:", e);
		result = -1;
	    }
	}
	return result;
    }

    /**
     * CMDComponentSpec and description are assumed to be valid.
     */
    @Override
    public int register(AbstractDescription desc, CMDComponentSpec spec) {
	LOG.info("Attempt to register " + desc.getType() + ": " + desc);
	hashUserId(desc);
	return register(getDir(desc), desc, spec, new RegisterClosureOnFail(desc));
    }

    @Override
    public int update(AbstractDescription desc, CMDComponentSpec spec, Principal principal, boolean forceUpdate) {
	LOG.info("Attempt to update " + desc.getType() + ": " + desc);
	return register(getDir(desc), desc, spec, new UpdateClosureOnFail(desc));
    }

    private File getDir(AbstractDescription desc) {
	return desc.isProfile() ? getProfileDir() : getComponentDir();
    }

    private int register(File storageDir, AbstractDescription description, CMDComponentSpec spec, Closure onFail) {
	String strippedId = stripRegistryId(description.getId());
	File dir = new File(storageDir, strippedId);
	boolean success = false;
	try {
	    boolean dirCreated = dir.mkdir();
	    if (dirCreated || dir.exists()) {
		writeDescription(dir, description);
		if (spec != null) {
		    enrichSpecHeader(spec, description);
		    writeCMDComponentSpec(dir, strippedId + ".xml", spec);
		}
		success = true;
	    }
	} catch (IOException e) {
	    LOG.error("Register failed:", e);
	} catch (JAXBException e) {
	    LOG.error("Register failed:", e);
	} finally {
	    if (!success) {
		onFail.execute(dir);
		return -1;
	    }
	}
	LOG.info("Succesfully registered/updated a " + description.getType() + " in " + dir + " " + description.
		getType() + "= "
		+ description);
	updateCache(description);
	return 0;
    }

    private void writeDescription(File dir, AbstractDescription description) throws IOException, JAXBException {
	File metadataFile = new File(dir, DESCRIPTION_FILE_NAME);
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

    @Override
    public void deleteMDProfile(String profileId, Principal principal) throws IOException, UserUnauthorizedException, DeleteFailedException {
	ProfileDescription desc = profileDescriptions.get(profileId);
	if (desc != null) {
	    checkAuthorisation(desc, principal);
	    checkAge(desc, principal);
	    File profileFile = getProfileFile(profileId);
	    if (profileFile.exists()) {
		FileUtils.moveDirectoryToDirectory(profileFile.getParentFile(), resourceConfig.
			getProfileDeletionDir(), true);
	    } // else no profile so nothing to delete
	    profileDescriptions.remove(profileId);
	    profilesCache.remove(profileId);
	}
    }

    private void checkAuthorisation(AbstractDescription desc, Principal principal) throws UserUnauthorizedException {
	if (!desc.isThisTheOwner(principal.getName()) && !Configuration.
		getInstance().isAdminUser(principal)) {
	    throw new UserUnauthorizedException("Unauthorized operation user '" + principal.
		    getName()
		    + "' is not the creator (nor an administrator) of the " + (desc.
		    isProfile() ? "profile" : "component") + "(" + desc
		    + ").");
	}
    }

    @Override
    public void deleteMDComponent(String componentId, Principal principal, boolean forceDelete) throws IOException,
	    UserUnauthorizedException, DeleteFailedException, ComponentRegistryException {
	ComponentDescription desc = componentDescriptions.get(componentId);
	if (desc != null) {
	    checkAuthorisation(desc, principal);
	    checkAge(desc, principal);
	    if (!forceDelete) {
		checkStillUsed(componentId);
	    }
	    File componentFile = getComponentFile(componentId);
	    if (componentFile.exists()) {
		FileUtils.moveDirectoryToDirectory(componentFile.getParentFile(), resourceConfig.
			getComponentDeletionDir(), true);
	    } // else no component so nothing to delete
	    componentDescriptions.remove(componentId);
	    componentsCache.remove(componentId);
	}
    }

    private void checkAge(AbstractDescription desc, Principal principal) throws DeleteFailedException {
	if (isPublic() && !Configuration.getInstance().isAdminUser(principal)) {
	    try {
		Date regDate = AbstractDescription.getDate(desc.
			getRegistrationDate());
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
		if (regDate.before(calendar.getTime())) { //More then month old
		    throw new DeleteFailedException(
			    "The "
			    + (desc.isProfile() ? "Profile" : "Component")
			    + " is more then a month old and cannot be deleted anymore. It might have been used to create metadata, deleting it would invalidate that metadata.");
		}
	    } catch (ParseException e) {
		LOG.error("Cannot parse date of " + desc + " Error:" + e);
	    }
	}
    }

    @Override
    public boolean isPublic() {
	return isPublic;
    }

    //'packaged' visibility does not belong in the api but I need to do this as admin.
    void emptyFromTrashcan(AbstractDescription description) throws IOException {
	File file = null;
	if (description.isProfile()) {
	    file = new File(resourceConfig.getProfileDeletionDir(), stripRegistryId(description.
		    getId()));
	} else {
	    file = new File(resourceConfig.getComponentDeletionDir(), stripRegistryId(description.
		    getId()));
	}
	if (file.exists()) {
	    FileUtils.deleteDirectory(file);
	}
    }

    private static class RegisterClosureOnFail implements Closure {

	private final AbstractDescription desc;

	RegisterClosureOnFail(AbstractDescription desc) {
	    this.desc = desc;
	}

	@Override
	public void execute(Object input) {
	    File dir = (File) input;
	    LOG.info("Registration of " + desc + " unsuccessful. Cleaning up created folders.");
	    try {
		FileUtils.deleteDirectory(dir);
	    } catch (IOException e) {
		LOG.error("Error in registration. Cleaning up " + desc.getId() + " failed: " + dir + " :", e);
	    }

	}
    }

    private static class UpdateClosureOnFail implements Closure {

	private final AbstractDescription desc;

	UpdateClosureOnFail(AbstractDescription desc) {
	    this.desc = desc;
	}

	@Override
	public void execute(Object input) {
	    LOG.info("Update of " + desc + " unsuccessful.");
	}
    }

    @Override
    public String getName() {
	if (isPublic()) {
	    return ComponentRegistry.PUBLIC_NAME;
	} else {
	    return "User " + getComponentDir().getParentFile().getName() + " Registry";
	}
    }

    @Override
    public List<ProfileDescription> getDeletedProfileDescriptions() {
	throw new NotImplementedException();
    }

    @Override
    public List<ComponentDescription> getDeletedComponentDescriptions() {
	throw new NotImplementedException();
    }

}
