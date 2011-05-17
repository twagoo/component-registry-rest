package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase.*;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.UserMapping.User;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import java.io.IOException;
import java.security.Principal;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext-database-impl.xml"})
public class ComponentRegistryDbImplTest {

    protected final static UserCredentials USER_CREDS = DummyPrincipal.DUMMY_CREDENTIALS;
    protected static final DummyPrincipal PRINCIPAL_ADMIN = DummyPrincipal.DUMMY_ADMIN_PRINCIPAL;
    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private UserDao userDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
	resetDatabase(jdbcTemplate);
	createTableComponentDescription(jdbcTemplate);
	createTableProfileDescription(jdbcTemplate);
	createTableXmlContent(jdbcTemplate);
	createTableRegistryUser(jdbcTemplate);
    }

    @Test
    public void testRegisterComponent() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);
	ComponentDescription description = ComponentDescription.
		createNewDescription();

	description.setName("Aap");
	description.setDescription("MyDescription");

	assertEquals(0, register.getComponentDescriptions().size());
	assertEquals(0, register.getProfileDescriptions().size());

	CMDComponentSpec testComponent = RegistryTestHelper.getTestComponent();
	assertNull(testComponent.getHeader().getID());
	assertNull(testComponent.getHeader().getName());
	assertNull(testComponent.getHeader().getDescription());
	testComponent.getHeader().setDescription("Will not be overwritten");

	register.register(description, testComponent);

	assertEquals(1, register.getComponentDescriptions().size());
	assertEquals(0, register.getProfileDescriptions().size());

	ComponentDescription desc = register.getComponentDescriptions().get(0);
	assertNotNull(desc);
	CMDComponentSpec component = register.getMDComponent(desc.getId());
	assertNotNull(component);

	assertNull(register.getMDProfile(desc.getId()));

	ComponentDescription componentDescription = register.
		getComponentDescription(description.getId());
	assertNotNull(componentDescription);

	assertEquals("Header id should be set from description id", description.
		getId(), component.getHeader().getID());
	assertEquals("Aap", component.getHeader().getName());
	assertEquals("Will not be overwritten", component.getHeader().
		getDescription());
    }

    @Test
    public void testRegisterProfile() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);
	ProfileDescription description = ProfileDescription.createNewDescription();
	description.setName("Aap");
	description.setDescription("MyDescription");

	assertEquals(0, register.getComponentDescriptions().size());
	assertEquals(0, register.getProfileDescriptions().size());

	CMDComponentSpec testProfile = RegistryTestHelper.getTestProfile();
	assertNull(testProfile.getHeader().getID());
	assertNull(testProfile.getHeader().getName());
	assertNull(testProfile.getHeader().getDescription());

	register.register(description, testProfile);

	assertEquals(0, register.getComponentDescriptions().size());
	assertEquals(1, register.getProfileDescriptions().size());
	ProfileDescription desc = register.getProfileDescriptions().get(0);
	assertNull(register.getMDComponent(desc.getId()));

	CMDComponentSpec profile = register.getMDProfile(desc.getId());

	ProfileDescription profileDescription = register.getProfileDescription(description.
		getId());
	assertNotNull(profileDescription);

	assertNotNull(profile);
	assertEquals("Header id should be set from description id", description.
		getId(), profile.getHeader().getID());
	assertEquals("Aap", profile.getHeader().getName());
	assertEquals("MyDescription", profile.getHeader().getDescription());
    }

    @Test
    public void testDeletePublicProfile() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);
	ProfileDescription description = createProfile(register);

	// Delete as admin
	register.deleteMDProfile(description.getId(), PRINCIPAL_ADMIN);

	assertEquals(0, register.getProfileDescriptions().size());
	assertNull(register.getMDProfile(description.getId()));
    }

    @Test
    public void testDeleteUserProfile() throws Exception {
	User user = createUser();
	Number userId = userDao.insertUser(user);
	ComponentRegistry registry = getComponentRegistryForUser(userId);
	ProfileDescription description = createProfile(registry);
	// Delete as user
	registry.deleteMDProfile(description.getId(), USER_CREDS.getPrincipal());
	assertEquals(0, registry.getComponentDescriptions().size());
	assertNull(registry.getMDProfile(description.getId()));
	// Delete as admin
	description = createProfile(registry);

	registry.deleteMDProfile(description.getId(), PRINCIPAL_ADMIN);
	assertEquals(0, registry.getProfileDescriptions().size());
	assertNull(registry.getMDProfile(description.getId()));
    }

    private ProfileDescription createProfile(ComponentRegistry register) throws IOException, JAXBException, DeleteFailedException {
	ProfileDescription description = ProfileDescription.createNewDescription();
	description.setName("Aap");
	description.setCreatorName(USER_CREDS.getDisplayName());
	description.setUserId(USER_CREDS.getPrincipalNameMD5Hex());
	description.setDescription("MyDescription");

	CMDComponentSpec testProfile = RegistryTestHelper.getTestProfile();
	register.register(description, testProfile);

	assertEquals(1, register.getProfileDescriptions().size());
	assertNotNull(register.getMDProfile(description.getId()));
	// Non authorized user should never be able to delete
	try {
	    register.deleteMDProfile(description.getId(), new DummyPrincipal("Fake User"));
	    fail("Should have thrown exception");
	} catch (UserUnauthorizedException e) {
	}
	
	assertEquals(1, register.getProfileDescriptions().size());
	assertNotNull(register.getMDProfile(description.getId()));
	return description;
    }

    @Test
    public void testDeletePublicComponent() throws Exception {
	ComponentRegistry registry = getComponentRegistryForUser(null);
	ComponentDescription description = createComponent(registry);
	// Delete as admin
	registry.deleteMDComponent(description.getId(), PRINCIPAL_ADMIN, false);
	assertEquals(0, registry.getComponentDescriptions().size());
	assertNull(registry.getMDProfile(description.getId()));
    }

    @Test
    public void testDeleteUserComponent() throws Exception {
	User user = createUser();
	Number userId = userDao.insertUser(user);
	ComponentRegistry registry = getComponentRegistryForUser(userId);
	ComponentDescription description = createComponent(registry);
	// Delete as user
	registry.deleteMDComponent(description.getId(), USER_CREDS.getPrincipal(), false);
	assertEquals(0, registry.getComponentDescriptions().size());
	assertNull(registry.getMDProfile(description.getId()));

	// Delete as admin
	description = createComponent(registry);

	registry.deleteMDComponent(description.getId(), PRINCIPAL_ADMIN, false);
	assertEquals(0, registry.getComponentDescriptions().size());
	assertNull(registry.getMDProfile(description.getId()));
    }

    private ComponentDescription createComponent(ComponentRegistry registry) throws IOException, DeleteFailedException, JAXBException {
	ComponentDescription description = ComponentDescription.
		createNewDescription();
	description.setName("Aap");
	description.setCreatorName(USER_CREDS.getDisplayName());
	description.setUserId(USER_CREDS.getPrincipalNameMD5Hex());
	description.setDescription("MyDescription");
	CMDComponentSpec testComp = RegistryTestHelper.getTestComponent();

	registry.register(description, testComp);

	// Non authorized user should never be able to delete
	try {
	    registry.deleteMDComponent(description.getId(), new DummyPrincipal("Fake User"), false);
	    fail("Should have thrown exception");
	} catch (UserUnauthorizedException e) {
	}
	
	assertEquals(1, registry.getComponentDescriptions().size());
	assertNotNull(registry.getMDComponent(description.getId()));
	return description;
    }

    private ComponentRegistry getComponentRegistryForUser(Number userId) {
	ComponentRegistryDbImpl componentRegistry = componentRegistryBeanFactory.
		getNewComponentRegistry();
	componentRegistry.setUserId(userId);
	return componentRegistry;
    }

    private User createUser() {
	User user = new User();
	user.setName(USER_CREDS.getDisplayName());
	user.setPrincipalName(USER_CREDS.getPrincipalName());
	return user;
    }
}
