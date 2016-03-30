package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.RegistrySpace;
import java.io.Serializable;

import clarin.cmdi.componentregistry.model.BaseDescription;

public class DisplayDataNode implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final boolean isDeleted;
    private BaseDescription desc;
    private final RegistrySpace space;

    public DisplayDataNode(String name, boolean isDeleted) {
	// TODO: what is sensible default status?
	this(name, isDeleted, null, RegistrySpace.PRIVATE);
    }

    public DisplayDataNode(String name, boolean isDeleted, BaseDescription desc, RegistrySpace space) {
	this.name = name;
	this.isDeleted = isDeleted;
	this.desc = desc;
	this.space = space;
    }

    /**
     * Can be null for non leaves.
     *
     * @return
     */
    public BaseDescription getDescription() {
	return desc;
    }

    public boolean isDeleted() {
	return isDeleted;
    }

    @Override
    public String toString() {
	return name;
    }

    public RegistrySpace getSpace() {
	return space;
    }
}
