package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.CMDComponentSpecExpander;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.Owner;
import clarin.cmdi.componentregistry.OwnerUser;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.impl.ComponentRegistryImplBase;
import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.CommentsDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ComponentRegistry that uses Database Acces Objects for
 * accessing the registry (ergo: a database implementation)
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
@Transactional
public class ComponentRegistryDbImpl extends ComponentRegistryImplBase implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryDbImpl.class);
    private Owner registryOwner;
    private RegistrySpace registrySpace;
    private Number groupId;
    @Autowired
    private Configuration configuration;
    @Autowired
    @Qualifier("componentsCache")
    private CMDComponentSpecCache componentsCache;
    @Autowired
    @Qualifier("profilesCache")
    private CMDComponentSpecCache profilesCache;
    // DAO's
    @Autowired
    private ComponentDao componentDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private CommentsDao commentsDao;
    @Autowired
    private MDMarshaller marshaller;
    @Autowired
    private GroupService groupService;

    
    // 
    /**
     * Default constructor, to use this as a (spring) bean. The public registry
     * by default. Use setRegistryStatus(), setRegistryOwner(), setGroupId() to make it another kind of
     * registry.
     *
     * 
     */
    public ComponentRegistryDbImpl() throws TransformerException {
        this.registrySpace = RegistrySpace.PUBLISHED;
    }


    @Override
    public Owner getRegistryOwner() {
        return this.registryOwner;
    }
    
    @Override
    public void setRegistryOwner(Owner registryOwner) {
        this.registryOwner = registryOwner;
    }
    
    
    @Override
    public RegistrySpace getRegistrySpace() {
        return this.registrySpace;
    }

    @Override
    public void setRegistrySpace(RegistrySpace registrySpace) {
        this.registrySpace = registrySpace;
    }

    @Override
    public Number getGroupId() {
        return this.groupId;
    }
    
    @Override
    public void setGroupId(Number groupId) {
        this.groupId = groupId;
    }
    
    @Override
    public Number getBaseDescriptionOwnerId(String cmdId){
        BaseDescription bd = componentDao.getByCmdId(cmdId);
        return bd.getDbUserId();
    }
    
    @Override
    public  List<Number> getItemGroups(String cmdId){
        List<Group> groups = groupService.getGroupsTheItemIsAMemberOf(cmdId);
        List<Number> result = new ArrayList<Number>();
        if (groups != null) {
            for (Group group: groups) {
                result.add(group.getId());
            }
        }
        return result;
    }
    
    /**
     * @return The user id, or null if there is no owner or it is not a user.
     */
    private Number getUserId() {
        if (registryOwner instanceof OwnerUser) {
            return registryOwner.getId();
        } else {
            return null;
        }
    }
    
    @Override
    public List<ProfileDescription> getProfileDescriptions() throws ComponentRegistryException, UserUnauthorizedException {
        try {
            switch (registrySpace) {
                case PRIVATE:
                    if (registryOwner == null) {
                        throw new ComponentRegistryException("Private workspace without owner!");
                    }
                    return ComponentUtils.toProfiles(componentDao.getPrivateBaseDescriptions(registryOwner.getId(), ProfileDescription.PROFILE_PREFIX));
                case GROUP:
                    return this.getProfileDescriptionsInGroup(groupId);
                case PUBLISHED:
                    return ComponentUtils.toProfiles(componentDao.getPublicBaseDescriptions(ProfileDescription.PROFILE_PREFIX));
                default:
                    throw new ComponentRegistryException("Unsupported status type" + registrySpace);
            }
        } catch (DataAccessException ex) {
            throw new ComponentRegistryException("Database access error while trying to get profile descriptions", ex);
        }
    }

    @Override
    public Boolean isItemPublic(String id) throws ItemNotFoundException{
        BaseDescription desc = componentDao.getByCmdId(id);
        if (desc == null) {
            String idS = (id == null) ? "null" : id;
            throw new ItemNotFoundException("The component with the id " + idS+ "is not found in the database.");
        }
        return desc.isPublic();
    }

    @Override
    public ProfileDescription getProfileDescriptionAccessControlled(String id) throws ItemNotFoundException, UserUnauthorizedException, ComponentRegistryException {
        boolean hasAccess = this.canCurrentUserAccessDescription(id);
        if (hasAccess) {
            try {
                return ComponentUtils.toProfile(componentDao.getByCmdId(id));
            } catch (DataAccessException ex) {
                throw new ComponentRegistryException("Database access error while trying to get profile description", ex);
            }
        } else {
            throw new UserUnauthorizedException("The logged-in user (aka registry owner) does not have access the the given profile");
        }
    }

    private ProfileDescription getProfileDescription(String id) throws ComponentRegistryException {
        try {
            return ComponentUtils.toProfile(componentDao.getByCmdId(id));
        } catch (DataAccessException ex) {
            throw new ComponentRegistryException("Database access error while trying to get profile description", ex);
        }

    }

    @Override
    public List<ComponentDescription> getComponentDescriptions() throws ComponentRegistryException, UserUnauthorizedException {
        try {
            switch (registrySpace) {
                case PRIVATE:
                    if (registryOwner == null) {
                        throw new ComponentRegistryException("Private workspace without owner!");
                    }
                    return ComponentUtils.toComponents(componentDao.getPrivateBaseDescriptions(registryOwner.getId(), ComponentDescription.COMPONENT_PREFIX));
                case GROUP:
                    return this.getComponentDescriptionsInGroup(groupId);
                case PUBLISHED:
                    return ComponentUtils.toComponents(componentDao.getPublicBaseDescriptions(ComponentDescription.COMPONENT_PREFIX));
                default:
                    throw new ComponentRegistryException("Unsupported status type" + registrySpace);
            }
        } catch (DataAccessException ex) {
            throw new ComponentRegistryException("Database access error while trying to get profile descriptions", ex);
        }
    }

    @Override
    public ComponentDescription getComponentDescriptionAccessControlled(String id) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        boolean hasAccess = this.canCurrentUserAccessDescription(id);
        if (hasAccess) {
            try {
                return ComponentUtils.toComponent(componentDao.getByCmdId(id));
            } catch (DataAccessException ex) {
                throw new ComponentRegistryException("Database access error while trying to get component description", ex);
            }
        } else {
            throw new UserUnauthorizedException("The logged-in user (aka registry owner) does not have access the the given component");
        }
    }

    private ComponentDescription getComponentDescription(String id) throws ComponentRegistryException {
        try {
            return ComponentUtils.toComponent(componentDao.getByCmdId(id));
        } catch (DataAccessException ex) {
            throw new ComponentRegistryException("Database access error while trying to get component description", ex);
        }

    }

    @Override
    public List<Comment> getCommentsInProfile(String profileId) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        try {
            //Olha was here
            //if (componentDao.isInRegistry(profileId, getUserId())) {
            if (this.canCurrentUserAccessDescription(profileId)) {
                final List<Comment> commentsFromProfile = commentsDao.getCommentsFromItem(profileId);
                this.setCanDeleteInComments(commentsFromProfile);
                return commentsFromProfile;
            } else {
                throw new UserUnauthorizedException("The logged-in user (aka registry owner) does not have access the the given profile");
            }
        } catch (DataAccessException ex) {
            throw new ComponentRegistryException(
                    "Database access error while trying to get list of comments from profile", ex);
        }
    }

    @Override
    public Comment getSpecifiedCommentInProfile(String profileId, String commentId)
            throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        if (this.canCurrentUserAccessDescription(profileId)) {
            try {
                Comment comment = commentsDao.findOne(Long.parseLong(commentId));
                if (comment != null && profileId.equals(comment.getComponentId())) {
                    this.setCanDeleteInComments(Collections.singleton(comment));
                    return comment;
                } else {
                    throw new ComponentRegistryException("Comment " + commentId  + "for the profile "+profileId);
                }
            } catch (DataAccessException ex) {
                throw new ComponentRegistryException("Database access error while trying to get comment from profile", ex);
            }
        } else {
            throw new UserUnauthorizedException("The logged-in user (aka registry owner) does not have access the the given profile");
        }
    }

    @Override
    public List<Comment> getCommentsInComponent(String componentId)
            throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        if (this.canCurrentUserAccessDescription(componentId)) {
            try {
                final List<Comment> commentsFromComponent = commentsDao.getCommentsFromItem(componentId);
                this.setCanDeleteInComments(commentsFromComponent);
                return commentsFromComponent;

            } catch (DataAccessException ex) {
                throw new ComponentRegistryException(
                        "Database access error while trying to get list of comments from component", ex);
            }
        } else {
            throw new UserUnauthorizedException("The logged-in user (aka registry owner) does not have access the the given component");
        }
    }

    @Override
    public Comment getSpecifiedCommentInComponent(String componentId, String commentId)
            throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        if (this.canCurrentUserAccessDescription(componentId)) {
            try {
                Comment comment = commentsDao.findOne(Long.parseLong(commentId));
                if (comment != null && componentId.equals(comment.getComponentId().toString())) {
                    this.setCanDeleteInComments(Collections.singleton(comment));
                    return comment;
                } else {
                    throw new ComponentRegistryException("Comment " + commentId  + "for the profile "+componentId);
                }
            } catch (DataAccessException ex) {
                throw new ComponentRegistryException("Database access error while trying to get comment from component", ex);
            }
        } else {
            throw new UserUnauthorizedException("The logged-in user (aka registry owner) does not have access the the given component");
        }
    }

    /**
     * Sets the {@link Comment#setCanDelete(boolean) canDelete} property on all
     * comments in the provided collection for the perspective of the specified
     * principal. Comment owners (determined by {@link Comment#getUserId() })
     * and admins can delete, others cannot.
     *
     * @param comments comments to configure
     * @param principal user to configure for
     * @see Comment#isCanDelete()
     */
    private void setCanDeleteInComments(Collection<Comment> comments) {
        String principalName = (userDao.getPrincipalNameById(registryOwner.getId())).getPrincipalName();
        final boolean isAdmin = configuration.isAdminUser(principalName);
        for (Comment comment : comments) {
            comment.setCanDelete(isAdmin || comment.getUserId() == registryOwner.getId().longValue());
        }

    }

    @Override
    public CMDComponentSpec getMDProfileAccessControled(String id) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        if (this.canCurrentUserAccessDescription(id)) {
            return this.getMDProfile(id);
        } else {
            throw new UserUnauthorizedException("The logged-in user (aka registry owner) does not have access the the given profile or the profile is not found.");
        }
    }

    private CMDComponentSpec getMDProfile(String id) throws ComponentRegistryException {
        if (id.startsWith(ProfileDescription.PROFILE_PREFIX)) {
            CMDComponentSpec result = profilesCache.get(id);
            if (result == null && !profilesCache.containsKey(id)) {
                result = this.getUncachedMDProfile(id);
                profilesCache.put(id, result);
            }
            return result;
        } else {
            throw new ComponentRegistryException("the id " + id + " is not a profile id.");
        }
    }

    public CMDComponentSpec getUncachedMDProfile(String id) throws ComponentRegistryException {
        try {
            return this.getUncachedMDComponent(id, componentDao);
        } catch (DataAccessException ex) {
            throw new ComponentRegistryException("Database access error while trying to get profile", ex);
        }
    }

    @Override
    public CMDComponentSpec getMDComponentAccessControlled(String id) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        if (this.canCurrentUserAccessDescription(id)) {
            return this.getMDComponent(id);
        } else {
            throw new UserUnauthorizedException("The logged-in user (aka registry owner) does not have access the the given component or the component is not found.");
        }
    }

    @Override
    public CMDComponentSpec getMDComponent(String id) throws ComponentRegistryException {
        if (id.startsWith(ComponentDescription.COMPONENT_PREFIX)) {
            CMDComponentSpec result = componentsCache.get(id);
            if (result == null && !componentsCache.containsKey(id)) {
                result = getUncachedMDComponent(id);
                componentsCache.put(id, result);
            }
            return result;
        } else {
            throw new ComponentRegistryException("The id " + id + " is not a component id");
        }
    }

    public CMDComponentSpec getUncachedMDComponent(String id) throws ComponentRegistryException {
        try {
            return this.getUncachedMDComponent(id, componentDao);
        } catch (DataAccessException ex) {
            throw new ComponentRegistryException("Database access error while trying to get component", ex);
        }
    }

    @Override
    public int register(BaseDescription description, CMDComponentSpec spec) {
        enrichSpecHeader(spec, description);
        try {
            String xml = componentSpecToString(spec);
            // Convert principal name to user record id
            Number uid = convertUserInDescription(description);
            componentDao.insertDescription(description, xml, description.isPublic(), uid);
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
    public int registerComment(Comment comment, String principalName) throws ComponentRegistryException, ItemNotFoundException, UserUnauthorizedException {
        try {
            if (comment.getComponentId() != null)  {
                if (this.canCurrentUserAccessDescription(comment.getComponentId()))
                {
                // Convert principal name to user record id
                Number uid = convertUserIdInComment(comment, principalName);
                // Set date to current date
                comment.setCommentDate(new Date());
                comment.setUserId(uid.longValue());
                commentsDao.saveAndFlush(comment);
            
            } else {
                throw new UserUnauthorizedException("The logged-in user cannot access the component/profile with id "+comment.getComponentId());
            }
            } else {
                throw new ComponentRegistryException("The component/profile id for this comment is null.");            
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
     * It also sets the name in the description according to the display name in
     * the database.
     *
     * @param description Description containing principle name as userId
     * @return Id (from database)
     * @throws DataAccessException
     */
    private Number convertUserInDescription(BaseDescription description) throws DataAccessException {
        Number uid = null;
        String name = null;
        if (description.getUserId() != null) {
            RegistryUser user = userDao.getByPrincipalName(description.getUserId());
            if (user != null) {
                uid = user.getId();
                name = user.getName();
            }
        } else {
            uid = getUserId(); // this can be null as well
        }
        if (uid != null) {
            description.setUserId(uid.toString());
        }
        if (name != null) {
            description.setCreatorName(name);
        }
        return uid;
    }

    /**
     * Calling service sets user id to principle. Our task is to convert this to
     * an id for later reference. If none is set and this is a user's workspace,
     * set from that user's id.
     *
     * @param comment Comment containing principle name as userId
     * @return Id (from database)
     * @throws DataAccessException
     */
    private Number convertUserIdInComment(Comment comment, String principalName) throws DataAccessException,
            ComponentRegistryException {
        if (principalName != null) {
            RegistryUser user = userDao.getByPrincipalName(principalName);
            if (user != null) {
                Long id = user.getId();
                if (id != null) {
                    // Set user id in comment for convenience of calling method
                    comment.setUserId(id);
                    // Set name to user's preferred display name
                    comment.setUserName(user.getName());
                    return id;
                } else {
                    throw new ComponentRegistryException("Cannot find user with principal name: " + principalName);
                }
            }
        }
        return null;
    }

    @Override
    public int update(BaseDescription description, CMDComponentSpec spec, boolean forceUpdate) throws UserUnauthorizedException, ItemNotFoundException {
        try {
            this.checkAuthorisation(description);
            this.checkAge(description);
            // For public components, check if used in other components or
            // profiles (unless forced)
            if (!forceUpdate && description.isPublic() && !description.isProfile()) {
                this.checkStillUsed(description.getId());
            }
            componentDao.updateDescription(getIdForDescription(description), description, componentSpecToString(spec));
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
        } catch (DeleteFailedException e) {
            LOG.error("Error while updating component", e);
            return -1;
        } catch (ComponentRegistryException e) {
            LOG.error("Error while updating component", e);
            return -1;
        }
    }

    @Override
    public int publish(BaseDescription desc, CMDComponentSpec spec, Principal principal) throws UserUnauthorizedException, ItemNotFoundException {
        int result = 0;
        this.checkAuthorisation(desc);
        if (desc.isPublic()) { // if already in published
            // todo
            desc.setHref(ComponentUtils.createPublicHref(desc.getHref()));
            Number id = getIdForDescription(desc);
            try {
                // Update description & content
                componentDao.updateDescription(id, desc, componentSpecToString(spec));
                // Set to public
                componentDao.setPublished(id, true);
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
    public void deleteMDProfile(String profileId) throws UserUnauthorizedException,
            DeleteFailedException, ComponentRegistryException, ItemNotFoundException {
        ProfileDescription desc = getProfileDescriptionAccessControlled(profileId);
        if (desc != null) {
            try {
                this.checkAuthorisation(desc);
                this.checkAge(desc);
                componentDao.setDeleted(desc, true);
                invalidateCache(desc);
            } catch (DataAccessException ex) {
                throw new DeleteFailedException("Database access error while trying to delete profile", ex);
            }
        }
    }

    @Override
    public void deleteMDComponent(String componentId, boolean forceDelete)
            throws UserUnauthorizedException, DeleteFailedException, ComponentRegistryException, ItemNotFoundException {
        BaseDescription desc = componentDao.getByCmdId(componentId);
        if (desc != null) {
            try {
                this.checkAuthorisation(desc);
                this.checkAge(desc);

                if (!forceDelete) {
                    checkStillUsed(componentId);
                }
                componentDao.setDeleted(desc, true);
                invalidateCache(desc);
            } catch (DataAccessException ex) {
                throw new DeleteFailedException("Database access error while trying to delete component", ex);
            }
        }
    }

    

    private void invalidateCache(BaseDescription description) {
        if (description.isProfile()) {
            profilesCache.remove(description.getId());
        } else {
            componentsCache.remove(description.getId());
        }
    }

    /**
     * Looks up description on basis of CMD Id. This will also check if such a
     * record even exists.
     *
     * @param description Description to look up
     * @return Database id for description
     * @throws IllegalArgumentException If description with non-existing id is
     * passed
     */
    private Number getIdForDescription(BaseDescription description) throws IllegalArgumentException {
        Number dbId = null;
        try {
            dbId = componentDao.getDbId(description.getId());
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
        getMarshaller().marshal(spec, os);
        String xml = os.toString("UTF-8");
        return xml;
    }

    private CMDComponentSpec getUncachedMDComponent(String id, ComponentDao dao) {
        String xml = dao.getContent(false, id);
        if (xml != null) {
            try {
                InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
                CMDComponentSpec result = getMarshaller().unmarshal(CMDComponentSpec.class, is, null);
                try {
                    is.close();
                    return result;
                } catch (IOException ex) {
                    LOG.error("Cannot close the stream", ex);
                    return result;
                }

            } catch (JAXBException ex) {
                LOG.error("Error while unmarshalling", ex);
            } catch (UnsupportedEncodingException ex) {
                LOG.error("Exception while reading XML from database", ex);
            }
        }
        return null;
    }

    private void checkAuthorisation(BaseDescription desc) throws UserUnauthorizedException, ItemNotFoundException {
        if (!this.canCurrentUserAccessDescription(desc.getId())) {            
            String principalName = (registryOwner != null) ? userDao.getPrincipalNameById(registryOwner.getId()).getPrincipalName() : "null";
            throw new UserUnauthorizedException("Unauthorized operation user '" + principalName
                    + "' is not the creator (nor a member of the group, nor an administrator) of the "
                    + (desc.isProfile() ? "profile" : "component") + "(" + desc + ").");
        }
    }

    private void checkAuthorisationComment(Comment desc) throws UserUnauthorizedException {
        String principalName = userDao.getPrincipalNameById(registryOwner.getId()).getPrincipalName();
        if (!(this.isOwnerOfComment(desc, principalName) || configuration.isAdminUser(principalName))) {
            throw new UserUnauthorizedException("Unauthorized operation user '" + principalName
                    + "' is not the creator (nor a member of the group, nor an administrator) of the " + (desc.getId()) + "(" + desc + ").");
        }
    }

//    private boolean isOwnerOfDescription(BaseDescription desc, String principalName) {
//        String owner = componentDao.getOwnerPrincipalName(getIdForDescription(desc));
//        return owner != null // If owner is null, no one can be owner
//                && principalName.equals(owner);
//    }
    private boolean isOwnerOfComment(Comment com, String principalName) {
        long id = Long.parseLong(com.getId());
        RegistryUser owner = commentsDao.getOwnerOfComment(id);
        return owner != null // If owner is null, no one can be owner
                && principalName.equals(owner.getPrincipalName());
    }

    private void checkAge(BaseDescription desc) throws DeleteFailedException {
        String principalName = userDao.getPrincipalNameById(registryOwner.getId()).getPrincipalName();
        if (desc.isPublic() && !configuration.isAdminUser(principalName)) {
            Date regDate = desc.getRegistrationDate();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
            if (regDate.before(calendar.getTime())) { // More then month old
                throw new DeleteFailedException(
                        "The "
                        + (desc.isProfile() ? "Profile" : "Component")
                        + " is more then a month old and cannot be deleted anymore. It might have been used to create metadata, deleting it would invalidate that metadata.");
            }
        }
    }

    @Override
    public String getName() {
        if (this.getRegistrySpace() != null) {
            if (this.getRegistrySpace().equals(RegistrySpace.PUBLISHED)) {
                return ComponentRegistry.PUBLIC_NAME;
            } else {
                if (this.getRegistrySpace().equals(RegistrySpace.GROUP)) {
                    if (groupId != null) {
                        return "Registry of group" + groupId.toString();
                    } else {
                        return "Error: Registry of group null.";
                    }
                }
            }
        };
        RegistryUser u = userDao.findOne(getUserId().longValue());
        return "Registry of " + u.getName();
    }

    private boolean canCurrentUserAccessDescription(String cmdId) throws ItemNotFoundException{
        if (cmdId == null) {
            throw new ItemNotFoundException("Item with the null cmdIdentifier.");
        }
        
        BaseDescription description = componentDao.getByCmdId(cmdId);
        if (description == null) {
            throw new ItemNotFoundException("Item with the id "+cmdId+" is not found.");
        }
        
       
        Number userId = getUserId();
        if (userId == null) {
            return false;
        }
        RegistryUser user = userDao.findOne(userId.longValue());
        if (user == null) {
            return false;
        }
       

        if (configuration.isAdminUser(user.getPrincipalName())) {
            return true;
        }

        return groupService.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(user, description);
    }

    @Override
    public List<ProfileDescription> getDeletedProfileDescriptions() throws ComponentRegistryException {
        return ComponentUtils.toProfiles(componentDao.getDeletedDescriptions(getUserId()));
    }

    @Override
    public List<ComponentDescription> getDeletedComponentDescriptions() throws ComponentRegistryException {
        return ComponentUtils.toComponents(componentDao.getDeletedDescriptions(getUserId()));
    }

    @Override
    public void deleteComment(String commentId) throws IOException, 
            UserUnauthorizedException, DeleteFailedException, ItemNotFoundException {
        try {
            Comment comment = commentsDao.findOne(Long.parseLong(commentId));
            if (comment != null
                    // Comment must have an existing (in this registry)
                    // componentId or profileId
                    && comment.getComponentId() != null
                    && this.canCurrentUserAccessDescription(comment.getComponentId())) {
                this.checkAuthorisationComment(comment);
                commentsDao.delete(comment);
            } else {
                // Comment exists in DB, but component is not in this registry
                throw new ItemNotFoundException("Comment " + commentId + " cannot be found in specified registry");
            }
        } catch (DataAccessException ex) {
            throw new DeleteFailedException("Database access error while trying to delete component", ex);
        } catch (NumberFormatException ex) {
            throw new DeleteFailedException("Illegal comment ID, cannot parse integer", ex);
        }
    }
    
  

    @Override
    public CMDComponentSpecExpander getExpander() {
        return new CMDComponentSpecExpanderDbImpl(this);
    }

    @Override
    protected MDMarshaller getMarshaller() {
        return marshaller;
    }

    @Override
    public String toString() {
        return getName();
    }

    private List<ComponentDescription> getComponentDescriptionsInGroup(Number groupId)
            throws ComponentRegistryException, UserUnauthorizedException {

        String principalName = userDao.getPrincipalNameById(registryOwner.getId()).getPrincipalName();

        if (!groupService.userGroupMember(principalName, groupId.toString())) {
            throw new UserUnauthorizedException("The user \'" + principalName + "\' does not have access to components of the group " + groupId);
        }

        List<String> componentIds = componentDao.getAllItemIdsInGroup(ComponentDescription.COMPONENT_PREFIX, groupId.longValue());
        List<ComponentDescription> components = new ArrayList<ComponentDescription>();
        for (String id : componentIds) {
            BaseDescription description = componentDao.getByCmdId(id);
            components.add(ComponentUtils.toComponent(description));
        }
        return components;
    }

    private List<ProfileDescription> getProfileDescriptionsInGroup(Number groupId) throws ComponentRegistryException, UserUnauthorizedException {

        String principalName = userDao.getPrincipalNameById(registryOwner.getId()).getPrincipalName();

        if (!groupService.userGroupMember(principalName, groupId.toString())) {
            throw new UserUnauthorizedException("The user \'" + principalName + "\' does not have access to profiles of the group " + groupId);
        }

        List<String> profileIds = componentDao.getAllItemIdsInGroup(ProfileDescription.PROFILE_PREFIX, groupId.longValue());
        List<ProfileDescription> profiles = new ArrayList<ProfileDescription>();
        for (String id : profileIds) {
            BaseDescription description = componentDao.getByCmdId(id);
            if (description != null) {
                profiles.add(ComponentUtils.toProfile(description));
            }
        }
        return profiles;
    }

    @Override
    public List<ProfileDescription> getProfileDescriptionsForMetadaEditor(Number groupId) throws UserUnauthorizedException,
            ComponentRegistryException {
        return this.getProfileDescriptionsInGroup(groupId.longValue());
    }

    @Override
    public List<String> getAllNonDeletedProfileIds() {
        return componentDao.getAllNonDeletedProfileIds();
    }

    @Override
    public List<String> getAllNonDeletedComponentIds() {
        return componentDao.getAllNonDeletedComponentIds();
    }

    @Override
    public List<ComponentDescription> getUsageInComponents(String componentId) throws ComponentRegistryException {
        LOG.debug("Checking usage of component {} in components", componentId);
        List<ComponentDescription> result = new ArrayList<ComponentDescription>();
        List<String> ids = getAllNonDeletedComponentIds();
        for (String id : ids) {
            CMDComponentSpec spec = getMDComponent(id);
            if (spec != null && hasComponentId(componentId, spec.getCMDComponent())) {
                LOG.debug("Component {} used in component {}", componentId, spec.getHeader().getID());
                result.add(getComponentDescription(id));
            }
        }
        return result;
    }

    @Override
    public List<ProfileDescription> getUsageInProfiles(String componentId) throws ComponentRegistryException {
        LOG.debug("Checking usage of component {} in profiles", componentId);
        List<ProfileDescription> result = new ArrayList<ProfileDescription>();
        for (String id : getAllNonDeletedProfileIds()) {
            CMDComponentSpec profile = getMDProfile(id);
            if (profile != null && hasComponentId(componentId, profile.getCMDComponent())) {
                LOG.debug("Component {} used in profile {}", componentId, profile.getHeader().getID());
                result.add(getProfileDescription(id));
            }
        }
        return result;
    }

    protected static boolean findComponentId(String componentId, List<CMDComponentType> componentReferences) {
        for (CMDComponentType cmdComponent : componentReferences) {
            if (hasComponentId(componentId, cmdComponent)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasComponentId(String componentId, CMDComponentType cmdComponent) {
        if (componentId.equals(cmdComponent.getComponentId())) {
            return true;
        } else if (findComponentId(componentId, cmdComponent.getCMDComponent())) {
            return true;
        } else {
            return false;
        }
    }

    protected void checkStillUsed(String componentId) throws DeleteFailedException, ComponentRegistryException {
        for (String id : getAllNonDeletedProfileIds()) {
            CMDComponentSpec spec = getMDProfile(id);
            if (spec != null && hasComponentId(componentId, spec.getCMDComponent())) {
                LOG.warn("Cannot delete component {}, still used in profile {} and possibly other profiles and/or components", componentId, spec.getHeader().getID());
                // Profile match - throw
                throw new DeleteFailedException("Component is still in use by other components or profiles. Request component usage for details.");
            }
        }

        LOG.debug("Component {} is not used in any profiles", componentId);

        for (String id : getAllNonDeletedComponentIds()) {
            CMDComponentSpec spec = getMDComponent(id);
            if (spec != null && hasComponentId(componentId, spec.getCMDComponent())) {
                LOG.warn("Cannot delete component {}, still used in component {} and possibly other components", componentId, spec.getHeader().getID());
                // Component match -> throw
                throw new DeleteFailedException("Component is still in use by one or more other components. Request component usage for details.");
            }
        }
    }
}
