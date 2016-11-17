package clarin.cmdi.componentregistry.validation;

import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import java.util.List;

public interface Validator {

    
    /**
     * 
     * @return the errormessage that might have been created by the validate().
     */
    public List<String> getErrorMessages();

    /**
     * Validate and create an error message when something is wrong.
     * @return whether validation was successful
     * @throws clarin.cmdi.componentregistry.UserUnauthorizedException
     */
    public boolean validate()  throws ValidationException, UserUnauthorizedException;
 
    /**
     * 
     * @return whether this validator should be executed if validation errors have been found earlier in the validation chain
     */
    public boolean runIfInvalid();

}