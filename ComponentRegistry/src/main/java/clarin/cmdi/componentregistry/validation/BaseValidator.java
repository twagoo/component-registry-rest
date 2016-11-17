package clarin.cmdi.componentregistry.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of validator that keeps track of error messages in an
 * {@link ArrayList}
 *
 * @author twagoo
 */
public abstract class BaseValidator implements Validator {

    private final List<String> errorMessages = new ArrayList<>();

    protected void addErrorMessage(String message) {
        this.errorMessages.add(message);
    }

    @Override
    public List<String> getErrorMessages() {
        return this.errorMessages;
    }

    public boolean hasErrors() {
        return !this.errorMessages.isEmpty();
    }

}
