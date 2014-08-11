/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryBeanFactory;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.util.Assert;
import static org.junit.Assert.*;
/**
 *
 * @author olhsha
 */
public class SanboxTest extends
	ComponentRegistryRestServiceTestCase {

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ComponentRegistry baseRegistry;

    @Before
    public void init() {
	ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
	createUserRecord();
	baseRegistry = componentRegistryFactory.getBaseRegistry(DummyPrincipal.DUMMY_CREDENTIALS);
    }

    private String expectedUserId(String principal) {
	return getUserDao().getByPrincipalName(principal).getId().toString();
    }

    private ComponentDescription component1;
    private ComponentDescription component2;
    private ProfileDescription profile1;
    private ProfileDescription profile2;
    
    private ComponentDescription component3;
    private ProfileDescription profile3;
    
    private Comment profile1Comment1;
    private Comment profile1Comment2;
    private Comment component1Comment3;
    private Comment component1Comment4;
    
    private Comment profile3Comment5;
    private Comment component3Comment7;

    private void fillUpPublicItems() throws Exception {
        
	profile1 = RegistryTestHelper.addProfile(baseRegistry, "profile2", true);
	profile2 = RegistryTestHelper.addProfile(baseRegistry, "profile1", true);
	component1 = RegistryTestHelper.addComponent(baseRegistry,
		"component2", true);
	component2 = RegistryTestHelper.addComponent(baseRegistry,
		"component1", true);
	profile1Comment2 = RegistryTestHelper.addComment(baseRegistry, "comment2",
		ProfileDescription.PROFILE_PREFIX + "profile1",
		"JUnit@test.com");
	profile1Comment1 = RegistryTestHelper.addComment(baseRegistry, "comment1",
		ProfileDescription.PROFILE_PREFIX + "profile1",
		"JUnit@test.com");
	component1Comment3 = RegistryTestHelper.addComment(baseRegistry, "comment3",
		ComponentDescription.COMPONENT_PREFIX + "component1",
		"JUnit@test.com");
	component1Comment4 = RegistryTestHelper.addComment(baseRegistry, "comment4",
		ComponentDescription.COMPONENT_PREFIX + "component1",
		"JUnit@test.com");
    }
    
    private void fillUpPrivateItems() throws Exception {
	profile3 = RegistryTestHelper.addProfile(baseRegistry, "profile3", false);
	component3 = RegistryTestHelper.addComponent(baseRegistry,
		"component3", false);
	profile3Comment5 = RegistryTestHelper.addComment(baseRegistry, "comment5",
		ProfileDescription.PROFILE_PREFIX + "profile3",
		"JUnit@test.com");
	component3Comment7 = RegistryTestHelper.addComment(baseRegistry, "comment7",
		ComponentDescription.COMPONENT_PREFIX + "component3",
		"JUnit@test.com");
    }
    
       @Test
    public void testCreatePrivateComponentWithRecursion() throws Exception {
        
        fillUpPrivateItems();
	// Create new componet
	FormDataMultiPart form = createFormData(RegistryTestHelper
		.getComponentTestContent());
	ClientResponse cResponse = getAuthenticatedResource(getResource().path("/registry/components")).type(
		MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
	assertEquals(ClientResponse.Status.OK.getStatusCode(),
		cResponse.getStatus());
	RegisterResponse response = cResponse.getEntity(RegisterResponse.class);
	assertTrue(response.isRegistered());
	ComponentDescription desc = (ComponentDescription) response
		.getDescription();

	// Re-define with self-recursion
	String compContent = "";
	compContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	compContent += "\n";
	compContent += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	compContent += "    xsi:noNamespaceSchemaLocation=\"../../general-component-schema.xsd\">\n";
	compContent += "    \n";
	compContent += "    <Header/>\n";
	compContent += "    \n";
	compContent += "    <CMD_Component name=\"Nested\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
	compContent += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
	compContent += "        <CMD_Component ComponentId=\""
		+ desc.getId()
		+ "\" name=\"Recursive\" CardinalityMin=\"1\" CardinalityMax=\"1\" />\n";
	compContent += "    </CMD_Component>\n";
	compContent += "\n";
	compContent += "</CMD_ComponentSpec>\n";

	// Update component
	form = createFormData(
		RegistryTestHelper.getComponentContentAsStream(compContent),
		"UPDATE DESCRIPTION!");
	cResponse = getAuthenticatedResource(getResource().path(
			"/registry/components/" + desc.getId() + "/update")).type(
		MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
	assertEquals(ClientResponse.Status.OK.getStatusCode(),
		cResponse.getStatus());
	response = cResponse.getEntity(RegisterResponse.class);
	assertFalse("Recursive definition should fail", response.isRegistered());
	assertEquals("There should be an error message for the recursion", 1,
		response.getErrors().size());
	assertTrue(
		"There error message should specify the point of recursion",
		response.getErrors().get(0)
			.contains("already contains " + desc.getId()));

    }

     private FormDataMultiPart createFormData(Object content) {
	return createFormData(content, "My Test");
    }

    private FormDataMultiPart createFormData(Object content, String description) {
	FormDataMultiPart form = new FormDataMultiPart();
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD, content,
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	form.field(IComponentRegistryRestService.NAME_FORM_FIELD, "Test1");
	form.field(IComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
		description);
	form.field(IComponentRegistryRestService.DOMAIN_FORM_FIELD, "My domain");
	form.field(IComponentRegistryRestService.GROUP_FORM_FIELD, "TestGroup");
	return form;
    }
}
