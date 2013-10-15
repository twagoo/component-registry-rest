package clarin.cmdi.componentregistry.impl.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ComponentStatus;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.frontend.CMDItemInfo;
import clarin.cmdi.componentregistry.frontend.DisplayDataNode;
import clarin.cmdi.componentregistry.frontend.SubmitFailedException;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 *
 */
public class AdminRegistryTest extends BaseUnitTest{

    @Autowired
    private ComponentDao componentDao;
    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    private static final Principal PRINCIPAL_ADMIN = DummyPrincipal.DUMMY_ADMIN_PRINCIPAL;
    @Autowired
    private MDMarshaller marshaller;

    @Before
    public void init() {
	ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
    }

    @Test
    public void testForceUpdate() throws Exception {
	ComponentRegistry testRegistry = componentRegistryFactory.getPublicRegistry();
	String content1 = "";
	content1 += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content1 += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	content1 += "    <Header/>\n";
	content1 += "    <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
	content1 += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
	content1 += "    </CMD_Component>\n";
	content1 += "</CMD_ComponentSpec>\n";
	ComponentDescription compDesc1 = RegistryTestHelper.addComponent(testRegistry, "XXX1", content1);

	String content2 = "";
	content2 += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content2 += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	content2 += "    <Header/>\n";
	content2 += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
	content2 += "        <CMD_Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
	content2 += "        </CMD_Component>\n";
	content2 += "    </CMD_Component>\n";
	content2 += "</CMD_ComponentSpec>\n";
	ProfileDescription profileDesc = RegistryTestHelper.addProfile(testRegistry, "YYY1", content2);

	AdminRegistry adminReg = new AdminRegistry();
	adminReg.setComponentRegistryFactory(componentRegistryFactory);
	adminReg.setComponentDao(componentDao);
	adminReg.setMarshaller(marshaller);
	CMDItemInfo fileInfo = new CMDItemInfo(new MDMarshaller());
	fileInfo.setForceUpdate(false);
	fileInfo.setDataNode(new DisplayDataNode(compDesc1.getName(), false, compDesc1, ComponentStatus.PUBLISHED));
	fileInfo.setContent(content1);
	try {
	    adminReg.submitFile(fileInfo, PRINCIPAL_ADMIN);
	    fail();
	} catch (SubmitFailedException e) {
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
	fileInfo.setDataNode(new DisplayDataNode(profileDesc.getName(), false, profileDesc, ComponentStatus.PUBLISHED));
	adminReg.delete(fileInfo, PRINCIPAL_ADMIN); //Profile do not need to be forced they cannot be used by other profiles
	assertEquals(0, testRegistry.getProfileDescriptions().size());
    }
}
