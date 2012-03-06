package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.RegistryUser;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import org.junit.Before;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class UserDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserDao userDao;

    @Before
    public void init() {
	ComponentRegistryTestDatabase.resetDatabase(jdbcTemplate);
	ComponentRegistryTestDatabase.createTableRegistryUser(jdbcTemplate);
    }

    @Test
    public void testInjection() {
	assertNotNull(jdbcTemplate);
	assertNotNull(userDao);
    }

    @Test
    public void testInsertUser() {
	RegistryUser testUser = createTestUser();

	assertEquals(0, userDao.getAllUsers().size());
	Number newId = userDao.insertUser(testUser);
	assertNotNull(newId);

	List<RegistryUser> users = userDao.getAllUsers();
	assertEquals(1, users.size());

	assertEquals(TEST_USER_NAME, users.get(0).getName());
	assertEquals(TEST_USER_PRINCIPAL_NAME, users.get(0).getPrincipalName());
    }

    @Test
    public void testGetAllUsers() {
	assertEquals(0, userDao.getAllUsers().size());
    }

    @Test
    public void testGetUserByPrincipalName() {
	RegistryUser testUser = createTestUser();
	userDao.insertUser(testUser);

	assertNotNull(userDao.getByPrincipalName(TEST_USER_PRINCIPAL_NAME));
	assertNull(userDao.getByPrincipalName("NON-EXISTING PRINCIPAL NAME"));
    }

    public static RegistryUser createTestUser() {
	RegistryUser testUser = new RegistryUser();
	testUser.setName(TEST_USER_NAME);
	testUser.setPrincipalName(TEST_USER_PRINCIPAL_NAME);
	return testUser;
    }

    @Test
    public void testUpdateUser() {
	RegistryUser testUser = createTestUser();
	Number id = userDao.insertUser(testUser);

	testUser.setName("I. Changed");
	userDao.updateUser(id, testUser);

	assertEquals("I. Changed", userDao.getById(id).getName());
    }
    public final static String TEST_USER_NAME = "Aap";
    public final static String TEST_USER_PRINCIPAL_NAME = "aap@clarin.eu";
}
