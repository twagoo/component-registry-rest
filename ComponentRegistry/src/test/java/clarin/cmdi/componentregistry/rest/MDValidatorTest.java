package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ComponentStatus;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class MDValidatorTest {

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ComponentRegistry publicRegistry;

    @Before
    public void init() {
	ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
	publicRegistry = componentRegistryFactory.getPublicRegistry();
    }

    @Test
    public void testValidateSucces() {
	MDValidator validator = getValidProfileValidator();
	assertTrue(validator.validate());
    }

    @Test
    public void testValidateIllegalComponentAttributeName() {
	String profileContent = "";
	profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	profileContent += "    <Header />\n";
	profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
	profileContent += "	   <AttributeList>\n";
	profileContent += "		<Attribute>\n";
	profileContent += "		    <Name>myattribute</Name>\n"; // this should be allowed
	profileContent += "		    <Type>string</Type>\n";
	profileContent += "		</Attribute>\n";
	profileContent += "		<Attribute>\n";
	profileContent += "		    <Name>ref</Name>\n"; // this should NOT be allowed
	profileContent += "		    <Type>string</Type>\n";
	profileContent += "		</Attribute>\n";
	profileContent += "		<Attribute>\n";
	profileContent += "		    <Name>ComponentId</Name>\n"; // neither should this
	profileContent += "		    <Type>string</Type>\n";
	profileContent += "		</Attribute>\n";
	profileContent += "	   </AttributeList>\n";
	profileContent += "        <CMD_Element name=\"Age\">\n";
	profileContent += "		<AttributeList>\n";
	profileContent += "		    <Attribute>\n";
	profileContent += "			<Name>ref</Name>\n"; // allowed here, only forbidden on components
	profileContent += "			<Type>string</Type>\n";
	profileContent += "		    </Attribute>\n";
	profileContent += "		    <Attribute>\n";
	profileContent += "			<Name>ComponentId</Name>\n"; // allowed here, only forbidden on components
	profileContent += "			<Type>string</Type>\n";
	profileContent += "		    </Attribute>\n";
	profileContent += "		</AttributeList>\n";
	profileContent += "            <ValueScheme>\n";
	profileContent += "                <pattern>[23][0-9]</pattern>\n";
	profileContent += "            </ValueScheme>\n";
	profileContent += "        </CMD_Element>\n";
	profileContent += "    </CMD_Component>\n";
	profileContent += "</CMD_ComponentSpec>\n";
	InputStream input = new ByteArrayInputStream(profileContent.getBytes());

	ProfileDescription desc = ProfileDescription.createNewDescription();
	MDValidator validator = new MDValidator(input, desc, publicRegistry, null, publicRegistry);
	assertFalse(validator.validate());
	assertEquals(4, validator.getErrorMessages().size());
	assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.PARSE_ERROR));
	assertTrue(validator.getErrorMessages().get(1).startsWith(MDValidator.PARSE_ERROR));
    }

    @Test
    public void testValidateNoComponentId() throws Exception {
	String profileContent = "";
	profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	profileContent += "    <Header />\n";
	profileContent += "    <CMD_Component filename=\"component-actor.xml\"/>\n";
	profileContent += "</CMD_ComponentSpec>\n";
	InputStream input = new ByteArrayInputStream(profileContent.getBytes());

	ProfileDescription desc = ProfileDescription.createNewDescription();
	MDValidator validator = new MDValidator(input, desc, publicRegistry, null, publicRegistry);
	assertFalse(validator.validate());
	assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));
    }

    @Test
    public void testValidateComponentIdNotRegistered() throws Exception {
	String id1 = "component1";
	String id2 = "component2";

	String profileContent = "";
	profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">";
	profileContent += "    <Header />";
	profileContent += "    <CMD_Component name=\"Test\">";
	profileContent += "	<CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + id1 + "\"/>"; //id not registered
	profileContent += "	<CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + id2 + "\"/>"; //id not registered
	profileContent += "    </CMD_Component>";
	profileContent += "</CMD_ComponentSpec>";

	// Ids not registered will return two errors. One for each id
	ProfileDescription desc = ProfileDescription.createNewDescription();
	MDValidator validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, publicRegistry, null, publicRegistry);
	assertFalse(validator.validate());
	assertEquals(2, validator.getErrorMessages().size());
	assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));
	assertTrue(validator.getErrorMessages().get(1).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));

	// id1 will be added and therefore only id2 is not registered
	RegistryTestHelper.addComponent(publicRegistry, id1);
	validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, publicRegistry, null, publicRegistry);
	assertFalse(validator.validate());
	assertEquals(1, validator.getErrorMessages().size());
	assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));

	// id2 is added, no more errors shoud be return
	RegistryTestHelper.addComponent(publicRegistry, id2);
	validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, publicRegistry, null, publicRegistry);
	assertTrue("component is registered should be valid now", validator.validate());
	assertEquals(0, validator.getErrorMessages().size());
    }

    @Test
    public void testValidateUserRegistry() throws Exception {
	String id1 = "component1";
	String id2 = "component2";
	ComponentRegistry userRegistry = componentRegistryFactory.getComponentRegistry(ComponentStatus.DEVELOPMENT, null, DummyPrincipal.DUMMY_CREDENTIALS);

	String profileContent = "";
	profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">";
	profileContent += "    <Header />";
	profileContent += "    <CMD_Component name=\"Test\">";
	profileContent += "	<CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + id1 + "\"/>"; //id not registered
	profileContent += "	<CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + id2 + "\"/>"; //id not registered
	profileContent += "    </CMD_Component>";
	profileContent += "</CMD_ComponentSpec>";

	ProfileDescription desc = ProfileDescription.createNewDescription();
	MDValidator validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, userRegistry, userRegistry, publicRegistry);
	assertFalse(validator.validate());
	assertEquals(2, validator.getErrorMessages().size());
	assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));
	assertTrue(validator.getErrorMessages().get(1).startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));

	RegistryTestHelper.addComponent(userRegistry, id1);
	RegistryTestHelper.addComponent(publicRegistry, id2);
	validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, publicRegistry, null, publicRegistry);
	assertFalse(validator.validate());
	assertEquals(1, validator.getErrorMessages().size());
	assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));

	validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, userRegistry, userRegistry, publicRegistry);
	assertTrue(validator.validate());
	assertEquals(0, validator.getErrorMessages().size());
    }

    @Test
    public void testValidateNestedComponents() throws Exception {
	String id1 = "component1";

	String content = "";
	content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	content += "    <Header />\n";
	content += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
	content += "        <CMD_Element name=\"Name\" ValueScheme=\"string\" />\n";
	content += "      <CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + id1 + "\"/>\n"; //id not registered
	content += "    </CMD_Component>\n";
	content += "</CMD_ComponentSpec>\n";

	ComponentDescription desc = ComponentDescription.createNewDescription();
	MDValidator validator = new MDValidator(new ByteArrayInputStream(content.getBytes()), desc, publicRegistry, null, publicRegistry);
	assertFalse(validator.validate());
	assertEquals(1, validator.getErrorMessages().size());
	assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));

	RegistryTestHelper.addComponent(publicRegistry, id1);
	validator = new MDValidator(new ByteArrayInputStream(content.getBytes()), desc, publicRegistry, null, publicRegistry);
	assertTrue(validator.validate());
	assertEquals(0, validator.getErrorMessages().size());
    }

    /**
     * Test of getCMDComponentSpec method, of class MDValidator.
     */
    @Test
    public void testGetCMDComponentSpec() throws Exception {
	String profileContent = getValidProfileString();
	InputStream input = new ByteArrayInputStream(profileContent.getBytes());

	ProfileDescription desc = ProfileDescription.createNewDescription();
	MDValidator validator = new MDValidator(input, desc, publicRegistry, null, publicRegistry);

	// Spec is created during validation, before it should be null
	assertNull(validator.getCMDComponentSpec());
	validator.validate();

	// Get spec created during validation
	final CMDComponentSpec cmdComponentSpec = validator.getCMDComponentSpec();
	assertNotNull(cmdComponentSpec);

	// Spec content should match XML
	assertTrue(cmdComponentSpec.isIsProfile());
	assertEquals("Actor", cmdComponentSpec.getCMDComponent().get(0).getName());

	// Spec copy should be a freshly unmarshalled copy
	final CMDComponentSpec specCopy = validator.getCopyOfCMDComponentSpec();
	assertNotSame(cmdComponentSpec, specCopy);

	// Content should still match XML
	assertTrue(specCopy.isIsProfile());
	assertEquals("Actor", specCopy.getCMDComponent().get(0).getName());
    }

    private MDValidator getValidProfileValidator() {
	final String profileContent = getValidProfileString();
	InputStream input = new ByteArrayInputStream(profileContent.getBytes());
	ProfileDescription desc = ProfileDescription.createNewDescription();
	MDValidator validator = new MDValidator(input, desc, publicRegistry, null, publicRegistry);
	return validator;
    }

    private String getValidProfileString() {
	String profileContent = "";
	profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	profileContent += "    <Header />\n";
	profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
	profileContent += "        <CMD_Element name=\"Age\">\n";
	profileContent += "            <ValueScheme>\n";
	profileContent += "                <pattern>[23][0-9]</pattern>\n";
	profileContent += "            </ValueScheme>\n";
	profileContent += "        </CMD_Element>\n";
	profileContent += "    </CMD_Component>\n";
	profileContent += "</CMD_ComponentSpec>\n";
	return profileContent;
    }
}
