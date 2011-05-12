package clarin.cmdi.componentregistry.impl.filesystem;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

public class ComponentRegistryImplTest extends ComponentRegistryTestCase {

    @Test
    public void testRegisterProfile() throws JAXBException {
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

        register.register(description, testProfile);

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
    public void testRegisterComponent() throws JAXBException {
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

        register.register(description, testComponent);

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
        ResourceConfig config = new ResourceConfig();
        config.setResourceRoot(getRegistryDir());
        config.init();
        ComponentRegistryImpl register = new ComponentRegistryImpl(false);
        register.setResourceConfig(config);

        ProfileDescription description = ProfileDescription.createNewDescription();
        description.setName("Aap");

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());
        register.register(description, RegistryTestHelper.getTestProfile());
        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(1, register.getProfileDescriptions().size());
        assertNull(register.getMDComponent(description.getId()));
        assertNotNull(register.getMDProfile(description.getId()));

        register.setResourceConfig(config); //triggers cache
        assertEquals(1, register.getProfileDescriptions().size());
        assertEquals(0, register.getComponentDescriptions().size());
        assertNotNull(register.getMDProfile(description.getId()));
    }

    @Test
    public void testCacheLoadCorrect() throws Exception {
        ComponentRegistryImpl publicReg = getTestRegistry(getRegistryDir());
        ComponentRegistryImpl userReg = (ComponentRegistryImpl) ComponentRegistryFactoryImpl.getInstance().getComponentRegistry(true,
                DummyPrincipal.DUMMY_CREDENTIALS);

        ProfileDescription pDesc = ProfileDescription.createNewDescription();
        pDesc.setName("Aap1");
        pDesc.setUserId(DummyPrincipal.DUMMY_CREDENTIALS.getPrincipalNameMD5Hex());
        ComponentDescription cDesc = ComponentDescription.createNewDescription();
        cDesc.setName("Aap2");
        cDesc.setUserId(DummyPrincipal.DUMMY_CREDENTIALS.getPrincipalNameMD5Hex());

        assertEquals(0, userReg.getComponentDescriptions().size());
        assertEquals(0, userReg.getProfileDescriptions().size());
        assertEquals(0, publicReg.getComponentDescriptions().size());
        assertEquals(0, publicReg.getProfileDescriptions().size());
        userReg.register(cDesc, RegistryTestHelper.getTestComponent());
        publicReg.register(cDesc, RegistryTestHelper.getTestComponent());
        publicReg.register(pDesc, RegistryTestHelper.getTestProfile());
        userReg.register(pDesc, RegistryTestHelper.getTestProfile());
        assertEquals(1, userReg.getComponentDescriptions().size());
        assertEquals(1, userReg.getProfileDescriptions().size());
        assertEquals(1, publicReg.getComponentDescriptions().size());
        assertEquals(1, publicReg.getProfileDescriptions().size());

        publicReg.deleteMDProfile(pDesc.getId(), USER_CREDS.getPrincipal());
        userReg.initCache();
        publicReg.initCache();
        assertEquals(1, userReg.getComponentDescriptions().size());
        assertEquals(1, userReg.getProfileDescriptions().size());
        assertEquals(1, publicReg.getComponentDescriptions().size());
        assertEquals(0, publicReg.getProfileDescriptions().size());

        userReg.deleteMDComponent(cDesc.getId(), USER_CREDS.getPrincipal(), false);
        userReg.initCache();
        publicReg.initCache();
        assertEquals(0, userReg.getComponentDescriptions().size());
        assertEquals(1, userReg.getProfileDescriptions().size());
        assertEquals(1, publicReg.getComponentDescriptions().size());
        assertEquals(0, publicReg.getProfileDescriptions().size());

        publicReg.deleteMDComponent(cDesc.getId(), USER_CREDS.getPrincipal(), false);
        userReg.deleteMDProfile(pDesc.getId(), USER_CREDS.getPrincipal());
        userReg.initCache();
        publicReg.initCache();
        assertEquals(0, userReg.getComponentDescriptions().size());
        assertEquals(0, userReg.getProfileDescriptions().size());
        assertEquals(0, publicReg.getComponentDescriptions().size());
        assertEquals(0, publicReg.getProfileDescriptions().size());
    }

