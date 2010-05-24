package clarin.cmdi.componentregistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Test;

import clarin.cmdi.componentregistry.model.UserMapping;
import clarin.cmdi.componentregistry.model.UserMapping.User;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;

public class ComponentRegistryFactoryTest {

    private File registryDir;

    @Test
    public void testGetUserDir() throws Exception {
        registryDir = ComponentRegistryImplTest.createTempRegistryDir();
        Configuration.getInstance().setRegistryRoot(registryDir);
        Configuration.getInstance().init();

        ComponentRegistryFactory instance = ComponentRegistryFactory.getInstance();
        assertFalse(Configuration.getInstance().getUserDirMappingFile().exists());
        ComponentRegistry componentRegistry = instance.getComponentRegistry(true, new DummyPrincipal("noot"));
        
        assertTrue(Configuration.getInstance().getUserDirMappingFile().exists());
        UserMapping mapping = getUserMapping();
        assertEquals(1, mapping.getUsers().size());
        
        ComponentRegistry componentRegistry2 = instance.getComponentRegistry(true, new DummyPrincipal("noot"));
        assertSame(componentRegistry, componentRegistry2);
        mapping = getUserMapping();
        assertEquals(1, mapping.getUsers().size());

        ComponentRegistry componentRegistry3 = instance.getComponentRegistry(true, new DummyPrincipal("aap"));
        assertNotSame(componentRegistry, componentRegistry3);
        mapping = getUserMapping();
        assertEquals(2, mapping.getUsers().size());
        User user = mapping.getUsers().get(0);
        assertEquals("user0", user.getUserDir());
        assertEquals("noot", user.getName());
        user = mapping.getUsers().get(1);
        assertEquals("user1", user.getUserDir());
        assertEquals("aap", user.getName());

    }

    private UserMapping getUserMapping() throws JAXBException, FileNotFoundException {
        return MDMarshaller.unmarshal(UserMapping.class, new FileInputStream(Configuration.getInstance().getUserDirMappingFile()), null);
    }

    @After
    public void cleanup() {
        ComponentRegistryImplTest.cleanUpRegistryDir(registryDir);
    }
}
