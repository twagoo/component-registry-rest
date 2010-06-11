package clarin.cmdi.componentregistry.frontend;

import java.io.Serializable;

public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String text;
    private String name;

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
        return name;
    }

}