    @Test
    public void testCacheCorruptFile() throws JAXBException {
        ResourceConfig config = new ResourceConfig();
        config.setResourceRoot(getRegistryDir());
        config.init();
        ComponentRegistryImpl register = new ComponentRegistryImpl(false);
        register.setResourceConfig(config);

        ProfileDescription description = new ProfileDescription();
        description.setName("Aap");
        String id = "Aap" + System.currentTimeMillis();
        description.setId(id);

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());
        register.register(description, RegistryTestHelper.getTestProfile());
        description = new ProfileDescription();
        description.setName("Aap2");
        String id2 = "Aap2" + System.currentTimeMillis();
        description.setId(id2);
        register.register(description, RegistryTestHelper.getTestProfile());

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(2, register.getProfileDescriptions().size());
        assertNull(register.getMDComponent(id));
        assertNotNull(register.getMDProfile(id));
        assertNotNull(register.getMDProfile(id2));

        File profileFile = new File(config.getProfileDir(), id + File.separator + id + ".xml");
        assertTrue(profileFile.exists());
        assertTrue(profileFile.delete());
        assertFalse(profileFile.exists());

        register.setResourceConfig(config); //triggers cache
        assertEquals(2, register.getProfileDescriptions().size()); //Description will still be shown, CMDSpec will be null though
        assertEquals(0, register.getComponentDescriptions().size());
        assertNull(register.getMDProfile(id));
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

        OutputStream output = new ByteArrayOutputStream();
        register.getMDProfileAsXsd(description.getId(), output);
        String xsd = output.toString();
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

        OutputStream output = new ByteArrayOutputStream();
        register.getMDProfileAsXsd(description.getId(), output);
        String xsd = output.toString();

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

