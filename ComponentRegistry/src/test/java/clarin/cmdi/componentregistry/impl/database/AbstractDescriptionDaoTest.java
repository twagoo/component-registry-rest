package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

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
        Number newId = getDao().insertDescription(description, testComponent, true, null);
        assertNotNull(newId);
        assertNotNull(getDao().getById(newId));
    }

    @Test
    public void testGetPublicComponents() throws Exception {
        List<AbstractDescription> descriptions = getDao().getPublicDescriptions();
        assertNotNull(descriptions);
    }

    @Test
    public void testGetUserspaceDescriptions() throws Exception {
        List<AbstractDescription> descriptions = getDao().getUserspaceDescriptions(-1);
        assertEquals(0, descriptions.size());
    }

    @Test
    public void testDeleteDescription() throws Exception {
	ComponentDescription description = ComponentDescription.createNewDescription();
        description.setName("Aap");
        description.setDescription("MyDescription");
        String testComponent = RegistryTestHelper.getComponentTestContentString();

	int count = getDao().getPublicDescriptions().size();
	// insert
        getDao().insertDescription(description, testComponent, true, null);
	assertEquals(count+1, getDao().getPublicDescriptions().size());

	// delete
	getDao().setDeleted(description.getId());
	assertEquals(count, getDao().getPublicDescriptions().size());
    }
}
