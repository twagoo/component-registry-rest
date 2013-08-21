package clarin.cmdi.componentregistry.impl.database;

import java.util.Date;

import static org.junit.Assert.*;

import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TransactionConfiguration;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDescriptionDao;

/**
 * Test transactionality on comments
 * @author george.georgovassilis@mpi.nl
 * 
 */
@TransactionConfiguration(defaultRollback = true)
public class TestTransactionRollbacks extends BaseUnitTest {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ComponentDescriptionDao componentDescriptionDao;

    @Test
    public void test01() {
	ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
    }

    @Test(expected = DataAccessException.class)
    public void test02() {
	ComponentDescription cd1 = componentDescriptionDao.getByCmdId("c1");
	assertNull(cd1);

	ComponentDescription cd = new ComponentDescription();
	cd.setCreatorName("user 123");
	cd.setDescription("description");
	cd.setDomainName("domain name");
	cd.setGroupName("group name");
	cd.setHref("href");
	cd.setName("name");
	cd.setRegistrationDate(DateFormatUtils.format(new Date(),
		DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()));
	cd.setUserId("123");
	cd.setId("c1");
	componentDescriptionDao.insertDescription(cd, "Content 1", false, 123);

	cd1 = componentDescriptionDao.getByCmdId("c1");
	assertNotNull(cd1);
	componentDescriptionDao.insertDescription(cd, "Content 1", false, 123);
	fail("Expected second insert to fail");
    }

    @Test
    public void test03() {
	ComponentDescription cd1 = componentDescriptionDao.getByCmdId("c1");
	assertNull(cd1);
    }

}
