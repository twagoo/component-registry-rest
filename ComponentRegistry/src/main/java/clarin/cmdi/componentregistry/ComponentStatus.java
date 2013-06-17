package clarin.cmdi.componentregistry;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public enum ComponentStatus {

    /**
     * Private mode, editable non-public
     */
    PRIVATE(false, true),
    /**
     * Development mode, editable and publicly visible
     */
    DEVELOPMENT(false, true),
    /**
     * Published mode, non-editable and publicly visible
     */
    PUBLISHED(true, false),
    /**
     * Deprecated mode, non-editable and not publicly listed
     */
    DEPRECATED(false, false);
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
