package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.impl.ComponentRegistryImplBase;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.UserMapping.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;

/**
 * Implementation of ComponentRegistry that uses Database Acces Objects for
 * accessing the registry (ergo: a database implementation)
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryDbImpl extends ComponentRegistryImplBase implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryDbImpl.class);
    private Number userId;
    @Autowired
    private ProfileDescriptionDao profileDescriptionDao;
    @Autowired
    private ComponentDescriptionDao componentDescriptionDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    Configuration configuration;
    @Autowired
    @Qualifier("componentsCache")
    CMDComponentSpecCache componentsCache;
    @Autowired
    @Qualifier("profilesCache")
    CMDComponentSpecCache profilesCache;

    /**
     * Default constructor, makes this a (spring) bean. No user is set, so
     * public registry by default. Use setUser() to make it a user registry.
     * @see setUser
     */
    public ComponentRegistryDbImpl() {
    }

    /**
     * Creates a new ComponentRegistry (either public or not) for the provided user
     * @param userId User id of the user to create registry for. Pass null for public
     */
    public ComponentRegistryDbImpl(Number userId) {
	this.userId = userId;
    }

    @Override
    public List<ProfileDescription> getProfileDescriptions() {
	try {
	    if (isPublic()) {
		return profileDescriptionDao.getPublicProfileDescriptions();
	    } else {
		return profileDescriptionDao.getUserspaceDescriptions(getUserId());
	    }
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to get profile descriptions", ex);
	    throw ex;
	}
    }

    @Override
    public ProfileDescription getProfileDescription(String id) {
	try {
	    return profileDescriptionDao.getByCmdId(id);
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to get profile description", ex);
	    throw ex;
	}
    }

    @Override
    public List<ComponentDescription> getComponentDescriptions() {
	try {
	    if (isPublic()) {
		return componentDescriptionDao.getPublicComponentDescriptions();
	    } else {
		return componentDescriptionDao.getUserspaceDescriptions(getUserId());
	    }
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to get component descriptions", ex);
	    throw ex;
	}
    }

    @Override
    public ComponentDescription getComponentDescription(String id) {
	try {
	    return componentDescriptionDao.getByCmdId(id);
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to get component description", ex);
	    throw ex;
	}
    }

    @Override
    public CMDComponentSpec getMDProfile(String id) {
	CMDComponentSpec result = profilesCache.get(id);
	if (result == null && !profilesCache.containsKey(id)) {
	    result = getUncachedMDProfile(id);
	    profilesCache.put(id, result);
	}
	return result;
    }

    public CMDComponentSpec getUncachedMDProfile(String id) {
	try {
	    return getUncachedMDComponent(id, profileDescriptionDao);
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to get profile", ex);
	    throw ex;
	}
    }

    @Override
    public CMDComponentSpec getMDComponent(String id) {
	CMDComponentSpec result = componentsCache.get(id);
	if (result == null && !componentsCache.containsKey(id)) {
	    result = getUncachedMDComponent(id);
	    componentsCache.put(id, result);
	}
	return result;
    }

    public CMDComponentSpec getUncachedMDComponent(String id) {
	try {
	    return getUncachedMDComponent(id, componentDescriptionDao);
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to get component", ex);
	    throw ex;
	}
    }

    @Override
    public int register(AbstractDescription description, CMDComponentSpec spec) {
	enrichSpecHeader(spec, description);
	try {
	    String xml = componentSpecToString(spec);
	    // Convert principal name to user record id
	    Number uid = convertUserIdInDescription(description);
	    getDaoForDescription(description).insertDescription(description, xml, isPublic(), uid);
	    invalidateCache(description);
	    return 0;
	} catch (JAXBException ex) {
	    LOG.error(null, ex);
	} catch (UnsupportedEncodingException ex) {
	    LOG.error(null, ex);
	} catch (DataAccessException ex) {
	    LOG.error("Database error while registering component", ex);
	}
	return -1;
    }

    /**
     * Calling service sets user id to principle. Our task is to convert this to
     * an id for later reference. If none is set and this is a user's workspace,
     * set from that user's id.
     * @param description Description containing principle name as userId
     * @return Id (from database)
     * @throws DataAccessException
     */
    private Number convertUserIdInDescription(AbstractDescription description) throws DataAccessException {
	Number uid = null;
	if (description.getUserId() != null) {
	    User user = userDao.getByPrincipalName(description.getUserId());
	    if (user != null) {
		uid = user.getId();
	    }
	} else {
	    uid = userId;
	}
	if (uid != null) {
	    description.setUserId(uid.toString());
	}
	return uid;
    }

    @Override
    public int update(AbstractDescription description, CMDComponentSpec spec) {
	try {
	    AbstractDescriptionDao<?> dao = getDaoForDescription(description);
	    dao.updateDescription(getIdForDescription(description), description, componentSpecToString(spec));
	    invalidateCache(description);
	    return 0;
	} catch (DataAccessException ex) {
	    LOG.error(null, ex);
	    return -1;
	} catch (IllegalArgumentException ex) {
	    LOG.error(null, ex);
	    return -2;
	} catch (JAXBException ex) {
	    LOG.error(null, ex);
	    return -3;
	} catch (UnsupportedEncodingException ex) {
	    LOG.error(null, ex);
	    return -4;
	}
    }

    @Override
    public int publish(AbstractDescription desc, CMDComponentSpec spec, Principal principal) {
	int result = 0;
	AbstractDescriptionDao<?> dao = getDaoForDescription(desc);
	if (!isPublic()) { //if already in public workspace there is nothing todo
	    try {
		desc.setHref(AbstractDescription.createPublicHref(desc.getHref()));
		dao.setPublished(getIdForDescription(desc), true);
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

    @Override
    public void getMDProfileAsXml(String profileId, OutputStream output) {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderDbImpl.
		expandProfile(profileId, this);
	writeXml(expandedSpec, output);
    }

    @Override
    public void getMDProfileAsXsd(String profileId, OutputStream outputStream) {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderDbImpl.
		expandProfile(profileId, this);
	writeXsd(expandedSpec, outputStream);
    }

    @Override
    public void getMDComponentAsXml(String componentId, OutputStream output) {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderDbImpl.
		expandComponent(componentId, this);
	writeXml(expandedSpec, output);
    }

    @Override
    public void getMDComponentAsXsd(String componentId, OutputStream outputStream) {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderDbImpl.
		expandComponent(componentId, this);
	writeXsd(expandedSpec, outputStream);
    }

    @Override
    public void deleteMDProfile(String profileId, Principal principal) throws IOException, UserUnauthorizedException, DeleteFailedException {
	try {
	    ProfileDescription desc = getProfileDescription(profileId);
	    if (desc != null) {
		checkAuthorisation(desc, principal);
		checkAge(desc, principal);
		profileDescriptionDao.setDeleted(getIdForDescription(desc));
		invalidateCache(desc);
	    }
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to delete profile", ex);
	    throw ex;
	}

    }

    @Override
    public void deleteMDComponent(String componentId, Principal principal, boolean forceDelete) throws IOException, UserUnauthorizedException, DeleteFailedException {
	try {
	    ComponentDescription desc = componentDescriptionDao.getByCmdId(componentId);
	    if (desc != null) {
		checkAuthorisation(desc, principal);
		checkAge(desc, principal);

		if (!forceDelete) {
		    checkStillUsed(componentId);
		}
		componentDescriptionDao.setDeleted(getIdForDescription(desc));
		invalidateCache(desc);
	    }
	} catch (DataAccessException ex) {
	    LOG.error("Database access error while trying to delete component", ex);
	    throw ex;
	}
    }

    @Override
    public boolean isPublic() {
	return null == userId;
    }

    public void setPublic() {
	this.userId = null;
    }

    /**
     * @return The user, or null if this is the public registry.
     */
    public Number getUserId() {
	return userId;
    }

    /**
     * @param User for which this should be the registry. Pass null for
     * the public registry
     */
    public void setUserId(Number user) {
	this.userId = user;
    }

    private void invalidateCache(AbstractDescription description) {
	if (description.isProfile()) {
	    profilesCache.remove(description.getId());
	} else {
	    componentsCache.remove(description.getId());
	}
    }

    private AbstractDescriptionDao<?> getDaoForDescription(AbstractDescription description) {
	return description.isProfile() ? profileDescriptionDao : componentDescriptionDao;
    }

    /**
     * Looks up description on basis of CMD Id. This will also check if such a
     * record even exists.
     * @param description Description to look up
     * @return Database id for description
     * @throws IllegalArgumentException If description with non-existing id is passed
     */
    private Number getIdForDescription(AbstractDescription description) throws IllegalArgumentException {
	Number dbId = null;
	AbstractDescriptionDao<?> dao = getDaoForDescription(description);
	try {
	    dbId = dao.getDbId(description.getId());
	} catch (DataAccessException ex) {
	    LOG.error("Error getting dbId for component with id "
		    + description.getId(), ex);
	}
	if (dbId == null) {
	    throw new IllegalArgumentException("Could not get database Id for description");
	} else {
	    return dbId;
	}
    }

    private String componentSpecToString(CMDComponentSpec spec) throws UnsupportedEncodingException, JAXBException {
	OutputStream os = new ByteArrayOutputStream();
	MDMarshaller.marshal(spec, os);
	String xml = os.toString();
	return xml;
    }

    private CMDComponentSpec getUncachedMDComponent(String id, AbstractDescriptionDao dao) {
	String xml = dao.getContent(id);
	if (xml != null) {
	    try {
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		return MDMarshaller.unmarshal(CMDComponentSpec.class, is, MDMarshaller.
			getCMDComponentSchema());
	    } catch (JAXBException ex) {
		LOG.error(null, ex);
	    }
	}
	return null;
    }

    private void checkAuthorisation(AbstractDescription desc, Principal principal) throws UserUnauthorizedException {
	if (!isOwnerOfDescription(desc, principal.getName())
		&& !configuration.isAdminUser(principal)) {
	    throw new UserUnauthorizedException("Unauthorized operation user '" + principal.
		    getName()
		    + "' is not the creator (nor an administrator) of the " + (desc.
		    isProfile() ? "profile" : "component") + "(" + desc
		    + ").");
	}
    }

    private boolean isOwnerOfDescription(AbstractDescription desc, String principalName) {
	String owner = getDaoForDescription(desc).getOwnerPrincipalName(getIdForDescription(desc));
	return owner != null // If owner is null, no one can be owner
		&& principalName.equals(owner);
    }

    private void checkAge(AbstractDescription desc, Principal principal) throws DeleteFailedException {
	if (isPublic() && !configuration.isAdminUser(principal)) {
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
    public String getName() {
	if (isPublic()) {
	    return "Public Registry";
	} else {
	    return "User " + getUserId() + " Registry";
	}
    }
}
