package clarin.cmdi.componentregistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.rest.ComponentRegistryRestServiceTest;

public class ComponentRegistryImplTest {

    private File tmpRegistryDir;

    @Test
    public void testRegisterMDProfile() throws JAXBException {
        ComponentRegistry register = getTestRegistry(getRegistryDir());
        ProfileDescription description = new ProfileDescription();
        description.setName("Aap");
        description.setId("Aap" + System.currentTimeMillis());

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());
        register.registerMDProfile(description, ComponentRegistryRestServiceTest.getTestProfile());
        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(1, register.getProfileDescriptions().size());
        ProfileDescription desc = register.getProfileDescriptions().get(0);
        assertNull(register.getMDComponent(desc.getId()));
        assertNotNull(register.getMDProfile(desc.getId()));
    }

    @Test
    public void testCache() throws JAXBException {
        ComponentRegistryImpl register = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        Configuration config = new Configuration();
        config.setRegistryRoot(getRegistryDir());
        config.init();
        register.setConfiguration(config);

        ProfileDescription description = new ProfileDescription();
        description.setName("Aap");
        String id = "Aap" + System.currentTimeMillis();
        description.setId(id);

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());
        register.registerMDProfile(description, ComponentRegistryRestServiceTest.getTestProfile());
        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(1, register.getProfileDescriptions().size());
        assertNull(register.getMDComponent(id));
        assertNotNull(register.getMDProfile(id));
        
        register.setConfiguration(config); //triggers cache
        assertEquals(1, register.getProfileDescriptions().size());
        assertEquals(0, register.getComponentDescriptions().size());
        assertNotNull(register.getMDProfile(id));      
    }

    @Test
    public void testCacheCorruptFile() throws JAXBException {
        ComponentRegistryImpl register = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        Configuration config = new Configuration();
        config.setRegistryRoot(getRegistryDir());
        config.init();
        register.setConfiguration(config);

        ProfileDescription description = new ProfileDescription();
        description.setName("Aap");
        String id = "Aap" + System.currentTimeMillis();
        description.setId(id);

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());
        register.registerMDProfile(description, ComponentRegistryRestServiceTest.getTestProfile());
        description = new ProfileDescription();
        description.setName("Aap2");
        String id2 = "Aap2" + System.currentTimeMillis();
        description.setId(id2);
        register.registerMDProfile(description, ComponentRegistryRestServiceTest.getTestProfile());

        
        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(2, register.getProfileDescriptions().size());
        assertNull(register.getMDComponent(id));
        assertNotNull(register.getMDProfile(id));
        assertNotNull(register.getMDProfile(id2));
        
        File profileFile = new File(config.getProfileDir(), id+File.separator+id+".xml");
        assertTrue(profileFile.exists());
        assertTrue(profileFile.delete()); //profile file deleted so file system corrupt file should no longer be loaded in cache
        assertFalse(profileFile.exists());
        
        register.setConfiguration(config); //triggers cache
        assertEquals(1, register.getProfileDescriptions().size());
        assertEquals(0, register.getComponentDescriptions().size());
        assertNull(register.getMDProfile(id));
        assertNotNull(register.getMDProfile(id2));
    }
    
    private File getRegistryDir() {
        if (tmpRegistryDir == null)
            tmpRegistryDir = createTempRegistryDir();
        return tmpRegistryDir;
    }

    @After
    public void cleanupRegistryDir() {
        cleanUpRegistryDir(tmpRegistryDir);
        tmpRegistryDir = null;
    }

    public static ComponentRegistry getTestRegistry(File registryRoot) {
        ComponentRegistryImpl register = (ComponentRegistryImpl) ComponentRegistryImpl.getInstance();
        Configuration config = new Configuration();
        config.setRegistryRoot(registryRoot);
        config.init();
        register.setConfiguration(config);
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
