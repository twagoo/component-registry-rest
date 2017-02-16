/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.model.AuthenticationInfo;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.ClientResponse;
import java.security.Principal;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author twagoo
 */
public class AuthenticationRestServiceTest extends ComponentRegistryRestServiceTestCase {

    private static final String UNREGISTERED_USER_NAME = "unregistered@unregistered.org";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final String REGISTRY_BASE = "/registry/1.x";
    public static final String NON_CANONICAL_REGISTRY_BASE = "/registry/1.1";

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
        createUserRecord();
    }

    @Override
    protected Map<String, String> getSecurityFilterInitParams() {
        return ImmutableMap.of(DummySecurityFilter.ALLOWED_USERS_PARAM, DummyPrincipal.DUMMY_PRINCIPAL.getName() + " " + UNREGISTERED_USER_NAME);
    }

    /**
     * Test of getAuthenticationInformation method, of class
     * AuthenticationRestService.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetAuthenticationInformationUnauthenticated() throws Exception {
        final ClientResponse response = getResource().path("/authentication").get(ClientResponse.class);
        assertEquals(200, response.getStatus());

        final AuthenticationInfo authInfo = response.getEntity(AuthenticationInfo.class);
        assertFalse(authInfo.isAuthenticated());
    }

    /**
     *
     * Test of getAuthenticationInformation method, of class
     * AuthenticationRestService.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetAuthenticationInformationAuthenticatedKnownUser() throws Exception {
        final DummyPrincipal registeredUser = DummyPrincipal.DUMMY_PRINCIPAL;

        if (getUserDao().getByPrincipalName(registeredUser.getName()) == null) {
            throw new RuntimeException("Expected user does not exist, cannot complete test");
        }

        final ClientResponse response
                = getAuthenticatedResource(registeredUser,
                        getResource().path("/authentication"))
                        .get(ClientResponse.class);

        assertEquals(200, response.getStatus());

        final AuthenticationInfo authInfo = response.getEntity(AuthenticationInfo.class);
        assertTrue(authInfo.isAuthenticated());
        assertEquals(registeredUser.getName(), authInfo.getUsername());
    }

    /**
     *
     * Test of getAuthenticationInformation method, of class
     * AuthenticationRestService.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetAuthenticationInformationAuthenticatedUnknownUser() throws Exception {
        final Principal unregisteredUser = new DummyPrincipal(UNREGISTERED_USER_NAME);

        if (getUserDao().getByPrincipalName(unregisteredUser.getName()) != null) {
            throw new RuntimeException("Unregistered exists in database, cannot complete test");
        }

        final ClientResponse response
                = getAuthenticatedResource(
                        unregisteredUser,
                        getResource().path("/authentication"))
                        .get(ClientResponse.class);

        assertEquals(200, response.getStatus());

        final AuthenticationInfo authInfo = response.getEntity(AuthenticationInfo.class);
        assertTrue(authInfo.isAuthenticated());
        assertEquals(unregisteredUser.getName(), authInfo.getUsername());
    }

    /**
     * Test of triggerAuthenticationRequest method, of class
     * AuthenticationRestService.
     */
    @Test
    public void testTriggerAuthenticationRequest() {

    }

}
