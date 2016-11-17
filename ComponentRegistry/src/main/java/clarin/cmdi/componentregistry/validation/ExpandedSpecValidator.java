package clarin.cmdi.componentregistry.validation;

import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import clarin.cmdi.schema.cmd.ValidatorException;
import eu.clarin.cmdi.toolkit.CMDToolkit;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author twagoo
 */
public class ExpandedSpecValidator extends BaseValidator {

    private final ExpandedComponentSpecContainer expandedSpecFactory;
    private final MDMarshaller marshaller;
    private boolean preRegistrationMode = true;

    public ExpandedSpecValidator(MDMarshaller marshaller, ExpandedComponentSpecContainer expandedSpecFactory) {
        this.expandedSpecFactory = expandedSpecFactory;
        this.marshaller = marshaller;
    }

    @Override
    public boolean validate() throws ValidationException, UserUnauthorizedException {
        final ComponentSpec spec;
        try {
            spec = expandedSpecFactory.getExpandedSpec();
        } catch (ComponentRegistryException ex) {
            throw new ValidationException("Error while getting expanded spec for validation", ex);
        }
        final String expandedSpecXml = marshaller.marshalToString(spec);
        final StreamSource source = new StreamSource(new StringReader(expandedSpecXml));

        //prepare validator runner
        final String schematronPhase = preRegistrationMode
                ? CMDToolkit.SCHEMATRON_PHASE_CMD_COMPONENT_PRE_REGISTRATION
                : CMDToolkit.SCHEMATRON_PHASE_CMD_COMPONENT_POST_REGISTRATION;
        final CMDValidateRunner validatorRunner = new CMDValidateRunner(source, schematronPhase) {
            @Override
            protected void handleError(String text) {
                //collect error messages form validator
                addErrorMessage(text);
            }
        };

        //perform validation
        try {
            return validatorRunner.validate();
        } catch (ValidatorException | IOException ex) {
            addErrorMessage("Validator error: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean runIfInvalid() {
        return false;
    }

    public void setPreRegistrationMode(boolean preRegistrationMode) {
        this.preRegistrationMode = preRegistrationMode;
    }

    public static interface ExpandedComponentSpecContainer {

        ComponentSpec getExpandedSpec() throws ComponentRegistryException;
    }

}
