package clarin.cmdi.componentregistry;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import clarin.cmdi.componentregistry.model.ProfileDescription;

public class ComponentRegistryImplTest {

    @Test
    public void testRegisterMDProfile() {
        ComponentRegistry register = getTestRegistry(createTempRegistryDir());
        String profileContent = "<profile></profile>";
        ProfileDescription description = new ProfileDescription();
        description.setName("Aap");
        description.setId("Aap" + System.currentTimeMillis());

        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(0, register.getProfileDescriptions().size());
        register.registerMDProfile(description, profileContent);
        assertEquals(0, register.getComponentDescriptions().size());
        assertEquals(1, register.getProfileDescriptions().size());
        //TODO Patrick test with empty name/id/description etc..
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
}
