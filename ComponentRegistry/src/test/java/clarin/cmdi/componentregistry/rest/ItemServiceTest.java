/*
 * Copyright (C) 2017 CLARIN ERIC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.GroupService;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ItemServiceTest extends ComponentRegistryRestServiceTestCase {

    private static final GenericType<List<Group>> GENERIC_GROUP_LIST_TYPE = new GenericType<List<Group>>() {
    };

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private GroupService groupService;

    private RegistryUser registryUser;
    private ComponentRegistry baseRegistry;

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
        registryUser = createUserRecord();
        baseRegistry = componentRegistryFactory.getBaseRegistry(DummyPrincipal.DUMMY_CREDENTIALS);
    }

    private long createTeam(String teamName) throws ItemNotFoundException, ValidationException {
        long groupId = groupService.createNewGroup(teamName, registryUser.getPrincipalName());
        groupService.makeMember(registryUser.getPrincipalName(), teamName);
        return groupId;
    }

    private void addToTeam(final String componentId, final long groupId) {
        final Ownership ownership = new Ownership();
        ownership.setComponentRef(componentId);
        ownership.setGroupId(groupId);
        groupService.addOwnership(ownership);
    }

    @Test
    public void testGetPublicProfileDescription() throws Exception {
        final String profileId = RegistryTestHelper.addProfile(baseRegistry, "profile1", true).getId();

        BaseDescription result = getResource().path("/items/" + profileId)
                .accept(MediaType.APPLICATION_XML)
                .get(BaseDescription.class);
        assertNotNull(result);
        assertThat(result, instanceOf(ProfileDescription.class));
        assertEquals(profileId, result.getId());
    }

    @Test
    public void testGetPublicComponentDescription() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component1", true).getId();

        BaseDescription result = getResource().path("/items/" + componentId)
                .accept(MediaType.APPLICATION_XML)
                .get(BaseDescription.class);
        assertNotNull(result);
        assertThat(result, instanceOf(ComponentDescription.class));
        assertEquals(componentId, result.getId());
    }

    @Test
    public void testGetPrivateProfileDescription() throws Exception {
        final String profileId = RegistryTestHelper.addProfile(baseRegistry, "profile3", false).getId();

        BaseDescription result = getAuthenticatedResource("/items/" + profileId)
                .accept(MediaType.APPLICATION_XML)
                .get(BaseDescription.class);
        assertNotNull(result);
        assertThat(result, instanceOf(ProfileDescription.class));
        assertEquals(profileId, result.getId());
    }

    @Test
    public void testGetPrivateComponentDescription() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();

        BaseDescription result = getAuthenticatedResource("/items/" + componentId)
                .accept(MediaType.APPLICATION_XML)
                .get(BaseDescription.class);
        assertNotNull(result);
        assertThat(result, instanceOf(ComponentDescription.class));
        assertEquals(componentId, result.getId());
    }

    @Test
    public void testGetPrivateItemUnAuthenticated() throws Exception {
        final String profileId = RegistryTestHelper.addProfile(baseRegistry, "profile3", false).getId();

        ClientResponse result = getResource().path("/items/" + profileId)
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetPrivateItemForbidden() throws Exception {
        final String componentId = RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "component3", false).getId();

        ClientResponse result = getAuthenticatedResource("/items/" + componentId)
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertEquals(Status.FORBIDDEN.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetItemGroups() throws Exception {
        final String componentId = RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "component3", false).getId();
        addToTeam(componentId, createTeam("testGroup1"));
        addToTeam(componentId, createTeam("testGroup2"));

        final List<Group> result = getAuthenticatedResource("/items/" + componentId + "/groups")
                .accept(MediaType.APPLICATION_XML)
                .get(GENERIC_GROUP_LIST_TYPE);
        assertEquals(2, result.size());
        assertEquals("testGroup1", result.get(0).getName());
        assertEquals("testGroup2", result.get(1).getName());
    }

    @Test
    public void testTransferItemToGroup() throws Exception {
        final String componentId = RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "component3", false).getId();
        addToTeam(componentId, createTeam("testGroup1"));
        //target team
        long targetTeamId = createTeam("testGroup2");

        final ClientResponse result = getAuthenticatedResource(
                getResource().path("/items/" + componentId + "/transferownership").queryParam("groupId", String.valueOf(targetTeamId)))
                .accept(MediaType.APPLICATION_XML)
                .post(ClientResponse.class);
        assertEquals(Status.OK.getStatusCode(), result.getStatus());
    }
}
