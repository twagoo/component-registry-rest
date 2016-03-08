/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.impl.database.GroupService;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.CommentResponse;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.CommentsDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 *
 * @author olhsha
 */
public class SanboxTest extends ComponentRegistryRestServiceTestCase {
    
    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired 
    private GroupService groupService;
    @Autowired 
    private UserDao userDao;
     @Autowired 
    private CommentsDao commentsDao;
    private ComponentRegistry baseRegistry;

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
        createUserRecord();
        baseRegistry = componentRegistryFactory.getBaseRegistry(DummyPrincipal.DUMMY_CREDENTIALS);
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
    
     protected void createAntherUserRecord() {
	RegistryUser user = new RegistryUser();
	user.setName("Another database test user");
	user.setPrincipalName("anotherPrincipal");
	userDao.save(user);
    }
     
    private void MakeGroupA(){
        groupService.createNewGroup("group A", DummyPrincipal.DUMMY_PRINCIPAL.getName());
    }
    
     private void fillUpGroupA() throws ParseException, JAXBException, ItemNotFoundException{
         
        MakeGroupA();
        
        RegistryTestHelper.addProfile(baseRegistry, "profile-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "component-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "component-2", false);
        
        Ownership ownership = new Ownership();
        ownership.setComponentId(ProfileDescription.PROFILE_PREFIX+"profile-1");
        ownership.setGroupId(1);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);
        
        ownership.setComponentId(ComponentDescription.COMPONENT_PREFIX+"component-1");
        ownership.setGroupId(1);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);
        
        ownership.setComponentId(ComponentDescription.COMPONENT_PREFIX+"component-2");
        ownership.setGroupId(1);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);
        
    }
     
     
    
    private void MakeGroupB() throws ItemNotFoundException{
        createAntherUserRecord();
        groupService.createNewGroup("group B", "anotherPrincipal");
        groupService.makeMember(DummyPrincipal.DUMMY_PRINCIPAL.getName(), "group B");
    }
    
     private void MakeGroupC() throws ItemNotFoundException{
        groupService.createNewGroup("group C", "anotherPrincipal");
    }
    
     private void fillUpGroupB() throws ParseException, JAXBException, ItemNotFoundException{
         
        MakeGroupB();
        
        RegistryTestHelper.addProfile(baseRegistry, "Bprofile-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "Bcomponent-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "Bcomponent-2", false);
        
        Ownership ownership = new Ownership();
        ownership.setComponentId(ProfileDescription.PROFILE_PREFIX+"Bprofile-1");
        ownership.setGroupId(2);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);
        
        ownership.setComponentId(ComponentDescription.COMPONENT_PREFIX+"Bcomponent-1");
        ownership.setGroupId(2);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);
        
        ownership.setComponentId(ComponentDescription.COMPONENT_PREFIX+"Bcomponent-2");
        ownership.setGroupId(2);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);
        
    }
     
     private void fillUpGroupC() throws ParseException, JAXBException, ItemNotFoundException{
         
        MakeGroupC();
        
        RegistryTestHelper.addProfileAnotherPrincipal(baseRegistry, "Cprofile-1", false);
        RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "Ccomponent-1", false);
        RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "Ccomponent-2", false);
        
        Ownership ownership = new Ownership();
        ownership.setComponentId(ProfileDescription.PROFILE_PREFIX+"Cprofile-1");
        ownership.setGroupId(3);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);
        
        ownership.setComponentId(ComponentDescription.COMPONENT_PREFIX+"Ccomponent-1");
        ownership.setGroupId(3);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);
        
        ownership.setComponentId(ComponentDescription.COMPONENT_PREFIX+"Ccomponent-2");
        ownership.setGroupId(3);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);
        
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
    public void testGetRegisteredGroupProfilecomponentRawData() throws Exception {

        System.out.println("test getRegisteredComponentAndProfileRawData");

        fillUpGroupB();        
        fillUpGroupC();

        String id = ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1";
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
        
        id = ProfileDescription.PROFILE_PREFIX + "Bprofile-1";
        String profile = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + id + "/xsd"))
                .accept(MediaType.TEXT_XML).get(String.class).trim();
        assertTrue(profile
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));

        profile = this.getAuthenticatedResource(getResource().path("/registry/profiles/" + id + "/xml"))
                .accept(MediaType.TEXT_XML).get(String.class).trim();
        assertTrue(profile
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<CMD_ComponentSpec"));
        assertTrue(profile.endsWith("</CMD_ComponentSpec>"));
        assertTrue(profile.contains("xsi:schemaLocation"));

        id = ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1";
        ClientResponse resp = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + id + "/xsd"))
                .accept(MediaType.TEXT_XML).get( ClientResponse.class);
        assertEquals("XSD should always be available", 200, resp.getStatus());
        
        id = ProfileDescription.PROFILE_PREFIX + "Cprofile-1";
       resp = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + id + "/xsd"))
                .accept(MediaType.TEXT_XML).get( ClientResponse.class);
        assertEquals("XSD should always be available", 200, resp.getStatus());
        
        
    }

}
