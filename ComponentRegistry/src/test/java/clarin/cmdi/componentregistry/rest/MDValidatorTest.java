package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.OwnerUser;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author george.georgovassilis@mpi.nl
 *
 */
public class MDValidatorTest extends BaseUnitTest {

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private ComponentRegistry testRegistry;

    private RegistryUser user;

    @Before
    @Override
    public void setUp() throws TransformerException, UserUnauthorizedException {
        testRegistry = componentRegistryFactory.getPublicRegistry();
        user = componentRegistryFactory.getOrCreateUser(DummyPrincipal.DUMMY_CREDENTIALS);
    }

    @Test
    public void testValidateSucces() throws UserUnauthorizedException {
        MDValidator validator = this.getValidProfileValidator();
        boolean result = validator.validate();
        assertTrue(result);
    }

    @Test
    public void testValidateIllegalComponentAttributeName() throws UserUnauthorizedException {
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        profileContent += "    <Header />\n";
        profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        profileContent += "	   <AttributeList>\n";
        profileContent += "		<Attribute>\n";
        profileContent += "		    <Name>myattribute</Name>\n"; // this should be
        // allowed
        profileContent += "		    <Type>string</Type>\n";
        profileContent += "		</Attribute>\n";
        profileContent += "		<Attribute>\n";
        profileContent += "		    <Name>ref</Name>\n"; // this should NOT be
        // allowed
        profileContent += "		    <Type>string</Type>\n";
        profileContent += "		</Attribute>\n";
        profileContent += "		<Attribute>\n";
        profileContent += "		    <Name>ComponentId</Name>\n"; // neither should
        // this
        profileContent += "		    <Type>string</Type>\n";
        profileContent += "		</Attribute>\n";
        profileContent += "	   </AttributeList>\n";
        profileContent += "        <CMD_Element name=\"Age\">\n";
        profileContent += "		<AttributeList>\n";
        profileContent += "		    <Attribute>\n";
        profileContent += "			<Name>ref</Name>\n"; // allowed here, only
        // forbidden on components
        profileContent += "			<Type>string</Type>\n";
        profileContent += "		    </Attribute>\n";
        profileContent += "		    <Attribute>\n";
        profileContent += "			<Name>ComponentId</Name>\n"; // allowed here, only
        // forbidden on
        // components
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

        MDValidator validator = new MDValidator(input, desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals("Messages expected for 2 failed tests:\n"
                + "- Actor/@ref ('ref' attribute not allowed on component)\n"
                + "- Actor/@ComponentId ('ComponentId' attribute not allowed on component)",
                2, validator.getErrorMessages().size());
        assertTrue(validator.getErrorMessages().get(0)
                .startsWith(MDValidator.PARSE_ERROR));
        assertTrue(validator.getErrorMessages().get(1)
                .startsWith(MDValidator.PARSE_ERROR));
    }

    @Test
    public void testValidateNoComponentId() throws Exception {
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        profileContent += "    <Header />\n";
        profileContent += "    <CMD_Component filename=\"component-actor.xml\"/>\n";
        profileContent += "</CMD_ComponentSpec>\n";
        InputStream input = new ByteArrayInputStream(profileContent.getBytes());

        ProfileDescription desc = ProfileDescription.createNewDescription();
        desc.setPublic(true);
        MDValidator validator = new MDValidator(input, desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertTrue(validator
                .getErrorMessages()
                .get(0)
                .startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));
        //
    }

    @Test
    public void testValidateComponentIdNotRegistered() throws Exception {
        String id1 = "component1";
        String id2 = "component2";

        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">";
        profileContent += "    <Header />";
        profileContent += "    <CMD_Component name=\"Test\">";
        profileContent += "	<CMD_Component ComponentId=\""
                + ComponentDescription.COMPONENT_PREFIX + id1 + "\"/>"; // id not
        // registered
        profileContent += "	<CMD_Component ComponentId=\""
                + ComponentDescription.COMPONENT_PREFIX + id2 + "\"/>"; // id not
        // registered
        profileContent += "    </CMD_Component>";
        profileContent += "</CMD_ComponentSpec>";

        // Ids not registered. will return 2 errors: one per each id.
        ProfileDescription desc = ProfileDescription.createNewDescription();
        desc.setPublic(true);
        MDValidator validator = new MDValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator
                .getErrorMessages()
                .get(0)
                .startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));

