package clarin.cmdi.componentregistry.impl.database;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.GroupMembership;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDescriptionDao;
import clarin.cmdi.componentregistry.persistence.ProfileDescriptionDao;
import clarin.cmdi.componentregistry.persistence.UserDao;
import clarin.cmdi.componentregistry.persistence.jpa.GroupDao;
import clarin.cmdi.componentregistry.persistence.jpa.OwnershipDao;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import static org.junit.Assert.*;

public class GroupServiceImplTest extends BaseUnitTest {

    @Autowired
    GroupService groupService;
    @Autowired
    UserDao userDao;
    @Autowired
    ProfileDescriptionDao profileDescriptionDao;

    @Autowired
    GroupDao groupDao;
    
    @Autowired
    OwnershipDao ownershipDao;

    @Autowired
    ComponentDescriptionDao componentDescriptionDao;

    RegistryUser user;
    RegistryUser user2;
    RegistryUser user3;

    protected ProfileDescription makeTestProfile(boolean isPublic, long ownerId) {
	ProfileDescription profile = new ProfileDescription();
	profile.setDescription("some description");
	profile.setId("4567");
	profile.setName("profilename");
	Number id = profileDescriptionDao.insertDescription(profile, "someContent",
		isPublic, ownerId);
	return profileDescriptionDao.getById(id);
    }

    protected ComponentDescription makeTestComponent(boolean isPublic,
	    long ownerId) {
	ComponentDescription componentDescription = new ComponentDescription();
	componentDescription.setDescription("some description");
	componentDescription.setId("4567");
	componentDescription.setName("componentname");
	Number id = componentDescriptionDao.insertDescription(
		componentDescription, "someContent", isPublic, ownerId);
	return componentDescriptionDao.getById(id);
    }

    @Before
    public void setupThisTest() {
	user = new RegistryUser();
	user.setName("Test User");
	user.setPrincipalName(DummyPrincipal.DUMMY_CREDENTIALS
		.getPrincipalName());
	userDao.insertUser(user);
	user = userDao.getByPrincipalName(user.getPrincipalName());

	user2 = new RegistryUser();
	user2.setName("Test User 2");
	user2.setPrincipalName(DummyPrincipal.DUMMY_CREDENTIALS
		.getPrincipalName() + "2");
	userDao.insertUser(user2);
	user2 = userDao.getByPrincipalName(user2.getPrincipalName());

	user3 = new RegistryUser();
	user3.setName("Test User 3");
	user3.setPrincipalName(DummyPrincipal.DUMMY_CREDENTIALS
		.getPrincipalName() + "3");
	userDao.insertUser(user3);
	user3 = userDao.getByPrincipalName(user3.getPrincipalName());
    }

    @Test
    public void testCreateNewGroup_getGroupsOwnedByUser() {
	// Expect no groups for the current user
	List<Group> groups = groupService.getGroupsOwnedByUser(user
		.getPrincipalName());
	assertTrue(groups.isEmpty());

	// Make a group
	Group group1 = groupDao.findOne(groupService.createNewGroup("Group 1",
		user.getPrincipalName()));
	// and another one
	Group group2 = groupDao.findOne(groupService.createNewGroup("Group 2",
		user.getPrincipalName()));
	groups = groupService.getGroupsOwnedByUser(user.getPrincipalName());
	assertEquals(2, groups.size());

	assertTrue(groups.contains(group1));
	assertTrue(groups.contains(group2));
    }

    @Test(expected = ValidationException.class)
    public void testCreateDuplicateGroup() {
	// Make a group
	groupService.createNewGroup("Group X", user.getPrincipalName());
	// Make same group, again
	groupService.createNewGroup("Group X", user.getPrincipalName());
    }

