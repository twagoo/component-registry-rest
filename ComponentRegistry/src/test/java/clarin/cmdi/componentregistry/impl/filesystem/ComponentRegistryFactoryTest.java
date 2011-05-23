package clarin.cmdi.componentregistry.impl.filesystem;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserCredentials;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import clarin.cmdi.componentregistry.model.UserMapping;
import clarin.cmdi.componentregistry.model.UserMapping.User;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext-filesystem-impl.xml"})
public class ComponentRegistryFactoryTest {

    private File registryDir;

    @Test
    public void testGetOrCreateUserDir() throws Exception {
        ComponentRegistryFactoryImpl instance = ComponentRegistryFactoryImpl.getInstance();
        assertFalse(FileSystemConfiguration.getInstance().getUserDirMappingFile().exists());
        ComponentRegistry componentRegistry = instance.getComponentRegistry(true, createUserCredentials("noot", "Mr.Noot"));

        assertTrue(FileSystemConfiguration.getInstance().getUserDirMappingFile().exists());
        UserMapping mapping = getUserMapping();
        assertEquals(1, mapping.getUsers().size());

        ComponentRegistry componentRegistry2 = instance.getComponentRegistry(true, createUserCredentials("noot", "Mr.Noot"));
        assertSame(componentRegistry, componentRegistry2);
        mapping = getUserMapping();
        assertEquals(1, mapping.getUsers().size());

        ComponentRegistry componentRegistry3 = instance.getComponentRegistry(true, createUserCredentials("aap", "Mr.Aap"));
        assertNotSame(componentRegistry, componentRegistry3);
        mapping = getUserMapping();
        assertEquals(2, mapping.getUsers().size());
        User user = mapping.getUsers().get(0);
        assertEquals("user0", user.getUserDir());
        assertEquals("noot", user.getPrincipalName());
        assertEquals("Mr.Noot", user.getName());
        user = mapping.getUsers().get(1);
        assertEquals("user1", user.getUserDir());
        assertEquals("aap", user.getPrincipalName());
        assertEquals("Mr.Aap", user.getName());
    }

    @Test
    public void testGetOtherUserComponentRegistry() throws Exception {
        ComponentRegistryFactoryImpl instance = ComponentRegistryFactoryImpl.getInstance();
        ComponentRegistry reg1 = instance.getComponentRegistry(true, createUserCredentials("noot", "Mr.Noot"));
        assertNotNull(reg1);
        ComponentRegistry reg2 = instance.getComponentRegistry(true, createUserCredentials("aap", "Mr.Aap"));
        assertNotNull(reg2);
        assertNotSame(reg1, reg2);
        String userDir = instance.getOrCreateUserDir("aap", "Mr.Aap");
        String principalNameMD5 = DigestUtils.md5Hex("aap");
        assertEquals("user1", userDir);
//        Principal admin = new DummyPrincipal("noot");
        try {
            instance.getOtherUserComponentRegistry(DummyPrincipal.DUMMY_PRINCIPAL, principalNameMD5);
            fail("Should have failed because 'noot' is not an admin user");
        } catch (IllegalArgumentException e) {
        }
//        Configuration.getInstance().setAdminUsers(Collections.singleton("noot"));
        ComponentRegistry reg = instance.getOtherUserComponentRegistry(DummyPrincipal.DUMMY_ADMIN_PRINCIPAL, principalNameMD5);
        assertNotNull(reg);
        assertSame(reg2, reg);
        reg = instance.getOtherUserComponentRegistry(DummyPrincipal.DUMMY_ADMIN_PRINCIPAL, null);
        assertNull(reg);
    }

    private UserCredentials createUserCredentials(String principalName, final String displayName) {
        return new UserCredentials(new DummyPrincipal(principalName)) {
            @Override
            public String getDisplayName() {
                return displayName;
            };
        };
    }

    private UserMapping getUserMapping() throws JAXBException, FileNotFoundException {
        return MDMarshaller.unmarshal(UserMapping.class, new FileInputStream(FileSystemConfiguration.getInstance().getUserDirMappingFile()), null);
    }

    @Before
    public void startClean() {
        registryDir = ComponentRegistryTestCase.createTempRegistryDir();
        FileSystemConfiguration.getInstance().setRegistryRoot(registryDir);
        FileSystemConfiguration.getInstance().init();
        ComponentRegistryFactoryImpl.getInstance().reset();
    }

    @After
    public void cleanup() {
        ComponentRegistryTestCase.cleanUpRegistryDir(registryDir);
    }
}