        assertTrue(hasComponent(xsd, "ZZZ\u00e9", "1", "unbounded"));
        assertTrue(hasComponent(xsd, "YYY", "0", "2"));
        assertTrue(hasComponent(xsd, "XXX", "1", "10"));
        assertTrue(hasComponent(xsd, "XXX", "0", "99"));
    }

    @Test
    public void testDeleteProfile() throws Exception {
        ComponentRegistry register = getTestRegistry(getRegistryDir());
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
        ComponentRegistry registry = getTestRegistry(getRegistryDir());
        ComponentDescription description = ComponentDescription.createNewDescription();
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

    @Test
    public void testDoNotDeleteOldPublicComponent() throws Exception {
        ComponentRegistry registry = getTestRegistry(getRegistryDir());
        ComponentDescription description = ComponentDescription.createNewDescription();
        description.setName("Aap");
        description.setCreatorName(USER_CREDS.getDisplayName());
        description.setUserId(USER_CREDS.getPrincipalNameMD5Hex());
        description.setDescription("MyDescription");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1999);
        description.setRegistrationDate(DateFormatUtils.formatUTC(calendar.getTime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()));
        CMDComponentSpec testComp = RegistryTestHelper.getTestComponent();

        registry.register(description, testComp);
        try {
            registry.deleteMDComponent(description.getId(), USER_CREDS.getPrincipal(), false);
            fail("Should have thrown exception");
        } catch (DeleteFailedException e) {
        }
        assertEquals(1, registry.getComponentDescriptions().size());
        registry.deleteMDComponent(description.getId(), PRINCIPAL_ADMIN, false);
        assertEquals(0, registry.getComponentDescriptions().size());

        registry = ComponentRegistryFactoryImpl.getInstance().getComponentRegistry(true, USER_CREDS); //user registry
        registry.register(description, testComp);
        assertEquals(1, registry.getComponentDescriptions().size());
        registry.deleteMDComponent(description.getId(), USER_CREDS.getPrincipal(), false); //user workspace can always delete
        assertEquals(0, registry.getComponentDescriptions().size());
    }

    @Test
    public void testDoNotDeleteOldPublicProfile() throws Exception {
        ComponentRegistry registry = getTestRegistry(getRegistryDir());
        ProfileDescription description = ProfileDescription.createNewDescription();
        description.setName("Aap");
        description.setCreatorName(USER_CREDS.getDisplayName());
        description.setUserId(USER_CREDS.getPrincipalNameMD5Hex());
        description.setDescription("MyDescription");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1999);
        description.setRegistrationDate(DateFormatUtils.formatUTC(calendar.getTime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()));
        CMDComponentSpec testComp = RegistryTestHelper.getTestProfile();

        registry.register(description, testComp);
        try {
            registry.deleteMDProfile(description.getId(), USER_CREDS.getPrincipal());
            fail("Should have thrown exception");
        } catch (DeleteFailedException e) {
        }
        assertEquals(1, registry.getProfileDescriptions().size());
        registry.deleteMDProfile(description.getId(), PRINCIPAL_ADMIN);
        assertEquals(0, registry.getProfileDescriptions().size());

        registry = ComponentRegistryFactoryImpl.getInstance().getComponentRegistry(true, USER_CREDS); //user registry
        registry.register(description, testComp);
        assertEquals(1, registry.getProfileDescriptions().size());
        registry.deleteMDProfile(description.getId(), USER_CREDS.getPrincipal()); //user workspace can always delete
        assertEquals(0, registry.getProfileDescriptions().size());
    }

    @Test
    public void testUpdateDescription() throws Exception {
        ComponentRegistryImpl registry = getTestRegistry(getRegistryDir());
        ComponentDescription description = ComponentDescription.createNewDescription();
        description.setName("Aap");
        description.setCreatorName(USER_CREDS.getDisplayName());
        description.setUserId(USER_CREDS.getPrincipalName());
        description.setDescription("MyDescription");
        CMDComponentSpec testComponent = RegistryTestHelper.getTestComponent();
        registry.register(description, testComponent);

        assertEquals(1, registry.getComponentDescriptions().size());
        ComponentDescription desc = registry.getComponentDescription(description.getId());
        assertEquals("MyDescription", desc.getDescription());
        desc.setDescription("NewDesc");
        registry.update(desc, testComponent);

        registry = getTestRegistry(getRegistryDir());
        assertEquals(1, registry.getComponentDescriptions().size());
        ComponentDescription result = registry.getComponentDescription(description.getId());
        assertEquals("NewDesc", result.getDescription());
        assertEquals("Aap", result.getName());
    }

    @Test
    public void testGetUsageInProfiles() throws Exception {
        ComponentRegistryImpl registry = getTestRegistry(getRegistryDir());
        ComponentDescription cd = ComponentDescription.createNewDescription();
        cd.setName("Y");
        CMDComponentSpec testComponent = RegistryTestHelper.getTestComponent();
        registry.register(cd, testComponent);

        List<ProfileDescription> result = registry.getUsageInProfiles(cd.getId());
        assertEquals(0, result.size());

        ProfileDescription pd = ProfileDescription.createNewDescription();
        pd.setName("X");
        CMDComponentSpec testProfile = RegistryTestHelper.getTestProfile();
        CMDComponentType reference = new CMDComponentType();
        reference.setComponentId(cd.getId());
        testProfile.getCMDComponent().get(0).getCMDComponent().add(reference);
        registry.register(pd, testProfile);

        result = registry.getUsageInProfiles(cd.getId());
        assertEquals(1, result.size());
    }

    @Test
    public void testGetUsageInComponents() throws Exception {
        ComponentRegistryImpl registry = getTestRegistry(getRegistryDir());
        ComponentDescription cd = ComponentDescription.createNewDescription();
        cd.setName("Y");
        registry.register(cd, RegistryTestHelper.getTestComponent());

        List<ComponentDescription> result = registry.getUsageInComponents(cd.getId());
        assertEquals(0, result.size());

        ComponentDescription cd2 = ComponentDescription.createNewDescription();
        cd2.setName("X");
        CMDComponentSpec testComponent = RegistryTestHelper.getTestProfile();
        CMDComponentType reference = new CMDComponentType();
        reference.setComponentId(cd.getId());
        testComponent.getCMDComponent().get(0).getCMDComponent().add(reference);
        registry.register(cd2, testComponent);

        ComponentDescription cd3 = ComponentDescription.createNewDescription();
        cd3.setName("X2");
        testComponent = RegistryTestHelper.getTestProfile();
        reference = new CMDComponentType();
        reference.setComponentId(cd.getId());
        testComponent.getCMDComponent().get(0).getCMDComponent().add(reference);
        registry.register(cd3, testComponent);

        result = registry.getUsageInComponents(cd.getId());
        assertEquals(2, result.size());
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
