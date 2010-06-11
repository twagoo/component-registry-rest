package clarin.cmdi.componentregistry.frontend;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileNode {

    private final File file;

    public FileNode(File file) {
        this.file = file;
    }

    public String getFileContent() {
        try {
            return FileUtils.readFileToString(getFile(), "UTF-8");
        } catch (IOException e) {
            return "Error could not read file: " + e;
        }
    }

    @Override
    public String toString() {
        return getFile().getName();
    }

    public File getFile() {
        return file;
    }
}
