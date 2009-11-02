package clarin.cmdi.componentregistry.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryImplTest;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class ComponentRegistryRestServiceTest {

    private static ComponentRegistry testRegistry;
    private static File registryDir;

    @Test
    public void testGetRegisteredProfiles() throws Exception {
        ComponentRegistryRestService service = new ComponentRegistryRestService();
        String profiles = service.getRegisteredProfiles();
        String expected = "";
        expected += "<profiles>\n";
        expected += "<profile xlink=\"link:profile1\" registrationDate=\"Thu Jan 01 00:00:00 CET 2009\" name=\"testProfile\" id=\"profile1\" description=\"Test Description\" creatorName=\"J. Unit\"/>\n";
        expected += "<profile xlink=\"link:profile2\" registrationDate=\"Thu Jan 01 00:00:00 CET 2009\" name=\"testProfile\" id=\"profile2\" description=\"Test Description\" creatorName=\"J. Unit\"/>\n";
        expected += "</profiles>\n";
        assertEquals(expected, profiles);
    }

    @Test
    public void testGetRegisteredComponents() {
        ComponentRegistryRestService service = new ComponentRegistryRestService();
        String components = service.getRegisteredComponents();
        String expected = "";
        expected += "<components>\n";
        expected += "<component xlink=\"link:component1\" registrationDate=\"Thu Jan 01 00:00:00 CET 2009\" name=\"testComponent\" id=\"component1\" description=\"Test Description\" creatorName=\"J. Unit\"/>\n";
        expected += "<component xlink=\"link:component2\" registrationDate=\"Thu Jan 01 00:00:00 CET 2009\" name=\"testComponent\" id=\"component2\" description=\"Test Description\" creatorName=\"J. Unit\"/>\n";
        expected += "</components>\n";
        assertEquals(expected, components);
    }

    @Test
    public void testGetRegisteredComponent() {
        ComponentRegistryRestService service = new ComponentRegistryRestService();
        String components = service.getRegisteredComponent("clarin.eu:cr1:component1");
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        expected += "\n";
        expected += "<CMD_ComponentSpec xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        expected += "    xsi:noNamespaceSchemaLocation=\"../../general-component-schema.xsd\">\n";
        expected += "    \n";
        expected += "    <Header/>\n";
        expected += "    \n";
        expected += "    <CMD_Component name=\"Access\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        expected += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        expected += "        <CMD_Element name=\"Date\">\n";
        expected += "            <ValueScheme>\n";
        expected += "                <!-- matching dates of the pattern yyyy-mm-dd (ISO 8601); this only matches dates from the years 1000 through 2999 and does allow some invalid dates (e.g. February, the 30th) -->\n";
        expected += "                <pattern>(1|2)\\d{3}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])</pattern>                \n";
        expected += "            </ValueScheme>\n";
        expected += "        </CMD_Element>\n";
        expected += "        <CMD_Element name=\"Owner\" ValueScheme=\"string\" />\n";
        expected += "        <CMD_Element name=\"Publisher\" ValueScheme=\"string\" />\n";
        expected += "    </CMD_Component>\n";
        expected += "\n";
        expected += "</CMD_ComponentSpec>\n";
        assertEquals(expected, components);
    }

    @Test
    public void testGetRegisteredProfile() throws Exception {
        ComponentRegistryRestService service = new ComponentRegistryRestService();
        String components = service.getRegisteredProfile("clarin.eu:cr1:profile2");
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        expected += "<CMD_ComponentSpec xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        expected += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        expected += "    <Header />\n";
        expected += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        expected += "        <AttributeList>\n";
        expected += "            <Attribute>\n";
        expected += "                <Name>Name</Name>\n";
        expected += "                <Type>string</Type>\n";
        expected += "            </Attribute>\n";
        expected += "        </AttributeList>\n";
        expected += "        <CMD_Element name=\"Age\">\n";
        expected += "            <ValueScheme>\n";
        expected += "                <pattern>[23][0-9]</pattern>\n";
        expected += "            </ValueScheme>\n";
        expected += "        </CMD_Element>\n";
        expected += "    </CMD_Component>\n";
        expected += "</CMD_ComponentSpec>\n";
        assertEquals(expected, components);
    }

    @BeforeClass
    public static void setUpTestRegistry() throws ParseException {
        registryDir = ComponentRegistryImplTest.createTempRegistryDir();
        testRegistry = ComponentRegistryImplTest.getTestRegistry(registryDir);
        addProfile(testRegistry, "profile1");
        addProfile(testRegistry, "profile2");
        addComponent(testRegistry, "component1");
        addComponent(testRegistry, "component2");
    }

    private static void addProfile(ComponentRegistry testRegistry, String id) throws ParseException {
        ProfileDescription desc = new ProfileDescription();
        desc.setCreatorName("J. Unit");
        desc.setName("testProfile");
        desc.setRegistrationDate("" + SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse("1/1/2009"));
        desc.setDescription("Test Description");
        desc.setId(id);
        desc.setXlink("link:" + id);

        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        profileContent += "    <Header />\n";
        profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        profileContent += "        <AttributeList>\n";
        profileContent += "            <Attribute>\n";
        profileContent += "                <Name>Name</Name>\n";
        profileContent += "                <Type>string</Type>\n";
        profileContent += "            </Attribute>\n";
        profileContent += "        </AttributeList>\n";
        profileContent += "        <CMD_Element name=\"Age\">\n";
        profileContent += "            <ValueScheme>\n";
        profileContent += "                <pattern>[23][0-9]</pattern>\n";
        profileContent += "            </ValueScheme>\n";
        profileContent += "        </CMD_Element>\n";
        profileContent += "    </CMD_Component>\n";
        profileContent += "</CMD_ComponentSpec>\n";
        testRegistry.registerMDProfile(desc, profileContent);
    }

    private static void addComponent(ComponentRegistry testRegistry, String id) throws ParseException {
        ComponentDescription desc = new ComponentDescription();
        desc.setCreatorName("J. Unit");
        desc.setName("testComponent");
        desc.setRegistrationDate("" + SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse("1/1/2009"));
        desc.setDescription("Test Description");
        desc.setId(id);
        desc.setXlink("link:" + id);

        String compContent = "";
        compContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        compContent += "\n";
        compContent += "<CMD_ComponentSpec xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        compContent += "    xsi:noNamespaceSchemaLocation=\"../../general-component-schema.xsd\">\n";
        compContent += "    \n";
        compContent += "    <Header/>\n";
        compContent += "    \n";
        compContent += "    <CMD_Component name=\"Access\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        compContent += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        compContent += "        <CMD_Element name=\"Date\">\n";
        compContent += "            <ValueScheme>\n";
        compContent += "                <!-- matching dates of the pattern yyyy-mm-dd (ISO 8601); this only matches dates from the years 1000 through 2999 and does allow some invalid dates (e.g. February, the 30th) -->\n";
        compContent += "                <pattern>(1|2)\\d{3}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])</pattern>                \n";
        compContent += "            </ValueScheme>\n";
        compContent += "        </CMD_Element>\n";
        compContent += "        <CMD_Element name=\"Owner\" ValueScheme=\"string\" />\n";
        compContent += "        <CMD_Element name=\"Publisher\" ValueScheme=\"string\" />\n";
        compContent += "    </CMD_Component>\n";
        compContent += "\n";
        compContent += "</CMD_ComponentSpec>\n";

        testRegistry.registerMDComponent(desc, compContent);
    }

    @AfterClass
    public static void deleteRegistry() {
        if (registryDir != null && registryDir.exists()) {
            assertTrue(FileUtils.deleteQuietly(registryDir));
        }
    }

}
