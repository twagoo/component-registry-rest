package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.UserMapping.User;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
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
@ContextConfiguration(locations = {"/applicationContext-database-impl.xml"})
public class UserDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserDao userDao;

    @Before
    public void init() {
	ComponentRegistryDatabase.resetDatabase(jdbcTemplate);
	ComponentRegistryDatabase.createTableRegistryUser(jdbcTemplate);
    }

    @Test
    public void testInjection() {
	assertNotNull(jdbcTemplate);
	assertNotNull(userDao);
    }

    @Test
    public void testInsertUser() {
	User testUser = new User();
	testUser.setName("Aap");
	testUser.setPrincipalName("aap@clarin.eu");

	assertEquals(0, userDao.getAllUsers().size());
	userDao.insertUser(testUser);
	List<User> users = userDao.getAllUsers();
	assertEquals(1, users.size());
	assertEquals("Aap", users.get(0).getName());
	assertEquals("aap@clarin.eu", users.get(0).getPrincipalName());
    }

    @Test
    public void testGetAllUsers() {
	assertEquals(0, userDao.getAllUsers().size());
    }
}
