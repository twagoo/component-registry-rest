package clarin.cmdi.componentregistry.frontend;

import java.io.Serializable;

public class DisplayDataNode implements Serializable, DisplayNode {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final boolean isDeleted;

    public DisplayDataNode(String name, boolean isDeleted) {
        this.name = name;
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getContent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasContent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public boolean isEditable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUserNode() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }
}
