package clarin.cmdi.componentregistry.impl.database;

import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase.*;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class ProfileDescriptionDaoTest extends AbstractDescriptionDaoTest {

    @Autowired
    IProfileDescriptionDAO profileDescriptionDao;

    @Before
    public void init() {
	resetDatabase(jdbcTemplate);
	createTableProfileDescription(jdbcTemplate);
	createTableXmlContent(jdbcTemplate);
	createTableRegistryUser(jdbcTemplate);
	createTableComments(jdbcTemplate);
    }

    @Override
    protected IAbstractDescriptionDao getDao() {
	return profileDescriptionDao;
    }

    @Override
    protected AbstractDescription createNewDescription() {
	return ProfileDescription.createNewDescription();
    }

    @Override
    protected String getContentString() {
	return RegistryTestHelper.getProfileTestContentString();
    }

}
