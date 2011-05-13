package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

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
public abstract class AbstractDescriptionDaoTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected abstract AbstractDescriptionDao getDao();

    @Test
    public void testInjection() {
        assertNotNull(jdbcTemplate);
        assertNotNull(getDao());
    }

    @Test
    public void testInsertComponent() throws Exception {
        ComponentDescription description = ComponentDescription.createNewDescription();
        description.setName("Aap");
        description.setDescription("MyDescription");

        String testComponent = RegistryTestHelper.getComponentTestContentString();
        getDao().insertComponent(description, testComponent);
    }
}
