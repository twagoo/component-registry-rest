package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.runner.RunWith;
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
        //you can use standard SQL statements executed by JdbcTemplate
        jdbcTemplate.execute("CREATE TABLE component_description ("
                + "id IDENTITY NOT NULL,"
                + "user_id integer,"
                + "    content_id integer NOT NULL,"
                + "  is_public boolean NOT NULL,"
                + "  is_deleted boolean DEFAULT false NOT NULL,"
                + "  component_id character varying NOT NULL,"
                + "  name character varying NOT NULL,"
                + "  description character varying NOT NULL,"
                + "  registration_date timestamp,"
                + "  creator_name character varying,"
                + "  domain_name character varying);");
        jdbcTemplate.execute("CREATE TABLE xml_content ("
                + "id IDENTITY NOT NULL, content CHARACTER VARYING NOT NULL);");
        //create other tables and insert data
    }

    @Test
    @Ignore
    public void testRegisterComponent() throws Exception {
        ComponentRegistryDbImpl register = componentRegistryBeanFactory.getNewComponentRegistry();
        ComponentDescription description = ComponentDescription.createNewDescription();

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
        assertNull(register.getMDProfile(desc.getId()));

        CMDComponentSpec component = register.getMDComponent(desc.getId());
        assertNotNull(component);
        assertEquals("Header id should be set from description id", description.getId(), component.getHeader().getID());
        assertEquals("Aap", component.getHeader().getName());
        assertEquals("Will not be overwritten", component.getHeader().getDescription());
    }
}
