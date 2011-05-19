package clarin.cmdi.componentregistry.frontend;

import java.io.Serializable;

public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String text;
    private String name;
    private boolean forceUpdate = false;

    private DisplayNode displayNode;

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
        if (displayNode != null) {
            name = displayNode.toString();
        }
        return name;
    }

    public DisplayNode getDisplayNode() {
        return displayNode;
    }

    public void setDisplayNode(DisplayNode displayNode) {
        this.displayNode = displayNode;
        setUndeletable(false);
        setDeletable(false);
        setEditable(false);
        if (displayNode != null) {
            if (displayNode.hasContent()) {
                setText(displayNode.getContent());
                setEditable(displayNode.isEditable()); //file in root are not editable like:userMapping.xml
            } else {
                //TODO PD have to test this
                if (displayNode.isDeleted() && !displayNode.isEditable() && (displayNode.toString().startsWith("c_") || displayNode.toString().startsWith("p_"))) {
                    setText("Press undelete button to put this item back in the registry");
                    setUndeletable(true);
                } else if (!displayNode.isDeleted() && !displayNode.isEditable() && (displayNode.toString().startsWith("c_") || displayNode.toString().startsWith("p_"))) {
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
        return displayNode.isUserNode();
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

}
