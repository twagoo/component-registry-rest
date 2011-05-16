package clarin.cmdi.componentregistry.impl.database;

import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryDatabase.*;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

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
}
