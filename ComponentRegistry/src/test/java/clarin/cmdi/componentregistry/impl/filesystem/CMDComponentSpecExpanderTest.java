package clarin.cmdi.componentregistry.impl.filesystem;

import clarin.cmdi.componentregistry.Configuration;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import org.junit.Before;

public class CMDComponentSpecExpanderTest {

    private File tmpRegistryDir;
    private File registryDir;

    @Before
    public void startClean() {
        registryDir = ComponentRegistryTestCase.createTempRegistryDir();
        Configuration.getInstance().setRegistryRoot(registryDir);
        Configuration.getInstance().init();
        ComponentRegistryFactoryImpl.getInstance().reset();
        ComponentRegistryFactoryImpl.getInstance().setConfiguration(Configuration.getInstance());
    }

    @After
    public void cleanUp() {
        ComponentRegistryTestCase.cleanUpRegistryDir(registryDir);
        ComponentRegistryFactoryImpl.getInstance().reset();
        ComponentRegistryFactoryImpl.getInstance().setConfiguration(null);
        registryDir = null;
    }

    @Test
    public void testExpandProfileWithNestedComponents() throws Exception {
        ComponentRegistryImpl registry = ComponentRegistryTestCase.getTestRegistry(getRegistryDir());

        String content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
        content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc1 = RegistryTestHelper.addComponent(registry, "component1", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc2 = RegistryTestHelper.addComponent(registry, "component2", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"ZZZ\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc2.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"2\">\n";
        content += "        </CMD_Component>\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ProfileDescription profileDesc3 = RegistryTestHelper.addProfile(registry, "profile3", content);

        CMDComponentSpec expandedProfile = CMDComponentSpecExpander.expandProfile(profileDesc3.getId(), registry);

        //printToFile(expandedProfile);

        List<CMDComponentType> cmdComponents = expandedProfile.getCMDComponent();
        assertEquals(1, cmdComponents.size());
        CMDComponentType cmdComponent = cmdComponents.get(0);
        cmdComponents = cmdComponent.getCMDComponent();
        assertEquals(2, cmdComponents.size());

        cmdComponent = cmdComponents.get(0);
        assertEquals("YYY", cmdComponent.getName());
        assertEquals("clarin.eu:cr1:component2", cmdComponent.getComponentId());
        assertEquals(1, cmdComponent.getCMDComponent().size());
        assertEquals("XXX", cmdComponent.getCMDComponent().get(0).getName());
        assertEquals("clarin.eu:cr1:component1", cmdComponent.getCMDComponent().get(0).getComponentId());
        assertEquals(0, cmdComponent.getCMDComponent().get(0).getCMDComponent().size());
        cmdComponent = cmdComponents.get(1);
        assertEquals("XXX", cmdComponent.getName());
        assertEquals("clarin.eu:cr1:component1", cmdComponent.getComponentId());
        assertEquals(0, cmdComponent.getCMDComponent().size());
    }

    @Test
    public void testExpandProfileWithNestedComponentsFromUserRegistry() throws Exception {
        ComponentRegistryImpl registry = ComponentRegistryTestCase.getTestRegistry(getRegistryDir());
        ComponentRegistryImpl userRegistry = (ComponentRegistryImpl) ComponentRegistryFactoryImpl.getInstance().getComponentRegistry(true,
                DummyPrincipal.DUMMY_CREDENTIALS);

        String content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
        content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc1 = RegistryTestHelper.addComponent(userRegistry, "component1", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc2 = RegistryTestHelper.addComponent(registry, "component2", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"ZZZ\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc2.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"2\">\n";
        content += "        </CMD_Component>\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        //register in userRegistry registering in public registry is not possible through the services if one component is not public
        ProfileDescription profileDesc3 = RegistryTestHelper.addProfile(userRegistry, "profile3", content);

        CMDComponentSpec expandedProfile = CMDComponentSpecExpander.expandProfile(profileDesc3.getId(), userRegistry);

        List<CMDComponentType> cmdComponents = expandedProfile.getCMDComponent();
        assertEquals(1, cmdComponents.size());
        CMDComponentType cmdComponent = cmdComponents.get(0);
        cmdComponents = cmdComponent.getCMDComponent();
        assertEquals(2, cmdComponents.size());

        cmdComponent = cmdComponents.get(0);
        assertEquals("YYY", cmdComponent.getName());
        assertEquals(1, cmdComponent.getCMDComponent().size());
        assertEquals("XXX", cmdComponent.getCMDComponent().get(0).getName());
        assertEquals(0, cmdComponent.getCMDComponent().get(0).getCMDComponent().size());
        cmdComponent = cmdComponents.get(1);
        assertEquals("XXX", cmdComponent.getName());
        assertEquals(0, cmdComponent.getCMDComponent().size());
    }

    @Test
    public void testExpandEmbeddedWithNested() throws Exception {
        ComponentRegistryImpl registry = ComponentRegistryTestCase.getTestRegistry(getRegistryDir());

        String content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"AAA\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        content += "     <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
        content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "     </CMD_Component>\n";
        content += "     <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "     </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc2 = RegistryTestHelper.addComponent(registry, "component2", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"ZZZ\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc2.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"2\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ProfileDescription profileDesc3 = RegistryTestHelper.addProfile(registry, "profile3", content);

        CMDComponentSpec expandedProfile = CMDComponentSpecExpander.expandProfile(profileDesc3.getId(), registry);

        List<CMDComponentType> cmdComponents = expandedProfile.getCMDComponent();
        assertEquals(1, cmdComponents.size());
        CMDComponentType cmdComponent = cmdComponents.get(0);
        assertEquals("ZZZ", cmdComponent.getName());
        cmdComponents = cmdComponent.getCMDComponent();
        assertEquals(1, cmdComponents.size());

        cmdComponent = cmdComponents.get(0);
        assertEquals("AAA", cmdComponent.getName());
        cmdComponents = cmdComponent.getCMDComponent();
        assertEquals(2, cmdComponents.size());
        cmdComponent = cmdComponents.get(0);
        assertEquals("XXX", cmdComponent.getName());
        assertEquals(0, cmdComponent.getCMDComponent().size());
        cmdComponent = cmdComponents.get(1);
        assertEquals("YYY", cmdComponent.getName());
        assertEquals(0, cmdComponent.getCMDComponent().size());
    }

//    private void printToFile(CMDComponentSpec spec) throws Exception {
//        File file = new File("/tmp/test.xml");
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        MDMarshaller.marshal(spec, out);
//        FileUtils.writeStringToFile(file, out.toString("UTF-8"), "UTF-8");
//    }

    private File getRegistryDir() {
        if (tmpRegistryDir == null)
            tmpRegistryDir = ComponentRegistryTestCase.createTempRegistryDir();
        return tmpRegistryDir;
    }

    @After
    public void cleanupRegistryDir() {
        ComponentRegistryTestCase.cleanUpRegistryDir(tmpRegistryDir);
        tmpRegistryDir = null;
    }
}
