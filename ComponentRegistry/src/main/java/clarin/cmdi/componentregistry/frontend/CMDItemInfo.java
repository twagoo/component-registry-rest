package clarin.cmdi.componentregistry.frontend;

import java.io.Serializable;

import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.model.AbstractDescription;

public class CMDItemInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String description;
    private String content;
    private String name;
    private boolean forceUpdate = false;

    private DisplayDataNode displayNode;

    private boolean deletable = false;
    private boolean undeletable = false;

    private boolean editable = false;

    public void setDescription(String descriptionText) {
        this.description = descriptionText;
    }

    public String getDescription() {
        return description;
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

    public DisplayDataNode getDataNode() {
        return displayNode;
    }

    public void setDataNode(DisplayDataNode dataNode) {
        this.displayNode = dataNode;
        setUndeletable(false);
        setDeletable(false);
        setEditable(false);
        setDescription("");
        setContent("");
        if (dataNode != null) {
            AbstractDescription desc = dataNode.getDescription();
            if (desc != null) {
                String content = MDMarshaller.marshalToString(desc);
                setDescription(content);
                setEditable(true);
                if (dataNode.isDeleted()) {
                    setUndeletable(true);
                } else {
                    setDeletable(true);
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


    public boolean isInUserWorkSpace() {
        return !displayNode.isPublic();
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
