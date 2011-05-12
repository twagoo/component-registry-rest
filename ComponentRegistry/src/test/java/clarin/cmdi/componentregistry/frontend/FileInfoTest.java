package clarin.cmdi.componentregistry.frontend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import clarin.cmdi.componentregistry.impl.filesystem.ComponentRegistryTestCase;
import clarin.cmdi.componentregistry.impl.filesystem.FileSystemConfiguration;

public class FileInfoTest {

    private File rootDir;

    @Test
    public void testGetUserDir() throws Exception {
        rootDir = ComponentRegistryTestCase.createTempRegistryDir();
        FileSystemConfiguration.getInstance().setRegistryRoot(rootDir);
        FileInfo info = new FileInfo();
        info.setFileNode(new FileNode(createFile("users/user1/components/c_123/description.xml"), false));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertTrue(info.isEditable());
        assertFalse(info.getFileNode().isDeleted());
        assertTrue(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("users/user1/components/deleted/c_123/description.xml"), true));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertTrue(info.isEditable());
        assertTrue(info.getFileNode().isDeleted());
        assertTrue(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("components/c_123/description.xml"), false));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertTrue(info.isEditable());
        assertFalse(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("components/c_123/"), false));
        assertTrue(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertFalse(info.isEditable());
        assertFalse(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("components/deleted/c_123/description.xml"), true));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertTrue(info.isEditable());
        assertFalse(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("components/deleted/c_123/"), true));
        assertFalse(info.isDeletable());
        assertTrue(info.isUndeletable());
        assertFalse(info.isEditable());
        assertFalse(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("components/deleted/"), true));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertFalse(info.isEditable());
        assertFalse(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("/"), false));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertFalse(info.isEditable());
        assertFalse(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("/userMapping.xml"), false));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertFalse(info.isEditable());
        assertFalse(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("users/"), false));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertFalse(info.isEditable());
        assertTrue(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("users/user1/components/c_123/"), false));
        assertTrue(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertFalse(info.isEditable());
        assertTrue(info.isInUserWorkSpace());
        info.setFileNode(new FileNode(createFile("users/user1/components/deleted/c_123/"), true));
        assertFalse(info.isDeletable());
        assertTrue(info.isUndeletable());
        assertFalse(info.isEditable());
        assertTrue(info.isInUserWorkSpace());
    }

    private File createFile(String fileName) throws IOException {
        File file = new File(rootDir, fileName);
        if (fileName.endsWith("/")) {
            file.mkdirs();
        } else {
            FileUtils.writeStringToFile(file, "");
        }
        return file;
    }

    @After
    public void cleanup() throws IOException {
        if (rootDir != null && rootDir.exists()) {
            FileUtils.deleteDirectory(rootDir);
        }
    }

}
