package clarin.cmdi.componentregistry;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class OwnerUser implements Owner {

    private final Number id;

    public OwnerUser(Number id) {
	this.id = id;
    }

    @Override
    public Number getId() {
	return id;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final OwnerUser other = (OwnerUser) obj;
	if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
	return hash;
    }
}
