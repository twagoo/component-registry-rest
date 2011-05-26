package clarin.cmdi.componentregistry.rest;

import java.io.File;

import org.junit.After;
import org.junit.Before;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.impl.filesystem.ComponentRegistryFactoryImpl;
import clarin.cmdi.componentregistry.impl.filesystem.ComponentRegistryTestCase;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FileImplComponentRegistryRestServiceTest extends ComponentRegistryRestServiceTest {

    @Override
    protected String getApplicationContextFile() {
	return "classpath:applicationContext-filesystem-impl.xml";
    }
    protected static ComponentRegistry testRegistry;
    private static File registryDir;

    @Before
    public void setUpTestRegistry() throws Exception {
	registryDir = ComponentRegistryTestCase.createTempRegistryDir();
	testRegistry = ComponentRegistryTestCase.getTestRegistry(registryDir);
    }

    @After
    public void deleteAndRecreateEmptyRegistry() {
	ComponentRegistryTestCase.cleanUpRegistryDir(registryDir);
    }

    @Override
    protected ComponentRegistry getTestRegistry() {
	return testRegistry;
    }

    @Override
    protected String expectedUserId(String principal) {
	return UserCredentials.getPrincipalNameMD5Hex(principal);
    }

    @Override
    protected ComponentRegistryFactory getRegistryFactory() {
	return ComponentRegistryFactoryImpl.getInstance();
    }
}
