package clarin.cmdi.componentregistry.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class CMDItemInfoTest {

    @Test
    public void testGetUserDir() throws Exception {
        CMDItemInfo info = new CMDItemInfo();
        //info.setDataNode(new FileNode(createFile("users/user1/components/c_123/description.xml"), false));
        info.setDataNode(new DisplayDataNode("test", false, createDescription(), false));
        assertTrue(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertTrue(info.isEditable());
        assertFalse(info.getDataNode().isDeleted());
        assertTrue(info.isInUserWorkSpace());
        //info.setDataNode(new FileNode(createFile("users/user1/components/deleted/c_123/description.xml"), true));
        info.setDataNode(new DisplayDataNode("test", true, createDescription(), false));
        assertFalse(info.isDeletable());
        assertTrue(info.isUndeletable());
        assertTrue(info.isEditable());
        assertTrue(info.getDataNode().isDeleted());
        assertTrue(info.isInUserWorkSpace());
        //info.setDataNode(new FileNode(createFile("components/c_123/description.xml"), false));
        info.setDataNode(new DisplayDataNode("test", false, createDescription(), true));
        assertTrue(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertTrue(info.isEditable());
        assertFalse(info.isInUserWorkSpace());
        //info.setDataNode(new FileNode(createFile("components/c_123/"), false));
        info.setDataNode(new DisplayDataNode("test", false));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertFalse(info.isEditable());
        //info.setDataNode(new FileNode(createFile("components/deleted/c_123/description.xml"), true));
        info.setDataNode(new DisplayDataNode("test", true, createDescription(), true));
        assertFalse(info.isDeletable());
        assertTrue(info.isUndeletable());
        assertTrue(info.isEditable());
        assertFalse(info.isInUserWorkSpace());
        assertTrue(info.getDescription().startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<profileDescription"));
        //info.setDataNode(new FileNode(createFile("components/deleted/c_123/"), true));
        info.setDataNode(new DisplayDataNode("test", true));
        assertFalse(info.isDeletable());
        assertFalse(info.isUndeletable());
        assertFalse(info.isEditable());
        assertEquals("", info.getDescription());
    }

    private AbstractDescription createDescription() {
        return ProfileDescription.createNewDescription();
    }

}
