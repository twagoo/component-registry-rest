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
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.*;

/**
 *
 * @author olhsha
 */
public class SanboxTest extends ComponentRegistryRestServiceTestCase {

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
    public void testDeleteCommentFromPublicProfile() throws Exception {

        System.out.println("testDeleteCommentFromPublicProfile");

        fillUpPublicItems();

        List<Comment> comments = this.getAuthenticatedResource(getResource().path(
                "/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(2, comments.size());
        Comment aComment = this.getAuthenticatedResource(getResource().path(
                "/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/" + profile1Comment1.getId())).get(Comment.class);
        assertNotNull(aComment);

        // Try to delete from other profile
        ClientResponse response = getAuthenticatedResource(
                "/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile2/comments/9999").delete(ClientResponse.class);
        assertEquals(500, response.getStatus());
        // Delete from correct profile
        response = getAuthenticatedResource(
                "/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/" + profile1Comment1.getId()).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        comments = this.getAuthenticatedResource(getResource().path(
                "/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(1, comments.size());

        response = getAuthenticatedResource(
                "/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/" + profile1Comment2.getId()).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        comments = this.getAuthenticatedResource(getResource().path(
                "/registry/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(0, comments.size());
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
