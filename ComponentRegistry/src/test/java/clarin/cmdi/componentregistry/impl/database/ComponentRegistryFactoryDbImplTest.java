package clarin.cmdi.componentregistry.impl.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext-database-impl.xml"})
public class ComponentRegistryFactoryDbImplTest {

    @Autowired
    ComponentRegistryFactoryDbImpl componentRegistryFactory;
}
