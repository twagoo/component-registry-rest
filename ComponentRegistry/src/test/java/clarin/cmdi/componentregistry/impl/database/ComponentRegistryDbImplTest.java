package clarin.cmdi.componentregistry.impl.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.UserMapping.User;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
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
	ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
    }

    @Test
    public void testRegisterComponent() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);
	ComponentDescription description = ComponentDescription.createNewDescription();

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

	ComponentDescription componentDescription = register.getComponentDescription(description.getId());
	assertNotNull(componentDescription);

	assertEquals("Header id should be set from description id", description.getId(), component.getHeader().getID());
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

	ProfileDescription profileDescription = register.getProfileDescription(description.getId());
	assertNotNull(profileDescription);

	assertNotNull(profile);
	assertEquals("Header id should be set from description id", description.getId(), profile.getHeader().getID());
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

    @Test
    public void testGetDeletedDescriptions() throws Exception {
	User user = createUser();
	Number userId = userDao.insertUser(user);
	ComponentRegistry registry = getComponentRegistryForUser(userId);
	ComponentRegistry publicReg = getComponentRegistryForUser(null);
	ProfileDescription desc1 = createProfile(registry);
	ProfileDescription desc2 = createProfile(publicReg);
	ComponentDescription desc3 = createComponent(registry);
	ComponentDescription desc4 = createComponent(publicReg);

	assertEquals(0, registry.getDeletedProfileDescriptions().size());
	assertEquals(0, publicReg.getDeletedProfileDescriptions().size());
	assertEquals(0, registry.getDeletedComponentDescriptions().size());
	assertEquals(0, publicReg.getDeletedComponentDescriptions().size());

	registry.deleteMDProfile(desc1.getId(), USER_CREDS.getPrincipal());

	assertEquals(1, registry.getDeletedProfileDescriptions().size());
	assertEquals(0, publicReg.getDeletedProfileDescriptions().size());
	assertEquals(0, registry.getDeletedComponentDescriptions().size());
	assertEquals(0, publicReg.getDeletedComponentDescriptions().size());

	publicReg.deleteMDProfile(desc2.getId(), USER_CREDS.getPrincipal());

	assertEquals(1, registry.getDeletedProfileDescriptions().size());
	assertEquals(1, publicReg.getDeletedProfileDescriptions().size());
	assertEquals(0, registry.getDeletedComponentDescriptions().size());
	assertEquals(0, publicReg.getDeletedComponentDescriptions().size());

	registry.deleteMDComponent(desc3.getId(), USER_CREDS.getPrincipal(), false);

	assertEquals(1, registry.getDeletedProfileDescriptions().size());
	assertEquals(1, publicReg.getDeletedProfileDescriptions().size());
	assertEquals(1, registry.getDeletedComponentDescriptions().size());
	assertEquals(0, publicReg.getDeletedComponentDescriptions().size());

	publicReg.deleteMDComponent(desc4.getId(), USER_CREDS.getPrincipal(), false);

	assertEquals(1, registry.getDeletedProfileDescriptions().size());
	assertEquals(1, publicReg.getDeletedProfileDescriptions().size());
	assertEquals(1, registry.getDeletedComponentDescriptions().size());
	assertEquals(1, publicReg.getDeletedComponentDescriptions().size());

    }

    private ProfileDescription createProfile(ComponentRegistry register) throws Exception {
	ProfileDescription description = getProfileDesc();

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

    private ProfileDescription getProfileDesc() {
	ProfileDescription description = ProfileDescription.createNewDescription();
	description.setName("Aap");
	description.setCreatorName(USER_CREDS.getDisplayName());
	description.setUserId(USER_CREDS.getPrincipalName());
	description.setDescription("MyDescription");
	return description;
    }

    @Test
    public void testDeletePublicComponent() throws Exception {
	ComponentRegistry registry = getComponentRegistryForUser(null);
	ComponentDescription description = createComponent(registry);
	// Delete as admin
	registry.deleteMDComponent(description.getId(), PRINCIPAL_ADMIN, false);
	assertEquals(0, registry.getComponentDescriptions().size());
	assertNull(registry.getMDComponent(description.getId()));
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
	assertNull(registry.getMDComponent(description.getId()));
    }

    @Test
    public void testDoNotDeleteOldPublicComponent() throws Exception {
	Number userId = userDao.insertUser(createUser());
	ComponentRegistry registry = getComponentRegistryForUser(userId);

	ComponentDescription description = getComponentDesc();
	Calendar calendar = Calendar.getInstance();
	calendar.set(Calendar.YEAR, 1999);
	description.setRegistrationDate(DateFormatUtils.formatUTC(calendar.getTime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()));
	CMDComponentSpec testComp = RegistryTestHelper.getTestComponent();

	registry.register(description, testComp);
	registry.publish(description, testComp, USER_CREDS.getPrincipal());

	// Switch to public registry
	registry = getComponentRegistryForUser(null);

	try {
	    registry.deleteMDComponent(description.getId(), USER_CREDS.getPrincipal(), false);
	    fail("Should have thrown exception");
	} catch (DeleteFailedException e) {
	}
	assertEquals(1, registry.getComponentDescriptions().size());
	registry.deleteMDComponent(description.getId(), PRINCIPAL_ADMIN, false);
	assertEquals(0, registry.getComponentDescriptions().size());

	registry = getComponentRegistryForUser(userId); // ComponentRegistryFactoryImpl.getInstance().getComponentRegistry(true, USER_CREDS); //user registry
	description = getComponentDesc();
	registry.register(description, testComp);
	assertEquals(1, registry.getComponentDescriptions().size());
	registry.deleteMDComponent(description.getId(), USER_CREDS.getPrincipal(), false); //user workspace can always delete
	assertEquals(0, registry.getComponentDescriptions().size());
    }

    @Test
    public void testDoNotDeleteOldPublicProfile() throws Exception {
	Number userId = userDao.insertUser(createUser());
	ComponentRegistry registry = getComponentRegistryForUser(userId);

	ProfileDescription description = getProfileDesc();
	Calendar calendar = Calendar.getInstance();
	calendar.set(Calendar.YEAR, 1999);
	description.setRegistrationDate(DateFormatUtils.formatUTC(calendar.getTime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()));
	CMDComponentSpec testComp = RegistryTestHelper.getTestProfile();

	registry.register(description, testComp);
	registry.publish(description, testComp, USER_CREDS.getPrincipal());

	// Switch to public registry
	registry = getComponentRegistryForUser(null);
	try {
	    registry.deleteMDProfile(description.getId(), USER_CREDS.getPrincipal());
	    fail("Should have thrown exception");
	} catch (DeleteFailedException e) {
	}
	assertEquals(1, registry.getProfileDescriptions().size());
	registry.deleteMDProfile(description.getId(), PRINCIPAL_ADMIN);
	assertEquals(0, registry.getProfileDescriptions().size());

	registry = getComponentRegistryForUser(userId);
	description = getProfileDesc();//Need to create a new one for a new id
	registry.register(description, testComp);
	assertEquals(1, registry.getProfileDescriptions().size());
	registry.deleteMDProfile(description.getId(), USER_CREDS.getPrincipal()); //user workspace can always delete
	assertEquals(0, registry.getProfileDescriptions().size());
    }

    private ComponentDescription createComponent(ComponentRegistry registry) throws Exception {
	ComponentDescription description = getComponentDesc();
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

    private ComponentDescription getComponentDesc() {
	ComponentDescription description = ComponentDescription.createNewDescription();
	description.setName("Aap");
	description.setCreatorName(USER_CREDS.getDisplayName());
	description.setUserId(USER_CREDS.getPrincipalName());
	description.setDescription("MyDescription");
	return description;
    }
    
    private ComponentRegistry getComponentRegistryForUser(Number userId) {
	ComponentRegistryDbImpl componentRegistry = componentRegistryBeanFactory.getNewComponentRegistry();
	componentRegistry.setUserId(userId);
	return componentRegistry;
    }

    private User createUser() {
	User user = new User();
	user.setName(USER_CREDS.getDisplayName());
	user.setPrincipalName(USER_CREDS.getPrincipalName());
	return user;
    }

    @Test
    public void testGetProfileAsXsd() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);
	String profileContent = "";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	profileContent += "    <Header />\n";
	profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
	profileContent += "        <CMD_Element name=\"Age\">\n";
	profileContent += "            <ValueScheme>\n";
	profileContent += "                <pattern>[0-9][0-9]</pattern>\n";
	profileContent += "            </ValueScheme>\n";
	profileContent += "        </CMD_Element>\n";
	profileContent += "    </CMD_Component>\n";
	profileContent += "</CMD_ComponentSpec>\n";

	String id = "profile1";
	ProfileDescription description = RegistryTestHelper.addProfile(register, id, profileContent);

	OutputStream output = new ByteArrayOutputStream();
	register.getMDProfileAsXsd(description.getId(), output);
	String xsd = output.toString();
	assertTrue(xsd.endsWith("</xs:schema>"));

	assertTrue(RegistryTestHelper.hasComponent(xsd, "Actor", "0", "unbounded"));
    }

    @Test
    public void testGetNestedRecursiveComponentAsXsd() throws Exception {
	User user = createUser();
	Number userId = userDao.insertUser(user);
	ComponentRegistry register = getComponentRegistryForUser(null);

	String comp1Id = "component1";
	String comp2Id = "component2";

	// Component1 references component2
	
	String comp1Content = "";
	comp1Content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	comp1Content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	comp1Content += "    <Header/>\n";
	comp1Content += "    <CMD_Component name=\"Recursion\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
	comp1Content += "       <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
	comp1Content += "	<CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + comp2Id + "\" CardinalityMin=\"0\" CardinalityMax=\"5\"/>\n";
	comp1Content += "    </CMD_Component>\n";
	comp1Content += "</CMD_ComponentSpec>\n";

	ComponentDescription comp1Desc = RegistryTestHelper.addComponent(register, comp1Id, comp1Content);

	// Component2 references component1

	String comp2Content = "";
	comp2Content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	comp2Content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	comp2Content += "    <Header/>\n";
	comp2Content += "    <CMD_Component name=\"Recursion\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
	comp2Content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
	comp2Content += "	 <CMD_Component ComponentId=\"" + comp1Desc.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"5\"/>\n";
	comp2Content += "    </CMD_Component>\n";
	comp2Content += "</CMD_ComponentSpec>\n";

	RegistryTestHelper.addComponent(register, comp2Id, comp2Content);

	String profileContent = "";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	profileContent += "    <Header />\n";
	profileContent += "    <CMD_Component ComponentId=\"" + comp1Desc.getId()
		+ "\" filename=\"component-test-file\" CardinalityMin=\"0\" CardinalityMax=\"5\">\n";
	profileContent += "    </CMD_Component>\n";
	profileContent += "</CMD_ComponentSpec>\n";

	String id = "profile1";
	ProfileDescription description = RegistryTestHelper.addProfile(register, id, profileContent);

	OutputStream output = new ByteArrayOutputStream();
	try {
	    register.getMDProfileAsXml(description.getId(), output);
	    // This should throw a ComponentRegistryException for recursion
	    fail();
	} catch (ComponentRegistryException ex) {
	}
    }

    @Test
    public void testGetNestedComponentAsXsd() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);

	String compId = "component1";
	String compContent = "";
	compContent += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	compContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	compContent += "    <Header/>\n";
	compContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
	compContent += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
	compContent += "    </CMD_Component>\n";
	compContent += "</CMD_ComponentSpec>\n";

	ComponentDescription compDesc = RegistryTestHelper.addComponent(register, compId, compContent);

	String profileContent = "";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	profileContent += "    <Header />\n";
	profileContent += "    <CMD_Component ComponentId=\"" + compDesc.getId()
		+ "\" filename=\"component-test-file\" CardinalityMin=\"0\" CardinalityMax=\"5\">\n";
	profileContent += "    </CMD_Component>\n";
	profileContent += "</CMD_ComponentSpec>\n";

	String id = "profile1";
	ProfileDescription description = RegistryTestHelper.addProfile(register, id, profileContent);

	OutputStream output = new ByteArrayOutputStream();
	register.getMDProfileAsXsd(description.getId(), output);
	String xsd = output.toString();

	assertTrue(xsd.endsWith("</xs:schema>"));
	assertTrue(RegistryTestHelper.hasComponent(xsd, "Actor", "0", "5"));
    }

    @Test
    public void testGetNestedComponentAsXsdComplex() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);

	String compContent = "";
	compContent += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	compContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	compContent += "    <Header/>\n";
	compContent += "    <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
	compContent += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
	compContent += "    </CMD_Component>\n";
	compContent += "</CMD_ComponentSpec>\n";
	ComponentDescription compDesc1 = RegistryTestHelper.addComponent(register, "component1", compContent);

	compContent = "";
	compContent += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	compContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	compContent += "    <Header/>\n";
	compContent += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
	compContent += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
	compContent += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" filename=\"component-test-file\">\n";
	compContent += "        </CMD_Component>\n";
	compContent += "    </CMD_Component>\n";
	compContent += "</CMD_ComponentSpec>\n";
	ComponentDescription compDesc2 = RegistryTestHelper.addComponent(register, "component2", compContent);

	compContent = "";
	compContent += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	compContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	compContent += "    <Header/>\n";
	compContent += "    <CMD_Component name=\"ZZZ\u00e9\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
	compContent += "        <CMD_Component ComponentId=\"" + compDesc2.getId()
		+ "\" filename=\"component-test-file\" CardinalityMin=\"0\" CardinalityMax=\"2\">\n";
	compContent += "        </CMD_Component>\n";
	compContent += "        <CMD_Component ComponentId=\"" + compDesc1.getId()
		+ "\" filename=\"component-test-file\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
	compContent += "        </CMD_Component>\n";
	compContent += "    </CMD_Component>\n";
	compContent += "</CMD_ComponentSpec>\n";
	ComponentDescription compDesc3 = RegistryTestHelper.addComponent(register, "component3", compContent);

	ByteArrayOutputStream output = new ByteArrayOutputStream();
	register.getMDComponentAsXsd(compDesc3.getId(), output);
	String xsd = output.toString("UTF-8");

	assertTrue(xsd.endsWith("</xs:schema>"));
	// System.out.println(xsd);

	assertTrue(RegistryTestHelper.hasComponent(xsd, "YYY", "0", "2"));
	assertTrue(RegistryTestHelper.hasComponent(xsd, "XXX", "1", "10"));
	assertTrue(RegistryTestHelper.hasComponent(xsd, "XXX", "0", "99"));
	assertTrue(RegistryTestHelper.hasComponent(xsd, "ZZZ\u00e9", "1", "unbounded"));
    }

    @Test
    public void testUpdate() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);
	ComponentDescription description = ComponentDescription.createNewDescription();
	description.setName("Aap");
	description.setDescription("MyDescription");

	CMDComponentSpec testComponent = RegistryTestHelper.getTestComponent("Test1");
	register.register(description, testComponent);

	// Change values
	description.setName("Noot");
	description.setDescription("AnotherDescription");
	// Update in db
	register.update(description, testComponent, PRINCIPAL_ADMIN, false);
	description = register.getComponentDescription(description.getId());
	// Test if new values are there
	assertNotNull(description);
	assertEquals("Noot", description.getName());
	assertEquals("AnotherDescription", description.getDescription());

	// Update content
	CMDComponentSpec testComponent2 = RegistryTestHelper.getTestComponent("Test2");
	register.update(description, testComponent2, PRINCIPAL_ADMIN, false);
	// Test if new content is there
	assertEquals(RegistryTestHelper.getXml(testComponent2),
		RegistryTestHelper.getXml(register.getMDComponent(description.getId())));


	// Update both
	description.setName("Mies");
	description.setDescription("YetAnotherDescription");
	CMDComponentSpec testComponent3 = RegistryTestHelper.getTestComponent("Test3");

	// Update in db
	register.update(description, testComponent3, PRINCIPAL_ADMIN, false);
	description = register.getComponentDescription(description.getId());
	// Test if new values are there
	assertNotNull(description);
	assertEquals("Mies", description.getName());
	assertEquals("YetAnotherDescription", description.getDescription());
	// Test if new content is there
	assertEquals(RegistryTestHelper.getXml(testComponent3),
		RegistryTestHelper.getXml(register.getMDComponent(description.getId())));
    }

    @Test
    public void testPublish() throws Exception {
	Number userId = userDao.insertUser(createUser());

	ComponentRegistry userRegistry = getComponentRegistryForUser(userId);
	ComponentRegistry publicRegistry = getComponentRegistryForUser(null);
	ComponentDescription description = ComponentDescription.createNewDescription();
	description.setName("Aap");
	description.setDescription("MyDescription");
	description.setUserId(DummyPrincipal.DUMMY_CREDENTIALS.getPrincipalName());

	CMDComponentSpec testComponent = RegistryTestHelper.getTestComponent("Test1");
	userRegistry.register(description, testComponent);
	description = userRegistry.getComponentDescription(description.getId());
	assertNotNull(description);
	assertEquals("Aap", description.getName());
	assertEquals("MyDescription", description.getDescription());

	// Change values
	description.setName("Noot");
	description.setDescription("AnotherDescription");
	CMDComponentSpec testComponent2 = RegistryTestHelper.getTestComponent("Test2");

	// Publish
	int result = userRegistry.publish(description, testComponent2, DummyPrincipal.DUMMY_PRINCIPAL);
	assertEquals(0, result);

	// Should not be in user registry
	assertNull(userRegistry.getComponentDescription(description.getId()));
	// Get from public registry
	description = publicRegistry.getComponentDescription(description.getId());
	// Test if new values are there
	assertNotNull(description);
	assertEquals("Noot", description.getName());
	assertEquals("AnotherDescription", description.getDescription());
	// Test if new content is there
	assertEquals(RegistryTestHelper.getXml(testComponent2),
		RegistryTestHelper.getXml(publicRegistry.getMDComponent(description.getId())));
	assertNull(userRegistry.getMDComponent(description.getId()));
    }
}
