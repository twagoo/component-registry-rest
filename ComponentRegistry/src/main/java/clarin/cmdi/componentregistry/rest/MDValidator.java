package clarin.cmdi.componentregistry.rest;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;

public class MDValidator implements Validator {

    private String errorMessage = null;
    private CMDComponentSpec spec = null;
    private final InputStream input;

    /**
     * 
     * @param input In order to validate the input is consumed. So use @see getCMDComponentSpec to get the parsed CMDComponentSpec.
     */
    public MDValidator(InputStream input) {
        this.input = input;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean validate() {
        try {
            spec = MDMarshaller.unmarshal(CMDComponentSpec.class, input, MDMarshaller.getCMDComponentSchema());
        } catch (JAXBException e) {
            errorMessage = "Error in validation input file. Error is: " + e;
        }
        return errorMessage == null;
    }

    public CMDComponentSpec getCMDComponentSpec() {
        return spec;
    }

}
