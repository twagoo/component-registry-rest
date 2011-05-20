package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryBeanFactory;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase;
import clarin.cmdi.componentregistry.impl.database.UserDao;
import clarin.cmdi.componentregistry.model.UserMapping.User;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext-database-impl.xml"})
public class DbImplComponentRegistryRestServiceTest extends ComponentRegistryRestServiceTest {
    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserDao userDao;
    private ComponentRegistry testRegistry;


    @Before
    public void init() {
	ComponentRegistryDatabase.resetDatabase(jdbcTemplate);
	ComponentRegistryDatabase.createTableRegistryUser(jdbcTemplate);
	ComponentRegistryDatabase.createTableXmlContent(jdbcTemplate);
	ComponentRegistryDatabase.createTableProfileDescription(jdbcTemplate);
	ComponentRegistryDatabase.createTableComponentDescription(jdbcTemplate);
	createUserRecord();
	setUpTestRegistry();
    }

    private void createUserRecord(){
	User user = new User();
	user.setName("Database test user");
	user.setPrincipalName(DummyPrincipal.DUMMY_PRINCIPAL.getName());
	userDao.insertUser(user);
    }

    private void setUpTestRegistry() {
	// Get public component registry
	testRegistry = componentRegistryBeanFactory.getNewComponentRegistry();
    }

    @Override
    protected String getApplicationContextFile() {
	return "classpath:applicationContext-database-impl.xml";
    }

    @Override
    protected ComponentRegistry getTestRegistry() {
	return testRegistry;
    }

    @Override
    protected String expectedUserId(String principal) {
	return userDao.getByPrincipalName(principal).getId().toString();
    }

    @Override
    protected ComponentRegistryFactory getRegistryFactory() {
	return componentRegistryFactory;
    }
}
