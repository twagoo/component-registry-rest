package clarin.cmdi.componentregistry.frontend;

import java.io.File;
import java.io.Serializable;

import clarin.cmdi.componentregistry.ResourceConfig;

public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String text;
    private String name;

    private FileNode fileNode;

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        if (fileNode != null) {
            if (fileNode.getFile().isFile()) {
                text = fileNode.getFileContent();
            } else {
                String dir = fileNode.getFile().getParentFile().getName();
                if (ResourceConfig.DELETED_DIR_NAME.equals(dir)) {
                    text = "Press undelete button to put this item back in the registry";
                } else if (ResourceConfig.COMPONENTS_DIR_NAME.equals(dir) || ResourceConfig.PROFILES_DIR_NAME.equals(dir)) {
                    text = "Press delete button to delete this item";
                } else {
                    text = "";
                }
            }
        }
        return text;
    }

    /**
     * @return the userDir when this file is a directory and located inside the users part of the registry.  
     * Otherwise returns null
     */
    public String getUserDir() {
        String result = null;
        File file = fileNode.getFile();
        if (file.isDirectory()) {
            File parentDir = file.getParentFile();
            if (ResourceConfig.DELETED_DIR_NAME.equals(parentDir.getName())) {
                result = parentDir.getParentFile().getParentFile().getName(); //e.g. users/user0/components/deleted/c_12324
            } else if (ResourceConfig.COMPONENTS_DIR_NAME.equals(parentDir.getName())
                    || ResourceConfig.PROFILES_DIR_NAME.equals(parentDir.getName())) {
                result = parentDir.getParentFile().getName(); //e.g. users/user0/components/c_12324
            }
        }
        if (result != null && !result.startsWith("user")) {
            result = null;
        }
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (fileNode != null) {
            name = fileNode.toString();
        }
        return name;
    }

    public void setFileNode(FileNode fileNode) {
        this.fileNode = fileNode;

    }

}
