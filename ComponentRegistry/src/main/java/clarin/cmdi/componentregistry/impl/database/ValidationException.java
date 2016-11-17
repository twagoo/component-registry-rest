package clarin.cmdi.componentregistry.impl.database;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 *
 */
public class ValidationException extends RuntimeException{

    public ValidationException(String message) {
	super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
