package clarin.cmdi.componentregistry.model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public enum ComponentStatus {

    DEVELOPMENT("development"),
    PRODUCTION("production"),
    DEPRECATED("deprecated");

    private final String stringValue;

    ComponentStatus(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
