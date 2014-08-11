package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.UserUnauthorizedException;
import java.util.List;

public interface Validator {

    
    /**
     * 
     * @return the errormessage that might have been created by the validate().
     */
    public List<String> getErrorMessages();

    /**
     * Validate and create an error message when something is wrong.
     */
    public boolean validate()  throws UserUnauthorizedException;

}