package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.CMDComponentSpecExpander;
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

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.impl.ComponentRegistryImplBase;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;

/**
 * Implementation of ComponentRegistry that uses Database Acces Objects for
 * accessing the registry (ergo: a database implementation)
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentRegistryDbImpl extends ComponentRegistryImplBase implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryDbImpl.class);
    private Number userId;
    @Autowired
    private Configuration configuration;
    @Autowired
    private ProfileDescriptionDao profileDescriptionDao;
    @Autowired
    private ComponentDescriptionDao componentDescriptionDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    @Qualifier("componentsCache")
    private CMDComponentSpecCache componentsCache;
    @Autowired
    @Qualifier("profilesCache")
    private CMDComponentSpecCache profilesCache;
    @Autowired
    private CommentsDao commentsDao;

    /**
     * Default constructor, makes this a (spring) bean. No user is set, so
     * public registry by default. Use setUser() to make it a user registry.
     * 
     * @see setUser
     */
    public ComponentRegistryDbImpl() {
    }

    /**
     * Creates a new ComponentRegistry (either public or not) for the provided
     * user
     * 
     * @param userId
     *            User id of the user to create registry for. Pass null for
     *            public
     */
    public ComponentRegistryDbImpl(Number userId) {
	this.userId = userId;
    }

    @Override
    public List<ProfileDescription> getProfileDescriptions() throws ComponentRegistryException {
	try {
	    if (isPublic()) {
		return profileDescriptionDao.getPublicProfileDescriptions();
	    } else {
		return profileDescriptionDao.getUserspaceDescriptions(getUserId());
	    }
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get profile descriptions", ex);
	}
    }

    @Override
    public ProfileDescription getProfileDescription(String id) throws ComponentRegistryException {
	try {
	    return profileDescriptionDao.getByCmdId(id, getUserId());
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get profile description", ex);
	}
    }

    @Override
    public List<ComponentDescription> getComponentDescriptions() throws ComponentRegistryException {
	try {
	    if (isPublic()) {
		return componentDescriptionDao.getPublicComponentDescriptions();
	    } else {
		return componentDescriptionDao.getUserspaceDescriptions(getUserId());
	    }
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get component descriptions", ex);
	}
    }

    @Override
    public ComponentDescription getComponentDescription(String id) throws ComponentRegistryException {
	try {
	    return componentDescriptionDao.getByCmdId(id, getUserId());
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get component description", ex);
	}
    }

    @Override
    public List<Comment> getCommentsInProfile(String profileId) throws ComponentRegistryException {
	try {
	    if (profileDescriptionDao.isInRegistry(profileId, getUserId())) {
		return commentsDao.getCommentsFromProfile(profileId);
	    } else {
		// Profile does not exist (at least not in this registry)
		throw new ComponentRegistryException("Profile " + profileId + " does not exist in specified registry");
	    }
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get list of comments from profile", ex);
	}
    }

    @Override
    public Comment getSpecifiedCommentInProfile(String profileId, String commentId) throws ComponentRegistryException {
	try {
	    Comment comment = commentsDao.getSpecifiedCommentFromProfile(commentId);
	    if (comment != null
		    && profileId.equals(comment.getProfileDescriptionId())
		    && profileDescriptionDao.isInRegistry(comment.getProfileDescriptionId(), getUserId())) {
		return comment;
	    } else {
		// Comment exists in DB, but profile is not in this registry
		throw new ComponentRegistryException("Comment " + commentId + " cannot be found in specified registry");
	    }
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get comment from profile", ex);
	}
    }

    @Override
    public List<Comment> getCommentsInComponent(String componentId) throws ComponentRegistryException {
	try {
	    if (componentDescriptionDao.isInRegistry(componentId, getUserId())) {
		return commentsDao.getCommentsFromComponent(componentId);
	    } else {
		// Component does not exist (at least not in this registry)
		throw new ComponentRegistryException("Component " + componentId + " does not exist in specified registry");
	    }
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get list of comments from component", ex);
	}
    }

    @Override
    public Comment getSpecifiedCommentInComponent(String componentId, String commentId) throws ComponentRegistryException {
	try {
	    Comment comment = commentsDao.getSpecifiedCommentFromComponent(commentId);
	    if (comment != null
		    && componentId.equals(comment.getComponentDescriptionId())
		    && componentDescriptionDao.isInRegistry(comment.getComponentDescriptionId(), getUserId())) {
		return comment;
	    } else {
		// Comment does not exists in DB or component is not in this registry
		throw new ComponentRegistryException("Comment " + commentId + " cannot be found in specified registry for specified component");
	    }
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get comment from component", ex);
	}
    }

    @Override
    public CMDComponentSpec getMDProfile(String id) throws ComponentRegistryException {
	if (inWorkspace(profileDescriptionDao, id)) {
	    CMDComponentSpec result = profilesCache.get(id);
	    if (result == null && !profilesCache.containsKey(id)) {
		result = getUncachedMDProfile(id);
		profilesCache.put(id, result);
	    }
	    return result;
	} else {
	    // May exist, but not in this workspace
	    return null;
	}
    }

    public CMDComponentSpec getUncachedMDProfile(String id) throws ComponentRegistryException {
	try {
	    return getUncachedMDComponent(id, profileDescriptionDao);
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get profile", ex);
	}
    }

    @Override
    public CMDComponentSpec getMDComponent(String id) throws ComponentRegistryException {
	if (inWorkspace(componentDescriptionDao, id)) {
	    CMDComponentSpec result = componentsCache.get(id);
	    if (result == null && !componentsCache.containsKey(id)) {
		result = getUncachedMDComponent(id);
		componentsCache.put(id, result);
	    }
	    return result;
	} else {
	    return null;
	}
    }

    public CMDComponentSpec getUncachedMDComponent(String id) throws ComponentRegistryException {
	try {
	    return getUncachedMDComponent(id, componentDescriptionDao);
	} catch (DataAccessException ex) {
	    throw new ComponentRegistryException("Database access error while trying to get component", ex);
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
	} catch (DataAccessException ex) {
	    LOG.error("Database error while registering component", ex);
	    return -1;
	} catch (JAXBException ex) {
	    LOG.error("Error while registering component", ex);
	    return -2;
	} catch (UnsupportedEncodingException ex) {
	    LOG.error("Error while registering component", ex);
	    return -3;
	}
    }

    @Override
    public int registerComment(Comment comment, String principalName) throws ComponentRegistryException {
	try {
	    if (comment.getComponentDescriptionId() != null && componentDescriptionDao.isInRegistry(comment.getComponentDescriptionId(), getUserId())
		    || comment.getProfileDescriptionId() != null && profileDescriptionDao.isInRegistry(comment.getProfileDescriptionId(), getUserId())) {
		// Convert principal name to user record id
		Number uid = convertUserIdInComment(principalName);
		if (uid != null) {
		    // Set user id in comment for convenience of calling method 
		    comment.setUserId(uid.toString());
		} else {
		    throw new ComponentRegistryException("Cannot find user with principal name: " + principalName);
		}
		// Set date to current date
		comment.setCommentDate(Comment.createNewDate());
		Number commentId = commentsDao.insertComment(comment, uid);
		comment.setId(commentId.toString());
	    } else {
		throw new ComponentRegistryException("Cannot insert comment into this registry. Unknown profileId or componentId");
	    }
	    return 0;
	} catch (DataAccessException ex) {
	    LOG.error("Database error while registering component", ex);
	    return -1;
	}
    }

    /**
     * Calling service sets user id to principle. Our task is to convert this to
     * an id for later reference. If none is set and this is a user's workspace,
     * set from that user's id.
     * 
     * @param description
     *            Description containing principle name as userId
     * @return Id (from database)
     * @throws DataAccessException
     */
    private Number convertUserIdInDescription(AbstractDescription description) throws DataAccessException {
	Number uid = null;
	if (description.getUserId() != null) {
	    RegistryUser user = userDao.getByPrincipalName(description.getUserId());
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

    /**
     * Calling service sets user id to principle. Our task is to convert this to
     * an id for later reference. If none is set and this is a user's workspace,
     * set from that user's id.
     * 
     * @param comment
     *            Comment containing principle name as userId
     * @return Id (from database)
     * @throws DataAccessException
     */
    private Number convertUserIdInComment(String principalName) throws DataAccessException {
	if (principalName != null) {
	    RegistryUser user = userDao.getByPrincipalName(principalName);
	    if (user != null) {
		return user.getId();
	    }
	}
	return null;
    }

    @Override
    public int update(AbstractDescription description, CMDComponentSpec spec, Principal principal, boolean forceUpdate) {
	try {
	    checkAuthorisation(description, principal);
	    checkAge(description, principal);
	    // For public components, check if used in other components or profiles (unless forced)
	    if (!forceUpdate && this.isPublic() && !description.isProfile()) {
		checkStillUsed(description.getId());
	    }
	    AbstractDescriptionDao<?> dao = getDaoForDescription(description);
	    dao.updateDescription(getIdForDescription(description), description, componentSpecToString(spec));
	    invalidateCache(description);
	    return 0;
	} catch (JAXBException ex) {
	    LOG.error("Error while updating component", ex);
	    return -1;
	} catch (UnsupportedEncodingException ex) {
	    LOG.error("Error while updating component", ex);
	    return -1;
	} catch (IllegalArgumentException ex) {
	    LOG.error("Error while updating component", ex);
	    return -1;
	} catch (UserUnauthorizedException e) {
	    LOG.error("Error while updating component", e);
	    return -1;
	} catch (DeleteFailedException e) {
	    LOG.error("Error while updating component", e);
	    return -1;
	} catch (ComponentRegistryException e) {
	    LOG.error("Error while updating component", e);
	    return -1;
	}
    }

    @Override
    public int publish(AbstractDescription desc, CMDComponentSpec spec, Principal principal) {
	int result = 0;
	AbstractDescriptionDao<?> dao = getDaoForDescription(desc);
	if (!isPublic()) { // if already in public workspace there is nothing todo
	    desc.setHref(AbstractDescription.createPublicHref(desc.getHref()));
	    Number id = getIdForDescription(desc);
	    try {
		// Update description & content
		dao.updateDescription(id, desc, componentSpecToString(spec));
		// Set to public
		dao.setPublished(id, true);
	    } catch (DataAccessException ex) {
		LOG.error("Database error while updating component", ex);
		return -1;
	    } catch (JAXBException ex) {
		LOG.error("Error while updating component", ex);
		return -2;
	    } catch (UnsupportedEncodingException ex) {
		LOG.error("Error while updating component", ex);
		return -3;
	    }
	}
	return result;
    }

    @Override
    public void getMDProfileAsXml(String profileId, OutputStream output) throws ComponentRegistryException {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderDbImpl.expandProfile(profileId, this);
	writeXml(expandedSpec, output);
    }

    @Override
    public void getMDProfileAsXsd(String profileId, OutputStream outputStream) throws ComponentRegistryException {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderDbImpl.expandProfile(profileId, this);
	writeXsd(expandedSpec, outputStream);
    }

    @Override
    public void getMDComponentAsXml(String componentId, OutputStream output) throws ComponentRegistryException {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderDbImpl.expandComponent(componentId, this);
	writeXml(expandedSpec, output);
    }

    @Override
    public void getMDComponentAsXsd(String componentId, OutputStream outputStream) throws ComponentRegistryException {
	CMDComponentSpec expandedSpec = CMDComponentSpecExpanderDbImpl.expandComponent(componentId, this);
	writeXsd(expandedSpec, outputStream);
    }

    @Override
    public void deleteMDProfile(String profileId, Principal principal) throws UserUnauthorizedException, DeleteFailedException,
	    ComponentRegistryException {
	ProfileDescription desc = getProfileDescription(profileId);
	if (desc != null) {
	    try {
		checkAuthorisation(desc, principal);
		checkAge(desc, principal);
		profileDescriptionDao.setDeleted(desc, true);
		invalidateCache(desc);
	    } catch (DataAccessException ex) {
		throw new DeleteFailedException("Database access error while trying to delete profile", ex);
	    }
	}
    }

    @Override
    public void deleteMDComponent(String componentId, Principal principal, boolean forceDelete) throws UserUnauthorizedException,
	    DeleteFailedException, ComponentRegistryException {
	ComponentDescription desc = componentDescriptionDao.getByCmdId(componentId);
	if (desc != null) {
	    try {
		checkAuthorisation(desc, principal);
		checkAge(desc, principal);

		if (!forceDelete) {
		    checkStillUsed(componentId);
		}
		componentDescriptionDao.setDeleted(desc, true);
		invalidateCache(desc);
	    } catch (DataAccessException ex) {
		throw new DeleteFailedException("Database access error while trying to delete component", ex);
	    }
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
     * @param User
     *            for which this should be the registry. Pass null for the
     *            public registry
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
     * 
     * @param description
     *            Description to look up
     * @return Database id for description
     * @throws IllegalArgumentException
     *             If description with non-existing id is passed
     */
    private Number getIdForDescription(AbstractDescription description) throws IllegalArgumentException {
	Number dbId = null;
	AbstractDescriptionDao<?> dao = getDaoForDescription(description);
	try {
	    dbId = dao.getDbId(description.getId());
	} catch (DataAccessException ex) {
	    LOG.error("Error getting dbId for component with id " + description.getId(), ex);
	}
	if (dbId == null) {
	    throw new IllegalArgumentException("Could not get database Id for description");
	} else {
	    return dbId;
	}
    }

    private String componentSpecToString(CMDComponentSpec spec) throws UnsupportedEncodingException, JAXBException {
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	MDMarshaller.marshal(spec, os);
	String xml = os.toString("UTF-8");
	return xml;
    }

    private CMDComponentSpec getUncachedMDComponent(String id, AbstractDescriptionDao dao) {
	String xml = dao.getContent(false, id);
	if (xml != null) {
	    try {
		InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		return MDMarshaller.unmarshal(CMDComponentSpec.class, is, null);

	    } catch (JAXBException ex) {
		LOG.error(null, ex);
	    } catch (UnsupportedEncodingException ex) {
		LOG.error(null, ex);
	    }
	}
	return null;
    }

    private void checkAuthorisation(AbstractDescription desc, Principal principal) throws UserUnauthorizedException {
	if (!isOwnerOfDescription(desc, principal.getName()) && !configuration.isAdminUser(principal)) {
	    throw new UserUnauthorizedException("Unauthorized operation user '" + principal.getName()
		    + "' is not the creator (nor an administrator) of the " + (desc.isProfile() ? "profile" : "component") + "(" + desc
		    + ").");
	}
    }

    private void checkAuthorisationComment(Comment desc, Principal principal) throws UserUnauthorizedException {
	if (!isOwnerOfComment(desc, principal.getName()) && !configuration.isAdminUser(principal)) {
	    throw new UserUnauthorizedException("Unauthorized operation user '" + principal.getName()
		    + "' is not the creator (nor an administrator) of the " + (desc.getId()) + "(" + desc
		    + ").");
	}
    }

    private boolean isOwnerOfDescription(AbstractDescription desc, String principalName) {
	String owner = getDaoForDescription(desc).getOwnerPrincipalName(getIdForDescription(desc));
	return owner != null // If owner is null, no one can be owner
		&& principalName.equals(owner);
    }

    private boolean isOwnerOfComment(Comment com, String principalName) {
	String owner = commentsDao.getOwnerPrincipalName(Integer.parseInt(com.getId()));
	return owner != null // If owner is null, no one can be owner
		&& principalName.equals(owner);
    }

    private void checkAge(AbstractDescription desc, Principal principal) throws DeleteFailedException {
	if (isPublic() && !configuration.isAdminUser(principal)) {
	    try {
		Date regDate = AbstractDescription.getDate(desc.getRegistrationDate());
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
		if (regDate.before(calendar.getTime())) { // More then month old
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

    private boolean inWorkspace(AbstractDescriptionDao<?> dao, String cmdId) {
	if (isPublic()) {
	    return dao.isPublic(cmdId);
	} else {
	    return dao.isInUserSpace(cmdId, getUserId());
	}
    }

    @Override
    public String getName() {
	if (isPublic()) {
	    return ComponentRegistry.PUBLIC_NAME;
	} else {
	    return "User " + getUserId() + " Registry";
	}
    }

    @Override
    public List<ProfileDescription> getDeletedProfileDescriptions() {
	return profileDescriptionDao.getDeletedDescriptions(getUserId());
    }

    @Override
    public List<ComponentDescription> getDeletedComponentDescriptions() {
	return componentDescriptionDao.getDeletedDescriptions(getUserId());
    }

    @Override
    public void deleteComment(String commentId, Principal principal) throws IOException,
	    ComponentRegistryException, UserUnauthorizedException, DeleteFailedException {
	try {
	    Comment comment = commentsDao.getById(commentId);
	    if (comment != null
		    // Comment must have an existing (in this registry) componentId or profileId
		    && (comment.getComponentDescriptionId() != null && componentDescriptionDao.isInRegistry(comment.getComponentDescriptionId(), getUserId())
		    || comment.getProfileDescriptionId() != null && profileDescriptionDao.isInRegistry(comment.getProfileDescriptionId(), getUserId()))) {
		checkAuthorisationComment(comment, principal);
		commentsDao.deleteComment(comment);
	    } else {
		// Comment exists in DB, but component is not in this registry
		throw new ComponentRegistryException("Comment " + commentId + " cannot be found in specified registry");
	    }
	} catch (DataAccessException ex) {
	    throw new DeleteFailedException("Database access error while trying to delete component", ex);
	}
    }

    @Override
    public CMDComponentSpecExpander getExpander() {
	return new CMDComponentSpecExpanderDbImpl(this);
    }
}
