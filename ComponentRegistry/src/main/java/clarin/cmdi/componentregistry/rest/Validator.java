package clarin.cmdi.componentregistry.rest;

public interface Validator {

    
    /**
     * 
     * @return the errormessage that might have been created by the validate().
     */
    public String getErrorMessage();

    /**
     * Validate and create an error message when something is wrong.
     */
    public boolean validate();

}