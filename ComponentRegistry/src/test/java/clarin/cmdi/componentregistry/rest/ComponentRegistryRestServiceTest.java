package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryBeanFactory;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.CommentResponse;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_PARAM;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.multipart.FormDataMultiPart;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * Launches a servlet container environment and performs HTTP calls to the
 * {@link ComponentRegistryRestService}
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
public class ComponentRegistryRestServiceTest extends
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

    @Test//ok
    public void testGetPublicProfiles() throws Exception {
        
        fillUpPublicItems();
        
	RegistryTestHelper.addProfile(baseRegistry, "PROFILE2", true);
	List<ProfileDescription> response = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles")).accept(MediaType.APPLICATION_XML)
		.get(PROFILE_LIST_GENERICTYPE);
	assertEquals(3, response.size());
	response = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles"))
		.accept(MediaType.APPLICATION_JSON)
		.get(PROFILE_LIST_GENERICTYPE);
	assertEquals(3, response.size());
	assertEquals("profile1", response.get(0).getName());
	assertEquals("PROFILE2", response.get(1).getName());
	assertEquals("profile2", response.get(2).getName());
    }

    @Test //ok
    public void testGetPublicComponents() throws Exception {
        
        fillUpPublicItems();
        
        RegistryTestHelper.addComponent(baseRegistry, "COMPONENT2", true);
	List<ComponentDescription> response = this.getAuthenticatedResource(getResource()
		.path("/registry/components")).accept(MediaType.APPLICATION_XML)
		.get(COMPONENT_LIST_GENERICTYPE);
	assertEquals(3, response.size());
	response = this.getAuthenticatedResource(getResource()
		.path("/registry/components"))
		.accept(MediaType.APPLICATION_JSON)
		.get(COMPONENT_LIST_GENERICTYPE);
	assertEquals(3, response.size());
	assertEquals("component1", response.get(0).getName());
	assertEquals("COMPONENT2", response.get(1).getName());
	assertEquals("component2", response.get(2).getName());
    }

    @Test  //ok
    public void testGetUserComponents() throws Exception {
        
        fillUpPrivateItems();
        
       	List<ComponentDescription> response = this.getUserComponents();
	assertEquals(1, response.size());
        
        ////
        
        fillUpPublicItems();
	response = getAuthenticatedResource(getResource().path("/registry/components")).accept(
		MediaType.APPLICATION_JSON).get(COMPONENT_LIST_GENERICTYPE);
	assertEquals("Get public components", 2, response.size());
        
	ClientResponse cResponse = getResource().path("/registry/components")
		.queryParam(REGISTRY_SPACE_PARAM, "private")
		.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
	assertEquals("Trying to get userspace without credentials", 401,
		cResponse.getStatus());
    }

    @Test //ok
    public void testGetPublicComponent() throws Exception {
        
        fillUpPublicItems();
        
        String id = ComponentDescription.COMPONENT_PREFIX + "component1";
	String id2 = ComponentDescription.COMPONENT_PREFIX + "component2";
	CMDComponentSpec component = this.getAuthenticatedResource(getResource()
		.path("/registry/components/" + id))
		.accept(MediaType.APPLICATION_JSON).get(CMDComponentSpec.class);
	assertNotNull(component);
	assertEquals("Access", component.getCMDComponent().getName());
        
        
	component = this.getAuthenticatedResource(getResource().path("/registry/components/" + id2))
		.accept(MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
	assertNotNull(component);
	assertEquals("Access", component.getCMDComponent().getName());
	assertEquals(id2, component.getHeader().getID());
	assertEquals("component2", component.getHeader().getName());
	assertEquals("Test Description", component.getHeader().getDescription());
    }

    @Test   // ok    
    public void testGetCommentsInPublicProfile() throws Exception {
        
        fillUpPublicItems();
        
	String id = ProfileDescription.PROFILE_PREFIX + "profile1";
	RegistryTestHelper.addComment(baseRegistry, "COMMENT1", id,
		"JUnit@test.com");
	List<Comment> response = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles/" + id + "/comments/"))
		.accept(MediaType.APPLICATION_XML)
		.get(COMMENT_LIST_GENERICTYPE);
	assertEquals(3, response.size());
        
	response = this.getAuthenticatedResource(getResource().path("/registry/profiles/" + id + "/comments"))
		.accept(MediaType.APPLICATION_JSON)
		.get(COMMENT_LIST_GENERICTYPE);
	assertEquals(3, response.size());
	assertEquals("comment2", response.get(0).getComment());
	assertEquals("comment1", response.get(1).getComment());
	assertEquals("COMMENT1", response.get(2).getComment());

	assertEquals("Database test user", response.get(0).getUserName());
    }

    @Test // ok
    public void testGetCommentsInPubliComponent() throws Exception {
        fillUpPublicItems();
        
	String id = ComponentDescription.COMPONENT_PREFIX + "component1";
	RegistryTestHelper.addComment(baseRegistry, "COMMENT2", id,
		"JUnit@test.com");
	List<Comment> response = this.getAuthenticatedResource(getResource()
		.path("/registry/components/" + id + "/comments/"))
		.accept(MediaType.APPLICATION_XML)
		.get(COMMENT_LIST_GENERICTYPE);
	assertEquals(3, response.size());
	response = this.getAuthenticatedResource(getResource()
		.path("/registry/components/" + id + "/comments"))
		.accept(MediaType.APPLICATION_JSON)
		.get(COMMENT_LIST_GENERICTYPE);
	assertEquals(3, response.size());
	assertEquals("comment3", response.get(0).getComment());
	assertEquals("comment4", response.get(1).getComment());
	assertEquals("COMMENT2", response.get(2).getComment());

	assertEquals("Database test user", response.get(0).getUserName());
    }

    @Test // ok
    public void testGetSpecifiedCommentInPublicComponent() throws Exception {
        
        fillUpPublicItems();
        
	String id = ComponentDescription.COMPONENT_PREFIX + "component1";
	Comment comment = this.getAuthenticatedResource(getResource()
		.path("/registry/components/" + id + "/comments/"+component1Comment3.getId()))
		.accept(MediaType.APPLICATION_JSON).get(Comment.class);
	assertNotNull(comment);
	assertEquals("comment3", comment.getComment());
	assertEquals(component1Comment3.getId(), comment.getId());
	comment = this.getAuthenticatedResource(getResource()
		.path("/registry/components/" + id + "/comments/"+component1Comment4.getId()))
		.accept(MediaType.APPLICATION_JSON).get(Comment.class);
	assertNotNull(comment);
	assertEquals("comment4", comment.getComment());
	assertEquals(component1Comment4.getId(), comment.getId());
	assertEquals(id, comment.getComponentId());
    }

    @Test //ok
    public void testGetSpecifiedCommentInPublicProfile() throws Exception {
        
        fillUpPublicItems();
        
	String id = ProfileDescription.PROFILE_PREFIX + "profile1";
	Comment comment = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles/" + id + "/comments/"+profile1Comment2.getId()))
		.accept(MediaType.APPLICATION_JSON).get(Comment.class);
	assertNotNull(comment);
	assertEquals("comment2", comment.getComment());
	assertEquals(profile1Comment2.getId(), comment.getId());
        
	comment = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles/" + id + "/comments/"+profile1Comment1.getId()))
                .accept(MediaType.APPLICATION_JSON).get(Comment.class);
	assertNotNull(comment);
	assertEquals("comment1", comment.getComment());
	assertEquals(profile1Comment1.getId(), comment.getId());
	assertEquals(id, comment.getComponentId());
    }

    @Test  //ok
    public void testDeleteCommentFromPublicComponent() throws Exception {
        
        fillUpPublicItems();
        
	String id = ComponentDescription.COMPONENT_PREFIX + "component1";
	String id2 = ComponentDescription.COMPONENT_PREFIX + "component2";
	List<Comment> comments = this.getAuthenticatedResource(getResource().path(
		"/registry/components/" + id + "/comments")).get(
		COMMENT_LIST_GENERICTYPE);
	assertEquals(2, comments.size());
	Comment aComment = this.getAuthenticatedResource(getResource().path(
		"/registry/components/" + id + "/comments/"+component1Comment3.getId()))
		.get(Comment.class);
	assertNotNull(aComment);

	// Try to delete from other component
	ClientResponse response = getAuthenticatedResource(
		"/registry/components/" + id2 + "/comments/"+component1Comment4.getId()).delete(
		ClientResponse.class);
	assertEquals(500, response.getStatus());
	// Delete from correct component
	response = getAuthenticatedResource(
		"/registry/components/" + id + "/comments/"+component1Comment3.getId()).delete(
		ClientResponse.class);
	assertEquals(200, response.getStatus());

	comments = this.getAuthenticatedResource(getResource().path(
		"/registry/components/" + id + "/comments/")).get(
		COMMENT_LIST_GENERICTYPE);
	assertEquals(1, comments.size());

	response = getAuthenticatedResource(
		"/registry/components/" + id + "/comments/"+component1Comment4.getId()).delete(
		ClientResponse.class);
	assertEquals(200, response.getStatus());

	comments = this.getAuthenticatedResource(getResource().path(
		"/registry/components/" + id + "/comments")).get(
		COMMENT_LIST_GENERICTYPE);
	assertEquals(0, comments.size());
    }

    @Test
    public void testManipulateCommentFromPublicComponent() throws Exception {
        
        fillUpPublicItems();
        
	List<Comment> comments = this.getAuthenticatedResource(getResource().path(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component1/comments")).get(COMMENT_LIST_GENERICTYPE);
	assertEquals(2, comments.size());
	Comment aComment = this.getAuthenticatedResource(getResource().path(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component1/comments/"+component1Comment3.getId())).get(Comment.class);
	assertNotNull(aComment);

	Form manipulateForm = new Form();
	manipulateForm.add("method", "delete");

	// Try to delete from other component
	ClientResponse response = getAuthenticatedResource(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component1/comments/"+component1Comment3.getId()).post(ClientResponse.class,
		manipulateForm);
	assertEquals(200, response.getStatus());

	comments = this.getAuthenticatedResource(getResource().path(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component1/comments/")).get(COMMENT_LIST_GENERICTYPE);
	assertEquals(1, comments.size());
    }

    @Test
    public void testDeleteCommentFromPublicProfile() throws Exception {
        
        fillUpPublicItems();
        
	List<Comment> comments = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments")).get(COMMENT_LIST_GENERICTYPE);
	assertEquals(2, comments.size());
	Comment aComment = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments/"+profile1Comment1.getId())).get(Comment.class);
	assertNotNull(aComment);

	// Try to delete from other profile
	ClientResponse response = getAuthenticatedResource(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile2/comments/9999").delete(ClientResponse.class);
	assertEquals(500, response.getStatus());
	// Delete from correct profile
	response = getAuthenticatedResource(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments/"+profile1Comment1.getId()).delete(ClientResponse.class);
	assertEquals(200, response.getStatus());

	comments = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments/")).get(COMMENT_LIST_GENERICTYPE);
	assertEquals(1, comments.size());

	response = getAuthenticatedResource(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments/"+profile1Comment2.getId()).delete(ClientResponse.class);
	assertEquals(200, response.getStatus());

	comments = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments")).get(COMMENT_LIST_GENERICTYPE);
	assertEquals(0, comments.size());
    }

    @Test
    public void testManipulateCommentFromPublicProfile() throws Exception {
        
        fillUpPublicItems();
        
	List<Comment> comments = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments")).get(COMMENT_LIST_GENERICTYPE);
	assertEquals(2, comments.size());
	Comment aComment = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments/"+profile1Comment2.getId())).get(Comment.class);
	assertNotNull(aComment);

	Form manipulateForm = new Form();
	manipulateForm.add("method", "delete");
	// Delete from correct profile
	ClientResponse response = getAuthenticatedResource(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments/"+profile1Comment2.getId()).post(ClientResponse.class,
		manipulateForm);
	assertEquals(200, response.getStatus());

	comments = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/comments/")).get(COMMENT_LIST_GENERICTYPE);
	assertEquals(1, comments.size());
    }

    @Test
    public void testDeletePublicComponent() throws Exception {
        
        fillUpPublicItems();
        
	List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
		"/registry/components")).get(COMPONENT_LIST_GENERICTYPE);
	assertEquals(2, components.size());
	CMDComponentSpec profile = this.getAuthenticatedResource(getResource().path(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component1")).get(CMDComponentSpec.class);
	assertNotNull(profile);
	ClientResponse response = getAuthenticatedResource(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component1").delete(ClientResponse.class);
	assertEquals(200, response.getStatus());

	components = this.getAuthenticatedResource(getResource().path("/registry/components")).get(
		COMPONENT_LIST_GENERICTYPE);
	assertEquals(1, components.size());

	response = getAuthenticatedResource(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component2").delete(ClientResponse.class);
	assertEquals(200, response.getStatus());

	components = this.getAuthenticatedResource(getResource().path("/registry/components")).get(
		COMPONENT_LIST_GENERICTYPE);
	assertEquals(0, components.size());

	response = getAuthenticatedResource(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component1").delete(ClientResponse.class);
	assertEquals(200, response.getStatus());
    }

    @Test
    public void testDeletePublicComponentStillUsed() throws Exception {
        
        fillUpPublicItems();
        
        String content = "";
	content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	content += "    <Header/>\n";
	content += "    <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
	content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
	content += "    </CMD_Component>\n";
	content += "</CMD_ComponentSpec>\n";
	ComponentDescription compDesc1 = RegistryTestHelper.addComponent(
		baseRegistry, "XXX1", content, true);

	content = "";
	content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	content += "    <Header/>\n";
	content += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
	content += "        <CMD_Component ComponentId=\"" + compDesc1.getId()
		+ "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
	content += "        </CMD_Component>\n";
	content += "    </CMD_Component>\n";
	content += "</CMD_ComponentSpec>\n";
	ComponentDescription compDesc2 = RegistryTestHelper.addComponent(
		baseRegistry, "YYY1", content,true);

	content = "";
	content += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	content += "    <Header/>\n";
	content += "    <CMD_Component name=\"ZZZ\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
	content += "        <CMD_Component ComponentId=\"" + compDesc1.getId()
		+ "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
	content += "        </CMD_Component>\n";
	content += "    </CMD_Component>\n";
	content += "</CMD_ComponentSpec>\n";
	ProfileDescription profile = RegistryTestHelper.addProfile(
		baseRegistry, "TestProfile3", content, true);

	List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
		"/registry/components")).get(COMPONENT_LIST_GENERICTYPE);
	assertEquals(4, components.size());

	ClientResponse response = getAuthenticatedResource(
		"/registry/components/" + compDesc1.getId()).delete(
		ClientResponse.class);
	assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
	assertEquals(
		"Component is still in use by other components or profiles. Request component usage for details.",
		response.getEntity(String.class));

	components = this.getAuthenticatedResource(getResource().path("/registry/components")).get(
		COMPONENT_LIST_GENERICTYPE);
	assertEquals(4, components.size());

	response = getAuthenticatedResource(
		"/registry/profiles/" + profile.getId()).delete(
		ClientResponse.class);
	assertEquals(200, response.getStatus());
	response = getAuthenticatedResource(
		"/registry/components/" + compDesc2.getId()).delete(
		ClientResponse.class);
	assertEquals(200, response.getStatus());
	response = getAuthenticatedResource(
		"/registry/components/" + compDesc1.getId()).delete(
		ClientResponse.class);
	assertEquals(200, response.getStatus());
	components = this.getAuthenticatedResource(getResource().path("/registry/components")).get(
		COMPONENT_LIST_GENERICTYPE);
	assertEquals(2, components.size());
    }

    @Test
    public void testManipulatePublicComponent() throws Exception {
        
        fillUpPublicItems();
        
	List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
		"/registry/components")).get(COMPONENT_LIST_GENERICTYPE);
	assertEquals(2, components.size());
	CMDComponentSpec profile = this.getAuthenticatedResource(getResource().path(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component1")).get(CMDComponentSpec.class);
	assertNotNull(profile);

	Form manipulateForm = new Form();
	manipulateForm.add("method", "delete");

	ClientResponse response = getAuthenticatedResource(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component1").post(ClientResponse.class,
		manipulateForm);
	assertEquals(200, response.getStatus());

	components = this.getAuthenticatedResource(getResource().path("/registry/components")).get(
		COMPONENT_LIST_GENERICTYPE);
	assertEquals(1, components.size());

	response = getAuthenticatedResource(
		"/registry/components/" + ComponentDescription.COMPONENT_PREFIX
			+ "component2").post(ClientResponse.class,
		manipulateForm);
	assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetRegisteredProfile() throws Exception {
        
        fillUpPublicItems();
        
	String id = ProfileDescription.PROFILE_PREFIX + "profile1";
	String id2 = ProfileDescription.PROFILE_PREFIX + "profile2";
	CMDComponentSpec profile = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles/" + id))
		.accept(MediaType.APPLICATION_JSON).get(CMDComponentSpec.class);
	assertNotNull(profile);
	assertEquals("Actor", profile.getCMDComponent().getName());
	profile = this.getAuthenticatedResource(getResource().path("/registry/profiles/" + id2))
		.accept(MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
	assertNotNull(profile);
	assertEquals("Actor", profile.getCMDComponent().getName());

	assertEquals(id2, profile.getHeader().getID());
	assertEquals("profile2", profile.getHeader().getName());
	assertEquals("Test Description", profile.getHeader().getDescription());

	try {
	    profile = this.getAuthenticatedResource(getResource()
		    .path("/registry/profiles/"
			    + ProfileDescription.PROFILE_PREFIX + "profileXXXX"))
		    .accept(MediaType.APPLICATION_XML)
		    .get(CMDComponentSpec.class);
	    fail("Exception should have been thrown resource does not exist, HttpStatusCode 404");
	} catch (UniformInterfaceException e) {
	    assertEquals(404, e.getResponse().getStatus());
	}
    }

    @Test
    public void testGetRegisteredProfileRawData() throws Exception {
        
        fillUpPublicItems();
        
	String profile = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/xsd")).accept(MediaType.TEXT_XML)
		.get(String.class).trim();
	assertTrue(profile
		.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema"));
	assertTrue(profile.endsWith("</xs:schema>"));

	profile = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1/xml")).accept(MediaType.TEXT_XML)
		.get(String.class).trim();
	assertTrue(profile
		.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<CMD_ComponentSpec"));
	assertTrue(profile.endsWith("</CMD_ComponentSpec>"));
	assertTrue(profile.contains("xsi:schemaLocation"));

	try {
	    this.getAuthenticatedResource(getResource()
		    .path("/registry/components/"
			    + ComponentDescription.COMPONENT_PREFIX
			    + "component1/xsl")).accept(MediaType.TEXT_XML)
		    .get(String.class);
	    fail("Should have thrown exception, unsupported path parameter");
	} catch (UniformInterfaceException e) {// server error
	}
    }

    // 
    @Test
    public void testPrivateProfileXsd() throws Exception {
        
        fillUpPublicItems();
        
	FormDataMultiPart form = createFormData(RegistryTestHelper
		.getTestProfileContent());
	RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(
			REGISTRY_SPACE_PARAM, "private")).type(
		MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
		form);
	assertTrue(response.isProfile());
	assertEquals(1, getUserProfiles().size());
	BaseDescription desc = response.getDescription();
	String profile = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles/" + desc.getId() + "/xsd"))
		.accept(MediaType.TEXT_XML).get(String.class).trim();
	assertTrue(profile
		.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema"));
	assertTrue(profile.endsWith("</xs:schema>"));
    }

    @Test
    public void testPrivateComponentXsd() throws Exception {
        
        fillUpPublicItems();
        
	FormDataMultiPart form = createFormData(RegistryTestHelper
		.getComponentTestContent());
	RegisterResponse response = getAuthenticatedResource(
		getResource().path("/registry/components").queryParam(
			REGISTRY_SPACE_PARAM, "private")).type(
		MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
		form);
	assertFalse(response.isProfile());
	assertEquals(1, getUserComponents().size());
	BaseDescription desc = response.getDescription();
	String profile = this.getAuthenticatedResource(getResource()
		.path("/registry/components/" + desc.getId() + "/xsd"))
		.accept(MediaType.TEXT_XML).get(String.class).trim();
	assertTrue(profile
		.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema"));
	assertTrue(profile.endsWith("</xs:schema>"));
    }

    @Test
    public void testDeleteRegisteredProfile() throws Exception {
        
        fillUpPublicItems();
        
	List<ProfileDescription> profiles = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles")).get(PROFILE_LIST_GENERICTYPE);
	assertEquals(2, profiles.size());
	CMDComponentSpec profile = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1")).get(CMDComponentSpec.class);
	assertNotNull(profile);

	ClientResponse response = getAuthenticatedResource(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1").delete(ClientResponse.class);
	assertEquals(200, response.getStatus());

	profiles = this.getAuthenticatedResource(getResource().path("/registry/profiles")).get(
		PROFILE_LIST_GENERICTYPE);
	assertEquals(1, profiles.size());

	response = getAuthenticatedResource(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile2").delete(ClientResponse.class);
	assertEquals(200, response.getStatus());

	profiles = this.getAuthenticatedResource(getResource().path("/registry/profiles")).get(
		PROFILE_LIST_GENERICTYPE);
	assertEquals(0, profiles.size());

	response = getAuthenticatedResource(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1").delete(ClientResponse.class);
	assertEquals(404, response.getStatus());
    }

    @Test
    public void testManipulateRegisteredProfile() throws Exception {
        
        fillUpPublicItems();
        
	List<ProfileDescription> profiles = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles")).get(PROFILE_LIST_GENERICTYPE);
	assertEquals(2, profiles.size());
	CMDComponentSpec profile = this.getAuthenticatedResource(getResource().path(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1")).get(CMDComponentSpec.class);
	assertNotNull(profile);

	Form manipulateForm = new Form();
	manipulateForm.add("method", "delete");

	ClientResponse response = getAuthenticatedResource(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile1")
		.post(ClientResponse.class, manipulateForm);
	assertEquals(200, response.getStatus());

	profiles = this.getAuthenticatedResource(getResource().path("/registry/profiles")).get(
		PROFILE_LIST_GENERICTYPE);
	assertEquals(1, profiles.size());

	response = getAuthenticatedResource(
		"/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
			+ "profile2")
		.post(ClientResponse.class, manipulateForm);
	assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetRegisteredComponentRawData() throws Exception {
        
        fillUpPublicItems();
        
	String id = ComponentDescription.COMPONENT_PREFIX + "component1";
	String component = this.getAuthenticatedResource(getResource()
		.path("/registry/components/" + id + "/xsd"))
		.accept(MediaType.TEXT_XML).get(String.class).trim();
	assertTrue(component
		.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema"));
	assertTrue(component.endsWith("</xs:schema>"));

	component = this.getAuthenticatedResource(getResource().path("/registry/components/" + id + "/xml"))
		.accept(MediaType.TEXT_XML).get(String.class).trim();
	assertTrue(component
		.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<CMD_ComponentSpec"));
	assertTrue(component.endsWith("</CMD_ComponentSpec>"));
	assertTrue(component.contains("xsi:schemaLocation"));

	try {
	    this.getAuthenticatedResource(getResource()
		    .path("/registry/components/"
			    + ComponentDescription.COMPONENT_PREFIX
			    + "component1/jpg")).accept(MediaType.TEXT_XML)
		    .get(String.class);
	    fail("Should have thrown exception, unsopported path parameter");
	} catch (UniformInterfaceException e) {
	}
    }

    @Test
    public void testRegisterProfile() throws Exception {
        
        fillUpPublicItems();
        
	FormDataMultiPart form = new FormDataMultiPart();
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD,
		RegistryTestHelper.getTestProfileContent(),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	form.field(IComponentRegistryRestService.NAME_FORM_FIELD,
		"ProfileTest1");
	form.field(IComponentRegistryRestService.DOMAIN_FORM_FIELD,
		"TestDomain");
	form.field(IComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
		"My Test Profile");
	form.field(IComponentRegistryRestService.GROUP_FORM_FIELD,
		"My Test Group");
	RegisterResponse response = getAuthenticatedResource(
		"/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
		RegisterResponse.class, form);
	assertTrue(response.isProfile());
	assertTrue(response.isInUserSpace());
	ProfileDescription profileDesc = (ProfileDescription) response
		.getDescription();
	assertNotNull(profileDesc);
	assertEquals("ProfileTest1", profileDesc.getName());
	assertEquals("My Test Profile", profileDesc.getDescription());
	assertEquals("TestDomain", profileDesc.getDomainName());
	assertEquals("My Test Group", profileDesc.getGroupName());
	assertEquals(expectedUserId("JUnit@test.com"), profileDesc.getUserId());
	assertEquals("Database test user", profileDesc.getCreatorName());
	assertTrue(profileDesc.getId().startsWith(
		ComponentRegistry.REGISTRY_ID + "p_"));
	assertNotNull(profileDesc.getRegistrationDate());
	assertEquals(
		"http://localhost:9998/registry/profiles/"
			+ profileDesc.getId(), profileDesc.getHref());
    }

    @Test
    public void testPublishProfile() throws Exception {
        
        fillUpPrivateItems();
        fillUpPublicItems();
        
	assertEquals("user registered profiles", 1, getUserProfiles().size());
	assertEquals("public registered profiles", 2, getPublicProfiles()
		.size());
	FormDataMultiPart form = createFormData(
		RegistryTestHelper.getTestProfileContent(), "Unpublished");
	RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/profiles")).type(
		MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
		form);
	assertTrue(response.isProfile());
	BaseDescription desc = response.getDescription();
	assertEquals("Unpublished", desc.getDescription());
	assertEquals(2, getUserProfiles().size());
	assertEquals(2, getPublicProfiles().size());
	form = createFormData(
		RegistryTestHelper.getTestProfileContent("publishedName"),
		"Published");
	response = getAuthenticatedResource(getResource().path(
			"/registry/profiles/" + desc.getId() + "/publish"))
		.type(MediaType.MULTIPART_FORM_DATA).post(
			RegisterResponse.class, form);

	assertEquals(1, getUserProfiles().size());
	List<ProfileDescription> profiles = getPublicProfiles();
	assertEquals(3, profiles.size());
	ProfileDescription profileDescription = profiles.get(2);
	assertNotNull(profileDescription.getId());
	assertEquals(desc.getId(), profileDescription.getId());
	assertEquals("http://localhost:9998/registry/profiles/" + desc.getId(),
		profileDescription.getHref());
	assertEquals("Published", profileDescription.getDescription());
	CMDComponentSpec spec = getPublicSpec(profileDescription);
	assertEquals("publishedName", spec.getCMDComponent().getName());
    }

    private CMDComponentSpec getPublicSpec(BaseDescription desc) {
	if (desc.isProfile()) {
	    return this.getAuthenticatedResource(getResource().path("/registry/profiles/" + desc.getId()))
		    .get(CMDComponentSpec.class);
	} else {
	    return this.getAuthenticatedResource(getResource().path("/registry/components/" + desc.getId()))
		    .get(CMDComponentSpec.class);
	}
    }

    @Test
    public void testPublishRegisteredComponent() throws Exception {
        
        fillUpPrivateItems();
        fillUpPublicItems();
        
	assertEquals(1, getUserComponents().size());
	assertEquals(2, getPublicComponents().size());
        
	FormDataMultiPart form = createFormData(
		RegistryTestHelper.getComponentTestContent(), "Unpublished");
	RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/components")).type(
		MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
		form);
	assertFalse(response.isProfile());
	BaseDescription desc = response.getDescription();
	assertEquals("Unpublished", desc.getDescription());
	assertEquals(2, getUserComponents().size());
	assertEquals(2, getPublicComponents().size());
	form = createFormData(
		RegistryTestHelper.getComponentTestContentAsStream("publishedName"),
		"Published");
	response = getAuthenticatedResource(getResource().path(
			"/registry/components/" + desc.getId() + "/publish"))
		.type(MediaType.MULTIPART_FORM_DATA).post(
			RegisterResponse.class, form);

	assertEquals(1, getUserComponents().size());
	List<ComponentDescription> components = getPublicComponents();
	assertEquals(3, components.size());
	ComponentDescription componentDescription = components.get(2);
	assertNotNull(componentDescription.getId());
	assertEquals(desc.getId(), componentDescription.getId());
	assertEquals(
		"http://localhost:9998/registry/components/" + desc.getId(),
		componentDescription.getHref());
	assertEquals("Published", componentDescription.getDescription());
	CMDComponentSpec spec = getPublicSpec(componentDescription);
	assertEquals("publishedName", spec.getCMDComponent().getName());
    }

    // duplicates ("extendedly") testRegisterProfile, since you can pot only in user space, and then publish or move to group or so
    @Test
    public void testRegisterUserspaceProfile() throws Exception {
        
        fillUpPrivateItems();
        fillUpPublicItems();
        
	List<ProfileDescription> profiles = getUserProfiles();
	assertEquals("user registered profiles", 1, profiles.size());
	assertEquals("public registered profiles", 2, getPublicProfiles()
		.size());
	FormDataMultiPart form = createFormData(RegistryTestHelper
		.getTestProfileContent());
	RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/profiles")).type(
		MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
		form);
	assertTrue(response.isProfile());
	assertTrue(response.isInUserSpace());
	ProfileDescription profileDesc = (ProfileDescription) response
		.getDescription();
	assertNotNull(profileDesc);
	assertEquals("Test1", profileDesc.getName());
	assertEquals("My Test", profileDesc.getDescription());
	assertEquals(expectedUserId("JUnit@test.com"), profileDesc.getUserId());
	assertEquals("Database test user", profileDesc.getCreatorName());
	assertTrue(profileDesc.getId().startsWith(
		ComponentRegistry.REGISTRY_ID + "p_"));
	assertNotNull(profileDesc.getRegistrationDate());
	assertEquals(
		"http://localhost:9998/registry/profiles/"
			+ profileDesc.getId(),
		profileDesc.getHref());

	profiles = getUserProfiles();
	assertEquals(2, profiles.size());
	assertEquals(2, getPublicProfiles().size());

	// Try to post unauthenticated
	ClientResponse cResponse = getResource().path("/registry/profiles")
		.type(MediaType.MULTIPART_FORM_DATA)
		.post(ClientResponse.class, form);
	assertEquals(401, cResponse.getStatus());

	
	
	CMDComponentSpec spec = getAuthenticatedResource(getResource().path("/registry/profiles/" + profileDesc.getId())
			.queryParam(REGISTRY_SPACE_PARAM, "private")).accept(
		MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
	assertNotNull(spec);

	cResponse = this.getAuthenticatedResource(getResource()
		.path("/registry/profiles/" + profileDesc.getId() + "/xsd"))
		.accept(MediaType.TEXT_XML).get(ClientResponse.class);
	assertEquals(200, cResponse.getStatus());
	String profile = cResponse.getEntity(String.class);
	assertTrue(profile.length() > 0);

	profile = getAuthenticatedResource(getResource().path(
			"/registry/profiles/" + profileDesc.getId() + "/xml"))
		.accept(MediaType.TEXT_XML).get(String.class);
	assertTrue(profile.length() > 0);

	cResponse = getAuthenticatedResource(getResource().path("/registry/profiles/" + profileDesc.getId())
			.queryParam(REGISTRY_SPACE_PARAM, "private")).delete(
		ClientResponse.class);
	assertEquals(200, cResponse.getStatus());

	profiles = getUserProfiles();
	assertEquals(1, profiles.size());
    }

    private List<ProfileDescription> getPublicProfiles() {
	return getAuthenticatedResource("/registry/profiles").accept(
		MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE);
    }

    private List<ProfileDescription> getUserProfiles() {
	return getAuthenticatedResource(getResource().path("/registry/profiles").queryParam(
			REGISTRY_SPACE_PARAM, "private")).accept(
		MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE);
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

    @Test
    public void testRegisterWithUserComponents() throws Exception {
        fillUpPrivateItems();
        
        // kid component, not public
	String content = "";
	content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	content += "    <Header/>\n";
	content += "    <CMD_Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
	content += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
	content += "    </CMD_Component>\n";
	content += "</CMD_ComponentSpec>\n";
	ComponentDescription compDesc1 = RegistryTestHelper.addComponent(
		baseRegistry, "XXX1", content, false);

        // a containing component, referring to the kid (which is not public, so the containing component cannot be registered
	content = "";
	content += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	content += "    <Header/>\n";
	content += "    <CMD_Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
	content += "        <CMD_Component ComponentId=\"" + compDesc1.getId()
		+ "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
	content += "        </CMD_Component>\n";
	content += "    </CMD_Component>\n";
	content += "</CMD_ComponentSpec>\n";
	FormDataMultiPart form = createFormData(content);
	RegisterResponse response = getAuthenticatedResource(
		"/registry/components").type(MediaType.MULTIPART_FORM_DATA)
		.post(RegisterResponse.class, form);
        // we first alway register in a private space, so reference to a private component should work
	assertTrue(response.isRegistered());


        /// profiles ///
	content = "";
	content += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	content += "    <Header/>\n";
	content += "    <CMD_Component name=\"ZZZ\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
	content += "        <CMD_Component ComponentId=\"" + compDesc1.getId()
		+ "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
	content += "        </CMD_Component>\n";
	content += "    </CMD_Component>\n";
	content += "</CMD_ComponentSpec>\n";

	form = createFormData(content);
	response = getAuthenticatedResource("/registry/profiles").type(
		MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
		form);
	assertTrue(response.isRegistered());

    }

    @Test
    public void testRegisterUserspaceComponent() throws Exception {
        
        fillUpPrivateItems();
        
	List<ComponentDescription> components = getUserComponents();
	assertEquals("user registered components", 1, components.size());
	assertEquals("public registered components", 0, getPublicComponents()
		.size());
	FormDataMultiPart form = createFormData(RegistryTestHelper
		.getComponentTestContent());

	RegisterResponse response = getAuthenticatedResource(getResource().path("/registry/components")).type(
		MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
		form);
	assertTrue(response.isRegistered());
	assertFalse(response.isProfile());
	assertTrue(response.isInUserSpace());
	ComponentDescription desc = (ComponentDescription) response
		.getDescription();
	assertNotNull(desc);
	assertEquals("Test1", desc.getName());
	assertEquals("My Test", desc.getDescription());
	assertEquals(expectedUserId("JUnit@test.com"), desc.getUserId());
	assertEquals("Database test user", desc.getCreatorName());
	assertEquals("TestGroup", desc.getGroupName());
	assertTrue(desc.getId()
		.startsWith(ComponentRegistry.REGISTRY_ID + "c_"));
	assertNotNull(desc.getRegistrationDate());
	String url = getResource().getUriBuilder().build().toString();
	assertEquals(url + "registry/components/" + desc.getId(), desc.getHref());

	components = getUserComponents();
	assertEquals(2, components.size());
	assertEquals(0, getPublicComponents().size());

	// Try to post unauthenticated
	ClientResponse cResponse = getResource().path("/registry/components")
		.type(MediaType.MULTIPART_FORM_DATA)
		.post(ClientResponse.class, form);
	assertEquals(401, cResponse.getStatus());

	// Try to get 
	cResponse = this.getAuthenticatedResource(getResource().path("/registry/components/" + desc.getId()))
		.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
	
	assertEquals(200, cResponse.getStatus());
	CMDComponentSpec spec = getUserComponent(desc);
	assertNotNull(spec);

	cResponse = this.getAuthenticatedResource(getResource()
		.path("/registry/components/" + desc.getId() + "/xsd"))
		.accept(MediaType.TEXT_XML).get(ClientResponse.class);
	assertEquals(200, cResponse.getStatus());
	String result = cResponse.getEntity(String.class);
	assertTrue(result.length() > 0);

	result = getAuthenticatedResource(getResource().path(
			"/registry/components/" + desc.getId() + "/xml"))
		.accept(MediaType.TEXT_XML).get(String.class);
	assertTrue(result.length() > 0);

	cResponse = getAuthenticatedResource(getResource().path("/registry/components/" + desc.getId())).delete(
		ClientResponse.class);
	assertEquals(200, cResponse.getStatus());

	components = getUserComponents();
	assertEquals(1, components.size());
    }
//
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

    @Test
    public void testUpdatePrivateComponent() throws Exception {
        
        fillUpPrivateItems();
        
	List<ComponentDescription> components = getUserComponents();
	assertEquals("user registered components", 1, components.size());
	assertEquals("public registered components", 0, getPublicComponents()
		.size());
        
        // first, post a component to update
	FormDataMultiPart form = createFormData(RegistryTestHelper
		.getComponentTestContent());
	ClientResponse cResponse = getAuthenticatedResource(getResource().path("/registry/components")).type(
		MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
	assertEquals(ClientResponse.Status.OK.getStatusCode(),
		cResponse.getStatus());
	RegisterResponse response = cResponse.getEntity(RegisterResponse.class);
	assertTrue(response.isRegistered());
	assertFalse(response.isProfile());
	assertTrue(response.isInUserSpace());
	ComponentDescription desc = (ComponentDescription) response
		.getDescription();
	assertNotNull(desc);
	assertEquals("Test1", desc.getName());
	assertEquals("My Test", desc.getDescription());
	Date firstDate = desc.getRegistrationDate();
	CMDComponentSpec spec = getUserComponent(desc);
	assertNotNull(spec);
	assertEquals("Access", spec.getCMDComponent().getName());
	components = getUserComponents();
	assertEquals(2, components.size());
	assertEquals(0, getPublicComponents().size());

	// second, now update
	form = createFormData(
		RegistryTestHelper.getComponentTestContentAsStream("TESTNAME"),
		"UPDATE DESCRIPTION!");
	cResponse = getAuthenticatedResource(getResource().path(
			"/registry/components/" + desc.getId() + "/update")
			.queryParam(REGISTRY_SPACE_PARAM, "private")).type(
		MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
	assertEquals(ClientResponse.Status.OK.getStatusCode(),
		cResponse.getStatus());
	response = cResponse.getEntity(RegisterResponse.class);
	assertTrue(response.isRegistered());
	assertFalse(response.isProfile());
	assertTrue(response.isInUserSpace());
	desc = (ComponentDescription) response.getDescription();
	assertNotNull(desc);
	assertEquals("Test1", desc.getName());
	assertEquals("UPDATE DESCRIPTION!", desc.getDescription());
	Date secondDate = desc.getRegistrationDate();
	assertTrue(firstDate.before(secondDate) || firstDate.equals(secondDate));

	spec = getUserComponent(desc);
	assertNotNull(spec);
	assertEquals("TESTNAME", spec.getCMDComponent().getName());
	components = getUserComponents();
	assertEquals(2, components.size());
	assertEquals(0, getPublicComponents().size());
    }

    @Test
    public void testUpdatePrivateProfile() throws Exception {
        
        fillUpPrivateItems();
        
	List<ProfileDescription> profiles = getUserProfiles();
	assertEquals(1, profiles.size());
	assertEquals(0, getPublicProfiles().size());

	FormDataMultiPart form = createFormData(RegistryTestHelper
		.getTestProfileContent());
	ClientResponse cResponse = getAuthenticatedResource(getResource().path("/registry/profiles")).type(
		MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
	assertEquals(ClientResponse.Status.OK.getStatusCode(),
		cResponse.getStatus());
	RegisterResponse response = cResponse.getEntity(RegisterResponse.class);
	assertTrue(response.isRegistered());
	assertTrue(response.isProfile());
	assertTrue(response.isInUserSpace());
	ProfileDescription desc = (ProfileDescription) response
		.getDescription();
	assertNotNull(desc);
	assertEquals("Test1", desc.getName());
	assertEquals("My Test", desc.getDescription());
	assertEquals("TestGroup", desc.getGroupName());
	Date firstDate = desc.getRegistrationDate();
	CMDComponentSpec spec = getUserProfile(desc);
	assertNotNull(spec);
	assertEquals("Actor", spec.getCMDComponent().getName());
	profiles = getUserProfiles();
	assertEquals(2, profiles.size());
	assertEquals(0, getPublicComponents().size());

	// Now update
	form = createFormData(
		RegistryTestHelper.getTestProfileContent("TESTNAME"),
		"UPDATE DESCRIPTION!");
	cResponse = getAuthenticatedResource(getResource().path(
			"/registry/profiles/" + desc.getId() + "/update")).type(
		MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
	assertEquals(ClientResponse.Status.OK.getStatusCode(),
		cResponse.getStatus());
	response = cResponse.getEntity(RegisterResponse.class);
	assertTrue(response.isRegistered());
	assertTrue(response.isProfile());
	assertTrue(response.isInUserSpace());
	desc = (ProfileDescription) response.getDescription();
	assertNotNull(desc);
	assertEquals("Test1", desc.getName());
	assertEquals("UPDATE DESCRIPTION!", desc.getDescription());
	Date secondDate = desc.getRegistrationDate();
	assertTrue(firstDate.before(secondDate) || firstDate.equals(secondDate));

	spec = getUserProfile(desc);
	assertNotNull(spec);
	assertEquals("TESTNAME", spec.getCMDComponent().getName());
	profiles = getUserProfiles();
	assertEquals(2, profiles.size());
	assertEquals(0, getPublicComponents().size());
    }

    private CMDComponentSpec getUserComponent(ComponentDescription desc) {
	return getAuthenticatedResource(getResource().path("/registry/components/" + desc.getId())
			.queryParam(REGISTRY_SPACE_PARAM, "private")).accept(
		MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
    }

    private CMDComponentSpec getUserProfile(ProfileDescription desc) {
	return getAuthenticatedResource(getResource().path("/registry/profiles/" + desc.getId())
			.queryParam(REGISTRY_SPACE_PARAM, "private")).accept(
		MediaType.APPLICATION_XML).get(CMDComponentSpec.class);
    }

    private List<ComponentDescription> getPublicComponents() {
	return getAuthenticatedResource("/registry/components").accept(
		MediaType.APPLICATION_XML).get(COMPONENT_LIST_GENERICTYPE);
    }

    private List<ComponentDescription> getUserComponents() {
	return getAuthenticatedResource(getResource().path("/registry/components").queryParam(
			REGISTRY_SPACE_PARAM, "private")).accept(
		MediaType.APPLICATION_XML).get(COMPONENT_LIST_GENERICTYPE);
    }

    @Test
    public void testRegisterComponent() throws Exception {
        
        fillUpPrivateItems();
        
	FormDataMultiPart form = new FormDataMultiPart();
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD,
		RegistryTestHelper.getComponentTestContent(),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	form.field(IComponentRegistryRestService.NAME_FORM_FIELD,
		"ComponentTest1");
	form.field(IComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
		"My Test Component");
	form.field(IComponentRegistryRestService.DOMAIN_FORM_FIELD,
		"TestDomain");
	form.field(IComponentRegistryRestService.GROUP_FORM_FIELD, "TestGroup");
	RegisterResponse response = getAuthenticatedResource(
		"/registry/components").type(MediaType.MULTIPART_FORM_DATA)
		.post(RegisterResponse.class, form);
	assertTrue(response.isRegistered());
	assertFalse(response.isProfile());
	assertTrue(response.isInUserSpace());
	ComponentDescription desc = (ComponentDescription) response
		.getDescription();
	assertNotNull(desc);
	assertEquals("ComponentTest1", desc.getName());
	assertEquals("My Test Component", desc.getDescription());
	assertEquals(expectedUserId("JUnit@test.com"), desc.getUserId());
	assertEquals("Database test user", desc.getCreatorName());
	assertEquals("TestGroup", desc.getGroupName());
	assertEquals("TestDomain", desc.getDomainName());
	assertTrue(desc.getId()
		.startsWith(ComponentRegistry.REGISTRY_ID + "c_"));
	assertNotNull(desc.getRegistrationDate());
	String url = getResource().getUriBuilder().build().toString();
	assertEquals(url + "registry/components/" + desc.getId(),
		desc.getHref());
    }

    @Test
    public void testRegisterComment() throws Exception {
        
        fillUpPublicItems();
        
	FormDataMultiPart form = new FormDataMultiPart();
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD,
		RegistryTestHelper.getCommentTestContent(),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	String id = ProfileDescription.PROFILE_PREFIX + "profile1";
	CommentResponse response = getAuthenticatedResource(
		"/registry/profiles/" + id + "/comments").type(
		MediaType.MULTIPART_FORM_DATA)
		.post(CommentResponse.class, form);
	assertTrue(response.isRegistered());
	assertFalse(response.isInUserSpace());
	Comment comment = response.getComment();
	assertNotNull(comment);
	assertEquals("Actual", comment.getComment());
	assertEquals("Database test user", comment.getUserName());
	Assert.notNull(comment.getCommentDate());
	Assert.hasText(comment.getId());

	// User id should not be serialized!
	assertEquals(0, comment.getUserId());
    }

    @Test
    public void testRegisterCommentToNonExistent() throws Exception {
        fillUpPublicItems();
        
	FormDataMultiPart form = new FormDataMultiPart();
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD,
		RegistryTestHelper.getCommentTestContent(),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	ClientResponse cResponse = getAuthenticatedResource(
		"/registry/profiles/clarin.eu:cr1:profile99/comments").type(
		MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
	assertEquals(404, cResponse.getStatus());
    }

    @Test
    public void testRegisterCommentUnauthenticated() throws Exception {
        fillUpPublicItems();
        
	FormDataMultiPart form = new FormDataMultiPart();
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD,
		RegistryTestHelper.getCommentTestContent(),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	ClientResponse cResponse = getResource()
		.path("/registry/profiles/clarin.eu:cr1:profile1/comments")
		.type(MediaType.MULTIPART_FORM_DATA)
		.post(ClientResponse.class, form);
	assertEquals(401, cResponse.getStatus());
    }

    @Test
    public void testRegisterProfileInvalidData() throws Exception {
        fillUpPrivateItems();
	FormDataMultiPart form = new FormDataMultiPart();
	String notAValidProfile = "<CMD_ComponentSpec> </CMD_ComponentSpec>";
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD,
		new ByteArrayInputStream(notAValidProfile.getBytes()),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	form.field(IComponentRegistryRestService.NAME_FORM_FIELD,
		"ProfileTest1");
	form.field(IComponentRegistryRestService.DOMAIN_FORM_FIELD, "Domain");
	form.field(IComponentRegistryRestService.GROUP_FORM_FIELD, "Group");
	form.field(IComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
		"My Test Profile");
	RegisterResponse postResponse = getAuthenticatedResource(
		"/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
		RegisterResponse.class, form);
	assertTrue(postResponse.isProfile());
	assertFalse(postResponse.isRegistered());
	assertEquals(1, postResponse.getErrors().size());
	assertTrue(postResponse.getErrors().get(0).contains("isProfile"));
    }

    /**
     * Two elements on the same level with the same name violates schematron
     * rule, and should fail validation
     * 
     * @throws Exception
     */
    @Test
    public void testRegisterInvalidProfile() throws Exception {
        fillUpPrivateItems();
	FormDataMultiPart form = new FormDataMultiPart();
	String profileContent = "";
	profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
	profileContent += "    <Header />\n";
	profileContent += "    <CMD_Component name=\"ProfileTest1\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
	profileContent += "        <CMD_Element name=\"Age\">\n";
	profileContent += "            <ValueScheme>\n";
	profileContent += "                <pattern>[23][0-9]</pattern>\n";
	profileContent += "            </ValueScheme>\n";
	profileContent += "        </CMD_Element>\n";
	profileContent += "        <CMD_Element name=\"Age\">\n";
	profileContent += "            <ValueScheme>\n";
	profileContent += "                <pattern>[23][0-9]</pattern>\n";
	profileContent += "            </ValueScheme>\n";
	profileContent += "        </CMD_Element>\n";
	profileContent += "    </CMD_Component>\n";
	profileContent += "</CMD_ComponentSpec>\n";
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD,
		RegistryTestHelper.getComponentContentAsStream(profileContent),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	form.field(IComponentRegistryRestService.NAME_FORM_FIELD,
		"ProfileTest1");
	form.field(IComponentRegistryRestService.DOMAIN_FORM_FIELD,
		"TestDomain");
	form.field(IComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
		"My Test Profile");
	form.field(IComponentRegistryRestService.GROUP_FORM_FIELD,
		"My Test Group");
	RegisterResponse response = getAuthenticatedResource(
		"/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
		RegisterResponse.class, form);
	assertFalse(
		"Subsequent elements should not be allowed to have the same name",
		response.isRegistered());
	assertTrue(response.getErrors().get(0)
		.contains(MDValidator.PARSE_ERROR));
    }

    @Test
    public void testRegisterProfileInvalidDescriptionAndContent()
	    throws Exception {
        fillUpPrivateItems();
	FormDataMultiPart form = new FormDataMultiPart();
	String profileContent = "";
	profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	profileContent += "<CMD_ComponentSpec> \n"; // No isProfile attribute
	profileContent += "    <Header />\n";
	profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
	profileContent += "        <AttributeList>\n";
	profileContent += "            <Attribute>\n";
	profileContent += "                <Name>Name</Name>\n";
	profileContent += "                <Type>string</Type>\n";
	profileContent += "            </Attribute>\n";
	profileContent += "        </AttributeList>\n";
	profileContent += "    </CMD_Component>\n";
	profileContent += "</CMD_ComponentSpec>\n";
	form.field("data", new ByteArrayInputStream(profileContent.getBytes()),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	form.field("name", "");// Empty name so invalid
	form.field("description", "My Test Profile");
	RegisterResponse response = getAuthenticatedResource(
		"/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
		RegisterResponse.class, form);
	assertFalse(response.isRegistered());
	assertEquals(2, response.getErrors().size());
	assertNotNull(response.getErrors().get(0));
	assertEquals(MDValidator.PARSE_ERROR, response.getErrors().get(1)
		.substring(0, MDValidator.PARSE_ERROR.length()));
    }

    @Test
    public void testRegisterLargeProfile() throws Exception {
        //fillUpPrivateItems(); not necessary
	FormDataMultiPart form = new FormDataMultiPart();
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD,
		RegistryTestHelper.getLargeProfileContent(),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	form.field(IComponentRegistryRestService.NAME_FORM_FIELD,
		"ProfileTest1");
	form.field(IComponentRegistryRestService.DOMAIN_FORM_FIELD,
		"TestDomain");
	form.field(IComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
		"My Test Profile");
	form.field(IComponentRegistryRestService.GROUP_FORM_FIELD,
		"My Test Group");
	ClientResponse response = getAuthenticatedResource("/registry/profiles")
		.type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
			form);
	assertEquals(200, response.getStatus());
    }

    @Test
    public void testRegisterComponentAsProfile() throws Exception {
        //fillUpPrivateItems();; not necessary
	FormDataMultiPart form = new FormDataMultiPart();
	form.field(IComponentRegistryRestService.DATA_FORM_FIELD,
		RegistryTestHelper.getComponentTestContent(),
		MediaType.APPLICATION_OCTET_STREAM_TYPE);
	form.field(IComponentRegistryRestService.NAME_FORM_FIELD, "t");
	form.field(IComponentRegistryRestService.DOMAIN_FORM_FIELD, "domain");
	form.field(IComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
		"My Test");
	form.field(IComponentRegistryRestService.GROUP_FORM_FIELD, "My Group");
	RegisterResponse response = getAuthenticatedResource(
		"/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
		RegisterResponse.class, form);
	assertFalse(response.isRegistered());
	assertTrue(response.isProfile());
	assertEquals(1, response.getErrors().size());
	assertEquals(MDValidator.MISMATCH_ERROR, response.getErrors().get(0));
    }

    /**
     * creates a tree-like component, whose root has two kids, and the kids have
     * three their own kids each the corresponding filenames are: "wortel",
     * "nodel-L", "node-R", "leaf-LL", "leaf-LM", "leaf-LR", "leaf-RL",
     * "leaf-RM", "leaf-RR"
     * 
     * @throws Exception
     */
    @Test
    public void testSetFilenamesToNull() throws Exception {

	// helper assists in generating a test component
	CMDComponentSetFilenamesToNullTestRunner helper = new CMDComponentSetFilenamesToNullTestRunner();

	// making an (up to ternary) tree of test components
	// inductive explaination of the variable names, an example: leaf-LM is
	// the middle child of the nodeL
	// the filename is this leaf is "leaf-LM"

	// making children of the node L
	CMDComponentType leafLR = helper.makeTestComponent("leaf-LR", null);
	CMDComponentType leafLM = helper.makeTestComponent("leaf-LM", null);
	CMDComponentType leafLL = helper.makeTestComponent("leaf-LL", null);

	// making node L
	List<CMDComponentType> nodeLchild = (new ArrayList<CMDComponentType>());
	nodeLchild.add(leafLL);
	nodeLchild.add(leafLM);
	nodeLchild.add(leafLR);
	CMDComponentType nodeL = helper.makeTestComponent("node-L", nodeLchild);

	// making children of the node R
	CMDComponentType leafRR = helper.makeTestComponent("leaf-RR", null);
	CMDComponentType leafRM = helper.makeTestComponent("leaf-RM", null);
	CMDComponentType leafRL = helper.makeTestComponent("leaf-RL", null);

	// making node R
	List<CMDComponentType> nodeRchild = (new ArrayList<CMDComponentType>());
	nodeRchild.add(leafRL);
	nodeRchild.add(leafRM);
	nodeRchild.add(leafRR);
	CMDComponentType nodeR = helper.makeTestComponent("node-R", nodeRchild);

	// making the root, which has children NodeL and nodeR
	List<CMDComponentType> wortelchild = (new ArrayList<CMDComponentType>());
	wortelchild.add(nodeL);
	wortelchild.add(nodeR);
	CMDComponentType root = helper.makeTestComponent("wortel", wortelchild);

	// checking if the test compnent has the expected structure and the
	// expected filenames
	// ALSO this checking code below shows the strtucture of the tree

	assertEquals(root.getCMDComponent().size(), 2);
	assertEquals(root.getCMDComponent().get(0).getCMDComponent().size(), 3);
	assertEquals(root.getCMDComponent().get(1).getCMDComponent().size(), 3);

	assertEquals(root.getFilename(), "wortel");

	assertEquals(root.getCMDComponent().get(0).getFilename(), "node-L");
	assertEquals(root.getCMDComponent().get(1).getFilename(), "node-R");

	assertEquals(root.getCMDComponent().get(0).getCMDComponent().get(0)
		.getFilename(), "leaf-LL");
	assertEquals(root.getCMDComponent().get(0).getCMDComponent().get(1)
		.getFilename(), "leaf-LM");
	assertEquals(root.getCMDComponent().get(0).getCMDComponent().get(2)
		.getFilename(), "leaf-LR");

	assertEquals(root.getCMDComponent().get(1).getCMDComponent().get(0)
		.getFilename(), "leaf-RL");
	assertEquals(root.getCMDComponent().get(1).getCMDComponent().get(1)
		.getFilename(), "leaf-RM");
	assertEquals(root.getCMDComponent().get(1).getCMDComponent().get(2)
		.getFilename(), "leaf-RR");

	// the actual job
	// nulling the filenames will be called as a method of testrestservice
	ComponentRegistryRestService restservice = new ComponentRegistryRestService();
	restservice.setFileNamesToNullCurrent(root);

	// check if the filenames are nulled
	assertEquals(root.getFilename(), null);

	assertEquals(root.getCMDComponent().get(0).getFilename(), null);
	assertEquals(root.getCMDComponent().get(1).getFilename(), null);

	assertEquals(root.getCMDComponent().get(0).getCMDComponent().get(0)
		.getFilename(), null);
	assertEquals(root.getCMDComponent().get(0).getCMDComponent().get(1)
		.getFilename(), null);
	assertEquals(root.getCMDComponent().get(0).getCMDComponent().get(2)
		.getFilename(), null);

	assertEquals(root.getCMDComponent().get(1).getCMDComponent().get(0)
		.getFilename(), null);
	assertEquals(root.getCMDComponent().get(1).getCMDComponent().get(1)
		.getFilename(), null);
	assertEquals(root.getCMDComponent().get(1).getCMDComponent().get(2)
		.getFilename(), null);
    }
    
    @Test
    public void testGetDescription() throws Exception{
        fillUpPublicItems();
        String id = ComponentDescription.COMPONENT_PREFIX + "component1";
        ComponentDescription component = getAuthenticatedResource(getResource().path("/registry/items/" + id)).accept(MediaType.APPLICATION_JSON).get(ComponentDescription.class);
        assertNotNull(component);
        assertEquals(id, component.getId());
        assertEquals("component1", component.getName());
        assertEquals("Test Description", component.getDescription());

    }
}
