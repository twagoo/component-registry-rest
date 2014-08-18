/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.impl.database.GroupService;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.CommentsDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import com.sun.jersey.api.client.ClientResponse;
import java.text.ParseException;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.*;
import org.junit.Test;

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
    
 
    
    @Test
    public void testGetGroupComments() throws Exception {

        System.out.println("test getGroupComments");

        fillUpGroupA();
        fillUpGroupB();        
        fillUpGroupC();

       
        RegistryTestHelper.addComment(baseRegistry, "COMMENTc1",  ComponentDescription.COMPONENT_PREFIX + "component-1",
                "JUnit@test.com");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTp1",  ProfileDescription.PROFILE_PREFIX + "profile-1",
                "JUnit@test.com");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTBc1",  ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1",
                "anotherPrincipal");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTBp1",  ProfileDescription.PROFILE_PREFIX + "Bprofile-1",
                "anotherPrincipal");
       (new RegistryTestHelper()).addCommentBypassAuthorisation(commentsDao, "COMMENTCc1",  ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1",
                "anotherPrincipal");
       (new RegistryTestHelper()).addCommentBypassAuthorisation(commentsDao, "COMMENTCp1",  ProfileDescription.PROFILE_PREFIX + "Cprofile-1","anotherPrincipal");
         
          
                // lists 
        
        List<Comment> response = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1" + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);        
        assertEquals(1, response.size());
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ProfileDescription.PROFILE_PREFIX + "profile-1" + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);        
        assertEquals(1, response.size());
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1" + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);        
        assertEquals(1, response.size());
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1" + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);        
        assertEquals(1, response.size());
        
        
    
        ClientResponse clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX+"Cprofile-1"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());
        
         clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX+"Ccomponent-1"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());
        
        // particular comments
        
       Comment responseComment = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1" + "/comments/1"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);        
        assertNotNull(responseComment);
      
        assertEquals(1, Long.parseLong(responseComment.getId()));
        
        responseComment = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1" + "/comments/2"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);        
        assertNotNull(responseComment);
       
        assertEquals(2, Long.parseLong(responseComment.getId()));
        
       responseComment = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1" + "/comments/3"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);        
        assertNotNull(responseComment);
        assertEquals(3, Long.parseLong(responseComment.getId()));
        
        responseComment = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1" + "/comments/4"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);        
        assertNotNull(responseComment);
        assertEquals(4, Long.parseLong(responseComment.getId()));
       
        clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX+"Cprofile-1/comments/6"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());
        
         clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX+"Ccomponent-1/comments/5"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());
        
    }

}
