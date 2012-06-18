package clarin.cmdi.componentregistry;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public enum ComponentStatus {

    DEVELOPMENT(true, true),
    PUBLIC(true, false);
    // TODO: Add status types
    //    PRIVATE(false, true),
    //    DEPRECATED(true, false);
    final boolean publiclyListable;
    final boolean editable;

    private ComponentStatus(boolean publiclyListable, boolean editable) {
	this.publiclyListable = publiclyListable;
	this.editable = editable;
    }

    public boolean isEditable() {
	return editable;
    }

    public boolean isPubliclyListable() {
	return publiclyListable;
    }
}
