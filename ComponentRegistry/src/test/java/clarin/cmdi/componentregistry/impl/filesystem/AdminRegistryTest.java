package clarin.cmdi.componentregistry.impl.filesystem;

import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.impl.filesystem.AdminRegistry;
import clarin.cmdi.componentregistry.impl.filesystem.ComponentRegistryImpl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import clarin.cmdi.componentregistry.frontend.FileInfo;
import clarin.cmdi.componentregistry.frontend.FileNode;
import clarin.cmdi.componentregistry.frontend.SubmitFailedException;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

public class AdminRegistryTest extends ComponentRegistryTestCase {

    @Test
    public void testForceUpdate() throws Exception {
        ComponentRegistryImpl testRegistry = getTestRegistry();
        String content = "";
        content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
        content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ComponentDescription compDesc1 = RegistryTestHelper.addComponent(testRegistry, "XXX1", content);

        content = "";
        content += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        content += "    <Header/>\n";
        content += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
        content += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </CMD_Component>\n";
        content += "    </CMD_Component>\n";
        content += "</CMD_ComponentSpec>\n";
        ProfileDescription profileDesc = RegistryTestHelper.addProfile(testRegistry, "YYY1", content);

        AdminRegistry adminReg = new AdminRegistry();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setForceUpdate(false);
        fileInfo.setDisplayNode(new FileNode(new File(getRegistryDir(), "components" + File.separator + compDesc1.getName() + File.separator
                + compDesc1.getName() + ".xml"), false));
        try {
            adminReg.submitFile(fileInfo, PRINCIPAL_ADMIN);
            fail();
        } catch (SubmitFailedException e) {
            assertTrue(e.getCause() instanceof DeleteFailedException);
        }
        fileInfo.setForceUpdate(true);
        adminReg.submitFile(fileInfo, PRINCIPAL_ADMIN); //Component needs to be forced because they can be used by other profiles/components

        assertEquals(1, testRegistry.getComponentDescriptions().size());
        try {
            fileInfo.setForceUpdate(false);
            adminReg.delete(fileInfo, PRINCIPAL_ADMIN);
            fail();
        } catch (SubmitFailedException e) {
            assertTrue(e.getCause() instanceof DeleteFailedException);
        }
        assertEquals(1, testRegistry.getComponentDescriptions().size());
        fileInfo.setForceUpdate(true);
        adminReg.delete(fileInfo, PRINCIPAL_ADMIN);
        assertEquals(0, testRegistry.getComponentDescriptions().size());

        assertEquals(1, testRegistry.getProfileDescriptions().size());
        fileInfo.setForceUpdate(false);
        fileInfo.setDisplayNode(new FileNode(new File(getRegistryDir(), "profiles" + File.separator + profileDesc.getName() + File.separator
                + profileDesc.getName() + ".xml"), false));
        adminReg.delete(fileInfo, PRINCIPAL_ADMIN); //Profile do not need to be forced they cannot be used by other profiles
        assertEquals(0, testRegistry.getProfileDescriptions().size());
    }
}
