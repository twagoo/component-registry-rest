package clarin.cmdi.componentregistry.validation;

import clarin.cmdi.componentregistry.ComponentRegistryResourceResolver;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.schema.cmd.Validator;
import clarin.cmdi.schema.cmd.ValidatorException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.transform.stream.StreamSource;

/**
 * "Bridge" to the CMDValidate validator for component specifications
 *
 * @see https://github.com/clarin-eric/cmd-validate
 * @author twagoo
 */
public abstract class ValidatorRunner {

    static final String UNKNOWN_VALIDATION_ERROR = "Unknown validation error";

    private final StreamSource source;
    private final String schematronPhase;
    private final URL componentSchemaUrl;

    public ValidatorRunner(StreamSource source, String schematronPhase) {
        this.source = source;
        this.schematronPhase = schematronPhase;
        try {
            this.componentSchemaUrl = new URL(Configuration.getInstance().getGeneralComponentSchema());
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Invalid general component schema URL", ex);
        }
    }

    public boolean validate() throws ValidatorException, IOException {
        final Validator validator = new Validator(componentSchemaUrl);
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
