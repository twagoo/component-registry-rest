package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.AbstractDescription;
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
	AbstractDescription description = createNewDescription();
	description.setName("Aap");
	description.setDescription("MyDescription");

	String testComponent = RegistryTestHelper.getComponentTestContentString();
	Number newId = getDao().insertDescription(description, testComponent, true, null);
	assertNotNull(newId);
	AbstractDescription descr = getDao().getById(newId);
	assertNotNull(descr);
	assertEquals("Aap", descr.getName());
	assertEquals("MyDescription", descr.getDescription());
	assertEquals(testComponent, getDao().getContent(description.getId()));
    }

    @Test
    public void testGetPublicComponents() throws Exception {
	List<AbstractDescription> descriptions = getDao().getPublicDescriptions();
	assertNotNull(descriptions);
    }

    @Test
    public void testGetUserspaceDescriptions() throws Exception {
	List<AbstractDescription> descriptions = getDao().
		getUserspaceDescriptions(-1);
	assertEquals(0, descriptions.size());
    }

    @Test
    public void testDeleteDescription() throws Exception {
	AbstractDescription description = createNewDescription();
	description.setName("Aap");
	description.setDescription("MyDescription");
	String testComponent = RegistryTestHelper.getComponentTestContentString();

	int count = getDao().getPublicDescriptions().size();
	// insert
	Number dbId = getDao().insertDescription(description, testComponent, true, null);
	assertEquals(count + 1, getDao().getPublicDescriptions().size());

	// delete
	getDao().setDeleted(dbId);
	assertEquals(count, getDao().getPublicDescriptions().size());
    }

    @Test
    public void testUpdateDescription() {
	AbstractDescription description = createNewDescription();
	description.setName("Aap");
	description.setDescription("MyDescription");

	String testComponent = RegistryTestHelper.getComponentTestContentString();
	Number newId = getDao().insertDescription(description, testComponent, true, null);

	// Change values
	description.setName("Noot");
	description.setDescription("AnotherDescription");
	// Update in db
	getDao().updateDescription(newId, description, null);
	description = getDao().getById(newId);
	// Test if new values are there
	assertNotNull(description);
	assertEquals("Noot", description.getName());
	assertEquals("AnotherDescription", description.getDescription());

	// Update content
	String testContent2 = "<test>Test content</test>";
	getDao().updateDescription(newId, null, testContent2);
	// Test if new content is there
	assertEquals(testContent2, getDao().getContent(description.getId()));

	// Update both
	description.setName("Mies");
	description.setDescription("YetAnotherDescription");
	String testContent3 = "<test>More test content</test>";

	// Update in db
	getDao().updateDescription(newId, description, testContent3);
	description = getDao().getById(newId);
	// Test if new values are there
	assertNotNull(description);
	assertEquals("Mies", description.getName());
	assertEquals("YetAnotherDescription", description.getDescription());
	// Test if new content is there
	assertEquals(testContent3, getDao().getContent(description.getId()));
    }

    protected abstract AbstractDescription createNewDescription();
}
