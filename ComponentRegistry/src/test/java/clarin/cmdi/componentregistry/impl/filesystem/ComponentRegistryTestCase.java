package clarin.cmdi.componentregistry.impl.filesystem;

import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.impl.filesystem.ComponentRegistryFactoryImpl;
import clarin.cmdi.componentregistry.impl.filesystem.ComponentRegistryImpl;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;

import clarin.cmdi.componentregistry.rest.DummyPrincipal;

public abstract class ComponentRegistryTestCase {

    protected File tmpRegistryDir;
    protected final static UserCredentials USER_CREDS = DummyPrincipal.DUMMY_CREDENTIALS;
    protected static final Principal PRINCIPAL_ADMIN = new DummyPrincipal("admin");

    protected File getRegistryDir() {
        if (tmpRegistryDir == null)
            tmpRegistryDir = createTempRegistryDir();
        return tmpRegistryDir;
    }

    @After
    public void cleanupRegistryDir() {
        ComponentRegistryFactoryImpl.getInstance().reset();
        cleanUpRegistryDir(tmpRegistryDir);
        tmpRegistryDir = null;
    }

    protected ComponentRegistryImpl getTestRegistry() {
        return getTestRegistry(getRegistryDir());
    }

    public static ComponentRegistryImpl getTestRegistry(File registryRoot) {
        Configuration config = Configuration.getInstance();
        config.setRegistryRoot(registryRoot);
        Set<String> adminUsers = new HashSet<String>();
        adminUsers.add(PRINCIPAL_ADMIN.getName());
        config.setAdminUsers(adminUsers);
        config.init();
        ComponentRegistryFactoryImpl.getInstance().reset();
        ComponentRegistryImpl register = (ComponentRegistryImpl) ComponentRegistryFactoryImpl.getInstance().getPublicRegistry();
        register.setResourceConfig(config.getPublicResourceConfig()); //LOADS cache again but is necessary for tests normally we wouldn't have this
        return register;
    }

    public static File createTempRegistryDir() {
        final String baseTempPath = System.getProperty("java.io.tmpdir");
        File tempDir = new File(baseTempPath + File.separator + "testRegistry_" + System.currentTimeMillis());
        tempDir.mkdir();
        tempDir.deleteOnExit();
        return tempDir;
    }

    public static void cleanUpRegistryDir(File registryDir) {
        if (registryDir != null && registryDir.exists()) {
            assertTrue(FileUtils.deleteQuietly(registryDir));
        }
    }

}
