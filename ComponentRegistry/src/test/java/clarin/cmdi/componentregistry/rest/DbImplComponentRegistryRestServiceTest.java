package clarin.cmdi.componentregistry.rest;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryBeanFactory;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class DbImplComponentRegistryRestServiceTest extends ComponentRegistryRestServiceTest {
    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ComponentRegistry testRegistry;


    @Before
    public void init() {
	ComponentRegistryDatabase.resetAndCreateAllTables(jdbcTemplate);
	createUserRecord();
	setUpTestRegistry();
    }

    private void setUpTestRegistry() {
	// Get public component registry
	testRegistry = componentRegistryBeanFactory.getNewComponentRegistry();
    }

    @Override
    protected String getApplicationContextFile() {
	return "classpath:applicationContext.xml";
    }

    @Override
    protected ComponentRegistry getTestRegistry() {
	return testRegistry;
    }

    @Override
    protected String expectedUserId(String principal) {
	return getUserDao().getByPrincipalName(principal).getId().toString();
    }

    @Override
    protected ComponentRegistryFactory getRegistryFactory() {
	return componentRegistryFactory;
    }
}
