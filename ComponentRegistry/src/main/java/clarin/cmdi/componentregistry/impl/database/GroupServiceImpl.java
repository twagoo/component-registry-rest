package clarin.cmdi.componentregistry.impl.database;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.GroupMembership;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.AbstractDescriptionDao;
import clarin.cmdi.componentregistry.persistence.ComponentDescriptionDao;
import clarin.cmdi.componentregistry.persistence.ProfileDescriptionDao;
import clarin.cmdi.componentregistry.persistence.UserDao;
import clarin.cmdi.componentregistry.persistence.jpa.GroupDao;
import clarin.cmdi.componentregistry.persistence.jpa.GroupMembershipDao;
import clarin.cmdi.componentregistry.persistence.jpa.OwnershipDao;

/**
 * Service that manages groups, memberships and ownerships. It exposes some
 * functions over JMX, that's why some methods use human-friendly names (user
 * principal names, group names) rather than ID arguments.
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
@ManagedResource(objectName = "componentregistry:name=GroupService", description = "Operations for managing groups")
@Service("GroupService")
@Transactional
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupDao groupDao;
    @Autowired
    private GroupMembershipDao groupMembershipDao;
    @Autowired
    private OwnershipDao ownershipDao;
    @Autowired
    private ProfileDescriptionDao profileDescriptionDao;
    @Autowired
    private ComponentDescriptionDao componentDescriptionDao;
    @Autowired
    private UserDao userDao;

    public void setGroupDao(GroupDao groupDao) {
	this.groupDao = groupDao;
    }

    public void setGroupMembershipDao(GroupMembershipDao groupMembershipDao) {
	this.groupMembershipDao = groupMembershipDao;
    }

    public void setOwnershipDao(OwnershipDao ownershipDao) {
	this.ownershipDao = ownershipDao;
    }

    @Override
    public List<Group> getGroupsOwnedByUser(String ownerPrincipalName) {
	RegistryUser owner = userDao.getByPrincipalName(ownerPrincipalName);
	return groupDao.findGroupOwnedByUser(owner.getId().longValue());
    }

    @Override
    public boolean isUserOwnerOfGroup(long groupId, RegistryUser user) {
	List<Group> groups = getGroupsOwnedByUser(user.getPrincipalName());
	for (Group group : groups)
	    if (group.getId() == groupId)
		return true;
	return false;
    }

    private void checkOwnership(Ownership ownership) {
	if (ownership.getComponentId() == null
		&& ownership.getProfileId() == null)
	    throw new RuntimeException(
		    "Ownership needs either a componentId or a profileId");
	if (ownership.getComponentId() != null
		&& ownership.getProfileId() != null)
	    throw new RuntimeException(
		    "Ownership has both a componentId and a profileId");
	if (ownership.getUserId() == 0 && ownership.getGroupId() == 0)
	    throw new RuntimeException("Ownership needs a groupId or userId");
	if (ownership.getUserId() != 0 && ownership.getGroupId() != 0)
	    throw new RuntimeException(
		    "Ownership has both a groupId and a userId ");
    }

    private void assertOwnershipDoesNotExist(Ownership ownership) {
	Ownership o = ownershipDao.findOwnershipByGroupAndComponent(
		ownership.getGroupId(), ownership.getComponentId());
	if (o != null)
	    throw new ValidationException("Ownership exists");
	o = ownershipDao.findOwnershipByGroupAndProfile(ownership.getGroupId(),
		ownership.getProfileId());
	if (o != null)
	    throw new ValidationException("Ownership exists");
	o = ownershipDao.findOwnershipByUserAndProfile(ownership.getUserId(),
		ownership.getProfileId());
	if (o != null)
	    throw new ValidationException("Ownership exists");
	o = ownershipDao.findOwnershipByUserAndComponent(ownership.getUserId(),
		ownership.getComponentId());
	if (o != null)
	    throw new ValidationException("Ownership exists");
    }

    @Override
    public void addOwnership(Ownership ownership) {
	checkOwnership(ownership);
	assertOwnershipDoesNotExist(ownership);
	ownershipDao.save(ownership);
    }

    @Override
    public void removeOwnership(Ownership ownership) {
	throw new RuntimeException("not implemented");
    }

    protected boolean canUserAccessAbstractDescriptionEitherOnHisOwnOrThroughGroupMembership(
	    RegistryUser user, AbstractDescription description) {
	// TODO make some joins and multi-id queries to speed the entire method
	// up
	boolean isProfile = (description instanceof ProfileDescription);
	AbstractDescriptionDao<? extends AbstractDescription> dao = isProfile ? profileDescriptionDao
		: componentDescriptionDao;
	long userId = user.getId().longValue();
	// anyone can access public profile
	if (dao.isPublic(description.getId()))
	    return true;
	// the creator can also access any profile
	if (description.getUserId().equals(user.getId() + ""))
	    return true;

	// a co-ownership on the profile also allows access
	Ownership ownership = isProfile ? ownershipDao
		.findOwnershipByUserAndProfile(userId, description.getId())
		: ownershipDao.findOwnershipByUserAndComponent(userId,
			description.getId());
	if (ownership != null)
	    return true;

	// get a list of groups the user owns and is a member of
	List<Group> groups = groupDao.findGroupOwnedByUser(userId);
	Set<Long> groupIds = new HashSet<Long>();
	for (Group group : groups)
	    groupIds.add(group.getId());

	List<GroupMembership> memberships = groupMembershipDao
		.findGroupsTheUserIsAmemberOf(userId);
	for (GroupMembership gm : memberships)
	    groupIds.add(gm.getGroupId());

	for (Long groupId : groupIds) {
	    ownership = isProfile ? ownershipDao
		    .findOwnershipByGroupAndProfile(groupId,
			    description.getId()) : ownershipDao
		    .findOwnershipByGroupAndComponent(groupId,
			    description.getId());
	    if (ownership != null)
		return true;
	}
	return false;
    }

    @Override
    public boolean canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
	    RegistryUser user, ProfileDescription profile) {
	return canUserAccessAbstractDescriptionEitherOnHisOwnOrThroughGroupMembership(
		user, profile);
    }

    @Override
    public boolean canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
	    RegistryUser user, ComponentDescription component) {
	return canUserAccessAbstractDescriptionEitherOnHisOwnOrThroughGroupMembership(
		user, component);
    }

    @Override
    @ManagedOperation(description = "Mage a user member of a group")
    @ManagedOperationParameters({
	@ManagedOperationParameter(name = "principalName", description = "Principal name of the user to make a member") ,
    	@ManagedOperationParameter(name = "groupName", description = "Name of the group")})
    public long makeMember(String userPrincipalName, String groupName) {
	RegistryUser user = userDao.getByPrincipalName(userPrincipalName);
	Group group = groupDao.findGroupByName(groupName);
	GroupMembership gm = groupMembershipDao.findMembership(user.getId()
		.longValue(), group.getId());
	if (gm != null)
	    return gm.getId();
	gm = new GroupMembership();
	gm.setGroupId(group.getId());
	gm.setUserId(user.getId().longValue());
	return groupMembershipDao.save(gm).getId();
    }

    @ManagedOperation(description = "Create a new group")
    @ManagedOperationParameters({
	    @ManagedOperationParameter(name = "name", description = "Name of the group, must be unique"),
	    @ManagedOperationParameter(name = "ownerPrincipalName", description = "Principal name of the user") })
    @Override
    public long createNewGroup(String name, String ownerPrincipalName) {
	RegistryUser owner = userDao.getByPrincipalName(ownerPrincipalName);
	if (owner == null)
	    throw new ValidationException("No principal '" + ownerPrincipalName
		    + "' found");
	Group group = groupDao.findGroupByName(name);
	if (group != null)
	    throw new ValidationException("Group '" + name + "' already exists");
	group = new Group();
	group.setName(name);
	group.setOwnerId(owner.getId().longValue());
	group = groupDao.save(group);
	return group.getId();
    }

    @ManagedOperation(description = "List available groups")
    @Override
    public List<String> listGroupNames() {
	List<String> groupNames = new ArrayList<String>();
	for (Group group : groupDao.findAll())
	    groupNames.add(group.getName());
	return groupNames;
    }

    protected void transferAbstractDescriptionOwnershipFromUserToGroup(
	    String principal, String groupName, String descriptionId,
	    boolean isProfile) {
	RegistryUser user = userDao.getByPrincipalName(principal);
	Group group = groupDao.findGroupByName(groupName);
	Ownership ownership = isProfile ? ownershipDao
		.findOwnershipByUserAndProfile(user.getId().longValue(),
			descriptionId) : ownershipDao
		.findOwnershipByUserAndComponent(user.getId().longValue(),
			descriptionId);
	if (ownership != null)
	    ownershipDao.delete(ownership);
	ownership = new Ownership();
	if (isProfile)
	    ownership.setProfileId(descriptionId);
	else
	    ownership.setComponentId(descriptionId);
	ownership.setGroupId(group.getId());
	addOwnership(ownership);
    }

    @Override
    public void transferComponentOwnershipFromUserToGroup(String principal,
	    String groupName, String componentId) {
	transferAbstractDescriptionOwnershipFromUserToGroup(principal,
		groupName, componentId, false);
    }

    @Override
    public void transferProfileOwnershipFromUserToGroup(String principal,
	    String groupName, String profileId) {
	transferAbstractDescriptionOwnershipFromUserToGroup(principal,
		groupName, profileId, true);
    }

    @Override
    public List<Group> getGroupsOfWhichUserIsAMember(String principal) {
	RegistryUser user = userDao.getByPrincipalName(principal);
	List<GroupMembership> memberships = groupMembershipDao
		.findGroupsTheUserIsAmemberOf(user.getId().longValue());
	List<Group> groups = new ArrayList<Group>();
	for (GroupMembership m : memberships)
	    groups.add(groupDao.findOne(m.getGroupId()));
	return groups;
    }

}
