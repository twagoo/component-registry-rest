package clarin.cmdi.componentregistry.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.ParseException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ComponentRegistryImplTest;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class MDValidatorTest {

    private static File registryDir;
    private static ComponentRegistry testRegistry;

    @Before
    public void setUpTestRegistry() throws ParseException, JAXBException {
        registryDir = ComponentRegistryImplTest.createTempRegistryDir();
        testRegistry = ComponentRegistryImplTest.getTestRegistry(registryDir);
    }

    @After
    public void deleteRegistry() {
        ComponentRegistryImplTest.cleanUpRegistryDir(registryDir);
    }

    @Test
    public void testValidateSucces() {
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
        InputStream input = new ByteArrayInputStream(profileContent.getBytes());

        ProfileDescription desc = ProfileDescription.createNewDescription();
        MDValidator validator = new MDValidator(input, desc, testRegistry);
        assertTrue(validator.validate());
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
        MDValidator validator = new MDValidator(input, desc, testRegistry);
        assertFalse(validator.validate());
        assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));
    }

    @Test
    public void testValidateComponentIdNotRegistered() throws Exception {
        String id1 = "component1";
        String id2 = "component2";

        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        profileContent += "    <Header />\n";
        profileContent += "    <CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + id1 + "\"/>\n"; //id not registered
        profileContent += "    <CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + id2 + "\"/>\n"; //id not registered
        profileContent += "</CMD_ComponentSpec>\n";

        ProfileDescription desc = ProfileDescription.createNewDescription();
        MDValidator validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, testRegistry);
        assertFalse(validator.validate());
        assertEquals(2, validator.getErrorMessages().size());
        assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));
        assertTrue(validator.getErrorMessages().get(1).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));

        RegistryTestHelper.addComponent(testRegistry, id1);
        validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, testRegistry);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));

        RegistryTestHelper.addComponent(testRegistry, id2);
        validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, testRegistry);
        assertTrue("component is registered should be valid now", validator.validate());
        assertEquals(0, validator.getErrorMessages().size());
    }

    @Test
    public void testValidateUserRegistry() throws Exception {
        String id1 = "component1";
        String id2 = "component2";
        ComponentRegistry userRegistry = ComponentRegistryFactory.getInstance().getComponentRegistry(true, DummyPrincipal.DUMMY_PRINCIPAL);

        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        profileContent += "    <Header />\n";
        profileContent += "    <CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + id1 + "\"/>\n"; //id not registered
        profileContent += "    <CMD_Component ComponentId=\"" + ComponentRegistry.REGISTRY_ID + id2 + "\"/>\n"; //id not registered
        profileContent += "</CMD_ComponentSpec>\n";

        ProfileDescription desc = ProfileDescription.createNewDescription();
        MDValidator validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, userRegistry);
        assertFalse(validator.validate());
        assertEquals(2, validator.getErrorMessages().size());
        assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));
        assertTrue(validator.getErrorMessages().get(1).startsWith(MDValidator.COMPONENT_NOT_REGISTERED_ERROR));

        RegistryTestHelper.addComponent(userRegistry, id1);
        RegistryTestHelper.addComponent(testRegistry, id2);
        validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, testRegistry);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));

        validator = new MDValidator(new ByteArrayInputStream(profileContent.getBytes()), desc, userRegistry);
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
        MDValidator validator = new MDValidator(new ByteArrayInputStream(content.getBytes()), desc, testRegistry);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator.getErrorMessages().get(0).startsWith(MDValidator.COMPONENT_NOT_PUBLICLY_REGISTERED_ERROR));

        RegistryTestHelper.addComponent(testRegistry, id1);
        validator = new MDValidator(new ByteArrayInputStream(content.getBytes()), desc, testRegistry);
        assertTrue(validator.validate());
        assertEquals(0, validator.getErrorMessages().size());

    }

}
