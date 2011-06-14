package clarin.cmdi.componentregistry.rest;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class DbImplConcurrentRestServiceTest extends ConcurrentRestServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
	ComponentRegistryDatabase.resetAndCreateAllTables(jdbcTemplate);
	createUserRecord();
    }

    @Override
    protected String getApplicationContextFile() {
	return "classpath:applicationContext.xml";
    }
}