    @Test
    public void testAccessToNonOwnedPrivateProfile() {
	// Make a private profile that belongs to someone else
	ProfileDescription profile = makeTestProfile(false, 9999);
	// Expect that user can't access profile
	boolean result = groupService
		.canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
			user, profile);
	assertFalse(result);
    }

    @Test
    public void testAccessToNonOwnedPublicProfile() {
	// Make a public profile that belongs to someone else
	ProfileDescription profile = makeTestProfile(true, 9999);

	// Expect that user can access the profile
	boolean result = groupService
		.canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
			user, profile);
	assertTrue(result);
    }

    @Test
    public void testAccessToOwnedPrivateProfile() {
	// Make a profile that belongs to someone else
	ProfileDescription profile = makeTestProfile(false, user.getId()
		.longValue());
	// Add an ownership to that profile to the current user
	Ownership ownership = new Ownership();
	ownership.setProfileId(profile.getId());
	ownership.setUserId(user.getId().longValue());
	groupService.addOwnership(ownership);

	// Expect that user can access the profile
	boolean result = groupService
		.canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
			user, profile);
	assertTrue(result);
    }

    @Test
    public void testAccessViaGroupToPrivateProfile() {
	// Make a profile that belongs to someone else
	ProfileDescription profile = makeTestProfile(false, 9999);

	// Make a group that belongs to someone else
	Group group = groupDao.findOne(groupService.createNewGroup("Group 1",
		user2.getPrincipalName()));

	groupService.makeMember(user.getPrincipalName(),
		group.getName());
	// Make me a member of that group

	// Add an ownership to that profile to the group
	Ownership ownership = new Ownership();
	ownership.setProfileId(profile.getId());
	ownership.setGroupId(group.getId());
	groupService.addOwnership(ownership);

	// Expect that user can access the profile
	boolean result = groupService
		.canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
			user, profile);
	assertTrue(result);
    }

    @Test
    public void testAccessToNonOwnedPrivateComponent() {
	// Make a private profile that belongs to someone else
	ComponentDescription componentDescription = makeTestComponent(false,
		9999);

	// Expect that user can't access profile
	boolean result = groupService
		.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
			user, componentDescription);
	assertFalse(result);
    }

    @Test
    public void testAccessToNonOwnedPublicComponent() {
	// Make a public profile that belongs to someone else
	ComponentDescription componentDescription = makeTestComponent(true,
		9999);

	// Expect that user can access the profile
	boolean result = groupService
		.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
			user, componentDescription);
	assertTrue(result);
    }

    @Test
    public void testAccessToOwnedPrivateComponent() {
	// Make a profile that belongs to someone else
	ComponentDescription componentDescription = makeTestComponent(false,
		9999);

	// Add an ownership to that profile to the current user
	Ownership ownership = new Ownership();
	ownership.setComponentId(componentDescription.getId());
	ownership.setUserId(user.getId().longValue());
	groupService.addOwnership(ownership);

	// Expect that user can access the profile
	boolean result = groupService
		.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
			user, componentDescription);
	assertTrue(result);
    }

    @Test
    public void testAccessViaGroupToPrivateComponent() {
	// Make a profile that belongs to someone else
	ComponentDescription componentDescription = makeTestComponent(false,
		user3.getId().longValue());

	// Make a group that belongs to someone else
	Group group = groupDao.findOne(groupService.createNewGroup("Group 1",
		user2.getPrincipalName()));

	// Make me a member of that group
	groupService.makeMember(user.getPrincipalName(),
		group.getName());

	// Add an ownership to that component to the group
	Ownership ownership = new Ownership();
	ownership.setComponentId(componentDescription.getId());
	ownership.setGroupId(group.getId());
	groupService.addOwnership(ownership);

	// Expect that user can access the profile
	boolean result = groupService
		.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
			user, componentDescription);
	assertTrue(result);
    }

    @Test
    public void testTransferComponentOwnershipFromUserToGroup() {

	// Make a group
	Group group = groupDao.findOne(groupService.createNewGroup("Group 1",
		user.getPrincipalName()));

	// Make user and user2 members of the same group
	groupService.makeMember(user.getPrincipalName(), group.getName());
	
	assertTrue(groupService.getGroupsOfWhichUserIsAMember(user.getPrincipalName()).contains(group));
	groupService.makeMember(user2.getPrincipalName(), group.getName());
	assertTrue(groupService.getGroupsOfWhichUserIsAMember(user2.getPrincipalName()).contains(group));

	// Make a component that belongs to user
	ComponentDescription componentDescription = makeTestComponent(false,
		user.getId().longValue());

	// Just for the fun of it: check that user can access the component
	// while user2 can't
	boolean result = groupService
		.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
			user, componentDescription);
	assertTrue(result);
	result = groupService
		.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
			user2, componentDescription);
	assertFalse(result);

	// user transfers ownership of the component to his group
	groupService.transferComponentOwnershipFromUserToGroup(
		user.getPrincipalName(), group.getName(),
		componentDescription.getId());
	
	List<Ownership> ownerships = (List<Ownership>)ownershipDao.findAll();

	// Check that user and user2 have access to the component...
	result = groupService
		.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
			user, componentDescription);
	assertTrue(result);
	result = groupService
		.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
			user2, componentDescription);
	assertTrue(result);

	// ... but someone unreleated not
	result = groupService
		.canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
			user3, componentDescription);
	assertFalse(result);
    }

    @Test
    public void testTransferProfileOwnershipFromUserToGroup() {

	// Make a group
	Group group = groupDao.findOne(groupService.createNewGroup("Group 1",
		user.getPrincipalName()));

	// Make user and user2 members of the same group
	groupService.makeMember(user.getPrincipalName(), group.getName());
	
	assertTrue(groupService.getGroupsOfWhichUserIsAMember(user.getPrincipalName()).contains(group));
	groupService.makeMember(user2.getPrincipalName(), group.getName());
	assertTrue(groupService.getGroupsOfWhichUserIsAMember(user2.getPrincipalName()).contains(group));

	// Make a profile that belongs to user
	ProfileDescription profileDescription= makeTestProfile(false,
		user.getId().longValue());

	// Just for the fun of it: check that user can access the profile
	// while user2 can't
	boolean result = groupService
		.canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
			user, profileDescription);
	assertTrue(result);
	result = groupService
		.canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
			user2, profileDescription);
	assertFalse(result);

	// user transfers ownership of the component to his group
	groupService.transferProfileOwnershipFromUserToGroup(
		user.getPrincipalName(), group.getName(),
		profileDescription.getId());
	
	List<Ownership> ownerships = (List<Ownership>)ownershipDao.findAll();

	// Check that user and user2 have access to the component...
	result = groupService
		.canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
			user, profileDescription);
	assertTrue(result);
	result = groupService
		.canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
			user2, profileDescription);
	assertTrue(result);

	// ... but someone unreleated not
	result = groupService
		.canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(
			user3, profileDescription);
	assertFalse(result);
    }
}