        // id1 will be added as public and therefore only id2 is not registered
        RegistryTestHelper.addComponent(testRegistry, id1, true);
        validator = new MDValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator
                .getErrorMessages()
                .get(0)
                .startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));

        // id2 is added as public, no more errors shoud be return
        RegistryTestHelper.addComponent(testRegistry, id2, true);
        validator = new MDValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertTrue("component is registered should be valid now",
                validator.validate());
        assertEquals(0, validator.getErrorMessages().size());
    }

    @Test
    public void testValidateBaseAndUserRegistry() throws Exception {
        String id1 = "component1";
        String id2 = "component2";

        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">";
        profileContent += "    <Header />";
        profileContent += "    <CMD_Component name=\"Test\">";
        profileContent += "	<CMD_Component ComponentId=\""
                + ComponentDescription.COMPONENT_PREFIX + id1 + "\"/>"; // id not
        // registered
        profileContent += "	<CMD_Component ComponentId=\""
                + ComponentDescription.COMPONENT_PREFIX + id2 + "\"/>"; // id not
        // registered
        profileContent += "    </CMD_Component>";
        profileContent += "</CMD_ComponentSpec>";

        // Public Registry
        ProfileDescription desc = ProfileDescription.createNewDescription();
        MDValidator validator = new MDValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size()); // the exception is thrown and propagated on the first non-registered component
        assertTrue(validator.getErrorMessages().get(0)
                .startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));

        // registering publically the first component
        RegistryTestHelper.addComponent(testRegistry, id1, true);
        validator = new MDValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size()); // the exception is thrown on the second non-registered component
        assertTrue(validator.getErrorMessages().get(0)
                .startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));

        RegistryTestHelper.addComponent(testRegistry, id2, false);
        validator = new MDValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator
                .getErrorMessages()
                .get(0)
                .startsWith(MDValidator.COMPONENT_NOT_REGISTERED_IN_APPROPRIATE_SPACE_ERROR));

        // make it user registry
        testRegistry.setRegistryOwner(new OwnerUser(user.getId()));
        testRegistry.setRegistrySpace(RegistrySpace.PRIVATE);

        validator = new MDValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        Boolean result = validator.validate();
        assertTrue(result);
        assertEquals(0, validator.getErrorMessages().size());
    }

    @Test
    public void testValidateNestedComponents() throws Exception {
        String id1 = "component1";

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        content += "    <Header />\n";
        content += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Element name=\"Name\" ValueScheme=\"string\" />\n";
        content += "      <CMD_Component ComponentId=\""
                + ComponentDescription.COMPONENT_PREFIX + id1 + "\"/>\n"; // id not
        // registered
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";

        ComponentDescription desc = ComponentDescription.createNewDescription();
        MDValidator validator = new MDValidator(new ByteArrayInputStream(
                content.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator
                .getErrorMessages()
                .get(0)
                .startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));

        RegistryTestHelper.addComponent(testRegistry, id1, true);
        validator = new MDValidator(
                new ByteArrayInputStream(content.getBytes()), desc, testRegistry, marshaller);
        assertTrue(validator.validate());
        assertEquals(0, validator.getErrorMessages().size());
    }

    /**
     * Test of getComponentSpec method, of class MDValidator.
     */
    @Test
    public void testGetCMDComponentSpec() throws Exception {
        String profileContent = getValidProfileString();
        InputStream input = new ByteArrayInputStream(profileContent.getBytes());

        ProfileDescription desc = ProfileDescription.createNewDescription();
        MDValidator validator = new MDValidator(input, desc, testRegistry, marshaller);

        // Spec is created during validation, before it should be null
        assertNull(validator.getComponentSpec());
        validator.validate();

        // Get spec created during validation
        final ComponentSpec cmdComponentSpec = validator
                .getComponentSpec();
        assertNotNull(cmdComponentSpec);

        // Spec content should match XML
        assertTrue(cmdComponentSpec.isIsProfile());
        assertEquals("Actor", cmdComponentSpec.getComponent().getName());

        // Spec copy should be a freshly unmarshalled copy
        final ComponentSpec specCopy = validator.getCopyOfCMDComponentSpec();
        assertNotSame(cmdComponentSpec, specCopy);

        // Content should still match XML
        assertTrue(specCopy.isIsProfile());
        assertEquals("Actor", specCopy.getComponent().getName());
    }

    private MDValidator getValidProfileValidator() {
        final String profileContent = getValidProfileString();
        InputStream input = new ByteArrayInputStream(profileContent.getBytes());
        ProfileDescription desc = ProfileDescription.createNewDescription();
        desc.setPublic(true);
        desc.setName("test_name");
        MDValidator validator = new MDValidator(input, desc, testRegistry, marshaller);
        return validator;
    }

    private String getValidProfileString() {
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
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
