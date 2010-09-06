package clarin.cmdi.componentregistry.frontend;

import java.io.File;
import java.io.Serializable;

import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.ResourceConfig;

public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String text;
    private String name;
    private boolean forceUpdate = false;

    private FileNode fileNode;

    private boolean deletable = false;
    private boolean undeletable = false;

    private boolean editable = false;

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
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

    public FileNode getFileNode() {
        return fileNode;
    }

    public void setFileNode(FileNode fileNode) {
        this.fileNode = fileNode;
        setUndeletable(false);
        setDeletable(false);
        setEditable(false);
        if (fileNode != null) {
            File file = fileNode.getFile();
            if (file.isFile()) {
                setText(fileNode.getFileContent());
                setEditable(!file.getParentFile().equals(Configuration.getInstance().getRegistryRoot())); //file in root are not editable like:userMapping.xml
            } else {
                String dir = fileNode.getFile().getParentFile().getName();
                if (ResourceConfig.DELETED_DIR_NAME.equals(dir)) {
                    setText("Press undelete button to put this item back in the registry");
                    setUndeletable(true);
                } else if ((ResourceConfig.COMPONENTS_DIR_NAME.equals(dir) || ResourceConfig.PROFILES_DIR_NAME.equals(dir))
                        && !fileNode.isDeleted()) {
                    setText("Press delete button to delete this item");
                    setDeletable(true);
                } else {
                    setText("");
                }
            }
        }

    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setUndeletable(boolean undeletable) {
        this.undeletable = undeletable;
    }

    public boolean isUndeletable() {
        return undeletable;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isComponent() {
        return getName().startsWith("c_");
    }

    public boolean isInUserWorkSpace() {
        File file = fileNode.getFile();
        return file.getAbsolutePath().startsWith(
                new File(Configuration.getInstance().getRegistryRoot(), ResourceConfig.USERS_DIR_NAME).getAbsolutePath());
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }
}
