package clarin.cmdi.componentregistry.impl.database;

import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase.*;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ProfileDescriptionDaoTest extends AbstractDescriptionDaoTest {

    @Autowired
    ProfileDescriptionDao profileDescriptionDao;

    @Before
    public void init() {
	resetDatabase(jdbcTemplate);
	createTableProfileDescription(jdbcTemplate);
	createTableXmlContent(jdbcTemplate);
	createTableRegistryUser(jdbcTemplate);
    }

    @Override
    protected AbstractDescriptionDao getDao() {
	return profileDescriptionDao;
    }

    @Override
    protected AbstractDescription createNewDescription() {
	return ProfileDescription.createNewDescription();
    }
}
