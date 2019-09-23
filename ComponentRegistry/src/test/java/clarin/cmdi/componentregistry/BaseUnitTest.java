package clarin.cmdi.componentregistry;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;

/**
 * Base test with common facilities. Starts a test application context and
 * enables transaction handling
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-config/applicationContext.xml",
		"classpath:spring-config/test-applicationContext-fragment.xml" })
@Transactional
public abstract class BaseUnitTest extends XMLTestCase {

	@Autowired
	protected MDMarshaller marshaller;

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Before
	public void setupDatabase() {
		XMLUnit.setIgnoreAttributeOrder(true);
		ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
	}

}
