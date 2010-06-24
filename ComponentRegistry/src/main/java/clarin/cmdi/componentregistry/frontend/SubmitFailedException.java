package clarin.cmdi.componentregistry.frontend;

public class SubmitFailedException extends Exception {

    public SubmitFailedException(String message) {
        super(message);
    }

    public SubmitFailedException(Throwable cause) {
        super(cause);
    }
    public SubmitFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
