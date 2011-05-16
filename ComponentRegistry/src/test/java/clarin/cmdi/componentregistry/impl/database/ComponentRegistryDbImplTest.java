package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext-database-impl.xml"})
public class ComponentRegistryDbImplTest {

    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
	resetDatabase(jdbcTemplate);
	createTableComponentDescription(jdbcTemplate);
	createTableProfileDescription(jdbcTemplate);
	createTableXmlContent(jdbcTemplate);
    }

    @Test
    public void testRegisterComponent() throws Exception {
	ComponentRegistry register = getComponentRegistryForUser(null);
	ComponentDescription description = ComponentDescription.
		createNewDescription();

	description.setName("Aap");
	description.setDescription("MyDescription");

	assertEquals(0, register.getComponentDescriptions().size());
	assertEquals(0, register.getProfileDescriptions().size());

	CMDComponentSpec testComponent = RegistryTestHelper.getTestComponent();
	assertNull(testComponent.getHeader().getID());
	assertNull(testComponent.getHeader().getName());
	assertNull(testComponent.getHeader().getDescription());
	testComponent.getHeader().setDescription("Will not be overwritten");

	register.register(description, testComponent);

	assertEquals(1, register.getComponentDescriptions().size());
	assertEquals(0, register.getProfileDescriptions().size());

	ComponentDescription desc = register.getComponentDescriptions().get(0);
	assertNotNull(desc);
	CMDComponentSpec component = register.getMDComponent(desc.getId());
	assertNotNull(component);

	assertNull(register.getMDProfile(desc.getId()));

	ComponentDescription componentDescription = register.getComponentDescription(description.getId());
	assertNotNull(componentDescription);

	assertEquals("Header id should be set from description id", description.
		getId(), component.getHeader().getID());
	assertEquals("Aap", component.getHeader().getName());
	assertEquals("Will not be overwritten", component.getHeader().
		getDescription());
    }

    private ComponentRegistry getComponentRegistryForUser(Number userId) {
	ComponentRegistryDbImpl componentRegistry = componentRegistryBeanFactory.
		getNewComponentRegistry();
	componentRegistry.setUserId(userId);
	return componentRegistry;
    }
}
