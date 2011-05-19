package clarin.cmdi.componentregistry.frontend;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;

import clarin.cmdi.componentregistry.impl.filesystem.FileSystemConfiguration;
import clarin.cmdi.componentregistry.impl.filesystem.ResourceConfig;

public class FileNode implements Serializable, DisplayNode {

    private static final long serialVersionUID = 1L;
    private final File file;
    private final boolean deleted;

    public FileNode(File file, boolean deleted) {
        this.file = file;
        this.deleted = deleted;
    }

    @Override
    public String getContent() {
        try {
            return FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            return "Error could not read file: " + e;
        }
    }

    @Override
    public boolean hasContent() {
        return file.isFile();
    }

    @Override
    public String toString() {
        return file.getName();
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public boolean isEditable() {
        return hasContent() && !file.getParentFile().equals(FileSystemConfiguration.getInstance().getRegistryRoot());
    }

    @Override
    public boolean isUserNode() {
        return file.getAbsolutePath().startsWith(
                new File(FileSystemConfiguration.getInstance().getRegistryRoot(), ResourceConfig.USERS_DIR_NAME).getAbsolutePath());
    }

    @Override
    public String getId() {
        return file.getParentFile().getName();
    }

    public File getFile() {
        return file;
    }
    
}
