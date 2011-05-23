package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.impl.filesystem.ComponentRegistryTestCase;
import java.io.File;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FileImplConcurrentRestServiceTest extends ConcurrentRestServiceTest {

    @Before
    public void setUpTestRegistry() throws Exception {
	registryDir = ComponentRegistryTestCase.createTempRegistryDir();
	testRegistry = ComponentRegistryTestCase.getTestRegistry(registryDir);
    }

    @After
    public void deleteAndRecreateEmptyRegistry() {
	ComponentRegistryTestCase.cleanUpRegistryDir(registryDir);
    }
    
    protected static ComponentRegistry testRegistry;
    private static File registryDir;

    @Override
    protected String getApplicationContextFile() {
	return "classpath:applicationContext-filesystem-impl.xml";
    }
}
