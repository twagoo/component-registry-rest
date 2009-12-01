package clarin.cmdi.componentregistry.rest;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;

public class MDValidator implements Validator {

    static final String ISPROFILE_NOT_SET_ERROR = "'isProfile' attribute is obligated for this registry please specify it in the xml.";
    static final String MISMATCH_ERROR = "Cannot register component as a profile or vica versa.";
    private String errorMessage = null;
    private CMDComponentSpec spec = null;
    private final InputStream input;
    private final AbstractDescription description;

    /**
     * 
     * @param input In order to validate the input is consumed. So use @see getCMDComponentSpec to get the parsed CMDComponentSpec.
     * @param desc 
     */
    public MDValidator(InputStream input, AbstractDescription description) {
        this.input = input;
        this.description = description;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean validate() {
        try {
            spec = MDMarshaller.unmarshal(CMDComponentSpec.class, input, MDMarshaller.getCMDComponentSchema());
            if (spec.isIsProfile() == null) {
                errorMessage = ISPROFILE_NOT_SET_ERROR;
            } else if (spec.isIsProfile().booleanValue() != description.isProfile()){
                errorMessage = MISMATCH_ERROR;
            }
        } catch (JAXBException e) {
            errorMessage = "Error in validation input file. Error is: " + e;
        }
        return errorMessage == null;
    }

    public CMDComponentSpec getCMDComponentSpec() {
        return spec;
    }

}
