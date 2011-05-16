package clarin.cmdi.componentregistry.impl.database;

import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase.*;

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
    }

    @Override
    protected AbstractDescriptionDao getDao() {
        return profileDescriptionDao;
    }
}
