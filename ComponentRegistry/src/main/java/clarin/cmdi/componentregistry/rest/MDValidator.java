package clarin.cmdi.componentregistry.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.components.CMDComponentType;
import clarin.cmdi.componentregistry.model.AbstractDescription;

public class MDValidator implements Validator {

    static final String ISPROFILE_NOT_SET_ERROR = "'isProfile' attribute is obligated for this registry please specify it in the xml.";
    static final String MISMATCH_ERROR = "Cannot register component as a profile or vica versa.";
    static final String COMPONENT_NOT_REGISTERED_ERROR = "referenced component is not registered or does not have a correct componentId : ";
    static final String PARSE_ERROR = "Error in validation input file. Error is: ";

    private List<String> errorMessages = new ArrayList<String>();
    private CMDComponentSpec spec = null;
    private final InputStream input;
    private final AbstractDescription description;
    private final ComponentRegistry registry;

    /**
     * 
     * @param input In order to validate the input is consumed. So use @see getCMDComponentSpec to get the parsed CMDComponentSpec.
     * @param desc
     */
    public MDValidator(InputStream input, AbstractDescription description, ComponentRegistry registry) {
        this.input = input;
        this.description = description;
        this.registry = registry;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public boolean validate() {
        try {
            spec = MDMarshaller.unmarshal(CMDComponentSpec.class, input, MDMarshaller.getCMDComponentSchema());
            if (spec.isIsProfile() == null) {
                errorMessages.add(ISPROFILE_NOT_SET_ERROR);
            } else if (spec.isIsProfile().booleanValue() != description.isProfile()) {
                errorMessages.add(MISMATCH_ERROR);
            }
        } catch (JAXBException e) {
            errorMessages.add(PARSE_ERROR + e);
        }
        if (errorMessages.isEmpty()) {
            validateComponents(spec.getCMDComponent());
        }
        return errorMessages.isEmpty();
    }


    private void validateComponents(List<CMDComponentType> cmdComponents) {
        for (CMDComponentType cmdComponentType : cmdComponents) {
            validateDescribedComponents(cmdComponentType);
            validateComponents(cmdComponentType.getCMDComponent());//Recursion
        }
    }

    private void validateDescribedComponents(CMDComponentType cmdComponentType) {
        if (isDefinedInSeparateFile(cmdComponentType)) {
            String id = cmdComponentType.getComponentId();
            CMDComponentSpec registeredComponent = registry.getMDComponent(id);
            if (registeredComponent == null) {
                errorMessages.add(COMPONENT_NOT_REGISTERED_ERROR + cmdComponentType.getComponentId());
            }
        }
    }

    private boolean isDefinedInSeparateFile(CMDComponentType cmdComponentType) {
        return cmdComponentType.getName() == null;
    }

    public CMDComponentSpec getCMDComponentSpec() {
        return spec;
    }

}
