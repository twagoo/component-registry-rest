package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
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
	String regDate = AbstractDescription.createNewDate();

	AbstractDescription description = createNewDescription();
	description.setName("MyComponent");
	description.setDescription("MyDescription");
	description.setCreatorName("Aap");
	description.setGroupName("MyGroup");
	description.setDomainName("MyDomain");
	description.setHref("http://MyHref");

	description.setRegistrationDate(regDate);

	String testComponent = RegistryTestHelper.getComponentTestContentString();
	Number newId = getDao().insertDescription(description, testComponent, true, null);
	assertNotNull(newId);
	AbstractDescription descr = getDao().getById(newId);
	assertNotNull(descr);
	assertEquals("MyComponent", descr.getName());
	assertEquals("MyDescription", descr.getDescription());
	assertEquals("Aap", descr.getCreatorName());
	assertEquals("MyGroup", descr.getGroupName());
	assertEquals("MyDomain", descr.getDomainName());
	assertEquals("http://MyHref", descr.getHref());
	assertEquals(AbstractDescription.getDate(regDate), AbstractDescription.getDate(descr.getRegistrationDate()));
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
	description.setCreatorName("Aap");
	description.setGroupName("MyGroup");
	description.setDomainName("MyDomain");
	description.setHref("http://MyHref");
	
	String testComponent = RegistryTestHelper.getComponentTestContentString();
	Number newId = getDao().insertDescription(description, testComponent, true, null);

	// Change values
	description.setName("Noot");
	description.setDescription("AnotherDescription");
		description.setCreatorName("AnotherAap");
	description.setGroupName("AnotherGroup");
	description.setDomainName("AnotherDomain");
	description.setHref("http://AnotherHref");
	// Update in db
	getDao().updateDescription(newId, description, null);
	description = getDao().getById(newId);
	// Test if new values are there
	assertNotNull(description);
	assertEquals("Noot", description.getName());
	assertEquals("AnotherDescription", description.getDescription());
	assertEquals("AnotherAap", description.getCreatorName());
	assertEquals("AnotherGroup", description.getGroupName());
	assertEquals("AnotherDomain", description.getDomainName());
	assertEquals("http://AnotherHref", description.getHref());

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
