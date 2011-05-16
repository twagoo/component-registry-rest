package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase.*;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import java.security.Principal;

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
    protected static final Principal PRINCIPAL_ADMIN = new DummyPrincipal("admin");
    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
	resetDatabase(jdbcTemplate);
	createTableComponentDescription(jdbcTemplate);
	createTableProfileDescription(jdbcTemplate);
	createTableXmlContent(jdbcTemplate);
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
    public void testDeleteProfile() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);
	ProfileDescription description = ProfileDescription.createNewDescription();
	description.setName("Aap");
	description.setCreatorName(USER_CREDS.getDisplayName());
	description.setUserId(USER_CREDS.getPrincipalNameMD5Hex());
	description.setDescription("MyDescription");
	CMDComponentSpec testProfile = RegistryTestHelper.getTestProfile();

	register.register(description, testProfile);

	assertEquals(1, register.getProfileDescriptions().size());
	assertNotNull(register.getMDProfile(description.getId()));

	try {
	    register.deleteMDProfile(description.getId(), new DummyPrincipal("Fake User"));
	    fail("Should have thrown exception");
	} catch (UserUnauthorizedException e) {
	}
	register.deleteMDComponent(description.getId(), new DummyPrincipal("Fake User"), false);

	assertEquals(1, register.getProfileDescriptions().size());
	assertNotNull(register.getMDProfile(description.getId()));

	register.deleteMDProfile(description.getId(), USER_CREDS.getPrincipal());

	assertEquals(0, register.getProfileDescriptions().size());
	assertNull(register.getMDProfile(description.getId()));
    }

    @Test
    public void testDeleteComponent() throws Exception {
	ComponentRegistry registry = getComponentRegistryForUser(null);
	ComponentDescription description = ComponentDescription.
		createNewDescription();
	description.setName("Aap");
	description.setCreatorName(USER_CREDS.getDisplayName());
	description.setUserId(USER_CREDS.getPrincipalNameMD5Hex());
	description.setDescription("MyDescription");
	CMDComponentSpec testComp = RegistryTestHelper.getTestComponent();

	registry.register(description, testComp);
	try {
	    registry.deleteMDComponent(description.getId(), new DummyPrincipal("Fake User"), false);
	    fail("Should have thrown exception");
	} catch (UserUnauthorizedException e) {
	}

	assertEquals(1, registry.getComponentDescriptions().size());
	assertNotNull(registry.getMDComponent(description.getId()));

	registry.deleteMDComponent(description.getId(), USER_CREDS.getPrincipal(), false);

	assertEquals(0, registry.getComponentDescriptions().size());
	assertNull(registry.getMDProfile(description.getId()));
    }

    private ComponentRegistry getComponentRegistryForUser(Number userId) {
	ComponentRegistryDbImpl componentRegistry = componentRegistryBeanFactory.
		getNewComponentRegistry();
	componentRegistry.setUserId(userId);
	return componentRegistry;
    }
}
