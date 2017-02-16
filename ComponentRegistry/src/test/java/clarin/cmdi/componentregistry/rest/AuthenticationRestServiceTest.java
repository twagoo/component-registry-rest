/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.model.AuthenticationInfo;
import com.sun.jersey.api.client.ClientResponse;
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

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ComponentRegistry baseRegistry;

    public static final String REGISTRY_BASE = "/registry/1.x";
    public static final String NON_CANONICAL_REGISTRY_BASE = "/registry/1.1";

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
        createUserRecord();
        baseRegistry = componentRegistryFactory.getBaseRegistry(DummyPrincipal.DUMMY_CREDENTIALS);
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
        if (getUserDao().getByPrincipalName(DummyPrincipal.DUMMY_PRINCIPAL.getName()) == null) {
            throw new RuntimeException("Expected user does not exist, cannot complete test");
        }

        final ClientResponse response
                = getAuthenticatedResource(
                        DummyPrincipal.DUMMY_PRINCIPAL,
                        getResource().path("/authentication"))
                        .get(ClientResponse.class);

        assertEquals(200, response.getStatus());

        final AuthenticationInfo authInfo = response.getEntity(AuthenticationInfo.class);
        assertTrue(authInfo.isAuthenticated());
        assertEquals(DummyPrincipal.DUMMY_PRINCIPAL.getName(), authInfo.getUsername());
    }

    /**
     * Test of triggerAuthenticationRequest method, of class
     * AuthenticationRestService.
     */
    @Test
    public void testTriggerAuthenticationRequest() {

    }

}
