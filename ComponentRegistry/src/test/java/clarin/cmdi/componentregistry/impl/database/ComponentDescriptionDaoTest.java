package clarin.cmdi.componentregistry.impl.database;

import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase.*;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentDescriptionDaoTest extends AbstractDescriptionDaoTest {

    @Autowired
    ComponentDescriptionDao componentDescriptionDao;

    @Before
    public void init() {
	resetDatabase(jdbcTemplate);
	createTableComponentDescription(jdbcTemplate);
	createTableXmlContent(jdbcTemplate);
	createTableRegistryUser(jdbcTemplate);
    }

    @Override
    protected AbstractDescriptionDao getDao() {
	return componentDescriptionDao;
    }

    @Override
    protected AbstractDescription createNewDescription() {
	return ComponentDescription.createNewDescription();
    }

    @Test
    public void testTransaction() {
	// Reset database and only create Content table so that inserting a description will fail
	resetDatabase(jdbcTemplate);
	createTableXmlContent(jdbcTemplate);

	// Create description and component
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

	// Try inserting description, which should fail because there is not ComponentDescription table
	try {
	    getDao().insertDescription(description, testComponent, true, null);
	    fail("No exception thrown for missing table");
	} catch (DataAccessException ex) {
	}

	// Check whether content was not inserted
	final String select = "SELECT count(*) FROM " + ComponentDescriptionDao.TABLE_XML_CONTENT;
	int count = jdbcTemplate.queryForInt(select);
	assertEquals(0, count);
    }
}
