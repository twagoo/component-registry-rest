package clarin.cmdi.componentregistry;

import clarin.cmdi.schema.cmd.Validator;
import clarin.cmdi.schema.cmd.ValidatorException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author twagoo
 */
public abstract class ValidatorRunner {

    static final String UNKNOWN_VALIDATION_ERROR = "Unknown validation error";

    private final StreamSource source;
    private final String schematronPhase;

    public ValidatorRunner(StreamSource source, String schematronPhase) {
        this.source = source;
        this.schematronPhase = schematronPhase;
    }

    public boolean validate() throws MalformedURLException, ValidatorException, IOException {
        clarin.cmdi.schema.cmd.Validator validator = new clarin.cmdi.schema.cmd.Validator(new URL(Configuration.getInstance().getGeneralComponentSchema()));
        validator.setSchematronPhase(this.schematronPhase);
        validator.setResourceResolver(new ComponentRegistryResourceResolver());
        
        if (validator.validateProfile(source)) {
            return true;
        } else {
            final List<Validator.Message> validatorMessages = validator.getMessages();
            if (validatorMessages.size() > 0) {
                for (Validator.Message message : validatorMessages) {
                    handleError(message.getText());
                }
            } else {
                handleError(UNKNOWN_VALIDATION_ERROR);
            }
            return false;
        }
    }

    protected abstract void handleError(String text);
}
