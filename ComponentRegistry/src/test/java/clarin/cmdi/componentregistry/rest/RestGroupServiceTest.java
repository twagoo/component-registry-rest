/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.impl.database.GroupService;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.CommentsDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import clarin.cmdi.componentregistry.rss.Rss;
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
public class RestGroupServiceTest extends ComponentRegistryRestServiceTestCase {
    
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
    
 //    Response createNewGroup(String groupName) throws IOException;
    
    @Test
    public void testCreateNewGroup() {
        System.out.println("test createNewGroup");
        ClientResponse cr = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/create").queryParam("groupName", "newGroup"))
                .accept(MediaType.TEXT_XML).post(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        assertEquals("Group with the name newGroup is created and given an id 1", cr.getEntity(String.class));
    }
    
    
    
    
//  List<Group> getGroupsOwnedByUser(String pricipalName) throws IOException;
    
    @Test
    public void testGetGroupsOwnedByUser() {
        System.out.println("test GetGroupsOwnedByUser");
        ClientResponse cr = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/create").queryParam("groupName", "newGroup1"))
                .accept(MediaType.TEXT_XML).post(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        ClientResponse cr1 = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/create").queryParam("groupName", "newGroup2"))
                .accept(MediaType.TEXT_XML).post(ClientResponse.class);
        assertEquals(200, cr1.getStatus());
        
        // test itself
        
       List<Group> result = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/principal").queryParam("principalName", DummyPrincipal.DUMMY_PRINCIPAL.getName()))
                .accept(MediaType.APPLICATION_XML).get(GROUP_LIST_GENERICTYPE);
        
       assertEquals(2, result.size());
       assertEquals("newGroup1", result.get(0).getName());
       assertEquals("newGroup2", result.get(1).getName());
    }
    
    
//    List<Group> getGroupsTheCurrentUserIsAMemberOf();   
    
    @Test
    public void testGetGroupsTheCurrentUserIsAMemberOf() throws ItemNotFoundException{
        System.out.println("test getGroupsTheCurrentUserIsAMemberOfr");
        
        MakeGroupA();
        MakeGroupB();
        // test itself
        
       List<Group> result = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/usermembership"))
                .accept(MediaType.APPLICATION_XML).get(GROUP_LIST_GENERICTYPE);
        
       assertEquals(1, result.size());
       assertEquals("group B", result.get(0).getName());
    }
//   
//    List<Group> getGroupsTheItemIsAMemberOf(String itemId);
   
    @Test
    public void testGetGroupsTheItemIsAMemberOf() throws ParseException, JAXBException, ItemNotFoundException{
        System.out.println("test getGroupsTheItemIsAMemberOf");
        
        fillUpGroupA();
        fillUpGroupB();
        // test itself
        
       List<Group> result = this.getAuthenticatedResource(getResource()
                .path("/registry/items/"+ComponentDescription.COMPONENT_PREFIX+"component-1/groups"))
                .accept(MediaType.APPLICATION_XML).get(GROUP_LIST_GENERICTYPE);
        
       assertEquals(1, result.size());
       assertEquals("group A", result.get(0).getName());
       
       result = this.getAuthenticatedResource(getResource()
                .path("/registry/items/"+ComponentDescription.COMPONENT_PREFIX+"Bcomponent-1/groups"))
                .accept(MediaType.APPLICATION_XML).get(GROUP_LIST_GENERICTYPE);
        
       assertEquals(1, result.size());
       assertEquals("group B", result.get(0).getName());
       
       result = this.getAuthenticatedResource(getResource()
                .path("/registry/items/"+ProfileDescription.PROFILE_PREFIX+"Bprofile-1/groups"))
                .accept(MediaType.APPLICATION_XML).get(GROUP_LIST_GENERICTYPE);
        
       assertEquals(1, result.size());
       assertEquals("group B", result.get(0).getName());
       
       result = this.getAuthenticatedResource(getResource()
                .path("/registry/items/"+ProfileDescription.PROFILE_PREFIX+"profile-1/groups"))
                .accept(MediaType.APPLICATION_XML).get(GROUP_LIST_GENERICTYPE);
        
       assertEquals(1, result.size());
       assertEquals("group A", result.get(0).getName());
    }
    


  
      
    @Test
    public void testListGroupNames() throws ItemNotFoundException{
        System.out.println("test listGroupNames");
        
        MakeGroupA();
        MakeGroupB();
        
        // test itself
        
       ClientResponse cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/names")).accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       List<String> result = cr.getEntity(ComponentRegistryRestService.StringsWrapper.class).strings;
       assertEquals(2, result.size());
       assertEquals("group A", result.get(0));
       assertEquals("group B", result.get(1));
    }
    
//
//    Response isOwner(String groupName) throws IOException;
    
    @Test
    public void testIsOwner() throws ItemNotFoundException{
        System.out.println("test isOwner");
        
        MakeGroupA();
        MakeGroupB();
        
        // test itself
        
       ClientResponse cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/ownership").queryParam("groupName", "group A")).accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       String result = cr.getEntity(String.class);
       assertEquals("true", result);
       
       cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/ownership").queryParam("groupName", "group B")).accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       result = cr.getEntity(String.class);
       assertEquals("false", result);
    }
    
    
//
//    Response makeGroupMember(String groupName, String principalName) throws IOException;
    
    @Test
    public void testMakeGroupMember() throws ItemNotFoundException{
        System.out.println("test makeGroupMember");
        
        MakeGroupA();
        MakeGroupB();
        // test itself
        
       //MultivaluedMap<String, String> params  = new  MultivaluedHashMap<String, String>(); 
       ClientResponse cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/makemember").queryParam("groupName", "group A").queryParam("principalName", "anotherPrincipal")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       
       assertTrue(groupService.userGroupMember("anotherPrincipal", "1"));
       
               ;
       cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/makemember").queryParam("groupName", "group B").queryParam("principalName", "anotherPrincipal")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
       assertEquals(403, cr.getStatus());
    }
    
//
//   Response listProfiles(String groupId) throws IOException;
    
    @Test
    public void testListProfilesAndComponents() throws Exception{
        System.out.println("test listProfilesAndComponents");
        
        fillUpGroupA();
        
        // test itself
        
       ClientResponse cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/profiles").queryParam("groupId", "1")).accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       List<String> result = cr.getEntity(ComponentRegistryRestService.StringsWrapper.class).strings;
       assertEquals(1, result.size());
       assertEquals(ProfileDescription.PROFILE_PREFIX+"profile-1", result.get(0));
       
       cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/components").queryParam("groupId", "1")).accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       result = cr.getEntity(ComponentRegistryRestService.StringsWrapper.class).strings;
       assertEquals(2, result.size());
       assertEquals(ComponentDescription.COMPONENT_PREFIX+"component-1", result.get(0));
       assertEquals(ComponentDescription.COMPONENT_PREFIX+"component-2", result.get(1));
    }
    

 
//
//    Response getGroupNameById(String groupId) throws IOException;
    
    @Test
    public void testGetGroupNamebyId() throws Exception{
        System.out.println("test getGroupNamebyId");
        
        MakeGroupA();
        MakeGroupB();
        // test itself
        
       ClientResponse cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/nameById").queryParam("groupId", "1")).accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       String result = cr.getEntity(String.class);
       assertEquals("group A", result);
       
      cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/nameById").queryParam("groupId", "2")).accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       result = cr.getEntity(String.class);
       assertEquals("group B", result);
    }
    
//
//    Response getGroupIdByName(String groupName) throws IOException;
    
    @Test
    public void testGetIdByGroupName() throws Exception{
        System.out.println("test getIdByGroupName");
        
        MakeGroupA();
        MakeGroupB();
        // test itself
        
       ClientResponse cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/idByName").queryParam("groupName", "group B")).accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       String result = cr.getEntity(String.class);
       assertEquals("2", result);
       
      cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/groups/idByName").queryParam("groupName", "group A")).accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       result = cr.getEntity(String.class);
       assertEquals("1", result);
    }
    
     
//    Response transferItemOwnershipToGroup(String itemId, long groupId) throws IOException;   
//   

    @Test
    public void testTransferOwnership() throws Exception{
        System.out.println("test makeTransferOwnership");
        
        fillUpGroupA();
        fillUpGroupB();        
        fillUpGroupC();
        // test itself
        
        
       RegistryTestHelper.addComponent(baseRegistry, "test_component", false);
       RegistryTestHelper.addProfile(baseRegistry, "test_profile", false);
       String test_profile_id = ProfileDescription.PROFILE_PREFIX+"test_profile";
       String test_component_id = ComponentDescription.COMPONENT_PREFIX+"test_component";
       //I'm not a member
       ClientResponse cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/items/"+test_profile_id+"/transferownership").queryParam("groupId", "3")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
       assertEquals(403, cr.getStatus());
       
       //make me a member
       groupService.makeMember(DummyPrincipal.DUMMY_PRINCIPAL.getName(), "group C");
       assertTrue(groupService.userGroupMember(DummyPrincipal.DUMMY_PRINCIPAL.getName(), "3"));
       
       cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/items/"+test_profile_id+"/transferownership").queryParam("groupId", "3")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       cr  = this.getAuthenticatedResource(getResource()
                .path("/registry/items/"+test_component_id+"/transferownership").queryParam("groupId", "3")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
       assertEquals(200, cr.getStatus());
       
       
       List<String> components = groupService.getComponentIdsInGroup(3);
       assertEquals(3, components.size());
       assertEquals(test_component_id, components.get(2));
       List<String> profiles = groupService.getProfileIdsInGroup(3);
       assertEquals(2, profiles.size());
       assertEquals(test_profile_id, profiles.get(1));
       
    }
    
    @Test
    public void testGetGroupProfilesAndComponents() throws Exception {

        System.out.println("test getGroupProfiles");

        fillUpGroupA();
        fillUpGroupB();        
        fillUpGroupC();

        // lists
        
        List<ProfileDescription> response = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles").queryParam("registrySpace", "group").queryParam("groupid", "1")).accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(1, response.size());
        
        List<ComponentDescription> responseC = this.getAuthenticatedResource(getResource()
                .path("/registry/components").queryParam("registrySpace", "group").queryParam("groupid", "1")).accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, responseC.size());
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles").queryParam("registrySpace", "group").queryParam("groupid", "2")).accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(1, response.size());
        
        responseC = this.getAuthenticatedResource(getResource()
                .path("/registry/components").queryParam("registrySpace", "group").queryParam("groupid", "2")).accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, responseC.size());
        
        ClientResponse clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/components").queryParam("registrySpace", "group").queryParam("groupid", "3")).accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        
        assertEquals(403, clientResponse.getStatus());
        
        // particular components and profiles
        
        CMDComponentSpec component = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX+"profile-1"))
                .accept(MediaType.APPLICATION_JSON).get(CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Actor", component.getCMDComponent().getName());
    
        component = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX+"component-1"))
                .accept(MediaType.APPLICATION_JSON).get(CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Access", component.getCMDComponent().getName());
    
        component = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX+"Bprofile-1"))
                .accept(MediaType.APPLICATION_JSON).get(CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Actor", component.getCMDComponent().getName());
    
        component = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX+"Bcomponent-1"))
                .accept(MediaType.APPLICATION_JSON).get(CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Access", component.getCMDComponent().getName());
    
        clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX+"Cprofile-1"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());
        
         clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX+"Ccomponent-1"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());
        
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
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX+"Cprofile-1/comments"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());
        
         clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX+"Ccomponent-1/comments"))
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
   
    @Test
    public void testGetGroupRss() throws Exception {

        System.out.println("test getGroupRss");

        fillUpGroupA();
        fillUpGroupB();        
        fillUpGroupC();

        
         // lists of profiles and components
        
        Rss response = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/rss").queryParam("registrySpace", "group").queryParam("groupid", "1")).accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(1, response.getChannel().getItem().size());
        
         response = this.getAuthenticatedResource(getResource()
                .path("/registry/components/rss").queryParam("registrySpace", "group").queryParam("groupid", "1")).accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(2, response.getChannel().getItem().size());
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/rss").queryParam("registrySpace", "group").queryParam("groupid", "2")).accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(1, response.getChannel().getItem().size());
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/components/rss").queryParam("registrySpace", "group").queryParam("groupid", "2")).accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(2, response.getChannel().getItem().size());
        
        ClientResponse clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/components").queryParam("registrySpace", "group").queryParam("groupid", "3")).accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        
        assertEquals(403, clientResponse.getStatus());
       
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
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1" + "/comments/rss"))
                .accept(MediaType.APPLICATION_XML)
                .get(Rss.class);        
        assertEquals(1, response.getChannel().getItem().size());
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1" + "/comments/rss"))
                .accept(MediaType.APPLICATION_XML)
                .get(Rss.class);        
        assertEquals(1, response.getChannel().getItem().size());
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1" + "/comments/rss"))
                .accept(MediaType.APPLICATION_XML)
                .get(Rss.class);        
        assertEquals(1, response.getChannel().getItem().size());
        
        response = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1" + "/comments/rss"))
                .accept(MediaType.APPLICATION_XML)
                .get(Rss.class);        
        assertEquals(1, response.getChannel().getItem().size());
        
        
    
        clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/profiles/" + ProfileDescription.PROFILE_PREFIX+"Cprofile-1/comments/rss"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());
        
         clientResponse = this.getAuthenticatedResource(getResource()
                .path("/registry/components/" + ComponentDescription.COMPONENT_PREFIX+"Ccomponent-1/comments/rss"))
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
