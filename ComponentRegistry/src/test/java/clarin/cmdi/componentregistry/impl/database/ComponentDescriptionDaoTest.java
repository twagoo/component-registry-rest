package clarin.cmdi.componentregistry.impl.database;

import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase.*;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class ComponentDescriptionDaoTest extends AbstractDescriptionDaoTest {

    @Autowired
    IComponentDescriptionDao componentDescriptionDao;

    @Before
    public void init() {
	resetDatabase(jdbcTemplate);
	createTableComponentDescription(jdbcTemplate);
	createTableXmlContent(jdbcTemplate);
	createTableRegistryUser(jdbcTemplate);
	createTableComments(jdbcTemplate);
    }

    @Override
    protected IAbstractDescriptionDao getDao() {
	return componentDescriptionDao;
    }

    @Override
    protected AbstractDescription createNewDescription() {
	return ComponentDescription.createNewDescription();
    }

    @Override
    protected String getContentString() {
	return RegistryTestHelper.getComponentTestContentString();
    }
}
