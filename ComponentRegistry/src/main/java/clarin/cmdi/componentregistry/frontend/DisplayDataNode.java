package clarin.cmdi.componentregistry.frontend;

import java.io.Serializable;

import clarin.cmdi.componentregistry.model.AbstractDescription;

public class DisplayDataNode implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final boolean isDeleted;
    private AbstractDescription desc;
    private final boolean isPublic;

    public DisplayDataNode(String name, boolean isDeleted) {
        this(name, isDeleted, null, false);
    }

    public DisplayDataNode(String name, boolean isDeleted, AbstractDescription desc, boolean isPublic) {
        this.name = name;
        this.isDeleted = isDeleted;
        this.desc = desc;
        this.isPublic = isPublic;
    }

    /**
     * Can be null for non leaves.
     * @return
     */
    public AbstractDescription getDescription() {
        return desc;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isPublic() {
        return isPublic;
    }

}
