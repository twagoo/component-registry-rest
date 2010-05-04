package clarin.cmdi.componentregistry;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

public class ComponentRegistryImplTest {

    private File tmpRegistryDir;
    private final static DummyPrincipal PRINCIPAL = DummyPrincipal.DUMMY_PRINCIPAL;

    @Test
    public void testRegisterMDProfile() throws JAXBException {
        ComponentRegistry register = getTestRegistry(getRegistryDir());
        ProfileDescription description = ProfileDescription.createNewDescription();
        description.setName("Aap");
        description.setDescription("MyDescription");

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());

        CMDComponentSpec testProfile = RegistryTestHelper.getTestProfile();
        assertNull(testProfile.getHeader().getID());
        assertNull(testProfile.getHeader().getName());
        assertNull(testProfile.getHeader().getDescription());

        register.registerMDProfile(description, testProfile);

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(1, register.getProfileDescriptions().size());
        ProfileDescription desc = register.getProfileDescriptions().get(0);
        assertNull(register.getMDComponent(desc.getId()));

        CMDComponentSpec profile = register.getMDProfile(desc.getId());
        assertNotNull(profile);
        assertEquals("Header id should be set from description id", description.getId(), profile.getHeader().getID());
        assertEquals("Aap", profile.getHeader().getName());
        assertEquals("MyDescription", profile.getHeader().getDescription());
    }

    @Test
    public void testRegisterMDComponent() throws JAXBException {
        ComponentRegistry register = getTestRegistry(getRegistryDir());
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

        register.registerMDComponent(description, testComponent);

        assertEquals(1, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());
        ComponentDescription desc = register.getComponentDescriptions().get(0);
        assertNull(register.getMDProfile(desc.getId()));

        CMDComponentSpec component = register.getMDComponent(desc.getId());
        assertNotNull(component);
        assertEquals("Header id should be set from description id", description.getId(), component.getHeader().getID());
        assertEquals("Aap", component.getHeader().getName());
        assertEquals("Will not be overwritten", component.getHeader().getDescription());
    }

    @Test
    public void testCache() throws JAXBException {
        ComponentRegistryImpl register = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        Configuration config = new Configuration();
        config.setRegistryRoot(getRegistryDir());
        config.init();
        register.setConfiguration(config);

        ProfileDescription description = new ProfileDescription();
        description.setName("Aap");
        String id = "Aap" + System.currentTimeMillis();
        description.setId(id);

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());
        register.registerMDProfile(description, RegistryTestHelper.getTestProfile());
        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(1, register.getProfileDescriptions().size());
        assertNull(register.getMDComponent(id));
        assertNotNull(register.getMDProfile(id));

        register.setConfiguration(config); //triggers cache
        assertEquals(1, register.getProfileDescriptions().size());
        assertEquals(0, register.getComponentDescriptions().size());
        assertNotNull(register.getMDProfile(id));
    }

    @Test
    public void testCacheCorruptFile() throws JAXBException {
        ComponentRegistryImpl register = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        Configuration config = new Configuration();
        config.setRegistryRoot(getRegistryDir());
        config.init();
        register.setConfiguration(config);

        ProfileDescription description = new ProfileDescription();
        description.setName("Aap");
        String id = "Aap" + System.currentTimeMillis();
        description.setId(id);

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());
        register.registerMDProfile(description, RegistryTestHelper.getTestProfile());
        description = new ProfileDescription();
        description.setName("Aap2");
        String id2 = "Aap2" + System.currentTimeMillis();
        description.setId(id2);
        register.registerMDProfile(description, RegistryTestHelper.getTestProfile());

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(2, register.getProfileDescriptions().size());
        assertNull(register.getMDComponent(id));
        assertNotNull(register.getMDProfile(id));
        assertNotNull(register.getMDProfile(id2));

        File profileFile = new File(config.getProfileDir(), id + File.separator + id + ".xml");
        assertTrue(profileFile.exists());
        assertTrue(profileFile.delete()); //profile file deleted so file system corrupt file should no longer be loaded in cache
        assertFalse(profileFile.exists());

        register.setConfiguration(config); //triggers cache
        assertEquals(1, register.getProfileDescriptions().size());
        assertEquals(0, register.getComponentDescriptions().size());
        assertNull(register.getMDProfile(id));
        assertNotNull(register.getMDProfile(id2));
    }

    @Test
    public void testGetProfileAsXsd() throws Exception {
        ComponentRegistry register = getTestRegistry(getRegistryDir());
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

        String xsd = register.getMDProfileAsXsd(description.getId());
        assertTrue(xsd.endsWith("</xs:schema>"));

        assertTrue(hasComponent(xsd, "Actor", "0", "unbounded"));
    }

    @Test
    public void testGetNestedComponentAsXsd() throws Exception {
        ComponentRegistry register = getTestRegistry(getRegistryDir());

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

        String xsd = register.getMDProfileAsXsd(description.getId());

        assertTrue(xsd.endsWith("</xs:schema>"));
        assertTrue(hasComponent(xsd, "Actor", "0", "5"));
    }

    @Test
    public void testGetNestedComponentAsXsdComplex() throws Exception {
        ComponentRegistry register = getTestRegistry(getRegistryDir());

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
        compContent += "    <CMD_Component name=\"ZZZ\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        compContent += "        <CMD_Component ComponentId=\"" + compDesc2.getId()
                + "\" filename=\"component-test-file\" CardinalityMin=\"0\" CardinalityMax=\"2\">\n";
        compContent += "        </CMD_Component>\n";
        compContent += "        <CMD_Component ComponentId=\"" + compDesc1.getId()
                + "\" filename=\"component-test-file\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        compContent += "        </CMD_Component>\n";
        compContent += "    </CMD_Component>\n";
        compContent += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc3 = RegistryTestHelper.addComponent(register, "component3", compContent);

        String xsd = register.getMDComponentAsXsd(compDesc3.getId());
        assertTrue(xsd.endsWith("</xs:schema>"));
        System.out.println(xsd);

        assertTrue(hasComponent(xsd, "ZZZ", "1", "unbounded"));
        assertTrue(hasComponent(xsd, "YYY", "0", "2"));
        assertTrue(hasComponent(xsd, "XXX", "1", "10"));
        assertTrue(hasComponent(xsd, "XXX", "0", "99"));
    }

    @Test
    public void testDeleteProfile() throws Exception {
        ComponentRegistry register = getTestRegistry(getRegistryDir());
        ProfileDescription description = ProfileDescription.createNewDescription();
        description.setName("Aap");
        description.setCreatorName(PRINCIPAL.getName());
        description.setDescription("MyDescription");
        CMDComponentSpec testProfile = RegistryTestHelper.getTestProfile();

        register.registerMDProfile(description, testProfile);

        assertEquals(1, register.getProfileDescriptions().size());
        assertNotNull(register.getMDProfile(description.getId()));

        try {
            register.deleteMDProfile(description.getId(), new DummyPrincipal("Fake User"));
            fail("Should have thrown exception");
        } catch (UserUnauthorizedException e) {
        }
        register.deleteMDComponent(description.getId(), new DummyPrincipal("Fake User"));

        assertEquals(1, register.getProfileDescriptions().size());
        assertNotNull(register.getMDProfile(description.getId()));

        register.deleteMDProfile(description.getId(), PRINCIPAL);

        assertEquals(0, register.getProfileDescriptions().size());
        assertNull(register.getMDProfile(description.getId()));
    }

    @Test
    public void testDeleteComponent() throws Exception {
        ComponentRegistry register = getTestRegistry(getRegistryDir());
        ComponentDescription description = ComponentDescription.createNewDescription();
        description.setName("Aap");
        description.setCreatorName(PRINCIPAL.getName());
        description.setDescription("MyDescription");
        CMDComponentSpec testComp = RegistryTestHelper.getTestComponent();

        register.registerMDComponent(description, testComp);
        try {
            register.deleteMDComponent(description.getId(), new DummyPrincipal("Fake User"));
            fail("Should have thrown exception");
        } catch (UserUnauthorizedException e) {
        }

        assertEquals(1, register.getComponentDescriptions().size());
        assertNotNull(register.getMDComponent(description.getId()));

        register.deleteMDComponent(description.getId(), PRINCIPAL);

        assertEquals(0, register.getComponentDescriptions().size());
        assertNull(register.getMDProfile(description.getId()));
    }

    @Test
    public void testUpdateDescription() throws Exception {
        ComponentRegistryImpl registry = getTestRegistry(getRegistryDir());
        ComponentDescription description = ComponentDescription.createNewDescription();
        description.setName("Aap");
        description.setCreatorName(PRINCIPAL.getName());
        description.setDescription("MyDescription");
        CMDComponentSpec testProfile = RegistryTestHelper.getTestProfile();
        registry.registerMDComponent(description, testProfile);

        assertEquals(1, registry.getComponentDescriptions().size());
        ComponentDescription desc = registry.getComponentDescription(description.getId());
        assertEquals("MyDescription", desc.getDescription());
        desc.setDescription("NewDesc");
        registry.updateDescription(desc, PRINCIPAL);

        registry = getTestRegistry(getRegistryDir());
        assertEquals(1, registry.getComponentDescriptions().size());
        ComponentDescription result = registry.getComponentDescription(description.getId());
        assertEquals("NewDesc", result.getDescription());
        assertEquals("Aap", result.getName());
    }

    private File getRegistryDir() {
        if (tmpRegistryDir == null)
            tmpRegistryDir = createTempRegistryDir();
        return tmpRegistryDir;
    }

    @After
    public void cleanupRegistryDir() {
        cleanUpRegistryDir(tmpRegistryDir);
        tmpRegistryDir = null;
    }

    public static ComponentRegistryImpl getTestRegistry(File registryRoot) {
        ComponentRegistryImpl register = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        Configuration config = new Configuration();
        config.setRegistryRoot(registryRoot);
        Set<String> adminUsers = new HashSet<String>();
        adminUsers.add(PRINCIPAL.getName());
        config.setAdminUsers(adminUsers);
        config.init();
        register.setConfiguration(config);
        return register;
    }

    public static File createTempRegistryDir() {
        final String baseTempPath = System.getProperty("java.io.tmpdir");
        File tempDir = new File(baseTempPath + File.separator + "testRegistry_" + System.currentTimeMillis());
        tempDir.mkdir();
        tempDir.deleteOnExit();
        return tempDir;
    }

    public static void cleanUpRegistryDir(File registryDir) {
        if (registryDir != null && registryDir.exists()) {
            assertTrue(FileUtils.deleteQuietly(registryDir));
        }
    }

    /**
     * Testing a big xsd string is a bit hard, so doing a best effort by checking the xs:element which represent the nested components used
     * in a profile/component
     */
    private boolean hasComponent(String xsd, String name, String min, String max) {
        Pattern pattern = Pattern.compile("<xs:element name=\"" + name + "\" minOccurs=\"" + min + "\" maxOccurs=\"" + max + "\">");
        Matcher matcher = pattern.matcher(xsd);
        return matcher.find() && !matcher.find(); //find only one
    }
}
