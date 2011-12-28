package clarin.cmdi.componentregistry.impl.database;

import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.Before;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class ComponentRegistryFactoryDbImplTest {

    @Autowired
    private ComponentRegistryFactoryDbImpl componentRegistryFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
	ComponentRegistryTestDatabase.resetDatabase(jdbcTemplate);
	ComponentRegistryTestDatabase.createTableRegistryUser(jdbcTemplate);
    }

    @Test
    public void testInjection() {
	assertNotNull(componentRegistryFactory);
	assertNotNull(jdbcTemplate);
    }

    @Test
    public void testGetPublicRegistry() {
	ComponentRegistry registry = componentRegistryFactory.getPublicRegistry();
	assertNotNull(registry);
    }

    @Test
    public void getComponentRegistry() {
	// Get public
	assertNotNull(componentRegistryFactory.getComponentRegistry(false, null));

	// Get for non-existing user
	final RegistryUser testUser = UserDaoTest.createTestUser();
	UserCredentials credentials = new DummyPrincipal(testUser.
		getPrincipalName()).getCredentials();

	ComponentRegistryDbImpl cr1 = (ComponentRegistryDbImpl) componentRegistryFactory.
		getComponentRegistry(true, credentials);
	assertNotNull(cr1);
	// Get for existing user
	ComponentRegistryDbImpl cr2 = (ComponentRegistryDbImpl) componentRegistryFactory.
		getComponentRegistry(true, credentials);
	assertNotNull(cr2);
	assertEquals(cr1.getUserId(), cr2.getUserId());

	// Get for another new user
	UserCredentials credentials2 = new DummyPrincipal(testUser.
		getPrincipalName() + "2").getCredentials();
	ComponentRegistryDbImpl cr3 = (ComponentRegistryDbImpl) componentRegistryFactory.
		getComponentRegistry(true, credentials2);
	assertNotNull(cr3);
	assertNotSame(cr1.getUserId(), cr3.getUserId());
    }

    @Test
    public void testGetOtherUserComponentRegistry() {
	UserCredentials userCredentials = DummyPrincipal.DUMMY_PRINCIPAL.
		getCredentials();

	// Create registry for new user
	ComponentRegistryDbImpl cr1 = (ComponentRegistryDbImpl) componentRegistryFactory.
		getComponentRegistry(true, userCredentials);

	String id = cr1.getUserId().toString();

	// Get it as admin
	ComponentRegistryDbImpl cr2 = (ComponentRegistryDbImpl) componentRegistryFactory.
		getOtherUserComponentRegistry(DummyPrincipal.DUMMY_ADMIN_PRINCIPAL, id);
	assertNotNull(cr2);
	// Should be this user's registry
	assertEquals(cr1.getUserId(), cr2.getUserId());

	// Try get it as non-admin
	try {
	    componentRegistryFactory.getOtherUserComponentRegistry(DummyPrincipal.DUMMY_PRINCIPAL, id);
	    fail("Non-admin can get other user's component registry");
	} catch (Exception ex) {
	    // Exception should occur
	}
    }
}
