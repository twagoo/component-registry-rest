package clarin.cmdi.componentregistry;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base test with common facilities. Starts a test application context and enables transaction handling
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
	"classpath:spring-config/applicationContext.xml",
	"classpath:spring-config/datasource-hsqldb.xml" })
@TransactionConfiguration(defaultRollback = true)
@Transactional
public abstract class BaseUnitTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

}
