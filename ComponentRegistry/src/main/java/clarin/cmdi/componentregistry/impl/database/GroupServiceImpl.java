package clarin.cmdi.componentregistry.impl.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.GroupMembership;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.GroupDao;
import clarin.cmdi.componentregistry.persistence.jpa.GroupMembershipDao;
import clarin.cmdi.componentregistry.persistence.jpa.OwnershipDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;

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
    private ComponentDao componentDao;
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
	if (ownership.getComponentId() == null)
	    throw new RuntimeException("Ownership needs a componentId");
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
	    RegistryUser user, BaseDescription description) {
	// TODO make some joins and multi-id queries to speed the entire method
	// up
	boolean isProfile = (description instanceof ProfileDescription);
	long userId = user.getId().longValue();
	// anyone can access public profile
	if (componentDao.isPublic(description.getId()))
	    return true;
	// the creator can also access any profile
	if (description.getUserId().equals(user.getId() + ""))
	    return true;

	// a co-ownership on the profile also allows access
	Ownership ownership = ownershipDao.findOwnershipByUserAndComponent(userId,
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
	    ownership = ownershipDao.findOwnershipByGroupAndComponent(groupId,
		    description.getId());
	    if (ownership != null)
		return true;
	}
	return false;
    }

    @Override
    public boolean canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
	    RegistryUser user, BaseDescription baseDescription) {
	return canUserAccessAbstractDescriptionEitherOnHisOwnOrThroughGroupMembership(
		user, baseDescription);
    }

    @Override
    @ManagedOperation(description = "Mage a user member of a group")
    @ManagedOperationParameters({
	    @ManagedOperationParameter(name = "principalName", description = "Principal name of the user to make a member"),
	    @ManagedOperationParameter(name = "groupName", description = "Name of the group") })
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

    @Override
    public List<Group> getGroupsOfWhichUserIsAMember(String principal) {
	RegistryUser user = userDao.getByPrincipalName(principal);
	if (user == null || user.getId() == null)
	    return new ArrayList<Group>();
	List<GroupMembership> memberships = groupMembershipDao
		.findGroupsTheUserIsAmemberOf(user.getId().longValue());
	List<Group> groups = new ArrayList<Group>();
	for (GroupMembership m : memberships)
	    groups.add(groupDao.findOne(m.getGroupId()));
	return groups;
    }

    @Override
    public List<String> getComponentIdsInGroup(long groupId) {
	List<Ownership> ownerships = ownershipDao.findOwnershipByGroup(groupId);
	Set<String> componentIds = new HashSet<String>();
	for (Ownership o : ownerships)
	    if (ComponentUtils.isComponentId(o.getComponentId()))
		componentIds.add(o.getComponentId());
	List<String> idsList = new ArrayList<String>(componentIds);
	Collections.sort(idsList);
	return idsList;
    }

    @Override
    public List<String> getProfileIdsInGroup(long groupId) {
	List<Ownership> ownerships = ownershipDao.findOwnershipByGroup(groupId);
	Set<String> profileIds = new HashSet<String>();
	for (Ownership o : ownerships)
	    if (ComponentUtils.isProfileId(o.getComponentId()))
		profileIds.add(o.getComponentId());
	List<String> idsList = new ArrayList<String>(profileIds);
	Collections.sort(idsList);
	return idsList;
    }

    @Override
    public List<Group> getGroupsTheItemIsAMemberOf(String itemId) {
	Set<Ownership> ownerships = new HashSet<Ownership>();
	ownerships.addAll(ownershipDao.findOwnershipByComponentId(itemId));
	Set<Group> groups = new HashSet<Group>();
	for (Ownership ownership : ownerships)
	    groups.add(groupDao.findOne(ownership.getGroupId()));
	List<Group> groupList = new ArrayList<Group>(groups);
	Collections.sort(groupList, new Comparator<Group>() {

	    @Override
	    public int compare(Group g1, Group g2) {
		return (int) (g1.getId() - g2.getId());
	    }
	});
	return groupList;
    }

    @ManagedOperation(description = "Make a component owned by a group instead of a user")
    @ManagedOperationParameters({
	    @ManagedOperationParameter(name = "principal", description = "Name of the principal who owns the component"),
	    @ManagedOperationParameter(name = "groupName", description = "Name of the group to move the component to"),
	    @ManagedOperationParameter(name = "componentId", description = "Id of component") })
    @Override
    public void transferItemOwnershipFromUserToGroup(String principal,
	    String groupName, String itemId) {

	BaseDescription item = null;
	item = componentDao.getByCmdId(itemId);
	if (item == null)
	    throw new ValidationException(
		    "No profile or component found with ID " + itemId);
	Group group = groupDao.findGroupByName(groupName);
	if (group == null)
	    throw new ValidationException("No group found with name "
		    + groupName);
	Ownership ownership = null;
	List<Ownership> oldOwnerships = ownershipDao
		.findOwnershipByComponentId(itemId);
	ownershipDao.delete(oldOwnerships);
	ownership = new Ownership();
	ownership.setComponentId(itemId);
	ownership.setGroupId(group.getId());
	addOwnership(ownership);
    }

    @Override
    public void transferItemOwnershipFromUserToGroupId(String principal,
	    long groupId, String componentId) {
	Group group = groupDao.findOne(groupId);
	if (group == null)
	    throw new ValidationException("No group found with id " + groupId);
	transferItemOwnershipFromUserToGroup(principal, group.getName(),
		componentId);
    }

}
